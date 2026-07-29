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

  private static final Log          LOG = ExoLogger.getLogger(OrganizationalUnitService.class);

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

  private boolean isNestedInManagedOrganizationalUnit(String groupId, String userName) {
    Set<String> enclosingGroupIds = new HashSet<>();
    collectEnclosingGroupIds(groupId, new HashSet<>(), enclosingGroupIds);
    return CollectionUtils.isNotEmpty(enclosingGroupIds)
           && organizationalUnitStorage.isAnyManagedOrganizationalUnit(enclosingGroupIds, userName);
  }

  private void collectEnclosingGroupIds(String groupId, Set<String> visitedGroupIds, Set<String> enclosingGroupIds) {
    if (!visitedGroupIds.add(groupId)) {
      return;
    }
    try {
      Group group = organizationService.getGroupHandler().findGroupById(groupId);
      if (group != null && CollectionUtils.isNotEmpty(group.getEnclosingMemberships())) {
        group.getEnclosingMemberships().forEach(enclosingMembership -> {
          enclosingGroupIds.add(enclosingMembership.getGroupId());
          collectEnclosingGroupIds(enclosingMembership.getGroupId(), visitedGroupIds, enclosingGroupIds);
        });
      }
    } catch (Exception e) {
      LOG.warn("Error retrieving enclosing groups of group {}", groupId, e);
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
