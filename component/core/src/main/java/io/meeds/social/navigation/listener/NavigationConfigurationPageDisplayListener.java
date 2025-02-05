/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.PageType;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.mop.navigation.NodeData;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerBase;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.portal.navigation.constant.SidebarItemType;
import io.meeds.portal.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.service.NavigationConfigurationServiceImpl;

import jakarta.annotation.PostConstruct;

@Component
public class NavigationConfigurationPageDisplayListener
    implements ListenerBase<NavigationConfiguration, NavigationConfiguration> {

  @Autowired
  private ListenerService   listenerService;

  @Autowired
  private LayoutService     layoutService;

  @Autowired
  private NavigationService navigationService;

  @PostConstruct
  public void init() {
    listenerService.addListener(NavigationConfigurationServiceImpl.NAVIGATION_CONFIGURATION_UPDATED_EVENT, this);
  }

  @Override
  public void onEvent(Event<NavigationConfiguration, NavigationConfiguration> event) throws Exception {
    NavigationConfiguration oldConfiguration = event.getSource();
    NavigationConfiguration newConfiguration = event.getData();

    List<Page> oldPages = oldConfiguration == null ?
                                                   Collections.emptyList() :
                                                   getPages(oldConfiguration);
    List<Page> pagesWithSharedLayout = getPages(newConfiguration);
    List<Page> pagesWithoutSharedLayout = new ArrayList<>(oldPages);
    pagesWithoutSharedLayout.removeAll(pagesWithSharedLayout);

    pagesWithoutSharedLayout.forEach(page -> {
      page.setShowSharedLayout(false);
      layoutService.save(new PageContext(page.getPageKey(), Utils.toPageState(page)));
    });
    pagesWithSharedLayout.forEach(page -> {
      page.setShowSharedLayout(true);
      layoutService.save(new PageContext(page.getPageKey(), Utils.toPageState(page)));
    });
  }

  private List<Page> getPages(NavigationConfiguration configuration) {
    return configuration.getSidebar()
                        .getItems()
                        .stream()
                        .filter(item -> item.getType() == SidebarItemType.PAGE)
                        .map(item -> {
                          String nodeId = item.getProperties().get(NODE_ID_PROP_NAME);
                          NodeData node = navigationService.getNodeById(Long.parseLong(nodeId));
                          if (node.getState().getPageRef() != null) {
                            Page page = layoutService.getPage(node.getState().getPageRef());
                            if (PageType.PAGE.name().equals(page.getType())) {
                              return page;
                            }
                          }
                          return null;
                        })
                        .filter(Objects::nonNull)
                        .toList();
  }

}
