/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Profile;

public class NewUserPlugin extends BaseNotificationPlugin {

  public static final String ID = "NewUserPlugin";
  public NewUserPlugin(InitParams initParams) {
    super(initParams);
  }
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Profile profile = ctx.value(SocialNotificationUtils.PROFILE);
    String remoteId = profile.getIdentity().getRemoteId();
    try {
      UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
      //
      userSettingService.initDefaultSettings(remoteId);
      //

      return NotificationInfo.instance()
                              .key(getId())
                              .with(SocialNotificationUtils.REMOTE_ID.getKey(), remoteId)
                              .setSendAll(true)
                              .setFrom(remoteId)
                              .end();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

}
