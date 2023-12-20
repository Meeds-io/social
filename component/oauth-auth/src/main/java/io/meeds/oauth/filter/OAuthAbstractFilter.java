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
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.filter.Filter;
import org.exoplatform.web.security.AuthenticationRegistry;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public abstract class OAuthAbstractFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    if (req.getRemoteUser() != null) {
      chain.doFilter(request, response);
      return;
    }

    if (!this.isOauthEnable()) {
      chain.doFilter(req, res);
      return;
    }

    AuthenticationRegistry authReg = getService(AuthenticationRegistry.class);
    User authenticated = (User) authReg.getAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS);
    if (authenticated != null) {
      // Found user mapped with oauth-user, let LoginModule continue process
      // login
      chain.doFilter(req, res);
      return;
    }

    User oauthAuthenticatedUser = (User) authReg.getAttributeOfClient(req, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER);
    if (oauthAuthenticatedUser == null) {
      // Not in oauth process, do not need to process here
      chain.doFilter(req, res);
      return;
    }

    // . In oauth process
    this.executeFilter(req, res, chain);
  }

  protected abstract void executeFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException,
                                                                                                            ServletException;

  private Boolean oauthEnable = null;

  protected boolean isOauthEnable() {
    if (oauthEnable == null) {
      OAuthProviderTypeRegistry registry = getService(OAuthProviderTypeRegistry.class);
      oauthEnable = registry.isOAuthEnabled();
    }
    return this.oauthEnable;
  }

  protected <T> T getService(Class<T> clazz) {
    return PortalContainer.getInstance().getComponentInstanceOfType(clazz);
  }

  protected ServletContext getContext() {
    return PortalContainer.getInstance().getPortalContext();
  }
}
