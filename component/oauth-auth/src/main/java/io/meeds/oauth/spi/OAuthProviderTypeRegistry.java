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
package io.meeds.oauth.spi;

import java.util.Collection;

/**
 * Registry of all registered instances of {@link OAuthProviderType}, which is
 * used by portal to know about all registered OAuth Providers (social networks)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface OAuthProviderTypeRegistry {

  /**
   * Obtain registered OAuth provider
   *
   * @param  key                     of Oauth provider (for example 'FACEBOOK')
   * @param  accessTokenContextClass just for adding more type safety, so caller
   *                                   knows the type of returned
   *                                   {@link OAuthProviderType}
   * @return                         oauth provider for given key
   */
  <T extends AccessTokenContext> OAuthProviderType<T> getOAuthProvider(String key, Class<T> accessTokenContextClass);

  /**
   * @return collection of all registered OAuth providers
   */
  Collection<OAuthProviderType> getEnabledOAuthProviders();

  /**
   * @return true if at least one OAuth provider is enabled
   */
  boolean isOAuthEnabled();
}
