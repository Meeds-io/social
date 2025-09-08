/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.activity.listener;

import jakarta.annotation.PostConstruct;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListener;
import org.exoplatform.social.core.activity.model.ActivityShareAction;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ActivitySharedActionsListener implements ActivityListener {

  @Autowired
  private PortalContainer container;

  private ActivityManager activityManager;

    @PostConstruct
  public void init() {
    activityManager = container.getComponentInstanceOfType(ActivityManager.class);
    activityManager.addActivityListener(this);
  }

  public void updateActivity(ActivityLifeCycleEvent event) {
    Set<ActivityShareAction> sharedActions = event.getActivity().getShareActions();
    for (ActivityShareAction action : sharedActions) {
      for (Long id : action.getSharedActivityIds()) {
        ExoSocialActivity sharedactivity =  activityManager.getActivity(id.toString());
        sharedactivity.setCacheTime(System.currentTimeMillis());
        activityManager.updateActivity(sharedactivity);
      }
    }
  }

}
