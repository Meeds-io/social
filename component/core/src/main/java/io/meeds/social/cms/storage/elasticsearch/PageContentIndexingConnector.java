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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  public static final String                  TYPE                 = "page";

  private static final String                 PAGE_STORAGE_ID_PREFIX = "page_";

  private static final Log                    LOGGER               = ExoLogger.getExoLogger(PageContentIndexingConnector.class);

  private static final String                 CONTENT_MAPPING      = """
        "@field@" : {
          "type" : "text",
          "index_options": "offsets",
          "term_vector": "with_positions_offsets",
          "store": true
        }
      """;

  private static final String                 ES_MAPPING           = """
      {
        "properties" : {
          "siteName" : {"type" : "keyword"},
          "siteType" : {"type" : "keyword"},
          "pageName" : {"type" : "keyword"},
          "pageTitle" : {"type" : "text"},
          "pagePath" : {"type" : "keyword"},
          "author" : {"type" : "keyword"},
          "permissions" : {"type" : "keyword"},
          @content_mappings@
        }
      }
      """;

  private final PageContentBlockPluginService pluginService;

  private final CMSService                   cmsService;

  private final LayoutService                 layoutService;

  private final LocaleConfigService           localeConfigService;

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
    return pluginService.getContentTypes()
                        .stream()
                        .flatMap(type -> cmsService.getSettingsByType(type)
                                                   .stream()
                                                   .filter(s -> StringUtils.isNotBlank(s.getPageReference()))
                                                   .filter(s -> isNotDraftPageReference(s.getPageReference()))
                                                   .map(s -> buildBlockId(type, s)))
                        .filter(StringUtils::isNotBlank)
                        .distinct()
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

      PageContentBlock content = findContentBlock(pageKey.format(), parseBlockHash(id));
      if (content == null) {
        LOGGER.warn("Content block {} doesn't exist anymore on page {}, thus it can't be indexed", id, pageKey);
        return null;
      }

      Map<String, String> fields = new HashMap<>();
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
      if (content.getContent() != null) {
        content.getContent().forEach((lang, text) -> fields.put(contentFieldName(lang), text));
      }

      Document document = new Document();
      document.setId(id);
      document.setLastUpdatedDate(content.getDate());
      document.setPermissions(page.getAccessPermissions() == null ? new HashSet<>()
                                                                    : new HashSet<>(Arrays.asList(page.getAccessPermissions())));
      document.setFields(fields);
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
   * every block bound to {@code pageReference}, across every registered
   * content type.
   */
  private PageContentBlock findContentBlock(String pageReference, String blockHash) {
    for (String contentType : pluginService.getContentTypes()) {
      PageContentBlockPlugin plugin = pluginService.getPlugin(contentType);
      if (plugin == null) {
        continue;
      }
      CMSSetting setting = cmsService.getSettingsByType(contentType)
                                     .stream()
                                     .filter(s -> StringUtils.equals(s.getPageReference(), pageReference))
                                     .filter(s -> StringUtils.equals(blockHash(contentType, s.getName()), blockHash))
                                     .findFirst()
                                     .orElse(null);
      if (setting != null) {
        return plugin.getContent(setting);
      }
    }
    return null;
  }

  private String buildBlockId(String contentType, CMSSetting setting) {
    try {
      Page page = layoutService.getPage(PageKey.parse(setting.getPageReference()));
      return page == null ? null : buildBlockId(page.getStorageId(), contentType, setting.getName());
    } catch (Exception e) {
      LOGGER.debug("Cannot resolve storage id of page {}", setting.getPageReference(), e);
      return null;
    }
  }

  /**
   * @return the document id for the content block named {@code settingName}
   *         (of type {@code contentType}) bound to the page whose storage
   *         id is {@code pageStorageId}.
   */
  public static String buildBlockId(String pageStorageId, String contentType, String settingName) {
    return pageStorageId + "_" + blockHash(contentType, settingName);
  }

  private static String blockHash(String contentType, String settingName) {
    return Integer.toHexString(Objects.hash(contentType, settingName));
  }

  /**
   * A page currently being edited as a draft is cloned by the Layout addon
   * as either a whole draft site ({@link SiteType#DRAFT}) or, within the
   * same site, a page named {@code <original>_draft_<username>} — neither
   * is a published page and must not be indexed.
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
