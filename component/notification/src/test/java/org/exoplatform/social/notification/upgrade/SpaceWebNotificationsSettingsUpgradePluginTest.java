package org.exoplatform.social.notification.upgrade;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.junit.Test;

public class SpaceWebNotificationsSettingsUpgradePluginTest extends AbstractCoreTest {

  @Test
  public void testSpaceWebNotificationsSettingsUpgradePlugin() {
    InitParams initParams = new InitParams();

    ValueParam valueParam = new ValueParam();
    valueParam.setName("product.group.id");
    valueParam.setValue("org.exoplatform.social");
    initParams.addParameter(valueParam);

    SpaceWebNotificationsSettingsUpgradePlugin upgradePlugin =
                                                             new SpaceWebNotificationsSettingsUpgradePlugin(settingService,
                                                                                                            pluginSettingService,
                                                                                                            initParams);
    upgradePlugin.processUpgrade("", "");
    assertEquals(17, upgradePlugin.getPluginCount());
    assertEquals(17, upgradePlugin.getSuccessCount());
    assertEquals(0, upgradePlugin.getErrorCount());
  }

}
