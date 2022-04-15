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
package org.exoplatform.social.metadata.favorite;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataType;
import java.util.List;

public interface FavoriteService {

  public static final MetadataType METADATA_TYPE = new MetadataType(1, "favorites");

  /**
   * Create a new Favorite for corresponding object identified by its type/id
   * for a user
   * 
   * @param favorite {@link Favorite} object that has to define:
   *    - objectType object type, can be of any type: activity, comment, notes...
   *    - objectId object technical unique identifier
   *    - userIdentityId {@link Identity} technical identifier of the user
   * @throws ObjectAlreadyExistsException when the favorite already exists
   */
  public void createFavorite(Favorite favorite) throws ObjectAlreadyExistsException;

  /**
   * Retrieves the favorite items attached to a given {@link MetadataItem} creatorId
   *
   * @param creatorId {@link MetadataItem} creatorId
   * @param offset offset of ids to retrieve
   * @param limit limit of ids to retrieve
   * @return {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getFavoriteItemsByCreator(long creatorId, long offset, long limit);

  /**
   * Check if an object identified by its type/id for a user has favorite metadata
   * or not
   * 
   * @param favorite {@link Favorite} object that has to define: - objectType
   *          object type, can be of any type: activity, comment, notes... -
   *          objectId object technical unique identifier - userIdentityId
   *          {@link Identity} technical identifier of the user
   * @throws ObjectNotFoundException when the favorite doesn't exists
   */
  public boolean isFavorite(Favorite favorite);

  /**
   * Deletes an existing Favorite for corresponding object identified by its
   * type/id for a user
   *
   * @param favorite {@link Favorite} object that has to define:
   *    - objectType object type, can be of any type: activity, comment, notes...
   *    - objectId object technical unique identifier
   *    - userIdentityId {@link Identity} technical identifier of the user
   * @throws ObjectNotFoundException when the favorite doesn't exists
   */
  public void deleteFavorite(Favorite favorite) throws ObjectNotFoundException;

}
