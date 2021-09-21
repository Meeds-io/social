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

  public abstract boolean canCreateMetadata(Metadata metadata, long userIdentityId);

  public abstract boolean canUpdateMetadata(Metadata metadata, long userIdentityId);

  public abstract boolean canDeleteMetadata(Metadata metadata, long userIdentityId);

  public abstract boolean canCreateMetadataItem(MetadataItem metadataItem, long userIdentityId);

  public abstract boolean canUpdateMetadataItem(MetadataItem metadataItem, long userIdentityId);

  public abstract boolean canDeleteMetadataItem(MetadataItem metadataItem, long userIdentityId);

}
