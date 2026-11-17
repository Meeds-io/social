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
package io.meeds.social.space.security.plugin;

import org.exoplatform.social.core.space.model.Space;

/**
 * Contributed by an addon that must act before the "Keep documents inside the
 * space" setting changes. Implementations are collected at call time among the
 * beans of every addon, so an implementation carries {@code @Service}, is not
 * final and has a bean name of its own.
 */
public interface SpaceSecuritySettingsPlugin {

  /**
   * Called before the change is written, outside its transaction. Throwing
   * refuses the whole change: nothing is written.
   *
   * @param space the space whose setting changes
   * @param newValue the value about to be stored
   * @param username the user changing the setting
   * @throws Exception when the change must not be applied
   */
  void beforeDocumentsKeptInsideChange(Space space, boolean newValue, String username) throws Exception; // NOSONAR

}
