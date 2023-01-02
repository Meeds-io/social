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
package io.meeds.oauth.provider.twitter.model;

import io.meeds.oauth.model.AccessTokenContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulate informations about Twitter access token
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TwitterAccessTokenContext extends AccessTokenContext {

  private final String accessToken;

  private final String accessTokenSecret;

  public TwitterAccessTokenContext(String accessToken, String accessTokenSecret) {
    if (accessToken == null || accessTokenSecret == null) {
      throw new IllegalArgumentException("Passed arguments need to be non-null. Passed arguments: accessToken=" +
          accessToken + ", accessTokenSecret=" + accessTokenSecret);
    }
    this.accessToken = accessToken;
    this.accessTokenSecret = accessTokenSecret;
  }
}
