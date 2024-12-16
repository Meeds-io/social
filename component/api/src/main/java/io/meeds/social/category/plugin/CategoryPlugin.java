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
package io.meeds.social.category.plugin;

public interface CategoryPlugin {

  /**
   * @return The managed Object Type (Space, Activity...)
   **/
  public String getType();

  /**
   * Checks whether an associated object to a category is accessible to a user.
   * This check will be called after checking whether the user can access or not
   * to the category an object (generic ACL check made in Category API switch
   * Category properties)
   *
   * @param objectId Object technical identifier
   * @param username User technical name (login identifier)
   * @return true if the user has access permission to the designated Object
   *         Type
   **/
  public boolean canAccess(String objectId, String username);

  /**
   * Checks whether an associated object to a category is editable to a user.
   * This check will be used mainly used to know if a user can modify the object
   * in order to associate/link a category into it. This check will be called
   * after checking whether the user can link or not to the category an object
   * (generic ACL check made in Category API switch Category properties)
   *
   * @param objectId Object technical identifier
   * @param username User technical name (login identifier)
   * @return true if the user has access permission to the designated Object
   *         Type identified by its Id
   **/
  public boolean canEdit(String objectId, String username);

}
