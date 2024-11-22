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
package io.meeds.social.navigation.model;

import lombok.Data;

@Data
public class NavigationConfiguration implements Cloneable {

  private TopbarConfiguration  topbar;

  private SidebarConfiguration sidebar;

  private final long           lastModified;

  public NavigationConfiguration() {
    this.lastModified = System.currentTimeMillis();
  }

  public NavigationConfiguration(TopbarConfiguration topbar, SidebarConfiguration sidebar) {
    this.topbar = topbar;
    this.sidebar = sidebar;
    this.lastModified = System.currentTimeMillis();
  }

  public NavigationConfiguration(TopbarConfiguration topbar,
                                 SidebarConfiguration sidebar,
                                 long lastModified) {
    this.topbar = topbar;
    this.sidebar = sidebar;
    this.lastModified = lastModified;
  }

  @Override
  public NavigationConfiguration clone() { // NOSONAR
    return new NavigationConfiguration(topbar == null ? null : topbar.clone(),
                                       sidebar == null ? null : sidebar.clone(),
                                       lastModified);
  }

}
