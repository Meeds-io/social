/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.service;

import static io.meeds.oauth.constant.OAuthConstants.REGISTER_ON_FLY_INIT_PARAM;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;

import io.meeds.oauth.model.AccessTokenContext;
import io.meeds.oauth.model.OAuthPrincipal;
import io.meeds.oauth.model.OAuthProviderType;

public class OAuthRegistrationService {

  private static final Log     LOG          = ExoLogger.getLogger(OAuthRegistrationService.class);

  private static final int     IMAGE_WIDTH  = 200;

  private static final int     IMAGE_HEIGHT = 200;

  private PortalContainer      container;

  private OrganizationService  organizationService;

  private IdentityManager      identityManager;

  private SocialNetworkService socialNetworkService;

  private List<String>         registerOnFly;

  public OAuthRegistrationService(PortalContainer container,
                                  OrganizationService organizationService,
                                  IdentityManager identityManager,
                                  SocialNetworkService socialNetworkService,
                                  InitParams initParams) {
    this.container = container;
    this.organizationService = organizationService;
    this.socialNetworkService = socialNetworkService;
    this.identityManager = identityManager;

    if (initParams != null && initParams.containsKey(REGISTER_ON_FLY_INIT_PARAM)) {
      String onFlyProviders = initParams.getValueParam(REGISTER_ON_FLY_INIT_PARAM).getValue();
      if (StringUtils.isBlank(onFlyProviders)) {
        registerOnFly = Collections.emptyList();
      } else {
        registerOnFly = Arrays.asList(onFlyProviders.split(","));
      }
    }
  }

  public boolean isRegistrationOnFly(OAuthProviderType<? extends AccessTokenContext> oauthProviderType) {
    return registerOnFly.contains(oauthProviderType.getKey());
  }

  /**
   * attempts to detect if a user account already exists for current social
   * network user
   * 
   * @param  request
   * @param  principal
   * @return           existing user
   */
  public User detectPortalUser(OAuthPrincipal<? extends AccessTokenContext> principal) {
    User foundUser = socialNetworkService.findUserByOAuthProviderUsername(principal.getOauthProviderType(),
                                                                          principal.getUserName());
    // If not found, try to get user with email as unique identifier of oAuth
    // Provider
    if (foundUser == null) {
      foundUser = socialNetworkService.findUserByOAuthProviderUsername(principal.getOauthProviderType(),
                                                                       principal.getEmail());
    }
    if (foundUser == null) {
      // The user email is the unique identifier of the user which determines
      String email = principal.getEmail();

      begin();
      try {
        Query query = new Query();
        query.setEmail(email);
        ListAccess<User> list = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
        int size = list == null ? 0 : list.getSize();
        if (size == 1) {
          return list.load(0, 1)[0];
        } else {
          if (size > 0) {
            LOG.warn("More than one user was found for email {}. Ignore getting user by email.", email);
          }
          return null;
        }
      } catch (Exception e) {
        // Re-throw exception to avoid creating a new user
        throw new IllegalStateException("Error when retrieving user with provider "
            + principal.getOauthProviderType().getFriendlyName()
            + " from IDM store", e);
      } finally {
        end();
      }
    }
  }

  /**
   * Creates a new User with oAuth authenticated user attributes
   * 
   * @param  principal {@link OAuthPrincipal} of authenticated user
   * @return           newly created user
   */
  public User createPortalUser(OAuthPrincipal<? extends AccessTokenContext> principal) {
    User user = principal.getOauthProviderType().getOauthProviderProcessor().createPortalUser(principal);
    createUser(user, principal.getOauthProviderType());
    updateUserIdentityAvatar(user.getUserName(), principal);
    return user;
  }

  /**
   * Saves user OAuth profile attributes in {@link UserProfile} entity
   * 
   * @param  username     existing {@link User} entity username attribute
   * @param  providerType {@link OAuthProviderType}
   * @throws Exception    when error occurs while saving user profile
   */
  public void updateUserProfileAttributes(String username, OAuthProviderType<?> providerType) throws Exception {
    begin();
    try {
      UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
      if (userProfile == null) {
        userProfile = organizationService.getUserProfileHandler().createUserProfileInstance(username);
      }
      userProfile.setAttribute(providerType.getUserNameAttrName(), username);
      organizationService.getUserProfileHandler().saveUserProfile(userProfile, true);
    } finally {
      end();
    }
  }

  /**
   * Saves User Avatar from oAuth Provider into user {@link Identity}
   * 
   * @param username  existing {@link User} entity username attribute
   * @param principal {@link OAuthPrincipal} of authenticated user
   */
  public void updateUserIdentityAvatar(String username, OAuthPrincipal<? extends AccessTokenContext> principal) {
    if (principal == null || StringUtils.isBlank(principal.getAvatar())) {
      return;
    }
    String avatar = principal.getAvatar();
    begin();
    try {
      String fileName = FilenameUtils.getBaseName(avatar);
      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
      String mimeType = mimeTypeResolver.getMimeType(avatar);
      if (!mimeType.toLowerCase().contains("image")) {
        mimeType = "image/png";
      }

      URL url = new URL(avatar);
      try (InputStream is = url.openStream()) {
        if (is != null) {
          AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(is,
                                                                                       IMAGE_WIDTH,
                                                                                       IMAGE_HEIGHT,
                                                                                       null,
                                                                                       fileName,
                                                                                       mimeType,
                                                                                       null);
          if (avatarAttachment == null) {
            avatarAttachment = new AvatarAttachment(null, fileName, mimeType, is, System.currentTimeMillis());
          }

          Identity identity = identityManager.getOrCreateUserIdentity(username);
          Profile profile = identity.getProfile();

          profile.setProperty(Profile.AVATAR, avatarAttachment);
          Map<String, Object> props = profile.getProperties();
          for (String key : props.keySet()) {
            if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
              profile.removeProperty(key);
            }
          }

          identityManager.updateProfile(profile);
        }
      }
    } catch (MalformedURLException ex) {
      LOG.warn("Can not fetch Avatar of oauth user because the URL is invalid: " + avatar, ex);
    } catch (Exception ex) {
      // This exception may be thrown by the AvatarAttachment constructor
      LOG.warn("Can not fetch Avatar at URL: " + avatar, ex);
    } finally {
      end();
    }
  }

  @SuppressWarnings("deprecation")
  private User createUser(User user, OAuthProviderType<?> oAuthProviderType) {
    begin();
    try {
      String username = generateUsername(oAuthProviderType.getKey(), user.getEmail());
      user.setUserName(username);
      organizationService.getUserHandler().createUser(user, true);
      updateUserProfileAttributes(user.getUserName(), oAuthProviderType);
      return user;
    } catch (Exception e) {
      throw new IllegalStateException("Error when trying to create authenticated oAuth user " + user.getEmail()
          + " on-fly with provider " + oAuthProviderType.getFriendlyName(),
                                      e);
    } finally {
      end();
    }
  }

  private String generateUsername(String oAuthProviderKey, String email) throws Exception {
    if (StringUtils.isBlank(email) || !StringUtils.contains(email, "@")) {
      return null;
    }
    String username = StringUtils.lowerCase(oAuthProviderKey) + "_" + email.split("@")[0].replaceAll("\\s", "");
    username = unAccent(username);

    String userNameBase = username;
    Random rand = new Random();// NOSONAR
    while (organizationService.getUserHandler().findUserByName(username, UserStatus.ANY) != null) {
      int num = rand.nextInt(89) + 10;// range between 10 and 99.
      username = userNameBase + String.valueOf(num);
    }
    return username;
  }

  private String unAccent(String src) {
    return Normalizer.normalize(src, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replace("'", "");
  }

  private void begin() {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
  }

  private void end() {
    RequestLifeCycle.end();
  }

}
