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
package io.meeds.oauth.provider.facebook.model;

import java.io.Serializable;
import java.util.Collection;

import io.meeds.oauth.model.AccessTokenContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulate informations about Facebook access token
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FacebookAccessTokenContext extends AccessTokenContext implements Serializable {

  private static final long serialVersionUID = -7264897192745766989L;

  private final String      accessToken;

  public FacebookAccessTokenContext(String accessToken, String... scopes) {
    super(scopes);
    if (accessToken == null) {
      throw new IllegalArgumentException("accessToken is null");
    }
    this.accessToken = accessToken;
  }

  public FacebookAccessTokenContext(String accessToken, String scopesAsString) {
    super(scopesAsString);
    if (accessToken == null) {
      throw new IllegalArgumentException("accessToken is null");
    }
    this.accessToken = accessToken;
  }

  public FacebookAccessTokenContext(String accessToken, Collection<String> scopes) {
    super(scopes);
    this.accessToken = accessToken;
  }

}
