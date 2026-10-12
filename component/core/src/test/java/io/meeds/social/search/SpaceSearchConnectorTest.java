/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.search;

import static io.meeds.social.search.SpaceSearchConnector.SEARCH_COUNT_FILE_PATH_PARAM;
import static io.meeds.social.search.SpaceSearchConnector.SEARCH_QUERY_FILE_PATH_PARAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.core.jpa.search.SpaceIndexingServiceConnector;

import io.meeds.social.search.model.SpaceSearchFilter;
import io.meeds.social.search.model.SpaceSearchResult;
import io.meeds.social.space.constant.SpaceMembershipStatus;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class SpaceSearchConnectorTest {

  private static final ObjectMapper OBJECT_MAPPER     = new ObjectMapper();

  private static final String MEMBER            = "member";

  private static final String PERMISSIONS_FIELD = "permissions";

  private static final String TERM              = "term";

  private static final String PHRASE            = "term1 term2";

  private static final long   ID                = 461l;

  private static final String SEARCH_RESULT     = String.format("""
        {
           "hits":{
              "hits":[
                 {
                    "_id":"%s",
                    "_score":2.6705298,
                    "highlight":{
                       "displayName":[
                          "<span class='searchMatchExcerpt'>Test</span> space"
                       ],
                       "description":[
                          "<span class='searchMatchExcerpt'>Test</span> space #TestTag"
                       ]
                    }
                 }
              ]
           }
        }
      """, ID);

  private static final String COUNT_RESULT      = """
              {
         "count":1,
         "_shards":{
            "total":10,
            "successful":10,
            "skipped":0,
            "failed":0
         }
      }
            """;

  private static final String USER_NAME         = "testuser";

  private static final long   USER_IDENTITY_ID  = 25;

  String                      index             = "_space";

  String                      searchPath        = "spaces-search-query.json";

  String                      countPath         = "spaces-count-query.json";

  @Mock
  ConfigurationManager        configurationManager;

  @Mock
  ElasticSearchingClient      client;

  @Mock
  InitParams                  initParams;

  @Mock
  PropertiesParam             param;

  SpaceSearchConnector        spaceSearchConnector;

  @Before
  @SneakyThrows
  @SuppressWarnings("resource")
  public void setup() {
    when(initParams.getPropertiesParam("constructor.params")).thenReturn(param);
    when(param.getProperty("index")).thenReturn(index);

    ValueParam valueParam = mock(ValueParam.class);
    when(initParams.getValueParam(SEARCH_QUERY_FILE_PATH_PARAM)).thenReturn(valueParam);
    when(valueParam.getValue()).thenReturn(searchPath);

    valueParam = mock(ValueParam.class);
    when(initParams.getValueParam(SEARCH_COUNT_FILE_PATH_PARAM)).thenReturn(valueParam);
    when(valueParam.getValue()).thenReturn(countPath);
    when(configurationManager.getInputStream(anyString())).thenAnswer(invocation -> getClass().getClassLoader()
                                                                                              .getResourceAsStream(invocation.getArgument(0)));

    spaceSearchConnector = new SpaceSearchConnector(configurationManager, client, initParams);
  }

  @Test
  public void testCountFavorites() {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME,
                                                     USER_IDENTITY_ID,
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     null,
                                                     true,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    when(client.countRequest(argThat(esQuery -> hasUserFavoriteQueryPart(esQuery)
                                                && hasPermissionQueryPart(esQuery, MEMBER, 1, USER_NAME)),
                             eq(index))).thenReturn(COUNT_RESULT);
    assertEquals(1, spaceSearchConnector.count(filter));
  }

  @Test
  public void testSearchFavorites() {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME,
                                                     USER_IDENTITY_ID,
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     null,
                                                     true,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    when(client.sendRequest(argThat(esQuery -> hasUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery, MEMBER, 1, USER_NAME)),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  @Test
  public void testSearchManagingSpaces() {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME,
                                                     USER_IDENTITY_ID,
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     null,
                                                     true,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    filter.setManagingTemplateIds(Collections.singletonList(2l));
    when(client.sendRequest(argThat(esQuery -> hasUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery,
                                                                         MEMBER,
                                                                         2,
                                                                         USER_NAME,
                                                                         String.format(SpaceIndexingServiceConnector.TEMPLATE_MANAGER_PATTERN,
                                                                                       2l))),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  @Test
  public void testSearchSpacesByCategoryIds() {
    List<Long> categoryIds = new ArrayList<>();
    categoryIds.add(25l);
    categoryIds.add(37l);
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME,
                                                     USER_IDENTITY_ID,
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     categoryIds,
                                                     Collections.emptyList(),
                                                     null,
                                                     true,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    when(client.sendRequest(argThat(esQuery -> hasCategoryIdsQueryPart(esQuery, categoryIds)),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());
  }

  @Test
  public void testNotSearchWhenNotUnifiedSearch() {
    assertThrows(IllegalArgumentException.class,
                 () -> spaceSearchConnector.search(new SpaceSearchFilter(USER_NAME,
                                                                         USER_IDENTITY_ID,
                                                                         Collections.emptyList(),
                                                                         Collections.emptyList(),
                                                                         Collections.emptyList(),
                                                                         Collections.emptyList(),
                                                                         null,
                                                                         false,
                                                                         null,
                                                                         SpaceMembershipStatus.MEMBER,
                                                                         null,
                                                                         null,
                                                                         null,
                                                                         null),
                                                   0,
                                                   10));
  }

  @Test
  public void testSearchMembers() {
    checkPermissionField(SpaceMembershipStatus.MEMBER, MEMBER);
  }

  @Test
  public void testSearchManagers() {
    checkPermissionField(SpaceMembershipStatus.MANAGER, "manager");
  }

  @Test
  public void testSearchPending() {
    checkPermissionField(SpaceMembershipStatus.PENDING, "pending");
  }

  @Test
  public void testSearchInvited() {
    checkPermissionField(SpaceMembershipStatus.INVITED, "invited");
  }

  @Test
  public void testSearchPublisher() {
    checkPermissionField(SpaceMembershipStatus.PUBLISHER, "publisher");
  }

  @Test
  public void testSearchRedactor() {
    checkPermissionField(SpaceMembershipStatus.REDACTOR, "redactor");
  }

  @Test
  public void testSearchSpaceNoMembership() {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME,
                                                     USER_IDENTITY_ID,
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     PHRASE,
                                                     false,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    when(client.sendRequest(argThat(esQuery -> hasNotUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery,
                                                                         PERMISSIONS_FIELD,
                                                                         2,
                                                                         "all",
                                                                         USER_NAME)),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  @Test
  public void testSearchTags() {
    SpaceSearchFilter filter =
                             new SpaceSearchFilter(USER_NAME,
                                                   USER_IDENTITY_ID,
                                                   Collections.emptyList(),
                                                   Collections.emptyList(),
                                                   Collections.emptyList(),
                                                   Collections.emptyList(),
                                                   null,
                                                   false,
                                                   Arrays.asList("tag1", "tag2"),
                                                   null,
                                                   null,
                                                   null,
                                                   null,
                                                   null);
    when(client.sendRequest(argThat(esQuery -> hasNotUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery, PERMISSIONS_FIELD, 2, "all", USER_NAME)
                                               && hasTagsQueryPart(esQuery, 2, "tag1", "tag2")),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  /**
   * The typed text reaches the analyzed queries as typed, Latin diacritics
   * removed, escaped for JSON only: no character of it is query syntax, and
   * the field's search analyzer tokenizes it the way the index was.
   */
  @Test
  public void testSearchWritesTheTypedTextInTheAnalyzedQueries() {
    assertEquals("test\"", searchedQueryText("test\""));
    assertEquals("test\\", searchedQueryText("test\\"));
    assertEquals("a/b", searchedQueryText("a/b"));
    assertEquals("@limit@", searchedQueryText("@limit@"));
    assertEquals("@fuzziness@ @term@", searchedQueryText("@fuzziness@ @term@"));
    assertEquals("@fuzziness@", searchedQueryText("@fuzziness@"));
    assertEquals("#release\"", searchedQueryText("#release\""));
    assertEquals("test-tes", searchedQueryText("tést-tés"));
    assertEquals("Test-T", searchedQueryText("Tést-T"));
    assertEquals("\ud55c\uad6d\uc5b4 \u304c\u3063\u3053\u3046", searchedQueryText("\ud55c\uad6d\uc5b4 \u304c\u3063\u3053\u3046"));
    assertEquals("www.meeds.io test_test 3.14", searchedQueryText("www.meeds.io test_test 3.14"));
    assertEquals("Tests\t\r\ntes\u3000x", searchedQueryText("Tests\t\r\ntes\u3000x"));
    assertEquals("\" \\ /", searchedQueryText("\" \\ /"));
  }

  /**
   * The three clauses of the term query, on the search and on the count:
   * every token required and the last one a prefix, one edit on the word of
   * a one-word text, the phrase boosted. What each clause matches is the
   * engine's: a text with no token matches nothing (checked on Elasticsearch
   * 8.13.4, not by this suite).
   */
  @Test
  public void testSearchTermQueryClauses() {
    JsonNode request = searchRequest(filterWithTerm("test"), null);
    assertTermQueryClauses(request, "test");

    when(client.countRequest(anyString(), eq(index))).thenReturn(COUNT_RESULT);
    assertEquals(1, spaceSearchConnector.count(filterWithTerm("tést\"")));
    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client).countRequest(query.capture(), eq(index));
    assertTermQueryClauses(new ObjectMapper().readTree(query.getValue()), "test\"");
  }

  /**
   * A tag is an exact value: it is written as typed, escaped for JSON, and a
   * placeholder name in it is not replaced by the template's value (the
   * template is filled in one pass).
   */
  @Test
  public void testSearchWritesTagsAsExactEscapedValues() {
    JsonNode request = searchRequest(filterWithTerm(null), Arrays.asList("release\"", "@limit@", "a\\b"));
    JsonNode should = request.at("/query/bool/should");
    assertEquals(request.toString(), 3, should.size());
    assertEquals("release\"", should.get(0).at("/term/metadatas.tags.metadataName.keyword/value").asText());
    assertEquals("@limit@", should.get(1).at("/term/metadatas.tags.metadataName.keyword/value").asText());
    assertEquals("a\\b", should.get(2).at("/term/metadatas.tags.metadataName.keyword/value").asText());
    assertEquals(1, request.at("/query/bool/minimum_should_match").asInt());
    assertEquals("10", request.get("size").asText());
    assertEquals("all", request.at("/query/bool/filter/0/terms/permissions/0").asText());
    assertEquals(USER_NAME, request.at("/query/bool/filter/0/terms/permissions/1").asText());
    assertTrue(request.toString(), request.at("/query/bool/must").isMissingNode());
  }

  private void assertTermQueryClauses(JsonNode request, String text) {
    JsonNode termQuery = request.at("/query/bool/must/bool");
    assertEquals(request.toString(), 1, termQuery.get("minimum_should_match").asInt());
    JsonNode clauses = termQuery.get("should");
    assertEquals(request.toString(), 3, clauses.size());
    for (JsonNode clause : clauses.values()) {
      JsonNode multiMatch = clause.get("multi_match");
      assertEquals(text, multiMatch.get("query").asText());
      assertEquals("displayName", multiMatch.get("fields").get(0).asText());
      assertEquals("description", multiMatch.get("fields").get(1).asText());
    }
    JsonNode prefix = clauses.get(0).get("multi_match");
    assertEquals("bool_prefix", prefix.get("type").asText());
    assertEquals("and", prefix.get("operator").asText());
    assertTrue(prefix.toString(), prefix.get("fuzziness") == null);
    JsonNode fuzzy = clauses.get(1).get("multi_match");
    assertEquals("best_fields", fuzzy.get("type").asText());
    assertEquals("and", fuzzy.get("operator").asText());
    assertEquals("AUTO:2,1000", fuzzy.get("fuzziness").asText());
    JsonNode phrase = clauses.get(2).get("multi_match");
    assertEquals("phrase", phrase.get("type").asText());
    assertEquals(1, phrase.get("slop").asInt());
    assertEquals(5, phrase.get("boost").asInt());
    assertTrue(request.toString(), request.at("/query/bool/must/query_string").isMissingNode());
  }

  /**
   * One edit is allowed on each token of two letters or more when the text is
   * one word (a single letter is matched exactly), on tokens of five letters
   * or more when the text holds whitespace.
   */
  @Test
  public void testSearchAllowsOneEditOnShortWordsOfAOneWordTextOnly() {
    assertEquals("AUTO:2,1000", searchedFuzziness("tset"));
    assertEquals("AUTO:2,1000", searchedFuzziness("test-test"));
    assertEquals("AUTO:2,1000", searchedFuzziness("a/b"));
    assertEquals("AUTO:5,1000", searchedFuzziness("test tes"));
    assertEquals("AUTO:5,1000", searchedFuzziness("a\tb"));
  }

  private String searchedFuzziness(String term) {
    JsonNode request = searchRequest(filterWithTerm(term), null);
    return request.at("/query/bool/must/bool/should/1/multi_match/fuzziness").asText();
  }

  private String searchedQueryText(String term) {
    JsonNode request = searchRequest(filterWithTerm(term), null);
    JsonNode clauses = request.at("/query/bool/must/bool/should");
    assertEquals(request.toString(), 3, clauses.size());
    String text = clauses.get(0).at("/multi_match/query").asText();
    assertEquals(text, clauses.get(1).at("/multi_match/query").asText());
    assertEquals(text, clauses.get(2).at("/multi_match/query").asText());
    assertEquals(request.toString(), "10", request.get("size").asText());
    return text;
  }

  private SpaceSearchFilter filterWithTerm(String term) {
    return new SpaceSearchFilter(USER_NAME,
                                 USER_IDENTITY_ID,
                                 Collections.emptyList(),
                                 Collections.emptyList(),
                                 Collections.emptyList(),
                                 Collections.emptyList(),
                                 term,
                                 false,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null,
                                 null);
  }

  /**
   * Runs a search on the shipped query template and returns the request sent
   * to Elasticsearch, parsed by a strict JSON parser
   */
  @SneakyThrows
  private JsonNode searchRequest(SpaceSearchFilter filter, List<String> tagNames) {
    filter.setTagNames(tagNames);
    when(client.sendRequest(anyString(), eq(index))).thenReturn(SEARCH_RESULT);
    assertEquals(1, spaceSearchConnector.search(filter, 0, 10).size());
    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client).sendRequest(query.capture(), eq(index));
    Mockito.reset(client);
    return new ObjectMapper().readTree(query.getValue());
  }

  private void checkPermissionField(SpaceMembershipStatus status, String fieldName) {
    SpaceSearchFilter filter = new SpaceSearchFilter(USER_NAME,
                                                     USER_IDENTITY_ID,
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     Collections.emptyList(),
                                                     TERM,
                                                     false,
                                                     null,
                                                     status,
                                                     null,
                                                     null,
                                                     null,
                                                     null);
    when(client.sendRequest(argThat(esQuery -> hasNotUserFavoriteQueryPart(esQuery)
                                               && hasPermissionQueryPart(esQuery, fieldName, 1, USER_NAME)),
                            eq(index))).thenReturn(SEARCH_RESULT);
    List<SpaceSearchResult> search = spaceSearchConnector.search(filter, 0, 10);
    assertEquals(1, search.size());

    assertNotNull(search.get(0).getNameExcerpts());
    assertNotNull(search.get(0).getDescriptionExcerpts());

    assertEquals(1, search.get(0).getNameExcerpts().size());
    assertEquals(1, search.get(0).getDescriptionExcerpts().size());
  }

  @SneakyThrows
  private boolean hasUserFavoriteQueryPart(String esQuery) {
    JsonNode favorite = getFilterNode(esQuery, "metadatas.favorites.metadataName.keyword");
    return favorite != null && favorite.size() == 1 && String.valueOf(USER_IDENTITY_ID).equals(favorite.get(0).asText());
  }

  @SneakyThrows
  private boolean hasNotUserFavoriteQueryPart(String esQuery) {
    JsonNode favorite = getFilterNode(esQuery, "metadatas.favorites.metadataName.keyword");
    return favorite == null;
  }

  @SneakyThrows
  private boolean hasPermissionQueryPart(String esQuery, String permissionField, int size, String... permissionValues) {
    JsonNode permission = getFilterNode(esQuery, permissionField);
    return permission != null
           && permission.size() == size
           && StreamSupport.stream(permission.spliterator(), false)
                           .allMatch(e -> Stream.of(permissionValues)
                                                .anyMatch(v -> v.equals(e.asText())));
  }

  @SneakyThrows
  private boolean hasCategoryIdsQueryPart(String esQuery, List<Long> categoryIds) {
    JsonNode categoryId = getFilterNode(esQuery, "categoryId");
    return categoryId != null
           && categoryId.size() == 2
           && StreamSupport.stream(categoryId.spliterator(), false)
                           .allMatch(e -> categoryIds
                                                     .stream()
                                                     .anyMatch(v -> v.equals(e.asLong())));
  }

  @SneakyThrows
  private boolean hasTagsQueryPart(String esQuery, int size, String... tagValues) {
    return Stream.of(tagValues).allMatch(v -> hasTagsNode(esQuery, v));
  }

  @SneakyThrows
  private JsonNode getFilterNode(String esQuery, String filterName) {
    JsonNode jsonNode = OBJECT_MAPPER.readTree(esQuery);
    JsonNode filter = jsonNode.get("query")
                              .get("bool")
                              .get("filter");
    if (filter == null || filter.size() == 0) {
      return null;
    }
    Iterator<JsonNode> filterElements = filter.values().iterator();
    JsonNode filterNode = null;
    while (filterElements.hasNext() && filterNode == null) {
      JsonNode filterElement = filterElements.next();
      JsonNode terms = filterElement.get("terms");
      filterNode = terms == null ? null : terms.get(filterName);
    }
    return filterNode;
  }

  @SneakyThrows
  private boolean hasTagsNode(String esQuery, String tagValue) {
    JsonNode jsonNode = OBJECT_MAPPER.readTree(esQuery);
    JsonNode filter = jsonNode.get("query")
                              .get("bool")
                              .get("should");
    if (filter == null || filter.size() == 0) {
      return false;
    }
    return StreamSupport.stream(filter.spliterator(), false)
                        .anyMatch(element -> {
                          JsonNode term = element.get("term");
                          JsonNode tagElement = term == null ? null : term.get("metadatas.tags.metadataName.keyword");
                          return tagElement != null && tagValue.equals(tagElement.get("value").asText());
                        });
  }

}
