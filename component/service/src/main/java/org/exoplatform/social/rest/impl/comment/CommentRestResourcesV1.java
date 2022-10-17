/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.rest.impl.comment;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.deprecation.DeprecatedAPI;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.impl.activity.ActivityRestResourcesV1;
import org.exoplatform.social.service.rest.api.VersionResources;


@Path(VersionResources.VERSION_ONE + "/social/comments")
@Tag(
    name = VersionResources.VERSION_ONE + "/social/comments",
    description = "Operations on a comment"
)
@Deprecated
public class CommentRestResourcesV1 implements ResourceContainer {

  private ActivityRestResourcesV1 activityRestResourcesV1;

  public CommentRestResourcesV1(ActivityRestResourcesV1 activityRestResourcesV1) {
    this.activityRestResourcesV1 = activityRestResourcesV1;
  }

  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(
      summary = "Gets a specific comment by id",
      method = "GET",
      description = "This returns the comment if the authenticated user has permissions to see the related activity."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  @DeprecatedAPI(value = "Use ActivityRestResourcesV1.getActivityById insteadk", insist = true)
  public Response getCommentById(
                                 @Context UriInfo uriInfo,
                                 @Context Request request,
                                 @Parameter(description = "Comment id", required = true)
                                 @PathParam("id") String id,
                                 @Parameter(
                                     description = "Asking for a full representation of a specific subresource if any",
                                     required = false
                                 )
                                 @QueryParam("expand") String expand) {
    return activityRestResourcesV1.getActivityById(uriInfo, request, id, expand);
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Updates a specific comment by id",
      method = "PUT",
      description = "This updates the comment in the following cases: <br/><ul><li>the authenticated user is the owner of the comment</li><li>the authenticated user is the super user</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  @DeprecatedAPI(value = "Use ActivityRestResourcesV1.updateComment instead", insist = true)
  public Response updateCommentById(
                                    @Context UriInfo uriInfo,
                                    @Parameter(description = "Comment id", required = true)
                                    @PathParam("id") String id,
                                    @Parameter(
                                        description = "Asking for a full representation of a subresource if any",
                                        required = false
                                    )
                                    @QueryParam("expand") String expand,
                                    @RequestBody(
                                        description = "Comment object to be updated, in which the title of comment is required.",
                                        required = true
                                    ) CommentEntity model) {
    return activityRestResourcesV1.updateComment(uriInfo, id, expand, model);
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(
      summary = "Deletes a specific comment by id",
      method = "DELETE",
      description = "This deletes the comment in the following cases: <br/><ul><li>the authenticated user is the owner of the comment</li><li>the authenticated user is the super user</li></ul>"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  @DeprecatedAPI(value = "Use ActivityRestResourcesV1.updateComment instead", insist = true)
  public Response deleteCommentById(
                                    @Context
                                    UriInfo uriInfo,
                                    @Parameter(description = "Comment id", required = true)
                                    @PathParam("id") String id,
                                    @Parameter(
                                        description = "Asking for a full representation of a specific subresource if any",
                                        required = false
                                    ) @QueryParam("expand") String expand) {
    return activityRestResourcesV1.deleteActivityById(uriInfo, id, false, expand);
  }

  @GET
  @Path("{id}/likes")
  @RolesAllowed("users")
  @Operation(
      summary = "Gets likes of a specific comment",
      method = "GET",
      description = "This returns a list of likes if the authenticated user has permissions to see the comment."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  @DeprecatedAPI(value = "Use ActivityRestResourcesV1.getLikesOfActivity instead", insist = true)
  public Response getLikesOfComment(
                                    @Context UriInfo uriInfo,
                                    @Parameter(description = "Comment id", required = true)
                                    @PathParam("id") String id,
                                    @Parameter(description = "Offset", required = false) @Schema(defaultValue = "0")
                                    @QueryParam("offset") int offset,
                                    @Parameter(description = "Limit", required = false) @Schema(defaultValue = "20")
                                    @QueryParam("limit") int limit,
                                    @Parameter(
                                        description = "Asking for a full representation of a specific subresource if any",
                                        required = false
                                    )
                                    @QueryParam("expand") String expand) {
    return activityRestResourcesV1.getLikesOfActivity(uriInfo, id, offset, limit, expand);
  }

  @POST
  @Path("{id}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Adds a like to a specific comment",
      method = "POST",
      description = "This adds the like if the authenticated user has permissions to see the comment."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  @DeprecatedAPI(value = "Use ActivityRestResourcesV1.addLike instead", insist = true)
  public Response addLikeOnComment(
                                   @Context UriInfo uriInfo,
                                   @Context Request request,
                                   @Parameter(description = "Comment id", required = true)
                                   @PathParam("id") String id,
                                   @Parameter(description = "Asking for a full representation of a subresource if any", required = false)
                                   @QueryParam("expand") String expand) {
    return activityRestResourcesV1.addLike(uriInfo, request, id);
  }

  @DELETE
  @Path("{id}/likes/{username}")
  @RolesAllowed("users")
  @Operation(
      summary = "Deletes a like of a specific user for a given comment",
      method = "DELETE",
      description = "This deletes the like if the authenticated user is the given user or the super user."
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input") }
  )
  @DeprecatedAPI(value = "Use ActivityRestResourcesV1.deleteLike instead", insist = true)
  public Response deleteLikeOnComment(
                                      @Context UriInfo uriInfo,
                                      @Context Request request,
                                      @Parameter(description = "Comment id", required = true)
                                      @PathParam("id") String id,
                                      @Parameter(description = "User name", required = true)
                                      @PathParam("username") String username,
                                      @Parameter(
                                          description = "Asking for a full representation of a specific subresource if any",
                                          required = false
                                      )
                                      @QueryParam("expand") String expand) {
    return activityRestResourcesV1.deleteLike(uriInfo, request, id);
  }

}
