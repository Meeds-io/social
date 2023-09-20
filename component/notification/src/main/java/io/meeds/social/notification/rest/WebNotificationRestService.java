/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.notification.rest;

import io.meeds.social.notification.rest.model.WebNotificationListRestEntity;
import io.meeds.social.notification.rest.utils.WebNotificationRestEntityBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.services.rest.http.PATCH;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Provides REST Services in order to perform all read/write operations related
 * to web notifications.
 */

@Path("notifications/webNotifications")
@Tag(name = "notifications/webNotifications", description = "Manage web notifications")
public class WebNotificationRestService implements ResourceContainer {

  private static final String    MARK_ALL_AS_READ_OPERATION = "markAllAsRead";

  private static final String    RESET_NEW_OPERATION        = "resetBadge";

  private static final String    MARK_AS_READ_OPERATION     = "markAsRead";

  private static final Log       LOG                        = ExoLogger.getLogger(WebNotificationRestService.class);

  private WebNotificationService webNftService;

  private IdentityManager        identityManager;

  private SpaceService           spaceService;

  public WebNotificationRestService(WebNotificationService webNftService,
                                    IdentityManager identityManager,
                                    SpaceService spaceService) {
    this.webNftService = webNftService;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get notifications list", description = "This gets the list of the notifications", method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Notifications list returned"),
      @ApiResponse(responseCode = "404", description = "Notifications list not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNotifications(
                                   @Parameter(description = "The list of plugins to include in list", required = false)
                                   @QueryParam("plugin")
                                   List<String> plugins,
                                   @Parameter(description = "Includes in response the unread badges count by plugin", required = false)
                                   @DefaultValue("false")
                                   @QueryParam("badgeByPlugin")
                                   boolean badgeByPlugin,
                                   @Parameter(description = "Includes hidden notifications in response", required = false)
                                   @DefaultValue("false")
                                   @QueryParam("includeHidden")
                                   boolean includeHidden,
                                   @Parameter(description = "Whether include only unread notifications or all", required = false)
                                   @DefaultValue("false")
                                   @QueryParam("onlyUnread")
                                   boolean onlyUnread,
                                   @Parameter(description = "Search Offset", required = false)
                                   @DefaultValue("0")
                                   @QueryParam("offset")
                                   int offset,
                                   @Parameter(description = "Search Limit", required = false)
                                   @DefaultValue("10")
                                   @QueryParam("limit")
                                   int limit) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    int badge = webNftService.getNumberOnBadge(currentUser);
    WebNotificationFilter filter =
                                 new WebNotificationFilter(currentUser,
                                                           plugins == null ? Collections.emptyList()
                                                                           : plugins.stream().map(PluginKey::key).toList(),
                                                           !includeHidden);
    if (onlyUnread) {
      filter.setIsRead(false);
    }
    List<NotificationInfo> notificationInfos = limit > 0 ? webNftService.getNotificationInfos(filter, offset, limit)
                                                         : Collections.emptyList();
    Map<String, Integer> badgesByPlugin = null;
    if (badgeByPlugin) {
      badgesByPlugin = webNftService.countUnreadByPlugin(currentUser);
    }
    WebNotificationListRestEntity webNotificationsList = WebNotificationRestEntityBuilder.toRestEntity(webNftService,
                                                                                                       identityManager,
                                                                                                       spaceService,
                                                                                                       notificationInfos,
                                                                                                       !includeHidden,
                                                                                                       badge,
                                                                                                       badgesByPlugin,
                                                                                                       offset,
                                                                                                       limit);
    return Response.ok(webNotificationsList).build();
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @Operation(summary = "Hide notification", description = "Hides a designated notification", method = "DELETE")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Request fullfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response hideNotifications(
                                    @Parameter(description = "notification id", required = true)
                                    @PathParam("id")
                                    String notificationId) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (notificationId == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    } else {
      NotificationInfo notification = webNftService.getNotificationInfo(notificationId);
      if (currentUser.equals(notification.getTo())) {
        webNftService.hidePopover(notificationId);
        return Response.noContent().build();
      } else {
        LOG.warn("User {} is not allowed to hide notification {}", currentUser, notificationId);
        return Response.status(Response.Status.UNAUTHORIZED).build();

      }
    }
  }

  @PATCH
  @Consumes(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(summary = "Update notification", description = "Perform some patch operations on notifications", method = "PATCH")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Notification updated"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response updateNotifications(
                                      @Parameter(description = "The list of plugins to include in list", required = false)
                                      @QueryParam("plugin")
                                      List<String> plugins,
                                      @Parameter(description = "notification operation", required = true)
                                      @QueryParam("operation")
                                      String operation) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (RESET_NEW_OPERATION.equals(operation)) {
      webNftService.resetNumberOnBadge(plugins, currentUser);
    } else if (MARK_ALL_AS_READ_OPERATION.equals(operation)) {
      webNftService.markAllRead(plugins, currentUser);
      webNftService.resetNumberOnBadge(plugins, currentUser);
    } else {
      return Response.status(Response.Status.BAD_REQUEST).entity("Unrecognized operation parameter value: " + operation).build();
    }
    return Response.noContent().build();
  }

  @PATCH
  @Path("{id}")
  @Consumes(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(summary = "Update notification", description = "Perform some patch operations on notifications", method = "PATCH")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Notification updated"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response updateNotification(
                                     @Parameter(description = "notification operation", required = true)
                                     @QueryParam("operation")
                                     String operation,
                                     @Parameter(description = "notification id", required = false)
                                     @PathParam("id")
                                     String notificationId) {
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (MARK_AS_READ_OPERATION.equals(operation)) {
      if (notificationId == null) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      } else {
        NotificationInfo notification = webNftService.getNotificationInfo(notificationId);
        if (currentUser.equals(notification.getTo())) {
          webNftService.markRead(notificationId);
        } else {
          LOG.warn("User {} is not allowed to mark notification {} as read", currentUser, notificationId);
          return Response.status(Response.Status.UNAUTHORIZED).build();

        }
      }
    } else {
      return Response.status(Response.Status.BAD_REQUEST).entity("Unrecognized operation parameter value: " + operation).build();
    }
    return Response.noContent().build();
  }

}
