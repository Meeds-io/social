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
package org.exoplatform.social.notification;

import static org.exoplatform.commons.notification.template.TemplateUtils.getExcerptSubject;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;
import org.exoplatform.social.notification.plugin.ActivityCommentWatchPlugin;
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;
import org.exoplatform.social.notification.plugin.ActivityReplyToCommentPlugin;
import org.exoplatform.social.notification.plugin.EditActivityPlugin;
import org.exoplatform.social.notification.plugin.EditCommentPlugin;
import org.exoplatform.social.notification.plugin.LikeCommentPlugin;
import org.exoplatform.social.notification.plugin.LikePlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;
import org.exoplatform.social.notification.plugin.PostActivitySpaceStreamPlugin;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPlugin;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform thanhvc@exoplatform.com
 * Aug 20, 2013
 */
public abstract class AbstractPluginTest extends AbstractCoreTest {

  protected Locale             initialDefaultLocale;

  protected UserSettingService userSettingService;

  public abstract BaseNotificationPlugin getPlugin();

  public static final List<String> MANAGED_USERS = Arrays.asList("root", "john", "demo", "mary", "ghost");

  public static final List<String> PLUGIN_IDS    = Arrays.asList(PostActivityPlugin.ID,
                                                                 ActivityCommentPlugin.ID,
                                                                 ActivityCommentWatchPlugin.ID,
                                                                 ActivityReplyToCommentPlugin.ID,
                                                                 ActivityMentionPlugin.ID,
                                                                 LikePlugin.ID,
                                                                 EditActivityPlugin.ID,
                                                                 EditCommentPlugin.ID,
                                                                 LikeCommentPlugin.ID,
                                                                 RequestJoinSpacePlugin.ID,
                                                                 SpaceInvitationPlugin.ID,
                                                                 RelationshipReceivedRequestPlugin.ID,
                                                                 PostActivitySpaceStreamPlugin.ID);

  @Override
  public void setUp() throws Exception {
    super.setUp();

    userSettingService = Utils.getService(UserSettingService.class);

    // set default locale to en (used for notification wording) to have
    // deterministic tests
    initialDefaultLocale = Locale.getDefault();
    Locale.setDefault(new Locale("en", "US"));

    turnON(getPlugin());
    initUsersSetting();
    notificationService.clearAll();
  }

  @Override
  public void tearDown() throws Exception {
    //
    turnOFF(getPlugin());

    if(initialDefaultLocale != null) {
      Locale.setDefault(initialDefaultLocale);
    }

    super.tearDown();
  }

  public void destroyPlugins(BaseNotificationPlugin plugin) {
    plugin = null;
  }

  /**
   * It will be invoked after make Activity, Relationship and New User also.
   * Makes the notification message and retrieve from MockNotificationService
   * 
   * @return
   */
  protected NotificationInfo getNotificationInfo(String username) {
    List<NotificationInfo> list = notificationService.storeDigest(username);
    assertTrue(list.size() > 0);
    return list.get(0);
  }

  protected List<NotificationInfo> getNotificationInfos(String username) {
    return notificationService.storeDigest(username);
  }

  /**
   * Validates the Message's subject
   * 
   * @param message
   * @param validatedString
   */
  protected void assertSubject(MessageInfo message, String validatedString) {
    assertEquals(getExcerptSubject(validatedString), message.getSubject());
  }

  /**
   * Validates the Message's body
   * 
   * @param message
   * @param includedString
   */
  protected void assertBody(MessageInfo message, String includedString) {
    assertTrue("body = '" + message.getBody() + "' \r\n doesn't contain\r\n " + includedString,
               message.getBody().indexOf(includedString) > 0);
  }

  /**
   * Validate the digest email
   * 
   * @param writer
   * @param includedString
   */
  protected void assertDigest(Writer writer, String includedString) {
    assertEquals(includedString, writer.toString().replaceAll("\\<.*?>", ""));
  }

  /**
   * Asserts the number of notification what made by the plugins.
   * 
   * @param number
   */
  protected void assertMadeMailDigestNotifications(int number) {
    UserSetting setting = userSettingService.get(rootIdentity.getRemoteId());
    if (setting.isInDaily(getPlugin().getKey().getId())) {
      assertEquals(number, notificationService.sizeOfStoredDigest());
    }
  }

  /**
   * Asserts the number of notification what made by the plugins.
   * 
   * @param number
   */
  protected List<NotificationInfo> assertMadeMailDigestNotifications(String username, int number) {
    UserSetting setting = userSettingService.get(username);
    List<NotificationInfo> got = notificationService.storeDigest(username);
    if (setting.isActive(MailChannel.ID, getPlugin().getKey().getId())) {
      got = notificationService.storeInstantly(username);
      assertEquals(number, got.size());
    }
    return got;
  }

  /**
   * Asserts the number of web's notifications what made by the plugins.
   * 
   * @param number
   */
  protected void assertMadeWebNotifications(int number) {
    UserSetting setting = userSettingService.get(rootIdentity.getRemoteId());
    if (setting.isActive(WebChannel.ID, getPlugin().getKey().getId())) {
      assertEquals(number, notificationService.sizeOfWebNotifs());
    }
  }

  /**
   * Asserts the number of web's notifications what made by the plugins.
   * 
   * @param number
   */
  protected List<NotificationInfo> assertMadeWebNotifications(String username, int number) {
    UserSetting setting = userSettingService.get(username);
    List<NotificationInfo> got = notificationService.storeWebNotifs(username);
    if (setting.isActive(WebChannel.ID, getPlugin().getKey().getId())) {
      got = notificationService.storeWebNotifs(username);
      assertEquals(number, got.size());
    }
    return got;
  }

  /**
   * Turn on the plug in
   * 
   * @param plugin
   */
  protected void turnON(BaseNotificationPlugin plugin) {
    pluginSettingService.saveActivePlugin(MailChannel.ID, plugin.getId(), true);
  }

  /**
   * Turn off the plugin
   * 
   * @param plugin
   */
  protected void turnOFF(BaseNotificationPlugin plugin) {
    pluginSettingService.saveActivePlugin(MailChannel.ID, plugin.getId(), false);
  }

  protected void turnFeatureOn() {
    exoFeatureService.saveActiveFeature("notification", true);
  }

  protected void turnFeatureOff() {
    exoFeatureService.saveActiveFeature("notification", false);
  }

  protected Relationship makeRelationship(Identity identity1, Identity identity2) {
    Relationship relationship = relationshipManager.inviteToConnect(identity1, identity2);
    tearDownRelationshipList.add(relationship);
    return relationship;
  }

  protected void cancelRelationship(Identity identity1, Identity identity2) {
    Relationship relationship = relationshipManager.get(identity1, identity2);
    if (relationship != null && relationship.getStatus() == Relationship.Type.PENDING) {
      relationshipManager.delete(relationship);
      tearDownRelationshipList.remove(relationship);
    }
  }

  /**
   * Makes a comment reply used for Test Case
   * 
   * @param  activity
   * @param  commenter
   * @param  commentTitle
   * @param  parentCommentId
   * @return
   */
  protected ExoSocialActivity makeCommentReply(ExoSocialActivity activity, Identity commenter, String commentTitle,
                                               String parentCommentId) {
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    comment.setUserId(commenter.getId());
    comment.setParentCommentId(parentCommentId);
    activityManager.saveComment(activity, comment);

    return comment;
  }

  /**
   * Makes the comment for Test Case
   * 
   * @param  activity
   * @param  commenter
   * @param  commentTitle
   * @return
   */
  protected ExoSocialActivity makeComment(ExoSocialActivity activity, Identity commenter, String commentTitle) {
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    comment.setUserId(commenter.getId());
    activityManager.saveComment(activity, comment);

    return comment;
  }

  /**
   * Edit the comment for Test Case
   * 
   * @param  activity
   * @param  comment
   * @param  commentTitle
   * @return
   */
  protected ExoSocialActivity editComment(ExoSocialActivity activity, ExoSocialActivity comment, String commentTitle) {
    comment.setTitle(commentTitle);
    activityManager.saveComment(activity, comment);

    return comment;
  }

  /**
   * Make space with space name
   * 
   * @param  number
   * @return
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  protected Space getSpaceInstance(int number) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setAvatarUrl("my-avatar-url");
    String[] managers = new String[] {
        rootIdentity.getRemoteId()
    };
    String[] members = new String[] {
        rootIdentity.getRemoteId()
    };
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    space.setAvatarLastUpdated(System.currentTimeMillis());
    this.spaceService.saveSpace(space, true);
    tearDownSpaceList.add(space);
    return space;
  }

  /**
   * Make Instantly setting
   * 
   * @param userId
   * @param settings the list of plugins
   */
  protected void setInstantlySettings(String userId, List<String> settings) {
    UserSetting userSetting = userSettingService.get(userId);

    if (userSetting == null) {
      userSetting = UserSetting.getInstance();
      userSetting.setUserId(userId);
    }
    userSetting.setChannelActive(MailChannel.ID);
    //
    userSetting.setChannelPlugins(MailChannel.ID, settings);
    userSettingService.save(userSetting);
  }

  /**
   * Make Daily setting
   * 
   * @param userId
   * @param settings
   */
  protected void setDailySetting(String userId, List<String> settings) {
    UserSetting userSetting = userSettingService.get(userId);

    if (userSetting == null) {
      userSetting = UserSetting.getInstance();
      userSetting.setUserId(userId);
    }
    userSetting.setChannelActive(MailChannel.ID);

    userSetting.setDailyPlugins(settings);
    userSettingService.save(userSetting);
  }

  /**
   * Make Weekly setting
   * 
   * @param userId
   * @param settings
   */
  protected void setWeeklySetting(String userId, List<String> settings) {
    UserSetting userSetting = userSettingService.get(userId);

    if (userSetting == null) {
      userSetting = UserSetting.getInstance();
      userSetting.setUserId(userId);
    }
    userSetting.setChannelActive(MailChannel.ID);

    userSetting.setWeeklyPlugins(settings);
    userSettingService.save(userSetting);
  }

  private void initUsersSetting() {
    initSettings(rootIdentity.getRemoteId());
    initSettings(maryIdentity.getRemoteId());
    initSettings(johnIdentity.getRemoteId());
    initSettings(demoIdentity.getRemoteId());
  }

  private void initSettings(String username) {
    List<String> instantly = new ArrayList<>(PLUGIN_IDS);
    instantly.add(NewUserPlugin.ID);
    List<String> daily = new ArrayList<>(PLUGIN_IDS);
    daily.add(NewUserPlugin.ID);
    List<String> weekly = new ArrayList<>(PLUGIN_IDS);
    List<String> webNotifs = new ArrayList<>(PLUGIN_IDS);
    webNotifs.add(NewUserPlugin.ID);

    // root
    saveSetting(instantly, daily, weekly, webNotifs, username);
  }

  private void saveSetting(List<String> instantly,
                           List<String> daily,
                           List<String> weekly,
                           List<String> webNotifs,
                           String userId) {
    UserSetting model = UserSetting.getInstance();
    model.setUserId(userId);
    model.setChannelActives(new HashSet<>(Arrays.asList(MailChannel.ID, WebChannel.ID)));
    model.setDailyPlugins(daily);
    model.setWeeklyPlugins(weekly);
    model.setChannelPlugins(MailChannel.ID, instantly);
    model.setChannelPlugins(WebChannel.ID, webNotifs);
    userSettingService.save(model);
  }

  protected AbstractTemplateBuilder getTemplateBuilder(NotificationContext ctx) {
    //
    AbstractChannel channel = ctx.getChannelManager().getChannel(ChannelKey.key(MailChannel.ID));
    assertNotNull(channel);
    return channel.getTemplateBuilder(ctx.getNotificationInfo().getKey());
  }

  /**
   * It will be invoked after the notification will be created. Makes the
   * Message Info by the plugin and NotificationContext
   * 
   * @ctx    the provided NotificationContext
   * @return
   */
  protected MessageInfo buildMessageInfo(NotificationContext ctx) {
    AbstractTemplateBuilder templateBuilder = getTemplateBuilder();
    if (templateBuilder == null) {
      templateBuilder = getTemplateBuilder(ctx);
    }
    MessageInfo massage = templateBuilder.buildMessage(ctx);
    assertNotNull(massage);
    return massage;
  }

  protected void buildDigest(NotificationContext ctx, Writer writer) {
    AbstractTemplateBuilder templateBuilder = getTemplateBuilder();
    if (templateBuilder == null) {
      templateBuilder = getTemplateBuilder(ctx);
    }
    templateBuilder.buildDigest(ctx, writer);
  }

  public abstract AbstractTemplateBuilder getTemplateBuilder();
}
