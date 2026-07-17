/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.cms.storage.elasticsearch;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;

import io.meeds.social.search.model.PageSearchResult;

/**
 * Searches pages previously indexed by {@link PageContentIndexingConnector}
 * (index alias {@code page_content_alias}) for the unified search bar.
 */
public class PageContentSearchConnector {

  private static final String        INDEX                     = "page_content_alias";

  private static final Log           LOG                       = ExoLogger.getLogger(PageContentSearchConnector.class);

  private static final String        TERM_REPLACEMENT          = "@term@";

  private static final String        OFFSET_REPLACEMENT        = "@offset@";

  private static final String        LIMIT_REPLACEMENT         = "@limit@";

  private static final String        PERMISSIONS_REPLACEMENT   = "@permissions_filter@";

  private final ConfigurationManager configurationManager;

  private final ElasticSearchingClient client;

  private final LayoutService        layoutService;

  private final ResourceBundleManager resourceBundleManager;

  private final FavoriteService      favoriteService;

  private final IdentityManager      identityManager;

  private final String               queryFilePath;

  private String                     query;

  public PageContentSearchConnector(ConfigurationManager configurationManager,
                                    ElasticSearchingClient client,
                                    LayoutService layoutService,
                                    ResourceBundleManager resourceBundleManager,
                                    FavoriteService favoriteService,
                                    IdentityManager identityManager,
                                    InitParams initParams) {
    this.configurationManager = configurationManager;
    this.client = client;
    this.layoutService = layoutService;
    this.resourceBundleManager = resourceBundleManager;
    this.favoriteService = favoriteService;
    this.identityManager = identityManager;
    ValueParam queryFileParam = initParams.getValueParam("query.file.path");
    this.queryFilePath = queryFileParam.getValue();
    this.query = retrieveQueryFromFile();
  }

  public List<PageSearchResult> search(String term, int offset, int limit, Locale locale) {
    if (StringUtils.isBlank(term)) {
      throw new IllegalArgumentException("Term is mandatory");
    }
    String esQuery = retrieveQuery().replace(TERM_REPLACEMENT, escape(term))
                                    .replace(PERMISSIONS_REPLACEMENT, buildPermissionFilter())
                                    .replace(OFFSET_REPLACEMENT, String.valueOf(Math.max(offset, 0)))
                                    .replace(LIMIT_REPLACEMENT, String.valueOf(limit < 1 ? 20 : limit));
    String jsonResponse = client.sendRequest(esQuery, INDEX);
    return buildResults(jsonResponse, locale == null ? Locale.getDefault() : locale);
  }

  /**
   * Fetches a single previously-indexed page by its storage id, regardless
   * of whether any term matches it — used to hydrate a page that a user
   * already bookmarked for display in their favorites list. Unlike
   * {@link #search}, there is no query term to highlight against, so the
   * excerpt is a plain (non-highlighted) snippet of the user's own
   * language content, falling back to the default content.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public PageSearchResult getById(String id, Locale locale) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is mandatory");
    }
    Locale effectiveLocale = locale == null ? Locale.getDefault() : locale;
    String esQuery = """
        {
          "query": {
            "terms": {"_id": ["%s"]}
          }
        }
        """.formatted(id);
    String jsonResponse = client.sendRequest(esQuery, INDEX);
    JSONObject jsonHit = firstHit(jsonResponse);
    return jsonHit == null ? null : buildFavoriteResult(jsonHit, effectiveLocale);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private JSONObject firstHit(String jsonResponse) {
    JSONParser parser = new JSONParser();
    Map json;
    try {
      json = (Map) parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }
    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) {
      return null;
    }
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    return jsonHits == null || jsonHits.isEmpty() ? null : (JSONObject) jsonHits.get(0);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private PageSearchResult buildFavoriteResult(JSONObject jsonHit, Locale locale) {
    String id = (String) jsonHit.get("_id");
    JSONObject source = (JSONObject) jsonHit.get("_source");
    String langField = PageContentIndexingConnector.contentFieldName(locale.toLanguageTag());
    String content = source == null ? null : (String) source.get(langField);
    if (StringUtils.isBlank(content)) {
      content = source == null ? null : (String) source.get("content");
    }
    List<String> excerpts = StringUtils.isBlank(content) ? Collections.emptyList()
                                                          : List.of(StringUtils.abbreviate(content, 150));
    Object dateValue = source == null ? null : source.get("lastUpdatedDate");
    String siteType = source == null ? null : (String) source.get("siteType");
    String siteName = source == null ? null : (String) source.get("siteName");
    return new PageSearchResult(id,
                                resolveSiteLabel(siteType, siteName, locale),
                                source == null ? null : (String) source.get("pageName"),
                                source == null ? null : (String) source.get("pageTitle"),
                                source == null ? null : (String) source.get("pagePath"),
                                source == null ? null : (String) source.get("author"),
                                dateValue == null ? 0L : ((Number) dateValue).longValue(),
                                excerpts,
                                isFavorite(id));
  }

  private boolean isFavorite(String id) {
    ConversationState conversationState = ConversationState.getCurrent();
    if (conversationState == null || conversationState.getIdentity() == null) {
      return false;
    }
    String username = conversationState.getIdentity().getUserId();
    org.exoplatform.social.core.identity.model.Identity socialIdentity = identityManager.getOrCreateUserIdentity(username);
    if (socialIdentity == null) {
      return false;
    }
    return favoriteService.isFavorite(new Favorite(PageContentIndexingConnector.TYPE,
                                                    id,
                                                    null,
                                                    Long.parseLong(socialIdentity.getId())));
  }

  private String retrieveQuery() {
    if (StringUtils.isBlank(query) || PropertyManager.isDevelopping()) {
      query = retrieveQueryFromFile();
    }
    return query;
  }

  private String retrieveQueryFromFile() {
    try {
      InputStream inputStream = configurationManager.getInputStream(queryFilePath);
      return IOUtil.getStreamContentAsString(inputStream);
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving search query from file: " + queryFilePath, e);
    }
  }

  private String escape(String term) {
    return term.replaceAll("([\\Q+-!():^[]\"{}~*?|&/\\E])", " ").trim();
  }

  /**
   * A page is visible to the current user when its {@code permissions} field
   * (raw {@code Page#getAccessPermissions()} ACL expressions) contains
   * either the current username, the {@link UserACL#EVERYONE} sentinel, or a
   * membership the user holds.
   */
  private String buildPermissionFilter() {
    ConversationState conversationState = ConversationState.getCurrent();
    if (conversationState == null || conversationState.getIdentity() == null) {
      return """
          {"term": {"permissions": "%s"}}
          """.formatted(UserACL.EVERYONE);
    }
    String username = conversationState.getIdentity().getUserId();
    Set<String> memberships = getUserMemberships(conversationState);
    List<String> should = new ArrayList<>();
    should.add("""
        {"term": {"permissions": "%s"}}
        """.formatted(username));
    should.add("""
        {"term": {"permissions": "%s"}}
        """.formatted(UserACL.EVERYONE));
    if (!memberships.isEmpty()) {
      should.add("""
          {"regexp": {"permissions": "%s"}}
          """.formatted(StringUtils.join(memberships, "|")));
    }
    return "{\"bool\": {\"should\": [" + StringUtils.join(should, ",") + "], \"minimum_should_match\": 1}}";
  }

  private Set<String> getUserMemberships(ConversationState conversationState) {
    if (conversationState.getIdentity().getMemberships() == null) {
      return Collections.emptySet();
    }
    Set<String> entries = new HashSet<>();
    for (MembershipEntry entry : conversationState.getIdentity().getMemberships()) {
      if (entry.getMembershipType().equals(MembershipEntry.ANY_TYPE)) {
        entries.add(entry.toString().replace("*", ".*"));
      } else {
        entries.add(entry.toString());
        entries.add("*:" + entry.getGroup());
      }
    }
    return entries;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<PageSearchResult> buildResults(String jsonResponse, Locale locale) {
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
    List<PageSearchResult> results = new ArrayList<>();
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    for (Object jsonHit : jsonHits) {
      try {
        PageSearchResult result = buildResult((JSONObject) jsonHit, locale);
        if (result != null) {
          results.add(result);
        }
      } catch (Exception e) {
        LOG.warn("Error processing page search result item, ignore it from results", e);
      }
    }
    return results;
  }

  /**
   * @return the built result, or {@code null} when the page only matched
   *         through content in a language that shouldn't be shown to this
   *         user (see {@link #extractExcerpts}) — such a page isn't a valid
   *         result for this user at all, it isn't merely missing an
   *         excerpt. A page matching through its title/name/site instead is
   *         still a valid result even without any content excerpt.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private PageSearchResult buildResult(JSONObject jsonHit, Locale locale) {
    String id = (String) jsonHit.get("_id");
    JSONObject source = (JSONObject) jsonHit.get("_source");
    JSONObject highlight = (JSONObject) jsonHit.get("highlight");
    List<String> excerpts = extractExcerpts(source, highlight, locale);
    if (excerpts.isEmpty() && matchedContentInAnyLanguage(highlight)) {
      return null;
    }
    Object dateValue = source == null ? null : source.get("lastUpdatedDate");
    String siteType = source == null ? null : (String) source.get("siteType");
    String siteName = source == null ? null : (String) source.get("siteName");
    return new PageSearchResult(id,
                                resolveSiteLabel(siteType, siteName, locale),
                                source == null ? null : (String) source.get("pageName"),
                                source == null ? null : (String) source.get("pageTitle"),
                                source == null ? null : (String) source.get("pagePath"),
                                source == null ? null : (String) source.get("author"),
                                dateValue == null ? 0L : ((Number) dateValue).longValue(),
                                excerpts,
                                isFavorite(id));
  }

  /**
   * A page's content is indexed once per configured language (
   * {@code content}/{@code content-<lang>}). An excerpt is only ever shown
   * when the search term actually matched the searching user's own
   * language (or, when the page has no translation for it, the default
   * no-language content) — a match that only exists in some other language
   * (be it the default content when a translation exists, or an unrelated
   * language when it doesn't) never surfaces any excerpt at all.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<String> extractExcerpts(JSONObject source, JSONObject highlight, Locale locale) {
    String langField = PageContentIndexingConnector.contentFieldName(locale == null ? null : locale.toLanguageTag());
    boolean hasTranslation = source != null && source.get(langField) != null;
    String fieldToUse = hasTranslation ? langField : "content";

    JSONArray fragments = highlight == null ? null : (JSONArray) highlight.get(fieldToUse);
    if (fragments == null) {
      return Collections.emptyList();
    }
    List<String> excerpts = new ArrayList<>();
    fragments.forEach(fragment -> excerpts.add((String) fragment));
    return excerpts;
  }

  /**
   * @return whether the term matched a {@code content}/{@code content-<lang>}
   *         field at all, in any language. Used to tell apart "no excerpt
   *         because the page matched through its title/name/site" (still a
   *         valid result) from "no excerpt because the only content match
   *         was in a language we won't show this user" (not a valid result
   *         for this user at all).
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private boolean matchedContentInAnyLanguage(JSONObject highlight) {
    if (highlight == null) {
      return false;
    }
    return highlight.keySet().stream().anyMatch(key -> StringUtils.startsWith((String) key, "content"));
  }

  /**
   * Resolves the site's display label in the searching user's locale. The
   * raw site label ({@link PortalConfig#getLabel()}) is only indexed as-is
   * ({@code siteName}) — resolving it earlier, at index time, would freeze
   * it in one locale for every searching user.
   */
  private String resolveSiteLabel(String siteType, String siteName, Locale locale) {
    if (StringUtils.isAnyBlank(siteType, siteName)) {
      return siteName;
    }
    try {
      PortalConfig portalConfig = layoutService.getPortalConfig(new SiteKey(siteType, siteName));
      String label = portalConfig == null ? null : portalConfig.getLabel();
      if (StringUtils.isBlank(label)) {
        return siteName;
      } else if (!ExpressionUtil.isResourceBindingExpression(label)) {
        return label;
      }
      ResourceBundle bundle = resourceBundleManager.getNavigationResourceBundle(LocaleContextInfo.getLocaleAsString(locale),
                                                                                siteType,
                                                                                siteName);
      String resolved = bundle == null ? null : ExpressionUtil.getExpressionValue(bundle, label);
      return StringUtils.isNotBlank(resolved) ? resolved : siteName;
    } catch (Exception e) {
      LOG.debug("Cannot resolve site label for site {}:{}", siteType, siteName, e);
      return siteName;
    }
  }

}
