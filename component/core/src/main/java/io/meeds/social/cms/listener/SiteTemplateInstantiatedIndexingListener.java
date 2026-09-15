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

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;

/**
 * Reacts to {@link org.exoplatform.portal.config.UserPortalConfigService#SITE_TEMPLATE_INSTANTIATED} —
 * broadcast once a site's configuration, pages <em>and</em> navigation have
 * all been created from a template, as happens for a new space's site or a
 * site an administrator creates — to index the bare page document (see
 * {@link PageContentIndexingConnector}) of every page of the new site.
 * <p>
 * {@link PageSavedIndexingListener} already hears each of those pages being
 * created, but at that moment the site's navigation doesn't exist yet: the
 * connector indexes a page only once it is reachable from a navigation node,
 * so a creation processed by the indexing job before the navigation import
 * completes yields nothing, and nothing else would ever come back to it —
 * an imported navigation broadcasts none of the Layout events. This event is
 * the first one guaranteed to fire after both.
 * <p>
 * Kernel-registered, for the same reason as {@link PageSavedIndexingListener}.
 */
@Asynchronous
public class SiteTemplateInstantiatedIndexingListener extends Listener<SiteKey, SiteKey> {

  /** Used to queue the page documents' (re)indexing. */
  private final IndexingService indexingService;

  /** Used to enumerate the new site's pages. */
  private final LayoutService   layoutService;

  public SiteTemplateInstantiatedIndexingListener(IndexingService indexingService, LayoutService layoutService) {
    this.indexingService = indexingService;
    this.layoutService = layoutService;
  }

  @Override
  public void onEvent(Event<SiteKey, SiteKey> event) throws Exception {
    SiteKey siteKey = event.getData();
    if (!PageContentIndexingConnector.isIndexableSite(siteKey)) {
      // Draft and template sites are instantiated from templates too, and
      // the connector indexes none of their pages
      return;
    }
    List<PageContext> pages = layoutService.findPages(siteKey);
    if (pages == null) {
      return;
    }
    pages.stream()
         .map(PageContext::getState)
         .filter(Objects::nonNull)
         .map(PageState::getStorageId)
         .filter(StringUtils::isNotBlank)
         .forEach(storageId -> indexingService.reindex(PageContentIndexingConnector.TYPE, storageId));
  }

}
