/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.exoplatform.social.notification.channel.template;


import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.plugin.AccountDeactivationRequestPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class AccountDeactivationRequestMailBuilderTest extends AbstractCoreTest {

  private AbstractTemplateBuilder builder;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    ChannelManager manager = getService(ChannelManager.class);
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
    assertNotNull(channel);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(AccountDeactivationRequestPlugin.ID)));
    builder = channel.getTemplateBuilder(PluginKey.key(AccountDeactivationRequestPlugin.ID));
  }

  public void testMakeMessage() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(makeNotification(maryIdentity).setTo(rootIdentity.getRemoteId()));

    MessageInfo message = builder.buildMessage(ctx);

    assertNotNull(message);
    String requesterFullName = maryIdentity.getProfile().getFullName();
    assertEquals("Account Deactivation Request by " + requesterFullName, message.getSubject());
    assertTrue(message.getBody().contains(requesterFullName));
    assertTrue(message.getBody().contains("/portal/administration/home/organisation/users?status=DISABLED"));
  }

  private NotificationInfo makeNotification(Identity requesterIdentity) {
    return NotificationInfo.instance()
                           .key(AccountDeactivationRequestPlugin.ID)
                           .with(SocialNotificationUtils.REMOTE_ID.getKey(), requesterIdentity.getRemoteId())
                           .setFrom(requesterIdentity.getRemoteId())
                           .setTo(rootIdentity.getRemoteId());
  }
}
