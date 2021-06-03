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
package org.exoplatform.social.websocket;

import org.exoplatform.social.websocket.entity.ActivityStreamModification;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

/**
 * A websocket service that will send information about modified Activity. This
 * will allow to have dynamic UI and fresh updates without refreshing page.
 */
public class ActivityStreamWebSocketService {

  public static final String  COMETD_CHANNEL = "/eXo/Application/ActivityStream";

  private ContinuationService continuationService;

  public ActivityStreamWebSocketService(ContinuationService continuationService) {
    this.continuationService = continuationService;
  }

  /**
   * Propagate an ActivityStream modification from Backend to frontend to add
   * dynamism in pages
   * 
   * @param activityStreamModification The ActivityStream modification object
   */
  public void sendMessage(ActivityStreamModification activityStreamModification) {
    String message = activityStreamModification.toString();
    continuationService.sendBroadcastMessage(COMETD_CHANNEL, message);
  }

}
