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
package io.meeds.social.cms.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.plugin.PageContentBlockPlugin;
import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;

@Service
public class PageContentBlockPluginServiceImpl implements PageContentBlockPluginService {

  /** Class-level logger. */
  private static final Log LOGGER = ExoLogger.getExoLogger(PageContentBlockPluginServiceImpl.class);

  /** Used to resolve a content type's {@link CMSSetting}. */
  private final CMSService      cmsService;

  /** Used to resolve the page a {@link CMSSetting} is bound to. */
  private final LayoutService   layoutService;

  /** Used to trigger a reindex of a content block's ES document. */
  private final IndexingService indexingService;

  /** Registered {@link PageContentBlockPlugin}s, keyed by content type. */
  private final Map<String, PageContentBlockPlugin> plugins = new HashMap<>();

  public PageContentBlockPluginServiceImpl(CMSService cmsService, LayoutService layoutService, IndexingService indexingService) {
    this.cmsService = cmsService;
    this.layoutService = layoutService;
    this.indexingService = indexingService;
  }

  @Override
  public void addPlugin(PageContentBlockPlugin plugin) {
    plugins.put(plugin.getContentType(), plugin);
  }

  @Override
  public Set<String> getContentTypes() {
    return plugins.keySet();
  }

  @Override
  public PageContentBlockPlugin getPlugin(String contentType) {
    return plugins.get(contentType);
  }

  @Override
  public void reindexContentBlock(String contentType, String settingName) {
    String blockId = resolveBlockId(contentType, settingName);
    if (blockId != null) {
      indexingService.reindex(PageContentIndexingConnector.TYPE, blockId);
    }
  }

  @Override
  public void unindexContentBlock(String contentType, String settingName) {
    String blockId = resolveBlockId(contentType, settingName);
    if (blockId != null) {
      indexingService.unindex(PageContentIndexingConnector.TYPE, blockId);
    }
  }

  private String resolveBlockId(String contentType, String settingName) {
    CMSSetting setting = cmsService.getSetting(contentType, settingName);
    if (setting == null) {
      return null;
    }
    Page page;
    try {
      page = layoutService.getPage(PageKey.parse(setting.getPageReference()));
    } catch (Exception e) {
      LOGGER.debug("Cannot parse page reference {}", setting.getPageReference(), e);
      return null;
    }
    if (page == null) {
      return null;
    }
    return PageContentIndexingConnector.buildBlockId(page.getStorageId(), contentType, settingName);
  }

}
