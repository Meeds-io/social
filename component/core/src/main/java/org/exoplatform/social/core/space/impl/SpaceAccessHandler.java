/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.space.impl;

import static org.exoplatform.portal.application.PortalRequestHandler.REQUEST_SITE_NAME;
import static org.exoplatform.portal.application.PortalRequestHandler.REQUEST_SITE_TYPE;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.url.PortalURLContext;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceAccessType;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.url.URLFactoryService;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SpaceAccessHandler extends WebRequestHandler {

  private static final String     SPACES_GROUP_PREFIX = SpaceUtils.SPACE_GROUP + "/";

  public static final String      PAGE_URI            = "space-access";

  private IdentityManager         identityManager;

  private SpaceService            spaceService;

  private IdentityRegistry        identityRegistry;

  private URLFactoryService       urlFactoryService;

  private UserPortalConfigService userPortalConfigService;

  private LayoutService           layoutService;

  private UserACL                 userAcl;

  @Override
  public void onInit(WebAppController controller, ServletConfig sConfig) throws Exception {
    super.onInit(controller, sConfig);

    PortalContainer container = PortalContainer.getInstance();
    this.spaceService = container.getComponentInstanceOfType(SpaceService.class);
    this.identityRegistry = container.getComponentInstanceOfType(IdentityRegistry.class);
    this.identityManager = container.getComponentInstanceOfType(IdentityManager.class);
    this.urlFactoryService = container.getComponentInstanceOfType(URLFactoryService.class);
    this.userPortalConfigService = container.getComponentInstanceOfType(UserPortalConfigService.class);
    this.layoutService = container.getComponentInstanceOfType(LayoutService.class);
    this.userAcl = container.getComponentInstanceOfType(UserACL.class);
  }

  @Override
  public String getHandlerName() {
    return PAGE_URI;
  }

  @Override
  protected boolean getRequiresLifeCycle() {
    return true;
  }

  @Override
  public boolean execute(ControllerContext controllerContext) throws Exception {
    String username = controllerContext.getRequest().getRemoteUser();
    String requestSiteType = controllerContext.getParameter(REQUEST_SITE_TYPE);
    String requestSiteName = controllerContext.getParameter(REQUEST_SITE_NAME);
    if (!SiteType.GROUP.name().equalsIgnoreCase(requestSiteType)
        || !requestSiteName.startsWith(SPACES_GROUP_PREFIX)) {
      return false;
    }
    Space space = spaceService.getSpaceByGroupId(requestSiteName);
    if (canAccessSpace(space, username)) {
      HttpSession session = controllerContext.getRequest().getSession();
      String lastAccessedSpaceId = (String) session.getAttribute(SpaceAccessType.ACCESSED_SPACE_ID_KEY);
      if (!StringUtils.equals(lastAccessedSpaceId, space.getId())) {
        spaceService.updateSpaceAccessed(username, space);
      }
      cleanupSession(controllerContext);
      session.setAttribute(SpaceAccessType.ACCESSED_SPACE_ID_KEY, space.getId());
      return false;
    } else if (space == null || Space.HIDDEN.equals(space.getVisibility())) {
      controllerContext.getResponse()
                       .sendRedirect(String.format("%s/%s/page-not-found",
                                                   controllerContext.getRequest().getContextPath(),
                                                   getPageNotFoundSite(username)));
      return true;
    } else if (canAccessSpacePublicSite(space, username)) {
      controllerContext.getResponse()
                       .sendRedirect(String.format("%s/%s",
                                                   controllerContext.getRequest().getContextPath(),
                                                   spaceService.getSpacePublicSiteName(space)));
      return true;
    } else {
      processSpaceAccess(controllerContext, username, space);
      return true;
    }
  }

  private boolean canAccessSpacePublicSite(Space space, String username) {
    return spaceService.canAccessSpacePublicSite(space, username)
           && userAcl.hasAccessPermission(layoutService.getPortalConfig(space.getPublicSiteId()),
                                          userAcl.getUserIdentity(username));
  }

  private boolean canAccessSpace(Space space, String username) {
    Identity identity = identityRegistry.getIdentity(username);
    if (identity == null || space == null) {
      return false;
    } else if (spaceService.isMember(space, username)) {
      return true;
    } else if (spaceService.canViewSpace(space, username)) {
      // Add user as 'member' to Identity if it's absent
      Collection<MembershipEntry> memberships = identity.getMemberships();
      MembershipEntry memberMembership = new MembershipEntry(space.getGroupId(), SpaceUtils.MEMBER);
      memberships.add(memberMembership);
      return true;
    } else {
      return false;
    }
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

  private void cleanupSession(ControllerContext controllerContext) {
    // cleanup session attributes once the attributes are used from Request
    removeAttributeFromSession(controllerContext, SpaceAccessType.ACCESSED_TYPE_KEY);
    removeAttributeFromSession(controllerContext, SpaceAccessType.ACCESSED_SPACE_ID_KEY);
    removeAttributeFromSession(controllerContext, SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY);
    removeAttributeFromSession(controllerContext, SpaceAccessType.ACCESSED_SPACE_REQUEST_PATH_KEY);
  }

  private void removeAttributeFromSession(ControllerContext controllerContext, String key) {
    if (controllerContext.getRequest().getAttribute(key) != null) {
      controllerContext.getRequest().getSession().removeAttribute(key);
    }
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
