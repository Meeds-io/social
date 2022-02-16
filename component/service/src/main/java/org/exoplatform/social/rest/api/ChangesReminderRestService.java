package org.exoplatform.social.rest.api;

import io.swagger.annotations.*;
import io.swagger.jaxrs.PATCH;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.log.Log;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("changes/reminder")
@RolesAllowed("users")
@Api(value = "/changes/reminder", description = "Check for exo changes reminder for currently authenticated user") // NOSONAR

public class ChangesReminderRestService implements ResourceContainer {

  private static final Log     LOG       = ExoLogger.getLogger(ChangesReminderRestService.class);

  public static final Scope    APP_SCOPE = Scope.APPLICATION.id("changesReminder");

  private final SettingService settingService;

  public ChangesReminderRestService(SettingService settingService) {
    this.settingService = settingService;
  }

  @GET
  @Path("{reminderName}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get changes reminder status for authenticated user.", httpMethod = "GET", response = Response.class, produces = "application/json")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response getChangesReminderStatus(@ApiParam(value = "Reminder name", required = true) @PathParam("reminderName") String featureName) {
    ConversationState current = ConversationState.getCurrent();
    SettingValue<?> settingValue = settingService.get(Context.USER.id(current.getIdentity().getUserId()), APP_SCOPE, featureName);
    boolean isNotificationRead = settingValue != null;
    return Response.ok().entity(String.valueOf(isNotificationRead)).type(MediaType.TEXT_PLAIN).build();
  }

  @PATCH
  @Path("{reminderName}")
  @RolesAllowed("users")
  @ApiOperation(value = "Mark changes reminder as read for authenticated user", httpMethod = "PUT", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.NO_CONTENT, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Bad request"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response markReminderAsRead(@ApiParam(value = "Reminder name", required = true) @PathParam("reminderName") String featureName) {
    ConversationState current = ConversationState.getCurrent();
    try {
      settingService.set(Context.USER.id(current.getIdentity().getUserId()), APP_SCOPE, featureName, SettingValue.create(true));
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.warn("Error updating reminder status for user with id '{}'", current.getIdentity().getUserId(), e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
