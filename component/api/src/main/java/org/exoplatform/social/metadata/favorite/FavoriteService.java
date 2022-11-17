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
import org.exoplatform.social.metadata.FavoriteACLPlugin;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;
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
   * Retrieves the favorite items attached to a given {@link MetadataItem}
   * creatorId and {@link MetadataObject} type
   * 
   * @param objectType {@link MetadataObject} type
   * @param creatorId {@link MetadataItem} creatorId
   * @param offset offset of ids to retrieve
   * @param limit limit of ids to retrieve
   * @return {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getFavoriteItemsByCreatorAndType(String objectType, long creatorId, long offset, long limit);

  /**
   * Retrieves the favorite items attached to a given {@link MetadataItem}
   * creatorId and {@link MetadataObject} type and {@link MetadataObject} spaceId
   *
   * @param objectType {@link MetadataObject} type
   * @param creatorId {@link MetadataItem} creatorId
   * @param spaceId {@link MetadataItem} spaceId
   * @param offset offset of ids to retrieve
   * @param limit limit of ids to retrieve
   * @return {@link List} of linked {@link MetadataItem}
   */
  default List<MetadataItem> getFavoriteItemsByCreatorAndTypeAndSpaceId(String objectType,
                                                                        long creatorId,
                                                                        long spaceId,
                                                                        long offset,
                                                                        long limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Count the favorite items attached to a given {@link MetadataItem} creatorId
   *
   * @param creatorId {@link MetadataItem} creatorId
   * @return integer
   */
  int getFavoriteItemsSize(long creatorId);

  /**
   * Check if an object identified by its type/id for a user has favorite metadata
   * or not
   * 
   * @param favorite {@link Favorite} object that has to define: - objectType
   *          object type, can be of any type: activity, comment, notes... -
   *          objectId object technical unique identifier - userIdentityId
   *          {@link Identity} technical identifier of the user
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

  /**
   * Checks whether the user can mark an entity as favorite
   *
   * @param userIdentity user identity
   * @param entityType object type, can be of any type: activity, comment,
   *          notes...
   * @param entityId object technical unique identifier
   * @return true if the user can mark entity as favorite, else false.
   */
  default boolean canCreateFavorite(org.exoplatform.services.security.Identity userIdentity, String entityType, String entityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Add a favorite ACL plugins
   */
  default void addFavoriteACLPlugin(FavoriteACLPlugin favoriteACLPlugin) {
    throw new UnsupportedOperationException();
  }
}
