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
package io.meeds.social.identity.permission.storage;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.meeds.social.identity.permission.dao.UserPermissionDAO;
import io.meeds.social.identity.permission.entity.UserPermissionEntity;
import io.meeds.social.identity.permission.model.UserPermission;
import io.meeds.social.identity.permission.utils.EntityMapper;

@Component
public class UserPermissionStorage {

  private UserPermissionDAO userPermissionDAO;

  public UserPermissionStorage(UserPermissionDAO userPermissionDAO) {
    this.userPermissionDAO = userPermissionDAO;
  }

  @Cacheable(cacheNames = "social.userPermissions")
  public List<UserPermission> getPermissions(String userName) {
    return userPermissionDAO.findByUserName(userName).stream().map(EntityMapper::fromEntity).toList();
  }

  @Transactional
  @CacheEvict(cacheNames = "social.userPermissions", allEntries = true)
  public UserPermission saveMembership(UserPermission userPermission) {
    UserPermissionEntity entity = userPermissionDAO
                                                   .findByUserNameAndGroupIdAndMembershipType(userPermission.getUserName(),
                                                                                              userPermission.getGroupId(),
                                                                                              userPermission.getMembershipType())
                                                   .orElseGet(UserPermissionEntity::new);
    entity.setIdentityId(userPermission.getIdentityId());
    entity.setUserName(userPermission.getUserName());
    entity.setGroupId(userPermission.getGroupId());
    entity.setMembershipType(userPermission.getMembershipType());
    entity.setInherited(userPermission.isInherited());
    entity = userPermissionDAO.save(entity);
    return EntityMapper.fromEntity(entity);
  }

  @Transactional
  @CacheEvict(cacheNames = "social.userPermissions", allEntries = true)
  public void deleteMembership(String userName, String groupId, String membershipType) {
    userPermissionDAO.findByUserNameAndGroupIdAndMembershipType(userName, groupId, membershipType)
                     .ifPresent(userPermissionDAO::delete);
  }

  @Transactional
  @CacheEvict(cacheNames = "social.userPermissions", allEntries = true)
  public void deleteInheritedMemberships(String userName) {
    userPermissionDAO.deleteByUserNameAndInheritedTrue(userName);
  }

  @Transactional
  @CacheEvict(cacheNames = "social.userPermissions", allEntries = true)
  public void deleteByUserName(String userName) {
    userPermissionDAO.deleteByUserName(userName);
  }

  @Transactional
  @CacheEvict(cacheNames = "social.userPermissions", allEntries = true)
  public void deleteByGroupId(String groupId) {
    userPermissionDAO.deleteByGroupId(groupId);
  }

}
