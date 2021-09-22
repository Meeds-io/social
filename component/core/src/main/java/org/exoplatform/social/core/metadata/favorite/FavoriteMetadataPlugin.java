package org.exoplatform.social.core.metadata.favorite;

import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.*;

public final class FavoriteMetadataPlugin extends MetadataTypePlugin {

  public static final MetadataType METADATA_TYPE = new MetadataType(1, "favorites");

  public FavoriteMetadataPlugin() {
    super(METADATA_TYPE);
  }

  @Override
  public boolean canCreateMetadata(Metadata metadata, long userIdentityId) {
    return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
  }

  @Override
  public boolean canUpdateMetadata(Metadata metadata, long userIdentityId) {
    return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
  }

  @Override
  public boolean canDeleteMetadata(Metadata metadata, long userIdentityId) {
    return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
  }

  @Override
  public boolean canCreateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
    return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
  }

  @Override
  public boolean canUpdateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
    return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
  }

  @Override
  public boolean canDeleteMetadataItem(MetadataItem metadataItem, long userIdentityId) {
    return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
  }

  @Override
  public boolean allowMultipleItemsPerObject() {
    return false;
  }
}
