package org.exoplatform.social.notification.upgrade;

import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

public class SpaceWebNotificationsSettingsUpgradePlugin extends UpgradeProductPlugin {
  private static final Log     LOG = ExoLogger.getLogger(SpaceWebNotificationsSettingsUpgradePlugin.class);

  private PluginSettingService pluginSettingService;

  public SpaceWebNotificationsSettingsUpgradePlugin(SettingService settingService,
                                                    PluginSettingService pluginSettingService,
                                                    InitParams initParams) {
    super(settingService, initParams);
    this.pluginSettingService = pluginSettingService;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    List<PluginInfo> plugins = pluginSettingService.getAllPlugins();
    for (PluginInfo pluginInfo : plugins) {
      String pluginInfoType = pluginInfo.getType();
      try {
        LOG.info("=== Start initialisation of {} settings", pluginInfoType);
        long startTime = System.currentTimeMillis();
        pluginSettingService.saveActivePlugin("SPACE_WEB_CHANNEL", pluginInfoType, true);
        long endTime = System.currentTimeMillis();
        LOG.info("{} Notifications settings initialised in {} ms", pluginInfoType, (endTime - startTime));
      } catch (Exception e) {
        LOG.error("Error while initialisation of {} Notifications settings - Cause :", pluginInfoType, e.getMessage(), e);
      }
      LOG.info("=== End initialisation of {} Notifications settings", pluginInfoType);
    }
  }

}
