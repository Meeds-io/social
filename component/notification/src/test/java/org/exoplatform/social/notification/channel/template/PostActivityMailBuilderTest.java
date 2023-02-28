/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.notification.channel.template;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class PostActivityMailBuilderTest extends AbstractPluginTest {
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
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(PostActivityPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(PostActivityPlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(PostActivityPlugin.ID));
  }
  
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity
    makeActivity(demoIdentity, "demo post activity on activity stream of root");
    
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo postActivityNotification = list.get(0);
    
    //STEP 2 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(postActivityNotification.setTo("root"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, getFullName("demo") + " posted on your activity stream");
    assertBody(info, "New post on your activity stream");
    assertBody(info, "demo post activity on activity stream of root");
  }
  
  public void testDigest() throws Exception {
    //config setting of root to receive notification daily
    UserSetting rootSetting = new UserSetting();
    rootSetting.setUserId(rootIdentity.getRemoteId());
    rootSetting.setDailyPlugins(Arrays.asList(getPlugin().getId()));
    userSettingService.save(rootSetting);
    
    //create new user
    Identity ghostIdentity = identityManager.getOrCreateUserIdentity("ghost");
    notificationService.clearAll();
    
    makeActivity(demoIdentity, "demo post activity on activity stream of root");
    makeActivity(maryIdentity, "mary post activity on activity stream of root");
    makeActivity(johnIdentity, "john post activity on activity stream of root");
    makeActivity(ghostIdentity, "ghost post activity on activity stream of root");
    
    //Digest
    assertMadeMailDigestNotifications(4);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 4);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("demo") + ", " + getFullName("mary") + ", " + getFullName("john") + " and 1 others posted on your activity stream.");
    
    tearDownIdentityList.add(ghostIdentity);
  }
  
  public void testDigestWithSameUser() throws Exception {
    makeActivity(demoIdentity, "demo post activity on activity stream of root");
    makeActivity(demoIdentity, "mary post activity on activity stream of root");
    makeActivity(johnIdentity, "john post activity on activity stream of root");
    
    //Digest
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 3);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("demo") + ", " + getFullName("john") + " posted on your activity stream.");
  }

  public void testDigestWithDeletedActivity() throws Exception {
    ExoSocialActivity demoActivity = makeActivity(demoIdentity, "demo post activity on activity stream of root");
    makeActivity(johnIdentity, "john post activity on activity stream of root");
    
    //Digest
    assertMadeMailDigestNotifications(2);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 2);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    
    //demo delete his activity
    activityManager.deleteActivity(demoActivity);
    tearDownActivityList.remove(demoActivity);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("john") + " posted on your activity stream.");
  }
}
