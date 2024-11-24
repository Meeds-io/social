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

import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.NODE_ID_PROP_NAME;
import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_DISPLAY_NAME_PROP_NAME;
import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_ICON_PROP_NAME;
import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_ID_PROP_NAME;
import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_NAME_PROP_NAME;
import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_TYPE_PROP_NAME;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NodeContext;

import io.meeds.social.navigation.AbstractNavigationConfigurationTest;
import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

public class PageSidebarPluginTest extends AbstractNavigationConfigurationTest {

  @Test
  public void testGetType() {
    assertEquals(SidebarItemType.PAGE, pageSidebarPlugin.getType());
  }

  @Test
  public void testGetDefaultItems() {
    List<SidebarItem> defaultItems = pageSidebarPlugin.getDefaultItems();
    assertNotNull(defaultItems);
    assertTrue(defaultItems.isEmpty());
  }

  @Test
  public void testItemExists() {
    assertFalse(pageSidebarPlugin.itemExists(null, null));
    assertFalse(pageSidebarPlugin.itemExists(new SidebarItem(), null));

    SidebarItem item = new SidebarItem(SidebarItemType.PAGE);
    item.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, "3555"));
    assertFalse(pageSidebarPlugin.itemExists(item, null));

    NodeContext<NodeContext<Object>> node = navigationService.loadNode(SiteKey.portal(SITE_NAME));
    assertNotNull(node);

    item.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.get("a").getId()));
    assertTrue(pageSidebarPlugin.itemExists(item, userAcl.getSuperUser()));
    assertTrue(pageSidebarPlugin.itemExists(item, "demo"));
    assertTrue(pageSidebarPlugin.itemExists(item, null));

    item.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.get("b").getId()));
    assertFalse(pageSidebarPlugin.itemExists(item, userAcl.getSuperUser()));
    assertFalse(pageSidebarPlugin.itemExists(item, "demo"));
    assertFalse(pageSidebarPlugin.itemExists(item, null));

    item.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.get("c").getId()));
    assertTrue(pageSidebarPlugin.itemExists(item, userAcl.getSuperUser()));
    assertTrue(pageSidebarPlugin.itemExists(item, "demo"));
    assertFalse(pageSidebarPlugin.itemExists(item, null));

    item.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.get("d").getId()));
    assertTrue(pageSidebarPlugin.itemExists(item, userAcl.getSuperUser()));
    assertFalse(pageSidebarPlugin.itemExists(item, "demo"));
    assertFalse(pageSidebarPlugin.itemExists(item, null));
  }

  @Test
  public void testResolveProperties() {
    SiteKey siteKey = SiteKey.portal(SITE_NAME);

    SidebarItem item = new SidebarItem(SidebarItemType.PAGE);
    NodeContext<NodeContext<Object>> node = navigationService.loadNode(siteKey);
    item.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.get("d").getId()));
    pageSidebarPlugin.resolveProperties(item, userAcl.getSuperUser(), Locale.ENGLISH);

    assertEquals("d", item.getName());
    assertEquals("/portal/contribute/d", item.getUrl());
    assertNull(item.getTarget());
    assertNull(item.getAvatar());
    assertNull(item.getItems());
    assertEquals("d-icon", item.getIcon());

    PortalConfig site = layoutService.getPortalConfig(siteKey);
    String siteId = (site.getStorageId().split("_"))[1];

    assertEquals(siteId, item.getProperties().get(SITE_ID_PROP_NAME));
    assertEquals(siteKey.getTypeName(), item.getProperties().get(SITE_TYPE_PROP_NAME));
    assertEquals(siteKey.getName(), item.getProperties().get(SITE_NAME_PROP_NAME));
    assertEquals("a-icon", item.getProperties().get(SITE_ICON_PROP_NAME));
    assertEquals(SITE_NAME, item.getProperties().get(SITE_DISPLAY_NAME_PROP_NAME));
  }

}
