/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.rest.api;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.profile.settings.ProfilePropertySettingsService;
import org.exoplatform.social.core.profilepropertysetting.model.ProfilePropertySetting;
import org.exoplatform.social.service.rest.api.VersionResources;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Path(VersionResources.VERSION_ONE + "/social/profile/settings")
@Tag(name = VersionResources.VERSION_ONE + "/social/profile/settings\"", description = "Operations on profile settings")
public class ProfileSettingsRest implements ResourceContainer {

  private static final Log          LOG                         = ExoLogger.getLogger(ProfileSettingsRest.class);

  private final ProfilePropertySettingsService profilePropertySettingsService;

  public ProfileSettingsRest(ProfilePropertySettingsService profilePropertySettingsService) {
    this.profilePropertySettingsService = profilePropertySettingsService;
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
          summary = "Gets all profile settings",
          method = "GET",
          description = "This returns a list of profile settings")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Request fulfilled"),
    @ApiResponse(responseCode = "500", description = "Internal server error"),
    @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response getPropertySettings(@Context
  UriInfo uriInfo, @Context
  Request request) {
    long currentIdentityId = RestUtils.getCurrentUserIdentityId();
    if (currentIdentityId == 0) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      return Response.ok(profilePropertySettingsService.getPropertySettings()).build();
    }catch (Exception e) {
      LOG.error("An error occurred while getting list of settings", e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @RolesAllowed("administrators")
  @Operation(
          summary = "Creates a Profile property setting",
          method = "POST",
          description = "\"Creates a Profile property setting.")
  @ApiResponses(value = { 
    @ApiResponse (responseCode = "200", description = "Request fulfilled"),
    @ApiResponse (responseCode = "500", description = "Internal server error"),
    @ApiResponse (responseCode = "400", description = "Invalid query input"),
    @ApiResponse(responseCode = "409", description = "Conflict"),
    @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response createPropertySetting(@Context UriInfo uriInfo,
                              @RequestBody(description = "Profile property setting object to be created"
                                                , required = true) ProfilePropertySetting profilePropertySetting) {
    long currentIdentityId = RestUtils.getCurrentUserIdentityId();
    if (currentIdentityId == 0) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (profilePropertySetting == null || profilePropertySetting.getPropertyName() == null ) {
      return Response.status(Status.BAD_REQUEST).entity("Profile property setting is null or property name not provided").build();
    }
    try {
      profilePropertySettingsService.createPropertySetting(profilePropertySetting);
      return Response.ok().build();
    } catch (ObjectAlreadyExistsException ex) {
      LOG.warn("property.already.exist", ex);
      return Response.status(Status.CONFLICT).build();
    }catch (Exception ex) {
      LOG.warn("Failed to create a new Property setting", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @PUT
  @RolesAllowed("administrators")
  @Operation(
          summary = "Update a Profile property setting",
          method = "PUT",
          description = "\"update a Profile property setting.")
  @ApiResponses(value = {
          @ApiResponse (responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "500", description = "Internal server error"),
          @ApiResponse (responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response updatePropertySetting(@Context UriInfo uriInfo,
                              @RequestBody(description = "Profile property setting object to be updated"
                                                , required = true) ProfilePropertySetting profilePropertySetting) {
    long currentIdentityId = RestUtils.getCurrentUserIdentityId();
    if (currentIdentityId == 0) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (profilePropertySetting == null) {
      return Response.status(Status.BAD_REQUEST).entity("Profile property setting is null").build();
    }
    try {
      profilePropertySettingsService.updatePropertySetting(profilePropertySetting);
      return Response.ok().build();
    } catch (Exception ex) {
      LOG.warn("Failed to update the Property setting", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * {@inheritDoc}
   */
  @DELETE
  @RolesAllowed("administrators")
  @Operation(
          summary = "delete a Profile property setting",
          method = "DELETE",
          description = "\"delete a Profile property setting.")
  @ApiResponses(value = {
          @ApiResponse (responseCode = "200", description = "Request fulfilled"),
          @ApiResponse (responseCode = "500", description = "Internal server error"),
          @ApiResponse (responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response deletePropertySetting(@Context UriInfo uriInfo,
                              @RequestBody(description = "Profile property setting object to be updated"
                                                , required = true) ProfilePropertySetting profilePropertySetting) {
    long currentIdentityId = RestUtils.getCurrentUserIdentityId();
    if (currentIdentityId == 0) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    if (profilePropertySetting == null) {
      return Response.status(Status.BAD_REQUEST).entity("Profile property setting is null").build();
    }
    try {
      profilePropertySettingsService.deleteProfilePropertySetting(profilePropertySetting.getId());
      return Response.ok().build();
    } catch (Exception ex) {
      LOG.warn("Failed to update the Property setting", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

}
