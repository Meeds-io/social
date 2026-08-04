/**
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.identity.permission.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.services.organization.Membership;

import io.meeds.social.identity.permission.model.UserPermission;
import io.meeds.social.identity.permission.storage.UserPermissionStorage;

/**
 * Business logic keeping {@code SOC_USER_PERMISSION} in sync with the
 * organization service's membership/group model (direct rows are one-to-one
 * with a {@link Membership}; inherited rows are recomputed wholesale from the
 * organization service's nested-group resolution on every change, see
 * {@code io.meeds.social.identity.permission.listener}).
 */
@Service
public class UserPermissionService {

  private static final Log      LOG                    = ExoLogger.getLogger(UserPermissionService.class);

  public static final String    ANY_MEMBERSHIP_TYPE    = "*";

  public static final String    INDEX_CONNECTOR_NAME   = "user_permission";

  /**
   * Prefix marking a token as inherited (via nested-group resolution), mirroring
   * the JPA {@code isInherited} column.
   */
  public static final String    INHERITED_TOKEN_PREFIX = "inherited:";

  @Autowired
  private UserPermissionStorage userPermissionStorage;

  @Autowired
  private OrganizationService   organizationService;

  private List<String>          membershipTypes        = null;

  /**
   * @return one {@code "<membershipType>:<groupId>"} token per row for the given
   *         prefixed with {@code "inherited:"} when the row is inherited (mirrors
   *         the JPA {@code isInherited} column exactly). This is the vocabulary
   *         used to build both the {@code social_user_permission} ES document and
   *         the profile document's merged {@code permissions} field.
   */
  public List<String> getPermissionTokens(String userName) {
    return userPermissionStorage.getPermissions(userName).stream().map(permission -> {
      String token = permission.getMembershipType() + ":" + permission.getGroupId();
      return permission.isInherited() ? INHERITED_TOKEN_PREFIX + token : token;
    }).distinct().toList();
  }

  public void saveDirectMembership(long identityId, String userName, String groupId, String membershipType) {
    userPermissionStorage.saveMembership(new UserPermission(0, identityId, userName, groupId, membershipType, false));
  }

  public void removeDirectMembership(String userName, String groupId, String membershipType) {
    userPermissionStorage.deleteMembership(userName, groupId, membershipType);
  }

  /**
   * this only rebuilds the inherited rows
   */
  public void recomputeInheritedMemberships(long identityId, String userName, Collection<Membership> allMemberships) {
    userPermissionStorage.deleteInheritedMemberships(userName);
    allMemberships.stream()
                  .filter(Membership::isInherited)
                  .forEach(membership -> userPermissionStorage.saveMembership(new UserPermission(0,
                                                                                                 identityId,
                                                                                                 userName,
                                                                                                 membership.getGroupId(),
                                                                                                 membership.getMembershipType(),
                                                                                                 true)));
  }

  /**
   * Counts how many groups nested inside the given parent group the given
   * user is member of. A group is considered nested when it is reachable from
   * the parent group at any depth, either as a child group or through an
   * explicit group link (nested membership). Distinct groups are counted, not
   * memberships: a user holding two membership types (ex: member and manager)
   * on the same nested group counts once. The parent group itself is
   * excluded, as are inherited memberships computed by nested-group
   * propagation.
   *
   * @param userName user name of the member
   * @param parentGroupId parent group id under which the nested groups the
   *          user is member of are counted, ex: /platform
   * @return number of distinct nested groups the user is member of
   */
  public long countNestedGroups(String userName, String parentGroupId) {
    return userPermissionStorage.getDirectGroupIds(userName)
                                .stream()
                                .filter(groupId -> !StringUtils.equals(parentGroupId, groupId))
                                .filter(groupId -> isNestedInGroup(groupId, parentGroupId, new HashSet<>()))
                                .count();
  }

  private boolean isNestedInGroup(String groupId, String targetGroupId, Set<String> visitedGroupIds) {
    if (!visitedGroupIds.add(groupId)) { // Cycle guard
      return false;
    }
    try {
      Group group = organizationService.getGroupHandler().findGroupById(groupId);
      return group != null
             && CollectionUtils.isNotEmpty(group.getEnclosingMemberships())
             && group.getEnclosingMemberships()
                     .stream()
                     .anyMatch(enclosingMembership -> StringUtils.equals(targetGroupId, enclosingMembership.getGroupId())
                                                      || isNestedInGroup(enclosingMembership.getGroupId(),
                                                                         targetGroupId,
                                                                         visitedGroupIds));
    } catch (Exception e) {
      LOG.warn("Error retrieving enclosing groups of group {}", groupId, e);
      return false;
    }
  }

  public void deleteAllForUser(String userName) {
    userPermissionStorage.deleteByUserName(userName);
  }

  public void deleteAllForGroup(String groupId) {
    userPermissionStorage.deleteByGroupId(groupId);
  }

  @SneakyThrows
  public Collection<String> getMembershipTypes() {
    if (membershipTypes == null) {
      membershipTypes = organizationService.getMembershipTypeHandler()
                                           .findMembershipTypes()
                                           .stream()
                                           .map(MembershipType::getName)
                                           .toList();
    }
    return membershipTypes;
  }

}
