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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Aggregates, over a set of indexed profiles, the values their profile
 * properties have in common. It only ever queries the profile index it is
 * configured with: the index is not a request parameter.
 */
public class ComplementaryFilterSearchConnector {

  public static final String           DEFAULT_PROFILE_INDEX     = "profile_alias";

  public static final String           UNKNOWN_ATTRIBUTE_MESSAGE = "complementaryFilter.unknownAttribute";

  public static final String           INVALID_OBJECT_ID_MESSAGE = "complementaryFilter.invalidObjectId";

  public static final String           INVALID_MIN_COUNT_MESSAGE = "complementaryFilter.invalidMinDocCount";

  private static final Log             LOG                       = ExoLogger.getLogger(ComplementaryFilterSearchConnector.class);

  private static final JsonMapper      JSON_MAPPER               = new JsonMapper();

  private static final String          QUERY_HEAD                = """
      {
       "size": 0,
       "query": {
         "terms": {
           "_id": [%s]
         }
       },
       "aggs": {""";

  private static final String          AGGREGATION_TEMPLATE      = """
         %s: {
           "terms": {
             "field": %s,
             "exclude": ["hidden"],
             "min_doc_count": %s,
             "size": 50,
             "order": {
               "_count": "desc"
             }
           }
         }""";

  private static final String          QUERY_TAIL                = """

       }
      }
      """;

  private final ElasticSearchingClient client;

  private final ProfilePropertyService profilePropertyService;

  private final String                 index;

  public ComplementaryFilterSearchConnector(InitParams initParams,
                                            ElasticSearchingClient client,
                                            ProfilePropertyService profilePropertyService) {
    this.client = client;
    this.profilePropertyService = profilePropertyService;
    PropertiesParam param = initParams == null ? null : initParams.getPropertiesParam("constructor.params");
    String configuredIndex = param == null ? null : param.getProperty("index");
    this.index = StringUtils.isBlank(configuredIndex) ? DEFAULT_PROFILE_INDEX : configuredIndex;
  }

  /**
   * @param attributes names of profile property settings to aggregate on
   * @param objectIds identity ids of the profiles to aggregate over
   * @param minDocCount minimum number of profiles sharing a value for it to
   *          be suggested
   * @return one row per suggested value: {@code key} (the attribute),
   *         {@code value} and {@code count}
   * @throws IllegalArgumentException when an attribute is not a visible
   *           profile property setting (the only ones the profile index
   *           holds), an object id is not a number, or minDocCount is lower
   *           than 1; the message is a code, returned as the 400 body
   */
  public List<Map<String, String>> search(List<String> attributes, List<String> objectIds, int minDocCount) {
    List<String> validatedAttributes = validateAttributes(attributes);
    List<Long> identityIds = parseObjectIds(objectIds);
    if (minDocCount < 1) {
      throw new IllegalArgumentException(INVALID_MIN_COUNT_MESSAGE);
    }
    String esQuery = buildQuery(validatedAttributes, identityIds, minDocCount);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return buildResult(jsonResponse, validatedAttributes);
  }

  private List<String> validateAttributes(List<String> attributes) {
    if (attributes == null || attributes.isEmpty()) {
      throw new IllegalArgumentException(UNKNOWN_ATTRIBUTE_MESSAGE);
    }
    List<String> propertyNames = profilePropertyService.getPropertySettings()
                                                       .stream()
                                                       .filter(ProfilePropertySetting::isVisible)
                                                       .map(ProfilePropertySetting::getPropertyName)
                                                       .toList();
    List<String> validated = new ArrayList<>();
    for (String attribute : attributes) {
      if (StringUtils.isBlank(attribute) || !propertyNames.contains(attribute)) {
        throw new IllegalArgumentException(UNKNOWN_ATTRIBUTE_MESSAGE);
      }
      if (!validated.contains(attribute)) {
        validated.add(attribute);
      }
    }
    return validated;
  }

  private List<Long> parseObjectIds(List<String> objectIds) {
    if (objectIds == null || objectIds.isEmpty()) {
      throw new IllegalArgumentException(INVALID_OBJECT_ID_MESSAGE);
    }
    List<Long> identityIds = new ArrayList<>();
    for (String objectId : objectIds) {
      try {
        identityIds.add(Long.parseLong(StringUtils.trim(objectId)));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(INVALID_OBJECT_ID_MESSAGE, e);
      }
    }
    return identityIds;
  }

  private List<Map<String, String>> buildResult(String jsonResponse, List<String> attributes) {
    List<Map<String, String>> result = new ArrayList<>();
    try {
      JsonNode root = JSON_MAPPER.readTree(jsonResponse);
      for (String attribute : attributes) {
        JsonNode aggregations = root.get("aggregations");
        JsonNode attributeAggregation = aggregations.path("common_" + attribute);
        JsonNode buckets = attributeAggregation.path("buckets");
        for (JsonNode bucket : buckets) {
          String value = bucket.path("key").asString();
          String count = bucket.path("doc_count").asString();
          result.add(Map.of("value", value, "key", attribute, "count", count));
        }
      }
    } catch (Exception e) { // NOSONAR a malformed engine answer yields no suggestion
      LOG.error("Error while parsing es json response: {}", jsonResponse, e);
    }
    return result;
  }

  private String buildQuery(List<String> attributes, List<Long> identityIds, int minDocCount) {
    StringBuilder query = new StringBuilder(QUERY_HEAD.formatted(StringUtils.join(identityIds, ",")));
    for (int i = 0; i < attributes.size(); i++) {
      String attribute = attributes.get(i);
      // the aggregation keeps the property name, the field is the indexed one
      String aggregationName = jsonString("common_" + attribute);
      String fieldName = jsonString(ProfileIndexingServiceConnector.getIndexedFieldName(attribute) + ".raw");
      query.append(AGGREGATION_TEMPLATE.formatted(aggregationName, fieldName, minDocCount));
      if (i != attributes.size() - 1) {
        query.append(",");
      }
    }
    return query.append(QUERY_TAIL).toString();
  }

  private static String jsonString(String value) {
    return JSON_MAPPER.writeValueAsString(value);
  }
}
