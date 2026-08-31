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
package org.exoplatform.social.notification.channel.template;

import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

import io.meeds.social.report.notification.plugin.PostReportPlugin;

public class PostReportMailBuilderTest extends AbstractPluginTest {

  private ChannelManager manager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    manager = getService(ChannelManager.class);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  public AbstractTemplateBuilder getTemplateBuilder() {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(PostReportPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(PostReportPlugin.ID));
  }

  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(PostReportPlugin.ID));
  }

  public void testReportedPostNotifiesSpaceManagersWithLinkedTarget() throws Exception {
    Space space = getSpaceInstance(1);
    ExoSocialActivity activity = makeActivity(rootIdentity, "a post worth reporting");

    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(SocialNotificationUtils.ACTIVITY, activity)
                                                     .append(SocialNotificationUtils.SPACE, space)
                                                     .append(SocialNotificationUtils.REMOTE_ID,
                                                             demoIdentity.getRemoteId())
                                                     .append(PostReportPlugin.REASON, "spam");
    NotificationInfo notification = getPlugin().buildNotification(ctx);

    assertNotNull(notification);
    List<String> managers = notification.getSendToUserIds();
    assertEquals(List.of(rootIdentity.getRemoteId()), managers);
    assertEquals(activity.getId(), notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey()));
    assertEquals("post", notification.getValueOwnerParameter(PostReportPlugin.TARGET_TYPE_PARAM));
    assertEquals("spam", notification.getValueOwnerParameter(PostReportPlugin.REASON_PARAM));

    NotificationContext messageCtx = NotificationContextImpl.cloneInstance();
    messageCtx.setNotificationInfo(notification.setTo(rootIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(messageCtx);
    assertSubject(info, getFullName(demoIdentity.getRemoteId()) + " has reported a post as Spam");
    assertBody(info, "has reported");
    assertBody(info, "Please review");
  }

  public void testManagerReportingInOwnSpaceIsNotSelfNotified() throws Exception {
    Space space = getSpaceInstance(3);
    ExoSocialActivity activity = makeActivity(demoIdentity, "a post reported by the space manager");

    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(SocialNotificationUtils.ACTIVITY, activity)
                                                     .append(SocialNotificationUtils.SPACE, space)
                                                     .append(SocialNotificationUtils.REMOTE_ID,
                                                             rootIdentity.getRemoteId())
                                                     .append(PostReportPlugin.REASON, "spam");
    NotificationInfo notification = getPlugin().buildNotification(ctx);

    assertNotNull(notification);
    assertEquals(List.of(), notification.getSendToUserIds());
  }

  public void testReportedCommentTargetsTheCommentLink() throws Exception {
    Space space = getSpaceInstance(2);
    ExoSocialActivity activity = makeActivity(rootIdentity, "a post with a reported comment");
    ExoSocialActivity comment = makeComment(activity, demoIdentity, "an inappropriate comment");

    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(SocialNotificationUtils.ACTIVITY, comment)
                                                     .append(SocialNotificationUtils.SPACE, space)
                                                     .append(SocialNotificationUtils.REMOTE_ID,
                                                             maryIdentity.getRemoteId())
                                                     .append(PostReportPlugin.REASON, "spam");
    NotificationInfo notification = getPlugin().buildNotification(ctx);

    assertNotNull(notification);
    assertEquals("comment", notification.getValueOwnerParameter(PostReportPlugin.TARGET_TYPE_PARAM));
    assertEquals(activity.getId(), notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey()));
    assertEquals(comment.getId(), notification.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey()));

    NotificationContext messageCtx = NotificationContextImpl.cloneInstance();
    messageCtx.setNotificationInfo(notification.setTo(rootIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(messageCtx);
    // subjects longer than 50 characters get excerpted by assertSubject but not
    // by the mail channel, so the asserted subject must stay short
    assertSubject(info, getFullName(maryIdentity.getRemoteId()) + " has reported a comment as Spam");
    assertBody(info, "view_full_activity_highlight_comment");
  }

}
