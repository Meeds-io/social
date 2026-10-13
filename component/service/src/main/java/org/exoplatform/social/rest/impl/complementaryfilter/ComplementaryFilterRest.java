/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.rest.impl.complementaryfilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.jpa.search.ComplementaryFilterSearchConnector;
import org.exoplatform.social.service.rest.api.VersionResources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path(VersionResources.VERSION_ONE + "/social/complementaryfilter")
@Tag(name = VersionResources.VERSION_ONE + "/social/complementaryfilter", description = "Managing complementary filter")
public class ComplementaryFilterRest implements ResourceContainer {

  private static final Log                         LOG = ExoLogger.getLogger(ComplementaryFilterRest.class);

  private final ComplementaryFilterSearchConnector complementaryFilterSearchConnector;

  public ComplementaryFilterRest(ComplementaryFilterSearchConnector complementaryFilterSearchConnector) {
    this.complementaryFilterSearchConnector = complementaryFilterSearchConnector;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Path("suggestions")
  @Operation(summary = "Gets complementary filter suggestions over profiles",
             description = "Aggregates the values the given profiles share on the given profile properties. Only the profile index is queried.",
             method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400",
                       description = "Bad request: an empty list, an unknown or hidden profile property, a non-numeric identity id "
                           + "or a minDocCount lower than 1; the body is the message code"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getComplementaryFilterSuggestions(@RequestBody(description = "identity ids of the profiles to aggregate over") List<String> objectIds,
                                                    @Parameter(description = "profile property names to aggregate on") @QueryParam("attributes") List<String> attributes,
                                                    @Parameter(description = "min count of occurrence")  @QueryParam("minDocCount") @DefaultValue ("2") int minDocCount) {
    try {
      List<Map<String, String>> result = complementaryFilterSearchConnector.search(attributes, objectIds, minDocCount);
      return Response.ok(result).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error while getting complementary filter suggestions", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
