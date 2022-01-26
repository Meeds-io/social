/*
* Copyright (C) 2003-2021 eXo Platform SAS.
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
*/
package org.exoplatform.social.websocket.entity;

import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * Websocket message that will be sent to browser
 */
public class ActivityStreamModification {

  private String activityId;

  private String commentId;

  private String parentCommentId;

  private String eventName;

  private String spaceId;

  public ActivityStreamModification(String activityId, String eventName, String spaceId) {
    this.activityId = activityId;
    this.eventName = eventName;
    this.spaceId = spaceId;
  }

  @Override
  public String toString() {
    try {
      return new JsonGeneratorImpl().createJsonObject(this).toString();
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing object to JSON string", e);
    }
  }
}
