package org.exoplatform.social.gettingStarted.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.gettingStarted.dto.GettingStartedStep;

public class GettingStartedService {

  private static final Log         LOG = ExoLogger.getLogger(GettingStartedService.class);

  private List<GettingStartedStep> gettingStartedSteps;

  public List<GettingStartedStep> getUserSteps(String lang) {

    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    List<GettingStartedStep> gettingStartedSteps = new ArrayList<>();
    ResourceBundleService resourceBundleService = CommonsUtils.getService(ResourceBundleService.class);
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("locale.portlet.social.GettingStartedPortlet",
                                                                            new Locale(lang));

    GettingStartedStep stepAvatar = new GettingStartedStep();
    stepAvatar.setName(resourceBundle.getString("social.portlet.gettingStarted.avatar"));
    stepAvatar.setStatus(hasAvatar(userId));
    gettingStartedSteps.add(stepAvatar);

    GettingStartedStep stepSpaces = new GettingStartedStep();
    stepSpaces.setName(resourceBundle.getString("social.portlet.gettingStarted.spaces"));
    stepSpaces.setStatus(hasSpaces(userId));
    gettingStartedSteps.add(stepSpaces);

    GettingStartedStep stepContact = new GettingStartedStep();
    stepContact.setName(resourceBundle.getString("social.portlet.gettingStarted.contacts"));
    stepContact.setStatus(hasContacts(userId));
    gettingStartedSteps.add(stepContact);

    GettingStartedStep stepActivities = new GettingStartedStep();
    stepActivities.setName(resourceBundle.getString("social.portlet.gettingStarted.activities"));
    stepActivities.setStatus(hasActivities(userId));
    gettingStartedSteps.add(stepActivities);

    return gettingStartedSteps;
  }

  private boolean hasAvatar(String userId) {
    try {
      IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(IdentityManager.class);
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
      SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer()
                                                                    .getComponentInstanceOfType(SpaceService.class);
      Space[] spaces = spaceService.getAccessibleSpacesWithListAccess(userId).load(0, 1);
      return spaces != null && spaces.length > 0;

    } catch (Exception e) {
      LOG.warn("Error when cheking user spaces: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean hasContacts(String userId) {
    try {
      IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(IdentityManager.class);
      RelationshipManager relationshipManager =
                                              (RelationshipManager) ExoContainerContext.getCurrentContainer()
                                                                                       .getComponentInstanceOfType(RelationshipManager.class);
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
      IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(IdentityManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
      ActivityManager activityService = (ActivityManager) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(ActivityManager.class);
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
