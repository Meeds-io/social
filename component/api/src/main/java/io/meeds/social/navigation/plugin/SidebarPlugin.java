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

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

/**
 * A class to allow managing Sidebar item types
 */
public interface SidebarPlugin {

  /**
   * @return {@link SidebarItemType} managed by the implementing plugin
   */
  default SidebarItemType getType() {
    return null;
  }

  /**
   * Resolves Item Name and Icon when storage is maintained
   * 
   * @param item
   * @param username
   * @param locale
   */
  default SidebarItem resolveProperties(SidebarItem item, String username, Locale locale) {
    return item;
  }

  /**
   * @return {@link List} of {@link SidebarItem} to inject on startup
   */
  default List<SidebarItem> getDefaultItems() {
    return Collections.emptyList();
  }

  /**
   * @param item {@link SidebarItem}
   * @param username User name
   * @return true if the item exists and the user has access to it, else false
   */
  default boolean itemExists(SidebarItem item, String username) {
    return true;
  }

}
