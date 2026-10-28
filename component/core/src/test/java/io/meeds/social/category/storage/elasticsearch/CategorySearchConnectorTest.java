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
package io.meeds.social.category.storage.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;

import io.meeds.social.category.model.CategorySearchFilter;

/**
 * The query the connector builds is text: every test parses the captured
 * request with a strict JSON parser (a trailing or a missing comma between the
 * must clauses fails, as Elasticsearch would) and reads the query as
 * Elasticsearch reads it. What the query matches is the engine's (the ngram
 * name field's own search analyzer tokenizes the text; a text with no token
 * matches nothing), checked on Elasticsearch 8.13.4, not by this suite.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategorySearchConnectorTest {

  private static final String     INDEX      = "category_alias";

  private static final String     NAME_FIELD = "name-en";

  @Mock
  private ElasticSearchingClient  client;

  @InjectMocks
  private CategorySearchConnector connector;

  /**
   * The typed text reaches the analyzed match query as typed, Latin
   * diacritics removed, escaped for JSON only: no character of it is query
   * syntax.
   */
  @Test
  public void testSearchWritesTheTypedTextInTheAnalyzedQuery() {
    assertEquals("test\"", searchedQueryText("test\""));
    assertEquals("test\\", searchedQueryText("test\\"));
    assertEquals("a/b", searchedQueryText("a/b"));
    assertEquals("@limit@", searchedQueryText("@limit@"));
    assertEquals("@sort_query@", searchedQueryText("@sort_query@"));
    assertEquals("@must_queries@", searchedQueryText("@must_queries@"));
    assertEquals("@name_field@ @term@", searchedQueryText("@name_field@ @term@"));
    assertEquals("#release\"", searchedQueryText("#release\""));
    assertEquals("test-tes", searchedQueryText("tést-tés"));
    assertEquals("Publie", searchedQueryText("Publié"));
    assertEquals("한국어 がっこう", searchedQueryText("한국어 がっこう"));
    assertEquals("a\tb\r\nc", searchedQueryText("a\tb\r\nc"));
    assertEquals("(test) AND {x} OR [y] NOT ^~*?:+-=&|><!", searchedQueryText("(test) AND {x} OR [y] NOT ^~*?:+-=&|><!"));
    assertEquals("\" \\ /", searchedQueryText("\" \\ /"));
  }

  @Test
  public void testSearchWritesAnAndMatchOnTheNameFieldOfTheLocale() {
    JsonNode request = search(filter("test"));
    JsonNode match = request.at("/query/bool/filter/match/" + NAME_FIELD);
    assertEquals(request.toString(), "test", match.get("query").asText());
    assertEquals("and", match.get("operator").asText());
    assertTrue(request.toString(), request.at("/query/bool/filter/query_string").isMissingNode());
    assertEquals("0", request.get("from").asText());
    assertEquals("100", request.get("size").asText());
    assertEquals("_score", request.get("sort").get(0).asText());
  }

  @Test
  public void testSearchWithParentAndLinkPermissions() {
    CategorySearchFilter filter = filter("test");
    filter.setParentId(5);
    filter.setLinkPermission(true);
    filter.setLimit(20);
    filter.setOffset(40);
    filter.setSortByName(true);
    JsonNode request = search(filter, List.of(1L, 2L));
    JsonNode must = request.at("/query/bool/must");
    assertEquals(request.toString(), 2, must.size());
    assertEquals("5", must.get(0).at("/term/parentId").asText());
    assertEquals(2, must.get(1).at("/terms/linkPermissions").size());
    assertEquals("1", must.get(1).at("/terms/linkPermissions").get(0).asText());
    assertEquals("2", must.get(1).at("/terms/linkPermissions").get(1).asText());
    assertEquals("40", request.get("from").asText());
    assertEquals("20", request.get("size").asText());
    assertEquals("asc", request.get("sort").get(0).get(NAME_FIELD + ".raw").asText());
  }

  @Test
  public void testSearchWithOwnerAndNoTerm() {
    CategorySearchFilter filter = filter(null);
    filter.setOwnerId(7);
    filter.setLinkPermission(true);
    JsonNode request = search(filter, List.of(3L));
    JsonNode must = request.at("/query/bool/must");
    assertEquals(request.toString(), 2, must.size());
    assertEquals("7", must.get(0).at("/term/ownerId").asText());
    assertEquals("3", must.get(1).at("/terms/linkPermissions").get(0).asText());
    assertFalse(request.toString(), request.at("/query/bool").has("filter"));
  }

  @Test
  public void testSearchWithoutAllowedIdsHasNoIdsClause() {
    CategorySearchFilter filter = filter("test");
    filter.setParentId(5);
    filter.setLinkPermission(true);
    JsonNode must = search(filter, List.of(3L, 4L)).at("/query/bool/must");
    assertEquals(must.toString(), 2, must.size());
    assertNull(clause(must, "ids"));
  }

  @Test
  public void testSearchRestrictedToAllowedIds() {
    CategorySearchFilter filter = filter("test");
    filter.setParentId(5);
    filter.setLinkPermission(true);
    JsonNode must = search(filter, List.of(3L, 4L), List.of(7L, 9L)).at("/query/bool/must");
    assertEquals(must.toString(), 3, must.size());
    assertEquals("5", clause(must, "term").get("parentId").asText());
    assertEquals(2, clause(must, "terms").get("linkPermissions").size());
    assertEquals("[\"7\",\"9\"]", clause(must, "ids").get("values").toString());
  }

  @Test
  public void testSearchWithAllowedIdsAlone() {
    JsonNode must = search(filter("test"), List.of(3L, 4L), List.of(7L)).at("/query/bool/must");
    assertEquals(must.toString(), 1, must.size());
    assertEquals("[\"7\"]", clause(must, "ids").get("values").toString());
  }

  @Test
  public void testSearchWithoutAnyClause() {
    JsonNode must = search(filter("test"), List.of(3L, 4L)).at("/query/bool/must");
    assertTrue(must.toString(), must.isEmpty());
  }

  @Test
  public void testCountWritesTheSameTermAsTheSearch() {
    when(client.countRequest(anyString(), anyString())).thenReturn("{\"count\": 3}");
    CategorySearchFilter filter = filter("tést\" a/b");
    filter.setLinkPermission(true);
    assertEquals(3, connector.count(filter, List.of(1L), Locale.ENGLISH));
    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client).countRequest(query.capture(), eq(INDEX));
    JsonNode request = new ObjectMapper().readTree(query.getValue());
    assertEquals("test\" a/b", request.at("/query/bool/filter/match/" + NAME_FIELD + "/query").asText());
    assertEquals("and", request.at("/query/bool/filter/match/" + NAME_FIELD + "/operator").asText());
    assertEquals("1", request.at("/query/bool/must/0/terms/linkPermissions/0").asText());
    assertTrue(request.toString(), request.get("from") == null);
  }

  @Test
  public void testCountWithoutAllowedIdsHasNoIdsClause() {
    when(client.countRequest(anyString(), anyString())).thenReturn("{\"count\": 3}");
    CategorySearchFilter filter = filter("test");
    filter.setOwnerId(2);
    assertEquals(3, connector.count(filter, List.of(3L, 4L), Locale.ENGLISH));
    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client).countRequest(query.capture(), eq(INDEX));
    JsonNode must = new ObjectMapper().readTree(query.getValue()).at("/query/bool/must");
    assertEquals(must.toString(), 1, must.size());
    assertNull(clause(must, "ids"));
    must = new ObjectMapper().readTree(connector.buildSearchQuery("{\"query\":{\"bool\":{\"must\":[@must_queries@]}}}",
                                                                 filter,
                                                                 List.of(3L, 4L),
                                                                 null,
                                                                 Locale.ENGLISH))
                             .at("/query/bool/must");
    assertEquals(must.toString(), 1, must.size());
    assertNull(clause(must, "ids"));
  }

  private String searchedQueryText(String term) {
    JsonNode request = search(filter(term));
    return request.at("/query/bool/filter/match/" + NAME_FIELD + "/query").asText();
  }

  private CategorySearchFilter filter(String term) {
    CategorySearchFilter filter = new CategorySearchFilter();
    filter.setTerm(term);
    return filter;
  }

  private JsonNode search(CategorySearchFilter filter) {
    return search(filter, List.of());
  }

  private JsonNode search(CategorySearchFilter filter, List<Long> identityIds) {
    return search(filter, identityIds, null);
  }

  private JsonNode search(CategorySearchFilter filter, List<Long> identityIds, List<Long> categoryIds) {
    when(client.sendRequest(anyString(), anyString())).thenReturn("{}");
    assertTrue(connector.search(filter, identityIds, categoryIds, Locale.ENGLISH).isEmpty());
    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client).sendRequest(query.capture(), eq(INDEX));
    Mockito.clearInvocations(client);
    return new ObjectMapper().readTree(query.getValue());
  }

  private static JsonNode clause(JsonNode must, String name) {
    return StreamSupport.stream(must.spliterator(), false)
                        .filter(node -> node.has(name))
                        .map(node -> node.get(name))
                        .findFirst()
                        .orElse(null);
  }

}
