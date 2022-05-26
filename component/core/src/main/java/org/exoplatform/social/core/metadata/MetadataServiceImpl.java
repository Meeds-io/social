/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.metadata.storage.MetadataStorage;
import org.exoplatform.social.metadata.MetadataInitPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;
import org.picocontainer.Startable;

public class MetadataServiceImpl implements MetadataService, Startable {

  private static final Log                LOG                 = ExoLogger.getLogger(MetadataServiceImpl.class);

  private MetadataStorage                 metadataStorage;

  private ListenerService                 listenerService;

  private Map<String, MetadataTypePlugin> metadataTypePlugins = new HashMap<>();

  private List<MetadataInitPlugin>        metadataPlugins     = new ArrayList<>();

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
    metadata.setProperties(metadata.getProperties());
    metadata = metadataStorage.createMetadata(metadata);
    try {
      this.listenerService.broadcast("social.metadata.created", userIdentityId, metadata);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata creation", e);
    }
    return metadata;
  }

  @Override
  public Metadata updateMetadata(Metadata metadata, long userIdentityId) {
    if (metadata == null) {
      throw new IllegalArgumentException("Metadata is mandatory");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }
    metadata = metadataStorage.updateMetadata(metadata);
    try {
      this.listenerService.broadcast("social.metadata.updated", userIdentityId, metadata);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata update", e);
    }
    return metadata;

  }

  @Override
  public Metadata deleteMetadataById(long metadataId) {
    if (metadataId <= 0) {
      throw new IllegalArgumentException("Metadata Technical Identifier is mandatory");
    }
    return this.metadataStorage.deleteMetadataById(metadataId);
  }

  @Override
  public int deleteMetadataBySpaceId(long spaceId) {
    if (spaceId <= 0) {
      throw new IllegalArgumentException("Space Technical Identifier is mandatory");
    }
    return this.metadataStorage.deleteMetadataItemsBySpaceId(spaceId);
  }

  @Override
  public int deleteMetadataBySpaceIdAndAudienceId(long spaceId, long audienceId) {
    if (spaceId <= 0) {
      throw new IllegalArgumentException("Space Technical Identifier is mandatory");
    }
    if (audienceId <= 0) {
      throw new IllegalArgumentException("Audience Identity Technical Identifier is mandatory");
    }
    return this.metadataStorage.deleteMetadataItemsBySpaceIdAndAudienceId(spaceId, audienceId);
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
  public MetadataItem createMetadataItem(MetadataObject metadataObject,
                                         MetadataKey metadataKey,
                                         long userIdentityId) throws ObjectAlreadyExistsException {
    return this.createMetadataItem(metadataObject, metadataKey, null, userIdentityId);
  }

  @Override
  public MetadataItem createMetadataItem(MetadataObject metadataObject,
                                         MetadataKey metadataKey,
                                         Map<String, String> properties,
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
                                                 properties);
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
  public MetadataItem deleteMetadataItem(long itemId, boolean broadcast) throws ObjectNotFoundException {
    if (itemId <= 0) {
      throw new IllegalArgumentException("Metadata Item Technical Identifier is mandatory");
    }
    MetadataItem metadataItem = this.metadataStorage.getMetadataItemById(itemId);
    if (metadataItem == null) {
      throw new ObjectNotFoundException("Metadata Item with identifier " + itemId + " wasn't found");
    }
    metadataItem = this.metadataStorage.deleteMetadataItemById(itemId);
    if (broadcast) {
      try {
        this.listenerService.broadcast("social.metadataItem.deleted", 0l, metadataItem);
      } catch (Exception e) {
        LOG.warn("Error while broadcasting event for metadata item deleted", e);
      }
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
      LOG.warn("Error while broadcasting event for metadata item deleted", e);
    }
    return metadataItem;
  }

  @Override
  public void deleteMetadataItemsByObject(MetadataObject object) {
    this.metadataStorage.deleteMetadataItemsByObject(object);
  }

  @Override
  public void deleteMetadataItemsByMetadataTypeAndObject(String metadataType, MetadataObject object) {
    this.metadataStorage.deleteMetadataItemsByMetadataTypeAndObject(metadataType, object);
  }

  @Override
  public void deleteMetadataItemsByParentObject(MetadataObject object) {
    this.metadataStorage.deleteMetadataItemsByParentObject(object);
  }

  @Override
  public List<MetadataItem> shareMetadataItemsByObject(MetadataObject sourceObject,
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
      LOG.warn("Error while broadcasting event for metadata item shared", e);
    }
    return sharedMetadataItems;
  }

  @Override
  public List<MetadataItem> getMetadataItemsByObject(MetadataObject object) {
    return this.metadataStorage.getMetadataItemsByObject(object);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataTypeAndObject(String metadataType, MetadataObject object) {
    return this.metadataStorage.getMetadataItemsByMetadataTypeAndObject(metadataType, object);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObject(String metadataName,
                                                                           String metadataTypeName,
                                                                           String objectType,
                                                                           long offset,
                                                                           long limit) {
    return this.metadataStorage.getMetadataItemsByMetadataNameAndTypeAndObject(metadataName,
                                                                               metadataTypeName,
                                                                               objectType,
                                                                               offset,
                                                                               limit);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(String metadataName,
                                                                                                  String metadataTypeName,
                                                                                                  String objectType,
                                                                                                  String propertyKey,
                                                                                                  String propertyValue,
                                                                                                  long offset,
                                                                                                  long limit) {
    return this.metadataStorage.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(metadataName,
                                                                                                      metadataTypeName,
                                                                                                      objectType,
                                                                                                      propertyKey,
                                                                                                      propertyValue,
                                                                                                      offset,
                                                                                                      limit);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataTypeAndCreator(String metadataTypeName,
                                                                     long creatorId,
                                                                     long offset,
                                                                     long limit) {
    MetadataType metadataType = getMetadataTypeByName(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataType + " is not registered as a plugin");
    }
    if (creatorId <= 0) {
      throw new IllegalArgumentException("creatorId is mandatory.");
    }
    return this.metadataStorage.getMetadataItemsByMetaDataTypeAndCreator(metadataType.getId(), creatorId, offset, limit);
  }

  public int countMetadataItemsByMetadataTypeAndCreator(String metadataTypeName, long creatorId) {
    MetadataType metadataType = getMetadataTypeByName(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataType + " is not registered as a plugin");
    }
    if (creatorId <= 0) {
      throw new IllegalArgumentException("creatorId is mandatory.");
    }
    return this.metadataStorage.countMetadataItemsByMetadataTypeAndCreator(metadataType.getId(), creatorId);
  }

  @Override
  public Set<String> getMetadataNamesByObject(MetadataObject object) {
    return this.metadataStorage.getMetadataNamesByObject(object);
  }

  @Override
  public List<String> findMetadataNamesByCreator(String term,
                                                 String metadataTypeName,
                                                 long creatorId,
                                                 long limit) {
    MetadataType metadataType = getMetadataTypeByName(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataTypeName + " is not registered as a plugin");
    }
    if (StringUtils.isBlank(term)) {
      return this.metadataStorage.getMetadataNamesByCreator(metadataType.getId(), creatorId, limit);
    } else {
      return this.metadataStorage.findMetadataNameByCreatorAndQuery(term, metadataType.getId(), creatorId, limit);
    }
  }

  @Override
  public List<String> findMetadataNamesByUserAndQuery(String term,
                                                      String metadataTypeName,
                                                      Set<Long> audienceIds,
                                                      long creatorId,
                                                      long limit) {
    MetadataType metadataType = getMetadataTypeByName(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataTypeName + " is not registered as a plugin");
    }
    if (StringUtils.isBlank(term)) {
      return this.metadataStorage.getMetadataNamesByUser(metadataType.getId(), creatorId, audienceIds, limit);
    } else {
      return this.metadataStorage.findMetadataNamesByUserAndQuery(term, metadataType.getId(), creatorId, audienceIds, limit);
    }
  }

  @Override
  public List<String> findMetadataNamesByAudiences(String term,
                                                   String metadataTypeName,
                                                   Set<Long> audienceIds,
                                                   long limit) {
    MetadataType metadataType = getMetadataTypeByName(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataTypeName + " is not registered as a plugin");
    }
    if (StringUtils.isBlank(term)) {
      return this.metadataStorage.getMetadataNamesByAudiences(metadataType.getId(), audienceIds, limit);
    } else {
      return this.metadataStorage.findMetadataNameByAudiencesAndQuery(term, metadataType.getId(), audienceIds, limit);
    }
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataAndObject(MetadataKey metadataKey, MetadataObject object) {
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
                           .anyMatch(registeredPlugin -> {
                             boolean sameIdWithdifferentName = !StringUtils.equals(registeredPlugin.getName(),
                                                                                   metadataTypePlugin.getName())
                                 && registeredPlugin.getId() == metadataTypePlugin.getId();
                             boolean sameNameWithDifferentId = StringUtils.equals(registeredPlugin.getName(),
                                                                                  metadataTypePlugin.getName())
                                 && registeredPlugin.getId() != metadataTypePlugin.getId();
                             return sameIdWithdifferentName || sameNameWithDifferentId;
                           })) {
      throw new UnsupportedOperationException("Overriding existing Metadata Type with different ID or Name is not allowed. Please verify the unicity of Metadata Type id and name.");
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

  @Override
  public void addMetadataPlugin(MetadataInitPlugin metadataInitPlugin) {
    this.metadataPlugins.add(metadataInitPlugin);
  }

  @Override
  public List<Metadata> getMetadatas(String metadataTypeName, long limit) {
    return metadataStorage.getMetadatas(metadataTypeName, limit);
  }

  @Override
  public List<Metadata> getMetadatasByProperty(String propertyKey, String propertyValue, long limit) {
    return metadataStorage.getMetadatasByProperty(propertyKey, propertyValue, limit);
  }

  @Override
  @ExoTransactional
  public void start() {
    this.metadataPlugins.forEach(plugin -> {
      Metadata metadata = plugin.getMetadata();
      try {
        MetadataKey metadataKey = new MetadataKey(metadata.getTypeName(), metadata.getName(), metadata.getAudienceId());
        Metadata storedMetadata = metadataStorage.getMetadataByKey(metadataKey);
        if (storedMetadata == null) {
          metadata.setId(0);
          metadata.setCreatorId(0);
          metadataStorage.createMetadata(metadata);
        }
      } catch (Exception e) {
        LOG.warn("Can't process initialization of metadata : " + metadata, e);
      }
    });
  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  private MetadataItem shareMetadataItem(MetadataObject metadataObject,
                                         MetadataKey metadataKey,
                                         String targetObjectId,
                                         long audienceId,
                                         long creatorId) {
    if (isShareable(metadataKey.getType())) {
      MetadataObject metadataObjectToShare = metadataObject.clone();
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
