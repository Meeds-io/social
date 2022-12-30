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

import java.io.Serializable;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

import io.meeds.oauth.spi.AccessTokenContext;

/**
 * Encapsulate informations about Google+ access token
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GoogleAccessTokenContext extends AccessTokenContext implements Serializable {

  private static final long         serialVersionUID = -7038197192745766989L;

  private final GoogleTokenResponse tokenData;

  public GoogleAccessTokenContext(GoogleTokenResponse tokenData, String... scopes) {
    super(scopes);
    if (tokenData == null) {
      throw new IllegalArgumentException("tokenData can't be null");
    }
    this.tokenData = tokenData;
  }

  public GoogleAccessTokenContext(GoogleTokenResponse tokenData, String scopeAsString) {
    super(scopeAsString);
    if (tokenData == null) {
      throw new IllegalArgumentException("tokenData can't be null");
    }
    this.tokenData = tokenData;
  }

  public GoogleTokenResponse getTokenData() {
    return tokenData;
  }

  @Override
  public String getAccessToken() {
    return tokenData.getAccessToken();
  }

  @Override
  public String toString() {
    return new StringBuilder("GoogleAccessTokenContext [")
                                                          .append("accessToken=" + tokenData)
                                                          .append(super.toString())
                                                          .toString();
  }

  @Override
  public boolean equals(Object that) {
    if (!super.equals(that)) {
      return false;
    }
    GoogleAccessTokenContext thatt = (GoogleAccessTokenContext) that;
    return this.tokenData.equals(thatt.getTokenData());
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 13 + tokenData.hashCode();
  }
}
