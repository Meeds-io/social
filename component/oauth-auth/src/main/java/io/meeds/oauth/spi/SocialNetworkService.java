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

import org.exoplatform.services.organization.User;

/**
 * Service for handling persistence of OAuth data (usernames, access tokens)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface SocialNetworkService {

  /**
   * Find user from Identity DB by oauth provider username
   *
   * @param  oauthProviderType
   * @param  oauthProviderUsername
   * @return                       portal user
   */
  User findUserByOAuthProviderUsername(OAuthProviderType oauthProviderType, String oauthProviderUsername);

  /**
   * Save access token of given user into DB
   */
  <T extends AccessTokenContext> void updateOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username,
                                                             T accessToken);

  /**
   * Obtain access token of given user from DB
   */
  <T extends AccessTokenContext> T getOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username);

  /**
   * Save OAuth informations (both username and access token) into DB
   */
  <T extends AccessTokenContext> void updateOAuthInfo(OAuthProviderType<T> oauthProviderType, String username,
                                                      String oauthUsername, T accessToken);

  /**
   * Remove access token of given user from DB
   */
  <T extends AccessTokenContext> void removeOAuthAccessToken(OAuthProviderType<T> oauthProviderType, String username);

  /**
   * Locates a user by its email address. If no user is found or more than one
   * user has that email, it returns null
   * 
   * @param  email
   * @return       User having provided email
   */
  User findUserByEmail(String email);
}
