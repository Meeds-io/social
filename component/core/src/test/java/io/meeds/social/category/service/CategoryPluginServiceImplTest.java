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
package io.meeds.social.category.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;

import io.meeds.social.category.model.CategoryEntryItem;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.plugin.CategoryPlugin;

public class CategoryPluginServiceImplTest {

  private static final String OBJECT_TYPE = "typeA";

  private static final String OBJECT_ID   = "1";

  private static final String TEST_USER   = "testuser";

  @Test
  public void testDispatchToRegisteredPlugin() {
    CategoryPluginServiceImpl categoryPluginService = new CategoryPluginServiceImpl();
    CategoryPlugin plugin = mock(CategoryPlugin.class);
    when(plugin.getType()).thenReturn(OBJECT_TYPE);
    when(plugin.canEdit(OBJECT_ID, TEST_USER)).thenReturn(true);
    when(plugin.canAccess(OBJECT_ID, TEST_USER)).thenReturn(true);
    CategoryEntryItem entryItem = mock(CategoryEntryItem.class);
    when(plugin.getEntryItem(OBJECT_ID, TEST_USER)).thenReturn(entryItem);
    when(plugin.getCategoryIds(0l, TEST_USER)).thenReturn(Collections.singletonList(5l));

    categoryPluginService.addPlugin(plugin);

    assertSame(plugin, categoryPluginService.getCategoryPlugin(OBJECT_TYPE));
    assertTrue(categoryPluginService.canEdit(OBJECT_TYPE, OBJECT_ID, TEST_USER));
    assertTrue(categoryPluginService.canAccess(OBJECT_TYPE, OBJECT_ID, TEST_USER));
    assertSame(entryItem, categoryPluginService.getEntryItem(OBJECT_TYPE, OBJECT_ID, TEST_USER));
    assertEquals(Collections.singletonList(5l), categoryPluginService.getCategoryIds(OBJECT_TYPE, 0l, TEST_USER));

    CategoryObject object = new CategoryObject(OBJECT_TYPE, OBJECT_ID, 0l);
    when(plugin.getObject(object)).thenReturn(object);
    assertSame(object, categoryPluginService.getObject(object));
  }

  @Test
  public void testDispatchToDefaultPluginWhenNoneRegistered() {
    CategoryPluginServiceImpl categoryPluginService = new CategoryPluginServiceImpl();
    CategoryPlugin plugin = mock(CategoryPlugin.class);
    when(plugin.getType()).thenReturn(OBJECT_TYPE);
    categoryPluginService.addPlugin(plugin);

    CategoryPlugin defaultPlugin = categoryPluginService.getCategoryPlugin("unregisteredType");
    assertEquals("unregisteredType", defaultPlugin.getType());
  }

  @Test
  public void testDispatchToPluginRegisteredAfterAnInitialLookupMiss() {
    // Plugins self-register asynchronously (each via its own @PostConstruct), so a lookup
    // for an objectType can legitimately happen before that type's plugin has registered
    // yet. The fallback returned in that case must not be cached, otherwise the objectType
    // stays permanently stuck on the default no-op plugin even once the real one registers.
    CategoryPluginServiceImpl categoryPluginService = new CategoryPluginServiceImpl();

    CategoryPlugin defaultPlugin = categoryPluginService.getCategoryPlugin(OBJECT_TYPE);
    assertEquals(OBJECT_TYPE, defaultPlugin.getType());

    CategoryPlugin plugin = mock(CategoryPlugin.class);
    when(plugin.getType()).thenReturn(OBJECT_TYPE);
    categoryPluginService.addPlugin(plugin);

    assertSame(plugin, categoryPluginService.getCategoryPlugin(OBJECT_TYPE));
  }

}
