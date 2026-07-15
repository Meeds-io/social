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
package io.meeds.social.activity.schedule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.social.publication.plugin.PublicationPlugin;

/**
 * {@link PublicationPlugin} contributing scheduled activities (posts) to the
 * generic content publication mechanism, delegating operations to
 * {@link ActivityManager}
 */
@Component
public class ActivityPublicationPlugin implements PublicationPlugin {

  public static final String OBJECT_TYPE = "activity";

  @Autowired
  private ActivityManager    activityManager;

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public List<String> getDueObjectIds(long dueTime, int offset, int limit) {
    return activityManager.getScheduledActivityIds(dueTime, offset, limit);
  }

  @Override
  public ExoSocialActivity publish(String objectId) {
    return activityManager.publishScheduledActivity(objectId);
  }

}
