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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The filter a People Directory instance applies to every list it serves,
 * whoever the viewer: the server builds it from the instance's settings, the
 * client never sends it. Its attribute and group criteria must all be
 * matched.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeopleDirectoryFilter implements Cloneable {

  /**
   * Per profile property name, the values a listed user must hold one of, as
   * the profile index holds them
   * ({@code ProfileIndexingServiceConnector#toIndexedValue}). Several
   * properties must all be matched.
   */
  private Map<String, List<String>> attributes = new LinkedHashMap<>();

  /**
   * The ids of the groups a listed user must be a member of one of, a member
   * of one of their sub-groups included; null when the directory is not
   * filtered by group, empty when none of its stored groups still exists, so
   * that it lists nobody.
   */
  private List<String>              groups;

  public PeopleDirectoryFilter(Map<String, List<String>> attributes) {
    this.attributes = attributes;
  }

  @Override
  public PeopleDirectoryFilter clone() { // NOSONAR
    return new PeopleDirectoryFilter(attributes == null ? null : new LinkedHashMap<>(attributes),
                                     groups == null ? null : new ArrayList<>(groups));
  }
}
