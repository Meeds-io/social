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

import java.io.StringWriter;
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

  public void testMakeDigestWithOneRequester() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(List.of(makeNotification(maryIdentity)));
    StringWriter writer = new StringWriter();

    assertTrue(builder.buildDigest(ctx, writer));

    String digest = writer.toString();
    assertTrue(digest, digest.contains(maryIdentity.getProfile().getFullName()));
    assertTrue(digest, digest.contains("has requested to deactivate their account"));
  }

  public void testMakeDigestDeduplicatesSameRequester() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    // the same account deactivated, reactivated and deactivated again within
    // one digest period counts once
    ctx.setNotificationInfos(Arrays.asList(makeNotification(maryIdentity), makeNotification(maryIdentity)));
    StringWriter writer = new StringWriter();

    assertTrue(builder.buildDigest(ctx, writer));

    String digest = writer.toString();
    assertTrue(digest, digest.contains("has requested to deactivate their account"));
  }

  public void testMakeDigestWithFewRequesters() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(Arrays.asList(makeNotification(maryIdentity), makeNotification(johnIdentity)));
    StringWriter writer = new StringWriter();

    assertTrue(builder.buildDigest(ctx, writer));

    String digest = writer.toString();
    assertTrue(digest, digest.contains(maryIdentity.getProfile().getFullName()));
    assertTrue(digest, digest.contains(johnIdentity.getProfile().getFullName()));
    assertTrue(digest, digest.contains("have requested to deactivate their accounts"));
  }

  public void testMakeDigestWithManyRequesters() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(Arrays.asList(makeNotification(maryIdentity),
                                           makeNotification(johnIdentity),
                                           makeNotification(demoIdentity),
                                           makeNotification(rootIdentity)));
    StringWriter writer = new StringWriter();

    assertTrue(builder.buildDigest(ctx, writer));

    String digest = writer.toString();
    assertTrue(digest, digest.contains(maryIdentity.getProfile().getFullName()));
    assertTrue(digest, digest.contains(johnIdentity.getProfile().getFullName()));
    assertTrue(digest, digest.contains(demoIdentity.getProfile().getFullName()));
    assertFalse(digest, digest.contains(rootIdentity.getProfile().getFullName()));
    assertTrue(digest, digest.contains("more have requested to deactivate their accounts"));
  }

  private NotificationInfo makeNotification(Identity requesterIdentity) {
    return NotificationInfo.instance()
                           .key(AccountDeactivationRequestPlugin.ID)
                           .with(SocialNotificationUtils.REMOTE_ID.getKey(), requesterIdentity.getRemoteId())
                           .setFrom(requesterIdentity.getRemoteId())
                           .setTo(rootIdentity.getRemoteId());
  }
}
