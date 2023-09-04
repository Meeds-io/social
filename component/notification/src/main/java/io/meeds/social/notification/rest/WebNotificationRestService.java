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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.services.rest.http.PATCH;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

/**
 * Provides REST Services in order to perform all read/write operations related
 * to web notifications.
 */

@Path("notifications/webNotifications")
@Tag(name = "notifications/webNotifications", description = "Manage web notifications")
public class WebNotificationRestService implements ResourceContainer {

  private static final Log       LOG         = ExoLogger.getLogger(WebNotificationRestService.class);

  private WebNotificationService webNftService;

  public WebNotificationRestService(WebNotificationService webNftService) {
    this.webNftService = webNftService;
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
          summary = "Get notifications list",
          description = "This gets the list of the notifications",
          method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Notifications list returned"),
      @ApiResponse(responseCode = "404", description = "Notifications list not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNotifications() {
    int maxItemsInPopover = NotificationMessageUtils.getMaxItemsInPopover();
    JSONArray notificationsJsonArray = new JSONArray();
    JSONObject response = new JSONObject();
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (currentUser == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    List<String> notifications = webNftService.get(new WebNotificationFilter(currentUser, true), 0, maxItemsInPopover);
    for (String notification : notifications) {
      JSONObject notificationJsonObject = new JSONObject();
      notificationJsonObject.put("notification", notification);
      notificationsJsonArray.put(notificationJsonObject);
    }
    int badge = webNftService.getNumberOnBadge(currentUser);
    response.put("notifications", notificationsJsonArray);
    response.put("badge", badge);
    return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
  }

  @PATCH
  @Path("{id}")
  @Consumes(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(
          summary = "Update notification",
          description = "Perform some patch operations on notifications",
          method = "PATCH")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Notification updated"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response updateNotifications(@Parameter(description = "notification operation", required = true) String operation,
                                      @Parameter(description = "notification id", required = true) @PathParam("id") String notificationId) {

    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      if (operation == null) {
        LOG.warn("Notification operation should be not null");
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      if (currentUser == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      if (operation.equals("markAsRead")) {
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
      }

      if (operation.equals("hide")) {
        if (notificationId == null) {
          return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
          NotificationInfo notification = webNftService.getNotificationInfo(notificationId);
          if (currentUser.equals(notification.getTo())) {
            webNftService.hidePopover(notificationId);
          } else {
            LOG.warn("User {} is not allowed to hide notification {}", currentUser, notificationId);
            return Response.status(Response.Status.UNAUTHORIZED).build();
  
          }
        }
      }

      if (operation.equals("resetNew")) {
        webNftService.resetNumberOnBadge(currentUser);
      }

      if (operation.equals("markAllAsRead")) {
        webNftService.markAllRead(currentUser);
        webNftService.resetNumberOnBadge(currentUser);
      }
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.error("Error when trying to patch operation {}  on notification {} for user {}", operation, notificationId, currentUser, e);
      return Response.serverError().build();
    }
  }
}
