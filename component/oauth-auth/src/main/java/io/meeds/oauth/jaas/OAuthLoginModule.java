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
package io.meeds.oauth.jaas;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.gatein.sso.agent.tomcat.ServletAccess;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.services.security.jaas.AbstractLoginModule;
import org.exoplatform.web.security.AuthenticationRegistry;

import io.meeds.oauth.common.OAuthConstants;
import io.meeds.oauth.spi.OAuthProviderTypeRegistry;

/**
 * JAAS login module to finish Authentication after successfully finished OAuth
 * workflow
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthLoginModule extends AbstractLoginModule {

  private static final Log LOG = ExoLogger.getLogger(OAuthLoginModule.class);

  @Override
  protected Log getLogger() {
    return LOG;
  }

  @Override
  public boolean login() throws LoginException {
    try {
      ExoContainer container = getContainer();

      OAuthProviderTypeRegistry oauthRegistry = container.getComponentInstanceOfType(OAuthProviderTypeRegistry.class);
      if (!oauthRegistry.isOAuthEnabled()) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("OAuth is disabled. Ignoring this login module");
        }
        return false;
      }

      HttpServletRequest servletRequest = ServletAccess.getRequest();
      if (servletRequest == null) {
        LOG.warn("HttpServletRequest is null. OAuthLoginModule will be ignored.");
        return false;
      }

      User portalUser = getOauthAuthenticatedUser(container, servletRequest);
      if (portalUser == null) {
        return false;
      }

      Callback[] callbacks = new Callback[] {
          new NameCallback("Username")
      };
      callbackHandler.handle(callbacks);
      String username = ((NameCallback) callbacks[0]).getName();

      boolean authenticated = StringUtils.equals(username, portalUser.getUserName());
      if (authenticated) {
        establishSecurityContext(container, username);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      throw new LoginException("OAuth login failed due to exception: " + e.getMessage());
    }
  }

  private User getOauthAuthenticatedUser(ExoContainer container, HttpServletRequest servletRequest) {
    AuthenticationRegistry authRegistry = container.getComponentInstanceOfType(AuthenticationRegistry.class);
    User portalUser = (User) authRegistry.getAttributeOfClient(servletRequest,
                                                               OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS);
    if (portalUser != null) {
      // We do not need this attribute any more
      authRegistry.removeAttributeOfClient(servletRequest, OAuthConstants.ATTRIBUTE_AUTHENTICATED_PORTAL_USER_FOR_JAAS);
    }
    return portalUser;
  }

  @Override
  public boolean commit() throws LoginException {
    return true;
  }

  @Override
  public boolean abort() throws LoginException {
    return true;
  }

  @Override
  public boolean logout() throws LoginException {
    return true;
  }

  @SuppressWarnings("unchecked")
  protected void establishSecurityContext(ExoContainer container, String username) throws Exception {
    Authenticator authenticator = container.getComponentInstanceOfType(Authenticator.class);
    Identity identity = authenticator.createIdentity(username);
    sharedState.put("exo.security.identity", identity);
    sharedState.put("javax.security.auth.login.name", username);
    subject.getPublicCredentials().add(new UsernameCredential(username));
  }

}
