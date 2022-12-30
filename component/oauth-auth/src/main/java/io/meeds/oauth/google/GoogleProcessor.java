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
package io.meeds.oauth.google;

import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.services.plus.Plus;

import io.meeds.oauth.spi.OAuthProviderProcessor;

/**
 * OAuth processor for calling Google+ operations
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface GoogleProcessor extends OAuthProviderProcessor<GoogleAccessTokenContext> {

  /**
   * Obtain informations about user from Google+ .
   *
   * @param  accessTokenContext google access token
   * @return                    userinfo object with filled info about this user
   */
  Userinfo obtainUserInfo(GoogleAccessTokenContext accessTokenContext);

  /**
   * Obtain instance of Google {@link Oauth2} object, which can be used to call
   * various operations in Google API (obtain user informations, obtain
   * informations about your access token etc)
   *
   * @param  accessTokenContext
   * @return                    oauth2 object
   */
  Oauth2 getOAuth2Instance(GoogleAccessTokenContext accessTokenContext);

  /**
   * Obtain instance of Google (@link Plus} object, which can be used to call
   * various operations in Google+ API (Obtain list of your friends, obtain your
   * statuses, comments, activities etc...)
   *
   * @param  accessTokenContext
   * @return                    plus object
   */
  Plus getPlusService(GoogleAccessTokenContext accessTokenContext);

  /**
   * Refresh Google+ token. Note that argument needs to have "refreshToken"
   * available. The "accessToken" will be refreshed and updated directly on this
   * instance of accessTokenContext
   *
   * @param accessTokenContext
   */
  void refreshToken(GoogleAccessTokenContext accessTokenContext);

}
