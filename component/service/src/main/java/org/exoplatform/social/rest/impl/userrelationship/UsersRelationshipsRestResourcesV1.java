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
package org.exoplatform.social.rest.impl.userrelationship;

import static org.exoplatform.social.rest.api.RestUtils.*;

import java.util.*;
import java.util.stream.Collectors;

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
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.rest.api.*;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;


@Path(VersionResources.VERSION_ONE + "/social/usersRelationships")
@Tag(name = VersionResources.VERSION_ONE + "/social/usersRelationships", description = "Managing relationships of users") // NOSONAR
public class UsersRelationshipsRestResourcesV1 implements UsersRelationshipsRestResources {

  private RelationshipManager relationshipManager;

  public UsersRelationshipsRestResourcesV1(RelationshipManager relationshipManager) {
    this.relationshipManager = relationshipManager;
  }

  @RolesAllowed("users")
  @GET
  @Operation(summary = "Gets all user relationships", method = "GET", description = "This returns a list of relationships in the following cases: <br/><ul>"
      +
      "<li>if the query param \"user\" is not defined: returns the relationships of the authenticated user</li>" +
      "<li>if the \"user\" is defined and the authenticated user is not an administrator: returns the relationships of the authenticated user</li>"
      +
      "<li>if the \"user\" is defined and the authenticated user is an administrator: returns the relationships of the defined user</li>"
      +
      "<li>if the \"others\" is defined: returns the relationships between the user and the users defined in \"others\" only</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getUsersRelationships(@Context UriInfo uriInfo,
                                        @Parameter(description = "Specific status of relationships: pending, confirmed or all") @Schema(defaultValue = "all") @QueryParam("status") String status,
                                        @Parameter(description = "User name to get relationships") @QueryParam("user") String user,
                                        @Parameter(description = "Usernames of the others users to get relationships with the given user") @QueryParam("others") String others,
                                        @Parameter(description = "Offset") @Schema(defaultValue = "0") @QueryParam("offset") int offset,
                                        @Parameter(description = "Limit") @Schema(defaultValue = "20") @QueryParam("limit") int limit,
                                        @Parameter(description = "Returning the number of relationships or not") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                        @Parameter(description = "Asking for a full representation of a specific subresource, ex: sender or receiver") @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Relationship.Type type =
                           StringUtils.isBlank(status) ? Relationship.Type.ALL : Relationship.Type.valueOf(status.toUpperCase());

    List<Relationship> relationships;

    String username = user;
    if (username == null || !RestUtils.isMemberOfAdminGroup()) {
      username = getCurrentUser();
    }
    Identity givenUser = getUserIdentity(username);

    if (StringUtils.isNotEmpty(others)) {
      String[] othersUsernames = others.split(",");
      relationships = Arrays.stream(othersUsernames)
                            .map(other -> getUserIdentity(other))
                            .map(otherIdentity -> relationshipManager.get(givenUser, otherIdentity))
                            .filter(Objects::nonNull)
                            .filter(relationship -> type.equals(Relationship.Type.ALL) || type.equals(relationship.getStatus()))
                            .collect(Collectors.toList());
    } else {
      relationships = relationshipManager.getRelationshipsByStatus(givenUser, type, offset, limit);
    }
    int size = returnSize ? relationshipManager.getRelationshipsCountByStatus(givenUser, type) : -1;

    List<DataEntity> relationshipEntities = EntityBuilder.buildRelationshipEntities(relationships, uriInfo);
    CollectionEntity collectionRelationship = new CollectionEntity(relationshipEntities,
                                                                   EntityBuilder.USERS_RELATIONSHIP_TYPE,
                                                                   offset,
                                                                   limit);
    if (returnSize) {
      collectionRelationship.setSize(size);
    }
    return EntityBuilder.getResponse(collectionRelationship, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @RolesAllowed("users")
  @Operation(summary = "Creates a relationship between two specific users", method = "POST", description = "This creates the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response createUsersRelationships(@Context UriInfo uriInfo,
                                           @Parameter(description = "Asking for a full representation of a specific subresource, ex: sender or receiver") @QueryParam("expand") String expand,
                                           @RequestBody(description = "Relationship object to be created, required fields: <br/>sender - user name of the sender,<br/>receiver - user name of the receiver,<br/>status - pending or confirmed", required = true) RelationshipEntity model) throws Exception {
    if (model == null || model.getReceiver() == null || model.getSender() == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    Relationship.Type status = null;
    if (model.getStatus() != null) {
      try {
        status = Relationship.Type.valueOf(model.getStatus().toUpperCase());
      } catch (Exception e) {
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
      }
    }

    // Not allowed to create confirmed relationship directly
    if (Relationship.Type.CONFIRMED.equals(status)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    //
    String authenticatedUser = getCurrentUser();
    if (!model.getSender().equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    //
    Identity sender = getCurrentUserIdentity();
    Identity receiver = getUserIdentity(model.getReceiver());
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    Relationship existingRelationship = relationshipManager.get(sender, receiver);
    // Authorize to create invitation only when the old
    if (existingRelationship != null && !Relationship.Type.IGNORED.equals(existingRelationship.getStatus())) {
      throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
    }

    Relationship relationship = saveRelationship(sender, receiver, null, status);
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @GET
  @RolesAllowed("users")
  @Path("{id}")
  @Operation(summary = "Gets a specific relationship of user by id", method = "GET", description = "This returns the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  @Deprecated
  public Response getUsersRelationshipsById(@Context UriInfo uriInfo,
                                            @Parameter(description = "Relationship id", required = true) @PathParam("id") String id,
                                            @Parameter(description = "Asking for a full representation of a specific subresource, ex: sender or receiver") @QueryParam("expand") String expand) throws Exception {
    if (StringUtils.isBlank(id)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    String receiver = relationship.getReceiver().getRemoteId();
    String sender = relationship.getSender().getRemoteId();
    return getUsersRelationship(uriInfo, sender, receiver, expand);
  }

  @PUT
  @RolesAllowed("users")
  @Path("{id}")
  @Operation(summary = "Updates a specific relationship of user by id", method = "PUT", description = "This updates the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  @Deprecated
  public Response updateUsersRelationshipsById(@Context UriInfo uriInfo,
                                               @Parameter(description = "Relationship id", required = true) @PathParam("id") String id,
                                               @Parameter(description = "Asking for a full representation of a specific subresource, ex: sender or receiver") @QueryParam("expand") String expand,
                                               @RequestBody(description = "Relationship object to be updated", required = true) RelationshipEntity model) throws Exception {
    if (StringUtils.isBlank(id)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    model.setReceiver(relationship.getReceiver().getRemoteId());
    model.setSender(relationship.getSender().getRemoteId());
    return updateUsersRelationship(uriInfo, model, expand);
  }

  @DELETE
  @RolesAllowed("users")
  @Path("{id}")
  @Operation(summary = "Deletes a specific relationship of user by id", method = "DELETE", description = "This deletes the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  @Deprecated
  public Response deleteUsersRelationshipsById(@Context UriInfo uriInfo,
                                               @Parameter(description = "Relationship id", required = true) @PathParam("id") String id,
                                               @Parameter(description = "Asking for a full representation of a specific subresource if any") @QueryParam("expand") String expand) throws Exception {
    if (StringUtils.isBlank(id)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return deleteUsersRelationship(uriInfo,
                                   relationship.getSender().getRemoteId(),
                                   relationship.getReceiver().getRemoteId(),
                                   expand);
  }

  @GET
  @RolesAllowed("users")
  @Path("{sender}/{receiver}")
  @Operation(summary = "Gets a specific relationship of user with another user", method = "GET", description = "This returns the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getUsersRelationship(@Context UriInfo uriInfo,
                                       @Parameter(description = "Relationship id", required = true) @PathParam("sender") String sender,
                                       @Parameter(description = "Relationship id", required = true) @PathParam("receiver") String receiver,
                                       @Parameter(description = "Asking for a full representation of a specific subresource, ex: sender or receiver") @QueryParam("expand") String expand) {
    checkCurrentUserIsPartOfRelationship(sender, receiver);

    Identity senderIdentity = getUserIdentity(sender);
    Identity receiverIdentity = getUserIdentity(receiver);
    Relationship relationship = relationshipManager.get(receiverIdentity, senderIdentity);

    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @PUT
  @RolesAllowed("users")
  @Operation(summary = "Updates a specific relationship of two users. One of them must be the current user.", method = "PUT", description = "Return updated relationship")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response updateUsersRelationship(@Context UriInfo uriInfo,
                                          @RequestBody(description = "Relationship object to be updated", required = true) RelationshipEntity model,
                                          @Parameter(description = "Asking for a full representation of a specific subresource, ex: sender or receiver") @QueryParam("expand") String expand) {
    if (model == null || StringUtils.isBlank(model.getStatus()) || StringUtils.isBlank(model.getSender())
        || StringUtils.isBlank(model.getReceiver())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    String currentUser = getCurrentUser();
    String receiver = model.getReceiver();
    String sender = model.getSender();
    checkCurrentUserIsPartOfRelationship(sender, receiver);

    Relationship.Type status = Relationship.Type.valueOf(model.getStatus().toUpperCase());

    Identity currentUserIdentity = getCurrentUserIdentity();
    String otherUser = StringUtils.equals(sender, currentUser) ? receiver : sender;
    Identity otherUserIdentity = getUserIdentity(otherUser);
    Relationship relationship = relationshipManager.get(currentUserIdentity, otherUserIdentity);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    receiver = relationship.getReceiver().getRemoteId();
    sender = relationship.getSender().getRemoteId();

    if (StringUtils.equals(sender, currentUser) && status != Type.IGNORED) {
      // The sender must not be able to change his request to anything but
      // IGNORED
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    } else if (StringUtils.equals(receiver, currentUser) && status == Type.PENDING) {
      // The user asked to connect with a user that already submitted a
      // connection request to the same user, so transform the relationship
      // type to CONFIRMED
      status = Type.CONFIRMED;
    }

    relationship = saveRelationship(currentUserIdentity, otherUserIdentity, relationship, status);
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @DELETE
  @Path("{sender}/{receiver}")
  @RolesAllowed("users")
  @Operation(summary = "Deletes a specific relationship of two users.One of them must be currrent user", method = "DELETE", description = "Return the deleted relationship")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response deleteUsersRelationship(@Context UriInfo uriInfo,
                                          @Parameter(description = "Relationship sender", required = true) @PathParam("sender") String sender,
                                          @Parameter(description = "Relationship receiver", required = true) @PathParam("receiver") String receiver,
                                          @Parameter(description = "Asking for a full representation of a specific subresource, ex: sender or receiver") @QueryParam("expand") String expand) {
    checkCurrentUserIsPartOfRelationship(sender, receiver);
    Identity senderIdentity = getUserIdentity(sender);
    Identity receiverIdentity = getUserIdentity(receiver);
    Relationship relationship = relationshipManager.get(receiverIdentity, senderIdentity);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    relationshipManager.delete(relationship);
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                     uriInfo,
                                     RestUtils.getJsonMediaType(),
                                     Response.Status.OK);
  }

  @SuppressWarnings("incomplete-switch")
  private Relationship saveRelationship(Identity currentUserIdentity,
                                        Identity otherUserIdentity,
                                        Relationship existingRelationship,
                                        Type newStatus) {
    Type oldStatus = existingRelationship == null ? null : existingRelationship.getStatus();
    switch (newStatus) { // NOSONAR
    case PENDING:
      if (oldStatus == null || oldStatus == Type.IGNORED) {
        return relationshipManager.inviteToConnect(currentUserIdentity, otherUserIdentity);
      }
      break;
    case IGNORED:
      if (existingRelationship != null) {
        relationshipManager.delete(existingRelationship);
      } else {
        relationshipManager.ignore(currentUserIdentity, otherUserIdentity);
      }
      break;
    case CONFIRMED:
      if (existingRelationship != null && oldStatus == Type.PENDING
          && currentUserIdentity.getRemoteId().equals(existingRelationship.getReceiver().getRemoteId())) {
        relationshipManager.confirm(currentUserIdentity, otherUserIdentity);
      }
    }
    return relationshipManager.get(currentUserIdentity, otherUserIdentity);
  }

  private void checkCurrentUserIsPartOfRelationship(String sender, String receiver) {
    String currentUser = getCurrentUser();
    if (!StringUtils.equals(receiver, currentUser) && !StringUtils.equals(sender, currentUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
  }

}
