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
package io.meeds.oauth.linkedin;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.github.scribejava.apis.LinkedInApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.web.security.security.SecureRandomService;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthCodec;
import io.meeds.oauth.utils.OAuthPersistenceUtils;

public class LinkedinProcessorImpl implements LinkedinProcessor {

  private static Log                log   = ExoLogger.getLogger(LinkedinProcessorImpl.class);

  private final String              redirectURL;

  private final String              apiKey;

  private final String              apiSecret;

  private String                    scope = "r_liteprofile r_emailaddress w_member_social";

  private final SecureRandomService secureRandomService;

  private final int                 chunkLength;

  public OAuth20Service             oAuth20Service;

  public LinkedinProcessorImpl(ExoContainerContext context, SecureRandomService secureRandomService, InitParams params) {
    this.secureRandomService = secureRandomService;
    this.apiKey = params.getValueParam("apiKey").getValue();
    this.apiSecret = params.getValueParam("apiSecret").getValue();
    String redirectURLParam = params.getValueParam("redirectURL").getValue();

    if (apiKey == null || apiKey.length() == 0 || apiKey.trim().equals("<<to be replaced>>")) {
      throw new IllegalArgumentException("Property 'clientId' needs to be provided. The value should be " +
          "clientId of your Twitter application");
    }

    if (apiSecret == null || apiSecret.length() == 0 || apiSecret.trim().equals("<<to be replaced>>")) {
      throw new IllegalArgumentException("Property 'clientSecret' needs to be provided. The value should be " +
          "clientSecret of your Twitter application");
    }

    if (redirectURLParam == null || redirectURLParam.length() == 0) {
      this.redirectURL = "http://localhost:8080/" + context.getName() + OAuthConstants.TWITTER_AUTHENTICATION_URL_PATH;
    } else {
      this.redirectURL = redirectURLParam.replaceAll("@@portal.container.name@@", context.getName());
    }

    if (log.isDebugEnabled()) {
      log.debug("configuration: apiKey=" + apiKey +
          ", apiSecret=" + apiSecret +
          ", redirectURL=" + redirectURL);
    }

    this.chunkLength = OAuthPersistenceUtils.getChunkLength(params);

    this.oAuth20Service = new ServiceBuilder(apiKey)
                                                    .apiSecret(apiSecret)
                                                    .callback(redirectURL)
                                                    .build(LinkedInApi20.instance());

  }

  public LinkedinProcessorImpl(String apiKey, String apiSecret, String redirectURL, String scope, int chunkLength) {

    this.apiSecret = apiSecret;
    this.apiKey = apiKey;
    this.scope = scope;
    this.redirectURL = redirectURL;
    this.chunkLength = chunkLength;
    this.oAuth20Service = new ServiceBuilder(apiKey).apiSecret(apiSecret)
                                                    .defaultScope(scope)
                                                    .callback(redirectURL)
                                                    .build(LinkedInApi20.instance());
    this.secureRandomService = PortalContainer.getInstance().getComponentInstanceOfType(SecureRandomService.class);
  }

  @Override
  public InteractionState<LinkedinAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest,
                                                                              HttpServletResponse httpResponse) throws IOException,
                                                                                                                OAuthException,
                                                                                                                ExecutionException,
                                                                                                                InterruptedException {

    // See if we are a callback
    oAuth20Service = new ServiceBuilder(apiKey)
                                               .apiSecret(apiSecret)
                                               .defaultScope(scope) // replace
                                                                    // with
                                                                    // desired
                                                                    // scope
                                               .callback(redirectURL)
                                               .build(LinkedInApi20.instance());
    if (httpRequest.getParameter("code") == null) {
      String redirect = oAuth20Service.getAuthorizationUrl("secret" + secureRandomService.getSecureRandom().nextLong());
      httpResponse.sendRedirect(redirect);
      return new InteractionState<LinkedinAccessTokenContext>(InteractionState.State.AUTH, null);
    } else {

      String verifierCode = httpRequest.getParameter("code");
      if (verifierCode != null) {
        OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(verifierCode);
        LinkedinAccessTokenContext accessTokenContext = new LinkedinAccessTokenContext(accessToken, this.oAuth20Service);
        return new InteractionState<LinkedinAccessTokenContext>(InteractionState.State.FINISH, accessTokenContext);
      } else {
        String oauthProblem = httpRequest.getParameter("oauth_problem");
        if ("user_refused".equals(oauthProblem)) {
          throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, "User denied scope on LinkedIn authorization page");
        }
        throw new OAuthException(OAuthExceptionCode.LINKEDIN_ERROR, "Can not get oauth verifier code from LinkedIn");
      }
    }
  }

  @Override
  public InteractionState<LinkedinAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest,
                                                                              HttpServletResponse httpResponse,
                                                                              String scope) throws IOException, OAuthException,
                                                                                            ExecutionException,
                                                                                            InterruptedException {
    if (scope != null) {
      this.oAuth20Service = new ServiceBuilder(apiKey)
                                                      .apiSecret(apiSecret)
                                                      .defaultScope(scope)
                                                      .callback(redirectURL)
                                                      .build(LinkedInApi20.instance());
    } else {
      this.oAuth20Service = new ServiceBuilder(apiKey)
                                                      .apiSecret(apiSecret)
                                                      .callback(redirectURL)
                                                      .build(LinkedInApi20.instance());
    }
    return this.processOAuthInteraction(httpRequest, httpResponse);
  }

  @Override
  public void revokeToken(LinkedinAccessTokenContext accessToken) throws OAuthException {
  }

  @Override
  public LinkedinAccessTokenContext validateTokenAndUpdateScopes(LinkedinAccessTokenContext accessToken) throws OAuthException {
    return accessToken;
  }

  @Override
  public <C> C getAuthorizedSocialApiObject(LinkedinAccessTokenContext accessToken, Class<C> socialApiObjectType) {
    return null;
  }

  @Override
  public void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, OAuthCodec codec,
                                                     LinkedinAccessTokenContext accessToken) {
    String encodedAccessToken = codec.encodeString(accessToken.accessToken.getAccessToken());
    OAuthPersistenceUtils.saveLongAttribute(encodedAccessToken,
                                            userProfile,
                                            OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN,
                                            false,
                                            chunkLength);

  }

  @Override
  public LinkedinAccessTokenContext getAccessTokenFromUserProfile(UserProfile userProfile, OAuthCodec codec) {
    String encodedAccessToken = OAuthPersistenceUtils.getLongAttribute(userProfile,
                                                                       OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN,
                                                                       false);
    String encodedAccessTokenSecret = OAuthPersistenceUtils.getLongAttribute(userProfile,
                                                                             OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN_SECRET,
                                                                             false);
    String decodedAccessToken = codec.decodeString(encodedAccessToken);
    String decodedAccessTokenSecret = codec.decodeString(encodedAccessTokenSecret);

    if (decodedAccessToken == null || decodedAccessTokenSecret == null) {
      return null;
    } else {
      OAuth2AccessToken token = new OAuth2AccessToken(decodedAccessToken, decodedAccessTokenSecret);
      return new LinkedinAccessTokenContext(token, oAuth20Service);
    }
  }

  @Override
  public void removeAccessTokenFromUserProfile(UserProfile userProfile) {
    OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN, false);
    OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_LINKEDIN_ACCESS_TOKEN_SECRET, false);
  }
}
