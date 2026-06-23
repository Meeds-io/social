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
package io.meeds.social.handler;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.url.PortalURLContext;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceAccessType;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.url.URLFactoryService;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;

import java.io.IOException;
import java.util.Arrays;

public class ActivityAccessHandler extends WebRequestHandler {

  public static final String      PAGE_URI            = "space-access";

  private IdentityManager identityManager;
  private ActivityManager activityManager;

  private SpaceService            spaceService;

  private URLFactoryService       urlFactoryService;

  private UserPortalConfigService userPortalConfigService;


  @Override
  public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception {
    super.onInit(controller, sConfig);

    PortalContainer container = PortalContainer.getInstance();
    this.spaceService = container.getComponentInstanceOfType(SpaceService.class);
    this.identityManager = container.getComponentInstanceOfType(IdentityManager.class);
    this.activityManager = container.getComponentInstanceOfType(ActivityManager.class);
    this.urlFactoryService = container.getComponentInstanceOfType(URLFactoryService.class);
    this.userPortalConfigService = container.getComponentInstanceOfType(UserPortalConfigService.class);
  }

  @Override
  public String getHandlerName() {
    return "activity-access";
  }

  @Override
  protected boolean getRequiresLifeCycle() {
    return true;
  }

  @Override
  public boolean execute(ControllerContext controllerContext) throws Exception {
    String username = controllerContext.getRequest().getRemoteUser();

    String idParameter = controllerContext.getRequest().getParameter("id");
    if (idParameter!=null) {
      ExoSocialActivity activity = activityManager.getActivity(idParameter);
      org.exoplatform.services.security.Identity authenticatedUserIdentity = ConversationState.getCurrent().getIdentity();
      if (activity != null && !activityManager.isActivityViewable(activity, authenticatedUserIdentity)) {
        if (activity.isComment()) {
          activity = activityManager.getActivity(activity.getParentId());
        }
        String spaceId = activity.getSpaceId();
        Space space = spaceService.getSpaceById(spaceId);
        if (space == null || username == null) {
          return false;
        } else {
          processSpaceAccess(controllerContext, authenticatedUserIdentity.getUserId(), space);
          return true;
        }
      }
    }
    return false;
  }

  private void processSpaceAccess(ControllerContext controllerContext,
                                  String remoteId,
                                  Space space) throws IOException {
    org.exoplatform.social.core.identity.model.Identity identity = identityManager.getOrCreateUserIdentity(remoteId);
    if (identity.isExternal()
        && (space == null || !spaceService.isInvitedUser(space, remoteId))) {
      controllerContext.getResponse().sendRedirect("/");
      return;
    }

    SpaceAccessType spaceAccessType = Arrays.stream(SpaceAccessType.values())
                                            .filter(accessType -> accessType.doCheck(remoteId, space))
                                            .findFirst()
                                            .orElse(null);
    sendRedirect(controllerContext, spaceAccessType, space);
  }

  private void sendRedirect(ControllerContext controllerContext, SpaceAccessType type, Space space) throws IOException {
    // set original parameter in session to share it with SpaceAccess App after
    // redirection
    HttpServletRequest request = controllerContext.getRequest();
    HttpSession session = request.getSession();
    session.setAttribute(SpaceAccessType.ACCESSED_TYPE_KEY, type);

    if (space == null) {
      session.removeAttribute(SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY);
      session.removeAttribute(SpaceAccessType.ACCESSED_SPACE_DISPLAY_NAME_KEY);
    } else {
      session.setAttribute(SpaceAccessType.ACCESSED_SPACE_ID_KEY, space.getId());
      session.setAttribute(SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY, space.getDisplayName());
      session.setAttribute(SpaceAccessType.ACCESSED_SPACE_DISPLAY_NAME_KEY, space.getPrettyName());
      session.setAttribute(SpaceAccessType.ACCESSED_SPACE_REQUEST_PATH_KEY, request.getRequestURI());
    }

    controllerContext.getResponse().sendRedirect(getURI(controllerContext, PAGE_URI));
  }

  private String getURI(ControllerContext controllerContext, String uri) {
    String portalName = userPortalConfigService.getMetaPortal();

    SiteKey siteKey = SiteKey.portal(portalName);
    NavigationResource resource = new NavigationResource(siteKey.getType(), siteKey.getName(), uri);

    NodeURL url = urlFactoryService.newURL(NodeURL.TYPE, new PortalURLContext(controllerContext, siteKey));
    url.setAjax(false);
    url.setResource(resource);
    return url.toString();
  }

  private String getPageNotFoundSite(String username) {
    return StringUtils.isBlank(username) ? "public" : userPortalConfigService.getMetaPortal();
  }
}
