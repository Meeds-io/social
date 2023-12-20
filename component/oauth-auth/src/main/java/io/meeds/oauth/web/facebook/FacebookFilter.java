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
package io.meeds.oauth.web.facebook;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.facebook.FacebookAccessTokenContext;
import io.meeds.oauth.facebook.GateInFacebookProcessor;
import io.meeds.oauth.social.FacebookPrincipal;
import io.meeds.oauth.spi.AccessTokenContext;
import io.meeds.oauth.spi.InteractionState;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.utils.OAuthUtils;
import io.meeds.oauth.web.OAuthProviderFilter;

/**
 * Filter for integration with authentication handhsake via Facebook with usage
 * of OAuth2
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FacebookFilter extends OAuthProviderFilter<FacebookAccessTokenContext> {

  @Override
  protected OAuthProviderType<FacebookAccessTokenContext> getOAuthProvider() {
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
    FacebookPrincipal principal = ((GateInFacebookProcessor) getOauthProviderProcessor()).getPrincipal(accessTokenContext);
    String avatarURL = ((GateInFacebookProcessor) getOauthProviderProcessor()).getAvatar(accessTokenContext);

    if (principal == null) {
      log.error("Principal was null");
      return null;
    } else {
      if (log.isTraceEnabled()) {
        log.trace("Finished Facebook OAuth2 flow with state: " + interactionState);
        log.trace("Facebook accessToken: " + principal.getAccessToken());
      }

      OAuthPrincipal<FacebookAccessTokenContext> oauthPrincipal = OAuthUtils.convertFacebookPrincipalToOAuthPrincipal(
                                                                                                                      principal,
                                                                                                                      avatarURL,
                                                                                                                      getOAuthProvider(),
                                                                                                                      accessTokenContext);

      return oauthPrincipal;
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
