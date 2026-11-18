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

import java.util.List;

/**
 * An attribute of a People Directory filter, as its settings drawer shows it.
 *
 * @param name the profile property name
 * @param values the selected values
 * @param available false when the property is no longer a visible, active,
 *          top-level setting, or the stored entry is not valid: the directory
 *          then lists nobody until the attribute is removed
 */
public record PeopleDirectoryAttribute(String name, List<PeopleDirectoryAttributeValue> values, boolean available) {
}
