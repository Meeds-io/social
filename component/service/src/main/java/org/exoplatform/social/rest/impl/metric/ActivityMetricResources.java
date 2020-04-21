package org.exoplatform.social.rest.impl.metric;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.api.VersionResources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(VersionResources.VERSION_ONE + "/social/metrics")
public class ActivityMetricResources implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(ActivityMetricResources.class);

  private IdentityManager identityManager;

  private SpaceService spaceService;

  public ActivityMetricResources(IdentityManager identityManager, SpaceService spaceService) {
    this.identityManager = identityManager;
    this.spaceService = spaceService;
  }

  @POST
  @Path("composer/click")
  @RolesAllowed("users")
  @ApiOperation(value = "Log a click action on the composer", httpMethod = "POST", response = Response.class, notes = "This logs a message when the user performs opens a composer")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Click logged"),
          @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response openComposer(@Context UriInfo uriInfo,
                               @ApiParam(value = "The clicked composer", required = true) @QueryParam("composer") String composer,
                               @ApiParam(value = "The space id") @QueryParam("spaceId") String spaceId) {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, false);

    Space space = null;
    if(StringUtils.isNotBlank(spaceId)) {
      space = spaceService.getSpaceById(spaceId);
    }

    LOG.info("service=composer operation=open_composer parameters=\"composer_type:{},space_name:{},space_id:{},user_id:{}\"",
            composer,
            space != null ? space.getPrettyName() : "",
            space != null ? space.getId() : "",
            currentUser != null ? currentUser.getId() : "");

    return Response.status(Response.Status.OK).build();
  }

}
