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
package org.exoplatform.social.core.metadata.storage;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataItemDAO;
import org.exoplatform.social.core.jpa.storage.entity.MetadataEntity;
import org.exoplatform.social.core.jpa.storage.entity.MetadataItemEntity;
import org.exoplatform.social.metadata.model.*;

public class MetadataStorage {

  private static final Log   LOG           = ExoLogger.getLogger(MetadataStorage.class);

  private MetadataDAO        metadataDAO;

  private MetadataItemDAO    metadataItemDAO;

  private List<MetadataType> metadataTypes = new ArrayList<>();

  public MetadataStorage(MetadataDAO metadataDAO, MetadataItemDAO metadataItemDAO) {
    this.metadataDAO = metadataDAO;
    this.metadataItemDAO = metadataItemDAO;
  }

  public Metadata getMetadataByKey(MetadataKey metadataKey) {
    String type = metadataKey.getType();
    MetadataType metadataType = getMetadataType(type);
    if (metadataType == null) {
      throw new IllegalStateException("Metadata type with name " + type + " isn't defined");
    }
    MetadataEntity metadataEntity = this.metadataDAO.findMetadata(metadataType.getId(),
                                                                  metadataKey.getName(),
                                                                  metadataKey.getAudienceId());
    return fromEntity(metadataEntity);
  }

  public Metadata createMetadata(Metadata metadata) {
    MetadataEntity metadataEntity = toEntity(metadata);
    metadataEntity = this.metadataDAO.create(metadataEntity);
    return fromEntity(metadataEntity);
  }

  public Metadata deleteMetadataById(long id) {
    MetadataEntity metadataEntity = this.metadataDAO.find(id);
    if (metadataEntity != null) {
      this.metadataDAO.delete(metadataEntity);
    }
    return fromEntity(metadataEntity);
  }

  public MetadataItem createMetadataItem(MetadataItem metadataItem) {
    MetadataItemEntity metadataItemEntity = toEntity(metadataItem);
    metadataItemEntity = this.metadataItemDAO.create(metadataItemEntity);
    return fromEntity(metadataItemEntity);
  }

  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(String metadataName,
                                                                                                  String metadataTypeName,
                                                                                                  String objectType,
                                                                                                  String propertyKey,
                                                                                                  String propertyValue,
                                                                                                  long offset,
                                                                                                  long limit) {
    MetadataType metadataType = getMetadataType(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalStateException("Metadata type with name " + metadataType + " isn't defined");
    }
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(metadataName, metadataType.getId(), objectType, propertyKey, propertyValue, offset, limit);
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObject(String metadataName,
                                                                           String metadataTypeName,
                                                                           String objectType,
                                                                           long offset,
                                                                           long limit) {
    MetadataType metadataType = getMetadataType(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalStateException("Metadata type with name " + metadataType + " isn't defined");
    }
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadataItemsByMetadataNameAndTypeAndObject(metadataName, metadataType.getId(), objectType, offset, limit);
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public MetadataItem deleteMetadataItemById(long id) {
    MetadataItemEntity metadataItemEntity = this.metadataItemDAO.find(id);
    if (metadataItemEntity != null) {
      this.metadataItemDAO.deleteMetadataItemById(id);
    }
    return fromEntity(metadataItemEntity);
  }

  public MetadataItem getMetadataItemById(long itemId) {
    MetadataItemEntity metadataItemEntity = this.metadataItemDAO.find(itemId);
    return fromEntity(metadataItemEntity);
  }

  public int deleteMetadataItemsByObject(MetadataObject object) {
    return this.metadataItemDAO.deleteMetadataItemsByObject(object.getType(), object.getId());
  }

  public int deleteMetadataItemsByParentObject(MetadataObject object) {
    return this.metadataItemDAO.deleteMetadataItemsByParentObject(object.getType(), object.getParentId());
  }

  public void deleteMetadataItemsByMetadataTypeAndObject(String metadataTypeName, MetadataObject object) {
    List<MetadataItem> metadataItems = getMetadataItemsByMetadataTypeAndObject(metadataTypeName, object);
    for(MetadataItem metadataItem : metadataItems) {
      deleteMetadataItemById(metadataItem.getId());
    }
  }

  public List<MetadataItem> getMetadataItemsByObject(MetadataObject object) {
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadataItemsByObject(object.getType(), object.getId());
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public List<MetadataItem> getMetadataItemsByMetadataTypeAndObject(String metadataTypeName, MetadataObject object) {
    MetadataType metadataType = getMetadataType(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalStateException("Metadata type with name " + metadataType + " isn't defined");
    }
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadataItemsByMetadataTypeAndObject(metadataType.getId(), object.getType(), object.getId());
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public Set<String> getMetadataNamesByObject(MetadataObject object) {
    return metadataItemDAO.getMetadataNamesByObject(object.getType(), object.getId());
  }

  public List<String> getMetadataNamesByAudiences(long metadataTypeId, Set<Long> audienceIds, long limit) {
    return metadataDAO.getMetadataNamesByAudiences(metadataTypeId, audienceIds, limit);
  }

  public List<String> getMetadataNamesByCreator(long metadataTypeId, Long creatorId, long limit) {
    return metadataDAO.getMetadataNamesByCreator(metadataTypeId, creatorId, limit);
  }
  public List<String> getMetadataNamesByUser(long metadataTypeId, Long creatorId, Set<Long> audienceIds, long limit) {
    return metadataDAO.getMetadataNamesByUser(metadataTypeId, creatorId, audienceIds, limit);
  }

  public List<String> findMetadataNameByAudiencesAndQuery(String term, long metadataTypeId, Set<Long> audienceIds, long limit) {
    return metadataDAO.findMetadataNameByAudiencesAndQuery(term, metadataTypeId, audienceIds, limit);
  }

  public List<String> findMetadataNameByCreatorAndQuery(String term, long metadataTypeId, long creatorId, long limit) {
    return metadataDAO.findMetadataNameByCreatorAndQuery(term, metadataTypeId, creatorId, limit);
  }

  public List<String> findMetadataNamesByUserAndQuery(String term, long metadataTypeId, long creatorId, Set<Long> audienceIds, long limit) {
    return metadataDAO.findMetadataNamesByUserAndQuery(term, metadataTypeId, creatorId, audienceIds, limit);
  }

  public List<MetadataItem> getMetadataItemsByMetadataAndObject(long metadataId, MetadataObject object) {
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadataItemsByMetadataAndObject(metadataId,
                                                                                                        object.getType(),
                                                                                                        object.getId());
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public List<String> getMetadataObjectIds(String metadataType,
                                           String metadataName,
                                           String objectType,
                                           long offset,
                                           long limit) {
    MetadataType type = getMetadataType(metadataType);
    if (type == null) {
      throw new IllegalStateException("Metadata type with name " + type + " isn't defined");
    }
    List<String> objectIds = metadataItemDAO.getMetadataObjectIds(type.getId(),
                                                                  metadataName,
                                                                  objectType,
                                                                  offset,
                                                                  limit);
    if (CollectionUtils.isEmpty(objectIds)) {
      return Collections.emptyList();
    }
    return objectIds;
  }

  public void addMetadataType(MetadataType metadataType) {
    metadataTypes.add(metadataType);
  }

  public List<Metadata> getMetadatas(String metadataTypeName, long limit) {
    MetadataType metadataType = getMetadataType(metadataTypeName);
    if (metadataType == null) {
      throw new IllegalStateException("Metadata type with name " + metadataTypeName + " isn't defined");
    }
    List<MetadataEntity> metadatasEntities = metadataDAO.getMetadatas(metadataType.getId(), limit);
    return metadatasEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public List<Metadata> getMetadatasByProperty(String propertyKey, String propertyValue, long limit) {
    List<String> metadatasEntitiesIds = metadataDAO.getMetadatasByProperty(propertyKey, propertyValue, limit);
    List<MetadataEntity> metadatasEntities = new ArrayList<>();
    if (metadatasEntitiesIds != null && !metadatasEntitiesIds.isEmpty()) {
      for (String id : metadatasEntitiesIds) {
        MetadataEntity metadataEntity = this.metadataDAO.find(Long.parseLong(id));
        if (metadataEntity != null) {
          metadatasEntities.add(metadataEntity);
        }
      }
    }
    return metadatasEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public MetadataType getMetadataType(String name) {
    return metadataTypes.stream()
                        .filter(metadataType -> StringUtils.equals(metadataType.getName(), name))
                        .findFirst()
                        .orElse(null);
  }

  public List<MetadataType> getMetadataTypes() {
    return Collections.unmodifiableList(metadataTypes);
  }

  private Metadata fromEntity(MetadataEntity metadataEntity) {
    if (metadataEntity == null) {
      return null;
    }
    Metadata metadata = new Metadata();
    metadata.setId(metadataEntity.getId());
    metadata.setName(metadataEntity.getName());
    metadata.setAudienceId(metadataEntity.getAudienceId());
    metadata.setCreatorId(metadataEntity.getCreatorId());
    metadata.setCreatedDate(metadataEntity.getCreatedDate().getTime());
    MetadataType metadataType = metadataTypes.stream()
                                             .filter(registeredType -> registeredType.getId() == metadataEntity.getType())
                                             .findFirst()
                                             .orElse(null);
    if (metadataType != null) {
      metadata.setType(metadataType);
    } else {
      LOG.warn("No registered Metadata Type with identifier {}", metadataEntity.getType());
    }
    if (metadataEntity.getProperties() != null && !metadataEntity.getProperties().isEmpty()) {
      metadata.setProperties(new HashMap<>(metadataEntity.getProperties()));
    }
    return metadata;
  }

  private MetadataEntity toEntity(Metadata metadata) {
    MetadataEntity metadataEntity;
    if (metadata.getId() <= 0) {
      metadataEntity = new MetadataEntity();
      metadataEntity.setId(null);
      metadataEntity.setCreatedDate(new Date());
      metadataEntity.setCreatorId(metadata.getCreatorId());
      metadataEntity.setType(metadata.getType().getId());
    } else {
      metadataEntity = metadataDAO.find(metadata.getId());
      if (metadataEntity == null) {
        throw new IllegalStateException("Can't find Metadata with id " + metadata.getId());
      }
    }
    metadataEntity.setAudienceId(metadata.getAudienceId());
    metadataEntity.setName(StringUtils.isBlank(metadata.getName()) ? "" : metadata.getName());
    metadataEntity.setProperties(metadata.getProperties());
    return metadataEntity;
  }

  private MetadataItem fromEntity(MetadataItemEntity metadataItemEntity) {
    if (metadataItemEntity == null) {
      return null;
    }
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setId(metadataItemEntity.getId());
    metadataItem.setObjectId(metadataItemEntity.getObjectId());
    metadataItem.setObjectType(metadataItemEntity.getObjectType());
    metadataItem.setParentObjectId(metadataItemEntity.getParentObjectId());
    metadataItem.setCreatorId(metadataItemEntity.getCreatorId());
    metadataItem.setCreatedDate(metadataItemEntity.getCreatedDate().getTime());
    MetadataEntity metadataEntity = metadataItemEntity.getMetadata();
    Metadata metadata = fromEntity(metadataEntity);
    metadataItem.setMetadata(metadata);
    if (metadataItemEntity.getProperties() != null && !metadataItemEntity.getProperties().isEmpty()) {
      metadataItem.setProperties(new HashMap<>(metadataItemEntity.getProperties()));
    }
    return metadataItem;
  }

  private MetadataItemEntity toEntity(MetadataItem metadataItem) {
    MetadataItemEntity metadataItemEntity;
    if (metadataItem.getId() <= 0) {
      metadataItemEntity = new MetadataItemEntity();
      metadataItemEntity.setId(null);
      metadataItemEntity.setCreatedDate(new Date());
      metadataItemEntity.setCreatorId(metadataItem.getCreatorId());

      MetadataEntity metadataEntity = this.metadataDAO.find(metadataItem.getMetadata().getId());
      metadataItemEntity.setMetadata(metadataEntity);
    } else {
      metadataItemEntity = metadataItemDAO.find(metadataItem.getId());
      if (metadataItemEntity == null) {
        throw new IllegalStateException("Can't find Metadata item with id " + metadataItem.getId());
      }
    }

    metadataItemEntity.setObjectId(metadataItem.getObjectId());
    metadataItemEntity.setObjectType(metadataItem.getObjectType());
    metadataItemEntity.setParentObjectId(metadataItem.getParentObjectId());
    metadataItemEntity.setProperties(metadataItem.getProperties());
    return metadataItemEntity;
  }

}
