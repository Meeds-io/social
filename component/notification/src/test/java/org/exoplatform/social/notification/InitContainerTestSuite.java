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
package org.exoplatform.social.notification;

import io.meeds.social.notification.listener.SpaceMembershipNotificationListenerTest;
import io.meeds.social.notification.plugin.JoinedSpaceByInvitationLinkPluginTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.notification.channel.MailTemplateProviderTest;
import org.exoplatform.social.notification.channel.template.ActivityCommentMailBuilderTest;
import org.exoplatform.social.notification.channel.template.ActivityCommentReplyMailBuilderTest;
import org.exoplatform.social.notification.channel.template.ActivityMentionMailBuilderTest;
import org.exoplatform.social.notification.channel.template.EditActivityMailBuilderTest;
import org.exoplatform.social.notification.channel.template.EditCommentMailBuilderTest;
import org.exoplatform.social.notification.channel.template.LikeCommentMailBuilderTest;
import org.exoplatform.social.notification.channel.template.LikeMailBuilderTest;
import org.exoplatform.social.notification.channel.template.NewUserMailBuilderTest;
import org.exoplatform.social.notification.channel.template.PostActivityMailBuilderTest;
import org.exoplatform.social.notification.channel.template.ReceiveRequestMailBuilderTest;
import org.exoplatform.social.notification.channel.template.RequestJoinSpaceMailBuilderTest;
import org.exoplatform.social.notification.channel.template.SpaceInvitationMailBuilderTest;
import org.exoplatform.social.notification.impl.SpaceWebNotificationServiceTest;
import org.exoplatform.social.notification.plugin.SocialNotificationUtilsTest;

import io.meeds.social.security.plugin.EmailOtpPluginTest;

@RunWith(Suite.class)
@SuiteClasses({
  ActivityMentionMailBuilderTest.class,
  ActivityCommentMailBuilderTest.class,
  ActivityCommentReplyMailBuilderTest.class,
  PostActivityMailBuilderTest.class,
  NewUserMailBuilderTest.class,
  ReceiveRequestMailBuilderTest.class,
  RequestJoinSpaceMailBuilderTest.class,
  SpaceInvitationMailBuilderTest.class,
  LikeMailBuilderTest.class,
  LikeCommentMailBuilderTest.class,
  LinkProviderUtilsTest.class,
  MailTemplateProviderTest.class,
  EditActivityMailBuilderTest.class,
  EditCommentMailBuilderTest.class,
  SpaceWebNotificationServiceTest.class,
  SocialNotificationUtilsTest.class,
  UtilsTestCase.class,
  EmailOtpPluginTest.class,
  SpaceMembershipNotificationListenerTest.class,
  JoinedSpaceByInvitationLinkPluginTest.class
})
@ConfigTestCase(AbstractCoreTest.class)
public class InitContainerTestSuite extends BaseExoContainerTestSuite {
  
  @BeforeClass
  public static void setUp() throws Exception {
    initConfiguration(InitContainerTestSuite.class);
    beforeSetup();
  }

  @AfterClass
  public static void tearDown() {
    afterTearDown();
  }
}
