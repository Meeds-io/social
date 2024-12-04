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
package io.meeds.social.navigation.listener;

import static io.meeds.social.navigation.plugin.AbstractLayoutSidebarPlugin.SITE_NAME_PROP_NAME;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteFilter;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;

import io.meeds.social.navigation.AbstractNavigationConfigurationTest;
import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.model.SidebarItem;

public class NavigationConfigurationSiteDisplayListenerTest extends AbstractNavigationConfigurationTest {

  @Test
  public void testUpdateSiteDisplayedStatusByConfiguration() {
    NavigationConfiguration configuration = navigationConfigurationService.getConfiguration();
    assertNotNull(configuration);
    assertNotNull(configuration.getTopbar());
    assertNotNull(configuration.getSidebar());

    SiteFilter siteFilter = new SiteFilter();
    siteFilter.setSiteType(SiteType.PORTAL);
    List<PortalConfig> sites = layoutService.getSites(siteFilter);
    assertNotNull(sites);

    List<String> siteNames = getSiteNames(configuration);
    String siteName = siteNames.get(0);

    List<SidebarItem> originalSidebarItems = configuration.getSidebar().getItems();
    List<SidebarItem> sidebarItems = new ArrayList<>(originalSidebarItems);
    sidebarItems.removeIf(item -> item.getType() == SidebarItemType.SITE && siteName.equals(item.getProperties().get(SITE_NAME_PROP_NAME)));

    configuration.getSidebar().setItems(sidebarItems);
    navigationConfigurationService.updateConfiguration(configuration);

    SiteKey standaloneSiteKey = SiteKey.portal(siteName);

    PortalConfig updatedSite = layoutService.getPortalConfig(standaloneSiteKey);
    assertFalse(updatedSite.isDisplayed());

    configuration.getSidebar().setItems(originalSidebarItems);
    navigationConfigurationService.updateConfiguration(configuration);

    updatedSite = layoutService.getPortalConfig(standaloneSiteKey);
    assertTrue(updatedSite.isDisplayed());
  }

  private List<String> getSiteNames(NavigationConfiguration configuration) {
    return configuration.getSidebar()
                        .getItems()
                        .stream()
                        .filter(item -> item.getType() == SidebarItemType.SITE)
                        .map(item -> item.getProperties().get(SITE_NAME_PROP_NAME))
                        .toList();
  }

}
