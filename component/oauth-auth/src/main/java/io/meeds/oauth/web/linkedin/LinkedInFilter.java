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
package io.meeds.oauth.web.linkedin;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.linkedin.LinkedinAccessTokenContext;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.web.OAuthProviderFilter;

public class LinkedInFilter extends OAuthProviderFilter<LinkedinAccessTokenContext> {
  private static String URL_CURRENT_PROFILE_USER       =
                                                 "https://api.linkedin.com/v2/userinfo";

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
    accessTokenContext.oauth20Service.signRequest(accessTokenContext.accessToken, oauthRequest);

    try {
      Response responses = accessTokenContext.oauth20Service.execute(oauthRequest);
      String body = responses.getBody();
      JSONObject json = new JSONObject(body);
      String firstName = json.getString("given_name");
      String lastName = json.getString("family_name");
      String displayName = json.getString("name");
      String email = json.getString("email");

      String avatar = json.getString("picture");

      OAuthPrincipal<LinkedinAccessTokenContext> principal =
                                                           new OAuthPrincipal<LinkedinAccessTokenContext>(null,
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
