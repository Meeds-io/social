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
package io.meeds.oauth.linkedin;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;

import io.meeds.oauth.spi.OAuthPrincipal;
import io.meeds.oauth.spi.OAuthPrincipalProcessor;
import io.meeds.oauth.utils.OAuthUtils;

public class LinkedInPrincipalProcessor implements OAuthPrincipalProcessor {
  @Override
  public User convertToGateInUser(OAuthPrincipal principal) {
    String email = principal.getEmail();
    String username = principal.getUserName();
    User gateinUser = new UserImpl();
    if (StringUtils.isNotBlank(username)) {
      gateinUser.setUserName(OAuthUtils.refineUserName(username));
    }
    gateinUser.setFirstName(principal.getFirstName());
    gateinUser.setLastName(principal.getLastName());
    gateinUser.setEmail(email);
    gateinUser.setDisplayName(principal.getDisplayName());

    return gateinUser;
  }
}
