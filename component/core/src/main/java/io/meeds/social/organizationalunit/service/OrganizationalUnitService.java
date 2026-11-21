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
package io.meeds.social.organizationalunit.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.social.organizationalunit.model.OrganizationalUnit;
import io.meeds.social.organizationalunit.storage.OrganizationalUnitStorage;

@Service
public class OrganizationalUnitService {

  private static final Log          LOG               = ExoLogger.getLogger(OrganizationalUnitService.class);

  private static final String       PLATFORM_GROUP_ID = "/platform";

  @Autowired
  private OrganizationalUnitStorage organizationalUnitStorage;

  @Autowired
  private OrganizationService       organizationService;

  public boolean isOrganizationalUnit(String groupId) {
    return organizationalUnitStorage.isOrganizationalUnit(groupId);
  }

  /**
   * @return the Organizational Units the given user directly manages
   *         ({@code manager} or {@code *} membership on the Organizational Unit
   *         group itself, not inherited from a parent group).
   */
  public List<OrganizationalUnit> getManagedOrganizationalUnits(String userName) {
    return organizationalUnitStorage.getManagedOrganizationalUnits(userName);
  }

  /**
   * @return true if the given group is designated as an Organizational Unit
   *         that the given user directly manages ({@code manager} or {@code *}
   *         membership on the Organizational Unit group itself, not inherited
   *         from a parent group).
   */
  public boolean isManagedOrganizationalUnit(String groupId, String userName) {
    return organizationalUnitStorage.isManagedOrganizationalUnit(groupId, userName);
  }

  /**
   * Whether the given group is inside the management perimeter of the given
   * user: either an Organizational Unit the user directly manages, or a group
   * nested, at any level, inside such an Organizational Unit.
   *
   * @param groupId group id to check
   * @param userName user name of the potential manager
   * @return true if the given user can manage the given group
   */
  public boolean canManageGroup(String groupId, String userName) {
    return isManagedOrganizationalUnit(groupId, userName)
           || isNestedInManagedOrganizationalUnit(groupId, userName);
  }

  /**
   * Whether the given user can add or remove members of the given group as an
   * Organizational Unit manager: the group is in the user's management
   * perimeter ({@link #canManageGroup(String, String)}) and is neither a
   * platform group nor a group nested, at any level, inside one.
   *
   * @param groupId group id to check
   * @param userName user name of the potential manager
   * @return true if the given user can manage the memberships of the group
   */
  public boolean canManageGroupMemberships(String groupId, String userName) {
    if (isPlatformGroup(groupId)) {
      return false;
    }
    Set<String> enclosingGroupIds = new HashSet<>();
    // a group whose enclosing groups could not all be read is refused
    if (!collectEnclosingGroupIds(groupId, new HashSet<>(), enclosingGroupIds)
        || enclosingGroupIds.stream().anyMatch(OrganizationalUnitService::isPlatformGroup)) {
      return false;
    }
    return isManagedOrganizationalUnit(groupId, userName)
           || (CollectionUtils.isNotEmpty(enclosingGroupIds)
               && organizationalUnitStorage.isAnyManagedOrganizationalUnit(enclosingGroupIds, userName));
  }

  private static boolean isPlatformGroup(String groupId) {
    return PLATFORM_GROUP_ID.equals(groupId) || StringUtils.startsWith(groupId, PLATFORM_GROUP_ID + "/");
  }

  private boolean isNestedInManagedOrganizationalUnit(String groupId, String userName) {
    Set<String> enclosingGroupIds = new HashSet<>();
    collectEnclosingGroupIds(groupId, new HashSet<>(), enclosingGroupIds);
    return CollectionUtils.isNotEmpty(enclosingGroupIds)
           && organizationalUnitStorage.isAnyManagedOrganizationalUnit(enclosingGroupIds, userName);
  }

  /**
   * Collects the groups enclosing the given group, at any level.
   *
   * @return false when the group or one of its enclosing groups could not be
   *         read, the collected set being then incomplete
   */
  private boolean collectEnclosingGroupIds(String groupId, Set<String> visitedGroupIds, Set<String> enclosingGroupIds) {
    if (!visitedGroupIds.add(groupId)) {
      return true;
    }
    try {
      Group group = organizationService.getGroupHandler().findGroupById(groupId);
      boolean complete = true;
      if (group != null && CollectionUtils.isNotEmpty(group.getEnclosingMemberships())) {
        for (var enclosingMembership : group.getEnclosingMemberships()) {
          enclosingGroupIds.add(enclosingMembership.getGroupId());
          complete = collectEnclosingGroupIds(enclosingMembership.getGroupId(), visitedGroupIds, enclosingGroupIds) && complete;
        }
      }
      return complete;
    } catch (Exception e) {
      LOG.warn("Error retrieving enclosing groups of group {}", groupId, e);
      return false;
    }
  }

  public void setOrganizationalUnit(String groupId, boolean organizationalUnit) throws Exception {
    if (organizationalUnit) {
      Group group = organizationService.getGroupHandler().findGroupById(groupId);
      if (group == null) {
        throw new ObjectNotFoundException("Group " + groupId + " doesn't exist");
      }
      organizationalUnitStorage.setOrganizationalUnit(groupId, group.getLabel(), true);
    } else {
      organizationalUnitStorage.setOrganizationalUnit(groupId, null, false);
    }
  }

}
