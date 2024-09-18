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
package org.exoplatform.social.rest.impl.spacemembership;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.SpaceMembershipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/spacesMemberships")
@Tag(name = VersionResources.VERSION_ONE + "/social/spacesMemberships", description = "Managing memberships of users in a space")
public class SpaceMembershipRest implements ResourceContainer {

  private SpaceService    spaceService;

  private IdentityManager identityManager;

  private enum MembershipType {
    ALL, PENDING, APPROVED, IGNORED, INVITED, MEMBER, MANAGER
  }

  public SpaceMembershipRest(SpaceService spaceService, IdentityManager identityManager) {
    this.spaceService = spaceService;
    this.identityManager = identityManager;
  }

  @GET
  @RolesAllowed("users")
  @Operation(
             summary = "Gets space memberships",
             method = "GET",
             description = "This returns space memberships in the following cases: <br/><ul><li>the sender of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "404", description = "Resource not found"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response getSpacesMemberships(
                                       @Context
                                       UriInfo uriInfo,
                                       @Parameter(description = "Space technical identifier to include to get membership")
                                       @QueryParam("space")
                                       String spaceId,
                                       @Parameter(description = "User name to filter only memberships of the given user")
                                       @QueryParam("user")
                                       String user,
                                       @Parameter(description = "Type of membership to get (All, Pending, Approved, Invited)")
                                       @QueryParam("status")
                                       String status,
                                       @Parameter(description = "Search query")
                                       @QueryParam("query")
                                       String query,
                                       @Parameter(description = "Offset", required = false)
                                       @Schema(defaultValue = "0")
                                       @QueryParam("offset")
                                       int offset,
                                       @Parameter(description = "Limit", required = false)
                                       @Schema(defaultValue = "20")
                                       @QueryParam("limit")
                                       int limit,
                                       @Parameter(description = "Asking for a full representation of a specific subresource if any")
                                       @QueryParam("expand")
                                       String expand,
                                       @Parameter(description = "Returning the number of memberships or not")
                                       @Schema(defaultValue = "false")
                                       @QueryParam("returnSize")
                                       boolean returnSize) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();

    MembershipType membershipType;
    if (StringUtils.isBlank(status)) {
      membershipType = MembershipType.ALL;
    } else {
      try {
        membershipType = MembershipType.valueOf(status.toUpperCase());
      } catch (Exception e) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Status is not managed").build();
      }
    }

    if (StringUtils.isBlank(user)) {
      user = authenticatedUser;
    }

    Space space = null;
    if (StringUtils.isNotBlank(spaceId)) {
      space = spaceService.getSpaceById(spaceId);
    }

    if (!canRetrieveSpaceMemberships(space, user, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    ListAccess<Space> listAccess = null;
    SpaceFilter spaceFilter = new SpaceFilter();
    if (space != null) {
      spaceFilter.setIncludeSpaces(Collections.singletonList(space));
    }
    if (query != null) {
      spaceFilter.setSpaceNameSearchCondition(query);
    }
    switch (membershipType) {
    case PENDING: {
      listAccess = spaceService.getPendingSpacesByFilter(user, spaceFilter);
      break;
    }
    case APPROVED, MEMBER: {
      listAccess = spaceService.getMemberSpacesByFilter(user, spaceFilter);
      break;
    }
    case MANAGER: {
      listAccess = spaceService.getManagerSpacesByFilter(user, spaceFilter);
      break;
    }
    case INVITED: {
      listAccess = spaceService.getInvitedSpacesByFilter(user, spaceFilter);
      break;
    }
    default:
      listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
      break;
    }
    List<DataEntity> spaceMemberships = getSpaceMemberships(Arrays.asList(listAccess.load(offset, limit)),
                                                            user,
                                                            uriInfo.getPath(),
                                                            expand);
    CollectionEntity spacesMemberships = new CollectionEntity(spaceMemberships,
                                                              EntityBuilder.SPACES_MEMBERSHIP_TYPE,
                                                              offset,
                                                              limit);
    if (returnSize) {
      spacesMemberships.setSize(listAccess.getSize());
    }
    return EntityBuilder.getResponseBuilder(spacesMemberships,
                                            uriInfo,
                                            RestUtils.getJsonMediaType(),
                                            Response.Status.OK)
                        .build();
  }

  @POST
  @RolesAllowed("users")
  @Operation(
             summary = "Creates a space membership for a specific user",
             method = "POST",
             description = "This creates the space membership in the following cases: <br/><ul><li>the sender of the space membership is the authenticated user and the space subscription is open</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "500", description = "Internal server error"),
                          @ApiResponse(responseCode = "400", description = "Invalid query input"),
  })
  public Response addSpacesMemberships(
                                       @RequestBody(description = "Space membership object to be created, ex:<br />{" +
                                           "<br />\"role\": \"manager\"," +
                                           "<br />\"user\": \"john\"," +
                                           "<br />\"space\": \"1552\"" +
                                           "<br />}", required = true)
                                       SpaceMembershipEntity model) {
    if (model == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if (model.getSpace() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Space is null").build();
    }
    if (model.getUser() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("User is null").build();
    }
    if (model.getRole() == null && model.getStatus() == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Role and Status are null").build();
    }
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    String user = model.getUser();
    String spaceId = model.getSpace();
    String status = StringUtils.lowerCase(model.getStatus());
    String role = StringUtils.lowerCase(model.getRole());
    //
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (identityManager.getOrCreateUserIdentity(user) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    boolean canManageSpace = spaceService.canManageSpace(space, authenticatedUser);
    boolean selfMembership = authenticatedUser.equals(user);
    if (MembershipType.IGNORED.name().equalsIgnoreCase(status)) {
      if (!canManageSpace && !selfMembership) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      } else if (spaceService.isMember(space, user)) {
        throw new WebApplicationException(Response.Status.CONFLICT);
      } else if (spaceService.isPendingUser(space, user)) {
        spaceService.removePendingUser(space, user);
      } else if (spaceService.isInvitedUser(space, user)) {
        spaceService.removeInvitedUser(space, user);
      }
      if (selfMembership) {
        spaceService.setIgnored(space.getId(), user);
      }
    } else if (MembershipType.APPROVED.name().equalsIgnoreCase(status)) {
      if (!canManageSpace && !selfMembership) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      } else if (spaceService.isMember(space, user)) {
        throw new WebApplicationException(Response.Status.CONFLICT);
      } else if (spaceService.isInvitedUser(space, user) && selfMembership) {
        spaceService.addMember(space, authenticatedUser);
      } else if (spaceService.isPendingUser(space, user) && canManageSpace) {
        spaceService.addMember(space, authenticatedUser);
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else if (MembershipType.PENDING.name().equalsIgnoreCase(status)) {
      if (!selfMembership) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      } else if (spaceService.isInvitedUser(space, user)) {
        spaceService.addMember(space, authenticatedUser);
      } else if (spaceService.isMember(space, user)
                 || spaceService.isPendingUser(space, user)) {
        throw new WebApplicationException(Response.Status.CONFLICT);
      } else if (space.getRegistration().equals(Space.OPEN)) {
        spaceService.addMember(space, user);
      } else if (space.getVisibility().equals(Space.HIDDEN)
                 || space.getRegistration().equals(Space.CLOSED)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      } else {
        spaceService.addPendingUser(space, user);
      }
    } else if (MembershipType.INVITED.name().equalsIgnoreCase(status)) {
      if (!canManageSpace) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      } else if (spaceService.isMember(space, user)
                 || spaceService.isInvitedUser(space, user)) {
        throw new WebApplicationException(Response.Status.CONFLICT);
      } else if (spaceService.isInvitedUser(space, user)) {
        spaceService.addMember(space, authenticatedUser);
      } else {
        spaceService.addInvitedUser(space, authenticatedUser);
      }
    } else if (StringUtils.isNotBlank(status)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Status is not managed").build();
    } else if (isAddSelfToSpace(space, user, role, authenticatedUser)) {
      spaceService.addMember(space, user);
    } else if (canManageSpace) {
      if (SpaceUtils.MANAGER.equalsIgnoreCase(role)) {
        spaceService.addMember(space, user);
        spaceService.setManager(space, user, true);
      } else if (SpaceUtils.REDACTOR.equalsIgnoreCase(role)) {
        spaceService.addMember(space, user);
        spaceService.addRedactor(space, user);
      } else if (SpaceUtils.PUBLISHER.equalsIgnoreCase(role)) {
        spaceService.addMember(space, user);
        spaceService.addPublisher(space, user);
      } else if (SpaceUtils.MEMBER.equalsIgnoreCase(role)
                 || StringUtils.isBlank(role)) {
        spaceService.addMember(space, user);
      } else {
        return Response.status(Response.Status.BAD_REQUEST).entity("Role is not managed").build();
      }
    } else {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return Response.noContent().build();
  }

  @DELETE
  @RolesAllowed("users")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
             summary = "Deletes a specific space membership by id",
             method = "DELETE",
             description = "This deletes the space membership in the following cases: <br/><ul><li>the user of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = {
                          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
                          @ApiResponse(responseCode = "404", description = "Resource not found"),
                          @ApiResponse(responseCode = "412",
                                       description = "Precondition is not acceptable. For instance, the last manager membership could not be removed."),
                          @ApiResponse(responseCode = "500", description = "Internal server error due to data encoding") })
  public Response deleteSpaceMembershipById(
                                            @RequestBody(description = "Space membership object to be created, ex:<br />{" +
                                                "<br />\"role\": \"manager\"," +
                                                "<br />\"user\": \"john\"," +
                                                "<br />\"space\": \"1552\"" +
                                                "<br />}", required = true)
                                            SpaceMembershipEntity model) {
    if (model == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if (StringUtils.isBlank(model.getUser())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("User is null").build();
    }
    if (StringUtils.isBlank(model.getSpace())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Space is null").build();
    }
    if (StringUtils.isBlank(model.getRole())) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Role is null").build();
    }
    //
    String user = model.getUser();
    Space space = spaceService.getSpaceById(model.getSpace());
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    boolean canManageSpace = spaceService.canManageSpace(space, authenticatedUser);
    boolean selfMembership = authenticatedUser.equals(user);
    if (!selfMembership && !canManageSpace) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    String role = model.getRole();
    if ((role.equals(SpaceUtils.MEMBER) || role.equals(SpaceUtils.MANAGER))
        && spaceService.isOnlyManager(space, user)) {
      throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
    }
    space.setEditor(authenticatedUser);
    if (selfMembership && role.equalsIgnoreCase(SpaceUtils.MEMBER)) {
      spaceService.removeMember(space, user);
    } else if (canManageSpace) {
      if (role.equalsIgnoreCase(SpaceUtils.REDACTOR)) {
        spaceService.removeRedactor(space, user);
      } else if (role.equalsIgnoreCase(SpaceUtils.PUBLISHER)) {
        spaceService.removePublisher(space, user);
      } else if (role.equalsIgnoreCase(SpaceUtils.MANAGER)) {
        spaceService.setManager(space, user, false);
      } else if (role.equalsIgnoreCase(SpaceUtils.MEMBER)) {
        spaceService.removeMember(space, user);
      } else {
        return Response.status(Response.Status.BAD_REQUEST).entity("Role is not managed").build();
      }
    } else {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return Response.noContent().build();
  }

  private List<DataEntity> getSpaceMemberships(List<Space> spaces, String userId, String path, String expand) {
    List<DataEntity> spaceMemberships = new ArrayList<>();
    SpaceMembershipEntity membershipEntity = null;
    for (Space space : spaces) {
      if (spaceService.isMember(space, userId)) {
        membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, SpaceUtils.MEMBER, path, expand);
        spaceMemberships.add(membershipEntity.getDataEntity());
      }
      if (spaceService.isManager(space, userId)) {
        membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, SpaceUtils.MANAGER, path, expand);
        spaceMemberships.add(membershipEntity.getDataEntity());
      }
      if (spaceService.isPublisher(space, userId)) {
        membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, SpaceUtils.PUBLISHER, path, expand);
        spaceMemberships.add(membershipEntity.getDataEntity());
      }
      if (spaceService.isRedactor(space, userId)) {
        membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, SpaceUtils.REDACTOR, path, expand);
        spaceMemberships.add(membershipEntity.getDataEntity());
      }
      if (spaceService.isInvitedUser(space, userId)) {
        membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "invited", path, expand);
        spaceMemberships.add(membershipEntity.getDataEntity());
      }
      if (spaceService.isPendingUser(space, userId)) {
        membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "pending", path, expand);
        spaceMemberships.add(membershipEntity.getDataEntity());
      }
    }
    return spaceMemberships;
  }

  private boolean canRetrieveSpaceMemberships(Space space, String targetUser, String authenticatedUser) {
    if (spaceService.isSuperManager(authenticatedUser)
        || (space == null && StringUtils.equals(targetUser, authenticatedUser))) {
      return true;
    } else if (space == null) {
      return StringUtils.equals(targetUser, authenticatedUser);
    } else {
      return !Space.HIDDEN.equals(space.getVisibility())
             || spaceService.canViewSpace(space, authenticatedUser)
             || spaceService.isInvitedUser(space,
                                           authenticatedUser);
    }
  }

  private boolean isAddSelfToSpace(Space space, String targetUser, String role, String authenticatedUser) {
    return SpaceUtils.MEMBER.equalsIgnoreCase(role)
           && authenticatedUser.equals(targetUser)
           && canAddSelfToSpace(space, authenticatedUser);
  }

  private boolean canAddSelfToSpace(Space space, String authenticatedUser) {
    return space.getRegistration().equals(Space.OPEN) || spaceService.isInvitedUser(space, authenticatedUser);
  }

}
