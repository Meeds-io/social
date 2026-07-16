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
package io.meeds.social.cms.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerBase;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.service.CMSService;
import io.meeds.social.cms.service.PageContentBlockPluginService;
import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;

import jakarta.annotation.PostConstruct;

/**
 * Reacts to Layout's {@code layout.page.updated} / {@code layout.page.permissions.updated}
 * events to keep the "page" search index in sync with whether a page
 * currently carries a content block backed by any registered
 * {@link io.meeds.social.cms.plugin.PageContentBlockPlugin}.
 */
@Component
public class PageContentBlockIndexingListener implements ListenerBase<String, String> {

  public static final String          PAGE_UPDATED_EVENT             = "layout.page.updated";

  public static final String          PAGE_PERMISSIONS_UPDATED_EVENT = "layout.page.permissions.updated";

  @Autowired
  private ListenerService             listenerService;

  @Autowired
  private IndexingService             indexingService;

  @Autowired
  private CMSService                  cmsService;

  @Autowired
  private PageContentBlockPluginService pluginService;

  @Autowired
  private LayoutService               layoutService;

  @PostConstruct
  public void init() {
    listenerService.addListener(PAGE_UPDATED_EVENT, this);
    listenerService.addListener(PAGE_PERMISSIONS_UPDATED_EVENT, this);
  }

  @Override
  public void onEvent(Event<String, String> event) throws Exception {
    String pageRef = event.getData();
    if (StringUtils.isBlank(pageRef)) {
      return;
    }
    Page page = layoutService.getPage(PageKey.parse(pageRef));
    if (page == null) {
      return;
    }
    String storageId = page.getStorageId();
    if (hasContentBlock(pageRef)) {
      indexingService.index(PageContentIndexingConnector.TYPE, storageId);
    } else {
      indexingService.unindex(PageContentIndexingConnector.TYPE, storageId);
    }
  }

  private boolean hasContentBlock(String pageRef) {
    return pluginService.getContentTypes()
                        .stream()
                        .flatMap(type -> cmsService.getSettingsByType(type).stream())
                        .map(CMSSetting::getPageReference)
                        .anyMatch(pageReference -> StringUtils.equals(pageReference, pageRef));
  }

}
