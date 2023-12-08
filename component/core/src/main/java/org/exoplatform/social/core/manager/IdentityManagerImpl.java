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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.IdentityProviderPlugin;
import org.exoplatform.social.core.identity.ProfileFilterListAccess;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.*;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * Class IdentityManagerImpl implements IdentityManager without caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class IdentityManagerImpl implements IdentityManager {
  /** Logger */
  private static final Log                   LOG               = ExoLogger.getExoLogger(IdentityManagerImpl.class);
  
  /** The offset for list access loading. */
  private static final int                   OFFSET = 0;
  
  /** The limit for list access loading. */
  private static final int                   LIMIT = 200;

  /** The identity providers */
  protected Map<String, IdentityProvider<?>> identityProviders       = new HashMap<>();

  /** The identity providers */
  protected Map<String, ProfileListener>     profileListeners        = new HashMap<>();

  /** The activityStorage */
  protected IdentityStorage                  identityStorage;

  /** The relationship manager */
  protected RelationshipManager              relationshipManager;

  /** The profile Property Settings Service */
  protected ProfilePropertyService profilePropertyService;

  /** lifecycle for profile */
  protected ProfileLifeCycle                 profileLifeCycle  = new ProfileLifeCycle();

  private Sorting                            defaultSorting          = DEFAULT_SORTING;

  private int                                imageUploadLimit        = IdentityStorage.DEFAULT_UPLOAD_IMAGE_LIMIT;

  /**
   * Instantiates a new identity manager.
   *
   * @param identityStorage
   * @param defaultIdentityProvider the built-in default identity provider to use
   *          when no other provider matches
   */
  public IdentityManagerImpl(IdentityStorage identityStorage, // NOSONAR
                             IdentityProvider<?> defaultIdentityProvider,
                             ProfilePropertyService profilePropertyService,
                             InitParams initParams) {
    this.identityStorage = identityStorage;
    this.profilePropertyService = profilePropertyService;
    this.addIdentityProvider(defaultIdentityProvider);
    if (initParams != null) {
      String sortFieldName = this.defaultSorting.sortBy.getFieldName();
      if (initParams.containsKey("sort.field.name")) {
        sortFieldName = initParams.getValueParam("sort.field.name").getValue();
      }
      String sortDirection = this.defaultSorting.orderBy.name();
      if (initParams.containsKey("sort.order.direction")) {
        sortDirection = initParams.getValueParam("sort.order.direction").getValue();
      }
      if (initParams.containsKey("upload.limit.size")) {
        String uploadLimit = initParams.getValueParam("upload.limit.size").getValue();
        if (StringUtils.isNotBlank(uploadLimit)) {
          this.imageUploadLimit = Integer.parseInt(uploadLimit.trim());
          this.identityStorage.setImageUploadLimit(this.imageUploadLimit);
        }
      }
      Sorting configuredSorting = Sorting.valueOf(sortFieldName, sortDirection);
      if (configuredSorting != null) {
        this.defaultSorting = configuredSorting;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getImageUploadLimit() {
    return imageUploadLimit;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Sorting getDefaultSorting() {
    return defaultSorting;
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getLastIdentities(int limit) {
    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setSorting(new Sorting(Sorting.SortBy.DATE, Sorting.OrderBy.DESC));
    return identityStorage.getIdentitiesForUnifiedSearch(OrganizationIdentityProvider.NAME, profileFilter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getConnectionsWithListAccess(Identity identity) {
    return getRelationshipManager().getConnections(identity);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter,
                                                           boolean forceLoadProfile) {
    if (profileFilter == null) {
      profileFilter = new ProfileFilter();
    }
    if (profileFilter.isSortingEmpty()) {
      profileFilter.setSorting(this.defaultSorting);
    }
    return (new ProfileFilterListAccess(identityStorage, providerId, profileFilter, forceLoadProfile));
  }

  
  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getSpaceIdentityByProfileFilter(Space space, ProfileFilter profileFilter, SpaceMemberFilterListAccess.Type type,
                                                           boolean forceLoadProfile) {
    return (new SpaceMemberFilterListAccess(identityStorage, space, profileFilter, type));
  }
  
  /**
   * {@inheritDoc}
   */
  public Profile getProfile(Identity identity) {
    Profile profile = identity.getProfile();
    if (profile.getId() == null) {
      profile = identityStorage.loadProfile(profile);
      identity.setProfile(profile);
    }
    return profile;
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream getAvatarInputStream(Identity identity) throws IOException {
    if (identity == null) {
      return null;
    }
    return identityStorage.getAvatarInputStreamById(identity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FileItem getAvatarFile(Identity identity) {
    if (identity == null) {
      return null;
    }
    return identityStorage.getAvatarFile(identity);
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getBannerInputStream(Identity identity) throws IOException {
    if (identity == null) {
      return null;
    }
    return identityStorage.getBannerInputStreamById(identity);
  }

  /**
   * {@inheritDoc}
   */
  public void updateProfile(Profile existingProfile) {
    updateProfile(existingProfile, false);
  }

  @Override
  public void updateProfile(Profile profile, boolean broadcastChanges) {
    updateProfile(profile, null, broadcastChanges);
  }

  @Override
  public void updateProfile(Profile specificProfile, String modifierUsername, boolean broadcastChanges) {
    if (broadcastChanges) {
      detectChanges(specificProfile);
    }
    identityStorage.updateProfile(specificProfile);
    broadcastUpdateProfileEvent(specificProfile, modifierUsername);
    this.getIdentityProvider(specificProfile.getIdentity().getProviderId()).onUpdateProfile(specificProfile);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerProfileListener(ProfileListenerPlugin profileListenerPlugin) {
    profileLifeCycle.addListener(profileListenerPlugin);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Identity updateIdentity(Identity identity) {
    return identityStorage.updateIdentity(identity);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void addIdentityProvider(IdentityProvider<?> idProvider) {
    if (idProvider != null) {
      LOG.debug("Registering identity provider for " + idProvider.getName() + ": " + idProvider);
      identityProviders.put(idProvider.getName(), idProvider);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeIdentityProvider(IdentityProvider<?> identityProvider) {
    if (identityProvider != null) {
      LOG.debug("Removing identity provider for " + identityProvider.getName() + ": " + identityProvider);
      identityProviders.remove(identityProvider.getName());
    }
  }

  @Override
  public void deleteIdentity(Identity identity) {
    if (identity.getId() == null) {
      LOG.warn("identity.getId() must not be null of [" + identity + "]");
      return;
    }
    identityStorage.deleteIdentity(identity);
  }

  @Override
  public void hardDeleteIdentity(Identity identity) {
    if (identity.getId() == null) {
      LOG.warn("identity.getId() must not be null of [" + identity + "]");
      return;
    }
    identityStorage.hardDeleteIdentity(identity);
  }

  @Override
  public List<Identity> getConnections(Identity ownerIdentity) throws Exception {
    return Arrays.asList(getConnectionsWithListAccess(ownerIdentity).load(OFFSET, LIMIT));
  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load(OFFSET, LIMIT));
  }

  @Override
  public ListAccess<Identity> getIdentitiesForUnifiedSearch(String providerId,
                                                            ProfileFilter profileFilter) {
    return (new ProfileFilterListAccess(identityStorage, providerId, profileFilter, true, ProfileFilterListAccess.Type.UNIFIED_SEARCH));
  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(String providerId,
                                                     ProfileFilter profileFilter,
                                                     long offset,
                                                     long limit) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load((int)offset, (int)limit));
  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(null, profileFilter, false).load(OFFSET, LIMIT));
  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter,
                                                     long offset,
                                                     long limit) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(null, profileFilter, false).load((int) offset, (int)limit));
  }

  @Override
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load(OFFSET, LIMIT));
  }

  @Override
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId,
                                                      ProfileFilter profileFilter,
                                                      long offset,
                                                      long limit) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load((int)offset, (int)limit));
  }

  @Override
  public List<Identity> getIdentitiesFilterByAlphaBet(ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(null, profileFilter, false).load(OFFSET, LIMIT));
  }

  @Override
  public Identity getIdentity(String id) {
    return getIdentity(id, true);
  }

  @Override
  public long getIdentitiesCount(String providerId) {
    return identityStorage.getIdentitiesCount(providerId);
  }

  @Override
  public Identity getIdentity(String identityId, boolean forceLoadOrReloadProfile) {
    return identityStorage.findIdentityById(identityId);
  }

  @Override
  public Identity getIdentity(String providerId, String remoteId, boolean loadProfile) {
    return getOrCreateIdentity(providerId, remoteId, loadProfile);
  }

  @Override
  public Identity getOrCreateIdentity(String providerId, String remoteId) {
    return getOrCreateIdentity(providerId, remoteId, true);
  }

  @Override
  public Identity getOrCreateIdentity(String providerId, String remoteId, boolean forceLoadOrReloadProfile) {
    Identity result = identityStorage.findIdentity(providerId, remoteId);
    if (result == null || result.isDeleted()) {
      IdentityProvider<?> identityProvider = this.getIdentityProvider(providerId);
      Identity identityFoundByRemoteProvider = identityProvider.getIdentityByRemoteId(remoteId);
      if (identityFoundByRemoteProvider != null) {
        // identity is valid for provider, but no yet
        // referenced in activityStorage
        if (result == null) {
          identityStorage.saveIdentity(identityFoundByRemoteProvider);
          identityStorage.saveProfile(identityFoundByRemoteProvider.getProfile());
          identityProvider.onSaveIdentity(identityFoundByRemoteProvider);
        } else {
          result.setDeleted(false);
          identityStorage.updateIdentity(result);
        }
        result = identityStorage.findIdentity(providerId, remoteId);

        // case of create new space or user, an event will be called only in
        // case of create user
        if (result.isUser()) {
          profileLifeCycle.createProfile(result.getProfile());
        }
      } else {
        // Not found in provider, so return null
        return result;
      }
    }
    return result;
  }

  @Override
  public boolean identityExisted(String providerId, String remoteId) {
    IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
    return identityProvider.getIdentityByRemoteId(remoteId) != null;
  }

  @Override
  public void registerIdentityProviders(IdentityProviderPlugin plugin) {
    List<IdentityProvider<?>> pluginProviders = plugin.getProviders();
    if (pluginProviders != null) {
      for (IdentityProvider<?> identityProvider : pluginProviders) {
        this.addIdentityProvider(identityProvider);
      }
    }
  }

  /**
   * Gets identityProvider.
   *
   * @param providerId
   * @return
   */
  public <T> IdentityProvider<T> getIdentityProvider(String providerId) {
    @SuppressWarnings("unchecked")
    IdentityProvider<T> provider = (IdentityProvider<T>) identityProviders.get(providerId);
    if (provider == null) {
      throw new IllegalStateException("No suitable identity provider exists for " + providerId);
    }
    return provider;
  }

  @Override
  public IdentityStorage getIdentityStorage() {
    return this.identityStorage;
  }

  @Override
  public void addProfileListener(ProfileListenerPlugin plugin) {
    registerProfileListener(plugin);
  }

  @Override
  public void registerProfileListener(ProfileListener listener) {
    profileLifeCycle.addListener(listener);
  }

  @Override
  public void unregisterProfileListener(ProfileListener listener) {
    profileLifeCycle.removeListener(listener);
  }

  @Override
  public void processEnabledIdentity(String remoteId, boolean isEnable) {
    Identity identity = getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, false);
    identityStorage.processEnabledIdentity(identity, isEnable);
  }

  @Override
  public List<String> sortIdentities(List<String> identityRemoteIds,
                                     String sortField,
                                     String sortDirection,
                                     boolean filterDisabled) {
    if (StringUtils.isBlank(sortField)) {
      sortField = this.getDefaultSorting().sortBy.getFieldName();
    }
    if (StringUtils.isBlank(sortDirection)) {
      sortDirection = this.getDefaultSorting().orderBy.name();
    }
    return identityStorage.sortIdentities(identityRemoteIds, sortField, sortDirection, filterDisabled);
  }

  @Override
  public List<String> sortIdentities(List<String> identityRemoteIds,
                                     String sortField,
                                     String sortDirection) {
    return sortIdentities(identityRemoteIds, sortField, sortDirection, true);
  }

  @Override
  public IdentityStorage getStorage() {
    return this.identityStorage;
  }

  public void setIdentityStorage(IdentityStorage identityStorage) {
    this.identityStorage = identityStorage;
  }

  public RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = PortalContainer.getInstance()
                                           .getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }

  private void broadcastUpdateProfileEvent(Profile profile, String modifierUsername) {
    for (UpdateType type : profile.getListUpdateTypes()) {
      type.updateActivity(profileLifeCycle, profile, modifierUsername);
    }
  }

  private void detectChanges(Profile specificProfile) { // NOSONAR
    if (specificProfile.getId() != null) {
      String identityId = specificProfile.getId();
      Identity identity = getIdentity(identityId);
      if (identity == null) {
        return;
      }

      Profile existingProfile = identity.getProfile();
      if (existingProfile == null) {
        return;
      }

      List<Profile.UpdateType> list = new ArrayList<>();
      if (UserProfileComparator.hasChanged(specificProfile, existingProfile, Profile.ABOUT_ME)) {
        list.add(Profile.UpdateType.ABOUT_ME);
      }
      if (UserProfileComparator.hasChanged(specificProfile,
              existingProfile,
              profilePropertyService.getPropertySettingNames())) {
        list.add(Profile.UpdateType.CONTACT);
      }
      if (UserProfileComparator.hasChanged(specificProfile, existingProfile, Profile.EXPERIENCES)) {
        list.add(Profile.UpdateType.EXPERIENCES);
      }
      if (specificProfile.getProperty(Profile.AVATAR) != null) {
        list.add(UpdateType.AVATAR);
      }
      if (specificProfile.getProperty(Profile.BANNER) != null){
        list.add(UpdateType.BANNER);
      }
      if (UserProfileComparator.hasChanged(specificProfile, existingProfile, Profile.LAST_LOGIN_TIME, Profile.ENROLLMENT_DATE)){
        list.add(UpdateType.TECHNICAL);
      }
      specificProfile.setListUpdateTypes(list);
    }
  }

}
