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
 * The groups of a People Directory window's filter, as the settings drawer
 * shows them to an editor.
 *
 * @param groups the stored groups the editor can select, shown as chips
 * @param otherGroupsApplied whether the filter also holds groups the editor
 *          cannot select: they stay applied, and the drawer says so without
 *          naming them
 * @param otherGroupsCount for an editor who may set the filter, the number of
 *          those groups, which count against the bound of the filter; 0
 *          otherwise
 * @param unresolvableGroups whether, for an editor who may set the filter,
 *          stored groups no longer exist: they match nobody until the editor
 *          selects groups or turns the filter OFF
 */
public record PeopleDirectoryEditorGroups(List<PeopleDirectoryGroup> groups,
                                          boolean otherGroupsApplied,
                                          int otherGroupsCount,
                                          boolean unresolvableGroups) {
}
