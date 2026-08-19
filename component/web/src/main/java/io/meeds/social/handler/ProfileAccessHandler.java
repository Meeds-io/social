/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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

import static org.exoplatform.portal.application.PortalRequestHandler.REQUEST_PATH;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;

import jakarta.servlet.ServletConfig;

/**
 * Checks the profile page owner before letting the portal render it: the
 * profile of a not existing, disabled or deleted user isn't accessible and
 * ends up on the page not found, following the space access check pattern.
 */
public class ProfileAccessHandler extends WebRequestHandler {

  public static final String      HANDLER_NAME = "profile-access";

  private IdentityManager         identityManager;

  private UserPortalConfigService portalConfigService;

  @Override
  public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception {
    super.onInit(controller, sConfig);

    PortalContainer container = PortalContainer.getInstance();
    this.identityManager = container.getComponentInstanceOfType(IdentityManager.class);
    this.portalConfigService = container.getComponentInstanceOfType(UserPortalConfigService.class);
  }

  @Override
  public String getHandlerName() {
    return HANDLER_NAME;
  }

  @Override
  protected boolean getRequiresLifeCycle() {
    return true;
  }

  @Override
  public boolean execute(ControllerContext controllerContext) throws Exception {
    String path = controllerContext.getParameter(REQUEST_PATH);
    String profileOwner = StringUtils.isBlank(path) ? null : path.split("/")[0];
    String username = controllerContext.getRequest().getRemoteUser();
    if (StringUtils.isBlank(profileOwner) || StringUtils.equals(profileOwner, username)) {
      return false;
    }
    Identity identity = identityManager.getOrCreateUserIdentity(profileOwner);
    if (identity == null || !identity.isEnable() || identity.isDeleted()) {
      String pageNotFoundUrl = "/portal/" + getPageNotFoundSite(username) + "/page-not-found";
      controllerContext.getResponse().sendRedirect(pageNotFoundUrl);
      return true;
    }
    return false;
  }

  private String getPageNotFoundSite(String username) {
    return StringUtils.isBlank(username) ? "public" : portalConfigService.getMetaPortal();
  }

}
