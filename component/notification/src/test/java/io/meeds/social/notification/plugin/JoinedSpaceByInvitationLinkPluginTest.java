/*
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
package io.meeds.social.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import io.meeds.social.notification.util.NotificationUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JoinedSpaceByInvitationLinkPluginTest {

  @Mock
  private InitParams initParams;

  private JoinedSpaceByInvitationLinkPlugin plugin;

  @Before
  public void setUp() {
		plugin = new JoinedSpaceByInvitationLinkPlugin(initParams);
	}

  @Test
  public void testGetId() {
		assertEquals(JoinedSpaceByInvitationLinkPlugin.ID, plugin.getId());
	}

  @Test
  public void testIsValid() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    assertTrue(plugin.isValid(ctx));
  }

  @Test
  public void testMakeNotification() {

    NotificationContext ctx = NotificationContextImpl.cloneInstance();

    ctx.append(NotificationUtils.INVITED_USER, "John Doe");
    ctx.append(NotificationUtils.INVITED_USER_ID, "user1");
    ctx.append(NotificationUtils.INVITER_ID, "inviter1");
    ctx.append(NotificationUtils.SPACE_ID, "100");
    ctx.append(NotificationUtils.SPACE_AVATAR_URL, "/avatar.png");
    ctx.append(NotificationUtils.SPACE_DISPLAY_NAME, "Test Space");

    NotificationInfo info = plugin.makeNotification(ctx);

    assertEquals("John Doe", info.getValueOwnerParameter(NotificationUtils.INVITED_USER.getKey()));
    assertEquals("inviter1", info.getValueOwnerParameter(NotificationUtils.INVITER_ID.getKey()));
    assertEquals("100", info.getValueOwnerParameter(NotificationUtils.SPACE_ID.getKey()));
    assertEquals("/avatar.png", info.getValueOwnerParameter(NotificationUtils.SPACE_AVATAR_URL.getKey()));
    assertEquals("Test Space", info.getValueOwnerParameter(NotificationUtils.SPACE_DISPLAY_NAME.getKey()));

    assertEquals(plugin.getKey(), info.getKey());

    assertEquals("user1", info.getFrom());
    assertEquals(Collections.singletonList("inviter1"), info.getSendToUserIds());
  }
}
