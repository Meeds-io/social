package org.exoplatform.social.core.metadata;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

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
    MetadataType type = getMetadataTypeByName(metadata.getType().getName());
    if (type == null) {
      throw new IllegalArgumentException("Metadata Type is not registered as a plugin");
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
  public Metadata deleteMetadata(String type, String name, long audienceId, long userIdentityId) throws ObjectNotFoundException {
    if (type == null) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    if (getMetadataTypeByName(type) == null) {
      throw new IllegalArgumentException("Metadata Type is not registered as a plugin");
    }
    Metadata metadata = metadataStorage.getMetadata(type, name, audienceId);
    if (metadata == null) {
      throw new ObjectNotFoundException("Metadata with type=" + type + ",name=" + name + ",audienceId=" + audienceId
          + " not found");
    }

    try {
      this.listenerService.broadcast("social.metadata.deleted", userIdentityId, metadata);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata creation", e);
    }
    return metadataStorage.deleteMetadataById(metadata.getId());
  }

  @Override
  public Metadata getMetadataByTypeAndNameAndAudience(String metadataType, String metadataName, long audienceId) {
    if (StringUtils.isBlank(metadataType)) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    return metadataStorage.getMetadata(metadataType, metadataName, audienceId);
  }

  @Override
  public MetadataItem createMetadataItem(MetadataItem metadataItem,
                                         String type,
                                         String name,
                                         long audienceId,
                                         long userIdentityId,
                                         boolean allowMultipleItemsPerObject) throws ObjectAlreadyExistsException {
    if (metadataItem == null) {
      throw new IllegalArgumentException("Metadata Item is mandatory");
    }
    if (StringUtils.isBlank(metadataItem.getObjectType())) {
      throw new IllegalArgumentException("Metadata Item Object Type is mandatory");
    }
    if (StringUtils.isBlank(metadataItem.getObjectId())) {
      throw new IllegalArgumentException("Metadata Item Object Id is mandatory");
    }
    if (StringUtils.isBlank(type)) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    if (getMetadataTypeByName(type) == null) {
      throw new IllegalArgumentException("Metadata Type is not registered as a plugin");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }

    Metadata metadata = getMetadataByTypeAndNameAndAudience(type, name, audienceId);
    if (metadata == null) {
      metadata = new Metadata();
      metadata.setName(name);
      metadata.setType(getMetadataTypeByName(type));
      metadata.setAudienceId(audienceId);
      metadata = createMetadata(metadata, userIdentityId);
    }
    metadataItem.setMetadata(metadata);
    metadataItem.setCreatorId(userIdentityId);
    metadataItem.setCreatedDate(System.currentTimeMillis());
    if (!allowMultipleItemsPerObject) {
      List<MetadataItem> storedMetadataItems = metadataStorage.getMetadataItemsByMetadataAndObject(metadata.getId(),
                                                                                                   metadataItem.getObjectType(),
                                                                                                   metadataItem.getObjectId());
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
  public void deleteMetadataItemsByObject(String objectType, String objectId) {
    this.metadataStorage.deleteMetadataItemsByObject(objectType, objectId);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByObject(String objectType, String objectId) {
    return this.metadataStorage.getMetadataItemsByObject(objectType, objectId);
  }

  @Override
  public List<MetadataItem> getMetadataItemsByMetadataAndObject(long metadataId, String objectType, String objectId) {
    return this.metadataStorage.getMetadataItemsByMetadataAndObject(metadataId, objectType, objectId);
  }

  @Override
  public List<String> getMetadataObjectIds(String metadataType, String metadataName, String objectType, int limit) {
    return this.metadataStorage.getMetadataObjectIds(metadataType, metadataName, objectType, limit);
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
}
