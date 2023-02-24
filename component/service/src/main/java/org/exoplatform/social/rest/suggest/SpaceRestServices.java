package org.exoplatform.social.rest.suggest;

import java.util.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.RuntimeDelegate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.json.*;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.deprecation.DeprecatedAPI;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;


@Path("/homepage/intranet/spaces/")
@Produces("application/json")
@Deprecated
public class SpaceRestServices implements ResourceContainer {

    private static final Log LOG = ExoLogger.getLogger(SpaceRestServices.class);

    private static final CacheControl cacheControl;
    private static final String SPACE_NAME = "name";
    private static final String SPACE_ID = "spaceId";
    private static final String SPACE_DISPLAY_NAME = "displayName";
    private static final String SPACE_URL = "spaceUrl";
    private static final String SPACE_AVATAR_URL = "spaceAvatarUrl";
    private static final String SPACE_REGISTRATION = "registration";
    private static final String SPACE_MEMBERS = "members";
    private static final String SPACE_PRIVACY = "privacy";
    private static final String NUMBER = "number";
    private static final String SPACE_CREATED_DATE = "createdDate";
    private static final String ITEMS ="items";
    static {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
        cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
    }

    // The owner of the rest component
    private final ExoContainer container;

    public SpaceRestServices(ExoContainerContext ctx) {
        this.container = ctx.getContainer();
    }

    @GET
    @Path("suggestions")
    @DeprecatedAPI("Use SpaceRestResourcesV1.getSpaces instead")
    public Response getSuggestions(@Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonGlobal = new JSONObject();
            
            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
            ListAccess<Space> suggestedSpacesLA = spaceService.getPublicSpacesWithListAccess(userId);

            // new create system with no spaces
            int size = suggestedSpacesLA.getSize();
            if (size == 0) {
              jsonGlobal.put(ITEMS,jsonArray);
              jsonGlobal.put("noConnections", 0);
              jsonGlobal.put("username", userId);
              return Response.ok(jsonGlobal.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
            }
            
            IdentityManager identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
            RelationshipManager relationshipManager = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
            ListAccess<Identity> connectionsLA = relationshipManager.getConnections(identity);
            
            final Map<Space, Integer> spacesWithMemberNum = new HashMap<Space, Integer>();
            int maxConnectionsToLoad = 100;
            int maxSpacesToLoad = 50;
            int maxSuggestions = 10;
            int totalConnections = connectionsLA.getSize();
            Random random = new Random();
            Identity[] connections;
            if (totalConnections > maxConnectionsToLoad) {
               int startIndex = random.nextInt(totalConnections - maxConnectionsToLoad);
               connections = connectionsLA.load(startIndex, maxConnectionsToLoad);
            } else {
               connections = connectionsLA.load(0, totalConnections);
            }
            Space[] suggestedSpaces;
            if (size > maxSpacesToLoad) {
               int startIndex = random.nextInt(size - maxSpacesToLoad);
               suggestedSpaces = suggestedSpacesLA.load(startIndex, maxSpacesToLoad);
            } else {
               suggestedSpaces = suggestedSpacesLA.load(0, size);
            }
            for (Space space : suggestedSpaces) {
              // Skip space when the current user is already a member of
              if(spaceService.isMember(space, identity.getRemoteId())) {
                  continue;
              }
              for (Identity connector : connections) {
                //
                if (Space.HIDDEN.equals(space.getVisibility()))
                  continue;
                if (Space.CLOSED.equals(space.getRegistration()))
                  continue;
                if (!spaceService.isMember(space, connector.getRemoteId())) 
                  continue;
                if (!spaceService.isIgnored(space, connector.getRemoteId()))
                  continue;
                Integer value = spacesWithMemberNum.get(space);
                
                if (value == null) {
                  value = new Integer(1);
                } else {
                  value = new Integer(value.intValue() + 1);
                }
                spacesWithMemberNum.put(space, value);
              }
            }
            
            if (!spacesWithMemberNum.isEmpty()) {
              NavigableMap<Integer, List<Space>> groupByCommonConnections = new TreeMap<Integer, List<Space>>();
              // This for loop allows to group the suggestions by total amount of common connections
              for (Space space : spacesWithMemberNum.keySet()) {
                Integer commonSpaces = spacesWithMemberNum.get(space);
                List<Space> spaces = groupByCommonConnections.get(commonSpaces);
                if (spaces == null) {
                  spaces = new ArrayList<Space>();
                  groupByCommonConnections.put(commonSpaces, spaces);
                }
                spaces.add(space);
              }
              int suggestionLeft = maxSuggestions;
              // We iterate over the suggestions starting from the suggestions with the highest amount common
              // connections
              main: for (Integer key : groupByCommonConnections.descendingKeySet()) {
                List<Space> spaces = groupByCommonConnections.get(key);
                for (Space space : spaces) {
                  JSONObject json = buildJSONObject(space, key);
                  jsonArray.put(json);
                  // We stop once we have enough suggestions
                  if (--suggestionLeft == 0)
                    break main;
                }
              }
            } else {
              // Propose the last spaces
              List<Space> lastSpaces = spaceService.getLastSpaces(10);
              for (Space space : lastSpaces) {
                if (Space.HIDDEN.equals(space.getVisibility()))
                  continue;
                if (Space.CLOSED.equals(space.getRegistration()))
                  continue;
                if (spaceService.isMember(space, identity.getRemoteId())) 
                  continue;
                if (spaceService.isPendingUser(space, identity.getRemoteId())) 
                   continue;
                if (spaceService.isInvitedUser(space, identity.getRemoteId())) 
                  continue;
                if (spaceService.isIgnored(space, identity.getRemoteId()))
                  continue;
                JSONObject json = buildJSONObject(space, 0);
                json.put("username", userId);
                jsonArray.put(json);
              }
            }
            
            jsonGlobal.put(ITEMS,jsonArray);
            jsonGlobal.put("noConnections", spacesWithMemberNum.size());
            jsonGlobal.put("username", userId);
            return Response.ok(jsonGlobal.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();

        } catch (Exception e) {
            LOG.error("Error in space invitation rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }

    private JSONObject buildJSONObject(Space space, int k) throws JSONException {
      String avatar = space.getAvatarUrl();
      if (avatar == null) {
          avatar = LinkProvider.SPACE_DEFAULT_AVATAR_URL;
      }
      
      String spaceType = "";
      if (space.getRegistration() == null || space.getRegistration().equals(Space.OPEN)) {
          spaceType = "Public";
      } else {
          spaceType = "Private";
      }
      
      JSONObject json = new JSONObject();
      json.put(SPACE_NAME, space.getPrettyName());
      json.put(SPACE_ID, space.getId());
      json.put(SPACE_DISPLAY_NAME, space.getDisplayName());
      json.put(SPACE_URL, space.getUrl());
      json.put(SPACE_AVATAR_URL, avatar);
      json.put(SPACE_REGISTRATION, space.getRegistration());
      json.put(SPACE_MEMBERS, space.getMembers() == null ? 0 : space.getMembers().length);
      json.put(SPACE_PRIVACY, spaceType);
      json.put(NUMBER, k);
      json.put(SPACE_CREATED_DATE, space.getCreatedTime());
      return json;
    }

    @GET
    @Path("accept/{spaceName}")
    @DeprecatedAPI("Use SpaceMembershipRestResourcesV1.updateSpaceMembershipById instead")
    public Response accept(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);

            if (spaceService.isInvitedUser(spaceService.getSpaceById(spaceName), userId))
                spaceService.addMember(spaceService.getSpaceById(spaceName), userId);

            return Response.ok("{}", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();

        } catch (Exception e) {
            LOG.error("Error in space accept rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }

    @GET
    @Path("deny/{spaceName}")
    @DeprecatedAPI("Use SpaceMembershipRestResourcesV1.updateSpaceMembershipById instead")
    public Response deny(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
            spaceService.removeInvitedUser(spaceService.getSpaceById(spaceName), userId);

            return Response.ok("{}", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            LOG.error("Error in space deny rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }

    @GET
    @Path("request/{spaceName}")
    @DeprecatedAPI("Use SpaceMembershipRestResourcesV1.updateSpaceMembershipById instead")
    public Response request(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
            spaceService.addPendingUser(spaceService.getSpaceById(spaceName), userId);

            return Response.ok("{}", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            LOG.error("Error in space deny rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }

    @DELETE
    @Path("leave/{spaceId}")
    @RolesAllowed("users")
    @Operation(summary = "A user leaves a space", method = "POST", description = "This can only be done by the logged in user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request fulfilled"),
        @ApiResponse(responseCode = "500", description = "Internal server error"),
        @ApiResponse(responseCode = "404", description = "Space not found") })
    @DeprecatedAPI("Use SpaceMembershipRestResourcesV1.updateSpaceMembershipById instead")
    public Response leave(@Parameter(description = "Space technical identifier", required = true) @PathParam("spaceId") String spaceId,
                          @Context SecurityContext sc,
                          @Context UriInfo uriInfo) {
      try {
        String userId = getUserId(sc, uriInfo);
        if (userId == null) {
          return Response.status(HTTPStatus.INTERNAL_ERROR).build();
        }
  
        SpaceService spaceService = container.getComponentInstanceOfType(SpaceService.class);
        Space space = spaceService.getSpaceById(spaceId);
        if (space == null) {
          return Response.status(HTTPStatus.NOT_FOUND).build();
        }
        if (spaceService.isMember(space, userId)) {
          spaceService.removeMember(space, userId);
        }
        return Response.ok().build();
      } catch (Exception e) {
        LOG.error("Error in space deny rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
    }

    @DELETE
    @Path("cancel/{spaceId}")
    @RolesAllowed("users")
    @Operation(summary = "A user cancels his request to join a space", method = "POST", description = "This can only be done by the logged in user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Request fulfilled"),
        @ApiResponse(responseCode = "500", description = "Internal server error"),
        @ApiResponse(responseCode = "404", description = "Space not found") })
    @DeprecatedAPI("Use SpaceMembershipRestResourcesV1.updateSpaceMembershipById instead")
    public Response cancel(@Parameter(description = "Space technical identifier", required = true) @PathParam("spaceId") String spaceId,
                          @Context SecurityContext sc,
                          @Context UriInfo uriInfo) {
      try {
        String userId = getUserId(sc, uriInfo);
        if (userId == null) {
          return Response.status(HTTPStatus.INTERNAL_ERROR).build();
        }

        SpaceService spaceService = container.getComponentInstanceOfType(SpaceService.class);
        Space space = spaceService.getSpaceById(spaceId);
        if (space == null) {
          return Response.status(HTTPStatus.NOT_FOUND).build();
        }

        if (spaceService.isPendingUser(space, userId)) {
          spaceService.removePendingUser(space, userId);
        }

        return Response.ok().build();
      } catch (Exception e) {
        LOG.error("Error in space deny rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
    }

    @GET
    @Path("join/{spaceName}")
    @DeprecatedAPI("Use SpaceMembershipRestResourcesV1.addSpacesMemberships instead")
    public Response join(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
            if (spaceService.getSpaceById(spaceName).getRegistration().equals("open"))
                spaceService.addMember(spaceService.getSpaceById(spaceName), userId);


            return Response.ok("{}", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            LOG.error("Error in space deny rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }

    @GET
    @Path("myspaces")
    @DeprecatedAPI("Use SpaceRestResourcesV1.getSpaces instead")
    public Response request(@Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {

            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
            List<Space> mySpaces = spaceService.getAccessibleSpaces(userId);

            JSONArray jsonArray = new JSONArray();

            for (Space space : mySpaces) {
                JSONObject json = new JSONObject();
                json.put(SPACE_NAME, space.getName());
                json.put(SPACE_ID, space.getId());
                json.put(SPACE_DISPLAY_NAME, space.getDisplayName());
                json.put(SPACE_URL, space.getUrl());
                json.put(SPACE_MEMBERS, space.getMembers().length);
                jsonArray.put(json);
            }

            return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();

        } catch (Exception e) {
            LOG.error("Error in space deny rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }

    }

    @GET
    @Path("public")
    @DeprecatedAPI("Use SpaceRestResourcesV1.getSpaces instead")
    public Response getPublicSpaces(@Context SecurityContext sc, @Context UriInfo uriInfo) {

        try {
            String userId = getUserId(sc, uriInfo);
            if (userId == null) {
                return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
            }

            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
            ListAccess<Space> publicSpaces = spaceService.getPublicSpacesWithListAccess(userId);

            JSONArray jsonArray = new JSONArray();

            Space[] spaces = publicSpaces.load(0, publicSpaces.getSize());
            if (spaces != null && spaces.length > 0) {
                for (Space space : spaces) {

                    if (space.getVisibility().equals(Space.HIDDEN))
                        continue;
                    if (space.getRegistration().equals(Space.CLOSED))
                        continue;

                    JSONObject json = new JSONObject();
                    json.put(SPACE_NAME, space.getName());
                    json.put(SPACE_DISPLAY_NAME, space.getDisplayName());
                    json.put(SPACE_ID, space.getId());
                    jsonArray.put(json);
                }
            }

            return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        } catch (Exception e) {
            LOG.error("Error in space invitation rest service: " + e.getMessage(), e);
            return Response.ok("error").cacheControl(cacheControl).build();
        }
    }


    private String getUserId(SecurityContext sc, UriInfo uriInfo) {
        try {
            return sc.getUserPrincipal().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
