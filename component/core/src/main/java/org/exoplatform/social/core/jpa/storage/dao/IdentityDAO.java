/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.storage.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public interface IdentityDAO extends GenericDAO<IdentityEntity, Long> {

  IdentityEntity findByProviderAndRemoteId(String providerId, String remoteId);

  long countIdentityByProvider(String providerId);

  ListAccess<IdentityEntity> findIdentities(ExtendProfileFilter filter);

  List<Long> getAllIds(int offset, int limit);  

  List<Long> getAllIdsByProvider(String providerId, int offset, int limit);

  ListAccess<Map.Entry<IdentityEntity, ConnectionEntity>> findAllIdentitiesWithConnections(long identityId,
                                                                                           String sortField,
                                                                                           String sortDirection);

  /**
   * set the DELETED flag to true
   * @param identityId the identity Id
   */
  void setAsDeleted(long identityId);

  /**
   * delete definitely an identity
   * @param identityId the identity Id
   */
  void hardDeleteIdentity(long identityId);

  /**
   * Get all identities by providerId sorted by sortField
   * 
   * @param providerId
   * @param sortField
   * @param sortDirection
   * @param enabled
   * @param userType
   * @param isConnected
   * @param enrollmentStatus
   * @param offset
   * @param limit
   * @return
   */
  List<String> getAllIdsByProviderSorted(String providerId, String sortField, String sortDirection, boolean enabled, String userType, Boolean isConnected, String enrollmentStatus, long offset, long limit);

  /**
   * Get identity ids by providerId sorted by sortField
   * 
   * @param providerId
   * @param sortField
   * @param sortDirection
   * @param isEnabled
   * @param userType
   * @param isConnected
   * @param enrollmentStatus
   * @param offset
   * @param limit
   * @return
   */
  default List<Long> getIdentityIdsByProviderSorted(String providerId,
                                                    String sortField,
                                                    String sortDirection,
                                                    boolean isEnabled,
                                                    String userType,
                                                    Boolean isConnected,
                                                    String enrollmentStatus,
                                                    long offset,
                                                    long limit) {
    List<String> remoteIds = getAllIdsByProviderSorted(providerId, sortField, sortDirection, isEnabled, userType, isConnected, enrollmentStatus, offset, limit);
    return remoteIds == null ? Collections.emptyList() :
                             remoteIds.stream()
                                      .map(remoteId -> findIdByProviderAndRemoteId(providerId, remoteId))
                                      .filter(Objects::nonNull)
                                      .toList();
  }

  /**
   * Count identities by providerId
   *
   * @param providerId
   * @param userType
   * @param isConnected
   * @param enabled
   * @return
   */
  int getAllIdsCountByProvider(String providerId, String userType, Boolean isConnected, boolean enabled, String enrollmentStatus);

  default Long findIdByProviderAndRemoteId(String providerId, String remoteId) {
    IdentityEntity identity = findByProviderAndRemoteId(providerId, remoteId);
    return identity == null ? null : identity.getId();
  }

}
