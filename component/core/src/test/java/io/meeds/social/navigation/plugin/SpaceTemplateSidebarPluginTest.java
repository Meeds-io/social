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

import static io.meeds.social.navigation.plugin.SpaceTemplateSidebarPlugin.SPACE_TEMPLATE_ID_PROP_NAME;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.social.core.space.model.Space;

import io.meeds.portal.navigation.constant.SidebarItemType;
import io.meeds.portal.navigation.model.SidebarItem;
import io.meeds.social.navigation.AbstractNavigationConfigurationTest;

public class SpaceTemplateSidebarPluginTest extends AbstractNavigationConfigurationTest {

  @Before
  @Override
  public void beforeEach() throws Exception {
    super.beforeEach();
    if (spaceTemplate == null) {
      mockSpaceTemplate();
      for (int i = 0; i < 5; i++) {
        Space space = new Space();
        space.setRegistration(Space.OPEN);
        space.setVisibility(Space.PUBLIC);
        space.setTemplateId(spaceTemplate.getId());
        spaceService.createSpace(space, userAcl.getSuperUser());
      }
    }
  }

  @Test
  public void testGetType() {
    assertEquals(SidebarItemType.SPACE_TEMPLATE, spaceTemplateSidebarPlugin.getType());
  }

  @Test
  public void testGetDefaultItems() {
    List<SidebarItem> defaultItems = spaceTemplateSidebarPlugin.getDefaultItems();
    assertNotNull(defaultItems);
    assertTrue(defaultItems.stream()
                           .anyMatch(item -> item.getProperties() != null
                                             && StringUtils.equals(item.getProperties().get(SPACE_TEMPLATE_ID_PROP_NAME),
                                                                   String.valueOf(spaceTemplate.getId()))));
  }

  @Test
  public void testItemExists() {
    assertFalse(spaceTemplateSidebarPlugin.itemExists(null, null));
    assertFalse(spaceTemplateSidebarPlugin.itemExists(new SidebarItem(), null));

    SidebarItem item = new SidebarItem(SidebarItemType.SPACE_TEMPLATE);
    item.setProperties(Collections.singletonMap(SPACE_TEMPLATE_ID_PROP_NAME, String.valueOf(spaceTemplate.getId())));
    assertTrue(spaceTemplateSidebarPlugin.itemExists(item, null));
  }

  @Test
  public void testResolveProperties() {
    SidebarItem item = new SidebarItem(SidebarItemType.SPACE_TEMPLATE);
    item.setProperties(Collections.singletonMap(SPACE_TEMPLATE_ID_PROP_NAME, String.valueOf(spaceTemplate.getId())));
    spaceTemplateSidebarPlugin.resolveProperties(item, userAcl.getSuperUser(), Locale.ENGLISH);

    assertEquals(spaceTemplate.getName(), item.getName());
    assertEquals(spaceTemplate.getIcon(), item.getIcon());
    assertNull(item.getUrl());
    assertNull(item.getTarget());
    assertNull(item.getAvatar());

    assertNotNull(item.getItems());
    assertEquals(AbstractSpaceSidebarPlugin.SPACES_LIMIT_DEFAULT,
                 item.getItems().size());
  }

}
