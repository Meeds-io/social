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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Event;

import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;

@RunWith(MockitoJUnitRunner.class)
public class PageSavedIndexingListenerTest {

  private static final PageKey      PAGE_KEY   = PageKey.parse("portal::site::page");

  private static final String       STORAGE_ID = "page_139";

  @Mock
  private IndexingService           indexingService;

  @Mock
  private LayoutService             layoutService;

  private PageSavedIndexingListener listener;

  @Before
  public void setup() {
    listener = new PageSavedIndexingListener(indexingService, layoutService);
  }

  @Test
  public void shouldQueueTheStoredPageWhenAPageIsCreated() throws Exception {
    // The broadcast instance is the caller's own and has no storage id yet
    // when the page was just created: the stored page is the reference
    Page broadcastPage = mock(Page.class);
    when(broadcastPage.getPageKey()).thenReturn(PAGE_KEY);
    Page storedPage = mock(Page.class);
    when(storedPage.getStorageId()).thenReturn(STORAGE_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(storedPage);

    listener.onEvent(new Event<>(LayoutService.PAGE_CREATED, "source", broadcastPage));

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE, STORAGE_ID);
  }

  @Test
  public void shouldQueueTheStoredPageWhenAPageIsUpdated() throws Exception {
    Page broadcastPage = mock(Page.class);
    when(broadcastPage.getPageKey()).thenReturn(PAGE_KEY);
    Page storedPage = mock(Page.class);
    when(storedPage.getStorageId()).thenReturn(STORAGE_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(storedPage);

    listener.onEvent(new Event<>(LayoutService.PAGE_UPDATED, "source", broadcastPage));

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE, STORAGE_ID);
  }

  @Test
  public void shouldIgnoreDraftPagesWithoutLoadingThem() throws Exception {
    // The layout editor stores its draft copy on every change, and the
    // connector never indexes a draft
    Page draftPage = mock(Page.class);
    when(draftPage.getPageKey()).thenReturn(PageKey.parse("portal::site::page_draft_john"));

    listener.onEvent(new Event<>(LayoutService.PAGE_UPDATED, "source", draftPage));

    verify(layoutService, never()).getPage(any(PageKey.class));
    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void shouldIgnoreAPageNoLongerStored() throws Exception {
    Page broadcastPage = mock(Page.class);
    when(broadcastPage.getPageKey()).thenReturn(PAGE_KEY);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(null);

    listener.onEvent(new Event<>(LayoutService.PAGE_CREATED, "source", broadcastPage));

    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void shouldIgnoreEventWithoutPage() throws Exception {
    listener.onEvent(new Event<>(LayoutService.PAGE_CREATED, "source", null));

    verify(layoutService, never()).getPage(any(PageKey.class));
    verify(indexingService, never()).reindex(any(), any());
  }

}
