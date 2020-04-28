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
package org.exoplatform.social.core.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.model.GettingStartedStep;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class GettingStartedService {

  private static final Log    LOG                 = ExoLogger.getLogger(GettingStartedService.class);

  public static final String AVATAR_STEP_KEY     = "avatar";

  public static final String SPACES_STEP_KEY     = "spaces";

  public static final String CONTACTS_STEP_KEY   = "contacts";

  public static final String ACTIVITIES_STEP_KEY = "activities";

  private IdentityManager     identityManager;

  private SpaceService        spaceService;

  private RelationshipManager relationshipManager;

  private ActivityManager     activityService;

  public GettingStartedService(IdentityManager identityManager,
                               SpaceService spaceService,
                               RelationshipManager relationshipManager,
                               ActivityManager activityService) {
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.relationshipManager = relationshipManager;
    this.activityService = activityService;
  }

  /**
   * Computed the Getting Started steps for an authenticated user on the following
   * steps :
   * - Upload user avatar
   * - Join a space
   * - Add Connections
   * - Post a message
   * 
   * @param userId username of type {@link String}
   * @return {@link List} of {@link GettingStartedStep} contain list of user steps
   *         name and status
   */
  public List<GettingStartedStep> getUserSteps(String userId) {
    if (StringUtils.isBlank(userId)) {
      throw new IllegalArgumentException("userId is mandatory");
    }
    List<GettingStartedStep> gettingStartedSteps = new ArrayList<>();

    GettingStartedStep stepAvatar = new GettingStartedStep();
    stepAvatar.setName(AVATAR_STEP_KEY);
    stepAvatar.setStatus(hasAvatar(userId));
    gettingStartedSteps.add(stepAvatar);

    GettingStartedStep stepContact = new GettingStartedStep();
    stepContact.setName(CONTACTS_STEP_KEY);
    stepContact.setStatus(hasContacts(userId));
    gettingStartedSteps.add(stepContact);

    GettingStartedStep stepSpaces = new GettingStartedStep();
    stepSpaces.setName(SPACES_STEP_KEY);
    stepSpaces.setStatus(hasSpaces(userId));
    gettingStartedSteps.add(stepSpaces);

    GettingStartedStep stepActivities = new GettingStartedStep();
    stepActivities.setName(ACTIVITIES_STEP_KEY);
    stepActivities.setStatus(hasActivities(userId));
    gettingStartedSteps.add(stepActivities);

    return gettingStartedSteps;
  }

  private boolean hasAvatar(String userId) {
    try {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
      Profile profile = identity.getProfile();
      return profile.hasAvatar();
    } catch (Exception e) {
      LOG.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean hasSpaces(String userId) {
    try {
      ListAccess<Space> accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess(userId);
      return accessibleSpaces.getSize() > 0;
    } catch (Exception e) {
      LOG.warn("Error when cheking user spaces: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean hasContacts(String userId) {
    try {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
      ListAccess<Identity> confirmedContacts = relationshipManager.getConnections(identity);
      return confirmedContacts != null && confirmedContacts.getSize() > 0;
    } catch (Exception e) {
      LOG.warn("Error when checking user activity: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean hasActivities(String userId) {
    try {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
      RealtimeListAccess activities = activityService.getActivitiesWithListAccess(identity);
      int activitiesCount = activities.getSize();
      if (activitiesCount != 0) {
        return true;
      }
      if ((hasAvatar(userId)) && (hasContacts(userId)) && (hasSpaces(userId)) && (activitiesCount >= 5)) {
        return true;
      } else if ((hasAvatar(userId)) && (hasContacts(userId)) && (!hasSpaces(userId)) && (activitiesCount >= 4)) {
        return true;
      } else if ((hasAvatar(userId)) && (!hasContacts(userId)) && (hasSpaces(userId)) && (activitiesCount >= 3)) {
        return true;
      } else if ((!hasAvatar(userId)) && (hasContacts(userId)) && (hasSpaces(userId)) && (activitiesCount >= 4)) {
        return true;
      } else if ((!hasAvatar(userId)) && (!hasContacts(userId)) && (hasSpaces(userId)) && (activitiesCount >= 2)) {
        return true;
      } else {
        return (!hasAvatar(userId)) && (!hasContacts(userId)) && (!hasSpaces(userId)) && (activitiesCount >= 1);
      }
    } catch (Exception e) {
      LOG.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }
}
