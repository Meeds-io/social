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
package io.meeds.oauth.social;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.utils.HttpResponseContext;
import io.meeds.oauth.utils.OAuthUtils;

/**
 * Processor to perform Facebook interaction
 *
 * @author Anil Saldhana
 * @since  Sep 22, 2011
 */
public class FacebookProcessor {

  private static Log log   = ExoLogger.getLogger(FacebookProcessor.class);

  protected boolean  trace = log.isTraceEnabled();

  protected String   clientID;

  protected String   clientSecret;

  protected String   scope;

  protected String   returnURL;

  public FacebookProcessor(String clientID, String clientSecret, String scope, String returnURL) {
    super();
    this.clientID = clientID;
    this.clientSecret = clientSecret;
    this.scope = scope;
    this.returnURL = returnURL;
  }

  public boolean initialInteraction(HttpServletRequest request, HttpServletResponse response,
                                    String verificationState) throws IOException {
    Map<String, String> params = new HashMap<String, String>();
    params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnURL);
    params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);
    params.put(OAuthConstants.STATE_PARAMETER, verificationState);

    if (scope != null) {
      params.put(OAuthConstants.SCOPE_PARAMETER, scope);
    }

    String location = new StringBuilder(FacebookConstants.SERVICE_URL).append("?")
                                                                      .append(OAuthUtils.createQueryString(params))
                                                                      .toString();

    if (trace)
      log.trace("Redirect:" + location);
    response.sendRedirect(location);
    return false;
  }

  public String getAccessToken(HttpServletRequest request, HttpServletResponse response) throws OAuthException {
    String authorizationCode = request.getParameter(OAuthConstants.CODE_PARAMETER);
    if (authorizationCode == null) {
      log.error("Authorization code parameter not found");
      handleCodeRequestError(request, response);
      return null;
    }

    String stateFromSession = (String) request.getSession().getAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE);
    String stateFromRequest = request.getParameter(OAuthConstants.STATE_PARAMETER);
    if (stateFromSession == null || stateFromRequest == null || !stateFromSession.equals(stateFromRequest)) {
      throw new OAuthException(OAuthExceptionCode.INVALID_STATE,
                               "Validation of state parameter failed. stateFromSession="
                                   + stateFromSession + ", stateFromRequest=" + stateFromRequest);
    }

    String accessToken = new FacebookRequest<String>() {

      @Override
      protected URL createURL(String authorizationCode) throws IOException {
        return sendAccessTokenRequest(authorizationCode);
      }

      @Override
      protected String parseResponse(String httpResponse) throws JSONException {
        JSONObject json = new JSONObject(httpResponse);
        String accessToken = json.getString(OAuthConstants.ACCESS_TOKEN_PARAMETER);
        String expires = json.getString(FacebookConstants.EXPIRES);
        if (trace)
          log.trace("Access Token=" + accessToken + " :: Expires=" + expires);

        return accessToken;
      }

    }.executeRequest(authorizationCode);

    return accessToken;
  }

  protected URL sendAccessTokenRequest(String authorizationCode) throws IOException {
    String returnUri = returnURL;

    Map<String, String> params = new HashMap<String, String>();
    params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnUri);
    params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);
    params.put(OAuthConstants.CLIENT_SECRET_PARAMETER, clientSecret);
    params.put(OAuthConstants.CODE_PARAMETER, authorizationCode);

    String location = new StringBuilder(FacebookConstants.ACCESS_TOKEN_ENDPOINT_URL).append("?")
                                                                                    .append(OAuthUtils.createQueryString(params))
                                                                                    .toString();

    if (trace)
      log.trace("AccessToken Request=" + location);
    return new URL(location);
  }

  public Set<String> getScopes(String accessToken) {
    Set<String> scopes = new FacebookRequest<Set<String>>() {

      @Override
      protected URL createURL(String accessToken) throws IOException {
        String urlString =
                         new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("/permissions")
                                                                                  .append("?access_token=")
                                                                                  .append(URLEncoder.encode(accessToken, "UTF-8"))
                                                                                  .toString();
        if (trace)
          log.trace("Read info about available scopes:" + urlString);

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
            if (names != null) {
              Set<String> scopes = new HashSet<String>();
              for (String name : names) {
                scopes.add(name);
              }
              return scopes;
            }
          }
        }

        return new HashSet<String>();
      }

    }.executeRequest(accessToken);

    return scopes;
  }

  public FacebookPrincipal getPrincipal(String accessToken) {
    FacebookPrincipal facebookPrincipal = new FacebookRequest<FacebookPrincipal>() {

      private String accessToken;

      @Override
      protected URL createURL(String accessToken) throws IOException {
        String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("?access_token=")
                                                                                    .append(URLEncoder.encode(accessToken,
                                                                                                              "UTF-8"))
                                                                                    .toString();
        if (trace)
          log.trace("Profile read:" + urlString);

        // Little hack but sufficient for now
        this.accessToken = accessToken;

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

    }.executeRequest(accessToken);

    return facebookPrincipal;
  }

  public String getUserAvatarURL(String accessToken) {
    return new FacebookRequest<String>() {
      @Override
      protected URL createURL(String accessToken) throws IOException {
        String urlString =
                         new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("/picture?type=large&format=json&redirect=false")
                                                                                  .append("&access_token=")
                                                                                  .append(URLEncoder.encode(accessToken, "UTF-8"))
                                                                                  .toString();
        if (trace)
          log.trace("Profile read:" + urlString);

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

    }.executeRequest(accessToken);
  }

  public void revokeToken(String accessToken) {
    try {
      String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("/permissions?access_token=")
                                                                                  .append(URLEncoder.encode(accessToken, "UTF-8"))
                                                                                  .append("&method=delete")
                                                                                  .toString();
      URL revokeUrl = new URL(urlString);
      HttpResponseContext revokeContent = OAuthUtils.readUrlContent(revokeUrl.openConnection());
      if (revokeContent.getResponseCode() != 200) {
        throw new OAuthException(OAuthExceptionCode.TOKEN_REVOCATION_FAILED,
                                 "Error when revoking token. Http response code: " + revokeContent.getResponseCode()
                                     + ", Error details: " + revokeContent.getResponse());
      }

      if (log.isTraceEnabled()) {
        log.trace("Successfully revoked facebook accessToken " + accessToken + ", revokeContent=" + revokeContent);
      }
    } catch (IOException ioe) {
      throw new OAuthException(OAuthExceptionCode.TOKEN_REVOCATION_FAILED, "Error when revoking token", ioe);
    }
  }

  private void handleCodeRequestError(HttpServletRequest request, HttpServletResponse response) {
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
    if (error != null) {
      if (OAuthConstants.ERROR_ACCESS_DENIED.equals(error)) {
        throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, errorMessage);
      }
    }

    throw new OAuthException(OAuthExceptionCode.FACEBOOK_ERROR, errorMessage);
  }

}
