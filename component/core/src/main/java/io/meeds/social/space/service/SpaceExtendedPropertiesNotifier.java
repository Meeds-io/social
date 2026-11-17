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
package io.meeds.social.space.service;

import java.util.List;

import org.exoplatform.social.core.space.model.Space;

/**
 * Reports to the space lifecycle listeners the extended properties a writer of
 * this module stored outside {@link SpaceServiceImpl#updateSpace(Space)}: the
 * space security settings writer. Implemented by {@link SpaceServiceImpl},
 * which owns the lifecycle; not part of the space API, so that no caller
 * reports a change it did not store.
 */
public interface SpaceExtendedPropertiesNotifier {

  /**
   * @param space the space as stored after the change
   * @param changedProperties the keys whose stored value changed
   * @param username user who made the change
   */
  void extendedPropertiesUpdated(Space space, List<String> changedProperties, String username);

}
