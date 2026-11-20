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
package io.meeds.social.people.model;

/**
 * A group of a People Directory window's group filter, as the window stores
 * it.
 *
 * @param groupId the group id, {@code /spaces/<name>} for a space's group
 * @param spaceId the id of the space whose group it is, resolved by the
 *          server when the filter is saved, null for any other group: a space
 *          deleted then re-created under the same name gets a new id, and the
 *          stored group then designates no space
 */
public record DirectoryGroup(String groupId, String spaceId) {
}
