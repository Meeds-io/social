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

import static io.meeds.social.portlet.SubspacesListPortlet.ACCESS_DENIED_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.AVATAR_RESOURCE_ID;
import static io.meeds.social.portlet.SubspacesListPortlet.PARENT_SPACE_NOT_FOUND_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.DEFAULT_SUBSPACES_LIMIT;
import static io.meeds.social.portlet.SubspacesListPortlet.HEADER_TRANSLATIONS_INVALID_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.LIMIT_PARAMETER;
import static io.meeds.social.portlet.SubspacesListPortlet.MAX_RESOURCE_LIMIT;
import static io.meeds.social.portlet.SubspacesListPortlet.HEADER_TRANSLATIONS_PREFERENCE;
import static io.meeds.social.portlet.SubspacesListPortlet.LIMIT_OUT_OF_RANGE_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.SHOW_HIDDEN_SUBSPACES_INVALID_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.SHOW_HIDDEN_SUBSPACES_PREFERENCE;
import static io.meeds.social.portlet.SubspacesListPortlet.SPACE_ID_INVALID_MESSAGE;
import static io.meeds.social.portlet.SubspacesListPortlet.SPACE_ID_PARAMETER;
import static io.meeds.social.portlet.SubspacesListPortlet.SUBSPACES_LIMIT_PREFERENCE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

import java.io.ByteArrayOutputStream;
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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.json.JSONObject;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.services.thumbnail.ImageThumbnailService;

import io.meeds.social.image.plugin.FileThumbnailPlugin;

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

  /**
   * Rows the avatar resource looks through: the widget's own stored limit
   * plus the 'see more' row, not the 500-row cap of the listing resource.
   */
  private static final long         AVATAR_LISTING_BOUND = DEFAULT_SUBSPACES_LIMIT + 1L;

  @Mock
  private SpaceService              spaceService;

  @Mock
  private IdentityManager           identityManager;

  @Mock
  private FileItem                  avatarFile;

  @Mock
  private FileItem                  thumbnailFile;

  @Mock
  private ImageThumbnailService     imageThumbnailService;

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
    commonsUtils.when(() -> CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
    commonsUtils.when(() -> CommonsUtils.getService(ImageThumbnailService.class)).thenReturn(imageThumbnailService);
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
    lenient().when(preferences.getValue(eq(HEADER_TRANSLATIONS_PREFERENCE), anyString()))
             .thenReturn("{}");
    lenient().when(preferences.getValue(eq(SUBSPACES_LIMIT_PREFERENCE), any()))
             .thenReturn(String.valueOf(DEFAULT_SUBSPACES_LIMIT));
  }

  private ByteArrayOutputStream givenAnAvatarRequest(String spaceId) throws Exception {
    givenAResourceRequest();
    ByteArrayOutputStream imageBody = new ByteArrayOutputStream();
    when(resourceRequest.getResourceID()).thenReturn(AVATAR_RESOURCE_ID);
    lenient().when(resourceRequest.getParameter(SPACE_ID_PARAMETER)).thenReturn(spaceId);
    lenient().when(resourceResponse.getPortletOutputStream()).thenReturn(imageBody);
    return imageBody;
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
                                                                                              .thenThrow(new IllegalAccessException("User john isn't allowed to view space 42"));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "403");
    // a fixed message code, not the Service's sentence: that one names the
    // acting user and the space id, which belongs in LOG.debug
    assertEquals(ACCESS_DENIED_MESSAGE, responseBody.toString().trim());
  }

  @Test
  void serveResourceAnswers404WhenTheParentIsGone() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong()))
                                                                                              .thenThrow(new org.exoplatform.commons.exception.ObjectNotFoundException("Space with id 42 wasn't found"));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
    assertEquals(PARENT_SPACE_NOT_FOUND_MESSAGE, responseBody.toString().trim());
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

  @Test
  void serveResourceEchoesTheStoredPreferencesSoASaveCanBeConfirmed() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(preferences.getValue(eq(HEADER_TRANSLATIONS_PREFERENCE), anyString())).thenReturn("{\"en\":\"Teams\"}");
    when(preferences.getValue(eq(SHOW_HIDDEN_SUBSPACES_PREFERENCE), anyString())).thenReturn("true");
    when(preferences.getValue(eq(SUBSPACES_LIMIT_PREFERENCE), any())).thenReturn("9");
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(List.of());

    portlet.serveResource(resourceRequest, resourceResponse);

    // the action phase answers 200 even when it refused the write, so the
    // drawer can only tell a stored value from a refused one by reading the
    // preferences back from here
    JSONObject settings = new JSONObject(responseBody.toString()).getJSONObject("settings");
    assertEquals("Teams", settings.getJSONObject("headerTranslations").getString("en"));
    assertTrue(settings.getBoolean("showHiddenSubspaces"));
    assertEquals(9, settings.getInt("subspacesLimit"));
  }

  @Test
  void serveResourceAnswersUnreadableStoredTranslationsWithNoneRatherThanFailing() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(preferences.getValue(eq(HEADER_TRANSLATIONS_PREFERENCE), anyString())).thenReturn("not json");
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(List.of());

    portlet.serveResource(resourceRequest, resourceResponse);

    JSONObject settings = new JSONObject(responseBody.toString()).getJSONObject("settings");
    assertTrue(settings.getJSONObject("headerTranslations").isEmpty());
    // the widget still renders, on its default label
    assertTrue(new JSONObject(responseBody.toString()).getBoolean("parentSpace"));
  }

  @Test
  void serveResourceCarriesTheInvitedFlagOfEachRow() throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    Space hidden = subspace(7L, "Secret team", Space.HIDDEN);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(List.of(hidden));
    when(spaceService.isMember(hidden, USERNAME)).thenReturn(false);
    when(spaceService.isInvitedUser(hidden, USERNAME)).thenReturn(true);

    portlet.serveResource(resourceRequest, resourceResponse);

    // an invited viewer is served by the space REST endpoints exactly like a
    // member, so the row keeps its popover and the standard avatar URL
    JSONObject item = new JSONObject(responseBody.toString()).getJSONArray("subspaces").getJSONObject(0);
    assertFalse(item.getBoolean("isMember"));
    assertTrue(item.getBoolean("isInvited"));
  }

  @Test
  void serveAvatarStreamsTheAvatarOfASubspaceListedToTheViewer() throws Exception {
    ByteArrayOutputStream imageBody = givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    Space hidden = subspace(7L, "Secret team", Space.HIDDEN);
    when(spaceService.getSubspaces(anyLong(), eq(USERNAME), anyBoolean(), eq(0L), eq(AVATAR_LISTING_BOUND)))
                                                                                                                 .thenReturn(List.of(hidden));
    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, "secret_team");
    when(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "secret_team")).thenReturn(spaceIdentity);
    when(identityManager.getAvatarFile(spaceIdentity)).thenReturn(avatarFile);
    FileInfo fileInfo = new FileInfo(42L, "avatar.jpg", "image/jpeg", "social", 3L, null, "john", null, false);
    when(avatarFile.getFileInfo()).thenReturn(fileInfo);
    byte[] thumbnail = new byte[] { 1, 2, 3 };
    when(imageThumbnailService.getOrCreateThumbnail(FileThumbnailPlugin.FILE_TYPE, "42", "john", 100, 100)).thenReturn(thumbnailFile);
    when(thumbnailFile.getAsByte()).thenReturn(thumbnail);

    portlet.serveResource(resourceRequest, resourceResponse);

    // the same 100x100 thumbnail the space avatar endpoint serves by default
    assertArrayEquals(thumbnail, imageBody.toByteArray());
    verify(avatarFile, never()).getAsByte();
    verify(resourceResponse).setContentType("image/jpeg");
    verify(resourceResponse, never()).setProperty(eq(ResourceResponse.HTTP_STATUS_CODE), anyString());
  }

  @Test
  void serveAvatarFallsBackToTheOriginalWhenNoThumbnailCanBeProduced() throws Exception {
    ByteArrayOutputStream imageBody = givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    Space hidden = subspace(7L, "Secret team", Space.HIDDEN);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(List.of(hidden));
    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, "secret_team");
    when(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "secret_team")).thenReturn(spaceIdentity);
    when(identityManager.getAvatarFile(spaceIdentity)).thenReturn(avatarFile);
    when(avatarFile.getFileInfo()).thenReturn(new FileInfo(42L, "avatar", null, "social", 3L, null, "john", null, false));
    when(imageThumbnailService.getOrCreateThumbnail(anyString(), anyString(), anyString(), eq(100), eq(100)))
                                                                                                          .thenThrow(new IllegalStateException("no thumbnail"));
    byte[] original = new byte[] { 9, 8 };
    when(avatarFile.getAsByte()).thenReturn(original);

    portlet.serveResource(resourceRequest, resourceResponse);

    assertArrayEquals(original, imageBody.toByteArray());
    verify(resourceResponse).setContentType("image/png");
  }

  @Test
  void serveAvatarAnswers404WhenTheSpaceIdentityIsUnknown() throws Exception {
    givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    Space hidden = subspace(7L, "Secret team", Space.HIDDEN);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(List.of(hidden));
    when(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "secret_team")).thenReturn(null);

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
    verify(identityManager, never()).getAvatarFile(any());
  }

  @Test
  void serveAvatarAsksTheListingUnderTheStoredHiddenPreference() throws Exception {
    givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(preferences.getValue(eq(SHOW_HIDDEN_SUBSPACES_PREFERENCE), anyString())).thenReturn("true");
    // forging the flag on the query string must change nothing
    lenient().when(resourceRequest.getParameter(SHOW_HIDDEN_SUBSPACES_PREFERENCE)).thenReturn("false");

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(spaceService).getSubspaces(anyLong(), eq(USERNAME), eq(true), eq(0L), eq(AVATAR_LISTING_BOUND));
  }

  @ParameterizedTest
  @CsvSource({ "-5, 1", "0, 1", "100000, 25", "notANumber, 4", "12, 12" })
  void storedLimitIsBroughtBackInsideTheBoundsWhateverWroteIt(String stored, int expected) throws Exception {
    givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    // processAction refuses these, but a preference imported through the
    // layout editor never went through it
    when(preferences.getValue(eq(SUBSPACES_LIMIT_PREFERENCE), any())).thenReturn(stored);

    portlet.serveResource(resourceRequest, resourceResponse);

    // a huge value would put a huge listing on the render path, and a
    // negative one would make the Service throw IllegalArgumentException,
    // which this method does not catch — it would escape as a 500
    verify(spaceService).getSubspaces(anyLong(), eq(USERNAME), anyBoolean(), eq(0L), eq(expected + 1L));
  }

  @ParameterizedTest
  @CsvSource({ "-5, 1", "100000, 25", "notANumber, 4" })
  void envelopeEchoesTheClampedLimitSoTheWidgetNeverSlicesOnAnUnusableOne(String stored, int expected) throws Exception {
    givenAResourceRequest();
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(preferences.getValue(eq(SUBSPACES_LIMIT_PREFERENCE), any())).thenReturn(stored);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(List.of());

    portlet.serveResource(resourceRequest, resourceResponse);

    // the widget re-seats $root.settings on this value and slices with it:
    // a negative one would drop rows from the end of its own list
    JSONObject settings = new JSONObject(responseBody.toString()).getJSONObject("settings");
    assertEquals(expected, settings.getInt("subspacesLimit"));
  }

  @Test
  void serveAvatarLooksOnlyThroughTheRowsTheWidgetRenders() throws Exception {
    givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(preferences.getValue(eq(SUBSPACES_LIMIT_PREFERENCE), any())).thenReturn("12");

    portlet.serveResource(resourceRequest, resourceResponse);

    // the stored limit plus the 'see more' row, never the 500-row cap of the
    // listing resource: this runs on the render path, once per hidden image
    verify(spaceService).getSubspaces(anyLong(), eq(USERNAME), anyBoolean(), eq(0L), eq(13L));
    verify(spaceService, never()).getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), eq((long) MAX_RESOURCE_LIMIT));
  }

  @Test
  void serveAvatarAnswers404ForASubspaceTheViewerIsNotListed() throws Exception {
    ByteArrayOutputStream imageBody = givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong()))
                                                                                              .thenReturn(List.of(subspace(8L, "Other", Space.PRIVATE)));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
    verifyNoInteractions(identityManager);
    assertEquals(0, imageBody.size());
  }

  @Test
  void serveAvatarAnswers404WhenTheSubspaceHasNoAvatarFile() throws Exception {
    givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    Space hidden = subspace(7L, "Secret team", Space.HIDDEN);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(List.of(hidden));
    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, "secret_team");
    when(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "secret_team")).thenReturn(spaceIdentity);
    when(identityManager.getAvatarFile(spaceIdentity)).thenReturn(null);

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
  }

  @ParameterizedTest
  @ValueSource(strings = { "0", "-3", "seven" })
  void serveAvatarAnswers400OnAnUnusableId(String spaceId) throws Exception {
    givenAnAvatarRequest(spaceId);
    when(spaceService.isParentSpace(space)).thenReturn(true);

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
    assertEquals(SPACE_ID_INVALID_MESSAGE, responseBody.toString().trim());
    verify(spaceService, never()).getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong());
  }

  @Test
  void serveAvatarAnswers404OutsideAParentSpaceWithoutAnyListingQuery() throws Exception {
    givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(false);

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
    verify(spaceService, never()).getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong());
    verifyNoInteractions(identityManager);
  }

  @Test
  void serveAvatarAnswers403WhenTheViewerCannotViewTheParent() throws Exception {
    givenAnAvatarRequest("7");
    when(spaceService.isParentSpace(space)).thenReturn(true);
    when(spaceService.getSubspaces(anyLong(), anyString(), anyBoolean(), anyLong(), anyLong()))
                                                                                              .thenThrow(new IllegalAccessException("refused"));

    portlet.serveResource(resourceRequest, resourceResponse);

    verify(resourceResponse).setProperty(ResourceResponse.HTTP_STATUS_CODE, "403");
    verifyNoInteractions(identityManager);
  }
}
