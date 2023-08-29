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
package io.meeds.oauth.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.services.oauth2.model.Userinfo;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.facebook.FacebookAccessTokenContext;
import io.meeds.oauth.google.GoogleAccessTokenContext;
import io.meeds.oauth.openid.OpenIdAccessTokenContext;
import io.meeds.oauth.social.FacebookPrincipal;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.twitter.TwitterAccessTokenContext;

/**
 * Various util methods
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthUtils {

  // Private constructor for utils class
  private OAuthUtils() {
  }

  // Converting objects

  public static OAuthPrincipal<FacebookAccessTokenContext> convertFacebookPrincipalToOAuthPrincipal(FacebookPrincipal facebookPrincipal,
                                                                                                    String avatar,
                                                                                                    OAuthProviderType<FacebookAccessTokenContext> facebookProviderType,
                                                                                                    FacebookAccessTokenContext fbAccessTokenContext) {
    return new OAuthPrincipal<>(facebookPrincipal.getUsername(),
                                facebookPrincipal.getFirstName(),
                                facebookPrincipal.getLastName(),
                                facebookPrincipal.getAttribute("name"),
                                facebookPrincipal.getEmail(),
                                avatar,
                                fbAccessTokenContext,
                                facebookProviderType);
  }

  public static OAuthPrincipal<TwitterAccessTokenContext> convertTwitterUserToOAuthPrincipal(twitter4j.User twitterUser,
                                                                                             TwitterAccessTokenContext accessToken,
                                                                                             OAuthProviderType<TwitterAccessTokenContext> twitterProviderType) {
    String fullName = twitterUser.getName();
    String firstName;
    String lastName;
    String avatar = twitterUser.getBiggerProfileImageURL();

    int spaceIndex = fullName.lastIndexOf(' ');

    if (spaceIndex != -1) {
      firstName = fullName.substring(0, spaceIndex);
      lastName = fullName.substring(spaceIndex + 1);
    } else {
      firstName = fullName;
      lastName = null;
    }

    return new OAuthPrincipal<>(twitterUser.getScreenName(),
                                firstName,
                                lastName,
                                fullName,
                                null,
                                avatar,
                                accessToken,
                                twitterProviderType);
  }

  public static OAuthPrincipal<GoogleAccessTokenContext> convertGoogleInfoToOAuthPrincipal(Userinfo userInfo,
                                                                                           GoogleAccessTokenContext accessToken,
                                                                                           OAuthProviderType<GoogleAccessTokenContext> googleProviderType) {
    // Assume that username is first part of email
    String email = userInfo.getEmail();
    String username = email != null ? email.substring(0, email.indexOf('@')) : userInfo.getGivenName();
    return new OAuthPrincipal<>(username,
                                userInfo.getGivenName(),
                                userInfo.getFamilyName(),
                                userInfo.getName(),
                                userInfo.getEmail(),
                                userInfo.getPicture(),
                                accessToken,
                                googleProviderType);
  }

  public static OAuthPrincipal<OpenIdAccessTokenContext> convertOpenIdInfoToOAuthPrincipal(JSONObject userInfo,
                                                                                           OpenIdAccessTokenContext accessTokenContext,
                                                                                           OAuthProviderType<OpenIdAccessTokenContext> openIdProviderType) {
    try {
      String email = userInfo.has(OAuthConstants.EMAIL_ATTRIBUTE) ? userInfo.getString(OAuthConstants.EMAIL_ATTRIBUTE)
                                                                  : accessTokenContext.getCustomClaims()
                                                                                      .get(OAuthConstants.EMAIL_ATTRIBUTE);
      if (email == null) {
        throw new OAuthException(OAuthExceptionCode.UNKNOWN_ERROR,
                                 "Error when reading user info, the email is not present, so we are unable to identify the user.");
      }
      String givenName =
                       userInfo.has(OAuthConstants.GIVEN_NAME_ATTRIBUTE) ? userInfo.getString(OAuthConstants.GIVEN_NAME_ATTRIBUTE)
                                                                         : accessTokenContext.getCustomClaims()
                                                                                             .get(OAuthConstants.GIVEN_NAME_ATTRIBUTE);
      String familyName =
                        userInfo.has(OAuthConstants.FAMILY_NAME_ATTRIBUTE) ? userInfo.getString(OAuthConstants.FAMILY_NAME_ATTRIBUTE)
                                                                           : accessTokenContext.getCustomClaims()
                                                                                               .get(OAuthConstants.FAMILY_NAME_ATTRIBUTE);
      String name = userInfo.has(OAuthConstants.NAME_ATTRIBUTE) ? userInfo.getString(OAuthConstants.NAME_ATTRIBUTE)
                                                                : givenName + " " + familyName;
      String picture =
                     userInfo.has(OAuthConstants.PICTURE_ATTRIBUTE) ? userInfo.getString(OAuthConstants.PICTURE_ATTRIBUTE) : null;
      return new OAuthPrincipal<>(null, givenName, familyName, name, email, picture, accessTokenContext, openIdProviderType);
    } catch (JSONException jsonException) {
      throw new OAuthException(OAuthExceptionCode.ACCESS_TOKEN_ERROR, "Error during user info reading: response format is ko");
    }
  }

  public static User convertOAuthPrincipalToGateInUser(OAuthPrincipal principal) {
    User gateinUser = new UserImpl(OAuthUtils.refineUserName(principal.getUserName()));
    gateinUser.setFirstName(principal.getFirstName());
    gateinUser.setLastName(principal.getLastName());
    gateinUser.setEmail(principal.getEmail());
    gateinUser.setDisplayName(principal.getDisplayName());
    return gateinUser;
  }

  public static String getURLToRedirectAfterLinkAccount(HttpServletRequest request, HttpSession session) {
    String urlToRedirect = (String) session.getAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT);
    if (urlToRedirect == null) {
      urlToRedirect = request.getContextPath();
    } else {
      session.removeAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT);
    }

    return urlToRedirect;
  }

  // HTTP related utils

  /**
   * Given a {@link java.util.Map} of params, construct a query string
   *
   * @param  params parameters for query
   * @return        query string
   */
  public static String createQueryString(Map<String, String> params) {
    StringBuilder queryString = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      String paramName = entry.getKey();
      String paramValue = entry.getValue();
      if (first) {
        first = false;
      } else {
        queryString.append("&");
      }
      queryString.append(paramName).append("=");
      String encodedParamValue;
      try {
        if (paramValue == null)
          throw new RuntimeException("paramValue is null for paramName=" + paramName);
        encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new OAuthException(OAuthExceptionCode.UNKNOWN_ERROR, e);
      }
      queryString.append(encodedParamValue);
    }
    return queryString.toString();
  }

  public static String encodeParam(String param) {
    try {
      return URLEncoder.encode(param, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new OAuthException(OAuthExceptionCode.UNKNOWN_ERROR, uee);
    }
  }

  /**
   * Whole HTTP response as String from given URLConnection
   *
   * @param  connection
   * @return            whole HTTP response as String
   */
  public static HttpResponseContext readUrlContent(URLConnection connection) throws IOException {
    StringBuilder result = new StringBuilder();

    HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
    int statusCode = httpURLConnection.getResponseCode();

    Reader reader = null;
    try {
      try {
        reader = new InputStreamReader(connection.getInputStream());
      } catch (IOException ioe) {
        if (httpURLConnection.getErrorStream() != null) {
          reader = new InputStreamReader(httpURLConnection.getErrorStream());
        } else {
          return new HttpResponseContext(statusCode, "");
        }
      }

      char[] buffer = new char[50];
      int nrOfChars;
      while ((nrOfChars = reader.read(buffer)) != -1) {
        result.append(buffer, 0, nrOfChars);
      }

      String response = result.toString();
      return new HttpResponseContext(statusCode, response);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * Decode given String to map. For example for input:
   * {@code accessToken=123456&expires=20071458} it returns map with two keys
   * "accessToken" and "expires" and their corresponding values
   *
   * @param  encodedData
   * @return             map with output data
   */
  public static Map<String, String> formUrlDecode(String encodedData) {
    Map<String, String> params = new HashMap<String, String>();
    String[] elements = encodedData.split("&");
    for (String element : elements) {
      String[] pair = element.split("=");
      if (pair.length == 2) {
        String paramName = pair[0];
        String paramValue;
        try {
          paramValue = URLDecoder.decode(pair[1], "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
        params.put(paramName, paramValue);
      } else {
        throw new RuntimeException("Unexpected name-value pair in response: " + element);
      }
    }
    return params;
  }

  public static String refineUserName(String username) {
    final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012456789._";
    final char replaced = '_';

    if (username == null || username.isEmpty()) {
      return "";
    }

    final char[] chars = username.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (ALLOWED_CHARACTERS.indexOf(chars[i]) == -1) {
        chars[i] = replaced;
      }
    }
    return new String(chars);
  }
}
