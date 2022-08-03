package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;

public class SharedActivitySpaceStreamPlugin extends BaseNotificationPlugin {

    public static final String ID = "SharedActivitySpaceStreamPlugin";

  public SharedActivitySpaceStreamPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext notificationContext) {
    return false;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext notificationContext) {
    return null;
  }
}
