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
package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.notification.plugin.*;

import java.util.HashMap;
import java.util.Map;

public class ActivityNotificationImpl extends ActivityListenerPlugin {

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity originalActivity = event.getSource();
    ExoSocialActivity activity = CommonsUtils.getService(ActivityManager.class).getActivity(originalActivity.getId());
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.ACTIVITY, activity);
    ctx.append(SocialNotificationUtils.ORIGINAL_TITLE, originalActivity.getTitle());

    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(PostActivityPlugin.ID)))
                                 .with(ctx.makeCommand(PluginKey.key(PostActivitySpaceStreamPlugin.ID)))
                                 .with(ctx.makeCommand(PluginKey.key(ActivityMentionPlugin.ID)))
                                 .execute(ctx);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();
    Map<String, String> mentionsTemplateParams = activity.getTemplateParams() != null ? activity.getTemplateParams()
                                                                                      : new HashMap<>();
    activity = CommonsUtils.getService(ActivityManager.class).getActivity(activity.getId());

    // We check for the previous mentions in the event activity's template params
    // and retrieve them to be included in the activity to proceed the notification
    if (mentionsTemplateParams.containsKey("PreviousMentions")) {
      Map<String, String> templateParams = activity.getTemplateParams() != null ? activity.getTemplateParams() : new HashMap<>();
      templateParams.put("PreviousMentions", mentionsTemplateParams.get("PreviousMentions"));
      activity.setTemplateParams(templateParams);
    }

    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.ACTIVITY, activity);

    ctx.getNotificationExecutor()
       .with(ctx.makeCommand(PluginKey.key(EditActivityPlugin.ID)))
       .with(ctx.makeCommand(PluginKey.key(ActivityMentionPlugin.ID)))
       .execute(ctx);
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();
    activity = CommonsUtils.getService(ActivityManager.class).getActivity(activity.getId());
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.ACTIVITY, activity);

    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(ActivityCommentPlugin.ID)))
                                 .with(ctx.makeCommand(PluginKey.key(ActivityReplyToCommentPlugin.ID)))
                                 .with(ctx.makeCommand(PluginKey.key(ActivityMentionPlugin.ID)))
                                 .with(ctx.makeCommand(PluginKey.key(ActivityCommentWatchPlugin.ID)))
                                 .execute(ctx);
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();
    Map<String, String> mentionsTemplateParams = activity.getTemplateParams() != null ? activity.getTemplateParams()
                                                                                      : new HashMap<>();
    activity = CommonsUtils.getService(ActivityManager.class).getActivity(activity.getId());

    // We check for the previous mentions in the event activity's template params
    // and retrieve them to be included in the activity to proceed the notification
    if (mentionsTemplateParams.containsKey("PreviousMentions")) {
      Map<String, String> templateParams = activity.getTemplateParams() != null ? activity.getTemplateParams() : new HashMap<>();
      templateParams.put("PreviousMentions", mentionsTemplateParams.get("PreviousMentions"));
      activity.setTemplateParams(templateParams);
    }

    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.ACTIVITY, activity);

    ctx.getNotificationExecutor()
       .with(ctx.makeCommand(PluginKey.key(EditCommentPlugin.ID)))
       .with(ctx.makeCommand(PluginKey.key(ActivityMentionPlugin.ID)))
       .execute(ctx);
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();    
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.ACTIVITY, activity);
    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(LikePlugin.ID)))
                                 .execute(ctx);
  }

  @Override
  public void likeComment(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.ACTIVITY, activity);

    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(LikeCommentPlugin.ID)))
            .execute(ctx);
  }
  @Override
  public void shareActivity(ActivityLifeCycleEvent event) {
    // Create a new notification that contains all objects needed to create Mail notification with information about initial activity and new one
    ExoSocialActivity originalActivity = event.getSource();
    ExoSocialActivity activity = CommonsUtils.getService(ActivityManager.class).getActivity(originalActivity.getId());
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.ACTIVITY, activity);
    ctx.append(SocialNotificationUtils.ORIGINAL_TITLE, originalActivity.getTitle());
    ctx.append(SocialNotificationUtils.ORIGINAL_TITLE_SHARED, originalActivity.getBody());
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(SharedActivitySpaceStreamPlugin.ID)))
       .execute(ctx);
  }
}
