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
package io.meeds.oauth.twitter;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthCodec;
import io.meeds.oauth.utils.OAuthPersistenceUtils;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TwitterProcessorImpl implements TwitterProcessor {

  private static Log           log = ExoLogger.getLogger(TwitterProcessorImpl.class);

  private final String         redirectURL;

  private final String         clientID;

  private final String         clientSecret;

  private final TwitterFactory twitterFactory;

  private final int            chunkLength;

  public TwitterProcessorImpl(ExoContainerContext context, InitParams params) {
    this.clientID = params.getValueParam("clientId").getValue();
    this.clientSecret = params.getValueParam("clientSecret").getValue();
    String redirectURLParam = params.getValueParam("redirectURL").getValue();

    if (clientID == null || clientID.length() == 0 || clientID.trim().equals("<<to be replaced>>")) {
      throw new IllegalArgumentException("Property 'clientId' needs to be provided. The value should be " +
          "clientId of your Twitter application");
    }

    if (clientSecret == null || clientSecret.length() == 0 || clientSecret.trim().equals("<<to be replaced>>")) {
      throw new IllegalArgumentException("Property 'clientSecret' needs to be provided. The value should be " +
          "clientSecret of your Twitter application");
    }

    if (redirectURLParam == null || redirectURLParam.length() == 0) {
      this.redirectURL = "http://localhost:8080/" + context.getName() + OAuthConstants.TWITTER_AUTHENTICATION_URL_PATH;
    } else {
      this.redirectURL = redirectURLParam.replaceAll("@@portal.container.name@@", context.getName());
    }

    this.chunkLength = OAuthPersistenceUtils.getChunkLength(params);

    if (log.isDebugEnabled()) {
      log.debug("configuration: clientId=" + clientID +
          ", clientSecret=" + clientSecret +
          ", redirectURL=" + redirectURL +
          ", chunkLength=" + chunkLength);
    }
    // Create 'generic' twitterFactory for user authentication to GateIn
    ConfigurationBuilder builder = new ConfigurationBuilder();
    builder.setOAuthConsumerKey(clientID).setOAuthConsumerSecret(clientSecret);
    twitterFactory = new TwitterFactory(builder.build());
  }

  @Override
  public InteractionState<TwitterAccessTokenContext> processOAuthInteraction(HttpServletRequest request,
                                                                             HttpServletResponse response) throws IOException,
                                                                                                           OAuthException {
    Twitter twitter = twitterFactory.getInstance();

    HttpSession session = request.getSession();

    // See if we are a callback
    RequestToken requestToken = (RequestToken) session.getAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN);

    try {
      if (requestToken == null) {
        requestToken = twitter.getOAuthRequestToken(redirectURL);

        // Save requestToken to session, but only temporarily until oauth
        // workflow is finished
        session.setAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN, requestToken);

        if (log.isTraceEnabled()) {
          log.trace("RequestToken obtained from twitter. Redirecting to Twitter for authorization");
        }

        // Redirect to twitter to perform authentication
        response.sendRedirect(requestToken.getAuthenticationURL());

        return new InteractionState<TwitterAccessTokenContext>(InteractionState.State.AUTH, null);
      } else {
        String verifier = request.getParameter(OAuthConstants.OAUTH_VERIFIER);

        // User denied scope
        if (request.getParameter(OAuthConstants.OAUTH_DENIED) != null) {
          throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, "User denied scope on Twitter authorization page");
        }

        // Obtain accessToken from twitter
        AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

        if (log.isTraceEnabled()) {
          log.trace("Twitter accessToken: " + accessToken);
        }

        // Remove requestToken from session. We don't need it anymore
        session.removeAttribute(OAuthConstants.ATTRIBUTE_TWITTER_REQUEST_TOKEN);
        TwitterAccessTokenContext accessTokenContext = new TwitterAccessTokenContext(accessToken.getToken(),
                                                                                     accessToken.getTokenSecret());

        return new InteractionState<TwitterAccessTokenContext>(InteractionState.State.FINISH, accessTokenContext);
      }
    } catch (TwitterException twitterException) {
      throw new OAuthException(OAuthExceptionCode.TWITTER_ERROR, twitterException);
    }
  }

  @Override
  public InteractionState<TwitterAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest,
                                                                             HttpServletResponse httpResponse,
                                                                             String scope) throws IOException, OAuthException {
    throw new OAuthException(OAuthExceptionCode.TWITTER_ERROR, "This is currently not supported for Twitter");
  }

  @Override
  public <C> C getAuthorizedSocialApiObject(TwitterAccessTokenContext accessToken, Class<C> socialApiObjectType) {
    if (Twitter.class.equals(socialApiObjectType)) {
      return socialApiObjectType.cast(getAuthorizedTwitterInstance(accessToken));
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Class '" + socialApiObjectType + "' not supported by this processor");
      }
      return null;
    }
  }

  @Override
  public Twitter getAuthorizedTwitterInstance(TwitterAccessTokenContext accessTokenContext) {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    builder.setOAuthConsumerKey(clientID).setOAuthConsumerSecret(clientSecret);

    // Now add accessToken properties to builder
    builder.setOAuthAccessToken(accessTokenContext.getAccessToken());
    builder.setOAuthAccessTokenSecret(accessTokenContext.getAccessTokenSecret());

    // Return twitter instance with successfully established accessToken
    return new TwitterFactory(builder.build()).getInstance();
  }

  @Override
  public void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, OAuthCodec codec,
                                                     TwitterAccessTokenContext accessToken) {
    String encodedAccessToken = codec.encodeString(accessToken.getAccessToken());
    String encodedAccessTokenSecret = codec.encodeString(accessToken.getAccessTokenSecret());
    OAuthPersistenceUtils.saveLongAttribute(encodedAccessToken,
                                            userProfile,
                                            OAuthConstants.PROFILE_TWITTER_ACCESS_TOKEN,
                                            false,
                                            chunkLength);
    OAuthPersistenceUtils.saveLongAttribute(encodedAccessTokenSecret,
                                            userProfile,
                                            OAuthConstants.PROFILE_TWITTER_ACCESS_TOKEN_SECRET,
                                            false,
                                            chunkLength);
  }

  @Override
  public TwitterAccessTokenContext getAccessTokenFromUserProfile(UserProfile userProfile, OAuthCodec codec) {
    String encodedAccessToken = OAuthPersistenceUtils.getLongAttribute(userProfile,
                                                                       OAuthConstants.PROFILE_TWITTER_ACCESS_TOKEN,
                                                                       false);
    String encodedAccessTokenSecret = OAuthPersistenceUtils.getLongAttribute(userProfile,
                                                                             OAuthConstants.PROFILE_TWITTER_ACCESS_TOKEN_SECRET,
                                                                             false);
    String decodedAccessToken = codec.decodeString(encodedAccessToken);
    String decodedAccessTokenSecret = codec.decodeString(encodedAccessTokenSecret);

    if (decodedAccessToken == null || decodedAccessTokenSecret == null) {
      return null;
    } else {
      return new TwitterAccessTokenContext(decodedAccessToken, decodedAccessTokenSecret);
    }
  }

  @Override
  public TwitterAccessTokenContext validateTokenAndUpdateScopes(TwitterAccessTokenContext accessToken) throws OAuthException {
    try {
      // Perform validation by obtaining some info about user
      Twitter twitter = getAuthorizedTwitterInstance(accessToken);
      twitter.verifyCredentials();
      return accessToken;
    } catch (TwitterException tw) {
      if (tw.getStatusCode() == 401) {
        throw new OAuthException(OAuthExceptionCode.ACCESS_TOKEN_ERROR,
                                 "Error when verifying twitter access token: " + tw.getMessage(),
                                 tw);
      } else {
        throw new OAuthException(OAuthExceptionCode.IO_ERROR,
                                 "IO Error when obtaining tokenInfo: " + tw.getClass() + ": " + tw.getMessage(),
                                 tw);
      }
    }
  }

  @Override
  public void removeAccessTokenFromUserProfile(UserProfile userProfile) {
    OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_TWITTER_ACCESS_TOKEN, false);
    OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_TWITTER_ACCESS_TOKEN_SECRET, false);
  }

  @Override
  public void revokeToken(TwitterAccessTokenContext accessToken) {
    // TODO: (if it's possible with Twitter... Maybe it's noop)
  }
}
