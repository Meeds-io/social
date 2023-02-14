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

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.model.ProfileLabel;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/profile/label")
@Tag(name = VersionResources.VERSION_ONE + "/social/profile/label", description = "Operations on labels")
public class ProfileLabelRest implements ResourceContainer {

  private static final Log          LOG = ExoLogger.getLogger(ProfileLabelRest.class);

  private final ProfileLabelService profileLabelService;

  public ProfileLabelRest(ProfileLabelService profileLabelService) {
    this.profileLabelService = profileLabelService;
  }

  @GET
  @Path("{type}/{id}/{language}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Gets  label for given object and language", method = "GET", description = "This returns label for given object and language")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getLabel(@Context
  UriInfo uriInfo, @Context
  Request request,
                           @Parameter(description = "Object type", required = true)
                           @PathParam("type")
                           String type,
                           @Parameter(description = "Object id", required = true)
                           @PathParam("id")
                           String id,
                           @Parameter(description = "language", required = true)
                           @PathParam("id")
                           String language) {
    try {
      ProfileLabel profileLabel = profileLabelService.findLabelByObjectTypeAndObjectIdAndLang(type, id, language);
      return Response.ok(profileLabel).build();
    } catch (Exception e) {
      LOG.error("An error occurred while getting list of labels for object type {} and object id {} and language {}",
                type,
                id,
                language,
                e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @GET
  @Path("labels/{type}/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Gets all labels of given object", method = "GET", description = "This returns a list of labels for given object")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getLabels(@Context
  UriInfo uriInfo, @Context
  Request request,
                            @Parameter(description = "Object type", required = true)
                            @PathParam("type")
                            String type,
                            @Parameter(description = "Object id", required = true)
                            @PathParam("id")
                            String id) {
    try {
      List<ProfileLabel> profileLabels = profileLabelService.findLabelByObjectTypeAndObjectId(type, id);
      return Response.ok(profileLabels).build();
    } catch (Exception e) {
      LOG.error("An error occurred while getting list of labels for object type {} and object id {}", type, id, e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @POST
  @RolesAllowed("administrators")
  @Operation(summary = "Creates a Profile label", method = "POST", description = "Creates a Profile label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "409", description = "Conflict") })
  public Response createLabel(@Context
  UriInfo uriInfo, @RequestBody(description = "Profile label object to be created", required = true)
  ProfileLabel profileLabel) {
    if (profileLabel == null || profileLabel.getObjectType() == null || profileLabel.getLanguage() == null
        || profileLabel.getLabel() == null || profileLabel.getObjectId() == null) {
      return Response.status(Status.BAD_REQUEST).entity("Profile label is null or an object property is not provided").build();
    }
    try {
      profileLabelService.createLabel(profileLabel);
      return Response.noContent().build();
    } catch (ObjectAlreadyExistsException ex) {
      LOG.warn("Cannot add a profile label with type {}, object Id {} and language {} , it is already created",
               profileLabel.getObjectType(),
               profileLabel.getObjectId(),
               profileLabel.getLanguage(),
               ex);
      return Response.status(Status.CONFLICT).build();
    } catch (Exception ex) {
      LOG.warn("Failed to create a new profile label with type {}, object Id {} and language {}",
               profileLabel.getObjectType(),
               profileLabel.getObjectId(),
               profileLabel.getLanguage(),
               ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @POST
  @Path("bulk")
  @RolesAllowed("administrators")
  @Operation(summary = "Creates a list of new Labels", method = "POST", description = "Creates a list of Labels.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response createLabels(@Context
  UriInfo uriInfo,
                               @Parameter(description = "Object type", required = true)
                               @RequestBody(description = "Profile label object to be created", required = true)
                               List<ProfileLabel> profileLabels) {
    if (profileLabels == null || profileLabels.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Labels list is empty").build();
    }
    try {
      for (ProfileLabel label : profileLabels) {
        profileLabelService.createLabel(label);
      }
      return Response.noContent().build();
    } catch (Exception ex) {
      LOG.warn("Failed to create  the list of profile labels f", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @PUT
  @RolesAllowed("administrators")
  @Operation(summary = "Update a Profile label", method = "PUT", description = "update a Profile label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response updateLabel(@Context
  UriInfo uriInfo, @RequestBody(description = "Profile label object to be updated", required = true)
  ProfileLabel profileLabel) {
    if (profileLabel == null) {
      return Response.status(Status.BAD_REQUEST).entity("Profile label object is null").build();
    }
    try {
      profileLabelService.updateLabel(profileLabel);
      return Response.noContent().build();
    } catch (Exception ex) {
      LOG.warn("Failed to update the profile label with type {}, object Id {} and language {}",
               profileLabel.getObjectType(),
               profileLabel.getObjectId(),
               profileLabel.getLanguage(),
               ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @PUT
  @Path("bulk")
  @RolesAllowed("administrators")
  @Operation(summary = "Update a Profile label", method = "PUT", description = "update a Profile label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response updateLabels(@Context
  UriInfo uriInfo, @RequestBody(description = "list of Labels to be updated", required = true)
  List<ProfileLabel> profileLabels) {
    if (profileLabels == null || profileLabels.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Profile label list is empty").build();
    }
    try {
      for (ProfileLabel label : profileLabels) {
        profileLabelService.updateLabel(label);
      }
      return Response.noContent().build();
    } catch (Exception ex) {
      LOG.warn("Failed to merge profile labels for the object with type {} and Id {}", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @DELETE
  @RolesAllowed("administrators")
  @Operation(summary = "delete a label", method = "DELETE", description = "\"delete a label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response deleteLabel(@Context
  UriInfo uriInfo, @RequestBody(description = "Profile label object to be deleted", required = true)
  ProfileLabel profileLabel) {
    if (profileLabel == null || profileLabel.getId() == null || profileLabel.getId() == 0L) {
      return Response.status(Status.BAD_REQUEST).entity("Profile label is null or id not provided").build();
    }
    try {
      profileLabelService.deleteLabel(profileLabel.getId());
      return Response.noContent().build();
    } catch (Exception ex) {
      LOG.warn("Failed to delete labels with type {}, object Id {} and language {}",
               profileLabel.getObjectType(),
               profileLabel.getObjectId(),
               profileLabel.getLanguage(),
               ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @DELETE
  @Path("bulk")
  @RolesAllowed("administrators")
  @Operation(summary = "delete a profile labels", method = "DELETE", description = "delete a list of Profile labels.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input") })
  public Response deleteLabels(@Context
  UriInfo uriInfo, @RequestBody(description = "list of Labels to be deleted", required = true)
  List<ProfileLabel> profileLabels) {
    if (profileLabels == null || profileLabels.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Profile label list is null or empty").build();
    }
    try {
      for (ProfileLabel label : profileLabels) {
        profileLabelService.deleteLabel(label.getId());
      }
      return Response.noContent().build();
    } catch (Exception ex) {
      LOG.warn("Failed to delete list of profile labels", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

}
