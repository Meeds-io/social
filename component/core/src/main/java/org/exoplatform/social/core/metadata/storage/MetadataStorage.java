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
    MetadataEntity metadataEntity = this.metadataDAO.findMetadata(type, name, audienceId);
    return fromEntity(metadataEntity);
  }

  public Metadata createMetadata(Metadata metadata) {
    MetadataEntity metadataEntity = toEntity(metadata);
    metadataEntity = this.metadataDAO.create(metadataEntity);
    return fromEntity(metadataEntity);
  }

  public MetadataItem createMetadataItem(MetadataItem metadataItem) {
    MetadataEntity metadataEntity = this.metadataDAO.find(metadataItem.getMetadata().getId());

    MetadataItemEntity metadataItemEntity = new MetadataItemEntity();
    metadataItemEntity.setMetadata(metadataEntity);
    metadataItemEntity.setId(metadataItem.getId() <= 0 ? null : metadataItem.getId());
    metadataItemEntity.setCreationDate(new Date());
    metadataItemEntity.setCreatorId(metadataItem.getCreatorId());
    metadataItemEntity.setObjectId(metadataItem.getObjectId());
    metadataItemEntity.setObjectType(metadataItem.getObjectType());
    metadataItemEntity.setParentObjectId(metadataItem.getParentObjectId());
    metadataItemEntity.setProperties(metadataItem.getProperties());
    return fromEntity(metadataItemEntity);
  }

  public List<MetadataItem> getMetadatasByObject(String objectType, String objectId) {
    List<MetadataItemEntity> metadataItemEntities = metadataItemDAO.getMetadatasByObject(objectType, objectId);
    if (CollectionUtils.isEmpty(metadataItemEntities)) {
      return Collections.emptyList();
    }
    return metadataItemEntities.stream().map(this::fromEntity).collect(Collectors.toList());
  }

  public void addMetadataType(MetadataType metadataType) {
    if (metadataTypes.stream()
                     .anyMatch(
                               registeredType -> StringUtils.equals(registeredType.getName(), metadataType.getName())
                                   || registeredType.getId() == metadataType.getId())) {
      throw new UnsupportedOperationException("Overriding existing Metadata Type by another one is not allowed");
    }
    metadataTypes.add(metadataType);
  }

  public List<MetadataType> getMetadataTypes() {
    return metadataTypes;
  }

  private MetadataItem fromEntity(MetadataItemEntity metadataItemEntity) {
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setId(metadataItemEntity.getId());
    metadataItem.setObjectId(metadataItemEntity.getObjectId());
    metadataItem.setObjectType(metadataItemEntity.getObjectType());
    metadataItem.setParentObjectId(metadataItemEntity.getParentObjectId());
    metadataItem.setCreationDate(metadataItemEntity.getCreationDate().getTime());
    if (metadataItemEntity.getProperties() != null && !metadataItemEntity.getProperties().isEmpty()) {
      metadataItem.setProperties(new HashMap<>(metadataItemEntity.getProperties()));
    }
    MetadataEntity metadataEntity = metadataItemEntity.getMetadata();
    Metadata metadata = fromEntity(metadataEntity);
    metadataItem.setMetadata(metadata);
    return metadataItem;
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
    MetadataEntity metadataEntity = new MetadataEntity();
    metadataEntity.setAudienceId(metadata.getAudienceId());
    metadataEntity.setCreatorId(metadata.getCreatorId());
    metadataEntity.setName(metadata.getName());
    metadataEntity.setType(metadata.getType().getId());
    metadataEntity.setId(metadata.getId() <= 0 ? null : metadata.getId());
    return metadataEntity;
  }

}
