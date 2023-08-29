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
package io.meeds.oauth.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.organization.User;
import org.exoplatform.web.security.AuthenticationRegistry;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.contant.OAuthConst;
import io.meeds.oauth.service.OAuthRegistrationService;
import io.meeds.oauth.spi.OAuthPrincipal;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class OauthSignupOnflyFilter extends OAuthAbstractFilter {

  static final String SESSION_KEY_SIGNUP_ON_FLY_ERROR = "__onfly_error";

  @Override
  @SuppressWarnings("unchecked")
  protected void executeFilter(HttpServletRequest request, // NOSONAR
                               HttpServletResponse response,
                               FilterChain chain) throws IOException, ServletException {

    AuthenticationRegistry authenticationRegistry = getService(AuthenticationRegistry.class);
    OAuthRegistrationService oAuthRegistrationService = getService(OAuthRegistrationService.class);
    if (oAuthRegistrationService == null || userExists(request, authenticationRegistry)) {
      chain.doFilter(request, response);
      return;
    }

    OAuthPrincipal<?> principal = getPrincipal(request, authenticationRegistry);
    if (oAuthRegistrationService.isRegistrationOnFly(principal.getOauthProviderType())) {
      String oauth = principal.getOauthProviderType().getKey() + "_" + principal.getUserName();
      String onFlyError = (String) request.getSession().getAttribute(SESSION_KEY_SIGNUP_ON_FLY_ERROR);
      if (StringUtils.isNotBlank(onFlyError)) {
        if (StringUtils.equals(oauth, onFlyError)) {
          // Did not detect and auto create user for this oauth-user, just
          // show registration form
          chain.doFilter(request, response);
          return;
        } else {
          request.getSession().removeAttribute(SESSION_KEY_SIGNUP_ON_FLY_ERROR);
        }
      }

      User user = oAuthRegistrationService.detectGateInUser(request, principal);
      if (user != null) {
        authenticationRegistry.setAttributeOfClient(request,
                                                    OAuthConst.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED,
                                                    user);

      } else {
        User registeredUser = oAuthRegistrationService.createGateInUser(principal);
        if (registeredUser != null) {
          authenticationRegistry.removeAttributeOfClient(request, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
          // send redirect to continue oauth login
          response.sendRedirect(getContext().getContextPath());
          return;
        } else {
          request.getSession().setAttribute(SESSION_KEY_SIGNUP_ON_FLY_ERROR, oauth);
          request.getSession().setAttribute(OAuthConst.SESSION_KEY_ON_FLY_ERROR, Boolean.TRUE);
        }
      }
    }
    chain.doFilter(request, response);
  }

  private OAuthPrincipal<?> getPrincipal(HttpServletRequest request, AuthenticationRegistry authenticationRegistry) {
    OAuthPrincipal<?> principal;
    principal = (OAuthPrincipal<?>) authenticationRegistry.getAttributeOfClient(request,
                                                                                OAuthConstants.ATTRIBUTE_AUTHENTICATED_OAUTH_PRINCIPAL);
    return principal;
  }

  private boolean userExists(HttpServletRequest request, AuthenticationRegistry authenticationRegistry) {
    User detectedUser;
    detectedUser = (User) authenticationRegistry.getAttributeOfClient(request,
                                                                      OAuthConst.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_DETECTED);
    return detectedUser != null;
  }
}
