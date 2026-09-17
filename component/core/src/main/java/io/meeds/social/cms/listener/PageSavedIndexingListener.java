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

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;

/**
 * Reacts to the portal's own {@link LayoutService#PAGE_CREATED} /
 * {@link LayoutService#PAGE_UPDATED} events — broadcast by every path that
 * stores a page, the layout editor included — to index the bare page
 * document (see {@link PageContentIndexingConnector}) of a page that
 * {@link PageContentBlockIndexingListener} never hears about: a page
 * imported from a site or space template, injected by an addon's
 * configuration, or saved through the portal API rather than through the
 * Layout addon's services, which are the only ones broadcasting
 * {@code layout.page.*}.
 * <p>
 * Deliberately registered through Kernel configuration
 * ({@code indexing-configuration.xml}) rather than as a Spring
 * {@code @Component}: the Spring contexts finish booting in a post-init task
 * of the portal container, after every Kernel {@code Startable} has run —
 * including the one importing the product's sites on a fresh installation.
 * A Spring listener would therefore miss the events of exactly the pages
 * this one exists for. For the same reason it depends on Kernel services
 * only.
 * <p>
 * It only ever queues the bare page document itself: whether it materializes
 * is the connector's decision (a page carrying content blocks, or not
 * reachable from any navigation node, yields none), and a page's content
 * block documents are reconciled by the Layout events.
 * <p>
 * The portal events also fire for the saves the Layout addon performs
 * itself — the layout editor's own save, a page creation, a layout restore
 * — right before Layout broadcasts {@code layout.page.updated} for the same
 * page, so on those paths both this listener and
 * {@link PageContentBlockIndexingListener} run for one edit. This is
 * accepted deliberately, and its cost is bounded: the indexing queue
 * executes duplicates, but what this listener adds for an update is one
 * bare page document <em>refresh</em>, which the connector builds without
 * re-queuing the live blocks the other listener already refreshes (see
 * {@code PageContentIndexingConnector#update}). Only a creation queues them.
 * The two Layout saves that broadcast no {@code layout.page.*} event at all
 * (a section clone, a page link update) are what keeps {@code PAGE_UPDATED}
 * in this listener's scope.
 */
@Asynchronous
public class PageSavedIndexingListener extends Listener<Object, Page> {

  private static final Log      LOG = ExoLogger.getExoLogger(PageSavedIndexingListener.class);

  /** Used to queue the page document's (re)indexing. */
  private final IndexingService indexingService;

  /** Used to resolve the stored page, whose storage id the broadcast instance may lack. */
  private final LayoutService   layoutService;

  public PageSavedIndexingListener(IndexingService indexingService, LayoutService layoutService) {
    this.indexingService = indexingService;
    this.layoutService = layoutService;
  }

  @Override
  public void onEvent(Event<Object, Page> event) throws Exception {
    Page page = event.getData();
    if (page == null) {
      return;
    }
    PageKey pageKey = page.getPageKey();
    if (pageKey == null || PageContentIndexingConnector.isDraftPage(pageKey)) {
      // The layout editor stores its draft copy on every change; the
      // connector never indexes a draft, so there is nothing to queue for it
      return;
    }
    // The broadcast instance is the caller's own, whose storage id isn't
    // necessarily set when the page was just created: the stored page is
    String storageId = resolveStorageId(pageKey);
    if (StringUtils.isBlank(storageId)) {
      return;
    }
    if (LayoutService.PAGE_CREATED.equals(event.getEventName())) {
      // A creation: the connector also queues the content blocks the new
      // page may already carry, which nothing else would ever index
      indexingService.index(PageContentIndexingConnector.TYPE, storageId);
    } else {
      // A refresh: the page's content blocks, if any, are refreshed by the
      // Layout event listener, not re-queued from here
      indexingService.reindex(PageContentIndexingConnector.TYPE, storageId);
    }
  }

  private String resolveStorageId(PageKey pageKey) {
    try {
      Page storedPage = layoutService.getPage(pageKey);
      return storedPage == null ? null : storedPage.getStorageId();
    } catch (Exception e) {
      LOG.debug("Cannot resolve stored page {}, thus it can't be queued for indexing", pageKey, e);
      return null;
    }
  }

}
