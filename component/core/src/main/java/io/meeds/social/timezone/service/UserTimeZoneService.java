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
package io.meeds.social.timezone.service;

import java.time.DateTimeException;
import java.time.ZoneId;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * Holds the timezone of each user, the one his browser lives in, under the
 * profile attribute {@link Profile#USER_TIME_ZONE}. The browser of the user
 * keeps it up to date on every page load, so that everything sent to him on a
 * schedule, starting with the digest mail notifications, goes out at the right
 * local hour. This synchronization used to live in the agenda addon and is now
 * owned by the platform, so that it works whatever the installed addons;
 * agenda delegates its own copy here.
 */
@Service
public class UserTimeZoneService {

  /**
   * Event broadcast when the timezone of a user is saved, with the username as
   * source and the timezone as data. A profile listener can't be used for
   * that: it never fires when this attribute changes.
   */
  public static final String        USER_TIME_ZONE_SAVED_EVENT = "social.timeZone.saved";

  private static final Logger       LOG                        = LoggerFactory.getLogger(UserTimeZoneService.class);

  private final OrganizationService organizationService;

  private final ListenerService     listenerService;

  public UserTimeZoneService(OrganizationService organizationService, ListenerService listenerService) {
    this.organizationService = organizationService;
    this.listenerService = listenerService;
  }

  /**
   * @param username the user to read the timezone of
   * @return the timezone of the user, null when it is unknown: a user who never
   *         loaded a page has no timezone yet. The caller falls back to the
   *         server timezone.
   */
  public String getUserTimeZone(String username) {
    if (StringUtils.isBlank(username)) {
      return null;
    }
    try {
      UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
      return userProfile == null ? null : userProfile.getAttribute(Profile.USER_TIME_ZONE);
    } catch (Exception e) {
      // An unknown timezone only makes what is sent on a schedule go out on
      // the server hour, it must never break the caller
      LOG.warn("Can't read the timezone of user {}, the server timezone will be used instead", username, e);
      return null;
    }
  }

  /**
   * Saves the timezone the browser of the user lives in, then broadcasts
   * {@link #USER_TIME_ZONE_SAVED_EVENT} so that whatever keeps a copy of the
   * timezone, like the digest work list, can refresh it.
   *
   * @param username the user to save the timezone of
   * @param zoneId the timezone identifier, for example Europe/Paris
   * @throws IllegalArgumentException when the timezone is not a known one
   * @throws IllegalStateException when the profile of the user can't be saved
   */
  public void saveUserTimeZone(String username, String zoneId) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is mandatory");
    }
    try {
      ZoneId.of(zoneId);
    } catch (DateTimeException | NullPointerException e) {
      throw new IllegalArgumentException("Unknown timezone " + zoneId, e);
    }
    try {
      UserProfileHandler userProfileHandler = organizationService.getUserProfileHandler();
      UserProfile userProfile = userProfileHandler.findUserProfileByName(username);
      if (userProfile == null) {
        userProfile = userProfileHandler.createUserProfileInstance(username);
      }
      userProfile.setAttribute(Profile.USER_TIME_ZONE, zoneId);
      userProfileHandler.saveUserProfile(userProfile, true);
    } catch (Exception e) {
      throw new IllegalStateException("Can't save the timezone of user " + username, e);
    }
    broadcast(username, zoneId);
  }

  private void broadcast(String username, String zoneId) {
    try {
      listenerService.broadcast(USER_TIME_ZONE_SAVED_EVENT, username, zoneId);
    } catch (Exception e) {
      // The timezone is saved, a listener failure must not undo that for the
      // user: whoever keeps a copy refreshes it at worst on his next save
      LOG.warn("Error broadcasting the timezone change of user {}", username, e);
    }
  }

}
