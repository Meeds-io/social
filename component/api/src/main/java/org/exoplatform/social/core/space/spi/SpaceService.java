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
package org.exoplatform.social.core.space.spi;

import java.time.Instant;
import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;

/**
 * Provides methods to work with Space.
 *
 * @since Aug 29, 2008
 */
public interface SpaceService {

  /**
   * Gets a space by its display name.
   *
   * @param spaceDisplayName The space display name.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space getSpaceByDisplayName(String spaceDisplayName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a space by its pretty name.
   *
   * @param spacePrettyName The space's pretty name.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space getSpaceByPrettyName(String spacePrettyName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a space by its group Id.
   *
   * @param groupId The group Id.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space getSpaceByGroupId(String groupId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a space by its Id.
   *
   * @param spaceId Id of the space.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space getSpaceById(String spaceId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a space by its URL.
   *
   * @param spaceUrl URL of the space.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space getSpaceByUrl(String spaceUrl) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access that contains all spaces.
   *
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getAllSpacesWithListAccess() {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access that contains all spaces matching with a filter.
   *
   * @param spaceFilter The space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getAllSpacesByFilter(SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the "manager"
   * role.
   *
   * @param username The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   */
  default ListAccess<Space> getManagerSpaces(String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the "manager"
   * role. This list access matches with the provided space filter.
   *
   * @param username The remote user Id.
   * @param spaceFilter The space filter.
   * @return The list access.
   * @LevelAPI Platform
   */
  default ListAccess<Space> getManagerSpacesByFilter(String username, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the "member" role.
   *
   * @param username The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getMemberSpaces(String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the "member" role.
   * This list access matches with the provided space filter.
   *
   * @param username The remote user Id.
   * @param spaceFilter The space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getMemberSpacesByFilter(String username, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list of favorite spaces of a user. This list access matches with the
   * provided space filter.
   *
   * @param username The remote Id of user
   * @param spaceFilter {@link SpaceFilter} used to filter on spaces
   * @return {@link ListAccess} of {@link Space} marked as favorite of user
   * @since Meeds 1.4.0
   */
  default ListAccess<Space> getFavoriteSpacesByFilter(String username, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the access
   * permission.
   *
   * @param username The remote user Id.
   * @return The space list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getAccessibleSpacesWithListAccess(String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the access
   * permission. This list access matches with the provided space filter.
   *
   * @param username The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getAccessibleSpacesByFilter(String username, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user is invited to join.
   *
   * @param username The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getInvitedSpacesWithListAccess(String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user is invited to join.
   * This list access matches with the provided space filter.
   *
   * @param username The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getInvitedSpacesByFilter(String username, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user sent a request for
   * joining a space.
   *
   * @param username The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getPendingSpacesWithListAccess(String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user sent a request for
   * joining a space. This list access matches with the provided space filter.
   *
   * @param username The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default ListAccess<Space> getPendingSpacesByFilter(String username, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new space: creating a group, its group navigation with pages for
   * installing space applications.
   *
   * @param space The space to be created.
   * @param creatorUsername The remote user Id.
   * @return The created space.
   * @LevelAPI Platform
   */
  default Space createSpace(Space space, String creatorUsername) {
    throw new UnsupportedOperationException();
  }

  /**
   * Updates information of a space and invites all users from
   * identitiesToInvite to join this space.
   *
   * @param existingSpace The existing space to be updated.
   * @param identitiesToInvite The list of identities who are invited to join
   *          the space. Identity could be of type user or space
   * @return updated space entity
   */
  default Space updateSpace(Space existingSpace, List<Identity> identitiesToInvite) {
    throw new UnsupportedOperationException();
  }

  /**
   * Updates information of a space.
   *
   * @param existingSpace The existing space to be updated.
   * @return The updated space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space updateSpace(Space existingSpace) {
    throw new UnsupportedOperationException();
  }

  /**
   * Updates a space's avatar.
   *
   * @param existingSpace The existing space to be updated.
   * @param username user making the modification
   * @return The updated space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space updateSpaceAvatar(Space existingSpace, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Updates a space's avatar.
   *
   * @param existingSpace The existing space to be updated.
   * @param username user making the modification
   * @return The updated space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default Space updateSpaceBanner(Space existingSpace, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Deletes a space. When a space is deleted, all of its page navigations and
   * its group will be deleted.
   *
   * @param space The space to be deleted.
   * @throws SpaceException when the space can't be deleted
   */
  default void deleteSpace(Space space) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Deletes a space without deleting the user's group. When a space is deleted,
   * all of its page navigations and its group will be deleted.
   *
   * @param space The space to be deleted.
   * @throws SpaceException when the space can't be deleted
   */
  default void deleteSpace(Space space, boolean deleteGroup) throws SpaceException {
  }

  /**
   * Adds a user to the list of pending requests for joining a space.
   *
   * @param space The exising space.
   * @param username The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default void addPendingUser(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes a user from a list of pending requests for joining a space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default void removePendingUser(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a user is in the list of pending requests for joining a space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @return TRUE if the user request is pending. Otherwise, it is FALSE.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default boolean isPendingUser(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds a user to the list of users who are invited to join a space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default void addInvitedUser(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes a user from the list of users who are invited to join a space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default void removeInvitedUser(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a user is in the list of users who are invited to join a space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @return TRUE if the user is in the list of invited users. Otherwise, it is
   *         FALSE.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default boolean isInvitedUser(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds a user to a space. The user will get the "member" role in a space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @LevelAPI Platform
   */
  default void addMember(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes a member from a space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @LevelAPI Platform
   */
  default void removeMember(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a given user is member of space or not.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @return TRUE if the user is member. Otherwise, it is FALSE.
   * @LevelAPI Platform
   */
  default boolean isMember(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Assigns the "redactor" role to a user in a space.
   *
   * @param space The space that its user is assigned to redactor.
   * @param username The remote user Id.
   * @LevelAPI Platform
   */
  default void addRedactor(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes the "redactor" role of a user in a space.
   *
   * @param space The space that its user is assigned to redactor.
   * @param username The remote user Id.
   * @LevelAPI Platform
   */
  default void removeRedactor(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a given user has the "redactor" role in a space.
   *
   * @param space The space that its user is checked if he has the "redactor"
   *          role or not.
   * @param username The remote user Id.
   * @return "True" if the user has the "redactor" role. Otherwise, it returns
   *         "false".
   * @LevelAPI Platform
   */
  default boolean isRedactor(Space space, String username) {
    return false;
  }

  /**
   * Assigns the "publisher" role to a user in a space.
   *
   * @param space The space that its user is assigned to publisher.
   * @param username The remote user Id.
   * @LevelAPI Platform
   */
  default void addPublisher(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes the "publisher" role of a user in a space.
   *
   * @param space The space that its user is assigned to publisher.
   * @param username The remote user Id.
   * @LevelAPI Platform
   */
  default void removePublisher(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a given user has the "publisher" role in a space.
   *
   * @param space The space that its user is checked if he has the "publisher"
   *          role or not.
   * @param username The remote user Id.
   * @return "True" if the user has the "publisher" role. Otherwise, it returns
   *         "false".
   * @LevelAPI Platform
   */
  default boolean isPublisher(Space space, String username) {
    return false;
  }

  /**
   * Checks if a given space has at least one "redactor"
   *
   * @param space The space that its user is checked if he has the "redactor"
   *          role or not.
   * @return true if the user has a "redactor" role into it
   */
  default boolean hasRedactor(Space space) {
    return false;
  }

  /**
   * checks whether the user can add content on space or not
   * 
   * @param space {@link Space}
   * @param viewer {@link org.exoplatform.services.security.Identity}
   * @return true if can add content, else false
   */
  default boolean canRedactOnSpace(Space space, org.exoplatform.services.security.Identity viewer) {
    return viewer != null && canRedactOnSpace(space, viewer.getUserId());
  }

  /**
   * checks whether the user can add content on space or not
   * 
   * @param space {@link Space}
   * @param username
   * @return true if can add content, else false
   */
  default boolean canRedactOnSpace(Space space, String username) {
    if (username == null) {
      return false;
    } else if (isMember(space, username)
               && (!hasRedactor(space)
                   || isRedactor(space, username)
                   || isManager(space, username))) {
      return true;
    } else {
      return isSuperManager(username);
    }
  }

  /**
   * checks whether the user can publish content in a space or not
   * 
   * @param space {@link Space}
   * @param username
   * @return true if can publish content, else false
   */
  default boolean canPublishOnSpace(Space space, String username) {
    if (username == null) {
      return false;
    } else if (isMember(space, username)
               && (isPublisher(space, username) || isManager(space, username))) {
      return true;
    } else {
      return isSuperManager(username) || isContentPublisher(username);
    }
  }

  /**
   * checks whether the user can manage the space or not
   * 
   * @param space {@link Space}
   * @param username
   * @return true if can manage the space, else false
   */
  default boolean canManageSpace(Space space, String username) {
    if (username == null || space == null) {
      return false;
    } else if (isMember(space, username) && isManager(space, username)) {
      return true;
    } else {
      return isSuperManager(username);
    }
  }

  /**
   * checks whether the user can view the space or not
   * 
   * @param space {@link Space}
   * @param username
   * @return true if can view the space, else false
   */
  default boolean canViewSpace(Space space, String username) {
    if (username == null) {
      return false;
    } else if (isMember(space, username)) {
      return true;
    } else {
      return isSuperManager(username);
    }
  }

  /**
   * Checks whether the user can access Space Public Site or not
   * 
   * @param space {@link Space}
   * @param username User name identifier
   * @return true if can access sitch Space Settings else false
   */
  default boolean canAccessSpacePublicSite(Space space, String username) {
    return false;
  }

  /**
   * Assigns the "manager" role to a user in a space.
   *
   * @param space The space that its user is assigned to manager.
   * @param username The remote user Id.
   * @param isManager "True" if the user gets the "manager" role. "False" if the
   *          user only gets the "member" role.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default void setManager(Space space, String username, boolean isManager) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a given user has the "manager" role in a space.
   *
   * @param space The space that its user is checked if he has the "manager"
   *          role or not.
   * @param username The remote user Id.
   * @return "True" if the user has the "manager" role. Otherwise, it returns
   *         "false".
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default boolean isManager(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a given user is the only one who has the "manager" role in a
   * space.
   *
   * @param space The space that its user is checked if he is the only manager
   *          or not.
   * @param username The remote user Id.
   * @return "True" if the user Id is the only one who has "manager" role in the
   *         space. Otherwise, it returns "false".
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default boolean isOnlyManager(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a given user can access a space or not.
   *
   * @param space The space that its user is checked if he can access it.
   * @param username The remote user Id.
   * @return "True" if the access permission is allowed. Otherwise, it returns
   *         "false".
   */
  default boolean hasAccessPermission(Space space, String username) {
    return canViewSpace(space, username);
  }

  /**
   * Checks if a given user has the setting permission to a space or not.
   *
   * @param space The space that its user is checked if he has the setting
   *          permission or not.
   * @param username The remote user Id.
   * @return If the user is root or the space's member, "true" is returned.
   *         Otherwise, it returns "false".
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default boolean hasSettingPermission(Space space, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Registers a space listener plugin to listen to space lifecyle events:
   * creating, updating, installing an application, and more.
   *
   * @param spaceListenerPlugin The space listener plugin to be registered.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default void registerSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unregisters an existing space listener plugin.
   *
   * @param spaceListenerPlugin The space listener plugin to be unregistered.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  default void unregisterSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets spaces that a given user can see.
   * 
   * @param username Id of the user.
   * @param spaceFilter The condition by which spaces are filtered.
   * @return The list of spaces.
   * @LevelAPI Platform
   * @since 1.2.5-GA
   */
  default ListAccess<Space> getVisibleSpacesWithListAccess(String username, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new space and invites all users from identitiesToInvite to join
   * this newly created space.
   *
   * @param space The space to be created.
   * @param creator The user who creates the space.
   * @param identitiesToInvite The list of identities who are invited to join
   *          the space. Identity could be of type user or space
   * @return The space.
   * @throws SpaceException with possible code
   *           SpaceException.Code.SPACE_ALREADY_EXIST; UNABLE_TO_ADD_CREATOR
   */
  default Space createSpace(Space space, String creator, List<Identity> identitiesToInvite) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Saves a new space or updates a space.
   *
   * @param space The space to be saved or updated.
   */
  default void createSpace(Space space) {
    throw new UnsupportedOperationException();
  }

  /**
   * Renames a space.
   *
   * @param space The space to be renamed.
   * @param newDisplayName New name of the space.
   */
  default void renameSpace(Space space, String newDisplayName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Renames a space by an identity who has rights of super admin.
   *
   * @param username The identity who has renamed a space.
   * @param space The space to be renamed.
   * @param newDisplayName New name of the space.
   * @throws SpaceException
   * @LevelAPI Platform
   * @since 4.0.0
   */
  default void renameSpace(Space space, String newDisplayName, String username) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Deletes a space by its Id.
   *
   * @param spaceId Id of the deleted space.
   * @throws SpaceException 
   */
  default void deleteSpace(String spaceId) throws SpaceException {
    deleteSpace(getSpaceById(spaceId));
  }

  /**
   * Adds a user to space as "member".
   *
   * @param spaceId Id of the space.
   * @param username Id of the user (remoteId).
   */
  default void addMember(String spaceId, String username) {
    addMember(getSpaceById(spaceId), username);
  }

  /**
   * Removes a member from space.
   *
   * @param spaceId Id of the space.
   * @param username Id of the user (remoteId).
   */
  default void removeMember(String spaceId, String username) {
    removeMember(getSpaceById(spaceId), username);
  }

  /**
   * Checks if a given user is space member or not.
   *
   * @param spaceId Id of the space.
   * @param username Id of user (remoteId).
   * @return "True" if the user is space member. Otherwise, it returns "false".
   */
  default boolean isMember(String spaceId, String username) {
    return isMember(getSpaceById(spaceId), username);
  }

  /**
   * Checks if a user is in the list of users who have ignored the space.
   *
   * @param space The existing space.
   * @param username The remote user Id.
   * @return TRUE if the space is an ignored space. Otherwise, it is FALSE.
   */
  default boolean isIgnored(Space space, String username) {
    return false;
  }

  /**
   * Assigns the user who has ignored the space.
   *
   * @param spaceId The Id of the space ignored by the user.
   * @param username The remote user Id.
   */
  default void setIgnored(String spaceId, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Restores a page layout to its default associated to Space Template
   * 
   * @param spaceId {@link Space} technical identifier
   * @param appId Id of the installed application or can be 'home' to designate
   *          the space home page
   * @param identity user {@link Identity} making the change
   * @throws IllegalAccessException when current user doesn't have permission to
   *           manage spaces
   * @throws SpaceException when error reading Space identified by its id
   * @deprecated Will be removed when the Meeds-io/MIPs#150 get implemented with new way of Layout management
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void restoreSpacePageLayout(String spaceId,
                                      String appId,
                                      org.exoplatform.services.security.Identity identity) throws IllegalAccessException,
                                                                                           SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Installs an application in a space.
   *
   * @param space The space that the application is installed.
   * @param appId Id of the installed application.
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   * @deprecated the spaces pages aren't managed through applications anymore,
   *             thus this operation isn't needed anymore
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void installApplication(Space space, String appId) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Activates an installed application in a space.
   *
   * @param space The space that the installed application is activated.
   * @param appId Id of the installed application.
   * @throws SpaceException with possible code:
   *           SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *           SpaceExeption.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   * @deprecated the spaces pages aren't managed through applications anymore,
   *             thus this operation isn't needed anymore
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void activateApplication(Space space, String appId) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Activates an installed application in a space.
   *
   * @param spaceId Id of the space that the installed application is activated.
   * @param appId Id of the installed application.
   * @throws SpaceException with possible code:
   *           SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *           SpaceExeption.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   * @deprecated the spaces pages aren't managed through applications anymore,
   *             thus this operation isn't needed anymore
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void activateApplication(String spaceId, String appId) throws SpaceException {
    activateApplication(getSpaceById(spaceId), appId);
  }

  /**
   * Deactivates an installed application in a space.
   *
   * @param space The space that the installed application is deactivated.
   * @param appId Id of the installed application.
   * @throws SpaceException
   * @LevelAPI Platform
   * @deprecated the spaces pages aren't managed through applications anymore,
   *             thus this operation isn't needed anymore
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void deactivateApplication(Space space, String appId) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Deactivates an installed application in a space.
   *
   * @param spaceId Id of the space that the installed application is
   *          deactivated.
   * @param appId Id of the installed application.
   * @throws SpaceException
   * @LevelAPI Platform
   * @deprecated the spaces pages aren't managed through applications anymore,
   *             thus this operation isn't needed anymore
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void deactivateApplication(String spaceId, String appId) throws SpaceException {
    deactivateApplication(getSpaceById(spaceId), appId);
  }

  /**
   * Removes an installed application from a space.
   *
   * @param space The space that the installed application is removed.
   * @param appId Id of the installed application.
   * @throws SpaceException
   * @LevelAPI Platform
   * @deprecated the spaces pages aren't managed through applications anymore,
   *             thus this operation isn't needed anymore
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void removeApplication(Space space, String appId, String appName) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * @param spaceId technical id of the space
   * @param appId application identifier
   * @param transition how much levels to move up or down an application
   * @throws SpaceException
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void moveApplication(String spaceId, String appId, int transition) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes an installed application from a space.
   *
   * @param spaceId Id of the space that the installed application is removed.
   * @param appId Id of the installed application.
   * @LevelAPI Platform
   * @deprecated the spaces pages aren't managed through applications anymore,
   *             thus this operation isn't needed anymore
   */
  @Deprecated(since = "7.0", forRemoval = true)
  default void removeApplication(String spaceId, String appId, String appName) throws SpaceException {
    removeApplication(getSpaceById(spaceId), appId, appName);
  }

  /**
   * Updates the most recently accessed space of a user to the top of spaces
   * list.
   *
   * @param remoteId The remote Id of the user.
   * @param space The last accessed space of the user.
   */
  default void updateSpaceAccessed(String remoteId, Space space) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets the last spaces that have been created.
   *
   * @param limit the limit of spaces to provide.
   * @return The last spaces.
   * @LevelAPI Experimental
   * @since 4.0.x
   */
  default List<Space> getLastSpaces(int limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list of the most recently accessed spaces of a user.
   *
   * @param remoteId The remote Id of a user.
   * @LevelAPI Platform
   */
  default ListAccess<Space> getLastAccessedSpace(String remoteId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Registers a space lifecycle listener.
   *
   * @param listener The space lifecycle listener to be registered.
   */
  default void registerSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unregisters a space lifecycle listener.
   *
   * @param listener The space lifecycle listener to be unregistered.
   */
  default void unregisterSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets the list of spaces which are visited by users
   *
   * @param remoteId
   * @return
   */
  default ListAccess<Space> getVisitedSpaces(String remoteId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if the user is a super manager of all spaces
   *
   * @param username user name
   * @return true if the user is member of super administrators groups, else
   *         false
   */
  default boolean isSuperManager(String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if the user is a content manager of all spaces
   *
   * @param username user name
   * @return true if the user is a content manager
   */
  default boolean isContentManager(String username) {
    return isSuperManager(username);
  }

  /**
   * Checks if the user is a content publisher of all spaces
   *
   * @param username user name
   * @return true if the user is a content publisher
   */
  default boolean isContentPublisher(String username) {
    return isSuperManager(username);
  }

  /**
   * Invites users or members of spaces identified by identities list into a a
   * designated space
   * 
   * @param space target {@link Space} to invite identites
   * @param identitiesToInvite {@link List} of {@link Identity} (space or user)
   *          to invite
   */
  default void inviteIdentities(Space space, List<Identity> identitiesToInvite) {
    throw new UnsupportedOperationException();
  }

  /**
   * Checks if a specific space contains external users
   *
   * @param spaceId
   * @return true if the space contains external users
   */
  default boolean isSpaceContainsExternals(Long spaceId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Retrieves the list of pending 'requests to join' a space that the user
   * manages
   * 
   * @param remoteId
   * @return {@link ListAccess} of {@link Space} with pending users requesting
   *         to join spaces that the designated user manages
   */
  default ListAccess<Space> getPendingSpaceRequestsToManage(String remoteId) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return a list of external invitations in space
   */
  default List<SpaceExternalInvitation> findSpaceExternalInvitationsBySpaceId(String spaceId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Add an external invitation to space
   *
   * @param spaceId
   * @param email
   * @param tokenId
   */
  default void saveSpaceExternalInvitation(String spaceId, String email, String tokenId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a Space external invitation by its Id.
   *
   * @param invitationId
   */
  default SpaceExternalInvitation getSpaceExternalInvitationById(String invitationId) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete an external invitation from a space
   *
   * @param invitationId
   */
  default void deleteSpaceExternalInvitation(String invitationId) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param email
   * @return a list of spaces ids by external email
   */
  default List<String> findExternalInvitationsSpacesByEmail(String email) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete external invitations
   *
   * @param email
   */
  default void deleteExternalUserInvitations(String email) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all common spaces between two users
   *
   * @param username current user id
   * @param otherUsername visited profile user id
   * @return list of common spaces between two users in param
   */
  default ListAccess<Space> getCommonSpaces(String username, String otherUsername) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a {@link List} of Space technical identifiers where the user has the
   * "member" role.
   *
   * @param username The remote user Id.
   * @param offset The starting point
   * @param limit The limitation of returned results
   * @return {@link List} of Space technical identifiers
   */
  default List<String> getMemberSpacesIds(String username, int offset, int limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a {@link List} of Space technical identifiers where the user has the
   * "manager" role.
   *
   * @param username The remote user Id.
   * @param offset The starting point
   * @param limit The limitation of returned results
   * @return {@link List} of Space technical identifiers
   */
  default List<String> getManagerSpacesIds(String username, int offset, int limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Retrieves the Space Membership date
   * 
   * @param spaceId {@link Space} technical id
   * @param username User name (identifier)
   * @return {@link Instant} corresponding to the creation date of the
   *         membership
   */
  default Instant getSpaceMembershipDate(long spaceId, String username) {
    throw new UnsupportedOperationException();
  }

  /**
   * Saves the space public site characteristics
   * 
   * @param spaceId
   * @param publicSiteVisibility Visibility of public site, possible values:
   *          manager, member, internal, authenticated or everyone.
   * @param username user identifier who's making the operation
   * @throws IllegalAccessException when
   * @throws ObjectNotFoundException
   */
  default void saveSpacePublicSite(String spaceId,
                                   String publicSiteVisibility,
                                   String username) throws ObjectNotFoundException, IllegalAccessException {
    throw new UnsupportedOperationException();
  }

  /**
   * Saves the space public site characteristics
   * 
   * @param space {@link Space}
   * @param publicSiteVisibility Visibility of public site, possible values:
   *          manager, member, internal, authenticated or everyone.
   */
  default void saveSpacePublicSite(Space space, String publicSiteVisibility) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param space {@link Space}
   * @return Public site name if exists, else null
   */
  default String getSpacePublicSiteName(Space space) {
    throw new UnsupportedOperationException();
  }

}
