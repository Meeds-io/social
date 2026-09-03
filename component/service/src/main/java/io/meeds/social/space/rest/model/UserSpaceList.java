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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A page of a profile owner's spaces.
 *
 * @param spaces the page of spaces
 * @param size the total number of spaces the viewer may see, null unless it was
 *          requested — the widget displays no total and must not pay for the
 *          count query, the "See all" drawer does (eXIP 7.3.0.18, note 50524 §2)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserSpaceList(List<UserSpace> spaces, Integer size) {
}
