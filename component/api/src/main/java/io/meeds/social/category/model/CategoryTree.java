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
public class CategoryTree extends Category {

  private List<CategoryTree> categories;

  private long               offset;

  private long               limit;

  private long               size;

  private boolean            canLink;

  public CategoryTree(Category category) {
    super(category.getId(),
          category.getParentId(),
          category.getName(),
          category.getIcon(),
          category.getCreatorId(),
          category.getOwnerId(),
          category.getAccessPermissionIds(),
          category.getLinkPermissionIds());
  }

  public CategoryTree(CategoryTree categoryTree) {
    super(categoryTree.getId(),
          categoryTree.getParentId(),
          categoryTree.getName(),
          categoryTree.getIcon(),
          categoryTree.getCreatorId(),
          categoryTree.getOwnerId(),
          categoryTree.getAccessPermissionIds(),
          categoryTree.getLinkPermissionIds());
    this.categories = categoryTree.getCategories();
    this.offset = categoryTree.getOffset();
    this.limit = categoryTree.getLimit();
    this.size = categoryTree.getSize();
    this.canLink = categoryTree.isCanLink();
  }

}
