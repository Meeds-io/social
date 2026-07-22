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
package io.meeds.social.organizationalunit.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.meeds.social.organizationalunit.entity.OrganizationalUnitEntity;

@Repository
public interface OrganizationalUnitDAO extends JpaRepository<OrganizationalUnitEntity, Long> {

  Optional<OrganizationalUnitEntity> findByGroupId(String groupId);

  boolean existsByGroupId(String groupId);

  void deleteByGroupId(String groupId);

  /**
   * Groups the given user directly manages ({@code manager} or {@code *}
   * membership, non inherited) that are also designated as an Organizational
   * Unit, joined directly against {@code SOC_USER_PERMISSION} by group id.
   */
  @Query("SELECT ou FROM SocOrganizationalUnit ou, SocUserPermission up " +
         "WHERE up.groupId = ou.groupId AND up.userName = :userName " +
         "AND up.membershipType IN ('manager', '*') AND up.inherited = false")
  List<OrganizationalUnitEntity> findManagedByUserName(@Param("userName") String userName);

  /**
   * Whether the given group is designated as an Organizational Unit that the
   * given user directly manages ({@code manager} or {@code *} membership, non
   * inherited).
   */
  @Query("SELECT COUNT(ou) > 0 FROM SocOrganizationalUnit ou, SocUserPermission up " +
         "WHERE ou.groupId = :groupId AND up.groupId = ou.groupId AND up.userName = :userName " +
         "AND up.membershipType IN ('manager', '*') AND up.inherited = false")
  boolean isManagedByUserName(@Param("groupId") String groupId, @Param("userName") String userName);

}
