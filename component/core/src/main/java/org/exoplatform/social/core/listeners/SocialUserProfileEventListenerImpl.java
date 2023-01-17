/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.settings.ProfilePropertySettingsService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.storage.api.IdentityStorage;

public class SocialUserProfileEventListenerImpl extends UserProfileEventListener {

  private static final Log                     LOG                   =
                                                   ExoLogger.getLogger(SocialUserProfileEventListenerImpl.class);

  private final List<String>                   exlcudedAttributeList = List.of("authenticationAttempts");

  private static final String                  PLATFORM_GROUP_ID     = "/platform";

  private static final String                  PROFILE_GROUP_NAME    = "profile";

  private static final String                  MEMBER                = "member";

  private final IdentityManager                identityManager;

  private final ProfilePropertySettingsService profilePropertySettingsService;

  private final OrganizationService            organizationService;

  public SocialUserProfileEventListenerImpl(IdentityManager identityManager,
                                            ProfilePropertySettingsService profilePropertySettingsService,
                                            OrganizationService organizationService) {
    this.identityManager = identityManager;
    this.profilePropertySettingsService = profilePropertySettingsService;
    this.organizationService = organizationService;
  }

  @Override
  public void postSave(UserProfile userProfile, boolean isNew) throws Exception {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userProfile.getUserName());
    Profile profile = identity.getProfile();
    User user = organizationService.getUserHandler().findUserByName(identity.getRemoteId());
    String uGender = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]);// "user.gender"
    String uPosition = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]);// user.jobtitle
    String pGender = (String) profile.getProperty(Profile.GENDER);
    String pPosition = (String) profile.getProperty(Profile.POSITION);

    AtomicBoolean hasUpdated = new AtomicBoolean(false);
    Map<String, String> properties = userProfile.getUserInfoMap();
    exlcudedAttributeList.forEach(properties.keySet()::remove);
    properties.forEach((name, value) -> {
      updateProfilePropertySettings(name, profilePropertySettingsService);
      if (isNew) {
        profile.setProperty(name, value);
      } else if (!StringUtils.equals((String) profile.getProperty(name), userProfile.getAttribute(name))) {
        profile.setProperty(name, value);
        hasUpdated.set(true);
      }
    });

    if (!StringUtils.equals(uGender, pGender)) {
      profile.setProperty(Profile.GENDER, uGender);
      hasUpdated.set(true);
    }
    if (!StringUtils.equals(uPosition, pPosition)) {
      profile.setProperty(Profile.POSITION, uPosition);
      hasUpdated.set(true);
    }

    if (hasUpdated.get()) {
      List<Profile.UpdateType> updateTypes = new ArrayList<>();
      updateTypes.add(Profile.UpdateType.CONTACT);
      profile.setListUpdateTypes(updateTypes);
    }

    if (hasUpdated.get() && !isNew) {
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      identityManager.updateProfile(profile);
    }

    if (isNew) {
      IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);
      identityStorage.updateProfile(profile);
    }
    synchronizePropertyGroups(profile, user, profilePropertySettingsService, organizationService);
  }

  private void synchronizePropertyGroups(Profile profile,
                                         User user,
                                         ProfilePropertySettingsService profilePropertySettingsService,
                                         OrganizationService organizationService) throws Exception {
    Group platformGroup = organizationService.getGroupHandler().findGroupById(PLATFORM_GROUP_ID);
    Group profileGroup = createGroup(PROFILE_GROUP_NAME, platformGroup, organizationService);
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

  private void updateProfilePropertySettings(String propertyName, ProfilePropertySettingsService profilePropertySettingsService) {
    ProfilePropertySetting propertySetting = profilePropertySettingsService.getProfileSettingByName(propertyName);
    if (propertySetting == null) {
      ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
      profilePropertySetting.setPropertyName(propertyName);
      profilePropertySetting.setSystemProperty(false);
      profilePropertySetting.setActive(true);
      profilePropertySetting.setEditable(false);
      profilePropertySetting.setVisible(true);
      profilePropertySetting.setParentId(null);
      try {
        profilePropertySettingsService.createPropertySetting(profilePropertySetting);
      } catch (ObjectAlreadyExistsException e) {
        LOG.error("Error while adding new profile setting property", e);
      }
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
      Group group = getGroup(buildGroupId(parentGroup.getId(), groupName), organizationService);
      if (group != null) {
        return group;
      }
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group newGroup = groupHandler.createGroupInstance();
      newGroup.setGroupName(groupName.toLowerCase());
      newGroup.setLabel(StringUtils.capitalize(groupName));
      newGroup.setDescription(groupName + " group");
      groupHandler.addChild(parentGroup, newGroup, true);
      return getGroup(buildGroupId(parentGroup.getId(), groupName), organizationService);
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

  private String buildGroupId(String parentGroupId, String groupName) {
    return parentGroupId + "/" + groupName.toLowerCase();
  }
}
