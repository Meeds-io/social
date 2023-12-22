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

import jakarta.persistence.Tuple;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity.Status;
import org.exoplatform.social.core.space.model.Space;

public interface SpaceMemberDAO extends GenericDAO<SpaceMemberEntity, Long> {
    void deleteBySpace(SpaceEntity entity);

    SpaceMemberEntity getSpaceMemberShip(String remoteId, Long spaceId, Status status);

    /**
     * Get space identity ids switch user status
     *
     * @param remoteId user {@link Identity} remote Id
     * @param status equals to MEMBER, MANAGER, PENDING, INVITED or IGNORED
     * @param offset The starting point
     * @param limit limit of results to retrieve
     * @return {@link List} of {@link Space} technical identifiers of type
     *         {@link Long}
     */
    default List<Long> getSpaceIdentityIdsByUserRole(String remoteId, Status status, int offset, int limit) {
      throw new UnsupportedOperationException();
    }
    
    List<Long> getSpacesIdsByUserName(String userId, int offset, int limit);

    /**
     * Get space members switch status
     * 
     * @param spaceId
     * @param status equals to MEMBER, MANAGER, PENDING, INVITED or IGNORED
     * @param offset
     * @param limit
     * @return
     */
    List<String> getSpaceMembers(Long spaceId, Status status, int offset, int limit);

    /**
     * Retrieves the list of {@link Space} technical identifiers
     * 
     * @param username user remote id
     * @param offset The starting point
     * @param limit The limitation of returned results
     * @return {@link List} of {@link Space} technical identifiers of type {@link Long}
     */
    default List<Long> getSpaceIdByMemberId(String username, int offset, int limit) {
      throw new UnsupportedOperationException();
    }

    /**
     * Retrieves the list of {@link Space} technical identifiers switch status
     *
     * @param username user remote id
     * @param status equals to MEMBER, MANAGER, PENDING, INVITED or IGNORED
     * @param offset The starting point
     * @param limit The limitation of returned results
     * @return {@link List} of {@link Space} technical identifiers of type {@link Long}
     */
    default List<Long> getSpaceIdsByUserRole(String username, Status status, int offset, int limit) {
        throw new UnsupportedOperationException();
    }

  /**
   * Sort user identity remote ids
   * 
   * @param userNames
   * @param sortField
   * @param sortDirection
   * @param filterDisabled
   * @return {@link List} of userNames sorted by sortField
   */
  List<String> sortSpaceMembers(List<String> userNames,
                                String sortField,
                                String sortDirection,
                                boolean filterDisabled);

    /**
     * Count space members switch status
     * 
     * @param spaceId
     * @param status equals to MEMBER, MANAGER, PENDING, INVITED or IGNORED
     * @return
     */
    int countSpaceMembers(Long spaceId, Status status);

    /**
     * @param username username used to retrieve user spaces
     * @return the count of users requested to join spaces that user manages
     */
    int countPendingSpaceRequestsToManage(String username);

    /**
     * @param username username used to retrieve user spaces
     * @param offset offset of the query
     * @param limit limit of the query
     * @return {@link List} {@link Tuple} of users requested to
     *         join spaces that designated user (with userId parameter)
     *         manages
     */
    List<Tuple> getPendingSpaceRequestsToManage(String username, int offset, int limit);

    /**
     * Counts the number of external users in a specific space
     *
     * @param spaceId
     * @return counts the external members in the space
     */
    default int countExternalMembers(Long spaceId) {
        throw new UnsupportedOperationException();
    }

}
