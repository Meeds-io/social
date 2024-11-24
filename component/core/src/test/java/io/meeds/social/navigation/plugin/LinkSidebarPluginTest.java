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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import io.meeds.social.navigation.AbstractNavigationConfigurationTest;
import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.util.JsonUtils;

public class LinkSidebarPluginTest extends AbstractNavigationConfigurationTest {

  @Test
  public void testGetType() {
    assertEquals(SidebarItemType.LINK, linkSidebarPlugin.getType());
  }

  @Test
  public void testGetDefaultItems() {
    List<SidebarItem> defaultItems = linkSidebarPlugin.getDefaultItems();
    assertNotNull(defaultItems);
    assertTrue(defaultItems.isEmpty());
  }

  @Test
  public void testItemExists() {
    assertTrue(linkSidebarPlugin.itemExists(null, null));
  }

  @Test
  public void testResolveProperties() {
    String enName = "Test EN";
    String frName = "Test FR";

    Map<String, String> names = new HashMap<>();
    names.put(Locale.ENGLISH.toLanguageTag(), enName);
    names.put(Locale.FRENCH.toLanguageTag(), frName);
    String namesJsonString = JsonUtils.toJsonString(names);

    SidebarItem item = new SidebarItem("fakeName",
                                       "url",
                                       "target",
                                       "avatar",
                                       "icon",
                                       SidebarItemType.LINK,
                                       null,
                                       Collections.singletonMap(LinkSidebarPlugin.LINK_NAMES, namesJsonString));

    SidebarItem result = linkSidebarPlugin.resolveProperties(item, null, Locale.ENGLISH);
    assertNotNull(result);
    assertEquals(enName, result.getName());

    result = linkSidebarPlugin.resolveProperties(item, null, Locale.FRENCH);
    assertNotNull(result);
    assertEquals(frName, result.getName());
  }

}
