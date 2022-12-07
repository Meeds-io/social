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
package org.exoplatform.social.websocket.entity;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WebSocketMessage {

  String              wsEventName;

  Map<String, Object> message;

  public WebSocketMessage(String wsEventName, Object... data) {
    this.wsEventName = wsEventName;
    if (data != null && data.length > 0) {
      message = new HashMap<>();
      for (Object object : data) {
        if (object != null) {
          message.put(object.getClass().getSimpleName().toLowerCase(), object);
        }
      }
    }
  }

  @Override
  public String toString() {
    return toJsonString();
  }

  public String toJsonString() {
    try {
      return new JsonGeneratorImpl().createJsonObject(this).toString();
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing current global object to string", e);
    }
  }

}
