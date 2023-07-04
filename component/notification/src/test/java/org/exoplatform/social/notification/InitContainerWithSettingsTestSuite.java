package org.exoplatform.social.notification;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;

import io.meeds.social.notification.upgrade.NotificationUpgradePluginTest;

@RunWith(Suite.class)
@SuiteClasses({
    NotificationUpgradePluginTest.class,
})
@ConfigTestCase(AbstractNotificationCoreTest.class)
public class InitContainerWithSettingsTestSuite extends BaseExoContainerTestSuite {

  @BeforeClass
  public static void setUp() throws Exception {
    initConfiguration(InitContainerWithSettingsTestSuite.class);
    beforeSetup();
  }

  @AfterClass
  public static void tearDown() {
    afterTearDown();
  }

}
