/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.notification.digest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

import io.meeds.commons.digest.model.DigestItem;
import io.meeds.commons.digest.model.DigestLine;
import io.meeds.commons.digest.plugin.DigestLineContext;
import io.meeds.commons.digest.plugin.DigestLinePlugin;

/**
 * The digest email lines of the notification types social owns: the space
 * life (invitations, join requests) and the feed (posts in my spaces, comments
 * on what I watch). Everything is read fresh from the stored ids: a space or
 * an activity that no longer exists gives no line.
 */
public class SocialDigestLinePlugin extends DigestLinePlugin {

  public static final String  SPACE_INVITATION_PLUGIN     = "SpaceInvitationPlugin";

  public static final String  REQUEST_JOIN_SPACE_PLUGIN   = "RequestJoinSpacePlugin";

  public static final String  POST_ACTIVITY_SPACE_PLUGIN  = "PostActivitySpaceStreamPlugin";

  public static final String  ACTIVITY_COMMENT_WATCH_PLUGIN = "ActivityCommentWatchPlugin";

  /** The stored parameters: the very keys the notification plugins write */
  static final String         SPACE_ID_PARAM              = SocialNotificationUtils.SPACE_ID.getKey();

  static final String         INVITER_PARAM               = SocialNotificationUtils.PROFILE.getKey();

  static final String         REQUESTER_PARAM             = SocialNotificationUtils.REQUEST_FROM.getKey();

  static final String         POSTER_PARAM                = SocialNotificationUtils.POSTER.getKey();

  static final String         ACTIVITY_ID_PARAM           = SocialNotificationUtils.ACTIVITY_ID.getKey();

  private static final String LINE_KEY_PREFIX             = "digest.line.";

  private static final int    TITLE_MAX_LENGTH            = 100;

  private SpaceService        spaceService;

  private ActivityManager     activityManager;

  private IdentityManager     identityManager;

  public SocialDigestLinePlugin(InitParams params) {
    super(params);
  }

  SocialDigestLinePlugin(InitParams params,
                         SpaceService spaceService,
                         ActivityManager activityManager,
                         IdentityManager identityManager) {
    super(params);
    this.spaceService = spaceService;
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  @Override
  public DigestLine buildLine(DigestItem item, DigestLineContext context) {
    return switch (item.getPluginId()) {
      case SPACE_INVITATION_PLUGIN -> spaceLine(item, item.getParam(INVITER_PARAM), "space");
      case REQUEST_JOIN_SPACE_PLUGIN -> spaceLine(item, item.getParam(REQUESTER_PARAM), "space_members");
      case POST_ACTIVITY_SPACE_PLUGIN -> activityLine(item, true);
      case ACTIVITY_COMMENT_WATCH_PLUGIN -> activityLine(item, false);
      default -> null;
    };
  }

  /** "{actor} invited you to join {space}" / "{actor} requested to join {space}" */
  private DigestLine spaceLine(DigestItem item, String actor, String redirectType) {
    Space space = findSpace(item.getParam(SPACE_ID_PARAM));
    if (space == null) {
      return null;
    }
    return DigestLine.of(LINE_KEY_PREFIX + item.getPluginId(), fullName(actor), space.getDisplayName())
                     .withUrl(redirectUrl(redirectType, space.getId()));
  }

  /**
   * "{actor} posted "{title}" in {space}" / "{actor} commented on "{title}"
   * you're watching"
   */
  private DigestLine activityLine(DigestItem item, boolean withSpace) {
    String activityId = item.getParam(ACTIVITY_ID_PARAM);
    ExoSocialActivity activity = StringUtils.isBlank(activityId) ? null : getActivityManager().getActivity(activityId);
    if (activity == null) {
      return null;
    }
    String actor = fullName(item.getParam(POSTER_PARAM));
    String title = cleanTitle(activity.getTitle());
    String url = redirectUrl("view_full_activity", activityId);
    if (withSpace) {
      Space space = findSpace(activity.getSpaceId());
      return DigestLine.of(LINE_KEY_PREFIX + item.getPluginId(), actor, title, space == null ? "" : space.getDisplayName())
                       .withUrl(url);
    }
    return DigestLine.of(LINE_KEY_PREFIX + item.getPluginId(), actor, title).withUrl(url);
  }

  private Space findSpace(String spaceId) {
    return StringUtils.isBlank(spaceId) ? null : getSpaceService().getSpaceById(spaceId);
  }

  private String fullName(String username) {
    if (StringUtils.isBlank(username)) {
      return "";
    }
    Identity identity = getIdentityManager().getOrCreateUserIdentity(username);
    String fullName = identity == null || identity.getProfile() == null ? null : identity.getProfile().getFullName();
    return StringUtils.isBlank(fullName) ? username : fullName;
  }

  /**
   * An activity title is stored as HTML: the email line wants its text, short
   */
  static String cleanTitle(String title) {
    if (StringUtils.isBlank(title)) {
      return "";
    }
    String text = StringEscapeUtils.unescapeHtml4(title.replaceAll("<[^>]+>", " "));
    text = StringUtils.normalizeSpace(text);
    return StringUtils.abbreviate(text, TITLE_MAX_LENGTH);
  }

  /**
   * The same redirection links as the instant notification emails, resolved
   * by the platform at click time
   */
  protected String redirectUrl(String type, String objectId) {
    return LinkProviderUtils.getRedirectUrl(type, objectId);
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  private ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = ExoContainerContext.getService(ActivityManager.class);
    }
    return activityManager;
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }

}
