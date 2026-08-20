/**
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
package org.exoplatform.social.notification.plugin;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * Notifies the platform administrators, and only them, that a user has
 * requested to deactivate their account.
 */
public class AccountDeactivationRequestPlugin extends BaseNotificationPlugin {

  public static final String                  ID                    = "AccountDeactivationRequestPlugin";

  public static final String                  ADMINISTRATORS_GROUP  = "/platform/administrators";

  public static final ArgumentLiteral<String> REQUESTER             = new ArgumentLiteral<>(String.class, "requester");

  public AccountDeactivationRequestPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    String requester = ctx.value(REQUESTER);
    try {
      List<String> administrators = getPlatformAdministrators(requester);
      if (administrators.isEmpty()) {
        return null;
      }
      return NotificationInfo.instance()
                             .key(getId())
                             .to(administrators)
                             .with(SocialNotificationUtils.REMOTE_ID.getKey(), requester)
                             .setFrom(requester)
                             .end();
    } catch (Exception e) {
      ctx.setException(e);
      return null;
    }
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return StringUtils.isNotBlank(ctx.value(REQUESTER));
  }

  private List<String> getPlatformAdministrators(String requester) throws Exception {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    ListAccess<User> administrators = organizationService.getUserHandler().findUsersByGroupId(ADMINISTRATORS_GROUP);
    return Arrays.stream(administrators.load(0, administrators.getSize()))
                 .map(User::getUserName)
                 .filter(username -> !StringUtils.equals(username, requester))
                 .toList();
  }

}
