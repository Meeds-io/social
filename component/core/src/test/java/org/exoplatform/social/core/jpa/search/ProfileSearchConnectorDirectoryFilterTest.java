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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.social.identity.permission.service.UserPermissionService;
import io.meeds.social.people.model.PeopleDirectoryFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The People Directory clauses and the attribute values aggregation, read
 * back with a strict JSON parser.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileSearchConnectorDirectoryFilterTest {

  private static final String    INDEX       = "profile_alias";

  private static final String    NO_HITS     = "{\"hits\":{\"total\":{\"value\":0},\"hits\":[]}}";

  private static final JsonMapper JSON_MAPPER = new JsonMapper();

  @Mock
  private ElasticSearchingClient client;

  @Mock
  private ProfilePropertyService profilePropertyService;

  @Mock
  private UserPermissionService  userPermissionService;

  private ProfileSearchConnector connector;

  private List<ProfilePropertySetting> settings;

  @Before
  public void setUp() {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("index", INDEX);
    params.addParam(constructorParams);
    connector = new ProfileSearchConnector(params, client, profilePropertyService, userPermissionService);
    settings = new ArrayList<>();
    settings.add(setting("department", false));
    settings.add(setting("skills.lang", true));
  }

  @Test
  public void testSingleValuedAttributeIsATermPerValueOnTheRawField() {
    JsonNode clauses = searchWithDirectory(Map.of("department", List.of("HR", "R&D \"quoted\" \\ back")));

    assertEquals(1, clauses.size());
    JsonNode bool = clauses.get(0).get("bool");
    assertEquals(1, bool.get("minimum_should_match").asInt());
    JsonNode should = bool.get("should");
    assertEquals(2, should.size());
    assertEquals("HR", should.get(0).get("term").get("department.raw").asString());
    assertEquals("R&D \"quoted\" \\ back", should.get(1).get("term").get("department.raw").asString());
    assertFalse(should.get(0).has("regexp"));
  }

  @Test
  public void testMultiValuedDottedAttributeIsAnEscapedRegexpOnTheIndexedField() {
    JsonNode clauses = searchWithDirectory(Map.of("skills.lang", List.of("C++", "a.b")));

    JsonNode should = clauses.get(0).get("bool").get("should");
    JsonNode regexp = should.get(0).get("regexp").get("skills_lang.raw");
    assertEquals("(.*,)? *C\\+\\+ *(,.*)?", regexp.get("value").asString());
    assertEquals("NONE", regexp.get("flags").asString());
    assertEquals("(.*,)? *a\\.b *(,.*)?", should.get(1).get("regexp").get("skills_lang.raw").get("value").asString());
    assertFalse(should.get(0).has("term"));
  }

  @Test
  public void testEachAttributeIsItsOwnClause() {
    Map<String, List<String>> attributes = new LinkedHashMap<>();
    attributes.put("department", List.of("HR"));
    attributes.put("skills.lang", List.of("Java"));

    JsonNode clauses = searchWithDirectory(attributes);

    assertEquals(2, clauses.size());
    assertTrue(clauses.get(0).get("bool").get("should").get(0).has("term"));
    assertTrue(clauses.get(1).get("bool").get("should").get(0).has("regexp"));
  }

  @Test
  public void testAttributeNoLongerAvailableMatchesNobody() {
    settings.add(setting("city", false));
    settings.getLast().setVisible(false);
    settings.add(setting("country", false));
    settings.getLast().setActive(false);
    settings.add(setting("street", false));
    settings.getLast().setParentId(5L);

    for (String attribute : List.of("city", "country", "street", "unknown")) {
      JsonNode clauses = searchWithDirectory(Map.of(attribute, List.of("x")));
      assertEquals(attribute, 1, clauses.size());
      assertTrue(attribute, clauses.get(0).has("match_none"));
    }
  }

  @Test
  public void testAttributeWithoutValueMatchesNobody() {
    JsonNode clauses = searchWithDirectory(Map.of("department", List.of()));

    assertTrue(clauses.get(0).has("match_none"));
  }

  @Test
  public void testNoDirectoryFilterAddsNoClause() {
    when(client.sendRequest(anyString(), anyString())).thenReturn(NO_HITS);
    ProfileFilter filter = new ProfileFilter();
    filter.setName("john");

    connector.search(null, filter, null, 0, 10);

    JsonNode query = capturedQuery();
    JsonNode filterClauses = query.get("query").get("constant_score").get("filter").get("bool").get("filter");
    assertEquals(1, filterClauses.size());
    assertTrue(filterClauses.get(0).has("query_string"));
  }

  @Test
  public void testViewerFilterOnANonVisibleSettingMatchesNobody() {
    settings.add(setting("city", false));
    settings.getLast().setVisible(false);
    when(profilePropertyService.getPropertySettings()).thenReturn(settings);
    when(client.sendRequest(anyString(), anyString())).thenReturn(NO_HITS);
    ProfileFilter filter = new ProfileFilter();
    filter.setProfileSettings(Map.of("city", "Tunis"));

    connector.search(null, filter, null, 0, 10);

    JsonNode query = capturedQuery();
    // without the clause the query matches everyone
    assertTrue(query.toString(), query.has("query"));
    JsonNode filterClauses = query.get("query").get("constant_score").get("filter").get("bool").get("filter");
    assertEquals(1, filterClauses.size());
    assertTrue(filterClauses.toString(), filterClauses.get(0).has("match_none"));
  }

  @Test
  public void testViewerFilterSkipsAnUnknownSettingWithoutLookingItUpByName() {
    settings.add(setting("city", false));
    settings.getLast().setVisible(false);
    when(profilePropertyService.getPropertySettings()).thenReturn(settings);
    when(client.sendRequest(anyString(), anyString())).thenReturn(NO_HITS);
    ProfileFilter filter = new ProfileFilter();
    filter.setName("john");
    Map<String, String> viewerSettings = new LinkedHashMap<>();
    viewerSettings.put("city", "Tunis");
    viewerSettings.put("unknown", "x");
    viewerSettings.put("department", "HR");
    filter.setProfileSettings(viewerSettings);

    connector.search(null, filter, null, 0, 10);

    String query = capturedQuery().toString();
    assertTrue(query, query.contains("department.whitespace"));
    assertTrue(query, query.contains("match_none"));
    assertFalse(query, query.contains("city"));
    assertFalse(query, query.contains("unknown"));
    verify(profilePropertyService, never()).getProfileSettingByName(anyString());
  }

  @Test
  public void testDirectoryFilterIsANonEmptyFilter() {
    ProfileFilter filter = new ProfileFilter();
    assertTrue(filter.isEmpty());
    filter.setDirectoryFilter(new PeopleDirectoryFilter());
    assertFalse(filter.isEmpty());
  }

  @Test
  public void testSingleValuedAttributeValuesQuery() {
    JsonNode terms = JSON_MAPPER.readTree(connector.buildAttributeValuesQuery("skills.lang", false, "C++", 50))
                                .get("aggs")
                                .get("values")
                                .get("terms");

    assertEquals("skills_lang.raw", terms.get("field").asString());
    assertEquals(50, terms.get("size").asInt());
    assertEquals(".*[cC]\\+\\+.*", terms.get("include").asString());
    assertEquals("hidden", terms.get("exclude").get(0).asString());
    assertEquals(1, terms.get("exclude").size());
  }

  @Test
  public void testAttributeValuesQueryWithoutTextHasNoInclude() {
    JsonNode terms = JSON_MAPPER.readTree(connector.buildAttributeValuesQuery("department", true, null, 50))
                                .get("aggs")
                                .get("values")
                                .get("terms");

    assertFalse(terms.has("include"));
    assertEquals(ProfileSearchConnector.MULTI_VALUED_AGGREGATION_SIZE, terms.get("size").asInt());
  }

  @Test
  public void testMultiValuedAttributeValuesAreSplitDeduplicatedAndNarrowed() {
    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"aggregations":{"values":{"buckets":[{"key":"a,b","doc_count":3},{"key":"b,c","doc_count":2},{"key":"c","doc_count":1}]}}}
        """);

    assertEquals(List.of("a", "b", "c"), connector.getAttributeValues("skills.lang", true, null, 50));
    assertEquals(List.of("c"), connector.getAttributeValues("skills.lang", true, "c", 50));
    assertEquals(List.of("a", "b"), connector.getAttributeValues("skills.lang", true, null, 2));
  }

  @Test
  public void testAttributeValuesAreNarrowedIgnoringCase() {
    JsonNode terms = JSON_MAPPER.readTree(connector.buildAttributeValuesQuery("department", false, "hr", 50))
                                .get("aggs")
                                .get("values")
                                .get("terms");
    String include = terms.get("include").asString();

    assertEquals(".*[hH][rR].*", include);
    // java.util.regex reads this subset (classes, escapes, .*) as Lucene does
    Pattern pattern = Pattern.compile(include);
    assertTrue(pattern.matcher("HR Business Partner").matches());
    assertTrue(pattern.matcher("hr").matches());
    assertFalse(pattern.matcher("Sales").matches());

    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"aggregations":{"values":{"buckets":[{"key":"HR,Sales","doc_count":2},{"key":"Finance","doc_count":1}]}}}
        """);
    assertEquals(List.of("HR"), connector.getAttributeValues("skills.lang", true, "hr", 50));
  }

  @Test
  public void testToCaseInsensitiveRegexpEscapesWhatIsNotACasedLetter() {
    assertEquals("[rR]\\&[dD] 1", ProfileSearchConnector.toCaseInsensitiveRegexp("R&D 1"));
    assertEquals("[éÉ]\\.", ProfileSearchConnector.toCaseInsensitiveRegexp("é."));
  }

  /**
   * Known limit pinned: the index joins a multi-valued property's values with
   * commas, so a value holding a comma is listed, and matched, as its pieces.
   * Every piece the picker offers is one the directory clause matches, the
   * one after a comma and a space included.
   */
  @Test
  public void testMultiValuedValueHoldingACommaIsReadAsItsPieces() {
    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"aggregations":{"values":{"buckets":[{"key":"Paris, France,HR","doc_count":1}]}}}
        """);

    List<String> pieces = connector.getAttributeValues("skills.lang", true, null, 50);
    assertEquals(List.of("Paris", "France", "HR"), pieces);
    for (String piece : pieces) {
      String regexp = searchWithDirectory(Map.of("skills.lang", List.of(piece))).get(0)
                                                                                 .get("bool")
                                                                                 .get("should")
                                                                                 .get(0)
                                                                                 .get("regexp")
                                                                                 .get("skills_lang.raw")
                                                                                 .get("value")
                                                                                 .asString();
      assertTrue(piece, Pattern.compile(regexp).matcher("Paris, France,HR").matches());
    }
  }

  /**
   * The picker strips the spaces the directory clause skips, and only them: a
   * piece keeping another white space is offered as it is held.
   */
  @Test
  public void testMultiValuedPiecesAreStrippedOfSpacesOnly() {
    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"aggregations":{"values":{"buckets":[{"key":" Paris ,\\tBerlin","doc_count":1}]}}}
        """);

    assertEquals(List.of("Paris", "\tBerlin"), connector.getAttributeValues("skills.lang", true, null, 50));
  }

  @Test
  public void testSingleValuedAttributeValuesAreTheBucketKeys() {
    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"aggregations":{"values":{"buckets":[{"key":"Paris, France","doc_count":3},{"key":"HR","doc_count":2}]}}}
        """);

    assertEquals(List.of("Paris, France", "HR"), connector.getAttributeValues("department", false, null, 50));
  }

  @Test
  public void testEscapeRegexpEscapesEveryOperator() {
    String reserved = ".?+*|{}[]()\"\\#@&<>~";
    StringBuilder expected = new StringBuilder();
    for (char c : reserved.toCharArray()) {
      expected.append('\\').append(c);
    }
    assertEquals(expected.toString(), ProfileSearchConnector.escapeRegexp(reserved));
    assertEquals("plain text, 1", ProfileSearchConnector.escapeRegexp("plain text, 1"));
  }

  @Test
  public void testGroupsAreOneClauseOfMembershipTermsAndSubGroupPrefixes() {
    when(userPermissionService.getMembershipTypes()).thenReturn(List.of("member", "manager"));

    JsonNode clauses = searchWithDirectory(Map.of(), List.of("/organization/hr", "/spaces/paris", "/organization/hr"));

    assertEquals(1, clauses.size());
    JsonNode bool = clauses.get(0).get("bool");
    assertEquals(1, bool.get("minimum_should_match").asInt());
    JsonNode should = bool.get("should");
    List<String> terms = new ArrayList<>();
    should.get(0).get("terms").get("permissions").forEach(token -> terms.add(token.asString()));
    List<String> prefixes = new ArrayList<>();
    for (int i = 1; i < should.size(); i++) {
      prefixes.add(should.get(i).get("prefix").get("permissions").asString());
    }
    // two groups, three membership types (the star always), direct and inherited
    assertEquals(12, terms.size());
    assertTrue(terms.containsAll(List.of("member:/organization/hr",
                                         "inherited:member:/organization/hr",
                                         "*:/organization/hr",
                                         "manager:/spaces/paris",
                                         "inherited:*:/spaces/paris")));
    assertEquals(12, prefixes.size());
    assertTrue(prefixes.containsAll(List.of("member:/organization/hr/",
                                            "inherited:manager:/organization/hr/",
                                            "*:/spaces/paris/",
                                            "inherited:*:/spaces/paris/")));
    // a sub-group prefix ends with its separator: /organization/hr does not
    // match /organization/hrx
    assertTrue(prefixes.toString(), prefixes.stream().allMatch(prefix -> prefix.endsWith("/")));
  }

  @Test
  public void testGroupsAndAttributesMustBothBeMatched() {
    when(userPermissionService.getMembershipTypes()).thenReturn(List.of("member"));

    JsonNode clauses = searchWithDirectory(Map.of("department", List.of("HR")), List.of("/organization/hr"));

    assertEquals(2, clauses.size());
    assertTrue(clauses.get(0).get("bool").get("should").get(0).has("terms"));
    assertTrue(clauses.get(1).get("bool").get("should").get(0).has("term"));
  }

  @Test
  public void testNoLiveGroupMatchesNobody() {
    JsonNode clauses = searchWithDirectory(Map.of(), List.of());

    assertEquals(1, clauses.size());
    assertTrue(clauses.get(0).has("match_none"));
    verify(userPermissionService, never()).getMembershipTypes();
  }

  @Test
  public void testNoGroupFilterAddsNoGroupClause() {
    JsonNode clauses = searchWithDirectory(Map.of("department", List.of("HR")), null);

    assertEquals(1, clauses.size());
    assertFalse(clauses.toString(), clauses.toString().contains("permissions"));
  }

  @Test
  public void testGroupIdIsWrittenAsAJsonValue() {
    when(userPermissionService.getMembershipTypes()).thenReturn(List.of("member"));
    String groupId = "/organization/a\"b\\c\"}]";

    JsonNode clauses = searchWithDirectory(Map.of(), List.of(groupId));

    JsonNode should = clauses.get(0).get("bool").get("should");
    assertEquals("member:" + groupId, should.get(0).get("terms").get("permissions").get(0).asString());
    assertEquals("member:" + groupId + "/", should.get(1).get("prefix").get("permissions").asString());
  }

  @Test
  public void testGuestRestrictionMatchesSpaceMembersConnectionsAndSelf() {
    when(userPermissionService.getMembershipTypes()).thenReturn(List.of("member", "manager"));

    JsonNode clauses = searchAsGuest(null, List.of("/spaces/paris", "/spaces/paris"), 42L);

    assertEquals(1, clauses.size());
    JsonNode bool = clauses.get(0).get("bool");
    assertEquals(1, bool.get("minimum_should_match").asInt());
    JsonNode should = bool.get("should");
    assertEquals(3, should.size());
    List<String> terms = new ArrayList<>();
    should.get(0).get("terms").get("permissions").forEach(token -> terms.add(token.asString()));
    // one space, three membership types (the star always), direct and inherited
    assertEquals(List.of("member:/spaces/paris",
                         "inherited:member:/spaces/paris",
                         "manager:/spaces/paris",
                         "inherited:manager:/spaces/paris",
                         "*:/spaces/paris",
                         "inherited:*:/spaces/paris"),
                 terms);
    assertEquals(42L, should.get(1).get("term").get("connections").asLong());
    assertEquals("42", should.get(2).get("ids").get("values").get(0).asString());
    // members of the space only, never of its sub-groups
    assertFalse(clauses.toString(), clauses.toString().contains("prefix"));
  }

  @Test
  public void testGuestInNoSpaceMatchesConnectionsAndSelfOnly() {
    JsonNode clauses = searchAsGuest(null, List.of(), 42L);

    assertEquals(1, clauses.size());
    JsonNode should = clauses.get(0).get("bool").get("should");
    assertEquals(2, should.size());
    assertTrue(should.get(0).has("term"));
    assertTrue(should.get(1).has("ids"));
    assertFalse(clauses.toString(), clauses.toString().contains("permissions"));
    assertFalse(clauses.toString(), clauses.toString().contains("match_none"));
    verify(userPermissionService, never()).getMembershipTypes();
  }

  @Test
  public void testGuestRestrictionIsANDedWithTheGroupFilterNeverMerged() {
    when(userPermissionService.getMembershipTypes()).thenReturn(List.of("member"));

    JsonNode clauses = searchAsGuest(List.of("/organization/hr"), List.of("/spaces/paris"), 42L);

    assertEquals(2, clauses.size());
    String groupClause = clauses.get(0).toString();
    String guestClause = clauses.get(1).toString();
    assertTrue(groupClause, groupClause.contains("member:/organization/hr"));
    assertFalse(groupClause, groupClause.contains("/spaces/paris"));
    assertTrue(guestClause, guestClause.contains("member:/spaces/paris"));
    assertFalse(guestClause, guestClause.contains("/organization/hr"));
    // both clauses share one read of the membership types, which is not cached
    verify(userPermissionService, times(1)).getMembershipTypes();
  }

  @Test
  public void testNoGuestAddsNoGuestRestriction() {
    JsonNode clauses = searchWithDirectory(Map.of("department", List.of("HR")), null);

    assertFalse(clauses.toString(), clauses.toString().contains("connections"));
    assertFalse(clauses.toString(), clauses.toString().contains("\"ids\""));
  }

  @Test
  public void testGuestRestrictionAloneMakesTheFilterNonEmpty() {
    ProfileFilter filter = new ProfileFilter();
    assertTrue(filter.isEmpty());
    filter.setGuestIdentityId(42L);
    filter.setViewerSpaceGroupIds(List.of());
    // an empty filter is served from the database, which applies no guest
    // restriction
    assertFalse(filter.isEmpty());
  }

  private JsonNode searchAsGuest(List<String> directoryGroups, List<String> spaceGroups, long guestIdentityId) {
    when(client.sendRequest(anyString(), anyString())).thenReturn(NO_HITS);
    ProfileFilter filter = new ProfileFilter();
    if (directoryGroups != null) {
      PeopleDirectoryFilter directoryFilter = new PeopleDirectoryFilter(Map.of());
      directoryFilter.setGroups(directoryGroups);
      filter.setDirectoryFilter(directoryFilter);
    }
    filter.setViewerSpaceGroupIds(spaceGroups);
    filter.setGuestIdentityId(guestIdentityId);

    connector.search(null, filter, null, 0, 10);

    return capturedQuery().get("query").get("constant_score").get("filter").get("bool").get("filter");
  }

  private JsonNode searchWithDirectory(Map<String, List<String>> attributes, List<String> groups) {
    lenient().when(profilePropertyService.getPropertySettings()).thenReturn(settings);
    when(client.sendRequest(anyString(), anyString())).thenReturn(NO_HITS);
    ProfileFilter filter = new ProfileFilter();
    PeopleDirectoryFilter directoryFilter = new PeopleDirectoryFilter(attributes);
    directoryFilter.setGroups(groups);
    filter.setDirectoryFilter(directoryFilter);

    connector.search(null, filter, null, 0, 10);

    return capturedQuery().get("query").get("constant_score").get("filter").get("bool").get("filter");
  }

  private JsonNode searchWithDirectory(Map<String, List<String>> attributes) {
    when(profilePropertyService.getPropertySettings()).thenReturn(settings);
    when(client.sendRequest(anyString(), anyString())).thenReturn(NO_HITS);
    ProfileFilter filter = new ProfileFilter();
    filter.setDirectoryFilter(new PeopleDirectoryFilter(attributes));

    connector.search(null, filter, null, 0, 10);

    return capturedQuery().get("query").get("constant_score").get("filter").get("bool").get("filter");
  }

  private JsonNode capturedQuery() {
    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client, atLeastOnce()).sendRequest(query.capture(), eq(INDEX));
    return JSON_MAPPER.readTree(query.getValue());
  }

  private static ProfilePropertySetting setting(String name, boolean multiValued) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId((long) name.hashCode());
    setting.setPropertyName(name);
    setting.setVisible(true);
    setting.setActive(true);
    setting.setMultiValued(multiValued);
    return setting;
  }
}
