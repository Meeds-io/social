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
package org.exoplatform.social.rest.impl.relationship;

import java.util.List;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.RelationshipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/relationships")
@Tag(name = VersionResources.VERSION_ONE + "/social/relationships", description = "Managing relationships of identities")
public class RelationshipsRestResources implements ResourceContainer {

  private RelationshipManager relationshipManager;

  private IdentityManager     identityManager;

  public RelationshipsRestResources(RelationshipManager relationshipManager,
                                    IdentityManager identityManager) {
    this.relationshipManager = relationshipManager;
    this.identityManager = identityManager;
  }

  @GET
  @RolesAllowed("users")
  @Operation(summary = "Gets relationships of identities", method = "GET", description = "This returns a list of relationships in the following cases: <br/><ul><li>the authenticated user has permissions to view the 2 objects linked to the 2 identities</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "412", description = "Precondition failed, check your input params")
  })
  public Response getRelationships(@Context
                                   UriInfo uriInfo,
                                   @Parameter(description = "Status of the target relationship: pending, confirmed or all")
                                   @QueryParam("status")
                                   String status,
                                   @Parameter(description = "Identity id which is a UUID")
                                   @QueryParam("identityId")
                                   String identityId,
                                   @Parameter(description = "Offset", required = false)
                                   @Schema(defaultValue = "0")
                                   @QueryParam("offset")
                                   int offset,
                                   @Parameter(description = "Limit", required = false)
                                   @Schema(defaultValue = "20")
                                   @QueryParam("limit")
                                   int limit,
                                   @Parameter(description = "Returning the number of relationships or not")
                                   @Schema(defaultValue = "false")
                                   @QueryParam("returnSize")
                                   boolean returnSize) throws WebApplicationException {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Type type;
    try {
      type = Type.valueOf(status.toUpperCase());
    } catch (Exception e) {
      type = Type.ALL;
    }

    List<Relationship> relationships;
    int size = 0;
    if (identityId != null && RestUtils.isMemberOfAdminGroup()) {
      Identity identity = identityManager.getIdentity(identityId);
      if (identity == null) {
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
      }
      relationships = relationshipManager.getRelationshipsByStatus(identity, type, offset, limit);
      if (returnSize) {
        size = relationshipManager.getRelationshipsCountByStatus(identity, type);
      }
    } else {
      String currentUser = getCurrentUserName();
      Identity authenticatedUser = getUserIdentity(currentUser);
      relationships = relationshipManager.getRelationshipsByStatus(authenticatedUser, type, offset, limit);
      if (returnSize) {
        size = relationshipManager.getRelationshipsCountByStatus(authenticatedUser, type);
      }
    }
    List<DataEntity> relationshipEntities = EntityBuilder.buildRelationshipEntities(relationships, uriInfo);
    CollectionEntity collectionRelationship = new CollectionEntity(relationshipEntities,
                                                                   RestProperties.RELATIONSHIPS,
                                                                   offset,
                                                                   limit);
    collectionRelationship.setSize(size);

    Response.ResponseBuilder builder = EntityBuilder.getResponseBuilder(collectionRelationship,
                                                                        uriInfo,
                                                                        RestUtils.getJsonMediaType(),
                                                                        Response.Status.OK);
    CacheControl cc = new CacheControl();
    cc.setNoStore(true);
    builder.cacheControl(cc);

    return builder.build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Creates a relationship", method = "POST", description = "This creates the relationship in the following cases: <br/><ul><li>the authenticated user has permissions to view the 2 objects linked to the 2 identities</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "204", description = "Request fulfilled and no content"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input")
  })
  public Response createRelationship(@Context
                                     UriInfo uriInfo,
                                     @Parameter(description = "Asking for a full representation of a specific subresource if any", required = false)
                                     @QueryParam("expand")
                                     String expand,
                                     @RequestBody(description = "Relationship object to be created", required = true)
                                     RelationshipEntity model) throws WebApplicationException {

    if (model == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    String senderRemoteId = model.getSender();
    String receiverRemoteId = model.getReceiver();
    if (StringUtils.isBlank(senderRemoteId) || StringUtils.isBlank(receiverRemoteId)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Type type = Type.valueOf(model.getStatus().toUpperCase());
    if (type != Type.PENDING && type != Type.IGNORED) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    String authenticatedUser = getCurrentUserName();
    if (StringUtils.equals(receiverRemoteId, authenticatedUser)) {
      String temp = receiverRemoteId;
      receiverRemoteId = senderRemoteId;
      senderRemoteId = temp;
    }
    if (!StringUtils.equals(senderRemoteId, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Identity receiver = getUserIdentity(receiverRemoteId);
    Identity currentSenderIdentity = getUserIdentity(senderRemoteId);

    if (receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    } else if (type == Type.PENDING) {
      Relationship relationship = relationshipManager.inviteToConnect(currentSenderIdentity, receiver);
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, true),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    } else if (type == Type.IGNORED) {// NOSONAR kept for code clearness
      Relationship relationship = relationshipManager.ignore(currentSenderIdentity, receiver);
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    } else {// NOSONAR unreachable, but kept in case of code evolves with more statuses to manage
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
  }

  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(summary = "Gets a specific relationship by id", method = "GET", description = "This returns the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input")
  })
  public Response getRelationshipById(@Context
                                      UriInfo uriInfo,
                                      @Parameter(description = "Relationship id", required = true)
                                      @PathParam("id")
                                      String id,
                                      @Parameter(description = "Asking for a full representation of a specific subresource if any", required = false)
                                      @QueryParam("expand")
                                      String expand) throws WebApplicationException {
    Identity authenticatedUser = getUserIdentity(getCurrentUserName());
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (!isUserInRelationship(relationship, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    } else {
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, true),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    }
  }

  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Updates a specific relationship by id", method = "PUT", description = "This updates the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "204", description = "Request fulfilled with no content"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input")
  })
  public Response updateRelationshipById(@Context
                                         UriInfo uriInfo,
                                         @Parameter(description = "Relationship id", required = true)
                                         @PathParam("id")
                                         String id,
                                         @Parameter(description = "Asking for a full representation of a specific subresource if any", required = false)
                                         @QueryParam("expand")
                                         String expand,
                                         @RequestBody(description = "Relationship object to be updated", required = true)
                                         RelationshipEntity model) throws WebApplicationException {

    if (model == null || StringUtils.isBlank(model.getStatus())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Relationship relationship = relationshipManager.get(id);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    if (StringUtils.isNotBlank(model.getReceiver())
        && StringUtils.isNotBlank(model.getSender())
        && !isUserInRelationship(model, getCurrentUserName())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    Type type = Type.valueOf(model.getStatus().toUpperCase());
    return updateRelationship(relationship, type, uriInfo, expand);
  }

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Updates a specific relationship by provided model", method = "PUT", description = "This updates the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "204", description = "Request fulfilled with no content"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input")
  })
  public Response updateRelationship(@Context
                                     UriInfo uriInfo,
                                     @Parameter(description = "Asking for a full representation of a specific subresource if any", required = false)
                                     @QueryParam("expand")
                                     String expand,
                                     @RequestBody(description = "Relationship object to be updated", required = true)
                                     RelationshipEntity model) throws WebApplicationException {

    if (model == null
        || StringUtils.isBlank(model.getStatus())
        || StringUtils.isBlank(model.getSender())
        || StringUtils.isBlank(model.getReceiver())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Identity sender = identityManager.getOrCreateUserIdentity(model.getSender());
    Identity receiver = identityManager.getOrCreateUserIdentity(model.getReceiver());
    if (sender == null
        || receiver == null
        || StringUtils.equals(receiver.getId(), sender.getId())) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Type type = Type.valueOf(model.getStatus().toUpperCase());
    Relationship relationship = relationshipManager.get(sender, receiver);
    if (relationship == null) {
      if(type == Type.IGNORED) {
        relationship = relationshipManager.ignore(sender, receiver);
        return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                         uriInfo,
                                         RestUtils.getJsonMediaType(),
                                         Response.Status.OK);
      } else {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } else {
      return updateRelationship(relationship, type, uriInfo, expand);
    }
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(summary = "Deletes a specific relationship by id", method = "DELETE", description = "This deletes the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "400", description = "Invalid query input")
  })
  public Response deleteRelationshipById(@Parameter(description = "Relationship id", required = true)
                                         @PathParam("id")
                                         String id) throws WebApplicationException {
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return deleteRelationship(relationship);
  }

  @DELETE
  @RolesAllowed("users")
  @Operation(summary = "Deletes a specific relationship by id", method = "DELETE", description = "This deletes the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "400", description = "Invalid query input")
  })
  public Response deleteRelationship(@RequestBody(description = "Relationship object to be updated", required = true)
                                     RelationshipEntity model) throws WebApplicationException {
    if (model == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    String senderRemoteId = model.getSender();
    String receiverRemoteId = model.getReceiver();
    if (StringUtils.isBlank(senderRemoteId) || StringUtils.isBlank(receiverRemoteId)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Identity sender = getUserIdentity(senderRemoteId);
    Identity receiver = getUserIdentity(receiverRemoteId);
    Relationship relationship = relationshipManager.get(sender, receiver);

    return deleteRelationship(relationship);
  }

  private Response updateRelationship(Relationship relationship, Type type, UriInfo uriInfo, String expand) {
    String currentUserName = getCurrentUserName();
    Identity authenticatedUserIdentity = getUserIdentity(currentUserName);
    if (!isUserInRelationship(relationship, authenticatedUserIdentity)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    } else if (relationship.getStatus() == type) {
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, true),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    } else if (type.equals(Type.IGNORED)) {
      relationship = relationshipManager.ignore(relationship.getSender(), relationship.getReceiver());
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    } else if (type.equals(Type.PENDING)) {
      if (isSender(relationship, authenticatedUserIdentity)) {
        relationship = relationshipManager.inviteToConnect(relationship.getSender(), relationship.getReceiver());
      } else if (isReceiver(relationship, authenticatedUserIdentity)) {
        relationship = relationshipManager.inviteToConnect(relationship.getReceiver(), relationship.getSender());
      }
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, true),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    } else if (type.equals(Type.CONFIRMED)) {
      if (!StringUtils.equals(relationship.getReceiver().getRemoteId(), currentUserName)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      relationship = relationshipManager.confirm(relationship.getSender(), relationship.getReceiver());
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  private Response deleteRelationship(Relationship relationship) {
    String authenticatedUser = getCurrentUserName();
    if (relationship == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (StringUtils.equals(relationship.getReceiver().getRemoteId(), authenticatedUser)) {
      relationshipManager.deny(relationship.getReceiver(), relationship.getSender());
    } else if (StringUtils.equals(relationship.getSender().getRemoteId(), authenticatedUser)) {
      relationshipManager.delete(relationship);
    } else {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return Response.noContent().build();
  }

  private boolean isUserInRelationship(Relationship relationship,
                                       Identity authenticatedUserIdentity) {
    return isSender(relationship, authenticatedUserIdentity) || isReceiver(relationship, authenticatedUserIdentity);
  }

  private boolean isSender(Relationship relationship,
                           Identity authenticatedUserIdentity) {
    return StringUtils.equals(relationship.getSender().getId(), authenticatedUserIdentity.getId());
  }

  private boolean isReceiver(Relationship relationship,
                             Identity authenticatedUserIdentity) {
    return StringUtils.equals(relationship.getReceiver().getId(), authenticatedUserIdentity.getId());
  }

  private boolean isUserInRelationship(RelationshipEntity model, String authenticatedUser) {
    return StringUtils.equals(model.getSender(), authenticatedUser)
        || StringUtils.equals(model.getReceiver(), authenticatedUser);
  }

  private Identity getUserIdentity(String currentUserName) {
    return identityManager.getOrCreateUserIdentity(currentUserName);
  }

  private String getCurrentUserName() {
    return ConversationState.getCurrent().getIdentity().getUserId();
  }

}
