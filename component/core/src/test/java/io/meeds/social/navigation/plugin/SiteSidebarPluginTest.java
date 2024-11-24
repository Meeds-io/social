/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation.plugin;

import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_NAME_PROP_NAME;
import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_TYPE_PROP_NAME;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import io.meeds.social.navigation.AbstractNavigationConfigurationTest;
import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

public class SiteSidebarPluginTest extends AbstractNavigationConfigurationTest {

  @Test
  public void testGetType() {
    assertEquals(SidebarItemType.SITE, siteSidebarPlugin.getType());
  }

  @Test
  public void testGetDefaultItems() {
    List<SidebarItem> defaultItems = siteSidebarPlugin.getDefaultItems();
    assertNotNull(defaultItems);
    assertFalse(defaultItems.isEmpty());
    assertTrue(defaultItems.stream().anyMatch(item -> StringUtils.equals(item.getName(), "classic")));
  }

  @Test
  public void testItemExists() {
    assertFalse(siteSidebarPlugin.itemExists(null, null));
    assertFalse(siteSidebarPlugin.itemExists(new SidebarItem(), null));

    SidebarItem item = new SidebarItem(SidebarItemType.SITE);
    Map<String, String> properties = new HashMap<>();
    item.setProperties(properties);
    properties.put(SITE_TYPE_PROP_NAME, "portal");

    properties.put(SITE_NAME_PROP_NAME, "notfound");
    assertFalse(siteSidebarPlugin.itemExists(item, null));

    properties.put(SITE_NAME_PROP_NAME, SITE_NAME);
    assertTrue(siteSidebarPlugin.itemExists(item, null));
  }

  @Test
  public void testResolveProperties() {
    SidebarItem item = new SidebarItem(SidebarItemType.SITE);
    Map<String, String> properties = new HashMap<>();
    item.setProperties(properties);
    properties.put(SITE_TYPE_PROP_NAME, "portal");
    properties.put(SITE_NAME_PROP_NAME, SITE_NAME);

    siteSidebarPlugin.resolveProperties(item, userAcl.getSuperUser(), Locale.ENGLISH);

    assertEquals(SITE_NAME, item.getName());
    assertEquals("/portal/" + SITE_NAME, item.getUrl());
    assertNull(item.getTarget());
    assertNull(item.getAvatar());
    assertNull(item.getItems());
    assertEquals("a-icon", item.getIcon());
  }

}
