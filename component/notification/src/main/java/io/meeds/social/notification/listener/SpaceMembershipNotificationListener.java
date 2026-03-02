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
package io.meeds.social.notification.listener;

import jakarta.annotation.PostConstruct;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.social.notification.plugin.JoinedSpaceByInvitationLinkPlugin;
import io.meeds.social.notification.util.NotificationUtils;
import io.meeds.social.space.plugin.SpaceInvitationLifeCycleEvent;

@Component
@Asynchronous
public class SpaceMembershipNotificationListener implements SpaceLifeCycleListener {

  @Autowired
  private IdentityManager identityManager;

  @Autowired
  private SpaceService spaceService;

  @PostConstruct
  public void init() {
    spaceService.registerSpaceLifeCycleListener(this);
  }

  @Override
  public void userJoinedByInvitationLink(SpaceInvitationLifeCycleEvent event) {
    String inviterId = event.getInviterId();
    String invitedUserId = event.getTarget();
    Space space = event.getSpace();
    Long spaceId = space.getSpaceId();
    String spaceAvatarUrl = space.getAvatarUrl();
    String spaceDisplayName = space.getDisplayName();
    Identity identity = identityManager.getOrCreateUserIdentity(invitedUserId);
    if (identity == null) {
      return;
    }
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(NotificationUtils.INVITED_USER_ID, invitedUserId);
    ctx.append(NotificationUtils.INVITED_USER, identity.getProfile().getFullName());
    ctx.append(NotificationUtils.INVITER_ID, inviterId);
    ctx.append(NotificationUtils.SPACE_ID, String.valueOf(spaceId));
    ctx.append(NotificationUtils.SPACE_AVATAR_URL, spaceAvatarUrl);
    ctx.append(NotificationUtils.SPACE_DISPLAY_NAME, spaceDisplayName);
    ctx.getNotificationExecutor()
      .with(ctx.makeCommand(PluginKey.key(JoinedSpaceByInvitationLinkPlugin.ID))).execute(ctx);
  }
}
