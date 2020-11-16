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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.application.registry.Application;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.*;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.*;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

import io.swagger.annotations.*;

@Path(VersionResources.VERSION_ONE + "/social/spaces")
@Api(tags = VersionResources.VERSION_ONE + "/social/spaces", value = VersionResources.VERSION_ONE + "/social/spaces", description = "Operations on spaces with their activities and users")
public class SpaceRestResourcesV1 implements SpaceRestResources {

  private static final Log LOG = ExoLogger.getLogger(SpaceRestResourcesV1.class);

  private static final String SPACE_FILTER_TYPE_ALL = "all";
  
  private static final String SPACE_FILTER_TYPE_MEMBER = "member";

  private static final String SPACE_FILTER_TYPE_MANAGER = "manager";

  private static final String SPACE_FILTER_TYPE_PENDING = "pending";

  private static final String SPACE_FILTER_TYPE_INVITED = "invited";

  private static final String SPACE_FILTER_TYPE_REQUESTS = "requests";

  private static final CacheControl CACHE_CONTROL               = new CacheControl();

  private static final Date         DEFAULT_IMAGES_LAST_MODIFED = new Date();

  // 7 days
  private static final int          CACHE_IN_SECONDS            = 7 * 86400;

  private static final int          CACHE_IN_MILLI_SECONDS      = CACHE_IN_SECONDS * 1000;

  private IdentityManager           identityManager;

  private UploadService             uploadService;

  private SpaceService              spaceService;

  private byte[]              defaultSpaceAvatar = null;

  public SpaceRestResourcesV1(SpaceService spaceService, IdentityManager identityManager, UploadService uploadService) {
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.uploadService = uploadService;

    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
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

    ListAccess<Space> listAccess = null;
    SpaceFilter spaceFilter = new SpaceFilter();

    if (StringUtils.isNotBlank(q)) {
      spaceFilter.setSpaceNameSearchCondition(StringUtils.trim(q));
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
        if (space.hasBindings()) {
          spaceInfo.setIsUserBound(spaceBindingService.countUserBindings(space.getId(), authenticatedUser) > 0);
        }
        //
        spaceInfos.add(spaceInfo.getDataEntity()); 
      }
    }

    CollectionEntity collectionSpace = new CollectionEntity(spaceInfos, EntityBuilder.SPACES_TYPE, offset, limit);
    if (returnSize) {
      collectionSpace.setSize(listAccess.getSize());
    }
    
    EntityTag eTag = null;
    eTag = new EntityTag(Integer.toString(collectionSpace.hashCode()));

    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
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
    if (model == null || model.getDisplayName() == null || model.getDisplayName().length() < 3 || model.getDisplayName().length() > 200 || !SpaceUtils.isValidSpaceName(model.getDisplayName())) {
      throw new SpaceException(SpaceException.Code.INVALID_SPACE_NAME);
    }

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
                               @Context Request request,
                               @ApiParam(value = "Space id", required = true) @PathParam("id") String id,
                               @ApiParam(value = "Asking for a full representation of a specific subresource, ex: members or managers", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceById(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    EntityTag eTag;
    Long lastUpdateDate = space.getLastUpdatedTime();
    eTag = new EntityTag(String.valueOf(lastUpdateDate.hashCode()));

    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = EntityBuilder.getResponseBuilder(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
      builder.tag(eTag);
    }

    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(space.getLastUpdatedTime() > 0 ? new Date(space.getLastUpdatedTime()) : new Date());
    if (lastUpdateDate > 0) {
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("byPrettyName/{prettyName}")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Gets a specific space by pretty name",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns the space in the following cases: <br/><ul><li>the authenticated user is a member of the space</li><li>the space is \"public\"</li><li>the authenticated user is a spaces super manager</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response getSpaceByPrettyName(@Context UriInfo uriInfo,
                                       @Context Request request,
                                       @ApiParam(value = "Space id", required = true) @PathParam(
                                         "prettyName"
                                       ) String prettyName,
                                       @ApiParam(
                                           value = "Asking for a full representation of a specific subresource, ex: members or managers",
                                           required = false
                                       ) @QueryParam("expand") String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceByPrettyName(prettyName);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && !spaceService.isMember(space, authenticatedUser)
        && !spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    EntityTag eTag;
    Long lastUpdateDate = space.getLastUpdatedTime();
    eTag = new EntityTag(String.valueOf(lastUpdateDate.hashCode()));

    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = EntityBuilder.getResponseBuilder(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
      builder.tag(eTag);
    }

    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(space.getLastUpdatedTime() > 0 ? new Date(space.getLastUpdatedTime()) : new Date());
    if (lastUpdateDate > 0) {
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("byDisplayName/{displayName}")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Gets a specific space by display name",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns the space in the following cases: <br/><ul><li>the authenticated user is a member of the space</li><li>the space is \"public\"</li><li>the authenticated user is a spaces super manager</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response getSpaceByDisplayName(@Context UriInfo uriInfo,
                                        @Context Request request,
                                        @ApiParam(value = "Space id", required = true) @PathParam(
                                          "displayName"
                                        ) String displayName,
                                        @ApiParam(
                                            value = "Asking for a full representation of a specific subresource, ex: members or managers",
                                            required = false
                                        ) @QueryParam("expand") String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceByDisplayName(displayName);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && !spaceService.isMember(space, authenticatedUser)
        && !spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    EntityTag eTag = null;
    Long lastUpdateDate = space.getLastUpdatedTime();
    eTag = new EntityTag(String.valueOf(lastUpdateDate.hashCode()));

    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      builder = EntityBuilder.getResponseBuilder(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
      builder.tag(eTag);
    }

    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(space.getLastUpdatedTime() > 0 ? new Date(space.getLastUpdatedTime()) : new Date());
    if (lastUpdateDate > 0) {
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
  }

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
                                     @ApiParam(value = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'") @QueryParam("lastModified") String lastModified,
                                     @ApiParam(value = "Space pretty name", required = true) @PathParam("id") String id) throws IOException {

    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    Response.ResponseBuilder builder = null;
    Long lastUpdated = null;
    if (!isDefault) {
      String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
      
      Space space = spaceService.getSpaceByPrettyName(id);
      if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      //
      Profile profile = identity.getProfile();
      if (profile != null) {
        lastUpdated = profile.getAvatarLastUpdated();
      }
      EntityTag eTag = null;
      if (lastUpdated != null) {
        eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
      }
      //
      builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
      if (builder == null) {
        InputStream stream = identityManager.getAvatarInputStream(identity);
        if (stream != null) {
          builder = Response.ok(stream, "image/png");
          builder.tag(eTag);
        }
      }
    }

    if (builder == null) {
      builder = getDefaultAvatarBuilder();
    }

    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(lastUpdated == null ? DEFAULT_IMAGES_LAST_MODIFED : new Date(lastUpdated));
    // If the query has a lastModified parameter, it means that the client
    // will change the lastModified entry when it really changes
    // Which means that we can cache the image in browser side
    // for a long time
    if (StringUtils.isNotBlank(lastModified)) {
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
  }

  @GET
  @Path("{id}/banner")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Gets a space banner by id",
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
                                     @ApiParam(value = "The value of lastModified parameter will determine whether the query should be cached by browser or not. If not set, no 'expires HTTP Header will be sent'") @QueryParam("lastModified") String lastModified,
                                     @ApiParam(value = "Space id", required = true) @PathParam("id") String id) throws IOException {
    boolean isDefault = StringUtils.equals(LinkProvider.DEFAULT_IMAGE_REMOTE_ID, id);
    if (isDefault) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();

    Space space = spaceService.getSpaceByPrettyName(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
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
    builder.cacheControl(CACHE_CONTROL);
    builder.lastModified(lastUpdated == null ? DEFAULT_IMAGES_LAST_MODIFED : new Date(lastUpdated));
    // If the query has a lastModified parameter, it means that the client
    // will change the lastModified entry when it really changes
    // Which means that we can cache the image in browser side
    // for a long time
    if (StringUtils.isNotBlank(lastModified)) {
      builder.expires(new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS));
    }
    return builder.build();
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
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (model.getDisplayName() != null && (model.getDisplayName().length() < 3 || model.getDisplayName().length() > 200 || !SpaceUtils.isValidSpaceName(model.getDisplayName()))) {
      throw new SpaceException(SpaceException.Code.INVALID_SPACE_NAME);
    }

    if (StringUtils.isNotBlank(model.getDisplayName()) && !StringUtils.equals(space.getDisplayName(), model.getDisplayName())) {
      spaceService.renameSpace(authenticatedUser, space, model.getDisplayName());
    }

    fillSpaceFromModel(space, model);
    space.setEditor(authenticatedUser);
    spaceService.updateSpace(space, model.getInvitedMembers());
    space = spaceService.getSpaceById(space.getId());

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
                                  @ApiParam(value = "User name search information", required = false) @QueryParam("q") String q,
                                  @ApiParam(value = "Role of the target user in this space: manager, member, invited and pending", required = false, defaultValue = "member") @QueryParam("role") String role,
                                  @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                  @ApiParam(value = "Returning the number of users or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = spaceService.getSpaceById(id);
    if (space == null || (!spaceService.isMember(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    if (StringUtils.isBlank(role)) {
      role = Type.MEMBER.name();
    }
    Type type = Type.valueOf(role.toUpperCase());

    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setName(q);

    ListAccess<Identity> spaceIdentitiesListAccess = identityManager.getSpaceIdentityByProfileFilter(space,
                                                                                                     profileFilter,
                                                                                                     type,
                                                                                                     true);
    Identity[] spaceIdentities = spaceIdentitiesListAccess.load(offset, limit);

    List<DataEntity> profileInfos = null;
    if (spaceIdentities == null || spaceIdentities.length == 0) {
      profileInfos = Collections.emptyList();
    } else {
      profileInfos = Arrays.stream(spaceIdentities)
                           .map(identity -> EntityBuilder.buildEntityProfile(space, identity.getProfile(), uriInfo.getPath(), expand)
                                                         .getDataEntity())
                           .collect(Collectors.toList());
    }

    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(spaceIdentitiesListAccess.getSize());
    }
    return EntityBuilder.getResponseBuilder(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK).build();
  }

  @GET
  @Path("{id}/navigations")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Return list of navigations of a space",
    httpMethod = "GET",
    response = Response.class
  )
  @ApiResponses(value = {
    @ApiResponse (code = 204, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 401, message = "Unauthorized")
  })
  public Response getSpaceNavigations(@Context HttpServletRequest request,
                                      @ApiParam(value = "Space id", required = true) @PathParam("id") String spaceId) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null || (!spaceService.isMember(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<UserNode> navigations = SpaceUtils.getSpaceNavigations(space,
                                                                request.getLocale(),
                                                                authenticatedUser);

    if (navigations == null) {
      return Response.ok(Collections.emptyList()).build();
    }
    List<DataEntity> spaceNavigations = navigations.stream().map(node -> {
      BaseEntity app = new BaseEntity(node.getId());
      app.setProperty("label", node.getResolvedLabel());
      app.setProperty("icon", node.getIcon());
      app.setProperty("uri", node.getURI());
      return app.getDataEntity();
    }).collect(Collectors.toList());
    return Response.ok(spaceNavigations).build();
  }

  @GET
  @Path("applications")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Return list of applications that a use is allowed to add to a space",
    httpMethod = "GET",
    response = Response.class
  )
  @ApiResponses(value = {
      @ApiResponse (code = 200, message = "Request fulfilled"),
      @ApiResponse (code = 500, message = "Internal server error"),
      @ApiResponse (code = 401, message = "Unauthorized")
  })
  public Response getSpaceApplicationsChoices() {
    try {
      List<Application> applications = spaceService.getSpacesApplications();
      List<DataEntity> spaceApplications = applications.stream().map(application -> {
        application = computeApplicationAttributes(application);
        BaseEntity app = new BaseEntity(application.getApplicationName());
        app.setProperty(RestProperties.DISPLAY_NAME, application.getDisplayName());
        app.setProperty("contentId", application.getContentId());
        app.setProperty("applicationName", application.getApplicationName());
        app.setProperty("description", application.getDescription());
        app.setProperty("iconUrl", application.getIconURL());
        app.setProperty("removable", true);
        return app.getDataEntity();
      }).collect(Collectors.toList());
      return Response.ok(spaceApplications).build();
    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @POST
  @Path("applications")
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Add an application into list of allowed application to instantiate in spaces",
    httpMethod = "POST",
    response = Response.class
  )
  @ApiResponses(value = {
      @ApiResponse (code = 204, message = "Request fulfilled"),
      @ApiResponse (code = 500, message = "Internal server error"),
      @ApiResponse (code = 401, message = "Unauthorized")
  })
  public Response addSpaceApplication(Application application) {
    if (application == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("application is mandatory").build();
    }
    if (application.getContentId() == null || application.getApplicationName() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("application name and contentId are mandatory").build();
    }

    spaceService.addSpacesApplication(application);
    return Response.noContent().build();
  }

  @DELETE
  @Path("applications/{applicationName}")
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
      value = "Deletes an application from list of allowed application to instantiate in spaces",
      httpMethod = "DELETE",
      response = Response.class
  )
  @ApiResponses(value = {
      @ApiResponse (code = 204, message = "Request fulfilled"),
      @ApiResponse (code = 500, message = "Internal server error"),
      @ApiResponse (code = 401, message = "Unauthorized")
  })
  public Response deleteSpaceApplication(@ApiParam(value = "Application name", required = true) @PathParam("applicationName") String applicationName) {
    if (applicationName == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("application name is mandatory").build();
    }

    spaceService.deleteSpacesApplication(applicationName);
    return Response.noContent().build();
  }

  @GET
  @Path("{id}/applications")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
    value = "Return list of applications of a space",
    httpMethod = "GET",
    response = Response.class
  )
  @ApiResponses(value = {
      @ApiResponse (code = 204, message = "Request fulfilled"),
      @ApiResponse (code = 500, message = "Internal server error"),
      @ApiResponse (code = 401, message = "Unauthorized")
  })
  public Response getSpaceApplications(@ApiParam(value = "Space id", required = true) @PathParam("id") String spaceId) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null || (!spaceService.isMember(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    List<String> appList = SpaceUtils.getAppIdList(space);
    List<String> sortedAppList = new ArrayList<>();
    try {
      UserNode spaceUserNode = SpaceUtils.getSpaceUserNode(space);
      Collection<UserNode> children = spaceUserNode.getChildren();
      for (UserNode userNode : children) {
        if (userNode.getPageRef() != null) {
          String appId = userNode.getPageRef().getName();
          if (appList.contains(appId)) {
            sortedAppList.add(appId);
          }
        }
      }
    } catch (Exception e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
    
    List<DataEntity> applications = sortedAppList.stream().map(appId -> {
      Application application = null;
      try {
        application = SpaceUtils.getAppFromPortalContainer(appId);
      } catch (Exception e) {
        return null;
      }

      if (application == null) {
        String displayName = SpaceUtils.getAppNodeName(space, appId);

        BaseEntity app = new BaseEntity(appId);
        app.setProperty(RestProperties.DISPLAY_NAME, displayName);
        app.setProperty("description", displayName);
        app.setProperty("removable", SpaceUtils.isRemovableApp(space, appId));
        app.setProperty("order", sortedAppList.indexOf(appId));
        return app.getDataEntity();
      } else {
        BaseEntity app = new BaseEntity(appId);
        app.setProperty(RestProperties.DISPLAY_NAME, application.getDisplayName());
        app.setProperty("contentId", application.getContentId());
        app.setProperty("applicationName", application.getApplicationName());
        app.setProperty("description", application.getDescription());
        app.setProperty("iconUrl", application.getIconURL());

        app.setProperty("order", sortedAppList.indexOf(appId));
        app.setProperty("removable", SpaceUtils.isRemovableApp(space, appId));
        return app.getDataEntity();
      }
    }).collect(Collectors.toList());
    return Response.ok(applications).build();
  }

  @DELETE
  @Path("{id}/applications/{appId}")
  @RolesAllowed("users")
  @ApiOperation(
    value = "Deletes a selected application of a space",
    httpMethod = "DELETE",
    response = Response.class
  )
  @ApiResponses(value = {
    @ApiResponse (code = 204, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 401, message = "Unauthorized")
  })
  public Response deleteSpaceApplication(@ApiParam(value = "Space id", required = true) @PathParam("id") String spaceId,
                                         @ApiParam(value = "Application id", required = true) @PathParam("appId") String appId) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null || (!spaceService.isManager(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<String> appPartsList = Arrays.asList(space.getApp().split(","));
    String appPartToChange = appPartsList.stream()
                                         .filter(appPart -> StringUtils.startsWith(appPart, appId + ":"))
                                         .findFirst()
                                         .orElse(null);
    if (StringUtils.isBlank(appPartToChange)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    String[] appParts = appPartToChange.split(":");
    try {
      spaceService.removeApplication(space, appId, appParts[1]);
    } catch (SpaceException e) {
      return Response.serverError().entity(e.getLocalizedMessage()).build();
    }
    return Response.noContent().build();
  }

  @POST
  @Path("{id}/applications")
  @RolesAllowed("users")
  @ApiOperation(
    value = "Add a new application into space",
    httpMethod = "POST",
    response = Response.class
  )
  @ApiResponses(value = {
    @ApiResponse (code = 204, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 401, message = "Unauthorized")
  })
  public Response addSpaceApplication(@ApiParam(value = "Space id", required = true) @PathParam("id") String spaceId,
                                      @ApiParam(value = "Application id", required = true) @FormParam("appId") String appId) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null || (!spaceService.isManager(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    try {
      spaceService.installApplication(space, appId);
      spaceService.activateApplication(space, appId);
    } catch (SpaceException e) {
      return Response.serverError().entity(e.getLocalizedMessage()).build();
    }
    return Response.noContent().build();
  }

  @PUT
  @Path("{id}/applications/{appId}")
  @RolesAllowed("users")
  @ApiOperation(
    value = "Deletes a selected application of a space",
    httpMethod = "PUT",
    response = Response.class
  )
  @ApiResponses(value = {
    @ApiResponse(code = 204, message = "Request fulfilled"),
    @ApiResponse(code = 500, message = "Internal server error"),
    @ApiResponse(code = 401, message = "Unauthorized")
  })
  public Response moveSpaceApplicationOrder(@ApiParam(value = "Space id", required = true) @PathParam("id") String spaceId,
                                            @ApiParam(value = "Application id", required = true) @PathParam("appId") String appId,
                                            @ApiParam(value = "Move transition: 1 to move up, -1 to move down", required = true) @FormParam("transition") int transition) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null || (!spaceService.isManager(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    try {
      spaceService.moveApplication(spaceId, appId, transition);
    } catch (SpaceException e) {
      return Response.serverError().entity(e.getLocalizedMessage()).build();
    }
    return Response.noContent().build();
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
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
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
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
    Identity poster = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    
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

  private void fillSpaceFromModel(Space space, SpaceEntity model) throws Exception {
    space.setPriority(Space.INTERMEDIATE_PRIORITY);

    if (StringUtils.isNotBlank(model.getDisplayName())) {
      space.setDisplayName(model.getDisplayName());
      space.setDescription(model.getDescription());

      if (StringUtils.isBlank(space.getPrettyName())) {
        space.setPrettyName(model.getDisplayName());
      }
    } else if (StringUtils.isNotBlank(model.getPrettyName())) {
      space.setPrettyName(model.getPrettyName());
      space.setDescription(model.getDescription());

      if (StringUtils.isBlank(space.getDisplayName())) {
        space.setDisplayName(model.getPrettyName());
      }
    }

    if (StringUtils.isNotBlank(model.getTemplate())) {
      space.setTemplate(model.getTemplate());
    }

    if (StringUtils.isNotBlank(model.getBannerId())) {
      updateProfileField(space, Profile.BANNER, model.getBannerId());
    }

    if (StringUtils.isNotBlank(model.getAvatarId())) {
      updateProfileField(space, Profile.AVATAR, model.getAvatarId());
    }

    if (StringUtils.equalsIgnoreCase(Space.HIDDEN, model.getVisibility())) {
      space.setVisibility(Space.HIDDEN);
    } else if (StringUtils.equalsIgnoreCase(Space.PRIVATE, model.getVisibility())) {
      space.setVisibility(Space.PRIVATE);
    } else if (StringUtils.equalsIgnoreCase(Space.PUBLIC, model.getVisibility())) {
      space.setVisibility(Space.PUBLIC);
    } else if (StringUtils.isBlank(model.getVisibility()) && space.getId() == null) {
      space.setVisibility(Space.PRIVATE);
    }

    if (StringUtils.equalsIgnoreCase(Space.OPEN, model.getSubscription())) {
      space.setRegistration(Space.OPEN);
    } else if (StringUtils.equalsIgnoreCase(Space.CLOSE, model.getSubscription())) {
      space.setRegistration(Space.CLOSE);
    } else if (StringUtils.equalsIgnoreCase(Space.VALIDATION, model.getSubscription())) {
      space.setRegistration(Space.VALIDATION);
    } else if (StringUtils.isBlank(model.getSubscription()) && space.getId() == null) {
      space.setRegistration(Space.VALIDATION);
    }
  }

  private void updateProfileField(Space space,
                                  String name,
                                  String value) throws Exception {
    if (Profile.AVATAR.equals(name) || Profile.BANNER.equals(name)) {
      UploadResource uploadResource = uploadService.getUploadResource(value);
      if (uploadResource == null) {
        throw new IllegalStateException("No uploaded resource found with uploadId = " + value);
      }
      String storeLocation = uploadResource.getStoreLocation();
      try (FileInputStream inputStream = new FileInputStream(storeLocation)) {
        if (Profile.AVATAR.equals(name)) {
          AvatarAttachment attachment = new AvatarAttachment(null,
                                            uploadResource.getFileName(),
                                            uploadResource.getMimeType(),
                                            inputStream,
                                            System.currentTimeMillis());
          space.setAvatarAttachment(attachment);
          spaceService.updateSpaceAvatar(space);
        } else {
          BannerAttachment attachment = new BannerAttachment(null,
                                            uploadResource.getFileName(),
                                            uploadResource.getMimeType(),
                                            inputStream,
                                            System.currentTimeMillis());
          space.setBannerAttachment(attachment);
          spaceService.updateSpaceBanner(space);
        }
      } finally {
        uploadService.removeUploadResource(value);
      }
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
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
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

  private Response.ResponseBuilder getDefaultAvatarBuilder() throws IOException {
    if (defaultSpaceAvatar == null) {
      InputStream is = PortalContainer.getInstance()
                                      .getPortalContext()
                                      .getResourceAsStream("/skin/images/avatar/DefaultSpaceAvatar.png");
      if (is == null) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
      defaultSpaceAvatar = IOUtil.getStreamContentAsBytes(is);
    }
    return Response.ok(new ByteArrayInputStream(defaultSpaceAvatar), "image/png");
  }

  private Application computeApplicationAttributes(Application application) {
    Application applicationFromContainer = null;
    try {
      applicationFromContainer = SpaceUtils.getAppFromPortalContainer(application.getApplicationName());
    } catch (Exception e) {
      LOG.debug("Error retrieving");
    }
    if (applicationFromContainer != null) {
      application = applicationFromContainer;
    }
    return application;
  }

}
