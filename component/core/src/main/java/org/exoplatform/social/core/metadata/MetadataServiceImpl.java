/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.metadata.storage.MetadataStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.metadata.MetadataFilter;
import org.exoplatform.social.metadata.MetadataInitPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

import lombok.Synchronized;

@SuppressWarnings("removal")
public class MetadataServiceImpl implements MetadataService, Startable {

  private static final String             METADATA_TYPE_IS_MANDATORY_MESSAGE = "Metadata Type is mandatory";

  private static final Log                LOG                                = ExoLogger.getLogger(MetadataServiceImpl.class);

  private UserACL                         userAcl;

  private IdentityStorage                 identityStorage;

  private MetadataStorage                 metadataStorage;

  private ListenerService                 listenerService;

  private Map<String, MetadataTypePlugin> metadataTypePlugins                = new HashMap<>();

  private List<MetadataInitPlugin>        metadataPlugins                    = new ArrayList<>();

  private long                            superUserIdentityId;

  public MetadataServiceImpl(UserACL userAcl,
                             IdentityStorage identityStorage,
                             MetadataStorage metadataStorage,
                             ListenerService listenerService) {
    this.userAcl = userAcl;
    this.identityStorage = identityStorage;
    this.metadataStorage = metadataStorage;
    this.listenerService = listenerService;
  }

  @Override
  public Metadata createMetadata(Metadata metadata, long userIdentityId) {
    validateMetadata(metadata);
    validateAndGetMetadataType(metadata.getType().getName());
    validateUserIdentityId(userIdentityId);

    metadata.setCreatorId(userIdentityId);
    return createMetadataAndBroadcast(metadata, userIdentityId);
  }

  @Override
  public Metadata createMetadata(Metadata metadata) {
    return createMetadata(metadata, getSuperUserIdentityId());
  }

  @Override
  public Metadata updateMetadata(Metadata metadata, long userIdentityId) {
    validateMetadata(metadata);
    validateUserIdentityId(userIdentityId);
    metadata = metadataStorage.updateMetadata(metadata);
    this.listenerService.broadcast("social.metadata.updated", userIdentityId, metadata);
    return metadata;

  }

  @Override
  public Metadata updateMetadata(Metadata metadata) {
    return updateMetadata(metadata, getSuperUserIdentityId());
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
    validateSpaceId(spaceId);
    return this.metadataStorage.deleteMetadataItemsBySpaceId(spaceId);
  }

  @Override
  public int deleteMetadataBySpaceIdAndAudienceId(long spaceId, long audienceId) {
    validateSpaceId(spaceId);
    validateUserIdentityId(audienceId);
    return this.metadataStorage.deleteMetadataItemsBySpaceIdAndAudienceId(spaceId, audienceId);
  }

  @Override
  public Metadata getMetadataByKey(MetadataKey metadataKey) {
    if (metadataKey == null) {
      throw new IllegalArgumentException("Metadata Key is mandatory");
    }
    if (StringUtils.isBlank(metadataKey.getType())) {
      throw new IllegalArgumentException(METADATA_TYPE_IS_MANDATORY_MESSAGE);
    }
    if (StringUtils.isBlank(metadataKey.getName())) {
      throw new IllegalArgumentException("Metadata Name is mandatory");
    }
    return metadataStorage.getMetadataByKey(metadataKey);
  }

  @Override
  public Metadata getMetadataById(long id) {
    return metadataStorage.getMetadataById(id);
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
                                         long userIdentityId,
                                         boolean broadcast) throws ObjectAlreadyExistsException {
    validateUserIdentityId(userIdentityId);
    Metadata metadata = checkAndCreateMetadata(metadataObject, metadataKey, userIdentityId);
    MetadataItem metadataItem = new MetadataItem(0,
                                                 metadata,
                                                 metadataObject,
                                                 userIdentityId,
                                                 System.currentTimeMillis(),
                                                 properties);
    return createMetadataItem(metadataItem, userIdentityId, broadcast);
  }

  @Override
  public MetadataItem createMetadataItem(MetadataObject metadataObject,
                                         MetadataKey metadataKey,
                                         Map<String, String> properties,
                                         long userIdentityId) throws ObjectAlreadyExistsException {
    return createMetadataItem(metadataObject, metadataKey, properties, userIdentityId, true);
  }

  @Override
  public MetadataItem createMetadataItem(MetadataObject metadataObject,
                                         MetadataKey metadataKey,
                                         Map<String, String> properties,
                                         boolean broadcast) throws ObjectAlreadyExistsException {
    Metadata metadata = checkAndCreateMetadata(metadataObject, metadataKey, 0);
    MetadataItem metadataItem = new MetadataItem(0,
                                                 metadata,
                                                 metadataObject,
                                                 0,
                                                 System.currentTimeMillis(),
                                                 properties);
    return createMetadataItem(metadataItem, 0, broadcast);
  }

  @Override
  public MetadataItem createMetadataItem(MetadataObject metadataObject,
                                         MetadataKey metadataKey,
                                         Map<String, String> properties) throws ObjectAlreadyExistsException {
    return createMetadataItem(metadataObject, metadataKey, properties, true);
  }

  @Override
  public MetadataItem updateMetadataItem(MetadataItem metadataItem, long userIdentityId, boolean broadcast) {
    if (metadataItem == null) {
      throw new IllegalArgumentException("MetadataItem is mandatory");
    }
    validateMetadataItemId(metadataItem.getId());
    validateUserIdentityId(userIdentityId);
    metadataItem = metadataStorage.updateMetadataItem(metadataItem);
    if (broadcast) {
      this.listenerService.broadcast("social.metadataItem.updated", userIdentityId, metadataItem);
    }
    return metadataItem;
  }

  @Override
  public MetadataItem updateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
    return updateMetadataItem(metadataItem, userIdentityId, true);
  }

  @Override
  public MetadataItem deleteMetadataItem(long itemId, boolean broadcast) throws ObjectNotFoundException {
    validateMetadataItemId(itemId);
    MetadataItem metadataItem = this.metadataStorage.getMetadataItemById(itemId);
    if (metadataItem == null) {
      throw new ObjectNotFoundException("Metadata Item with identifier " + itemId + " wasn't found");
    }
    metadataItem = this.metadataStorage.deleteMetadataItemById(itemId);
    if (broadcast) {
      this.listenerService.broadcast("social.metadataItem.deleted", 0l, metadataItem);
    }
    return metadataItem;
  }

  @Override
  public MetadataItem deleteMetadataItem(long itemId, long userIdentityId) throws ObjectNotFoundException {
    validateMetadataItemId(itemId);
    validateUserIdentityId(userIdentityId);

    MetadataItem metadataItem = this.metadataStorage.getMetadataItemById(itemId);
    if (metadataItem == null) {
      throw new ObjectNotFoundException("Metadata Item with identifier " + itemId + " wasn't found");
    }
    metadataItem = this.metadataStorage.deleteMetadataItemById(itemId);
    broadcastDeleted(metadataItem, userIdentityId);
    return metadataItem;
  }

  @Override
  public void deleteMetadataItemsByObject(MetadataObject object) {
    List<MetadataItem> metadataItems = this.metadataStorage.getMetadataItemsByObject(object);
    this.metadataStorage.deleteMetadataItemsByObject(object);
    if (CollectionUtils.isNotEmpty(metadataItems)) {
      for (MetadataItem metadataItem : metadataItems) {
        broadcastDeleted(metadataItem, 0l);
      }
    }
  }

  @Override
  public void deleteMetadataItemsByMetadata(String metadataTypeName, String metadataName) {
    this.metadataStorage.deleteMetadataItemsByMetadata(getMetadataTypeByName(metadataTypeName).getId(),
                                                       metadataName);
  }

  @Override
  public void deleteByMetadataTypeAndSpaceIdAndCreatorId(String metadataTypeName, long spaceId, long userIdentityId) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    validateSpaceId(spaceId);
    validateUserIdentityId(userIdentityId);
    List<MetadataItem> deletedMetadataItems =
                                            this.metadataStorage.deleteByMetadataTypeAndSpaceIdAndCreatorId(metadataType.getId(),
                                                                                                            spaceId,
                                                                                                            userIdentityId);
    if (CollectionUtils.isNotEmpty(deletedMetadataItems)) {
      for (MetadataItem metadataItem : deletedMetadataItems) {
        broadcastDeleted(metadataItem, userIdentityId);
      }
    }
  }

  @Override
  public void deleteByMetadataTypeAndCreatorId(String metadataTypeName, long userIdentityId) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    validateUserIdentityId(userIdentityId);
    List<MetadataItem> deletedMetadataItems = this.metadataStorage.deleteByMetadataTypeAndCreatorId(metadataType.getId(),
                                                                                                    userIdentityId);
    if (CollectionUtils.isNotEmpty(deletedMetadataItems)) {
      for (MetadataItem metadataItem : deletedMetadataItems) {
        broadcastDeleted(metadataItem, userIdentityId);
      }
    }
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
  public int deleteByMetadataItemsTypeAndUntilCreationDate(String metadataTypeName, long untilDate) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    return this.metadataStorage.deleteByMetadataItemsTypeAndUntilCreationDate(metadataType.getId(), untilDate);
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
    this.listenerService.broadcast("social.metadataItem.shared", sourceObject, targetObjectId);
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
  public List<MetadataItem> getMetadataItemsByMetadataTypeAndObjectType(String metadataType, String objectType) {
    return this.metadataStorage.getMetadataItemsByMetadataTypeAndObjectType(metadataType, objectType);
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
  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObject(String metadataName,
                                                                           String metadataTypeName,
                                                                           String objectType,
                                                                           String objectId,
                                                                           long offset,
                                                                           long limit) {
    return this.metadataStorage.getMetadataItemsByMetadataNameAndTypeAndObject(metadataName,
                                                                               metadataTypeName,
                                                                               objectType,
                                                                               objectId,
                                                                               offset,
                                                                               limit);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObjectAndSpaceId(String metadataName,
                                                                                     String metadataTypeName,
                                                                                     String objectType,
                                                                                     long spaceId,
                                                                                     long offset,
                                                                                     long limit) {
    return this.metadataStorage.getMetadataItemsByMetadataNameAndTypeAndObjectAndSpaceIds(metadataName,
                                                                                          metadataTypeName,
                                                                                          objectType,
                                                                                          Collections.singletonList(spaceId),
                                                                                          offset,
                                                                                          limit);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndSpaceIds(String metadataName,
                                                                             String metadataTypeName,
                                                                             List<Long> spaceIds,
                                                                             long offset,
                                                                             long limit) {
    return this.metadataStorage.getMetadataItemsByMetadataNameAndTypeAndSpaceIds(metadataName,
                                                                                 metadataTypeName,
                                                                                 spaceIds,
                                                                                 offset,
                                                                                 limit);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByFilter(MetadataFilter filter, long offset, long limit) {
    return metadataStorage.getMetadataItemsByFilter(filter, offset, limit);
  }

  @Override
  public Long countMetadataItemsByFilter(MetadataFilter filter) {
    return metadataStorage.countMetadataItemsByFilter(filter);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObjectAndSpaceIds(String metadataName,
                                                                                      String metadataTypeName,
                                                                                      String objectType,
                                                                                      List<Long> spaceIds,
                                                                                      long offset,
                                                                                      long limit) {
    return this.metadataStorage.getMetadataItemsByMetadataNameAndTypeAndObjectAndSpaceIds(metadataName,
                                                                                          metadataTypeName,
                                                                                          objectType,
                                                                                          spaceIds,
                                                                                          offset,
                                                                                          limit);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadata(MetadataKey metadataKey, int offset, int limit) {
    return this.metadataStorage.getMetadataItemsByMetadata(metadataKey, offset, limit);
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
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    validateUserIdentityId(creatorId);
    return this.metadataStorage.getMetadataItemsByMetaDataTypeAndCreator(metadataType.getId(), creatorId, offset, limit);
  }

  public int countMetadataItemsByMetadataTypeAndCreator(String metadataTypeName, long creatorId) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    validateUserIdentityId(creatorId);
    return this.metadataStorage.countMetadataItemsByMetadataTypeAndCreator(metadataType.getId(), creatorId);
  }

  public int countMetadataItemsByMetadataTypeAndObjectTypeAndCreator(String metadataTypeName, String objectType, long creatorId) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    validateUserIdentityId(creatorId);
    return this.metadataStorage.countMetadataItemsByMetadataTypeAndObjectTypeAndCreator(metadataType.getId(),
                                                                                        objectType,
                                                                                        creatorId);
  }

  public Map<String, Long> countMetadataItemsByMetadataTypeAndAudienceId(String metadataTypeName, long creatorId, long spaceId) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    validateUserIdentityId(creatorId);
    return this.metadataStorage.countMetadataItemsByMetadataTypeAndAudienceId(metadataType.getId(), creatorId, spaceId);
  }

  public Map<Long, Long> countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(String metadataTypeName,
                                                                                 long creatorId,
                                                                                 List<Long> spacesId) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    return this.metadataStorage.countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(metadataType.getId(),
                                                                                        creatorId,
                                                                                        spacesId);
  }

  @Override
  public Set<String> getMetadataNamesByObject(MetadataObject object) {
    return this.metadataStorage.getMetadataNamesByObject(object);
  }

  @Override
  public List<String> getMetadataNamesByMetadataTypeAndObject(String metadataTypeName, String objectType, String objectId) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    List<MetadataItem> metadataItems = this.metadataStorage.getMetadataItemsByMetadataTypeAndObject(metadataType.getId(),
                                                                                                    new MetadataObject(objectType,
                                                                                                                       objectId));
    return metadataItems.stream()
                        .map(MetadataItem::getMetadata)
                        .map(Metadata::getName)
                        .distinct()
                        .toList();
  }

  @Override
  public List<String> getMetadataNamesByMetadataTypeAndObject(String metadataTypeName,
                                                              String objectType,
                                                              String objectId,
                                                              int offset,
                                                              int limit) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
    List<MetadataItem> metadataItems = this.metadataStorage.getMetadataItemsByMetadataTypeAndObject(metadataType.getId(),
                                                                                                    new MetadataObject(objectType,
                                                                                                                       objectId),
                                                                                                    offset,
                                                                                                    limit);
    return metadataItems.stream().map(MetadataItem::getMetadata).map(Metadata::getName).distinct().toList();
  }

  @Override
  public List<String> findMetadataNamesByCreator(String term,
                                                 String metadataTypeName,
                                                 long creatorId,
                                                 long limit) {
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
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
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
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
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);
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
  public List<MetadataItem> getMetadataItemsByMetadataIdAndObjectTypes(long metadataId,
                                                                       List<String> objectTypes,
                                                                       int offset,
                                                                       int limit) {
    return this.metadataStorage.getMetadataItemsByMetadataIdAndObjectTypes(metadataId, objectTypes, offset, limit);
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
  public List<Long> getMetadataIds(String metadataTypeName, int offset, int limit) {
    return metadataStorage.getMetadataIds(metadataTypeName, offset, limit);
  }

  @Override
  public List<Metadata> getMetadatasByProperty(String propertyKey, String propertyValue, long limit) {
    List<Long> ids = metadataStorage.getMetadataIdsByProperty(propertyKey, propertyValue, 0l, limit, true);
    return ids.stream().map(this::getMetadataById).toList();
  }

  @Override
  public long countMetadataIdsByProperty(String propertyKey, String propertyValue) {
    return metadataStorage.countMetadataIdsByProperty(propertyKey, propertyValue);
  }

  @Override
  public List<Long> getMetadataIdsByProperty(String propertyKey, String propertyValue, long offset, long limit) {
    return metadataStorage.getMetadataIdsByProperty(propertyKey, propertyValue, offset, limit, false);
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

  private Metadata checkAndCreateMetadata(MetadataObject metadataObject,
                                          MetadataKey metadataKey,
                                          long userIdentityId) throws ObjectAlreadyExistsException {
    if (metadataObject == null) {
      throw new IllegalArgumentException("Metadata Item Object is mandatory");
    }
    if (StringUtils.isBlank(metadataObject.getType())) {
      throw new IllegalArgumentException("Metadata Item Object Type is mandatory");
    }
    if (StringUtils.isBlank(metadataObject.getId())) {
      throw new IllegalArgumentException("Metadata Item Object Id is mandatory");
    }
    if (metadataKey == null) {
      throw new IllegalArgumentException("Metadata Key is mandatory");
    }
    if (StringUtils.isBlank(metadataKey.getName())) {
      throw new IllegalArgumentException("Metadata Name is mandatory");
    }
    String metadataTypeName = metadataKey.getType();
    MetadataType metadataType = validateAndGetMetadataType(metadataTypeName);

    Metadata metadata = getMetadataByKey(metadataKey);
    if (metadata == null) {
      metadata = new Metadata();
      metadata.setName(metadataKey.getName());
      metadata.setType(metadataType);
      metadata.setAudienceId(metadataKey.getAudienceId());
      metadata.setCreatorId(userIdentityId);
      metadata = createMetadataAndBroadcast(metadata, userIdentityId);
    }
    if (!isAllowMultipleItemsPerObject(metadata.getTypeName())) {
      List<MetadataItem> storedMetadataItems = metadataStorage.getMetadataItemsByMetadataAndObject(metadata.getId(),
                                                                                                   metadataObject);
      if (!storedMetadataItems.isEmpty()) {
        throw new ObjectAlreadyExistsException(storedMetadataItems.get(0));
      }
    }
    return metadata;
  }

  @Synchronized
  private Metadata createMetadataAndBroadcast(Metadata metadata, long userIdentityId) {
    // Need to commit previous changes in order to not rollback the entire
    // transaction if an error happens
    RequestLifeCycle.restartTransaction();
    MetadataKey metadataKey = new MetadataKey(metadata.getType().getName(),
                                              metadata.getName(),
                                              metadata.getAudienceId());
    Metadata existingMetadata = metadataStorage.getMetadataByKey(metadataKey);
    if (existingMetadata != null) {
      return existingMetadata;
    }
    try {
      Metadata createdMetadata = metadataStorage.createMetadata(metadata);
      // Need to commit changes in order to propagate changes through JPA
      // Sessions
      RequestLifeCycle.restartTransaction();
      this.listenerService.broadcast("social.metadata.created", userIdentityId, createdMetadata);
      return createdMetadata;
    } catch (Exception e) {
      // Need to restart a clean transaction knowing that the previous one will
      // be roolbacked
      RequestLifeCycle.restartTransaction();
      Metadata createdMetadata = metadataStorage.getMetadataByKey(metadataKey);
      if (createdMetadata != null) {
        return createdMetadata;
      } else {
        throw e;
      }
    }
  }

  private MetadataItem createMetadataItem(MetadataItem metadataItem, long userIdentityId, boolean broadcast) {
    metadataItem = metadataStorage.createMetadataItem(metadataItem);
    if (broadcast) {
      this.listenerService.broadcast("social.metadataItem.created", userIdentityId, metadataItem);
    }
    return metadataItem;
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
        LOG.warn("The metadata object {} is already associated to Metadata with unique key {}." +
            " This doesn't affect the expected result, so continue processing.",
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

  private void validateMetadata(Metadata metadata) {
    if (metadata == null) {
      throw new IllegalArgumentException("Metadata is mandatory");
    }
    if (metadata.getType() == null) {
      throw new IllegalArgumentException(METADATA_TYPE_IS_MANDATORY_MESSAGE);
    }
  }

  private void validateMetadataItemId(long itemId) {
    if (itemId <= 0) {
      throw new IllegalArgumentException("Metadata Item Technical Identifier is mandatory");
    }
  }

  private void validateUserIdentityId(long userIdentityId) {
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }
  }

  private void validateSpaceId(long spaceId) {
    if (spaceId <= 0) {
      throw new IllegalArgumentException("spaceId is mandatory");
    }
  }

  private MetadataType validateAndGetMetadataType(String metadataTypeName) {
    MetadataType type = getMetadataTypeByName(metadataTypeName);
    if (type == null) {
      throw new IllegalArgumentException("Metadata Type " + metadataTypeName + " is not registered as a plugin");
    }
    return type;
  }

  private void broadcastDeleted(MetadataItem metadataItem, long userIdentityId) {
    this.listenerService.broadcast("social.metadataItem.deleted", userIdentityId, metadataItem);
  }

  private long getSuperUserIdentityId() {
    if (superUserIdentityId == 0) {
      superUserIdentityId = Long.parseLong(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userAcl.getSuperUser())
                                                          .getId());
    }
    return superUserIdentityId;
  }

}
