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
package io.meeds.social.identity.permission.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.social.identity.permission.service.UserPermissionService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Reacts to group-level changes affecting {@code SOC_USER_PERMISSION}:
 * <ul>
 * <li>a deleted group must drop its rows;</li>
 * <li>linking/unlinking a nested group affects every member of the child group,
 * so it is handled in batches, via
 * {@link UserPermissionNestedMembershipListener} (see MIP-237 &sect;5).</li>
 * </ul>
 */
@Component
public class UserPermissionGroupListener extends GroupEventListener {

  public static final String    NESTED_MEMBERSHIP_CHANGED_EVENT = "social.userPermission.nestedMembership.changed";

  private static final Log      LOG                             = ExoLogger.getLogger(UserPermissionGroupListener.class);

  @Autowired
  private OrganizationService   organizationService;

  @Autowired
  private ListenerService       listenerService;

  @Autowired
  private UserPermissionService userPermissionService;

  @PostConstruct
  public void init() {
    organizationService.getGroupHandler().addGroupEventListener(this);
  }

  @PreDestroy
  public void destroy() {
    organizationService.getGroupHandler().removeGroupEventListener(this);
  }

  @Override
  public void postDelete(Group group) throws Exception {
    userPermissionService.deleteAllForGroup(group.getId());
  }

  @Override
  public void linkGroups(NestedMembership nestedMembership) throws Exception {
    broadcastNestedMembershipChange(nestedMembership);
  }

  @Override
  public void unlinkGroups(NestedMembership nestedMembership) throws Exception {
    broadcastNestedMembershipChange(nestedMembership);
  }

  private void broadcastNestedMembershipChange(NestedMembership nestedMembership) {
    try {
      listenerService.broadcast(NESTED_MEMBERSHIP_CHANGED_EVENT, nestedMembership, nestedMembership);
    } catch (Exception e) {
      LOG.warn("Error broadcasting nested membership change for group {}", nestedMembership.getNestedGroupId(), e);
    }
  }

}
