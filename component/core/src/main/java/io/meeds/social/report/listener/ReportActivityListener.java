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
package io.meeds.social.report.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.social.report.service.ActivityReportService;

import jakarta.annotation.PostConstruct;

/**
 * Glue listener, no business logic: when an activity or a comment is edited by
 * its author, delegates to {@link ActivityReportService} so every active
 * report on it is flipped to stale and reporting becomes available again.
 */
@Component
public class ReportActivityListener extends ActivityListenerPlugin {

  @Autowired
  private ActivityManager       activityManager;

  @Autowired
  private ActivityReportService activityReportService;

  @PostConstruct
  public void init() {
    activityManager.addActivityEventListener(this);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    activityReportService.markReportsStale(event.getActivity());
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent event) {
    activityReportService.markReportsStale(event.getActivity());
  }

}
