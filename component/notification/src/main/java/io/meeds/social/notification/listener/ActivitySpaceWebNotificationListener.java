/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.social.notification.listener;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;

import io.meeds.social.activity.plugin.ActivityPermanentLinkPlugin;

import jakarta.annotation.PostConstruct;

@Asynchronous
@Component
public class ActivitySpaceWebNotificationListener extends Listener<SpaceWebNotificationItem, Long> {

  private static final List<String>   EVENT_NAMES = Arrays.asList("notification.read.item");

  @Autowired
  private ListenerService             listenerService;

  @Autowired
  private ActivityManager             activityManager;

  @Autowired
  private SpaceWebNotificationService spaceWebNotificationService;

  @PostConstruct
  public void init() {
    EVENT_NAMES.forEach(name -> listenerService.addListener(name, this));
  }

  @Override
  @ExoTransactional
  public void onEvent(Event<SpaceWebNotificationItem, Long> event) throws Exception {
    SpaceWebNotificationItem spaceWebNotificationItem = event.getSource();
    if (ActivityPermanentLinkPlugin.OBJECT_TYPE.equals(spaceWebNotificationItem.getApplicationName())) {
      ExoSocialActivity activity = activityManager.getActivity(spaceWebNotificationItem.getApplicationItemId());
      if (activity != null && activity.hasSpecificMetadataObject()) {
        spaceWebNotificationItem = spaceWebNotificationItem.clone();
        spaceWebNotificationItem.setApplicationName(activity.getMetadataObjectType());
        spaceWebNotificationItem.setApplicationItemId(activity.getMetadataObjectId());
        spaceWebNotificationService.markAsRead(spaceWebNotificationItem);
      }
    }
  }

}
