package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.notification.Utils;

public class SharedActivitySpaceStreamPlugin extends BaseNotificationPlugin {

  public static final String ID = "SharedActivitySpaceStreamPlugin";

  protected ActivityStorage activityStorage;

  public SharedActivitySpaceStreamPlugin(InitParams initParams , ActivityStorage activityStorage) {
    super(initParams);
    this.activityStorage = activityStorage;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext ctx) {
    try {
      ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
      String originalTitle = ctx.value(SocialNotificationUtils.ORIGINAL_TITLE);
      String titleShared = ctx.value(SocialNotificationUtils.ORIGINAL_TITLE_SHARED);
      Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
      String poster = Utils.getUserId(activity.getPosterId());

      return NotificationInfo.instance()
                             .key(getId())
                             .with(SocialNotificationUtils.POSTER.getKey(), poster)
                             .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                             .with(SocialNotificationUtils.ORIGINAL_TITLE.getKey(), originalTitle)
                             .with(SocialNotificationUtils.ORIGINAL_TITLE_SHARED.getKey(),titleShared)
                             .to(Utils.getDestinataires(activity, space)).end();
    } catch (Exception e) {
      ctx.setException(e);
    }
    return null;
  }
}
