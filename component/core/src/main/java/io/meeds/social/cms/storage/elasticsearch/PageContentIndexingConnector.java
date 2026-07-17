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
 * Indexes a portal Page as soon as it carries a content block bound through
 * a {@link CMSSetting} whose type is backed by a registered
 * {@link PageContentBlockPlugin} — generic across content-block types, no
 * knowledge of any specific addon (e.g. Notes' Single Note View).
 * <p>
 * The document id is the page's numeric storage id (not its
 * {@link PageKey#format()}, which can exceed the 50-character limit of the
 * {@code ES_INDEXING_QUEUE.ENTITY_ID} column) — it is also stable across
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
          "pageTitle" : {"type" : "keyword"},
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
                        .flatMap(type -> cmsService.getSettingsByType(type).stream())
                        .map(CMSSetting::getPageReference)
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .filter(this::isNotDraftPageReference)
                        .map(this::resolveStorageId)
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
      Page page = layoutService.getPage(parseStorageId(id));
      if (page == null) {
        LOGGER.warn("Page with storage id {} wasn't found, thus it can't be indexed", id);
        return null;
      }
      PageKey pageKey = page.getPageKey();
      if (isDraftPage(pageKey)) {
        LOGGER.debug("Page {} is a draft, thus it can't be indexed", pageKey);
        return null;
      }

      PageContentBlock content = findContentBlock(pageKey);
      if (content == null) {
        LOGGER.warn("Page {} doesn't carry any registered content block anymore, thus it can't be indexed", pageKey);
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
      LOGGER.warn("Cannot index page with id {}", id, e);
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

  private PageContentBlock findContentBlock(PageKey pageKey) {
    String pageReference = pageKey.format();
    for (String contentType : pluginService.getContentTypes()) {
      CMSSetting setting = cmsService.getSettingsByType(contentType)
                                     .stream()
                                     .filter(s -> StringUtils.equals(s.getPageReference(), pageReference))
                                     .findFirst()
                                     .orElse(null);
      if (setting == null) {
        continue;
      }
      PageContentBlockPlugin plugin = pluginService.getPlugin(contentType);
      PageContentBlock content = plugin == null ? null : plugin.getContent(setting);
      if (content != null) {
        return content;
      }
    }
    return null;
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

  private long parseStorageId(String storageId) {
    // PageStorageImpl builds page storage ids as "page_" + <numeric DB id>
    return Long.parseLong(StringUtils.removeStart(storageId, PAGE_STORAGE_ID_PREFIX));
  }

  private String resolveStorageId(String pageReference) {
    try {
      Page page = layoutService.getPage(PageKey.parse(pageReference));
      return page == null ? null : page.getStorageId();
    } catch (Exception e) {
      LOGGER.debug("Cannot resolve storage id of page {}", pageReference, e);
      return null;
    }
  }

}
