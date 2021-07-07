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

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
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
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.SharedActivityRestIn;

import io.swagger.annotations.*;

@Path(VersionResources.VERSION_ONE + "/social/activities")
@Api(
    tags = VersionResources.VERSION_ONE + "/social/activities",
    value = VersionResources.VERSION_ONE + "/social/activities",
    description = "Managing activities together with comments and likes" // NOSONAR
)
public class ActivityRestResourcesV1 implements ResourceContainer {

  private static final Log        LOG = ExoLogger.getLogger(ActivityRestResourcesV1.class);

  private ActivityManager         activityManager;

  private IdentityManager         identityManager;

  private SpaceService            spaceService;

  private ActivitySearchConnector activitySearchConnector;

  public ActivityRestResourcesV1(ActivityManager activityManager,
                                 IdentityManager identityManager,
                                 SpaceService spaceService,
                                 ActivitySearchConnector activitySearchConnector) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.activitySearchConnector = activitySearchConnector;
  }

  @GET
  @RolesAllowed("users")
  @ApiOperation(
      value = "Gets activities of a specific user",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns an activity in the list in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user or one of his connections</li><li>this is a space activity and the authenticated user is a member of the space</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 401, message = "Unauthorized"),
          @ApiResponse(code = 500, message = "Internal server error"),
      }
  )
  public Response getActivities(
                                @Context
                                UriInfo uriInfo,
                                @ApiParam(
                                    value = "Space technical identifier",
                                    required = false
                                )
                                @QueryParam("spaceId")
                                String spaceId,
                                @ApiParam(
                                    value = "offset time to use for searching newer activities until a time identified using format yyyy-MM-dd HH:mm:ss",
                                    required = false,
                                    defaultValue = "0"
                                )
                                @QueryParam("beforeTime")
                                String beforeTime,
                                @ApiParam(
                                    value = "offset time to use for searching newer activities since a time identified using format yyyy-MM-dd HH:mm:ss",
                                    required = false,
                                    defaultValue = "0"
                                )
                                @QueryParam("afterTime")
                                String afterTime,
                                @ApiParam(
                                    value = "Offset",
                                    required = false
                                )
                                @QueryParam("offset")
                                int offset,
                                @ApiParam(
                                    value = "Limit",
                                    required = false,
                                    defaultValue = "20"
                                )
                                @QueryParam("limit")
                                int limit,
                                @ApiParam(
                                    value = "Returning the number of activities or not",
                                    defaultValue = "false"
                                )
                                @QueryParam("returnSize")
                                boolean returnSize,
                                @ApiParam(
                                    value = "Asking for a full representation of a specific subresource, ex: <em>comments</em> or <em>likes</em>",
                                    required = false
                                )
                                @QueryParam("expand")
                                String expand) {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    // Check if the given user doesn't exist
    Identity currentUserIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    if (currentUserIdentity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    RealtimeListAccess<ExoSocialActivity> listAccess;
    if (StringUtils.isBlank(spaceId)) {
      listAccess = activityManager.getActivityFeedWithListAccess(currentUserIdentity);
    } else {
      Space space = spaceService.getSpaceById(spaceId);
      if (space == null
          || (!spaceService.isMember(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    }

    List<DataEntity> activityEntities = new ArrayList<>();
    List<ExoSocialActivity> activities = null;
    if (StringUtils.isNotBlank(afterTime)) {
      try {
        activities = listAccess.loadNewer(RestUtils.getBaseTime(afterTime), limit);
      } catch (ParseException e) {
        return Response.status(Status.BAD_REQUEST).entity("afterTime Date has to be of format yyyy-MM-dd HH:mm:ss").build();
      }
    } else if (StringUtils.isNotBlank(beforeTime)) {
      try {
        activities = listAccess.loadOlder(RestUtils.getBaseTime(beforeTime), limit);
      } catch (ParseException e) {
        return Response.status(Status.BAD_REQUEST).entity("afterTime Date has to be of format yyyy-MM-dd HH:mm:ss").build();
      }
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    for (ExoSocialActivity activity : activities) {
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity,
                                                                            currentUserIdentity,
                                                                            uriInfo.getPath(),
                                                                            expand);
      activityEntities.add(activityEntity.getDataEntity());
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE, offset, limit);
    if (returnSize) {
      collectionActivity.setSize(listAccess.getSize());
    }
    if (StringUtils.contains(expand, "canPost")) {
      collectionActivity.put("canPost", activityManager.canPostActivity(currentUserIdentity, spaceId));
    }
    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Posts an activity to a specific space",
      httpMethod = "POST",
      response = Response.class,
      produces = "application/json",
      notes = "This posts the activity if the authenticated user is a member of the space or a spaces super manager."
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response postActivity(
                               @Context
                               UriInfo uriInfo,
                               @ApiParam(value = "Space id", required = true)
                               @QueryParam("spaceId")
                               String spaceId,
                               @ApiParam(
                                   value = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                   required = false
                               )
                               @QueryParam("expand")
                               String expand,
                               @ApiParam(value = "Activity object to be created", required = true)
                               ActivityEntity model) {
    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    org.exoplatform.services.security.Identity currentUser = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = currentUser.getUserId();
    Identity authenticatedUserIdentity =
                                       identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    Identity spaceIdentity = null;
    if (StringUtils.isNotBlank(spaceId)) {
      Space space = spaceService.getSpaceById(spaceId);
      if (space == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
    }

    if (!activityManager.canPostActivity(authenticatedUserIdentity, spaceId)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(model.getTitle());
    activity.setBody(model.getBody());
    activity.setType(model.getType());
    activity.setUserId(authenticatedUserIdentity.getId());
    activity.setFiles(model.getFiles());

    EntityBuilder.buildActivityParamsFromEntity(activity, model.getTemplateParams());

    if (StringUtils.isBlank(spaceId)) {
      activityManager.saveActivityNoReturn(authenticatedUserIdentity, activity);
    } else {
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
    }

    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity,
                                                                          authenticatedUserIdentity,
                                                                          uriInfo.getPath(),
                                                                          expand);
    return EntityBuilder.getResponse(activityEntity,
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @GET
  @Path("{activityId}")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Gets a specific activity by id",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user or one of his connections</li><li>this is a space activity and the authenticated user is a member of the space</li><li>the authenticated user is the super user</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response getActivityById(
                                  @Context
                                  UriInfo uriInfo,
                                  @ApiParam(value = "Activity id", required = true)
                                  @PathParam("activityId")
                                  String activityId,
                                  @ApiParam(
                                      value = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                      required = false
                                  )
                                  @QueryParam("expand")
                                  String expand) {

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @PUT
  @Path("{activityId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Updates a specific activity by id",
      httpMethod = "PUT",
      response = Response.class,
      notes = "This updates the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user</li><li>the authenticated user is the super user</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response updateActivityById(
                                     @Context
                                     UriInfo uriInfo,
                                     @ApiParam(value = "Activity id", required = true)
                                     @PathParam("activityId")
                                     String activityId,
                                     @ApiParam(
                                         value = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                         required = false
                                     )
                                     @QueryParam("expand")
                                     String expand,
                                     @ApiParam(
                                         value = "Activity object to be updated, ex: <br/>{<br/>\"title\" : \"My activity\"<br/>}",
                                         required = true
                                     )
                                     ActivityEntity model) {

    if (model == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //
    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (!activityManager.isActivityEditable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    EntityBuilder.buildActivityFromEntity(model, activity);
    activity.setFiles(model.getFiles());
    activity.setUpdated(System.currentTimeMillis());
    activityManager.updateActivity(activity, true);

    ActivityEntity activityInfo = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
    return EntityBuilder.getResponse(activityInfo.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @DELETE
  @Path("{activityId}")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Deletes a specific activity by id",
      httpMethod = "DELETE",
      response = Response.class,
      notes = "This deletes the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user</li><li>the authenticated user is the super user</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response deleteActivityById(
                                     @Context
                                     UriInfo uriInfo,
                                     @ApiParam(value = "Activity id", required = true)
                                     @PathParam("activityId")
                                     String activityId,
                                     @ApiParam(
                                         value = "Asking for a full representation of a specific subresource if any",
                                         required = false
                                     )
                                     @QueryParam("expand")
                                     String expand) {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityDeletable(activity, ConversationState.getCurrent().getIdentity())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
    activityManager.deleteActivity(activity);
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("{activityId}/comments")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Gets comments of a specific activity",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns a list of comments if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response getComments(
                              @Context
                              UriInfo uriInfo,
                              @ApiParam(value = "Activity id", required = true)
                              @PathParam("activityId")
                              String activityId,
                              @ApiParam(value = "Offset", required = false, defaultValue = "0")
                              @QueryParam("offset")
                              int offset,
                              @ApiParam(value = "Limit", required = false, defaultValue = "20")
                              @QueryParam("limit")
                              int limit,
                              @ApiParam(value = "Returning the number of activities or not", defaultValue = "false")
                              @QueryParam("returnSize")
                              boolean returnSize,
                              @ApiParam(
                                  value = "Retrieve comments by last post time or by first post time",
                                  defaultValue = "false"
                              )
                              @QueryParam("sortDescending")
                              boolean sortDescending,
                              @ApiParam(
                                  value = "Asking for a full representation of a specific subresource if any",
                                  required = false
                              )
                              @QueryParam("expand")
                              String expand) {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    org.exoplatform.services.security.Identity currentUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = currentUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, currentUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<DataEntity> commentsEntity = EntityBuilder.buildEntityFromComment(activity,
                                                                           currentUser,
                                                                           uriInfo.getPath(),
                                                                           expand,
                                                                           sortDescending,
                                                                           offset,
                                                                           limit);
    CollectionEntity collectionComment = new CollectionEntity(commentsEntity, EntityBuilder.COMMENTS_TYPE, offset, limit);
    if (returnSize) {
      boolean expandSubComments = EntityBuilder.expandSubComments(expand);
      RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity, expandSubComments);
      collectionComment.setSize(listAccess.getSize());
    }
    //
    return EntityBuilder.getResponse(collectionComment, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Path("{activityId}/comments")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Posts a comment on a specific activity",
      httpMethod = "POST",
      response = Response.class,
      notes = "This posts the comment if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response postComment(
                              @Context
                              UriInfo uriInfo,
                              @ApiParam(value = "Activity id", required = true)
                              @PathParam("activityId")
                              String activityId,
                              @ApiParam(
                                  value = "Asking for a full representation of a specific subresource if any",
                                  required = false
                              )
                              @QueryParam("expand")
                              String expand,
                              @ApiParam(
                                  value = "Comment object to be posted, ex: <br/>{<br/>\"title\" : \"My comment\"<br/>}",
                                  required = true
                              )
                              CommentEntity model) {

    if (model == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Comment entity is mandatory").build();
    }
    if (StringUtils.isNotBlank(model.getId())) {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity("comment identifier is not expected for comment creation")
                     .build();
    }
    if (StringUtils.isBlank(model.getTitle())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("comment title is mandatory").build();
    }

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setParentCommentId(model.getParentCommentId());
    comment.setPosterId(currentUser.getId());
    comment.setUserId(currentUser.getId());
    EntityBuilder.buildActivityFromEntity(model, comment);
    activityManager.saveComment(activity, comment);

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromComment(activityManager.getActivity(comment.getId()),
                                                                          currentUser,
                                                                          uriInfo.getPath(),
                                                                          expand,
                                                                          false),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @PUT
  @Path("{activityId}/comments")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Updates an existing comment",
      httpMethod = "PUT",
      response = Response.class,
      notes = "This updates an existing comment if the authenticated user is poster of the comment."
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response updateComment(
                                @Context
                                UriInfo uriInfo,
                                @ApiParam(value = "Activity id", required = true)
                                @PathParam("activityId")
                                String activityId,
                                @ApiParam(
                                    value = "Asking for a full representation of a specific subresource if any",
                                    required = false
                                )
                                @QueryParam("expand")
                                String expand,
                                @ApiParam(
                                    value = "Comment object to be posted, ex: <br/>{<br/>\"title\" : \"My comment\"<br/>}",
                                    required = true
                                )
                                CommentEntity model) {

    if (model == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Comment entity is mandatory").build();
    }
    if (StringUtils.isBlank(model.getId())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("comment identifier id mandatory").build();
    }
    if (StringUtils.isBlank(model.getTitle())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("comment title is mandatory").build();
    }

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity comment = activityManager.getActivity(model.getId());
    if (comment == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (!comment.isComment() || StringUtils.isBlank(comment.getParentId())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("activity can't be updated as a comment").build();
    }
    if (!StringUtils.equals(comment.getParentId(), activityId)) {
      return Response.status(Response.Status.UNAUTHORIZED).entity("Can't move a comment from an activity to another").build();
    }
    if (!StringUtils.equals(comment.getParentCommentId(), model.getParentCommentId())) {
      return Response.status(Response.Status.UNAUTHORIZED).entity("Can't move a comment reply from a comment to another").build();
    }
    if (!activityManager.isActivityEditable(comment, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    EntityBuilder.buildActivityFromEntity(model, comment);
    comment.setFiles(model.getFiles());
    comment.setUpdated(System.currentTimeMillis());
    activityManager.updateActivity(comment, true);
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromComment(activityManager.getActivity(comment.getId()),
                                                                          currentUser,
                                                                          uriInfo.getPath(),
                                                                          expand,
                                                                          false),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @POST
  @Path("{activityId}/share")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Shares a specific activity to specific spaces",
      httpMethod = "POST",
      response = Response.class,
      notes = "This shares the given activity to the target spaces if the authenticated user has permissions to post to the target spaces"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response shareActivityOnSpaces(
                                        @Context
                                        UriInfo uriInfo,
                                        @ApiParam(value = "Activity id", required = true)
                                        @PathParam("activityId")
                                        String activityId,
                                        @ApiParam(
                                            value = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                            required = false
                                        )
                                        @QueryParam("expand")
                                        String expand,
                                        @ApiParam(value = "Share target spaces", required = true)
                                        SharedActivityRestIn sharedActivityRestIn) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (activityManager.getActivity(activityId) == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    if (sharedActivityRestIn == null || sharedActivityRestIn.getTargetSpaces() == null
        || sharedActivityRestIn.getTargetSpaces().isEmpty() || sharedActivityRestIn.getType() == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    Identity authenticatedUserIdentity =
                                       identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    List<ActivityEntity> sharedActivitiesEntities = new ArrayList<>();
    for (String targetSpaceName : sharedActivityRestIn.getTargetSpaces()) {
      Space targetSpace = spaceService.getSpaceByPrettyName(targetSpaceName);
      if (SpaceUtils.isSpaceManagerOrSuperManager(authenticatedUser, targetSpace.getGroupId())
          || (spaceService.isMember(targetSpace, authenticatedUser)
              && SpaceUtils.isRedactor(authenticatedUser, targetSpace.getGroupId()))) {
        // create activity
        ExoSocialActivity sharedActivity = new ExoSocialActivityImpl();
        sharedActivity.setTitle(sharedActivityRestIn.getTitle());
        sharedActivity.setType(sharedActivityRestIn.getType());
        sharedActivity.setUserId(authenticatedUserIdentity.getId());
        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("originalActivityId", activityId);
        sharedActivity.setTemplateParams(templateParams);
        Identity targetSpaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, targetSpaceName);
        if (targetSpaceIdentity != null) {
          activityManager.saveActivityNoReturn(targetSpaceIdentity, sharedActivity);
          ActivityEntity sharedActivityEntity = EntityBuilder.buildEntityFromActivity(sharedActivity,
                                                                                      authenticatedUserIdentity,
                                                                                      uriInfo.getPath(),
                                                                                      expand);
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
  @Path("{activityId}/likes")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Gets likes of a specific activity",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns a list of likes if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response getLikesOfActivity(
                                     @Context
                                     UriInfo uriInfo,
                                     @ApiParam(value = "Activity id", required = true)
                                     @PathParam("activityId")
                                     String activityId,
                                     @ApiParam(value = "Offset", required = false, defaultValue = "0")
                                     @QueryParam("offset")
                                     int offset,
                                     @ApiParam(value = "Limit", required = false, defaultValue = "20")
                                     @QueryParam("limit")
                                     int limit,
                                     @ApiParam(
                                         value = "Asking for a full representation of a specific subresource if any",
                                         required = false
                                     )
                                     @QueryParam("expand")
                                     String expand) {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<DataEntity> likesEntity = EntityBuilder.buildEntityFromLike(activity, uriInfo.getPath(), expand, offset, limit);
    CollectionEntity collectionLike = new CollectionEntity(likesEntity, EntityBuilder.LIKES_TYPE, offset, limit);
    //
    return EntityBuilder.getResponse(collectionLike, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Path("{activityId}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(
      value = "Adds a like to a specific activity",
      httpMethod = "POST",
      response = Response.class,
      notes = "This adds the like if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 204, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response addLike(
                          @Context
                          UriInfo uriInfo,
                          @ApiParam(value = "Activity id", required = true)
                          @PathParam("activityId")
                          String activityId) {

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    activityManager.saveLike(activity, currentUser);
    return Response.noContent().build();
  }

  @DELETE
  @Path("{activityId}/likes")
  @RolesAllowed("users")
  @ApiOperation(
      value = "Deletes a like of a specific user for a given activity",
      httpMethod = "DELETE",
      response = Response.class,
      notes = "This deletes the like of authenticated user from an activity"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 204, message = "Request fulfilled"),
          @ApiResponse(code = 500, message = "Internal server error"),
          @ApiResponse(code = 400, message = "Invalid query input") }
  )
  public Response deleteLike(
                             @Context
                             UriInfo uriInfo,
                             @ApiParam(value = "Activity id", required = true)
                             @PathParam("activityId")
                             String activityId) {

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
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
  public Response searchActivities(
                                   @Context
                                   UriInfo uriInfo,
                                   @ApiParam(value = "Term to search", required = true)
                                   @QueryParam(
                                     "q"
                                   )
                                   String query,
                                   @ApiParam(value = "Offset", required = false, defaultValue = "0")
                                   @QueryParam(
                                     "offset"
                                   )
                                   int offset,
                                   @ApiParam(value = "Limit", required = false, defaultValue = "20")
                                   @QueryParam(
                                     "limit"
                                   )
                                   int limit) {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    if (StringUtils.isBlank(query)) {
      return Response.status(Status.BAD_REQUEST).entity("'q' parameter is mandatory").build();
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

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
