/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.core.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

/**
 * A listener that extends {@link ProfileListenerPlugin} It will be triggued
 * when profile is updated to make profile properties synchronization with
 * organization groups: - Check for every profile property is it should be
 * synchronized using the {@link ProfilePropertyService} - if yes, create the
 * group related to this property (if the group not exist) - Add the profile
 * owner to the group
 */
public class GroupSynchronizationSocialProfileListener extends ProfileListenerPlugin {

  private static final Log             LOG                = ExoLogger.getLogger(GroupSynchronizationSocialProfileListener.class);

  private static final String          PROFILE_GROUP_NAME = "profile";

  private static final String          MEMBER             = "member";

  private final ProfilePropertyService profilePropertyService;

  private final OrganizationService    organizationService;

  public GroupSynchronizationSocialProfileListener(ProfilePropertyService profilePropertyService,
                                                   OrganizationService organizationService) {
    this.profilePropertyService = profilePropertyService;
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
    try {
      User user = organizationService.getUserHandler().findUserByName(profile.getIdentity().getRemoteId());
      synchronizePropertyGroups(profileLifeCycleEvent.getProfile(), user);
    } catch (Exception e) {
      LOG.error("Error while synchronizing profile property of user {} to groups", profile.getIdentity().getRemoteId(), e);
    }
  }

  private void synchronizePropertyGroups(Profile profile, User user) {
    try {
      Group profileGroup = getOrCreateGroup(PROFILE_GROUP_NAME, null);
      List<String> synchronizedProperties = profilePropertyService.getSynchronizedPropertySettings()
                                                                  .stream()
                                                                  .filter(ProfilePropertySetting::isActive)
                                                                  .map(ProfilePropertySetting::getPropertyName)
                                                                  .toList();
      Set<Map.Entry<String, Object>> properties = profile.getProperties().entrySet();
      Set<Map.Entry<String, Object>> propertiesToSynchronize =
                                                             properties.stream()
                                                                       .filter(property -> synchronizedProperties.contains(property.getKey()))
                                                                       .collect(Collectors.toSet());
      propertiesToSynchronize.forEach(property -> synchronizeProperty(property, profileGroup, user));
    } catch (Exception e) {
      LOG.error("Error while trying to add / create profile group {} for user ", PROFILE_GROUP_NAME, user.getUserName(), e);
    }
  }

  private void synchronizeProperty(Map.Entry<String, Object> property, Group profileGroup, User user) {
    String propertyName = property.getKey();
    String propertyValue = (String) property.getValue();
    try {
      Group newPropertyNameGroup = getOrCreateGroup(propertyName, profileGroup);
      try {
        Group newPropertyValueGroup = getOrCreateGroup(propertyValue, newPropertyNameGroup);
        addUserToGroup(newPropertyValueGroup, user);
      } catch (Exception e) {
        LOG.error("Error while adding property value group {} under property Group {}",
                  propertyValue,
                  newPropertyNameGroup != null ? newPropertyNameGroup.getId() : "/",
                  e);
      }
    } catch (Exception e) {
      LOG.error("Error while adding property group {} under profile Group {}",
                propertyName,
                profileGroup != null ? profileGroup.getId() : "/",
                e);
    }
  }

  private Group getGroup(String groupId) throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    return groupHandler.findGroupById(groupId);
  }

  private Group getOrCreateGroup(String groupName, Group parentGroup) throws Exception {
    Group group = getGroup(buildGroupId(parentGroup, groupName));
    if (group != null) {
      return group;
    }
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group newGroup = groupHandler.createGroupInstance();
    newGroup.setGroupName(groupName.toLowerCase());
    newGroup.setLabel(StringUtils.capitalize(groupName));
    newGroup.setDescription(groupName + " group");
    groupHandler.addChild(parentGroup, newGroup, true);
    return getGroup(buildGroupId(parentGroup, groupName));
  }

  private void addUserToGroup(Group group, User user) throws Exception {
    if (group == null) {
      return;
    }
    try {
      MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(MEMBER);
      organizationService.getMembershipHandler().linkMembership(user, group, membershipType, true);
    } catch (Exception e) {
      LOG.error("Error while adding user {} to Group {}", user.getUserName(), group.getId(), e);
      throw e;
    }
  }

  private String buildGroupId(Group parentGroup, String groupName) {
    if (parentGroup == null) {
      return "/" + groupName.toLowerCase();
    }
    return parentGroup.getId() + "/" + groupName.toLowerCase();
  }
}
