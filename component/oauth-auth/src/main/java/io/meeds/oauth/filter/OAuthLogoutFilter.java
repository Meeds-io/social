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
package io.meeds.oauth.filter;

import io.meeds.oauth.spi.OAuthProviderType;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.sso.agent.filter.api.AbstractSSOInterceptor;

import java.io.IOException;

public class OAuthLogoutFilter extends AbstractSSOInterceptor {

  protected final Log LOG = ExoLogger.getLogger(OAuthLogoutFilter.class);
  public static final String OAUTH_LOGOUT_ATTRIBUTE = "OAUTH_LOGOUT_IN_PROGRESS";


  public OAuthLogoutFilter() {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    String remoteUser = request.getRemoteUser();
    if (isPortalLogoutInProgress(request) && !StringUtils.isEmpty(remoteUser)) {
      try {
        OAuthProviderTypeRegistry oauthRegistry = getService(OAuthProviderTypeRegistry.class);
        for (OAuthProviderType oAuthProviderType : oauthRegistry.getEnabledOAuthProviders()) {
          LOG.debug("Logging out from OAuth provider: " + oAuthProviderType.getKey());
          if (oAuthProviderType.getOauthProviderProcessor().processLogout(request, response, oAuthProviderType)) {
            return;
          }
        }
      } catch (Exception e) {
        LOG.error("Unable to get OAuthProviderTypeRegistry during logout", e);
      }
    }
    filterChain.doFilter(servletRequest, servletResponse);

  }

  protected <T> T getService(Class<T> clazz) {
    return PortalContainer.getInstance().getComponentInstanceOfType(clazz);
  }
  public static boolean isPortalLogoutInProgress(HttpServletRequest request) {
    return request.getRequestURI().equals("/portal/logout") && request.getRemoteUser() != null;
  }

  @Override
  protected void initImpl() {

  }

  private static String getPortalLogoutURLFromSession(HttpServletRequest request) {
    return request.getSession().getAttribute(OAUTH_LOGOUT_ATTRIBUTE) == null ? null
                                                                             : request.getSession()
                                                                                      .getAttribute(OAUTH_LOGOUT_ATTRIBUTE)
                                                                                      .toString();
  }

  public static boolean isOAuthLogoutInProgress(HttpServletRequest request) {
    return request.getRemoteUser() != null && StringUtils.isNotBlank(getPortalLogoutURLFromSession(request))
        && !StringUtils.equals(getPortalLogoutURLFromSession(request), "DONE");
  }
}
