/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.digest.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;

/**
 * Reads the timezone of a user, so that his digest is sent at the right local
 * hour. The digest engine lives in commons and can't read a user profile, the
 * timezone is therefore resolved here and handed over to it.
 */
@Component
public class DigestTimeZoneResolver {

  /**
   * The only profile attribute holding a real timezone, the one the browser of
   * the user keeps up to date. Beware that today it is written by the agenda
   * addon alone, on every page load: without that addon nothing fills it, and
   * every digest goes out on the server timezone. Moving that synchronization
   * into the platform is planned with the digest sending itself.
   */
  public static final String        USER_TIME_ZONE_ATTRIBUTE = "user.timeZone";

  private static final Logger       LOG                      = LoggerFactory.getLogger(DigestTimeZoneResolver.class);

  private final OrganizationService organizationService;

  public DigestTimeZoneResolver(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  /**
   * @param username the user to read the timezone of
   * @return the timezone of the user, null when it is unknown. The digest of a
   *         user with no known timezone is sent on the server one.
   */
  public String getUserTimeZone(String username) {
    if (StringUtils.isBlank(username)) {
      return null;
    }
    try {
      UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
      return userProfile == null ? null : userProfile.getAttribute(USER_TIME_ZONE_ATTRIBUTE);
    } catch (Exception e) {
      // An unknown timezone only makes the digest go out on the server hour,
      // it must never make the user unable to save his choices
      LOG.warn("Can't read the timezone of user {}, the server timezone will be used for his digest", username, e);
      return null;
    }
  }

}
