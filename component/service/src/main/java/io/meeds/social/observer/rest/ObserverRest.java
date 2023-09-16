/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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
package io.meeds.social.observer.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.rest.api.RestUtils;

import io.meeds.social.observe.model.ObserverObject;
import io.meeds.social.observe.service.ObserverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/social/observers")
@Tag(name = "/social/observers", description = "Managing observers for any type of data")
public class ObserverRest implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(ObserverRest.class);

  private ObserverService observerService;

  public ObserverRest(ObserverService observerService) {
    this.observerService = observerService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Retrieves all observed objects by current user",
      description = "Retrieves all observed objects by current user",
      method = "GET"
  )
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"), })
  public Response getObservedObjects(
                                     @Parameter(description = "Query Offset", required = true)
                                     @QueryParam("offset")
                                     int offset,
                                     @Parameter(description = "Query results limit", required = true)
                                     @QueryParam("limit")
                                     int limit) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    List<ObserverObject> objects = observerService.getObservedObjects(userIdentityId, offset, limit);
    return Response.ok(objects).build();
  }

  @GET
  @Path("{objectType}/{objectId}")
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(
      summary = "Retrieves all observed objects by current user",
      description = "Retrieves all observed objects by current user",
      method = "GET"
  )
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"), })
  public Response isObservedObject(
                                   @Parameter(description = "Object type: activity, comment, notes ...", required = true)
                                   @PathParam("objectType")
                                   String objectType,
                                   @Parameter(description = "Object identifier: technical id to identify object", required = true)
                                   @PathParam("objectId")
                                   String objectId) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    boolean observed = observerService.isObserved(userIdentityId, objectType, objectId);
    return Response.ok(String.valueOf(observed)).build();
  }

  @POST
  @Path("{objectType}/{objectId}")
  @RolesAllowed("users")
  @Operation(
      summary = "Observe an object",
      description = "Observe an object",
      method = "POST")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "401", description = "Unauthorized"),
      }
  )
  public Response createObserver(
                                 @Parameter(description = "Object type: activity, comment, notes ...", required = true)
                                 @PathParam("objectType")
                                 String objectType,
                                 @Parameter(description = "Object identifier: technical id to identify object", required = true)
                                 @PathParam("objectId")
                                 String objectId) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      observerService.createObserver(userIdentityId, objectType, objectId);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      LOG.warn("User {} is not allowed to watch object {}/{}", userIdentityId, objectType, objectId, e);
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (ObjectAlreadyExistsException e) {
      return Response.noContent().build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @DELETE
  @Path("{objectType}/{objectId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(
      summary = "Deletes the observed object for current user",
      description = "Deletes the observed object for current user",
      method = "DELETE"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "404", description = "Not found"),
      }
  )
  public Response deleteObserver(
                                 @Parameter(description = "Object type: activity, comment, notes ...", required = true)
                                 @PathParam("objectType")
                                 String objectType,
                                 @Parameter(description = "Object identifier: technical id to identify object as observed", required = true)
                                 @PathParam("objectId")
                                 String objectId) {
    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      observerService.deleteObserver(userIdentityId, objectType, objectId);
      return Response.noContent().build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

}
