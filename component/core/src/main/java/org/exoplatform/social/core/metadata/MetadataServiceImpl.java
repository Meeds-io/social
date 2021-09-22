package org.exoplatform.social.core.metadata;

import java.util.ArrayList;
import java.util.List;

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

  private static final Log         LOG                 = ExoLogger.getLogger(MetadataServiceImpl.class);

  private MetadataStorage          metadataStorage;

  private ListenerService          listenerService;

  private List<MetadataTypePlugin> metadataTypePlugins = new ArrayList<>();

  public MetadataServiceImpl(MetadataStorage metadataStorage, ListenerService listenerService) {
    this.metadataStorage = metadataStorage;
    this.listenerService = listenerService;
  }

  @Override
  public Metadata createMetadata(Metadata metadata, long userIdentityId) throws IllegalAccessException {
    if (metadata == null) {
      throw new IllegalArgumentException("Metadata is mandatory");
    }
    MetadataType type = metadata.getType();
    if (type == null) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }

    MetadataTypePlugin metadataTypePlugin = getMetadataTypePluginByName(metadata.getType().getName());
    if (metadataTypePlugin == null) {
      throw new IllegalArgumentException("Metadata Type " + type + " is not a registered plugin");
    }

    metadata.setType(metadataTypePlugin.getMetadataType());
    metadata.setCreatorId(userIdentityId);

    if (!metadataTypePlugin.canCreateMetadata(metadata, userIdentityId)) {
      throw new IllegalAccessException("User with identifier '" + userIdentityId + "' is not allowed to create Metadata "
          + metadata);
    }
    metadata = metadataStorage.createMetadata(metadata);
    try {
      this.listenerService.broadcast("social.metadata.created", userIdentityId, metadata);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event for metadata creation", e);
    }
    return metadata;
  }

  @Override
  public Metadata deleteMetadata(String type, String name, long audienceId, long userIdentityId) throws ObjectNotFoundException,
                                                                                                 IllegalAccessException {
    if (type == null) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }
    MetadataTypePlugin metadataTypePlugin = getMetadataTypePluginByName(type);
    if (metadataTypePlugin == null) {
      throw new IllegalArgumentException("Metadata Type " + type + " is not a registered plugin");
    }

    Metadata metadata = metadataStorage.getMetadata(type, name, audienceId);
    if (metadata == null) {
      throw new ObjectNotFoundException("Metadata with type=" + type + ",name=" + name + ",audienceId=" + audienceId
          + " not found");
    }

    if (!metadataTypePlugin.canDeleteMetadata(metadata, userIdentityId)) {
      throw new IllegalAccessException("User with identifier '" + userIdentityId + "' is not allowed to create Metadata "
          + metadata);
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
                                         long userIdentityId) throws IllegalAccessException, ObjectAlreadyExistsException {
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
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }

    MetadataTypePlugin metadataTypePlugin = getMetadataTypePluginByName(type);
    if (metadataTypePlugin == null) {
      throw new IllegalArgumentException("Metadata Type " + type + " is not a registered plugin");
    }

    Metadata metadata = getMetadataByTypeAndNameAndAudience(type, name, audienceId);
    if (metadata == null) {
      metadata = new Metadata();
      metadata.setName(name);
      metadata.setType(metadataTypePlugin.getMetadataType());
      metadata.setAudienceId(audienceId);
      metadata = createMetadata(metadata, userIdentityId);
    }
    metadataItem.setMetadata(metadata);
    metadataItem.setCreatorId(userIdentityId);
    metadataItem.setCreatedDate(System.currentTimeMillis());
    if (!metadataTypePlugin.canCreateMetadataItem(metadataItem, userIdentityId)) {
      throw new IllegalAccessException("User with identifier '" + userIdentityId + "' is not allowed to create Metadata Item "
          + metadataItem);
    }
    if (!metadataTypePlugin.allowMultipleItemsPerObject()) {
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
  public MetadataItem deleteMetadataItem(String type,
                                         String name,
                                         long itemId,
                                         long userIdentityId) throws IllegalAccessException, ObjectNotFoundException {
    if (StringUtils.isBlank(type)) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("Metadata Name is mandatory");
    }
    if (itemId <= 0) {
      throw new IllegalArgumentException("Metadata Item Technical Identifier is mandatory");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }

    MetadataTypePlugin metadataTypePlugin = getMetadataTypePluginByName(type);
    if (metadataTypePlugin == null) {
      throw new IllegalArgumentException("Metadata Type " + type + " is not a registered plugin");
    }

    MetadataItem metadataItem = this.metadataStorage.getMetadataItemById(itemId);
    if (metadataItem == null) {
      throw new ObjectNotFoundException("Metadata Item with identifier " + itemId + " wasn't found");
    }

    String metadataTypeName = metadataItem.getMetadata().getType().getName();
    if (!StringUtils.equals(metadataTypeName, type)) {
      throw new IllegalStateException("Designated Metadata Item to delete doesn't have the same Type '" + metadataTypeName
          + "' as the one designated: " + type);
    }
    String metadataName = metadataItem.getMetadata().getName();
    if (!StringUtils.equals(metadataName, name)) {
      throw new IllegalStateException("Designated Metadata Item to delete doesn't have the same Name '" + metadataName
          + "' as the one designated: " + name);
    }

    if (!metadataTypePlugin.canDeleteMetadataItem(metadataItem, userIdentityId)) {
      throw new IllegalAccessException("User with identifier '" + userIdentityId + "' is not allowed to delete Metadata Item "
          + metadataItem);
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
  public void addMetadataTypePlugin(MetadataTypePlugin metadataTypePlugin) {
    if (metadataTypePlugins.stream()
                           .anyMatch(registeredPlugin -> StringUtils.equals(registeredPlugin.getName(),
                                                                            metadataTypePlugin.getName())
                               || registeredPlugin.getId() == metadataTypePlugin.getId())) {
      throw new UnsupportedOperationException("Overriding existing Metadata Type is not allowed. Please verify the unicity of Metadata Type id and name.");
    }
    this.metadataStorage.addMetadataType(metadataTypePlugin.getMetadataType());
    this.metadataTypePlugins.add(metadataTypePlugin);
  }

  @Override
  public MetadataTypePlugin getMetadataTypePluginByName(String name) {
    return metadataTypePlugins.stream()
                              .filter(metadataTypePlugin -> StringUtils.equals(metadataTypePlugin.getName(),
                                                                               name))
                              .findFirst()
                              .orElse(null);
  }

  @Override
  public List<MetadataType> getMetadataTypes() {
    return this.metadataStorage.getMetadataTypes();
  }
}
