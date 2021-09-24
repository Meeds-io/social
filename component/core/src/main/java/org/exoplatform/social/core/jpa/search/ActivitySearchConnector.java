package org.exoplatform.social.core.jpa.search;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.favorite.FavoriteService;

public class ActivitySearchConnector {

  private static final Log              LOG                                       =
                                            ExoLogger.getLogger(ActivitySearchConnector.class);

  private static final String           SEARCH_QUERY_FILE_PATH_PARAM              = "query.file.path";

  private static final String           SEARCH_QUERY_BY_IDS_FILE_PATH_PARAM       = "queryByIds.file.path";

  private static final String           SEARCH_EMPTY_QUERY_BY_IDS_FILE_PATH_PARAM = "emptyQueryByIds.file.path";

  private static final int              SEARCH_FAVORITES_HARD_LIMIT               = 1000;

  private final ConfigurationManager    configurationManager;                                                   // NOSONAR

  private final ActivitySearchProcessor activitySearchProcessor;                                                // NOSONAR

  private final MetadataService         metadataService;                                                        // NOSONAR

  private final IdentityManager         identityManager;

  private final ActivityStorage         activityStorage;

  private final ElasticSearchingClient  client;

  private String                        index;

  private String                        searchQueryFilePath;

  private String                        searchQueryByIdsFilePath;

  private String                        searchEmptyQueryByIdsFilePath;

  private String                        searchQuery;

  private String                        searchQueryByIds;

  private String                        searchEmptyQueryByIds;

  public ActivitySearchConnector(ActivitySearchProcessor activitySearchProcessor,
                                 MetadataService metadataService,
                                 IdentityManager identityManager,
                                 ActivityStorage activityStorage,
                                 ConfigurationManager configurationManager,
                                 ElasticSearchingClient client,
                                 InitParams initParams) {
    this.configurationManager = configurationManager;
    this.activitySearchProcessor = activitySearchProcessor;
    this.metadataService = metadataService;
    this.identityManager = identityManager;
    this.activityStorage = activityStorage;
    this.client = client;

    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    if (initParams.containsKey(SEARCH_QUERY_FILE_PATH_PARAM)) {
      searchQueryFilePath = initParams.getValueParam(SEARCH_QUERY_FILE_PATH_PARAM).getValue();
      try {
        searchQuery = retrieveSearchQuery(searchQueryFilePath);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch search query from path {}", searchQueryFilePath, e);
      }
    }
    if (initParams.containsKey(SEARCH_QUERY_BY_IDS_FILE_PATH_PARAM)) {
      searchQueryByIdsFilePath = initParams.getValueParam(SEARCH_QUERY_BY_IDS_FILE_PATH_PARAM).getValue();
      try {
        searchQueryByIds = retrieveSearchQuery(searchQueryByIdsFilePath);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch search query from path {}", searchQueryByIdsFilePath, e);
      }
    }
    if (initParams.containsKey(SEARCH_EMPTY_QUERY_BY_IDS_FILE_PATH_PARAM)) {
      searchEmptyQueryByIdsFilePath = initParams.getValueParam(SEARCH_EMPTY_QUERY_BY_IDS_FILE_PATH_PARAM).getValue();
      try {
        searchEmptyQueryByIds = retrieveSearchQuery(searchEmptyQueryByIdsFilePath);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch search query from path {}", searchEmptyQueryByIdsFilePath, e);
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
    if (StringUtils.isBlank(filter.getTerm()) && !filter.isFavorites()) {
      throw new IllegalArgumentException("Filter term is mandatory");
    }
    Set<Long> streamFeedOwnerIds = activityStorage.getStreamFeedOwnerIds(viewerIdentity);
    List<String> activityIds;
    if (filter.isFavorites()) {
      int favoriteActivitiesLimit = StringUtils.isBlank(filter.getTerm()) ? ((int) (offset + limit))
                                                                          : SEARCH_FAVORITES_HARD_LIMIT;
      activityIds = metadataService.getMetadataObjectIds(FavoriteService.METADATA_TYPE.getName(),
                                                         viewerIdentity.getId(),
                                                         "activity",
                                                         favoriteActivitiesLimit);
      if (CollectionUtils.isEmpty(activityIds)) {
        return Collections.emptyList();
      }
    } else {
      activityIds = Collections.emptyList();
    }
    String esQuery = buildQueryStatement(streamFeedOwnerIds, activityIds, filter, offset, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return buildResult(jsonResponse, viewerIdentity, streamFeedOwnerIds);
  }

  private String buildQueryStatement(Set<Long> streamFeedOwnerIds,
                                     List<String> activityIds,
                                     ActivitySearchFilter filter,
                                     long offset,
                                     long limit) {
    String term = removeSpecialCharacters(filter.getTerm());
    List<String> termsQuery = Arrays.stream(term.split(" ")).filter(StringUtils::isNotBlank).map(word -> {
      word = word.trim();
      if (word.length() > 5) {
        word = word + "~1";
      }
      return word;
    }).collect(Collectors.toList());
    String termQuery = StringUtils.join(termsQuery, " AND ");
    return retrieveSearchQuery(activityIds, term).replace("@term@", term)
                                                 .replace("@term_query@", termQuery)
                                                 .replace("@activityIds@", StringUtils.join(activityIds, ","))
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
        if (!streamFeedOwnerIds.contains(streamOwner)) {
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

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }

  private String retrieveSearchQuery(List<String> activityIds, String term) {
    if (activityIds.isEmpty()) {
      // When this is not about favorites nor any other Metadata selection
      if (StringUtils.isBlank(this.searchQuery) || PropertyManager.isDevelopping()) {
        this.searchQuery = retrieveSearchQuery(searchQueryFilePath);
      }
      return this.searchQuery;
    } else if (StringUtils.isBlank(term)) {
      // When this is about searching favorites without a search term
      if (StringUtils.isBlank(this.searchEmptyQueryByIds) || PropertyManager.isDevelopping()) {
        this.searchEmptyQueryByIds = retrieveSearchQuery(searchEmptyQueryByIdsFilePath);
      }
      return this.searchEmptyQueryByIds;
    } else {
      // When this is about searching favorites with a search term
      if (StringUtils.isBlank(this.searchQueryByIds) || PropertyManager.isDevelopping()) {
        this.searchQueryByIds = retrieveSearchQuery(searchQueryByIdsFilePath);
      }
      return this.searchQueryByIds;
    }
  }

  private String retrieveSearchQuery(String filePath) {
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
}
