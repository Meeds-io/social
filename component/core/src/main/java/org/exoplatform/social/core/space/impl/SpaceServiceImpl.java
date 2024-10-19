/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.space.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceException.Code;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceLifecycle;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceListAccessType;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent.Type;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

import io.meeds.social.core.search.SpaceSearchConnector;
import io.meeds.social.space.template.service.SpaceTemplateLayoutService;
import io.meeds.social.space.template.service.SpaceTemplateService;

import lombok.SneakyThrows;

/**
 * {@link org.exoplatform.social.core.space.spi.SpaceService} implementation.
 * 
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since August 29, 2008
 */
public class SpaceServiceImpl implements SpaceService {

  private static final Log            LOG            = ExoLogger.getLogger(SpaceServiceImpl.class.getName());

  public static final String          MEMBER         = "member";

  public static final String          MANAGER        = "manager";

  private SpaceStorage                spaceStorage;

  private SpaceSearchConnector        spaceSearchConnector;

  private GroupSpaceBindingStorage    groupSpaceBindingStorage;

  private IdentityManager             identityManager;

  private OrganizationService         organizationService;

  private UserACL                     userAcl;

  private SpaceLifecycle              spaceLifeCycle = new SpaceLifecycle();

  /** The limit for list access loading. */
  private static final int            LIMIT          = 200;

  private SpacesAdministrationService spacesAdministrationService;

  private LayoutService               layoutService;

  private SpaceTemplateService        spaceTemplateService;

  private SpaceTemplateLayoutService  spaceTemplateLayoutService;

  public SpaceServiceImpl(SpaceStorage spaceStorage, // NOSONAR
                          GroupSpaceBindingStorage groupSpaceBindingStorage,
                          SpaceSearchConnector spaceSearchConnector,
                          IdentityManager identityManager,
                          SpacesAdministrationService spacesAdministrationService,
                          SpaceTemplateService spaceTemplateService,
                          SpaceTemplateLayoutService  spaceTemplateLayoutService,
                          LayoutService layoutService,
                          UserACL userAcl) {
    this.spaceStorage = spaceStorage;
    this.groupSpaceBindingStorage = groupSpaceBindingStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.identityManager = identityManager;
    this.spacesAdministrationService = spacesAdministrationService;
    this.spaceTemplateService = spaceTemplateService;
    this.spaceTemplateLayoutService = spaceTemplateLayoutService;
    this.layoutService = layoutService;
    this.userAcl = userAcl;
  }

  @Override
  public ListAccess<Space> getAllSpacesWithListAccess() {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, SpaceListAccessType.ALL);
  }

  @Override
  public Space getSpaceByDisplayName(String spaceDisplayName) {
    return spaceStorage.getSpaceByDisplayName(spaceDisplayName);
  }

  @Override
  public Space getSpaceByPrettyName(String spacePrettyName) {
    return spaceStorage.getSpaceByPrettyName(spacePrettyName);
  }

  @Override
  public Instant getSpaceMembershipDate(long spaceId, String username) {
    return spaceStorage.getSpaceMembershipDate(spaceId, username);
  }

  @Override
  public Space getSpaceByGroupId(String groupId) {
    return spaceStorage.getSpaceByGroupId(groupId);
  }

  @Override
  public Space getSpaceById(String id) {
    return spaceStorage.getSpaceById(id);
  }

  @Override
  public Space getSpaceByUrl(String url) {
    return spaceStorage.getSpaceByUrl(url);
  }

  @Override
  public SpaceListAccess getAccessibleSpacesWithListAccess(String username) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, SpaceListAccessType.ACCESSIBLE);
  }

  @Override
  public SpaceListAccess getVisibleSpacesWithListAccess(String username, SpaceFilter spaceFilter) {
    if (isSuperManager(username)) {
      if (spaceFilter == null)
        return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.ALL);
      else
        return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.ALL_FILTER);
    } else {
      return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.VISIBLE);
    }
  }

  @Override
  public SpaceListAccess getPendingSpacesWithListAccess(String username) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, SpaceListAccessType.PENDING);
  }

  @Override
  @SneakyThrows
  public Space createSpace(Space space, String creator) {
    return createSpace(space, creator, null);
  }

  @Override
  public Space createSpace(Space space, String username, List<Identity> identitiesToInvite) throws SpaceException {
    if (!spaceTemplateService.canCreateSpace(space.getTemplateId(), username)) {
      throw new SpaceException(Code.SPACE_PERMISSION);
    } else if (space.getDisplayName().length() > LIMIT) {
      throw new IllegalArgumentException("Error while creating the space " + space.getDisplayName() +
          ": space name cannot exceed 200 characters");
    }

    // Add creator as a manager and a member to this space
    String[] managers = space.getManagers();
    if (!ArrayUtils.contains(managers, username)) {
      managers = ArrayUtils.add(managers, username);
    }
    space.setManagers(managers);

    String[] members = space.getMembers();
    if (!ArrayUtils.contains(members, username)) {
      members = ArrayUtils.add(members, username);
    }
    space.setMembers(members);

    // Creates new space by creating new group
    String groupId = SpaceUtils.createGroup(space.getDisplayName(), space.getPrettyName(), username);
    String prettyName = groupId.replace("/spaces/", "");
    space.setGroupId(groupId);
    space.setPrettyName(prettyName);
    space.setUrl(space.getPrettyName());
    space.setTemplateId(space.getTemplateId());

    spaceLifeCycle.setCurrentEvent(Type.SPACE_CREATED);
    try {
      spaceStorage.saveSpace(space, true);
      spaceLifeCycle.spaceCreated(space, username);
    } catch (Exception e) {
      throw new SpaceException(Code.ERROR_DATASTORE,
                               String.format("Failed to save the space %s",
                                             space.getPrettyName()),
                               e);
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.SPACE_CREATED);
    }

    if (CollectionUtils.isNotEmpty(identitiesToInvite)) {
      try {
        inviteIdentities(space, identitiesToInvite);
      } catch (Exception e) {
        LOG.warn("Error inviting identities {} to space {}", identitiesToInvite, space.getDisplayName(), e);
      }
    }

    return getSpaceById(space.getId());
  }

  @Override
  public void inviteIdentities(Space space, List<Identity> identitiesToInvite) {
    if (identitiesToInvite == null || identitiesToInvite.isEmpty()) {
      return;
    }

    Set<String> usernames = getUsersToInvite(identitiesToInvite);
    for (String username : usernames) {
      if (isMember(space, username)) {
        continue;
      }

      if (!isInvitedUser(space, username)) {
        addInvitedUser(space, username);
      }
    }
  }

  @Override
  public boolean isSpaceContainsExternals(Long spaceId) {
    return spaceStorage.countExternalMembers(spaceId) != 0;
  }

  @Override
  public void createSpace(Space space) {
    spaceLifeCycle.setCurrentEvent(Type.SPACE_CREATED);
    try {
      spaceStorage.saveSpace(space, true);
      spaceLifeCycle.spaceCreated(space, space.getEditor());
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.SPACE_CREATED);
    }
  }

  @Override
  public void renameSpace(Space space, String newDisplayName) {
    spaceLifeCycle.setCurrentEvent(Type.SPACE_RENAMED);
    try {
      spaceStorage.renameSpace(space, newDisplayName);
      spaceLifeCycle.spaceRenamed(space, space.getEditor());
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.SPACE_RENAMED);
    }
  }

  @Override
  public void renameSpace(Space space, String newDisplayName, String username) throws SpaceException {
    if (username == null || !canManageSpace(getSpaceById(space.getId()), username)) {
      throw new SpaceException(Code.UNAUTHORIZED_TO_RENAME_SPACE);
    }
    renameSpace(space, newDisplayName);
  }

  @Override
  public void deleteSpace(Space space) throws SpaceException {
    deleteSpace(space, true);
  }

  @Override
  @SneakyThrows
  public void deleteSpace(Space space, boolean deleteGroup) throws SpaceException {
    spaceLifeCycle.setCurrentEvent(Type.SPACE_REMOVED);
    try {
      List<GroupSpaceBinding> groupSpaceBindings = groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(space.getId());
      groupSpaceBindings.stream().map(GroupSpaceBinding::getId).forEach(groupSpaceBindingStorage::deleteGroupBinding);

      // remove memberships of users with deleted space.
      SpaceUtils.removeMembershipFromGroup(space);

      Identity spaceIdentity = null;
      if (identityManager.identityExisted(SpaceIdentityProvider.NAME, space.getPrettyName())) {
        spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
      }
      spaceStorage.deleteSpace(space.getId());
      if (spaceIdentity != null) {
        identityManager.hardDeleteIdentity(spaceIdentity);
      }

      if (deleteGroup) {
        GroupHandler groupHandler = getOrgService().getGroupHandler();
        Group spaceGroup = groupHandler.findGroupById(space.getGroupId());
        List<String> mandatories = userAcl.getMandatoryGroups();
        if (spaceGroup != null && !isMandatory(groupHandler, spaceGroup, mandatories)) {
          SpaceUtils.removeGroup(space);
        }
      }
      spaceLifeCycle.spaceRemoved(space, space.getEditor());
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.SPACE_REMOVED);
    }
  }

  @Override
  public void addMember(Space space, String username) {
    spaceLifeCycle.setCurrentEvent(Type.JOINED);
    try {
      String[] members = space.getMembers();
      space = this.removeInvited(space, username);
      space = this.removePending(space, username);
      if (!ArrayUtils.contains(members, username)) {
        members = ArrayUtils.add(members, username);
        space.setMembers(members);
        this.updateSpace(space);
        SpaceUtils.addUserToGroupWithMemberMembership(username, space.getGroupId());
        spaceLifeCycle.memberJoined(space, username);
      }
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.JOINED);
    }
  }

  @Override
  public void removeMember(Space space, String username) {
    if (groupSpaceBindingStorage.countUserBindings(space.getId(), username) > 0) {
      throw new IllegalStateException("space.cantLeaveBoundSpace");
    }
    if (isManager(space, username)) {
      setManager(space, username, false);
    }
    if (isManager(space, username)) {
      setManager(space, username, false);
    }
    if (isRedactor(space, username)) {
      removeRedactor(space, username);
    }
    if (isPublisher(space, username)) {
      removePublisher(space, username);
    }
    spaceLifeCycle.setCurrentEvent(Type.LEFT);
    try {
      Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
      if (spaceIdentity != null && spaceIdentity.isDeleted()) {
        return;
      }
      String[] members = space.getMembers();
      List<String> disabledMembers = identityManager.getDisabledSpaceMembers(Long.parseLong(space.getId()));
      if (disabledMembers != null && !disabledMembers.isEmpty()) {
        members = ArrayUtils.addAll(members, disabledMembers.toArray(String[]::new));
      }
      if (ArrayUtils.contains(members, username)) {
        members = ArrayUtils.removeAllOccurrences(members, username);
        space.setMembers(members);
        this.updateSpace(space);
        SpaceUtils.removeUserFromGroupWithMemberMembership(username, space.getGroupId());
        setManager(space, username, false);
        removeRedactor(space, username);
        SpaceUtils.removeUserFromGroupWithAnyMembership(username, space.getGroupId());
        spaceLifeCycle.memberLeft(space, username);
      }
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.LEFT);
    }
  }

  @Override
  public boolean isMember(Space space, String username) {
    return space != null && ArrayUtils.contains(space.getMembers(), username);
  }

  @Override
  public boolean isIgnored(Space space, String username) {
    return space != null && spaceStorage.isSpaceIgnored(space.getId(), username);
  }

  @Override
  public void setIgnored(String spaceId, String username) {
    spaceLifeCycle.setCurrentEvent(Type.DENY_INVITED_USER);
    try {
      spaceStorage.ignoreSpace(spaceId, username);
      spaceLifeCycle.removeInvitedUser(getSpaceById(spaceId), username);
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.DENY_INVITED_USER);
    }
  }

  @Override
  public void registerSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    spaceLifeCycle.addListener(listener);
  }

  @Override
  public void unregisterSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    spaceLifeCycle.removeListener(listener);
  }

  @Override
  public void addInvitedUser(Space space, String username) {
    if (ArrayUtils.contains(space.getMembers(), username)) {
      // user is already member. Do nothing
      return;
    }

    if (isPendingUser(space, username)) {
      removePending(space, username);
      addMember(space, username);
    } else {
      addInvited(space, username);
    }
    this.updateSpace(space);
    spaceLifeCycle.addInvitedUser(space, username);
  }

  @Override
  public void addPendingUser(Space space, String username) {
    if (ArrayUtils.contains(space.getMembers(), username)) {
      // user is already member. Do nothing
      return;
    }

    if (ArrayUtils.contains(space.getPendingUsers(), username)) {
      // user is already pending. Do nothing
      return;
    }

    if (ArrayUtils.contains(space.getInvitedUsers(), username)) {
      this.addMember(space, username);
      removeInvited(space, username);
      this.updateSpace(space);
      return;
    }

    String registration = space.getRegistration();
    String visibility = space.getVisibility();
    if (visibility.equals(Space.HIDDEN) && registration.equals(Space.CLOSED)) {
      LOG.warn("Unable request to join hidden");
      return;
    }
    if (registration.equals(Space.OPEN)) {
      addMember(space, username);
    } else if (registration.equals(Space.VALIDATION)) {
      spaceLifeCycle.setCurrentEvent(Type.ADD_PENDING_USER);
      try {
        addPending(space, username);
        spaceStorage.saveSpace(space, false);
        spaceLifeCycle.addPendingUser(space, username);
      } finally {
        spaceLifeCycle.resetCurrentEvent(Type.ADD_PENDING_USER);
      }
    } else {
      LOG.warn("Unable request to join");
    }
  }

  @Override
  public ListAccess<Space> getAccessibleSpacesByFilter(String username, SpaceFilter spaceFilter) {
    if (isSuperManager(username)) {
      return new SpaceListAccess(spaceStorage,
                                 spaceSearchConnector,
                                 spaceFilter,
                                 spaceFilter == null ? SpaceListAccessType.ALL : SpaceListAccessType.ALL_FILTER);
    } else {
      return new SpaceListAccess(spaceStorage,
                                 spaceSearchConnector,
                                 username,
                                 spaceFilter,
                                 spaceFilter == null ? SpaceListAccessType.ACCESSIBLE : SpaceListAccessType.ACCESSIBLE_FILTER);
    }
  }

  @Override
  public ListAccess<Space> getAllSpacesByFilter(SpaceFilter spaceFilter) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, spaceFilter, SpaceListAccessType.ALL_FILTER);
  }

  @Override
  public ListAccess<Space> getInvitedSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.INVITED_FILTER);
  }

  @Override
  public ListAccess<Space> getMemberSpaces(String username) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, SpaceListAccessType.MEMBER);
  }

  @Override
  public List<String> getMemberSpacesIds(String username, int offset, int limit) {
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    if (identity == null) {
      return Collections.emptyList();
    } else {
      return this.spaceStorage.getMemberRoleSpaceIds(identity.getId(), offset, limit);
    }
  }

  @Override
  public List<String> getManagerSpacesIds(String username, int offset, int limit) {
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    if (identity == null) {
      return Collections.emptyList();
    } else {
      return this.spaceStorage.getManagerRoleSpaceIds(identity.getId(), offset, limit);
    }
  }

  @Override
  public ListAccess<Space> getManagerSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.MANAGER_FILTER);
  }

  @Override
  public ListAccess<Space> getManagerSpaces(String username) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, SpaceListAccessType.MANAGER);
  }

  @Override
  public ListAccess<Space> getMemberSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.MEMBER_FILTER);
  }

  @Override
  public ListAccess<Space> getFavoriteSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.FAVORITE_FILTER);
  }

  @Override
  public ListAccess<Space> getPendingSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, spaceFilter, SpaceListAccessType.PENDING_FILTER);
  }

  @Override
  public boolean hasSettingPermission(Space space, String username) {
    return canManageSpace(space, username);
  }

  @Override
  public boolean canAccessSpacePublicSite(Space space, String username) {
    if (space == null
        || space.getPublicSiteId() == 0
        || StringUtils.isBlank(space.getPublicSiteVisibility())) {
      return false;
    } else if (StringUtils.equals(space.getPublicSiteVisibility(), SpaceUtils.EVERYONE)) {
      return true;
    } else if (userAcl.isAnonymousUser(username)) {
      return false;
    } else if (StringUtils.equals(space.getPublicSiteVisibility(), SpaceUtils.AUTHENTICATED)) {
      return true;
    } else if (StringUtils.equals(space.getPublicSiteVisibility(), SpaceUtils.INTERNAL)) {
      return userAcl.getUserIdentity(username).isMemberOf(SpaceUtils.PLATFORM_USERS_GROUP);
    } else if (StringUtils.equals(space.getPublicSiteVisibility(), SpaceUtils.MEMBER)) {
      return canViewSpace(space, username);
    } else if (StringUtils.equals(space.getPublicSiteVisibility(), SpaceUtils.MANAGER)) {
      return canManageSpace(space, username);
    }
    return false;
  }

  @Override
  public String getSpacePublicSiteName(Space space) {
    if (space == null || space.getPublicSiteId() == 0) {
      return null;
    } else {
      PortalConfig portalConfig = layoutService.getPortalConfig(space.getPublicSiteId());
      return portalConfig == null ? null : portalConfig.getName();
    }
  }

  @Override
  public boolean isInvitedUser(Space space, String username) {
    return space != null && ArrayUtils.contains(space.getInvitedUsers(), username);
  }

  @Override
  public boolean isManager(Space space, String username) {
    return space != null && ArrayUtils.contains(space.getManagers(), username);
  }

  @Override
  public boolean isRedactor(Space space, String username) {
    return space != null && ArrayUtils.contains(space.getRedactors(), username);
  }

  @Override
  public boolean isPublisher(Space space, String username) {
    return space != null && ArrayUtils.contains(space.getPublishers(), username);
  }

  @Override
  public boolean hasRedactor(Space space) {
    return space != null && space.getRedactors() != null && space.getRedactors().length > 0;
  }

  @Override
  public boolean isOnlyManager(Space space, String username) {
    return space.getManagers() != null
           && space.getManagers().length == 1
           && StringUtils.equals(space.getManagers()[0], username);
  }

  @Override
  public boolean isPendingUser(Space space, String username) {
    return ArrayUtils.contains(space.getPendingUsers(), username);
  }

  @Override
  public void registerSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    spaceLifeCycle.addListener(spaceListenerPlugin);
  }

  @Override
  public void removeInvitedUser(Space space, String username) {
    if (ArrayUtils.contains(space.getInvitedUsers(), username)) {
      spaceLifeCycle.setCurrentEvent(Type.DENY_INVITED_USER);
      try {
        space = this.removeInvited(space, username);
        this.updateSpace(space);
        spaceLifeCycle.removeInvitedUser(space, username);
      } finally {
        spaceLifeCycle.resetCurrentEvent(Type.DENY_INVITED_USER);
      }
    }
  }

  @Override
  public void removePendingUser(Space space, String username) {
    if (ArrayUtils.contains(space.getPendingUsers(), username)) {
      this.removePending(space, username);
      space = this.updateSpace(space);
      spaceLifeCycle.removePendingUser(space, username);
    }
  }

  @Override
  public void addRedactor(Space space, String username) {
    String[] redactors = space.getRedactors();
    if (!ArrayUtils.contains(redactors, username)) {
      redactors = ArrayUtils.add(redactors, username);
      space.setRedactors(redactors);
      this.updateSpace(space);
      SpaceUtils.addUserToGroupWithRedactorMembership(username, space.getGroupId());
    }
  }

  @Override
  public void removeRedactor(Space space, String username) {
    String[] redactors = space.getRedactors();
    if (ArrayUtils.contains(redactors, username)) {
      redactors = ArrayUtils.removeAllOccurrences(redactors, username);
      space.setRedactors(redactors);
      this.updateSpace(space);
      SpaceUtils.removeUserFromGroupWithRedactorMembership(username, space.getGroupId());
    }
  }

  @Override
  public void addPublisher(Space space, String username) {
    String[] publishers = space.getPublishers();
    if (!ArrayUtils.contains(publishers, username)) {
      publishers = ArrayUtils.add(publishers, username);
      space.setPublishers(publishers);
      this.updateSpace(space);
      SpaceUtils.addUserToGroupWithPublisherMembership(username, space.getGroupId());
    }
  }

  @Override
  public void removePublisher(Space space, String username) {
    String[] publishers = space.getPublishers();
    if (ArrayUtils.contains(publishers, username)) {
      publishers = ArrayUtils.removeAllOccurrences(publishers, username);
      space.setPublishers(publishers);
      this.updateSpace(space);
      SpaceUtils.removeUserFromGroupWithPublisherMembership(username, space.getGroupId());
    }
  }

  @Override
  public void setManager(Space space, String username, boolean isManager) {
    String[] managers = space.getManagers();
    if (isManager) {
      if (!ArrayUtils.contains(managers, username)) {
        spaceLifeCycle.setCurrentEvent(Type.GRANTED_LEAD);
        try {
          managers = ArrayUtils.add(managers, username);
          space.setManagers(managers);
          this.updateSpace(space);
          SpaceUtils.addUserToGroupWithManagerMembership(username, space.getGroupId());
          spaceLifeCycle.grantedLead(space, username);
        } finally {
          spaceLifeCycle.resetCurrentEvent(Type.GRANTED_LEAD);
        }
      }
    } else {
      if (ArrayUtils.contains(managers, username)) {
        spaceLifeCycle.setCurrentEvent(Type.REVOKED_LEAD);
        try {
          managers = ArrayUtils.removeAllOccurrences(managers, username);
          space.setManagers(managers);
          this.updateSpace(space);
          SpaceUtils.removeUserFromGroupWithManagerMembership(username, space.getGroupId());
          Space updatedSpace = getSpaceById(space.getId());
          if (isMember(updatedSpace, username)) {
            spaceLifeCycle.revokedLead(space, username);
          }
        } finally {
          spaceLifeCycle.resetCurrentEvent(Type.REVOKED_LEAD);
        }
      }
    }
  }

  @Override
  public void unregisterSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    spaceLifeCycle.removeListener(spaceListenerPlugin);
  }

  @Override
  public Space updateSpace(Space space, List<Identity> identitiesToInvite) {
    Space storedSpace = spaceStorage.getSpaceById(space.getId());
    spaceStorage.saveSpace(space, false);
    triggerSpaceUpdate(space, storedSpace);

    inviteIdentities(space, identitiesToInvite);

    return getSpaceById(space.getId());
  }

  @Override
  public Space updateSpace(Space existingSpace) {
    return this.updateSpace(existingSpace, null);
  }

  @Override
  public Space updateSpaceAvatar(Space existingSpace, String username) {
    existingSpace.setEditor(username);
    checkSpaceEditorPermissions(existingSpace);

    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(existingSpace.getPrettyName());
    Profile profile = spaceIdentity.getProfile();
    if (existingSpace.getAvatarAttachment() != null) {
      profile.setProperty(Profile.AVATAR, existingSpace.getAvatarAttachment());
    } else {
      profile.removeProperty(Profile.AVATAR);
      profile.setAvatarUrl(null);
      profile.setAvatarLastUpdated(null);
    }
    identityManager.updateProfile(profile);
    spaceLifeCycle.spaceAvatarEdited(existingSpace, existingSpace.getEditor());

    existingSpace = spaceStorage.getSpaceById(existingSpace.getId());
    existingSpace.setAvatarLastUpdated(System.currentTimeMillis());
    spaceStorage.saveSpace(existingSpace, false);
    return existingSpace;
  }

  @Override
  public Space updateSpaceBanner(Space existingSpace, String username) {
    existingSpace.setEditor(username);
    checkSpaceEditorPermissions(existingSpace);

    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(existingSpace.getPrettyName());
    if (spaceIdentity != null) {
      Profile profile = spaceIdentity.getProfile();
      if (existingSpace.getBannerAttachment() != null) {
        profile.setProperty(Profile.BANNER, existingSpace.getBannerAttachment());
      } else {
        profile.removeProperty(Profile.BANNER);
      }
      identityManager.updateProfile(profile);

      existingSpace = spaceStorage.getSpaceById(existingSpace.getId());
      existingSpace.setAvatarLastUpdated(System.currentTimeMillis());
      spaceStorage.saveSpace(existingSpace, false);

      spaceLifeCycle.spaceBannerEdited(existingSpace, existingSpace.getEditor());
    } else {
      throw new IllegalStateException("Can not update space banner. Space identity " + existingSpace.getPrettyName() +
          " not found");
    }
    return existingSpace;
  }

  @Override
  public ListAccess<Space> getInvitedSpacesWithListAccess(String username) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, username, SpaceListAccessType.INVITED);
  }

  @Override
  public void updateSpaceAccessed(String remoteId, Space space) {
    if (isMember(space, remoteId)) {
      spaceStorage.updateSpaceAccessed(remoteId, space);
    }
  }

  @Override
  public List<Space> getLastSpaces(int limit) {
    return spaceStorage.getLastSpaces(limit);
  }

  @Override
  public ListAccess<Space> getLastAccessedSpace(String remoteId) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, remoteId, SpaceListAccessType.LASTEST_ACCESSED);
  }

  @Override
  public ListAccess<Space> getVisitedSpaces(String remoteId) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, remoteId, SpaceListAccessType.VISITED);
  }

  @Override
  public ListAccess<Space> getPendingSpaceRequestsToManage(String remoteId) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, remoteId, SpaceListAccessType.PENDING_REQUESTS);
  }

  @Override
  public List<SpaceExternalInvitation> findSpaceExternalInvitationsBySpaceId(String spaceId) {
    return spaceStorage.findSpaceExternalInvitationsBySpaceId(spaceId);
  }

  @Override
  public void saveSpaceExternalInvitation(String spaceId, String email, String tokenId) {
    spaceStorage.saveSpaceExternalInvitation(spaceId, email, tokenId);
  }

  @Override
  public void saveSpacePublicSite(String spaceId,
                                  String publicSiteVisibility,
                                  String authenticatedUser) throws ObjectNotFoundException, IllegalAccessException {
    Space space = getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException("Space not found");
    } else if (!canManageSpace(space, authenticatedUser)) {
      throw new IllegalAccessException("User isn't manager of the space");
    }
    space.setEditor(authenticatedUser);

    saveSpacePublicSite(space, publicSiteVisibility, authenticatedUser);
  }

  @Override
  public void saveSpacePublicSite(Space space, String publicSiteVisibility) {
    saveSpacePublicSite(space, publicSiteVisibility, null);
  }

  @Override
  public SpaceExternalInvitation getSpaceExternalInvitationById(String invitationId) {
    return spaceStorage.findSpaceExternalInvitationById(invitationId);
  }

  @Override
  public void deleteSpaceExternalInvitation(String invitationId) {
    SpaceExternalInvitation spaceExternalInvitation = spaceStorage.findSpaceExternalInvitationById(invitationId);
    spaceStorage.deleteSpaceExternalInvitation(spaceExternalInvitation);
    // Delete the token from store
    RemindPasswordTokenService remindPasswordTokenService = CommonsUtils.getService(RemindPasswordTokenService.class);
    if (remindPasswordTokenService != null) {
      remindPasswordTokenService.deleteToken(spaceExternalInvitation.getTokenId(),
                                             CookieTokenService.EXTERNAL_REGISTRATION_TOKEN);
    }
  }

  @Override
  public List<String> findExternalInvitationsSpacesByEmail(String email) {
    return spaceStorage.findExternalInvitationsSpacesByEmail(email);
  }

  @Override
  public void deleteExternalUserInvitations(String email) {
    spaceStorage.deleteExternalUserInvitations(email);
  }

  @Override
  public boolean isSuperManager(String username) {
    return spacesAdministrationService.isSuperManager(username);
  }

  @Override
  public boolean isContentManager(String username) {
    if (userAcl.isAnonymousUser(username)) {
      return false;
    } else if (isSuperManager(username)) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = userAcl.getUserIdentity(username);
    return identity != null && identity.isMemberOf(SpaceUtils.PLATFORM_PUBLISHER_GROUP, SpaceUtils.MANAGER);
  }

  @Override
  public boolean isContentPublisher(String username) {
    if (userAcl.isAnonymousUser(username)) {
      return false;
    } else if (isSuperManager(username)) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = userAcl.getUserIdentity(username);
    return identity != null && identity.isMemberOf(SpaceUtils.PLATFORM_PUBLISHER_GROUP, SpaceUtils.PUBLISHER);
  }

  @Override
  public ListAccess<Space> getCommonSpaces(String username, String otherUserId) {
    return new SpaceListAccess(spaceStorage, spaceSearchConnector, SpaceListAccessType.COMMON, username, otherUserId);
  }

  @Override
  public Map<Long, Long> countSpacesByTemplate() {
    return spaceStorage.countSpacesByTemplate();
  }

  public void addSpaceListener(SpaceListenerPlugin plugin) {
    registerSpaceLifeCycleListener(plugin);
  }

  private String checkSpaceEditorPermissions(Space space) {
    String editor = space.getEditor();
    if (StringUtils.isBlank(editor) || !canManageSpace(space, editor)) {
      throw new IllegalStateException("User " + editor + " is not authorized to change space.");
    }
    return editor;
  }

  private void triggerSpaceUpdate(Space newSpace, Space oldSpace) {
    if (oldSpace != null) {
      if (!StringUtils.equals(oldSpace.getDescription(), newSpace.getDescription())) {
        spaceLifeCycle.spaceDescriptionEdited(newSpace, newSpace.getEditor());
      }
      if (!oldSpace.getVisibility().equals(newSpace.getVisibility())) {
        spaceLifeCycle.spaceAccessEdited(newSpace, newSpace.getEditor());
      }
      String oldRegistration = oldSpace.getRegistration();
      String registration = newSpace.getRegistration();
      if ((oldRegistration == null && registration != null)
          || (oldRegistration != null && !oldRegistration.equals(registration))) {
        spaceLifeCycle.spaceRegistrationEdited(newSpace, newSpace.getEditor());
      }
    }
  }

  /**
   * Gets OrganizationService
   *
   * @return organizationService
   */
  private OrganizationService getOrgService() {
    if (organizationService == null) {
      organizationService = ExoContainerContext.getService(OrganizationService.class);
    }
    return organizationService;
  }

  private Set<String> getUsersToInvite(List<Identity> identities) {
    return identities.stream()
                     .filter(identity -> identity != null
                                         && StringUtils.isNotBlank(identity.getRemoteId())
                                         && StringUtils.isNotBlank(identity.getProviderId()))
                     .map(identity -> identityManager.getOrCreateIdentity(identity.getProviderId(), identity.getRemoteId()))
                     .filter(Objects::nonNull)
                     .flatMap(identity -> {
                       if (identity.isSpace()) {
                         Space space = getSpaceByPrettyName(identity.getRemoteId());
                         if (space != null) {
                           return Arrays.stream(space.getMembers());
                         } else {
                           return Stream.empty();
                         }
                       } else if (identity.isUser()) { // Otherwise, it's an
                                                       // user
                         return Stream.of(identity.getRemoteId());
                       } else {
                         return Stream.empty();
                       }
                     })
                     .collect(Collectors.toSet());

  }

  @SneakyThrows
  private boolean isMandatory(GroupHandler groupHandler, Group group, List<String> mandatories) {
    if (mandatories.contains(group.getId()))
      return true;
    Collection<Group> children = groupHandler.findGroups(group);
    for (Group g : children) {
      if (isMandatory(groupHandler, g, mandatories))
        return true;
    }
    return false;
  }

  private Space addPending(Space space, String username) {
    String[] pendingUsers = space.getPendingUsers();
    if (!ArrayUtils.contains(pendingUsers, username)) {
      pendingUsers = ArrayUtils.add(pendingUsers, username);
      space.setPendingUsers(pendingUsers);
    }
    return space;
  }

  private Space removePending(Space space, String username) {
    String[] pendingUsers = space.getPendingUsers();
    if (ArrayUtils.contains(pendingUsers, username)) {
      pendingUsers = ArrayUtils.removeAllOccurrences(pendingUsers, username);
      space.setPendingUsers(pendingUsers);
    }
    return space;
  }

  private Space addInvited(Space space, String username) {
    String[] invitedUsers = space.getInvitedUsers();
    if (!ArrayUtils.contains(invitedUsers, username)) {
      invitedUsers = ArrayUtils.add(invitedUsers, username);
      space.setInvitedUsers(invitedUsers);
    }
    return space;
  }

  private Space removeInvited(Space space, String username) {
    String[] invitedUsers = space.getInvitedUsers();
    if (ArrayUtils.contains(invitedUsers, username)) {
      invitedUsers = ArrayUtils.removeAllOccurrences(invitedUsers, username);
      space.setInvitedUsers(invitedUsers);
    }
    return space;
  }

  private void saveSpacePublicSite(Space space, String publicSiteVisibility, String authenticatedUser) {
    boolean visibilityChanged = StringUtils.isNotBlank(publicSiteVisibility)
                                && !StringUtils.equals(space.getPublicSiteVisibility(), publicSiteVisibility);

    if (space.getPublicSiteId() == 0
        || layoutService.getPortalConfig(space.getPublicSiteId()) == null) {
      long siteId = spaceTemplateLayoutService.createSpacePublicSite(space,
                                                                     space.getPrettyName(),
                                                                     space.getDisplayName(),
                                                                     getPublicSitePermissions(publicSiteVisibility,
                                                                                              space.getGroupId()));
      space.setPublicSiteId(siteId);
      space.setPublicSiteVisibility(publicSiteVisibility);
      spaceStorage.saveSpace(space, false);
      spaceLifeCycle.spacePublicSiteCreated(space, authenticatedUser);
    } else {
      PortalConfig portalConfig = layoutService.getPortalConfig(space.getPublicSiteId());
      if (visibilityChanged) {
        String[] publicSitePermissions = getPublicSitePermissions(publicSiteVisibility, space.getGroupId());
        portalConfig.setAccessPermissions(publicSitePermissions);
        portalConfig.setDefaultSite(false);
        layoutService.save(portalConfig);

        space.setPublicSiteVisibility(publicSiteVisibility);
        spaceStorage.saveSpace(space, false);
        spaceLifeCycle.spacePublicSiteUpdated(space, authenticatedUser);
      }
    }
  }

  private String[] getPublicSitePermissions(String publicSiteVisibility, String spaceGroupId) {
    if (StringUtils.isBlank(publicSiteVisibility)) {
      return null; // NOSONAR
    }
    switch (publicSiteVisibility) {
    case SpaceUtils.MANAGER: {
      return new String[] { SpaceUtils.MANAGER + ":" + spaceGroupId };
    }
    case SpaceUtils.MEMBER: {
      return new String[] { SpaceUtils.MEMBER + ":" + spaceGroupId };
    }
    case SpaceUtils.INTERNAL: {
      return new String[] { SpaceUtils.MEMBER + ":" + SpaceUtils.PLATFORM_USERS_GROUP };
    }
    case SpaceUtils.AUTHENTICATED: {
      return new String[] { SpaceUtils.MEMBER + ":" + SpaceUtils.PLATFORM_USERS_GROUP,
                            SpaceUtils.MEMBER + ":" + SpaceUtils.PLATFORM_EXTERNALS_GROUP };
    }
    case SpaceUtils.EVERYONE: {
      return new String[] { UserACL.EVERYONE };
    }
    default:
      throw new IllegalArgumentException("Unexpected value: " + publicSiteVisibility);
    }
  }

}
