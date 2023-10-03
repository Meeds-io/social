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
package org.exoplatform.social.notification.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceWebNotificationItem {

  private String      applicationName;

  private String      applicationItemId;

  private long        userId;

  private long        spaceId;

  private Set<String> applicationSubItemIds;

  private String      activityId;

  public SpaceWebNotificationItem(String applicationName, String applicationItemId, long userId, long spaceId) {
    this.applicationName = applicationName;
    this.applicationItemId = applicationItemId;
    this.userId = userId;
    this.spaceId = spaceId;
  }

  public void addApplicationSubItem(String itemId) {
    if (StringUtils.isNotBlank(itemId)) {
      if (applicationSubItemIds == null) {
        applicationSubItemIds = new HashSet<>();
      }
      applicationSubItemIds.add(itemId);
    }
  }

}
