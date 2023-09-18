package org.exoplatform.social.notification;

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
