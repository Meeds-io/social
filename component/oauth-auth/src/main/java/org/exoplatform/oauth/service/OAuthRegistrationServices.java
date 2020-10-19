/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.oauth.service;

import org.exoplatform.services.organization.User;
import org.gatein.security.oauth.spi.AccessTokenContext;
import org.gatein.security.oauth.spi.OAuthPrincipal;
import org.gatein.security.oauth.spi.OAuthProviderType;

import javax.servlet.http.HttpServletRequest;

public interface OAuthRegistrationServices {

    boolean isRegistrationOnFly(OAuthProviderType<? extends AccessTokenContext> oauthProviderType);

    /**
     * attempts to detect if a user account already exists for current social network user
     * @param request
     * @param principal
     * @return
     */
    User detectGateInUser(HttpServletRequest request, OAuthPrincipal<? extends AccessTokenContext> principal);


    User createGateInUser(OAuthPrincipal<? extends AccessTokenContext> principal);
}
