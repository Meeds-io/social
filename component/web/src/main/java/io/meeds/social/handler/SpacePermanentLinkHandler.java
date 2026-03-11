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

import static io.meeds.social.space.plugin.SpacePermanentLinkPlugin.APPLICATION_URI;
import static io.meeds.social.space.plugin.SpacePermanentLinkPlugin.OBJECT_TYPE;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.security.sso.SSOHelper;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.service.PermanentLinkService;

import jakarta.servlet.ServletConfig;

public class SpacePermanentLinkHandler extends WebRequestHandler {

  public static final QualifiedName REQUEST_SPACE_ID = QualifiedName.create("spaceId");

  public static final QualifiedName INVITATION_ID    = QualifiedName.create("invitation_id");

  public static final QualifiedName REQUEST_PATH     = QualifiedName.create("path");

  private SpaceService              spaceService;

  private UserPortalConfigService   portalConfigService;

  private PermanentLinkService      permanentLinkService;

  @Override
  public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception {
    super.onInit(controller, sConfig);

    PortalContainer container = PortalContainer.getInstance();
    this.spaceService = container.getComponentInstanceOfType(SpaceService.class);
    this.portalConfigService = container.getComponentInstanceOfType(UserPortalConfigService.class);
    this.permanentLinkService = container.getComponentInstanceOfType(PermanentLinkService.class);
  }

  @Override
  public String getHandlerName() {
    return "spaces";
  }

  @Override
  protected boolean getRequiresLifeCycle() {
    return true;
  }

  @Override
  public boolean execute(ControllerContext controllerContext) throws Exception {
    String username = controllerContext.getRequest().getRemoteUser();
    String spaceId = controllerContext.getParameter(REQUEST_SPACE_ID);
    String path = controllerContext.getParameter(REQUEST_PATH);
    String invitationId = controllerContext.getRequest().getParameter(INVITATION_ID.getName());
    String queryString = controllerContext.getRequest().getQueryString();
    if (StringUtils.isNotEmpty(queryString)) {
      path = path + "?" + queryString;
    }
    Space space = spaceService.getSpaceById(spaceId);
    if (StringUtils.isBlank(username)) {
      String loginPath = getAuthenticationUrl(controllerContext.getRequest().getRequestURI(),
                                              controllerContext.getRequest().getQueryString());
      controllerContext.getResponse().sendRedirect(loginPath);
    } else if (space == null
        || (isHiddenSpace(space, username) && !spaceService.isInvitedUser(space, username))) {
      String pageNotFoundUrl = "/portal/" + getPageNotFoundSite(username) + "/page-not-found";
      controllerContext.getResponse().sendRedirect(pageNotFoundUrl);
    } else {
      String uri = permanentLinkService.getLink(getPermanentLinkObject(spaceId, path));
      controllerContext.getResponse().sendRedirect(uri);
    }
    return true;
  }

  private String getPageNotFoundSite(String username) {
    return StringUtils.isBlank(username) ? "public" : portalConfigService.getMetaPortal();
  }

  private boolean isHiddenSpace(Space space, String username) {
    return !spaceService.canViewSpace(space, username)
           && Space.HIDDEN.equals(space.getVisibility());
  }

  private PermanentLinkObject getPermanentLinkObject(String spaceId, String path) {
    return new PermanentLinkObject(OBJECT_TYPE,
                                   spaceId,
                                   Collections.singletonMap(APPLICATION_URI, path));
  }

  private String getAuthenticationUrl(String permanentLink, String queryString) {
    StringBuilder loginPath = new StringBuilder();
    // Check sso enabled
    SSOHelper ssoHelper = ExoContainerContext.getService(SSOHelper.class);
    if (ssoHelper != null && ssoHelper.isSSOEnabled() && ssoHelper.skipJSPRedirection()) {
      loginPath.append("/portal").append(ssoHelper.getSSORedirectURLSuffix());
    } else {
      loginPath.append("/portal/login");
    }
    String fullUri = StringUtils.isNotBlank(queryString) ? permanentLink + "?" + queryString : permanentLink;
    loginPath.append("?initialURI=").append(URLEncoder.encode(fullUri, StandardCharsets.UTF_8));
    return loginPath.toString();
  }

}
