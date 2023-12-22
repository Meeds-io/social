package org.exoplatform.social.core.jpa.search;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.tag.TagService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ActivitySearchConnector {

  private static final Log              LOG                          =
                                            ExoLogger.getLogger(ActivitySearchConnector.class);

  public static final String            SEARCH_QUERY_FILE_PATH_PARAM = "query.file.path";

  public static final String            SEARCH_QUERY_TERM            = "\"must\":{" +
      "  \"query_string\":{" +
      "    \"fields\": [\"body\", \"posterName\"]," +
      "    \"default_operator\": \"AND\"," +
      "    \"query\": \"@term@~\"," +
      "    \"fuzziness\": 1," +
      "    \"phrase_slop\": 1" +
      "  }" +
      "},";

  public static final String            SEARCH_QUERY_WITH_PHRASE     = "\"must\":{" +
      "  \"query_string\":{" +
      "    \"fields\": [\"body\", \"posterName\"]," +
      "    \"default_operator\": \"AND\"," +
      "    \"query\": \"(@term@) OR (\\\"@phrase@\\\"~)^5\"," +
      "    \"fuzziness\": 1," +
      "    \"phrase_slop\": 1" +
      "  }" +
      "},";

  private static final String           TERM_REPLACEMENT             = "@term@";

  private static final String           PHRASE_REPLACEMENT           = "@phrase@";

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
    return retrieveSearchQuery().replace("@term_query@", termQuery)
                                .replace("@favorite_query@", favoriteQuery)
                                .replace("@tags_query@", tagsQuery)
                                .replace("@permissions@", StringUtils.join(streamFeedOwnerIds, ","))
                                .replace("@offset@", String.valueOf(offset))
                                .replace("@limit@", String.valueOf(limit));
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

  private String removeSpecialCharacters(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").replace("'", " ");
    return string;
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
                                                                         .append(value)
                                                                         .append("\",\n")
                                                                         .append("              \"case_insensitive\":true\n")
                                                                         .append("            }\n")
                                                                         .append("          }}")
                                                                         .toString())
                                        .collect(Collectors.toList());
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

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }
}
