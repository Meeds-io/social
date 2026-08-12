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
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;

import io.meeds.social.search.model.PageSearchResult;

/**
 * Searches pages previously indexed by {@link PageContentIndexingConnector}
 * (index alias {@code page_content_alias}) for the unified search bar.
 */
public class PageContentSearchConnector {

  /** The ES index alias page content blocks are searched through. */
  private static final String        INDEX                     = "page_content_alias";

  /** Class-level logger. */
  private static final Log           LOG                       = ExoLogger.getLogger(PageContentSearchConnector.class);

  /** Query template placeholder for the search term. */
  private static final String        TERM_REPLACEMENT          = "@term@";

  /** Query template placeholder for the result offset. */
  private static final String        OFFSET_REPLACEMENT        = "@offset@";

  /** Query template placeholder for the result limit. */
  private static final String        LIMIT_REPLACEMENT         = "@limit@";

  /** Query template placeholder for the permission and space filters. */
  private static final String        FILTERS_REPLACEMENT       = "\"@filters@\"";

  /** Matches the document id shape produced by {@code PageContentIndexingConnector#buildBlockId}. */
  private static final Pattern       BLOCK_ID_PATTERN          = Pattern.compile("^page_\\d+(_[0-9a-fA-F]+)?$");

  /** Upper bound on the content blocks {@link #findIndexedBlockIds} reports for one page. */
  private static final int           INDEXED_BLOCKS_PER_PAGE_LIMIT = 1000;

  /** Used to load the query template resource. */
  private final ConfigurationManager configurationManager;

  /** Used to send queries to Elasticsearch. */
  private final ElasticSearchingClient client;

  /** Used to resolve a hit's page and its metadata. */
  private final LayoutService        layoutService;

  /** Used to resolve a hit's site display label. */
  private final ResourceBundleManager resourceBundleManager;

  /** Used to resolve the current user's bookmarked pages. */
  private final FavoriteService      favoriteService;

  /** Used to resolve the current user's social identity. */
  private final IdentityManager      identityManager;

  /** Used to resolve a space's group id when filtering by space. */
  private final SpaceService         spaceService;

  /** Used to normalize a request locale to a configured content language. */
  private final LocaleConfigService  localeConfigService;

  /** Classpath location of the query template resource. */
  private final String               queryFilePath;

  /** The cached query template, reloaded on each call in development mode. */
  private String                     query;

  public PageContentSearchConnector(ConfigurationManager configurationManager,
                                    ElasticSearchingClient client,
                                    LayoutService layoutService,
                                    ResourceBundleManager resourceBundleManager,
                                    FavoriteService favoriteService,
                                    IdentityManager identityManager,
                                    SpaceService spaceService,
                                    LocaleConfigService localeConfigService,
                                    InitParams initParams) {
    this.configurationManager = configurationManager;
    this.client = client;
    this.layoutService = layoutService;
    this.resourceBundleManager = resourceBundleManager;
    this.favoriteService = favoriteService;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.localeConfigService = localeConfigService;
    ValueParam queryFileParam = initParams.getValueParam("query.file.path");
    this.queryFilePath = queryFileParam.getValue();
    this.query = retrieveQueryFromFile();
  }

  /**
   * @param term the search term, mandatory
   * @param offset the result offset
   * @param limit the result limit
   * @param locale the user's locale, used to pick the excerpts' language
   * @param spaceIds the space ids to restrict results to, or {@code null}/empty for none
   * @param favorites whether to restrict results to the pages the current
   *          user bookmarked — the unified search bar's "Favorites" toggle
   *          appends it as a query parameter for every connector declaring
   *          {@code favoritesEnabled}, so it has to be honoured here rather
   *          than silently dropped
   * @return the matching {@link PageSearchResult}s
   */
  public List<PageSearchResult> search(String term, int offset, int limit, Locale locale, List<Long> spaceIds, boolean favorites) {
    if (StringUtils.isBlank(term)) {
      throw new IllegalArgumentException("Term is mandatory");
    }
    Set<String> favoriteIds = resolveFavoriteIds();
    if (favorites && favoriteIds.isEmpty()) {
      return Collections.emptyList();
    }
    String esQuery = retrieveQuery().replace(TERM_REPLACEMENT, escape(term))
                                    .replace(FILTERS_REPLACEMENT,
                                             buildPermissionFilter() + buildSpaceFilter(spaceIds)
                                                 + buildFavoriteFilter(favorites, favoriteIds))
                                    .replace(OFFSET_REPLACEMENT, String.valueOf(Math.max(offset, 0)))
                                    .replace(LIMIT_REPLACEMENT, String.valueOf(limit < 1 ? 20 : limit));
    String jsonResponse = client.sendRequest(esQuery, INDEX);
    return buildResults(jsonResponse, locale == null ? Locale.getDefault() : locale, favoriteIds);
  }

  /**
   * Fetches a single previously-indexed page by its storage id, regardless
   * of whether any term matches it — used to hydrate a page that a user
   * already bookmarked for display in their favorites list. Unlike
   * {@link #search}, there is no query term to highlight against, so the
   * excerpt is a plain (non-highlighted) snippet of the user's own
   * language content, falling back to the default content.
   *
   * @param id the page's storage id
   * @param locale the user's locale, used to pick the excerpt's language
   * @return the matching {@link PageSearchResult}, or {@code null} if none
   *         was found or {@code id} isn't shaped like a block id
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public PageSearchResult getById(String id, Locale locale) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is mandatory");
    }
    if (!BLOCK_ID_PATTERN.matcher(id).matches()) {
      return null;
    }
    Locale effectiveLocale = locale == null ? Locale.getDefault() : locale;
    String esQuery = """
        {
          "query": {
            "bool": {
              "must": {"terms": {"_id": ["%s"]}},
              "filter": [%s]
            }
          }
        }
        """.formatted(id, buildPermissionFilter());
    String jsonResponse = client.sendRequest(esQuery, INDEX);
    JSONObject jsonHit = firstHit(jsonResponse);
    return jsonHit == null ? null : buildFavoriteResult(jsonHit, effectiveLocale);
  }

  /**
   * @param pageStorageId the storage id of the page to look up
   * @return the ids of every content block currently indexed under the
   *         given page, regardless of whether a {@link io.meeds.social.cms.model.CMSSetting} still
   *         binds them — used to detect blocks that were detached from the
   *         page (or the page itself renamed/never re-saved) so the caller
   *         can unindex them. Returns an empty list, without ever querying
   *         Elasticsearch, when {@code pageStorageId} isn't shaped like one
   *         (defends the same way {@link #getById} does, in case this public
   *         method is ever reached with untrusted input).
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<String> findIndexedBlockIds(String pageStorageId) {
    if (StringUtils.isBlank(pageStorageId) || !BLOCK_ID_PATTERN.matcher(pageStorageId).matches()) {
      return Collections.emptyList();
    }
    String esQuery = """
        {
          "query": {"term": {"pageStorageId": "%s"}},
          "_source": false,
          "size": %s
        }
        """.formatted(pageStorageId, INDEXED_BLOCKS_PER_PAGE_LIMIT);
    String jsonResponse = client.sendRequest(esQuery, INDEX);
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
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    if (jsonHits == null) {
      return Collections.emptyList();
    }
    if (jsonHits.size() >= INDEXED_BLOCKS_PER_PAGE_LIMIT) {
      // Callers use this list to unindex whatever isn't bound to the page
      // anymore, so a truncated list silently leaves stale documents behind
      LOG.warn("Page {} has at least {} indexed content blocks, the list is truncated: stale blocks beyond that count"
          + " won't be unindexed",
               pageStorageId,
               INDEXED_BLOCKS_PER_PAGE_LIMIT);
    }
    List<String> ids = new ArrayList<>();
    for (Object jsonHit : jsonHits) {
      ids.add((String) ((JSONObject) jsonHit).get("_id"));
    }
    return ids;
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
      return null; // NOSONAR JSONObject extends Map, but null here means "no hit
                   // found" — an empty map would make callers build a bogus result
                   // instead of correctly treating it as not-found
    }
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    return jsonHits == null || jsonHits.isEmpty() ? null : (JSONObject) jsonHits.get(0);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private PageSearchResult buildFavoriteResult(JSONObject jsonHit, Locale locale) {
    String id = (String) jsonHit.get("_id");
    JSONObject source = (JSONObject) jsonHit.get("_source");
    String langField = PageContentIndexingConnector.contentFieldName(resolveContentLanguageTag(locale));
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

  /**
   * @return the ids of every "page" the current user has bookmarked —
   *         resolved once per {@link #search} call and checked with a
   *         plain {@code Set} lookup per hit, instead of one
   *         {@link FavoriteService#isFavorite} call (and one identity
   *         resolution) per result.
   */
  private Set<String> resolveFavoriteIds() {
    ConversationState conversationState = ConversationState.getCurrent();
    if (conversationState == null || conversationState.getIdentity() == null) {
      return Collections.emptySet();
    }
    String username = conversationState.getIdentity().getUserId();
    org.exoplatform.social.core.identity.model.Identity socialIdentity = identityManager.getOrCreateUserIdentity(username);
    if (socialIdentity == null) {
      return Collections.emptySet();
    }
    long identityId = Long.parseLong(socialIdentity.getId());
    return favoriteService.getFavoriteItemsByCreatorAndType(PageContentIndexingConnector.TYPE, identityId, 0, -1)
                          .stream()
                          .map(MetadataItem::getObjectId)
                          .collect(Collectors.toSet());
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
    // The backslash matters as much as the double quote here: the escaped term
    // is spliced into a raw JSON query, so a term ending in one would escape
    // the template's own closing quote and make the whole request unparsable
    // for Elasticsearch (a 400 surfacing as a 500), rather than simply not
    // matching anything
    return term.replaceAll("([\\Q+-!():^[]\"{}~*?|&/\\\\E])", " ").trim();
  }

  /**
   * Restricts results to the sites of the given spaces (resolved to their
   * {@code siteName}, which for a {@code SiteType#SPACE} site is the
   * space's group id) when the user picked one or more spaces to filter
   * the unified search within.
   *
   * @param spaceIds the space ids to restrict results to, or {@code null}/empty for none
   * @return a leading-comma-prefixed ES filter clause, or an empty string when {@code spaceIds} is empty
   */
  private String buildSpaceFilter(List<Long> spaceIds) {
    if (CollectionUtils.isEmpty(spaceIds)) {
      return "";
    }
    List<String> groupIds = spaceIds.stream()
                                    .map(spaceService::getSpaceById)
                                    .filter(Objects::nonNull)
                                    .map(Space::getGroupId)
                                    .toList();
    if (groupIds.isEmpty()) {
      return "";
    }
    String quotedGroupIds = groupIds.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(","));
    return """
        ,{"terms": {"siteName": [%s]}}
        """.formatted(quotedGroupIds);
  }

  /**
   * Restricts results to the blocks the current user bookmarked, when the
   * unified search bar's "Favorites" toggle is on. The ids come from the
   * favorites store, but they're still matched against
   * {@link #BLOCK_ID_PATTERN} before being spliced into the raw JSON query,
   * the same way {@link #getById} defends itself.
   *
   * @param favorites whether the favorites filter is on
   * @param favoriteIds ids of the blocks the current user bookmarked
   * @return a leading-comma-prefixed ES filter clause, or an empty string when the filter is off
   */
  private String buildFavoriteFilter(boolean favorites, Set<String> favoriteIds) {
    if (!favorites) {
      return "";
    }
    String quotedIds = favoriteIds.stream()
                                  .filter(id -> BLOCK_ID_PATTERN.matcher(id).matches())
                                  .map(id -> "\"" + id + "\"")
                                  .collect(Collectors.joining(","));
    if (StringUtils.isBlank(quotedIds)) {
      // No favorite is shaped like a block id: match nothing rather than
      // degrade into "no filter at all", which would return every page
      return """
          ,{"match_none": {}}
          """;
    }
    return """
        ,{"terms": {"_id": [%s]}}
        """.formatted(quotedIds);
  }

  /**
   * A page is visible to the current user when its {@code permissions} field
   * (raw {@code Page#getAccessPermissions()} ACL expressions) contains
   * either the current username, the {@link UserACL#EVERYONE} sentinel, or a
   * membership the user holds.
   *
   * @return an ES filter clause matching the current user's visible permissions
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
        // Matches pages ACL'd with the wildcard-membership-type convention
        // (e.g. "*:/platform/administrators"); the "*" must be escaped here
        // since it's a literal character in that ACL string, not a regexp
        // quantifier — an unescaped "*" right after the "|" alternation has
        // no preceding token to repeat, which Elasticsearch rejects outright
        // (silently returning zero results, not an error the caller sees).
        // Doubled backslash: this string is spliced into a raw JSON text
        // (not JSON-serialized), so it must itself carry a JSON-escaped
        // backslash to decode to a single literal "\" for the regexp engine.
        entries.add("\\\\*:" + entry.getGroup());
      }
    }
    return entries;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<PageSearchResult> buildResults(String jsonResponse, Locale locale, Set<String> favoriteIds) {
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
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    if (jsonHits == null) {
      return Collections.emptyList();
    }
    List<PageSearchResult> results = new ArrayList<>();
    for (Object jsonHit : jsonHits) {
      try {
        PageSearchResult result = buildResult((JSONObject) jsonHit, locale, favoriteIds);
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
   * @param jsonHit a single ES hit from the search response
   * @param locale the user's locale, used to pick the excerpt's language
   * @param favoriteIds ids of the pages the current user has bookmarked
   * @return the built result, or {@code null} when the page only matched
   *         through content in a language that shouldn't be shown to this
   *         user (see {@link #extractExcerpts}) — such a page isn't a valid
   *         result for this user at all, it isn't merely missing an
   *         excerpt. A page matching through its title/name/site instead is
   *         still a valid result even without any content excerpt.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private PageSearchResult buildResult(JSONObject jsonHit, Locale locale, Set<String> favoriteIds) {
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
                                favoriteIds.contains(id));
  }

  /**
   * Index-side language field names are built from the bare language tags
   * {@link LocaleConfigService} was configured with (e.g. {@code fr}), but
   * a request locale can be region-qualified (e.g. {@code fr-FR}). Without
   * normalizing, {@code content-fr-FR} would never match the indexed
   * {@code content-fr} field, silently falling back to the default-language
   * content instead of the user's own translation.
   *
   * @param locale the request locale to normalize
   * @return the configured language tag matching {@code locale}'s language
   *         (ignoring region/variant), or {@code locale}'s own tag verbatim
   *         if none is configured for that language.
   */
  private String resolveContentLanguageTag(Locale locale) {
    if (locale == null) {
      return null;
    }
    return localeConfigService.getLocalConfigs()
                              .stream()
                              .map(LocaleConfig::getLocale)
                              .filter(configuredLocale -> StringUtils.equals(configuredLocale.getLanguage(), locale.getLanguage()))
                              .findFirst()
                              .map(Locale::toLanguageTag)
                              .orElseGet(locale::toLanguageTag);
  }

  /**
   * A page's content is indexed once per configured language (
   * {@code content}/{@code content-<lang>}). An excerpt is only ever shown
   * when the search term actually matched the searching user's own
   * language (or, when the page has no translation for it, the default
   * no-language content) — a match that only exists in some other language
   * (be it the default content when a translation exists, or an unrelated
   * language when it doesn't) never surfaces any excerpt at all.
   *
   * Whether the page has a translation for the user's language is read from
   * the {@code contentLanguages} keyword field rather than from the presence
   * of the {@code content-<lang>} field itself, so the search query can keep
   * every language's full text out of {@code _source} (see
   * {@code page-search-query.json}) instead of shipping the whole page's
   * content back for every hit just to answer a yes/no question. Highlighting
   * is unaffected: the content fields are stored with term vectors.
   *
   * @param source the hit's {@code _source}
   * @param highlight the hit's {@code highlight}, or {@code null} if none
   * @param locale the user's locale, used to pick the excerpt's language
   * @return the highlighted excerpts to show this user, or an empty list if none
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<String> extractExcerpts(JSONObject source, JSONObject highlight, Locale locale) {
    String languageTag = resolveContentLanguageTag(locale);
    String langField = PageContentIndexingConnector.contentFieldName(languageTag);
    String fieldToUse = hasTranslation(source, languageTag, langField) ? langField : "content";

    JSONArray fragments = highlight == null ? null : (JSONArray) highlight.get(fieldToUse);
    if (fragments == null) {
      return Collections.emptyList();
    }
    List<String> excerpts = new ArrayList<>();
    fragments.forEach(fragment -> excerpts.add((String) fragment));
    return excerpts;
  }

  /**
   * @param  source the hit's {@code _source}
   * @param  languageTag the configured language tag matching the user's locale
   * @param  langField the content field name for that language tag
   * @return whether the block has content for the user's language. Reads the
   *         {@code contentLanguages} field, falling back to the presence of
   *         the content field itself for callers ({@link #getById}) whose
   *         {@code _source} does carry the content.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private boolean hasTranslation(JSONObject source, String languageTag, String langField) {
    if (source == null || StringUtils.isBlank(languageTag)) {
      return false;
    }
    Object languages = source.get(PageContentIndexingConnector.CONTENT_LANGUAGES_FIELD);
    if (languages instanceof JSONArray languageTags) {
      return languageTags.contains(languageTag);
    } else if (languages instanceof String singleLanguageTag) {
      // A one-value keyword field comes back as a plain string, not an array
      return StringUtils.equals(singleLanguageTag, languageTag);
    }
    return source.get(langField) != null;
  }

  /**
   * @param highlight the hit's {@code highlight}, or {@code null} if none
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
   *
   * @param siteType the site's type
   * @param siteName the site's technical name
   * @param locale the user's locale to resolve the label in
   * @return the resolved site label, or {@code siteName} verbatim as a fallback
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
