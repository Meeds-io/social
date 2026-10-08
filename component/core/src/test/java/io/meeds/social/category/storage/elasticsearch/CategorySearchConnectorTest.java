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
package io.meeds.social.category.storage.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;

import io.meeds.social.category.model.CategorySearchFilter;

/**
 * Checks the structure of the queries the connector builds, parsed strictly.
 * It does not run them on Elasticsearch: the module has no engine harness, so
 * whether the engine accepts a clause is checked by sending the built query to
 * a real index ({@code _validate/query?explain}, then {@code _search} and
 * {@code _count}) when a clause changes.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategorySearchConnectorTest {

  private static final ObjectMapper  OBJECT_MAPPER = new ObjectMapper();

  private static final String        INDEX         = "category_alias";

  private static final String        NO_HIT        = "{\"hits\":{\"hits\":[]}}";

  private static final List<Long>    IDENTITY_IDS  = Arrays.asList(3l, 4l);

  @Mock
  private ElasticSearchingClient     client;

  @InjectMocks
  private CategorySearchConnector    connector;

  @Test
  public void testSearchWithoutAllowedIdsHasNoIdsClause() {
    when(client.sendRequest(argThat(query -> {
      JsonNode must = must(query);
      return must.size() == 2 && !hasClause(must, "ids");
    }), eq(INDEX))).thenReturn("{\"hits\":{\"hits\":[{\"_id\":\"8\"}]}}");
    assertEquals(Collections.singletonList(8l), connector.search(filter(5l, 0l, true), IDENTITY_IDS, Locale.ENGLISH));
  }

  @Test
  public void testSearchRestrictedToAllowedIds() {
    when(client.sendRequest(argThat(query -> {
      JsonNode must = must(query);
      JsonNode ids = clause(must, "ids");
      return must.size() == 3
             && hasClause(must, "term")
             && hasClause(must, "terms")
             && ids != null
             && "[\"7\",\"9\"]".equals(ids.get("values").toString());
    }), eq(INDEX))).thenReturn("{\"hits\":{\"hits\":[{\"_id\":\"7\"}]}}");
    assertEquals(Collections.singletonList(7l),
                 connector.search(filter(5l, 0l, true), IDENTITY_IDS, Arrays.asList(7l, 9l), Locale.ENGLISH));
  }

  @Test
  public void testSearchWithAllowedIdsAlone() {
    when(client.sendRequest(argThat(query -> {
      JsonNode must = must(query);
      return must.size() == 1 && "[\"7\"]".equals(clause(must, "ids").get("values").toString());
    }), eq(INDEX))).thenReturn("{\"hits\":{\"hits\":[{\"_id\":\"7\"}]}}");
    assertEquals(Collections.singletonList(7l),
                 connector.search(filter(0l, 0l, false), IDENTITY_IDS, Collections.singletonList(7l), Locale.ENGLISH));
  }

  @Test
  public void testSearchWithoutAnyClause() {
    when(client.sendRequest(argThat(query -> must(query).isEmpty()), eq(INDEX))).thenReturn(NO_HIT);
    assertTrue(connector.search(filter(0l, 0l, false), IDENTITY_IDS, Locale.ENGLISH).isEmpty());
  }

  @Test
  public void testCountWithoutAllowedIdsHasNoIdsClause() {
    when(client.countRequest(argThat(query -> {
      JsonNode must = must(query);
      return must.size() == 1 && !hasClause(must, "ids");
    }), eq(INDEX))).thenReturn("{\"count\":3}");
    assertEquals(3, connector.count(filter(0l, 2l, false), IDENTITY_IDS, Locale.ENGLISH));
    assertFalse(hasClause(must(connector.buildSearchQuery("{\"query\":{\"bool\":{\"must\":[@must_queries@]}}}",
                                                          filter(0l, 2l, false),
                                                          IDENTITY_IDS,
                                                          null,
                                                          Locale.ENGLISH)),
                          "ids"));
  }

  private CategorySearchFilter filter(long parentId, long ownerId, boolean linkPermission) {
    return new CategorySearchFilter("term", null, ownerId, parentId, 0, 10, linkPermission, false);
  }

  /**
   * Parses the query strictly, so that a trailing or a missing comma between
   * the must clauses fails, as Elasticsearch would.
   */
  private static JsonNode must(String query) {
    return OBJECT_MAPPER.readTree(query).get("query").get("bool").get("must");
  }

  private static boolean hasClause(JsonNode must, String name) {
    return clause(must, name) != null;
  }

  private static JsonNode clause(JsonNode must, String name) {
    return StreamSupport.stream(must.spliterator(), false)
                        .filter(node -> node.has(name))
                        .map(node -> node.get(name))
                        .findFirst()
                        .orElse(null);
  }

}
