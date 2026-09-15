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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfigService;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.model.PageContentBlock;
import io.meeds.social.cms.plugin.PageContentBlockPlugin;
import io.meeds.social.cms.service.CMSService;
import io.meeds.social.cms.service.PageContentBlockPluginService;
import io.meeds.social.cms.service.PageUrlResolverService;
import io.meeds.social.cms.utils.PageContentBlockUtils;

/**
 * Indexes portal Pages for the unified search bar, as two kinds of documents
 * sharing one index:
 * <ul>
 * <li>a <b>content block</b> document per block bound to the page through a
 * {@link CMSSetting} whose type is backed by a registered
 * {@link PageContentBlockPlugin} — generic across content-block types, no
 * knowledge of any specific addon (e.g. Notes' Single Note View). A page can
 * carry more than one block (e.g. several Single Note View blocks placed in
 * different sections of the same page); each one is indexed as its own
 * document, so that unified search returns one result per block rather than
 * a single blended excerpt for the whole page. Its id is
 * {@code <page's storage id>_<block hash>} ({@link #buildBlockId}).</li>
 * <li>a bare <b>page</b> document — the page's title, name and site, no
 * content — for a page carrying <em>no</em> live content block at all, so
 * that a page like "Spaces" or "Tasks" can still be found by name by a user
 * who doesn't know how to reach it. Its id is the page's storage id itself
 * ({@code page_<numeric DB id>}), and it exists only while the page is
 * reachable from a navigation node: a search result that leads nowhere is
 * worse than none.</li>
 * </ul>
 * The two kinds are exclusive for a given page: the moment a live block
 * appears, the block documents take over (they carry the same title, name
 * and site fields, so the page stays findable by name) and the bare page
 * document is removed — and a bare page document requested for a page that
 * turns out to carry live blocks queues those blocks' own indexing instead
 * ({@link #queueLiveBlockDocuments}): every one of them on a creation, so
 * that a full reindex started before the content-block plugins were
 * registered (a reindex-on-upgrade runs at Kernel start, the plugins arrive
 * with their addon's Spring context) still ends up with every block indexed;
 * only the ones the index lacks on a refresh ({@link #update}), so that a
 * page saved outside the Layout addon gets its blocks indexed without a
 * save through it indexing them twice.
 * "Live" is a purely structural rule — a
 * {@link CMSSetting} whose widget still sits on the page's layout, see
 * {@link PageContentBlockUtils} — so that every path deciding what the index
 * holds evaluates it identically, from layout alone. Its one blind spot: a
 * page whose only block's widget is still there but whose content is gone
 * (e.g. the note it displayed was deleted) has no document of either kind
 * until the widget is removed from the page.
 * <p>
 * Neither id is {@link PageKey#format()}, which can exceed the 50-character
 * limit of the {@code ES_INDEXING_QUEUE.ENTITY_ID} column, nor the setting's
 * own name, which can be arbitrarily long — and the page id portion stays
 * stable across page/site renames, unlike the formatted key.
 */
public class PageContentIndexingConnector extends ElasticIndexingServiceConnector {

  /** The ES connector/entity type name for indexed page documents. */
  public static final String                  TYPE                     = "page";

  /** Prefix a page's storage id is rendered with (e.g. {@code page_139}). */
  private static final String                 PAGE_STORAGE_ID_PREFIX   = "page_";

  /** Matches the id of a bare page document: a page storage id, no block hash. */
  private static final Pattern                PAGE_DOCUMENT_ID_PATTERN = Pattern.compile("^page_\\d+$");

  /**
   * The sites whose pages users reach through a navigation. Pages of any
   * other site type — draft copies, site templates, the legacy per-user
   * sites — never surface as bare page documents.
   */
  private static final Set<SiteType>          INDEXABLE_SITE_TYPES     = Set.of(SiteType.PORTAL, SiteType.GROUP);

  /** Class-level logger. */
  private static final Log                    LOGGER                   = ExoLogger.getExoLogger(PageContentIndexingConnector.class);

  /** ES mapping template for a single per-language content field. */
  private static final String                 CONTENT_MAPPING          = """
        "@field@" : {
          "type" : "text",
          "index_options": "offsets",
          "term_vector": "with_positions_offsets",
          "store": true
        }
      """;

  /**
   * The ES field listing the language tags a block actually has content for
   * (the default/no-language content isn't listed — it's always the
   * {@code content} field). Lets the search side apply its
   * "only excerpt in the searching user's own language" rule without having
   * to pull every language's full text back in {@code _source}.
   */
  public static final String                  CONTENT_LANGUAGES_FIELD  = "contentLanguages";

  /** ES index mapping for the "page" document type. */
  private static final String                 ES_MAPPING               = """
      {
        "properties" : {
          "pageStorageId" : {"type" : "keyword"},
          "siteName" : {"type" : "keyword"},
          "siteType" : {"type" : "keyword"},
          "pageName" : {"type" : "keyword"},
          "pageTitle" : {"type" : "text"},
          "pagePath" : {"type" : "keyword"},
          "author" : {"type" : "keyword"},
          "permissions" : {"type" : "keyword"},
          "contentLanguages" : {"type" : "keyword"},
          "lastUpdatedDate" : {"type" : "date", "format" : "epoch_millis"},
          @content_mappings@
        }
      }
      """;

  /** Used to enumerate registered content-block content types and extract their content. */
  private final PageContentBlockPluginService pluginService;

  /** Used to ask the index which of a page's documents it already holds, when it can be trusted to answer. */
  private final PageContentSearchConnector    searchConnector;

  /** Used to enumerate the {@link io.meeds.social.cms.model.CMSSetting}s of a content type. */
  private final CMSService                    cmsService;

  /** Used to resolve a setting's page and its metadata (site, title, permissions...). */
  private final LayoutService                 layoutService;

  /** Used to enumerate the configured languages a content block can be translated into. */
  private final LocaleConfigService           localeConfigService;

  /** Used to resolve a page's front-end URL, when one exists. */
  private final PageUrlResolverService        urlResolverService;

  /** Used to tell which of a page's documents the index already holds. */
  private final PageContentSearchConnector    searchConnector;

  public PageContentIndexingConnector(PageContentBlockPluginService pluginService,
                                      PageContentSearchConnector searchConnector,
                                      CMSService cmsService,
                                      LayoutService layoutService,
                                      LocaleConfigService localeConfigService,
                                      PageUrlResolverService urlResolverService,
                                      PageContentSearchConnector searchConnector,
                                      InitParams initParams) {
    super(initParams);
    this.pluginService = pluginService;
    this.searchConnector = searchConnector;
    this.cmsService = cmsService;
    this.layoutService = layoutService;
    this.localeConfigService = localeConfigService;
    this.urlResolverService = urlResolverService;
    this.searchConnector = searchConnector;
  }

  @Override
  public String getConnectorName() {
    return TYPE;
  }

  @Override
  public String getMapping() {
    String contentField = CONTENT_MAPPING.replace("@field@", "content");
    String translatedFields = localeConfigService.getLocalConfigs()
                                                 .stream()
                                                 .map(localeConfig -> CONTENT_MAPPING.replace("@field@",
                                                                                              contentFieldName(localeConfig.getLocale()
                                                                                                                           .toLanguageTag())))
                                                 .collect(Collectors.joining(",\n"));
    String contentMappings = StringUtils.isBlank(translatedFields) ? contentField : contentField + ",\n" + translatedFields;
    return ES_MAPPING.replace("@content_mappings@", contentMappings);
  }

  /**
   * Every content block id first, then the storage id of every page of the
   * store — the two lists are laid end to end, so that a batch spanning the
   * boundary still comes back full: ElasticIndexingOperationProcessor#reindexAll
   * loops while the returned batch size equals the batch size it asked for,
   * so a batch coming back even one id short — after it has already queued a
   * DELETE_ALL — ends the reindex and leaves every remaining document
   * unindexed.
   * <p>
   * The page portion is paged by the store itself, in its own deterministic
   * order (site type, site name, page name), and is <em>not</em> filtered
   * here: drafts, pages of sites nobody navigates, pages carrying content
   * blocks or unreachable ones are all decided in {@link #create(String)},
   * which returns {@code null} for them. Filtering before paging would need
   * the whole page table loaded on every batch call, filtering after it
   * would shorten batches; a disqualified id costs one queued operation
   * that indexes nothing, which is cheap.
   */
  @Override
  public List<String> getAllIds(int offset, int limit) {
    List<String> blockIds = getAllBlockIds();
    List<String> ids = new ArrayList<>(blockIds.stream().skip(offset).limit(limit).toList());
    int remaining = limit - ids.size();
    if (remaining > 0) {
      int pageOffset = Math.max(offset - blockIds.size(), 0);
      ids.addAll(getPageIds(pageOffset, remaining));
    }
    return ids;
  }

  /**
   * Builds a document from scratch — a full reindex, or a page or site that
   * just came to exist. Besides the document itself, a bare page document
   * requested this way for a page that turns out to carry live content
   * blocks queues <em>every</em> one of those blocks' own indexing (see
   * {@link #queueLiveBlockDocuments}): nothing else may ever reach them, and
   * the index cannot be asked which ones it holds at that moment.
   */
  @Override
  public Document create(String id) {
    return build(id, true);
  }

  /**
   * Refreshes a document that a listener already knows about. Unlike
   * {@link #create}, a bare page document refreshed for a page carrying live
   * blocks queues only the blocks the index does <em>not</em> hold yet (see
   * {@link #queueLiveBlockDocuments}): the ones it holds are being refreshed
   * by the listener that owns them, or need no refresh at all.
   */
  @Override
  public Document update(String id) {
    return build(id, false);
  }

  /**
   * @param  id       the document id, of either kind
   * @param  creation whether the document is built from scratch
   *                  ({@link #create}) rather than refreshed
   *                  ({@link #update}) — decides how a page that turns out
   *                  to be represented by content block documents has them
   *                  queued
   * @return the document, or {@code null} when nothing is to be indexed
   *         under that id
   */
  private Document build(String id, boolean creation) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    try {
      Page page = layoutService.getPage(parsePageId(id));
      if (page == null) {
        LOGGER.warn("Page of document {} wasn't found, thus it can't be indexed", id);
        return null;
      }
      PageKey pageKey = page.getPageKey();
      if (isDraftPage(pageKey)) {
        LOGGER.debug("Page {} is a draft, thus it can't be indexed", pageKey);
        return null;
      }
      return isPageDocumentId(id) ? createPageDocument(id, page, creation) : createBlockDocument(id, page);
    } catch (Exception e) {
      LOGGER.warn("Cannot index page document with id {}", id, e);
      return null;
    }
  }

  /**
   * @param  lang language tag, empty string for the default/no-language
   *              content
   * @return the ES field name holding that language's content
   */
  public static String contentFieldName(String lang) {
    return StringUtils.isBlank(lang) ? "content" : "content-" + lang;
  }

  /**
   * @param  id an indexed document id
   * @return whether {@code id} designates a bare page document (a page
   *         storage id carrying no block hash) rather than a content block
   *         one
   */
  public static boolean isPageDocumentId(String id) {
    return id != null && PAGE_DOCUMENT_ID_PATTERN.matcher(id).matches();
  }

  /**
   * A page currently being edited as a draft is cloned by the Layout addon
   * as either a whole draft site ({@link SiteType#DRAFT}) or, within the
   * same site, a page named {@code <original>_draft_<username>} — neither
   * is a published page and must not be indexed.
   *
   * @param pageKey the page key to check
   * @return {@code true} if the page is a draft
   */
  public static boolean isDraftPage(PageKey pageKey) {
    return pageKey.getSite().getType() == SiteType.DRAFT || StringUtils.contains(pageKey.getName(), "_draft_");
  }

  /**
   * @param  siteKey a site key
   * @return whether the site is one users reach through a navigation, i.e.
   *         whose pages may surface as bare page documents
   */
  public static boolean isIndexableSite(SiteKey siteKey) {
    return siteKey != null && INDEXABLE_SITE_TYPES.contains(siteKey.getType());
  }

  /**
   * Every content block document id, in a deterministic order: the
   * underlying content types (a Set) and settings carry no natural order
   * otherwise, which would let a full reindex skip or repeat blocks across
   * batches.
   * <p>
   * Every reason a setting can be disqualified (blank/draft page reference,
   * page gone, or the widget that created the block no longer present on the
   * page) is evaluated here, before {@link #getAllIds} applies skip/limit,
   * so a disqualified setting never consumes a slot in a batch — the block
   * portion of the ids has no store-side paging to lean on, unlike the page
   * portion.
   * <p>
   * Pages are resolved through a per-call memo: re-walking the settings that
   * precede {@code offset} on every batch then costs at most one page load
   * per distinct page reference, not one per setting.
   */
  private List<String> getAllBlockIds() {
    Map<String, ResolvedPage> resolvedPages = new HashMap<>();
    return pluginService.getContentTypes()
                        .stream()
                        .sorted()
                        .flatMap(type -> cmsService.getSettingsByType(type)
                                                   .stream()
                                                   .filter(s -> StringUtils.isNotBlank(s.getPageReference()))
                                                   .filter(s -> isNotDraftPageReference(s.getPageReference()))
                                                   .sorted(Comparator.comparing(CMSSetting::getName))
                                                   .map(s -> buildBlockId(type, s, resolvedPages)))
                        .filter(StringUtils::isNotBlank)
                        .toList();
  }

  /**
   * @param  offset the offset within the store's own page ordering
   * @param  limit  the number of pages to return at most
   * @return the storage ids of the pages of every site, as the store pages
   *         them — see {@link #getAllIds} for why nothing is filtered here
   */
  private List<String> getPageIds(int offset, int limit) {
    QueryResult<PageContext> pages = layoutService.findPages(offset, limit, null, null, null, null);
    if (pages == null) {
      return List.of();
    }
    return StreamSupport.stream(pages.spliterator(), false)
                        .map(PageContext::getState)
                        .filter(Objects::nonNull)
                        .map(PageState::getStorageId)
                        .filter(StringUtils::isNotBlank)
                        .toList();
  }

  /**
   * @param  id       the bare page document id
   * @param  page     the page it designates
   * @param  creation whether the document is built from scratch rather than
   *                  refreshed — decides whether the page's live content
   *                  blocks, if any, are all queued for indexing or only the
   *                  ones the index lacks (see {@link #queueLiveBlockDocuments})
   * @return the bare page document, or {@code null} when the page is
   *         represented by its content block documents instead, belongs to
   *         a site nobody navigates, or can't be reached from any navigation
   *         node (a result leading nowhere is worse than none)
   */
  private Document createPageDocument(String id, Page page, boolean creation) {
    PageKey pageKey = page.getPageKey();
    if (!isIndexableSite(pageKey.getSite())) {
      LOGGER.debug("Page {} belongs to a {} site which users don't navigate, thus it isn't indexed",
                   pageKey,
                   pageKey.getSite().getType());
      return null;
    }
    List<CMSSetting> liveBlocks = findLiveContentBlockSettings(page);
    if (!liveBlocks.isEmpty()) {
      queueLiveBlockDocuments(page, liveBlocks, creation);
      LOGGER.debug("Page {} carries {} content block(s) which are indexed on their own, thus no bare page document is indexed",
                   pageKey,
                   liveBlocks.size());
      return null;
    }
    String pagePath = urlResolverService.resolvePath(pageKey);
    if (StringUtils.isBlank(pagePath)) {
      LOGGER.debug("Page {} isn't reachable from any navigation node, thus it isn't indexed", pageKey);
      return null;
    }
    return buildDocument(id, page, pagePath);
  }

  /**
   * The block documents represent a page carrying live blocks — and the bare
   * page document request that lands here may be the only one that ever
   * reaches the page: a full reindex enumerates block ids through the
   * content-block plugins, which other addons register once their Spring
   * context has finished booting, i.e. after the Kernel start a
   * reindex-on-upgrade runs at, so that enumeration can have found none; and
   * a page saved outside the Layout addon — a {@code PageImporter} re-import,
   * a site template applied again, any addon calling
   * {@code LayoutService.save(page)} — broadcasts the portal's own
   * {@code PAGE_CREATED}/{@code PAGE_UPDATED} only, nothing for its blocks.
   * Queuing them here makes both paths converge on the right documents one
   * indexing cycle later.
   * <p>
   * How many of them are queued depends on the operation kind, because the
   * index can only be asked what it holds when nothing is deleting from it:
   * <ul>
   * <li>a <em>creation</em> ({@link #create}) queues every live block without
   * asking. A full reindex starts with a {@code _delete_by_query} that is not
   * refreshed, so a search issued right after it still sees the documents it
   * deleted and would wrongly report every block as present — and a creation
   * is exactly what a full reindex, or a page that just came to exist,
   * requests, i.e. a page whose blocks are by definition not indexed yet;</li>
   * <li>a <em>refresh</em> ({@link #update}) queues only the blocks the index
   * lacks. Nothing deletes from the index on that path, so its answer holds
   * (at worst a block indexed in the last second is queued once more), and
   * skipping the blocks it holds is what keeps a save through the Layout
   * addon from indexing each block twice: such a save reaches this connector
   * twice for one edit — {@code layout.page.updated} makes
   * {@link io.meeds.social.cms.listener.PageContentBlockIndexingListener}
   * reindex the live blocks directly, while the portal's own
   * {@code PAGE_UPDATED} makes
   * {@link io.meeds.social.cms.listener.PageSavedIndexingListener} refresh
   * the bare page document that lands here.</li>
   * </ul>
   * What a refresh does <em>not</em> cover, by design: a save outside the
   * Layout addon that only changes the page's title or permissions leaves
   * already-indexed block documents as they were, and one that gives the
   * page its first block leaves the bare page document in place, until the
   * Layout addon next saves the page (its listener reconciles and sweeps
   * stale documents) or a full reindex runs.
   *
   * @param page       the page carrying the blocks
   * @param liveBlocks the page's live content block settings
   * @param creation   whether every block is queued (a creation) or only the
   *                   ones the index lacks (a refresh)
   */
  private void queueLiveBlockDocuments(Page page, List<CMSSetting> liveBlocks, boolean creation) {
    String storageId = page.getStorageId();
    Set<String> indexedIds = creation ? Set.of() : new HashSet<>(searchConnector.findIndexedDocumentIds(storageId));
    liveBlocks.stream()
              .filter(setting -> !indexedIds.contains(buildBlockId(storageId, setting.getType(), setting.getName())))
              .forEach(setting -> pluginService.reindexContentBlock(setting.getType(), setting.getName()));
  }

  /**
   * @param  id   the content block document id
   * @param  page the page carrying the block
   * @return the content block document, or {@code null} when the block
   *         doesn't exist on the page anymore
   */
  private Document createBlockDocument(String id, Page page) {
    PageKey pageKey = page.getPageKey();
    PageContentBlock content = findContentBlock(page, parseBlockHash(id));
    if (content == null) {
      LOGGER.warn("Content block {} doesn't exist anymore on page {}, thus it can't be indexed", id, pageKey);
      return null;
    }
    Document document = buildDocument(id, page, urlResolverService.resolvePath(pageKey));
    document.setLastUpdatedDate(content.getDate());
    document.getFields().put("author", content.getAuthor());
    Set<String> contentLanguages = new HashSet<>();
    if (content.getContent() != null) {
      content.getContent().forEach((lang, text) -> {
        document.getFields().put(contentFieldName(lang), text);
        if (StringUtils.isNotBlank(lang)) {
          contentLanguages.add(lang);
        }
      });
    }
    document.addListField(CONTENT_LANGUAGES_FIELD, contentLanguages);
    return document;
  }

  /**
   * The fields both document kinds share: what identifies the page, what
   * the search result displays about it, and who may see it.
   *
   * @param  id       the document id
   * @param  page     the page the document describes
   * @param  pagePath the page's front-end path, or {@code null}/blank if none
   * @return the document, with a mutable field map callers can complete
   */
  private Document buildDocument(String id, Page page, String pagePath) {
    PageKey pageKey = page.getPageKey();
    Map<String, String> fields = new HashMap<>();
    fields.put("pageStorageId", page.getStorageId());
    fields.put("siteName", pageKey.getSite().getName());
    fields.put("siteType", pageKey.getSite().getType().getName());
    fields.put("pageName", pageKey.getName());
    if (StringUtils.isNotBlank(page.getTitle())) {
      fields.put("pageTitle", page.getTitle());
    }
    if (StringUtils.isNotBlank(pagePath)) {
      fields.put("pagePath", pagePath);
    }
    Document document = new Document();
    document.setId(id);
    document.setPermissions(page.getAccessPermissions() == null ? new HashSet<>()
                                                                : new HashSet<>(Arrays.asList(page.getAccessPermissions())));
    document.setFields(fields);
    return document;
  }

  /**
   * The same "live block" rule as {@link #getAllBlockIds} and the indexing
   * listener: a {@link CMSSetting} counts only while a widget carrying its
   * name still sits on the page's layout — whether that widget's content
   * currently resolves is deliberately not part of it (see the class
   * javadoc).
   *
   * @param  page the page to check
   * @return the {@link CMSSetting}s of the page's live content blocks, of
   *         any registered content type — empty when it carries none
   */
  private List<CMSSetting> findLiveContentBlockSettings(Page page) {
    Set<String> widgetSettingNames = PageContentBlockUtils.collectWidgetSettingNames(layoutService, page);
    if (widgetSettingNames.isEmpty()) {
      return List.of();
    }
    String pageReference = page.getPageKey().format();
    return pluginService.getContentTypes()
                        .stream()
                        .flatMap(type -> cmsService.getSettingsByTypeAndPageReference(type, pageReference).stream())
                        .filter(setting -> widgetSettingNames.contains(setting.getName()))
                        .toList();
  }

  /**
   * @param  id an indexed document id
   * @return whether {@code id} designates a bare page document (a page
   *         storage id carrying no block hash) rather than a content block
   *         one
   */
  public static boolean isPageDocumentId(String id) {
    return id != null && PAGE_DOCUMENT_ID_PATTERN.matcher(id).matches();
  }

  /**
   * A page currently being edited as a draft is cloned by the Layout addon
   * as either a whole draft site ({@link SiteType#DRAFT}) or, within the
   * same site, a page named {@code <original>_draft_<username>} — neither
   * is a published page and must not be indexed.
   *
   * @param pageKey the page key to check
   * @return {@code true} if the page is a draft
   */
  public static boolean isDraftPage(PageKey pageKey) {
    return pageKey.getSite().getType() == SiteType.DRAFT || StringUtils.contains(pageKey.getName(), "_draft_");
  }

  /**
   * @param  siteKey a site key
   * @return whether the site is one users reach through a navigation, i.e.
   *         whose pages may surface as bare page documents
   */
  public static boolean isIndexableSite(SiteKey siteKey) {
    return siteKey != null && INDEXABLE_SITE_TYPES.contains(siteKey.getType());
  }

  /**
   * Every content block document id, in a deterministic order: the
   * underlying content types (a Set) and settings carry no natural order
   * otherwise, which would let a full reindex skip or repeat blocks across
   * batches.
   * <p>
   * Every reason a setting can be disqualified (blank/draft page reference,
   * page gone, or the widget that created the block no longer present on the
   * page) is evaluated here, before {@link #getAllIds} applies skip/limit,
   * so a disqualified setting never consumes a slot in a batch — the block
   * portion of the ids has no store-side paging to lean on, unlike the page
   * portion.
   * <p>
   * Pages are resolved through a per-call memo: re-walking the settings that
   * precede {@code offset} on every batch then costs at most one page load
   * per distinct page reference, not one per setting.
   */
  private List<String> getAllBlockIds() {
    Map<String, ResolvedPage> resolvedPages = new HashMap<>();
    return pluginService.getContentTypes()
                        .stream()
                        .sorted()
                        .flatMap(type -> cmsService.getSettingsByType(type)
                                                   .stream()
                                                   .filter(s -> StringUtils.isNotBlank(s.getPageReference()))
                                                   .filter(s -> isNotDraftPageReference(s.getPageReference()))
                                                   .sorted(Comparator.comparing(CMSSetting::getName))
                                                   .map(s -> buildBlockId(type, s, resolvedPages)))
                        .filter(StringUtils::isNotBlank)
                        .toList();
  }

  /**
   * @param  offset the offset within the store's own page ordering
   * @param  limit  the number of pages to return at most
   * @return the storage ids of the pages of every site, as the store pages
   *         them — see {@link #getAllIds} for why nothing is filtered here
   */
  private List<String> getPageIds(int offset, int limit) {
    QueryResult<PageContext> pages = layoutService.findPages(offset, limit, null, null, null, null);
    if (pages == null) {
      return List.of();
    }
    return StreamSupport.stream(pages.spliterator(), false)
                        .map(PageContext::getState)
                        .filter(Objects::nonNull)
                        .map(PageState::getStorageId)
                        .filter(StringUtils::isNotBlank)
                        .toList();
  }

  /**
   * @param  id   the bare page document id
   * @param  page the page it designates
   * @return the bare page document, or {@code null} when the page is
   *         represented by its content block documents instead, belongs to
   *         a site nobody navigates, or can't be reached from any navigation
   *         node (a result leading nowhere is worse than none)
   */
  private Document createPageDocument(String id, Page page) {
    PageKey pageKey = page.getPageKey();
    if (!isIndexableSite(pageKey.getSite())) {
      LOGGER.debug("Page {} belongs to a {} site which users don't navigate, thus it isn't indexed",
                   pageKey,
                   pageKey.getSite().getType());
      return null;
    }
    List<CMSSetting> liveBlocks = findLiveContentBlockSettings(page);
    if (!liveBlocks.isEmpty()) {
      queueMissingBlockDocuments(page, liveBlocks);
      LOGGER.debug("Page {} carries {} content block(s) which are indexed on their own, thus no bare page document is indexed",
                   pageKey,
                   liveBlocks.size());
      return null;
    }
    String pagePath = urlResolverService.resolvePath(pageKey);
    if (StringUtils.isBlank(pagePath)) {
      LOGGER.debug("Page {} isn't reachable from any navigation node, thus it isn't indexed", pageKey);
      return null;
    }
    return buildDocument(id, page, pagePath);
  }

  /**
   * The block documents represent a page carrying live blocks — and the bare
   * page document request that lands here may be the only one that ever
   * reaches the page: a full reindex enumerates block ids through the
   * content-block plugins, which other addons register once their Spring
   * context has finished booting, i.e. after the Kernel start a
   * reindex-on-upgrade runs at, so that enumeration can have found none;
   * and a page stored outside the Layout services broadcasts nothing for its
   * blocks. Queuing them here makes both paths converge on the right
   * documents one indexing cycle later.
   * <p>
   * Only the blocks the index does <em>not</em> hold yet are queued, though:
   * a page saved through the Layout addon reaches this connector twice for
   * one edit — {@code layout.page.updated} makes
   * {@link io.meeds.social.cms.listener.PageContentBlockIndexingListener}
   * reindex the live blocks directly, while the portal's own
   * {@code PAGE_UPDATED} makes
   * {@link io.meeds.social.cms.listener.PageSavedIndexingListener} queue
   * the bare page document that ends here — and the indexing queue
   * executes every queued operation, duplicates included. One lookup of
   * what is indexed under the page (the query
   * {@link PageContentSearchConnector#findIndexedDocumentIds} already runs
   * for reconciliation) keeps that second pass from re-queuing every block a
   * second time; a block whose document is present is left to the listener
   * that owns its refresh.
   *
   * @param page       the page carrying the blocks
   * @param liveBlocks the page's live content block settings
   */
  private void queueMissingBlockDocuments(Page page, List<CMSSetting> liveBlocks) {
    String storageId = page.getStorageId();
    Set<String> indexedIds = new HashSet<>(searchConnector.findIndexedDocumentIds(storageId));
    liveBlocks.stream()
              .filter(setting -> !indexedIds.contains(buildBlockId(storageId, setting.getType(), setting.getName())))
              .forEach(setting -> pluginService.reindexContentBlock(setting.getType(), setting.getName()));
  }

  /**
   * @param  id   the content block document id
   * @param  page the page carrying the block
   * @return the content block document, or {@code null} when the block
   *         doesn't exist on the page anymore
   */
  private Document createBlockDocument(String id, Page page) {
    PageKey pageKey = page.getPageKey();
    PageContentBlock content = findContentBlock(page, parseBlockHash(id));
    if (content == null) {
      LOGGER.warn("Content block {} doesn't exist anymore on page {}, thus it can't be indexed", id, pageKey);
      return null;
    }
    Document document = buildDocument(id, page, urlResolverService.resolvePath(pageKey));
    document.setLastUpdatedDate(content.getDate());
    document.getFields().put("author", content.getAuthor());
    Set<String> contentLanguages = new HashSet<>();
    if (content.getContent() != null) {
      content.getContent().forEach((lang, text) -> {
        document.getFields().put(contentFieldName(lang), text);
        if (StringUtils.isNotBlank(lang)) {
          contentLanguages.add(lang);
        }
      });
    }
    document.addListField(CONTENT_LANGUAGES_FIELD, contentLanguages);
    return document;
  }

  /**
   * The fields both document kinds share: what identifies the page, what
   * the search result displays about it, and who may see it.
   *
   * @param  id       the document id
   * @param  page     the page the document describes
   * @param  pagePath the page's front-end path, or {@code null}/blank if none
   * @return the document, with a mutable field map callers can complete
   */
  private Document buildDocument(String id, Page page, String pagePath) {
    PageKey pageKey = page.getPageKey();
    Map<String, String> fields = new HashMap<>();
    fields.put("pageStorageId", page.getStorageId());
    fields.put("siteName", pageKey.getSite().getName());
    fields.put("siteType", pageKey.getSite().getType().getName());
    fields.put("pageName", pageKey.getName());
    if (StringUtils.isNotBlank(page.getTitle())) {
      fields.put("pageTitle", page.getTitle());
    }
    if (StringUtils.isNotBlank(pagePath)) {
      fields.put("pagePath", pagePath);
    }
    Document document = new Document();
    document.setId(id);
    document.setPermissions(page.getAccessPermissions() == null ? new HashSet<>()
                                                                : new HashSet<>(Arrays.asList(page.getAccessPermissions())));
    document.setFields(fields);
    return document;
  }

  /**
   * The same "live block" rule as {@link #getAllBlockIds} and the indexing
   * listener: a {@link CMSSetting} counts only while a widget carrying its
   * name still sits on the page's layout — whether that widget's content
   * currently resolves is deliberately not part of it (see the class
   * javadoc).
   *
   * @param  page the page to check
   * @return the {@link CMSSetting}s of the page's live content blocks, of
   *         any registered content type — empty when it carries none
   */
  private List<CMSSetting> findLiveContentBlockSettings(Page page) {
    Set<String> widgetSettingNames = PageContentBlockUtils.collectWidgetSettingNames(layoutService, page);
    if (widgetSettingNames.isEmpty()) {
      return List.of();
    }
    String pageReference = page.getPageKey().format();
    return pluginService.getContentTypes()
                        .stream()
                        .flatMap(type -> cmsService.getSettingsByTypeAndPageReference(type, pageReference).stream())
                        .filter(setting -> widgetSettingNames.contains(setting.getName()))
                        .toList();
  }

  /**
   * Resolves the single content block identified by {@code blockHash} among
   * every block bound to {@code page}, across every registered content type.
   * <p>
   * The block is matched by hash first, and only the one setting it resolves
   * to is then checked against the page's layout — the widget-presence rule
   * has to be enforced here, in the single funnel every indexed document goes
   * through (an explicit reindex request, e.g. Notes reacting to its own
   * {@code note.updated}, resolves a block id from the {@code CMSSetting}
   * alone and would otherwise re-add a block whose widget was removed), but
   * doing it this way around loads one widget's portlet preferences instead of
   * every widget's on the page.
   *
   * @param page the page carrying the block
   * @param blockHash the block's hash portion, as parsed from its document id
   * @return the resolved {@link PageContentBlock}, or {@code null} if none matched
   */
  private PageContentBlock findContentBlock(Page page, String blockHash) {
    String pageReference = page.getPageKey().format();
    for (String contentType : pluginService.getContentTypes()) {
      PageContentBlockPlugin plugin = pluginService.getPlugin(contentType);
      if (plugin == null) {
        continue;
      }
      CMSSetting setting = cmsService.getSettingsByTypeAndPageReference(contentType, pageReference)
                                     .stream()
                                     .filter(s -> StringUtils.equals(blockHash(contentType, s.getName()), blockHash))
                                     .findFirst()
                                     .orElse(null);
      if (setting != null) {
        // The hash is built from the content type and the setting name, so a
        // match identifies this very block: if its widget is gone, no other
        // content type can provide it either
        return PageContentBlockUtils.hasWidgetWithSettingName(layoutService, page, setting.getName()) ?
                                                                                                     plugin.getContent(setting) :
                                                                                                     null;
      }
    }
    return null;
  }

  /**
   * A page resolved once per {@link #getAllIds} call: its storage id, plus
   * the setting names of the content-block widgets it currently carries.
   *
   * @param storageId the page's storage id
   * @param widgetSettingNames setting names of the widgets currently present on the page
   */
  private record ResolvedPage(String storageId, Set<String> widgetSettingNames) {
  }

  /**
   * @param  contentType the content block's content type
   * @param  setting the {@link CMSSetting} binding the block to its page
   * @param  resolvedPages per-call memo of already resolved page references
   * @return the block's document id, or {@code null} when the setting's page
   *         is gone or no widget on it carries the setting's name anymore
   */
  private String buildBlockId(String contentType, CMSSetting setting, Map<String, ResolvedPage> resolvedPages) {
    String pageReference = setting.getPageReference();
    // computeIfAbsent can't be used: an unresolvable page reference has to be
    // memoized as null too, otherwise it's re-resolved on every batch
    if (!resolvedPages.containsKey(pageReference)) {
      resolvedPages.put(pageReference, resolvePage(pageReference));
    }
    ResolvedPage resolvedPage = resolvedPages.get(pageReference);
    if (resolvedPage == null || !resolvedPage.widgetSettingNames().contains(setting.getName())) {
      return null;
    }
    return buildBlockId(resolvedPage.storageId(), contentType, setting.getName());
  }

  private ResolvedPage resolvePage(String pageReference) {
    try {
      Page page = layoutService.getPage(PageKey.parse(pageReference));
      return page == null ? null
                          : new ResolvedPage(page.getStorageId(),
                                             PageContentBlockUtils.collectWidgetSettingNames(layoutService, page));
    } catch (Exception e) {
      LOGGER.debug("Cannot resolve storage id of page {}", pageReference, e);
      return null;
    }
  }

  /**
   * @param pageStorageId the storage id of the page carrying the block
   * @param contentType the content block's content type
   * @param settingName the content block's setting name
   * @return the document id for the content block named {@code settingName}
   *         (of type {@code contentType}) bound to the page whose storage
   *         id is {@code pageStorageId}.
   */
  public static String buildBlockId(String pageStorageId, String contentType, String settingName) {
    return pageStorageId + "_" + blockHash(contentType, settingName);
  }

  /**
   * @param contentType the content block's content type
   * @param settingName the content block's setting name
   * @return a 64-bit hex digest of {@code contentType}/{@code settingName} —
   *         collision-resistant enough that two distinct blocks on the same
   *         page won't silently share one document, unlike a 32-bit
   *         {@link java.util.Objects#hash}.
   */
  private static String blockHash(String contentType, String settingName) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest((contentType + ' ' + settingName).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash, 0, 8);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }

  private boolean isNotDraftPageReference(String pageReference) {
    try {
      return !isDraftPage(PageKey.parse(pageReference));
    } catch (Exception e) {
      LOGGER.debug("Cannot parse page reference {}", pageReference, e);
      return true;
    }
  }

  /**
   * @param  blockId a content block document id ({@code buildBlockId}) or a
   *                  plain page storage id — either way, the leading
   *                  {@code page_<numeric DB id>} portion is parsed out.
   * @return the numeric storage id of the page carrying the block.
   */
  public static long parsePageId(String blockId) {
    // PageStorageImpl builds page storage ids as "page_" + <numeric DB id>,
    // this connector appends "_" + <block hash> to that
    String withoutPrefix = StringUtils.removeStart(blockId, PAGE_STORAGE_ID_PREFIX);
    int separatorIndex = withoutPrefix.indexOf('_');
    return Long.parseLong(separatorIndex < 0 ? withoutPrefix : withoutPrefix.substring(0, separatorIndex));
  }

  private String parseBlockHash(String blockId) {
    int separatorIndex = blockId.lastIndexOf('_');
    return separatorIndex < 0 ? "" : blockId.substring(separatorIndex + 1);
  }

}
