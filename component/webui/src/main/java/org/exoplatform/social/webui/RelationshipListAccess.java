/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.webui;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.relationship.model.Relationship;

public class RelationshipListAccess implements ListAccess<Relationship> {
  /** The list is used for relation storage. */
  private final List<Relationship> list;

  /**
   * Initializes the access list.<br>
   *
   * @param list
   *        List is used for initialize.
   */
  public RelationshipListAccess(List<Relationship> list) { this.list = list;}

  /**
   * Implements load method of ListAccess interfaces for load all initial variables.<br>
   * @return result
   *         Array of relationship using in access list.
   */
  public Relationship[] load(int index, int length) throws Exception, IllegalArgumentException {
    if (index < 0)
      throw new IllegalArgumentException("Illegal index: index must be a positive number");

    if (length < 0)
      throw new IllegalArgumentException("Illegal length: length must be a positive number");

    if (index + length > list.size())
      throw new IllegalArgumentException("Illegal index or length: sum of the index " +
              "and the length cannot be greater than the list size");

    Relationship result[] = new Relationship[length];
    for (int i = 0; i < length; i++)
      result[i] = list.get(i + index);

    return result;
  }

  /**
   * Gets the number of element in accessing list.<br>
   *
   * @return size
   *         Number of element in list.
   */
  public int getSize() throws Exception { return list.size();}
}