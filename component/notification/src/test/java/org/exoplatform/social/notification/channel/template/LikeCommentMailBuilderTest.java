/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.notification.channel.template;

import java.io.StringWriter;
import java.io.Writer;
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
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.LikeCommentPlugin;

public class LikeCommentMailBuilderTest extends AbstractPluginTest {
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
    assertNotNull(channel);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(LikeCommentPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(LikeCommentPlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(LikeCommentPlugin.ID));
  }
  
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity and comment it
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    ExoSocialActivity comment = makeComment(activity, rootIdentity, getName());

    //STEP 2 like activity
    activityManager.saveLike(comment, demoIdentity);

    assertMadeMailDigestNotifications(1);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo likeNotification = list.get(0);
    
    //STEP 3 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(likeNotification.setTo("root"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, getFullName("demo") + " likes your comment testSimpleCase");
    assertBody(info, "New like on your activity stream");
  }
  
  public void testDigest() throws Exception {
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    ExoSocialActivity comment = makeComment(activity, rootIdentity, getName());
    activityManager.saveLike(comment, maryIdentity);
    activityManager.saveLike(comment, demoIdentity);
    activityManager.saveLike(comment, johnIdentity);
    //
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 3);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("mary") + ", " + getFullName("demo") + ", " + getFullName("john") + " have liked your comment: testDigest");
  }
  
  public void testDigestWithUnLike() throws Exception {
    // mary post activity on her stream
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    ExoSocialActivity comment = makeComment(activity, rootIdentity, getName());
    notificationService.clearAll();

    assertMadeMailDigestNotifications(0);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 0);
    activityManager.saveLike(comment, demoIdentity);
    activityManager.saveLike(comment, johnIdentity);

    assertMadeMailDigestNotifications(2);
    list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 2);

    // john unlike
    activityManager.deleteLike(comment, johnIdentity);

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("demo") + " has liked your comment: testDigestWithUnLike");
  }
}
