package org.exoplatform.social.rest.impl.unread;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.rest.api.VersionResources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(VersionResources.VERSION_ONE + "/social/unread")
@Tag(name = VersionResources.VERSION_ONE + "/social/unread", description = "Managing unread for any type of data")
public class UnreadRest implements ResourceContainer {

  private static final Log                  LOG = ExoLogger.getLogger(UnreadRest.class);

  private final SpaceWebNotificationService spaceWebNotificationService;

  public UnreadRest(SpaceWebNotificationService spaceWebNotificationService) {
    this.spaceWebNotificationService = spaceWebNotificationService;
  }

  @DELETE
  @Path("{spaceId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Delete a metadata items identified by space id", description = "Delete a metadata items identified by space id", method = "DELETE")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fulfilled"),
      @ApiResponse(responseCode = "500", description = "Internal server error"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "404", description = "Not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"), })
  public Response deleteAllUnreadItems(@Parameter(description = "The space Id", required = true)
  @PathParam("spaceId")
  String spaceId, @QueryParam("ignoreNotExisting")
  boolean ignoreNotExisting) {
    if (StringUtils.isBlank(spaceId)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("UnreadSpaceIdRequired").build();
    }

    long userIdentityId = RestUtils.getCurrentUserIdentityId();
    try {
      spaceWebNotificationService.markAllAsRead(userIdentityId, Long.parseLong(spaceId));
      return Response.noContent().build();
    } catch (ObjectNotFoundException e) {
      if (ignoreNotExisting) {
        return Response.noContent().build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      LOG.warn("Error deleting a unread items", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

}
