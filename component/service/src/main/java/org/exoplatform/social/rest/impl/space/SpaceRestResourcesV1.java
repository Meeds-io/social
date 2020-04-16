/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.rest.impl.space;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.*;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;

import io.swagger.annotations.*;

@Path(VersionResources.VERSION_ONE + "/social/spaces")
@Api(tags = VersionResources.VERSION_ONE + "/social/spaces", value = VersionResources.VERSION_ONE + "/social/spaces", description = "Operations on spaces with their activities and users")
public class SpaceRestResourcesV1 implements SpaceRestResources {

  private static final String SPACE_FILTER_TYPE_ALL = "all";
  
  private static final String SPACE_FILTER_TYPE_MEMBER = "member";

  private static final String SPACE_FILTER_TYPE_MANAGER = "manager";

  private static final String SPACE_FILTER_TYPE_PENDING = "pending";

  private static final String SPACE_FILTER_TYPE_INVITED = "invited";

  private static final String SPACE_FILTER_TYPE_REQUESTS = "requests";

  private IdentityManager identityManager;
  private static final Log LOG = ExoLogger.getLogger(SpaceRestResourcesV1.class);


  public SpaceRestResourcesV1(IdentityManager identityManager) {
    this.identityManager = identityManager;
  }

  /**
   * {@inheritDoc}
   */
  @RolesAllowed("users")
  @ApiOperation(value = "Gets spaces of user",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns a list of spaces switch request paramters")
  @ApiResponses(value = {
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getSpaces(@Context UriInfo uriInfo,
                            @Context Request request,
                            @ApiParam(value = "Space name search information", required = false) @QueryParam("q") String q,
                            @ApiParam(value = "Type of spaces to retrieve: all, userSpaces, invited, pending or requests", defaultValue = SPACE_FILTER_TYPE_ALL, required = false) @QueryParam("filterType") String filterType,
                            @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                            @ApiParam(value = "Limit, if equals to 0, it will not retrieve spaces", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                            @ApiParam(value = "Sort", required = false) @QueryParam("sort") String sort,
                            @ApiParam(value = "Order", required = false) @QueryParam("order") String order,
                            @ApiParam(value = "Returning the number of spaces found or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                            @ApiParam(value = "Asking for a full representation of a specific subresource, ex: members or managers", required = false) @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit >= 0 ? limit : RestUtils.getLimit(uriInfo);

    if (StringUtils.isBlank(filterType)) {
      filterType = SPACE_FILTER_TYPE_ALL;
    }

    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);

    ListAccess<Space> listAccess = null;
    SpaceFilter spaceFilter = new SpaceFilter();

    if (q != null) {
      spaceFilter.setSpaceNameSearchCondition(q);
    }
    
    if (StringUtils.isNotBlank(sort)) {
        SortBy sortBy = Sorting.SortBy.valueOf(sort.toUpperCase());
        OrderBy orderBy = Sorting.OrderBy.ASC;
        if (StringUtils.isNotBlank(order)) {
          orderBy = Sorting.OrderBy.valueOf(order.toUpperCase());
        }
        spaceFilter.setSorting(new Sorting(sortBy, orderBy));
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_ALL, filterType)) {
      listAccess = spaceService.getVisibleSpacesWithListAccess(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_MEMBER, filterType)) {
      listAccess = spaceService.getMemberSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_MANAGER, filterType)) {
      listAccess = spaceService.getManagerSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_PENDING, filterType)) {
      listAccess = spaceService.getPendingSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_INVITED, filterType)) {
      listAccess = spaceService.getInvitedSpacesByFilter(authenticatedUser, spaceFilter);
    } else if (StringUtils.equalsIgnoreCase(SPACE_FILTER_TYPE_REQUESTS, filterType)) {
      listAccess = spaceService.getPendingSpaceRequestsToManage(authenticatedUser);
    } else {
      return Response.status(400).entity("Unrecognized space filter type").build();
    }

    List<DataEntity> spaceInfos = new ArrayList<>();
    GroupSpaceBindingService spaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
    if (limit > 0) {
      for (Space space : listAccess.load(offset, limit)) {
        space.setHasBindings(spaceBindingService.isBoundSpace(space.getId()));
        
        SpaceEntity spaceInfo = EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand);
        //
        spaceInfos.add(spaceInfo.getDataEntity()); 
      }
    }
    CollectionEntity collectionSpace = new CollectionEntity(spaceInfos, EntityBuilder.SPACES_TYPE, offset, limit);
    if (returnSize) {
      collectionSpace.setSize(listAccess.getSize());
    }
    
    EntityTag eTag = null;
    if (collectionSpace != null) {
      eTag = new EntityTag(Integer.toString(collectionSpace.hashCode()));
    }
    //
    Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
    if (builder == null) {
      builder = EntityBuilder.getResponseBuilder(collectionSpace, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
      builder.tag(eTag);
    }

    CacheControl cc = new CacheControl();
    builder.cacheControl(cc);

    return builder.build();
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Creates a space",
                httpMethod = "POST",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response createSpace(@Context UriInfo uriInfo,
                              @ApiParam(value = "Asking for a full representation of a specific subresource, ex: members or managers", required = false) @QueryParam("expand") String expand,
                              @ApiParam(value = "Space object to be created, ex:<br />" +
                              		              "{<br />\"displayName\": \"My space\"," +
                                                "<br />\"description\": \"This is my space\"," +
                                                "<br />\"groupId\": \"/spaces/my_space\"," +
                                                "<br />\"visibility\": \"private\"," +
                                                "<br />\"subscription\": \"validation\"<br />}" 
                                                , required = true) SpaceEntity model) throws Exception {
    if (model == null || model.getDisplayName() == null || model.getDisplayName().length() == 0 || model.getDisplayName().length() > 200 || !SpaceUtils.isValidSpaceName(model.getDisplayName())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    // validate the display name
    if (spaceService.getSpaceByDisplayName(model.getDisplayName()) != null) {
      throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = new Space();
    fillSpaceFromModel(space, model);
    space.setEditor(authenticatedUser);

    String[] managers = new String[] { authenticatedUser };
    String[] members = new String[] { authenticatedUser };
    space.setManagers(managers);
    space.setMembers(members);

    //
    spaceService.createSpace(space, authenticatedUser, model.getInvitedMembers());

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific space by id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the space in the following cases: <br/><ul><li>the authenticated user is a member of the space</li><li>the space is \"public\"</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getSpaceById(@Context UriInfo uriInfo,
                               @ApiParam(value = "Space id", required = true) @PathParam("id") String id,
                               @ApiParam(value = "Asking for a full representation of a specific subresource, ex: members or managers", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   *
   * @param uriInfo
   * @param id
   * @return
   * @throws IOException
   */
  @GET
  @Path("{id}/avatar")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a space avatar by pretty name",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 404, message = "Resource not found")})
  public Response getSpaceAvatarById(@Context UriInfo uriInfo,
                                     @Context Request request,
                                     @ApiParam(value = "Space pretty name", required = true) @PathParam("id") String id) throws IOException {
  
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    
    Space space = spaceService.getSpaceByPrettyName(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
    //
    Profile profile = identity.getProfile();
    Long lastUpdated = null;
    if (profile != null) {
      lastUpdated = profile.getAvatarLastUpdated();
    }
    EntityTag eTag = null;
    if (lastUpdated != null) {
      eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
    }
    //
    Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
    if (builder == null) {
      InputStream stream = identityManager.getAvatarInputStream(identity);
      if (stream == null) {
        stream = getDefaultAvatarBuilder();
        builder = Response.ok(stream, "image/png");
      } else {
        builder = Response.ok(stream, "image/png");
        builder.tag(eTag);
      }
    }
    CacheControl cc = new CacheControl();
    cc.setMaxAge(86400);
    builder.cacheControl(cc);
    return builder.cacheControl(cc).build();
  }

  /**
   *
   * @param uriInfo
   * @param id
   * @return
   * @throws IOException
   */
  @GET
  @Path("{id}/banner")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a space banner by id",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 404, message = "Resource not found")})
  public Response getSpaceBannerById(@Context UriInfo uriInfo,
                                     @Context Request request,
                                     @ApiParam(value = "Space id", required = true) @PathParam("id") String id) throws IOException {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);

    Space space = spaceService.getSpaceByPrettyName(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
    //
    Profile profile = identity.getProfile();
    Long lastUpdated = null;
    if (profile != null) {
      lastUpdated = profile.getBannerLastUpdated();
    }
    EntityTag eTag = null;
    if (lastUpdated != null) {
      eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
    }
    //
    Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
    if (builder == null) {
      InputStream stream = identityManager.getBannerInputStream(identity);
      if (stream == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      /* As recommended in the the RFC1341 (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html),
      we set the banner content-type to "image/png". So, its data  would be recognized as "image" by the user-agent.
     */
      builder = Response.ok(stream, "image/png");
      builder.tag(eTag);
    }
    CacheControl cc = new CacheControl();
    cc.setMaxAge(86400);
    builder.cacheControl(cc);
    return builder.cacheControl(cc).build();
  }

  /**
   * {@inheritDoc}
   */
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific space by id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the space in the following cases: <br/><ul><li>the authenticated user is a manager of the space</li><li>the authenticated user is the owner of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response updateSpaceById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "Space id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource, ex: members or managers", required = false) @QueryParam("expand") String expand,
                                  @ApiParam(value = "Space object to be updated", required = true) SpaceEntity model) throws Exception {
    
    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (space.getDisplayName().length() > 200 || !SpaceUtils.isValidSpaceName(space.getDisplayName())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    if (StringUtils.isNotBlank(model.getDisplayName()) && !StringUtils.equals(space.getDisplayName(), model.getDisplayName())) {
      spaceService.renameSpace(authenticatedUser, space, model.getDisplayName());
    }

    fillSpaceFromModel(space, model);
    space.setEditor(authenticatedUser);
    spaceService.updateSpace(space, model.getInvitedMembers());

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")

  @ApiOperation(value = "Deletes a specific space by id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the space in the following cases: <br/><ul><li>the authenticated user is a manager of the space</li><li>the authenticated user is the owner of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteSpaceById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "Space id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    spaceService.deleteSpace(space);
    
    return Response.ok().build();
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/users")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets users of a specific space",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns a list of users if the authenticated user is a member or manager of the space or a spaces super manager.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getSpaceMembers(@Context UriInfo uriInfo,
                                  @Context Request request,
                                  @ApiParam(value = "Space id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Role of the target user in this space, ex: manager", required = false, defaultValue = "0") @QueryParam("role") String role,
                                  @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                  @ApiParam(value = "Returning the number of users or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    String[] users = (role != null && role.equals("manager")) ? space.getManagers() : space.getMembers();
    int size = users.length;
    //
    if (limit > 0) {
      users = Arrays.copyOfRange(users, offset > size - 1 ? size - 1 : offset, (offset + limit > size) ? size : (offset + limit));
    }
    List<DataEntity> profileInfos = EntityBuilder.buildEntityProfiles(users, uriInfo.getPath(), expand);
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(size);
    }
    EntityTag eTag = null;
    eTag = new EntityTag(Integer.toString(collectionUser.hashCode()));
    //
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = EntityBuilder.getResponseBuilder(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
      builder.tag(eTag);
    }

    CacheControl cc = new CacheControl();
    builder.cacheControl(cc);

    return builder.build();
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/activities")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets space activities by space id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the space's activities if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getSpaceActivitiesById(@Context UriInfo uriInfo,
      @ApiParam(value = "Space id", required = true) @PathParam("id") String id,
      @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
      @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
      @ApiParam(value = "Base time to load older activities (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("before") String before,
      @ApiParam(value = "Base time to load newer activities (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("after") String after,
      @ApiParam(value = "Returning the number of activities or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
      @ApiParam(value = "Asking for a full representation of a specific subresource, ex: comments or likes", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = CommonsUtils.getService(ActivityManager.class).getActivitiesOfSpaceWithListAccess(spaceIdentity);
    List<ExoSocialActivity> activities = null;
    if (after != null && RestUtils.getBaseTime(after) > 0) {
      activities = listAccess.loadNewer(RestUtils.getBaseTime(after), limit);
    } else if (before != null && RestUtils.getBaseTime(before) > 0) {
      activities = listAccess.loadOlder(RestUtils.getBaseTime(before), limit);
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    List<DataEntity> activityEntities = new ArrayList<DataEntity>();
    //
    BaseEntity as = new BaseEntity(spaceIdentity.getRemoteId());
    as.setProperty(RestProperties.TYPE, EntityBuilder.SPACE_ACTIVITY_TYPE);
    //
    for (ExoSocialActivity activity : activities) {
      ActivityEntity activityInfo = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
      activityInfo.setActivityStream(as.getDataEntity());
      //
      activityEntities.add(activityInfo.getDataEntity());
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE,  offset, limit);
    if(returnSize) {
      if (before != null || after != null) {
        collectionActivity.setSize(activities.size());   
      } else {
        collectionActivity.setSize(listAccess.getSize());
      }
    }
    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @POST
  @Path("{id}/activities")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Posts an activity to a specific space",
                httpMethod = "POST",
                response = Response.class,
                notes = "This posts the activity if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response postActivityOnSpace(@Context UriInfo uriInfo,
                                      @ApiParam(value = "Space id", required = true) @PathParam("id") String id,
                                      @ApiParam(value = "Asking for a full representation of a specific subresource, ex: comments or likes", required = false) @QueryParam("expand") String expand,
                                      @ApiParam(value = "Activity object to be created", required = true) ActivityRestIn model) throws Exception {
    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    Identity poster = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, false);
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(model.getTitle());
    activity.setBody(model.getBody());
    activity.setType(model.getType());
    activity.setUserId(poster.getId());
    activity.setTemplateParams(model.getTemplateParams());
    if(model.getFiles() != null) {
      activity.setFiles(model.getFiles()
              .stream()
              .map(fileModel -> new ActivityFile(fileModel.getId(),fileModel.getUploadId(), fileModel.getStorage(), fileModel.getDestinationFolder()))
              .collect(Collectors.toList()));
    }
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(spaceIdentity, activity);

    logMetrics(activity, space);

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  /**
   * Log metric about composer usage
   * @param activity The posted activity
   * @param space The space of the posted activity
   */
  private void logMetrics(ExoSocialActivity activity, Space space) {
    ExoFeatureService featureService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ExoFeatureService.class);
    if(!featureService.isActiveFeature("new-composer")) {
      return;
    }

    if(activity == null || space == null) {
      return;
    }

    String activityType;
    if(activity.getType() != null) {
      switch (activity.getType()) {
        case "files:spaces":
          activityType = "file";
          break;
        case "LINK_ACTIVITY":
          activityType = "link";
          break;
        default:
          activityType = "message";
          break;
      }
    } else {
      activityType = "message";
    }

    LOG.info("service=composer operation=post parameters=\"composer_type:new,activity_type:{},activity_id:{},space_id:{},user_id:{}\"",
            activityType,
            activity.getId(),
            space.getId(),
            activity.getPosterId());
  }

  private void fillSpaceFromModel(Space space, SpaceEntity model) {
    space.setPriority(Space.INTERMEDIATE_PRIORITY);

    if (StringUtils.isNotBlank(model.getDisplayName())) {
      space.setDisplayName(model.getDisplayName());

      if (StringUtils.isBlank(space.getPrettyName())) {
        space.setPrettyName(model.getDisplayName());
      }
    } else if (StringUtils.isNotBlank(model.getPrettyName())) {
      space.setPrettyName(model.getPrettyName());

      if (StringUtils.isBlank(space.getDisplayName())) {
        space.setDisplayName(model.getPrettyName());
      }
    }

    if (StringUtils.isNotBlank(model.getDescription())) {
      space.setDescription(StringEscapeUtils.escapeHtml(model.getDescription()));
    }

    if (StringUtils.isNotBlank(model.getTemplate())) {
      space.setTemplate(model.getTemplate());
    }

    if (StringUtils.equalsIgnoreCase(Space.HIDDEN, model.getVisibility())) {
      space.setVisibility(Space.HIDDEN);
    } else {
      space.setVisibility(Space.PRIVATE);
    }

    if (StringUtils.equalsIgnoreCase(Space.OPEN, model.getSubscription())) {
      space.setRegistration(Space.OPEN);
    } else if (StringUtils.equalsIgnoreCase(Space.CLOSE, model.getSubscription())) {
      space.setRegistration(Space.CLOSE);
    } else {
      space.setRegistration(Space.VALIDATION);
    }
  }

  /**
   * @param uriInfo
   * @param request
   * @param id
   * @param fileId
   * @return
   * @throws IOException
   */
  @GET
  @Path("{id}/files/{fileId}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets an activity file by its activity id and file id", httpMethod = "GET", response = Response.class, notes = "returns the associated activity file.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 404, message = "Resource not found") })
  public Response getSpaceActivityFileByFileId(@Context UriInfo uriInfo,
                                                       @Context Request request,
                                                       @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                                                       @ApiParam(value = "File id", required = true) @PathParam("fileId") String fileId) throws IOException {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Identity currentUser = CommonsUtils.getService(IdentityManager.class)
                                       .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    if (EntityBuilder.getActivityStream(activity, currentUser) == null
        && !Util.hasMentioned(activity, currentUser.getRemoteId())) { // current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    boolean hasFile=activityManager.getActivityFilesIds(activity).stream()
            .filter(fId -> fileId.equals(fId))
            .findAny()
            .isPresent();
    if (!hasFile) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    ActivityFile file = null;
    try {
      file = activityManager.getActivityFileById(Long.valueOf(fileId));
    } catch (Exception e) {
      LOG.error("An error occurred while retrieving activity file by id",e);
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (file == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    EntityTag eTag = null;
    eTag = new EntityTag(Integer.toString(((Long)file.getLastModified()).hashCode()));
    //
    Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
    if (builder == null) {
      InputStream stream = file.getInputStream();
      if (stream == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        builder = Response.ok(stream, "image/png");
        builder.tag(eTag);
      }
    }
    CacheControl cc = new CacheControl();
    cc.setMaxAge(86400);
    builder.cacheControl(cc);
    return builder.cacheControl(cc).build();
  }

  private InputStream getDefaultAvatarBuilder() {
    return PortalContainer.getInstance().getPortalContext().getResourceAsStream("/skin/images/system/SpaceAvtDefault.png");
  }

}
