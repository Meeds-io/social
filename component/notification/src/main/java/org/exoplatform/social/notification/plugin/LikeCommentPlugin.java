package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification plugin for Comments Likes
 */
public class LikeCommentPlugin extends BaseNotificationPlugin {

  public LikeCommentPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "LikeCommentPlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);

    String[] likersId = activity.getLikeIdentityIds();
    String liker = Utils.getUserId(likersId[likersId.length - 1]);

    String likeTo = Utils.getUserId(activity.getPosterId());
    String spaceId = !activity.isComment() ? activity.getSpaceId()
                                           : Utils.getActivityManager().getParentActivity(activity).getSpaceId();
    boolean isMember = false;

    if (spaceId != null) {
      SpaceService spaceService = Utils.getSpaceService();
      Space space = spaceService.getSpaceById(spaceId);
      isMember = spaceService.isMember(space, likeTo);
    }

    if (spaceId != null && !isMember) {
      return null;
    }
    return NotificationInfo.instance()
                           .to(likeTo)
                           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                           .with(SocialNotificationUtils.LIKER.getKey(), liker)
                           .key(getId())
                           .end();
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    String[] likersId = activity.getLikeIdentityIds();
    if (activity.getPosterId().equals(likersId[likersId.length - 1])) {
      return false;
    }
    return true;
  }
}
