/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.social.rest.impl.spacesadministration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.SocialRest;
import org.exoplatform.social.rest.entity.MembershipEntityWrapper;
import org.exoplatform.social.rest.entity.SpacesAdministrationMembershipsEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

@Path(VersionResources.VERSION_ONE + "/social/spacesAdministration")
@Tag(name = VersionResources.VERSION_ONE + "/social/spacesAdministration", description = "Managing Spaces Administration settings")
public class SpacesAdministrationRestResourcesV1 implements SocialRest {

  private SpacesAdministrationService spacesAdministrationService;

  private UserACL userACL;

  public SpacesAdministrationRestResourcesV1(SpacesAdministrationService spacesAdministrationService,
                                             UserACL userACL) {
    this.spacesAdministrationService = spacesAdministrationService;
    this.userACL = userACL;
  }

  @GET
  @Path("permissions")
  @RolesAllowed("administrators")
  @Operation(
          summary = "Gets all spaces administrators permissions settings",
          method = "GET",
          description = "This returns space memberships in the following cases: <br/><ul><li>the sender of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "401", description = "User unauthorized"),
          @ApiResponse (responseCode = "404", description = "Resource not found"),
          @ApiResponse (responseCode = "500", description = "Internal server error"),
          @ApiResponse (responseCode = "400", description = "Invalid query input") })
  public Response getAllSettings(@Context UriInfo uriInfo)  {

    if(!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List settings = Arrays.asList(
            new SpacesAdministrationMembershipsEntity("spacesAdministrators", spacesAdministrationService.getSpacesAdministratorsMemberships()),
            new SpacesAdministrationMembershipsEntity("spacesCreators", spacesAdministrationService.getSpacesCreatorsMemberships())
    );

    return EntityBuilder.getResponse(settings, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("permissions/spacesAdministrators")
  @RolesAllowed("administrators")
  @Operation(
          summary = "Gets spaces administrators memberships",
          method = "GET",
          description = "This returns space memberships in the following cases: <br/><ul><li>the sender of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = { 
          @ApiResponse (responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "401", description = "User not authorized to call this endpoint"),
          @ApiResponse (responseCode = "404", description = "Resource not found"),
          @ApiResponse (responseCode = "500", description = "Internal server error"),
          @ApiResponse (responseCode = "400", description = "Invalid query input") })
  public Response getSpacesAdministrators(@Context UriInfo uriInfo) {

    if(!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<MembershipEntry> memberships = spacesAdministrationService.getSpacesAdministratorsMemberships();
    
    return EntityBuilder.getResponse(new SpacesAdministrationMembershipsEntity("spacesAdministrators", memberships), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("permissions/spacesCreators")
  @RolesAllowed("administrators")
  @Operation(
          summary = "Gets spaces creators memberships",
          method = "GET",
          description = "This returns space memberships in the following cases: <br/><ul><li>the sender of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = {
          @ApiResponse (responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "401", description = "User not authorized to call this endpoint"),
          @ApiResponse (responseCode = "404", description = "Resource not found"),
          @ApiResponse (responseCode = "500", description = "Internal server error"),
          @ApiResponse (responseCode = "400", description = "Invalid query input") })
  public Response getSpacesCreators(@Context UriInfo uriInfo) {

    if(!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<MembershipEntry> memberships = spacesAdministrationService.getSpacesCreatorsMemberships();

    return EntityBuilder.getResponse(new SpacesAdministrationMembershipsEntity("spacesCreators", memberships), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }


  @GET
  @Path("permissions/canCreatespaces/{username}")
  @RolesAllowed("users")
  @Operation(
          summary = "Check if members can create spaces",
          method = "GET",
          description = "This returns if members can add spaces")
  @ApiResponses(value = {
      @ApiResponse (responseCode = "200", description = "Request fulfilled"),
      @ApiResponse (responseCode = "401", description = "User not authorized to call this endpoint"),
      @ApiResponse (responseCode = "404", description = "Resource not found"),
      @ApiResponse (responseCode = "500", description = "Internal server error")})
  public Response canCreatespaces(@Context UriInfo uriInfo, @Parameter(description = "Username", required = true) @PathParam("username") String username) {

    Boolean canCreateSpaces = spacesAdministrationService.canCreateSpace(username);

    return EntityBuilder.getResponse(canCreateSpaces.toString(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("permissions/spacesAdministrators")
  @RolesAllowed("administrators")
  @Operation(
          summary = "Updates spaces creators memberships",
          method = "PUT",
          description = "This updates the space membership in the following cases: <br/><ul><li>the user of the space membership is the authenticated user  but he cannot update his own membership to \"approved\" for a space with a \"validation\" subscription</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = { 
          @ApiResponse (responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "401", description = "User not authorized to call this endpoint"),
          @ApiResponse (responseCode = "500", description = "Internal server error") })
  public Response updateSpacesAdministrators(@Context UriInfo uriInfo,
                                             @RequestBody(description = "Space membership object to be updated", required = true) List<MembershipEntityWrapper> model) {

    if(!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<MembershipEntry> memberships = model.stream()
            .map(m -> new MembershipEntry(m.getGroup(), m.getMembershipType()))
            .collect(Collectors.toList());

    spacesAdministrationService.updateSpacesAdministratorsMemberships(memberships);

    return EntityBuilder.getResponse("", uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @PUT
  @Path("permissions/spacesCreators")
  @RolesAllowed("administrators")
  @Operation(
          summary = "Updates spaces creators memberships",
          method = "PUT",
          description = "This updates the space membership in the following cases: <br/><ul><li>the user of the space membership is the authenticated user  but he cannot update his own membership to \"approved\" for a space with a \"validation\" subscription</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = {
          @ApiResponse (responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "401", description = "User not authorized to call this endpoint"),
          @ApiResponse (responseCode = "500", description = "Internal server error") })
  public Response updateSpacesCreators(@Context UriInfo uriInfo,
                                       @RequestBody(description = "Space membership object to be updated", required = true) List<MembershipEntityWrapper> model) {

    if(!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<MembershipEntry> memberships = model.stream()
            .map(m -> new MembershipEntry(m.getGroup(), m.getMembershipType()))
            .collect(Collectors.toList());

    spacesAdministrationService.updateSpacesCreatorsMemberships(memberships);

    return EntityBuilder.getResponse("", uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
