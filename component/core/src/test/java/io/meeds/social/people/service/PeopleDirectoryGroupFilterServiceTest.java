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
package io.meeds.social.people.service;

import static io.meeds.social.people.service.PeopleDirectoryService.GROUP_FILTER_INVALID_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.GROUP_NOT_SELECTABLE_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.TOO_MANY_GROUPS_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.identity.permission.service.UserPermissionService;
import io.meeds.social.people.model.PeopleDirectoryEditorGroups;
import io.meeds.social.people.model.PeopleDirectoryGroup;
import io.meeds.social.translation.service.TranslationService;

/**
 * The group filter of a People Directory instance (EXO-93257): who may set
 * it, which groups each editor can select, how a save merges with the stored
 * groups, its fail-closed read and the users listed. A JUnit 4 class
 * registered in NoContainerTestSuite.
 */
@RunWith(MockitoJUnitRunner.class)
public class PeopleDirectoryGroupFilterServiceTest {

  private static final String  MANAGER       = "mary";

  private static final String  ADMIN         = "root";

  private static final String  EDITOR        = "john";

  private static final String  MANAGER_TYPE  = "manager";

  private static final String  WINDOW_ID     = "42";

  private static final SiteKey SITE_KEY      = SiteKey.portal("dw");

  private static final PageKey PAGE_KEY      = new PageKey(SITE_KEY, "people");

  private static final String  PARIS         = "/spaces/paris";

  private static final String  LYON          = "/spaces/lyon";

  private static final String  HR            = "/organization/hr";

  private static final String  ADMIN_GROUP   = "/platform/x";

  private static final String  GONE          = "/organization/gone";

  @Mock
  private UserACL                 userAcl;

  @Mock
  private LayoutService           layoutService;

  @Mock
  private UserPortalConfigService userPortalConfigService;

  @Mock
  private ProfilePropertyService  profilePropertyService;

  @Mock
  private TranslationService      translationService;

  @Mock
  private IdentityManager         identityManager;

  @Mock
  private RelationshipManager     relationshipManager;

  @Mock
  private ProfileSearchConnector  profileSearchConnector;

  @Mock
  private UserPermissionService   userPermissionService;

  @Mock
  private OrganizationService     organizationService;

  @Mock
  private SpaceService            spaceService;

  @Mock
  private GroupHandler            groupHandler;

  @Mock
  private ListAccess<org.exoplatform.social.core.identity.model.Identity> users;

  @InjectMocks
  private PeopleDirectoryService  peopleDirectoryService;

  private final Map<String, Identity> identities = new HashMap<>();

  private Page                    page;

  @Before
  public void setUp() throws Exception {
    lenient().when(userAcl.getAdminMSType()).thenReturn(MANAGER_TYPE);
    for (String username : List.of(MANAGER, ADMIN, EDITOR)) {
      Identity identity = mock(Identity.class);
      identities.put(username, identity);
      lenient().when(userAcl.getUserIdentity(username)).thenReturn(identity);
      lenient().when(userAcl.isAdministrator(identity)).thenReturn(ADMIN.equals(username));
    }
    lenient().when(userPermissionService.getPermissionTokens(anyString())).thenReturn(List.of());
    // a manager of Paris and HR, a member of Lyon, an inherited manager of Lyon
    lenient().when(userPermissionService.getPermissionTokens(MANAGER))
             .thenReturn(List.of(MANAGER_TYPE + ":" + PARIS,
                                 "*:" + HR,
                                 "member:" + LYON,
                                 UserPermissionService.INHERITED_TOKEN_PREFIX + MANAGER_TYPE + ":" + LYON));
    lenient().when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    group(PARIS, "paris", "Paris office");
    group(LYON, "lyon", "Lyon office");
    group(HR, "hr", "Human resources");
    group(ADMIN_GROUP, "x", "Board");
    space(PARIS, "3", "Paris office");
    space(LYON, "4", "Lyon office");
    page = pageHolding(WINDOW_ID);
    lenient().when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
  }

  @Test
  public void canSetGroupFilterHoldsForAnAdministratorOrADirectManagerOnly() {
    assertTrue(peopleDirectoryService.canSetGroupFilter(ADMIN));
    assertTrue(peopleDirectoryService.canSetGroupFilter(MANAGER));
    assertFalse(peopleDirectoryService.canSetGroupFilter(EDITOR));
    assertFalse(peopleDirectoryService.canSetGroupFilter(null));
    assertFalse(peopleDirectoryService.canSetGroupFilter("unknown"));
  }

  @Test
  public void managedGroupsAreTheDirectManagerAndStarMembershipsOnly() {
    // member of Lyon and an inherited manager of Lyon: not a manager of Lyon
    assertEquals(Set.of(PARIS, HR), peopleDirectoryService.getManagedGroupIds(MANAGER));

    when(userPermissionService.getPermissionTokens(EDITOR))
      .thenReturn(List.of(UserPermissionService.INHERITED_TOKEN_PREFIX + "*:" + HR, "member:" + PARIS, "malformed"));
    assertEquals(Set.of(), peopleDirectoryService.getManagedGroupIds(EDITOR));
    assertFalse(peopleDirectoryService.canSetGroupFilter(EDITOR));
  }

  @Test
  public void normalizeGroupFilterKeepsTheStoredValueForAnEditorWhoMayNotSetIt() {
    String stored = stored(ADMIN_GROUP, null);

    assertEquals(stored, peopleDirectoryService.normalizeGroupFilter("[]", stored, EDITOR));
    assertEquals(stored, peopleDirectoryService.normalizeGroupFilter("[\"" + PARIS + "\"]", stored, EDITOR));
    assertEquals("[]", peopleDirectoryService.normalizeGroupFilter("[\"" + PARIS + "\"]", null, EDITOR));
  }

  @Test
  public void normalizeGroupFilterKeepsTheGroupsAManagerCannotSelectAndReplacesTheOthers() {
    String stored = "[" + entry(ADMIN_GROUP, null) + "," + entry(PARIS, "3") + "]";

    JSONArray groups = new JSONArray(peopleDirectoryService.normalizeGroupFilter("[\"" + HR + "\"]", stored, MANAGER));

    assertEquals(2, groups.length());
    assertEquals(ADMIN_GROUP, groups.getJSONObject(0).getString("groupId"));
    assertEquals(HR, groups.getJSONObject(1).getString("groupId"));
    assertTrue(groups.getJSONObject(1).isNull("spaceId"));
  }

  @Test
  public void normalizeGroupFilterStoresASpaceGroupWithTheIdOfItsSpace() {
    JSONArray groups = new JSONArray(peopleDirectoryService.normalizeGroupFilter("[\"" + PARIS + "\",\"" + PARIS + "\"]",
                                                                                 null,
                                                                                 MANAGER));

    assertEquals(1, groups.length());
    assertEquals(PARIS, groups.getJSONObject(0).getString("groupId"));
    assertEquals("3", groups.getJSONObject(0).getString("spaceId"));
  }

  @Test
  public void normalizeGroupFilterStoresARecreatedSpaceGroupWithItsNewSpace() {
    // the space was deleted then re-created under the same name: id 99 is gone
    JSONArray groups = new JSONArray(peopleDirectoryService.normalizeGroupFilter("[\"" + PARIS + "\"]",
                                                                                 stored(PARIS, "99"),
                                                                                 MANAGER));

    assertEquals(1, groups.length());
    assertEquals(PARIS, groups.getJSONObject(0).getString("groupId"));
    assertEquals("3", groups.getJSONObject(0).getString("spaceId"));
    assertEquals(List.of(PARIS), peopleDirectoryService.getGroupFilter(groups.toString()));
  }

  @Test
  public void normalizeGroupFilterRefusesAGroupTheEditorCannotSelect() {
    // a group the manager is a member of, an inherited manager of, or not in
    for (String groupId : List.of(LYON, ADMIN_GROUP, GONE)) {
      IllegalArgumentException e = assertThrows(groupId,
                                                IllegalArgumentException.class,
                                                () -> peopleDirectoryService.normalizeGroupFilter("[\"" + groupId + "\"]",
                                                                                                  null,
                                                                                                  MANAGER));
      assertEquals(groupId, GROUP_NOT_SELECTABLE_MESSAGE, e.getMessage());
    }
    // an administrator selects any group that exists
    assertEquals(1, new JSONArray(peopleDirectoryService.normalizeGroupFilter("[\"" + LYON + "\"]", null, ADMIN)).length());
    assertEquals(GROUP_NOT_SELECTABLE_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> peopleDirectoryService.normalizeGroupFilter("[\"" + GONE + "\"]", null, ADMIN)).getMessage());
  }

  @Test
  public void normalizeGroupFilterTurnedOffKeepsTheOtherLiveGroupsAndDropsTheVanishedOnes() {
    String stored = "[" + entry(ADMIN_GROUP, null) + "," + entry(GONE, null) + "," + entry(PARIS, "3") + "]";

    JSONArray off = new JSONArray(peopleDirectoryService.normalizeGroupFilter("[]", stored, MANAGER));
    assertEquals(1, off.length());
    assertEquals(ADMIN_GROUP, off.getJSONObject(0).getString("groupId"));

    // ON, a vanished group stays stored as it was, matching nobody
    JSONArray on = new JSONArray(peopleDirectoryService.normalizeGroupFilter("[\"" + PARIS + "\"]", stored, MANAGER));
    assertEquals(List.of(ADMIN_GROUP, GONE, PARIS),
                 IntStream.range(0, on.length()).mapToObj(i -> on.getJSONObject(i).getString("groupId")).toList());

    // an administrator turning it OFF drops every group
    assertEquals("[]", peopleDirectoryService.normalizeGroupFilter("[]", stored, ADMIN));
  }

  @Test
  public void normalizeGroupFilterRefusesTheInvalidForms() {
    for (String value : List.of("not json", "{}", "[1]", "[\"\"]", "[null]", "[{\"groupId\":\"" + PARIS + "\"}]")) {
      IllegalArgumentException e = assertThrows(value,
                                                IllegalArgumentException.class,
                                                () -> peopleDirectoryService.normalizeGroupFilter(value, null, MANAGER));
      assertEquals(value, GROUP_FILTER_INVALID_MESSAGE, e.getMessage());
    }
  }

  @Test
  public void normalizeGroupFilterRefusesMoreThanTwentyLiveGroups() throws Exception {
    List<String> groupIds = IntStream.range(0, 21).mapToObj(i -> "/organization/g" + i).toList();
    for (String groupId : groupIds) {
      group(groupId, groupId.substring(14), groupId);
    }
    String posted = groupIds.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(",", "[", "]"));
    assertEquals(TOO_MANY_GROUPS_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> peopleDirectoryService.normalizeGroupFilter(posted, null, ADMIN)).getMessage());

    // 20 posted plus a live group the manager cannot select: 21 applied
    when(userPermissionService.getPermissionTokens(MANAGER))
      .thenReturn(groupIds.subList(0, 20).stream().map(id -> MANAGER_TYPE + ":" + id).toList());
    String twenty = groupIds.subList(0, 20).stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(",", "[", "]"));
    assertEquals(TOO_MANY_GROUPS_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> peopleDirectoryService.normalizeGroupFilter(twenty,
                                                                                stored(ADMIN_GROUP, null),
                                                                                MANAGER)).getMessage());
    // a vanished group does not count
    assertEquals(21, new JSONArray(peopleDirectoryService.normalizeGroupFilter(twenty, stored(GONE, null), MANAGER)).length());
  }

  @Test
  public void groupFilterReadsTheLiveGroupsAndFailsClosed() {
    assertNull(peopleDirectoryService.getGroupFilter(null));
    assertNull(peopleDirectoryService.getGroupFilter("[]"));
    assertEquals(List.of(), peopleDirectoryService.getGroupFilter("not json"));
    assertEquals(List.of(), peopleDirectoryService.getGroupFilter("{\"groupId\":\"" + HR + "\"}"));
    assertEquals(List.of(), peopleDirectoryService.getGroupFilter("[\"" + HR + "\"]"));
    assertEquals(List.of(), peopleDirectoryService.getGroupFilter(stored(GONE, null)));
    // a space deleted then re-created under the same name: a new space id
    assertEquals(List.of(), peopleDirectoryService.getGroupFilter(stored(PARIS, "99")));
    assertEquals(List.of(), peopleDirectoryService.getGroupFilter(stored(PARIS, null)));
    assertEquals(List.of(HR, PARIS),
                 peopleDirectoryService.getGroupFilter("[" + entry(HR, null) + "," + entry(GONE, null) + "," + entry(PARIS, "3")
                     + "]"));
  }

  @Test
  public void editorGroupsShowTheSelectableGroupsAndOnlyFlagTheOthers() {
    String stored = "[" + entry(ADMIN_GROUP, null) + "," + entry(PARIS, "3") + "," + entry(GONE, null) + "]";

    PeopleDirectoryEditorGroups manager = peopleDirectoryService.getEditorGroups(stored, MANAGER);
    assertEquals(List.of(new PeopleDirectoryGroup(PARIS, "Paris office")), manager.groups());
    assertTrue(manager.otherGroupsApplied());
    assertEquals(1, manager.otherGroupsCount());
    assertTrue(manager.unresolvableGroups());

    PeopleDirectoryEditorGroups editor = peopleDirectoryService.getEditorGroups(stored, EDITOR);
    assertEquals(List.of(), editor.groups());
    assertTrue(editor.otherGroupsApplied());
    // an editor who may not set the filter gets no count, nor the vanished groups
    assertEquals(0, editor.otherGroupsCount());
    assertFalse(editor.unresolvableGroups());
    assertFalse(peopleDirectoryService.getEditorGroups(stored(HR, null), MANAGER).unresolvableGroups());

    PeopleDirectoryEditorGroups admin = peopleDirectoryService.getEditorGroups(stored, ADMIN);
    assertEquals(List.of(new PeopleDirectoryGroup(ADMIN_GROUP, "Board"), new PeopleDirectoryGroup(PARIS, "Paris office")),
                 admin.groups());
    assertFalse(admin.otherGroupsApplied());
    assertEquals(0, admin.otherGroupsCount());

    // a vanished group is neither shown nor applied
    assertFalse(peopleDirectoryService.getEditorGroups(stored(GONE, null), EDITOR).otherGroupsApplied());
    assertFalse(peopleDirectoryService.getEditorGroups("[]", EDITOR).otherGroupsApplied());
  }

  @Test
  public void editorGroupsFlagAStoredValueTheDirectoryCannotReadSoThatTurningItOffClearsIt() {
    for (String invalid : List.of("not json", "{}", "[\"" + HR + "\"]")) {
      // the directory lists nobody: the drawer must show the filter as set
      assertEquals(invalid, List.of(), peopleDirectoryService.getGroupFilter(invalid));
      assertTrue(invalid, peopleDirectoryService.getEditorGroups(invalid, MANAGER).unresolvableGroups());
      assertTrue(invalid, peopleDirectoryService.getEditorGroups(invalid, ADMIN).unresolvableGroups());
      // an editor who may not set the filter cannot clear it either
      assertFalse(invalid, peopleDirectoryService.getEditorGroups(invalid, EDITOR).unresolvableGroups());
      // turning it OFF stores no group
      assertEquals(invalid, "[]", peopleDirectoryService.normalizeGroupFilter("[]", invalid, MANAGER));
    }
    // an array holding one invalid entry is not read either, its live groups still shown
    String mixed = "[" + entry(PARIS, "3") + ",1]";
    PeopleDirectoryEditorGroups manager = peopleDirectoryService.getEditorGroups(mixed, MANAGER);
    assertTrue(manager.unresolvableGroups());
    assertEquals(List.of(new PeopleDirectoryGroup(PARIS, "Paris office")), manager.groups());
    // a readable value is not flagged
    assertFalse(peopleDirectoryService.getEditorGroups(null, MANAGER).unresolvableGroups());
    assertFalse(peopleDirectoryService.getEditorGroups("[]", MANAGER).unresolvableGroups());
    assertFalse(peopleDirectoryService.getEditorGroups(stored(PARIS, "3"), MANAGER).unresolvableGroups());
  }

  @Test
  public void selectableGroupsOfAManagerAreTheManagedGroupsMatchingTheQuery() throws Exception {
    when(userAcl.hasEditPermission(page, identities.get(MANAGER))).thenReturn(true);

    assertEquals(List.of(new PeopleDirectoryGroup(PARIS, "Paris office"), new PeopleDirectoryGroup(HR, "Human resources")),
                 peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, " ", null, MANAGER));
    assertEquals(List.of(new PeopleDirectoryGroup(HR, "Human resources")),
                 peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "human", null, MANAGER));
    verify(groupHandler, never()).findGroupsByKeyword(anyString());
    verify(groupHandler, never()).getAllGroups();

    // the groups the drawer already holds are left out
    assertEquals(List.of(new PeopleDirectoryGroup(HR, "Human resources")),
                 peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "", List.of(PARIS), MANAGER));
  }

  @Test
  public void selectableGroupsOfAnAdministratorLeaveOutTheHeldGroupsBeforeTheBound() throws Exception {
    when(userAcl.hasEditPermission(page, identities.get(ADMIN))).thenReturn(true);
    List<String> groupIds = IntStream.range(0, 21).mapToObj(i -> "/organization/g" + i).toList();
    List<Group> groups = new ArrayList<>();
    for (String groupId : groupIds) {
      group(groupId, groupId.substring(14), groupId);
      groups.add(groupHandler.findGroupById(groupId));
    }
    when(groupHandler.getAllGroups()).thenReturn(groups);

    List<PeopleDirectoryGroup> selectable = peopleDirectoryService.getSelectableGroups(PAGE_KEY,
                                                                                       SITE_KEY,
                                                                                       WINDOW_ID,
                                                                                       "",
                                                                                       List.of(groupIds.getFirst()),
                                                                                       ADMIN);

    assertEquals(groupIds.subList(1, 21), selectable.stream().map(PeopleDirectoryGroup::groupId).toList());
    // at most MAX_GROUPS_SUGGESTED groups
    assertEquals(groupIds.subList(0, 20),
                 peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "", null, ADMIN)
                                       .stream()
                                       .map(PeopleDirectoryGroup::groupId)
                                       .toList());
  }

  @Test
  public void selectableGroupsOfAnAdministratorAreEveryGroupMatchingItsLabelOrId() throws Exception {
    when(userAcl.hasEditPermission(page, identities.get(ADMIN))).thenReturn(true);
    List<Group> allGroups = new ArrayList<>();
    for (String groupId : List.of(ADMIN_GROUP, PARIS, LYON, HR)) {
      allGroups.add(groupHandler.findGroupById(groupId));
    }
    when(groupHandler.getAllGroups()).thenReturn(allGroups);

    // the label, whatever the name, ignoring case
    assertEquals(List.of(new PeopleDirectoryGroup(PARIS, "Paris office"), new PeopleDirectoryGroup(LYON, "Lyon office")),
                 peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, " OFFICE ", null, ADMIN));
    assertEquals(List.of(new PeopleDirectoryGroup(ADMIN_GROUP, "Board")),
                 peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "board", null, ADMIN));
    // or the id
    assertEquals(List.of(new PeopleDirectoryGroup(HR, "Human resources")),
                 peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "/organization", null, ADMIN));
    verify(groupHandler, never()).findGroupsByKeyword(anyString());
  }

  @Test
  public void selectableGroupsAreRefusedWithoutTheEditRightOrTheGroupRight() {
    when(userAcl.hasEditPermission(page, identities.get(EDITOR))).thenReturn(true);
    assertThrows(IllegalAccessException.class,
                 () -> peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "", null, EDITOR));

    when(userAcl.hasEditPermission(page, identities.get(MANAGER))).thenReturn(false);
    assertThrows(IllegalAccessException.class,
                 () -> peopleDirectoryService.getSelectableGroups(PAGE_KEY, SITE_KEY, WINDOW_ID, "", null, MANAGER));
  }

  @Test
  public void searchFiltersTheDirectoryByTheLiveStoredGroups() throws Exception {
    when(userAcl.hasAccessPermission(page, identities.get(EDITOR))).thenReturn(true);
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true))).thenReturn(users);

    peopleDirectoryService.search(PAGE_KEY,
                                  SITE_KEY,
                                  WINDOW_ID,
                                  "{}",
                                  "[" + entry(HR, null) + "," + entry(GONE, null) + "]",
                                  "",
                                  Map.of(),
                                  false,
                                  EDITOR,
                                  0,
                                  12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(identityManager).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(true));
    assertEquals(List.of(HR), filter.getValue().getDirectoryFilter().getGroups());
    assertTrue(filter.getValue().getDirectoryFilter().getAttributes().isEmpty());
    assertFalse(filter.getValue().isEmpty());
  }

  @Test
  public void searchOfADirectoryWhoseGroupsAllVanishedListsNobody() throws Exception {
    when(userAcl.hasAccessPermission(page, identities.get(EDITOR))).thenReturn(true);
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true))).thenReturn(users);

    peopleDirectoryService.search(PAGE_KEY, SITE_KEY, WINDOW_ID, "{}", stored(GONE, null), "", Map.of(), false, EDITOR, 0, 12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(identityManager).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(true));
    assertEquals(List.of(), filter.getValue().getDirectoryFilter().getGroups());
  }

  private void group(String groupId, String name, String label) throws Exception {
    Group group = mock(Group.class);
    lenient().when(group.getId()).thenReturn(groupId);
    lenient().when(group.getGroupName()).thenReturn(name);
    lenient().when(group.getLabel()).thenReturn(label);
    lenient().when(groupHandler.findGroupById(groupId)).thenReturn(group);
  }

  private void space(String groupId, String id, String displayName) {
    Space space = new Space();
    space.setId(id);
    space.setDisplayName(displayName);
    space.setGroupId(groupId);
    lenient().when(spaceService.getSpaceByGroupId(groupId)).thenReturn(space);
  }

  private static String stored(String groupId, String spaceId) {
    return "[" + entry(groupId, spaceId) + "]";
  }

  private static String entry(String groupId, String spaceId) {
    return new JSONObject().put("groupId", groupId).put("spaceId", spaceId == null ? JSONObject.NULL : spaceId).toString();
  }

  private Page pageHolding(String windowId) {
    Application application = new Application(windowId);
    Container container = new Container();
    container.setChildren(new ArrayList<ModelObject>(List.of(application)));
    Page holdingPage = new Page();
    holdingPage.setChildren(new ArrayList<ModelObject>(List.of(container)));
    return holdingPage;
  }
}
