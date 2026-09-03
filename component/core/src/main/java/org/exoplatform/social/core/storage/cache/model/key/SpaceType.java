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
package org.exoplatform.social.core.storage.cache.model.key;

public enum SpaceType {
  MEMBER,
  MANAGER,
  PENDING,
  INVITED,
  PUBLIC,
  ACCESSIBLE,
  VISIBLE,
  ALL,
  LATEST_ACCESSED,
  MEMBER_IDENTITY_IDS,
  MEMBER_IDS,
  /**
   * A profile owner's spaces, restricted to the ones shared with the viewer
   * carried by {@code SpaceFilterKey#getViewerId()}.
   */
  USER_SPACES_COMMON,
  /**
   * A profile owner's spaces, minus the hidden ones the viewer carried by
   * {@code SpaceFilterKey#getViewerId()} is not a member of.
   */
  USER_SPACES_ALL
}
