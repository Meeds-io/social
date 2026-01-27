/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.notification.plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.Utils;

import io.meeds.social.observe.plugin.ActivityOberverPlugin;
import io.meeds.social.observe.service.ObserverService;

public class ActivityCommentWatchPlugin extends ActivityCommentPlugin {

  public static final String  ID = "ActivityCommentWatchPlugin";

  private final ObserverService     observerService;

  private final IdentityManager     identityManager;

  private final UserACL             userAcl;

  public ActivityCommentWatchPlugin(ObserverService observerService,
                                    IdentityManager identityManager,
                                    UserACL userAcl,
                                    OrganizationService organizationService,
                                    InitParams initParams) {
    super(initParams);
    this.observerService = observerService;
    this.identityManager = identityManager;
    this.userAcl = userAcl;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Set<Long> receivers = new HashSet<>();
    ExoSocialActivity comment = ctx.value(SocialNotificationUtils.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);

    addWatchers(receivers, comment);
    addWatchers(receivers, activity);
    String poster = Utils.getUserId(comment.getUserId());

    Set<String> commentReceivers = new HashSet<>();
    getNotificationReceivers(activity, comment, false).forEach(commentReceivers::add);
    if (StringUtils.isNotBlank(comment.getParentCommentId())) {
      getNotificationReceivers(activity, comment, true).forEach(commentReceivers::add);
    }
    commentReceivers.add(poster);

    List<String> receiversList = receivers.stream()
                                          .map(this::getUsername)
                                          .filter(u -> !commentReceivers.contains(u))
                                          .filter(username -> {
                                            org.exoplatform.services.security.Identity aclIdentity = userAcl.getUserIdentity(username);
                                            return aclIdentity != null
                                                && Utils.getActivityManager().isActivityViewable(comment, aclIdentity);
                                          })
                                          .toList();
    if (receiversList.isEmpty()) {
      return null;
    } else {
      return NotificationInfo.instance()
                             .to(receiversList)
                             .setFrom(poster)
                             .setSpaceId(activity.getSpaceId() == null ? 0 : Long.parseLong(activity.getSpaceId()))
                             .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                             .with(SocialNotificationUtils.COMMENT_ID.getKey(), comment.getId())
                             .with(SocialNotificationUtils.POSTER.getKey(), poster)
                             .with(SocialNotificationUtils.WATCHED.getKey(), "true")
                             .key(getId());
    }
  }

  private void addWatchers(Set<Long> receivers, ExoSocialActivity activity) {
    if (activity != null) {
      observerService.getObserverIdentityIds(activity.getMetadataObjectType(), activity.getMetadataObjectId())
                     .forEach(receivers::add);
      observerService.getObserverIdentityIds(ActivityOberverPlugin.OBJECT_TYPE, activity.getId().replace("comment", ""))
                     .forEach(receivers::add);
    }
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity comment = ctx.value(SocialNotificationUtils.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);
    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateSpaceIdentity(activity.getStreamOwner());
    // if the space is not null and it's not the default activity of space, then
    // it's valid to make notification
    return spaceIdentity == null || !activity.getPosterId().equals(spaceIdentity.getId());
  }

  private String getUsername(long identityId) {
    Identity identity = identityManager.getIdentity(identityId);
    return identity == null ? null : identity.getRemoteId();
  }

}
