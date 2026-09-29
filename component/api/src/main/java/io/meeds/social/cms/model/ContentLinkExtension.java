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
package io.meeds.social.cms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContentLinkExtension {

  private String  objectType;

  private String  titleKey;

  private String  icon;

  private String  command;

  private boolean drawer;

  private boolean hidden;

  /**
   * The i18n key, in the {@code locale.portlet.ContentLink} bundle, of the label
   * a chip of this type shows when its reader may not see the object, or when
   * the object no longer exists: both render alike, the type's icon and this
   * label, without a link, so a chip reveals nothing about the object. Null, the
   * default, keeps the generic rendering of a refused or missing object.
   */
  private String  privateTitleKey;

  /**
   * Builds an extension listed in the insert menu, without a drawer.
   *
   * @param objectType the content object type
   * @param titleKey the i18n key of the type's name
   * @param icon the type's icon class
   * @param command the word typed after the slash
   */
  public ContentLinkExtension(String objectType,
                              String titleKey,
                              String icon,
                              String command) {
    this.objectType = objectType;
    this.titleKey = titleKey;
    this.icon = icon;
    this.command = command;
  }

  /**
   * Builds an extension listed in the insert menu.
   *
   * @param objectType the content object type
   * @param titleKey the i18n key of the type's name
   * @param icon the type's icon class
   * @param command the word typed after the slash
   * @param drawer whether a click on a chip opens the type's drawer
   */
  public ContentLinkExtension(String objectType,
                              String titleKey,
                              String icon,
                              String command,
                              boolean drawer) {
    this.objectType = objectType;
    this.titleKey = titleKey;
    this.icon = icon;
    this.command = command;
    this.drawer = drawer;
  }

  /**
   * Builds an extension without a private label: its refused or missing chips
   * keep the generic rendering.
   *
   * @param objectType the content object type
   * @param titleKey the i18n key of the type's name
   * @param icon the type's icon class
   * @param command the word typed after the slash
   * @param drawer whether a click on a chip opens the type's drawer
   * @param hidden whether the type is hidden from the insert menu
   */
  public ContentLinkExtension(String objectType, // NOSONAR
                              String titleKey,
                              String icon,
                              String command,
                              boolean drawer,
                              boolean hidden) {
    this(objectType, titleKey, icon, command, drawer, hidden, null);
  }

}
