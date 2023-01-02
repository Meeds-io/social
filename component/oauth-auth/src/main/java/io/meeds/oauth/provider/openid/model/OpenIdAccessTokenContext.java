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
package io.meeds.oauth.openid;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.github.scribejava.core.model.OAuth2AccessToken;

import io.meeds.oauth.spi.AccessTokenContext;

/**
 * Encapsulate informations about OpenId access token
 */
public class OpenIdAccessTokenContext extends AccessTokenContext implements Serializable {

  private static final long      serialVersionUID = -7038197192745766989L;

  public final OAuth2AccessToken accessToken;

  private Map<String, String>    customClaims;

  public OpenIdAccessTokenContext(OAuth2AccessToken tokenData, String... scopes) {
    super(scopes);
    if (tokenData == null) {
      throw new IllegalArgumentException("tokenData can't be null");
    }
    customClaims = new HashMap<>();
    this.accessToken = tokenData;
  }

  public OpenIdAccessTokenContext(OAuth2AccessToken tokenData, String scopeAsString) {
    super(scopeAsString);
    if (tokenData == null) {
      throw new IllegalArgumentException("tokenData can't be null");
    }
    customClaims = new HashMap<>();
    this.accessToken = tokenData;
  }

  @Override
  public String getAccessToken() {
    return accessToken.getAccessToken();
  }

  public OAuth2AccessToken getTokenData() {
    return accessToken;
  }

  public void addCustomClaims(Map<String, String> newCustomClaims) {
    this.customClaims.putAll(newCustomClaims);
  }

  public Map<String, String> getCustomClaims() {
    return this.customClaims;
  }
}
