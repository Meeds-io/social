/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.storage.cache.model.data;

import java.util.List;

import org.exoplatform.social.core.space.model.Space;

import lombok.EqualsAndHashCode;

/**
 * Immutable space data.
 *
 */
@EqualsAndHashCode
public class SpaceData implements CacheData<Space> {
  private static final long serialVersionUID = 6109309246791818373L;

  private final long        id;

  private final String      prettyName;

  private final String      displayName;

  private final String      registration;

  private final String      description;

  private final String      visibility;

  private final String      avatarUrl;

  private final String      bannerUrl;

  private final String      groupId;

  private final String      url;

  private final Long        avatarLastUpdated;

  private final Long        bannerLastUpdated;

  private final Long        createdTime;

  private final Long        lastUpdatedTime;

  private final long        templateId;

  private final long        cacheTime;

  private final String[]    members;

  private final String[]    redactors;

  private final String[]    publishers;

  private final String[]    managers;

  private final String[]    pendingUser;

  private final String[]    invitedUser;

  private long              publicSiteId;

  private String            publicSiteVisibility;

  private List<String>      layoutPermissions;

  private List<String>      deletePermissions;

  private List<String>      publicSitePermissions;

  private List<Long>        categoryIds;

  private boolean           sovereign;

  private Long              parentSpaceId;

  public SpaceData(final Space space) {
    id = space.getSpaceId();
    templateId = space.getTemplateId();
    prettyName = space.getPrettyName();
    displayName = space.getDisplayName();
    registration = space.getRegistration();
    description = space.getDescription();
    visibility = space.getVisibility();
    avatarLastUpdated = space.getAvatarLastUpdated();
    bannerLastUpdated = space.getBannerLastUpdated();
    avatarUrl = space.getAvatarUrl();
    bannerUrl = space.getBannerUrl();
    groupId = space.getGroupId();
    url = space.getUrl();
    members = space.getMembers();
    redactors = space.getRedactors();
    publishers = space.getPublishers();
    managers = space.getManagers();
    pendingUser = space.getPendingUsers();
    invitedUser = space.getInvitedUsers();
    createdTime = space.getCreatedTime();
    publicSiteId = space.getPublicSiteId();
    publicSiteVisibility = space.getPublicSiteVisibility();
    layoutPermissions = space.getLayoutPermissions();
    deletePermissions = space.getDeletePermissions();
    publicSitePermissions = space.getPublicSitePermissions();
    categoryIds = space.getCategoryIds();
    sovereign = space.isSovereign();
    parentSpaceId = space.getParentSpaceId();
    lastUpdatedTime = space.getLastUpdatedTime();
    cacheTime = System.currentTimeMillis();
  }

  public Space build() {
    Space space = new Space();
    space.setId(id);
    space.setTemplateId(templateId);
    space.setDisplayName(displayName);
    space.setPrettyName(prettyName);
    space.setRegistration(registration);
    space.setDescription(description);
    space.setVisibility(visibility);
    space.setAvatarLastUpdated(avatarLastUpdated);
    space.setBannerLastUpdated(bannerLastUpdated);
    space.setAvatarUrl(avatarUrl);
    space.setBannerUrl(bannerUrl);
    space.setGroupId(groupId);
    space.setUrl(url);
    space.setMembers(members);
    space.setRedactors(redactors);
    space.setPublishers(publishers);
    space.setManagers(managers);
    space.setPendingUsers(pendingUser);
    space.setInvitedUsers(invitedUser);
    space.setCreatedTime(createdTime);
    space.setLastUpdatedTime(lastUpdatedTime);
    space.setCacheTime(cacheTime);
    space.setPublicSiteId(publicSiteId);
    space.setPublicSiteVisibility(publicSiteVisibility);
    space.setLayoutPermissions(layoutPermissions);
    space.setDeletePermissions(deletePermissions);
    space.setPublicSitePermissions(publicSitePermissions);
    space.setCategoryIds(categoryIds);
    space.setSovereign(sovereign);
    space.setParentSpaceId(parentSpaceId);
    return space;
  }
}
