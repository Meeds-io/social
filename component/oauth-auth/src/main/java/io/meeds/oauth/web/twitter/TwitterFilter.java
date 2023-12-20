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
package io.meeds.oauth.web.twitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.twitter.TwitterAccessTokenContext;
import io.meeds.oauth.twitter.TwitterProcessor;
import io.meeds.oauth.utils.OAuthUtils;
import io.meeds.oauth.web.OAuthProviderFilter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Filter for integration with authentication handhsake via Twitter with usage
 * of OAuth1
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TwitterFilter extends OAuthProviderFilter<TwitterAccessTokenContext> {

  @Override
  protected OAuthProviderType<TwitterAccessTokenContext> getOAuthProvider() {
    return getOauthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_TWITTER, TwitterAccessTokenContext.class);
  }

  @Override
  protected void initInteraction(HttpServletRequest request, HttpServletResponse response) {
    request.getSession().removeAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN);
  }

  @Override
  protected OAuthPrincipal<TwitterAccessTokenContext> getOAuthPrincipal(HttpServletRequest request, HttpServletResponse response,
                                                                        InteractionState<TwitterAccessTokenContext> interactionState) {
    TwitterAccessTokenContext accessTokenContext = interactionState.getAccessTokenContext();
    Twitter twitter = ((TwitterProcessor) getOauthProviderProcessor()).getAuthorizedTwitterInstance(accessTokenContext);

    User twitterUser;
    try {
      twitterUser = twitter.verifyCredentials();
    } catch (TwitterException te) {
      throw new OAuthException(OAuthExceptionCode.TWITTER_ERROR, "Error when obtaining user", te);
    }

    OAuthPrincipal<TwitterAccessTokenContext> oauthPrincipal = OAuthUtils.convertTwitterUserToOAuthPrincipal(twitterUser,
                                                                                                             accessTokenContext,
                                                                                                             getOAuthProvider());
    return oauthPrincipal;
  }
}
