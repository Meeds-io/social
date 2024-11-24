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

import static io.meeds.social.navigation.plugin.SidebarPluginUtils.getNameFromProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.social.core.space.SpaceFilter;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

@Component
@Order(40)
public class SpaceListSidebarPlugin extends AbstractSpaceSidebarPlugin {

  public static final String SPACES_NAMES = "names";

  @Autowired
  private LocaleConfigService localeConfigService;

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.SPACES;
  }

  @Override
  public boolean itemExists(SidebarItem item, String username) {
    if (item == null || item.getProperties() == null) {
      return false;
    }
    return item.getProperties().containsKey(SPACES_NAMES);
  }

  @Override
  public SidebarItem resolveProperties(SidebarItem item, String username, Locale locale) {
    item.setName(getNameFromProperties(localeConfigService,
                                       item,
                                       SPACES_NAMES,
                                       locale));
    item.setItems(getSpaces(item, username));
    return item;
  }

  @Override
  public List<SidebarItem> getDefaultItems() {
    Map<String, String> properties = new HashMap<>();
    properties.put(SPACES_NAMES, "{\"en\": \"sidebar.viewAllSpaces\"}");
    properties.put(SPACES_LIMIT, "0");
    properties.put(SPACES_SORT_BY, "TITLE");
    return Collections.singletonList(new SidebarItem(null,
                                                     null,
                                                     null,
                                                     null,
                                                     "fa-layer-group",
                                                     SidebarItemType.SPACES,
                                                     null,
                                                     properties));
  }

  @Override
  protected void buildSpaceFilter(SidebarItem item, SpaceFilter spaceFilter) {
    // No specific space filter
  }

}
