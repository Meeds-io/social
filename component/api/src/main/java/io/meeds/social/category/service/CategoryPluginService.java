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

import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryEntryItem;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.plugin.CategoryPlugin;

public interface CategoryPluginService {

  /**
   * @param objectType {@link CategoryObject} type
   * @return associated {@link CategoryPlugin} managing this type of objects
   */
  CategoryPlugin getCategoryPlugin(String objectType);

  /**
   * @param categoryPlugin add new {@link CategoryPlugin}
   */
  void addPlugin(CategoryPlugin categoryPlugin);

  /**
   * @param objectType {@link CategoryObject} type
   * @param objectId {@link CategoryObject} id
   * @param username User technical name (login identifier)
   * @return true if user can edit the object identified by its id and type,
   *         else false
   */
  boolean canEdit(String objectType, String objectId, String username);

  /**
   * @param objectType {@link CategoryObject} type
   * @param objectId {@link CategoryObject} id
   * @param username User technical name (login identifier)
   * @return true if user can access the object identified by its id and type,
   *         else false
   */
  boolean canAccess(String objectType, String objectId, String username);

  /**
   * @param objectType {@link CategoryObject} type
   * @param objectId {@link CategoryObject} id
   * @param username User technical name (login identifier)
   * @return {@link CategoryEntryItem} preview of the object identified by
   *         its id and type, or null when this objectType isn't meant to be
   *         browsable as an entry
   */
  CategoryEntryItem getEntryItem(String objectType, String objectId, String username);

  /**
   * @param objectType {@link CategoryObject} type
   * @param spaceId {@link Space} identifier
   * @param username User technical name (login identifier)
   * @return {@link List} of {@link Category} Ids associated to an object type
   */
  List<Long> getCategoryIds(String objectType, long spaceId, String username);

  /**
   * In some cases such as Activities, the Metadata Object is different, thus
   * this method is used to switch the associated {@link CategoryObject}
   *
   * @param object {@link CategoryObject}
   * @return {@link CategoryObject}
   */
  CategoryObject getObject(CategoryObject object);

}
