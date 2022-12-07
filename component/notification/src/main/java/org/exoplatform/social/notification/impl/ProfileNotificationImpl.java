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
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class ProfileNotificationImpl extends ProfileListenerPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(ProfileNotificationImpl.class);

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {
  }

  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    Profile profile = event.getProfile();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.PROFILE, profile);
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(NewUserPlugin.ID))).execute(ctx);
  }

  @Override
  public void aboutMeUpdated(ProfileLifeCycleEvent event) {
    
  }

}
