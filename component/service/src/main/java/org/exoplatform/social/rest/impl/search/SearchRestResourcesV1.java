package org.exoplatform.social.rest.impl.search;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.search.SearchService;
import org.exoplatform.social.service.rest.api.VersionResources;

@Path(VersionResources.VERSION_ONE + "/social/search")
@Tag(name = VersionResources.VERSION_ONE + "/social/search", description = "Operations on search connectors")
public class SearchRestResourcesV1 implements ResourceContainer {

  private SearchService searchService;

  public SearchRestResourcesV1(SearchService searchService) {
    this.searchService = searchService;
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Gets search connectors",
      method = "GET",
      description = "This returns a list of search connectors"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled")
      }
  )
  public Response getConnectors() {
    return Response.ok(searchService.getConnectors()).build();
  }

  @PUT
  @Path("{connectorName}")
  @RolesAllowed("administrators")
  @Operation(
      summary = "Changes status of search connector",
      method = "PUT",
      description = "This changes the status of a specific search connector"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Not authorized"),
          @ApiResponse(responseCode = "500", description = "Internal server error")
      }
  )
  public Response changeConnectorStatus(
                                        @Parameter(
                                            description = "Search connector name",
                                            required = false
                                        ) @PathParam("connectorName") String connectorName,
                                        @Parameter(
                                            description = "Search connector status enabled/disabled. possible values: true for enabled, else false",
                                            required = true) @Schema(defaultValue = "false") @QueryParam("enable") boolean enable) {
    searchService.setConnectorAsEnabled(connectorName, enable);
    return Response.noContent().build();
  }

}
