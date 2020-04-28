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
package org.exoplatform.social.notification;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.notification.channel.MailTemplateProviderTest;
import org.exoplatform.social.notification.channel.WebTemplateProviderTest;
import org.exoplatform.social.notification.channel.template.*;

import org.exoplatform.social.notification.web.template.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  ActivityMentionWebBuilderTest.class,
  ActivityMentionMailBuilderTest.class,
  ActivityCommentMailBuilderTest.class,
  ActivityCommentReplyWebBuilderTest.class,
  ActivityCommentReplyMailBuilderTest.class,
  LikeCommentWebBuilderTest.class,
  PostActivityMailBuilderTest.class,
  NewUserMailBuilderTest.class,
  ReceiveRequestMailBuilderTest.class,
  RequestJoinSpaceMailBuilderTest.class,
  SpaceInvitationMailBuilderTest.class,
  LikeMailBuilderTest.class,
  LinkProviderUtilsTest.class,
  MailTemplateProviderTest.class,
  WebTemplateProviderTest.class,
  LikeWebBuilderTest.class,
  EditActivityMailBuilderTest.class,
  EditCommentMailBuilderTest.class,
  EditActivityWebBuilderTest.class,
  EditCommentWebBuilderTest.class
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
