/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.metadata;

import java.util.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.metadata.storage.MetadataStorage;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.*;

public class MetadataServiceImpl implements MetadataService {

  private static final Log                LOG                 = ExoLogger.getLogger(MetadataServiceImpl.class);

  private MetadataStorage                 metadataStorage;

  private ListenerService                 listenerService;

  private Map<String, MetadataTypePlugin> metadataTypePlugins = new HashMap<>();

  public MetadataServiceImpl(MetadataStorage metadataStorage, ListenerService listenerService) {
    this.metadataStorage = metadataStorage;
    this.listenerService = listenerService;
  }

  @Override
  public Metadata createMetadata(Metadata metadata, long userIdentityId) {
    if (metadata == null) {
      throw new IllegalArgumentException("Metadata is mandatory");
    }
    if (metadata.getType() == null) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    String metadataTypeName = metadata.getType().getName();
    MetadataType type = getMetadataTypeByName(metadataTypeName);
    if (type == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataTypeName + " is not registered as a plugin");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }

    metadata.setType(metadata.getType());
    metadata.setCreatorId(userIdentityId);
    metadata = metadataStorage.createMetadata(metadata);
    try {
      this.listenerService.broadcast("social.metadata.created", userIdentityId, metadata);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata creation", e);
    }
    return metadata;
  }

  @Override
  public Metadata getMetadataByKey(MetadataKey metadataKey) {
    if (metadataKey == null) {
      throw new IllegalArgumentException("Metadata Key is mandatory");
    }
    if (StringUtils.isBlank(metadataKey.getType())) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    if (StringUtils.isBlank(metadataKey.getName())) {
      throw new IllegalArgumentException("Metadata Name is mandatory");
    }
    return metadataStorage.getMetadataByKey(metadataKey);
  }

  @Override
  public MetadataItem createMetadataItem(MetadataObjectKey metadataObject,
                                         MetadataKey metadataKey,
                                         long userIdentityId) throws ObjectAlreadyExistsException {
    if (metadataObject == null) {
      throw new IllegalArgumentException("Metadata Item Object is mandatory");
    }
    if (metadataKey == null) {
      throw new IllegalArgumentException("Metadata Key is mandatory");
    }
    if (StringUtils.isBlank(metadataObject.getType())) {
      throw new IllegalArgumentException("Metadata Item Object Type is mandatory");
    }
    if (StringUtils.isBlank(metadataObject.getId())) {
      throw new IllegalArgumentException("Metadata Item Object Id is mandatory");
    }
    String metadataTypeName = metadataKey.getType();
    if (StringUtils.isBlank(metadataTypeName)) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    if (StringUtils.isBlank(metadataKey.getName())) {
      throw new IllegalArgumentException("Metadata Name is mandatory");
    }
    MetadataType metadataType = getMetadataTypeByName(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataTypeName + " is not registered as a plugin");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }

    Metadata metadata = getMetadataByKey(metadataKey);
    if (metadata == null) {
      metadata = new Metadata();
      metadata.setName(metadataKey.getName());
      metadata.setType(metadataType);
      metadata.setAudienceId(metadataKey.getAudienceId());
      metadata = createMetadata(metadata, userIdentityId);
    }
    MetadataItem metadataItem = new MetadataItem(0,
                                                 metadata,
                                                 metadataObject,
                                                 userIdentityId,
                                                 System.currentTimeMillis(),
                                                 null);
    if (!isAllowMultipleItemsPerObject(metadataTypeName)) {
      List<MetadataItem> storedMetadataItems = metadataStorage.getMetadataItemsByMetadataAndObject(metadata.getId(),
                                                                                                   metadataItem.getObject());
      if (!storedMetadataItems.isEmpty()) {
        throw new ObjectAlreadyExistsException(storedMetadataItems.get(0));
      }
    }
    metadataItem = metadataStorage.createMetadataItem(metadataItem);
    try {
      this.listenerService.broadcast("social.metadataItem.created", userIdentityId, metadataItem);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata item creation", e);
    }
    return metadataItem;
  }

  @Override
  public MetadataItem deleteMetadataItem(long itemId, long userIdentityId) throws ObjectNotFoundException {
    if (itemId <= 0) {
      throw new IllegalArgumentException("Metadata Item Technical Identifier is mandatory");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }

    MetadataItem metadataItem = this.metadataStorage.getMetadataItemById(itemId);
    if (metadataItem == null) {
      throw new ObjectNotFoundException("Metadata Item with identifier " + itemId + " wasn't found");
    }
    metadataItem = this.metadataStorage.deleteMetadataItemById(itemId);
    try {
      this.listenerService.broadcast("social.metadataItem.deleted", userIdentityId, metadataItem);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata item creation", e);
    }
    return metadataItem;
  }

  @Override
  public void deleteMetadataItemsByObject(MetadataObjectKey object) {
    this.metadataStorage.deleteMetadataItemsByObject(object);
  }

  @Override
  public void deleteMetadataItemsByParentObject(MetadataObjectKey object) {
    this.metadataStorage.deleteMetadataItemsByParentObject(object);
  }

  @Override
  public List<MetadataItem> shareMetadataItemsByObject(MetadataObjectKey sourceObject,
                                                       String targetObjectId,
                                                       long audienceId,
                                                       long creatorId) {
    List<MetadataItem> sharedMetadataItems = new ArrayList<>();

    List<MetadataItem> metadataItems = getMetadataItemsByObject(sourceObject);
    for (MetadataItem metadataItem : metadataItems) {
      MetadataItem sharedMetadataItem = shareMetadataItem(metadataItem.getObject(),
                                                          metadataItem.getMetadata().key(),
                                                          targetObjectId,
                                                          audienceId,
                                                          creatorId);
      if (sharedMetadataItem != null) {
        sharedMetadataItems.add(sharedMetadataItem);
      }
    }
    try {
      this.listenerService.broadcast("social.metadataItem.shared", sourceObject, targetObjectId);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata item creation", e);
    }
    return sharedMetadataItems;
  }

  @Override
  public List<MetadataItem> getMetadataItemsByObject(MetadataObjectKey object) {
    return this.metadataStorage.getMetadataItemsByObject(object);
  }

  @Override
  public Set<String> getMetadataNamesByObject(MetadataObjectKey object) {
    return this.metadataStorage.getMetadataNamesByObject(object);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataAndObject(MetadataKey metadataKey, MetadataObjectKey object) {
    Metadata metadata = getMetadataByKey(metadataKey);
    if (metadata == null) {
      return Collections.emptyList();
    }
    return this.metadataStorage.getMetadataItemsByMetadataAndObject(metadata.getId(), object);
  }

  @Override
  public List<String> getMetadataObjectIds(String metadataType, String metadataName, String objectType, long offset, long limit) {
    return this.metadataStorage.getMetadataObjectIds(metadataType, metadataName, objectType, offset, limit);
  }

  @Override
  public void addMetadataTypePlugin(MetadataTypePlugin metadataTypePlugin) {
    if (metadataTypePlugins.values()
                           .stream()
                           .anyMatch(registeredPlugin -> StringUtils.equals(registeredPlugin.getName(),
                                                                            metadataTypePlugin.getName())
                               || registeredPlugin.getId() == metadataTypePlugin.getId())) {
      throw new UnsupportedOperationException("Overriding existing Metadata Type is not allowed. Please verify the unicity of Metadata Type id and name.");
    }
    this.metadataStorage.addMetadataType(metadataTypePlugin.getMetadataType());
    this.metadataTypePlugins.put(metadataTypePlugin.getName(), metadataTypePlugin);
  }

  @Override
  public MetadataTypePlugin getMetadataTypePluginByName(String name) {
    return this.metadataTypePlugins.get(name);
  }

  @Override
  public MetadataType getMetadataTypeByName(String name) {
    MetadataTypePlugin plugin = getMetadataTypePluginByName(name);
    return plugin == null ? null : plugin.getMetadataType();
  }

  @Override
  public List<MetadataType> getMetadataTypes() {
    return this.metadataStorage.getMetadataTypes();
  }

  private MetadataItem shareMetadataItem(MetadataObjectKey metadataObject,
                                         MetadataKey metadataKey,
                                         String targetObjectId,
                                         long audienceId,
                                         long creatorId) {
    if (isShareable(metadataKey.getType())) {
      MetadataObjectKey metadataObjectToShare = metadataObject.clone();
      metadataObjectToShare.setId(targetObjectId);
      MetadataKey metadataKeyToShare = metadataKey.clone();
      metadataKeyToShare.setAudienceId(audienceId);

      try {
        return createMetadataItem(metadataObjectToShare,
                                  metadataKeyToShare,
                                  creatorId);
      } catch (ObjectAlreadyExistsException e) {
        LOG.warn("The metadata object {} is already associated to Metadata with unique key {}."
            + " This doesn't affect the expected result, so continue processing.",
                 metadataObjectToShare,
                 metadataKeyToShare,
                 e);
      }
    }
    return null;
  }

  private boolean isShareable(String metadataTypeName) {
    MetadataTypePlugin metadataTypePlugin = getMetadataTypePluginByName(metadataTypeName);
    return metadataTypePlugin != null && metadataTypePlugin.isShareable();
  }

  private boolean isAllowMultipleItemsPerObject(String metadataType) {
    MetadataTypePlugin plugin = getMetadataTypePluginByName(metadataType);
    return plugin.isAllowMultipleItemsPerObject();
  }

}
