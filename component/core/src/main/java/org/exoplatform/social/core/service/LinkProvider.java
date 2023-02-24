/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.localization.LocaleContextInfoUtils;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.LocalePolicy;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;

/**
 * Builds and provides default links and links of users, spaces and activities.
 * Links are built basing on provided information as name or Id of the target user or space.
 * In case something is wrong when building, the default links will be returned.
 */
public class LinkProvider {
  public static final String RESOURCE_URL = "/social-resources";
  public static final String JAVASCRIPT_RESOURCE_URL = RESOURCE_URL + "/javascript/";

  public static final String PROFILE_DEFAULT_AVATAR_URL = "/platform-ui/skin/images/avatar/DefaultSpaceAvatar.png";
  public static final String SPACE_DEFAULT_AVATAR_URL = "/platform-ui/skin/images/avatar/DefaultUserAvatar.png";
  public static final String HAS_CONNECTION_ICON =
          RESOURCE_URL + "/platform-ui/skin/images/themes/default/social/skin/UIManageUsers/StatusIcon.png";
  public static final String WAITING_CONFIRMATION_ICON =
          RESOURCE_URL + "/platform-ui/skin/images/themes/default/social/skin/UIManageUsers/WaittingConfirm.png";
  public static final String SPACE_MANAGER_ICON =
          RESOURCE_URL + "/platform-ui/skin/images/themes/default/social/skin/UIManageSpaces/Manager.png";
  public static final String SPACE_MEMBER_ICON =
          RESOURCE_URL + "/platform-ui/skin/images/themes/default/social/skin/UIManageSpaces/Member.png";
  public static final String SPACE_WAITING_CONFIRM_ICON =
          RESOURCE_URL + "/platform-ui/skin/images/themes/default/social/skin/UIManageSpaces/WaitingConfirm.png";
  public static final String STARTER_ACTIVITY_AVATAR = "/platform-ui/skin/images/themes/default/social/skin/Activity/starterAvt.png";
  public static final String STARTER_ACTIVITY_IMAGE = "/platform-ui/skin/images/themes/default/social/skin/Activity/welcome-img.png";

  public static final String ROUTE_DELIMITER = "/";

  public static final long    DEFAULT_IMAGES_LAST_MODIFED = System.currentTimeMillis();

  public static final String  DEFAULT_IMAGE_REMOTE_ID     = "default-image";

  private static Log             LOG = ExoLogger.getLogger(LinkProvider.class);

  private static final  String BASE_URL_SOCIAL_REST_API = "/v1/social";

  private static String        baseURLSocialUserRest;

  private static String        baseURLSocialSpaceRest;

  public static final String TYPE = "file";

  public LinkProvider() {
  }

  /**
   * Gets URI to a space profile by its pretty name.
   *
   * @param prettyName The pretty name of space.
   * @return The URI.
   * @LevelAPI Platform
   * @since 1.2.0 GA
   */
  public static String getSpaceUri(final String prettyName) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceByPrettyName(prettyName);
    RequestContext ctx = RequestContext.getCurrentInstance();
    if (ctx != null && space != null) {
      NodeURL nodeURL = ctx.createURL(NodeURL.TYPE);
      NavigationResource resource = new NavigationResource(SiteType.GROUP, space.getGroupId(), space.getUrl());
      return nodeURL.setResource(resource).toString();
    } else {
      return null;
    }
  }

  /**
   * Gets URI to a user profile by username.
   *
   * @param username The name of user (remoteId).
   * @return The URI.
   * @LevelAPI Platform
   */
  public static String getProfileUri(final String username) {
    return buildProfileUri(username, null, null);
  }

  /**
   * Gets URI to a user profile by username and owner portal.
   *
   * @param username The name of user (remoteId).
   * @param portalOwner The portal owner (for example, classic or public).
   * @return The URI.
   * @LevelAPI Platform
   */
  public static String getProfileUri(final String username, final String portalOwner) {
    return buildProfileUri(username, null, portalOwner);
  }

  /**
   * Gets a link to the user profile.
   *
   * @param username The name of user (remoteId).
   * @return The link.
   * @LevelAPI Platform
   */
  public static String getProfileLink(final String username) {
    return getProfileLink(username, null);
  }

  /**
   * Gets a link to the user profile on a portal.
   *
   * @param username The name of user (remoteId).
   * @param portalOwner The portal owner (for example, classic or public).
   * @return The link.
   * @LevelAPI Platform
   */
  public static String getProfileLink(final String username, final String portalOwner) {
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
    Validate.notNull(identity, "Identity must not be null.");
    String lang = getCurrentUserLanguage(username);
    //
    String configured_domain_url = null;
    try {
      configured_domain_url = CommonsUtils.getCurrentDomain();
    } catch (NullPointerException e) {
      configured_domain_url = null;
    }

    StringBuilder profileLink = new StringBuilder("<a class=\"user-suggester\" href=\"");
    profileLink.append((configured_domain_url != null) ? configured_domain_url
                                                       : "")
               .append(buildProfileUri(identity.getRemoteId(),
                                       null,
                                       portalOwner))
               .append("\" target=\"_parent\"")
               .append(" v-identity-popover=\"{")
               .append("id: '")
               .append(identity.getId())
               .append("',")
               .append("username: '")
               .append(identity.getRemoteId())
               .append("',")
               .append("fullName: '")
               .append(identity.getProfile().getFullName().replace("'", "\\\\'"))
               .append("',")
               .append("avatar: '")
               .append(identity.getProfile().getAvatarUrl())
               .append("',")
               .append("position: '")
               .append(identity.getProfile().getPosition() == null ? "" : identity.getProfile().getPosition().replace("'", "\\\\'"))
               .append("',")
               .append("external: '")
               .append(identity.getProfile().getProperty(Profile.EXTERNAL) != null && StringUtils.equals("true", String.valueOf(identity.getProfile().getProperty(Profile.EXTERNAL))))
               .append("',")
               .append("}\"")
               .append(">")
               .append(StringEscapeUtils.escapeHtml(identity.getProfile().getFullName()));
    if(identity.getProfile().getProperty("external") != null && identity.getProfile().getProperty("external").equals("true")){
      profileLink = profileLink.append("<span \" class=\"externalFlagClass\">").append(" (").append(getResourceBundleLabel(new Locale(lang), "external.label.tag")).append(")").append("</span>");
    }
    return profileLink.append("</a>").toString();
  }

  /**
   * Gets an absolute profile URL of a user.
   *
   * @param userName The name of user (remoteId).
   * @param portalName The name of current portal.
   * @param portalOwner The portal owner (for example, classic or public).
   * @param host The name of the provided host.
   * @return The absolute profile URL.
   * @LevelAPI Platform
   */
  public static String getAbsoluteProfileUrl(final String userName, final String portalName,
                                             final String portalOwner, final String host) {
    return host + buildProfileUri(userName, portalName, portalOwner);
  }

  /**
   * Gets the activity URI of a user.
   * 
   * @param remoteId Name of the user, for example root.
   * @return The activity link.
   * @LevelAPI Platform
   */
  public static String getUserActivityUri(final String remoteId) {
    return getActivityUri(OrganizationIdentityProvider.NAME,remoteId);
  }

  /**
   * Gets URI to connections of a user and all people.
   * 
   * @param remoteId Name of the user (remoteId), for example root.
   * @return The URI.
   * @LevelAPI Platform
   */
  public static String getUserConnectionsUri(final String remoteId) {
    return getBaseUri(null, null) + "/connections/all-people" + ROUTE_DELIMITER + remoteId;
  }
  
  /**
   * Gets URI to connections of a user.
   * 
   * @param remoteId The name of user (remoteId), for example root.
   * @return The link to network of provided user who has connection with the current user. 
   */
  public static String getUserConnectionsYoursUri(final String remoteId) {
    return getBaseUri(null, null) + "/connections/network" + ROUTE_DELIMITER + remoteId;
  }
  
  /**
   * Gets URI to a user profile.
   *
   * @param remoteId The name of user (remoteId), for example root.
   * @return The link to profile of provided user.
   * @LevelAPI Platform
   */
  public static String getUserProfileUri(final String remoteId) {
    return getBaseUri(null, null) + "/profile" + ROUTE_DELIMITER + remoteId;
  }

  /**
   * Gets URI to an activity stream of space or user.
   *
   * @param providerId The provider information.
   * @param remoteId Id of the target identity, for example organization:root or space:abc_def.
   * @return The link to activity of provided user on the provided provider.
   * @LevelAPI Platform
   */
  public static String getActivityUri(final String providerId, final String remoteId) {
    final String prefix = getBaseUri(null, null) + "/";
    if (providerId.equals(OrganizationIdentityProvider.NAME)) {
      return String.format("%sactivities/%s",prefix,remoteId);
    } else if (providerId.equals(SpaceIdentityProvider.NAME)) {
      return String.format("/%s/g/:spaces:%s/%s",getPortalName(null),remoteId,remoteId);
    } else {
      LOG.warn("Failed to getActivityLink with providerId: " + providerId);
    }
    return null;
  }
  
  /**
   * @param activityId
   * @return
   */
  public static String getSingleActivityUrl(String activityId) {
    return getBaseUri(null, null) + "/activity?id=" + activityId;
  }

  /**
   * Gets an activity URI of the space.
   *
   * @param remoteId The Id of target space.
   * @param groupId The group Id of target space.
   * @return The URI.
   * @LevelAPI Platform
   * @since 1.2.8
   */
  public static String getActivityUriForSpace(final String remoteId, final String groupId) {
    return String.format("/%s/g/:spaces:%s/%s", getPortalName(null), groupId, remoteId);
  }

  /**
   * Builds a profile URI from userName and portalOwner.
   *
   * @param userName The name of user.
   * @param portalName The name of portal.
   * @param portalOwner The owner of portal (for example, classic or public).
   *        
   * @return The profile URI.
   */
  private static String buildProfileUri(final String userName, final String portalName, String portalOwner) {
    return getBaseUri(portalName, portalOwner) + "/profile" + ROUTE_DELIMITER + userName;
  }

  /**
   * Builds a profile URI from userName and portalName and portalOwner.
   *
   * @param portalName The name of portal.
   * @param portalOwner The owner of portal (for example, classic or public).
   *        
   * @return The profile URI.
   */
  private static String getBaseUri(final String portalName, String portalOwner) {
    return "/" + getPortalName(portalName) + "/" + getPortalOwner(portalOwner);
  }

  private static String getSpaceBaseUri(final String portalName, String portalOwner) {
    return "/" + getPortalName(portalName);
  }

  /**
   * Gets the link of notification settings page
   * 
   * @param remoteId
   * @return
   */
  public static String getUserNotificationSettingUri(final String remoteId) {
    return getBaseUri(null, null) + "/settings";
  }

  /**
   * Gets the link of all spaces page
   *
   * @return
   */
  public static String getRedirectUri(String type) {
    if (type.isEmpty()) {
      return getBaseUri(null, null);
    }
    return getBaseUri(null, null) + "/" + type;
  }

  public static String getRedirectSpaceUri(String type) {
    if (type.isEmpty()) {
      return getSpaceBaseUri(null, null);
    }
    return getSpaceBaseUri(null, null) + "/" + type;
  }

  /**
   * Gets an IdentityManager instance.
   *
   * @return The IdentityManager.
   */
  public static IdentityManager getIdentityManager() {
    return CommonsUtils.getService(IdentityManager.class);
  }
  
  /**
   * Builds the avatar URL for a given profile
   * 
   * @param providerId
   * @param remoteId
   * @return
   * @deprecated user {@link LinkProvider#buildAvatarURL(String, String, Long)}
   *             to use browser cache
   */
  @Deprecated
  public static String buildAvatarURL(String providerId, String remoteId) {
    return buildAttachmentUrl(providerId, remoteId, null, AvatarAttachment.TYPE);
  }

  /**
   * Builds the avatar URL for a given profile
   * @param providerId
   * @param remoteId
   * @param lastModifiedDate last modified date of avatar
   * @return
   */
  public static String buildAvatarURL(String providerId, String remoteId, Long lastModifiedDate) {
    return buildAttachmentUrl(providerId, remoteId, lastModifiedDate, AvatarAttachment.TYPE);
  }

  /**
   * Builds the banner URL for a given profile
   * @param providerId
   * @param remoteId
   * @return
   * @deprecated user {@link LinkProvider#buildBannerURL(String, String, Long)}
   *             to use browser cache
   */
  @Deprecated
  public static String buildBannerURL(String providerId, String remoteId) {
    return buildAttachmentUrl(providerId, remoteId, null, BannerAttachment.TYPE);
  }
  
  /**
   * Builds the banner URL for a given profile
   * @param providerId
   * @param remoteId
   * @param lastModifiedDate last modified date of avatar
   * @return
   */
  public static String buildBannerURL(String providerId, String remoteId, Long lastModifiedDate) {
    return buildAttachmentUrl(providerId, remoteId, lastModifiedDate, BannerAttachment.TYPE);
  }

  private static String buildAttachmentUrl(String providerId, String remoteId, Long lastModifiedDate, String type) {
    if (providerId == null || remoteId == null) {
      return null;
    } else if (providerId.equals("spaceTemplates")) {
      return new StringBuilder(getBaseURLSocialRest())
          .append("/")
          .append(providerId)
          .append("/")
          .append(remoteId)
          .append("/")
          .append(type)
          .append("?lastModified=")
          .append(DEFAULT_IMAGES_LAST_MODIFED)
          .toString();
    }

    try {
      remoteId = URLEncoder.encode(remoteId, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      LOG.warn("Failure to encode username for build URL", ex);
    }

    if (lastModifiedDate == null || lastModifiedDate <= 0 || lastModifiedDate == DEFAULT_IMAGES_LAST_MODIFED) {
      remoteId = DEFAULT_IMAGE_REMOTE_ID;
      lastModifiedDate = DEFAULT_IMAGES_LAST_MODIFED;
    }

    String lastModifiedString = String.valueOf(lastModifiedDate);
    String token = generateAttachmentToken(providerId, remoteId, type, lastModifiedString);
    if (StringUtils.isNotBlank(token)) {
      try {
        token = URLEncoder.encode(token, "UTF8");
      } catch (UnsupportedEncodingException e) {
        LOG.warn("Error encoding token", e);
        token = StringUtils.EMPTY;
      }
    }

    if (providerId.equals(OrganizationIdentityProvider.NAME)) {
      return new StringBuilder(getBaseURLSocialUserRest()).append("/")
                                                          .append(remoteId)
                                                          .append("/")
                                                          .append(type)
                                                          .append("?lastModified=")
                                                          .append(lastModifiedString)
                                                          .append("&r=")
                                                          .append(token)
                                                          .toString();
    } else if (providerId.equals(SpaceIdentityProvider.NAME)) {
      return new StringBuilder(getBaseURLSocialSpaceRest()).append("/")
                                                           .append(remoteId)
                                                           .append("/")
                                                           .append(type)
                                                           .append("?lastModified=")
                                                           .append(lastModifiedString)
                                                           .append("&r=")
                                                           .append(token)
                                                           .toString();
    } else {
      return null;
    }
  }

  public static String generateAttachmentToken(String providerId,
                                               String remoteId,
                                               String attachmentType,
                                               String lastModifiedDate) {
    String token = null;
    CodecInitializer codecInitializer = ExoContainerContext.getService(CodecInitializer.class);
    if (codecInitializer == null) {
      LOG.debug("Can't find an instance of CodecInitializer, an empty token will be generated");
      token = StringUtils.EMPTY;
    } else {
      try {
        String tokenPlain = attachmentType + ":" + providerId + ":" + remoteId + ":" + lastModifiedDate;
        token = codecInitializer.getCodec().encode(tokenPlain);
      } catch (TokenServiceInitializationException e) {
        LOG.warn("Error generating token of {} for identity '{}:{}'. An empty token will be used", attachmentType, remoteId, e);
        token = StringUtils.EMPTY;
      }
    }
    return token;
  }

  public static boolean isAttachmentTokenValid(String token,
                                               String providerId,
                                               String remoteId,
                                               String attachmentType,
                                               String lastModifiedDate) {
    if (StringUtils.isBlank(token)) {
      LOG.warn("An empty token is used for {} for identity '{}:{}'", attachmentType, remoteId);
      return false;
    }
    String validToken = generateAttachmentToken(providerId, remoteId, attachmentType, lastModifiedDate);
    return StringUtils.equals(validToken, token);
  }

  /**
   * Gets a portal owner. If the parameter is null or "", the method returns a default portal owner.
   *
   * @param portalOwner The owner of portal (for example, classic or public).
   * @return The portal owner.
   */
  public static String getPortalOwner(String portalOwner) {
    if (portalOwner == null || portalOwner.trim().length() == 0) {
      portalOwner = CommonsUtils.getCurrentPortalOwner();
    }
    return portalOwner;
  }

  /**
   * Gets a portal name. If the parameter is null or "", the method returns a default portal name.
   * 
   * @param portalName The name of portal.
   * @return The portal name.
   */
  public static String getPortalName(String portalName) {
    if (portalName == null || portalName.trim().length() == 0) {
      return PortalContainer.getCurrentPortalContainerName();
    }
    return portalName;
  }

  public static String getBaseURLSocialSpaceRest() {
    if (baseURLSocialSpaceRest == null) {
      baseURLSocialSpaceRest = getBaseURLSocialRest() + "/spaces";
    }
    return baseURLSocialSpaceRest;
  }

  public static String getBaseURLSocialUserRest() {
    if (baseURLSocialUserRest == null) {
      baseURLSocialUserRest = getBaseURLSocialRest() + "/users";
    }
    return baseURLSocialUserRest;
  }

  public static String getBaseURLSocialRest() {
    return "/" + PortalContainer.getCurrentPortalContainerName() + "/" + PortalContainer.getCurrentRestContextName() + BASE_URL_SOCIAL_REST_API;
  }

  /**
   * Gets platform language of current user. In case of any errors return null.
   *
   * @return the platform language
   */
  public static String getCurrentUserLanguage(String userId) {
    try {
      LocaleContextInfo localeCtx = LocaleContextInfoUtils.buildLocaleContextInfo(userId);
      LocalePolicy localePolicy = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LocalePolicy.class);
      String lang = null;
      if(localePolicy != null) {
        Locale locale = localePolicy.determineLocale(localeCtx);
        lang = locale.toString();
      }
      return lang;
    } catch (Exception e) {
      LOG.error("Error searching user " + userId, e);
      return null;
    }
  }

  /**
   * Gets the resource bundle label.
   *
   * @return the label
   */
  public static String getResourceBundleLabel(Locale locale, String label) {
    ResourceBundleService resourceBundleService =  ExoContainerContext.getService(ResourceBundleService.class);
    return resourceBundleService.getResourceBundle(resourceBundleService.getSharedResourceBundleNames(), locale).getString(label);
  }
}
