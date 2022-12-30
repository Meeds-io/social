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
package io.meeds.oauth.webapi;

import java.util.HashMap;
import java.util.Map;

import org.gatein.sso.agent.filter.api.SSOInterceptor;
import org.gatein.sso.integration.SSOFilterIntegratorImpl;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.oauth.spi.OAuthProviderTypeRegistry;

/**
 * Kernel component, which holds references to configured
 * {@link org.gatein.sso.agent.filter.api.SSOInterceptor} instances for OAuth
 * integration
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthFilterIntegratorImpl extends SSOFilterIntegratorImpl implements OAuthFilterIntegrator {

  private static final Log                log = ExoLogger.getLogger(OAuthFilterIntegratorImpl.class);

  private final OAuthProviderTypeRegistry oAuthProviderTypeRegistry;

  public OAuthFilterIntegratorImpl(OAuthProviderTypeRegistry oAuthProviderTypeRegistry) {
    this.oAuthProviderTypeRegistry = oAuthProviderTypeRegistry;
  }

  @Override
  public Map<SSOInterceptor, String> getOAuthInterceptors() {
    if (oAuthProviderTypeRegistry.isOAuthEnabled()) {
      return getSSOInterceptors();
    } else {
      // return empty map if oauth is disabled (we don't have any OAuthProviders
      // configured)
      log.debug("OAuth2 is disabled as there are not any OAuthProviderTypes configured. OAuth interceptors will be skipped");
      return new HashMap<SSOInterceptor, String>();
    }
  }
}
