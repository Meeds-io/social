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

import java.util.List;
import java.util.Locale;

import org.exoplatform.application.registry.Application;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.application.PortletPreferenceRequiredPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.model.Space;

/**
 * Provides methods to work with Space.
 *
 * @since Aug 29, 2008
 *
 */
public interface SpaceService {

  /**
   * Will be removed by 4.0.x.
   */
  @Deprecated
  final String SPACES_APP_ID = "exosocial:spaces";

  /**
   * Gets a space by its display name.
   *
   * @param spaceDisplayName The space display name.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByDisplayName(String spaceDisplayName);

  /**
   * Gets a space by its pretty name.
   *
   * @param spacePrettyName The space's pretty name.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByPrettyName(String spacePrettyName);

  /**
   * Gets a space by its group Id.
   *
   * @param groupId The group Id.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByGroupId(String groupId);

  /**
   * Gets a space by its Id.
   *
   * @param spaceId Id of the space.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceById(String spaceId);

  /**
   * Gets a space by its URL.
   *
   * @param spaceUrl URL of the space.
   * @return The space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByUrl(String spaceUrl);

  /**
   * Gets a list access that contains all spaces.
   *
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAllSpacesWithListAccess();


  /**
   * Gets a list access that contains all spaces matching with a filter.
   *
   * @param spaceFilter The space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAllSpacesByFilter(SpaceFilter spaceFilter);

  /**
   * Gets a list access containing all spaces that a user has the "manager" role.
   *
   * @param userId The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   */
  default ListAccess<Space> getManagerSpaces(String userId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the "manager" role. This list access matches with the provided space
   * filter.
   *
   * @param userId The remote user Id.
   * @param spaceFilter The space filter.
   * @return The list access.
   * @LevelAPI Platform
   */
  default ListAccess<Space> getManagerSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the "member" role.
   *
   * @param userId The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getMemberSpaces(String userId);
  
  /**
   * Gets a list access containing all spaces that a user has the "member" role. This list access matches with the provided space
   * filter.
   *
   * @param userId The remote user Id.
   * @param spaceFilter The space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a list of favorite spaces of a user. This list access matches with the provided space
   * filter.
   *
   * @param userId The remote Id of user
   * @param spaceFilter {@link SpaceFilter} used to filter on spaces
   * @return {@link ListAccess} of {@link Space} marked as favorite of user
   * @since Meeds 1.4.0
   */
  default ListAccess<Space> getFavoriteSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all spaces that a user has the access permission.
   *
   * @param userId The remote user Id.
   * @return The space list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAccessibleSpacesWithListAccess(String userId);

  /**
   * Gets a list access containing all spaces that a user has the access permission.
   * This list access matches with the provided space filter.
   *
   * @param userId The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a list access containing all spaces that a user has the setting permission.
   *
   * @param userId The remote user Id.
   * @return The space list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getSettingableSpaces(String userId);

  /**
   * Gets a list access containing all spaces that a user has the setting permission.
   * This list access matches with the provided space filter.
   *
   * @param userId The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The space list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getSettingabledSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a list access containing all spaces that a user is invited to join.
   *
   * @param userId The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getInvitedSpacesWithListAccess(String userId);

  /**
   * Gets a list access containing all spaces that a user is invited to join.
   * This list access matches with the provided
   * space filter.
   *
   * @param userId The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a list access containing all spaces that a user can request to join.
   *
   * @param userId The remote user Id.
   * @return The space list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getPublicSpacesWithListAccess(String userId);

  /**
   * Gets a list access containing all spaces that a user can request to join.
   * This list access matches with the provided
   * space filter.
   *
   * @param userId The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  ListAccess<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter);


  /**
   * Gets a list access containing all spaces that a user sent a request for joining a space.
   *
   * @param userId The remote user Id.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getPendingSpacesWithListAccess(String userId);

  /**
   * Gets a list access containing all spaces that a user sent a request for joining a space.
   * This list access matches with the provided space filter.
   *
   * @param userId The remote user Id.
   * @param spaceFilter The provided space filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Creates a new space: creating a group, its group navigation with pages for installing space applications.
   *
   * @param space The space to be created.
   * @param creatorUserId The remote user Id.
   * @return The created space.
   * @LevelAPI Platform
   */
  Space createSpace(Space space, String creatorUserId);

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
   * @since  1.2.0-GA
   */
  Space updateSpace(Space existingSpace);

  /**
   * Updates a space's avatar.
   *
   * @param existingSpace The existing space to be updated.
   * @return The updated space.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Space updateSpaceAvatar(Space existingSpace);

  /**
   * Updates a space's avatar.
   *
   * @param existingSpace The existing space to be updated.
   * @return The updated space.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Space updateSpaceBanner(Space existingSpace);

  /**
   * Deletes a space. When a space is deleted, all of its page navigations and its group will be deleted.
   *
   * @param space The space to be deleted.
   * @LevelAPI Platform
   */
  void deleteSpace(Space space);
  /**
   * Deletes a space without deleting the user's group. When a space is deleted, all of its page navigations and its group will be deleted.
   *
   * @param space The space to be deleted.
   * @LevelAPI Platform
   */
  default void deleteSpace(Space space, boolean deleteGroup) {}

  /**
   * Adds a user to the list of pending requests for joining a space.
   *
   * @param space The exising space.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void addPendingUser(Space space, String userId);

  /**
   * Removes a user from a list of pending requests for joining a space.
   *
   * @param space The existing space.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void removePendingUser(Space space, String userId);

  /**
   * Checks if a user is in the list of pending requests for joining a space.
   *
   * @param space The existing space.
   * @param userId The remote user Id.
   * @return TRUE if the user request is pending. Otherwise, it is FALSE.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   *
   */
  boolean isPendingUser(Space space, String userId);

  /**
   * Adds a user to the list of users who are invited to join a space.
   *
   * @param space The existing space.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void addInvitedUser(Space space, String userId);

  /**
   * Removes a user from the list of users who are invited to join a space.
   *
   * @param space  The existing space.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void removeInvitedUser(Space space, String userId);

  /**
   * Checks if a user is in the list of users who are invited to join a space.
   *
   * @param space The existing space.
   * @param userId The remote user Id.
   * @return TRUE if the user is in the list of invited users. Otherwise, it is FALSE.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   *
   */
  boolean isInvitedUser(Space space, String userId);

  /**
   * Adds a user to a space. The user will get the "member" role in a space.
   *
   * @param space The existing space.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   */
  void addMember(Space space, String userId);

  /**
   * Removes a member from a space.
   *
   * @param space The existing space.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   */
  void removeMember(Space space, String userId);

  /**
   * Checks if a given user is member of space or not.
   *
   * @param space The existing space.
   * @param userId The remote user Id.
   * @return TRUE if the user is member. Otherwise, it is FALSE.
   * @LevelAPI Platform
   */
  boolean isMember(Space space, String userId);
  
  /**
   * Assigns the "redactor" role to a user in a space.
   *
   * @param space The space that its user is assigned to redactor.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   */
  default void addRedactor(Space space, String userId) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Removes the "redactor" role of a user in a space.
   *
   * @param space The space that its user is assigned to redactor.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   */
  default void removeRedactor(Space space, String userId) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Checks if a given user has the "redactor" role in a space.
   *
   * @param space The space that its user is checked if he has the "redactor" role or not.
   * @param userId The remote user Id.
   * @return "True" if the user has the "redactor" role. Otherwise, it returns "false".
   * @LevelAPI Platform
   */
  default boolean isRedactor(Space space, String userId) {
    return false;
  }
  
  /**
   * Assigns the "publisher" role to a user in a space.
   *
   * @param space The space that its user is assigned to publisher.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   */
  default void addPublisher(Space space, String userId) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Removes the "publisher" role of a user in a space.
   *
   * @param space The space that its user is assigned to publisher.
   * @param userId The remote user Id.
   * @LevelAPI Platform
   */
  default void removePublisher(Space space, String userId) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Checks if a given user has the "publisher" role in a space.
   *
   * @param space The space that its user is checked if he has the "publisher" role or not.
   * @param userId The remote user Id.
   * @return "True" if the user has the "publisher" role. Otherwise, it returns "false".
   * @LevelAPI Platform
   */
  default boolean isPublisher(Space space, String userId) {
    return false;
  }

  /**
   * Checks if a given space has at least one "redactor"
   *
   * @param space The space that its user is checked if he has the "redactor" role or not.
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
    return false;
  }

  /**
   * Assigns the "manager" role to a user in a space.
   *
   * @param space The space that its user is assigned to manager.
   * @param userId The remote user Id.
   * @param isManager "True" if the user gets the "manager" role. "False" if the user only gets the "member" role.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void setManager(Space space, String userId, boolean isManager);

  /**
   * Checks if a given user has the "manager" role in a space.
   *
   * @param space The space that its user is checked if he has the "manager" role or not.
   * @param userId The remote user Id.
   * @return "True" if the user has the "manager" role. Otherwise, it returns "false".
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  boolean isManager(Space space, String userId);
  
  /**
   * Checks if a given user is the only one who has the "manager" role in a space.
   *
   * @param space The space that its user is checked if he is the only manager or not.
   * @param userId The remote user Id.
   * @return "True" if the user Id is the only one who has "manager" role in the space. Otherwise, it returns "false".
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  boolean isOnlyManager(Space space, String userId);

  /**
   * Checks if a given user can access a space or not.
   *
   * @param space The space that its user is checked if he can access it.
   * @param userId The remote user Id.
   * @return "True" if the access permission is allowed. Otherwise, it returns "false".
   * @LevelAPI Platform
   *
   */
  boolean hasAccessPermission(Space space, String userId);

  /**
   * Checks if a given user has the setting permission to a space or not.
   *
   * @param space The space that its user is checked if he has the setting permission or not.
   * @param userId The remote user Id.
   * @return If the user is root or the space's member, "true" is returned. Otherwise, it returns "false".
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  boolean hasSettingPermission(Space space, String userId);

  /**
   * Registers a space listener plugin to listen to space lifecyle events: creating, updating, installing an application, and more.
   *
   * @param spaceListenerPlugin The space listener plugin to be registered.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void registerSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin);

  /**
   * Unregisters an existing space listener plugin.
   *
   * @param spaceListenerPlugin The space listener plugin to be unregistered.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void unregisterSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin);

  /**
   * Sets a space application config plugin for configuring the home and space applications.
   * <br>
   * By configuring this, the space service will know how to create a new page node with title, URL and portlet.
   *
   * @param spaceApplicationConfigPlugin The space application config plugin to be set.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  @Deprecated
  void setSpaceApplicationConfigPlugin(SpaceApplicationConfigPlugin spaceApplicationConfigPlugin);

  /**
   * Gets the space application config plugin.
   *
   * @return The space application config plugin.
   * @LevelAPI Platform
   */
  @Deprecated
  SpaceApplicationConfigPlugin getSpaceApplicationConfigPlugin();

  /**
   * Gets all spaces in Social.
   *
   * @return The list of spaces in Social.
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllSpacesWithListAccess()} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Space> getAllSpaces() throws SpaceException;

  /**
   * Gets a space by its space name.
   *
   * @param spaceName The space name.
   * @return The stored space.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link SpaceService#getSpaceByPrettyName(String)} instead.
   *             Will be removed at 4.0.x.
   */
  public Space getSpaceByName(String spaceName) throws SpaceException;

  /**
   * Gets all spaces that their names or descriptions match with the input condition.
   *
   * @param condition The input condition.
   * @return The list of spaces.
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllSpacesByFilter(org.exoplatform.social.core.space.SpaceFilter)} instead.
   *             Will be removed by 4.0.x.
   */
  List<Space> getSpacesBySearchCondition(String condition) throws Exception;

  /**
   * Gets spaces that a given user is member.
   *
   * @param userId Id of the user.
   * @return All spaces that the user is member.
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   * @LevelAPI Provisional
   * @deprecated Use {@link #getMemberSpaces(String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Space> getSpaces(String userId) throws SpaceException;

  /**
   * Gets spaces that a given user has the access permission.
   *
   * @param userId Id of the user.
   * @return The list of spaces.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAccessibleSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x.
   */
  List<Space> getAccessibleSpaces(String userId) throws SpaceException;

  /**
   * Gets spaces that a given user can see.
   *
   * @param userId Id of the user.
   * @param spaceFilter Condition by which spaces are filtered.
   * @return The list of spaces.
   * @throws SpaceException
   * @LevelAPI Platform
   * @since 1.2.5-GA
   */
  public List<Space> getVisibleSpaces(String userId, SpaceFilter spaceFilter) throws SpaceException;

  /**
   * Gets spaces that a given user can see.
   * @param userId Id of the user.
   * @param spaceFilter The condition by which spaces are filtered.
   * @return The list of spaces.
   * @LevelAPI Platform
   * @since 1.2.5-GA
   */
  public SpaceListAccess getVisibleSpacesWithListAccess(String userId, SpaceFilter spaceFilter);

  /**
   * Provides the Unified Search feature to get spaces that a user can see.
   * @param userId Id of the user.
   * @param spaceFilter The condition by which spaces are filtered.
   * @return The list of spaces.
   * @LevelAPI Platform
   * @since 4.0.0-GA
   */
  public SpaceListAccess getUnifiedSearchSpacesWithListAccess(String userId, SpaceFilter spaceFilter);

  /**
   * Gets spaces that a given user has the edit permission.
   *
   * @param userId Id of the user.
   * @return The list of spaces.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getSettingableSpaces(String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Space> getEditableSpaces(String userId) throws SpaceException;

  /**
   * Gets all spaces that a given user is invited and can accept or deny requests.
   *
   * @param userId Id of the user.
   * @return The list of spaces that the user is invited.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getInvitedSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Space> getInvitedSpaces(String userId) throws SpaceException;


  /**
   * Gets public spaces which a given user can request to join.
   *
   * @param userId Id of the user.
   * @return The list of spaces that the user can request to join.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getPublicSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Space> getPublicSpaces(String userId) throws SpaceException;

  /**
   * Gets pending spaces which a given user can revoke requests.
   *
   * @param userId Id of the user.
   * @return The list of pending spaces.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getPendingSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Space> getPendingSpaces(String userId) throws SpaceException;

  /**
   * Creates a new space and invites all users from invitedGroupId to join this newly created space.
   *
   * @param space The space to be created.
   * @param creator The user who creates the space.
   * @param invitedGroupId Id of group who is invited to join the space.
   * @return The space.
   * @throws SpaceException with possible code SpaceException.Code.SPACE_ALREADY_EXIST; UNABLE_TO_ADD_CREATOR
   * @LevelAPI Platform
   * @deprecated Use {@link #createSpace(Space, String, List)} instead.
   */
  @Deprecated
  Space createSpace(Space space, String creator, String invitedGroupId) throws SpaceException;

  /**
   * Creates a new space and invites all users from identitiesToInvite to join this newly created space.
   *
   * @param space The space to be created.
   * @param creator The user who creates the space.
   * @param identitiesToInvite The list of identities who are invited to join the space.
   *                           Identity could be of type user or space
   * @return The space.
   * @throws SpaceException with possible code SpaceException.Code.SPACE_ALREADY_EXIST; UNABLE_TO_ADD_CREATOR
   */
  Space createSpace(Space space, String creator, List<Identity> identitiesToInvite) throws SpaceException;

  /**
   * Saves a new space or updates a space.
   *
   * @param space The space to be saved or updated.
   * @param isNew "True" if a new space is created. "False" if an existing space is updated.
   * @throws SpaceException with code: SpaceException.Code.ERROR_DATASTORE
   * @LevelAPI Provisional
   * @deprecated Use {@link #updateSpace(org.exoplatform.social.core.space.model.Space)} instead.
   *             Will be removed by 4.0.x.
   */
  void saveSpace(Space space, boolean isNew) throws SpaceException;

  /**
   * Renames a space.
   *
   * @param space The space to be renamed.
   * @param newDisplayName New name of the space.
   * @throws SpaceException
   * @LevelAPI Platform
   * @since 1.2.8
   */
  void renameSpace(Space space, String newDisplayName) throws SpaceException;

  /**
   * Renames a space by an identity who has rights of super admin.
   *
   * @param remoteId The identity who has renamed a space.
   * @param space The space to be renamed.
   * @param newDisplayName New name of the space.
   * @throws SpaceException
   * @LevelAPI Platform
   * @since 4.0.0
   */
  void renameSpace(String remoteId, Space space, String newDisplayName) throws SpaceException;

  /**
   * Deletes a space by its Id.
   *
   * @param spaceId Id of the deleted space.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #deleteSpace(org.exoplatform.social.core.space.model.Space)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void deleteSpace(String spaceId) throws SpaceException;

  /**
   * Does nothing, just for compatible.
   *
   * @param space The space.
   * @throws SpaceException with code SpaceException.Code.UNABLE_INIT_APP
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  @Deprecated
  void initApp(Space space) throws SpaceException;

  /**
   * Does nothing, just for compatible.
   *
   * @param space The space.
   * @throws SpaceException with code SpaceException.Code.UNABLE_INIT_APP
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  @Deprecated
  void initApps(Space space) throws SpaceException;

  /**
   * Does nothing, just for compatible.
   *
   * @param space The space.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  @Deprecated
  void deInitApps(Space space) throws SpaceException;

  /**
   * Adds a user to space as "member".
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void addMember(String spaceId, String userId) throws SpaceException;

  /**
   * Removes a member from space.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void removeMember(String spaceId, String userId) throws SpaceException;

  /**
   * Gets a list of members from a given space.
   *
   * @param space The space.
   * @return The list of space members.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link Space#getMembers()} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<String> getMembers(Space space) throws SpaceException;

  /**
   * Gets a list of members from a given space.
   *
   * @param spaceId Id of the space.
   * @return The list of space members.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link Space#getMembers()} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<String> getMembers(String spaceId) throws SpaceException;

  /**
   * Sets a space member to manager or vice versa.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @param isLeader If "true", the space member is set to manager. If "false", the space manager is set to member.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #setManager(org.exoplatform.social.core.space.model.Space, String, boolean)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void setLeader(Space space, String userId, boolean isLeader) throws SpaceException;

  /**
   * Sets a space member to manager or vice versa.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @param isLeader If "true", the space member is set to manager. If "false", the space manager is set to member.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #setManager(org.exoplatform.social.core.space.model.Space, String, boolean)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void setLeader(String spaceId, String userId, boolean isLeader) throws SpaceException;

  /**
   * Checks if a given user is space manager or not.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is space manager. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isLeader(Space space, String userId) throws SpaceException;

  /**
   * Checks if a given user is space manager or not.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is space manager. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isLeader(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a given user is the only manager of space or not.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is the only space manager. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isOnlyManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isOnlyLeader(Space space, String userId) throws SpaceException;

  /**
   * Checks if a given user is the only manager of space or not.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is the only space manager. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isOnlyManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isOnlyLeader(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a given user is space member or not.
   *
   * @param spaceId Id of the space.
   * @param userId Id of user (remoteId).
   * @return "True" if the user is space member. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isMember(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user can access a space or not.
   * If the user is root or the space's member, the "true" value is returned.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user has the access permission. "False" if the user does not have the access permission.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #hasAccessPermission(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean hasAccessPermission(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user has the edit permission on a space or not.
   * If the user is root or the space's manager, "true" is returned.
   *
   * @param space The provided space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user has the edit permission. "False" if the user does not have the edit permission.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #hasSettingPermission(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean hasEditPermission(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user has the edit permission on a space.
   * If user is root or the space's manager, "true" is returned.
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user has the edit permission. "False" if the user does not have the edit permission.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #hasSettingPermission(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean hasEditPermission(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user is in the list of invited users of a space.
   *
   * @param space The provided space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is in the list of invited users. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isInvited(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user is in the list of invited users of a space.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is in the list of invited users. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isInvited(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user is in the list of pending users of a space or not.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is in the list of pending users. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isPending(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user is in the list of pending users of a space.
   *
   * @param spaceId Id of the user.
   * @param userId Id of the user (remoteId).
   * @return "True" if the user is in the list of pending users. Otherwise, it returns "false".
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  boolean isPending(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user is in the list of users who have ignored the space.
   *
   * @param space  The existing space.
   * @param userId The remote user Id.
   * @return TRUE if the space is an ignored space. Otherwise, it
   * is FALSE.
   */
  default boolean isIgnored(Space space, String userId) {
    return false;
  }

  /**
   * Assigns the user who has ignored the space.
   *
   * @param spaceId The Id of the space ignored by the user.
   * @param userId  The remote user Id.
   */
  default void setIgnored(String spaceId, String userId) {

  }

  /**
   * Installs an application in a space.
   *
   * @param spaceId Id of the space that the application is installed.
   * @param appId Id of the application which is installed.
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void installApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Restores a page layout to its default associated to Space Template
   * 
   * @param spaceId {@link Space} technical identifier
   * @param appId Id of the installed application or can be 'home' to designate the space home page
   * @param identity user {@link Identity} making the change
   * @throws IllegalAccessException when current user doesn't have permission to manage spaces
   * @throws SpaceException when error reading Space identified by its id
   */
  default void restoreSpacePageLayout(String spaceId, String appId, org.exoplatform.services.security.Identity identity) throws IllegalAccessException, SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * Installs an application in a space.
   *
   * @param space The space that the application is installed.
   * @param appId Id of the installed application.
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void installApplication(Space space, String appId) throws SpaceException;

  /**
   * Activates an installed application in a space.
   *
   * @param space The space that the installed application is activated.
   * @param appId Id of the installed application.
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *                                            SpaceExeption.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void activateApplication(Space space, String appId) throws SpaceException;

  /**
   * Activates an installed application in a space.
   *
   * @param spaceId Id of the space that the installed application is activated.
   * @param appId Id of the installed application.
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *                                            SpaceExeption.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void activateApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Deactivates an installed application in a space.
   *
   * @param space The space that the installed application is deactivated.
   * @param appId Id of the installed application.
   * @throws SpaceException
   * @LevelAPI Platform
   */
  void deactivateApplication(Space space, String appId) throws SpaceException;

  /**
   * Deactivates an installed application in a space.
   *
   * @param spaceId Id of the space that the installed application is deactivated.
   * @param appId Id of the installed application.
   * @throws SpaceException
   * @LevelAPI Platform
   */
  void deactivateApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Removes an installed application from a space.
   *
   * @param space The space that the installed application is removed.
   * @param appId Id of the installed application.
   * @throws SpaceException
   * @LevelAPI Platform
   */
  void removeApplication(Space space, String appId, String appName) throws SpaceException;

  /**
   * Removes an installed application from a space.
   *
   * @param spaceId Id of the space that the installed application is removed.
   * @param appId Id of the installed application.
   * @LevelAPI Platform
   */
  void removeApplication(String spaceId, String appId, String appName) throws SpaceException;

  /**
   * Updates the most recently accessed space of a user to the top of spaces list.
   *
   * @param remoteId The remote Id of the user.
   * @param space The last accessed space of the user.
   * @LevelAPI Platform
   */
  void updateSpaceAccessed(String remoteId, Space space) throws SpaceException;

  /**
   * Gets a list of the most recently accessed spaces of a user.
   *
   * @param remoteId The remote Id of user.
   * @param appId Id of the installed application in a space.
   * @param offset The starting point to get the most recently accessed spaces.
   * @param limit The limitation of the most recently accessed spaces.
   * @LevelAPI Platform
   */
  List<Space> getLastAccessedSpace(String remoteId, String appId, int offset, int limit) throws SpaceException;

  /**
   * Gets the last spaces that have been created.
   *
   * @param limit the limit of spaces to provide.
   * @return The last spaces.
   * @LevelAPI Experimental
   * @since 4.0.x
   */
  List<Space> getLastSpaces(int limit);

  /**
   * Gets a list of the most recently accessed spaces of a user.
   *
   * @param remoteId The remote Id of a user.
   * @param appId Id of the installed application in a space.
   * @LevelAPI Platform
   */
  ListAccess<Space> getLastAccessedSpace(String remoteId, String appId);


  /**
   * Requests to join a space, then adds the requester to the list of pending spaces.
   *
   * @param space The space which the user requests to join.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void requestJoin(Space space, String userId) throws SpaceException;

  /**
   * Requests to join a space, then adds the requester to the list of pending spaces.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void requestJoin(String spaceId, String userId) throws SpaceException;

  /**
   * Revokes a request to join a space.
   *
   * @param space The space which the user requests to join.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void revokeRequestJoin(Space space, String userId) throws SpaceException;

  /**
   * Revokes a request to join a space.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void revokeRequestJoin(String spaceId, String userId) throws SpaceException;

  /**
   * Invites a user to become a space member.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void inviteMember(Space space, String userId) throws SpaceException;

  /**
   * Invites a user to become a space member.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void inviteMember(String spaceId, String userId) throws SpaceException;

  /**
   * Revokes an invitation - Removes the user from the list of invited users of the space.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void revokeInvitation(Space space, String userId) throws SpaceException;

  /**
   * Revokes an invitation - Removes the user from the list of invited users of the space.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void revokeInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Accepts an invitation - Moves the user from the invited users list to the members list.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void acceptInvitation(Space space, String userId) throws SpaceException;

  /**
   * Accepts an invitation - Moves the user from the invited users list to the members list.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void acceptInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Denies an invitation - Removes the user from the list of invited users.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void denyInvitation(Space space, String userId) throws SpaceException;

  /**
   * Denies an invitation - Removes the user from the list of invited users.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void denyInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Validates a request - Moves the user from the pending users list to the members list.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void validateRequest(Space space, String userId) throws SpaceException;

  /**
   * Validates a request - Moves the user from the pending users list to the members list.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void validateRequest(String spaceId, String userId) throws SpaceException;

  /**
   * Declines a request - Removes the user from the pending users list.
   *
   * @param space The space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void declineRequest(Space space, String userId) throws SpaceException;

  /**
   * Declines a request - Removes the user from the pending users list.
   *
   * @param spaceId Id of the space.
   * @param userId Id of the user (remoteId).
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void declineRequest(String spaceId, String userId) throws SpaceException;

  /**
   * Registers a space lifecycle listener.
   *
   * @param listener The space lifecycle listener to be registered.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  @Deprecated
  void registerSpaceLifeCycleListener(SpaceLifeCycleListener listener);

  /**
   * Unregisters a space lifecycle listener.
   *
   * @param listener The space lifecycle listener to be unregistered.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  @Deprecated
  void unregisterSpaceLifeCycleListener(SpaceLifeCycleListener listener);

  /**
   * Sets the portlet preferences got from the plugin configuration.
   *
   *
   * @param portletPrefsRequiredPlugin The plugin that configures portlets to store spaceUrl in its portlet-preference.
   * @LevelAPI Provisional
   * @deprecated Use {@link SpaceApplicationConfigPlugin} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void setPortletsPrefsRequired(PortletPreferenceRequiredPlugin portletPrefsRequiredPlugin);
  /**
   * Gets the portlet preferences which are required for creating the portlet application.
   *
   * @return Array of the portlet preferences.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  String [] getPortletsPrefsRequired();

  /**
   * Gets the list of spaces which are visited by users
   *
   * @param remoteId
   * @param appId
   * @return
   */
  ListAccess<Space> getVisitedSpaces(String remoteId, String appId);

  /**
   * Checks if the user is a super manager of all spaces
   *
   * @param userId username
   * @return true if the user is member of super administrators groups, else false
   */
  boolean isSuperManager(String userId);

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
  default boolean isSpaceContainsExternals(Long spaceId){
    throw new UnsupportedOperationException();
  }

  /**
   * Retrieves the list of pending 'requests to join' a space that the user
   * manages
   * 
   * @param remoteId
   * @return {@link ListAccess} of {@link Space} with pending users requesting to
   *         join spaces that the designated user manages
   */
  default ListAccess<Space> getPendingSpaceRequestsToManage(String remoteId) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param spaceId technical id of the space
   * @param appId application identifier
   * @param transition how much levels to move up or down an application
   * @throws SpaceException
   */
  default void moveApplication(String spaceId, String appId, int transition) throws SpaceException {
    throw new UnsupportedOperationException();
  }

  /**
   * @return {@link List} of {@link Application} that can be instantiated in spaces
   */
  default List<Application> getSpacesApplications() {
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
   * Add an application to applications list that can be instantiated in space
   * context
   * 
   * @param application of type {@link Application}
   */
  default void addSpacesApplication(Application application) {
    throw new UnsupportedOperationException();
  }

  /**
   * Delete application from list of spaces applications
   * 
   * @param applicationName application name to delete
   */
  default void deleteSpacesApplication(String applicationName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access containing all common spaces between two users
   *
   * @param userId connected user id
   * @param otherUserId visited profile user id
   * @return list of common spaces between two users in param
   */
  default ListAccess<Space> getCommonSpaces(String userId, String otherUserId) {
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

}