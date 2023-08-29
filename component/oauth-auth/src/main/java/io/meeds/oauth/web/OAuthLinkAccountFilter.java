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
package io.meeds.oauth.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.SocialNetworkService;
import io.meeds.oauth.utils.OAuthUtils;

/**
 * This filter has already access to authenticated OAuth principal, so it's work
 * starts after successful OAuth authentication. Filter is useful only for
 * logged user Responsibility of this filter is to finish "link social network"
 * functionality (Usecase where logged user wants to link his GateIn account
 * with social network)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthLinkAccountFilter extends AbstractSSOInterceptor {

  private static Log           log = ExoLogger.getLogger(OAuthLinkAccountFilter.class);

  private SocialNetworkService socialNetworkService;

  @Override
  protected void initImpl() {
    socialNetworkService = (SocialNetworkService) getExoContainer().getComponentInstanceOfType(SocialNetworkService.class);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    HttpSession session = httpRequest.getSession();

    // Do nothing for anonymous user
    if (httpRequest.getRemoteUser() == null) {
      chain.doFilter(request, response);
      return;
    }

    OAuthPrincipal oauthPrincipal = (OAuthPrincipal) request.getAttribute(OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);

    if (oauthPrincipal == null) {
      chain.doFilter(request, response);
      return;
    }

    try {
      socialNetworkService.updateOAuthInfo(oauthPrincipal.getOauthProviderType(),
                                           httpRequest.getRemoteUser(),
                                           oauthPrincipal.getUserName(),
                                           oauthPrincipal.getAccessToken());

      // Add some attribute to session, which will be read by OAuthLifecycle
      session.setAttribute(OAuthConstants.ATTRIBUTE_LINKED_OAUTH_PROVIDER,
                           oauthPrincipal.getOauthProviderType().getFriendlyName());
    } catch (OAuthException gtnOauthOAuthException) {
      // Show warning message if user with this facebookUsername (or
      // googleUsername) already exists
      if (gtnOauthOAuthException.getExceptionCode() == OAuthExceptionCode.DUPLICATE_OAUTH_PROVIDER_USERNAME) {
        // Add some attribute to session, which will be read by OAuthLifecycle
        session.setAttribute(OAuthConstants.ATTRIBUTE_EXCEPTION_AFTER_FAILED_LINK, gtnOauthOAuthException);
      } else {
        throw gtnOauthOAuthException;
      }
    }

    String urlToRedirect = OAuthUtils.getURLToRedirectAfterLinkAccount(httpRequest, session);

    if (log.isTraceEnabled()) {
      log.trace("User profile successfully updated with new userName and accessToken. oauthProvider="
          + oauthPrincipal.getOauthProviderType() +
          ", username=" + httpRequest.getRemoteUser() + ", oauthUsername=" + oauthPrincipal.getUserName());
      log.trace("Will redirect user to URL: " + urlToRedirect);
    }

    httpResponse.sendRedirect(httpResponse.encodeRedirectURL(urlToRedirect));
  }
}
