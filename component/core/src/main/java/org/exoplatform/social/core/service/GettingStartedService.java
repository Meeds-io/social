package org.exoplatform.social.core.service;

import java.util.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
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

  public static final Scope  APP_SCOPE           = Scope.APPLICATION.id("GettingStarted");

  public static final String USER_SETTING_NAME   = "gettingStartedStatus";

  private IdentityManager     identityManager;

  private SpaceService        spaceService;

  private RelationshipManager relationshipManager;

  private ActivityManager     activityService;

  private SettingService      settingService;

  public GettingStartedService(IdentityManager identityManager,
                               SpaceService spaceService,
                               RelationshipManager relationshipManager,
                               ActivityManager activityService,
                               SettingService settingService) {
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.relationshipManager = relationshipManager;
    this.activityService = activityService;
    this.settingService = settingService;
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
    SettingValue<?> settingValue = settingService.get(Context.USER.id(userId), APP_SCOPE, USER_SETTING_NAME);
    if (settingValue != null && settingValue.getValue() != null && !Boolean.parseBoolean(settingValue.getValue().toString())) {
      return Collections.emptyList();
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

  public void hideGettingStartedApplication(String userId) {
    settingService.set(Context.USER.id(userId), APP_SCOPE, USER_SETTING_NAME, SettingValue.create(true));
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
      RealtimeListAccess<ExoSocialActivity> activities = activityService.getActivitiesWithListAccess(identity);
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
