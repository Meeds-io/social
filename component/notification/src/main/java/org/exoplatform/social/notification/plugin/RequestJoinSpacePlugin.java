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

import java.util.Arrays;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.space.model.Space;

public class RequestJoinSpacePlugin extends BaseNotificationPlugin {
  
  public RequestJoinSpacePlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "RequestJoinSpacePlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Space space = ctx.value(SocialNotificationUtils.SPACE);
    String userId = ctx.value(SocialNotificationUtils.REMOTE_ID);
    
    return NotificationInfo.instance().key(getId())
           .with(SocialNotificationUtils.REQUEST_FROM.getKey(), userId)
           .with(SocialNotificationUtils.SPACE_ID.getKey(), space.getId())
           .to(Arrays.asList(space.getManagers()));
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    //only sent when the space has the registration option "Validation"
    Space space = ctx.value(SocialNotificationUtils.SPACE);
    if (space.getRegistration().equals(Space.VALIDATION)) {
      return true;
    }
    return false;
  }

}
