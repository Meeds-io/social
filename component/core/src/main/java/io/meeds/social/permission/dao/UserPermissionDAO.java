/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.permission.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.meeds.social.permission.entity.UserPermissionEntity;

@Repository
public interface UserPermissionDAO extends JpaRepository<UserPermissionEntity, Long> {

  List<UserPermissionEntity> findByUserName(String userName);

  UserPermissionEntity findByUserNameAndGroupIdAndMembershipType(String userName, String groupId, String membershipType);

  void deleteByUserName(String userName);

  void deleteByGroupId(String groupId);

  void deleteByUserNameAndGroupIdAndMembershipType(String userName, String groupId, String membershipType);

  /**
   * Returns the technical identity ids of the members of the given groups,
   * optionally restricted to a membership type (the "any" type {@code *} is
   * always matched), joined with {@code SOC_IDENTITIES} so that only enabled,
   * non-deleted identities of the requested provider are returned. Pagination
   * and ordering are pushed down to the database.
   *
   * @param groupIds       group identifiers to match
   * @param providerId     identity provider id (e.g. {@code organization})
   * @param membershipType membership type to match, or {@code null} to match
   *                       any type
   * @param enabled        identity enabled flag
   * @param pageable       pagination and sorting
   * @return ordered page of identity ids
   */
  @Query("""
      SELECT p.identityId FROM SocUserPermission p
      JOIN SocIdentityEntity i ON i.id = p.identityId
      WHERE p.groupId IN :groupIds
        AND (:membershipType IS NULL OR p.membershipType = :membershipType OR p.membershipType = '*')
        AND i.providerId = :providerId
        AND i.enabled = :enabled
        AND i.deleted = false
      GROUP BY p.identityId
      ORDER BY MIN(i.remoteId)
      """)
  List<Long> getGroupMembersIdentityIds(@Param("groupIds") List<String> groupIds,
                                        @Param("providerId") String providerId,
                                        @Param("membershipType") String membershipType,
                                        @Param("enabled") boolean enabled,
                                        Pageable pageable);

  @Query("""
      SELECT COUNT(DISTINCT p.identityId) FROM SocUserPermission p
      JOIN SocIdentityEntity i ON i.id = p.identityId
      WHERE p.groupId IN :groupIds
        AND (:membershipType IS NULL OR p.membershipType = :membershipType OR p.membershipType = '*')
        AND i.providerId = :providerId
        AND i.enabled = :enabled
        AND i.deleted = false
      """)
  long countGroupMembers(@Param("groupIds") List<String> groupIds,
                         @Param("providerId") String providerId,
                         @Param("membershipType") String membershipType,
                         @Param("enabled") boolean enabled);

  /**
   * @param groupId group identifier
   * @return distinct user names having any membership on the given group
   */
  @Query("SELECT DISTINCT p.userName FROM SocUserPermission p WHERE p.groupId = :groupId")
  List<String> getUserNamesByGroup(@Param("groupId") String groupId);

}
