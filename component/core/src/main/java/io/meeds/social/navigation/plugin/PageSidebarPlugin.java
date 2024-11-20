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
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.portal.mop.navigation.NodeData;
import org.exoplatform.portal.mop.service.NavigationService;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

@Component
@Order(20)
public class PageSidebarPlugin implements SidebarPlugin {

  private static final String NODE_ID_PROP_NAME = "navigationNodeId";

  @Autowired
  private NavigationService   navigationService;

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.PAGE;
  }

  @Override
  public SidebarItem resolveProperties(SidebarItem item, Locale locale) {
    String nodeId = item.getProperties().get(NODE_ID_PROP_NAME);
    NodeData node = navigationService.getNodeById(Long.parseLong(nodeId));
    if (node != null) {
      item.setName(node.getName());
      if (node.getState() != null && node.getState().getIcon() != null) {
        item.setIcon(node.getState().getIcon());
      }
    }
    return item;
  }

  @Override
  public List<SidebarItem> getDefaultItems() {
    return Collections.emptyList();
  }

  @Override
  public boolean itemExists(SidebarItem item) {
    String nodeId = item.getProperties().get(NODE_ID_PROP_NAME);
    return navigationService.getNodeById(Long.parseLong(nodeId)) != null;
  }

}
