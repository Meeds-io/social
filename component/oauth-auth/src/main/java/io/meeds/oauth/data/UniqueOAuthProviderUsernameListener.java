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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;
import io.meeds.oauth.spi.SocialNetworkService;

/**
 * Listener to validate that OAuth username of given user is unique, because we
 * can't have 2 users in portal with same OAuth username for same
 * {@link OAuthProviderType} If OAuth username is not unique, then
 * {@link OAuthException} with code
 * {@link OAuthExceptionCode#DUPLICATE_OAUTH_PROVIDER_USERNAME} will be thrown
 * and it will encapsulate some other needed info (useful for showing error
 * message)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UniqueOAuthProviderUsernameListener extends UserProfileEventListener {

  private final SocialNetworkService      socialNetworkService;

  private final OAuthProviderTypeRegistry oauthProviderTypeRegistry;

  public UniqueOAuthProviderUsernameListener(SocialNetworkService socialNetworkService,
                                             OAuthProviderTypeRegistry oauthProviderTypeRegistry) {
    this.socialNetworkService = socialNetworkService;
    this.oauthProviderTypeRegistry = oauthProviderTypeRegistry;
  }

  @Override
  public void preSave(UserProfile user, boolean isNew) throws Exception {
    for (OAuthProviderType opt : oauthProviderTypeRegistry.getEnabledOAuthProviders()) {
      String oauthProviderUsername = user.getAttribute(opt.getUserNameAttrName());

      if (oauthProviderUsername == null) {
        continue;
      }

      User foundUser = socialNetworkService.findUserByOAuthProviderUsername(opt, oauthProviderUsername);
      if (foundUser != null && !user.getUserName().equals(foundUser.getUserName())) {
        String message = "Attempt to save " + opt.getUserNameAttrName() + " with value " + oauthProviderUsername +
            " but it already exists. currentUser=" + user.getUserName() + ", userWithThisOAuthUsername="
            + foundUser.getUserName();
        Map<String, Object> exceptionAttribs = new HashMap<String, Object>();
        exceptionAttribs.put(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME_ATTRIBUTE_NAME, opt.getUserNameAttrName());
        exceptionAttribs.put(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_USERNAME, oauthProviderUsername);
        exceptionAttribs.put(OAuthConstants.EXCEPTION_OAUTH_PROVIDER_NAME, opt.getFriendlyName());

        throw new OAuthException(OAuthExceptionCode.DUPLICATE_OAUTH_PROVIDER_USERNAME, exceptionAttribs, message);
      }
    }
  }
}
