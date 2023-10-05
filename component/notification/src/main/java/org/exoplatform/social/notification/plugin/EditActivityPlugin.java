package org.exoplatform.social.notification.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.Utils;

public class EditActivityPlugin extends BaseNotificationPlugin {

  public EditActivityPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "EditActivityPlugin";

  @Override
  public String getId() {
    return ID;
  }

  protected boolean isSubComment = false;

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    String spaceId = activity.getSpaceId();
    Set<String> receivers = new HashSet<>();
    if (activity.getStreamOwner() != null) {
      Utils.sendToStreamOwner(receivers, activity.getStreamOwner(), activity.getPosterId());
    }
    // Send notification to all others users who have comment on this activity
    Utils.sendToCommeters(receivers, activity.getCommentedIds(), activity.getPosterId(), spaceId);
    Utils.sendToActivityPoster(receivers, activity.getPosterId(), activity.getPosterId(), spaceId);

    String poster = Utils.getUserId(activity.getUserId());
    return NotificationInfo.instance()
                           .to(new ArrayList<>(receivers))
                           .setFrom(poster)
                           .setSpaceId(spaceId == null ? 0 : Long.parseLong(spaceId))
                           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                           .with(SocialNotificationUtils.POSTER.getKey(), poster)
                           .key(getId());
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateSpaceIdentity(activity.getStreamOwner());
    // if the space is not null and it's not the default activity of space, then
    // it's valid to make notification
    return spaceIdentity == null || !activity.getPosterId().equals(spaceIdentity.getId());
  }

}
