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
package io.meeds.social.portlet;

import java.io.IOException;
import java.util.UUID;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import io.meeds.social.space.model.SpaceDirectorySettings;
import io.meeds.social.space.service.SpaceDirectoryService;
import io.meeds.social.util.JsonUtils;

public class SpacesListPortlet extends GenericDispatchedViewPortlet {

  private static final String   SETTING_NAME = "name";

  private UserACL               userAcl;

  private SpaceDirectoryService spaceDirectoryService;

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    boolean canModifySettings = canModifySettings();
    request.setAttribute("canEdit", canModifySettings);
    if (canModifySettings) {
      request.setAttribute("isPublicPage", isPublicPage());
    }
    super.doView(request, response);
  }

  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    PortletPreferences preferences = request.getPreferences();
    String settingName = preferences.getValue(SETTING_NAME, "");
    response.getWriter().write(settingName);
    response.setContentType("text/plain");
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    if (!canModifySettings()) {
      throw new PortletException("User is not allowed to save settings");
    }
    PortletPreferences preferences = request.getPreferences();
    String settings = request.getParameter("settings");
    preferences.setValue("settings", settings);

    String publicAccess = request.getParameter("publicAccess");
    if (StringUtils.isNotBlank(publicAccess) && isAdministrator()) {
      boolean enablePublicAccess = StringUtils.equals(publicAccess, "true");
      if (enablePublicAccess) {
        String settingName = getOrCreateSettingName(preferences);
        getSpaceDirectoryService().saveSpacesDirectorySettings(settingName, getSpaceDirectorySettings(settings));
        response.setProperty("X-Setting-Name", settingName);
      } else {
        String settingName = preferences.getValue(SETTING_NAME, null);
        if (settingName != null) {
          getSpaceDirectoryService().removeSpacesDirectorySettings(settingName);
          preferences.reset(SETTING_NAME);
        }
      }
    }
    preferences.store();
    response.setPortletMode(PortletMode.VIEW);
  }

  private SpaceDirectorySettings getSpaceDirectorySettings(String settings) {
    SpaceDirectorySettings spaceDirectorySettings = JsonUtils.fromJsonString(settings, SpaceDirectorySettings.class);
    Page currentPage = getCurrentPage();
    if (currentPage != null) {
      spaceDirectorySettings.setPageReference(currentPage.getPageKey().format());
    }
    return spaceDirectorySettings;
  }

  private String getOrCreateSettingName(PortletPreferences preferences) throws ReadOnlyException {
    String settingName = preferences.getValue(SETTING_NAME, null);
    if (StringUtils.isBlank(settingName)) {
      settingName = UUID.randomUUID().toString();
      preferences.setValue(SETTING_NAME, settingName);
    }
    return settingName;
  }

  private boolean canModifySettings() {
    Page currentPage = getCurrentPage();
    return currentPage != null
           && getUserAcl().hasEditPermission(currentPage,
                                             getCurrentIdentity());
  }

  private boolean isPublicPage() {
    PortalConfig site = getCurrentSite();
    return site != null
           && PortalConfig.PORTAL_TYPE.equalsIgnoreCase(site.getType())
           && ("public".equals(site.getName())
               || (site.getAccessPermissions() != null && ArrayUtils.contains(site.getAccessPermissions(), UserACL.EVERYONE)))
           && getSpaceDirectoryService().isPagePublicallyAccessible(getCurrentPage());
  }

  private Page getCurrentPage() {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    return requestContext == null ? null : requestContext.getPage();
  }

  private PortalConfig getCurrentSite() {
    PortalRequestContext requestContext = PortalRequestContext.getCurrentInstance();
    return requestContext == null ? null : requestContext.getPortalConfig();
  }

  private boolean isAdministrator() {
    return getUserAcl().isAdministrator(getCurrentIdentity());
  }

  private Identity getCurrentIdentity() {
    return ConversationState.getCurrent() == null ? null : ConversationState.getCurrent().getIdentity();
  }

  private UserACL getUserAcl() {
    if (userAcl == null) {
      userAcl = ExoContainerContext.getService(UserACL.class);
    }
    return userAcl;
  }

  private SpaceDirectoryService getSpaceDirectoryService() {
    if (spaceDirectoryService == null) {
      spaceDirectoryService = ExoContainerContext.getService(SpaceDirectoryService.class);
    }
    return spaceDirectoryService;
  }

}
