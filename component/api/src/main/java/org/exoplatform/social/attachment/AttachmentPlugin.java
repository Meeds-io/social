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
package org.exoplatform.social.attachment;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;

/**
 * A plugin that will be used by AttachmentServiceImpl to check the edit and
 * access permission of an attachment
 */
public abstract class AttachmentPlugin extends BaseComponentPlugin {

  /**
   * @return entity types that plugin handles
   */
  public abstract String getObjectType();

  /**
   * Checks whether the user can access to an entity
   *
   * @param  userIdentity            user identity
   * @param  entityId                object technical unique identifier
   * @return                         true if the user can access to an entity,
   *                                 else false.
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public abstract boolean hasAccessPermission(Identity userIdentity, String entityId) throws ObjectNotFoundException;

  /**
   * Checks whether the user can edit an entity
   *
   * @param  userIdentity            user identity
   * @param  entityId                object technical unique identifier
   * @return                         true if the user can edit an entity, else
   *                                 false.
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public abstract boolean hasEditPermission(Identity userIdentity, String entityId) throws ObjectNotFoundException;

  /**
   * Retrieves the identity Id of the target audience for which the attachment
   * will be accessible
   * 
   * @param  objectId                Object Identifier
   * @return                         {@link org.exoplatform.social.core.identity.model.Identity}
   *                                 Id as long
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public abstract long getAudienceId(String objectId) throws ObjectNotFoundException;

  /**
   * Retrieves the Space Id of the target space for which the attachment will be
   * accessible
   * 
   * @param  objectId                Object Identifier
   * @return                         {@link Space} Id as long
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public abstract long getSpaceId(String objectId) throws ObjectNotFoundException;

}
