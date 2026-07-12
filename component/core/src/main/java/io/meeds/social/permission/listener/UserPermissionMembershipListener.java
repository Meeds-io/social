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
package io.meeds.social.permission.listener;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;

import io.meeds.social.permission.service.UserPermissionService;

/**
 * Keeps the {@code SOC_USER_PERMISSION} projection in sync with PicketLink IDM
 * membership changes.
 * <p>
 * Registered as a kernel listener plugin on the {@code OrganizationService} (see
 * the {@code external-component-plugin} configuration). The Spring
 * {@link UserPermissionService} is resolved lazily through the kernel container
 * so that the listener does not depend on the Spring context being initialized
 * when the OrganizationService starts.
 */
public class UserPermissionMembershipListener extends MembershipEventListener {

  private static final Log      LOG = ExoLogger.getLogger(UserPermissionMembershipListener.class);

  private UserPermissionService userPermissionService;

  @Override
  public void postSave(Membership m, boolean isNew) throws Exception {
    if (m == null) {
      return;
    }
    try {
      getUserPermissionService().saveMembership(m.getUserName(), m.getGroupId(), m.getMembershipType());
    } catch (Exception e) {
      LOG.warn("Unable to save user permission projection for membership {}", m, e);
    }
  }

  @Override
  public void postDelete(Membership m) throws Exception {
    if (m == null) {
      return;
    }
    try {
      getUserPermissionService().deleteMembership(m.getUserName(), m.getGroupId(), m.getMembershipType());
    } catch (Exception e) {
      LOG.warn("Unable to delete user permission projection for membership {}", m, e);
    }
  }

  private UserPermissionService getUserPermissionService() {
    if (userPermissionService == null) {
      userPermissionService = ExoContainerContext.getService(UserPermissionService.class);
    }
    return userPermissionService;
  }

}
