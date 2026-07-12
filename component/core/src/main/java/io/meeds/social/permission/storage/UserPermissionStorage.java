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
package io.meeds.social.permission.storage;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.meeds.social.permission.dao.UserPermissionDAO;
import io.meeds.social.permission.entity.UserPermissionEntity;

/**
 * Persistence facade over {@link UserPermissionDAO} for the denormalized
 * {@code SOC_USER_PERMISSION} membership projection.
 */
@Component
public class UserPermissionStorage {

  private final UserPermissionDAO userPermissionDAO;

  public UserPermissionStorage(UserPermissionDAO userPermissionDAO) {
    this.userPermissionDAO = userPermissionDAO;
  }

  /**
   * Creates the membership row if it does not exist yet (idempotent on the
   * unique key userName + groupId + membershipType).
   */
  @Transactional
  public void saveMembership(long identityId, String userName, String groupId, String membershipType, boolean direct) {
    UserPermissionEntity entity = userPermissionDAO.findByUserNameAndGroupIdAndMembershipType(userName, groupId, membershipType);
    if (entity == null) {
      entity = new UserPermissionEntity();
      entity.setIdentityId(identityId);
      entity.setUserName(userName);
      entity.setGroupId(groupId);
      entity.setMembershipType(membershipType);
      entity.setDirect(direct);
      userPermissionDAO.save(entity);
    } else if (entity.isDirect() != direct || entity.getIdentityId() != identityId) {
      entity.setIdentityId(identityId);
      entity.setDirect(direct);
      userPermissionDAO.save(entity);
    }
  }

  @Transactional
  public void deleteMembership(String userName, String groupId, String membershipType) {
    userPermissionDAO.deleteByUserNameAndGroupIdAndMembershipType(userName, groupId, membershipType);
  }

  @Transactional
  public void deleteUserMemberships(String userName) {
    userPermissionDAO.deleteByUserName(userName);
  }

  @Transactional
  public void deleteGroupMemberships(String groupId) {
    userPermissionDAO.deleteByGroupId(groupId);
  }

  public List<UserPermissionEntity> getUserMemberships(String userName) {
    return userPermissionDAO.findByUserName(userName);
  }

  public List<Long> getGroupMembersIdentityIds(List<String> groupIds,
                                               String providerId,
                                               String membershipType,
                                               boolean enabled,
                                               long offset,
                                               long limit) {
    // Ordering is baked into the query (ORDER BY the member remote id). Spring
    // Data's page-based Pageable cannot express an arbitrary offset, so we fetch
    // the ordered ids up to (offset + limit) and trim: the result set is a list
    // of lightweight identity ids and member-listing offsets stay modest. A
    // native OFFSET query can replace this if deep pagination ever gets hot.
    if (limit <= 0) {
      List<Long> ids = userPermissionDAO.getGroupMembersIdentityIds(groupIds, providerId, membershipType, enabled,
                                                                    Pageable.unpaged());
      return trim(ids, offset, ids.size());
    }
    int to = (int) Math.min(offset + limit, Integer.MAX_VALUE);
    List<Long> ids = userPermissionDAO.getGroupMembersIdentityIds(groupIds, providerId, membershipType, enabled,
                                                                  PageRequest.of(0, to));
    return trim(ids, offset, to);
  }

  public long countGroupMembers(List<String> groupIds, String providerId, String membershipType, boolean enabled) {
    return userPermissionDAO.countGroupMembers(groupIds, providerId, membershipType, enabled);
  }

  private List<Long> trim(List<Long> ids, long offset, long end) {
    if (offset >= ids.size()) {
      return List.of();
    }
    int from = (int) offset;
    int to = (int) Math.min(end, ids.size());
    return List.copyOf(ids.subList(from, to));
  }

}
