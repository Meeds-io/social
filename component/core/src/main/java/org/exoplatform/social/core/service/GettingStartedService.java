package org.exoplatform.social.core.service;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
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

  private static final Log         LOG = ExoLogger.getLogger(GettingStartedService.class);
  IdentityManager                  identityManager;
  SpaceService                     spaceService;
  RelationshipManager              relationshipManager;
  ActivityManager                  activityService;
  private List<GettingStartedStep> gettingStartedSteps;

  public GettingStartedService(IdentityManager identityManager,
                               SpaceService spaceService,
                               RelationshipManager relationshipManager,
                               ActivityManager activityService) {
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.relationshipManager = relationshipManager;
    this.activityService = activityService;
  }

  public List<GettingStartedStep> getUserSteps(String userId) {

    List<GettingStartedStep> gettingStartedSteps = new ArrayList<>();

    GettingStartedStep stepAvatar = new GettingStartedStep();
    stepAvatar.setName("avatar");
    stepAvatar.setStatus(hasAvatar(userId));
    gettingStartedSteps.add(stepAvatar);

    GettingStartedStep stepSpaces = new GettingStartedStep();
    stepSpaces.setName("spaces");
    stepSpaces.setStatus(hasSpaces(userId));
    gettingStartedSteps.add(stepSpaces);

    GettingStartedStep stepContact = new GettingStartedStep();
    stepContact.setName("contacts");
    stepContact.setStatus(hasContacts(userId));
    gettingStartedSteps.add(stepContact);

    GettingStartedStep stepActivities = new GettingStartedStep();
    stepActivities.setName("activities");
    stepActivities.setStatus(hasActivities(userId));
    gettingStartedSteps.add(stepActivities);

    return gettingStartedSteps;
  }

  private boolean hasAvatar(String userId) {
    try {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
      Profile profile = identity.getProfile();

      if (profile.getAvatarUrl() != null)
        return true;
      else
        return false;
    } catch (Exception e) {
      LOG.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean hasSpaces(String userId) {
    try {
      Space[] spaces = spaceService.getAccessibleSpacesWithListAccess(userId).load(0, 1);
      return spaces != null && spaces.length > 0;

    } catch (Exception e) {
      LOG.warn("Error when cheking user spaces: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean hasContacts(String userId) {
    try {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
      ListAccess<Identity> confirmedContacts = relationshipManager.getConnections(identity);

      return confirmedContacts.getSize() > 0;
    } catch (Exception e) {
      LOG.warn("Error when checking user activity: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean hasActivities(String userId) {
    try {
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
      RealtimeListAccess activities = activityService.getActivitiesWithListAccess(identity);

      if (activities.getSize() != 0) {

        if ((hasAvatar(userId)) && (hasContacts(userId)) && (hasSpaces(userId)) && (activities.getSize() >= 5))
          return true;
        else if ((hasAvatar(userId)) && (hasContacts(userId)) && (!hasSpaces(userId)) && (activities.getSize() >= 4))
          return true;
        else if ((hasAvatar(userId)) && (!hasContacts(userId)) && (hasSpaces(userId)) && (activities.getSize() >= 3))
          return true;

        else if ((!hasAvatar(userId)) && (hasContacts(userId)) && (hasSpaces(userId)) && (activities.getSize() >= 4))
          return true;
        else if ((!hasAvatar(userId)) && (!hasContacts(userId)) && (hasSpaces(userId)) && (activities.getSize() >= 2))
          return true;
        else if ((!hasAvatar(userId)) && (!hasContacts(userId)) && (!hasSpaces(userId)) && (activities.getSize() >= 1))
          return true;
        else
          return false;
      } else
        return false;
    } catch (Exception e) {
      LOG.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }

}
