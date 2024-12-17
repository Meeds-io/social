/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.category.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category implements Cloneable {

  /**
   * Technical identifier of the element
   */
  private long       id;

  /**
   * Parent Tree identifier, 0 when it's the root element
   */
  private long       parentId;

  /**
   * The designation of the category, null when no user locale is chosen, else
   * it will depends from user Locale
   */
  private String     name;

  /**
   * Fontawesome Icon identifier
   */
  private String     icon;

  /**
   * Identity Id of the category creator
   */
  private long       creatorId;

  /**
   * Identity Id of the owner of the tree (Owner Id who can manage the tree)
   */
  private long       ownerId;

  /**
   * Access Permissions of the category
   */
  private List<Long> accessPermissionIds;

  /**
   * Link/Use Permissions of the category
   */
  private List<Long> linkPermissionIds;

  @Override
  protected Category clone() { // NOSONAR
    return new Category(id,
                        parentId,
                        name,
                        icon,
                        creatorId,
                        ownerId,
                        accessPermissionIds,
                        linkPermissionIds);
  }
}
