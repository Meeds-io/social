/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.space;

public enum SpaceListAccessType {
  /** Gets the all spaces (for super user). */
  ALL,
  /** Gets the all spaces by filter. */
  ALL_FILTER,
  /** Gets the accessible spaces of the user. */
  ACCESSIBLE,
  /** Gets the accessible spaces of the user by filter. */
  ACCESSIBLE_FILTER,
  /** Gets the invited spaces of the user. */
  INVITED,
  /** Gets the invited spaces of the user by filter. */
  INVITED_FILTER,
  /** Gets the pending spaces of the user. */
  PENDING,
  /** Gets the pending spaces of the user by filter. */
  PENDING_FILTER,
  /** Gets the spaces which the user has the "member" role. */
  MEMBER,
  /** Gets the spaces which the user has the "member" role by filter. */
  MEMBER_FILTER,
  /** Gets the favorite spaces of a user by filter. */
  FAVORITE_FILTER,
  /** Gets the spaces which the user has the "manager" role. */
  MANAGER,
  /** Gets the spaces which the user has the "manager" role by filter. */
  MANAGER_FILTER,
  /** Gets the spaces which are visible and not include these spaces hidden */
  VISIBLE,
  /** Provides SpaceNavigation to get the lastest spaces accessed */
  LASTEST_ACCESSED,
  /** Provides Relationship of Users requesting to join a Space */
  PENDING_REQUESTS,
  /** Gets the spaces which are visited at least once */
  VISITED,
  /** Gets the common spaces between two users */
  COMMON
}
