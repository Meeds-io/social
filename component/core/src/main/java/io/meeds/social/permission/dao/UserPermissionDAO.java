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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.meeds.social.permission.entity.UserPermissionEntity;

@Repository
public interface UserPermissionDAO extends org.springframework.data.jpa.repository.JpaRepository<UserPermissionEntity, Long> {

  List<UserPermissionEntity> findByUserName(String userName);

  UserPermissionEntity findByUserNameAndGroupIdAndMembershipType(String userName, String groupId, String membershipType);

  @Modifying
  void deleteByUserName(String userName);

  @Modifying
  void deleteByGroupId(String groupId);

  @Modifying
  void deleteByUserNameAndGroupIdAndMembershipType(String userName, String groupId, String membershipType);

  /**
   * @param groupId group identifier
   * @return list of distinct user names having any membership on the given group
   */
  @Query("SELECT DISTINCT p.userName FROM SocUserPermission p WHERE p.groupId = :groupId")
  List<String> getUserNamesByGroup(@Param("groupId") String groupId);

}
