/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.metadata;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.security.Identity;

/**
 * A plugin that will be used by AttachementServiceImpl to check the edit
 * and access permission of an attachement
 */

public abstract class AttachementPermissionPlugin extends BaseComponentPlugin{
  
  /**
   * @return entity types that plugin handles
   */
  public abstract String getEntityType();

  /**
   * Checks whether the user can access to an entity
   *
   * @param userIdentity user identity
   * @param entityId object technical unique identifier
   * @return true if the user can access to an entity, else false.
   */
  public abstract boolean hasAccessPermission(Identity userIdentity, String entityId);
  
  /**
   * Checks whether the user can edit an entity
   *
   * @param userIdentity user identity
   * @param entityId object technical unique identifier
   * @return true if the user can edit an entity, else false.
   */
  public abstract boolean hasEditPermission(Identity userIdentity, String entityId);


}
