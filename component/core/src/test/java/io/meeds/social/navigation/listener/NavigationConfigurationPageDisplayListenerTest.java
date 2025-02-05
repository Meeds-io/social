/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation.listener;

import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.NODE_ID_PROP_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NodeContext;

import io.meeds.portal.navigation.constant.SidebarItemType;
import io.meeds.portal.navigation.model.NavigationConfiguration;
import io.meeds.portal.navigation.model.SidebarItem;
import io.meeds.social.navigation.AbstractNavigationConfigurationTest;

public class NavigationConfigurationPageDisplayListenerTest extends AbstractNavigationConfigurationTest {

  @Test
  public void testUpdateSiteDisplayedStatusByConfiguration() {
    NavigationConfiguration configuration = navigationConfigurationService.getConfiguration();
    assertNotNull(configuration);
    assertNotNull(configuration.getTopbar());
    assertNotNull(configuration.getSidebar());

    List<SidebarItem> originalSidebarItems = configuration.getSidebar().getItems();
    List<SidebarItem> sidebarItems = new ArrayList<>(originalSidebarItems);
    sidebarItems.removeIf(item -> item.getType() == SidebarItemType.PAGE);
    configuration.getSidebar().setItems(sidebarItems);
    navigationConfigurationService.updateConfiguration(configuration);

    NodeContext<NodeContext<Object>> node = navigationService.loadNode(SiteKey.portal(SITE_NAME));
    assertNotNull(node);
    node = node.get("a");
    assertNotNull(node);

    Page page = layoutService.getPage(node.getState().getPageRef());
    assertNotNull(page);
    assertFalse(page.isShowSharedLayout());

    SidebarItem sidebarItem = new SidebarItem(SidebarItemType.PAGE);
    sidebarItem.setProperties(Collections.singletonMap(NODE_ID_PROP_NAME, node.getId()));
    sidebarItems.add(sidebarItem);
    navigationConfigurationService.updateConfiguration(configuration);

    page = layoutService.getPage(node.getState().getPageRef());
    assertNotNull(page);
    assertTrue(page.isShowSharedLayout());

    sidebarItems.removeIf(item -> item.getType() == SidebarItemType.PAGE);
    navigationConfigurationService.updateConfiguration(configuration);

    page = layoutService.getPage(node.getState().getPageRef());
    assertNotNull(page);
    assertFalse(page.isShowSharedLayout());
  }

}
