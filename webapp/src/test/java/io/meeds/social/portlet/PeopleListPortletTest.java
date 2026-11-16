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

import static io.meeds.social.portlet.PeopleListPortlet.HEADER_TRANSLATIONS_PREFERENCE;
import static io.meeds.social.portlet.PeopleListPortlet.PAGE_SIZE_PREFERENCE;
import static io.meeds.social.portlet.PeopleListPortlet.SETTINGS_RESOURCE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.webui.application.UIPortlet;

import io.meeds.social.people.service.PeopleDirectoryService;

/**
 * Pins the trust boundary of the People Directory settings: only a user whom
 * the service lets edit the window being rendered writes them, the window is
 * identified from the portal context only, and only the two known names are
 * stored, each validated before anything is written.
 */
@ExtendWith(MockitoExtension.class)
class PeopleListPortletTest {

  private static final String                USERNAME  = "john";

  private static final String                WINDOW_ID = "42";

  private static final SiteKey               SITE_KEY  = SiteKey.portal("dw");

  private static final PageKey               PAGE_KEY  = new PageKey(SITE_KEY, "people");

  @Mock
  private PeopleDirectoryService             peopleDirectoryService;

  @Mock
  private PortalRequestContext               portalRequestContext;

  @Mock
  private UIPortlet                          uiPortlet;

  @Mock
  private ActionRequest                      request;

  @Mock
  private ActionResponse                     response;

  @Mock
  private PortletPreferences                 preferences;

  private MockedStatic<CommonsUtils>         commonsUtils;

  private MockedStatic<PortalRequestContext> portalRequestContexts;

  private MockedStatic<UIPortlet>            uiPortlets;

  private final Map<String, String>          parameters = new HashMap<>();

  private PeopleListPortlet                  portlet;

  @BeforeEach
  void setUp() {
    commonsUtils = mockStatic(CommonsUtils.class);
    portalRequestContexts = mockStatic(PortalRequestContext.class);
    uiPortlets = mockStatic(UIPortlet.class);
    commonsUtils.when(() -> CommonsUtils.getService(PeopleDirectoryService.class)).thenReturn(peopleDirectoryService);
    portalRequestContexts.when(PortalRequestContext::getCurrentInstance).thenReturn(portalRequestContext);
    uiPortlets.when(UIPortlet::getCurrentUIPortlet).thenReturn(uiPortlet);

    Page page = mock(Page.class);
    lenient().when(page.getPageKey()).thenReturn(PAGE_KEY);
    lenient().when(portalRequestContext.getPage()).thenReturn(page);
    lenient().when(portalRequestContext.getSiteKey()).thenReturn(SITE_KEY);
    lenient().when(uiPortlet.getStorageId()).thenReturn(WINDOW_ID);

    lenient().when(peopleDirectoryService.normalizePageSize(nullable(String.class))).thenCallRealMethod();
    lenient().when(peopleDirectoryService.normalizeHeaderTranslations(nullable(String.class))).thenCallRealMethod();
    lenient().when(peopleDirectoryService.getPageSize(nullable(String.class))).thenCallRealMethod();
    lenient().when(peopleDirectoryService.getHeaderTranslations(nullable(String.class))).thenCallRealMethod();

    lenient().when(request.getRemoteUser()).thenReturn(USERNAME);
    lenient().when(request.getPreferences()).thenReturn(preferences);
    lenient().when(request.getParameter(anyString())).thenAnswer(invocation -> parameters.get(invocation.getArgument(0)));
    portlet = new PeopleListPortlet();
  }

  @AfterEach
  void tearDown() {
    uiPortlets.close();
    portalRequestContexts.close();
    commonsUtils.close();
  }

  @Test
  void canEditAsksTheServiceWithThePageSiteAndWindowOfThePortalContext() {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);

    assertTrue(PeopleListPortlet.canEdit(USERNAME));
  }

  @Test
  void canEditIsFalseWhenTheServiceRefuses() {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(false);

    assertFalse(PeopleListPortlet.canEdit(USERNAME));
  }

  @Test
  void canEditIsFalseOutsideAPortletRendering() {
    uiPortlets.when(UIPortlet::getCurrentUIPortlet).thenReturn(null);

    assertFalse(PeopleListPortlet.canEdit(USERNAME));
    verify(peopleDirectoryService, never()).canEditSettings(any(), any(), any(), any());
  }

  @Test
  void processActionRefusesAUserWithoutEditRight() throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(false);
    parameters.put(PAGE_SIZE_PREFERENCE, "20");

    assertThrows(PortletException.class, () -> portlet.processAction(request, response));
    verify(preferences, never()).setValue(anyString(), nullable(String.class));
    verify(preferences, never()).store();
  }

  @Test
  void processActionStoresTheNormalizedSettings() throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    parameters.put(PAGE_SIZE_PREFERENCE, " 20 ");
    parameters.put(HEADER_TRANSLATIONS_PREFERENCE, "{\"en\":\"Our team\",\"fr\":\"\"}");

    portlet.processAction(request, response);

    verify(preferences).setValue(PAGE_SIZE_PREFERENCE, "20");
    verify(preferences).setValue(HEADER_TRANSLATIONS_PREFERENCE, "{\"en\":\"Our team\"}");
    verify(preferences).store();
  }

  @Test
  void processActionStoresOnlyTheKnownPreferenceNames() throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    parameters.put(PAGE_SIZE_PREFERENCE, "20");
    parameters.put("canEdit", "true");
    parameters.put("groupIds", "[\"/platform/users\"]");

    portlet.processAction(request, response);

    verify(preferences).setValue(PAGE_SIZE_PREFERENCE, "20");
    verify(preferences, never()).setValue(eq("canEdit"), nullable(String.class));
    verify(preferences, never()).setValue(eq("groupIds"), nullable(String.class));
    verify(preferences, never()).setValue(eq(HEADER_TRANSLATIONS_PREFERENCE), nullable(String.class));
  }

  @ParameterizedTest
  @CsvSource(delimiter = '|', value = {
    "pageSize|0|peopleDirectory.pageSize.outOfRange",
    "pageSize|51|peopleDirectory.pageSize.outOfRange",
    "headerTranslations|{\"en\":12}|peopleDirectory.headerTranslations.invalid",
  })
  void processActionRefusesAnInvalidValueBeforeStoringAnything(String name, String value, String message) throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    parameters.put(name, value);

    PortletException e = assertThrows(PortletException.class, () -> portlet.processAction(request, response));
    assertEquals(message, e.getMessage());
    verify(preferences, never()).store();
  }

  @Test
  void settingsResourceAnswersTheStoredValuesAsTheDirectoryUsesThem() throws Exception {
    ResourceRequest resourceRequest = mock(ResourceRequest.class);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    StringWriter body = new StringWriter();
    when(resourceRequest.getResourceID()).thenReturn(SETTINGS_RESOURCE_ID);
    when(resourceRequest.getPreferences()).thenReturn(preferences);
    when(resourceResponse.getWriter()).thenReturn(new PrintWriter(body, true));
    when(preferences.getValue(eq(PAGE_SIZE_PREFERENCE), nullable(String.class))).thenReturn("5000");
    when(preferences.getValue(eq(HEADER_TRANSLATIONS_PREFERENCE), nullable(String.class))).thenReturn("{\"en\":\"Team\"}");

    portlet.serveResource(resourceRequest, resourceResponse);

    JSONObject settings = new JSONObject(body.toString());
    assertEquals(PeopleDirectoryService.MAX_PAGE_SIZE, settings.getInt(PAGE_SIZE_PREFERENCE));
    assertEquals("Team", settings.getJSONObject(HEADER_TRANSLATIONS_PREFERENCE).getString("en"));
    verify(resourceResponse).setProperty("Cache-Control", "private, no-store");
  }

  @Test
  void storedFilterAcceptsOnlyAllOrConnections() {
    when(preferences.getValue(eq(PeopleListPortlet.FILTER_PREFERENCE), nullable(String.class))).thenReturn("connections",
                                                                                                         "'); alert(1); ('",
                                                                                                         null);
    assertEquals("connections", PeopleListPortlet.storedFilter(preferences));
    assertEquals("all", PeopleListPortlet.storedFilter(preferences));
    assertEquals("all", PeopleListPortlet.storedFilter(preferences));
  }

}
