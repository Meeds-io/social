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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.service.CMSService;
import io.meeds.social.cms.service.PageContentBlockPluginService;
import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;
import io.meeds.social.cms.storage.elasticsearch.PageContentSearchConnector;

@RunWith(MockitoJUnitRunner.class)
public class PageContentBlockIndexingListenerTest {

  private static final String                CONTENT_TYPE = "notePage";

  private static final PageKey                PAGE_KEY     = PageKey.parse("portal::site::page");

  private static final String                 STORAGE_ID   = "page_139";

  @Mock
  private ListenerService                     listenerService;

  @Mock
  private IndexingService                     indexingService;

  @Mock
  private CMSService                          cmsService;

  @Mock
  private PageContentBlockPluginService       pluginService;

  @Mock
  private LayoutService                       layoutService;

  @Mock
  private PageContentSearchConnector          searchConnector;

  @InjectMocks
  private PageContentBlockIndexingListener    listener;

  private Page                                page;

  @Before
  public void setup() {
    page = mock(Page.class);
    when(page.getStorageId()).thenReturn(STORAGE_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);
    when(pluginService.getContentTypes()).thenReturn(Set.of(CONTENT_TYPE));
    when(searchConnector.findIndexedBlockIds(STORAGE_ID)).thenReturn(Collections.emptyList());
  }

  /**
   * Puts a widget on the page whose "name" preference matches
   * {@code settingName} — the shape {@link PageContentBlockIndexingListener}
   * expects to still consider that {@link CMSSetting} bound to a live
   * widget on the page.
   */
  private void mockActiveWidget(String settingName) {
    Application application = mock(Application.class);
    ApplicationState state = mock(ApplicationState.class);
    Portlet preferences = mock(Portlet.class);
    when(application.getState()).thenReturn(state);
    when(layoutService.load(state)).thenReturn(preferences);
    when(preferences.getValue("name")).thenReturn(settingName);
    ArrayList<ModelObject> children = new ArrayList<>(page.getChildren() == null ? List.of() : page.getChildren());
    children.add(application);
    when(page.getChildren()).thenReturn(children);
  }

  @Test
  public void shouldIgnoreEventWithBlankPageReference() throws Exception {
    listener.onEvent(new Event<>("layout.page.updated", "user", ""));

    verify(indexingService, never()).reindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    verify(indexingService, never()).unindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldIgnoreEventWhenPageNoLongerExists() throws Exception {
    when(layoutService.getPage(PAGE_KEY)).thenReturn(null);

    listener.onEvent(new Event<>("layout.page.updated", "user", PAGE_KEY.format()));

    verify(indexingService, never()).reindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    verify(indexingService, never()).unindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldIndexWhenPageCarriesContentBlock() throws Exception {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(setting));
    mockActiveWidget("name");

    listener.onEvent(new Event<>("layout.page.updated", "user", PAGE_KEY.format()));

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE,
                                    PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "name"));
    verify(indexingService, never()).unindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldReindexEveryBlockWhenPageCarriesMultipleContentBlocks() throws Exception {
    CMSSetting summary = new CMSSetting(CONTENT_TYPE, "summary", PAGE_KEY.format(), 0);
    CMSSetting description = new CMSSetting(CONTENT_TYPE, "description", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(summary, description));
    mockActiveWidget("summary");
    mockActiveWidget("description");

    listener.onEvent(new Event<>("layout.page.updated", "user", PAGE_KEY.format()));

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE,
                                    PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "summary"));
    verify(indexingService).reindex(PageContentIndexingConnector.TYPE,
                                    PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "description"));
  }

  @Test
  public void shouldDoNothingWhenPageNoLongerCarriesAnyContentBlock() throws Exception {
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(Collections.emptyList());

    listener.onEvent(new Event<>("layout.page.permissions.updated", "user", PAGE_KEY.format()));

    verify(indexingService, never()).unindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    verify(indexingService, never()).reindex(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldUnindexBlocksNoLongerBoundToThePage() throws Exception {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(setting));
    mockActiveWidget("name");
    String currentBlockId = PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "name");
    String detachedBlockId = PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "removed");
    when(searchConnector.findIndexedBlockIds(STORAGE_ID)).thenReturn(List.of(currentBlockId, detachedBlockId));

    listener.onEvent(new Event<>("layout.page.updated", "user", PAGE_KEY.format()));

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE, currentBlockId);
    verify(indexingService).unindex(PageContentIndexingConnector.TYPE, detachedBlockId);
    verify(indexingService, never()).unindex(PageContentIndexingConnector.TYPE, currentBlockId);
  }

  @Test
  public void shouldUnindexBlockWhenItsWidgetIsRemovedEvenIfSettingAndContentStillExist() throws Exception {
    CMSSetting orphanedSetting = new CMSSetting(CONTENT_TYPE, "orphaned", PAGE_KEY.format(), 0);
    when(cmsService.getSettingsByTypeAndPageReference(CONTENT_TYPE, PAGE_KEY.format())).thenReturn(List.of(orphanedSetting));
    // No widget on the page carries the "orphaned" setting name — the
    // CMSSetting survived, but the widget that created it didn't.
    String orphanedBlockId = PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "orphaned");
    when(searchConnector.findIndexedBlockIds(STORAGE_ID)).thenReturn(List.of(orphanedBlockId));

    listener.onEvent(new Event<>("layout.page.updated", "user", PAGE_KEY.format()));

    verify(indexingService, never()).reindex(PageContentIndexingConnector.TYPE, orphanedBlockId);
    verify(indexingService).unindex(PageContentIndexingConnector.TYPE, orphanedBlockId);
  }

}
