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

/**
 * Provides REST Services in order to check user getting started steps.
 */

@Path("getting-started")
@RolesAllowed("users")
@Api(value = "/getting-started", description = "Check getting Started steps for users")
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
