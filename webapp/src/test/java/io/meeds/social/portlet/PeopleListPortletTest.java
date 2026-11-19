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

import static io.meeds.social.portlet.PeopleListPortlet.ATTRIBUTE_FILTER_PREFERENCE;
import static io.meeds.social.portlet.PeopleListPortlet.ATTRIBUTE_VALUES_RESOURCE_ID;
import static io.meeds.social.portlet.PeopleListPortlet.GROUPS_RESOURCE_ID;
import static io.meeds.social.portlet.PeopleListPortlet.GROUP_FILTER_PREFERENCE;
import static io.meeds.social.portlet.PeopleListPortlet.HEADER_TRANSLATIONS_PREFERENCE;
import static io.meeds.social.portlet.PeopleListPortlet.PAGE_SIZE_PREFERENCE;
import static io.meeds.social.portlet.PeopleListPortlet.SETTINGS_RESOURCE_ID;
import static io.meeds.social.portlet.PeopleListPortlet.USERS_RESOURCE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.webui.application.UIPortlet;

import io.meeds.social.people.model.PeopleDirectoryAttribute;
import io.meeds.social.people.model.PeopleDirectoryAttributeValue;
import io.meeds.social.people.model.PeopleDirectoryEditorGroups;
import io.meeds.social.people.model.PeopleDirectoryGroup;
import io.meeds.social.people.model.PeopleDirectoryPage;
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
    parameters.put("otherGroupsApplied", "true");

    portlet.processAction(request, response);

    verify(preferences).setValue(PAGE_SIZE_PREFERENCE, "20");
    verify(preferences, never()).setValue(eq("canEdit"), nullable(String.class));
    verify(preferences, never()).setValue(eq("otherGroupsApplied"), nullable(String.class));
    verify(preferences, never()).setValue(eq(GROUP_FILTER_PREFERENCE), nullable(String.class));
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
    when(resourceRequest.getRemoteUser()).thenReturn(USERNAME);
    when(resourceRequest.getPreferences()).thenReturn(preferences);
    when(resourceResponse.getWriter()).thenReturn(new PrintWriter(body, true));
    when(preferences.getValue(eq(PAGE_SIZE_PREFERENCE), nullable(String.class))).thenReturn("5000");
    when(preferences.getValue(eq(HEADER_TRANSLATIONS_PREFERENCE), nullable(String.class))).thenReturn("{\"en\":\"Team\"}");
    // what an editor would get: a viewer must get none of it
    lenient().when(peopleDirectoryService.getEditorGroups(nullable(String.class), any()))
             .thenReturn(new PeopleDirectoryEditorGroups(List.of(new PeopleDirectoryGroup("/spaces/hidden", "Hidden")), true, 3, true));
    lenient().when(peopleDirectoryService.getEditorAttributes(nullable(String.class), any())).thenReturn(List.of());

    portlet.serveResource(resourceRequest, resourceResponse);

    JSONObject settings = new JSONObject(body.toString());
    assertEquals(PeopleDirectoryService.MAX_PAGE_SIZE, settings.getInt(PAGE_SIZE_PREFERENCE));
    assertEquals("Team", settings.getJSONObject(HEADER_TRANSLATIONS_PREFERENCE).getString("en"));
    // the attribute and group filters are an editor's only
    assertFalse(settings.has("attributes"));
    assertFalse(settings.has("attributeFilterInvalid"));
    assertFalse(settings.has("canSetGroupFilter"));
    assertFalse(settings.has("groups"));
    assertFalse(settings.has("otherGroupsApplied"));
    assertFalse(settings.has("unresolvableGroups"));
    assertFalse(settings.has("otherGroupsCount"));
    verify(peopleDirectoryService, never()).getEditorGroups(nullable(String.class), any());
    verify(resourceResponse).setProperty("Cache-Control", "private, no-store");
  }

  @Test
  void settingsResourceAnswersTheAttributesToAnEditor() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(SETTINGS_RESOURCE_ID);
    StringWriter body = new StringWriter();
    ResourceResponse resourceResponse = resourceResponse(body);
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    lenient().when(preferences.getValue(eq(ATTRIBUTE_FILTER_PREFERENCE), nullable(String.class))).thenReturn("{\"department\":[\"HR\"]}");
    when(peopleDirectoryService.getEditorAttributes("{\"department\":[\"HR\"]}", Locale.FRENCH))
      .thenReturn(List.of(new PeopleDirectoryAttribute("department",
                                                        List.of(new PeopleDirectoryAttributeValue("HR", "HR")),
                                                        false)));
    when(peopleDirectoryService.getEditorGroups(null, USERNAME))
      .thenReturn(new PeopleDirectoryEditorGroups(List.of(new PeopleDirectoryGroup("/spaces/paris", "Paris office")), true, 3, true));
    when(peopleDirectoryService.canSetGroupFilter(USERNAME)).thenReturn(true);

    portlet.serveResource(resourceRequest, resourceResponse);

    JSONObject settings = new JSONObject(body.toString());
    assertTrue(settings.getBoolean("canSetGroupFilter"));
    assertTrue(settings.getBoolean("otherGroupsApplied"));
    assertTrue(settings.getBoolean("unresolvableGroups"));
    assertEquals(3, settings.getInt("otherGroupsCount"));
    assertEquals(1, settings.getJSONArray("groups").length());
    assertEquals("/spaces/paris", settings.getJSONArray("groups").getJSONObject(0).getString("groupId"));
    assertEquals("Paris office", settings.getJSONArray("groups").getJSONObject(0).getString("label"));
    JSONObject attribute = settings.getJSONArray("attributes").getJSONObject(0);
    assertEquals("department", attribute.getString("name"));
    assertEquals("HR", attribute.getJSONArray("values").getJSONObject(0).getString("value"));
    assertFalse(attribute.getBoolean("available"));
  }

  @ParameterizedTest
  @ValueSource(strings = { SETTINGS_RESOURCE_ID, USERS_RESOURCE_ID, ATTRIBUTE_VALUES_RESOURCE_ID, GROUPS_RESOURCE_ID })
  void everyResourceIsRefusedToAnAnonymousUser(String resourceId) throws Exception {
    // a request complete enough to be answered, but for its user
    ResourceRequest resourceRequest = resourceRequest(resourceId);
    when(resourceRequest.getRemoteUser()).thenReturn(null);
    lenient().when(resourceRequest.getParameterMap()).thenReturn(Map.of());
    lenient().when(peopleDirectoryService.search(any(), any(), any(), any(), any(), any(), any(), anyBoolean(), any(), anyInt(), anyInt()))
             .thenReturn(new PeopleDirectoryPage(List.of(), 0));
    ResourceResponse resourceResponse = resourceResponse(new StringWriter());

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "403");
    verify(resourceResponse).setProperty("Cache-Control", "private, no-store");
    verify(resourceResponse, never()).getWriter();
    verify(peopleDirectoryService, never()).search(any(), any(), any(), any(), any(), any(), any(), anyBoolean(), any(), anyInt(), anyInt());
    verify(peopleDirectoryService, never()).getAttributeValues(any(), any(), any(), any(), any(), any(), any());
    verify(peopleDirectoryService, never()).getSelectableGroups(any(), any(), any(), any(), any(), any());
  }

  @Test
  void usersResourceListsTheDirectoryOfTheWindowOfThePortalContext() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(USERS_RESOURCE_ID);
    StringWriter body = new StringWriter();
    ResourceResponse resourceResponse = resourceResponse(body);
    when(preferences.getValue(eq(ATTRIBUTE_FILTER_PREFERENCE), nullable(String.class))).thenReturn("{\"department\":[\"HR\"]}");
    when(preferences.getValue(eq(GROUP_FILTER_PREFERENCE), nullable(String.class)))
      .thenReturn("[{\"groupId\":\"/platform/users\",\"spaceId\":null}]");
    when(resourceRequest.getParameter("q")).thenReturn("john");
    when(resourceRequest.getParameter("filter")).thenReturn("connections");
    when(resourceRequest.getParameter("offset")).thenReturn("12");
    when(resourceRequest.getParameter("limit")).thenReturn("13");
    when(resourceRequest.getParameterMap()).thenReturn(Map.of("attr.city",
                                                              new String[] { "Tunis" },
                                                              "attributeFilter",
                                                              new String[] { "{}" },
                                                              "q",
                                                              new String[] { "john" }));
    when(peopleDirectoryService.search(PAGE_KEY,
                                       SITE_KEY,
                                       WINDOW_ID,
                                       "{\"department\":[\"HR\"]}",
                                       "[{\"groupId\":\"/platform/users\",\"spaceId\":null}]",
                                       "john",
                                       Map.of("city", "Tunis"),
                                       true,
                                       USERNAME,
                                       12,
                                       13)).thenReturn(new PeopleDirectoryPage(List.of(), 40));

    portlet.serveResource(resourceRequest, resourceResponse);

    JSONObject users = new JSONObject(body.toString());
    assertEquals(40, users.getInt("size"));
    assertEquals(12, users.getInt("offset"));
    assertEquals(0, users.getJSONArray("users").length());
    verify(resourceResponse).setProperty("Cache-Control", "private, no-store");
  }

  @Test
  void usersResourceAnswersForbiddenWithoutTheViewRight() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(USERS_RESOURCE_ID);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(resourceRequest.getParameterMap()).thenReturn(Map.of());
    when(peopleDirectoryService.search(any(), any(), any(), any(), any(), any(), any(), anyBoolean(), any(), anyInt(), anyInt()))
      .thenThrow(new IllegalAccessException("refused"));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "403");
    verify(resourceResponse, never()).getWriter();
  }

  @Test
  void usersResourceAnswersBadRequestWithTheMessageCode() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(USERS_RESOURCE_ID);
    StringWriter body = new StringWriter();
    ResourceResponse resourceResponse = resourceResponse(body);
    lenient().when(resourceRequest.getParameter("limit")).thenReturn("many");

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
    assertEquals(PeopleDirectoryService.INVALID_RANGE_MESSAGE, body.toString());
    verify(peopleDirectoryService, never()).search(any(), any(), any(), any(), any(), any(), any(), anyBoolean(), any(), anyInt(), anyInt());
  }

  @Test
  void attributeValuesResourceAnswersTheValuesOfTheWindowOfThePortalContext() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(ATTRIBUTE_VALUES_RESOURCE_ID);
    StringWriter body = new StringWriter();
    ResourceResponse resourceResponse = resourceResponse(body);
    when(resourceRequest.getParameter("attribute")).thenReturn("team");
    when(resourceRequest.getParameter("q")).thenReturn("Eng");
    when(peopleDirectoryService.getAttributeValues(PAGE_KEY, SITE_KEY, WINDOW_ID, "team", "Eng", USERNAME, Locale.FRENCH))
      .thenReturn(List.of(new PeopleDirectoryAttributeValue("7", "Ingénierie </script>")));

    portlet.serveResource(resourceRequest, resourceResponse);

    JSONObject value = new JSONArray(body.toString()).getJSONObject(0);
    assertEquals("7", value.getString("value"));
    assertEquals("Ingénierie </script>", value.getString("label"));
    verify(resourceResponse).setProperty("Cache-Control", "private, no-store");
  }

  @Test
  void attributeValuesResourceAnswersBadRequestForAnUnknownAttribute() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(ATTRIBUTE_VALUES_RESOURCE_ID);
    StringWriter body = new StringWriter();
    ResourceResponse resourceResponse = resourceResponse(body);
    when(peopleDirectoryService.getAttributeValues(any(), any(), any(), any(), any(), any(), any()))
      .thenThrow(new IllegalArgumentException(PeopleDirectoryService.UNKNOWN_ATTRIBUTE_MESSAGE));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
    assertEquals(PeopleDirectoryService.UNKNOWN_ATTRIBUTE_MESSAGE, body.toString());
  }

  @Test
  void processActionStoresTheNormalizedAttributeFilter() throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    parameters.put(ATTRIBUTE_FILTER_PREFERENCE, "{\"department\":[\"HR\",\"HR\"]}");
    when(peopleDirectoryService.normalizeAttributeFilter("{\"department\":[\"HR\",\"HR\"]}")).thenReturn("{\"department\":[\"HR\"]}");

    portlet.processAction(request, response);

    verify(preferences).setValue(ATTRIBUTE_FILTER_PREFERENCE, "{\"department\":[\"HR\"]}");
    verify(preferences).store();
  }

  @Test
  void processActionRefusesAnInvalidAttributeFilterBeforeStoringAnything() throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    parameters.put(PAGE_SIZE_PREFERENCE, "20");
    parameters.put(ATTRIBUTE_FILTER_PREFERENCE, "{\"department\":null}");
    when(peopleDirectoryService.normalizeAttributeFilter("{\"department\":null}"))
      .thenThrow(new IllegalArgumentException(PeopleDirectoryService.ATTRIBUTE_FILTER_INVALID_MESSAGE));

    PortletException e = assertThrows(PortletException.class, () -> portlet.processAction(request, response));

    assertEquals(PeopleDirectoryService.ATTRIBUTE_FILTER_INVALID_MESSAGE, e.getMessage());
    verify(preferences, never()).store();
  }

  @Test
  void processActionStoresTheGroupFilterMergedForTheEditor() throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    String stored = "[{\"groupId\":\"/platform/x\",\"spaceId\":null}]";
    when(preferences.getValue(eq(GROUP_FILTER_PREFERENCE), nullable(String.class))).thenReturn(stored);
    parameters.put(GROUP_FILTER_PREFERENCE, "[\"/spaces/paris\"]");
    String merged = "[{\"groupId\":\"/platform/x\",\"spaceId\":null},{\"groupId\":\"/spaces/paris\",\"spaceId\":\"3\"}]";
    lenient().when(peopleDirectoryService.normalizeGroupFilter("[\"/spaces/paris\"]", stored, USERNAME)).thenReturn(merged);

    portlet.processAction(request, response);

    verify(preferences).setValue(GROUP_FILTER_PREFERENCE, merged);
    verify(preferences).store();
  }

  @Test
  void processActionRefusesAGroupTheEditorCannotSelectBeforeStoringAnything() throws Exception {
    when(peopleDirectoryService.canEditSettings(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME)).thenReturn(true);
    parameters.put(PAGE_SIZE_PREFERENCE, "20");
    parameters.put(GROUP_FILTER_PREFERENCE, "[\"/platform/administrators\"]");
    when(peopleDirectoryService.normalizeGroupFilter(eq("[\"/platform/administrators\"]"), nullable(String.class), eq(USERNAME)))
      .thenThrow(new IllegalArgumentException(PeopleDirectoryService.GROUP_NOT_SELECTABLE_MESSAGE));

    PortletException e = assertThrows(PortletException.class, () -> portlet.processAction(request, response));

    assertEquals(PeopleDirectoryService.GROUP_NOT_SELECTABLE_MESSAGE, e.getMessage());
    verify(preferences, never()).store();
  }

  @Test
  void groupsResourceAnswersTheGroupsTheEditorCanSelectForTheWindowOfThePortalContext() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(GROUPS_RESOURCE_ID);
    StringWriter body = new StringWriter();
    ResourceResponse resourceResponse = resourceResponse(body);
    when(resourceRequest.getParameter("q")).thenReturn("par");
    when(resourceRequest.getParameterValues("exclude")).thenReturn(new String[] { "/spaces/lyon" });
    when(peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "par", List.of("/spaces/lyon"), USERNAME))
      .thenReturn(List.of(new PeopleDirectoryGroup("/spaces/paris", "Paris </script>")));

    portlet.serveResource(resourceRequest, resourceResponse);

    JSONObject group = new JSONArray(body.toString()).getJSONObject(0);
    assertEquals("/spaces/paris", group.getString("groupId"));
    assertEquals("Paris </script>", group.getString("label"));
    verify(resourceResponse).setProperty("Cache-Control", "private, no-store");
  }

  @Test
  void groupsResourceAnswersForbiddenToAnEditorWhoMayNotSetTheGroupFilter() throws Exception {
    ResourceRequest resourceRequest = resourceRequest(GROUPS_RESOURCE_ID);
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    when(peopleDirectoryService.getSelectableGroups(any(), any(), any(), any(), any(), any()))
      .thenThrow(new IllegalAccessException("refused"));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "403");
    verify(resourceResponse, never()).getWriter();
  }

  private ResourceRequest resourceRequest(String resourceId) {
    ResourceRequest resourceRequest = mock(ResourceRequest.class);
    lenient().when(resourceRequest.getResourceID()).thenReturn(resourceId);
    lenient().when(resourceRequest.getRemoteUser()).thenReturn(USERNAME);
    lenient().when(resourceRequest.getPreferences()).thenReturn(preferences);
    lenient().when(resourceRequest.getLocale()).thenReturn(Locale.FRENCH);
    return resourceRequest;
  }

  private ResourceResponse resourceResponse(StringWriter body) throws Exception {
    ResourceResponse resourceResponse = mock(ResourceResponse.class);
    lenient().when(resourceResponse.getWriter()).thenReturn(new PrintWriter(body, true));
    return resourceResponse;
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
