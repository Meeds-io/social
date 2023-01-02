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
package io.meeds.oauth.listener;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

import io.meeds.oauth.model.OAuthProviderType;
import io.meeds.oauth.provider.spi.OAuthProviderProcessor;
import io.meeds.oauth.service.OAuthProviderTypeRegistry;

/**
 * Listener for invalidate access token of particular user, if OAuth username of
 * this user is changed to different value
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@SuppressWarnings("rawtypes")
public class AccessTokenInvalidationListener extends UserProfileEventListener {

  private static final Log                LOG =
                                              ExoLogger.getLogger(AccessTokenInvalidationListener.class);

  private final UserProfileHandler        userProfileHandler;

  private final OAuthProviderTypeRegistry oauthProviderTypeRegistry;

  private AbstractCodec                   codec;

  public AccessTokenInvalidationListener(OrganizationService orgService,
                                         OAuthProviderTypeRegistry oauthProviderTypeRegistry,
                                         CodecInitializer codecInitializer)
      throws TokenServiceInitializationException {
    this.userProfileHandler = orgService.getUserProfileHandler();
    this.oauthProviderTypeRegistry = oauthProviderTypeRegistry;
    this.codec = codecInitializer.getCodec();
  }

  @Override
  public void preSave(UserProfile userProfile, boolean isNew) throws Exception {
    UserProfile foundUserProfile = userProfileHandler.findUserProfileByName(userProfile.getUserName());
    if (foundUserProfile == null) {
      foundUserProfile = userProfileHandler.createUserProfileInstance(userProfile.getUserName());
    }

    for (OAuthProviderType oAuthProviderType : oauthProviderTypeRegistry.getEnabledOAuthProviders()) {
      String oauthProviderUsername = userProfile.getAttribute(oAuthProviderType.getUserNameAttrName());
      String foundOauthProviderUsername = foundUserProfile.getAttribute(oAuthProviderType.getUserNameAttrName());

      // This means that oauthUsername has been changed. We may need to
      // invalidate current accessToken as well
      if (!StringUtils.equals(oauthProviderUsername, foundOauthProviderUsername)) {
        OAuthProviderProcessor<?> processor = oAuthProviderType.getOauthProviderProcessor();
        Object currentAccessToken = processor.getAccessTokenFromUserProfile(userProfile);
        Object foundAccessToken = processor.getAccessTokenFromUserProfile(foundUserProfile);

        // In this case, we need to remove existing accessToken
        if (currentAccessToken != null && currentAccessToken.equals(foundAccessToken)) {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Removing accessToken for oauthProvider= {}, username= {}", oAuthProviderType, userProfile.getUserName());
          }
          processor.removeAccessTokenFromUserProfile(userProfile);
        }
      }
    }
  }

  public String encodeString(String input) {
    if (input == null) {
      return null;
    } else {
      return codec.encode(input);
    }
  }

  public String decodeString(String input) {
    if (input == null) {
      return null;
    } else {
      return codec.decode(input);
    }
  }

}
