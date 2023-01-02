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
package io.meeds.oauth.provider.facebook.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.meeds.oauth.constant.OAuthConstants;
import io.meeds.oauth.model.AccessTokenContext;
import io.meeds.oauth.model.InteractionState;
import io.meeds.oauth.model.OAuthPrincipal;
import io.meeds.oauth.model.OAuthProviderType;
import io.meeds.oauth.provider.facebook.model.FacebookAccessTokenContext;
import io.meeds.oauth.provider.facebook.model.FacebookPrincipal;
import io.meeds.oauth.provider.facebook.processor.FacebookOAuthProcessor;
import io.meeds.oauth.provider.spi.OAuthProviderFilter;
import io.meeds.oauth.utils.OAuthUtils;

/**
 * Filter for integration with authentication handhsake via Facebook with usage
 * of OAuth2
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FacebookFilter extends OAuthProviderFilter<FacebookAccessTokenContext> {

  @Override
  protected OAuthProviderType<FacebookAccessTokenContext> getOAuthProviderType() {
    return this.getOauthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_FACEBOOK, FacebookAccessTokenContext.class);
  }

  @Override
  protected void initInteraction(HttpServletRequest request, HttpServletResponse response) {
    request.getSession().removeAttribute(OAuthConstants.ATTRIBUTE_AUTH_STATE);
    request.getSession().removeAttribute(OAuthConstants.ATTRIBUTE_VERIFICATION_STATE);
  }

  @Override
  protected OAuthPrincipal<FacebookAccessTokenContext> getOAuthPrincipal(HttpServletRequest request, HttpServletResponse response,
                                                                         InteractionState<FacebookAccessTokenContext> interactionState) {
    FacebookAccessTokenContext accessTokenContext = interactionState.getAccessTokenContext();
    FacebookPrincipal principal = ((FacebookOAuthProcessor) getOauthProviderProcessor()).getPrincipal(accessTokenContext);
    String avatarURL = ((FacebookOAuthProcessor) getOauthProviderProcessor()).getAvatar(accessTokenContext);
    if (principal == null) {
      return null;
    } else {
      return OAuthUtils.convertFacebookPrincipalToOAuthPrincipal(principal,
                                                                 avatarURL,
                                                                 getOAuthProviderType(),
                                                                 accessTokenContext);

    }
  }

  @Override
  protected String obtainCustomScopeIfAvailable(HttpServletRequest httpRequest) {
    String customScope = super.obtainCustomScopeIfAvailable(httpRequest);

    // We need to remove "installed"
    if (customScope != null) {
      StringBuilder result = new StringBuilder();
      String[] scopes = customScope.split(AccessTokenContext.DELIMITER);
      boolean first = true;
      for (String scope : scopes) {
        if (!scope.equals("installed")) {
          if (!first) {
            result.append(AccessTokenContext.DELIMITER);
          }
          first = false;
          result.append(scope);
        }
      }
      customScope = result.toString();
    }
    return customScope;
  }
}
