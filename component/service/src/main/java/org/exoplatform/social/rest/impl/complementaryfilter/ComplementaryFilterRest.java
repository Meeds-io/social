/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
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
import javax.ws.rs.*;
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
  @Path("suggestions/{indexAlias}")
  @Operation(summary = "Gets complementary filter suggestions",
             description = "Gets complementary filter suggestions based on attributes", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getComplementaryFilterSuggestions(@RequestBody(description = "filter object ids") List<String> objectIds,
                                                    @Parameter(description = "filter attributes") @QueryParam("attributes") List<String> attributes,
                                                    @Parameter(description = "min count of occurrence")  @QueryParam("minDocCount") @DefaultValue ("2") int minDocCount,
                                                    @Parameter(description = "target search index alias")  @PathParam("indexAlias") String indexAlias) {

    if (objectIds.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Objects Ids list is mandatory").build();
    }
    if (attributes.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("attributes list is mandatory").build();
    }
    if (indexAlias == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("target index alias is mandatory").build();
    }

    try {
      List<Map<String, String>> result = complementaryFilterSearchConnector.search(attributes, objectIds, minDocCount, indexAlias);
      return Response.ok(result).build();
    } catch (Exception e) {
      LOG.error("Error while getting complementary filter suggestions", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
