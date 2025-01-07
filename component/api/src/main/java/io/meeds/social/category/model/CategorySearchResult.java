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

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategorySearchResult extends CategoryWithName {

  private String     name;

  private List<Long> ancestorIds;

  public CategorySearchResult(Category category) {
    super(category.getId(),
          category.getParentId(),
          null,
          category.getIcon(),
          category.getCreatorId(),
          category.getOwnerId(),
          category.getAccessPermissionIds(),
          category.getLinkPermissionIds());
  }

  public CategorySearchResult(CategoryWithName category) {
    super(category.getId(),
          category.getParentId(),
          category.getName(),
          category.getIcon(),
          category.getCreatorId(),
          category.getOwnerId(),
          category.getAccessPermissionIds(),
          category.getLinkPermissionIds());
  }

  public CategorySearchResult(CategoryWithName category, List<Long> ancestorIds) {
    super(category.getId(),
          category.getParentId(),
          category.getName(),
          category.getIcon(),
          category.getCreatorId(),
          category.getOwnerId(),
          category.getAccessPermissionIds(),
          category.getLinkPermissionIds());
    this.ancestorIds = ancestorIds;
  }

  @Override
  public CategorySearchResult clone() { // NOSONAR
    return new CategorySearchResult(this, ancestorIds == null ? null : new ArrayList<>(ancestorIds));
  }
}
