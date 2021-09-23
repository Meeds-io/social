package org.exoplatform.social.core.metadata.favorite;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;

public class FavoriteServiceImpl implements FavoriteService {

  private MetadataService metadataService;

  public FavoriteServiceImpl(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public void createFavorite(String objectType, String objectId, long userIdentityId) throws ObjectAlreadyExistsException {
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setObjectId(objectId);
    metadataItem.setObjectType(objectType);

    metadataService.createMetadataItem(metadataItem,
                                       METADATA_TYPE.getName(),
                                       String.valueOf(userIdentityId),
                                       userIdentityId,
                                       userIdentityId,
                                       false);
  }

  @Override
  public void deleteFavorite(String objectType, String objectId, long userIdentityId) throws ObjectNotFoundException {
    Metadata metadata = metadataService.getMetadataByTypeAndNameAndAudience(METADATA_TYPE.getName(),
                                                                            String.valueOf(userIdentityId),
                                                                            userIdentityId);
    if (metadata == null) {
      throw new ObjectNotFoundException("Favorite metadata entity not found for user " + userIdentityId);
    }
    List<MetadataItem> favorites = metadataService.getMetadataItemsByMetadataAndObject(metadata.getId(), objectType, objectId);
    if (CollectionUtils.isEmpty(favorites)) {
      throw new ObjectNotFoundException("Favorite entity not found for user " + userIdentityId + " and object type/id "
          + objectType + "/" + objectId);
    }
    for (MetadataItem favoriteItem : favorites) {
      metadataService.deleteMetadataItem(favoriteItem.getId(), userIdentityId);
    }
  }

}
