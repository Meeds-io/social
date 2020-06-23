package org.exoplatform.social.rest.impl.search;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.search.SearchService;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.annotations.*;

@Path(VersionResources.VERSION_ONE + "/social/search")
@Api(
    tags = VersionResources.VERSION_ONE + "/social/search",
    value = VersionResources.VERSION_ONE + "/social/search",
    description = "Operations on search connectors" // NOSONAR
)
public class SearchRestResourcesV1 implements ResourceContainer {

  private SearchService searchService;

  public SearchRestResourcesV1(SearchService searchService) {
    this.searchService = searchService;
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(
      value = "Gets search connectors",
      httpMethod = "GET",
      response = Response.class,
      produces = MediaType.APPLICATION_JSON,
      notes = "This returns a list of search connectors"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled")
      }
  )
  public Response getConnectors() {
    return Response.ok(searchService.getConnectors()).build();
  }

  @PUT
  @Path("{connectorName}")
  @RolesAllowed("administrators")
  @ApiOperation(
      value = "Changes status of search connector",
      httpMethod = "PUT",
      response = Response.class,
      notes = "This changes the status of a specific search connector"
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 401, message = "Not authorized"),
          @ApiResponse(code = 500, message = "Internal server error")
      }
  )
  public Response changeConnectorStatus(
                                        @ApiParam(
                                            value = "Search connector name",
                                            required = false
                                        ) @PathParam("connectorName") String connectorName,
                                        @ApiParam(
                                            value = "Search connector status enabled/disabled. possible values: true for enabled, else false",
                                            required = true,
                                            defaultValue = "false"
                                        ) @QueryParam("enable") boolean enable) {
    searchService.setConnectorAsEnabled(connectorName, enable);
    return Response.noContent().build();
  }

}
