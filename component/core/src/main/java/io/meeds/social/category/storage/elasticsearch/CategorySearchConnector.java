/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.category.model.CategorySearchFilter;

@Component
public class CategorySearchConnector {

  private static final String    CATEGORY_INDEX                = "category_alias";

  private static final Log       LOG                           = ExoLogger.getLogger(CategorySearchConnector.class);

  private static final String    SEARCH_QUERY_TERM             = """
          {
            "from": "@offset@",
            "size": "@limit@",
            "query":{
              "bool":{
                "must":{
                  "query_string":{
                    "fields": ["@name_field@"],
                    "default_operator": "AND",
                    "query": "@term@~",
                    "fuzziness": 1,
                    "phrase_slop": 1
                  }
                },
                "filter":[
                  @owner_id_query@
                  @parent_id_query@
                  @permissions_query@
                ]
              }
            },
            "fields": [],
            "_source": false
          }
      """;

  public static final String     OWNER_ID_QUERY                = """
      {
        "terms":{
          "ownerId": @ownerId@
        }
      }
      """;

  public static final String     PARENT_ID_QUERY               = """
      {
        "terms":{
          "parentId": @parentId@
        }
      }
      """;

  public static final String     PERMISSIONS_QUERY             = """
      {
        "terms":{
          "@permissions_field@": [@permissions@]
        }
      }
      """;

  private static final String    OFFSET_REPLACEMENT            = "@offset@";

  private static final String    LIMIT_REPLACEMENT             = "@limit@";

  private static final String    NAME_REPLACEMENT              = "@name_field@";

  private static final String    TERM_REPLACEMENT              = "@term@";

  private static final String    OWNER_ID_REPLACEMENT          = "@ownerId@";

  private static final String    OWNER_ID_QUERY_REPLACEMENT    = "@owner_id_query@";

  private static final String    PARENT_ID_REPLACEMENT         = "@parentId@";

  private static final String    PARENT_ID_QUERY_REPLACEMENT   = "@parent_id_query@";

  private static final String    PERMISSIONS_REPLACEMENT       = "@permissions@";

  private static final String    PERMISSIONS_FIELD_REPLACEMENT = "@permissions_field@";

  private static final String    PERMISSIONS_QUERY_REPLACEMENT = "@permissions_query@";

  @Autowired
  private ElasticSearchingClient client;

  public List<Long> search(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    String esQuery = SEARCH_QUERY_TERM.replace(NAME_REPLACEMENT, "name." + locale.toLanguageTag())
                                      .replace(TERM_REPLACEMENT, filter.getTerm())
                                      .replace(OFFSET_REPLACEMENT, String.valueOf(filter.getOffset()))
                                      .replace(LIMIT_REPLACEMENT, String.valueOf(filter.getLimit()));
    String append = "";
    if (filter.getParentId() > 0) {
      esQuery = esQuery.replace(PARENT_ID_QUERY_REPLACEMENT,
                                PARENT_ID_QUERY.replace(PARENT_ID_REPLACEMENT, String.valueOf(filter.getParentId())));
      append = ",";
    } else if (filter.getOwnerId() > 0) {
      esQuery = esQuery.replace(OWNER_ID_QUERY_REPLACEMENT,
                                OWNER_ID_QUERY.replace(OWNER_ID_REPLACEMENT, String.valueOf(filter.getParentId())));
      esQuery = esQuery.replace(PARENT_ID_QUERY_REPLACEMENT, "");
      append = ",";
    } else {
      esQuery = esQuery.replace(PARENT_ID_QUERY_REPLACEMENT, "");
      esQuery = esQuery.replace(OWNER_ID_QUERY_REPLACEMENT, "");
    }
    if (CollectionUtils.isNotEmpty(identityIds)) {
      esQuery = append + esQuery.replace(PERMISSIONS_QUERY_REPLACEMENT,
                                         PERMISSIONS_QUERY.replace(PERMISSIONS_REPLACEMENT, StringUtils.join(identityIds, ","))
                                                          .replace(PERMISSIONS_FIELD_REPLACEMENT,
                                                                   filter.isLinkPermission() ? "linkPermissionIds" :
                                                                                             "accessPermissionIds"));
    } else {
      esQuery = esQuery.replace(PERMISSIONS_QUERY_REPLACEMENT, "");
    }
    String jsonResponse = this.client.sendRequest(esQuery, CATEGORY_INDEX);
    return buildResult(jsonResponse);
  }

  @SuppressWarnings({ "rawtypes" })
  private List<Long> buildResult(String jsonResponse) {
    JSONParser parser = new JSONParser();

    Map json;
    try {
      json = (Map) parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) {
      return Collections.emptyList();
    }

    List<Long> results = new ArrayList<>();
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    for (Object jsonHit : jsonHits) {
      try {
        JSONObject jsonHitObject = (JSONObject) jsonHit;
        Long id = parseLong(jsonHitObject, "_id");
        results.add(id);
      } catch (Exception e) {
        LOG.warn("Error processing category search result item, ignore it from results", e);
      }
    }
    return results;
  }

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }

}
