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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerBase;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;
import io.meeds.social.cms.storage.elasticsearch.PageContentSearchConnector;

import jakarta.annotation.PostConstruct;

/**
 * Unindexes every content block still indexed under a page that stopped
 * being searchable — the gap {@link PageContentBlockIndexingListener}
 * documents it cannot cover, since that listener only reacts to a page that
 * still exists and is still reachable.
 * <p>
 * Two distinct events lead here, and they are deliberately not conflated by
 * their broadcasters:
 * <ul>
 * <li>{@link LayoutService#PAGE_REMOVED} — the page's own data is gone.</li>
 * <li>{@value #PAGE_UNREACHABLE_EVENT} — the page still exists but no
 * navigation node leads to it anymore (Layout broadcasts it after deleting
 * the last node pointing to it), so it can't be reached from a search result
 * either.</li>
 * </ul>
 */
@Component
@Asynchronous
public class PageRemovedIndexingListener implements ListenerBase<Object, Page> {

  /**
   * Broadcast by Layout when a page stops being reachable through any
   * navigation node while still existing — must match
   * {@code io.meeds.layout.service.NavigationLayoutService.PAGE_UNREACHABLE_EVENT}
   * (Social doesn't depend on the Layout addon, so the literal is duplicated
   * rather than shared).
   */
  public static final String         PAGE_UNREACHABLE_EVENT = "layout.page.unreachable";

  /** Used to register this listener on the page-gone events. */
  @Autowired
  private ListenerService            listenerService;

  /** Used to unindex the removed page's content block documents. */
  @Autowired
  private IndexingService            indexingService;

  /** Used to enumerate the blocks currently indexed under the removed page's storage id. */
  @Autowired
  private PageContentSearchConnector searchConnector;

  @PostConstruct
  public void init() {
    listenerService.addListener(LayoutService.PAGE_REMOVED, this);
    listenerService.addListener(PAGE_UNREACHABLE_EVENT, this);
  }

  @Override
  @ContainerTransactional
  public void onEvent(Event<Object, Page> event) throws Exception {
    Page page = event.getData();
    if (page == null) {
      return;
    }
    searchConnector.findIndexedBlockIds(page.getStorageId())
                   .forEach(id -> indexingService.unindex(PageContentIndexingConnector.TYPE, id));
  }

}
