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
package org.exoplatform.social.core.jpa.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.exoplatform.commons.search.es.client.ElasticIndexingAuditTrail;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.relationship.model.Relationship;

import io.meeds.social.identity.permission.service.UserPermissionService;
import io.meeds.social.people.model.PeopleDirectoryFilter;
import io.meeds.social.translation.service.TranslationService;

/**
 * The People Directory clauses and the attribute values aggregation,
 * evaluated by Elasticsearch itself, on documents built by
 * {@link ProfileIndexingServiceConnector}: exact matching, regexp
 * metacharacters and reserved query characters matched literally, a dotted
 * property, owner-hidden values absent from both, and the group clause on the
 * indexed membership tokens.
 * <p>
 * Opt-in, as {@link ProfileSearchConnectorElasticsearchIT}:
 * {@code mvn -pl component/core -Prun-its verify -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false -Dit.test=ProfileDirectoryFilterElasticsearchIT -Dfailsafe.failIfNoSpecifiedTests=false};
 * it creates an index of its own and deletes it.
 */
public class ProfileDirectoryFilterElasticsearchIT {

  private static final String           ES_URL      = System.getProperty("exo.es.search.server.url", "http://127.0.0.1:9200");

  private static final String           INDEX       = "profile_directory_it_" + System.currentTimeMillis();

  private static final HttpClient       HTTP        = HttpClient.newHttpClient();

  private static final String           DEPARTMENT  = "department";

  private static final String           LANGUAGES   = "skills.lang";

  private static final long             DEPARTMENT_ID = 1L;

  private static final long             POSITION_ID = 3L;

  private static final String           CPP_USER    = "2001";

  private static final String           RD_USER     = "2002";

  private static final String           DOT_USER    = "2003";

  private static final String           NOT_DOT_USER = "2004";

  private static final String           HR_USER     = "2005";

  private static final String           HR_PARTNER_USER = "2006";

  /** Hid their department and their position */
  private static final String           HIDDEN_USER = "2007";

  private static final String           QUOTE_USER  = "2008";

  /** Holds a plain accented value stored before the property became multi-valued */
  private static final String           PLAIN_MULTI_USER = "2009";

  /** Holds the multi-valued value 'Paris, Berlin', which the index joins as two */
  private static final String           COMMA_MULTI_USER = "2010";

  /** A guest, member of {@link #LAB_SPACE} */
  private static final String           GUEST_USER  = "2020";

  /** A manager of the guest's space */
  private static final String           SPACE_MATE_USER = "2021";

  /** A confirmed connection of the guest, sharing no group with them */
  private static final String           CONNECTED_USER = "2022";

  /** Holds only a pending connection request with the guest */
  private static final String           PENDING_USER = "2023";

  /** A member of the guest's space through a nested-group link */
  private static final String           NESTED_MATE_USER = "2024";

  /** Shares only an organizational unit with the guest */
  private static final String           ORG_MATE_USER = "2025";

  /** A member of a sub-group of the guest's space only */
  private static final String           SUB_MATE_USER = "2026";

  private static final String           HR_GROUP    = "/organization/hr";

  private static final String           PARIS_GROUP = "/spaces/paris";

  private static final String           LAB_SPACE   = "/spaces/lab";

  private static final String           LAB_UNIT    = "/organization/lab";

  private static ProfileSearchConnector searchConnector;

  @BeforeClass
  // the connector reads identities through the deprecated
  // IdentityManager#getIdentity(String), which the test stubs as such
  @SuppressWarnings("removal")
  public static void indexProfiles() throws Exception {
    List<ProfilePropertySetting> settings = List.of(setting(DEPARTMENT_ID, DEPARTMENT, false),
                                                    setting(2L, LANGUAGES, true),
                                                    setting(POSITION_ID, "position", false));
    ProfilePropertyService profilePropertyService = mock(ProfilePropertyService.class);
    when(profilePropertyService.getPropertySettings()).thenReturn(settings);
    when(profilePropertyService.getHiddenProfilePropertyIds(anyLong())).thenReturn(new ArrayList<>());
    when(profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(HIDDEN_USER))).thenReturn(new ArrayList<>(List.of(DEPARTMENT_ID,
                                                                                                                               POSITION_ID)));
    IdentityManager identityManager = mock(IdentityManager.class);
    UserPermissionService userPermissionService = mock(UserPermissionService.class);
    when(userPermissionService.getPermissionTokens(anyString())).thenReturn(Collections.emptyList());
    when(userPermissionService.getMembershipTypes()).thenReturn(List.of("member", "manager"));
    // a member of HR, a manager of its sub-group, a member through a nested
    // link, a member of a group whose id starts with HR's, a member of Paris
    when(userPermissionService.getPermissionTokens("directory" + HR_USER)).thenReturn(List.of("member:" + HR_GROUP));
    when(userPermissionService.getPermissionTokens("directory" + RD_USER)).thenReturn(List.of("manager:" + HR_GROUP + "/payroll"));
    when(userPermissionService.getPermissionTokens("directory" + DOT_USER))
      .thenReturn(List.of(UserPermissionService.INHERITED_TOKEN_PREFIX + "member:" + HR_GROUP));
    when(userPermissionService.getPermissionTokens("directory" + NOT_DOT_USER)).thenReturn(List.of("member:" + HR_GROUP + "x"));
    when(userPermissionService.getPermissionTokens("directory" + CPP_USER)).thenReturn(List.of("*:" + PARIS_GROUP));
    // the guest restriction's population
    when(userPermissionService.getPermissionTokens("directory" + GUEST_USER)).thenReturn(List.of("member:/platform/externals",
                                                                                                 "member:" + LAB_SPACE,
                                                                                                 "member:" + LAB_UNIT));
    when(userPermissionService.getPermissionTokens("directory" + SPACE_MATE_USER)).thenReturn(List.of("manager:" + LAB_SPACE));
    when(userPermissionService.getPermissionTokens("directory" + NESTED_MATE_USER))
      .thenReturn(List.of(UserPermissionService.INHERITED_TOKEN_PREFIX + "member:" + LAB_SPACE));
    when(userPermissionService.getPermissionTokens("directory" + CONNECTED_USER)).thenReturn(List.of("member:/platform/users"));
    when(userPermissionService.getPermissionTokens("directory" + PENDING_USER)).thenReturn(List.of("member:/platform/users"));
    when(userPermissionService.getPermissionTokens("directory" + ORG_MATE_USER)).thenReturn(List.of("member:" + LAB_UNIT));
    when(userPermissionService.getPermissionTokens("directory" + SUB_MATE_USER)).thenReturn(List.of("member:" + LAB_SPACE + "/sub"));
    // the connections the index stores on each side: a confirmed one with the
    // guest, and a pending one, which the index does not store
    ConnectionDAO connectionDAO = mock(ConnectionDAO.class);
    long guestId = Long.parseLong(GUEST_USER);
    when(connectionDAO.getSenderIds(guestId, Relationship.Type.CONFIRMED, 0, 200)).thenReturn(List.of(Long.parseLong(CONNECTED_USER)));
    when(connectionDAO.getReceiverIds(Long.parseLong(CONNECTED_USER), Relationship.Type.CONFIRMED, 0, 200)).thenReturn(List.of(guestId));
    when(connectionDAO.getSenderIds(guestId, Relationship.Type.PENDING, 0, 200)).thenReturn(List.of(Long.parseLong(PENDING_USER)));
    when(connectionDAO.getReceiverIds(Long.parseLong(PENDING_USER), Relationship.Type.PENDING, 0, 200)).thenReturn(List.of(guestId));

    Map<String, Identity> identities = new LinkedHashMap<>();
    identities.put(CPP_USER, identity(CPP_USER, "C++", List.of("a", "b"), null));
    identities.put(RD_USER, identity(RD_USER, "R&D", List.of("b", "c"), null));
    identities.put(DOT_USER, identity(DOT_USER, "a.b", List.of("(x)"), null));
    identities.put(NOT_DOT_USER, identity(NOT_DOT_USER, "aXb", List.of("x"), null));
    identities.put(HR_USER, identity(HR_USER, "HR", List.of(), "Engineer"));
    identities.put(HR_PARTNER_USER, identity(HR_PARTNER_USER, "HR Business Partner", List.of(), null));
    identities.put(HIDDEN_USER, identity(HIDDEN_USER, "HR", List.of(), "Engineer"));
    identities.put(QUOTE_USER, identity(QUOTE_USER, "say \"hi\", Paris", List.of(), null));
    Identity plainMulti = identity(PLAIN_MULTI_USER, "Sales", List.of(), null);
    plainMulti.getProfile().setProperty(LANGUAGES, "Développement");
    identities.put(PLAIN_MULTI_USER, plainMulti);
    identities.put(COMMA_MULTI_USER, identity(COMMA_MULTI_USER, "Sales", List.of("Paris, Berlin"), null));
    for (String id : List.of(GUEST_USER,
                             SPACE_MATE_USER,
                             CONNECTED_USER,
                             PENDING_USER,
                             NESTED_MATE_USER,
                             ORG_MATE_USER,
                             SUB_MATE_USER)) {
      identities.put(id, identity(id, "Sales", List.of(), null));
    }
    identities.forEach((id, identity) -> when(identityManager.getIdentity(id)).thenReturn(identity));

    InitParams indexingParams = new InitParams();
    PropertiesParam indexingProperties = new PropertiesParam();
    indexingProperties.setName("constructor.params");
    indexingProperties.setProperty("index_alias", INDEX);
    indexingProperties.setProperty("index_current", INDEX);
    indexingParams.addParam(indexingProperties);
    ProfileIndexingServiceConnector indexingConnector = new ProfileIndexingServiceConnector(indexingParams,
                                                                                            identityManager,
                                                                                            mock(IdentityDAO.class),
                                                                                            connectionDAO,
                                                                                            profilePropertyService,
                                                                                            mock(TranslationService.class),
                                                                                            mock(UserACL.class),
                                                                                            userPermissionService);

    send("PUT", "/" + INDEX, "{\"settings\": " + indexSettings() + ", \"mappings\": " + indexingConnector.getMapping() + "}");
    for (String id : identities.keySet()) {
      send("PUT", "/" + INDEX + "/_doc/" + id + "?refresh=wait_for", indexingConnector.create(id).toJSON());
    }

    InitParams searchParams = new InitParams();
    PropertiesParam searchProperties = new PropertiesParam();
    searchProperties.setName("constructor.params");
    searchProperties.setProperty("index", INDEX);
    searchParams.addParam(searchProperties);
    searchConnector = new ProfileSearchConnector(searchParams,
                                                 new ElasticSearchingClient(new ElasticIndexingAuditTrail()),
                                                 profilePropertyService,
                                                 userPermissionService);
  }

  @AfterClass
  public static void deleteIndex() throws Exception {
    send("DELETE", "/" + INDEX, null);
  }

  @Test
  public void testSingleValuedAttributeIsMatchedExactly() {
    assertListed(Map.of(DEPARTMENT, List.of("C++")), CPP_USER);
    assertListed(Map.of(DEPARTMENT, List.of("R&D")), RD_USER);
    assertListed(Map.of(DEPARTMENT, List.of("a.b")), DOT_USER);
    assertListed(Map.of(DEPARTMENT, List.of("say \"hi\", Paris")), QUOTE_USER);
    // neither 'HR Business Partner' nor the user who hid their department
    assertListed(Map.of(DEPARTMENT, List.of("HR")), HR_USER);
    assertListed(Map.of(DEPARTMENT, List.of("hr")));
  }

  @Test
  public void testValuesOfOneAttributeAreAlternatives() {
    assertListed(Map.of(DEPARTMENT, List.of("C++", "R&D")), CPP_USER, RD_USER);
  }

  @Test
  public void testMultiValuedDottedAttributeMatchesOneOfTheValues() {
    assertListed(Map.of(LANGUAGES, List.of("b")), CPP_USER, RD_USER);
    assertListed(Map.of(LANGUAGES, List.of("c")), RD_USER);
    assertListed(Map.of(LANGUAGES, List.of("(x)")), DOT_USER);
    assertListed(Map.of(LANGUAGES, List.of(".*")));
    // the string the directory computes for a multi-valued property's value
    assertListed(Map.of(LANGUAGES, List.of("Developpement")), PLAIN_MULTI_USER);
  }

  @Test
  public void testAttributesMustAllBeMatched() {
    Map<String, List<String>> attributes = new LinkedHashMap<>();
    attributes.put(DEPARTMENT, List.of("C++", "R&D"));
    attributes.put(LANGUAGES, List.of("c"));
    assertListed(attributes, RD_USER);
  }

  @Test
  public void testHiddenPositionIsNotFoundByAHiddenSearch() {
    ProfileFilter filter = new ProfileFilter();
    filter.setProfileSettings(Map.of("position", "hidden"));
    assertTrue(searchConnector.search(null, filter, null, 0, 20).isEmpty());
    filter.setProfileSettings(Map.of(DEPARTMENT, "hidden"));
    assertTrue(searchConnector.search(null, filter, null, 0, 20).isEmpty());
    filter.setProfileSettings(Map.of("position", "Engineer"));
    assertEquals(List.of(HR_USER), searchConnector.search(null, filter, null, 0, 20));
  }

  @Test
  public void testSingleValuedAttributeValues() {
    List<String> values = searchConnector.getAttributeValues(DEPARTMENT, false, null, 50);
    assertEquals(List.of("C++", "HR", "HR Business Partner", "R&D", "Sales", "a.b", "aXb", "say \"hi\", Paris").stream().sorted().toList(),
                 values.stream().sorted().toList());
    assertFalse(values.contains("hidden"));
    assertEquals(List.of("C++"), searchConnector.getAttributeValues(DEPARTMENT, false, "C++", 50));
    assertEquals(List.of("a.b"), searchConnector.getAttributeValues(DEPARTMENT, false, "a.b", 50));
    assertEquals(List.of("R&D"), searchConnector.getAttributeValues(DEPARTMENT, false, "R&", 50));
    assertEquals(List.of("HR", "HR Business Partner"),
                 searchConnector.getAttributeValues(DEPARTMENT, false, "hr", 50).stream().sorted().toList());
    assertEquals(List.of("say \"hi\", Paris"), searchConnector.getAttributeValues(DEPARTMENT, false, "PARIS", 50));
  }

  @Test
  public void testMultiValuedAttributeValuesAreSplit() {
    List<String> values = searchConnector.getAttributeValues(LANGUAGES, true, null, 50);
    assertEquals(List.of("(x)", "Berlin", "Developpement", "Paris", "a", "b", "c", "x"), values.stream().sorted().toList());
    assertEquals(List.of("c"), searchConnector.getAttributeValues(LANGUAGES, true, "c", 50));
    assertEquals(List.of("(x)"), searchConnector.getAttributeValues(LANGUAGES, true, "(x)", 50));
  }

  /**
   * Every piece the picker offers for a value holding a comma lists its
   * holder, the one after the comma and its space included.
   */
  @Test
  public void testEveryPieceOfferedForACommaValueListsItsHolder() {
    assertEquals(List.of("Berlin"), searchConnector.getAttributeValues(LANGUAGES, true, "Berl", 50));
    assertListed(Map.of(LANGUAGES, List.of("Berlin")), COMMA_MULTI_USER);
    assertListed(Map.of(LANGUAGES, List.of("Paris")), COMMA_MULTI_USER);
    assertListed(Map.of(LANGUAGES, List.of("Paris, Berlin")), COMMA_MULTI_USER);
    assertListed(Map.of(LANGUAGES, List.of("erlin")));
  }

  @Test
  public void testGroupListsItsMembersTheMembersOfItsSubGroupsAndOfItsNestedGroups() {
    // not the member of /organization/hrx
    assertListed(Map.of(), List.of(HR_GROUP), HR_USER, RD_USER, DOT_USER);
    assertListed(Map.of(), List.of(HR_GROUP + "/payroll"), RD_USER);
    assertListed(Map.of(), List.of(HR_GROUP + "x"), NOT_DOT_USER);
  }

  @Test
  public void testGroupsAreAlternatives() {
    assertListed(Map.of(), List.of(HR_GROUP + "/payroll", PARIS_GROUP), RD_USER, CPP_USER);
  }

  @Test
  public void testGroupsAndAttributesMustBothBeMatched() {
    assertListed(Map.of(DEPARTMENT, List.of("HR", "C++")), List.of(HR_GROUP), HR_USER);
  }

  @Test
  public void testNoLiveGroupListsNobody() {
    assertListed(Map.of(), List.of());
  }

  /**
   * A guest sees themself, their confirmed connections and the members of
   * their spaces through any membership type, directly or through a
   * nested-group link; never a pending connection, a user sharing only an
   * organizational unit, nor a member of a sub-group of their space only.
   */
  @Test
  public void testGuestSeesThemselfTheirConnectionsAndTheirSpacesMembers() {
    assertListedToGuest(null, List.of(LAB_SPACE), GUEST_USER, SPACE_MATE_USER, NESTED_MATE_USER, CONNECTED_USER);
  }

  @Test
  public void testGuestSeesTheMembersOfTheirSpaceUnderAnyMembershipType() {
    // CPP_USER holds the star membership of Paris
    assertListedToGuest(null, List.of(PARIS_GROUP), GUEST_USER, CONNECTED_USER, CPP_USER);
  }

  @Test
  public void testGuestInNoSpaceSeesThemselfAndTheirConnectionsOnly() {
    assertListedToGuest(null, List.of(), GUEST_USER, CONNECTED_USER);
  }

  /**
   * The restriction is ANDed with the directory's group filter, whose
   * sub-groups it never widens.
   */
  @Test
  public void testGuestRestrictionIsANDedWithTheDirectoryGroupFilter() {
    // the directory lists the space and its sub-group; the guest only the
    // space's members
    assertListedToGuest(List.of(LAB_SPACE), List.of(LAB_SPACE), GUEST_USER, SPACE_MATE_USER, NESTED_MATE_USER);
    // Paris's member is no member of the guest's space
    assertListedToGuest(List.of(PARIS_GROUP), List.of(LAB_SPACE));
  }

  private static void assertListedToGuest(List<String> directoryGroups, List<String> spaceGroups, String... expectedIds) {
    ProfileFilter filter = new ProfileFilter();
    if (directoryGroups != null) {
      PeopleDirectoryFilter directoryFilter = new PeopleDirectoryFilter(Map.of());
      directoryFilter.setGroups(directoryGroups);
      filter.setDirectoryFilter(directoryFilter);
    }
    filter.setViewerSpaceGroupIds(spaceGroups);
    filter.setGuestIdentityId(Long.parseLong(GUEST_USER));
    List<String> listed = searchConnector.search(null, filter, null, 0, 20);
    assertEquals(directoryGroups + " " + spaceGroups,
                 List.of(expectedIds).stream().sorted().toList(),
                 listed.stream().sorted().toList());
    assertEquals(expectedIds.length, searchConnector.count(null, filter, null));
  }

  private static void assertListed(Map<String, List<String>> attributes, String... expectedIds) {
    assertListed(attributes, null, expectedIds);
  }

  private static void assertListed(Map<String, List<String>> attributes, List<String> groups, String... expectedIds) {
    ProfileFilter filter = new ProfileFilter();
    PeopleDirectoryFilter directoryFilter = new PeopleDirectoryFilter(attributes);
    directoryFilter.setGroups(groups);
    filter.setDirectoryFilter(directoryFilter);
    List<String> listed = searchConnector.search(null, filter, null, 0, 20);
    assertEquals(attributes + " " + groups,
                 List.of(expectedIds).stream().sorted().toList(),
                 listed.stream().sorted().toList());
    assertEquals(expectedIds.length, searchConnector.count(null, filter, null));
  }

  private static ProfilePropertySetting setting(long id, String name, boolean multiValued) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId(id);
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setVisible(true);
    setting.setActive(true);
    setting.setEditable(true);
    setting.setMultiValued(multiValued);
    return setting;
  }

  private static Identity identity(String id, String department, List<String> languages, String position) {
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, "directory" + id);
    identity.setId(id);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "Directory");
    profile.setProperty(Profile.LAST_NAME, id);
    profile.setProperty(Profile.EMAIL, "directory" + id + "@example.com");
    profile.setProperty(DEPARTMENT, department);
    if (position != null) {
      profile.setProperty(Profile.POSITION, position);
    }
    if (!languages.isEmpty()) {
      List<Map<String, String>> values = new ArrayList<>();
      for (String language : languages) {
        Map<String, String> value = new HashMap<>();
        value.put("value", language);
        values.add(value);
      }
      profile.setProperty(LANGUAGES, values);
    }
    identity.setProfile(profile);
    return identity;
  }

  private static String indexSettings() throws IOException {
    try (InputStream settings = ElasticSearchingClient.class.getClassLoader()
                                                            .getResourceAsStream("es-default-index-settings.json")) {
      String json = new String(settings.readAllBytes(), StandardCharsets.UTF_8);
      return json.replace("shard.number", "1").replace("replica.number", "0").replace("max_regex.length", "1000");
    }
  }

  private static String send(String method, String path, String body) throws IOException, InterruptedException {
    HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(ES_URL + path)).header("Content-Type", "application/json");
    request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
    HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    assertTrue(method + " " + path + " answered " + response.statusCode() + ": " + response.body(),
               response.statusCode() < 300);
    return response.body();
  }
}
