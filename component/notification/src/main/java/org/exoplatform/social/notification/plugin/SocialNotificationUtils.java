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
package org.exoplatform.social.notification.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.child.DefaultActivityChildPlugin;

public class SocialNotificationUtils {
  private static final Log                               LOG                   =
                                                             ExoLogger.getLogger(SocialNotificationUtils.class);

  public final static Pattern                            IMG_SRC_REGEX         =
                                                                       Pattern.compile("<img[^>]*(?:(?:src\\s*=\\s*['\"]([^'\"]+)['\"])[^>]*(?:data-plugin-name\\s*=\\s*['\"](?:[^'\"]+)['\"]))[^>]*\\/>|<img[^>]*(?:(?:data-plugin-name\\s*=\\s*['\"](?:[^'\"]+)['\"])[^>]*(?:src\\s*=\\s*['\"]([^'\"]+)['\"]))[^>]*\\/>");

  public final static ArgumentLiteral<String>            ACTIVITY_ID           =
                                                                     new ArgumentLiteral<String>(String.class, "activityId");

  public final static ArgumentLiteral<String>            COMMENT_ID            =
                                                                    new ArgumentLiteral<String>(String.class, "commentId");

  public final static ArgumentLiteral<String>            COMMENT_REPLY_ID      = new ArgumentLiteral<String>(String.class,
                                                                                                             "commentReplyId");

  public final static ArgumentLiteral<String>            PARENT_ACTIVITY_ID    = new ArgumentLiteral<String>(String.class,
                                                                                                             "parentActivityId");

  public final static ArgumentLiteral<String>            POSTER                =
                                                                new ArgumentLiteral<String>(String.class, "poster");

  public final static ArgumentLiteral<String>            WATCHED               =
                                                                 new ArgumentLiteral<String>(String.class, "watched");

  public final static ArgumentLiteral<String>            LIKER                 =
                                                               new ArgumentLiteral<String>(String.class, "likersId");

  public final static ArgumentLiteral<String>            LIKERS                =
                                                                new ArgumentLiteral<String>(String.class, "likers");

  public final static ArgumentLiteral<String>            SENDER                =
                                                                new ArgumentLiteral<String>(String.class, "sender");

  public final static ArgumentLiteral<ExoSocialActivity> ACTIVITY              =
                                                                  new ArgumentLiteral<ExoSocialActivity>(ExoSocialActivity.class,
                                                                                                         "activity");

  public final static ArgumentLiteral<String>            ORIGINAL_TITLE        =
                                                                        new ArgumentLiteral<>(String.class, "original_title");

  public static final ArgumentLiteral<String>            ORIGINAL_TITLE_SHARED = new ArgumentLiteral<>(String.class,
                                                                                                       "original_title_shared");

  public final static ArgumentLiteral<Profile>           PROFILE               =
                                                                 new ArgumentLiteral<Profile>(Profile.class, "profile");

  public final static ArgumentLiteral<Space>             SPACE                 = new ArgumentLiteral<Space>(Space.class, "space");

  public final static ArgumentLiteral<String>            REMOTE_ID             =
                                                                   new ArgumentLiteral<String>(String.class, "remoteId");

  public final static ArgumentLiteral<String>            SPACE_ID              =
                                                                  new ArgumentLiteral<String>(String.class, "spaceId");

  public final static ArgumentLiteral<String>            REQUEST_FROM          =
                                                                      new ArgumentLiteral<String>(String.class, "request_from");

  public final static ArgumentLiteral<String>            PRETTY_NAME           =
                                                                     new ArgumentLiteral<String>(String.class, "prettyName");

  public final static ArgumentLiteral<Relationship>      RELATIONSHIP          =
                                                                      new ArgumentLiteral<Relationship>(Relationship.class,
                                                                                                        "relationship");

  public final static ArgumentLiteral<String>            RELATIONSHIP_ID       = new ArgumentLiteral<String>(String.class,
                                                                                                             "relationshipId");

  public static final String                             EMPTY_STR             = "";

  public static final String                             SPACE_STR             = " ";

  public static final String                             DOT_STRING            = ".";

  public static final String                             A_HREF_TAG_REGEX      = "</?a[^>]*>";

  public static String getUserId(String identityId) {
    return Utils.getIdentityManager().getIdentity(identityId, false).getRemoteId();
  }

  public static List<String> toListUserIds(String... userIds) {
    List<String> ids = new ArrayList<String>();

    for (String userId : userIds) {
      ids.add(userId);
    }

    return ids;
  }

  public static boolean isSpaceActivity(ExoSocialActivity activity) {
    Identity id = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
    return (id != null);
  }

  public static void addFooterAndFirstName(String remoteId, TemplateContext templateContext) {
    String firstName = "";
    String redirectUrl = "";

    Identity receiver = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
    if (receiver != null) {
      firstName = (String) receiver.getProfile().getProperty(Profile.FIRST_NAME);
      redirectUrl = LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId());
    }

    templateContext.put("FIRSTNAME", firstName);
    templateContext.put("FOOTER_LINK", redirectUrl);
    templateContext.put("COMPANY_LINK", LinkProviderUtils.getBaseUrl());
  }

  public static String processImageTitle(String body, String placeholder) {
    Matcher matcher = IMG_SRC_REGEX.matcher(body);
    int startIdex = 0;
    while (matcher.find(startIdex)) {
      String imageBody = matcher.group(0);

      body = body.replace(imageBody, "<i> [" + placeholder + "] </i>");
      startIdex = matcher.end(0);
    }
    return body;
  }

  public static String getImagePlaceHolder(String language) {
    return TemplateUtils.getResourceBundle("Notification.label.InlineImage",
                                           new Locale(language),
                                           "locale.social.Webui");
  }

  public static String getBody(NotificationContext ctx, TemplateContext context, ExoSocialActivity activity) {
    PluginKey childKey = new PluginKey(activity.getType());
    PluginContainer pluginContainer = CommonsUtils.getService(PluginContainer.class);
    BaseNotificationPlugin child = pluginContainer.getPlugin(childKey);
    if (child == null || (child instanceof AbstractNotificationChildPlugin) == false) {
      child = pluginContainer.getPlugin(new PluginKey(DefaultActivityChildPlugin.ID));
    }
    String content = ((AbstractNotificationChildPlugin) child).makeContent(ctx);
    String language = NotificationPluginUtils.getLanguage(ctx.getNotificationInfo().getTo());
    content = MentionUtils.substituteRoleWithLocale(content, Locale.forLanguageTag(language));
    context.put("ACTIVITY", content);

    String body = TemplateUtils.processGroovy(context);
    body = processImageTitle(body, getImagePlaceHolder(context.getLanguage()));
    return body;
  }

  public static NotificationInfo addUserToPreviousNotification(NotificationInfo notification,
                                                               String propertyName,
                                                               String activityId,
                                                               String userId) {
    WebNotificationService webNotificationService = CommonsUtils.getService(WebNotificationService.class);
    String receiver = notification.getTo();
    NotificationInfo previousNotification = webNotificationService.getUnreadNotification(notification.getKey().getId(),
                                                                                         activityId,
                                                                                         receiver);
    if (previousNotification != null && previousNotification.isOnPopOver()) {
      List<String> users = NotificationUtils.stringToList(previousNotification.getValueOwnerParameter(propertyName));
      if (users == null) {
        users = Collections.singletonList(userId);
      } else if (!users.contains(userId)) {
        users.add(0, userId);
      }
      previousNotification.with(propertyName, NotificationUtils.listToString(users));
      previousNotification.setUpdate(true);
      previousNotification.setRead(false);
      previousNotification.setResetOnBadge(false);
      previousNotification.setLastModifiedDate(Calendar.getInstance());
      previousNotification.setFrom(notification.getFrom());
      webNotificationService.update(previousNotification, true);

      // Mark new notification as Read as it was grouped
      // With previous one
      notification.setOnPopOver(false);
      notification.setRead(true);
    } else {
      notification.with(propertyName, NotificationUtils.listToString(Collections.singletonList(userId)));
    }
    return notification;
  }

  public static List<String> mergeUsers(NotificationInfo notification, String propertyName, String activityId, String userId) {
    return NotificationUtils.stringToList(notification.getValueOwnerParameter(propertyName));
  }

}
