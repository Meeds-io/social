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
package io.meeds.social.space.model;

import java.util.Set;

import io.meeds.social.space.constant.SpaceMembershipStatus;

/**
 * The statuses a {@link MembershipChange} actually added and removed for its
 * user. A writer links or removes the space group's memberships, and fires its
 * events, from this answer only.
 *
 * @param added statuses added
 * @param removed statuses removed
 */
public record MembershipChangeResult(Set<SpaceMembershipStatus> added, Set<SpaceMembershipStatus> removed) {

  public boolean isEmpty() {
    return added.isEmpty() && removed.isEmpty();
  }

}
