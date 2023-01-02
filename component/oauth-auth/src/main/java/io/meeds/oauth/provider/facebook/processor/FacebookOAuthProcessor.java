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
package io.meeds.oauth.provider.facebook.processor;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.SecureRandomService;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

import io.meeds.oauth.constant.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.model.HttpResponseContext;
import io.meeds.oauth.model.InteractionState;
import io.meeds.oauth.model.OAuthPrincipal;
import io.meeds.oauth.provider.facebook.constant.FacebookConstants;
import io.meeds.oauth.provider.facebook.model.FacebookAccessTokenContext;
import io.meeds.oauth.provider.facebook.model.FacebookPrincipal;
import io.meeds.oauth.provider.spi.OAuthProviderProcessor;
import io.meeds.oauth.utils.OAuthPersistenceUtils;
import io.meeds.oauth.utils.OAuthUtils;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FacebookOAuthProcessor extends OAuthProviderProcessor<FacebookAccessTokenContext> {

  private static final Log    LOG = ExoLogger.getLogger(FacebookOAuthProcessor.class);

  private String              clientId;

  private String              clientSecret;

  private String              scope;

  private String              redirectURL;

  private SecureRandomService secureRandomService;

  private int                 chunkLength;

  public FacebookOAuthProcessor(ExoContainerContext context,
                                SecureRandomService secureRandomService,
                                CodecInitializer codecInitializer,
                                InitParams params)
      throws TokenServiceInitializationException {
    super(codecInitializer);
    this.clientId = params.getValueParam("clientId").getValue();
    this.clientSecret = params.getValueParam("clientSecret").getValue();
    this.scope = params.getValueParam("scope").getValue();
    this.redirectURL = params.getValueParam("redirectURL").getValue();

    if (StringUtils.isBlank(this.redirectURL)) {
      this.redirectURL = "http://localhost:8080/" + context.getName() + OAuthConstants.FACEBOOK_AUTHENTICATION_URL_PATH;
    } else {
      this.redirectURL = redirectURL.replaceAll("@@portal.container.name@@", context.getName());
    }

    if (StringUtils.isBlank(this.scope)) {
      this.scope = "email";
    }
    this.chunkLength = OAuthPersistenceUtils.getChunkLength(params);
    this.secureRandomService = secureRandomService;
  }

  @Override
  public <C> C getAuthorizedSocialApiObject(FacebookAccessTokenContext accessToken, Class<C> socialApiObjectType) {
    // oAut
    return null;
  }

  public boolean initialInteraction(HttpServletResponse response,
                                    String verificationState) throws IOException {
    Map<String, String> params = new HashMap<>();
    params.put(OAuthConstants.REDIRECT_URI_PARAMETER, redirectURL);
    params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientId);
    params.put(OAuthConstants.STATE_PARAMETER, verificationState);

    if (scope != null) {
      params.put(OAuthConstants.SCOPE_PARAMETER, scope);
    }

    String location = new StringBuilder(FacebookConstants.SERVICE_URL).append("?")
                                                                      .append(OAuthUtils.createQueryString(params))
                                                                      .toString();

    response.sendRedirect(location);
    return false;
  }

  public String getAccessToken(HttpServletRequest request) throws OAuthException {
    String authorizationCode = request.getParameter(OAuthConstants.CODE_PARAMETER);
    if (authorizationCode == null) {
      LOG.error("Authorization code parameter not found");
      handleCodeRequestError(request);
      return null;
    }

    String stateFromSession = (String) request.getSession().getAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE);
    String stateFromRequest = request.getParameter(OAuthConstants.STATE_PARAMETER);
    if (stateFromSession == null || stateFromRequest == null || !stateFromSession.equals(stateFromRequest)) {
      throw new OAuthException(OAuthExceptionCode.INVALID_STATE,
                               "Validation of state parameter failed. stateFromSession="
                                   + stateFromSession + ", stateFromRequest=" + stateFromRequest);
    }

    return new FacebookRequest<String>() {
      @Override
      protected URL createURL() throws IOException {
        return sendAccessTokenRequest(authorizationCode);
      }

      @Override
      protected String parseResponse(String httpResponse) throws JSONException {
        JSONObject json = new JSONObject(httpResponse);
        return json.getString(OAuthConstants.ACCESS_TOKEN_PARAMETER);
      }
    }.executeRequest();
  }

  protected URL sendAccessTokenRequest(String authorizationCode) throws IOException {
    String returnUri = redirectURL;

    Map<String, String> params = new HashMap<>();
    params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnUri);
    params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientId);
    params.put(OAuthConstants.CLIENT_SECRET_PARAMETER, clientSecret);
    params.put(OAuthConstants.CODE_PARAMETER, authorizationCode);

    String location = new StringBuilder(FacebookConstants.ACCESS_TOKEN_ENDPOINT_URL).append("?")
                                                                                    .append(OAuthUtils.createQueryString(params))
                                                                                    .toString();
    return new URL(location);
  }

  public Set<String> getScopes(String accessToken) {
    return new FacebookRequest<Set<String>>() {

      @Override
      protected URL createURL() throws IOException {
        String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("/permissions")
                                                                                    .append("?access_token=")
                                                                                    .append(URLEncoder.encode(accessToken,
                                                                                                              StandardCharsets.UTF_8))
                                                                                    .toString();
        return new URL(urlString);
      }

      @Override
      protected Set<String> parseResponse(String httpResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(httpResponse);

        JSONArray json = jsonObject.getJSONArray("data");
        if (json != null) {
          jsonObject = json.optJSONObject(0);
          if (jsonObject != null) {
            String[] names = JSONObject.getNames(jsonObject);
            if (names == null) {
              return Collections.emptySet();
            } else {
              return new HashSet<>(Arrays.asList(names));
            }
          }
        }
        return Collections.emptySet();
      }
    }.executeRequest();
  }

  public FacebookPrincipal getPrincipal(String accessToken) {
    return new FacebookRequest<FacebookPrincipal>() {

      @Override
      protected URL createURL() throws IOException {
        String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("?access_token=")
                                                                                    .append(URLEncoder.encode(accessToken,
                                                                                                              StandardCharsets.UTF_8))
                                                                                    .toString();
        return new URL(urlString);
      }

      @Override
      protected FacebookPrincipal parseResponse(String httpResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(httpResponse);

        FacebookPrincipal facebookPrincipal = new FacebookPrincipal();
        facebookPrincipal.setAccessToken(accessToken);
        facebookPrincipal.setId(jsonObject.getString("id"));
        facebookPrincipal.setName(jsonObject.optString("name"));
        facebookPrincipal.setUsername(jsonObject.optString("username"));
        facebookPrincipal.setFirstName(jsonObject.optString("first_name"));
        facebookPrincipal.setLastName(jsonObject.optString("last_name"));
        facebookPrincipal.setGender(jsonObject.optString("gender"));
        facebookPrincipal.setTimezone(jsonObject.optString("timezone"));
        facebookPrincipal.setLocale(jsonObject.optString("locale"));
        facebookPrincipal.setEmail(jsonObject.optString("email"));
        facebookPrincipal.setJsonObject(jsonObject);

        // This could happen with Facebook testing users
        if (facebookPrincipal.getUsername() == null || facebookPrincipal.getUsername().length() == 0) {
          facebookPrincipal.setUsername(facebookPrincipal.getId());
        }

        return facebookPrincipal;
      }

    }.executeRequest();
  }

  public String getUserAvatarURL(String accessToken) {
    return new FacebookRequest<String>() {
      @Override
      protected URL createURL() throws IOException {
        String urlString =
                         new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("/picture?type=large&format=json&redirect=false")
                                                                                  .append("&access_token=")
                                                                                  .append(URLEncoder.encode(accessToken, "UTF-8"))
                                                                                  .toString();
        return new URL(urlString);
      }

      @Override
      protected String parseResponse(String httpResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(httpResponse);
        if (jsonObject.has("data")) {
          JSONObject data = jsonObject.getJSONObject("data");
          if (data.has("url")) {
            return data.getString("url");
          }
        }
        return "";
      }

    }.executeRequest();
  }

  public InteractionState<FacebookAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest,
                                                                              HttpServletResponse httpResponse,
                                                                              String scope) throws IOException {
    return processOAuthInteractionImpl(httpRequest,
                                       httpResponse);
  }

  public InteractionState<FacebookAccessTokenContext> processOAuthInteraction(HttpServletRequest httpRequest,
                                                                              HttpServletResponse httpResponse) throws IOException {
    return processOAuthInteractionImpl(httpRequest, httpResponse);
  }

  public void saveAccessTokenAttributesToUserProfile(UserProfile userProfile,
                                                     FacebookAccessTokenContext accessTokenContext) {
    String realAccessToken = accessTokenContext.getAccessToken();
    String encodedAccessToken = encodeString(realAccessToken);

    // Encoded accessToken could be longer than 255 characters. So we need to
    // split it
    OAuthPersistenceUtils.saveLongAttribute(encodedAccessToken,
                                            userProfile,
                                            OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN,
                                            true,
                                            chunkLength);

    userProfile.setAttribute(OAuthConstants.PROFILE_FACEBOOK_SCOPE, accessTokenContext.getScopesAsString());
  }

  public FacebookAccessTokenContext getAccessTokenFromUserProfile(UserProfile userProfile) {
    String encodedAccessToken = OAuthPersistenceUtils.getLongAttribute(userProfile,
                                                                       OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN,
                                                                       true);

    // We don't have token in userProfile
    if (encodedAccessToken == null) {
      return null;
    }

    String accessToken = decodeString(encodedAccessToken);
    String scopesAsString = userProfile.getAttribute(OAuthConstants.PROFILE_FACEBOOK_SCOPE);
    return new FacebookAccessTokenContext(accessToken, scopesAsString);
  }

  public void removeAccessTokenFromUserProfile(UserProfile userProfile) {
    OAuthPersistenceUtils.removeLongAttribute(userProfile, OAuthConstants.PROFILE_FACEBOOK_ACCESS_TOKEN, true);
  }

  public void revokeToken(FacebookAccessTokenContext accessToken) {
    String realAccessToken = accessToken.getAccessToken();
    revokeToken(realAccessToken);
  }

  public FacebookAccessTokenContext validateTokenAndUpdateScopes(FacebookAccessTokenContext accessToken) throws OAuthException {
    Set<String> scopes = getScopes(accessToken.getAccessToken());
    return new FacebookAccessTokenContext(accessToken.getAccessToken(), scopes);
  }

  protected InteractionState<FacebookAccessTokenContext> processOAuthInteractionImpl(HttpServletRequest httpRequest,
                                                                                     HttpServletResponse httpResponse) throws IOException {
    HttpSession session = httpRequest.getSession();
    String state = (String) session.getAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE);

    // Very initial request to portal
    if (StringUtils.isBlank(state)) {
      String verificationState = String.valueOf(secureRandomService.getSecureRandom().nextLong());
      initialInteraction(httpResponse, verificationState);
      state = InteractionState.State.AUTH.name();
      session.setAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE, state);
      session.setAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE, verificationState);
      return new InteractionState<>(InteractionState.State.valueOf(state), null);
    }

    // We are authenticated in Facebook and our app is authorized. Finish OAuth
    // handshake by obtaining accessToken and initial info
    if (state.equals(InteractionState.State.AUTH.name())) {
      String accessToken = getAccessToken(httpRequest);
      if (accessToken == null) {
        throw new OAuthException(OAuthExceptionCode.FACEBOOK_ERROR, "AccessToken was null");
      } else {
        Set<String> scopes = getScopes(accessToken);
        state = InteractionState.State.FINISH.name();

        // Clear session attributes
        session.removeAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE);
        session.removeAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE);

        FacebookAccessTokenContext accessTokenContext = new FacebookAccessTokenContext(accessToken, scopes);
        return new InteractionState<>(InteractionState.State.valueOf(state), accessTokenContext);
      }
    }

    // Likely shouldn't happen...
    return new InteractionState<>(InteractionState.State.valueOf(state), null);
  }

  public FacebookPrincipal getPrincipal(FacebookAccessTokenContext accessTokenContext) {
    String accessToken = accessTokenContext.getAccessToken();
    return getPrincipal(accessToken);
  }

  public String getAvatar(FacebookAccessTokenContext accessTokenContext) {
    String accessToken = accessTokenContext.getAccessToken();
    return getUserAvatarURL(accessToken);
  }

  private void handleCodeRequestError(HttpServletRequest request) {
    // Log all possible error parameters
    StringBuilder errorBuilder = new StringBuilder();
    Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String paramName = paramNames.nextElement();
      if (paramName.startsWith("error")) {
        errorBuilder.append(paramName + ": " + request.getParameter(paramName) + System.getProperty("line.separator"));
      }
    }
    String errorMessage = errorBuilder.toString();
    String error = request.getParameter(OAuthConstants.ERROR_PARAMETER);
    if (error != null && OAuthConstants.ERROR_ACCESS_DENIED.equals(error)) {
      throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, errorMessage);
    }
    throw new OAuthException(OAuthExceptionCode.FACEBOOK_ERROR, errorMessage);
  }

  private void revokeToken(String accessToken) {
    try {
      String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("/permissions?access_token=")
                                                                                  .append(URLEncoder.encode(accessToken, "UTF-8"))
                                                                                  .append("&method=delete")
                                                                                  .toString();
      URL revokeUrl = new URL(urlString);
      HttpResponseContext revokeContent = OAuthUtils.readUrlContent(revokeUrl.openConnection());
      if (revokeContent.responseCode() != 200) {
        throw new OAuthException(OAuthExceptionCode.TOKEN_REVOCATION_FAILED,
                                 "Error when revoking token. Http response code: " + revokeContent.responseCode()
                                     + ", Error details: " + revokeContent.response());
      }
    } catch (IOException ioe) {
      throw new OAuthException(OAuthExceptionCode.TOKEN_REVOCATION_FAILED, "Error when revoking token", ioe);
    }
  }

}
