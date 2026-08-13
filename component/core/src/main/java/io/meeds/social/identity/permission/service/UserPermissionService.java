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
package io.meeds.social.identity.permission.service;

import java.util.Collection;
import java.util.List;

import lombok.SneakyThrows;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.services.organization.Membership;

import io.meeds.social.identity.permission.model.UserPermission;
import io.meeds.social.identity.permission.storage.UserPermissionStorage;

@Service
public class UserPermissionService {

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

  public List<String> getPermissionTokens(String userName) {
    return userPermissionStorage.getPermissions(userName).stream().map(permission -> {
      String token = permission.getMembershipType() + ":" + permission.getGroupId();
      return permission.isInherited() ? INHERITED_TOKEN_PREFIX + token : token;
    }).distinct().toList();
  }

  public void saveDirectMembership(long identityId, String userName, String groupId, String membershipType) {
    userPermissionStorage.saveDirectMembership(new UserPermission(0, identityId, userName, groupId, membershipType, false));
  }

  public void removeDirectMembership(String userName, String groupId, String membershipType) {
    userPermissionStorage.deleteMembership(userName, groupId, membershipType);
  }

  public void recomputeInheritedMemberships(long identityId, String userName, Collection<Membership> allMemberships) {
    userPermissionStorage.deleteInheritedMemberships(userName);
    allMemberships.stream()
                  .filter(Membership::isInherited)
                  .forEach(membership -> userPermissionStorage.saveInheritedMembership(new UserPermission(0,
                                                                                                          identityId,
                                                                                                          userName,
                                                                                                          membership.getGroupId(),
                                                                                                          membership.getMembershipType(),
                                                                                                          true)));
  }

  public void deleteAllForUser(String userName) {
    userPermissionStorage.deleteByUserName(userName);
  }

  public void deleteAllForGroup(String groupId) {
    userPermissionStorage.deleteByGroupId(groupId);
  }

  /**
   * @return the names of the existing membership types. Not cached on purpose:
   *         membership types can be created or deleted at runtime, and a stale
   *         list would silently hide the members using a newly created type
   *         from search results.
   */
  @SneakyThrows
  public Collection<String> getMembershipTypes() {
    return organizationService.getMembershipTypeHandler()
                              .findMembershipTypes()
                              .stream()
                              .map(MembershipType::getName)
                              .toList();
  }

}
