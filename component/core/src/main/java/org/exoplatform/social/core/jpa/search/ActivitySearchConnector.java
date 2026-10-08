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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.exoplatform.social.core.activity.filter.ActivitySearchFilter;
import org.exoplatform.social.core.activity.model.ActivitySearchResult;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.tag.TagService;

public class ActivitySearchConnector {

  private static final Log              LOG                          =
                                            ExoLogger.getLogger(ActivitySearchConnector.class);

  public static final String            SEARCH_QUERY_FILE_PATH_PARAM = "query.file.path";

  /**
   * Term query of the searched text, analyzed by each field's own analyzer so
   * that the tokens searched are the ones the index holds: every token is
   * required and the last one, the one being typed, is a prefix
   * ({@code bool_prefix}); one edit is allowed on a token of two letters or
   * more when the text is one word ({@link #SINGLE_WORD_FUZZINESS}), of five
   * letters or more otherwise ({@link #MULTI_WORD_FUZZINESS}); the whole text
   * as a phrase, with one position of slop, is boosted. A text with no token
   * matches nothing. The text is written in a JSON string only, so no
   * character of it is query syntax.
   */
  public static final String            SEARCH_QUERY_TERM            = """
      "must":{
        "bool":{
          "should":[
            {
              "multi_match":{
                "query": "@term@",
                "fields": ["body", "posterName"],
                "type": "bool_prefix",
                "operator": "and"
              }
            },
            {
              "multi_match":{
                "query": "@term@",
                "fields": ["body", "posterName"],
                "type": "best_fields",
                "operator": "and",
                "fuzziness": "@fuzziness@"
              }
            },
            {
              "multi_match":{
                "query": "@term@",
                "fields": ["body", "posterName"],
                "type": "phrase",
                "slop": 1,
                "boost": 5
              }
            }
          ],
          "minimum_should_match": 1
        }
      },
      """;

  public static final String            CATEGORY_IDS_QUERY           = """
      {
        "terms":{
          "categoryId": [@categoryIds@]
        }
      }
      """;

  public static final String            DEFAULT_SORTING_QUERY        = """
          {
            "_score": {
              "order": "desc"
            }
          }
      """;

  public static final String            SORTING_QUERY                = """
          {
            "@sortField@": {
              "order": "@sortOrder@"
            }
          },
          "_score"
      """;

  /**
   * One edit allowed on each token of a one-word text, from two letters: a
   * single letter is matched exactly (and as a prefix when it ends the text)
   */
  public static final String            SINGLE_WORD_FUZZINESS        = "AUTO:2,1000";

  /** One edit allowed on the tokens of five letters or more of a longer text */
  public static final String            MULTI_WORD_FUZZINESS         = "AUTO:5,1000";

  private static final String           TERM_NAME                    = "term";

  private static final String           FUZZINESS_NAME               = "fuzziness";

  private final ConfigurationManager    configurationManager;                                  // NOSONAR

  private final ActivitySearchProcessor activitySearchProcessor;                               // NOSONAR

  private final IdentityManager         identityManager;

  private final ActivityStorage         activityStorage;

  private final ElasticSearchingClient  client;

  private String                        index;

  private String                        searchQueryFilePath;

  private String                        searchQuery;

  public ActivitySearchConnector(ActivitySearchProcessor activitySearchProcessor,
                                 IdentityManager identityManager,
                                 ActivityStorage activityStorage,
                                 ConfigurationManager configurationManager,
                                 ElasticSearchingClient client,
                                 InitParams initParams) {
    this.configurationManager = configurationManager;
    this.activitySearchProcessor = activitySearchProcessor;
    this.identityManager = identityManager;
    this.activityStorage = activityStorage;
    this.client = client;

    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    if (initParams.containsKey(SEARCH_QUERY_FILE_PATH_PARAM)) {
      searchQueryFilePath = initParams.getValueParam(SEARCH_QUERY_FILE_PATH_PARAM).getValue();
      try {
        searchQuery = retrieveSearchQueryFromFile(searchQueryFilePath);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch search query from path {}", searchQueryFilePath, e);
      }
    }
  }

  public List<ActivitySearchResult> search(Identity viewerIdentity,
                                           ActivitySearchFilter filter,
                                           long offset,
                                           long limit) {
    if (viewerIdentity == null) {
      throw new IllegalArgumentException("Viewer identity is mandatory");
    }
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

    return searchActivities(viewerIdentity, filter, offset, limit);
  }

  private List<ActivitySearchResult> searchActivities(Identity viewerIdentity,
                                                      ActivitySearchFilter filter,
                                                      long offset,
                                                      long limit) {
    Set<Long> streamFeedOwnerIds = activityStorage.getStreamFeedOwnerIds(viewerIdentity);
    if (!CollectionUtils.isEmpty(filter.getSpaceIdentityIds())) {
      streamFeedOwnerIds.retainAll(filter.getSpaceIdentityIds());
      if (streamFeedOwnerIds.isEmpty()) {
        return Collections.emptyList();
      }
    }
    Map<String, List<String>> metadataFilters = buildMetadatasFilter(filter, viewerIdentity);
    String esQuery = buildQueryStatement(streamFeedOwnerIds, metadataFilters, filter, offset, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return buildResult(jsonResponse, viewerIdentity, streamFeedOwnerIds);
  }

  private String buildQueryStatement(Set<Long> streamFeedOwnerIds,
                                     Map<String, List<String>> metadataFilters,
                                     ActivitySearchFilter filter,
                                     long offset,
                                     long limit) {
    String termQuery = buildTermQueryStatement(filter.getTerm());
    String favoriteQuery = buildFavoriteQueryStatement(metadataFilters.get(FavoriteService.METADATA_TYPE.getName()));
    String tagsQuery = buildTagsQueryStatement(metadataFilters.get(TagService.METADATA_TYPE.getName()));
    String categoryQuery = buildCategoryIdQueryStatement(filter);
    String sortQuery = buildSortQueryStatement(filter);
    // one pass: a placeholder name typed in the term or a tag stays literal
    Map<String, String> values = new HashMap<>();
    values.put("term_query", termQuery);
    values.put("favorite_query", favoriteQuery);
    values.put("tags_query", tagsQuery);
    values.put("category_query", categoryQuery);
    values.put("permissions", StringUtils.join(streamFeedOwnerIds, ","));
    values.put("sortQuery", sortQuery);
    values.put("offset", String.valueOf(offset));
    values.put("limit", String.valueOf(limit));
    return StorageUtils.fillQueryTemplate(retrieveSearchQuery(), values);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<ActivitySearchResult> buildResult(String jsonResponse, Identity viewerIdentity, Set<Long> streamFeedOwnerIds) {
    LOG.debug("Search Query response from ES : {} ", jsonResponse);

    List<ActivitySearchResult> results = new ArrayList<>();
    JSONParser parser = new JSONParser();

    Map json;
    try {
      json = (Map) parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) {
      return results;
    }

    //
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    for (Object jsonHit : jsonHits) {
      try {
        ActivitySearchResult activitySearchResult = new ActivitySearchResult();

        JSONObject jsonHitObject = (JSONObject) jsonHit;
        JSONObject hitSource = (JSONObject) jsonHitObject.get("_source");
        Long id = parseLong(hitSource, "id");
        Long posterId = parseLong(hitSource, "posterId");
        Long parentId = parseLong(hitSource, "parentId");
        Long streamOwner = parseLong(hitSource, "streamOwner");
        if (!streamFeedOwnerIds.contains(streamOwner) && !streamFeedOwnerIds.contains(posterId)) {
          LOG.warn("Activity '{}' is returned in search result while it's not permitted to user {}. Ignore it.",
                   id,
                   viewerIdentity.getId());
          continue;
        }
        Long postedTime = parseLong(hitSource, "postedTime");
        Long lastUpdatedTime = (Long) hitSource.get("lastUpdatedDate");
        String body = (String) hitSource.get("body");
        String type = (String) hitSource.get("type");
        JSONObject highlightSource = (JSONObject) jsonHitObject.get("highlight");
        List<String> excerpts = new ArrayList<>();
        if (highlightSource != null) {
          JSONArray bodyExcepts = (JSONArray) highlightSource.get("body");
          if (bodyExcepts != null) {
            String[] bodyExceptsArray = (String[]) bodyExcepts.toArray(new String[0]);
            excerpts = Arrays.asList(bodyExceptsArray);
          }
        }

        if (parentId == null) {
          // Activity
          activitySearchResult.setId(id);
          if (lastUpdatedTime != null) {
            activitySearchResult.setLastUpdatedTime(lastUpdatedTime);
          }
          if (postedTime != null) {
            activitySearchResult.setPostedTime(postedTime);
          }
          if (streamOwner != null) {
            Identity streamOwnerIdentity = identityManager.getIdentity(streamOwner.toString());
            activitySearchResult.setStreamOwner(streamOwnerIdentity);
          }
          if (posterId != null) {
            Identity posterIdentity = identityManager.getIdentity(posterId.toString());
            activitySearchResult.setPoster(posterIdentity);
          }
          activitySearchResult.setBody(body);
          activitySearchResult.setType(type);
          activitySearchResult.setExcerpts(excerpts);
        } else {
          // Comment or sub comment
          ActivitySearchResult commentSearchResult = new ActivitySearchResult();
          commentSearchResult.setId(id);
          if (lastUpdatedTime != null) {
            commentSearchResult.setLastUpdatedTime(lastUpdatedTime);
          }
          if (postedTime != null) {
            commentSearchResult.setPostedTime(postedTime);
          }
          if (streamOwner != null) {
            Identity streamOwnerIdentity = identityManager.getIdentity(streamOwner.toString());
            commentSearchResult.setStreamOwner(streamOwnerIdentity);
          }
          if (posterId != null) {
            Identity posterIdentity = identityManager.getIdentity(posterId.toString());
            commentSearchResult.setPoster(posterIdentity);
          }
          commentSearchResult.setBody(body);
          commentSearchResult.setType(type);
          commentSearchResult.setExcerpts(excerpts);
          activitySearchResult.setComment(commentSearchResult);
          activitySearchProcessor.formatSearchResult(commentSearchResult);

          transformActivityToResult(activitySearchResult, parentId);
        }

        results.add(activitySearchResult);
      } catch (Exception e) {
        LOG.warn("Error processing activity search result item, ignore it from results", e);
      }
    }
    return results;
  }

  private void transformActivityToResult(ActivitySearchResult activitySearchResult, Long parentId) {
    ExoSocialActivity activity = activityStorage.getActivity(parentId.toString());

    // Activity
    activitySearchResult.setType(activity.getType());
    activitySearchResult.setId(Long.parseLong(activity.getId()));
    if (activity.getUpdated() != null) {
      activitySearchResult.setLastUpdatedTime(activity.getUpdated().getTime());
    }

    if (activity.getPostedTime() != null) {
      activitySearchResult.setPostedTime(activity.getPostedTime());
    }
    ActivityStream activityStream = activity.getActivityStream();
    if (activityStream != null) {
      String prettyId = activityStream.getPrettyId();
      String providerId = activityStream.getType().getProviderId();

      Identity streamOwnerIdentity = identityManager.getOrCreateIdentity(providerId, prettyId);
      activitySearchResult.setStreamOwner(streamOwnerIdentity);
    }
    if (activity.getPosterId() != null) {
      Identity posterIdentity = identityManager.getIdentity(activity.getPosterId());
      activitySearchResult.setPoster(posterIdentity);
    }
    if (StringUtils.isNotBlank(activity.getTitle())) {
      activitySearchResult.setBody(activity.getTitle());
    } else {
      activitySearchResult.setBody(activity.getBody());
    }
    activitySearchProcessor.formatSearchResult(activitySearchResult);
  }

  private String retrieveSearchQuery() {
    if (StringUtils.isBlank(this.searchQuery) || PropertyManager.isDevelopping()) {
      this.searchQuery = retrieveSearchQueryFromFile(searchQueryFilePath);
    }
    return this.searchQuery;
  }

  private String retrieveSearchQueryFromFile(String filePath) {
    try {
      InputStream queryFileIS = this.configurationManager.getInputStream(filePath);
      return IOUtil.getStreamContentAsString(queryFileIS);
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving search query from file: " + filePath, e);
    }
  }

  private Map<String, List<String>> buildMetadatasFilter(ActivitySearchFilter filter, Identity viewerIdentity) {
    Map<String, List<String>> metadataFilters = new HashMap<>();
    if (filter.isFavorites()) {
      metadataFilters.put(FavoriteService.METADATA_TYPE.getName(), Collections.singletonList(viewerIdentity.getId()));
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
                              .append("\"]}},")
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
                                                                         .append(StorageUtils.escapeJsonValue(value))
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

  private String buildCategoryIdQueryStatement(ActivitySearchFilter filter) {
    if (CollectionUtils.isNotEmpty(filter.getCategoryIds())) {
      return CATEGORY_IDS_QUERY.replace("@categoryIds@", StringUtils.join(filter.getCategoryIds(), ","));
    } else {
      return StringUtils.EMPTY;
    }
  }

  private String buildTermQueryStatement(String phrase) {
    if (StringUtils.isBlank(phrase)) {
      return "";
    }
    String text = StorageUtils.normalizeSearchText(phrase);
    String fuzziness = StringUtils.containsWhitespace(text) ? MULTI_WORD_FUZZINESS : SINGLE_WORD_FUZZINESS;
    // one pass: a placeholder name typed in the text stays literal
    return StorageUtils.fillQueryTemplate(SEARCH_QUERY_TERM,
                                          Map.of(TERM_NAME, StorageUtils.escapeJsonValue(text), FUZZINESS_NAME, fuzziness));
  }

  private String buildSortQueryStatement(ActivitySearchFilter filter) {
    String sortFiled = filter.getSortField();
    String sortDirection = filter.getSortDirection();

    if (StringUtils.isBlank(sortFiled)) {
      return DEFAULT_SORTING_QUERY;
    }

    return switch (sortFiled) {
    case "date" -> SORTING_QUERY.replace("@sortField@", "lastUpdatedDate").replace("@sortOrder@", sortDirection);
    default -> SORTING_QUERY.replace("@sortField@", sortFiled).replace("@sortOrder@", sortDirection);
    };
  }

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }
}
