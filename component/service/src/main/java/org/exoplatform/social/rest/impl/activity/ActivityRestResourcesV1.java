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
package org.exoplatform.social.rest.impl.activity;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.filter.ActivitySearchFilter;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.ActivitySearchConnector;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.rest.api.ActivityRestResources;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.SharedActivityRestIn;

import io.swagger.annotations.*;

@Path(VersionResources.VERSION_ONE + "/social/activities")
@Api(tags = VersionResources.VERSION_ONE + "/social/activities", value = VersionResources.VERSION_ONE + "/social/activities", description = "Managing activities together with comments and likes")
public class ActivityRestResourcesV1 implements ActivityRestResources {

  private static final Log LOG = ExoLogger.getLogger(ActivityRestResourcesV1.class);

  private static final String TYPE = "space";

  
  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Gets all activities",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns an activity in the list in the following cases: <br/><ul><li>the authenticated user is the super user, so he can see all the activities</li><li>this is a user activity and the owner of the activity is the authenticated user or one of his connections</li><li>this is a space activity and the authenticated user is a member of the space</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getActivitiesOfCurrentUser(@Context UriInfo uriInfo,
                                             @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                             @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                             @ApiParam(value = "Returning the number of activities or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                             @ApiParam(value = "Asking for a full representation of a specific subresource, ex: comments or likes", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(currentUser);
    List<ExoSocialActivity> activities = listAccess.loadAsList(offset, limit);
    
    List<DataEntity> activityEntities = new ArrayList<DataEntity>();
    for (ExoSocialActivity activity : activities) {
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
      //
      activityEntities.add(activityEntity.getDataEntity()); 
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE,  offset, limit);
    if(returnSize) {
      collectionActivity.setSize(listAccess.getSize());
    }

    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific activity by id",
                httpMethod = "GET",
                response = Response.class,
				notes = "This returns the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user or one of his connections</li><li>this is a space activity and the authenticated user is a member of the space</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getActivityById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource, ex: comments or likes", required = false) @QueryParam("expand") String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
    if (activityEntity.getActivityStream() == null && !Util.hasMentioned(activity, currentUser.getRemoteId())) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific activity by id",
                httpMethod = "PUT",
                response = Response.class,
				notes = "This updates the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response updateActivityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Asking for a full representation of a specific subresource, ex: comments or likes", required = false) @QueryParam("expand") String expand,
                                     @ApiParam(value = "Activity object to be updated, ex: <br/>{<br/>\"title\" : \"My activity\"<br/>}", required = true) ActivityEntity model) throws Exception {
  
    if (model == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null || !activityManager.isActivityEditable(activity, ConversationState.getCurrent().getIdentity())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    ActivityStream activityStream = activity.getActivityStream();
    if("true".equals(model.getRead())
            && "news".equals(activity.getType())
            && activityStream != null && activityStream.getType().equals(ActivityStream.Type.SPACE)) {
      LOG.info("service=news operation=read_news parameters=\"activity_id:{},space_name:{},space_id:{},user_id:{}\"",
              activity.getId(),
              activityStream.getPrettyId(),
              activityStream.getId(),
              currentUser.getId());
    }

    if (model.getTitle() != null && !model.getTitle().equals(activity.getTitle())) {
      activity.setTitle(model.getTitle());
    }
    if (model.getBody() != null && !model.getBody().equals(activity.getBody())) {
      activity.setBody(model.getBody());
    }
    if(model.getTemplateParams() != null) {
      Map<String, String> templateParams = new HashMap<>();
      model.getTemplateParams().forEach((name, value) -> templateParams.put(name, (String) value));
      activity.setTemplateParams(templateParams);
    }
    if (model.getUpdateDate() != null) {
      activity.setUpdated(Long.parseLong(model.getUpdateDate()));
    }
    activityManager.updateActivity(activity);

    ActivityEntity activityInfo = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
    return EntityBuilder.getResponse(activityInfo.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific activity by id",
                httpMethod = "DELETE",
                response = Response.class,
				notes = "This deletes the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteActivityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null || !activityManager.isActivityDeletable(activity, ConversationState.getCurrent().getIdentity())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
    //Delete activity
    activityManager.deleteActivity(activity);
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/comments")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets comments of a specific activity",
                httpMethod = "GET",
                response = Response.class,
				notes = "This returns a list of comments if the authenticated user has permissions to see the activity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getCommentsOfActivity(@Context UriInfo uriInfo,
                                        @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                                        @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                        @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                        @ApiParam(value = "Returning the number of activities or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                        @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, uriInfo.getPath(), currentUser) == null && !Util.hasMentioned(activity, currentUser.getRemoteId())) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    List<DataEntity> commentsEntity = new ArrayList<DataEntity>();
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity, EntityBuilder.expandSubComments(expand));
    List<ExoSocialActivity> comments = listAccess.loadAsList(offset, limit);
    for (ExoSocialActivity comment : comments) {
      CommentEntity commentInfo = EntityBuilder.buildEntityFromComment(comment, uriInfo.getPath(), expand, true);
      commentsEntity.add(commentInfo.getDataEntity());
    }
    
    CollectionEntity collectionComment = new CollectionEntity(commentsEntity, EntityBuilder.COMMENTS_TYPE, offset, limit);    
    if(returnSize) {
      collectionComment.setSize(listAccess.getSize());
    }
    //
    return EntityBuilder.getResponse(collectionComment, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/comments")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Posts a comment on a specific activity",
                httpMethod = "POST",
                response = Response.class,
				notes = "This posts the comment if the authenticated user has permissions to see the activity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response postComment(@Context UriInfo uriInfo,
                              @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                              @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                              @ApiParam(value = "Comment object to be posted, ex: <br/>{<br/>\"title\" : \"My comment\"<br/>}", required = true) CommentEntity model) throws Exception {
    
    if (model == null || model.getTitle() == null || model.getTitle().length() == 0) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, uriInfo.getPath(), currentUser) == null && !Util.hasMentioned(activity, currentUser.getRemoteId())) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setBody(model.getBody());
    comment.setTitle(model.getTitle());
    comment.setParentCommentId(model.getParentCommentId());
    comment.setUserId(currentUser.getId());
    activityManager.saveComment(activity, comment);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromComment(activityManager.getActivity(comment.getId()), uriInfo.getPath(), expand, false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @POST
  @Path("{id}/share")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Shares a specific activity to specific spaces",
                httpMethod = "POST",
                response = Response.class,
                notes = "This shares the given activity to the target spaces if the authenticated user has permissions to post to the target spaces")
  @ApiResponses(value = {
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response shareActivityOnSpaces(@Context UriInfo uriInfo,
                                      @ApiParam(value = "Activity id", required = true) @PathParam("id") String activityId,
                                      @ApiParam(value = "Asking for a full representation of a specific subresource, ex: comments or likes", required = false) @QueryParam("expand") String expand,
                                      @ApiParam(value = "Share target spaces", required = true) SharedActivityRestIn sharedActivityRestIn) throws Exception {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    if (activityManager.getActivity(activityId) == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    if (sharedActivityRestIn == null || sharedActivityRestIn.getTargetSpaces() == null || sharedActivityRestIn.getTargetSpaces().isEmpty() || sharedActivityRestIn.getType() == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    Identity authenticatedUserIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    List<ActivityEntity> sharedActivitiesEntities = new ArrayList<ActivityEntity>();
    for (String targetSpaceName : sharedActivityRestIn.getTargetSpaces()) {
      Space targetSpace = CommonsUtils.getService(SpaceService.class).getSpaceByPrettyName(targetSpaceName);
      if (SpaceUtils.isSpaceManagerOrSuperManager(authenticatedUser, targetSpace.getGroupId()) || (CommonsUtils.getService(SpaceService.class).isMember(targetSpace, authenticatedUser) && SpaceUtils.isRedactor(authenticatedUser, targetSpace.getGroupId()))) {
        // create activity
        ExoSocialActivity sharedActivity = new ExoSocialActivityImpl();
        sharedActivity.setTitle(sharedActivityRestIn.getTitle());
        sharedActivity.setType(sharedActivityRestIn.getType());
        sharedActivity.setUserId(authenticatedUserIdentity.getId());
        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("originalActivityId", activityId); 
        sharedActivity.setTemplateParams(templateParams);
        Identity targetSpaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, targetSpaceName);
        if (targetSpaceIdentity != null) {
          CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(targetSpaceIdentity, sharedActivity);
          ActivityEntity sharedActivityEntity = EntityBuilder.buildEntityFromActivity(sharedActivity, authenticatedUserIdentity, uriInfo.getPath(), expand);
          sharedActivitiesEntities.add(sharedActivityEntity);
          LOG.info("service=activity operation=share parameters=\"activity_type:{},activity_id:{},space_id:{},user_id:{}\"",
                   sharedActivity.getType(),
                   sharedActivity.getId(),
                   targetSpace.getId(),
                   sharedActivity.getPosterId());
        }
      }
    }
    return EntityBuilder.getResponse(sharedActivitiesEntities, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/likes")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets likes of a specific activity",
                httpMethod = "GET",
                response = Response.class,
				notes = "This returns a list of likes if the authenticated user has permissions to see the activity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getLikesOfActivity(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                     @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                     @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, uriInfo.getPath(), currentUser) == null && !Util.hasMentioned(activity, currentUser.getRemoteId())) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<DataEntity> likesEntity = EntityBuilder.buildEntityFromLike(activity, uriInfo.getPath(), expand, offset, limit);
    CollectionEntity collectionLike = new CollectionEntity(likesEntity, EntityBuilder.LIKES_TYPE, offset, limit);    
    //
    return EntityBuilder.getResponse(collectionLike, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a like to a specific activity",
                httpMethod = "POST",
                response = Response.class,
				notes = "This adds the like if the authenticated user has permissions to see the activity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 204, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response addLike(@Context UriInfo uriInfo,
                          @ApiParam(value = "Activity id", required = true) @PathParam("id") String id) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, uriInfo.getPath(), currentUser) == null && !Util.hasMentioned(activity, currentUser.getRemoteId())) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    activityManager.saveLike(activity, currentUser);
    return Response.noContent().build();
  }
  
  @GET
  @Path("{id}/likes/{username}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a like of a specific user for a given activity",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the like if the authenticated user has permissions to see the activity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getLikesActivityOfUser(@Context UriInfo uriInfo,
                                         @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                                         @ApiParam(value = "User name", required = true) @PathParam("username") String username,
                                         @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    Identity userToGetLike = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<String> likerIds = Arrays.asList(activity.getLikeIdentityIds());
    List<DataEntity> likesEntity = new ArrayList<DataEntity>();
    if (likerIds.contains(userToGetLike.getId())) {
      likesEntity.add(EntityBuilder.buildEntityProfile(username, uriInfo.getPath(), expand).getDataEntity());
    }

    CollectionEntity collectionLike = new CollectionEntity(likesEntity, EntityBuilder.LIKES_TYPE, 0, 1);
    
    //
    return EntityBuilder.getResponse(collectionLike, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}/likes")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a like of a specific user for a given activity",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the like of authenticated user from an activity")
  @ApiResponses(value = { 
    @ApiResponse (code = 204, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteLike(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Activity id", required = true) @PathParam("id") String id) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null
            || (EntityBuilder.getActivityStream(activity, uriInfo.getPath(), currentUser) == null && !Util.hasMentioned(activity, currentUser.getRemoteId()))) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    activityManager.deleteLike(activity, currentUser);
    return Response.noContent().build();
  }

  @GET
  @Path("search")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Search activities using a query",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns a list of activities found by using search term"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response searchActivities(@Context UriInfo uriInfo,
                                   @ApiParam(value = "Term to search", required = true) @QueryParam(
                                       "q"
                                     ) String query,
                                     @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam(
                                       "offset"
                                     ) int offset,
                                     @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam(
                                       "limit"
                                   ) int limit) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    if (StringUtils.isBlank(query)) {
      return Response.status(Status.BAD_REQUEST).entity("'q' parameter is mandatory").build();
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class)
                                       .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ActivitySearchConnector activitySearchConnector = CommonsUtils.getService(ActivitySearchConnector.class);
    ActivitySearchFilter filter = new ActivitySearchFilter(query);
    List<ActivitySearchResult> searchResults = activitySearchConnector.search(currentUser, filter, offset, limit);
    List<ActivitySearchResultEntity> results = searchResults.stream().map(searchResult -> {
      ActivitySearchResultEntity entity = new ActivitySearchResultEntity(searchResult);
      entity.setPoster(EntityBuilder.buildEntityIdentity(searchResult.getPoster(), uriInfo.getPath(), "all"));
      entity.setStreamOwner(EntityBuilder.buildEntityIdentity(searchResult.getStreamOwner(), uriInfo.getPath(), "all"));
      ActivitySearchResult comment = searchResult.getComment();
      if (comment != null) {
        ActivitySearchResultEntity commentEntity = new ActivitySearchResultEntity(comment);
        commentEntity.setPoster(EntityBuilder.buildEntityIdentity(comment.getPoster(), uriInfo.getPath(), "all"));
        commentEntity.setStreamOwner(EntityBuilder.buildEntityIdentity(comment.getStreamOwner(), uriInfo.getPath(), "all"));
        entity.setComment(commentEntity);
      }

      ActivityStorage activityStorage = CommonsUtils.getService(ActivityStorage.class);
      ExoSocialActivity existingActivity = activityStorage.getActivity(entity.getId());
      int commentsCount = activityStorage.getNumberOfComments(existingActivity);
      entity.setCommentsCount(commentsCount);
      entity.setLikesCount(existingActivity.getNumberOfLikes());
      return entity;
    }).collect(Collectors.toList());

    return Response.ok(results).build();
  }

}