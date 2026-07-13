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
package io.meeds.social.category.service;

import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryObject;

public interface CategoryLinkService {

  public static final String EVENT_CATEGORY_LINK_ADDED   = "category.link.added";

  public static final String EVENT_CATEGORY_LINK_REMOVED = "category.link.removed";

  /**
   * @param object {@link CategoryObject} using type/id to designate any object
   *          in the platform (Space, Activity ...)
   * @return {@link List} of linked {@link Category} identifiers
   */
  List<Long> getLinkedIds(CategoryObject object);

  /**
   * @param objectType {@link CategoryObject} type, example: Space, Activity ...
   * @return {@link List} of linked {@link Category} identifiers
   */
  List<Long> getLinkedIds(String objectType);

  /**
   * @param categoryId {@link Category} identifier
   * @param objectTypes {@link List} of {@link CategoryObject} types to filter
   *          switch
   * @param offset offset of objects to retrieve
   * @param limit limit of objects to retrieve
   * @return {@link List} of {@link CategoryObject} linked to the category,
   *         ordered by last modification date descending
   */
  List<CategoryObject> getLinkedObjects(long categoryId, List<String> objectTypes, int offset, int limit);

  /**
   * @param categoryId {@link Category} identifier
   * @param object {@link CategoryObject} using type/id to designate any object
   *          in the platform (Space, Activity ...)
   * @return true if the object is linked to the category
   */
  boolean isLinked(long categoryId, CategoryObject object);

  /**
   * Creates a link from the designated object to the category
   * 
   * @param categoryId {@link Category} identifier
   * @param object {@link CategoryObject} using type/id to designate any object
   *          in the platform (Space, Activity ...)
   * @param username User name/login
   * @throws ObjectNotFoundException when the category doesn't exist
   * @throws ObjectAlreadyExistsException when the object is already linked to
   *           the category
   * @throws IllegalAccessException when the user doesn't have 'Link' permission
   *           on the category or not having 'edit' permission on the object
   */
  void link(long categoryId, CategoryObject object, String username) throws ObjectNotFoundException,
                                                                     ObjectAlreadyExistsException,
                                                                     IllegalAccessException;

  /**
   * Creates a link from the designated object to the category
   * 
   * @param categoryId {@link Category} identifier
   * @param object {@link CategoryObject} using type/id to designate any object
   *          in the platform (Space, Activity ...)
   */
  void link(long categoryId, CategoryObject object);

  /**
   * Deletes a link of the designated object from the category
   * 
   * @param categoryId {@link Category} identifier
   * @param object {@link CategoryObject} using type/id to designate any object
   *          in the platform (Space, Activity ...)
   * @param username User name/login
   * @throws ObjectNotFoundException when the link doesn't exist
   * @throws IllegalAccessException when the user doesn't have 'Link' permission
   *           on the category or not having 'edit' permission on the object
   */
  void unlink(long categoryId, CategoryObject object, String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * Deletes a link of the designated object from the category
   * 
   * @param categoryId {@link Category} identifier
   * @param object {@link CategoryObject} using type/id to designate any object
   *          in the platform (Space, Activity ...)
   */
  void unlink(long categoryId, CategoryObject object);

}
