/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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
