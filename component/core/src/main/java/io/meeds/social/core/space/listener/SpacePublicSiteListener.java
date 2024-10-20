/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.core.space.listener;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

import io.meeds.social.space.service.SpaceLayoutService;

/**
 * A listener to change public site visibility if the space has become hidden
 */
public class SpacePublicSiteListener extends SpaceListenerPlugin {

  private SpaceLayoutService spaceLayoutService;

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    if (StringUtils.equals(space.getVisibility(), Space.HIDDEN)
        && space.getPublicSiteId() > 0
        && !(StringUtils.equals(space.getPublicSiteVisibility(), SpaceUtils.MEMBER)
             || StringUtils.equals(space.getPublicSiteVisibility(), SpaceUtils.MANAGER))) {
      getSpaceLayoutService().saveSpacePublicSite(space, SpaceUtils.MEMBER);
    }
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    getSpaceLayoutService().removeSpacePublicSite(event.getPayload());
  }

  public SpaceLayoutService getSpaceLayoutService() {
    if (spaceLayoutService == null) {
      spaceLayoutService = ExoContainerContext.getService(SpaceLayoutService.class);
    }
    return spaceLayoutService;
  }
}
