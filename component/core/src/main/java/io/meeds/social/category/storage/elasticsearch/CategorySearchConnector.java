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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
                  @must_queries@
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
                  @must_queries@
                ]
              }
            }
          }
      """;

  public static final String     TERM_QUERY                       = """
        "filter":{
          "query_string":{
            "fields": ["@name_field@"],
            "default_operator": "AND",
            "query": "@term@"
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

  public static final String     IDS_QUERY                        = """
      {
        "ids":{
          "values": [@ids@]
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

  private static final String    OFFSET_REPLACEMENT               = "@offset@";

  private static final String    LIMIT_REPLACEMENT                = "@limit@";

  private static final String    NAME_REPLACEMENT                 = "@name_field@";

  private static final String    TERM_REPLACEMENT                 = "@term@";

  private static final String    TERM_QUERY_REPLACEMENT           = "@term_query@";

  private static final String    SORT_QUERY_REPLACEMENT           = "@sort_query@";

  private static final String    OWNER_ID_REPLACEMENT             = "@ownerId@";

  private static final String    PARENT_ID_REPLACEMENT            = "@parentId@";

  private static final String    PERMISSIONS_REPLACEMENT          = "@permissions@";

  private static final String    PERMISSIONS_FIELD_REPLACEMENT    = "@permissions_field@";

  private static final String    MUST_QUERIES_REPLACEMENT         = "@must_queries@";

  private static final String    IDS_REPLACEMENT                  = "@ids@";

  private static final String    SORT_FIELD_QUERY_REPLACEMENT     = "@sort_field@";

  private static final String    SORT_DIRECTION_QUERY_REPLACEMENT = "@sort_direction@";

  private static final String    STRING_VALUE_FORMAT              = "\"%s\"";

  private static final String    NAME_FORMAT                      = "name-%s";

  private static final int       DEFAULT_LIMIT                    = 100;

  @Autowired
  private ElasticSearchingClient client;

  public List<Long> search(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    return search(filter, identityIds, null, locale);
  }

  /**
   * @param filter search filter
   * @param identityIds identities of the user, used for link permissions
   * @param categoryIds when not null, only these categories may match
   * @param locale locale of the searched and sorted name
   * @return the matching category ids
   */
  public List<Long> search(CategorySearchFilter filter, List<Long> identityIds, List<Long> categoryIds, Locale locale) {
    String esQuery = buildSearchQuery(SEARCH_QUERY_TERM, filter, identityIds, categoryIds, locale);
    String jsonResponse = this.client.sendRequest(esQuery, CATEGORY_INDEX);
    return buildResult(jsonResponse);
  }

  public int count(CategorySearchFilter filter, List<Long> identityIds, Locale locale) {
    String esQuery = buildSearchQuery(COUNT_QUERY_TERM, filter, identityIds, null, locale);
    String jsonResponse = this.client.countRequest(esQuery, CATEGORY_INDEX);
    return buildCount(jsonResponse);
  }

  String buildSearchQuery(String queryBase,
                          CategorySearchFilter filter,
                          List<Long> identityIds,
                          List<Long> categoryIds,
                          Locale locale) {
    String esQuery = queryBase.replace(OFFSET_REPLACEMENT, String.valueOf(filter.getOffset()))
                              .replace(LIMIT_REPLACEMENT,
                                       String.valueOf(filter.getLimit() < 1 ? DEFAULT_LIMIT : filter.getLimit()));
    List<String> mustQueries = new ArrayList<>();
    if (filter.getParentId() > 0) {
      mustQueries.add(PARENT_ID_QUERY.replace(PARENT_ID_REPLACEMENT, String.format(STRING_VALUE_FORMAT, filter.getParentId())));
    } else if (filter.getOwnerId() > 0) {
      mustQueries.add(OWNER_ID_QUERY.replace(OWNER_ID_REPLACEMENT, String.format(STRING_VALUE_FORMAT, filter.getOwnerId())));
    }
    if (CollectionUtils.isNotEmpty(identityIds) && filter.isLinkPermission()) {
      mustQueries.add(PERMISSIONS_QUERY.replace(PERMISSIONS_REPLACEMENT,
                                                String.format(STRING_VALUE_FORMAT, StringUtils.join(identityIds, "\",\"")))
                                       .replace(PERMISSIONS_FIELD_REPLACEMENT, "linkPermissions"));
    }
    if (categoryIds != null) {
      mustQueries.add(IDS_QUERY.replace(IDS_REPLACEMENT,
                                        categoryIds.stream()
                                                   .map(id -> String.format(STRING_VALUE_FORMAT, id))
                                                   .collect(Collectors.joining(","))));
    }
    esQuery = esQuery.replace(MUST_QUERIES_REPLACEMENT, String.join(",", mustQueries));
    if (StringUtils.isNotBlank(filter.getTerm())) {
      esQuery = esQuery.replace(TERM_QUERY_REPLACEMENT,
                                TERM_QUERY.replace(NAME_REPLACEMENT, String.format(NAME_FORMAT, locale.toLanguageTag()))
                                          .replace(TERM_REPLACEMENT, escape(filter.getTerm())));
    } else {
      esQuery = esQuery.replace(TERM_QUERY_REPLACEMENT, "");
    }
    if (filter.isSortByName()) {
      esQuery = esQuery.replace(SORT_QUERY_REPLACEMENT, SORT_QUERY_BY_NAME);
      esQuery = esQuery.replace(SORT_FIELD_QUERY_REPLACEMENT, String.format(NAME_FORMAT, locale.toLanguageTag()));
      esQuery = esQuery.replace(SORT_DIRECTION_QUERY_REPLACEMENT, "asc");
    } else {
      esQuery = esQuery.replace(SORT_QUERY_REPLACEMENT, SORT_QUERY_BY_SCORE);
    }
    return esQuery;
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

  private String escape(String term) {
    return term.replaceAll("([\\Q+-!():^[]\"{}~*?|&/\\E])", " ").trim();
  }

}
