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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.activity.ActivityStreamType;
import org.exoplatform.social.core.activity.filter.ActivitySearchFilter;
import org.exoplatform.social.core.activity.model.ActivitySearchResult;
import org.exoplatform.social.core.activity.model.ActivityStream.Type;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.ActivitySearchConnector;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.ActivitySearchResultEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.MetadataItemEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/activities")
@Tag(name = VersionResources.VERSION_ONE + "/social/activities", description = "Managing activities together with comments and likes")
public class ActivityRestResourcesV1 implements ResourceContainer {

  private static final Log        LOG            = ExoLogger.getLogger(ActivityRestResourcesV1.class);

  private static final int        MAX_TO_PRELOAD = 10;

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
  @Operation(
      summary = "Gets activities of a specific user",
      description = "This returns an activity in the list in the following cases: " +
              "<br/><ul><li>this is a user activity and the owner of the activity is" +
              "the authenticated user or one of his connections</li><li>" +
              "this is a space activity and the authenticated user is a member of the space</li></ul>",
      method = "GET")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
      }
  )
  public Response getActivities( // NOSONAR
                                @Context UriInfo uriInfo,
                                @Parameter(
                                    description = "Space technical identifier",
                                    required = false
                                )
                                @QueryParam("spaceId") String spaceId,
                                @Parameter(
                                    description = "offset time to use for searching newer activities until a time identified using format yyyy-MM-dd HH:mm:ss",
                                    required = false
                                ) @Schema(defaultValue = "0")
                                @QueryParam("beforeTime") String beforeTime,
                                @Parameter(
                                    description = "offset time to use for searching newer activities since a time identified using format yyyy-MM-dd HH:mm:ss",
                                    required = false
                                ) @Schema(defaultValue = "0")
                                @QueryParam("afterTime") String afterTime,
                                @Parameter(description = "Offset", required = false)
                                @QueryParam("offset") int offset,
                                @Parameter(description = "Limit", required = false) @Schema(defaultValue = "20")
                                @QueryParam("limit") int limit,
                                @Parameter(description = "Returning the number of activities or not") @Schema(defaultValue = "false")
                                @QueryParam("returnSize") boolean returnSize,
                                @Parameter(
                                    description = "Asking for a full representation of a specific subresource, ex: <em>comments</em> or <em>likes</em>",
                                    required = false
                                )
                                @QueryParam("expand") String expand,
                                @Parameter(description = "Activity stream type. Possible values: ALL_STREAM, USER_STREAM, USER_FAVORITE_STREAM, MANAGE_SPACES_STREAM, FAVORITE_SPACES_STREAM.", required = false)
                                @QueryParam("streamType") ActivityStreamType streamType) {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    org.exoplatform.services.security.Identity currentUser = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = currentUser.getUserId();
    // Check if the given user doesn't exist
    Identity currentUserIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);
    if (currentUserIdentity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    boolean canPost;
    RealtimeListAccess<ExoSocialActivity> listAccess;
    ActivityFilter activityFilter = new ActivityFilter();
    if (!StringUtils.isBlank(spaceId)) {
      Space space = spaceService.getSpaceById(spaceId);
      if (space == null
          || (!spaceService.isMember(space, authenticatedUser) && !spaceService.isSuperManager(authenticatedUser))) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
      activityFilter.setSpaceId(spaceIdentity.getId());
      canPost = activityManager.canPostActivityInStream(currentUser, spaceIdentity);
    } else {
      canPost = activityManager.canPostActivityInStream(currentUser, currentUserIdentity);
    }
    if (streamType != null && !streamType.equals(ActivityStreamType.ALL_STREAM)) {
      activityFilter.setStreamType(streamType);
    } else if (!StringUtils.isBlank(spaceId)) {
      activityFilter.setStreamType(ActivityStreamType.ANY_SPACE_ACTIVITY);
    }
    if (!StringUtils.isEmpty(activityFilter.getSpaceId()) || activityFilter.getStreamType() != null) {
      listAccess = activityManager.getActivitiesByFilterWithListAccess(currentUserIdentity, activityFilter);
    } else {
      listAccess = activityManager.getActivityFeedWithListAccess(currentUserIdentity);
    }

    String entitiesName = null;
    List<DataEntity> activityEntities = null;
    boolean retrieveIds = StringUtils.contains(expand, "ids");
    List<String> activityIds = null;
    if (retrieveIds) {
      activityIds = listAccess.loadIdsAsList(offset, limit);
      activityEntities = activityIds.stream().map(id -> {
        DataEntity dataEntity = new DataEntity();
        dataEntity.setProperty("id", id);
        return dataEntity;
      }).toList();
      entitiesName = EntityBuilder.ACTIVITY_IDS_TYPE;
    } else {
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
      activityEntities = convertToEntities(activities, currentUserIdentity, uriInfo, expand);
      entitiesName = EntityBuilder.ACTIVITIES_TYPE;
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, entitiesName, offset, limit);
    if (returnSize) {
      collectionActivity.setSize(listAccess.getSize());
    }
    collectionActivity.put("canPost", canPost);

    ResponseBuilder responseBuilder = EntityBuilder.getResponseBuilder(collectionActivity,
                                                                       uriInfo,
                                                                       RestUtils.getJsonMediaType(),
                                                                       Response.Status.OK);
    if (activityIds != null && !activityIds.isEmpty()) {
      int preloadLimit = limit / 2;
      if (preloadLimit > 1) {
        String preloadExpand = expand.replaceFirst(",?ids,?", "");
        addPreloadActivityIds(activityIds, preloadLimit, preloadExpand, responseBuilder);
      }
    }
    return responseBuilder.build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Posts an activity to a specific space",
      description = "This posts the activity if the authenticated user is a member of the space or a spaces super manager.",
      method = "POST")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response postActivity(
                               @Context
                               UriInfo uriInfo,
                               @Parameter(description = "Space id", required = true)
                               @QueryParam("spaceId") String spaceId,
                               @Parameter(
                                   description = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                   required = false
                               )
                               @QueryParam("expand") String expand,
                               @RequestBody(description = "Activity object to be created", required = true) ActivityEntity model) {
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
      if (!activityManager.canPostActivityInStream(currentUser, spaceIdentity)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else if (!activityManager.canPostActivityInStream(currentUser, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    if (StringUtils.isBlank(model.getTitle())) {
      activity.setTitle("");
    } else {
      activity.setTitle(model.getTitle());
    }
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
    return EntityBuilder.getResponse(activityEntity.getDataEntity(),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @GET
  @Path("{activityId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Gets a specific activity by id",
      description = "This returns the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user or one of his connections</li><li>this is a space activity and the authenticated user is a member of the space</li><li>the authenticated user is the super user</li></ul>",
      method = "GET")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response getActivityById(
                                  @Context
                                  UriInfo uriInfo,
                                  @Context
                                  Request request,
                                  @Parameter(description = "Activity id", required = true)
                                  @PathParam("activityId")
                                  String activityId,
                                  @Parameter(
                                      description = "Asking for a full representation of a specific subresource, ex: comments or likes",
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

    long cacheTime = computeCacheTime(activity);
    String eTagValue = String.valueOf(Objects.hash(cacheTime, authenticatedUser, expand));
    EntityTag eTag = new EntityTag(eTagValue, true);
    Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
    if (builder == null) {
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
      builder = Response.ok(activityEntity.getDataEntity(), MediaType.APPLICATION_JSON);
      builder.tag(eTag);
      Date cacheTimeDate = new Date(computeLastUpdated(activity));
      builder.lastModified(cacheTimeDate);
      // Set cache control header to no-cache
      CacheControl cacheControl = new CacheControl();
      cacheControl.setNoCache(true);
      cacheControl.setPrivate(true);
      builder.cacheControl(cacheControl);
    }
    return builder.build();
  }

  @PUT
  @Path("{activityId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Updates a specific activity by id",
      description = "This updates the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user</li><li>the authenticated user is the super user</li></ul>",
      method = "PUT")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response updateActivityById(
                                     @Context
                                     UriInfo uriInfo,
                                     @Parameter(description = "Activity id", required = true)
                                     @PathParam("activityId") String activityId,
                                     @Parameter(
                                         description = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                         required = false
                                     )
                                     @QueryParam("expand")
                                     String expand,
                                     @RequestBody(
                                         description = "Activity object to be updated, ex: <br/>{<br/>\"title\" : \"My activity\"<br/>}",
                                         required = true
                                     ) ActivityEntity model) {

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
  @Operation(
      summary = "Deletes a specific activity by id",
      description = "This deletes the activity in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user</li><li>the authenticated user is the super user</li></ul>",
      method = "DELETE")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response deleteActivityById(
                                     @Context
                                     UriInfo uriInfo,
                                     @Parameter(description = "Activity id", required = true)
                                     @PathParam("activityId") String activityId,
                                     @Parameter(
                                         description = "Whether to just hide the activity or effectively delete it from database",
                                         required = false
                                     ) @Schema(defaultValue = "false")
                                     @QueryParam("hide") boolean hide,
                                     @Parameter(
                                         description = "Asking for a full representation of a specific subresource if any",
                                         required = false
                                     )
                                     @QueryParam("expand") String expand) {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityDeletable(activity, ConversationState.getCurrent().getIdentity())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    ActivityEntity activityEntity;
    if (hide) {
      activity = activityManager.hideActivity(activity.getId());
      activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
    } else {
      activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), expand);
      activityManager.deleteActivity(activity);
    }
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("{activityId}/comments")
  @RolesAllowed("users")
  @Operation(
      summary = "Gets comments of a specific activity",
      method = "GET",
      description = "This returns a list of comments if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response getComments(
                              @Context
                              UriInfo uriInfo,
                              @Parameter(description = "Activity id", required = true)
                              @PathParam("activityId") String activityId,
                              @Parameter(description = "Offset", required = false) @Schema(defaultValue = "0")
                              @QueryParam("offset") int offset,
                              @Parameter(description = "Limit", required = false) @Schema(defaultValue = "20")
                              @QueryParam("limit") int limit,
                              @Parameter(description = "Returning the number of activities or not") @Schema(defaultValue = "false")
                              @QueryParam("returnSize") boolean returnSize,
                              @Parameter(
                                  description = "Retrieve comments by last post time or by first post time"
                              ) @Schema(defaultValue = "false")
                              @QueryParam("sortDescending") boolean sortDescending,
                              @Parameter(
                                  description = "Asking for a full representation of a specific subresource if any",
                                  required = false
                              )
                              @QueryParam("expand") String expand) {

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
  @Operation(
      summary = "Posts a comment on a specific activity",
      method = "POST",
      description = "This posts the comment if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response postComment(
                              @Context
                              UriInfo uriInfo,
                              @Parameter(description = "Activity id", required = true)
                              @PathParam("activityId") String activityId,
                              @Parameter(
                                  description = "Asking for a full representation of a specific subresource if any",
                                  required = false
                              )
                              @QueryParam("expand")
                              String expand,
                              @RequestBody(
                                  description = "Comment object to be posted, ex: <br/>{<br/>\"title\" : \"My comment\"<br/>}",
                                  required = true
                              ) CommentEntity model) {

    if (model == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Comment entity is mandatory").build();
    }
    if (StringUtils.isNotBlank(model.getId())) {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity("comment identifier is not expected for comment creation")
                     .build();
    }
    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    if (StringUtils.isBlank(model.getTitle())) {
      comment.setTitle("");
    } else {
      comment.setTitle(model.getTitle());
    }
    comment.setParentCommentId(model.getParentCommentId());
    comment.setPosterId(currentUser.getId());
    comment.setUserId(currentUser.getId());
    comment.setFiles(model.getFiles());
    EntityBuilder.buildActivityFromEntity(model, comment);
    activityManager.saveComment(activity, comment);

    CommentEntity commentEntity = EntityBuilder.buildEntityFromComment(activityManager.getActivity(comment.getId()),
                                                                       currentUser,
                                                                       uriInfo.getPath(),
                                                                       expand,
                                                                       false);
    return EntityBuilder.getResponse(commentEntity.getDataEntity(),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @PUT
  @Path("{activityId}/comments")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Updates an existing comment",
      method = "PUT",
      description = "This updates an existing comment if the authenticated user is poster of the comment."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response updateComment(
                                @Context
                                UriInfo uriInfo,
                                @Parameter(description = "Activity id", required = true)
                                @PathParam("activityId") String activityId,
                                @Parameter(
                                    description = "Asking for a full representation of a specific subresource if any",
                                    required = false
                                )
                                @QueryParam("expand")
                                String expand,
                                @RequestBody(
                                    description = "Comment object to be posted, ex: <br/>{<br/>\"title\" : \"My comment\"<br/>}",
                                    required = true
                                ) CommentEntity model) {

    if (model == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Comment entity is mandatory").build();
    }
    if (StringUtils.isBlank(model.getId())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("comment identifier id mandatory").build();
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
    if (StringUtils.isBlank(model.getTitle())) {
      comment.setTitle("");
    } else {
      comment.setTitle(model.getTitle());
    }
    comment.setUpdated(System.currentTimeMillis());
    activityManager.updateActivity(comment, true);
    CommentEntity commentEntity = EntityBuilder.buildEntityFromComment(activityManager.getActivity(comment.getId()),
                                                                       currentUser,
                                                                       uriInfo.getPath(),
                                                                       expand,
                                                                       false);
    return EntityBuilder.getResponse(commentEntity.getDataEntity(),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @POST
  @Path("{activityId}/share")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Shares a specific activity to specific spaces",
      method = "POST",
      description = "This shares the given activity to the target spaces if the authenticated user has permissions to post to the target spaces"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response shareActivity(
                                @Context
                                UriInfo uriInfo,
                                @Parameter(description = "Activity id", required = true)
                                @PathParam("activityId")
                                String activityId,
                                @Parameter(
                                    description = "Asking for a full representation of a specific subresource, ex: comments or likes",
                                    required = false
                                )
                                @QueryParam("expand")
                                String expand,
                                @RequestBody(description = "Share target spaces", required = true)
                                ActivityEntity model) {
    if (StringUtils.isBlank(activityId)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    if (model == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    List<String> targetSpaces = model.getTargetSpaces();
    if (targetSpaces == null || targetSpaces.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    org.exoplatform.services.security.Identity currentUser = ConversationState.getCurrent().getIdentity();

    String authenticatedUser = currentUser.getUserId();
    Identity authenticatedUserIdentity =
                                       identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activityTemplate = new ExoSocialActivityImpl();
    activityTemplate.setTitle(model.getTitle());
    activityTemplate.setBody(model.getBody());
    activityTemplate.setType(model.getType());
    activityTemplate.setUserId(authenticatedUserIdentity.getId());
    activityTemplate.setFiles(model.getFiles());

    EntityBuilder.buildActivityParamsFromEntity(activityTemplate, model.getTemplateParams());

    List<ExoSocialActivity> sharedActivities;
    try {
      sharedActivities = activityManager.shareActivity(activityTemplate,
                                                       activityId,
                                                       targetSpaces,
                                                       currentUser);
    } catch (IllegalAccessException e) {
      LOG.warn("User {} doesn't have access to share activity {}", authenticatedUser, activityId, e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }

    List<DataEntity> sharedActivityEntities = convertToEntities(sharedActivities, authenticatedUserIdentity, uriInfo, expand);
    CollectionEntity collectionActivity = new CollectionEntity(sharedActivityEntities,
                                                               EntityBuilder.ACTIVITIES_TYPE,
                                                               0,
                                                               sharedActivityEntities.size());
    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @PUT
  @Path("{activityId}/unhide")
  @RolesAllowed("users")
  @Operation(summary = "Unhides an activity to publish it in users stream", method = "PUT", description = "This unhides the given activity to publish it in users stream if the authenticated user has edit permissions")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
  })
  public Response publishActivity(
                                  @Context
                                  UriInfo uriInfo,
                                  @Parameter(description = "Activity id", required = true)
                                  @PathParam("activityId")
                                  String activityId) {
    if (StringUtils.isBlank(activityId)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (!activityManager.isActivityEditable(activity, ConversationState.getCurrent().getIdentity())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    activity.isHidden(false);
    activityManager.updateActivity(activity, true);
    return Response.noContent().build();
  }

  @GET
  @Path("{activityId}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Gets likes of a specific activity",
      method = "GET",
      description = "This returns a list of likes if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response getLikesOfActivity(
                                     @Context UriInfo uriInfo,
                                     @Parameter(description = "Activity id", required = true)
                                     @PathParam("activityId") String activityId,
                                     @Parameter(description = "Offset", required = false) @Schema(defaultValue = "0")
                                     @QueryParam("offset") int offset,
                                     @Parameter(description = "Limit", required = false) @Schema(defaultValue = "20")
                                     @QueryParam("limit") int limit,
                                     @Parameter(
                                         description = "Asking for a full representation of a specific subresource if any",
                                         required = false
                                     )
                                     @QueryParam("expand") String expand) {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<DataEntity> likesEntity = EntityBuilder.buildEntityFromLike(activity, uriInfo.getPath(), expand, offset, limit);
    CollectionEntity collectionLike = new CollectionEntity(likesEntity, EntityBuilder.LIKES_TYPE, offset, limit);
    collectionLike.setSize(activity.getLikeIdentityIds() == null ? 0 : activity.getLikeIdentityIds().length);
    //
    return EntityBuilder.getResponse(collectionLike, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Path("{activityId}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Adds a like to a specific activity",
      method = "POST",
      description = "This adds the like if the authenticated user has permissions to see the activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response addLike(
                          @Context
                          UriInfo uriInfo,
                          @Context
                          Request request,
                          @Parameter(description = "Activity id", required = true)
                          @PathParam("activityId") String activityId) {

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    activityManager.saveLike(activity, currentUser);
    return getLikesOfActivity(uriInfo, activityId, 0, RestUtils.DEFAULT_LIMIT, null);
  }

  @DELETE
  @Path("{activityId}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Deletes a like of a specific user for a given activity",
      method = "DELETE",
      description = "This deletes the like of authenticated user from an activity"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response deleteLike(
                             @Context UriInfo uriInfo,
                             @Context Request request,
                             @Parameter(description = "Activity id", required = true)
                             @PathParam("activityId") String activityId) {

    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    activityManager.deleteLike(activity, currentUser);
    return getLikesOfActivity(uriInfo, activityId, 0, RestUtils.DEFAULT_LIMIT, null);
  }

  @POST
  @Path("{activityId}/pins")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Pin a specific activity to space stream", method = "POST", description = "This pins an activity to space stream if the authenticated user is a manager or a redactor of the space.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "404", description = "Activity not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response pinActivity(@Context UriInfo uriInfo,
                              @Context Request request,
                              @Parameter(description = "Activity id", required = true) @PathParam("activityId") String activityId) {

    if (StringUtils.isBlank(activityId)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    if (!activityManager.canPinActivity(activity, currentUser)) {
      return Response.status(Status.UNAUTHORIZED).build();
    }
    ActivityEntity activityEntity;
    activity = activityManager.pinActivity(activity.getId(), currentUser.getId());
    activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), null);
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @DELETE
  @Path("{activityId}/pins")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Unpin a specific activity from space stream", method = "DELETE", description = "This Unpins an activity from space stream")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "404", description = "Activity not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response unpinActivity(@Context UriInfo uriInfo,
                                @Context Request request,
                                @Parameter(description = "Activity id", required = true) @PathParam("activityId") String activityId) {

    if (StringUtils.isBlank(activityId)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = authenticatedUserIdentity.getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    if (!activityManager.canPinActivity(activity, currentUser)) {
      return Response.status(Status.UNAUTHORIZED).build();
    }
    activity = activityManager.unpinActivity(activity.getId());
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, currentUser, uriInfo.getPath(), null);
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("search")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Search activities using a query",
      method = "GET",
      description = "This returns a list of activities found by using search term"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  public Response searchActivities(
                                   @Context
                                   UriInfo uriInfo,
                                   @Parameter(description = "Term to search", required = true)
                                   @QueryParam("q") String query,
                                   @Parameter(description = "Whether to search in favorites only or not", required = true)
                                   @QueryParam(
                                     "favorites"
                                   ) boolean favorites,
                                   @Parameter(description = "Tag names used to search activities", required = true)
                                   @QueryParam(
                                     "tags"
                                   ) List<String> tagNames,
                                   @Parameter(description = "Offset", required = false) @Schema(defaultValue = "0")
                                   @QueryParam(
                                     "offset"
                                   ) int offset,
                                   @Parameter(description = "Limit", required = false) @Schema(defaultValue = "20")
                                   @QueryParam(
                                     "limit"
                                   ) int limit) {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    if (StringUtils.isBlank(query) && !favorites && CollectionUtils.isEmpty(tagNames)) {
      return Response.status(Status.BAD_REQUEST).entity("'q' parameter is mandatory").build();
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUserIdentity = identityManager.getOrCreateUserIdentity(authenticatedUser);

    ActivitySearchFilter filter = new ActivitySearchFilter(query, tagNames, favorites);
    List<ActivitySearchResult> searchResults = activitySearchConnector.search(currentUserIdentity, filter, offset, limit);
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
      Map<String, List<MetadataItemEntity>> activityMetadatasToPublish = EntityBuilder.retrieveMetadataItems(existingActivity,
                                                                                                             currentUserIdentity);
      if (MapUtils.isNotEmpty(activityMetadatasToPublish)) {
        entity.setMetadatas(activityMetadatasToPublish);
      }
      return entity;
    }).toList();

    return Response.ok(results).build();
  }

  private List<DataEntity> convertToEntities(List<ExoSocialActivity> activities,
                                             Identity currentUserIdentity,
                                             UriInfo uriInfo,
                                             String expand) {
    List<DataEntity> activityEntities = new ArrayList<>();
    for (ExoSocialActivity activity : activities) {
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity,
                                                                            currentUserIdentity,
                                                                            uriInfo.getPath(),
                                                                            expand);
      activityEntities.add(activityEntity.getDataEntity());
    }
    return activityEntities;
  }

  private void addPreloadActivityIds(List<String> activityIds, int preloadLimit, String expand, ResponseBuilder responseBuilder) {
    int offset = preloadLimit > MAX_TO_PRELOAD ? preloadLimit - MAX_TO_PRELOAD : 0;
    if (activityIds.size() < preloadLimit) {
          preloadLimit = activityIds.size();
          offset = offset > MAX_TO_PRELOAD ? offset : 0;
    }
    List<String> preloadActivityIds = activityIds.subList(offset, preloadLimit);
    for (String activityId : preloadActivityIds) {
      String activityLoadingURL = "/portal/rest/v1/social/activities/" + activityId + "?expand=" + expand;
      responseBuilder.header("Link", "<" + activityLoadingURL + ">; rel=preload; as=fetch; crossorigin=use-credentials");
    }
  }

  private long computeCacheTime(ExoSocialActivity activity) {
    long cacheTime = activity.getCacheTime();
    String prettyId = activity.getActivityStream().getPrettyId();
    Identity streamOwnerIdentity;
    if (activity.getActivityStream().getType() == Type.SPACE) {
      streamOwnerIdentity = identityManager.getOrCreateSpaceIdentity(prettyId);
    } else {
      streamOwnerIdentity = identityManager.getOrCreateUserIdentity(prettyId);
    }
    if (streamOwnerIdentity == null) {
      // When no stream owner detected or deleted, disable eTag
      cacheTime = System.currentTimeMillis();
    } else {
      cacheTime = streamOwnerIdentity.getCacheTime() + cacheTime * 32;
    }
    Identity posterIdentity = identityManager.getIdentity(activity.getPosterId());
    if (posterIdentity == null) {
      // When no stream owner detected or deleted, disable eTag
      cacheTime = System.currentTimeMillis();
    } else {
      cacheTime = posterIdentity.getCacheTime() + cacheTime * 32;
    }
    return cacheTime;
  }

  private long computeLastUpdated(ExoSocialActivity activity) {
    long lastUpdate = activity.getCacheTime();
    String prettyId = activity.getActivityStream().getPrettyId();
    Identity streamOwnerIdentity;
    if (activity.getActivityStream().getType() == Type.SPACE) {
      streamOwnerIdentity = identityManager.getOrCreateSpaceIdentity(prettyId);
    } else {
      streamOwnerIdentity = identityManager.getOrCreateUserIdentity(prettyId);
    }
    if (streamOwnerIdentity != null) {
      lastUpdate = streamOwnerIdentity.getCacheTime() > lastUpdate ? streamOwnerIdentity.getCacheTime() : lastUpdate;
    }
    Identity posterIdentity = identityManager.getIdentity(activity.getPosterId());
    if (posterIdentity != null) {
      lastUpdate = posterIdentity.getCacheTime() > lastUpdate ? posterIdentity.getCacheTime() : lastUpdate;
    }
    return lastUpdate;
  }

}
