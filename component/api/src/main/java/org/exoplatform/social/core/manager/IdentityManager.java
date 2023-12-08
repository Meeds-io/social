/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.core.manager;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.identity.*;
import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Provides APIs to manage identities.
 * APIs provide capability of getting, creating or updating identities and profile information.
 * <p></p>
 * One identity is got by Id and allows the profile to be loaded or not.
 * With a list of identities, the type of returned result is <code>ListAccess</code> which is used for lazy loading.
 * Also, the API which adds or removes the provider information is provided.  
 */
public interface IdentityManager {

  public static final Sorting DEFAULT_SORTING              = new Sorting(SortBy.FULLNAME, OrderBy.ASC);

  /**
   * Gets the last identities that have been created.
   *
   * @param limit the limit of identities to provide.
   * @return The last identities.
   * @LevelAPI Experimental
   * @since 4.0.x
   */
  List<Identity> getLastIdentities(int limit);

  /**
   * Gets or creates an identity provided by an identity provider and an identity
   * Id.
   *
   * @param providerId Id of the identity provider.
   * @param remoteId The user remote Id.
   * @param isProfileLoaded Is profile loaded or not.
   * @return The identity.
   * @LevelAPI Platform
   * @deprecated Use {@link #getOrCreateIdentity(String, String)}
   *             instead. * Will be moved by 6.0.x.
   */
  Identity getOrCreateIdentity(String providerId, String remoteId, boolean isProfileLoaded);

  /**
   * Gets an identity by a given Id. This Id is UUID defined by storage.
   *
   * @param identityId Id of the identity.
   * @param isProfileLoaded Is profile loaded or not.
   * @return The identity.
   * @LevelAPI Platform 
   * @deprecated Use {@link #getIdentity(String)} instead.
   */
  Identity getIdentity(String identityId, boolean isProfileLoaded);

  /**
   * Updates specific properties of an identity.
   *
   * @param identity The identity.
   * @return The updated identity.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Identity updateIdentity(Identity identity);

  /**
   * Deletes an identity.
   *
   * @param identity The identity to be deleted.
   * @LevelAPI Platform 
   */
  void deleteIdentity(Identity identity);

  /**
   * Cleans all data related to a specific identity.
   *
   * @param identity The identity whose all data is cleaned.
   * @LevelAPI Platform 
   */
  void hardDeleteIdentity(Identity identity);

  /**
   * Gets a list access which contains all identities connected to the provided identity.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The provided identity.
   * @return The identities which have connected to the provided identity.
   * @LevelAPI Platform 
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getConnectionsWithListAccess(Identity identity);

  /**
   * Gets a profile associated with a provided identity.
   *
   * @param identity The provided identity.
   * @return The profile.
   * @LevelAPI Platform 
   * @since  1.2.0-GA
   */
  Profile getProfile(Identity identity);
  
  /**
   *  Gets a profile avatar associated with a provided identity
   * @param identity
   * @return
   */
  InputStream getAvatarInputStream(Identity identity) throws IOException;

  /**
   *  Gets a profile banner associated with a provided identity
   * @param identity
   * @return
   */
  InputStream getBannerInputStream(Identity identity) throws IOException;

  /**
   * Updates a specific profile.
   *
   * @param specificProfile The specific profile.
   * @LevelAPI Platform 
   * @since  1.2.0-GA
   */
  void updateProfile(Profile specificProfile);

  /**
   * Updates a specific profile and broadcast detected changes on profile if
   * 'broadcastChanges' is turned on
   *
   * @param specificProfile The specific profile.
   * @param broadcastChanges whether detect and broadcast changed fields or not
   */
  default void updateProfile(Profile specificProfile, boolean broadcastChanges) {
    throw new UnsupportedOperationException();
  }

  /**
   * Updates a specific profile and broadcast detected changes on profile if
   * 'broadcastChanges' is turned on
   *
   * @param specificProfile The specific profile.
   * @param modifierUsername modifier username.
   * @param broadcastChanges whether detect and broadcast changed fields or not
   */
  default void updateProfile(Profile specificProfile, String modifierUsername, boolean broadcastChanges) {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets a list access which contains all identities from a given provider.
   * These identities are filtered by the profile filter.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param providerId Id of the provider.
   * @param profileFilter The filter.
   * @param isProfileLoaded Is profile loaded or not.
   * @return The identities.
   * @LevelAPI Platform 
   * @since 1.2.0-GA
   */
  ListAccess<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter,
                                                  boolean isProfileLoaded);
  
  /**
   * Gets a list access which contains identities matching with the Unified Search condition.
   *
   * @param providerId Id of the provider.
   * @param profileFilter The filter.
   * @return The identities.
   * @since 4.0.x
   */
  ListAccess<Identity> getIdentitiesForUnifiedSearch(String providerId, ProfileFilter profileFilter);  
  
  /**
   * Gets space identities by filter information.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   * 
   * @param space The space in which identities are got.
   * @param profileFilter The filter information.
   * @param type Type of identities to find.
   * @param isProfileLoaded Is profile loaded or not.
   * @return The space identities.
   * @LevelAPI Platform 
   */
  ListAccess<Identity> getSpaceIdentityByProfileFilter(Space space, ProfileFilter profileFilter, SpaceMemberFilterListAccess.Type type,
                                                       boolean isProfileLoaded);
  
  /**
   * Adds an identity provider to an identity manager.
   *
   * @param identityProvider The identity provider.
   * @LevelAPI Platform 
   */
  void addIdentityProvider(IdentityProvider<?> identityProvider);

  /**
   * Removes a specific identity provider.
   *
   * @param identityProvider The specific identity provider.
   * @LevelAPI Platform 
   * @since 1.2.0-GA
   */
  void removeIdentityProvider(IdentityProvider<?> identityProvider);

  /**
   * Registers a profile listener plugin by an external component plugin mechanism. For example:
   * <p></p>
   * <pre>
   *  &lt;external-component-plugins&gt;
   *    &lt;target-component&gt;org.exoplatform.social.core.manager.IdentityManager&lt;/target-component&gt;
   *    &lt;component-plugin&gt;
   *      &lt;name&gt;ProfileUpdatesPublisher&lt;/name&gt;
   *      &lt;set-method&gt;registerProfileListener&lt;/set-method&gt;
   *      &lt;type&gt;org.exoplatform.social.core.application.ProfileUpdatesPublisher&lt;/type&gt;
   *    &lt;/component-plugin&gt;
   *  &lt;/external-component-plugins&gt;
   *</pre>
   *
   * @param profileListenerPlugin The profile listener plugin.
   * @LevelAPI Platform 
   * @since 1.2.0-GA
   */
  void registerProfileListener(ProfileListenerPlugin profileListenerPlugin);

  /**
   * Registers one or more identity providers through
   * {@link IdentityProviderPlugin}.
   *
   * @param plugin The identity provider plugin.
   * @LevelAPI Platform 
   */
  void registerIdentityProviders(IdentityProviderPlugin plugin);
  
  /**
   * Process status of Identity when enable/disable user.
   * 
   * @param remoteId The user remote id
   * @param isEnable true if the user is enable, false if not
   * @LevelAPI Platform
   * @since 4.1.x
   */
  void processEnabledIdentity(String remoteId, boolean isEnable);

  /**
   * Gets an identity by Id and also loads his profile.
   *
   * @param id Id of a Social identity (such as {@link GlobalId}) or a raw identity (such as {@link Identity#getId()}).
   * @return Null if nothing is found, or the Identity object.
   * @see #getIdentity(String, boolean)
   * @LevelAPI Provisional
   */
  Identity getIdentity(String id);

  /**
   * Gets an identity by a remote Id.
   *
   * @param providerId Id of the provider.
   * @param remoteId The remote Id.
   * @return The identity.
   * @LevelAPI Provisional
   */
  Identity getOrCreateIdentity(String providerId, String remoteId);

  /**
   * Gets identities by a profile filter.
   *
   * @param providerId Id of the provider.
   * @param profileFilter The profile filter.
   * @return The identities.
   * @throws Exception the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 4.0.x.
   */
  List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter) throws Exception;

  /**
   * Gets identities by a profile filter.
   *
   * @param providerId Id of the provider.
   * @param profileFilter The profile filter.
   * @param offset The starting point from which the identities are got.
   * @param limit The limitation of identities.
   * @return The identities.
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 4.0.x.
   */
  List<Identity> getIdentitiesByProfileFilter(String providerId,
                                              ProfileFilter profileFilter,
                                              long offset,
                                              long limit) throws Exception;

  /**
   * Gets identities by the profile filter.
   *
   * @param profileFilter The profile filter.
   * @return The identities.
   * @throws Exception the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 4.0.x.
   */
  List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter) throws Exception;

  /**
   * Gets identities by the profile filter.
   *
   * @param profileFilter The profile filter.
   * @param offset The starting point from which the identities are got.
   * @param limit The limitation of identities.
   * @return The identities by the profile filter.
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 4.0.x.
   */
  List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter, long offset, long limit) throws Exception;

  /**
   * Gets identities by the alphabetical filter.
   *
   * @param providerId Id of the provider.
   * @param profileFilter The profile filter.
   * @return The identities.
   * @throws Exception the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 4.0.x.
   */
  List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFilter profileFilter) throws Exception;

  /**
   * Gets identities by the alphabetical filter with offset and limit points.
   *
   * @param providerId Id of the provider.
   * @param profileFilter The profile filter.
   * @param offset The starting point from which identities are got.
   * @param limit The limitation of identities.
   * @return The identities.
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 4.0.x.
   */
  List<Identity> getIdentitiesFilterByAlphaBet(String providerId,
                                               ProfileFilter profileFilter,
                                               long offset,
                                               long limit) throws Exception;

  /**
   * Gets identities by the alphabetical filter.
   *
   * @param profileFilter The profile filter.
   * @return The identities.
   * @throws Exception the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 4.0.x.
   */
  List<Identity> getIdentitiesFilterByAlphaBet(ProfileFilter profileFilter) throws Exception;

  /**
   * Gets an identity from the provider. To make sure to get information
   * from DB, use {@link #getOrCreateIdentity(String, String, boolean)}.
   *
   * @param providerId Id of the provider.
   * @param remoteId The remote Id.
   * @param loadProfile Load the identity profile or not.
   * @return The identity.
   * @LevelAPI Provisional
   * @deprecated Use {@link #getOrCreateIdentity(String, String, boolean)} instead.
   *             Will be removed by 4.0.x.
   */
  Identity getIdentity(String providerId, String remoteId, boolean loadProfile);

  /**
   * Gets a count of identities.
   * 
   * @return The count of identities.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  long getIdentitiesCount(String providerId);
  
  /**
   * Checks if an identity has already existed or not.
   *
   * @param providerId Id of the provider.
   * @param remoteId The remote Id.
   * @return "True" if the checked identity has already existed. Otherwise, it returns "false".
   * @LevelAPI Provisional
   */
  boolean identityExisted(String providerId, String remoteId);

  /**
   * Saves an identity.
   *
   * @param identity The identity to be saved.
   * @LevelAPI Provisional
   * @deprecated Use {@link #updateIdentity(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated(forRemoval = true, since = "1.4.0")
  default void saveIdentity(Identity identity) {
    updateIdentity(identity);
  }

  /**
   * Saves a profile.
   *
   * @param profile The profile to be saved.
   * @LevelAPI Provisional
   * @deprecated Use {@link #updateProfile(org.exoplatform.social.core.identity.model.Profile)}  instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated(forRemoval = true, since = "1.4.0")
  default void saveProfile(Profile profile) {
    updateProfile(profile);
  }

  /**
   * Gets connections of an identity.
   *
   * @param ownerIdentity The identity.
   * @return The list of identities.
   * @throws Exception
   * @since 1.1.1
   * @LevelAPI Provisional
   * @deprecated Use {@link #getConnectionsWithListAccess(org.exoplatform.social.core.identity.model.Identity)}
   *             instead. Will be removed by 4.0.x.
   */
  List<Identity> getConnections(Identity ownerIdentity) throws Exception;

  /**
   * Gets the identity storage.
   *
   * @return The identity storage.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  IdentityStorage getIdentityStorage();

  /**
   * Gets the identity storage.
   *
   * @return The identity storage.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  IdentityStorage getStorage();

  /**
   * Registers a profile listener.
   *
   * @param listener The profile listener to be registered.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  void registerProfileListener(ProfileListener listener);

  /**
   * Unregisters a profile listener.
   *
   * @param listener The listener to be unregistered.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  void unregisterProfileListener(ProfileListener listener);

  /**
   * Registers a profile listener component plugin.
   *
   * @param plugin The plugin to be registered.
   * @LevelAPI Provisional
   * @deprecated Use {@link #registerProfileListener(org.exoplatform.social.core.profile.ProfileListenerPlugin)}
   *             instead. Will be removed by 4.0.x.
   */
  void addProfileListener(ProfileListenerPlugin plugin);

  /**
   * Sorts a list of user identities using a field
   * 
   * @param identityRemoteIds
   * @param sortField
   * @return
   * @deprecated use {@link #sortIdentities(List, String, String, boolean)}
   */
  @Deprecated
  default List<String> sortIdentities(List<String> identityRemoteIds, String sortField) {
    Sorting defaultSorting = getDefaultSorting();
    return sortIdentities(identityRemoteIds,
                          defaultSorting.sortBy.getFieldName(),
                          defaultSorting.orderBy.name());
  }

  /**
   * Sorts a list of user identities using a field
   *
   * @param identityRemoteIds
   * @param sortField
   * @param sortDirection
   * @return {@link List} of userNames sorted by sortField
   */
  default List<String> sortIdentities(List<String> identityRemoteIds,
                                      String sortField,
                                      String sortDirection) {
    // No sorting to apply
    return identityRemoteIds;
  }

  /**
   * Sorts a list of user identities using a field. Additionally, if
   * filterDisabled is equal to true, only enabled users will be returned
   * 
   * @param identityRemoteIds
   * @param sortField
   * @param sortDirection
   * @param filterDisabled
   * @return
   */
  default List<String> sortIdentities(List<String> identityRemoteIds,
                                      String sortField,
                                      String sortDirection,
                                      boolean filterDisabled) {
    // No sorting to apply
    return identityRemoteIds;
  }

  /**
   * @return default sorting to apply when listing
   */
  default Sorting getDefaultSorting() {
    return DEFAULT_SORTING;
  }

  /**
   * @return banner and avatar of identities Max upload size in MB
   */
  default int getImageUploadLimit() {
    return IdentityStorage.DEFAULT_UPLOAD_IMAGE_LIMIT;
  }

  /**
   * Retrieves the identity of a given space identified by its prettyName
   * 
   * @param spacePrettyName {@link Space} prettyName
   * @return {@link Identity} if found, else null
   */
  default Identity getOrCreateSpaceIdentity(String spacePrettyName) {
    return getOrCreateIdentity(ActivityStream.SPACE_PROVIDER_ID, spacePrettyName);
  }

  /**
   * Retrieves the identity of a given user identified by his username/login
   * 
   * @param username login identifier of the user
   * @return {@link Identity} if found, else null
   */
  default Identity getOrCreateUserIdentity(String username) {
    return getOrCreateIdentity(ActivityStream.ORGANIZATION_PROVIDER_ID, username);
  }

  /**
   * Retrieves user avatar file by a given identity
   *
   * @param identity User social identity
   * @return {@link FileItem}
   */
  default FileItem getAvatarFile(Identity identity) {
    throw new UnsupportedOperationException();
  }

}
