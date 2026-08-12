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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.model.PageContentBlock;
import io.meeds.social.cms.plugin.PageContentBlockPlugin;
import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnector;

@RunWith(MockitoJUnitRunner.class)
public class PageContentBlockPluginServiceImplTest {

  private static final String                CONTENT_TYPE = "notePage";

  private static final PageKey                PAGE_KEY     = PageKey.parse("portal::site::page");

  private static final String                 STORAGE_ID   = "page_139";

  @Mock
  private CMSService                          cmsService;

  @Mock
  private LayoutService                       layoutService;

  @Mock
  private IndexingService                     indexingService;

  private PageContentBlockPluginServiceImpl   pluginService;

  @Before
  public void setup() {
    pluginService = new PageContentBlockPluginServiceImpl(cmsService, layoutService, indexingService);
  }

  @Test
  public void shouldReturnEmptyWhenNoPluginRegistered() {
    assertTrue(pluginService.getContentTypes().isEmpty());
    assertNull(pluginService.getPlugin(CONTENT_TYPE));
  }

  @Test
  public void shouldRegisterAndRetrievePluginByContentType() {
    PageContentBlockPlugin plugin = mock(PageContentBlockPlugin.class);
    when(plugin.getContentType()).thenReturn(CONTENT_TYPE);

    pluginService.addPlugin(plugin);

    assertEquals(1, pluginService.getContentTypes().size());
    assertTrue(pluginService.getContentTypes().contains(CONTENT_TYPE));
    assertEquals(plugin, pluginService.getPlugin(CONTENT_TYPE));
  }

  @Test
  public void shouldDelegateContentExtractionToRegisteredPlugin() {
    PageContentBlockPlugin plugin = mock(PageContentBlockPlugin.class);
    when(plugin.getContentType()).thenReturn(CONTENT_TYPE);
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", "portal::site::page", 0);
    PageContentBlock content = mock(PageContentBlock.class);
    when(plugin.getContent(setting)).thenReturn(content);
    pluginService.addPlugin(plugin);

    PageContentBlockPlugin registered = pluginService.getPlugin(CONTENT_TYPE);

    assertEquals(content, registered.getContent(setting));
  }

  @Test
  public void shouldReindexContentBlockWhenSettingAndPageExist() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSetting(CONTENT_TYPE, "name")).thenReturn(setting);
    Page page = mock(Page.class);
    when(page.getStorageId()).thenReturn(STORAGE_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);

    pluginService.reindexContentBlock(CONTENT_TYPE, "name");

    verify(indexingService).reindex(PageContentIndexingConnector.TYPE,
                                    PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "name"));
  }

  @Test
  public void shouldDoNothingWhenReindexingAnUnknownSetting() {
    when(cmsService.getSetting(CONTENT_TYPE, "name")).thenReturn(null);

    pluginService.reindexContentBlock(CONTENT_TYPE, "name");

    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void shouldDoNothingWhenReindexingABlockWhosePageNoLongerExists() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSetting(CONTENT_TYPE, "name")).thenReturn(setting);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(null);

    pluginService.reindexContentBlock(CONTENT_TYPE, "name");

    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void shouldUnindexContentBlockWhenSettingAndPageExist() {
    CMSSetting setting = new CMSSetting(CONTENT_TYPE, "name", PAGE_KEY.format(), 0);
    when(cmsService.getSetting(CONTENT_TYPE, "name")).thenReturn(setting);
    Page page = mock(Page.class);
    when(page.getStorageId()).thenReturn(STORAGE_ID);
    when(layoutService.getPage(PAGE_KEY)).thenReturn(page);

    pluginService.unindexContentBlock(CONTENT_TYPE, "name");

    verify(indexingService).unindex(PageContentIndexingConnector.TYPE,
                                    PageContentIndexingConnector.buildBlockId(STORAGE_ID, CONTENT_TYPE, "name"));
  }

  @Test
  public void shouldDoNothingWhenUnindexingAnUnknownSetting() {
    when(cmsService.getSetting(CONTENT_TYPE, "name")).thenReturn(null);

    pluginService.unindexContentBlock(CONTENT_TYPE, "name");

    verify(indexingService, never()).unindex(any(), any());
  }

}
