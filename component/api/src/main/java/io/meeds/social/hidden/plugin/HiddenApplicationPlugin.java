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
package io.meeds.social.hidden.plugin;

import org.exoplatform.services.security.Identity;

import io.meeds.social.hidden.model.HiddenApplication;

/**
 * SPI letting an application type offer each user to hide one of its windows
 * from its page. Declare a Spring bean annotated with
 * {@link org.springframework.stereotype.Service}, so that it is visible to the
 * social context, with a bean name unique across addons. The plugin carries no
 * business logic: it reads the window through its own Service.
 */
public interface HiddenApplicationPlugin {

  /**
   * @return the application type, unique across plugins, used in the REST
   *         paths
   */
  String getApplicationType();

  /**
   * @param name window name
   * @param language language of the returned title
   * @return the window, with its title in the given language and whether it
   *         allows hiding, null when it doesn't exist. Its page reference is
   *         null only for a window on no page (a shared or site layout): the
   *         listing then trusts the plugin for its presence, instead of
   *         checking that its page still holds it.
   */
  HiddenApplication getApplication(String name, String language);

  /**
   * @param name window name
   * @param identity user {@link Identity}
   * @return true when the user can view the window
   */
  boolean canView(String name, Identity identity);

}
