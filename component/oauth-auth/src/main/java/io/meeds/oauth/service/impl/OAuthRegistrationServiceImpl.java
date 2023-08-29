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
package io.meeds.oauth.service.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jakarta.servlet.http.HttpServletRequest;

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

import io.meeds.oauth.service.OAuthRegistrationService;
import io.meeds.oauth.spi.AccessTokenContext;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;

public class OAuthRegistrationServiceImpl implements OAuthRegistrationService {

  private static Log          log                        = ExoLogger.getLogger(OAuthRegistrationServiceImpl.class);

  private static final String REGISTER_ON_FLY_INIT_PARAM = "registerOnFly";

  private static final int    IMAGE_WIDTH                = 200;

  private static final int    IMAGE_HEIGHT               = 200;

  private PortalContainer     container;

  private OrganizationService organizationService;

  private IdentityManager     identityManager;

  private List<String>        registerOnFly;

  public OAuthRegistrationServiceImpl(PortalContainer container,
                                      OrganizationService organizationService,
                                      IdentityManager identityManager,
                                      InitParams initParams) {
    this.container = container;
    this.organizationService = organizationService;
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

  @Override
  public boolean isRegistrationOnFly(OAuthProviderType<? extends AccessTokenContext> oauthProviderType) {
    return registerOnFly.contains(oauthProviderType.getKey());
  }

  @Override
  public User detectGateInUser(HttpServletRequest request, OAuthPrincipal<? extends AccessTokenContext> principal) {
    String email = principal.getEmail();
    String username = principal.getUserName();

    startTransaction();
    try {
      User user = findUser(username);
      if (user == null) {
        user = findUser(email);
      }
      return user;
    } catch (Exception e) {
      // Re-throw exception to avoid creating a new user
      throw new IllegalStateException("Error when retrieving user with provider "
          + principal.getOauthProviderType().getFriendlyName() + " from IDM store",
                                      e);
    } finally {
      endTransaction();
    }
  }

  @Override
  public User createGateInUser(OAuthPrincipal<? extends AccessTokenContext> principal) {
    User user = principal.getOauthProviderType().getOauthPrincipalProcessor().convertToGateInUser(principal);
    createUser(user, principal.getOauthProviderType());
    updateUserIdentityAvatar(user.getUserName(), principal);
    return user;
  }

  @Override
  public void updateUserProfileAttributes(String username, OAuthProviderType<?> providerType) throws Exception {
    startTransaction();
    try {
      UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
      if (userProfile == null) {
        userProfile = organizationService.getUserProfileHandler().createUserProfileInstance(username);
      }
      userProfile.setAttribute(providerType.getUserNameAttrName(), username);
      organizationService.getUserProfileHandler().saveUserProfile(userProfile, true);
    } finally {
      endTransaction();
    }
  }

  @Override
  public void updateUserIdentityAvatar(String username, OAuthPrincipal<? extends AccessTokenContext> principal) {
    if (principal == null || StringUtils.isBlank(principal.getAvatar())) {
      return;
    }
    String avatar = principal.getAvatar();
    startTransaction();
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
      log.warn("Can not fetch Avatar of oauth user because the URL is invalid: " + avatar, ex);
    } catch (Exception ex) {
      // This exception may be thrown by the AvatarAttachment constructor
      log.warn("Can not fetch Avatar at URL: " + avatar, ex);
    } finally {
      endTransaction();
    }
  }

  @SuppressWarnings("deprecation")
  private User createUser(User user, OAuthProviderType<?> oAuthProviderType) {
    startTransaction();
    try {
      if (StringUtils.isBlank(user.getUserName())) {
        String username = generateUsername(user.getFirstName(), user.getLastName(), user.getEmail());
        user.setUserName(username);
      }
      organizationService.getUserHandler().createUser(user, true);
      updateUserProfileAttributes(user.getUserName(), oAuthProviderType);
      return user;
    } catch (Exception e) {
      throw new IllegalStateException("Error when trying to create authenticated oAuth user " + user.getEmail()
          + " on-fly with provider " + oAuthProviderType.getFriendlyName(),
                                      e);
    } finally {
      endTransaction();
    }
  }

  private String generateUsername(String firstname, String lastname, String email) throws Exception {
    String userNameBase;
    if (StringUtils.isNotBlank(firstname) && StringUtils.isNotBlank(lastname)) {
      userNameBase = new StringBuffer(firstname.replaceAll("\\s", "")).append(".")
                                                                      .append(lastname.replaceAll("\\s", ""))
                                                                      .toString()
                                                                      .toLowerCase();
    } else if (StringUtils.isNotBlank(email)) {
      userNameBase = email.split("@")[0].replaceAll("\\s", "");
    } else {
      userNameBase = "user1";
    }
    userNameBase = unAccent(userNameBase);
    String username = userNameBase;
    Random rand = new Random();// NOSONAR
    // Check if user name already existed (with identity manager, need to
    // move the handler to social)
    while (findUser(username) != null) {
      int num = rand.nextInt(89) + 10;// range between 10 and 99.
      username = userNameBase + String.valueOf(num);
    }
    return username;
  }

  private String unAccent(String src) {
    return Normalizer.normalize(src, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").replace("'", "");
  }

  private User findUser(String usernameOrEmail) throws Exception {
    User user = organizationService.getUserHandler().findUserByName(usernameOrEmail, UserStatus.ANY);
    if (user == null && usernameOrEmail != null && usernameOrEmail.contains("@")) {
      Query query = new Query();
      query.setEmail(usernameOrEmail);
      ListAccess<User> list = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
      if (list != null && list.getSize() > 0) {
        user = list.load(0, 1)[0];
      }
    }
    return user;
  }

  private void startTransaction() {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
  }

  private void endTransaction() {
    RequestLifeCycle.end();
  }

}
