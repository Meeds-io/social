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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
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
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceApplicationHandler;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent.Type;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

import lombok.SneakyThrows;

/**
 * {@link org.exoplatform.social.core.space.spi.SpaceService} implementation.
 * 
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since August 29, 2008
 */
public class SpaceServiceImpl implements SpaceService {

  private static final Log            LOG                  = ExoLogger.getLogger(SpaceServiceImpl.class.getName());

  public static final String          MEMBER               = "member";

  public static final String          MANAGER              = "manager";

  public static final String          DEFAULT_APP_CATEGORY = "spacesApplications";

  private SpaceStorage                spaceStorage;

  private IdentityManager             identityManager;

  private OrganizationService         organizationService  = null;

  private UserACL                     userACL              = null;

  private IdentityRegistry            identityRegistry;

  private Authenticator               authenticator;

  private SpaceLifecycle              spaceLifeCycle       = new SpaceLifecycle();

  /** The offset for list access loading. */
  private static final int            OFFSET               = 0;

  /** The limit for list access loading. */
  private static final int            LIMIT                = 200;

  private SpacesAdministrationService spacesAdministrationService;

  private SpaceTemplateService        spaceTemplateService;

  public SpaceServiceImpl(SpaceStorage spaceStorage,
                          IdentityManager identityManager,
                          UserACL userACL,
                          IdentityRegistry identityRegistry,
                          Authenticator authenticator,
                          SpacesAdministrationService spacesAdministrationService,
                          SpaceTemplateService spaceTemplateService) {
    this.spaceStorage = spaceStorage;
    this.identityManager = identityManager;
    this.identityRegistry = identityRegistry;
    this.userACL = userACL;
    this.authenticator = authenticator;
    this.spacesAdministrationService = spacesAdministrationService;
    this.spaceTemplateService = spaceTemplateService;
  }

  @Override
  public ListAccess<Space> getAllSpacesWithListAccess() {
    return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.ALL);
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
    return new SpaceListAccess(this.spaceStorage, username, SpaceListAccess.Type.ACCESSIBLE);
  }

  @Override
  public List<Space> getVisibleSpaces(String username, SpaceFilter spaceFilter) throws SpaceException {
    try {
      return Arrays.asList(this.getVisibleSpacesWithListAccess(username, spaceFilter).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  @Override
  public SpaceListAccess getVisibleSpacesWithListAccess(String username, SpaceFilter spaceFilter) {
    if (isSuperManager(username)) {
      if (spaceFilter == null)
        return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.ALL);
      else
        return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
    } else {
      return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.VISIBLE);
    }
  }

  @Override
  public SpaceListAccess getUnifiedSearchSpacesWithListAccess(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.UNIFIED_SEARCH);
  }

  @Override
  public SpaceListAccess getPublicSpacesWithListAccess(String username) {
    if (isSuperManager(username)) {
      return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.PUBLIC_SUPER_USER);
    } else {
      return new SpaceListAccess(this.spaceStorage, username, SpaceListAccess.Type.PUBLIC);
    }
  }

  @Override
  public SpaceListAccess getPendingSpacesWithListAccess(String username) {
    return new SpaceListAccess(this.spaceStorage, username, SpaceListAccess.Type.PENDING);
  }

  @Override
  @SneakyThrows
  public Space createSpace(Space space, String creator) {
    return createSpace(space, creator, null);
  }

  @Override
  public Space createSpace(Space space, String username, List<Identity> identitiesToInvite) throws SpaceException {
    if (!spacesAdministrationService.canCreateSpace(username)) {
      throw new SpaceException(Code.SPACE_PERMISSION);
    } else if (space.getDisplayName().length() > LIMIT) {
      throw new IllegalArgumentException("Error while creating the space " + space.getDisplayName() +
          ": space name cannot exceed 200 characters");
    }

    // Check the space template before creating the space and/or group
    SpaceTemplate spaceTemplate = getSpaceTemplateOrDefault(space.getTemplate());
    if (spaceTemplate == null) {
      throw new SpaceException(Code.UNKNOWN_SPACE_TEMPLATE);
    }

    // Add creator as a manager and a member to this space
    String[] managers = space.getManagers();
    String[] members = space.getMembers();
    managers = ArrayUtils.add(managers, username);
    members = ArrayUtils.add(members, username);
    space.setManagers(managers);
    space.setMembers(members);

    // Creates new space by creating new group
    String groupId = SpaceUtils.createGroup(space.getDisplayName(), space.getPrettyName(), username);
    String prettyName = groupId.split("/")[2];
    if (!prettyName.equals(space.getPrettyName())) {
      // work around for SOC-2366
      space.setPrettyName(groupId.split("/")[2]);
    }

    space.setGroupId(groupId);
    space.setUrl(space.getPrettyName());
    space.setTemplate(spaceTemplate.getName());

    spaceLifeCycle.setCurrentEvent(Type.SPACE_CREATED);
    try {
      spaceTemplateService.initSpaceApplications(space, getSpaceApplicationHandler(space));
      saveSpace(space, true);
      spaceLifeCycle.spaceCreated(space, username);
    } catch (Exception e) {
      throw new SpaceException(Code.UNABLE_TO_INIT_APP, "Failed to init apps for space " + space.getPrettyName(), e);
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

      if (!isInvited(space, username)) {
        inviteMember(space, username);
      }
    }
  }

  @Override
  public boolean isSpaceContainsExternals(Long spaceId) {
    return spaceStorage.countExternalMembers(spaceId) != 0;
  }

  @Override
  public void saveSpace(Space space, boolean isNew) {
    Space oldSpace = getSpaceById(space.getId());
    spaceStorage.saveSpace(space, isNew);
    if (!isNew) {
      triggerSpaceUpdate(space, oldSpace);
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
  public void deleteSpace(Space space) {
    deleteSpace(space, true);
  }

  @Override
  public void deleteSpace(Space space, boolean deleteGroup) {
    spaceLifeCycle.setCurrentEvent(Type.SPACE_REMOVED);
    try {
      try {
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
          UserACL acl = getUserACL();
          GroupHandler groupHandler = getOrgService().getGroupHandler();
          Group deletedGroup = groupHandler.findGroupById(space.getGroupId());
          List<String> mandatories = acl.getMandatoryGroups();
          if (deletedGroup != null
              && !isMandatory(groupHandler, deletedGroup, mandatories)) {
            SpaceUtils.removeGroup(space);
          }

          // remove pages and group navigation of space
          SpaceUtils.removePagesAndGroupNavigation(space);
        }
      } catch (Exception e) {
        LOG.error("Unable delete space: {}. Cause: {}", space.getPrettyName(), e.getMessage());
      }
      spaceLifeCycle.spaceRemoved(space, space.getEditor());
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.SPACE_REMOVED);
    }
  }

  @Override
  public void deleteSpace(String spaceId) {
    deleteSpace(getSpaceById(spaceId));
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
  public void addMember(String spaceId, String username) {
    addMember(getSpaceById(spaceId), username);
  }

  @Override
  public void removeMember(Space space, String username) {
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
  public void removeMember(String spaceId, String username) {
    removeMember(getSpaceById(spaceId), username);
  }

  @Override
  public List<String> getMembers(Space space) {
    if (space.getMembers() != null) {
      return Arrays.asList(space.getMembers());
    }
    return Collections.emptyList();
  }

  @Override
  public List<String> getMembers(String spaceId) {
    return getMembers(getSpaceById(spaceId));
  }

  @Override
  public boolean isMember(Space space, String username) {
    return space != null && ArrayUtils.contains(space.getMembers(), username);
  }

  @Override
  public boolean isMember(String spaceId, String username) {
    return isMember(getSpaceById(spaceId), username);
  }

  @Override
  public boolean hasAccessPermission(Space space, String username) {
    return canViewSpace(space, username);
  }

  @Override
  public boolean hasAccessPermission(String spaceId, String username) {
    return hasAccessPermission(getSpaceById(spaceId), username);
  }

  @Override
  public boolean hasEditPermission(Space space, String username) {
    return this.hasSettingPermission(space, username);
  }

  @Override
  public boolean hasEditPermission(String spaceId, String username) {
    return this.hasSettingPermission(this.getSpaceById(spaceId), username);
  }

  @Override
  public boolean isInvited(Space space, String username) {
    return this.isInvitedUser(space, username);
  }

  @Override
  public boolean isInvited(String spaceId, String username) {
    return this.isInvitedUser(this.getSpaceById(spaceId), username);
  }

  @Override
  public boolean isPending(Space space, String username) {
    return this.isPendingUser(space, username);
  }

  @Override
  public boolean isPending(String spaceId, String username) {
    return this.isPendingUser(this.getSpaceById(spaceId), username);
  }

  @Override
  public boolean isIgnored(Space space, String username) {
    return spaceStorage.isSpaceIgnored(space.getId(), username);
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
  public void restoreSpacePageLayout(String spaceId,
                                     String appId,
                                     org.exoplatform.services.security.Identity identity) throws IllegalAccessException,
                                                                                          SpaceException {
    if (identity == null || !isSuperManager(identity.getUserId())) {
      throw new IllegalAccessException("User is not allowed to change page layout of spaces");
    }
    Space space = getSpaceById(spaceId);
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    try {
      appHandler.restoreApplicationLayout(space, appId);
    } catch (SpaceException e) {
      throw e;
    } catch (Exception e) {
      throw new SpaceException(Code.UNABLE_TO_RESTORE_APPLICATION_LAYOUT, e);
    }
  }

  @Override
  public void moveApplication(String spaceId, String appId, int transition) throws SpaceException {
    Space space = getSpaceById(spaceId);
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    try {
      appHandler.moveApplication(space, appId, transition);
    } catch (Exception e) {
      throw new SpaceException(Code.UNABLE_TO_MOVE_APPLICATION, e);
    }
  }

  @Override
  public void installApplication(Space space, String appId) throws SpaceException {
    spaceLifeCycle.setCurrentEvent(Type.APP_ADDED);
    try {
      spaceLifeCycle.addApplication(space, getPortletId(appId));
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.APP_ADDED);
    }
  }

  @Override
  public void activateApplication(Space space, String appId) throws SpaceException {
    spaceLifeCycle.setCurrentEvent(Type.APP_ACTIVATED);
    try {
      String appName = null;
      if (SpaceUtils.isInstalledApp(space, appId)) {
        appName = appId + System.currentTimeMillis();
      } else {
        appName = appId;
      }
      SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
      appHandler.activateApplication(space, appId, appName);
      // Default is removable, or must be added by configuration or support
      // setting for applications.
      spaceTemplateService.setApp(space, appId, appName, true, Space.ACTIVE_STATUS);
      saveSpace(space, false);
      // Use portletId instead of appId for fixing SOC-1633.
      spaceLifeCycle.activateApplication(space, getPortletId(appId));
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.APP_ACTIVATED);
    }
  }

  @Override
  public void activateApplication(String spaceId, String appId) throws SpaceException {
    activateApplication(getSpaceById(spaceId), appId);
  }

  @Override
  public void deactivateApplication(Space space, String appId) throws SpaceException {
    String appStatus = SpaceUtils.getAppStatus(space, appId);
    if (appStatus == null) {
      LOG.warn("appStatus is null!");
      return;
    }
    if (appStatus.equals(Space.DEACTIVE_STATUS))
      return;

    spaceLifeCycle.setCurrentEvent(Type.APP_DEACTIVATED);
    try {
      SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
      appHandler.deactiveApplication(space, appId);
      spaceTemplateService.setApp(space, appId, appId, SpaceUtils.isRemovableApp(space, appId), Space.DEACTIVE_STATUS);
      saveSpace(space, false);
      spaceLifeCycle.deactivateApplication(space, getPortletId(appId));
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.APP_DEACTIVATED);
    }
  }

  @Override
  public void deactivateApplication(String spaceId, String appId) throws SpaceException {
    deactivateApplication(getSpaceById(spaceId), appId);
  }

  @Override
  public void removeApplication(Space space, String appId, String appName) throws SpaceException {
    String appStatus = SpaceUtils.getAppStatus(space, appId);
    if (appStatus == null)
      return;
    spaceLifeCycle.setCurrentEvent(Type.APP_REMOVED);
    try {
      SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
      appHandler.removeApplication(space, appId, appName);
      removeApp(space, appName);
      spaceLifeCycle.removeApplication(space, getPortletId(appId));
    } finally {
      spaceLifeCycle.resetCurrentEvent(Type.APP_REMOVED);
    }
  }

  @Override
  public void removeApplication(String spaceId, String appId, String appName) throws SpaceException {
    removeApplication(getSpaceById(spaceId), appId, appName);
  }

  @Override
  public void requestJoin(String spaceId, String username) {
    this.addPendingUser(this.getSpaceById(spaceId), username);
  }

  @Override
  public void requestJoin(Space space, String username) {
    this.addPendingUser(space, username);
  }

  @Override
  public void revokeRequestJoin(Space space, String username) {
    this.removePendingUser(space, username);
  }

  @Override
  public void revokeRequestJoin(String spaceId, String username) {
    Space space = this.getSpaceById(spaceId);
    this.removePending(space, username);
    spaceLifeCycle.removePendingUser(space, username);
  }

  @Override
  public void inviteMember(Space space, String username) {
    this.addInvitedUser(space, username);
  }

  @Override
  public void inviteMember(String spaceId, String username) {
    this.addInvitedUser(this.getSpaceById(spaceId), username);
  }

  @Override
  public void revokeInvitation(Space space, String username) {
    this.removeInvitedUser(space, username);
  }

  @Override
  public void revokeInvitation(String spaceId, String username) {
    this.removeInvitedUser(this.getSpaceById(spaceId), username);
  }

  @Override
  public void acceptInvitation(Space space, String username) throws SpaceException {
    this.addMember(space, username);
  }

  @Override
  public void acceptInvitation(String spaceId, String username) throws SpaceException {
    this.addMember(this.getSpaceById(spaceId), username);
  }

  @Override
  public void denyInvitation(String spaceId, String username) {
    this.removeInvitedUser(this.getSpaceById(spaceId), username);
  }

  @Override
  public void denyInvitation(Space space, String username) {
    this.removeInvitedUser(space, username);
  }

  @Override
  public void validateRequest(Space space, String username) {
    this.addMember(space, username);
  }

  @Override
  public void validateRequest(String spaceId, String username) {
    this.addMember(this.getSpaceById(spaceId), username);
  }

  @Override
  public void declineRequest(Space space, String username) {
    this.removePendingUser(space, username);
  }

  @Override
  public void declineRequest(String spaceId, String username) {
    this.removePendingUser(this.getSpaceById(spaceId), username);
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

    if (isPending(space, username)) {
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
      addPending(space, username);
      saveSpace(space, false);
    } else {
      LOG.warn("Unable request to join");
    }
    spaceLifeCycle.addPendingUser(space, username);
  }

  @Override
  public ListAccess<Space> getAccessibleSpacesByFilter(String username, SpaceFilter spaceFilter) {
    if (isSuperManager(username)
        && (spaceFilter == null || spaceFilter.getAppId() == null)) {
      if (spaceFilter == null) {
        return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL);
      } else {
        return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
      }
    } else {
      return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.ACCESSIBLE_FILTER);
    }
  }

  @Override
  public ListAccess<Space> getAllSpacesByFilter(SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
  }

  @Override
  public ListAccess<Space> getInvitedSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.INVITED_FILTER);
  }

  @Override
  public ListAccess<Space> getMemberSpaces(String username) {
    return new SpaceListAccess(this.spaceStorage, username, SpaceListAccess.Type.MEMBER);
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
    return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.MANAGER_FILTER);
  }

  @Override
  public ListAccess<Space> getManagerSpaces(String username) {
    return new SpaceListAccess(this.spaceStorage, username, SpaceListAccess.Type.MANAGER);
  }

  @Override
  public ListAccess<Space> getMemberSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.MEMBER_FILTER);
  }

  @Override
  public ListAccess<Space> getFavoriteSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.FAVORITE_FILTER);
  }

  @Override
  public ListAccess<Space> getPendingSpacesByFilter(String username, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.PENDING_FILTER);
  }

  @Override
  public ListAccess<Space> getPublicSpacesByFilter(String username, SpaceFilter spaceFilter) {
    if (isSuperManager(username)) {
      return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.PUBLIC_SUPER_USER);
    } else {
      return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.PUBLIC_FILTER);
    }
  }

  @Override
  public ListAccess<Space> getSettingableSpaces(String username) {
    if (isSuperManager(username)) {
      return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.ALL);
    } else {
      return new SpaceListAccess(this.spaceStorage, username, SpaceListAccess.Type.SETTING);
    }
  }

  @Override
  public ListAccess<Space> getSettingabledSpacesByFilter(String username, SpaceFilter spaceFilter) {
    if (isSuperManager(username)) {
      return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
    } else {
      return new SpaceListAccess(this.spaceStorage, username, spaceFilter, SpaceListAccess.Type.SETTING_FILTER);
    }
  }

  @Override
  public boolean hasSettingPermission(Space space, String username) {
    return canManageSpace(space, username);
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
    return new SpaceListAccess(this.spaceStorage, username, SpaceListAccess.Type.INVITED);
  }

  @Override
  public void updateSpaceAccessed(String remoteId, Space space) throws SpaceException {
    if (isMember(space, remoteId)) {
      spaceStorage.updateSpaceAccessed(remoteId, space);
    }
  }

  @Override
  public List<Space> getLastAccessedSpace(String remoteId, String appId, int offset, int limit) throws SpaceException {
    SpaceFilter filter = new SpaceFilter(remoteId, appId);
    return spaceStorage.getLastAccessedSpace(filter, offset, limit);
  }

  @Override
  public List<Space> getLastSpaces(int limit) {
    return spaceStorage.getLastSpaces(limit);
  }

  @Override
  public ListAccess<Space> getLastAccessedSpace(String remoteId, String appId) {
    return new SpaceListAccess(this.spaceStorage, remoteId, appId, SpaceListAccess.Type.LASTEST_ACCESSED);
  }

  @Override
  public ListAccess<Space> getVisitedSpaces(String remoteId, String appId) {
    return new SpaceListAccess(this.spaceStorage, remoteId, appId, SpaceListAccess.Type.VISITED);
  }

  @Override
  public ListAccess<Space> getPendingSpaceRequestsToManage(String remoteId) {
    return new SpaceListAccess(this.spaceStorage, remoteId, SpaceListAccess.Type.PENDING_REQUESTS);
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
    if (StringUtils.isBlank(username)
        || IdentityConstants.ANONIM.equals(username)
        || IdentityConstants.SYSTEM.equals(username)) {
      return false;
    } else if (username.equals(getUserACL().getSuperUser())) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = getIdentity(username);
    return identity != null && (identity.isMemberOf(userACL.getAdminGroups())
                                || spacesAdministrationService.getSpacesAdministratorsMemberships()
                                                              .stream()
                                                              .anyMatch(identity::isMemberOf));
  }

  @Override
  public boolean isContentManager(String username) {
    if (StringUtils.isBlank(username) || IdentityConstants.ANONIM.equals(username) || IdentityConstants.SYSTEM.equals(username)) {
      return false;
    } else if (username.equals(getUserACL().getSuperUser())) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = getIdentity(username);
    return identity != null && identity.isMemberOf(SpaceUtils.PLATFORM_PUBLISHER_GROUP, SpaceUtils.MANAGER);
  }

  @Override
  public boolean isContentPublisher(String username) {
    if (StringUtils.isBlank(username)
        || IdentityConstants.ANONIM.equals(username)
        || IdentityConstants.SYSTEM.equals(username)) {
      return false;
    } else if (username.equals(getUserACL().getSuperUser())) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = getIdentity(username);
    return identity != null && identity.isMemberOf(SpaceUtils.PLATFORM_PUBLISHER_GROUP, SpaceUtils.PUBLISHER);
  }

  @Override
  public ListAccess<Space> getCommonSpaces(String username, String otherUserId) {
    return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.COMMON, username, otherUserId);
  }

  public void addSpaceListener(SpaceListenerPlugin plugin) {
    registerSpaceLifeCycleListener(plugin);
  }

  private String checkSpaceEditorPermissions(Space space) {
    String editor = space.getEditor();
    if (StringUtils.isBlank(editor) || !hasEditPermission(space, editor)) {
      throw new IllegalStateException("User " + editor + " is not authorized to change space.");
    }
    return editor;
  }

  private String getPortletId(String appId) {
    final char SEPARATOR = '.';

    if (appId.indexOf(SEPARATOR) != -1) {
      int beginIndex = appId.lastIndexOf(SEPARATOR) + 1;
      int endIndex = appId.length();

      return appId.substring(beginIndex, endIndex);
    }

    return appId;
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

  /**
   * Gets UserACL
   *
   * @return userACL
   */
  private UserACL getUserACL() {
    return userACL;
  }

  /**
   * Gets space application handler
   *
   * @param space
   * @return
   * @throws SpaceException
   */
  private SpaceApplicationHandler getSpaceApplicationHandler(Space space) throws SpaceException {
    String spaceTemplate = space.getTemplate();
    SpaceApplicationHandler appHandler = spaceTemplateService.getSpaceApplicationHandlers().get(spaceTemplate);
    if (appHandler == null) {
      LOG.debug("No space application handler was defined for template with name {}. Default will be used.", spaceTemplate);
      String defaultTemplate = spaceTemplateService.getDefaultSpaceTemplate();
      appHandler = spaceTemplateService.getSpaceApplicationHandlers().get(defaultTemplate);
      if (appHandler == null) {
        throw new SpaceException(SpaceException.Code.UNKNOWN_SPACE_TEMPLATE);
      }
    }
    return appHandler;
  }

  private void removeApp(Space space, String appName) {
    String apps = space.getApp();
    StringBuilder remainApp = new StringBuilder();
    String[] listApp = apps.split(",");
    String[] appPart;
    String app;
    for (int idx = 0; idx < listApp.length; idx++) {
      app = listApp[idx];
      appPart = app.split(":");
      if (!appPart[1].equals(appName)) {
        if (remainApp.length() != 0)
          remainApp.append(",");
        remainApp.append(app);
      }
    }

    space.setApp(remainApp.toString());
    saveSpace(space, false);
  }

  private Set<String> getUsersToInvite(List<Identity> identities) {
    Set<String> invitedUserIds = new HashSet<>();
    for (Identity identity : identities) {
      if (identity == null
          || StringUtils.isBlank(identity.getRemoteId())
          || StringUtils.isBlank(identity.getProviderId())) {
        continue;
      }
      identity = identityManager.getOrCreateIdentity(identity.getProviderId(), identity.getRemoteId());
      if (identity == null || identity.isDeleted() || !identity.isEnable()) {
        continue;
      }
      String remoteId = identity.getRemoteId();
      // If it's a space
      if (identity.isSpace()) {
        Space space = getSpaceByPrettyName(remoteId);
        if (space != null) {
          String[] users = space.getMembers();
          invitedUserIds.addAll(Arrays.asList(users));
        }
      } else if (identity.isUser()) { // Otherwise, it's an user
        invitedUserIds.add(remoteId);
      }
    }
    return invitedUserIds;
  }

  private boolean isMandatory(GroupHandler groupHandler, Group group, List<String> mandatories) throws Exception {
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

  private org.exoplatform.services.security.Identity getIdentity(String username) {
    org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(username);
    if (identity == null) {
      try {
        identity = authenticator.createIdentity(username);
        identityRegistry.register(identity);
        return identity;
      } catch (Exception e) {
        LOG.warn("Error while retrieving user {} ACL identity", username, e);
        return null;
      }
    } else {
      return identity;
    }
  }

  private SpaceTemplate getSpaceTemplateOrDefault(String templaceName) {
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(templaceName);
    if (spaceTemplate == null) {
      LOG.warn("could not find space template of type {}, will use Default template", templaceName);
      String defaultTemplate = spaceTemplateService.getDefaultSpaceTemplate();
      spaceTemplate = spaceTemplateService.getSpaceTemplateByName(defaultTemplate);
    }
    return spaceTemplate;
  }

}
