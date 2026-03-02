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
package io.meeds.social.space.listener;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.space.invitation.model.SpaceInvitationLink;
import jakarta.annotation.PostConstruct;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Asynchronous
public class SpaceInvitationLinkJoinListener implements SpaceLifeCycleListener {

  @Autowired
  private SpaceService spaceService;

  @PostConstruct
  public void init() {
		spaceService.registerSpaceLifeCycleListener(this);
	}

  @Override
  @ContainerTransactional
  public void joined(SpaceLifeCycleEvent event) {
    long spaceId = event.getSpace().getSpaceId();
    String invitedUserId = event.getTarget();
    SpaceInvitationLink spaceInvitationLink = spaceService.getSpaceInvitationLink(spaceId, invitedUserId);
    if (spaceInvitationLink != null) {
      spaceService.triggerUserJoinedByInvitationLink(event.getSpace(), event.getTarget(), spaceInvitationLink.getInviterId());
      spaceService.removeSpaceInvitationLink(spaceId, invitedUserId);
    }
  }

  @Override
  @ContainerTransactional
  public void removePendingUser(SpaceLifeCycleEvent event) {
    spaceService.removeSpaceInvitationLink(event.getSpace().getSpaceId(), event.getTarget());
  }
}
