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
package io.meeds.social.category.storage.elasticsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.exoplatform.social.core.storage.impl.StorageUtils;

import io.meeds.social.category.model.CategorySearchFilter;

@Component
public class CategorySearchConnector {

  private static final String    CATEGORY_INDEX                   = "category_alias";

  private static final Log       LOG                              = ExoLogger.getLogger(CategorySearchConnector.class);

  private static final String    SEARCH_QUERY_TERM                = """
          {
            "from": "@offset@",
            "size": "@limit@",
            @sort_query@
            "query":{
              "bool":{
                @term_query@
                "must":[
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

  private static final String    COUNT_QUERY_TERM                 = """
          {
            "query":{
              "bool":{
                @term_query@
                "must":[
                  @owner_id_query@
                  @parent_id_query@
                  @permissions_query@
                ]
              }
            }
          }
      """;

  /**
   * Term query of the searched text on the name of the locale, analyzed by the
   * field's search analyzer so that the tokens searched are the ngram tokens
   * the index holds, every token required. A text with no token matches
   * nothing. The text is written in a JSON string only, so no character of it
   * is query syntax.
   */
  public static final String     TERM_QUERY                       = """
        "filter":{
          "match":{
            "@name_field@":{
              "query": "@term@",
              "operator": "and"
            }
          }
        },
      """;

  public static final String     OWNER_ID_QUERY                   = """
      {
        "term":{
          "ownerId": @ownerId@
        }
      }
      """;

  public static final String     PARENT_ID_QUERY                  = """
      {
        "term":{
          "parentId": @parentId@
        }
      }
      """;

  public static final String     PERMISSIONS_QUERY                = """
      {
        "terms":{
          "@permissions_field@": [@permissions@]
        }
      }
      """;

  public static final String     SORT_QUERY_BY_NAME               = """
      "sort" : [
        { "@sort_field@.raw" : "@sort_direction@" }
      ],
      """;

  public static final String     SORT_QUERY_BY_SCORE              = """
      "sort" : [
        "_score"
      ],
          """;

  private static final String    OFFSET_NAME                      = "offset";

  private static final String    LIMIT_NAME                       = "limit";

  private static final String    NAME_REPLACEMENT                 = "@name_field@";

  private static final String    TERM_REPLACEMENT                 = "@term@";

  private static final String    TERM_QUERY_NAME                  = "term_query";

  private static final String    SORT_QUERY_NAME                  = "sort_query";

  private static final String    OWNER_ID_REPLACEMENT             = "@ownerId@";

  private static final String    OWNER_ID_QUERY_NAME              = "owner_id_query";

  private static final String    PARENT_ID_REPLACEMENT            = "@parentId@";

  private static final String    PARENT_ID_QUERY_NAME             = "parent_id_query";

  private static final String    PERMISSIONS_REPLACEMENT          = "@permissions@";

  private static final String    PERMISSIONS_FIELD_REPLACEMENT    = "@permissions_field@";

  private static final String    PERMISSIONS_QUERY_NAME           = "permissions_query";

  private static final String    SORT_FIELD_QUERY_REPLACEMENT     = "@sort_field@";

  private static final String    SORT_DIRECTION_QUERY_REPLACEMENT = "@sort_direction@";

  private static final String    STRING_VALUE_FORMAT              = "\"%s\"";

  private static final String    NAME_FORMAT                      = "name-%s";

  private static final int       DEFAULT_LIMIT                    = 100;

  @Autowired
  private ElasticSearchingClient client;

  public List<Long> search(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    String esQuery = buildSearchQuery(SEARCH_QUERY_TERM, filter, identityIds, locale);
    String jsonResponse = this.client.sendRequest(esQuery, CATEGORY_INDEX);
    return buildResult(jsonResponse);
  }

  public int count(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    String esQuery = buildSearchQuery(COUNT_QUERY_TERM, filter, identityIds, locale);
    String jsonResponse = this.client.countRequest(esQuery, CATEGORY_INDEX);
    return buildCount(jsonResponse);
  }

  private String buildSearchQuery(String queryBase, CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    // each fragment is built on its own, then the base is filled in one pass:
    // a placeholder name typed in the term stays literal
    Map<String, String> values = new HashMap<>();
    values.put(OFFSET_NAME, String.valueOf(filter.getOffset()));
    values.put(LIMIT_NAME, String.valueOf(filter.getLimit() < 1 ? DEFAULT_LIMIT : filter.getLimit()));
    String append = "";
    if (filter.getParentId() > 0) {
      values.put(PARENT_ID_QUERY_NAME,
                 PARENT_ID_QUERY.replace(PARENT_ID_REPLACEMENT, String.format(STRING_VALUE_FORMAT, filter.getParentId())));
      append = ",";
    } else if (filter.getOwnerId() > 0) {
      values.put(OWNER_ID_QUERY_NAME,
                 OWNER_ID_QUERY.replace(OWNER_ID_REPLACEMENT, String.format(STRING_VALUE_FORMAT, filter.getOwnerId())));
      append = ",";
    }
    if (CollectionUtils.isNotEmpty(identityIds) && filter.isLinkPermission()) {
      values.put(PERMISSIONS_QUERY_NAME,
                 append + PERMISSIONS_QUERY.replace(PERMISSIONS_REPLACEMENT,
                                                    String.format(STRING_VALUE_FORMAT,
                                                                  StringUtils.join(identityIds, "\",\"")))
                                           .replace(PERMISSIONS_FIELD_REPLACEMENT, "linkPermissions"));
    }
    if (StringUtils.isNotBlank(filter.getTerm())) {
      values.put(TERM_QUERY_NAME,
                 TERM_QUERY.replace(NAME_REPLACEMENT, String.format(NAME_FORMAT, locale.toLanguageTag()))
                           .replace(TERM_REPLACEMENT, StorageUtils.escapeSearchText(filter.getTerm())));
    }
    if (filter.isSortByName()) {
      values.put(SORT_QUERY_NAME,
                 SORT_QUERY_BY_NAME.replace(SORT_FIELD_QUERY_REPLACEMENT, String.format(NAME_FORMAT, locale.toLanguageTag()))
                                   .replace(SORT_DIRECTION_QUERY_REPLACEMENT, "asc"));
    } else {
      values.put(SORT_QUERY_NAME, SORT_QUERY_BY_SCORE);
    }
    return StorageUtils.fillQueryTemplate(queryBase, values);
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

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private int buildCount(String jsonResponse) {
    JSONParser parser = new JSONParser();
    try {
      Map json = (Map) parser.parse(jsonResponse);
      String countString = json.getOrDefault("count", "0").toString();
      return Integer.parseInt(countString);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }
  }

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }

}
