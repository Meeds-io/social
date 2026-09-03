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
package io.meeds.social.space.constant;

/**
 * Scope of a profile owner's spaces listing, as requested by a viewer.
 * <p>
 * This is a client input: it can only ever narrow what is returned. The Service
 * layer forces {@link #COMMON} for an external viewer, whatever was requested.
 */
public enum UserSpacesScope {
  /**
   * Only the spaces where both the viewer and the profile owner have the member
   * role.
   */
  COMMON,
  /**
   * Every space where the profile owner has the member role, minus the hidden
   * ones the viewer is not a member of.
   */
  ALL
}
