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

import java.util.List;

/**
 * Maintains and queries the denormalized {@code SOC_USER_PERMISSION} membership
 * projection of PicketLink IDM, so that group members' profiles can be filtered
 * efficiently by group id and membership type.
 * <p>
 * PicketLink IDM remains the authoritative source; the rows managed here are a
 * derived projection kept in sync through membership events, an initial backfill
 * and periodic reconciliation.
 */
public interface UserPermissionService {

  /**
   * Records a single direct membership.
   *
   * @param userName       member remote id
   * @param groupId        group identifier
   * @param membershipType membership type
   */
  void saveMembership(String userName, String groupId, String membershipType);

  /**
   * Removes a single membership.
   */
  void deleteMembership(String userName, String groupId, String membershipType);

  /**
   * Rebuilds all direct membership rows of a user from the OrganizationService.
   * Used by the backfill and reconciliation.
   */
  void saveUserMemberships(String userName);

  /**
   * Removes every membership row of a user.
   */
  void deleteUserMemberships(String userName);

  /**
   * Removes every membership row of a group.
   */
  void deleteGroupMemberships(String groupId);

  /**
   * @param userName member remote id
   * @return membership tokens of the form {@code membershipType:groupId}, used
   *         to feed the Elasticsearch permission index
   */
  List<String> getMembershipTokens(String userName);

  /**
   * Returns the identity ids of members of the given groups, ordered and
   * paginated at the database level.
   *
   * @param groupIds       group identifiers
   * @param providerId     identity provider id
   * @param membershipType membership type to match ({@code *} always matched),
   *                       or {@code null} for any type
   * @param enabled        identity enabled flag
   * @param offset         result offset
   * @param limit          result limit ({@code <= 0} means unbounded)
   */
  List<Long> getGroupMembersIdentityIds(List<String> groupIds,
                                        String providerId,
                                        String membershipType,
                                        boolean enabled,
                                        long offset,
                                        long limit);

  /**
   * Counts the members of the given groups matching the membership type.
   */
  long countGroupMembers(List<String> groupIds, String providerId, String membershipType, boolean enabled);

}
