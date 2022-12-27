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
package org.gatein.security.oauth.web.linkedin;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gatein.security.oauth.common.OAuthConstants;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.linkedin.LinkedinAccessTokenContext;
import org.gatein.security.oauth.spi.InteractionState;
import org.gatein.security.oauth.spi.OAuthPrincipal;
import org.gatein.security.oauth.spi.OAuthProviderType;
import org.gatein.security.oauth.web.OAuthProviderFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

public class LinkedInFilter extends OAuthProviderFilter<LinkedinAccessTokenContext> {
  private static String URL_CURRENT_PROFILE_USER       =
                                                 "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))";

  private static String URL_CURRENT_PROFILE_USER_EMAIL =
                                                       "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))";

  @Override
  protected OAuthProviderType<LinkedinAccessTokenContext> getOAuthProvider() {
    return getOauthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_LINKEDIN, LinkedinAccessTokenContext.class);
  }

  @Override
  protected void initInteraction(HttpServletRequest request, HttpServletResponse response) {
    request.getSession().removeAttribute(OAuthConstants.ATTRIBUTE_LINKEDIN_REQUEST_TOKEN);
  }

  @Override
  protected OAuthPrincipal<LinkedinAccessTokenContext> getOAuthPrincipal(HttpServletRequest request, HttpServletResponse response,
                                                                         InteractionState<LinkedinAccessTokenContext> interactionState) {
    LinkedinAccessTokenContext accessTokenContext = interactionState.getAccessTokenContext();

    OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, URL_CURRENT_PROFILE_USER);
    OAuthRequest oauthRequest1 = new OAuthRequest(Verb.GET, URL_CURRENT_PROFILE_USER_EMAIL);
    accessTokenContext.oauth20Service.signRequest(accessTokenContext.accessToken, oauthRequest);
    accessTokenContext.oauth20Service.signRequest(accessTokenContext.accessToken, oauthRequest1);
    oauthRequest.addHeader("x-li-format", "json");
    oauthRequest.addHeader("Accept-Language", "ru-RU");
    oauthRequest1.addHeader("x-li-format", "json");
    oauthRequest1.addHeader("Accept-Language", "ru-RU");
    try {
      Response responses = accessTokenContext.oauth20Service.execute(oauthRequest);
      String body = responses.getBody();
      JSONObject json = new JSONObject(body);
      Response responses1 = accessTokenContext.oauth20Service.execute(oauthRequest1);
      String body1 = responses1.getBody();
      JSONObject json1 = new JSONObject(body1);
      String id = json.getString("id");
      String firstName = json.getString("firstName").substring(23, json.getString("firstName").indexOf("\"},"));
      String lastName = json.getString("lastName").substring(23, json.getString("lastName").indexOf("\"},"));
      String displayName = firstName + " " + lastName;
      String email = json1.getString("elements").substring(71, json1.getString("elements").indexOf("\"}}]"));

      String avatar = json.optString("pictureUrl");
      JSONObject profilePictures = json.optJSONObject("pictureUrls");
      if (profilePictures != null) {
        JSONArray arr = profilePictures.optJSONArray("values");
        if (arr != null && arr.length() > 0) {
          avatar = arr.getString(0);
        }
      }

      OAuthPrincipal<LinkedinAccessTokenContext> principal =
                                                           new OAuthPrincipal<LinkedinAccessTokenContext>(id,
                                                                                                          firstName,
                                                                                                          lastName,
                                                                                                          displayName,
                                                                                                          email,
                                                                                                          avatar,
                                                                                                          accessTokenContext,
                                                                                                          getOAuthProvider());

      return principal;

    } catch (JSONException | InterruptedException | ExecutionException | IOException ex) {
      throw new OAuthException(OAuthExceptionCode.LINKEDIN_ERROR, "Error when obtaining user", ex);
    }
  }
}
