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
package io.meeds.oauth.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.oauth.model.AccessTokenContext;
import io.meeds.oauth.model.OAuthProviderType;
import io.meeds.oauth.plugin.OauthProviderTypeRegistryPlugin;

/**
 * Registry of all registered instances of {@link OAuthProviderType}, which is
 * used by portal to know about all registered OAuth Providers (social networks)
 * 
 * @param <T> {@link AccessTokenContext} Type
 */
public class OAuthProviderTypeRegistry {

  private static final Log                                         LOG                =
                                                                       ExoLogger.getLogger(OAuthProviderTypeRegistry.class);

  // Key is String identifier of OauthProviderType (Key of this
  // OAuthProviderType). Value is OAuthProviderType
  private final Map<String, OAuthProviderType<AccessTokenContext>> oauthProviderTypes = new LinkedHashMap<>();

  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  public void addPlugin(OauthProviderTypeRegistryPlugin oauthPlugin) {
    OAuthProviderType<AccessTokenContext> oauthPrType = oauthPlugin.getOAuthProviderType();
    if (oauthPrType != null) {
      this.oauthProviderTypes.put(oauthPrType.getKey(), oauthPrType);
    } else {
      LOG.info("Skip disabled OAuthProviderType " + oauthPrType);
    }
  }

  /**
   * Obtain registered OAuth provider
   *
   * @param  key                     of Oauth provider (for example 'FACEBOOK')
   * @param  accessTokenContextClass just for adding more type safety, so caller
   *                                   knows the type of returned
   *                                   {@link OAuthProviderType}
   * @return                         oauth provider for given key
   */
  public OAuthProviderType<AccessTokenContext> getOAuthProvider(String key,
                                                                Class<? extends AccessTokenContext> accessTokenContextClass) {
    return oauthProviderTypes.get(key);
  }

  /**
   * @return collection of all registered OAuth providers
   */
  public Collection<OAuthProviderType<AccessTokenContext>> getEnabledOAuthProviders() {
    return Collections.unmodifiableCollection(oauthProviderTypes.values());
  }

  /**
   * @return true if at least one OAuth provider is enabled
   */
  public boolean isOAuthEnabled() {
    return !oauthProviderTypes.isEmpty();
  }

}
