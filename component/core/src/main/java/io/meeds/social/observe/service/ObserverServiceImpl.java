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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.observe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.observe.model.Observer;
import io.meeds.social.observe.model.ObserverObject;
import io.meeds.social.observe.plugin.ObserverPlugin;
import io.meeds.social.observe.storage.ObserverStorage;

public class ObserverServiceImpl implements ObserverService {

  private static final Log            LOG             = ExoLogger.getLogger(ObserverServiceImpl.class);

  private ObserverStorage             observerStorage;

  private ListenerService             listenerService;

  private Map<String, ObserverPlugin> observerPlugins = new HashMap<>();

  public ObserverServiceImpl(ObserverStorage observerStorage, ListenerService listenerService) {
    this.observerStorage = observerStorage;
    this.listenerService = listenerService;
  }

  @Override
  public void addPlugin(ObserverPlugin observerPlugin) {
    observerPlugins.put(observerPlugin.getObjectType(), observerPlugin);
  }

  @Override
  public void removePlugin(String objectType) {
    observerPlugins.remove(objectType);
  }

  @Override
  public List<ObserverObject> getObservedObjects(long identityId, long offset, long limit) {
    return observerStorage.getObservedObjects(identityId, offset, limit);
  }

  @Override
  public List<Long> getObserverIdentityIds(String objectType, String objectId) {
    return observerStorage.getObserverIdentityIds(objectType, objectId);
  }

  @Override
  public void createObserver(long identityId,
                             String objectType,
                             String objectId,
                             String parentObjectId) throws IllegalAccessException,
                                                    ObjectAlreadyExistsException,
                                                    ObjectNotFoundException {
    ObserverPlugin observerPlugin = observerPlugins.get(objectType);
    if (!observerPlugin.canObserve(identityId, objectId)) {
      throw new IllegalAccessException(String.format("User %s is not allowed to observe object %s/%s",
                                                     identityId,
                                                     objectType,
                                                     objectId));
    } else if (isObserved(identityId, objectType, objectId)) {
      throw new ObjectAlreadyExistsException(String.format("User %s is already observing object %s/%s",
                                                           identityId,
                                                           objectType,
                                                           objectId));
    }
    ObserverObject observerObject = getObserverObject(objectType, objectId, parentObjectId, observerPlugin.getSpaceId(objectId));
    Observer observer = new Observer(identityId, observerObject);
    observerStorage.createObserver(observer);

    ObserverObject extendedObserverObject = observerPlugin.getExtendedObserverObject(objectId);
    if (extendedObserverObject != null) {
      observerStorage.createObserver(new Observer(identityId, extendedObserverObject));
    }
    broadcastEvent(OBSERVATION_SAVED_EVENT_NAME, observer);
  }

  @Override
  public void deleteObserver(long identityId, String objectType, String objectId) throws ObjectNotFoundException {
    if (!isObserved(identityId, objectType, objectId)) {
      throw new ObjectNotFoundException(String.format("User %s isn't observing object %s/%s", identityId, objectType, objectId));
    }
    ObserverObject observerObject = getObserverObject(objectType, objectId);
    Observer observer = new Observer(identityId, observerObject);
    observerStorage.deleteObserver(observer);
    broadcastEvent(OBSERVATION_DELETED_EVENT_NAME, observer);
  }

  @Override
  public boolean isObserved(long identityId, String objectType, String objectId) {
    try {
      ObserverObject observerObject = getObserverObject(objectType, objectId);
      return observerStorage.isObserved(identityId, observerObject.getType(), observerObject.getId());
    } catch (ObjectNotFoundException e) {
      return false;
    }
  }

  @Override
  public boolean isObservable(long identityId, String objectType, String objectId) {
    ObserverPlugin observerPlugin = observerPlugins.get(objectType);
    try {
      return observerPlugin != null && observerPlugin.canObserve(identityId, objectId);
    } catch (ObjectNotFoundException e) {
      return false;
    }
  }

  private void broadcastEvent(String eventName, Observer observer) {
    try {
      listenerService.broadcast(eventName, observer, null);
    } catch (Exception e) {
      LOG.warn("An error occurred while broadcasting event {} for observer {}", observer, e);
    }
  }

  private ObserverObject getObserverObject(String objectType,
                                           String objectId,
                                           String parentObjectId,
                                           long spaceId) throws ObjectNotFoundException {
    ObserverObject observerObject = getObserverObject(objectType, objectId);
    if (StringUtils.isBlank(observerObject.getParentId())) {
      observerObject.setParentId(parentObjectId);
    }
    if (observerObject.getSpaceId() == 0) {
      observerObject.setSpaceId(spaceId);
    }
    return observerObject;
  }

  private ObserverObject getObserverObject(String objectType, String objectId) throws ObjectNotFoundException {
    ObserverPlugin observerPlugin = observerPlugins.get(objectType);
    ObserverObject observerObject = observerPlugin.getExtendedObserverObject(objectId);
    if (observerObject == null) {
      return new ObserverObject(objectType, objectId, null, observerPlugin.getSpaceId(objectId));
    } else {
      return observerObject;
    }
  }

}
