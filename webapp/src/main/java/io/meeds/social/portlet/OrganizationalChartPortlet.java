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
package io.meeds.social.portlet;

import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;

import io.meeds.social.organizationalchart.service.OrganizationalChartSettingService;

public class OrganizationalChartPortlet extends CMSPortlet {

  private static final String               OBJECT_TYPE       = "organizationalChart";

  private static final String               APPLICATION_ID    = "applicationId";

  private static final String               CAN_EDIT_SETTINGS = "canEditSettings";

  private OrganizationalChartSettingService organizationalChartSettingService;

  @Override
  public void init(PortletConfig config) throws PortletException {
    super.init(config);
    this.contentType = OBJECT_TYPE;
  }

  @Override
  public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    String applicationId = getOrCreateApplicationId(request.getPreferences());
    String id = StringUtils.removeStart(applicationId, OBJECT_TYPE);
    saveApplicationSpace(id);
    request.setAttribute(APPLICATION_ID, applicationId);
    request.setAttribute(CAN_EDIT_SETTINGS, getOrganizationalChartSettingService().canEdit(id, request.getRemoteUser()));
    super.doView(request, response);
  }

  private String getOrCreateApplicationId(PortletPreferences preferences) {
    String applicationId = preferences.getValue(APPLICATION_ID, null);
    if (applicationId == null) {
      // use the same way as the old application id is generated to avoid lost of of the settings
      applicationId = OBJECT_TYPE.concat(UIPortlet.getCurrentUIPortlet().getStorageId());
      savePreference(APPLICATION_ID, applicationId);
    }
    return applicationId;
  }

  /**
   * Records the space of the page that holds the window; a window of a site
   * or shared layout has no page ancestor and records none
   */
  void saveApplicationSpace(String id) {
    UIPortlet uiPortlet = UIPortlet.getCurrentUIPortlet();
    UIPage uiPage = uiPortlet == null ? null : uiPortlet.getAncestorOfType(UIPage.class);
    if (uiPage != null && StringUtils.isNotBlank(uiPage.getPageId())) {
      getOrganizationalChartSettingService().saveApplicationSpace(id,
                                                                  uiPortlet.getStorageId(),
                                                                  PageKey.parse(uiPage.getPageId()).getSite());
    }
  }

  void setOrganizationalChartSettingService(OrganizationalChartSettingService organizationalChartSettingService) {
    this.organizationalChartSettingService = organizationalChartSettingService;
  }

  private OrganizationalChartSettingService getOrganizationalChartSettingService() {
    if (organizationalChartSettingService == null) {
      organizationalChartSettingService = ExoContainerContext.getService(OrganizationalChartSettingService.class);
    }
    return organizationalChartSettingService;
  }

}
