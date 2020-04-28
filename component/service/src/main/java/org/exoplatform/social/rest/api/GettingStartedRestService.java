/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.rest.api;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.model.GettingStartedStep;
import org.exoplatform.social.core.service.GettingStartedService;

import io.swagger.annotations.*;

@Path("getting-started")
@RolesAllowed("users")
@Api(value = "/getting-started", description = "Check getting Started steps for currently authenticated user")
public class GettingStartedRestService implements ResourceContainer {

  private static final Log      LOG = ExoLogger.getLogger(GettingStartedRestService.class);

  private GettingStartedService gettingStartedService;

  public GettingStartedRestService(GettingStartedService gettingStartedService) {
    this.gettingStartedService = gettingStartedService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Collect Getting Started Steps", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "Return getting started steps in json format")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response getGettingStartedSteps() {
    ConversationState current = ConversationState.getCurrent();
    try {
      List<GettingStartedStep> gettinStartedSteps = gettingStartedService.getUserSteps(current.getIdentity().getUserId());
      return Response.ok(gettinStartedSteps).build();
    } catch (Exception e) {
      LOG.warn("Unknown error occurred while getting user steps", e);
      return Response.serverError().build();
    }

  }
}
