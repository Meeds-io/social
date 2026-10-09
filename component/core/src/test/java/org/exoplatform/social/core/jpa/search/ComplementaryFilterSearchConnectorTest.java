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
package org.exoplatform.social.core.jpa.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RunWith(MockitoJUnitRunner.class)
public class ComplementaryFilterSearchConnectorTest {

  private static final String                ES_RESPONSE = """
      {"took":1,"timed_out":false,"_shards":{"total":5,"successful":5,"skipped":0,"failed":0},
      "hits":{"total":{"value":5,"relation":"eq"},"max_score":null,"hits":[]},"aggregations":
      {"common_city":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":"Ariana","doc_count":4}]},
      "common_profession":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,
      "buckets":[{"key":"writer","doc_count":3},{"key":"Developer","doc_count":2}]}}}
      """;

  @Mock
  private ElasticSearchingClient             client;

  @Mock
  private ProfilePropertyService             profilePropertyService;

  private ComplementaryFilterSearchConnector connector;

  @Before
  public void setUp() {
    connector = new ComplementaryFilterSearchConnector(initParams("profile_alias"), client, profilePropertyService);
    lenient().when(profilePropertyService.getPropertySettings()).thenReturn(List.of(setting("profession"), setting("city")));
    lenient().when(client.sendRequest(anyString(), anyString())).thenReturn(ES_RESPONSE);
  }

  @Test
  public void testSearchParsesEveryBucketOfEveryAttribute() {
    List<Map<String, String>> result = connector.search(List.of("profession", "city"), List.of("1", "2"), 2);
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("profession", result.get(0).get("key"));
    assertEquals("writer", result.get(0).get("value"));
    assertEquals("3", result.get(0).get("count"));
    assertEquals("city", result.get(2).get("key"));
  }

  @Test
  public void testSearchOnlyQueriesTheConfiguredProfileIndex() {
    connector = new ComplementaryFilterSearchConnector(initParams("profile_v9"), client, profilePropertyService);
    connector.search(List.of("city"), List.of("1", "2"), 2);
    verify(client).sendRequest(anyString(), eq("profile_v9"));
  }

  @Test
  public void testMissingIndexParamFallsBackToTheProfileAlias() {
    connector = new ComplementaryFilterSearchConnector(null, client, profilePropertyService);
    connector.search(List.of("city"), List.of("1"), 1);
    verify(client).sendRequest(anyString(), eq(ComplementaryFilterSearchConnector.DEFAULT_PROFILE_INDEX));
  }

  /**
   * Checks the request body with a strict JSON parser only: this repository
   * has no Elasticsearch harness, so the engine never executes the query here.
   */
  @Test
  public void testQueryIsValidJsonWithNumericIdsAndEscapedAttributes() throws Exception {
    String hostile = "city\", \"script\": \"x";
    when(profilePropertyService.getPropertySettings()).thenReturn(List.of(setting("profession"), setting(hostile)));
    connector.search(List.of("profession", hostile), List.of(" 12 ", "34"), 3);

    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client).sendRequest(query.capture(), eq("profile_alias"));
    JsonNode root = new JsonMapper().readTree(query.getValue());
    JsonNode ids = root.path("query").path("terms").path("_id");
    assertEquals(2, ids.size());
    assertEquals(12, ids.get(0).asLong());
    assertEquals(34, ids.get(1).asLong());
    JsonNode aggs = root.path("aggs");
    assertEquals(2, aggs.size());
    assertEquals("profession.raw", aggs.path("common_profession").path("terms").path("field").asString());
    assertEquals(3, aggs.path("common_profession").path("terms").path("min_doc_count").asInt());
    assertEquals(hostile + ".raw", aggs.path("common_" + hostile).path("terms").path("field").asString());
    assertFalse(aggs.path("common_" + hostile).path("terms").has("script"));
  }

  @Test
  public void testUnknownAttributeIsRefusedBeforeAnyQuery() {
    List<String> attributes = List.of("profession", "title");
    List<String> objectIds = List.of("1", "2");
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> connector.search(attributes, objectIds, 2));
    assertEquals(ComplementaryFilterSearchConnector.UNKNOWN_ATTRIBUTE_MESSAGE, e.getMessage());
    verify(client, never()).sendRequest(anyString(), anyString());
  }

  /**
   * The profile index holds the visible settings only
   * ({@code ProfileIndexingServiceConnector}): a hidden one is refused rather
   * than aggregated on a field that exists for no profile.
   */
  @Test
  public void testNonVisibleAttributeIsRefusedBeforeAnyQuery() {
    ProfilePropertySetting hiddenSetting = setting("salary");
    hiddenSetting.setVisible(false);
    when(profilePropertyService.getPropertySettings()).thenReturn(List.of(setting("city"), hiddenSetting));
    List<String> attributes = List.of("city", "salary");
    List<String> objectIds = List.of("1", "2");
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> connector.search(attributes, objectIds, 2));
    assertEquals(ComplementaryFilterSearchConnector.UNKNOWN_ATTRIBUTE_MESSAGE, e.getMessage());
    verify(client, never()).sendRequest(anyString(), anyString());
  }

  @Test
  public void testEmptyOrBlankAttributesAreRefused() {
    List<String> objectIds = List.of("1");
    List<String> noAttribute = List.of();
    List<String> blankAttribute = List.of(" ");
    assertThrows(IllegalArgumentException.class, () -> connector.search(noAttribute, objectIds, 1));
    assertThrows(IllegalArgumentException.class, () -> connector.search(null, objectIds, 1));
    assertThrows(IllegalArgumentException.class, () -> connector.search(blankAttribute, objectIds, 1));
    verify(client, never()).sendRequest(anyString(), anyString());
  }

  @Test
  public void testNonNumericObjectIdIsRefusedBeforeAnyQuery() {
    List<String> attributes = List.of("city");
    List<String> injectedObjectIds = List.of("1", "\"x\"]}}");
    List<String> noObjectId = List.of();
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                              () -> connector.search(attributes, injectedObjectIds, 2));
    assertEquals(ComplementaryFilterSearchConnector.INVALID_OBJECT_ID_MESSAGE, e.getMessage());
    assertThrows(IllegalArgumentException.class, () -> connector.search(attributes, noObjectId, 2));
    assertThrows(IllegalArgumentException.class, () -> connector.search(attributes, null, 2));
    verify(client, never()).sendRequest(anyString(), anyString());
  }

  @Test
  public void testMinDocCountLowerThanOneIsRefused() {
    List<String> attributes = List.of("city");
    List<String> objectIds = List.of("1");
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> connector.search(attributes, objectIds, 0));
    assertEquals(ComplementaryFilterSearchConnector.INVALID_MIN_COUNT_MESSAGE, e.getMessage());
    assertThrows(IllegalArgumentException.class, () -> connector.search(attributes, objectIds, -5));
    verify(client, never()).sendRequest(anyString(), anyString());
    assertTrue(connector.search(attributes, objectIds, 1).size() > 0);
  }

  /**
   * The indexer stores a property's value under its name with every dot
   * replaced by an underscore ({@code ProfileIndexingServiceConnector}): the
   * aggregation keeps the property name, so the caller reads its rows under
   * the key it asked for, while the field is the indexed one.
   */
  @Test
  public void testDottedAttributeAggregatesOnTheIndexedFieldName() throws Exception {
    when(profilePropertyService.getPropertySettings()).thenReturn(List.of(setting("phones.work")));
    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"aggregations":{"common_phones.work":{"buckets":[{"key":"+216","doc_count":2}]}}}
        """);

    List<Map<String, String>> result = connector.search(List.of("phones.work"), List.of("1", "2"), 2);

    ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
    verify(client).sendRequest(query.capture(), eq("profile_alias"));
    JsonNode aggregation = new JsonMapper().readTree(query.getValue()).path("aggs").path("common_phones.work");
    assertEquals("phones_work.raw", aggregation.path("terms").path("field").asString());
    assertEquals(1, result.size());
    assertEquals("phones.work", result.get(0).get("key"));
    assertEquals("+216", result.get(0).get("value"));
  }

  @Test
  public void testDuplicateAttributesAreAggregatedOnce() {
    List<Map<String, String>> result = connector.search(List.of("city", "city"), List.of("1", "2"), 2);
    assertEquals(1, result.size());
  }

  private static ProfilePropertySetting setting(String name) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setPropertyName(name);
    setting.setVisible(true);
    return setting;
  }

  private static InitParams initParams(String index) {
    InitParams initParams = new InitParams();
    PropertiesParam param = new PropertiesParam();
    param.setName("constructor.params");
    param.setProperty("index", index);
    initParams.addParameter(param);
    return initParams;
  }
}
