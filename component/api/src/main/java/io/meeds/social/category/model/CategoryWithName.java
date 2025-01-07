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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryWithName extends Category {

  /**
   * The designation of the category, null when no user locale is chosen, else
   * it will depends from user Locale
   */
  private String name;

  public CategoryWithName(Category category,
                          String name) {
    super(category.getId(),
          category.getParentId(),
          category.getIcon(),
          category.getCreatorId(),
          category.getOwnerId(),
          category.getAccessPermissionIds(),
          category.getLinkPermissionIds());
    this.name = name;
  }

  public CategoryWithName(long id, // NOSONAR
                          long parentId,
                          String name,
                          String icon,
                          long creatorId,
                          long ownerId,
                          List<Long> accessPermissionIds,
                          List<Long> linkPermissionIds) {
    super(id, parentId, icon, creatorId, ownerId, accessPermissionIds, linkPermissionIds);
    this.name = name;
  }

  @Override
  protected CategoryWithName clone() { // NOSONAR
    return new CategoryWithName(getId(),
                                getParentId(),
                                name,
                                getIcon(),
                                getCreatorId(),
                                getOwnerId(),
                                getAccessPermissionIds(),
                                getLinkPermissionIds());
  }

}
