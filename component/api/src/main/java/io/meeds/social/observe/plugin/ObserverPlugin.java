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
package io.meeds.social.observe.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.observe.model.ObserverObject;
import io.meeds.social.observe.service.ObserverService;

/**
 * A plugin that will be used by {@link ObserverService} to check the edit and
 * access permission of observed objects
 */
public abstract class ObserverPlugin extends BaseComponentPlugin {

  /**
   * @return object types that plugin handles
   */
  public abstract String getObjectType();

  /**
   * Checks whether the user can watch an object
   *
   * @param objectId object technical unique identifier
   * @param identityId {@link Identity} id
   * @return true if the user can access to an object, else false.
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public abstract boolean canObserve(long identityId, String objectId) throws ObjectNotFoundException;

  /**
   * Retrieves the identity Id of the target audience for which the observation
   * will be possible
   * 
   * @param objectId Object Identifier
   * @return {@link org.exoplatform.social.core.identity.model.Identity} Id as
   *         long
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public abstract long getAudienceId(String objectId) throws ObjectNotFoundException;

  /**
   * Retrieves the Space Id of the target space for which which the observation
   * will be possible
   * 
   * @param objectId Object Identifier
   * @return {@link Space} Id as long
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public abstract long getSpaceId(String objectId) throws ObjectNotFoundException;

  /**
   * Retrieves a specific Observed Object to watch/unwatch at the same time than
   * the original content type. Usefull for Activty Stream with Specific
   * Activity Types for example.
   * 
   * @param objectId Object Identifier
   * @return {@link ObserverObject} if extended by the plugin with the
   *         identified object, else null
   * @throws ObjectNotFoundException thrown when the object doesn't exists
   */
  public ObserverObject getExtendedObserverObject(String objectId) throws ObjectNotFoundException { // NOSONAR
    return null;
  }

}
