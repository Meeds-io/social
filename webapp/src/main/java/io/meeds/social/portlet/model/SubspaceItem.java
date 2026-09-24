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
package io.meeds.social.portlet.model;

/**
 * One listed sub-space. {@code isMember} and {@code isInvited} carry the
 * viewer's relationship to it for the hidden-space rendering only; they are
 * not access decisions, which the Service already made by returning the
 * space at all. Both are needed because the space REST endpoints serve a
 * member <em>and</em> an invited user alike, so a row is only rendered
 * anonymously when the viewer is neither.
 */
public record SubspaceItem(String id,
                           String displayName,
                           String prettyName,
                           String avatarUrl,
                           String visibility,
                           boolean isMember,
                           boolean isInvited) {
}
