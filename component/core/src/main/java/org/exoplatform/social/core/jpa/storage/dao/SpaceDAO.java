/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;

public interface SpaceDAO extends GenericDAO<SpaceEntity, Long> {

  List<Long> getLastSpaces(int limit);

  SpaceEntity getSpaceByGroupId(String groupId);

  SpaceEntity getSpaceByURL(String url);

  SpaceEntity getSpaceByDisplayName(String spaceDisplayName);

  SpaceEntity getSpaceByPrettyName(String spacePrettyName);


  /**
   * Get common spaces between two users
   *
   * @param userId connected user id
   * @param otherUserId visited profile user id
   * @param offset
   * @param limit
   * @return list of common spaces between two users in param
   */
  default List<SpaceEntity> getCommonSpaces(String userId,String otherUserId, int offset, int limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Count common spaces between two users
   *
   * @param userId connected user id
   * @param otherUserId visited profile user id
   * @return list of common spaces between two users in param
   */
  default int countCommonSpaces(String userId,String otherUserId)  {
    throw new UnsupportedOperationException();
  }
}
