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
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

import java.util.HashMap;
import java.util.List;

public class RelationshipNotificationImpl extends RelationshipListenerPlugin {

  private WebNotificationService webNotificationService;
  private static final Log LOG = ExoLogger.getLogger(RelationshipNotificationImpl.class);
  
  @Override
  public void confirmed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    String receiverId = relationship.getReceiver().getRemoteId();
    String senderId = relationship.getSender().getRemoteId();
    markAllInvitationNotificationsAsAccepted(senderId, receiverId);
  }

  @Override
  public void requested(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    try {
      NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.RELATIONSHIP, relationship);
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RelationshipReceivedRequestPlugin.ID)))
                                   .execute(ctx);
    } catch (Exception e) {
      LOG.warn("Failed to get invite to connect information of " + event + ": " + e.getMessage());
    }
  }

  @Override
  public void denied(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    String receiverId = relationship.getReceiver().getRemoteId();
    String senderId = relationship.getSender().getRemoteId();
    removeAllDeniedInvitationsNotifications(senderId, receiverId);
  }

  /**
   * marks all invitation notifications as accepted.
   * when the status of an invitation is accepted, this method will loop on all invitation web notifications
   * from the same sender and to the same the receiver, and marks them as accepted
   *
   * @param senderId Id of the notification (invitation) sender.
   * @param receiverId Id of the notification (invitation) receiver
   */
  private void markAllInvitationNotificationsAsAccepted(String senderId, String receiverId) {
    WebNotificationFilter webNotificationFilter = new WebNotificationFilter(receiverId);
    webNotificationFilter.setParameter("sender", senderId);
    webNotificationFilter.setPluginKey(new PluginKey(RelationshipReceivedRequestPlugin.ID));
    List<NotificationInfo> webNotifs = getWebNotificationService().getNotificationInfos(webNotificationFilter, 0, -1);

    NotificationInfo info = webNotifs.get(0);
    info.setRead(true);
    info.setOwnerParameter(new HashMap<>(info.getOwnerParameter()));
    info.getOwnerParameter().put("status", "accepted");
    updateNotification(info);

    webNotifs.remove(0);
    for (NotificationInfo notificationInfo : webNotifs) {
      getWebNotificationService().remove(notificationInfo.getId());
    }
  }

  /**
   * removes all invitation notifications which are denied.
   * when the status of an invitation is denied, this method will loop on all invitation web notifications
   * from the same sender and to the same the receiver, and removes them
   *
   * @param senderId Id of the notification (invitation) sender.
   * @param receiverId Id of the notification (invitation) receiver
   */
  private void removeAllDeniedInvitationsNotifications(String senderId, String receiverId) {
    WebNotificationFilter webNotificationFilter = new WebNotificationFilter(receiverId);
    webNotificationFilter.setParameter("sender", senderId);
    PluginKey pluginKey = new PluginKey(RelationshipReceivedRequestPlugin.ID);
    webNotificationFilter.setPluginKey(pluginKey);
    List<NotificationInfo> webNotifs = getWebNotificationService().getNotificationInfos(webNotificationFilter, 0, -1);
    for (NotificationInfo notificationInfo : webNotifs) {
      getWebNotificationService().remove(notificationInfo.getId());
    }
  }

  private WebNotificationService getWebNotificationService() {
    if (webNotificationService == null) {
      webNotificationService = CommonsUtils.getService(WebNotificationService.class);
    }
    return webNotificationService;
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
        LOG.error("Can not update relationship notification.", e.getMessage());
      }
    }
  }

}
