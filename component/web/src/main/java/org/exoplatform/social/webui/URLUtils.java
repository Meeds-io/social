/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.webui;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;

/**
 * Processes url and returns the some type of result base on url.
 */
public class URLUtils {

  /**
   * @return the user carried by the current page URL — the profile owner on a
   *         profile or activity-stream page — or null when the URL carries
   *         none. Unlike {@link #getCurrentUser()}, no accessibility filtering
   *         is applied: this answers "is this page about a user, and which one",
   *         while what the viewer may see of that user belongs to the service
   *         consuming the answer. getCurrentUser() returns null for an external
   *         viewer on a profile it may not access, which conflates "not a
   *         user-scoped page" with "not accessible" — a widget switching modes
   *         on that null would silently fall back to its non-profile behaviour
   *         on a profile page.
   */
  public static String getStreamOwnerId() {
    PortalRequestContext pcontext = getPortalRequestContext();
    String requestPath = "/" + pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    if (route == null) {
      return null;
    }
    String currentUserName = route.localArgs.get("streamOwnerId");
    if (currentUserName == null) {
      return null;
    }
    IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateUserIdentity(currentUserName);
    return identity == null ? null : identity.getRemoteId();
  }

  /**
   * @return current user name base on analysis of current url
   */
  public static String getCurrentUser() {
    PortalRequestContext pcontext = getPortalRequestContext();
    String requestPath = "/" + pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    if (route == null) {
      return null;
    }

    String currentUserName = route.localArgs.get("streamOwnerId");
    org.exoplatform.social.core.identity.model.Identity viewerIdentity = Utils.getViewerIdentity();
    if (viewerIdentity.isExternal() && !isProfileAccessible(currentUserName, pcontext.getRemoteUser())) {
      return null;
    }
    if (currentUserName != null) {
      IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
      Identity identity = identityManager.getOrCreateUserIdentity(currentUserName);
      if (identity != null) {
        return identity.getRemoteId();
      }
    }
    return null;
  }

  private static boolean isProfileAccessible(String currentUserName, String externalUserId) {
    try {
      List<Identity> viewerFriends = Utils.getViewerFriends();
      // check if target user in viewer Friends
      boolean isFriend = viewerFriends.stream().anyMatch(value -> value.getRemoteId().equals(currentUserName));
      // Gets a list access containing all spaces witch a viewer is member
      SpaceService spaceService = ExoContainerContext.getService(SpaceService.class);
      ListAccess<Space> memberSpacesListAccess = spaceService.getMemberSpaces(externalUserId);
      Space[] spaces = memberSpacesListAccess.load(0, memberSpacesListAccess.getSize());
      // check if target user is member of these spaces
      boolean isMemberSpaces = Arrays.stream(spaces).anyMatch(space -> Utils.getSpaceService().isMember(space, currentUserName));
      return isFriend || isMemberSpaces;
    } catch (Exception e) {
      throw new IllegalStateException("Error checking whether profile " + currentUserName + " is accessible for " + externalUserId
          + " or not", e);
    }
  }

  private static PortalRequestContext getPortalRequestContext() {
    RequestContext context = RequestContext.getCurrentInstance();
    if (context != null && !(context instanceof PortalRequestContext)) {
      context = context.getParentAppRequestContext();
    }
    return (PortalRequestContext) context;
  }

}
