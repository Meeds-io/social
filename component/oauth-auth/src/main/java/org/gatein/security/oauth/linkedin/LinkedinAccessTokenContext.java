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
package org.gatein.security.oauth.linkedin;

import java.io.Serializable;

import org.gatein.security.oauth.spi.AccessTokenContext;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

public class LinkedinAccessTokenContext extends AccessTokenContext implements Serializable {

  public final OAuth2AccessToken        accessToken;

  public final transient OAuth20Service oauth20Service;

  public LinkedinAccessTokenContext(OAuth2AccessToken accessToken, OAuth20Service oauth20Service) {
    this.accessToken = accessToken;
    this.oauth20Service = oauth20Service;
  }

  @Override
  public String getAccessToken() {
    return accessToken.getAccessToken();
  }
}
