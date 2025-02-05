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

import static io.meeds.social.navigation.plugin.SpaceListSidebarPlugin.SPACES_NAMES;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.social.core.space.model.Space;

import io.meeds.portal.navigation.constant.SidebarItemType;
import io.meeds.portal.navigation.model.SidebarItem;
import io.meeds.social.navigation.AbstractNavigationConfigurationTest;
import io.meeds.social.util.JsonUtils;

public class SpaceListSidebarPluginTest extends AbstractNavigationConfigurationTest {

  @Before
  @Override
  public void beforeEach() throws Exception {
    super.beforeEach();
    for (int i = 0; i < 5; i++) {
      Space space = new Space();
      space.setRegistration(Space.OPEN);
      space.setVisibility(Space.PUBLIC);
      spaceService.createSpace(space, userAcl.getSuperUser());
    }
  }

  @Test
  public void testGetType() {
    assertEquals(SidebarItemType.SPACES, spaceListSidebarPlugin.getType());
  }

  @Test
  public void testGetDefaultItems() {
    List<SidebarItem> defaultItems = spaceListSidebarPlugin.getDefaultItems();
    assertNotNull(defaultItems);
    assertTrue(defaultItems.stream()
                           .anyMatch(item -> StringUtils.contains(item.getProperties().get(SPACES_NAMES),
                                                                  "sidebar.viewAllSpaces")));
  }

  @Test
  public void testItemExists() {
    assertFalse(spaceListSidebarPlugin.itemExists(null, null));
    assertFalse(spaceListSidebarPlugin.itemExists(new SidebarItem(), null));
    SidebarItem item = new SidebarItem(SidebarItemType.SPACES);
    assertFalse(spaceListSidebarPlugin.itemExists(item, null));

    item.setProperties(Collections.singletonMap(SPACES_NAMES, "{}"));
    assertTrue(spaceListSidebarPlugin.itemExists(item, null));
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
                                       SidebarItemType.SPACES,
                                       null,
                                       Collections.singletonMap(SPACES_NAMES, namesJsonString),
                                       false);

    SidebarItem result = spaceListSidebarPlugin.resolveProperties(item, null, Locale.ENGLISH);
    assertNotNull(result);
    assertEquals(enName, result.getName());

    result = spaceListSidebarPlugin.resolveProperties(item, null, Locale.FRENCH);
    assertNotNull(result);
    assertEquals(frName, result.getName());
    assertNotNull(result.getItems());
    assertTrue(result.getItems().isEmpty());

    result = spaceListSidebarPlugin.resolveProperties(item, userAcl.getSuperUser(), Locale.FRENCH);
    assertNotNull(result.getItems());
    assertEquals(AbstractSpaceSidebarPlugin.SPACES_LIMIT_DEFAULT,
                 item.getItems().size());
  }

}
