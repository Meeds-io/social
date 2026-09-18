/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

import static io.meeds.social.portlet.SubspacesListPortlet.HEADER_TRANSLATIONS_INVALID_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.LIMIT_PARAMETER;
import static io.meeds.social.portlet.SubspacesListPortlet.MAX_RESOURCE_LIMIT;
import static io.meeds.social.portlet.SubspacesListPortlet.HEADER_TRANSLATIONS_PREFERENCE;
import static io.meeds.social.portlet.SubspacesListPortlet.LIMIT_OUT_OF_RANGE_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.SHOW_HIDDEN_SUBSPACES_INVALID_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.SHOW_HIDDEN_SUBSPACES_PREFERENCE;
import static io.meeds.social.portlet.SubspacesListPortlet.SUBSPACES_LIMIT_PREFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.json.JSONObject;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/**
 * Pins the trust boundary of the widget preferences: only a user who can
 * manage the current space writes them, only the three known names are
 * stored, and a value the widget cannot render is refused before anything is
 * written.
 */
@ExtendWith(MockitoExtension.class)
class SubspacesListPortletTest {

  private static final String       USERNAME = "john";

  @Mock
  private SpaceService              spaceService;

  private final Space               space      = new Space();

  @Mock
  private ActionRequest             request;

  @Mock
  private ActionResponse            response;

  @Mock
  private PortletPreferences        preferences;

  @Mock
  private ResourceRequest           resourceRequest;

  @Mock
  private ResourceResponse          resourceResponse;

  private StringWriter              responseBody;

  private MockedStatic<SpaceUtils>  spaceUtils;

  private MockedStatic<CommonsUtils> commonsUtils;

  private final Map<String, String> parameters = new HashMap<>();

  private SubspacesListPortlet      portlet;

  @BeforeEach
  void setUp() {
    spaceUtils = mockStatic(SpaceUtils.class);
    commonsUtils = mockStatic(CommonsUtils.class);
    spaceUtils.when(SpaceUtils::getSpaceByContext).thenReturn(space);
    commonsUtils.when(() -> CommonsUtils.getService(SpaceService.class)).thenReturn(spaceService);
    lenient().when(request.getRemoteUser()).thenReturn(USERNAME);
    lenient().when(request.getPreferences()).thenReturn(preferences);
    lenient().when(request.getParameter(anyString())).thenAnswer(invocation -> parameters.get(invocation.getArgument(0)));
    portlet = new SubspacesListPortlet();
  }

  private void givenAResourceRequest() throws Exception {
    responseBody = new StringWriter();
    lenient().when(resourceRequest.getRemoteUser()).thenReturn(USERNAME);
    lenient().when(resourceRequest.getPreferences()).thenReturn(preferences);
    lenient().when(resourceRequest.getLocale()).thenReturn(Locale.ENGLISH);
    lenient().when(resourceResponse.getWriter()).thenReturn(new PrintWriter(responseBody, true));
    lenient().when(preferences.getValue(eq(SHOW_HIDDEN_SUBSPACES_PREFERENCE), anyString()))
             .thenReturn("false");
  }

  private Space subspace(long id, String displayName, String visibility) {
    Space subspace = new Space();
    subspace.setId(id);
    subspace.setDisplayName(displayName);
    subspace.setPrettyName(displayName);
    subspace.setVisibility(visibility);
    return subspace;
  }

  @AfterEach
  void tearDown() {
    spaceUtils.close();
    commonsUtils.close();
  }

  @Test
  void processActionRefusesAUserWhoCannotManageTheCurrentSpace() throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(false);

    assertThrows(PortletException.class, () -> portlet.processAction(request, response));
    verifyNoInteractions(preferences);
  }

  @Test
  void processActionStoresOnlyTheThreeKnownPreferences() throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(HEADER_TRANSLATIONS_PREFERENCE, "{\"en\":\"Teams\"}");
    parameters.put(SHOW_HIDDEN_SUBSPACES_PREFERENCE, "true");
    parameters.put(SUBSPACES_LIMIT_PREFERENCE, "6");
    parameters.put("arbitraryName", "arbitraryValue");

    portlet.processAction(request, response);

    verify(preferences).setValue(HEADER_TRANSLATIONS_PREFERENCE, new JSONObject("{\"en\":\"Teams\"}").toString());
    verify(preferences).setValue(SHOW_HIDDEN_SUBSPACES_PREFERENCE, "true");
    verify(preferences).setValue(SUBSPACES_LIMIT_PREFERENCE, "6");
    verify(preferences, never()).setValue(eq("arbitraryName"), anyString());
    verify(preferences).store();
  }

  @Test
  void processActionLeavesAnAbsentPreferenceUntouched() throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(SUBSPACES_LIMIT_PREFERENCE, "3");

    portlet.processAction(request, response);

    verify(preferences).setValue(SUBSPACES_LIMIT_PREFERENCE, "3");
    verify(preferences, never()).setValue(eq(HEADER_TRANSLATIONS_PREFERENCE), any());
    verify(preferences, never()).setValue(eq(SHOW_HIDDEN_SUBSPACES_PREFERENCE), any());
    verify(preferences).store();
  }

  @ParameterizedTest
  @ValueSource(strings = { "true", "false" })
  void processActionStoresAnExplicitHiddenFlag(String flag) throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(SHOW_HIDDEN_SUBSPACES_PREFERENCE, flag);

    portlet.processAction(request, response);

    verify(preferences).setValue(SHOW_HIDDEN_SUBSPACES_PREFERENCE, flag);
    verify(preferences).store();
  }

  @ParameterizedTest
  @ValueSource(strings = { "yes please", "TRUE", "1", "" })
  void processActionRefusesAHiddenFlagThatIsNotAnExplicitBoolean(String flag) throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(SHOW_HIDDEN_SUBSPACES_PREFERENCE, flag);

    PortletException exception = assertThrows(PortletException.class, () -> portlet.processAction(request, response));

    assertEquals(SHOW_HIDDEN_SUBSPACES_INVALID_MESSAGE, exception.getMessage());
    verify(preferences, never()).store();
  }

  @Test
  void processActionRefusesALimitOutOfRangeBeforeStoringAnything() throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(HEADER_TRANSLATIONS_PREFERENCE, "{}");
    parameters.put(SUBSPACES_LIMIT_PREFERENCE, "26");

    PortletException exception = assertThrows(PortletException.class, () -> portlet.processAction(request, response));

    assertEquals(LIMIT_OUT_OF_RANGE_MESSAGE, exception.getMessage());
    verify(preferences, never()).store();
  }

  @Test
  void processActionRefusesANonNumericLimit() throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(SUBSPACES_LIMIT_PREFERENCE, "four");

    assertThrows(PortletException.class, () -> portlet.processAction(request, response));
    verify(preferences, never()).store();
  }

  @Test
  void processActionRefusesWhenThePortletIsRenderedOutsideAnySpace() throws Exception {
    spaceUtils.when(SpaceUtils::getSpaceByContext).thenReturn(null);
    when(spaceService.canManageSpace(null, USERNAME)).thenReturn(false);

    assertThrows(PortletException.class, () -> portlet.processAction(request, response));
    verifyNoInteractions(preferences);
  }

  @ParameterizedTest
  @ValueSource(strings = { "1", "25" })
  void processActionAcceptsTheLimitBoundaries(String limit) throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(SUBSPACES_LIMIT_PREFERENCE, limit);

    portlet.processAction(request, response);

    verify(preferences).setValue(SUBSPACES_LIMIT_PREFERENCE, limit);
    verify(preferences).store();
  }

  @ParameterizedTest
  @ValueSource(strings = { "0", "-1" })
  void processActionRefusesALimitBelowTheMinimum(String limit) throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(SUBSPACES_LIMIT_PREFERENCE, limit);

    PortletException exception = assertThrows(PortletException.class, () -> portlet.processAction(request, response));

    assertEquals(LIMIT_OUT_OF_RANGE_MESSAGE, exception.getMessage());
    verify(preferences, never()).store();
  }

  @ParameterizedTest
  @ValueSource(strings = { "not json", "[\"en\"]", "\"Teams\"", "{\"en\":{\"x\":1}}", "{\"en\":[\"a\"]}", "{\"en\":1}" })
  void processActionRefusesHeaderTranslationsThatAreNotAJsonObject(String headerTranslations) throws Exception {
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    parameters.put(HEADER_TRANSLATIONS_PREFERENCE, headerTranslations);
    parameters.put(SUBSPACES_LIMIT_PREFERENCE, "4");

    PortletException exception = assertThrows(PortletException.class, () -> portlet.processAction(request, response));

    assertEquals(HEADER_TRANSLATIONS_INVALID_MESSAGE, exception.getMessage());
    verify(preferences, never()).store();
  }

  @Test
  void serveResourceAnswersNotAParentSpaceWithoutAnyListingQuery() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(false);

    portlet.serveResource(resourceRequest, resourceResponse);

    assertTrue(responseBody.toString().contains("\"parentSpace\":false"));
    verify(spaceService, never()).getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong());
    verify(spaceService, never()).canCreateSubspace(any(), anyString(), any());
  }

  @Test
  void serveResourceAnswersNotAParentSpaceOutsideAnySpaceContext() throws Exception {
    givenAResourceRequest();
    spaceUtils.when(SpaceUtils::getSpaceByContext).thenReturn(null);

    portlet.serveResource(resourceRequest, resourceResponse);

    assertTrue(responseBody.toString().contains("\"parentSpace\":false"));
    verifyNoInteractions(spaceService);
  }

  @Test
  void serveResourceReadsTheHiddenFlagFromThePreferencesNeverFromTheRequest() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(preferences.getValue(eq(SHOW_HIDDEN_SUBSPACES_PREFERENCE), anyString())).thenReturn("true");
    // a caller forging the flag on the query string must change nothing —
    // lenient because the portlet is expected never to read it
    lenient().when(resourceRequest.getParameter(SHOW_HIDDEN_SUBSPACES_PREFERENCE)).thenReturn("false");

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(spaceService).getSubspaces(anyLong(), eq(USERNAME), eq(true), eq(0L), anyLong());
  }

  @Test
  void serveResourceKeepsTheHiddenFlagOffWhenThePreferenceIsAbsent() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    lenient().when(resourceRequest.getParameter(SHOW_HIDDEN_SUBSPACES_PREFERENCE)).thenReturn("true");

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(spaceService).getSubspaces(anyLong(), eq(USERNAME), eq(false), eq(0L), anyLong());
    verify(resourceRequest, never()).getParameter(SHOW_HIDDEN_SUBSPACES_PREFERENCE);
  }

  @Test
  void serveResourceAsksTheLimitTheWidgetRequested() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(resourceRequest.getParameter(LIMIT_PARAMETER)).thenReturn("5");

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(spaceService).getSubspaces(anyLong(), anyString(), anyBoolean(), eq(0L), eq(5L));
  }

  @Test
  void serveResourceCapsTheLimitAndDefaultsToTheCap() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(resourceRequest.getParameter(LIMIT_PARAMETER)).thenReturn("100000");

    portlet.serveResource(resourceRequest, resourceResponse);
    verify(spaceService).getSubspaces(anyLong(), anyString(), anyBoolean(), eq(0L), eq((long) MAX_RESOURCE_LIMIT));

    givenAResourceRequest();
    when(resourceRequest.getParameter(LIMIT_PARAMETER)).thenReturn(null);
    portlet.serveResource(resourceRequest, resourceResponse);
    verify(spaceService, org.mockito.Mockito.times(2))
                                            .getSubspaces(anyLong(), anyString(), anyBoolean(), eq(0L), eq((long) MAX_RESOURCE_LIMIT));
  }

  @ParameterizedTest
  @ValueSource(strings = { "0", "-1", "notANumber" })
  void serveResourceAnswers400OnAnInvalidLimit(String limit) throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(resourceRequest.getParameter(LIMIT_PARAMETER)).thenReturn(limit);

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
    assertEquals(LIMIT_OUT_OF_RANGE_MESSAGE, responseBody.toString().trim());
    verify(spaceService, never()).getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong());
  }

  @Test
  void serveResourceAnswers403WhenTheViewerCannotViewTheParent() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong()))
                                                                                              .thenThrow(new IllegalAccessException("refused"));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "403");
  }

  @Test
  void serveResourceAnswers404WhenTheParentIsGone() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong()))
                                                                                              .thenThrow(new org.exoplatform.commons.exception.ObjectNotFoundException("gone"));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
  }

  @Test
  void serveResourceWritesTheEnvelopeTheWidgetRendersFrom() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(spaceService.canManageSpace(space, USERNAME)).thenReturn(true);
    when(spaceService.canCreateSubspace(eq(space), eq(USERNAME), any())).thenReturn(true);
    Space hidden = subspace(7L, "Secret team", Space.HIDDEN);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong()))
                                                                                              .thenReturn(List.of(hidden));
    when(spaceService.isMember(hidden, USERNAME)).thenReturn(false);

    portlet.serveResource(resourceRequest, resourceResponse);

    String body = responseBody.toString();
    assertTrue(body.contains("\"parentSpace\":true"));
    assertTrue(body.contains("\"canManageSpace\":true"));
    assertTrue(body.contains("\"canCreateSubspace\":true"));
    assertTrue(body.contains("\"displayName\":\"Secret team\""));
    assertTrue(body.contains("\"visibility\":\"hidden\""));
    // the shared <space-avatar> reads this exact field name to decide how a
    // hidden space is rendered to a viewer who is not a member of it
    assertTrue(body.contains("\"isMember\":false"));
    assertFalse(body.contains("\"size\""));
    verify(resourceResponse).setContentType("application/json");
  }
}
