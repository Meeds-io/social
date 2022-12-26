package org.exoplatform.social.core.branding.plugins;

import org.exoplatform.commons.api.settings.FeaturePlugin;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

public class TopBarNavigationPlugin extends FeaturePlugin {

  public static final Scope    APP_SCOPE                       = Scope.APPLICATION.id("topBarNavigation");

  public static final String   TOP_BAR_NAVIGATION_FEATURE_NAME = "topBarNavigationEnabled";

  private final SettingService settingService;

  public TopBarNavigationPlugin(SettingService settingService) {
    this.settingService = settingService;
  }

  @Override
  public boolean isFeatureActiveForUser(String featureName, String username) {
    SettingValue<?> userSettingValue = settingService.get(Context.GLOBAL, APP_SCOPE, featureName);
    return userSettingValue != null && userSettingValue.getValue().equals("true");
  }

  @Override
  public void init() {
    SettingValue<?> settingValue = settingService.get(Context.GLOBAL, APP_SCOPE, TOP_BAR_NAVIGATION_FEATURE_NAME);
    if (settingValue == null) {
      settingService.set(Context.GLOBAL, APP_SCOPE, TOP_BAR_NAVIGATION_FEATURE_NAME, SettingValue.create(true));
    }
  }
}
