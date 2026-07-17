/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;

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
   * @deprecated The old getAllIdsByProviderSorted method without remoteIds no longer exists, use getAllIdsByProviderSorted with the remoteIds parameter instead.
   */
  @Deprecated(forRemoval = true, since = "7.2.0")
  default List<String> getAllIdsByProviderSorted(String providerId, String sortField, String sortDirection, boolean enabled, String userType, Boolean isConnected, String enrollmentStatus, long offset, long limit) {
    return getAllIdsByProviderSorted(providerId, sortField, sortDirection, enabled, userType, isConnected, enrollmentStatus, null, offset, limit);
  }

  default List<String> getAllIdsByProviderSorted(String providerId, String sortField, String sortDirection, boolean enabled, String userType, Boolean isConnected, String enrollmentStatus, List<String> remoteIds, long offset, long limit) {
    return getAllIdsByProviderSorted(providerId, sortField, sortDirection, enabled, userType, isConnected, enrollmentStatus, remoteIds, null, null, offset, limit);
  }

  default List<String> getAllIdsByProviderSorted(String providerId, String sortField, String sortDirection, boolean enabled, String userType, Boolean isConnected, String enrollmentStatus, List<String> remoteIds, List<String> groupIds, String membershipType, long offset, long limit) {
    return getAllIdsByProviderSorted(providerId, sortField, sortDirection, enabled, userType, isConnected, enrollmentStatus, remoteIds, groupIds, membershipType, false, offset, limit);
  }

  default List<String> getAllIdsByProviderSorted(String providerId, String sortField, String sortDirection, boolean enabled, String userType, Boolean isConnected, String enrollmentStatus, List<String> remoteIds, List<String> groupIds, String membershipType, boolean includeInheritedMemberships, long offset, long limit) { // NOSONAR parameters count
    return Collections.emptyList();
  }

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
   *@deprecated The old getIdentityIdsByProviderSorted method without remoteIds no longer exists, use getIdentityIdsByProviderSorted with the remoteIds parameter instead.
   */
  @Deprecated(forRemoval = true, since = "7.2.0")
  default List<Long> getIdentityIdsByProviderSorted(String providerId,
                                                    String sortField,
                                                    String sortDirection,
                                                    boolean isEnabled,
                                                    String userType,
                                                    Boolean isConnected,
                                                    String enrollmentStatus,
                                                    long offset,
                                                    long limit) {
    return getIdentityIdsByProviderSorted(providerId, sortField, sortDirection, isEnabled, userType, isConnected, enrollmentStatus, null, offset, limit);
  }

  default List<Long> getIdentityIdsByProviderSorted(String providerId,
                                                    String sortField,
                                                    String sortDirection,
                                                    boolean isEnabled,
                                                    String userType,
                                                    Boolean isConnected,
                                                    String enrollmentStatus,
                                                    List<String> remoteIds,
                                                    long offset,
                                                    long limit) {
    return getIdentityIdsByProviderSorted(providerId, sortField, sortDirection, isEnabled, userType, isConnected, enrollmentStatus, remoteIds, null, null, offset, limit);
  }

  /**
   * Get identity ids by providerId sorted by sortField, additionally restricted to the members of the
   * given groupIds (optionally further restricted to a given membershipType - blank/null matches any
   * type),
   * backed by the indexed {@code SOC_USER_PERMISSION} table.
   */
  default List<Long> getIdentityIdsByProviderSorted(String providerId,
                                                    String sortField,
                                                    String sortDirection,
                                                    boolean isEnabled,
                                                    String userType,
                                                    Boolean isConnected,
                                                    String enrollmentStatus,
                                                    List<String> remoteIds,
                                                    List<String> groupIds,
                                                    String membershipType,
                                                    long offset,
                                                    long limit) {
    return getIdentityIdsByProviderSorted(providerId, sortField, sortDirection, isEnabled, userType, isConnected, enrollmentStatus, remoteIds, groupIds, membershipType, false, offset, limit);
  }

  /**
   * Get identity ids by providerId sorted by sortField, additionally restricted to the members of the
   * given groupIds (optionally further restricted to a given membershipType - blank/null matches any
   * type),
   * backed by the indexed {@code SOC_USER_PERMISSION} table. When includeInheritedMemberships
   * is false, only direct members are matched, excluding members inherited through nested groups.
   */
  default List<Long> getIdentityIdsByProviderSorted(String providerId,
                                                    String sortField,
                                                    String sortDirection,
                                                    boolean isEnabled,
                                                    String userType,
                                                    Boolean isConnected,
                                                    String enrollmentStatus,
                                                    List<String> remoteIds,
                                                    List<String> groupIds,
                                                    String membershipType,
                                                    boolean includeInheritedMemberships,
                                                    long offset,
                                                    long limit) { // NOSONAR parameters count
    List<String> foundRemoteIds = getAllIdsByProviderSorted(providerId, sortField, sortDirection, isEnabled, userType, isConnected, enrollmentStatus, remoteIds, groupIds, membershipType, includeInheritedMemberships, offset, limit);
    return foundRemoteIds == null ? Collections.emptyList() :
            foundRemoteIds.stream()
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
   *@deprecated The old getAllIdsCountByProvider method without remoteIds no longer exists, use getAllIdsCountByProvider with the remoteIds parameter instead.
   */
  @Deprecated(forRemoval = true, since = "7.2.0")
  default int getAllIdsCountByProvider(String providerId, String userType, Boolean isConnected, boolean enabled, String enrollmentStatus) {
    return getAllIdsCountByProvider(providerId, userType, isConnected, enabled, enrollmentStatus, null);
  }

  default int getAllIdsCountByProvider(String providerId, String userType, Boolean isConnected, boolean enabled, String enrollmentStatus, List<String> remoteIds) {
    return getAllIdsCountByProvider(providerId, userType, isConnected, enabled, enrollmentStatus, remoteIds, null, null);
  }

  default int getAllIdsCountByProvider(String providerId, String userType, Boolean isConnected, boolean enabled, String enrollmentStatus, List<String> remoteIds, List<String> groupIds, String membershipType) {
    return getAllIdsCountByProvider(providerId, userType, isConnected, enabled, enrollmentStatus, remoteIds, groupIds, membershipType, false);
  }
  
  default int getAllIdsCountByProvider(String providerId, String userType, Boolean isConnected, boolean enabled, String enrollmentStatus, List<String> remoteIds, List<String> groupIds, String membershipType, boolean includeInheritedMemberships) { // NOSONAR parameters count
    return 0;
  }

  default Long findIdByProviderAndRemoteId(String providerId, String remoteId) {
    IdentityEntity identity = findByProviderAndRemoteId(providerId, remoteId);
    return identity == null ? null : identity.getId();
  }

}
