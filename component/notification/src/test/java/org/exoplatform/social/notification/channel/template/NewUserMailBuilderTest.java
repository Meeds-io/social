/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.NewUserPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class NewUserMailBuilderTest extends AbstractPluginTest {
  private ChannelManager manager;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    manager = getService(ChannelManager.class);
    
    //By default the plugin and feature are active
    assertTrue(pluginSettingService.isActive(getPlugin().getId()));
    assertTrue(exoFeatureService.isActiveFeature("notification"));
    //
    notificationService.clearAll();
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  

  @Override
  public AbstractTemplateBuilder getTemplateBuilder() {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(NewUserPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(NewUserPlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(NewUserPlugin.ID));
  }
  
  public void testSimpleCase() throws Exception {
    //STEP 1 create new user
    createUser("user_1");
    // will sent 4 mails to 4 users existing.
    assertMadeMailDigestNotifications(4);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 4);
    NotificationInfo newUserNotification = list.get(0);
    //STEP 2 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(newUserNotification.setTo("mary"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "USER_1 gtn has joined eXo");
    assertBody(info, "New user on eXo");
    //
    removeUser("user_1");
  }
  
  public void testPluginONOFF() throws Exception {
    //by default the plugin is ON
    createUser("user_11");
    assertMadeMailDigestNotifications(4);
    assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    notificationService.clearAll();
    removeUser("user_11");
    //turn off the plugin
    turnOFF(getPlugin());
    
    createUser("user_12");
    assertMadeMailDigestNotifications(0);
    assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 0);
    removeUser("user_12");
    
    //turn on the plugin
    turnON(getPlugin());
    createUser("user_123");
    assertMadeMailDigestNotifications(4);
    assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    notificationService.clearAll();
    removeUser("user_123");
  }
  
  public void testFeatureONOFF() throws Exception {
    //by default the feature is ON
    createUser("user_13");
    assertMadeMailDigestNotifications(4);
    assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    notificationService.clearAll();
    removeUser("user_13");
    
    //turn off the feature
    turnFeatureOff();
    
    createUser("user_23");
    assertMadeMailDigestNotifications(0);
    assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 0);
    removeUser("user_23");
    //turn on the feature
    turnFeatureOn();
    //
    createUser("user_234");
    assertMadeMailDigestNotifications(4);
    assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    notificationService.clearAll();
    removeUser("user_234");
  }
  
  public void testDigest() throws Exception {
    createUser("user_111");
    createUser("user_222");
    createUser("user_333");
    
    //Digest
    // will sent 12 mails to 4 users existing.
    assertMadeMailDigestNotifications(12);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 3);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    // remove duplicate message
    list = new ArrayList<NotificationInfo>(new LinkedHashSet<NotificationInfo>(list));

    list.get(0).setTo(rootIdentity.getRemoteId());
    //
    removeUser("user_111");
    
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, "USER_222 gtn, USER_333 gtn have joined eXo.");
    removeUser("user_222");
    removeUser("user_333");
  }
  
  private void createUser(String userName) throws Exception {
    UserHandler userHandler = getUserHandler();
    User user = userHandler.createUserInstance(userName);
    user.setEmail(userName + "@plf.com");
    user.setFirstName(userName.toUpperCase());
    user.setLastName("gtn");
    user.setPassword("exo");
    //
    userHandler.createUser(user, true);
    //
    Identity identity = identityManager.getOrCreateIdentity("organization", userName, true);
    tearDownIdentityList.add(identity);
  }

  private UserHandler getUserHandler() {
    UserHandler userHandler = CommonsUtils.getService(OrganizationService.class).getUserHandler();
    return userHandler;
  }

  private void removeUser(String userName) throws Exception {
    UserHandler userHandler = getUserHandler();
    userHandler.removeUser(userName, true);
  }
}
