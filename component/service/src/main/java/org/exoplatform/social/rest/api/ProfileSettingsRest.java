/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.rest.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path(VersionResources.VERSION_ONE + "/social/profile/settings")
@Tag(name = VersionResources.VERSION_ONE + "/social/profile/settings\"", description = "Operations on profile settings")
public class ProfileSettingsRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(ProfileSettingsRest.class);

  private static final String GROUP_ADMINISTRATORS = "/platform/administrators";

  private final ProfilePropertyService profilePropertyService;

  public ProfileSettingsRest(ProfilePropertyService profilePropertyService) {
    this.profilePropertyService = profilePropertyService;
  }


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
      List<ProfilePropertySetting> properties = profilePropertyService.getPropertySettings();
      if (!ConversationState.getCurrent().getIdentity().isMemberOf(GROUP_ADMINISTRATORS)) {
        properties = properties.stream().filter(prop -> prop.isVisible() || prop.isEditable()).toList();
      }
      List<String> unHiddenbaleProperties = profilePropertyService.getUnhiddenableProfileProperties();
      List<String> excludedQuickSearchProperties = profilePropertyService.getExcludedQuickSearchProperties();
      List<ProfilePropertySettingEntity> propertySettingEntities =
                                                                 EntityBuilder.buildEntityProfilePropertySettingList(properties,
                                                                                                                     profilePropertyService,
                                                                                                                     ProfilePropertyService.LABELS_OBJECT_TYPE,
                                                                                                                     currentIdentityId);
      Map<String, List<?>> settings = new HashMap<>();
      settings.put("settings", propertySettingEntities);
      settings.put("unHiddenableProperties", unHiddenbaleProperties);
      settings.put("excludedQuickSearchProperties", excludedQuickSearchProperties);
      return Response.ok(settings).build();
    }catch (Exception e) {
      LOG.error("An error occurred while getting list of settings", e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }


  @POST
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
          summary = "Creates a Profile property setting",
          method = "POST",
          description = "\"Creates a Profile property setting.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "409", description = "Conflict"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation")})
  public Response createPropertySetting(@Context UriInfo uriInfo,
                                        @RequestBody(description = "Profile property setting object to be created"
                                                , required = true) ProfilePropertySettingEntity profilePropertySettingEntity) {
    if (profilePropertySettingEntity == null || profilePropertySettingEntity.getPropertyName() == null) {
      return Response.status(Status.BAD_REQUEST).entity("Profile property setting is null or property name not provided").build();
    }
    try {
      ProfilePropertySetting newProperty =
                                         profilePropertyService.createPropertySetting(EntityBuilder.buildProfilePropertySettingFromEntity(profilePropertySettingEntity,
                                                                                                                                          profilePropertyService));
      return Response.ok(EntityBuilder.buildEntityProfilePropertySetting(newProperty,profilePropertyService, ProfilePropertyService.LABELS_OBJECT_TYPE)).build();
    } catch (ObjectAlreadyExistsException ex) {
      LOG.warn("Cannot add a property named {}, this name is already used",profilePropertySettingEntity.getPropertyName(), ex);
      return Response.status(Status.CONFLICT).build();
    }catch (Exception ex) {
      LOG.warn("Failed to create a new Property setting named {}",profilePropertySettingEntity.getPropertyName(), ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @PUT
  @RolesAllowed("administrators")
  @Operation(
          summary = "Update a Profile property setting",
          method = "PUT",
          description = "\"update a Profile property setting.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation")})
  public Response updatePropertySetting(@Context UriInfo uriInfo,
                                        @RequestBody(description = "Profile property setting object to be updated"
                                                , required = true) ProfilePropertySettingEntity profilePropertySettingEntity) {
    if (profilePropertySettingEntity == null) {
      return Response.status(Status.BAD_REQUEST).entity("Profile property setting is null").build();
    }
    try {
      profilePropertyService.updatePropertySetting(EntityBuilder.buildProfilePropertySettingFromEntity(profilePropertySettingEntity,
                                                                                                       profilePropertyService));
      return Response.noContent().build();
    } catch (Exception ex) {
      LOG.warn("Failed to update the Property setting", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }


  @DELETE
  @RolesAllowed("administrators")
  @Operation(
          summary = "delete a Profile property setting",
          method = "DELETE",
          description = "\"delete a Profile property setting.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation")})
  public Response deletePropertySetting(@Context UriInfo uriInfo,
                                        @RequestBody(description = "Profile property setting object to be updated"
                                                , required = true) ProfilePropertySettingEntity profilePropertySettingEntity) {
    if (profilePropertySettingEntity == null) {
      return Response.status(Status.BAD_REQUEST).entity("Profile property setting is null").build();
    }
    try {
      profilePropertyService.deleteProfilePropertySetting(profilePropertySettingEntity.getId());
      return Response.noContent().build();
    } catch (Exception ex) {
      LOG.warn("Failed to update the Property setting", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }
}
