/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.navigation.service;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.config.UserPortalConfigService;

import io.meeds.portal.navigation.service.NavigationConfigurationService;

/**
 * Saves the page a user chose as his home, the one the company logo of the
 * topbar leads to, when the platform lets users choose it.
 */
@Service
public class UserHomePageService {

  public static final String             INVALID_URI_MESSAGE = "navigation.home.invalidUri";

  public static final int                MAX_URI_LENGTH      = 2048;

  /**
   * A path of the same origin, or an http(s) URL for a link node, without any
   * character that could leave the HTML attribute or the script literal the
   * URI is written into
   */
  private static final Pattern           URI_PATTERN         =
                                                     Pattern.compile("^(/(?![/\\\\])|https?://)[^\\s\\p{Cntrl}<>\"'`$\\\\]*$",
                                                                     Pattern.CASE_INSENSITIVE);

  @Autowired
  private UserPortalConfigService        userPortalConfigService;

  @Autowired
  private NavigationConfigurationService navigationConfigurationService;

  /**
   * @param username the user who chooses his home page
   * @param uri the URI of the chosen page
   * @throws IllegalAccessException when the platform doesn't let users choose
   *           their home page
   * @throws IllegalArgumentException when the URI isn't a valid home page URI
   */
  public void saveUserHomePage(String username, String uri) throws IllegalAccessException {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("username is mandatory");
    }
    if (!navigationConfigurationService.isAllowUserHome()) {
      throw new IllegalAccessException("Users aren't allowed to choose their home page");
    }
    if (!isValidUri(uri)) {
      throw new IllegalArgumentException(INVALID_URI_MESSAGE);
    }
    userPortalConfigService.setUserHomePage(username, uri);
  }

  private boolean isValidUri(String uri) {
    return uri != null && uri.length() <= MAX_URI_LENGTH && URI_PATTERN.matcher(uri).matches();
  }

}
