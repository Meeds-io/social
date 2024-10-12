/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.social.core.search;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.tag.TagService;

import io.meeds.social.core.search.model.SpaceSearchFilter;
import io.meeds.social.core.search.model.SpaceSearchResult;
import io.meeds.social.space.constant.SpaceMembershipStatus;

import lombok.Getter;

public class SpaceSearchConnector {

  public static final String           PERMISSION_FIELD_REDACTOR     = "redactor";

  public static final String           PERMISSION_FIELD_PUBLISHER    = "publisher";

  public static final String           PERMISSION_FIELD_INVITED      = "invited";

  public static final String           PERMISSION_FIELD_PENDING      = "pending";

  public static final String           PERMISSION_FIELD_MANAGER      = "manager";

  public static final String           PERMISSION_FIELD_MEMBER       = "member";

  public static final String           SEARCH_QUERY_FILE_PATH_PARAM  = "query.file.path";

  public static final String           SEARCH_COUNT_FILE_PATH_PARAM  = "count.file.path";

  private static final Log             LOG                           = ExoLogger.getLogger(SpaceSearchConnector.class);

  private static final String          SEARCH_QUERY_TERM             = """
      "must":{
        "query_string":{
          "fields": ["displayName", "description"],
          "default_operator": "AND",
          "query": "@term@~",
          "fuzziness": 1,
          "phrase_slop": 1
        }
      },
            """;

  private static final String          SEARCH_QUERY_WITH_PHRASE      = """
      "must":{
        "query_string":{
          "fields": ["displayName", "description"],
          "default_operator": "AND",
          "query": "(@term@) OR (\\"@phrase@\\"~)^5",
          "fuzziness": 1,
          "phrase_slop": 1
        }
      },
      """;

  public static final String           PERMISSIONS_QUERY             = """
      {
        "terms":{
          "@permissions_field@": @permissions@
        }
      }
      """;

  private static final String          TERM_REPLACEMENT              = "@term@";

  private static final String          PHRASE_REPLACEMENT            = "@phrase@";

  private static final String          PERMISSIONS_REPLACEMENT       = "@permissions@";

  private static final String          PERMISSIONS_FIELD_REPLACEMENT = "@permissions_field@";

  private final ConfigurationManager   configurationManager;

  private final ElasticSearchingClient client;

  private String                       index;

  private String                       searchQueryFilePath;

  private String                       countQueryFilePath;

  private String                       searchQuery;

  private String                       countQuery;

  @Getter
  private boolean                      enabled;

  public SpaceSearchConnector(ConfigurationManager configurationManager,
                              ElasticSearchingClient client,
                              InitParams initParams) {
    this.configurationManager = configurationManager;
    this.client = client;

    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");

    this.searchQueryFilePath = initParams.getValueParam(SEARCH_QUERY_FILE_PATH_PARAM).getValue();
    this.searchQuery = retrieveQueryFromFile(searchQueryFilePath);

    this.countQueryFilePath = initParams.getValueParam(SEARCH_COUNT_FILE_PATH_PARAM).getValue();
    this.countQuery = retrieveQueryFromFile(countQueryFilePath);

    String enabledParam = param.getProperty("enabled");
    this.enabled = enabledParam == null || StringUtils.equals(enabledParam, "true");
  }

  public List<SpaceSearchResult> search(SpaceSearchFilter filter,
                                        long offset,
                                        long limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must be positive");
    }
    if (limit < 0) {
      throw new IllegalArgumentException("Limit must be positive");
    }
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    if (StringUtils.isBlank(filter.getTerm())
        && !filter.isFavorites()
        && CollectionUtils.isEmpty(filter.getTagNames())) {
      throw new IllegalArgumentException("Filter term is mandatory");
    }

    Map<String, List<String>> metadataFilters = buildMetadatasFilter(filter);
    String esQuery = buildQueryStatement(filter, metadataFilters, retrieveSearchQuery(), offset, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return buildResult(jsonResponse);
  }

  public int count(SpaceSearchFilter filter) {
    if (filter == null) {
      throw new IllegalArgumentException("Filter is mandatory");
    }
    if (StringUtils.isBlank(filter.getTerm())
        && !filter.isFavorites()
        && CollectionUtils.isEmpty(filter.getTagNames())) {
      throw new IllegalArgumentException("Filter term is mandatory");
    }

    Map<String, List<String>> metadataFilters = buildMetadatasFilter(filter);
    String esQuery = buildQueryStatement(filter, metadataFilters, retrieveCountQuery(), 0, 0);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return buildCount(jsonResponse);
  }

  private String buildQueryStatement(SpaceSearchFilter filter,
                                     Map<String, List<String>> metadataFilters,
                                     String query,
                                     long offset,
                                     long limit) {
    String termQuery = buildTermQueryStatement(filter.getTerm());
    String favoriteQuery = buildFavoriteQueryStatement(metadataFilters.get(FavoriteService.METADATA_TYPE.getName()));
    String permissionsQuery = buildPermissionsQuery(filter);
    String tagsQuery = buildTagsQueryStatement(metadataFilters.get(TagService.METADATA_TYPE.getName()));
    return query.replace("@term_query@",
                         termQuery)
                .replace("@favorite_query@",
                         favoriteQuery)
                .replace("@tags_query@",
                         tagsQuery)
                .replace("@permissions_query@",
                         StringUtils.isBlank(favoriteQuery) ? permissionsQuery : String.format(",%n%s", permissionsQuery))
                .replace("@offset@",
                         String.valueOf(offset))
                .replace("@limit@",
                         String.valueOf(limit));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<SpaceSearchResult> buildResult(String jsonResponse) {
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

    List<SpaceSearchResult> results = new ArrayList<>();
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    for (Object jsonHit : jsonHits) {
      try {
        SpaceSearchResult spaceSearchResult = new SpaceSearchResult();

        JSONObject jsonHitObject = (JSONObject) jsonHit;
        Long id = parseLong(jsonHitObject, "_id");
        spaceSearchResult.setId(id);

        JSONObject highlightSource = (JSONObject) jsonHitObject.get("highlight");
        if (highlightSource != null) {
          JSONArray nameExcepts = (JSONArray) highlightSource.get("displayName");
          if (nameExcepts != null) {
            String[] nameExceptsArray = (String[]) nameExcepts.toArray(new String[0]);
            spaceSearchResult.setNameExcerpts(Arrays.asList(nameExceptsArray));
          }
          JSONArray descriptionExcepts = (JSONArray) highlightSource.get("description");
          if (descriptionExcepts != null) {
            String[] descriptionExceptsArray = (String[]) descriptionExcepts.toArray(new String[0]);
            spaceSearchResult.setDescriptionExcerpts(Arrays.asList(descriptionExceptsArray));
          }
        }
        results.add(spaceSearchResult);
      } catch (Exception e) {
        LOG.warn("Error processing space search result item, ignore it from results", e);
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

  private String retrieveSearchQuery() {
    if (StringUtils.isBlank(this.searchQuery) || PropertyManager.isDevelopping()) {
      this.searchQuery = retrieveQueryFromFile(searchQueryFilePath);
    }
    return this.searchQuery;
  }

  private String retrieveCountQuery() {
    if (StringUtils.isBlank(this.countQuery) || PropertyManager.isDevelopping()) {
      this.countQuery = retrieveQueryFromFile(countQueryFilePath);
    }
    return this.countQuery;
  }

  private String retrieveQueryFromFile(String filePath) {
    try {
      InputStream inputStream = this.configurationManager.getInputStream(filePath);
      return IOUtil.getStreamContentAsString(inputStream);
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving search query from file: " + filePath, e);
    }
  }

  private String removeSpecialCharacters(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").replace("'", " ");
    return string;
  }

  private Map<String, List<String>> buildMetadatasFilter(SpaceSearchFilter filter) {
    Map<String, List<String>> metadataFilters = new HashMap<>();
    if (filter.isFavorites()) {
      metadataFilters.put(FavoriteService.METADATA_TYPE.getName(), Collections.singletonList(String.valueOf(filter.getUserIdentityId())));
    }
    if (CollectionUtils.isNotEmpty(filter.getTagNames())) {
      metadataFilters.put(TagService.METADATA_TYPE.getName(), filter.getTagNames());
    }
    return metadataFilters;
  }

  private String buildFavoriteQueryStatement(List<String> values) {
    if (CollectionUtils.isEmpty(values)) {
      return "";
    }
    return new StringBuilder().append("{\"terms\":{")
                              .append("\"metadatas.favorites.metadataName.keyword\": [\"")
                              .append(StringUtils.join(values, "\",\""))
                              .append("\"]}}")
                              .toString();
  }

  private String buildTagsQueryStatement(List<String> values) {
    if (CollectionUtils.isEmpty(values)) {
      return "";
    }
    List<String> tagsQueryParts = values.stream()
                                        .map(value -> new StringBuilder().append("{\"term\": {\n")
                                                                         .append("            \"metadatas.tags.metadataName.keyword\": {\n")
                                                                         .append("              \"value\": \"")
                                                                         .append(value)
                                                                         .append("\",\n")
                                                                         .append("              \"case_insensitive\":true\n")
                                                                         .append("            }\n")
                                                                         .append("          }}")
                                                                         .toString())
                                        .toList();
    return new StringBuilder().append(",\"should\": [\n")
                              .append(StringUtils.join(tagsQueryParts, ","))
                              .append("      ],\n")
                              .append("      \"minimum_should_match\": 1")
                              .toString();
  }

  private String buildTermQueryStatement(String phrase) {
    if (StringUtils.isBlank(phrase)) {
      return "";
    }
    phrase = removeSpecialCharacters(phrase);

    if (StringUtils.contains(phrase, " ")) {// If multiple words
      String terms = Arrays.stream(StringUtils.split(phrase, " ")).map(keyword -> {
        String keywordTrim = keyword.trim();
        if (keywordTrim.length() > 4) {// Only words with 5 letters or greater
          return keywordTrim + "~";
        } else {
          return keywordTrim;
        }
      }).reduce("", (key1, key2) -> key1 + " " + key2);
      return SEARCH_QUERY_WITH_PHRASE.replace(TERM_REPLACEMENT, terms)
                                     .replace(PHRASE_REPLACEMENT, phrase);
    } else {
      return SEARCH_QUERY_TERM.replace(TERM_REPLACEMENT, phrase);
    }
  }

  private String buildPermissionsQuery(SpaceSearchFilter filter) {
    String permissionField = getPermissionField(filter);
    String username = filter.getUsername();

    return PERMISSIONS_QUERY.replace(PERMISSIONS_FIELD_REPLACEMENT,
                                     permissionField == null ? "permissions" : permissionField)
                            .replace(PERMISSIONS_REPLACEMENT,
                                     permissionField == null ? String.format("[\"all\", \"%s\"]", username) :
                                                             String.format("[\"%s\"]", username));
  }

  private String getPermissionField(SpaceSearchFilter filter) {
    if (filter.isFavorites()) {
      return PERMISSION_FIELD_MEMBER;
    }
    SpaceMembershipStatus status = filter.getStatus();
    if (status == null) {
      return null;
    }
    return switch (status) {
    case MEMBER:
      yield PERMISSION_FIELD_MEMBER;
    case MANAGER:
      yield PERMISSION_FIELD_MANAGER;
    case PENDING:
      yield PERMISSION_FIELD_PENDING;
    case INVITED:
      yield PERMISSION_FIELD_INVITED;
    case PUBLISHER:
      yield PERMISSION_FIELD_PUBLISHER;
    case REDACTOR:
      yield PERMISSION_FIELD_REDACTOR;
    default:
      yield null;
    };
  }

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }

}
