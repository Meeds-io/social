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
package io.meeds.social.space.rest.model;

/**
 * A space of a profile owner, as it is displayed to a viewer.
 * <p>
 * Everything the listing renders is carried here on purpose. A viewer may see a
 * space they are not a member of — a PRIVATE space is listed, only HIDDEN ones
 * are filtered out — and resolving such a space through the per-space endpoints
 * would be refused, leaving the UI to render a placeholder for a space it was
 * told to display. {@code member} lets the UI present the two cases
 * differently instead.
 *
 * @param id space technical identifier
 * @param displayName space name as displayed
 * @param prettyName space name as used in URLs
 * @param url space home URL
 * @param avatarUrl space avatar URL, null when the space has none
 * @param visibility space visibility
 * @param membersCount number of members of the space
 * @param member whether the viewer is a member of the space
 */
public record UserSpace(long id,
                        String displayName,
                        String prettyName,
                        String url,
                        String avatarUrl,
                        String visibility,
                        int membersCount,
                        boolean member) {
}
