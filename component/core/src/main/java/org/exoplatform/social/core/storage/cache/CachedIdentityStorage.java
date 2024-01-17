/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.AttachedActivityType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.ListIdentitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListSpaceMembersKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceKey;
import org.exoplatform.social.core.storage.cache.selector.IdentityCacheSelector;

/**
 * Cache support for IdentityStorage.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedIdentityStorage implements IdentityStorage {

  private static final Log                                                                                LOG =
                                                                                                              ExoLogger.getLogger(CachedIdentityStorage.class);

  private final FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>>                   identityCache;

  private final FutureExoCache<IdentityCompositeKey, IdentityKey, ServiceContext<IdentityKey>>            identityIndexCache;

  private final FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>>                     profileCache;

  private final FutureExoCache<IdentityFilterKey, IntegerData, ServiceContext<IntegerData>>               identitiesCountCache;

  private final FutureExoCache<ListIdentitiesKey, ListIdentitiesData, ServiceContext<ListIdentitiesData>> identitiesCache;

  private final FutureExoCache<IdentityKey, Long, ServiceContext<Long>>                                   profileAvatarCache;

  private final IdentityStorage                                                                           storage;

  private final SocialStorageCacheService                                                                 cacheService;

  private final FileService                                                                               fileService;

  private CachedRelationshipStorage                                                                       cachedRelationshipStorage;

  public CachedIdentityStorage(final RDBMSIdentityStorageImpl storage,
                               final SocialStorageCacheService cacheService,
                               final FileService fileService) {

    //
    this.storage = storage;
    this.cacheService = cacheService;
    this.fileService = fileService;

    //
    this.identityCache = CacheType.IDENTITY.createFutureCache(cacheService.getIdentityCache());
    this.identityIndexCache = CacheType.IDENTITY_INDEX.createFutureCache(cacheService.getIdentityIndexCache());
    this.profileCache = CacheType.PROFILE.createFutureCache(cacheService.getProfileCache());
    this.identitiesCountCache = CacheType.IDENTITIES_COUNT.createFutureCache(cacheService.getCountIdentitiesCache());
    this.identitiesCache = CacheType.IDENTITIES.createFutureCache(cacheService.getIdentitiesCache());
    this.profileAvatarCache = CacheType.PROFILE_AVATAR.createFutureCache(cacheService.getProfileAvatarCache());
  }

  @Override
  public void saveIdentity(final Identity identity) throws IdentityStorageException {
    try {
      storage.saveIdentity(identity);
    } finally {
      clearIdentityCache(identity, true, true);
    }
  }

  @Override
  public Identity updateIdentity(Identity identity) throws IdentityStorageException {
    try {
      return storage.updateIdentity(identity);
    } finally {
      clearIdentityCache(identity, false, true);
    }
  }

  @Override
  public void updateIdentityMembership(final String remoteId) throws IdentityStorageException {
    clearUserIdentitiesCache();
  }

  @Override
  public String findIdentityId(final String providerId, final String remoteId) {
    IdentityCompositeKey key = new IdentityCompositeKey(providerId, remoteId);
    IdentityKey keyById = identityIndexCache.get(() -> {
      String identityId = storage.findIdentityId(providerId, remoteId);
      return new IdentityKey(identityId);
    }, key);
    return keyById != null && keyById.getId() != null ? keyById.getId() : null;
  }

  @Override
  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {
    String identityId = findIdentityId(providerId, remoteId);
    if (identityId != null) {
      return findIdentityById(identityId);
    } else {
      return null;
    }
  }

  @Override
  public Identity findIdentityById(String identityId) throws IdentityStorageException {
    Identity identity = identityCache.get(() -> new IdentityData(storage.findIdentityById(identityId)), new IdentityKey(identityId))
                                     .build();
    if (identity != null) {
      identity.setProfile(null);
      identity.setProfileLoader(() -> loadProfile(new Profile(identity)));
    }
    return identity;
  }

  @Override
  public void deleteIdentity(final Identity identity) throws IdentityStorageException {
    try {
      storage.deleteIdentity(identity);
    } finally {
      clearIdentityCache(identity, true, true);
    }
  }

  @Override
  public void hardDeleteIdentity(final Identity identity) throws IdentityStorageException {
    try {
      storage.hardDeleteIdentity(identity);
    } finally {
      clearIdentityCache(identity, true, true);
    }
  }

  @Override
  public Profile loadProfile(final Profile profile) throws IdentityStorageException {
    Identity identity = profile.getIdentity();
    ProfileData profileData = profileCache.get(() -> {
      Profile loadedProfile = storage.loadProfile(profile);
      if (loadedProfile == null) {
        LOG.warn("Null profile for identity: " + identity.getRemoteId());
        return ProfileData.NULL_OBJECT;
      } else {
        return new ProfileData(loadedProfile);
      }
    }, new IdentityKey(identity.getId()));

    if (profileData == null || profileData.getProfileId() == null) {
      identity.setProfile(null);
      return profile;
    } else {
      Profile loadedProfile = profileData.build();
      loadedProfile.setIdentity(identity);
      identity.setProfile(loadedProfile);
      return loadedProfile;
    }
  }

  @Override
  public void saveProfile(final Profile profile) throws IdentityStorageException {
    try {
      storage.saveProfile(profile);
    } finally {
      clearIdentityCache(profile.getIdentity(), true, true);
    }
  }

  @Override
  public void updateProfile(final Profile profile) throws IdentityStorageException {
    try {
      storage.updateProfile(profile);
    } finally {
      clearIdentityCache(profile.getIdentity(), false, true);
    }
  }

  @Override
  public int getIdentitiesCount(final String providerId) throws IdentityStorageException {
    return storage.getIdentitiesCount(providerId);
  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(final String providerId,
                                                     final ProfileFilter profileFilter,
                                                     final long offset,
                                                     final long limit,
                                                     final boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);
    ListIdentitiesKey listKey = new ListIdentitiesKey(key, offset, limit);
    ListIdentitiesData keys = identitiesCache.get(() -> {
      List<Identity> got = storage.getIdentitiesByProfileFilter(providerId,
                                                                profileFilter,
                                                                offset,
                                                                limit,
                                                                forceLoadOrReloadProfile);
      return buildIds(got);
    }, listKey);
    return buildIdentities(keys);
  }

  @Override
  public List<Identity> getIdentitiesForMentions(final String providerId, final ProfileFilter profileFilter, final org.exoplatform.social.core.relationship.model.Relationship.Type type,
      final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    // Avoid using cache when requesting indexes
    return storage.getIdentitiesForMentions(providerId, profileFilter, type, offset, limit, forceLoadOrReloadProfile);
  }

  @Override
  public int getIdentitiesForMentionsCount(String providerId,
                                           ProfileFilter profileFilter,
                                           org.exoplatform.social.core.relationship.model.Relationship.Type type) throws IdentityStorageException {
    return storage.getIdentitiesForMentionsCount(providerId, profileFilter, type);
  }

  @Override
  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter) throws IdentityStorageException {
    return identitiesCountCache.get(() -> new IntegerData(storage.getIdentitiesByProfileFilterCount(providerId, profileFilter)),
                                    new IdentityFilterKey(providerId, profileFilter))
                               .build();
  }

  public List<Identity> getSpaceMemberIdentitiesByProfileFilter(final Space space,
                                                                final ProfileFilter profileFilter,
                                                                final SpaceMemberFilterListAccess.Type type,
                                                                final long offset,
                                                                final long limit) throws IdentityStorageException {
    ListIdentitiesData keys = identitiesCache.get(() -> {
      List<Identity> got = storage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, type, offset, limit);
      return buildIds(got);
    }, new ListSpaceMembersKey(new SpaceKey(space.getId()),
                               new IdentityFilterKey(SpaceIdentityProvider.NAME, profileFilter),
                               type,
                               offset,
                               limit));
    return buildIdentities(keys);
  }

  public void updateProfileActivityId(Identity identity, String activityId, AttachedActivityType type) {
    storage.updateProfileActivityId(identity, activityId, type);
    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    profileCache.remove(key);
    clearUserIdentitiesCache();
  }

  public String getProfileActivityId(Profile profile, AttachedActivityType type) {
    return storage.getProfileActivityId(profile, type);
  }

  public List<Identity> getIdentitiesForUnifiedSearch(final String providerId,
                                                      final ProfileFilter profileFilter,
                                                      final long offset,
                                                      final long limit) throws IdentityStorageException {
    // Avoid using cache when requesting ES
    return storage.getIdentitiesForUnifiedSearch(providerId, profileFilter, offset, limit);
  }

  public void processEnabledIdentity(Identity identity, boolean isEnable) {
    try {
      storage.processEnabledIdentity(identity, isEnable);
    } finally {
      clearIdentityCache(identity, false, true);
      getCachedRelationshipStorage().clearAllRelationshipCache();
    }
  }
  
  @Override
  public List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId, int offset, int limit) {
    return storage.getIdentitiesWithRelationships(identityId, offset, limit);
  }

  @Override
  public List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId,
                                                                       String sortFieldName,
                                                                       String sortDirection,
                                                                       int offset,
                                                                       int limit) {
    return storage.getIdentitiesWithRelationships(identityId,
                                                  sortFieldName,
                                                  sortDirection,
                                                  offset,
                                                  limit);
  }

  @Override
  public int countIdentitiesWithRelationships(String identityId) throws Exception {
    return storage.countIdentitiesWithRelationships(identityId);
  }
  
  @Override
  public InputStream getAvatarInputStreamById(Identity identity) throws IOException {
    return storage.getAvatarInputStreamById(identity);
  }

  @Override
  public FileItem getAvatarFile(Identity identity) {
    Long avatarId = profileAvatarCache.get(() -> {
      FileItem avatarFile = storage.getAvatarFile(identity);
      if (avatarFile == null) {
        return null;
      } else {
        return avatarFile.getFileInfo().getId();
      }
    }, new IdentityKey(identity.getId()));
    try {
      return avatarId == null ? null : fileService.getFile(avatarId);
    } catch (FileStorageException e) {
      LOG.error("Error reading file with id " + avatarId, e);
      return null;
    }
  }

  @Override
  public InputStream getBannerInputStreamById(Identity identity) throws IOException {
    return storage.getBannerInputStreamById(identity);
  }

  @Override
  public int countSpaceMemberIdentitiesByProfileFilter(Space space, ProfileFilter profileFilter, SpaceMemberFilterListAccess.Type type) {
    return storage.countSpaceMemberIdentitiesByProfileFilter(space, profileFilter, type);
  }

  @Override
  public List<String> getIdentityIds(String providerId,
                                     String sortField,
                                     String sortDirection,
                                     boolean isEnabled,
                                     String userType,
                                     Boolean isConnected,
                                     String enrollmentStatus,
                                     long offset,
                                     long limit) {
    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setEnabled(isEnabled);
    profileFilter.setUserType(userType);
    profileFilter.setConnected(isConnected);
    profileFilter.setEnrollmentStatus(enrollmentStatus);
    profileFilter.setSorting(Sorting.valueOf(sortField, sortDirection));

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);
    ListIdentitiesKey listKey = new ListIdentitiesKey(key, offset, limit);

    //
    ListIdentitiesData keys = identitiesCache.get(() -> {
      List<String> identityIds = storage.getIdentityIds(providerId,
                                                        sortField,
                                                        sortDirection,
                                                        isEnabled,
                                                        userType,
                                                        isConnected,
                                                        enrollmentStatus,
                                                        offset,
                                                        limit);
      return new ListIdentitiesData(identityIds.stream().map(IdentityKey::new).toList());
    }, listKey);
    return keys.getIds().stream().map(IdentityKey::getId).toList();
  }

  @Override
  public List<Identity> getIdentities(String providerId,
                                      String sortField,
                                      String sortDirection,
                                      boolean isEnabled,
                                      String userType,
                                      Boolean isConnected,
                                      String enrollmentStatus,
                                      long offset,
                                      long limit) {
    List<String> identityIds = getIdentityIds(providerId, sortField, sortDirection, isEnabled, userType, isConnected, enrollmentStatus, offset, limit);
    return buildIdentities(identityIds);
  }

  @Override
  public List<Identity> getIdentities(String providerId, long offset, long limit) {
    return getIdentities(providerId, null, null, true, null, null, null, offset, limit);
  }

  @Override
  public List<String> sortIdentities(List<String> identityRemoteIds,
                                     String sortField,
                                     String sortDirection) {
    return storage.sortIdentities(identityRemoteIds, sortField, sortDirection);
  }

  @Override
  public void setImageUploadLimit(int imageUploadLimit) {
    storage.setImageUploadLimit(imageUploadLimit);
  }

  public IdentityStorage getStorage() {
    return storage;
  }

  private CachedRelationshipStorage getCachedRelationshipStorage() {
    if (cachedRelationshipStorage == null) {
      cachedRelationshipStorage = PortalContainer.getInstance().getComponentInstanceOfType(CachedRelationshipStorage.class);
    }
    return cachedRelationshipStorage;
  }

  private ListIdentitiesData buildIds(List<Identity> identities) {
    return new ListIdentitiesData(identities.stream().map(IdentityKey::new).toList());
  }

  private List<Identity> buildIdentities(ListIdentitiesData data) {
    return data.getIds()
        .stream()
        .map(k -> findIdentityById(k.getId()))
        .toList();
  }

  private List<Identity> buildIdentities(List<String> ids) {
    return ids.stream()
              .map(this::findIdentityById)
              .toList();
  }

  void clearUserIdentitiesCache() {
    try {
      cacheService.getIdentitiesCache().select(new IdentityCacheSelector(OrganizationIdentityProvider.NAME));
      cacheService.getCountIdentitiesCache().select(new IdentityCacheSelector(OrganizationIdentityProvider.NAME));
    } catch (Exception e) {
      LOG.error("Error when clearing cache", e);
    }
  }

  /**
   * Clear identity cache.
   * 
   * @param identity
   * @param oldRemoteId
   * @since 1.2.8
   */
  public void clearIdentityCached(Identity identity, String oldRemoteId) {
    IdentityKey key = new IdentityKey(identity.getId());
    identityCache.remove(key);
    identityIndexCache.remove(new IdentityCompositeKey(identity.getProviderId(), oldRemoteId));
    profileCache.remove(key);
    profileAvatarCache.remove(key);
    clearUserIdentitiesCache();
  }

  /**
   * Clear the identity cache.
   *
   * @param remoteId
   * @param providerId
   * @param clearList
   */
  public void clearIdentityCache(String providerId, String remoteId, boolean clearList) {
    Identity identity = findIdentity(providerId, remoteId);
    if (identity == null) {
      IdentityCompositeKey compositeKey = new IdentityCompositeKey(providerId, remoteId);
      IdentityKey identityKey = identityIndexCache.get(compositeKey);
      if (identityKey != null) {
        identityIndexCache.remove(compositeKey);
        identityCache.remove(identityKey);
        profileCache.remove(identityKey);
        profileAvatarCache.remove(identityKey);
      }
    } else {
      IdentityKey key = new IdentityKey(identity.getId());
      identityCache.remove(key);
      profileCache.remove(key);
      profileAvatarCache.remove(key);
      identityIndexCache.remove(new IdentityCompositeKey(identity.getProviderId(), identity.getRemoteId()));
    }
    if (clearList) {
      clearUserIdentitiesCache();
    }
  }

  private void clearIdentityCache(Identity identity, boolean removeCachedId, boolean clearList) {
    IdentityKey key = new IdentityKey(identity.getId());
    identityCache.remove(key);
    profileCache.remove(key);
    profileAvatarCache.remove(key);
    if (removeCachedId) {
      identityIndexCache.remove(new IdentityCompositeKey(identity.getProviderId(), identity.getRemoteId()));
    }
    if (clearList) {
      clearUserIdentitiesCache();
    }
  }

}
