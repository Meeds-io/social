package org.exoplatform.social.core.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.metadata.storage.MetadataStorage;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.*;

public class MetadataServiceImpl implements MetadataService {

  private MetadataStorage          metadataStorage;

  private List<MetadataTypePlugin> metadataTypePlugins = new ArrayList<>();

  public MetadataServiceImpl(MetadataStorage metadataStorage) {
    this.metadataStorage = metadataStorage;
  }

  @Override
  public MetadataItem createMetadataItem(MetadataItem metadataItem,
                                         String type,
                                         String name,
                                         long audienceId,
                                         long userIdentityId) throws IllegalAccessException {

    if (StringUtils.isBlank(type)) {
      throw new IllegalArgumentException("Metadata Type is mandatory");
    }

    MetadataTypePlugin metadataTypePlugin = getMetadataTypePluginByName(type);
    if (metadataTypePlugin == null) {
      throw new IllegalArgumentException("Metadata Type " + type + " is not a registered plugin");
    }

    Metadata metadata = metadataStorage.getMetadata(type, name, audienceId);
    if (metadata == null) {
      metadata = new Metadata();
      metadata.setName(name);
      metadata.setType(metadataTypePlugin.getMetadataType());
      metadata.setCreatorId(userIdentityId);
      metadata.setAudienceId(audienceId);
      metadata = metadataStorage.createMetadata(metadata);
    }
    metadataItem.setMetadata(metadata);
    metadataItem.setCreatorId(userIdentityId);
    metadataItem.setCreationDate(System.currentTimeMillis());
    metadataItem = metadataStorage.createMetadataItem(metadataItem);
    return metadataItem;
  }

  @Override
  public List<MetadataItem> getMetadatasByObject(String objectType, String objectId) {
    return this.metadataStorage.getMetadatasByObject(objectType, objectId);
  }

  @Override
  public void addMetadataTypePlugin(MetadataTypePlugin metadataTypePlugin) {
    this.metadataStorage.addMetadataType(metadataTypePlugin.getMetadataType());
    this.metadataTypePlugins.add(metadataTypePlugin);
  }

  @Override
  public List<MetadataType> getMetadataTypes() {
    return this.metadataStorage.getMetadataTypes();
  }

  private MetadataTypePlugin getMetadataTypePluginByName(String name) {
    return metadataTypePlugins.stream()
                              .filter(metadataTypePlugin -> StringUtils.equals(metadataTypePlugin.getName(), name))
                              .findFirst()
                              .orElse(null);
  }
}
