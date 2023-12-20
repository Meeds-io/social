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

import jakarta.servlet.http.HttpServletRequest;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.social.core.identity.model.Identity;

import io.meeds.oauth.spi.AccessTokenContext;
import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthProviderType;

public interface OAuthRegistrationService {

  boolean isRegistrationOnFly(OAuthProviderType<? extends AccessTokenContext> oauthProviderType);

  /**
   * attempts to detect if a user account already exists for current social
   * network user
   * 
   * @param  request
   * @param  principal
   * @return           existing user
   */
  User detectGateInUser(HttpServletRequest request, OAuthPrincipal<? extends AccessTokenContext> principal);

  /**
   * Creates a new User with oAuth authenticated user attributes
   * 
   * @param  principal {@link OAuthPrincipal} of authenticated user
   * @return           newly created user
   */
  User createGateInUser(OAuthPrincipal<? extends AccessTokenContext> principal);

  /**
   * Saves user OAuth profile attributes in {@link UserProfile} entity
   * 
   * @param  username     existing {@link User} entity username attribute
   * @param  providerType {@link OAuthProviderType}
   * @throws Exception    when error occurs while saving user profile
   */
  void updateUserProfileAttributes(String username, OAuthProviderType<?> providerType) throws Exception; // NOSONAR

  /**
   * Saves User Avatar from oAuth Provider into user {@link Identity}
   * 
   * @param username  existing {@link User} entity username attribute
   * @param principal {@link OAuthPrincipal} of authenticated user
   */
  void updateUserIdentityAvatar(String username, OAuthPrincipal<? extends AccessTokenContext> principal);
}
