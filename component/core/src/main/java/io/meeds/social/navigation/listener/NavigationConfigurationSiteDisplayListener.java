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
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerBase;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.service.NavigationConfigurationService;

import jakarta.annotation.PostConstruct;

@Component
public class NavigationConfigurationSiteDisplayListener implements ListenerBase<NavigationConfiguration, NavigationConfiguration> {

  @Autowired
  private ListenerService listenerService;

  @Autowired
  private LayoutService   layoutService;

  @PostConstruct
  public void init() {
    listenerService.addListener(NavigationConfigurationService.NAVIGATION_CONFIGURATION_UPDATED_EVENT, this);
  }

  @Override
  public void onEvent(Event<NavigationConfiguration, NavigationConfiguration> event) throws Exception {
    NavigationConfiguration oldConfiguration = event.getSource();
    NavigationConfiguration newConfiguration = event.getData();

    List<String> oldSiteNames = oldConfiguration == null ?
                                                         Collections.emptyList() :
                                                         getSiteNames(oldConfiguration);
    List<String> newSiteNames = getSiteNames(newConfiguration);

    List<String> sitesWithoutSharedLayout = new ArrayList<>(oldSiteNames);
    sitesWithoutSharedLayout.removeAll(newSiteNames);
    sitesWithoutSharedLayout.forEach(siteName -> {
      PortalConfig site = layoutService.getPortalConfig(SiteKey.portal(siteName));
      if (site != null) {
        site.setDisplayed(false);
        layoutService.save(site);
      }
    });

    newSiteNames.forEach(siteName -> {
      PortalConfig site = layoutService.getPortalConfig(SiteKey.portal(siteName));
      int displayOrder = newSiteNames.indexOf(siteName) + 1;
      if (site != null
          && (!site.isDisplayed() || site.getDisplayOrder() != displayOrder)) {
        site.setDisplayed(true);
        site.setDisplayOrder(displayOrder);
        layoutService.save(site);
      }
    });
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
