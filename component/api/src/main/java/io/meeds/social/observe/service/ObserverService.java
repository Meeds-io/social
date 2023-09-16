/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.observe.service;

import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.observe.model.ObserverObject;
import io.meeds.social.observe.plugin.ObserverPlugin;

public interface ObserverService {

  String OBSERVATION_SAVED_EVENT_NAME   = "observe.added";

  String OBSERVATION_DELETED_EVENT_NAME = "observe.deleted";

  /**
   * Add a new {@link ObserverPlugin} for a given Object Type
   * 
   * @param observerPlugin {@link ObserverPlugin}
   */
  void addPlugin(ObserverPlugin observerPlugin);

  /**
   * Removes a {@link ObserverPlugin} identified by its objectType
   * 
   * @param objectType Object type
   */
  void removePlugin(String objectType);

  default void createObserver(long identityId, String objectType, String objectId) throws IllegalAccessException,
                                                                                   ObjectAlreadyExistsException,
                                                                                   ObjectNotFoundException {
    createObserver(identityId, objectType, objectId, null);
  }

  void createObserver(long identityId, String objectType, String objectId, String parentObjectId) throws IllegalAccessException,
                                                                                                  ObjectAlreadyExistsException,
                                                                                                  ObjectNotFoundException;

  void deleteObserver(long identityId, String objectType, String objectId) throws ObjectNotFoundException;

  boolean isObserved(long identityId, String objectType, String objectId);

  boolean isObservable(long identityId, String objectType, String objectId);

  List<ObserverObject> getObservedObjects(long identityId, long offset, long limit);

  List<Long> getObserverIdentityIds(String objectType, String objectId);

}
