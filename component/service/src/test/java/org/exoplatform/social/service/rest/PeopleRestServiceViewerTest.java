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
package org.exoplatform.social.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.rest.api.models.IdentityNameList;
import org.exoplatform.social.service.rest.api.models.IdentityNameList.Option;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PeopleRestServiceViewerTest {

  private static final Set<String> CONTAINER_LOOKUPS = Set.of("getViewerIdentity",
                                                              "getUserIdentity",
                                                              "getIdentityManager",
                                                              "getPortalContainer",
                                                              "isExternal");

  @Mock
  private RelationshipManager    relationshipManager;

  @Mock
  private SpaceService           spaceService;

  @Mock
  private ListAccess<Identity>   outgoingRequests;

  @Mock
  private IdentityManager        identityManager;

  @Mock
  private ListAccess<Identity>   searchedUsers;

  @Mock
  private ActivityManager       activityManager;

  @Mock
  private IdentityStorage        identityStorage;

  private MockedStatic<Util>     utilStatic;

  private MockedStatic<RestUtils> restUtilsStatic;

  @Mock
  private ListAccess<Space>      memberSpaces;

  private PeopleRestService      peopleRestService;

  private Identity               demoIdentity  = new Identity(OrganizationIdentityProvider.NAME, "demo");

  private Identity               rootIdentity  = new Identity(OrganizationIdentityProvider.NAME, "root");

  @Before
  public void setUp() throws Exception {
    demoIdentity.setId("1");
    rootIdentity.setId("2");
    peopleRestService = new PeopleRestService();
    setField("relationshipManager", relationshipManager);
    setField("spaceService", spaceService);
    setField("identityManager", identityManager);
    setField("activityManager", activityManager);
    when(identityManager.getIdentityStorage()).thenReturn(identityStorage);
    when(identityStorage.getIdentitiesForMentions(any(), any(), any(), anyLong(), anyLong(), anyBoolean())).thenReturn(List.of());
    // Real Util methods run, but those reading the container: Mockito invokes a
    // static method while stubbing it, and a container lookup boots a RootContainer
    utilStatic = mockStatic(Util.class,
                            invocation -> CONTAINER_LOOKUPS.contains(invocation.getMethod().getName()) ? Answers.RETURNS_DEFAULTS.answer(invocation)
                                                                                                       : invocation.callRealMethod());
    restUtilsStatic = mockStatic(RestUtils.class,
                                 invocation -> "getCurrentIdentity".equals(invocation.getMethod().getName()) ? Answers.RETURNS_DEFAULTS.answer(invocation)
                                                                                                             : invocation.callRealMethod());
    restUtilsStatic.when(RestUtils::getCurrentIdentity).thenReturn(demoIdentity);
    utilStatic.when(() -> Util.getViewerIdentity("demo")).thenReturn(demoIdentity);
    utilStatic.when(() -> Util.getViewerIdentity("root")).thenReturn(rootIdentity);
    utilStatic.when(() -> Util.getResponse(any(), any(), any(), any())).thenReturn(Response.ok().build());
    when(relationshipManager.getOutgoingByFilter(any(), any())).thenReturn(outgoingRequests);
    when(outgoingRequests.load(0, 20)).thenReturn(new Identity[0]);
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity("demo")));
  }

  @After
  public void tearDown() {
    ConversationState.setCurrent(null);
    utilStatic.close();
    restUtilsStatic.close();
  }

  @Test
  public void testTheViewerIsTheAuthenticatedUserWhateverCurrentUserNames() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);

    Response response = peopleRestService.suggestUsernames(mock(UriInfo.class),
                                                           request,
                                                           "m",
                                                           "root",
                                                           "pending",
                                                           null,
                                                           null,
                                                           null,
                                                           "json");

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertNoStore(response);
    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(relationshipManager).getOutgoingByFilter(eq(demoIdentity), filter.capture());
    assertEquals(demoIdentity, filter.getValue().getViewerIdentity());
  }

  @Test
  public void testABlankCurrentUserLeavesTheSpaceMemberSearchWithoutViewer() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
    when(spaceService.getSpaceByPrettyName("space1")).thenReturn(new Space());
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(false))).thenReturn(searchedUsers);
    when(searchedUsers.load(0, 20)).thenReturn(new Identity[0]);
    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);

    peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", "", "member_of_space", null, null, "space1", "json");
    verify(identityManager).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(false));
    assertNull("a viewer on the filter is left out of the results", filter.getValue().getViewerIdentity());

    peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", "root", "member_of_space", null, null, "space1", "json");
    verify(identityManager, times(2)).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME),
                                                                  filter.capture(),
                                                                  eq(false));
    assertEquals(demoIdentity, filter.getValue().getViewerIdentity());
  }

  @Test
  public void testTheSharedDocumentSearchListsTheSpacesOfTheAuthenticatedUser() throws Exception {
    searchSharedDocumentRecipients("root");

    verify(spaceService).getMemberSpacesByFilter(eq("demo"), any());
    verify(relationshipManager).getConnectionsByFilter(eq(demoIdentity), any());
  }

  @Test
  public void testTheSharedDocumentSearchWithoutCurrentUserListsTheSpacesOfTheAuthenticatedUser() throws Exception {
    searchSharedDocumentRecipients("");

    verify(spaceService).getMemberSpacesByFilter(eq("demo"), any());
  }

  @Test
  public void testTheSpaceMembersSuggestedForADocumentLeaveOutTheAuthenticatedUser() throws Exception {
    Space space = new Space();
    when(spaceService.getSpaceByPrettyName("space1")).thenReturn(space);
    @SuppressWarnings("unchecked")
    ListAccess<Identity> spaceMembers = mock(ListAccess.class);
    when(identityManager.getSpaceIdentityByProfileFilter(eq(space), any(), any(), eq(true))).thenReturn(spaceMembers);
    when(spaceMembers.getSize()).thenReturn(2);
    Identity[] members = new Identity[] { member("demo"), member("root") };
    when(spaceMembers.load(0, 2)).thenReturn(members);

    searchSharedDocumentRecipients("root", "space1");

    ArgumentCaptor<Object> suggestions = ArgumentCaptor.forClass(Object.class);
    utilStatic.verify(() -> Util.getResponse(suggestions.capture(), any(), any(), any()));
    List<String> suggestedUsers = ((IdentityNameList) suggestions.getValue()).getOptions()
                                                                             .stream()
                                                                             .map(Option::getValue)
                                                                             .toList();
    assertEquals(List.of("root"), suggestedUsers);
  }

  @Test
  public void testAnAnonymousOrUnauthenticatedCallIsRefused() throws Exception {
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity(IdentityConstants.ANONIM)));
    Response response = suggestPendingAsRoot();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    assertNoStore(response);

    ConversationState.setCurrent(null);
    response = suggestPendingAsRoot();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    assertNoStore(response);
  }

  @Test
  public void testABlankCurrentUserLeavesTheViewerOffTheOtherUsersSearch() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
    // As ConnectionFilterListAccess does, the connections lookup sets the viewer on the filter
    when(relationshipManager.getConnectionsByFilter(any(), any())).thenAnswer(invocation -> {
      ((ProfileFilter) invocation.getArgument(1)).setViewerIdentity(invocation.getArgument(0));
      return null;
    });
    Identity[] viewerAtSearch = new Identity[] { rootIdentity };
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(false))).thenAnswer(invocation -> {
      viewerAtSearch[0] = ((ProfileFilter) invocation.getArgument(1)).getViewerIdentity();
      return searchedUsers;
    });
    when(searchedUsers.load(anyInt(), anyInt())).thenReturn(new Identity[0]);

    peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", "", "user_to_invite", null, null, null, "json");
    assertNull(viewerAtSearch[0]);
    verify(relationshipManager, times(1)).getConnectionsByFilter(eq(demoIdentity), any());

    peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", "root", "user_to_invite", null, null, null, "json");
    assertEquals(demoIdentity, viewerAtSearch[0]);
    verify(relationshipManager, times(2)).getConnectionsByFilter(eq(demoIdentity), any());
  }

  @Test
  public void testABlankCurrentUserLeavesTheViewerOffTheOtherUsersSearchOfADocumentShare() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
    utilStatic.when(() -> Util.isExternal(any())).thenReturn(false);
    when(spaceService.getMemberSpacesByFilter(any(), any())).thenReturn(memberSpaces);
    when(memberSpaces.load(anyInt(), anyInt())).thenReturn(new Space[0]);
    // As ConnectionFilterListAccess does, the connections lookup sets the viewer on the filter
    when(relationshipManager.getConnectionsByFilter(any(), any())).thenAnswer(invocation -> {
      ((ProfileFilter) invocation.getArgument(1)).setViewerIdentity(invocation.getArgument(0));
      return null;
    });
    Identity[] viewerAtSearch = new Identity[] { rootIdentity };
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(false))).thenAnswer(invocation -> {
      viewerAtSearch[0] = ((ProfileFilter) invocation.getArgument(1)).getViewerIdentity();
      return searchedUsers;
    });
    when(searchedUsers.load(anyInt(), anyInt())).thenReturn(new Identity[0]);

    peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", "", "share_document", null, null, null, "json");
    assertNull(viewerAtSearch[0]);

    peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", "root", "share_document", null, null, null, "json");
    assertEquals(demoIdentity, viewerAtSearch[0]);
  }

  @Test
  public void testTheIncomingAndConfirmedSuggestionsAreThoseOfTheAuthenticatedUser() throws Exception {
    @SuppressWarnings("unchecked")
    ListAccess<Identity> relations = mock(ListAccess.class);
    when(relations.load(anyInt(), anyInt())).thenReturn(new Identity[0]);
    when(relationshipManager.getIncomingByFilter(any(), any())).thenReturn(relations);
    when(relationshipManager.getConnectionsByFilter(any(), any())).thenReturn(relations);

    assertEquals(Response.Status.OK.getStatusCode(), suggest("root", "incoming").getStatus());
    verify(relationshipManager).getIncomingByFilter(eq(demoIdentity), any());

    assertEquals(Response.Status.OK.getStatusCode(), suggest("root", "confirmed").getStatus());
    verify(relationshipManager).getConnectionsByFilter(eq(demoIdentity), any());
  }

  @Test
  public void testTheMentionSuggestionsListTheConnectionsOfTheAuthenticatedUser() throws Exception {
    suggest("", "mention_activity_stream");
    verify(relationshipManager, times(1)).getConnectionsByFilter(eq(demoIdentity), any());

    suggest("root", "mention_activity_stream");
    verify(relationshipManager, times(2)).getConnectionsByFilter(eq(demoIdentity), any());
  }

  @Test
  public void testTheCommentMentionSuggestionsListTheConnectionsOfTheAuthenticatedUser() throws Exception {
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    ActivityStream stream = mock(ActivityStream.class);
    when(stream.getType()).thenReturn(ActivityStream.Type.USER);
    when(activity.getActivityStream()).thenReturn(stream);
    when(activity.getCommentedIds()).thenReturn(new String[0]);
    when(activity.getMentionedIds()).thenReturn(new String[0]);
    when(activity.getLikeIdentityIds()).thenReturn(new String[0]);
    when(activityManager.getActivity("a1")).thenReturn(activity);

    suggest("", "mention_comment", "a1");
    verify(relationshipManager, times(1)).getConnectionsByFilter(eq(demoIdentity), any());

    suggest("root", "mention_comment", "a1");
    verify(relationshipManager, times(2)).getConnectionsByFilter(eq(demoIdentity), any());
  }

  private Response suggest(String currentUser, String typeOfRelation) throws Exception {
    return suggest(currentUser, typeOfRelation, null);
  }

  private Response suggest(String currentUser, String typeOfRelation, String activityId) throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
    return peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", currentUser, typeOfRelation, activityId, null, null, "json");
  }

  private Identity member(String username) {
    Identity identity = mock(Identity.class);
    when(identity.isEnable()).thenReturn(true);
    when(identity.getRemoteId()).thenReturn(username);
    when(identity.getId()).thenReturn(username + "-id");
    Profile profile = mock(Profile.class);
    when(profile.getFullName()).thenReturn(username);
    when(profile.getProperty(Profile.USERNAME)).thenReturn(username);
    when(identity.getProfile()).thenReturn(profile);
    return identity;
  }

  private void searchSharedDocumentRecipients(String currentUser) throws Exception {
    searchSharedDocumentRecipients(currentUser, null);
  }

  private void searchSharedDocumentRecipients(String currentUser, String spacePrettyName) throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getLocale()).thenReturn(Locale.ENGLISH);
    utilStatic.when(() -> Util.isExternal(any())).thenReturn(false);
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(false))).thenReturn(searchedUsers);
    when(searchedUsers.load(anyInt(), anyInt())).thenReturn(new Identity[0]);
    when(spaceService.getMemberSpacesByFilter(any(), any())).thenReturn(memberSpaces);
    when(memberSpaces.load(anyInt(), anyInt())).thenReturn(new Space[0]);

    Response response = peopleRestService.suggestUsernames(mock(UriInfo.class), request, "m", currentUser, "share_document", null, null, spacePrettyName, "json");
    assertNoStore(response);
  }

  private Response suggestPendingAsRoot() throws Exception {
    return peopleRestService.suggestUsernames(mock(UriInfo.class),
                                              mock(HttpServletRequest.class),
                                              "m",
                                              "root",
                                              "pending",
                                              null,
                                              null,
                                              null,
                                              "json");
  }

  private void assertNoStore(Response response) {
    assertTrue(String.valueOf(response.getMetadata().getFirst("Cache-Control")).contains("no-store"));
  }

  private void setField(String name, Object value) throws Exception {
    Field field = PeopleRestService.class.getDeclaredField(name);
    field.setAccessible(true); // NOSONAR
    field.set(peopleRestService, value);
  }
}
