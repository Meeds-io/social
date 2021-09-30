/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.metadata.favorite;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.favorite.model.FavoriteObject;
import org.exoplatform.social.metadata.model.*;

public class FavoriteServiceImpl implements FavoriteService {

  private MetadataService metadataService;

  public FavoriteServiceImpl(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public void createFavorite(Favorite favorite) throws ObjectAlreadyExistsException {
    long userIdentityId = favorite.getUserIdentityId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(),
                                              String.valueOf(userIdentityId),
                                              userIdentityId);
    FavoriteObject object = favorite.getObject();
    metadataService.createMetadataItem(object,
                                       metadataKey,
                                       userIdentityId);
  }

  @Override
  public void deleteFavorite(Favorite favorite) throws ObjectNotFoundException {
    long userIdentityId = favorite.getUserIdentityId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(),
                                              String.valueOf(userIdentityId),
                                              userIdentityId);
    List<MetadataItem> favorites = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, favorite.getObject());
    if (CollectionUtils.isEmpty(favorites)) {
      throw new ObjectNotFoundException("Favorite entity not found for favorite " + favorite);
    }
    for (MetadataItem favoriteItem : favorites) {
      metadataService.deleteMetadataItem(favoriteItem.getId(), userIdentityId);
    }
  }

}
