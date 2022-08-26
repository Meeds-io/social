/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2022 Meeds Association contact@meeds.io
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
 * A plugin that will be used by FavoriteServiceImpl to check ACL of create
 * favorite
 */
public abstract class FavoriteACLPlugin extends BaseComponentPlugin {

  /**
   * @return entity types that plugin handles
   */
  public abstract String getEntityType();

  /**
   * Checks whether the user can mark an entity as favorite
   *
   * @param userIdentity user identity
   * @param entityId object technical unique identifier
   * @return true if the user can mark entity as favorite, else false.
   */
  public abstract boolean canCreateFavorite(Identity userIdentity, String entityId);
}
