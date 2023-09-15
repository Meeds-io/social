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
package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.*;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.net.WebNotificationSender;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;
import java.util.HashMap;
import java.util.List;

public class SpaceNotificationImpl extends SpaceListenerPlugin {

  private WebNotificationService webNotificationService;

  private static final Log LOG = ExoLogger.getExoLogger(SpaceNotificationImpl.class);

  @Override
  public void removeInvitedUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    removeNotifications(space.getId(), userId, SpaceInvitationPlugin.ID);
    String[] managers = space.getManagers();
    if (managers != null && managers.length > 0) {
      for (String manager : managers) {
        removeNotifications(space.getId(), manager, RequestJoinSpacePlugin.ID);
      }
    }
  }

  @Override
  public void removePendingUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    removeNotifications(space.getId(), userId, SpaceInvitationPlugin.ID);
    String[] managers = space.getManagers();
    if (managers != null && managers.length > 0) {
      for (String manager : managers) {
        removeNotifications(space.getId(), manager, RequestJoinSpacePlugin.ID);
      }
    }
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    updateNotificationsStatus(space, userId, SpaceInvitationPlugin.ID);
    String[] managers = space.getManagers();
    if (managers != null && managers.length > 0) {
      for (String manager : managers) {
        updateNotificationsStatus(space, manager, RequestJoinSpacePlugin.ID);
      }
    }
  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    String senderRemoteId = space.getEditor();
    Identity identity = Utils.getIdentityManager().getOrCreateUserIdentity(senderRemoteId);
    String senderName = identity.getProfile().getFullName();
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
            .append(SocialNotificationUtils.REMOTE_ID, userId)
            .append(SocialNotificationUtils.SPACE, space)
            .append(SocialNotificationUtils.PROFILE, senderRemoteId)
            .append(SocialNotificationUtils.SENDER, senderName);

    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(SpaceInvitationPlugin.ID))).execute(ctx);
  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.REMOTE_ID, userId)
                                                             .append(SocialNotificationUtils.SPACE, space);
    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestJoinSpacePlugin.ID))).execute(ctx);
    
  }

  private void removeNotifications(String spaceId, String userId, String pluginId) {
    WebNotificationFilter webNotificationFilter = new WebNotificationFilter(userId);
    webNotificationFilter.setParameter("spaceId", spaceId);
    webNotificationFilter.setPluginKey(new PluginKey(pluginId));
    List<NotificationInfo> webNotifs = getWebNotificationService().getNotificationInfos(webNotificationFilter, 0, -1);
    for (NotificationInfo notificationInfo : webNotifs) {
      getWebNotificationService().remove(notificationInfo.getId());
    }
  }

  private void updateNotificationsStatus(Space space, String userId, String pluginId) {
    WebNotificationFilter webNotificationFilter = new WebNotificationFilter(userId);
    webNotificationFilter.setParameter("spaceId", space.getId());
    webNotificationFilter.setPluginKey(new PluginKey(pluginId));
    List<NotificationInfo> webNotifs = getWebNotificationService().getNotificationInfos(webNotificationFilter, 0, -1);
    for (NotificationInfo info : webNotifs) {
      info.setTo(userId);
      info.setRead(true);
      info.setOwnerParameter(new HashMap<>(info.getOwnerParameter()));
      info.getOwnerParameter().put("status", "accepted");
      updateNotification(info);
    }
  }

  private void updateNotification(NotificationInfo notification) {
    NotificationContext nCtx = NotificationContextImpl.cloneInstance().setNotificationInfo(notification);
    BaseNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
    if (plugin != null) {
      try {
        notification.setRead(false);
        notification.setOnPopOver(true);
        getWebNotificationService().save(notification);
        WebNotificationSender.sendJsonMessage(notification.getTo(), new MessageInfo());
      } catch (Exception e) {
        LOG.error("Can not update space invitation notification.", e.getMessage());
      }
    }
  }

  private WebNotificationService getWebNotificationService() {
    if (webNotificationService == null) {
      webNotificationService = CommonsUtils.getService(WebNotificationService.class);
    }
    return webNotificationService;
  }

}
