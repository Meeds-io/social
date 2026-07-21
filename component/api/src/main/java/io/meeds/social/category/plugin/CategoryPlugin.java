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
package io.meeds.social.category.plugin;

import java.util.List;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryEntryItem;
import io.meeds.social.category.model.CategoryObject;

public interface CategoryPlugin {

  /**
   * @return The managed Object Type (Space, Activity...)
   **/
  String getType();

  /**
   * Checks whether an associated object to a category is accessible to a user.
   * This check will be called after checking whether the user can access or not
   * to the category (generic ACL check made in Category API based on Category
   * properties)
   *
   * @param objectId Object technical identifier
   * @param username User technical name (login identifier)
   * @return true if the user has access permission to the designated Object
   *         Type
   **/
  boolean canAccess(String objectId, String username);

  /**
   * Checks whether an associated object to a category is editable to a user.
   * This check will be used mainly used to know if a user can modify the object
   * in order to associate/link a category into it. This check will be called
   * after checking whether the user can link or not an object to the category
   * (generic ACL check made in Category API based on Category properties)
   *
   * @param objectId Object technical identifier
   * @param username User technical name (login identifier)
   * @return true if the user has access permission to the designated Object
   *         Type identified by its Id
   **/
  boolean canEdit(String objectId, String username);

  /**
   * @return {@link List} of {@link Category} Ids associated to
   *         objects
   */
  default List<Long> getCategoryIds() {
    throw new UnsupportedOperationException();
  }

  /**
   * param spaceId Space Identifier
   * @return {@link List} of {@link Category} Ids associated to
   *         objects for the designated spaceId
   */
  default List<Long> getCategoryIds(long spaceId) {
    return getCategoryIds();
  }

  /**
   * param spaceId Space Identifier
   * @param username User technical name (login identifier)
   * @return {@link List} of {@link Category} Ids associated to
   *         objects for the designated spaceId
   */
  default List<Long> getCategoryIds(long spaceId, String username) {
    return getCategoryIds();
  }

  default CategoryObject getObject(CategoryObject metadataObject) {
    return metadataObject;
  }

  /**
   * Resolves a generic, object-type agnostic preview (title, summary, image,
   * author, date, url ...) of the designated object, so that it can be
   * displayed in a Category entries listing (browse entries by Category)
   * without the caller having to know anything specific to this objectType.
   *
   * @param objectId Object technical identifier
   * @param username User technical name (login identifier)
   * @return {@link CategoryEntryItem} or null when this objectType isn't
   *         meant to be browsable as an entry (Space, Activity ...)
   */
  default CategoryEntryItem getEntryItem(String objectId, String username) {
    return null;
  }

}
