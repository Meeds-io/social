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

import static io.meeds.social.people.service.PeopleDirectoryService.ATTRIBUTE_FILTER_INVALID_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.INVALID_RANGE_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.TOO_MANY_ATTRIBUTES_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.TOO_MANY_VALUES_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.UNKNOWN_ATTRIBUTE_MESSAGE;
import static io.meeds.social.people.service.PeopleDirectoryService.VALUE_TOO_LONG_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.social.people.model.PeopleDirectoryAttribute;
import io.meeds.social.people.model.PeopleDirectoryAttributeValue;
import io.meeds.social.people.model.PeopleDirectoryPage;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;

/**
 * The attribute filter of a People Directory instance (EXO-93256): its
 * validation, its fail-closed read, the users listed and the values an
 * editor can select. A JUnit 4 class registered in NoContainerTestSuite.
 */
@RunWith(MockitoJUnitRunner.class)
public class PeopleDirectoryFilterServiceTest {

  private static final String     USERNAME  = "john";

  private static final String     WINDOW_ID = "42";

  private static final SiteKey    SITE_KEY  = SiteKey.portal("dw");

  private static final PageKey    PAGE_KEY  = new PageKey(SITE_KEY, "people");

  private static final long       OPTION_ID = 7L;

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
  private Identity                identity;

  @Mock
  private ListAccess<org.exoplatform.social.core.identity.model.Identity> users;

  @InjectMocks
  private PeopleDirectoryService  peopleDirectoryService;

  private List<ProfilePropertySetting> settings;

  private Page                     page;

  @Before
  public void setUp() {
    settings = new ArrayList<>();
    settings.add(setting("department", false, false));
    settings.add(setting("skills.lang", true, false));
    settings.add(setting("team", false, true));
    settings.add(setting("email", false, false));
    lenient().when(profilePropertyService.getPropertySettings()).thenReturn(settings);
    lenient().when(userAcl.getUserIdentity(USERNAME)).thenReturn(identity);
    page = pageHolding(WINDOW_ID);
    lenient().when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
  }

  @Test
  public void normalizeAttributeFilterKeepsValidAttributesAndDeduplicatesValues() {
    String normalized = peopleDirectoryService.normalizeAttributeFilter("{\"department\":[\"HR\",\"Finance\",\"HR\"],\"skills.lang\":[\"C++\"]}");

    JSONObject json = new JSONObject(normalized);
    assertEquals(List.of("HR", "Finance"), json.getJSONArray("department").toList());
    assertEquals(List.of("C++"), json.getJSONArray("skills.lang").toList());
    assertEquals("{}", peopleDirectoryService.normalizeAttributeFilter(null));
    assertEquals("{}", peopleDirectoryService.normalizeAttributeFilter("{}"));
  }

  @Test
  public void normalizeAttributeFilterRefusesAnAttributeThatIsNotADirectoryAttribute() {
    settings.add(setting("city", false, false));
    settings.getLast().setVisible(false);
    settings.add(setting("country", false, false));
    settings.getLast().setActive(false);
    settings.add(setting("street", false, false));
    settings.getLast().setParentId(3L);
    for (String attribute : List.of("city", "country", "street", "email", "unknown")) {
      IllegalArgumentException e = assertThrows(attribute,
                                                IllegalArgumentException.class,
                                                () -> peopleDirectoryService.normalizeAttributeFilter("{\"" + attribute
                                                    + "\":[\"x\"]}"));
      assertEquals(attribute, UNKNOWN_ATTRIBUTE_MESSAGE, e.getMessage());
    }
    verify(profilePropertyService, never()).getProfileSettingByName(anyString());
  }

  @Test
  public void normalizeAttributeFilterRefusesTheNullAndInvalidForms() {
    for (String value : List.of("not json",
                                "[\"department\"]",
                                "{\"department\":null}",
                                "{\"department\":[\"a\",null]}",
                                "{\"department\":[]}",
                                "{\"department\":[12]}",
                                "{\"department\":\"HR\"}",
                                "{\"department\":[\" \"]}",
                                "{\"department\":[\"\u00A0\"]}",
                                "{\"department\":[\"\uFEFF \"]}")) {
      IllegalArgumentException e = assertThrows(value,
                                                IllegalArgumentException.class,
                                                () -> peopleDirectoryService.normalizeAttributeFilter(value));
      assertEquals(value, ATTRIBUTE_FILTER_INVALID_MESSAGE, e.getMessage());
    }
  }

  @Test
  public void normalizeAttributeFilterReadsBlankValuesAsTheDrawerDoes() {
    // JavaScript trim() keeps the separators U+001C to U+001F
    assertEquals(1,
                 new JSONObject(peopleDirectoryService.normalizeAttributeFilter("{\"department\":[\"\\u001F\"]}")).getJSONArray("department")
                                                                                                                  .length());
  }

  @Test
  public void normalizeAttributeFilterRefusesValuesBeyondTheBounds() {
    for (int i = 0; i < 11; i++) {
      settings.add(setting("attribute" + i, false, false));
    }
    String elevenAttributes = IntStream.range(0, 11)
                                       .mapToObj(i -> "\"attribute" + i + "\":[\"x\"]")
                                       .collect(Collectors.joining(",", "{", "}"));
    String tenAttributes = IntStream.range(0, 10)
                                    .mapToObj(i -> "\"attribute" + i + "\":[\"x\"]")
                                    .collect(Collectors.joining(",", "{", "}"));
    String twentyOneValues = IntStream.range(0, 21)
                                      .mapToObj(i -> "\"v" + i + "\"")
                                      .collect(Collectors.joining(",", "{\"department\":[", "]}"));
    String twentyValues = IntStream.range(0, 20)
                                   .mapToObj(i -> "\"v" + i + "\"")
                                   .collect(Collectors.joining(",", "{\"department\":[", "]}"));

    assertEquals(TOO_MANY_ATTRIBUTES_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> peopleDirectoryService.normalizeAttributeFilter(elevenAttributes)).getMessage());
    assertEquals(TOO_MANY_VALUES_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> peopleDirectoryService.normalizeAttributeFilter(twentyOneValues)).getMessage());
    String tooLongValue = "{\"department\":[\"" + "x".repeat(151) + "\"]}";
    assertEquals(VALUE_TOO_LONG_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> peopleDirectoryService.normalizeAttributeFilter(tooLongValue)).getMessage());
    assertEquals(10, new JSONObject(peopleDirectoryService.normalizeAttributeFilter(tenAttributes)).length());
    assertEquals(20, new JSONObject(peopleDirectoryService.normalizeAttributeFilter(twentyValues)).getJSONArray("department").length());
    assertEquals(1, new JSONObject(peopleDirectoryService.normalizeAttributeFilter("{\"department\":[\"" + "x".repeat(150)
        + "\"]}")).length());
  }

  /**
   * The settings drawer reads a filtered directory with no attribute to show
   * as a stored filter it must replace: that is exactly a stored value that
   * is not a JSON object. A JSON object the directory refuses still shows its
   * attributes, which the editor fixes or removes.
   */
  @Test
  public void aStoredFilterThatIsNotAnObjectListsNobodyAndShowsNoAttribute() {
    for (String stored : List.of("[]", "[\"department\"]", "\"department\"", "42", "not json")) {
      assertNull(stored, peopleDirectoryService.getAttributeFilter(stored));
      assertTrue(stored, peopleDirectoryService.getEditorAttributes(stored, Locale.ENGLISH).isEmpty());
    }
    for (String stored : List.of("{\"department\":null}", "{\"department\":[\"a\",null]}", "{\"department\":[]}")) {
      assertNull(stored, peopleDirectoryService.getAttributeFilter(stored));
      assertFalse(stored, peopleDirectoryService.getEditorAttributes(stored, Locale.ENGLISH).isEmpty());
    }
  }

  @Test
  public void getAttributeFilterReadsAnInvalidStoredValueAsListingNobody() {
    assertEquals(Map.of(), peopleDirectoryService.getAttributeFilter(null));
    assertEquals(Map.of("department", List.of("HR")), peopleDirectoryService.getAttributeFilter("{\"department\":[\"HR\"]}"));
    assertNull(peopleDirectoryService.getAttributeFilter("{\"department\":null}"));
    assertNull(peopleDirectoryService.getAttributeFilter("{\"department\":[\"a\",null]}"));
    settings.getFirst().setVisible(false);
    assertNull(peopleDirectoryService.getAttributeFilter("{\"department\":[\"HR\"]}"));
  }

  @Test
  public void canViewDirectoryWithTheViewRightOfTheHolder() {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    assertTrue(peopleDirectoryService.canViewDirectory(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));

    when(userAcl.hasAccessPermission(page, identity)).thenReturn(false);
    assertFalse(peopleDirectoryService.canViewDirectory(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  @Test
  public void canViewDirectoryWithTheViewRightOfTheSiteHoldingTheWindow() {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(pageHolding("other"));
    PortalConfig site = siteHolding(WINDOW_ID);
    when(layoutService.getPortalConfig(SITE_KEY)).thenReturn(site);
    when(userAcl.hasAccessPermission(site, identity)).thenReturn(true);

    assertTrue(peopleDirectoryService.canViewDirectory(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
  }

  @Test
  public void cannotViewADirectoryNoPageOrSiteHolds() {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(pageHolding("other"));

    assertFalse(peopleDirectoryService.canViewDirectory(PAGE_KEY, SITE_KEY, WINDOW_ID, USERNAME));
    assertFalse(peopleDirectoryService.canViewDirectory(PAGE_KEY, SITE_KEY, WINDOW_ID, null));
    verify(userAcl, never()).hasAccessPermission(any(Page.class), any(Identity.class));
  }

  @Test
  public void searchIsRefusedWithoutTheViewRight() {
    assertThrows(IllegalAccessException.class, () -> search("{}", Map.of(), false, 12));
    verifyNoInteractions(identityManager, relationshipManager);
  }

  @Test
  public void searchRefusesALimitOutOfRange() {
    Map<String, String> noViewerAttribute = Map.of();
    for (int limit : List.of(0, 101)) {
      assertEquals(INVALID_RANGE_MESSAGE,
                   assertThrows(IllegalArgumentException.class, () -> search("{}", noViewerAttribute, false, limit)).getMessage());
    }
  }

  @Test
  public void searchListsNobodyForAnInvalidStoredFilter() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);

    for (String stored : List.of("{\"department\":null}", "{\"department\":[\"a\",null]}", "{\"unknown\":[\"a\"]}")) {
      PeopleDirectoryPage result = search(stored, Map.of(), false, 12);
      assertEquals(stored, 0, result.size());
      assertTrue(stored, result.identities().isEmpty());
    }
    verifyNoInteractions(identityManager, relationshipManager);
  }

  @Test
  public void searchAppliesTheDirectoryFilterAndTheViewerCriteriaTheDirectoryLeavesFree() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    mockOptionLabels();
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true))).thenReturn(users);
    org.exoplatform.social.core.identity.model.Identity user = new org.exoplatform.social.core.identity.model.Identity("5");
    when(users.load(0, 12)).thenReturn(new org.exoplatform.social.core.identity.model.Identity[] { user });
    when(users.getSize()).thenReturn(1);
    Map<String, String> viewerAttributes = new HashMap<>();
    viewerAttributes.put("department", "Sales");
    viewerAttributes.put("email", "example.com");

    PeopleDirectoryPage result = search("{\"department\":[\"HR\"],\"team\":[\"" + OPTION_ID + "\"],\"skills.lang\":[\"Développement\"]}",
                                        viewerAttributes,
                                        false,
                                        12);

    assertEquals(1, result.size());
    assertEquals(List.of(user), result.identities());
    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(identityManager).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(true));
    Map<String, List<String>> directoryAttributes = filter.getValue().getDirectoryFilter().getAttributes();
    assertEquals(List.of("HR"), directoryAttributes.get("department"));
    assertEquals(List.of("Engineering-Ingenierie"), directoryAttributes.get("team"));
    assertEquals(List.of("Developpement"), directoryAttributes.get("skills.lang"));
    // the directory wins on its own attributes
    assertEquals(Map.of("email", "example.com"), filter.getValue().getProfileSettings());
    assertTrue(filter.getValue().isEnabled());
    assertNull(filter.getValue().getUserType());
    assertEquals("john d", filter.getValue().getName());
    verifyNoInteractions(relationshipManager);
  }

  @Test
  public void searchWithoutAttributeFilterSetsNoDirectoryFilter() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true))).thenReturn(users);

    search("{}", Map.of(), false, 12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(identityManager).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(true));
    assertNull(filter.getValue().getDirectoryFilter());
  }

  @Test
  public void searchOfConnectionsListsTheViewersConnectionsMatchingTheDirectory() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    org.exoplatform.social.core.identity.model.Identity viewer = new org.exoplatform.social.core.identity.model.Identity("3");
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(viewer);
    when(relationshipManager.getConnectionsByFilter(eq(viewer), any())).thenReturn(users);

    search("{\"department\":[\"HR\"]}", Map.of(), true, 12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(relationshipManager).getConnectionsByFilter(eq(viewer), filter.capture());
    assertEquals(List.of("HR"), filter.getValue().getDirectoryFilter().getAttributes().get("department"));
    assertFalse(filter.getValue().isEmpty());
    verify(identityManager, never()).getIdentitiesByProfileFilter(anyString(), any(), anyBoolean());
  }

  @Test
  public void searchOfConnectionsAppliesTheViewersAdvancedFilterWithinTheDirectory() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    org.exoplatform.social.core.identity.model.Identity viewer = new org.exoplatform.social.core.identity.model.Identity("3");
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(viewer);
    when(relationshipManager.getConnectionsByFilter(eq(viewer), any())).thenReturn(users);

    search("{\"department\":[\"HR\"]}", Map.of("team", "Eng", "department", "Finance"), true, 12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(relationshipManager).getConnectionsByFilter(eq(viewer), filter.capture());
    // the viewer's own criterion applies; the one on a directory attribute
    // is dropped, the directory's values win
    assertEquals(Map.of("team", "Eng"), filter.getValue().getProfileSettings());
    assertEquals(List.of("HR"), filter.getValue().getDirectoryFilter().getAttributes().get("department"));
  }

  @Test
  public void searchRestrictsAGuestToTheirSpacesConnectionsAndThemselfByDefault() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    when(identity.getGroups()).thenReturn(Set.of("/platform/externals", "/platform/users", "/organization/hr", "/spaces/paris"));
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(new org.exoplatform.social.core.identity.model.Identity("3"));
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true))).thenReturn(users);

    for (String stored : Arrays.asList(null, "true", "", "yes")) {
      search("{}", stored, Map.of(), false, 12);
    }

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(identityManager, times(4)).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(true));
    for (ProfileFilter captured : filter.getAllValues()) {
      assertEquals(Long.valueOf(3), captured.getGuestIdentityId());
      assertEquals(List.of("/spaces/paris"), captured.getViewerSpaceGroupIds());
      assertFalse(captured.isEmpty());
    }
  }

  @Test
  public void searchRestrictsAGuestsConnectionsToo() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    when(identity.getGroups()).thenReturn(Set.of("/platform/externals"));
    org.exoplatform.social.core.identity.model.Identity viewer = new org.exoplatform.social.core.identity.model.Identity("3");
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(viewer);
    when(relationshipManager.getConnectionsByFilter(eq(viewer), any())).thenReturn(users);

    search("{}", null, Map.of(), true, 12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(relationshipManager).getConnectionsByFilter(eq(viewer), filter.capture());
    assertEquals(Long.valueOf(3), filter.getValue().getGuestIdentityId());
    assertEquals(List.of(), filter.getValue().getViewerSpaceGroupIds());
  }

  @Test
  public void searchLeavesAGuestUnrestrictedWhenTheSettingIsOff() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    lenient().when(identity.getGroups()).thenReturn(Set.of("/platform/externals", "/spaces/paris"));
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true))).thenReturn(users);

    search("{}", " false ", Map.of(), false, 12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(identityManager).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(true));
    assertNull(filter.getValue().getGuestIdentityId());
    assertNull(filter.getValue().getViewerSpaceGroupIds());
  }

  @Test
  public void searchLeavesAnInternalViewerUnrestricted() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    when(identity.getGroups()).thenReturn(Set.of("/platform/users", "/spaces/paris"));
    when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true))).thenReturn(users);

    search("{}", null, Map.of(), false, 12);

    ArgumentCaptor<ProfileFilter> filter = ArgumentCaptor.forClass(ProfileFilter.class);
    verify(identityManager).getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), filter.capture(), eq(true));
    assertNull(filter.getValue().getGuestIdentityId());
    assertNull(filter.getValue().getViewerSpaceGroupIds());
    verify(identityManager, never()).getOrCreateUserIdentity(anyString());
  }

  @Test
  public void searchListsNobodyToAGuestWithoutSocialIdentity() throws Exception {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    when(identity.getGroups()).thenReturn(Set.of("/platform/externals", "/spaces/paris"));
    // a reachable listing, so that a guest let through fails on the
    // assertions below, not on a missing list
    lenient().when(identityManager.getIdentitiesByProfileFilter(eq(OrganizationIdentityProvider.NAME), any(), eq(true)))
             .thenReturn(users);
    lenient().when(users.getSize()).thenReturn(7);

    PeopleDirectoryPage result = search("{}", null, Map.of(), false, 12);

    assertEquals(0, result.size());
    verify(identityManager, never()).getIdentitiesByProfileFilter(anyString(), any(), anyBoolean());
  }

  @Test
  public void guestRestrictionFailsClosed() {
    assertTrue(peopleDirectoryService.isGuestRestriction(null));
    assertTrue(peopleDirectoryService.isGuestRestriction("true"));
    assertTrue(peopleDirectoryService.isGuestRestriction("FALSE"));
    assertTrue(peopleDirectoryService.isGuestRestriction("0"));
    assertFalse(peopleDirectoryService.isGuestRestriction("false"));
    assertEquals("false", peopleDirectoryService.normalizeGuestRestriction("false"));
    assertEquals("true", peopleDirectoryService.normalizeGuestRestriction("anything"));
    assertEquals("true", peopleDirectoryService.normalizeGuestRestriction(null));
  }

  @Test
  public void directoryAttributeNamesAreTheStoredAttributesOnly() {
    assertEquals(List.of("department", "skills.lang"),
                 peopleDirectoryService.getDirectoryAttributeNames("{\"department\":[\"HR\"],\"skills.lang\":[\"C++\"]}"));
    assertEquals(List.of(), peopleDirectoryService.getDirectoryAttributeNames("{}"));
    assertEquals(List.of(), peopleDirectoryService.getDirectoryAttributeNames(null));
    // a stored filter the directory cannot read names nothing
    assertEquals(List.of(), peopleDirectoryService.getDirectoryAttributeNames("{\"unknown\":[\"x\"]}"));
  }

  @Test
  public void searchRefusesAViewerAttributeThatIsNotSelectable() {
    when(userAcl.hasAccessPermission(page, identity)).thenReturn(true);
    settings.getFirst().setVisible(false);

    Map<String, String> hiddenAttribute = Map.of("department", "HR");
    Map<String, String> unknownAttribute = Map.of("unknown", "HR");
    assertEquals(UNKNOWN_ATTRIBUTE_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> search("{}", hiddenAttribute, false, 12)).getMessage());
    assertEquals(UNKNOWN_ATTRIBUTE_MESSAGE,
                 assertThrows(IllegalArgumentException.class,
                              () -> search("{}", unknownAttribute, false, 12)).getMessage());
  }

  @Test
  public void getAttributeValuesIsRefusedToWhoMayNotEditTheSettings() {
    when(userAcl.hasEditPermission(page, identity)).thenReturn(false);

    assertThrows(IllegalAccessException.class,
                 () -> peopleDirectoryService.getAttributeValues(PAGE_KEY,
                                                                 SITE_KEY,
                                                                 WINDOW_ID,
                                                                 "department",
                                                                 null,
                                                                 USERNAME,
                                                                 Locale.ENGLISH));
    verifyNoInteractions(profileSearchConnector);
  }

  @Test
  public void getAttributeValuesRefusesAnAttributeThatIsNotADirectoryAttribute() {
    when(userAcl.hasEditPermission(page, identity)).thenReturn(true);

    for (String attribute : List.of("email", "unknown")) {
      assertEquals(UNKNOWN_ATTRIBUTE_MESSAGE,
                   assertThrows(IllegalArgumentException.class,
                                () -> peopleDirectoryService.getAttributeValues(PAGE_KEY,
                                                                                SITE_KEY,
                                                                                WINDOW_ID,
                                                                                attribute,
                                                                                null,
                                                                                USERNAME,
                                                                                Locale.ENGLISH)).getMessage());
    }
  }

  @Test
  public void getAttributeValuesAnswersTheIndexedValues() throws Exception {
    when(userAcl.hasEditPermission(page, identity)).thenReturn(true);
    when(profileSearchConnector.getAttributeValues("skills.lang", true, "C+", 50)).thenReturn(List.of("C++", "C+" + "x".repeat(149), "C+"));

    List<PeopleDirectoryAttributeValue> values = peopleDirectoryService.getAttributeValues(PAGE_KEY,
                                                                                          SITE_KEY,
                                                                                          WINDOW_ID,
                                                                                          "skills.lang",
                                                                                          " C+ ",
                                                                                          USERNAME,
                                                                                          Locale.ENGLISH);

    assertEquals(List.of(new PeopleDirectoryAttributeValue("C++", "C++"), new PeopleDirectoryAttributeValue("C+", "C+")), values);
  }

  @Test
  public void getAttributeValuesAnswersTheOptionsOfADropdown() throws Exception {
    when(userAcl.hasEditPermission(page, identity)).thenReturn(true);
    when(translationService.getTranslationLabelOrDefault("propertySettingOption",
                                                         OPTION_ID,
                                                         "optionValue",
                                                         Locale.FRENCH)).thenReturn("Ingénierie");

    List<PeopleDirectoryAttributeValue> values = peopleDirectoryService.getAttributeValues(PAGE_KEY,
                                                                                          SITE_KEY,
                                                                                          WINDOW_ID,
                                                                                          "team",
                                                                                          null,
                                                                                          USERNAME,
                                                                                          Locale.FRENCH);

    assertEquals(List.of(new PeopleDirectoryAttributeValue(String.valueOf(OPTION_ID), "Ingénierie")), values);
    verify(profileSearchConnector, never()).getAttributeValues(anyString(), anyBoolean(), any(), anyInt());
  }

  @Test
  public void getEditorAttributesFlagsTheAttributesNoLongerAvailable() {
    settings.getFirst().setVisible(false);
    lenient().when(translationService.getTranslationLabelOrDefault("propertySettingOption",
                                                                   OPTION_ID,
                                                                   "optionValue",
                                                                   Locale.ENGLISH)).thenReturn("Engineering");

    List<PeopleDirectoryAttribute> attributes =
                                              peopleDirectoryService.getEditorAttributes("{\"department\":[\"HR\"],\"team\":[\""
                                                  + OPTION_ID + "\"],\"skills.lang\":[\"a\",null]}", Locale.ENGLISH);

    assertEquals(3, attributes.size());
    assertEquals(new PeopleDirectoryAttribute("department", List.of(new PeopleDirectoryAttributeValue("HR", "HR")), false),
                 attributes.get(0));
    assertEquals(new PeopleDirectoryAttribute("team",
                                              List.of(new PeopleDirectoryAttributeValue(String.valueOf(OPTION_ID), "Engineering")),
                                              true),
                 attributes.get(1));
    // an invalid stored shape shows no value: the drawer asks for one
    assertTrue(attributes.get(2).available());
    assertTrue(attributes.get(2).values().isEmpty());
    assertTrue(peopleDirectoryService.getEditorAttributes("not json", Locale.ENGLISH).isEmpty());
  }

  @Test
  public void getEditorAttributesKeepsAnAvailableAttributeWhoseValuesTheDirectoryRefuses() {
    String twentyOneValues = IntStream.range(0, 21)
                                      .mapToObj(i -> "\"v" + i + "\"")
                                      .collect(Collectors.joining(",", "[", "]"));
    String stored = "{\"department\":" + twentyOneValues + "}";

    List<PeopleDirectoryAttribute> attributes = peopleDirectoryService.getEditorAttributes(stored, Locale.ENGLISH);

    // the attribute is available: the drawer flags its values, the directory lists nobody
    assertTrue(attributes.getFirst().available());
    assertEquals(21, attributes.getFirst().values().size());
    assertNull(peopleDirectoryService.getAttributeFilter(stored));
  }

  private PeopleDirectoryPage search(String storedFilter,
                                     Map<String, String> viewerAttributes,
                                     boolean connectionsOnly,
                                     int limit) throws IllegalAccessException {
    return search(storedFilter, null, viewerAttributes, connectionsOnly, limit);
  }

  private PeopleDirectoryPage search(String storedFilter,
                                     String storedGuestRestriction,
                                     Map<String, String> viewerAttributes,
                                     boolean connectionsOnly,
                                     int limit) throws IllegalAccessException {
    return peopleDirectoryService.search(PAGE_KEY,
                                         SITE_KEY,
                                         WINDOW_ID,
                                         storedFilter,
                                         null,
                                         storedGuestRestriction,
                                         " john d ",
                                         viewerAttributes,
                                         connectionsOnly,
                                         USERNAME,
                                         0,
                                         limit);
  }

  private void mockOptionLabels() throws Exception {
    TranslationField translationField = mock(TranslationField.class);
    when(translationField.getLabels()).thenReturn(Map.of(Locale.FRENCH, "Ingenierie"));
    when(translationService.getTranslationField("propertySettingOption", OPTION_ID, "optionValue")).thenReturn(translationField);
  }

  private static ProfilePropertySetting setting(String name, boolean multiValued, boolean dropdown) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId((long) Math.abs(name.hashCode()));
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setVisible(true);
    setting.setActive(true);
    setting.setMultiValued(multiValued);
    setting.setDropdownList(dropdown);
    if (dropdown) {
      ProfilePropertyOption option = new ProfilePropertyOption();
      option.setId(OPTION_ID);
      option.setValue("Engineering");
      setting.setPropertyOptions(List.of(option));
    }
    return setting;
  }

  private Page pageHolding(String windowId) {
    Page holdingPage = new Page();
    holdingPage.setChildren(new ArrayList<ModelObject>(List.of(containerHolding(windowId))));
    return holdingPage;
  }

  private PortalConfig siteHolding(String windowId) {
    PortalConfig site = new PortalConfig();
    site.getPortalLayout().setChildren(new ArrayList<ModelObject>(List.of(containerHolding(windowId))));
    return site;
  }

  private Container containerHolding(String windowId) {
    Application application = new Application(windowId);
    Container container = new Container();
    container.setChildren(new ArrayList<ModelObject>(List.of(application)));
    return container;
  }
}
