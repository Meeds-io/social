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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.model.PageContentBlock;
import io.meeds.social.cms.plugin.PageContentBlockPlugin;

@RunWith(MockitoJUnitRunner.class)
public class PageContentBlockPluginServiceImplTest {

  private static final String                CONTENT_TYPE = "notePage";

  private PageContentBlockPluginServiceImpl   pluginService;

  @Before
  public void setup() {
    pluginService = new PageContentBlockPluginServiceImpl();
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

}
