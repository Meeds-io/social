/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.websocket.listener;

import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.websocket.SpaceWebSocketService;
import org.exoplatform.social.websocket.entity.SpaceModification;

public class SpaceUpdateListener extends Listener<SpaceModification, Long>{

  private SpaceWebSocketService spaceWebSocketService;

  @Override
  public void onEvent(Event<SpaceModification, Long> event) throws Exception {
    SpaceModification spaceModification = event.getSource();
    spaceWebSocketService.sendMessage(spaceModification.getEventName(),
    spaceModification.getSpaceId(),
    spaceModification.getDisplayName(),
    spaceModification.getUsername());
  }
}
