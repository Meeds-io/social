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
package io.meeds.social.permission.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.permission.entity.UserPermissionEntity;
import io.meeds.social.permission.storage.UserPermissionStorage;

@Service
public class UserPermissionServiceImpl implements UserPermissionService {

  private static final Log             LOG = ExoLogger.getLogger(UserPermissionServiceImpl.class);

  private final UserPermissionStorage  userPermissionStorage;

  private final IdentityManager        identityManager;

  private final OrganizationService    organizationService;

  public UserPermissionServiceImpl(UserPermissionStorage userPermissionStorage,
                                   IdentityManager identityManager,
                                   OrganizationService organizationService) {
    this.userPermissionStorage = userPermissionStorage;
    this.identityManager = identityManager;
    this.organizationService = organizationService;
  }

  @Override
  public void saveMembership(String userName, String groupId, String membershipType) {
    long identityId = resolveIdentityId(userName);
    if (identityId < 0) {
      return;
    }
    userPermissionStorage.saveMembership(identityId, userName, groupId, membershipType, true);
  }

  @Override
  public void deleteMembership(String userName, String groupId, String membershipType) {
    userPermissionStorage.deleteMembership(userName, groupId, membershipType);
  }

  @Override
  public void saveUserMemberships(String userName) {
    long identityId = resolveIdentityId(userName);
    if (identityId < 0) {
      return;
    }
    try {
      // Direct memberships only; inherited (nested-group) memberships are
      // handled separately.
      Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(userName, false);
      userPermissionStorage.deleteUserMemberships(userName);
      if (memberships != null) {
        for (Membership membership : memberships) {
          userPermissionStorage.saveMembership(identityId,
                                               userName,
                                               membership.getGroupId(),
                                               membership.getMembershipType(),
                                               true);
        }
      }
    } catch (Exception e) {
      LOG.warn("Unable to rebuild user permissions for user {}", userName, e);
    }
  }

  @Override
  public void deleteUserMemberships(String userName) {
    userPermissionStorage.deleteUserMemberships(userName);
  }

  @Override
  public void deleteGroupMemberships(String groupId) {
    userPermissionStorage.deleteGroupMemberships(groupId);
  }

  @Override
  public List<String> getMembershipTokens(String userName) {
    return userPermissionStorage.getUserMemberships(userName)
                                .stream()
                                .map(this::toToken)
                                .toList();
  }

  @Override
  public List<Long> getGroupMembersIdentityIds(List<String> groupIds,
                                               String providerId,
                                               String membershipType,
                                               boolean enabled,
                                               long offset,
                                               long limit) {
    return userPermissionStorage.getGroupMembersIdentityIds(groupIds, providerId, membershipType, enabled, offset, limit);
  }

  @Override
  public long countGroupMembers(List<String> groupIds, String providerId, String membershipType, boolean enabled) {
    return userPermissionStorage.countGroupMembers(groupIds, providerId, membershipType, enabled);
  }

  private String toToken(UserPermissionEntity entity) {
    return entity.getMembershipType() + ":" + entity.getGroupId();
  }

  private long resolveIdentityId(String userName) {
    try {
      Identity identity = identityManager.getOrCreateUserIdentity(userName);
      if (identity == null || identity.getId() == null) {
        return -1;
      }
      return Long.parseLong(identity.getId());
    } catch (NumberFormatException e) {
      LOG.warn("Identity id of user {} is not a number, skipping user permission projection", userName);
      return -1;
    }
  }

}
