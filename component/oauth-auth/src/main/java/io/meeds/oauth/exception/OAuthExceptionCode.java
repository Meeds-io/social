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
package io.meeds.oauth.exception;

/**
 * Enum with various exception codes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum OAuthExceptionCode {

  /**
   * Unspecified GateIn+OAuth error
   */
  UNKNOWN_ERROR,

  /**
   * This error could happen during saving of user into GateIn identity
   * database. It happens when there is an attempt to save user with
   * facebookUsername (or googleUsername), but there is already an existing user
   * with same facebookUsername. For example: We want to save user 'john' with
   * facebookUsername 'john.doyle' but we already have user 'johny2' with same
   * facebookUsername 'john.doyle'
   */
  DUPLICATE_OAUTH_PROVIDER_USERNAME,

  /**
   * Some error during Twitter processing
   */
  TWITTER_ERROR,

  /**
   * Some error during Facebook processing
   */
  FACEBOOK_ERROR,

  /**
   * Some error during Google processing
   */
  GOOGLE_ERROR,

  /**
   * Some error during LinkedIn processing
   */
  LINKEDIN_ERROR,

  /**
   * Error when we have invalid or revoked access token
   */
  ACCESS_TOKEN_ERROR,

  /**
   * Generic IO error (for example network error)
   */
  IO_ERROR,

  /**
   * Error when state parameter from request parameter, which is sent from OAuth
   * provider, is not equals to previously sent state
   */
  INVALID_STATE,

  /**
   * Error when revoking of accessToken of any provider failed
   */
  TOKEN_REVOCATION_FAILED,

  /**
   * Error when OAuth2 flow failed because user denied to permit privileges
   * (scope) for OAuth provider
   */
  USER_DENIED_SCOPE,

  /**
   * Error during DB operation (For example get/set/remove access token from DB)
   */
  PERSISTENCE_ERROR,
  /**
   * Error when OAuth2 flow failed because jwt token validation fails
   */
  TOKEN_VALIDATION_ERROR

}
