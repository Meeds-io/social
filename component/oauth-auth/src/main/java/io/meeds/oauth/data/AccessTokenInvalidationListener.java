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
package io.meeds.oauth.data;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.services.organization.UserProfileHandler;

import io.meeds.oauth.spi.OAuthCodec;
import io.meeds.oauth.spi.OAuthProviderProcessor;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;

/**
 * Listener for invalidate access token of particular user, if OAuth username of
 * this user is changed to different value
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AccessTokenInvalidationListener extends UserProfileEventListener {

  private static Log                      log = ExoLogger.getLogger(AccessTokenInvalidationListener.class);

  private final UserProfileHandler        userProfileHandler;

  private final OAuthProviderTypeRegistry oauthProviderTypeRegistry;

  private final OAuthCodec                oauthCodec;                                                      // actually
                                                                                                           // SocialNetworkServiceImpl

  public AccessTokenInvalidationListener(OrganizationService orgService,
                                         OAuthProviderTypeRegistry oauthProviderTypeRegistry,
                                         OAuthCodec oauthCodec) {
    this.userProfileHandler = orgService.getUserProfileHandler();
    this.oauthProviderTypeRegistry = oauthProviderTypeRegistry;
    this.oauthCodec = oauthCodec;
  }

  @Override
  public void preSave(UserProfile userProfile, boolean isNew) throws Exception {
    UserProfile foundUserProfile = userProfileHandler.findUserProfileByName(userProfile.getUserName());
    if (foundUserProfile == null) {
      foundUserProfile = userProfileHandler.createUserProfileInstance(userProfile.getUserName());
    }

    for (OAuthProviderType opt : oauthProviderTypeRegistry.getEnabledOAuthProviders()) {
      String oauthProviderUsername = userProfile.getAttribute(opt.getUserNameAttrName());
      String foundOauthProviderUsername = foundUserProfile.getAttribute(opt.getUserNameAttrName());

      // This means that oauthUsername has been changed. We may need to
      // invalidate current accessToken as well
      if (!Safe.equals(oauthProviderUsername, foundOauthProviderUsername)) {
        OAuthProviderProcessor processor = opt.getOauthProviderProcessor();
        Object currentAccessToken = processor.getAccessTokenFromUserProfile(userProfile, oauthCodec);
        Object foundAccessToken = processor.getAccessTokenFromUserProfile(foundUserProfile, oauthCodec);

        // In this case, we need to remove existing accessToken
        if (currentAccessToken != null && currentAccessToken.equals(foundAccessToken)) {
          if (log.isTraceEnabled()) {
            log.trace("Removing accessToken for oauthProvider=" + opt + ", username=" + userProfile.getUserName());
          }
          processor.removeAccessTokenFromUserProfile(userProfile);
        }
      }
    }
  }
}
