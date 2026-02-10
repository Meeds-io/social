/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Asynchronous
public class SpaceGroupCacheListener implements SpaceLifeCycleListener {

  @Autowired
  private OrganizationService  organizationService;

  @Autowired
  private ConversationRegistry conversationRegistry;

  @Autowired
  private IdentityRegistry     identityRegistry;

  @Autowired
  private SpaceService         spaceService;

  @PostConstruct
  public void init() {
    spaceService.registerSpaceLifeCycleListener(this);
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    if (space != null && StringUtils.isNotEmpty(space.getGroupId())) {
      organizationService.getGroupHandler().clearGroupCache(space.getGroupId());
      String spaceCreator = event.getTarget();
      identityRegistry.unregister(spaceCreator);
      conversationRegistry.unregisterByUserId(spaceCreator);
    }
  }

  @SneakyThrows
  @Override
  public void templateApplied(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceGroupId = space != null ? space.getGroupId() : null;
    if (spaceGroupId != null) {
      organizationService.getGroupHandler().clearGroupCache(space.getGroupId());
      String[] spaceMembers = space.getMembers();
      for (String userId : spaceMembers) {
        identityRegistry.unregister(userId);
        conversationRegistry.unregisterByUserId(userId);
      }
    }
  }
}
