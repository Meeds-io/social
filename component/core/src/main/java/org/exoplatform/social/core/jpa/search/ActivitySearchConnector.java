package org.exoplatform.social.core.jpa.search;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.IOUtil;
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

public class ActivitySearchConnector {
  private static final Log              LOG                          = ExoLogger.getLogger(ActivitySearchConnector.class);

  private static final String           SEARCH_QUERY_FILE_PATH_PARAM = "query.file.path";

  private final ActivitySearchProcessor activitySearchProcessor;                                                          // NOSONAR

  private final IdentityManager         identityManager;

  private final ActivityStorage         activityStorage;

  private final ElasticSearchingClient  client;

  private String                        index;

  private String                        searchType;

  private String                        searchQuery;

  public ActivitySearchConnector(ActivitySearchProcessor activitySearchProcessor,
                                 IdentityManager identityManager,
                                 ActivityStorage activityStorage,
                                 ConfigurationManager configurationManager,
                                 ElasticSearchingClient client,
                                 InitParams initParams) {
    this.activitySearchProcessor = activitySearchProcessor;
    this.identityManager = identityManager;
    this.activityStorage = activityStorage;
    this.client = client;

    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    this.searchType = param.getProperty("searchType");
    if (initParams.containsKey(SEARCH_QUERY_FILE_PATH_PARAM)) {
      String queryFilePath = initParams.getValueParam(SEARCH_QUERY_FILE_PATH_PARAM).getValue();
      try {
        InputStream queryFileIS = configurationManager.getInputStream(queryFilePath);
        this.searchQuery = IOUtil.getStreamContentAsString(queryFileIS);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch search query from path {}", queryFilePath, e);
      }
    }
  }

  public List<ActivitySearchResult> search(Identity viewerIdentity,
                                           ActivitySearchFilter filter,
                                           long offset,
                                           long limit) {
    String esQuery = buildQueryStatement(viewerIdentity, filter, offset, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index, this.searchType);
    return buildResult(jsonResponse);
  }

  private String buildQueryStatement(Identity viewerIdentity,
                                     ActivitySearchFilter filter,
                                     long offset,
                                     long limit) {
    Set<Long> streamFeedOwnerIds = activityStorage.getStreamFeedOwnerIds(viewerIdentity);

    return searchQuery.replaceAll("@term@", removeAccents(filter.getTerm()))
                      .replaceAll("@permissions@", StringUtils.join(streamFeedOwnerIds, ","))
                      .replaceAll("@offset@", String.valueOf(offset))
                      .replaceAll("@limit@", String.valueOf(limit));
  }

  @SuppressWarnings("rawtypes")
  private List<ActivitySearchResult> buildResult(String jsonResponse) {
    LOG.debug("Search Query response from ES : {} ", jsonResponse);

    List<ActivitySearchResult> results = new ArrayList<>();
    JSONParser parser = new JSONParser();

    Map json = null;
    try {
      json = (Map) parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null)
      return results;

    //
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    for (Object jsonHit : jsonHits) {
      ActivitySearchResult activitySearchResult = new ActivitySearchResult();

      JSONObject jsonHitObject = (JSONObject) jsonHit;
      JSONObject hitSource = (JSONObject) jsonHitObject.get("_source");
      Long id = parseLong(hitSource, "id");
      Long posterId = parseLong(hitSource, "posterId");
      Long parentId = parseLong(hitSource, "parentId");
      Long streamOwner = parseLong(hitSource, "streamOwner");
      Long postedTime = parseLong(hitSource, "postedTime");
      Long lastUpdatedTime = parseLong(hitSource, "lastUpdatedTime");
      String body = (String) hitSource.get("body");
      JSONObject hightlightSource = (JSONObject) jsonHitObject.get("highlight");
      JSONArray bodyExcepts = (JSONArray) hightlightSource.get("body");
      @SuppressWarnings("unchecked")
      String[] bodyExceptsArray = (String[]) bodyExcepts.toArray(new String[0]);
      List<String> excerpts = Arrays.asList(bodyExceptsArray);

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
          posterIdentity = posterIdentity.clone();
          posterIdentity.setProfile(null);
          commentSearchResult.setPoster(posterIdentity);
        }
        commentSearchResult.setBody(body);
        commentSearchResult.setExcerpts(excerpts);
        activitySearchResult.setComment(commentSearchResult);
        activitySearchProcessor.formatSearchResult(commentSearchResult);

        transformActivityToResult(activitySearchResult, parentId);
      }

      results.add(activitySearchResult);
    }
    return results;
  }

  private void transformActivityToResult(ActivitySearchResult activitySearchResult, Long parentId) {
    ExoSocialActivity activity = activityStorage.getActivity(parentId.toString());

    // Activity
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
    activitySearchResult.setBody(activity.getBody());
    activitySearchProcessor.formatSearchResult(activitySearchResult);
  }

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }

  private static String removeAccents(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    return string;
  }
}
