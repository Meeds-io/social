package org.exoplatform.social.core.metadata.favorite;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;

public class FavoriteServiceImpl implements FavoriteService {

  private MetadataService metadataService;

  public FavoriteServiceImpl(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public void createFavorite(Favorite favorite) throws ObjectAlreadyExistsException {
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setParentObjectId(favorite.getParentObjectId());
    metadataItem.setObjectId(favorite.getObjectId());
    metadataItem.setObjectType(favorite.getObjectType());

    long userIdentityId = favorite.getUserIdentityId();
    metadataService.createMetadataItem(metadataItem,
                                       METADATA_TYPE.getName(),
                                       String.valueOf(userIdentityId),
                                       userIdentityId,
                                       userIdentityId,
                                       false);
  }

  @Override
  public void deleteFavorite(Favorite favorite) throws ObjectNotFoundException {
    long userIdentityId = favorite.getUserIdentityId();
    Metadata metadata = metadataService.getMetadataByTypeAndNameAndAudience(METADATA_TYPE.getName(),
                                                                            String.valueOf(userIdentityId),
                                                                            userIdentityId);
    if (metadata == null) {
      throw new ObjectNotFoundException("Favorite metadata entity not found for user " + userIdentityId);
    }
    List<MetadataItem> favorites = metadataService.getMetadataItemsByMetadataAndObject(metadata.getId(),
                                                                                       favorite.getObjectType(),
                                                                                       favorite.getObjectId());
    if (CollectionUtils.isEmpty(favorites)) {
      throw new ObjectNotFoundException("Favorite entity not found for favorite " + favorite);
    }
    for (MetadataItem favoriteItem : favorites) {
      metadataService.deleteMetadataItem(favoriteItem.getId(), userIdentityId);
    }
  }

}
