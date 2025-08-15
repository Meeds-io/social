/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.web.openid;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.openid.OpenIdAccessTokenContext;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.security.AuthenticationRegistry;
import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;

import java.io.IOException;

public class OpenIdAuthenticationFilter extends AbstractSSOInterceptor {
  private static Log log = ExoLogger.getLogger(OpenIdAuthenticationFilter.class);
  private AuthenticationRegistry authenticationRegistry;
  private OAuthProviderTypeRegistry oAuthProviderTypeRegistry;
  private OAuthProviderType oAuthProviderType;

  private boolean openIdEnabled;
  @Override
  protected void initImpl() {
    this.openIdEnabled = Boolean.parseBoolean(System.getProperty("exo.oauth.openid.enabled"));
    if (this.openIdEnabled) {
      authenticationRegistry = getExoContainer().getComponentInstanceOfType(AuthenticationRegistry.class);
      oAuthProviderTypeRegistry =
          getExoContainer().getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
      oAuthProviderType = oAuthProviderTypeRegistry.getOAuthProvider(OAuthConstants.OAUTH_PROVIDER_KEY_OPEN_ID,
                                                                    OpenIdAccessTokenContext.class);
    }
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws
                                                                                                                IOException,
                                                                                                                ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    // Simply continue with request if we are already authenticated
    if (!this.openIdEnabled || request.getRemoteUser() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    // Simply continue with request if we are in the middle of registration
    // process
    User oauthAuthenticatedUser =
        (User) authenticationRegistry.getAttributeOfClient(request,
                                                           OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    if (oauthAuthenticatedUser != null) {
      filterChain.doFilter(request, response);
      return;
    }

    User jaasAuthenticatedUser =
        (User) authenticationRegistry.getAttributeOfClient(request,
                                                           OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS);
    if (jaasAuthenticatedUser != null) {
      filterChain.doFilter(request, response);
      return;
    }

    OAuthPrincipal principal =
        (OAuthPrincipal) authenticationRegistry.getAttributeOfClient(request,
                                                                     OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
    if (principal != null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!checkAccessTokenInCookies(request,response)) {
      filterChain.doFilter(request, response);
    }
  }

  private boolean checkAccessTokenInCookies(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("OPENID_ACCESS_TOKEN")) {
          try {
            saveInitialURI(request);
            String contextPath = request.getContextPath();
            String initialUri = request.getParameter("initialURI");
            if (StringUtils.isBlank(initialUri)) {
              initialUri = contextPath;
            }
            String initOauthURI = oAuthProviderType.getInitOAuthURL(contextPath,initialUri);
            response.sendRedirect(initOauthURI);
            return true;
          } catch (IOException ex) {
            log.error("Error when initializing OAuth flow with: " + ex.getMessage(), ex);
          }
        }
      }
    }
    return false;
  }
  private void saveInitialURI(HttpServletRequest request) {
    String initialURI = request.getParameter("initialURI");
    if (initialURI != null) {
      request.getSession().setAttribute(OAuthConstants.ATTRIBUTE_URL_TO_REDIRECT_AFTER_LINK_SOCIAL_ACCOUNT, initialURI);
    }
  }

}
