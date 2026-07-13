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
import java.util.Locale;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.identity.model.Identity;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryEntryList;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.model.CategorySearchResult;
import io.meeds.social.category.model.CategoryTree;
import io.meeds.social.category.model.CategoryWithName;

public interface CategoryService {

  public static final String EVENT_SOCIAL_CATEGORY_CREATED       = "social.category.created";

  public static final String EVENT_SOCIAL_CATEGORY_UPDATED       = "social.category.updated";

  public static final String EVENT_SOCIAL_CATEGORY_DELETED       = "social.category.deleted";

  public static final String EVENT_SOCIAL_CATEGORY_ITEM_LINKED   = "social.category.object.linked";

  public static final String EVENT_SOCIAL_CATEGORY_ITEM_UNLINKED = "social.category.object.unlinked";

  /**
   * Retrieves a {@link CategoryTree} with its associated categories switch
   * designated depth and limit from the filter. This method will return only
   * visible categories by the user.
   * 
   * @param filter used filter to query the Category tree
   * @param username User name/login
   * @param locale used {@link Locale} to retrieve translated name
   * @return {@link CategoryTree} switch used filter
   */
  CategoryTree getCategoryTree(CategoryFilter filter, String username, Locale locale);

  /**
   * Searches Categories in flat mode switch a name. This will return only
   * accessible categories by the user.
   * 
   * @param filter used filter to query the Category tree
   * @param username User name/login
   * @param locale used {@link Locale} to retrieve translated name
   * @return a {@link List} of {@link Category} switch used filter
   */
  List<CategorySearchResult> findCategories(CategorySearchFilter filter, String username, Locale locale);

  /**
   * Retrieve sub category identifiers
   * 
   * @param categoryId {@link Category} identifier
   * @param offset Request offset
   * @param limit Request limit. Can be 0 to retrieve all
   * @return {@link List} of Sub category ids
   */
  default List<Long> getSubcategoryIds(long categoryId, long offset, long limit) {
    return getSubcategoryIds(categoryId, offset, limit, 1);
  }

  /**
   * Retrieve sub category identifiers
   * 
   * @param categoryId {@link Category} identifier
   * @param offset Request offset
   * @param limit Request limit. Can be 0 to retrieve all
   * @param depth Category Tree depth. Can be 1 to retrieve only selected or -1
   *          to retrieve all subsequent categories
   * @return {@link List} of Sub category ids
   */
  List<Long> getSubcategoryIds(long categoryId, long offset, long limit, long depth);

  /**
   * Retrieve sub category identifiers
   * 
   * @param username User name/login
   * @param categoryId {@link Category} identifier
   * @param offset Request offset
   * @param limit Request limit. Can be 0 to retrieve all
   * @param depth Category Tree depth. Can be 1 to retrieve only selected or -1
   *          to retrieve all subsequent categories
   * @return {@link List} of Sub category ids
   */
  List<Long> getSubcategoryIds(String username, long categoryId, long offset, long limit, long depth);

  /**
   * Retrieve the {@link List} of ancestor Category Ids starting from nearest
   * ancestor to tree root id
   * 
   * @param categoryId {@link Category} identifier
   * @param username User name/login
   * @return {@link List} of technical {@link Category} identifiers
   */
  List<Long> getAncestorIds(long categoryId, String username) throws ObjectNotFoundException,
                                                              IllegalAccessException;

  /**
   * Retrieve the {@link List} of ancestor Category Ids starting from nearest
   * ancestor to tree root id
   * 
   * @param categoryId {@link Category} identifier
   * @return {@link List} of technical {@link Category} identifiers
   */
  List<Long> getAncestorIds(long categoryId);

  /**
   * Creates a new {@link Category}
   * 
   * @param category {@link Category} to create
   * @param username User name/login
   * @return created {@link Category}
   * @throws ObjectNotFoundException when the {@link Category} designated by the
   *           parentId doesn't exist
   * @throws IllegalAccessException when the user isn't allowed to edit a
   *           categories switch the ownerId field
   * @throws ObjectAlreadyExistsException when attempting to recreate an
   *           existing root tree element
   */
  Category createCategory(Category category, String username) throws ObjectNotFoundException,
                                                              IllegalAccessException,
                                                              ObjectAlreadyExistsException;

  /**
   * @param category {@link Category} to create
   * @param username User name/login
   * @return updated {@link Category}
   * @throws ObjectNotFoundException when the {@link Category} designated by the
   *           parentId or the id of the category itself doesn't exist
   * @throws IllegalAccessException when the user isn't allowed to edit a
   *           categories switch the ownerId field
   */
  Category updateCategory(Category category, String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * @param categoryId {@link Category} identifier
   * @param username User name/login
   * @return deleted {@link Category}
   * @throws ObjectNotFoundException when the {@link Category} designated by the
   *           the id doesn't exist
   * @throws IllegalAccessException when the user isn't allowed to edit a
   *           categories switch the ownerId field
   */
  Category deleteCategory(long categoryId, String username) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * @param categoryId {@link Category} identifier
   * @return {@link Category} if found, else null
   */
  Category getCategory(long categoryId);

  /**
   * @param categoryId {@link Category} identifier
   * @param username User name/login
   * @param locale used {@link Locale} to retrieve translated name
   * @return found {@link Category} with its associated name switch designated
   *         locale
   * @throws ObjectNotFoundException when the {@link Category} designated by the
   *           the id doesn't exist
   * @throws IllegalAccessException when the user isn't allowed to edit a
   *           categories switch the ownerId field
   */
  CategoryWithName getCategory(long categoryId, String username, Locale locale) throws ObjectNotFoundException,
                                                                                IllegalAccessException;

  /**
   * @param ownerId {@link Identity} identifier
   * @return the root {@link Category} of the tree if found, else null
   */
  Category getRootCategory(long ownerId);

  /**
   * @param category {@link Category}
   * @param username User name/login
   * @return true if can edit to the category switch its tree ownerId, else
   *         false
   */
  boolean canEdit(Category category, String username);

  /**
   * @param categoryId {@link Category} identifier
   * @param username User name/login
   * @return true if can edit to the category switch its tree ownerId, else
   *         false
   */
  boolean canEdit(long categoryId, String username);

  /**
   * @param category {@link Category}
   * @param username User name/login
   * @return true if can link an object to the category, else false
   */
  boolean canManageLink(Category category, String username);

  /**
   * @param categoryId {@link Category} identifier
   * @param username User name/login
   * @return true if can link an object to the category, else false
   */
  boolean canManageLink(long categoryId, String username);

  /**
   * Retrieves the entries linked to a Category, de-duplicated (when the same
   * entry is available switch several objectTypes, only the most specific
   * one is kept), filtered switch the accessibility of each item to the
   * designated user, ordered by last modification date descending.
   *
   * @param categoryId {@link Category} identifier
   * @param objectTypes {@link List} of objectTypes to browse
   * @param username User name/login
   * @param offset Request offset
   * @param limit Request limit
   * @return {@link CategoryEntryList} switch used filter
   */
  CategoryEntryList getCategoryEntries(long categoryId, List<String> objectTypes, String username, long offset, long limit);

}
