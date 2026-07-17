/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.identity.permission.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.meeds.social.identity.permission.entity.UserPermissionEntity;

/**
 * Write-side access to {@code SOC_USER_PERMISSION}, the source of truth kept in
 * sync with the organization service by
 * {@code io.meeds.social.identity.permission.listener}. The read path (group
 * member listing/counting) queries this table directly via a native SQL
 * {@code EXISTS} clause in {@code IdentityDAOImpl}, not through this
 * repository.
 */
@Repository
public interface UserPermissionDAO extends JpaRepository<UserPermissionEntity, Long> {

  List<UserPermissionEntity> findByUserName(String userName);

  Optional<UserPermissionEntity> findByUserNameAndGroupIdAndMembershipType(String userName,
                                                                           String groupId,
                                                                           String membershipType);

  void deleteByUserName(String userName);

  void deleteByUserNameAndInheritedTrue(String userName);

  void deleteByGroupId(String groupId);

}
