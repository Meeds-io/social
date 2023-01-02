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
package io.meeds.oauth.provider.google.processor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.services.plus.Plus;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.SecureRandomService;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

import io.meeds.oauth.constant.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.model.InteractionState;
import io.meeds.oauth.provider.google.model.GoogleAccessTokenContext;
import io.meeds.oauth.provider.spi.OAuthProviderProcessor;
import io.meeds.oauth.utils.OAuthPersistenceUtils;

/**
 * TODO Use <a href="https://github.com/googleapis/google-auth-library-java">
 * google-auth-library</a> for handling Google OAuth2 Credentials
 */
@SuppressWarnings("deprecation")
public class GoogleProcessor extends OAuthProviderProcessor<GoogleAccessTokenContext> {

  private static Log                  log          = ExoLogger.getLogger(GoogleProcessor.class);

  /**
   * This is Thread safe, thus made as constant
   */
  private static final HttpTransport  TRANSPORT    = new NetHttpTransport();

  /**
   * This is Thread safe, thus made as constant
   */
  private static final JacksonFactory JSON_FACTORY = new JacksonFactory();

  private final String                redirectURL;

  private final String                clientID;

  private final String                clientSecret;

  private final Set<String>           scopes;

  private final String                accessType;

  private final String                applicationName;

  private final int                   chunkLength;

  private final SecureRandomService   secureRandomService;

  public GoogleProcessor(ExoContainerContext context,
                         SecureRandomService secureRandomService,
                         CodecInitializer codecInitializer,
                         InitParams params)
      throws TokenServiceInitializationException {
    super(codecInitializer);
    this.clientID = params.getValueParam("clientId").getValue();
    this.clientSecret = params.getValueParam("clientSecret").getValue();
    String redirectURLParam = params.getValueParam("redirectURL").getValue();
    String scope = params.getValueParam("scope").getValue();
    this.accessType = params.getValueParam("accessType").getValue();
    ValueParam appNameParam = params.getValueParam("applicationName");
    if (appNameParam != null && appNameParam.getValue() != null) {
      applicationName = appNameParam.getValue();
    } else {
      applicationName = "GateIn portal";
    }

    if (clientID == null || clientID.length() == 0 || clientID.trim().equals("<<to be replaced>>")) {
      throw new IllegalArgumentException("Property 'clientId' needs to be provided. The value should be " +
          "clientId of your Google application");
    }

    if (clientSecret == null || clientSecret.length() == 0 || clientSecret.trim().equals("<<to be replaced>>")) {
      throw new IllegalArgumentException("Property 'clientSecret' needs to be provided. The value should be " +
          "clientSecret of your Google application");
    }

    if (redirectURLParam == null || redirectURLParam.length() == 0) {
      this.redirectURL = "http://localhost:8080/" + context.getName() + OAuthConstants.GOOGLE_AUTHENTICATION_URL_PATH;
    } else {
      this.redirectURL = redirectURLParam.replaceAll("@@portal.container.name@@", context.getName());
    }

    this.scopes = new HashSet<>(Arrays.asList(scope.split(" ")));

    this.chunkLength = OAuthPersistenceUtils.getChunkLength(params);

    if (log.isDebugEnabled()) {
      log.debug("configuration: clientId=" + clientID +
          ", clientSecret=" + clientSecret +
          ", redirectURL=" + redirectURL +
          ", scope=" + scopes +
          ", accessType=" + accessType +
          ", applicationName=" + applicationName +
          ", chunkLength=" + chunkLength);
    }

    this.secureRandomService = secureRandomService;
  }

  public InteractionState<GoogleAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest,
                                                                            HttpServletResponse httpResponse) throws IOException,
                                                                                                              OAuthException {
    return processOAuthInteractionImpl(httpRequest, httpResponse, this.scopes);
  }

  public InteractionState<GoogleAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest,
                                                                            HttpServletResponse httpResponse,
                                                                            String scope) throws IOException, OAuthException {
    return processOAuthInteractionImpl(httpRequest, httpResponse, new HashSet<>(Arrays.asList(scope.split(" "))));
  }

  public GoogleAccessTokenContext validateTokenAndUpdateScopes(GoogleAccessTokenContext accessTokenContext) {
    GoogleRequest<Tokeninfo> googleRequest = new GoogleRequest<Tokeninfo>() {

      @Override
      protected Tokeninfo invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException {
        GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
        Oauth2 oauth2 = getOAuth2InstanceImpl(tokenData);
        GoogleCredential credential = getGoogleCredential(tokenData);
        return oauth2.tokeninfo().setAccessToken(credential.getAccessToken()).execute();
      }

      @Override
      protected OAuthException createException(IOException cause) {
        if (cause instanceof HttpResponseException) {
          return new OAuthException(OAuthExceptionCode.ACCESS_TOKEN_ERROR,
                                    "Error when obtaining tokenInfo: " + cause.getMessage(),
                                    cause);
        } else {
          return new OAuthException(OAuthExceptionCode.IO_ERROR,
                                    "IO Error when obtaining tokenInfo: " + cause.getMessage(),
                                    cause);
        }
      }
    };
    Tokeninfo tokenInfo = googleRequest.executeRequest(accessTokenContext, this);
    // If there was an error in the token info, abort.
    if (tokenInfo.containsKey("error")) {
      throw new OAuthException(OAuthExceptionCode.ACCESS_TOKEN_ERROR,
                               "Error during token validation: " + tokenInfo.get("error").toString());
    }

    if (!tokenInfo.getIssuedTo().equals(clientID)) {
      throw new OAuthException(OAuthExceptionCode.ACCESS_TOKEN_ERROR,
                               "Token's client ID does not match app's. clientID from tokenINFO: " + tokenInfo.getIssuedTo());
    }
    return new GoogleAccessTokenContext(accessTokenContext.getTokenData(), tokenInfo.getScope().split(" "));
  }

  public <C> C getAuthorizedSocialApiObject(GoogleAccessTokenContext accessToken, Class<C> socialApiObjectType) {
    if (Oauth2.class.equals(socialApiObjectType)) {
      return socialApiObjectType.cast(getOAuth2Instance(accessToken));
    } else if (Plus.class.equals(socialApiObjectType)) {
      return socialApiObjectType.cast(getPlusService(accessToken));
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Class '" + socialApiObjectType + "' not supported by this processor");
      }
      return null;
    }
  }

  public void saveAccessTokenAttributesToUserProfile(UserProfile userProfile, GoogleAccessTokenContext accessToken) {
    GoogleTokenResponse tokenData = accessToken.getTokenData();
    String encodedAccessToken = encodeString(tokenData.getAccessToken());
    String encodedRefreshToken = encodeString(tokenData.getRefreshToken());
    String encodedScope = encodeString(accessToken.getScopesAsString());

    OAuthPersistenceUtils.saveLongAttribute(encodedAccessToken,
                                            userProfile,
                                            OAuthConstants.PROFILE_GOOGLE_ACCESS_TOKEN,
                                            false,
                                            chunkLength);
    userProfile.setAttribute(OAuthConstants.PROFILE_GOOGLE_SCOPE, encodedScope);

    // Don't overwrite existing refresh token because it's not present in every
    // accessToken response (only in very first one)
    if (encodedRefreshToken != null) {
      OAuthPersistenceUtils.saveLongAttribute(encodedRefreshToken,
                                              userProfile,
                                              OAuthConstants.PROFILE_GOOGLE_REFRESH_TOKEN,
                                              false,
                                              chunkLength);
    }
  }

  public GoogleAccessTokenContext getAccessTokenFromUserProfile(UserProfile userProfile) {
    String encodedAccessToken = OAuthPersistenceUtils.getLongAttribute(userProfile,
                                                                       OAuthConstants.PROFILE_GOOGLE_ACCESS_TOKEN,
                                                                       false);
    String decodedAccessToken = decodeString(encodedAccessToken);

    // We don't have token in userProfile
    if (decodedAccessToken == null) {
      return null;
    }

    String encodedRefreshToken = OAuthPersistenceUtils.getLongAttribute(userProfile,
                                                                        OAuthConstants.PROFILE_GOOGLE_REFRESH_TOKEN,
                                                                        false);
    String decodedRefreshToken = decodeString(encodedRefreshToken);
    String decodedScope = decodeString(userProfile.getAttribute(OAuthConstants.PROFILE_GOOGLE_SCOPE));
    GoogleTokenResponse grc = new GoogleTokenResponse();
    grc.setAccessToken(decodedAccessToken);
    grc.setRefreshToken(decodedRefreshToken);
    grc.setTokenType("Bearer");
    grc.setExpiresInSeconds(1000L);
    grc.setIdToken("someTokenId");
    return new GoogleAccessTokenContext(grc, decodedScope);
  }

  public void removeAccessTokenFromUserProfile(UserProfile userProfile) {
    OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_GOOGLE_ACCESS_TOKEN, false);
    OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_GOOGLE_REFRESH_TOKEN, false);
    userProfile.setAttribute(OAuthConstants.PROFILE_GOOGLE_SCOPE, null);
  }

  public void revokeToken(GoogleAccessTokenContext accessTokenContext) throws OAuthException {
    GoogleRequest<Void> googleRequest = new GoogleRequest<Void>() {

      @Override
      protected Void invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException {
        revokeTokenImpl(accessTokenContext.getTokenData());
        return null;
      }

      @Override
      protected OAuthException createException(IOException cause) {
        return new OAuthException(OAuthExceptionCode.TOKEN_REVOCATION_FAILED, "Error when revoking token", cause);
      }

    };
    googleRequest.executeRequest(accessTokenContext, this);
  }

  public Userinfo obtainUserInfo(GoogleAccessTokenContext accessTokenContext) {
    final Oauth2 oauth2 = getOAuth2Instance(accessTokenContext);

    GoogleRequest<Userinfo> googleRequest = new GoogleRequest<Userinfo>() {

      @Override
      protected Userinfo invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException {
        return oauth2.userinfo().v2().me().get().execute();
      }

      @Override
      protected OAuthException createException(IOException cause) {
        if (cause instanceof HttpResponseException) {
          return new OAuthException(OAuthExceptionCode.ACCESS_TOKEN_ERROR,
                                    "Error when obtaining userInfo: " + cause.getMessage(),
                                    cause);
        } else {
          return new OAuthException(OAuthExceptionCode.IO_ERROR,
                                    "IO Error when obtaining userInfo: " + cause.getMessage(),
                                    cause);
        }
      }

    };
    Userinfo uinfo = googleRequest.executeRequest(accessTokenContext, this);

    if (log.isTraceEnabled()) {
      log.trace("Successfully obtained userInfo from google: " + uinfo);
    }

    return uinfo;
  }

  public Oauth2 getOAuth2Instance(GoogleAccessTokenContext accessTokenContext) {
    GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
    return getOAuth2InstanceImpl(tokenData);
  }

  protected Oauth2 getOAuth2InstanceImpl(GoogleTokenResponse tokenData) {
    GoogleCredential credential = getGoogleCredential(tokenData);
    return new Oauth2.Builder(TRANSPORT, JSON_FACTORY, credential).setApplicationName(applicationName).build();
  }

  public Plus getPlusService(GoogleAccessTokenContext accessTokenContext) {
    GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
    // Build credential from stored token data.
    GoogleCredential credential = getGoogleCredential(tokenData);
    // Create a new authorized API client.
    return new Plus.Builder(TRANSPORT, JSON_FACTORY, credential)
                                                                .setApplicationName(applicationName)
                                                                .build();
  }

  private GoogleCredential getGoogleCredential(GoogleTokenResponse tokenResponse) {
    return new GoogleCredential.Builder().setJsonFactory(JSON_FACTORY)
                                         .setTransport(TRANSPORT)
                                         .setClientSecrets(clientID, clientSecret)
                                         .build()
                                         .setFromTokenResponse(tokenResponse);
  }

  public void refreshToken(GoogleAccessTokenContext accessTokenContext) {
    GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
    if (tokenData.getRefreshToken() == null) {
      throw new OAuthException(OAuthExceptionCode.GOOGLE_ERROR, "Given GoogleTokenResponse does not contain refreshToken");
    }

    try {
      GoogleRefreshTokenRequest refreshTokenRequest = new GoogleRefreshTokenRequest(TRANSPORT,
                                                                                    JSON_FACTORY,
                                                                                    tokenData.getRefreshToken(),
                                                                                    this.clientID,
                                                                                    this.clientSecret);
      GoogleTokenResponse refreshed = refreshTokenRequest.execute();

      // Update only 'accessToken' with new value
      tokenData.setAccessToken(refreshed.getAccessToken());

      if (log.isTraceEnabled()) {
        log.trace("AccessToken refreshed successfully with value " + refreshed.getAccessToken());
      }
    } catch (IOException ioe) {
      throw new OAuthException(OAuthExceptionCode.GOOGLE_ERROR, ioe);
    }
  }

  protected InteractionState<GoogleAccessTokenContext> processOAuthInteractionImpl(HttpServletRequest request,
                                                                                   HttpServletResponse response,
                                                                                   Set<String> scopes)
                                                                                                       throws IOException {
    HttpSession session = request.getSession();
    String state = (String) session.getAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE);

    // Very initial request to portal
    if (StringUtils.isBlank(state)) {
      return initialInteraction(request, response, scopes);
    } else if (state.equals(InteractionState.State.AUTH.name())) {
      GoogleTokenResponse tokenResponse = obtainAccessToken(request);
      GoogleAccessTokenContext accessTokenContext = validateTokenAndUpdateScopes(new GoogleAccessTokenContext(tokenResponse));

      // Clear session attributes
      session.removeAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE);
      session.removeAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE);

      return new InteractionState<>(InteractionState.State.FINISH, accessTokenContext);
    }

    // Likely shouldn't happen...
    return new InteractionState<>(InteractionState.State.valueOf(state), null);
  }

  protected InteractionState<GoogleAccessTokenContext> initialInteraction(HttpServletRequest request,
                                                                          HttpServletResponse response,
                                                                          Set<String> scopes) throws IOException {
    String verificationState = String.valueOf(secureRandomService.getSecureRandom().nextLong());
    String authorizeUrl = new GoogleAuthorizationCodeRequestUrl(clientID, redirectURL, scopes).setState(verificationState)
                                                                                              .setAccessType(accessType)
                                                                                              .build();
    HttpSession session = request.getSession();
    session.setAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE, verificationState);
    session.setAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE, InteractionState.State.AUTH.name());
    response.sendRedirect(authorizeUrl);
    return new InteractionState<>(InteractionState.State.AUTH, null);
  }

  protected GoogleTokenResponse obtainAccessToken(HttpServletRequest request) throws IOException {
    HttpSession session = request.getSession();
    String stateFromSession = (String) session.getAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE);
    String stateFromRequest = request.getParameter(OAuthConstants.STATE_PARAMETER);
    if (stateFromSession == null || stateFromRequest == null || !stateFromSession.equals(stateFromRequest)) {
      throw new OAuthException(OAuthExceptionCode.INVALID_STATE,
                               "Validation of state parameter failed. stateFromSession="
                                   + stateFromSession + ", stateFromRequest=" + stateFromRequest);
    }

    // Check if user didn't permit scope
    String error = request.getParameter(OAuthConstants.ERROR_PARAMETER);
    String code = request.getParameter(OAuthConstants.CODE_PARAMETER);
    if (StringUtils.isBlank(error) && StringUtils.isNotBlank(code)) {
      return new GoogleAuthorizationCodeTokenRequest(TRANSPORT,
                                                     JSON_FACTORY,
                                                     clientID,
                                                     clientSecret,
                                                     code,
                                                     redirectURL).execute();
    } else {
      if (OAuthConstants.ERROR_ACCESS_DENIED.equals(error)) {
        throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, error);
      } else {
        throw new OAuthException(OAuthExceptionCode.UNKNOWN_ERROR, error);
      }
    }
  }

  // Send request to google without any exception handling.
  private void revokeTokenImpl(GoogleTokenResponse tokenData) throws IOException {
    TRANSPORT.createRequestFactory()
             .buildGetRequest(new GenericUrl("https://accounts.google.com/o/oauth2/revoke?token=" + tokenData.getAccessToken()))
             .execute();
    if (log.isTraceEnabled()) {
      log.trace("Revoked token " + tokenData);
    }
  }

}
