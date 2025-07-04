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
package org.exoplatform.social.websocket;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.social.websocket.entity.WebSocketMessage;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.picocontainer.Startable;

public class SpaceWebSocketService implements Startable{

  public static final String    COMETD_CHANNEL = "/SpaceModification";


  private ContinuationService   continuationService;
   
  /**
   * Propagate a space modification from Backend to frontend
   * 
   * @param spaceModification The space modification object
   */
   public void sendMessage(String eventName, String spaceId, String displayName, String username) {
    Map<String, String> stringMap = new HashMap<>();
    stringMap.put("spaceId", spaceId);
    stringMap.put("displayName", displayName);
    String wsMessage = new WebSocketMessage(eventName, stringMap).toJsonString();
    continuationService.sendMessage(username, COMETD_CHANNEL, wsMessage);
  }
}
