package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.profile.settings.ProfilePropertySettingsService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupSynchronizationSocialProfileListener extends ProfileListenerPlugin {

  private static final Log                     LOG                =
                                                   ExoLogger.getLogger(GroupSynchronizationSocialProfileListener.class);

  private static final String                  PROFILE_GROUP_NAME = "profile";

  private static final String                  MEMBER             = "member";

  private final ProfilePropertySettingsService profilePropertySettingsService;

  private final OrganizationService            organizationService;

  public GroupSynchronizationSocialProfileListener(ProfilePropertySettingsService profilePropertySettingsService,
                                                   OrganizationService organizationService) {
    this.profilePropertySettingsService = profilePropertySettingsService;
    this.organizationService = organizationService;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    profileUpdated(event);
  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {
    profileUpdated(event);
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    profileUpdated(event);

  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    profileUpdated(event);

  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    profileUpdated(event);

  }

  private void profileUpdated(ProfileLifeCycleEvent profileLifeCycleEvent) {
    Profile profile = profileLifeCycleEvent.getProfile();
    User user;
    try {
      user = organizationService.getUserHandler().findUserByName(profile.getIdentity().getRemoteId());
      synchronizePropertyGroups(profileLifeCycleEvent.getProfile(), user, profilePropertySettingsService, organizationService);
    } catch (Exception e) {
      LOG.error("Error while synchronizing profile property groups");
    }
  }

  private void synchronizePropertyGroups(Profile profile,
                                         User user,
                                         ProfilePropertySettingsService profilePropertySettingsService,
                                         OrganizationService organizationService) {
    Group profileGroup = createGroup(PROFILE_GROUP_NAME, null, organizationService);
    if (profileGroup != null) {
      List<String> synchronizedProperties = profilePropertySettingsService.getSynchronizedPropertySettings()
                                                                          .stream()
                                                                          .filter(ProfilePropertySetting::isActive)
                                                                          .map(ProfilePropertySetting::getPropertyName)
                                                                          .toList();
      Set<Map.Entry<String, Object>> properties = profile.getProperties().entrySet();
      properties.stream().filter(property -> synchronizedProperties.contains(property.getKey())).forEach(property -> {
        String propertyName = property.getKey();
        String propertyValue = (String) property.getValue();
        Group newPropertyNameGroup = createGroup(propertyName, profileGroup, organizationService);
        if (newPropertyNameGroup != null) {
          Group newPropertyValueGroup = createGroup(propertyValue, newPropertyNameGroup, organizationService);
          addUserToGroup(newPropertyValueGroup, user, organizationService);
        }
      });
    }
  }

  private Group getGroup(String groupId, OrganizationService organizationService) {
    try {
      GroupHandler groupHandler = organizationService.getGroupHandler();
      return groupHandler.findGroupById(groupId);
    } catch (Exception e) {
      return null;
    }
  }

  private Group createGroup(String groupName, Group parentGroup, OrganizationService organizationService) {
    try {
      Group group = getGroup(buildGroupId(parentGroup, groupName), organizationService);
      if (group != null) {
        return group;
      }
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group newGroup = groupHandler.createGroupInstance();
      newGroup.setGroupName(groupName.toLowerCase());
      newGroup.setLabel(StringUtils.capitalize(groupName));
      newGroup.setDescription(groupName + " group");
      groupHandler.addChild(parentGroup, newGroup, true);
      return getGroup(buildGroupId(parentGroup, groupName), organizationService);
    } catch (Exception e) {
      return null;
    }
  }

  private void addUserToGroup(Group group, User user, OrganizationService organizationService) {
    if (group == null) {
      return;
    }
    try {
      MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(MEMBER);
      organizationService.getMembershipHandler().linkMembership(user, group, membershipType, true);
    } catch (Exception e) {
      LOG.error("Error while adding user to Group", e);
    }
  }

  private String buildGroupId(Group parentGroup, String groupName) {
    if (parentGroup == null) {
      return "/" + groupName.toLowerCase();
    }
    return parentGroup.getId() + "/" + groupName.toLowerCase();
  }
}
