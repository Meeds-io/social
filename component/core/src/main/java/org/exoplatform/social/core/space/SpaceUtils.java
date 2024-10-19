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
package org.exoplatform.social.core.space;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.cache.CachedIdentityStorage;
import org.exoplatform.social.core.storage.cache.CachedSpaceStorage;

/**
 * SpaceUtils Utility for working with space
 */
public class SpaceUtils {

  private static final Log    LOG                      = ExoLogger.getLogger(SpaceUtils.class);

  public static final String  SPACE_GROUP              = "/spaces";

  public static final String  PLATFORM_USERS_GROUP     = "/platform/users";

  public static final String  PLATFORM_EXTERNALS_GROUP = "/platform/externals";

  public static final String  PLATFORM_PUBLISHER_GROUP = "/platform/web-contributors";

  public static final String  MANAGER                  = "manager";

  public static final String  MEMBER                   = "member";

  public static final String  INTERNAL                 = "internal";

  public static final String  AUTHENTICATED            = "authenticated";

  public static final String  EVERYONE                 = "everyone";

  public static final String  REDACTOR                 = "redactor";

  public static final String  PUBLISHER                = "publisher";

  public static final String  PENDING                  = "pending";

  public static final String  INVITED                  = "invited";

  public static final String  IGNORED                  = "ignored";

  public static final String  MENU_CONTAINER           = "Menu";

  public static final String  APPLICATION_CONTAINER    = "Application";

  public static final String  SPACE_URL                = "SPACE_URL";

  public static final String  SPACE_SETTINGS_PAGE      = "settings";

  public static final String  PUBLIC_SITE_SPACE_ID     = "SPACE_ID";

  public static final String  IS_PUBLIC_SITE_SPACE     = "IS_SPACE_PUBLIC_SITE";

  public static final String  CURRENT_SPACE            = "CurrentSpace";

  private static final String NUMBER_REG_PATTERN       = "\\d";

  private static final String UNDER_SCORE_STR          = "_";

  private static final String SPACE_STR                = " ";

  /**
   * Utility for cleaning space name
   *
   * @param str
   * @return cleaned string
   */
  public static String cleanString(String str) {
    return org.exoplatform.social.common.Utils.cleanString(str);
  }

  /**
   * Check whether is being in a space context or not.
   * 
   * @return
   * @since 4.0.0-RC2
   */
  public static boolean isSpaceContext() {
    return (getSpaceByContext() != null);
  }

  /**
   * Gets the space url based on the current context.
   * 
   * @return
   * @since 4.0.0-RC2
   */
  public static String getSpaceUrlByContext() {
    Space space = getSpaceByContext();
    return (space != null ? space.getUrl() : null);
  }

  public static Space getSpaceByContext() {
    //
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    Object currentSpaceObject = pcontext.getAttribute(CURRENT_SPACE);
    if (currentSpaceObject != null) {
      if (Objects.equals(currentSpaceObject, StringUtils.EMPTY)) {
        return null;
      } else {
        return (Space) currentSpaceObject;
      }
    }
    if (!pcontext.getSiteType().equals(SiteType.GROUP) ||
        !pcontext.getSiteName().startsWith(SpaceUtils.SPACE_GROUP)) {
      setSpaceByContext(pcontext, StringUtils.EMPTY);
      return null;
    }

    //
    Space currentSpace = getSpaceService().getSpaceByGroupId(pcontext.getSiteName());
    setSpaceByContext(pcontext, currentSpace);
    return currentSpace;
  }

  public static void setSpaceByContext(PortalRequestContext context, Object space) {
    context.setAttribute(CURRENT_SPACE, space);
  }

  public static Identity getSpaceIdentityByContext() {
    Space space = getSpaceByContext();
    if (space != null) {
      IdentityManager identityManager = ExoContainerContext.getService(IdentityManager.class);
      return identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    }
    return null;
  }

  /**
   * Creates new group in /Spaces node and return groupId
   *
   * @param spaceName String
   * @param creator String
   * @return groupId String
   * @throws SpaceException
   */
  public static String createGroup(String spaceName, String creator) throws SpaceException {
    return createGroup(spaceName, spaceName, creator);
  }

  /**
   * Creates new group in /Spaces node and return groupId
   * 
   * @param groupLabel Space Display name.
   * @param spaceName Space name.
   * @param creator Name of user who creating space.
   * @return groupId Id of created space group.
   * @throws SpaceException
   */
  public static String createGroup(String groupLabel, String spaceName, String creator) throws SpaceException {
    OrganizationService organizationService = getOrganizationService();
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group parentGroup;
    Group newGroup;
    String groupId;
    String shortName;
    try {
      parentGroup = groupHandler.findGroupById(SPACE_GROUP);
      // Creates new group
      newGroup = groupHandler.createGroupInstance();
      shortName = Utils.cleanString(spaceName);
      groupId = parentGroup.getId() + "/" + shortName;

      if (getSpaceService().getSpaceByGroupId(groupId) != null) {
        shortName = buildGroupId(shortName, parentGroup.getId());
        groupId = parentGroup.getId() + "/" + shortName;
      }

      if (isSpaceNameExisted(spaceName)) {
        throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
      }
      newGroup.setGroupName(shortName);
      newGroup.setLabel(groupLabel);
      newGroup.setDescription("the " + parentGroup.getId() + "/" + shortName + " group");
      groupHandler.addChild(parentGroup, newGroup, true);
    } catch (Exception e) {
      if (e instanceof SpaceException spaceException) {
        throw spaceException;
      } else {
        throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_GROUP, e);
      }
    }

    try {
      // Adds user as creator (member, manager)
      addCreatorToGroup(creator, groupId);
      return groupId;
    } catch (Exception e) {
      try {
        groupHandler.removeGroup(newGroup, true);
      } catch (Exception ex) {
        LOG.warn("Unable to rollback group creation after a failure in space group creation. Throw original exception to stop space creation only.",
                 ex);
      }
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_CREATOR, e);
    }
  }

  /**
   * Removes a group owning a space
   *
   * @param space
   * @throws SpaceException
   */
  public static void removeGroup(Space space) throws SpaceException {
    try {
      OrganizationService organizationService = getOrganizationService();
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group group = groupHandler.findGroupById(space.getGroupId());
      groupHandler.removeGroup(group, true);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_GROUP, e);
    }
  }

  /**
   * Removes membership of users with a deleted spaces.
   * 
   * @param space
   */
  public static void removeMembershipFromGroup(Space space) {
    if (space == null)
      return;

    // remove users from group with role is member
    if (space.getMembers() != null) {
      for (String userId : space.getMembers()) {
        removeUserFromGroupWithMemberMembership(userId, space.getGroupId());
      }
    }

    // remove users from group with role is manager
    if (space.getManagers() != null) {
      for (String userId : space.getManagers()) {
        removeUserFromGroupWithManagerMembership(userId, space.getGroupId());
      }
    }

    // remove users from group with role is redactor
    if (space.getRedactors() != null) {
      for (String userId : space.getRedactors()) {
        removeUserFromGroupWithRedactorMembership(userId, space.getGroupId());
      }
    }

    // remove users from group which role is publisher
    if (space.getPublishers() != null) {
      for (String userId : space.getPublishers()) {
        removeUserFromGroupWithPublisherMembership(userId, space.getGroupId());
      }
    }
  }

  /**
   * Checks if a space exists
   *
   * @param spaceName
   * @return boolean if existed return true, else return false
   */
  public static boolean isSpaceNameExisted(String spaceName) {
    return getSpaceService().getSpaceByPrettyName(Utils.cleanString(spaceName)) != null;
  }

  /**
   * When user chooses an existing group, that user will be added to that group
   * as a manager
   *
   * @param creator String
   * @param groupId String
   */
  public static void addCreatorToGroup(String creator, String groupId) {
    addUserToGroupWithMemberMembership(creator, groupId);
    addUserToGroupWithManagerMembership(creator, groupId);
  }

  /**
   * Adds the user to group with the membership (member, manager).
   * 
   * @param remoteId
   * @param groupId
   * @param membership
   * @since 1.2.0-GA
   */
  private static void addUserToGroupWithMembership(String remoteId, String groupId, String membership) {
    OrganizationService organizationService = getOrganizationService();
    try {
      MembershipHandler membershipHandler = organizationService.getMembershipHandler();
      Membership found = membershipHandler.findMembershipByUserGroupAndType(remoteId, groupId, membership);
      if (found == null && !MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.equalsIgnoreCase(membership)) {
        found = membershipHandler.findMembershipByUserGroupAndType(remoteId, groupId, MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
      }
      if (found != null) {
        LOG.debug("user: {} was already added to group: {} with membership * or : {}", remoteId, groupId, membership);
        return;
      }
      User user = organizationService.getUserHandler().findUserByName(remoteId);
      MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(membership);
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group existingGroup = groupHandler.findGroupById(groupId);
      membershipHandler.linkMembership(user, existingGroup, membershipType, true);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to add user: " + remoteId + " to group: " + groupId + " with membership: " +
          membership, e);
    } finally {
      clearIdentityCaching(OrganizationIdentityProvider.NAME, remoteId);
      if (groupId.startsWith(SpaceUtils.SPACE_GROUP)) {
        Space space = getSpaceService().getSpaceByGroupId(groupId);
        if (space != null) {
          clearIdentityCaching(SpaceIdentityProvider.NAME, space.getPrettyName());
          clearSpaceCache(space.getId());
        }
      }
    }
  }

  /**
   * Adds the user to group with the membership (member).
   * 
   * @param remoteId
   * @param groupId
   * @since 1.2.0-GA
   */
  public static void addUserToGroupWithMemberMembership(String remoteId, String groupId) {
    addUserToGroupWithMembership(remoteId, groupId, MEMBER);
  }

  /**
   * Adds the user to group with the membership (redactor).
   * 
   * @param remoteId
   * @param groupId
   */
  public static void addUserToGroupWithRedactorMembership(String remoteId, String groupId) {
    addUserToGroupWithMembership(remoteId, groupId, REDACTOR);
  }

  /**
   * Adds the user to group with the membership (publisher).
   * 
   * @param remoteId
   * @param groupId
   */
  public static void addUserToGroupWithPublisherMembership(String remoteId, String groupId) {
    addUserToGroupWithMembership(remoteId, groupId, PUBLISHER);
  }

  /**
   * Adds the user to group with the membership (manager).
   * 
   * @param remoteId
   * @param groupId
   * @since 1.2.0-GA
   */
  public static void addUserToGroupWithManagerMembership(String remoteId, String groupId) {
    addUserToGroupWithMembership(remoteId, groupId, MANAGER);
  }

  /**
   * Removes the user from group with the membership (member, manager, redactor,
   * publisher).
   * 
   * @param remoteId
   * @param groupId
   * @param membership
   * @since 1.2.0-GA
   */
  private static void removeUserFromGroupWithMembership(String remoteId, String groupId, String membership) {
    try {
      OrganizationService organizationService = getOrganizationService();
      MembershipHandler memberShipHandler = organizationService.getMembershipHandler();
      if (MEMBER.equals(membership)) {
        Collection<Membership> memberships = memberShipHandler.findMembershipsByUserAndGroup(remoteId, groupId);
        if (CollectionUtils.isEmpty(memberships)) {
          LOG.debug("User: " + remoteId + " is not a member of group: " + groupId);
          return;
        }
        Iterator<Membership> itr = memberships.iterator();
        while (itr.hasNext()) {
          Membership mbShip = itr.next();
          memberShipHandler.removeMembership(mbShip.getId(), true);
        }
      } else {
        Membership memberShip = memberShipHandler.findMembershipByUserGroupAndType(remoteId,
                                                                                   groupId,
                                                                                   membership);
        Membership any = memberShipHandler.findMembershipByUserGroupAndType(remoteId,
                                                                            groupId,
                                                                            MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
        if (any != null) {
          memberShipHandler.removeMembership(any.getId(), true);
        }
        if (memberShip == null) {
          LOG.debug("User: " + remoteId + " is not a " + membership + " of group: " + groupId);
          return;
        }
        UserHandler userHandler = organizationService.getUserHandler();
        User user = userHandler.findUserByName(remoteId);
        memberShipHandler.removeMembership(memberShip.getId(), true);

        MembershipType mbShipTypeMember = organizationService.getMembershipTypeHandler().findMembershipType(MEMBER);
        GroupHandler groupHandler = organizationService.getGroupHandler();
        memberShipHandler.linkMembership(user, groupHandler.findGroupById(groupId), mbShipTypeMember, true);
      }
      clearIdentityCaching(OrganizationIdentityProvider.NAME, remoteId);
      if (groupId.startsWith(SpaceUtils.SPACE_GROUP)) {
        Space space = getSpaceService().getSpaceByGroupId(groupId);
        if (space != null) {
          clearIdentityCaching(SpaceIdentityProvider.NAME, space.getPrettyName());
          clearSpaceCache(space.getId());
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to remove user: " + remoteId + " to group: " + groupId + " with membership: " + membership, e);
    }
  }

  /**
   * Removes the user from group with member membership.
   * 
   * @param remoteId
   * @param groupId
   * @since 1.2.0-GA
   */
  public static void removeUserFromGroupWithMemberMembership(String remoteId, String groupId) {
    removeUserFromGroupWithMembership(remoteId, groupId, MEMBER);
  }

  /**
   * Removes the user from group with redactor membership.
   * 
   * @param remoteId
   * @param groupId
   */
  public static void removeUserFromGroupWithRedactorMembership(String remoteId, String groupId) {
    removeUserFromGroupWithMembership(remoteId, groupId, REDACTOR);
  }

  /**
   * Removes the user from group with publisher membership.
   * 
   * @param remoteId
   * @param groupId
   */
  public static void removeUserFromGroupWithPublisherMembership(String remoteId, String groupId) {
    removeUserFromGroupWithMembership(remoteId, groupId, PUBLISHER);
  }

  /**
   * Removes the user from group with manager membership.
   * 
   * @param remoteId
   * @param groupId
   * @since 1.2.0-GA
   */
  public static void removeUserFromGroupWithManagerMembership(String remoteId, String groupId) {
    removeUserFromGroupWithMembership(remoteId, groupId, MANAGER);
  }

  /**
   * Removes the user from group with any(*) membership.
   *
   * @param remoteId target user remote id
   * @param groupId group id
   */
  public static void removeUserFromGroupWithAnyMembership(String remoteId, String groupId) {
    removeUserFromGroupWithMembership(remoteId, groupId, MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
  }

  /**
   * Gets NavigationContext by a space's groupId
   *
   * @param groupId
   */
  public static NavigationContext getGroupNavigationContext(String groupId) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    NavigationService navService = (NavigationService) container.getComponentInstance(NavigationService.class);
    return navService.loadNavigation(SiteKey.group(groupId));
  }

  /**
   * Gets Organization Service
   *
   * @return
   */
  public static OrganizationService getOrganizationService() {
    PortalContainer portalContainer = PortalContainer.getInstance();
    return portalContainer.getComponentInstanceOfType(OrganizationService.class);
  }

  /**
   * Builds pretty name base on the basic name in case create more than one
   * space with the same name.
   * 
   * @param prettyName
   * @param parentGroupId
   * @return
   * @since 1.2.8
   */
  public static String buildGroupId(String prettyName, String parentGroupId) {
    String checkedGroupId = prettyName;
    String mainPatternGroupId = null;
    String numberPattern = NUMBER_REG_PATTERN;
    if (checkedGroupId.substring(checkedGroupId.lastIndexOf(UNDER_SCORE_STR) + 1).matches(numberPattern)) {
      mainPatternGroupId = checkedGroupId.substring(0, checkedGroupId.lastIndexOf(UNDER_SCORE_STR));
    } else {
      mainPatternGroupId = checkedGroupId;
    }

    boolean hasNext = true;
    int extendPattern = 0;

    while (hasNext) {
      ++extendPattern;
      checkedGroupId = Utils.cleanString(mainPatternGroupId + SPACE_STR + extendPattern);
      ExoContainer container = ExoContainerContext.getCurrentContainer();

      if (getSpaceService().getSpaceByGroupId(parentGroupId + "/" + checkedGroupId) != null) {
        continue;
      }

      IdentityManager idm = container.getComponentInstanceOfType(IdentityManager.class);
      Identity identity = idm.getOrCreateSpaceIdentity(checkedGroupId);
      if (identity == null) {
        hasNext = false;
      }
    }

    return checkedGroupId;
  }

  public static SpaceService getSpaceService() {
    return ExoContainerContext.getService(SpaceService.class);
  }

  private static void clearIdentityCaching(String providerId, String remoteId) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    CachedIdentityStorage cachedIdentityStorage = container.getComponentInstanceOfType(CachedIdentityStorage.class);
    // clear caching for identity
    cachedIdentityStorage.clearIdentityCache(providerId, remoteId, false);
  }

  private static void clearSpaceCache(String spaceId) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    CachedSpaceStorage cachedSpaceStorage = container.getComponentInstanceOfType(CachedSpaceStorage.class);
    // clear caching for space
    cachedSpaceStorage.clearSpaceCached(spaceId);
  }

}
