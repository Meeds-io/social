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
package org.exoplatform.social.notification.channel.template;

import java.util.Locale;

import io.meeds.social.notification.plugin.JoinedSpaceByInvitationLinkPlugin;
import io.meeds.social.notification.util.NotificationUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.*;
import org.jsoup.Jsoup;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = ActivityReplyToCommentPlugin.ID, template = "war:/notification/templates/ActivityReplyToCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityCommentPlugin.ID, template = "war:/notification/templates/ActivityCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityCommentWatchPlugin.ID, template = "war:/notification/templates/ActivityCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = EditActivityPlugin.ID, template = "war:/notification/templates/EditActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = EditCommentPlugin.ID, template = "war:/notification/templates/EditCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityMentionPlugin.ID, template = "war:/notification/templates/ActivityMentionPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikePlugin.ID, template = "war:/notification/templates/LikePlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeCommentPlugin.ID, template = "war:/notification/templates/LikeCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = NewUserPlugin.ID, template = "war:/notification/templates/NewUserPlugin.gtmpl"),
    @TemplateConfig(pluginId = AccountDeactivationRequestPlugin.ID, template = "war:/notification/templates/AccountDeactivationRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivityPlugin.ID, template = "war:/notification/templates/PostActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivitySpaceStreamPlugin.ID, template = "war:/notification/templates/PostActivitySpaceStreamPlugin.gtmpl"),
    @TemplateConfig(pluginId = RelationshipReceivedRequestPlugin.ID, template = "war:/notification/templates/RelationshipReceivedRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = SharedActivitySpaceStreamPlugin.ID, template = "war:/notification/templates/SharedActivitySpaceStreamPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestJoinSpacePlugin.ID, template = "war:/notification/templates/RequestJoinSpacePlugin.gtmpl"),
    @TemplateConfig(pluginId = SpaceInvitationPlugin.ID, template = "war:/notification/templates/SpaceInvitationPlugin.gtmpl"),
    @TemplateConfig(pluginId = JoinedSpaceByInvitationLinkPlugin.ID, template = "war:/notification/templates/JoinedSpaceByInvitationLinkPlugin.gtmpl")})

public class MailTemplateProvider extends TemplateProvider {

  private static final Log LOG = ExoLogger.getLogger(MailTemplateProvider.class);

  /** Defines the template builder for ActivityReplyToCommentPlugin*/
  private AbstractTemplateBuilder replyToComment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      String commentId = notification.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      ExoSocialActivity commentActivity = Utils.getActivityManager().getActivity(commentId);
      ExoSocialActivity parentCommentActivity = Utils.getActivityManager().getActivity(commentActivity.getParentCommentId());
      Identity identity = Utils.getIdentityManager().getIdentity(commentActivity.getPosterId(), true);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      templateContext.put("USER", Utils.addExternalFlag(identity));
      String subject = TemplateUtils.processSubject(templateContext);

      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String subCommentTitle = SocialNotificationUtils.processImageTitle(getActivityTitle(commentActivity, language), imagePlaceHolder);
      String commentTitle = SocialNotificationUtils.processImageTitle(getActivityTitle(parentCommentActivity, language), imagePlaceHolder);
      templateContext.put("COMMENT_REPLY", processBody(subCommentTitle, language));
      templateContext.put("COMMENT", processBody(commentTitle, language));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment_reply", activity.getId() + "-" + parentCommentActivity.getId() + "-" + commentActivity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment_reply", activity.getId() + "-" + parentCommentActivity.getId() + "-" + commentActivity.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, parentCommentActivity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

  };
  
  /** Defines the template builder for ActivityCommentPlugin*/
  private AbstractTemplateBuilder comment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      String commentId = notification.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      ExoSocialActivity comment = null;
      if (StringUtils.isNotBlank(commentId)) {
        comment = Utils.getActivityManager().getActivity(commentId);
      }
      if (activity == null) {
        LOG.debug("Activity with id '{}' was removed but the notification with id'{}' is remaining", activityId, notification.getId());
        return null;
      }
      if(activity.isComment()) {
        comment = Utils.getActivityManager().getParentActivity(activity);
      }
      if (comment == null) {
        LOG.debug("Comment of activity with id '{}' was removed but the notification with id'{}' is remaining", commentId, notification.getId());
        return null;
      }
      Identity identity = Utils.getIdentityManager().getIdentity(comment.getPosterId(), true);

      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      templateContext.put("USER", Utils.addExternalFlag(identity));
      String subject = TemplateUtils.processSubject(templateContext);

      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));

      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(getActivityTitle(comment, language), imagePlaceHolder);
      templateContext.put("COMMENT", processBody(title, language));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(comment));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", activity.getId() + "-" + comment.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activity.getId() + "-" + comment.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for EditCommentPlugin*/
  private AbstractTemplateBuilder editComment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
        MessageInfo messageInfo = new MessageInfo();
        NotificationInfo notification = ctx.getNotificationInfo();
        String language = getLanguage(notification);

        String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        String commentId = notification.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        ExoSocialActivity comment = null;
        if (StringUtils.isNotBlank(commentId)) {
            comment = Utils.getActivityManager().getActivity(commentId);
        }
        if (activity == null) {
            LOG.debug("Activity with id '{}' was removed but the notification with id'{}' is remaining", activityId, notification.getId());
            return null;
        }
        if(activity.isComment()) {
            comment = Utils.getActivityManager().getParentActivity(activity);
        }
        if (comment == null) {
            LOG.debug("Comment of activity with id '{}' was removed but the notification with id'{}' is remaining", commentId, notification.getId());
            return null;
        }
        Identity identity = Utils.getIdentityManager().getIdentity(comment.getPosterId(), true);

        TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
        templateContext.put("USER", Utils.addExternalFlag(identity));
        String subject = TemplateUtils.processSubject(templateContext);

        SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
        templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));

        String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
        String title = SocialNotificationUtils.processImageTitle(getActivityTitle(comment, language), imagePlaceHolder);
        templateContext.put("COMMENT", processBody(title, language));
        templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(comment));
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", activity.getId() + "-" + comment.getId()));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activity.getId() + "-" + comment.getId()));

        String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
        //binding the exception throws by processing template
        ctx.setException(templateContext.getException());
        return messageInfo.subject(subject).body(body).end();
    }

  };

    /** Defines the template builder for EditActivityPlugin*/
    private AbstractTemplateBuilder editActivity = new AbstractTemplateBuilder() {
        @Override
        protected MessageInfo makeMessage(NotificationContext ctx) {
            MessageInfo messageInfo = new MessageInfo();
            NotificationInfo notification = ctx.getNotificationInfo();
            String language = getLanguage(notification);

            String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
            ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
            if (activity == null) {
                LOG.debug("Activity with id '{}' was removed but the notification with id'{}' is remaining", activityId, notification.getId());
                return null;
            }

            Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

            TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
            templateContext.put("USER", Utils.addExternalFlag(identity));
            String subject = TemplateUtils.processSubject(templateContext);

            SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
            templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));

            String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
            String title = SocialNotificationUtils.processImageTitle(getActivityTitle(activity, language), imagePlaceHolder);
            templateContext.put("COMMENT", processBody(title, language));
            templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
            templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activityId));
            templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityId));

            String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
            //binding the exception throws by processing template
            ctx.setException(templateContext.getException());
            return messageInfo.subject(subject).body(body).end();
        }

    };

  /** Defines the template builder for ActivityMentionPlugin*/
  private AbstractTemplateBuilder mention = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

      templateContext.put("USER", Utils.addExternalFlag(identity));
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      String body = "";

      // In case of mention on a comment, we need provide the id of the activity, not of the comment
      ExoSocialActivity i18nActivity = getI18N(activity,new Locale(language));
      if (activity.isComment()) {
        ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
        activityId = parentActivity.getId();
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", activityId + "-" + activity.getId()));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activityId + "-" + activity.getId()));
        String title = getActivityTitle(i18nActivity, language);
        String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
        title = SocialNotificationUtils.processImageTitle(title, imagePlaceHolder);
        templateContext.put("ACTIVITY", processBody(title, language));
        body = TemplateUtils.processGroovy(templateContext);
      } else {
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activityId));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityId));
        body = SocialNotificationUtils.getBody(ctx, templateContext, i18nActivity);
      }

      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for LikePlugin*/
  private AbstractTemplateBuilder like = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("likersId"), true);
      if (identity == null) {
        return null;
      }

      templateContext.put("USER", Utils.addExternalFlag(identity));
      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(getActivityTitle(activity, language), imagePlaceHolder);
      String cleanedTitle = StringEscapeUtils.unescapeHtml4(title);
      templateContext.put("SUBJECT", cleanedTitle);
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);

      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for LikeCommentPlugin*/
  private AbstractTemplateBuilder likeComment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("likersId"), true);
      if (identity == null) {
        return null;
      }

      templateContext.put("USER", Utils.addExternalFlag(identity));
      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(getActivityTitle(activity, language), imagePlaceHolder);
      String cleanedTitle = StringEscapeUtils.unescapeHtml4(title);
      templateContext.put("SUBJECT", cleanedTitle);
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      ExoSocialActivity activityOfComment = Utils.getActivityManager().getParentActivity(activity);
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activityOfComment.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityOfComment.getId()));
      ExoSocialActivity i18nComment = getI18N(activity, new Locale(language));
      String commentTitle = getActivityTitle(i18nComment, language);
      commentTitle = SocialNotificationUtils.processImageTitle(commentTitle, imagePlaceHolder);
      templateContext.put("ACTIVITY", processBody(commentTitle, language));
      String body = TemplateUtils.processGroovy(templateContext);

      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for AccountDeactivationRequestPlugin */
  private AbstractTemplateBuilder accountDeactivationRequest = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
      if (identity == null) {
        return null;
      }

      templateContext.put("USER", identity.getProfile().getFullName());
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("USERS_MANAGEMENT_URL",
                          CommonsUtils.getCurrentDomain() + "/portal/administration/home/organisation/users?status=DISABLED");
      String body = TemplateUtils.processGroovy(templateContext);
      ctx.setException(templateContext.getException());

      return new MessageInfo().subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for NewUserPlugin*/
  private AbstractTemplateBuilder newUser = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
      if (identity == null) {
        return null;
      }
      Profile userProfile = identity.getProfile();

      templateContext.put("USER", Utils.addExternalFlag(identity));
      templateContext.put("PORTAL_NAME", NotificationPluginUtils.getBrandingPortalName());
      templateContext.put("PORTAL_HOME", org.exoplatform.commons.notification.NotificationUtils.getPortalHome(NotificationPluginUtils.getBrandingPortalName()));
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("CONNECT_ACTION_URL", LinkProviderUtils.getInviteToConnectUrl(identity.getRemoteId(), notification.getTo()));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for PostActivityPlugin*/
  private AbstractTemplateBuilder postActivity = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);


      templateContext.put("USER", Utils.addExternalFlag(identity));
      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(getActivityTitle(activity, language), imagePlaceHolder);
      templateContext.put("SUBJECT", title);
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for PostActivitySpaceStreamPlugin*/
  private AbstractTemplateBuilder postActivitySpace = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      String title = getActivityTitle(activity, language);
      String cleanedTitle = StringEscapeUtils.unescapeHtml4(title);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

      Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
      if (spaceIdentity == null) {
        return null;
      }
      Space space = Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());
      if (space == null) {
        return null;
      }

      templateContext.put("USER", Utils.addExternalFlag(identity));
      templateContext.put("SPACE", space.getDisplayName());

      templateContext.put("SUBJECT", cleanedTitle);
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("SPACE_URL", getSpaceUrl(space.getId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for PostActivitySpaceStreamPlugin*/
  private AbstractTemplateBuilder shareActivitySpace = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      String originalTitle = notification.getValueOwnerParameter(SocialNotificationUtils.ORIGINAL_TITLE.getKey());
      originalTitle = MentionUtils.substituteRoleWithLocale(originalTitle, Locale.forLanguageTag(language));
      String sharedTitle = Jsoup.parse(notification.getValueOwnerParameter(SocialNotificationUtils.ORIGINAL_TITLE_SHARED.getKey())).text();
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId());

      Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner());
      if (spaceIdentity == null) {
        return null;
      }
      Space space = Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());
      if (space == null) {
        return null;
      }
      templateContext.put("USER", Utils.addExternalFlag(identity));
      templateContext.put("SPACE", space.getDisplayName());
      templateContext.put("SHORT_TITLE", sharedTitle.substring(0, sharedTitle.length() < 10 ? sharedTitle.length() :   sharedTitle.length()/2 ) + "...");
      templateContext.put("SUBJECT", originalTitle);
      templateContext.put("TITLE", sharedTitle);
      templateContext.put("IMAGE", CommonsUtils.getCurrentDomain() +"/news/images/news.png");
      templateContext.put("SPACE_URL", getSpaceUrl(space.getId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
      String subject = TemplateUtils.processSubject(templateContext);
      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      MessageInfo messageInfo = new MessageInfo();

      return messageInfo.subject(subject).body(body).end();
    }

  };


  /** Defines the template builder for RelationshipReceivedRequestPlugin*/
  private AbstractTemplateBuilder relationshipReceived = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String sender = notification.getValueOwnerParameter("sender");
      String toUser = notification.getTo();
      SocialNotificationUtils.addFooterAndFirstName(toUser, templateContext);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
      Profile userProfile = identity.getProfile();

      templateContext.put("PORTAL_NAME", NotificationPluginUtils.getBrandingPortalName());
      templateContext.put("USER", Utils.addExternalFlag(identity));
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getConfirmInvitationToConnectUrl(sender, toUser));
      templateContext.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToConnectUrl(sender, toUser));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for RequestJoinSpacePlugin*/
  private AbstractTemplateBuilder requestJoinSpace = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
      Space space = Utils.getSpaceService().getSpaceById(spaceId);
      if (space == null) {
        return null;
      }
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("request_from"), true);
      if (identity == null) {
        return null;
      }
      Profile userProfile = identity.getProfile();

      templateContext.put("SPACE", space.getDisplayName());
      templateContext.put("USER", Utils.addExternalFlag(identity));
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space_members", space.getId()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("VALIDATE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
      templateContext.put("REFUSE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRefuseRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

  };

  /** Defines the template builder for SpaceInvitationPlugin*/
  private AbstractTemplateBuilder spaceInvitation = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
      Space space = Utils.getSpaceService().getSpaceById(spaceId);
      if (space == null) {
        return null;
      }

      templateContext.put("SPACE", space.getDisplayName());
      templateContext.put("SPACE_URL", getSpaceUrl(spaceId));
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("SPACE_AVATAR", LinkProviderUtils.getSpaceAvatarUrl(space));
      templateContext.put("ACCEPT_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(space.getId(), notification.getTo()));
      templateContext.put("REFUSE_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(space.getId(), notification.getTo()));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

  };

  private class JoinedSpaceByInvitationLinkTemplateBuilder extends AbstractTemplateBuilder {

  @Override
  protected MessageInfo makeMessage(NotificationContext notificationContext) {
    NotificationInfo notificationInfo = notificationContext.getNotificationInfo();
    String pluginId = notificationInfo.getKey().getId();
    HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();
    String invitedUserDisplayName = notificationInfo.getValueOwnerParameter(NotificationUtils.INVITED_USER.getKey());
    String inviterId = notificationInfo.getValueOwnerParameter(NotificationUtils.INVITER_ID.getKey());
    String spaceId = notificationInfo.getValueOwnerParameter(NotificationUtils.SPACE_ID.getKey());
    String spaceDisplayName = notificationInfo.getValueOwnerParameter(NotificationUtils.SPACE_DISPLAY_NAME.getKey());
    String spaceAvatarUrl = notificationInfo.getValueOwnerParameter(NotificationUtils.SPACE_AVATAR_URL.getKey());
    Identity inviterIdentity = Utils.getIdentityManager().getOrCreateUserIdentity(inviterId);
    String language = getLanguage(notificationInfo);
    TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);
    SocialNotificationUtils.addFooterAndFirstName(notificationInfo.getTo(), templateContext);


    templateContext.put("INVITED_USER_DISPLAY_NAME", encoder.encode(invitedUserDisplayName));
    templateContext.put("SPACE_DISPLAY_NAME", encoder.encode(spaceDisplayName));
    templateContext.put("SPACE_MEMBERS_URL", "/portal/s/" + encoder.encode(spaceId) + "/members");
    templateContext.put("SPACE_AVATAR_URL", encoder.encode(spaceAvatarUrl));
    templateContext.put("USER", Utils.addExternalFlag(inviterIdentity));
    templateContext.put("AVATAR", CommonsUtils.getCurrentDomain() + spaceAvatarUrl);

    String subject = TemplateUtils.processSubject(templateContext);
    String body = TemplateUtils.processGroovy(templateContext);
    notificationContext.setException(templateContext.getException());
    MessageInfo messageInfo = new MessageInfo();
    return messageInfo.subject(subject).body(body).end();
  }

  }

  protected ExoSocialActivity getI18N(ExoSocialActivity activity,Locale locale) {

    I18NActivityProcessor i18NActivityProcessor =(I18NActivityProcessor) PortalContainer.getInstance().getComponentInstanceOfType(I18NActivityProcessor.class);
    if (activity.getTitleId() != null) {
      activity = i18NActivityProcessor.process(activity, locale);
    }
    return activity;
  }

  public MailTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(ActivityCommentPlugin.ID), comment);
    this.templateBuilders.put(PluginKey.key(ActivityCommentWatchPlugin.ID), comment);
    this.templateBuilders.put(PluginKey.key(EditCommentPlugin.ID), editComment);
    this.templateBuilders.put(PluginKey.key(EditActivityPlugin.ID), editActivity);
    this.templateBuilders.put(PluginKey.key(ActivityReplyToCommentPlugin.ID), replyToComment);
    this.templateBuilders.put(PluginKey.key(ActivityMentionPlugin.ID), mention);
    this.templateBuilders.put(PluginKey.key(LikePlugin.ID), like);
    this.templateBuilders.put(PluginKey.key(LikeCommentPlugin.ID), likeComment);
    this.templateBuilders.put(PluginKey.key(NewUserPlugin.ID), newUser);
    this.templateBuilders.put(PluginKey.key(AccountDeactivationRequestPlugin.ID), accountDeactivationRequest);
    this.templateBuilders.put(PluginKey.key(PostActivityPlugin.ID), postActivity);
    this.templateBuilders.put(PluginKey.key(PostActivitySpaceStreamPlugin.ID), postActivitySpace);
    this.templateBuilders.put(PluginKey.key(SharedActivitySpaceStreamPlugin.ID), shareActivitySpace);
    this.templateBuilders.put(PluginKey.key(RelationshipReceivedRequestPlugin.ID), relationshipReceived);
    this.templateBuilders.put(PluginKey.key(RequestJoinSpacePlugin.ID), requestJoinSpace);
    this.templateBuilders.put(PluginKey.key(SpaceInvitationPlugin.ID), spaceInvitation);
    this.templateBuilders.put(PluginKey.key(JoinedSpaceByInvitationLinkPlugin.ID), new JoinedSpaceByInvitationLinkTemplateBuilder());
  }

  private String getActivityTitle(ExoSocialActivity activity, String language) {
    return MentionUtils.substituteRoleWithLocale(Utils.getActivityManager().getActivityTitle(activity),
                                                 Locale.forLanguageTag(language));
  }

  private String processBody(String message, String language) {
    message = MentionUtils.substituteRoleWithLocale(message, Locale.forLanguageTag(language));
    return org.exoplatform.commons.notification.NotificationUtils.processLinkTitle(message);
  }

  private String getSpaceUrl(String spaceId) {
    try {
      return LinkProvider.getSpaceLink(spaceId);
    } catch (Exception e) {
      return null;
    }
  }

}
