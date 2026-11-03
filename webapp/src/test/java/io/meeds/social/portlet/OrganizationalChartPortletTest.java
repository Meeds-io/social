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
package io.meeds.social.portlet;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;

import io.meeds.social.organizationalchart.service.OrganizationalChartSettingService;

@ExtendWith(MockitoExtension.class)
class OrganizationalChartPortletTest {

  @Mock
  private OrganizationalChartSettingService organizationalChartSettingService;

  @Mock
  private UIPortlet                         uiPortlet;

  private MockedStatic<UIPortlet>           uiPortletStatic;

  private OrganizationalChartPortlet        portlet;

  @BeforeEach
  void setUp() {
    uiPortletStatic = mockStatic(UIPortlet.class);
    uiPortletStatic.when(UIPortlet::getCurrentUIPortlet).thenReturn(uiPortlet);
    portlet = new OrganizationalChartPortlet();
    portlet.setOrganizationalChartSettingService(organizationalChartSettingService);
  }

  @AfterEach
  void tearDown() {
    uiPortletStatic.close();
  }

  /**
   * The service compares the application id with the storage id of the window
   * that renders, never with the application id again
   */
  @Test
  void saveApplicationSpaceGivesTheWindowStorageIdAndThePageSite() {
    givenAWindowInPage("130", "group::/spaces/sales::members");
    portlet.saveApplicationSpace("125");
    verify(organizationalChartSettingService).saveApplicationSpace("125", "130", SiteKey.group("/spaces/sales"));
  }

  /**
   * The site is the one of the page that holds the window, never the one of the
   * request: the portlet viewer renders any application under the global site
   */
  @Test
  void saveApplicationSpaceGivesTheSiteOfThePortletViewerPage() {
    givenAWindowInPage("125", "portal::global::portlet-viewer");
    portlet.saveApplicationSpace("125");
    verify(organizationalChartSettingService).saveApplicationSpace("125", "125", SiteKey.portal("global"));
  }

  @Test
  void saveApplicationSpaceRecordsNothingForAWindowOutOfAPage() {
    lenient().when(uiPortlet.getStorageId()).thenReturn("125");
    when(uiPortlet.getAncestorOfType(UIPage.class)).thenReturn(null);
    portlet.saveApplicationSpace("125");
    verifyNoInteractions(organizationalChartSettingService);
  }

  private void givenAWindowInPage(String storageId, String pageId) {
    UIPage uiPage = mock(UIPage.class);
    when(uiPage.getPageId()).thenReturn(pageId);
    when(uiPortlet.getStorageId()).thenReturn(storageId);
    when(uiPortlet.getAncestorOfType(UIPage.class)).thenReturn(uiPage);
  }

}
