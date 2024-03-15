/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package org.exoplatform.social.core.jpa.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.*;

public class ComplementaryFilterSearchConnector {

  private static final Log             LOG = ExoLogger.getLogger(ComplementaryFilterSearchConnector.class);

  private final ElasticSearchingClient client;


  public ComplementaryFilterSearchConnector(ElasticSearchingClient client) {
    this.client = client;
  }

  public List<Map<String, String>> search(List<String> attributes, List<String> objectIds, int minDocCount, String indexAlias) {
    String esQuery = buildQuery(attributes, objectIds, minDocCount);
    String jsonResponse = this.client.sendRequest(esQuery, indexAlias);
    return buildResult(jsonResponse, attributes);
  }

  private List<Map<String, String>> buildResult(String jsonResponse, List<String> attributes) {
    List<Map<String, String>> result = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode root = objectMapper.readTree(jsonResponse);
      for (String attribute : attributes) {
        JsonNode aggregations = root.get("aggregations");
        JsonNode attributeAggregation = aggregations.path("common_" + attribute);
        JsonNode buckets = attributeAggregation.path("buckets");
        for (JsonNode bucket : buckets) {
          String value = bucket.path("key").asText();
          String count = bucket.path("doc_count").asText();
          result.add(Map.of("value", value, "key", attribute, "count", count));
        }
      }
    } catch (Exception e) {
      LOG.error("Error while parsing es json response: {}", jsonResponse, e);
    }
    return result;
  }

  private String buildQuery(List<String> attributes, List<String> objectIds, int minDocCount) {
    StringBuilder query = new StringBuilder();
    query.append("""
                  {
                   "size": 0,
                   "query": {
                     "terms": {
                       "_id": [%s]
                     }
                   },
                  """.formatted(String.join(",", objectIds))).append("""
                  "aggs": {""");
              for (int i = 0; i < attributes.size(); i++) {
                String attribute = attributes.get(i);
                query.append(""" 
                    "common_%s": {
                       "terms": {
                         "field": "%s.raw",
                         "exclude": ["hidden"],
                         "min_doc_count":%s,
                         "size": 50,
                         "order": {
                           "_count": "desc"
                          }
                       }
                     }
                     """.formatted(attribute, attribute, minDocCount));
                if (i != attributes.size() - 1) {
                  query.append(", ");
                }
              }
    query.append("""
                    }
                  }
               """);
    return query.toString();
  }
}
