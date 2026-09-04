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
package io.meeds.social.report.notification.listener;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

import io.meeds.social.report.model.ActivityReport;
import io.meeds.social.report.notification.plugin.PostReportPlugin;

/**
 * Glue listener, no business logic: reacts to the generic
 * {@link io.meeds.social.report.service.ActivityReportService#EVENT_ACTIVITY_REPORTED}
 * event and triggers the moderation notification to the space managers through
 * the existing pluggable notification mechanism.
 */
@Asynchronous
public class ReportNotificationListener extends Listener<ActivityReport, Long> {

  private ActivityManager activityManager;

  private IdentityManager identityManager;

  private SpaceService    spaceService;

  public ReportNotificationListener(ActivityManager activityManager,
                                    IdentityManager identityManager,
                                    SpaceService spaceService) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
  }

  @Override
  public void onEvent(Event<ActivityReport, Long> event) throws Exception {
    ActivityReport report = event.getSource();
    ExoSocialActivity activity = activityManager.getActivity(report.getActivityId());
    Identity reporterIdentity = identityManager.getIdentity(String.valueOf(report.getReporterIdentityId()));
    Identity streamOwnerIdentity = identityManager.getIdentity(String.valueOf(report.getStreamOwnerIdentityId()));
    if (activity == null || reporterIdentity == null || streamOwnerIdentity == null) {
      return;
    }
    Space space = spaceService.getSpaceByPrettyName(streamOwnerIdentity.getRemoteId());
    if (space == null) {
      return;
    }
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(SocialNotificationUtils.ACTIVITY, activity)
                                                     .append(SocialNotificationUtils.SPACE, space)
                                                     .append(SocialNotificationUtils.REMOTE_ID,
                                                             reporterIdentity.getRemoteId())
                                                     .append(PostReportPlugin.REASON, report.getReason());
    ctx.getNotificationExecutor()
       .with(ctx.makeCommand(PluginKey.key(PostReportPlugin.ID)))
       .execute(ctx);
  }

}
