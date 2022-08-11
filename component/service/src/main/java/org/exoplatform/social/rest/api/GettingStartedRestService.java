package org.exoplatform.social.rest.api;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.model.GettingStartedStep;
import org.exoplatform.social.core.service.GettingStartedService;


@Path("getting-started")
@RolesAllowed("users")
@Tag(name = "/getting-started", description = "Check getting Started steps for currently authenticated user")
public class GettingStartedRestService implements ResourceContainer {

  private static final Log      LOG = ExoLogger.getLogger(GettingStartedRestService.class);

  private static final CacheControl CACHE_CONTROL               = new CacheControl();

  // A year
  private static final int          CACHE_IN_SECONDS            = 365 * 86400;

  private static final int          CACHE_IN_MILLI_SECONDS      = CACHE_IN_SECONDS * 1000;

  private static final Date         LAST_MODIFIED          = new Date(System.currentTimeMillis());

  private static final Date         EXPIRES_DATE           = new Date(System.currentTimeMillis() + CACHE_IN_MILLI_SECONDS);

  static {
    CACHE_CONTROL.setMaxAge(CACHE_IN_SECONDS);
  }

  private GettingStartedService gettingStartedService;

  public GettingStartedRestService(GettingStartedService gettingStartedService) {
    this.gettingStartedService = gettingStartedService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Collect Getting Started Steps", method = "GET", description = "Return getting started steps in json format")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getGettingStartedSteps() {
    ConversationState current = ConversationState.getCurrent();
    try {
      List<GettingStartedStep> gettinStartedSteps = gettingStartedService.getUserSteps(current.getIdentity().getUserId());
      if (gettinStartedSteps.isEmpty()) {
        ResponseBuilder builder = Response.ok(gettinStartedSteps, MediaType.APPLICATION_JSON);
        builder.lastModified(LAST_MODIFIED);
        builder.expires(EXPIRES_DATE);
        builder.cacheControl(CACHE_CONTROL);
        return builder.build();
      } else {
        return Response.ok(gettinStartedSteps).build();
      }
    } catch (Exception e) {
      LOG.warn("Unknown error occurred while getting user steps", e);
      return Response.serverError().build();
    }
  }

  @DELETE
  @Operation(
      summary = "Delete Getting Started Application display for current user",
      method = "DELETE",
      description = "Doesn't return a response content"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "204", description = "Internal server error"),
      }
  )
  public Response hideGettingStartedApplication() {
    ConversationState current = ConversationState.getCurrent();
    try {
      gettingStartedService.hideGettingStartedApplication(current.getIdentity().getUserId());
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.warn("Unknown error occurred while deleting Getting Started application for current user", e);
      return Response.serverError().build();
    }
  }
}
