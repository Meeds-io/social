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

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.application.RequestContext;

import io.meeds.portal.plugin.AclPlugin;
import io.meeds.social.user.plugin.UserAclPlugin;

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
    // Opening one's own profile through the navigation carries no username
    // segment (/profile, not /profile/{username}): the owner is the viewer —
    // the fallback Utils.getOwnerRemoteId() has always applied. Scoped by
    // path shape AND site type: a group/space site whose nav path is also
    // the single segment "profile" (a space named "Profile") must not gain
    // an implicit owner.
    if (("/profile".equals(requestPath) || "/profile/".equals(requestPath))
        && SiteType.PORTAL == pcontext.getSiteType()) {
      return pcontext.getRemoteUser();
    }
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
   * @return the user carried by the current page URL, or null when the URL
   *         carries none, or when the viewer is a guest who may not view that
   *         user's profile under the {@link UserAclPlugin} {@code VIEW} rule
   */
  public static String getCurrentUser() {
    PortalRequestContext pcontext = getPortalRequestContext();
    String requestPath = "/" + pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    if (route == null) {
      return null;
    }

    String currentUserName = route.localArgs.get("streamOwnerId");
    if (currentUserName == null || !isProfileAccessible(currentUserName, pcontext.getRemoteUser())) {
      return null;
    }
    IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateUserIdentity(currentUserName);
    return identity == null ? null : identity.getRemoteId();
  }

  /**
   * A guest viewer, decided by the same membership test as the rule
   * ({@link UserAclPlugin#isGuest}), opens a profile only when the rule grants
   * it; any other viewer opens every profile, as before.
   */
  private static boolean isProfileAccessible(String currentUserName, String viewerUserName) {
    if (StringUtils.isBlank(viewerUserName)) {
      return true;
    }
    UserACL userAcl = ExoContainerContext.getService(UserACL.class);
    org.exoplatform.services.security.Identity viewerAclIdentity = userAcl.getUserIdentity(viewerUserName);
    return !UserAclPlugin.isGuest(viewerAclIdentity)
        || userAcl.hasPermission(UserAclPlugin.OBJECT_TYPE, currentUserName, AclPlugin.VIEW_PERMISSION_TYPE, viewerAclIdentity);
  }

  private static PortalRequestContext getPortalRequestContext() {
    RequestContext context = RequestContext.getCurrentInstance();
    if (context != null && !(context instanceof PortalRequestContext)) {
      context = context.getParentAppRequestContext();
    }
    return (PortalRequestContext) context;
  }

}
