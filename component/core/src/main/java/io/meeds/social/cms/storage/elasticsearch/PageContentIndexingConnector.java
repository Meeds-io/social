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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageKey;
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
 * Indexes a portal Page's content blocks bound through a {@link CMSSetting}
 * whose type is backed by a registered {@link PageContentBlockPlugin} —
 * generic across content-block types, no knowledge of any specific addon
 * (e.g. Notes' Single Note View).
 * <p>
 * A page can carry more than one content block (e.g. several Single Note
 * View blocks placed in different sections of the same page); each one is
 * indexed as its own document, so that unified search returns one result
 * per block rather than a single blended excerpt for the whole page.
 * <p>
 * The document id is {@code <page's numeric storage id>_<block hash>} (not
 * {@link PageKey#format()}, which can exceed the 50-character limit of the
 * {@code ES_INDEXING_QUEUE.ENTITY_ID} column, nor the setting's own name,
 * which can be arbitrarily long) — the page id portion stays stable across
 * page/site renames, unlike the formatted key.
 */
public class PageContentIndexingConnector extends ElasticIndexingServiceConnector {

  /** The ES connector/entity type name for indexed page content blocks. */
  public static final String                  TYPE                 = "page";

  /** Prefix a page's storage id is rendered with (e.g. {@code page_139}). */
  private static final String                 PAGE_STORAGE_ID_PREFIX = "page_";

  /** Class-level logger. */
  private static final Log                    LOGGER               = ExoLogger.getExoLogger(PageContentIndexingConnector.class);

  /** ES mapping template for a single per-language content field. */
  private static final String                 CONTENT_MAPPING      = """
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
  public static final String                  CONTENT_LANGUAGES_FIELD = "contentLanguages";

  /** ES index mapping for the "page" document type. */
  private static final String                 ES_MAPPING           = """
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

  /** Used to enumerate the {@link io.meeds.social.cms.model.CMSSetting}s of a content type. */
  private final CMSService                   cmsService;

  /** Used to resolve a setting's page and its metadata (site, title, permissions...). */
  private final LayoutService                 layoutService;

  /** Used to enumerate the configured languages a content block can be translated into. */
  private final LocaleConfigService           localeConfigService;

  /** Used to resolve a page's front-end URL, when one exists. */
  private final PageUrlResolverService        urlResolverService;

  public PageContentIndexingConnector(PageContentBlockPluginService pluginService,
                                      CMSService cmsService,
                                      LayoutService layoutService,
                                      LocaleConfigService localeConfigService,
                                      PageUrlResolverService urlResolverService,
                                      InitParams initParams) {
    super(initParams);
    this.pluginService = pluginService;
    this.cmsService = cmsService;
    this.layoutService = layoutService;
    this.localeConfigService = localeConfigService;
    this.urlResolverService = urlResolverService;
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

  @Override
  public List<String> getAllIds(int offset, int limit) {
    // Sorted so that skip/limit page deterministically across calls — the
    // underlying content types (a Set) and settings carry no natural order
    // otherwise, which would let a full reindex skip or repeat blocks across
    // batches.
    //
    // Every reason a setting can be disqualified (blank/draft page
    // reference, page gone, or the widget that created the block no longer
    // present on the page) is evaluated BEFORE skip/limit, so a disqualified
    // setting never consumes a slot in the batch:
    // ElasticIndexingOperationProcessor#reindexAll loops while the returned
    // batch size equals the batch size it asked for, so a batch coming back
    // even one id short — after it has already queued a DELETE_ALL — ends
    // the reindex and leaves every remaining block unindexed.
    //
    // Pages are resolved through a per-call memo: re-walking the settings
    // that precede `offset` on every batch then costs at most one page load
    // per distinct page reference, not one per setting.
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
                        .skip(offset)
                        .limit(limit)
                        .toList();
  }

  @Override
  public Document create(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Id is null");
    }
    try {
      Page page = layoutService.getPage(parsePageId(id));
      if (page == null) {
        LOGGER.warn("Page for content block {} wasn't found, thus it can't be indexed", id);
        return null;
      }
      PageKey pageKey = page.getPageKey();
      if (isDraftPage(pageKey)) {
        LOGGER.debug("Page {} is a draft, thus it can't be indexed", pageKey);
        return null;
      }

      PageContentBlock content = findContentBlock(page, parseBlockHash(id));
      if (content == null) {
        LOGGER.warn("Content block {} doesn't exist anymore on page {}, thus it can't be indexed", id, pageKey);
        return null;
      }

      Map<String, String> fields = new HashMap<>();
      fields.put("pageStorageId", page.getStorageId());
      fields.put("siteName", pageKey.getSite().getName());
      fields.put("siteType", pageKey.getSite().getType().getName());
      fields.put("pageName", pageKey.getName());
      if (StringUtils.isNotBlank(page.getTitle())) {
        fields.put("pageTitle", page.getTitle());
      }
      String pagePath = urlResolverService.resolvePath(pageKey);
      if (StringUtils.isNotBlank(pagePath)) {
        fields.put("pagePath", pagePath);
      }
      fields.put("author", content.getAuthor());
      Set<String> contentLanguages = new HashSet<>();
      if (content.getContent() != null) {
        content.getContent().forEach((lang, text) -> {
          fields.put(contentFieldName(lang), text);
          if (StringUtils.isNotBlank(lang)) {
            contentLanguages.add(lang);
          }
        });
      }

      Document document = new Document();
      document.setId(id);
      document.setLastUpdatedDate(content.getDate());
      document.setPermissions(page.getAccessPermissions() == null ? new HashSet<>()
                                                                    : new HashSet<>(Arrays.asList(page.getAccessPermissions())));
      document.setFields(fields);
      document.addListField(CONTENT_LANGUAGES_FIELD, contentLanguages);
      return document;
    } catch (Exception e) {
      LOGGER.warn("Cannot index content block with id {}", id, e);
      return null;
    }
  }

  @Override
  public Document update(String id) {
    return create(id);
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

  /**
   * A page currently being edited as a draft is cloned by the Layout addon
   * as either a whole draft site ({@link SiteType#DRAFT}) or, within the
   * same site, a page named {@code <original>_draft_<username>} — neither
   * is a published page and must not be indexed.
   *
   * @param pageKey the page key to check
   * @return {@code true} if the page is a draft
   */
  private boolean isDraftPage(PageKey pageKey) {
    return pageKey.getSite().getType() == SiteType.DRAFT || StringUtils.contains(pageKey.getName(), "_draft_");
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
