/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.core.space.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.service.SpaceLayoutService;

import jakarta.annotation.PostConstruct;

@Service
public class SpaceLayoutHandlerPlugin implements SpaceLifeCycleListener {

  @Autowired
  private SpaceService spaceService;

  @Autowired
  private SpaceLayoutService spaceLayoutService;

  @PostConstruct
  public void init() {
    spaceService.registerSpaceLifeCycleListener(this);
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    spaceLayoutService.createSpaceSite(event.getPayload());
  }

}
