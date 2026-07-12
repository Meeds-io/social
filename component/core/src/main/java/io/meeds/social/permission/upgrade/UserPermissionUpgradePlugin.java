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
package io.meeds.social.permission.upgrade;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;

import io.meeds.social.permission.service.UserPermissionService;

/**
 * Backfills the {@code SOC_USER_PERMISSION} projection from the existing
 * PicketLink IDM memberships of every user. Runs once on upgrade; new
 * membership changes are then handled incrementally by
 * {@code UserPermissionMembershipListener}.
 */
public class UserPermissionUpgradePlugin extends UpgradeProductPlugin {

  private static final Log          LOG        = ExoLogger.getExoLogger(UserPermissionUpgradePlugin.class);

  private static final int          PAGE_SIZE  = 100;

  private final OrganizationService organizationService;

  private UserPermissionService     userPermissionService;

  public UserPermissionUpgradePlugin(OrganizationService organizationService, InitParams initParams) {
    super(initParams);
    this.organizationService = organizationService;
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    // Only backfill when upgrading an existing installation; fresh installs
    // populate the projection through membership events at seeding time.
    return StringUtils.isNotBlank(previousVersion);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    long startTime = System.currentTimeMillis();
    LOG.info("Start:: Backfill of user permission (group membership) projection");
    int processed = 0;
    try {
      ListAccess<User> usersListAccess = organizationService.getUserHandler().findAllUsers(UserStatus.ANY);
      int total = usersListAccess.getSize();
      while (processed < total) {
        int limit = Math.min(PAGE_SIZE, total - processed);
        User[] users = usersListAccess.load(processed, limit);
        for (User user : users) {
          if (user != null) {
            getUserPermissionService().saveUserMemberships(user.getUserName());
          }
        }
        processed += users.length;
        if (users.length == 0) {
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Error while backfilling user permission projection after processing {} users", processed, e);
      return;
    }
    LOG.info("End:: Backfill of user permission projection finished for {} users. It took {} ms",
             processed,
             (System.currentTimeMillis() - startTime));
  }

  private UserPermissionService getUserPermissionService() {
    if (userPermissionService == null) {
      userPermissionService = ExoContainerContext.getService(UserPermissionService.class);
    }
    return userPermissionService;
  }

}
