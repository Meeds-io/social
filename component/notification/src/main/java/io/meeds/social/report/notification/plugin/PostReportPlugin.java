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
package io.meeds.social.report.notification.plugin;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

/**
 * Notifies the managers of the space owning the reported activity/comment that
 * a member reported it, so they can review and moderate it. One notification
 * per report, consistent with existing activity notifications.
 */
public class PostReportPlugin extends BaseNotificationPlugin {

  public static final String                  ID              = "PostReportPlugin";

  public static final String                  REPORTER_PARAM     = "reporter";

  public static final String                  REASON_PARAM       = "reportReason";

  public static final ArgumentLiteral<String> REASON          = new ArgumentLiteral<>(String.class, REASON_PARAM);

  public static final String                  TARGET_TYPE_PARAM  = "reportedContentType";

  public static final String                  TARGET_TYPE_POST    = "post";

  public static final String                  TARGET_TYPE_COMMENT = "comment";

  public static final String                  TARGET_TYPE_ARTICLE = "article";

  private static final String                 NEWS_ACTIVITY_TYPE  = "news";

  public PostReportPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    Space space = ctx.value(SocialNotificationUtils.SPACE);
    String reporter = ctx.value(SocialNotificationUtils.REMOTE_ID);
    return activity != null && space != null && ArrayUtils.isNotEmpty(space.getManagers()) && StringUtils.isNotBlank(reporter);
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    Space space = ctx.value(SocialNotificationUtils.SPACE);
    String reporter = ctx.value(SocialNotificationUtils.REMOTE_ID);
    String reason = ctx.value(REASON);

    boolean comment = activity.isComment();
    ExoSocialActivity parentActivity = comment ? Utils.getActivityManager().getParentActivity(activity) : activity;
    if (parentActivity == null) {
      // orphan comment: its parent was deleted between the report and this
      // asynchronous notification — nothing left to review
      return null;
    }
    String targetType;
    if (comment) {
      targetType = TARGET_TYPE_COMMENT;
    } else if (NEWS_ACTIVITY_TYPE.equals(activity.getType())) {
      targetType = TARGET_TYPE_ARTICLE;
    } else {
      targetType = TARGET_TYPE_POST;
    }
    NotificationInfo notification = NotificationInfo.instance()
                                                    .key(getId())
                                                    .setSpaceId(space.getId() == null ? 0 : Long.parseLong(space.getId()))
                                                    .with(SocialNotificationUtils.ACTIVITY_ID.getKey(),
                                                          parentActivity.getId())
                                                    .with(TARGET_TYPE_PARAM, targetType)
                                                    .with(REPORTER_PARAM, reporter)
                                                    .with(REASON_PARAM, reason)
                                                    .setFrom(reporter);
    if (comment) {
      notification.with(SocialNotificationUtils.COMMENT_ID.getKey(), activity.getId());
    }
    // a manager reporting in their own space must not be notified of their
    // own report
    return notification.to(Arrays.stream(space.getManagers())
                                 .filter(manager -> !StringUtils.equals(manager, reporter))
                                 .toList());
  }

}
