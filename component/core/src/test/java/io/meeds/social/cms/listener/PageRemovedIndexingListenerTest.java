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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;
import io.meeds.social.cms.storage.elasticsearch.PageContentSearchConnector;

@RunWith(MockitoJUnitRunner.class)
public class PageRemovedIndexingListenerTest {

  private static final String                 STORAGE_ID = "page_139";

  @Mock
  private ListenerService                     listenerService;

  @Mock
  private IndexingService                     indexingService;

  @Mock
  private PageContentSearchConnector          searchConnector;

  @InjectMocks
  private PageRemovedIndexingListener         listener;

  @Test
  public void shouldRegisterListenerOnInit() {
    listener.init();

    verify(listenerService).addListener(LayoutService.PAGE_REMOVED, listener);
    // A page that still exists but isn't reachable from any navigation node
    // anymore can't be reached from a search result either — Layout signals
    // that with its own event, deliberately not PAGE_REMOVED, which other
    // listeners are entitled to read as "wipe everything attached to it"
    verify(listenerService).addListener(PageRemovedIndexingListener.PAGE_UNREACHABLE_EVENT, listener);
  }

  @Test
  public void shouldUnindexEveryBlockIndexedUnderAPageNoLongerReachable() throws Exception {
    Page page = mock(Page.class);
    when(page.getStorageId()).thenReturn(STORAGE_ID);
    String blockId = PageContentIndexingConnector.buildBlockId(STORAGE_ID, "notePage", "name1");
    when(searchConnector.findIndexedBlockIds(STORAGE_ID)).thenReturn(List.of(blockId));

    listener.onEvent(new Event<>(PageRemovedIndexingListener.PAGE_UNREACHABLE_EVENT, "source", page));

    verify(indexingService).unindex(PageContentIndexingConnector.TYPE, blockId);
  }

  @Test
  public void shouldUnindexEveryBlockIndexedUnderTheRemovedPage() throws Exception {
    Page page = mock(Page.class);
    when(page.getStorageId()).thenReturn(STORAGE_ID);
    String blockId1 = PageContentIndexingConnector.buildBlockId(STORAGE_ID, "notePage", "name1");
    String blockId2 = PageContentIndexingConnector.buildBlockId(STORAGE_ID, "notePage", "name2");
    when(searchConnector.findIndexedBlockIds(STORAGE_ID)).thenReturn(List.of(blockId1, blockId2));

    listener.onEvent(new Event<>(LayoutService.PAGE_REMOVED, "source", page));

    verify(indexingService).unindex(PageContentIndexingConnector.TYPE, blockId1);
    verify(indexingService).unindex(PageContentIndexingConnector.TYPE, blockId2);
  }

  @Test
  public void shouldDoNothingWhenNoBlockIsIndexedUnderTheRemovedPage() throws Exception {
    Page page = mock(Page.class);
    when(page.getStorageId()).thenReturn(STORAGE_ID);
    when(searchConnector.findIndexedBlockIds(STORAGE_ID)).thenReturn(Collections.emptyList());

    listener.onEvent(new Event<>(LayoutService.PAGE_REMOVED, "source", page));

    verify(indexingService, never()).unindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldIgnoreEventWithNullPage() throws Exception {
    listener.onEvent(new Event<>(LayoutService.PAGE_REMOVED, "source", null));

    verify(indexingService, never()).unindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    verify(searchConnector, never()).findIndexedBlockIds(org.mockito.ArgumentMatchers.any());
  }

}
