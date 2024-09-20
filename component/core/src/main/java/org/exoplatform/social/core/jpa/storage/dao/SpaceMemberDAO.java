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

import java.time.Instant;
import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.SpaceMembershipStatus;

import jakarta.persistence.Tuple;

public interface SpaceMemberDAO extends GenericDAO<SpaceMemberEntity, Long> {
    void deleteBySpace(SpaceEntity entity);

    SpaceMemberEntity getSpaceMemberShip(String remoteId, Long spaceId, SpaceMembershipStatus status);

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
    default List<Long> getSpaceIdentityIdsByUserRole(String remoteId, SpaceMembershipStatus status, int offset, int limit) {
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
    List<String> getSpaceMembers(Long spaceId, SpaceMembershipStatus status, int offset, int limit);

    /**
     * Get disabled space members
     *
     * @param spaceId the space ID
     * @param offset offset of the page
     * @param limit number of elements on each page
     * @return list of user names
     */
    default List<String> getDisabledSpaceMembers(Long spaceId, int offset, int limit){
      throw new UnsupportedOperationException();
    }

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
    default List<Long> getSpaceIdsByUserRole(String username, SpaceMembershipStatus status, int offset, int limit) {
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
    int countSpaceMembers(Long spaceId, SpaceMembershipStatus status);

    /**
     * Count disabled space members
     *
     * @param spaceId the spaceID
     * @return the number of disabled space members
     */
    default int countDisabledSpaceMembers(Long spaceId) {
      throw new UnsupportedOperationException();
    }

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

    /**
     * Retrieves the Space Membership date
     * 
     * @param spaceId {@link Space} technical id
     * @param username User name (identifier)
     * @return {@link Instant} corresponding to the creation date of the
     *         membership
     */
    default Instant getSpaceMembershipDate(long spaceId, String username) {
      throw new UnsupportedOperationException();
    }

}
