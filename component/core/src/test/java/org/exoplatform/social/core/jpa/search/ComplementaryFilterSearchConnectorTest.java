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
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;

@RunWith(MockitoJUnitRunner.class)
public class ComplementaryFilterSearchConnectorTest {

  private ComplementaryFilterSearchConnector complementaryFilterSearchConnector;

  @Mock
  private ElasticSearchingClient             client;

  @Before
  public void setUp() throws Exception {
    this.complementaryFilterSearchConnector = new ComplementaryFilterSearchConnector(client);
  }

  @Test
  public void testSearch() {

    List<String> attributes = new ArrayList<>();
    List<String> objectIds = new ArrayList<>();

    attributes.add("profession");
    attributes.add("city");

    objectIds.add("1");
    objectIds.add("2");
    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"took":1,"timed_out":false,"_shards":{"total":5,"successful":5,"skipped":0,"failed":0},
        "hits":{"total":{"value":5,"relation":"eq"},"max_score":null,"hits":[]},"aggregations":
        {"common_city":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":"Ariana","doc_count":4}]},
        "common_profession":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,
        "buckets":[{"key":"writer","doc_count":3},{"key":"Developer","doc_count":2}]}}}
          """);
    List<Map<String, String>> result = this.complementaryFilterSearchConnector.search(attributes, objectIds, 2, "index_alias");
    assertNotNull(result);
    assertEquals(3, result.size());
  }
}
