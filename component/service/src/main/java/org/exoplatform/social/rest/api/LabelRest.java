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
import org.exoplatform.social.core.label.LabelService;
import org.exoplatform.social.core.model.Label;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path(VersionResources.VERSION_ONE + "/social/label")
@Tag(name = VersionResources.VERSION_ONE + "/social/label\"", description = "Operations on labels")
public class LabelRest implements ResourceContainer {

  private static final Log       LOG = ExoLogger.getLogger(LabelRest.class);

  private final LabelService labelService;

  public LabelRest(LabelService labelService) {
    this.labelService = labelService;
  }

  @GET
  @Path("{type}/{id}/{language}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Gets  label for given object and language", method = "GET", description = "This returns label for given object and language")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
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
      Label label = labelService.findLabelByObjectTypeAndObjectIdAndLang(type, id, language);
      return Response.ok(label).build();
    } catch (Exception e) {
      LOG.error("An error occurred while getting list of labels for object type {} and object id {} and language {}", type, id,language, e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @GET
  @Path("labels/{type}/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Gets all labels of given object", method = "GET", description = "This returns a list of labels for given object")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
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
      List<Label> labels = labelService.findLabelByObjectTypeAndObjectId(type, id);
      return Response.ok(labels).build();
    } catch (Exception e) {
      LOG.error("An error occurred while getting list of labels for object type {} and object id {}", type, id, e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @POST
  @RolesAllowed("users")
  @Operation(summary = "Creates a Label", method = "POST", description = "\"Creates a Label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "409", description = "Conflict"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response createLabel(@Context
  UriInfo uriInfo, @RequestBody(description = "Label object to be created", required = true)
  Label label) {
    if (label == null || label.getObjectType() == null || label.getLanguage() == null || label.getLabel() == null
        || label.getObjectId() == null) {
      return Response.status(Status.BAD_REQUEST).entity("Label is null or an object property is not provided").build();
    }
    try {
      labelService.createLabel(label);
      return Response.ok().build();
    } catch (ObjectAlreadyExistsException ex) {
      LOG.warn("Cannot add a label with type {}, object Id {} and language {} , it is already created",
               label.getObjectType(),
               label.getObjectId(),
               label.getLanguage(),
               ex);
      return Response.status(Status.CONFLICT).build();
    } catch (Exception ex) {
      LOG.warn("Failed to create a new label with type {}, object Id {} and language {}",
               label.getObjectType(),
               label.getObjectId(),
               label.getLanguage(),
               ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @POST
  @Path("labels/{type}/{id}")
  @RolesAllowed("users")
  @Operation(summary = "Creates a list of new Labels", method = "POST", description = "\"Creates a list of Labels.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "409", description = "Conflict"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response createLabels(@Context
  UriInfo uriInfo,
                               @Parameter(description = "Object type", required = true)
                               @PathParam("type")
                               String type,
                               @Parameter(description = "Object id", required = true)
                               @PathParam("id")
                               String id,
                               @RequestBody(description = "Label object to be created", required = true)
                               List<Label> labels) {
    if (labels == null || labels.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Labels list is empty").build();
    }
    try {
      labelService.createLabels(labels, type, id);
      return Response.ok().build();
    } catch (Exception ex) {
      LOG.warn("Failed to create  the list of labels for the object with type {} and Id {}", type, id, ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @PUT
  @RolesAllowed("users")
  @Operation(summary = "Update a Label", method = "PUT", description = "\"update a Label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response updateLabel(@Context
  UriInfo uriInfo, @RequestBody(description = "Label object to be updated", required = true)
  Label label) {
    if (label == null) {
      return Response.status(Status.BAD_REQUEST).entity("Label object is null").build();
    }
    try {
      labelService.updateLabel(label);
      return Response.ok().build();
    } catch (Exception ex) {
      LOG.warn("Failed to update the label with type {}, object Id {} and language {}",
               label.getObjectType(),
               label.getObjectId(),
               label.getLanguage(),
               ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @PUT
  @Path("labels/{type}/{id}")
  @RolesAllowed("users")
  @Operation(summary = "Update a Label", method = "PUT", description = "\"update a Label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response margeLabels(@Context
  UriInfo uriInfo,
                              @Parameter(description = "Object type", required = true)
                              @PathParam("type")
                              String type,
                              @Parameter(description = "Object id", required = true)
                              @PathParam("id")
                              String id,
                              @RequestBody(description = "list of Labels to be updated", required = true)
                              List<Label> labels) {
    if (labels == null || labels.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Label list is empty").build();
    }
    try {
      labelService.mergeLabels(labels, type, id);
      return Response.ok().build();
    } catch (Exception ex) {
      LOG.warn("Failed to merge labels for the object with type {} and Id {}", type, id, ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  @DELETE
  @RolesAllowed("users")
  @Operation(summary = "delete a Label", method = "DELETE", description = "\"delete a Label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response deleteLabel(@Context
  UriInfo uriInfo, @RequestBody(description = "Label object to be deleted", required = true)
  Label label) {
    if (label == null || label.getId() == null || label.getId() == 0L) {
      return Response.status(Status.BAD_REQUEST).entity("Label is null or id not provided").build();
    }
    try {
      labelService.deleteLabel(label.getId());
      return Response.ok().build();
    } catch (Exception ex) {
      LOG.warn("Failed to delete label with type {}, object Id {} and language {}",
              label.getObjectType(),
              label.getObjectId(),
              label.getLanguage(),
              ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }
  @DELETE
  @Path("labels")
  @RolesAllowed("users")
  @Operation(summary = "delete a Label", method = "DELETE", description = "\"delete a list of Label.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation") })
  public Response deleteLabels(@Context
  UriInfo uriInfo, @RequestBody(description = "list of Labels to be deleted", required = true)
  List<Label> labels) {
    if (labels == null || labels.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Label list is null or empty").build();
    }
    try {
      labelService.deleteLabels(labels);
      return Response.ok().build();
    } catch (Exception ex) {
      LOG.warn("Failed to delete lst of labels", ex);
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

}
