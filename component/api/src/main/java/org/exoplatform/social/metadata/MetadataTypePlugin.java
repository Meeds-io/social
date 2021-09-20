package org.exoplatform.social.metadata;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.social.metadata.model.*;

public abstract class MetadataTypePlugin extends BaseComponentPlugin {

  protected MetadataType metadataType;

  protected MetadataTypePlugin(MetadataType metadataType) {
    this.metadataType = metadataType;
  }

  public MetadataType getMetadataType() {
    return metadataType;
  }

  public abstract boolean canCreateMetadata(Metadata metadata);

  public abstract boolean canUpdateMetadata(Metadata metadata);

  public abstract boolean canDeleteMetadata(Metadata metadata);

  public abstract boolean canCreateMetadataItem(MetadataItem metadataItem);

  public abstract boolean canUpdateMetadataItem(MetadataItem metadataItem);

  public abstract boolean canDeleteMetadataItem(MetadataItem metadataItem);

}
