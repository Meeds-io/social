/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.net.WebNotificationSender;
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
  private static final Log LOG = ExoLogger.getLogger(SocialNotificationUtils.class);

  public final static Pattern IMG_SRC_REGEX = Pattern.compile("<img[^>]*(?:(?:src\\s*=\\s*['\"]([^'\"]+)['\"])[^>]*(?:data-plugin-name\\s*=\\s*['\"](?:[^'\"]+)['\"]))[^>]*\\/>|<img[^>]*(?:(?:data-plugin-name\\s*=\\s*['\"](?:[^'\"]+)['\"])[^>]*(?:src\\s*=\\s*['\"]([^'\"]+)['\"]))[^>]*\\/>");

  public final static ArgumentLiteral<String> ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "activityId");

  public final static ArgumentLiteral<String> COMMENT_ID = new ArgumentLiteral<String>(String.class, "commentId");
  public final static ArgumentLiteral<String> COMMENT_REPLY_ID = new ArgumentLiteral<String>(String.class, "commentReplyId");
  public final static ArgumentLiteral<String> PARENT_ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "parentActivityId");
  public final static ArgumentLiteral<String> POSTER = new ArgumentLiteral<String>(String.class, "poster");
  public final static ArgumentLiteral<String> WATCHED = new ArgumentLiteral<String>(String.class, "watched");
  public final static ArgumentLiteral<String> LIKER = new ArgumentLiteral<String>(String.class, "likersId");
  public final static ArgumentLiteral<String> LIKERS = new ArgumentLiteral<String>(String.class, "likers");
  public final static ArgumentLiteral<String> SENDER = new ArgumentLiteral<String>(String.class, "sender");
  public final static ArgumentLiteral<ExoSocialActivity> ACTIVITY = new ArgumentLiteral<ExoSocialActivity>(ExoSocialActivity.class, "activity");
  public final static ArgumentLiteral<String> ORIGINAL_TITLE = new ArgumentLiteral<>(String.class, "original_title");
  public static final ArgumentLiteral<String> ORIGINAL_TITLE_SHARED = new ArgumentLiteral<>(String.class, "original_title_shared");
  public final static ArgumentLiteral<Profile> PROFILE = new ArgumentLiteral<Profile>(Profile.class, "profile");
  public final static ArgumentLiteral<Space> SPACE = new ArgumentLiteral<Space>(Space.class, "space");
  public final static ArgumentLiteral<String> REMOTE_ID = new ArgumentLiteral<String>(String.class, "remoteId");
  public final static ArgumentLiteral<String> SPACE_ID = new ArgumentLiteral<String>(String.class, "spaceId");
  public final static ArgumentLiteral<String> REQUEST_FROM = new ArgumentLiteral<String>(String.class, "request_from");
  public final static ArgumentLiteral<String> PRETTY_NAME = new ArgumentLiteral<String>(String.class, "prettyName");
  public final static ArgumentLiteral<Relationship> RELATIONSHIP = new ArgumentLiteral<Relationship>(Relationship.class, "relationship");
  public final static ArgumentLiteral<String> RELATIONSHIP_ID = new ArgumentLiteral<String>(String.class, "relationshipId");

  public static final String EMPTY_STR = "";

  public static final String SPACE_STR     = " ";
  
  public static final String DOT_STRING       = ".";
  public static final String A_HREF_TAG_REGEX = "</?a[^>]*>";
  
  
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
  
  /**
   * 
   * @param receiversMap
   * @param templateContext
   * @return
   */
  public static String getMessageInSpace(Map<String, List<String>> receiversMap, TemplateContext templateContext) {
    StringBuilder sb = new StringBuilder();
    Space space = null;
    String typeSpaceDisplay = (templateContext.getPluginId().equals("PostActivitySpaceStreamPlugin")) ? "space" : "space_members";
    
    for (Entry<String, List<String>> entry : receiversMap.entrySet()) {
      sb.append("<li style=\"margin: 0 0 13px 14px; font-size: 13px; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\">");
      String id = entry.getKey();
      try {       
        space = Utils.getSpaceService().getSpaceById(id);      
      } catch (Exception e) {
        continue;
      }
      List<String> values = entry.getValue();
      int count = values.size();     
      templateContext.put("SPACE", SocialNotificationUtils.buildRedirecUrl(typeSpaceDisplay, space.getId(), space.getDisplayName()));
      
      String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
      String key = "";
      StringBuilder value = new StringBuilder();
      
      for (int i = 0; i < count && i < 3; i++) {
        Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      templateContext.put(key, value.toString());
      if(count > 3) {       
        templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl(typeSpaceDisplay, space.getId(), String.valueOf((count - 3))));
      }

      String digester = TemplateUtils.processDigest(templateContext.digestType(count));
      sb.append(digester);
      sb.append("</li>");
    }
    
    return sb.toString();
  }

  public static String getMessageByIds(Map<String, List<String>> receiversMap, 
          TemplateContext templateContext) {
    return getMessageByIds(receiversMap, new HashMap<String, List<Pair<String, String>>>(), templateContext);
  }  
  
  /**
   * 
   * @param receiversMap
   * @param activityUserComments
   * @param templateContext
   * @return
   */
  public static String getMessageByIds(Map<String, List<String>> receiversMap, 
                                       Map<String, List<Pair<String, String>>> activityUserComments,
                                       TemplateContext templateContext) {
    StringBuilder sb = new StringBuilder();
    ExoSocialActivity activity = null;
    Space space = null;
    String typeActivityDisplay = (templateContext.getPluginId().equals("LikePlugin")) ? "view_likers_activity" : "view_full_activity";
    String typeSpaceDisplay = (templateContext.getPluginId().equals("PostActivitySpaceStreamPlugin")) ? "space" : "space_members";
    
    for (Entry<String, List<String>> entry : receiversMap.entrySet()) {
      sb.append("<li style=\"margin: 0 0 13px 14px; font-size: 13px; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\">");
      String id = entry.getKey();
      try {
        if (templateContext.getPluginId().equals("PostActivitySpaceStreamPlugin")) {
          space = Utils.getSpaceService().getSpaceById(id);
        } else {
          activity = Utils.getActivityManager().getActivity(id);        
        }
      } catch (Exception e) {
        continue;
      }
      List<String> values = entry.getValue();
      int count = values.size();
      if (activity != null) {
        String title = activity.getTitle();
        String imagePlaceHolder = getImagePlaceHolder(templateContext.getLanguage());
        title = processImageTitle(title, imagePlaceHolder);
        // removes a href link from title. Just for digest building case.
        title = title.replaceAll(A_HREF_TAG_REGEX, "");
        templateContext.put("ACTIVITY", SocialNotificationUtils.buildRedirecActivityUrl(typeActivityDisplay, activity.getId(), title));
      } else {
        templateContext.put("SPACE", SocialNotificationUtils.buildRedirecUrl(typeSpaceDisplay, space.getId(), space.getDisplayName()));
      }
      
      String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
      String key = "";
      StringBuilder value = new StringBuilder();
      
      for (int i = 0; i < count && i < 3; i++) {
        Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      templateContext.put(key, value.toString());
      if(count > 3) {
        if (activity != null) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecActivityUrl(typeActivityDisplay, activity.getId(), String.valueOf((count - 3))));
        } else {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl(typeSpaceDisplay, space.getId(), String.valueOf((count - 3))));
        }
      }

      String digester = TemplateUtils.processDigest(templateContext.digestType(count));
      sb.append(digester);
      List<Pair<String, String>> userComments = activityUserComments.get(id);
      if (userComments != null && userComments.size() > 0) {
          sb.append("<div style=\"background-color:#f9f9f9;padding: 10px;border-left:5px solid #AACDED; padding-left: 15px; color:black;overflow:hidden;text-overflow:ellipsis;max-width:500px;\">");
          for (Pair<String, String> pair: userComments) {
              if (userComments.size() > 1) {
                  sb.append("<b>").append(pair.getKey()).append(" : </b>");
              }
              sb.append("<div style=\"color:#333333; font-family: 'verdana,arial,sans-serif';white-space: normal; font-style: italic;max-height: 11px;\">")
                .append(pair.getValue());
              sb.append("</div>");
              if (userComments.size() > 1) {
                  sb.append("...");
              }
              sb.append("<br/>");
              break;
          }
          sb.append("</div>");
          sb.append("<br/>");
      }
      sb.append("</li>");
    }
    
    return sb.toString();
  }

  /**
   * 
   * @param receiversMap
   * @param templateContext
   * @param type
   * @return
   */
  public static String getMessageByIds(Map<String, List<String>> receiversMap, TemplateContext templateContext, String type) {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, List<String>> entry : receiversMap.entrySet()) {
      sb.append("<li style=\"margin: 0 0 13px 14px; font-size: 13px; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\">");
      String targetId = entry.getKey();
      List<String> values = entry.getValue();
      int count = values.size();

      String[] keys = new String[]{"USER", "USER_LIST", "LAST3_USERS"};
      if ("space".equals(type)) {
        keys = new String[]{"SPACE", "SPACE_LIST", "LAST3_SPACES"};
      }
      String key = "";
      StringBuilder value = new StringBuilder();

      for (int i = 0; i < count && i < 3; i++) {
        String name = "";
        if ("new_user".equals(type) || "user".equals(type) || "connections_request".equals(type)) {
          Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
          name = identity.getProfile().getFullName();
        } else {
          Space space = Utils.getSpaceService().getSpaceById(values.get(i));
          name = space.getDisplayName();
        }
        //
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(SocialNotificationUtils.buildRedirecUrl("new_user".equals(type) ? "user" : type, values.get(i), name));
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      templateContext.put(key, value.toString());
      if(count > 3) {
        if ("user".equals(type)) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("user_activity_stream", targetId, String.valueOf((count - 3))));
          templateContext.put("ACTIVITY_STREAM", LinkProviderUtils.getRedirectUrl("user_activity_stream", targetId));
        } else if ("space".equals(type)) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("space_invitation", targetId, String.valueOf((count - 3))));
        } else if ("new_user".equals(type)) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("connections", "all", String.valueOf((count - 3))));
        } else {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("connections_request", targetId, String.valueOf((count - 3))));
        }
      } else {
        if ("user".equals(type)) {
          templateContext.put("ACTIVITY_STREAM", LinkProviderUtils.getRedirectUrl("user_activity_stream", targetId));
        }
      }

      String digester = TemplateUtils.processDigest(templateContext.digestType(count));
      sb.append(digester);
      sb.append("</li>");
    }
    
    return sb.toString();
  }
  
  public static void processInforSendTo(Map<String, List<String>> map, String key, String value) {
    List<String> list = new LinkedList<String>();
    if (map.containsKey(key)) {
      list.addAll(map.get(key));
    }
    if (list.contains(value) == false) {
      list.add(value);
    }
    map.put(key, new ArrayList<String>(list));
  }
  
  public static void processInforUserComments(Map<String, List<Pair<String, String>>> map, String key, Pair<String, String> value) {
      List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
      if (map.containsKey(key)) {
        list.addAll(map.get(key));
      }
      list.add(value);
      map.put(key, list);
  }
  
  public static String buildRedirecUrl(String type, String id, String name) {
    String link = LinkProviderUtils.getRedirectUrl(type, id);
    return "<a target=\"_blank\" style=\"text-decoration: none; font-weight: bold; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\" href=\""+ link + "\">" + StringEscapeUtils.escapeHtml4(name) + "</a>";
  }
  
  public static String buildRedirecActivityUrl(String type, String id, String activityTitle) {
    String link = LinkProviderUtils.getRedirectUrl(type, id);
    return "<a target=\"_blank\" style=\"text-decoration: none; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\" href=\""+ link + "\"><b>" + activityTitle + "</b></a>";
  }
  
  public static void addFooterAndFirstName(String remoteId, TemplateContext templateContext) {
    String firstName = "";
    String redirectUrl = "";

    Identity receiver = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
    if(receiver != null) {
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
    WebNotificationStorage storage = CommonsUtils.getService(WebNotificationStorage.class);
    String receiver = notification.getTo();
    NotificationInfo previousNotification = storage.getUnreadNotification(notification.getKey().getId(),
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
      storage.update(previousNotification, true);
      sendJsonMessage(storage, receiver);

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

  private static void sendJsonMessage(WebNotificationStorage storage, String receiver) {
    MessageInfo msg = new MessageInfo();
    int badgeNumber = storage.getNumberOnBadge(receiver);
    msg.setNumberOnBadge(badgeNumber);
    WebNotificationSender.sendJsonMessage(receiver, msg);
  }
}
