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
package io.meeds.social.observe.storage;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

import io.meeds.social.observe.model.Observer;
import io.meeds.social.observe.model.ObserverObject;

public class ObserverStorage {

  private static final String      METADATA_NAME = "observers";

  public static final MetadataType METADATA_TYPE = new MetadataType(METADATA_NAME.hashCode(), METADATA_NAME);

  private MetadataService          metadataService;

  public ObserverStorage(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  public List<ObserverObject> getObservedObjects(long identityId, long offset, long limit) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndCreator(METADATA_TYPE.getName(),
                                                                                                identityId,
                                                                                                offset,
                                                                                                limit);
    return metadataItems.stream().map(MetadataItem::getObject).map(ObserverObject::new).toList();
  }

  public List<Long> getObserverIdentityIds(String objectType, String objectId) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE.getName(),
                                                                                               new MetadataObject(objectType,
                                                                                                                  objectId));
    return metadataItems.stream().map(MetadataItem::getCreatorId).toList();
  }

  public void createObserver(Observer observer) throws ObjectAlreadyExistsException {
    long identityId = observer.getIdentityId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(), String.valueOf(identityId), identityId);
    ObserverObject object = observer.getObject();
    metadataService.createMetadataItem(object, metadataKey, identityId);
  }

  public void deleteObserver(Observer observer) throws ObjectNotFoundException {
    long userIdentityId = observer.getIdentityId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(), String.valueOf(userIdentityId), userIdentityId);
    List<MetadataItem> observers = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, observer.getObject());
    if (CollectionUtils.isNotEmpty(observers)) {
      for (MetadataItem observerItem : observers) {
        metadataService.deleteMetadataItem(observerItem.getId(), userIdentityId);
      }
    }
  }

  public boolean isObserved(long identityId, String objectType, String objectId) {
    List<MetadataItem> observerItems = metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(String.valueOf(identityId),
                                                                                                      METADATA_TYPE.getName(),
                                                                                                      objectType,
                                                                                                      objectId,
                                                                                                      0,
                                                                                                      1);
    return !observerItems.isEmpty();
  }

}
