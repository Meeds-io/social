/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2022 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.notification.channel;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
public class SpaceWebChannel extends AbstractChannel {

  private static final Log            LOG = ExoLogger.getLogger(SpaceWebChannel.class);

  public static final String         ID  = "SPACE_WEB_CHANNEL";

  public final ChannelKey            key = ChannelKey.key(ID);

  private SpaceWebNotificationService spaceWebNotificationService;

  public SpaceWebChannel(SpaceWebNotificationService spaceWebNotificationService) {
    this.spaceWebNotificationService = spaceWebNotificationService;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public ChannelKey getKey() {
    return key;
  }

  @Override
  public void dispatch(NotificationContext ctx, String username) {
    NotificationInfo notification = ctx.getNotificationInfo();
    try {
      spaceWebNotificationService.dispatch(notification, username);
    } catch (Exception e) {
      LOG.warn("Error processing notification {}", notification, e);
    }
  }

  @Override
  public void registerTemplateProvider(TemplateProvider provider) {
    // No registration will be needed
  }

  @Override
  public boolean isDefaultChannel() {
    return false;
  }

  @Override
  protected AbstractTemplateBuilder getTemplateBuilderInChannel(PluginKey key) {
    // No Template Builder is used for notifications display
    return null;
  }

}
