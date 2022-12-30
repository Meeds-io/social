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

import java.security.Principal;

/**
 * Principal, which contains info about user, who was authenticated through
 * OAuth
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthPrincipal<T extends AccessTokenContext> implements Principal {

  private final String               userName;

  private final String               firstName;

  private final String               lastName;

  private final String               displayName;

  private final String               email;

  private final String               avatar;

  private final T                    accessToken;

  private final OAuthProviderType<T> oauthProviderType;

  public OAuthPrincipal(String userName,
                        String firstName,
                        String lastName,
                        String displayName,
                        String email,
                        T accessToken,
                        OAuthProviderType<T> oauthProviderType) {
    this(userName, firstName, lastName, displayName, email, null, accessToken, oauthProviderType);
  }

  public OAuthPrincipal(String userName,
                        String firstName,
                        String lastName,
                        String displayName,
                        String email,
                        String avatar,
                        T accessToken,
                        OAuthProviderType<T> oauthProviderType) {
    this.userName = userName;
    this.firstName = firstName;
    this.lastName = lastName;
    this.displayName = displayName;
    this.email = email;
    this.avatar = avatar;
    this.accessToken = accessToken;
    this.oauthProviderType = oauthProviderType;
  }

  @Override
  public String getName() {
    // Using userName as name of OAuthPrincipal
    return userName;
  }

  public String getUserName() {
    return userName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getEmail() {
    return email;
  }

  public String getAvatar() {
    return avatar;
  }

  public T getAccessToken() {
    return accessToken;
  }

  public OAuthProviderType getOauthProviderType() {
    return oauthProviderType;
  }
}
