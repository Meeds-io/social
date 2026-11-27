/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.hidden.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A page application window a user may hide from its page for themselves, as
 * its {@link io.meeds.social.hidden.plugin.HiddenApplicationPlugin} describes
 * it
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiddenApplication {

  /** Type of the application, the one its plugin declares */
  private String  type;

  /** Name of the application window, unique for its type */
  private String  name;

  /**
   * Title of the window in the requested language: its header, else the
   * default name of the application
   */
  private String  title;

  /**
   * Reference of the page holding the window; null when the window is on no
   * page (a shared or site layout), its plugin then answering for its
   * presence
   */
  private String  pageReference;

  /** Whether the window allows each user to hide it */
  private boolean allowHide;

  /** Label of the site holding the window, computed by the service */
  private String  siteLabel;

  /** Title of the page holding the window, computed by the service */
  private String  pageTitle;

  public HiddenApplication(String type, String name, String title, String pageReference, boolean allowHide) {
    this(type, name, title, pageReference, allowHide, null, null);
  }

}
