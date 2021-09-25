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

  public Metadata getMetadata(String type, String name, long audienceId) {
    MetadataType metadataType = getMetadataType(type);
    if (metadataType == null) {
      throw new IllegalStateException("Metadata type with name " + type + " isn't defined");
    }
    MetadataEntity metadataEntity = this.metadataDAO.findMetadata(metadataType.getId(), name, audienceId);
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

  public int deleteMetadataItemsByObject(String objectType, String objectId) {
    return this.metadataItemDAO.deleteMetadataItemsByObject(objectType, objectId);
  }

  public List<MetadataItem> getMetadataItemsByObject(String objectType, String objectId) {
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadataItemsByObject(objectType, objectId);
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public List<MetadataItem> getMetadataItemsByMetadataAndObject(long metadataId, String objectType, String objectId) {
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadataItemsByMetadataAndObject(metadataId,
                                                                                                        objectType,
                                                                                                        objectId);
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public List<String> getMetadataObjectIds(String type,
                                           String metadataName,
                                           String objectType,
                                           long offset,
                                           long limit) {
    MetadataType metadataType = getMetadataType(type);
    if (metadataType == null) {
      throw new IllegalStateException("Metadata type with name " + metadataType + " isn't defined");
    }
    List<String> objectIds = metadataItemDAO.getMetadataObjectIds(metadataType.getId(),
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
    if (metadataItemEntity.getProperties() != null && !metadataItemEntity.getProperties().isEmpty()) {
      metadataItem.setProperties(new HashMap<>(metadataItemEntity.getProperties()));
    }
    MetadataEntity metadataEntity = metadataItemEntity.getMetadata();
    Metadata metadata = fromEntity(metadataEntity);
    metadataItem.setMetadata(metadata);
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
