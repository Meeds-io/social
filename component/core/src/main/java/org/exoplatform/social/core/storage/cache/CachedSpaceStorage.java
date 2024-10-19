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

import java.util.*;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSSpaceStorageImpl;
import org.exoplatform.social.core.jpa.storage.dao.*;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.*;
import org.exoplatform.social.core.storage.cache.model.key.*;
import org.exoplatform.social.core.storage.cache.selector.*;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedSpaceStorage extends RDBMSSpaceStorageImpl {

  private static final SpaceKey                                                                    EMPTY_SPACE_KEY =
                                                                                                                   new SpaceKey("0");

  /** Logger */
  private static final Log                                                                         LOG             =
                                                                                                       ExoLogger.getLogger(CachedSpaceStorage.class);

  private final ExoCache<SpaceKey, SpaceData>                                                      spaceCache;

  private final ExoCache<SpaceRefKey, SpaceKey>                                                    spaceRefCache;

  private final ExoCache<SpaceFilterKey, IntegerData>                                              spacesCountCache;

  private final ExoCache<SpaceKey, HashMap<Long, Long>>                                            spacesCountByTemplateCache;

  private final ExoCache<ListSpacesKey, ListSpacesData>                                            spacesCache;

  private final ExoCache<ListIdentitiesKey, ListIdentitiesData>                                    identitiesCache;

  private final FutureExoCache<SpaceKey, SpaceData, ServiceContext<SpaceData>>                     spaceFutureCache;

  private final FutureExoCache<SpaceRefKey, SpaceKey, ServiceContext<SpaceKey>>                    spaceRefFutureCache;

  private final FutureExoCache<SpaceFilterKey, IntegerData, ServiceContext<IntegerData>>           spacesCountFutureCache;

  private final FutureExoCache<SpaceKey, HashMap<Long, Long>, ServiceContext<HashMap<Long, Long>>> spacesCountByTemplateFutureCache;

  private final FutureExoCache<ListSpacesKey, ListSpacesData, ServiceContext<ListSpacesData>>      spacesFutureCache;

  private SocialStorageCacheService                                                                cacheService;

  private CachedActivityStorage                                                                    cachedActivityStorage;

  private CachedIdentityStorage                                                                    cachedIdentityStorage;

  public CachedSpaceStorage(SpaceDAO spaceDAO, // NOSONAR
                            SpaceMemberDAO spaceMemberDAO,
                            IdentityStorage identityStorage,
                            IdentityDAO identityDAO,
                            ActivityDAO activityDAO,
                            SpaceExternalInvitationDAO spaceExternalInvitationDAO,
                            SocialStorageCacheService cacheService,
                            FavoriteService favoriteService,
                            RemindPasswordTokenService remindPasswordTokenService) {
    super(spaceDAO,
          spaceMemberDAO,
          identityStorage,
          identityDAO,
          activityDAO,
          spaceExternalInvitationDAO,
          favoriteService,
          remindPasswordTokenService);
    this.cacheService = cacheService;

    this.spaceCache = cacheService.getSpaceCache();
    this.spaceRefCache = cacheService.getSpaceRefCache();
    this.spacesCountCache = cacheService.getSpacesCountCache();
    this.spacesCache = cacheService.getSpacesCache();
    this.identitiesCache = cacheService.getIdentitiesCache();
    this.spacesCountByTemplateCache = cacheService.getSpacesCountByTemplateCache();

    this.spaceFutureCache = CacheType.SPACE.createFutureCache(spaceCache);
    this.spaceRefFutureCache = CacheType.SPACE_REF.createFutureCache(spaceRefCache);
    this.spacesCountFutureCache = CacheType.SPACES_COUNT.createFutureCache(spacesCountCache);
    this.spacesCountByTemplateFutureCache = CacheType.SPACES_COUNT_BY_TEMPLATE.createFutureCache(spacesCountByTemplateCache);
    this.spacesFutureCache = CacheType.SPACES.createFutureCache(spacesCache);

  }

  /**
   * Build the activity list from the caches Ids.
   *
   * @param data ids
   * @return activities
   */
  private List<Space> buildSpaces(ListSpacesData data) {

    List<Space> spaces = new ArrayList<>();
    for (SpaceKey k : data.getIds()) {
      Space s = getSpaceById(k.getId());
      spaces.add(s);
    }
    return spaces;

  }

  private List<String> buildIds(ListSpacesData data) {
    if (data == null || data.getIds() == null) {
      return Collections.emptyList();
    } else {
      List<String> identityIds = new ArrayList<>();
      for (SpaceKey k : data.getIds()) {
        identityIds.add(k.getId());
      }
      return identityIds;
    }
  }

  /**
   * Get cached identity storage.
   * 
   * @return
   * @since 1.2.8
   */
  public CachedIdentityStorage getCachedIdentityStorage() {
    if (cachedIdentityStorage == null) {
      cachedIdentityStorage = PortalContainer.getInstance()
                                             .getComponentInstanceOfType(CachedIdentityStorage.class);
    }
    return cachedIdentityStorage;
  }

  /**
   * Build the ids from the space list.
   *
   * @param spaces spaces
   * @return ids
   */
  private ListSpacesData buildIds(List<Space> spaces) {
    List<SpaceKey> data = new ArrayList<>();
    for (Space s : spaces) {
      SpaceKey k = putSpaceInCacheIfNotExists(s);
      data.add(k);
    }
    return new ListSpacesData(data);
  }

  /**
   * Build the ids from the space identity id list.
   *
   * @param identities identities
   * @return identities
   */
  private ListSpacesData buildListIdentityIds(List<String> identities) {
    List<SpaceKey> data = new ArrayList<>();
    for (String identityId : identities) {
      SpaceKey k = new SpaceKey(identityId);
      data.add(k);
    }
    return new ListSpacesData(data);
  }

  @Override
  public Space getSpaceByDisplayName(final String spaceDisplayName) throws SpaceStorageException {
    SpaceRefKey refKey = new SpaceRefKey(spaceDisplayName);
    SpaceKey key = spaceRefFutureCache.get(() -> {
      Space space = CachedSpaceStorage.super.getSpaceByDisplayName(spaceDisplayName);
      if (space != null) {
        return putSpaceInCacheIfNotExists(space);
      } else {
        return SpaceKey.NULL_OBJECT;
      }
    }, refKey);
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    } else {
      return null;
    }
  }

  @Override
  public void saveSpace(final Space space, final boolean isNew) throws SpaceStorageException {

    try {
      super.saveSpace(space, isNew);
    } finally {
      spaceCache.remove(new SpaceKey(space.getId()));
      clearSpaceCache();
      clearIdentityCache();
      cleanRef(space);
    }
  }

  @Override
  public void renameSpace(Space space, String newDisplayName) throws SpaceStorageException {
    String oldDisplayName = space.getDisplayName();
    String oldUrl = Utils.cleanString(oldDisplayName);
    String oldPrettyName = space.getPrettyName();

    //
    super.renameSpace(space, newDisplayName);

    // remove identity and profile from cache
    cachedIdentityStorage = this.getCachedIdentityStorage();
    Identity identitySpace = cachedIdentityStorage.findIdentity(SpaceIdentityProvider.NAME,
                                                                space.getPrettyName());
    if (identitySpace == null) {
      identitySpace = cachedIdentityStorage.findIdentity(SpaceIdentityProvider.NAME, oldPrettyName);
    }

    if (identitySpace != null) {
      cachedIdentityStorage.clearIdentityCached(identitySpace, oldPrettyName);
    }

    // remove activities cached of a space
    cachedActivityStorage = this.getCachedActivityStorage();
    cachedActivityStorage.clearOwnerStreamCache(oldPrettyName);

    // remove space cached
    spaceCache.remove(new SpaceKey(space.getId()));
    clearSpaceCache();
    clearIdentityCache();
    cleanRef(space);
    spaceRefCache.remove(new SpaceRefKey(oldDisplayName));
    spaceRefCache.remove(new SpaceRefKey(null, oldPrettyName));
    spaceRefCache.remove(new SpaceRefKey(null, null, space.getGroupId()));
    spaceRefCache.remove(new SpaceRefKey(null, null, null, oldUrl));
  }

  @Override
  public void deleteSpace(final String id) throws SpaceStorageException {

    //
    Space space = getSpaceById(id);
    super.deleteSpace(id);

    //
    spaceCache.remove(new SpaceKey(id));
    clearSpaceCache();
    cleanRef(space);

    //
    getCachedActivityStorage().clearCache();

  }

  @Override
  public void ignoreSpace(String spaceId, String userId) {
    try {
      super.ignoreSpace(spaceId, userId);
    } finally {
      SpaceData spaceData = spaceCache.remove(new SpaceKey(spaceId));
      if (spaceData != null) {
        Space space = spaceData.build();
        clearSpaceCache();
        clearIdentityCache();
        if (space != null) {
          cleanRef(space);
        }
      }
    }
  }

  @Override
  public List<Space> getManagerSpacesByFilter(
                                              final String userId,
                                              final SpaceFilter spaceFilter,
                                              final long offset,
                                              final long limit) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MANAGER);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got = CachedSpaceStorage.super.getManagerSpacesByFilter(userId,
                                                                          spaceFilter,
                                                                          offset,
                                                                          limit);
      return buildIds(got);
    },
                                                listKey);
    return buildSpaces(keys);

  }

  @Override
  public int getManagerSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MANAGER);
    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getManagerSpacesByFilterCount(userId,
                                                                                                                   spaceFilter)),
                                      key)
                                 .build();
  }

  @Override
  public int getMemberSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MEMBER);
    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getMemberSpacesByFilterCount(userId,
                                                                                                                  spaceFilter)),
                                      key)
                                 .build();

  }

  @Override
  public List<Space> getMemberSpacesByFilter(
                                             final String userId,
                                             final SpaceFilter spaceFilter,
                                             final long offset,
                                             final long limit) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MEMBER);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got =
                      CachedSpaceStorage.super.getMemberSpacesByFilter(userId,
                                                                       spaceFilter,
                                                                       offset,
                                                                       limit);
      return buildIds(got);
    }, listKey);
    return buildSpaces(keys);
  }

  @Override
  public int getPendingSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PENDING);
    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getPendingSpacesByFilterCount(userId,
                                                                                                                   spaceFilter)),
                                      key)
                                 .build();

  }

  @Override
  public List<Space> getPendingSpacesByFilter(
                                              final String userId,
                                              final SpaceFilter spaceFilter,
                                              final long offset,
                                              final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PENDING);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got =
                      CachedSpaceStorage.super.getPendingSpacesByFilter(userId,
                                                                        spaceFilter,
                                                                        offset,
                                                                        limit);
      return buildIds(got);
    }, listKey);

    //
    return

    buildSpaces(keys);

  }

  @Override
  public int getInvitedSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.INVITED);
    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getInvitedSpacesByFilterCount(userId,
                                                                                                                   spaceFilter)),
                                      key)
                                 .build();

  }

  @Override
  public List<Space> getInvitedSpacesByFilter(final String userId,
                                              final SpaceFilter spaceFilter,
                                              final long offset,
                                              final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.INVITED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got =
                      CachedSpaceStorage.super.getInvitedSpacesByFilter(userId,
                                                                        spaceFilter,
                                                                        offset,
                                                                        limit);
      return buildIds(got);
    }, listKey);

    //
    return

    buildSpaces(keys);

  }

  @Override
  public int getAccessibleSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.ACCESSIBLE);

    //
    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getAccessibleSpacesByFilterCount(userId,
                                                                                                                      spaceFilter)),
                                      key)
                                 .build();

  }

  @Override
  public int getVisibleSpacesCount(final String userId, final SpaceFilter spaceFilter) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.VISIBLE);

    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getVisibleSpacesCount(userId,
                                                                                                           spaceFilter)),
                                      key)
                                 .build();
  }

  @Override
  public List<Space> getAccessibleSpacesByFilter(
                                                 final String userId,
                                                 final SpaceFilter spaceFilter,
                                                 final long offset,
                                                 final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.ACCESSIBLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got =
                      CachedSpaceStorage.super.getAccessibleSpacesByFilter(userId,
                                                                           spaceFilter,
                                                                           offset,
                                                                           limit);
      return buildIds(got);
    }, listKey);

    //
    return

    buildSpaces(keys);

  }

  @Override
  public int getEditableSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.EDITABLE);
    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getEditableSpacesByFilterCount(userId,
                                                                                                                    spaceFilter)),
                                      key)
                                 .build();

  }

  @Override
  public List<Space> getEditableSpacesByFilter(String userId,
                                               SpaceFilter spaceFilter,
                                               long offset,
                                               long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.EDITABLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got =
                      CachedSpaceStorage.super.getEditableSpacesByFilter(userId,
                                                                         spaceFilter,
                                                                         offset,
                                                                         limit);
      return buildIds(got);
    }, listKey);

    //
    return

    buildSpaces(keys);

  }

  @Override
  public int getAllSpacesByFilterCount(final SpaceFilter spaceFilter) {
    SpaceFilterKey key = new SpaceFilterKey(null, spaceFilter, null);
    return spacesCountFutureCache.get(() -> new IntegerData(CachedSpaceStorage.super.getAllSpacesByFilterCount(spaceFilter)), key)
                                 .build();

  }

  @Override
  public List<Space> getSpacesByFilter(final SpaceFilter spaceFilter, final long offset, final long limit) {

    SpaceFilterKey key = new SpaceFilterKey(spaceFilter == null ? null : spaceFilter.getRemoteId(), spaceFilter, null);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got = CachedSpaceStorage.super.getSpacesByFilter(spaceFilter,
                                                                   offset,
                                                                   limit);
      return buildIds(got);
    }, listKey);

    //
    return

    buildSpaces(keys);

  }

  @Override
  public Space getSpaceById(final String id) throws SpaceStorageException {

    //
    SpaceKey key = new SpaceKey(id);

    //
    SpaceData data = spaceFutureCache.get(() -> {
      Space space = CachedSpaceStorage.super.getSpaceById(id);
      if (space != null) {
        putSpaceInCacheIfNotExists(space);
        return new SpaceData(space);
      } else {
        return null;
      }
    }, key);

    if (data != null) {
      return data.build();
    } else {
      return null;
    }

  }

  @Override
  public Space getSpaceByPrettyName(final String spacePrettyName) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, spacePrettyName);

    //
    SpaceKey key = spaceRefFutureCache.get(() -> {
      Space space = CachedSpaceStorage.super.getSpaceByPrettyName(spacePrettyName);
      if (space != null) {
        return putSpaceInCacheIfNotExists(space);
      } else {
        return SpaceKey.NULL_OBJECT;
      }
    }, refKey);

    //
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    } else {
      return null;
    }

  }

  @Override
  public Space getSpaceByGroupId(final String groupId) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, groupId);

    //
    SpaceKey key = spaceRefFutureCache.get(() -> {
      Space space = CachedSpaceStorage.super.getSpaceByGroupId(groupId);
      if (space != null) {
        return putSpaceInCacheIfNotExists(space);
      } else {
        return SpaceKey.NULL_OBJECT;
      }
    }, refKey);

    //
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    } else {
      return null;
    }

  }

  @Override
  public Space getSpaceByUrl(final String url) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, null, url);

    //
    SpaceKey key = spaceRefFutureCache.get(() -> {
      Space space = CachedSpaceStorage.super.getSpaceByUrl(url);
      if (space != null) {
        return putSpaceInCacheIfNotExists(space);
      } else {
        return SpaceKey.NULL_OBJECT;
      }
    }, refKey);

    //
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    } else {
      return null;
    }

  }

  @Override
  public void updateSpaceAccessed(String remoteId, Space space) throws SpaceStorageException {
    // we remove all cache entries for the given userId and for space type
    // LATEST_ACCESSED
    LastAccessedSpacesCacheSelector selector = new LastAccessedSpacesCacheSelector(remoteId, space, cacheService);
    try {
      // Update the storage only if the user has accessed a different space
      if (selector.isUpdateStore()) {
        super.updateSpaceAccessed(remoteId, space);
      }
      spacesCache.select(selector);
    } catch (Exception e) {
      LOG.error("Error while removing cache entries for remoteId=" + remoteId + ", space=" + space.getDisplayName() +
          " and type=" + SpaceType.LATEST_ACCESSED.name() + " or type=" + SpaceType.VISITED, e);
    }
  }

  @Override
  public List<Space> getLastAccessedSpace(final SpaceFilter filter,
                                          final int offset,
                                          final int limit) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(filter.getRemoteId(), filter, SpaceType.LATEST_ACCESSED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got = CachedSpaceStorage.super.getLastAccessedSpace(filter,
                                                                      offset,
                                                                      limit);
      return buildIds(got);
    }, listKey);
    return buildSpaces(keys);
  }

  @Override
  public List<Space> getLastSpaces(final int limit) {
    SpaceFilter filter = new SpaceFilter();
    filter.setSorting(new Sorting(Sorting.SortBy.DATE, Sorting.OrderBy.DESC));

    SpaceFilterKey key = new SpaceFilterKey(null, filter, null);
    ListSpacesKey listKey = new ListSpacesKey(key, 0, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got = CachedSpaceStorage.super.getLastSpaces(limit);
      return buildIds(got);
    },
                                                listKey);

    //
    return buildSpaces(keys);
  }

  @Override
  public List<Space> getVisitedSpaces(final SpaceFilter filter, final int offset, final int limit) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(filter.getRemoteId(), filter, SpaceType.VISITED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<Space> got = CachedSpaceStorage.super.getVisitedSpaces(filter, offset, limit);
      return buildIds(got);
    }, listKey);

    //
    return buildSpaces(keys);
  }

  @Override
  public List<String> getMemberRoleSpaceIdentityIds(String identityId, int offset, int limit) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(identityId, null, SpaceType.MEMBER_IDENTITY_IDS);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<String> got =
                       CachedSpaceStorage.super.getMemberRoleSpaceIdentityIds(identityId,
                                                                              offset,
                                                                              limit);
      return buildListIdentityIds(got);
    }, listKey);

    //
    return buildIds(keys);
  }

  @Override
  public List<String> getMemberRoleSpaceIds(String identityId, int offset, int limit) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(identityId, null, SpaceType.MEMBER_IDS);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesFutureCache.get(() -> {
      if (limit == 0) {
        return buildIds(Collections.emptyList());
      }
      List<String> got = CachedSpaceStorage.super.getMemberRoleSpaceIds(identityId,
                                                                        offset,
                                                                        limit);
      return buildListIdentityIds(got);
    }, listKey);

    //
    return buildIds(keys);
  }

  @Override
  public Map<Long, Long> countSpacesByTemplate() {
    return spacesCountByTemplateFutureCache.get(() -> new HashMap<>(super.countSpacesByTemplate()),
                                                EMPTY_SPACE_KEY);
  }

  public void clearSpaceCached(String spaceId) {
    SpaceKey cacheKey = new SpaceKey(spaceId);
    SpaceData cachedSpace = spaceCache.get(cacheKey);
    if (cachedSpace != null) {
      Space space = cachedSpace.build();
      spaceCache.remove(cacheKey);
      cleanRef(space);
    }
    clearSpaceCache();
    clearIdentityCache();
  }

  public void clearCaches() {
    spaceCache.clearCache();
    spacesCountCache.clearCache();
    spacesCountByTemplateCache.clearCache();
    spacesCache.clearCache();
    spaceRefCache.clearCache();
    identitiesCache.clearCache();
  }

  private SpaceKey putSpaceInCacheIfNotExists(Space space) {
    SpaceKey key = new SpaceKey(space.getId());
    if (spaceCache.get(key) == null) {
      spaceCache.putLocal(key, new SpaceData(space));
    }
    SpaceRefKey refKey = new SpaceRefKey(space.getDisplayName(), null, null, null);
    if (spaceRefCache.get(refKey) == null) {
      spaceRefCache.putLocal(refKey, key);
    }
    refKey = new SpaceRefKey(null, null, space.getGroupId(), null);
    if (spaceRefCache.get(refKey) == null) {
      spaceRefCache.putLocal(refKey, key);
    }
    refKey = new SpaceRefKey(null, space.getPrettyName(), null, null);
    if (spaceRefCache.get(refKey) == null) {
      spaceRefCache.putLocal(refKey, key);
    }
    refKey = new SpaceRefKey(null, null, null, space.getUrl());
    if (spaceRefCache.get(refKey) == null) {
      spaceRefCache.putLocal(refKey, key);
    }
    return key;
  }

  private CachedActivityStorage getCachedActivityStorage() {
    if (cachedActivityStorage == null) {
      cachedActivityStorage = PortalContainer.getInstance()
                                             .getComponentInstanceOfType(CachedActivityStorage.class);
    }
    return cachedActivityStorage;
  }

  private void cleanRef(Space removed) {
    if (removed == null) {
      return;
    }
    spaceRefCache.remove(new SpaceRefKey(removed.getDisplayName()));
    spaceRefCache.remove(new SpaceRefKey(null, removed.getPrettyName()));
    spaceRefCache.remove(new SpaceRefKey(null, null, removed.getGroupId()));
    spaceRefCache.remove(new SpaceRefKey(null, null, null, removed.getUrl()));

    spaceRefFutureCache.remove(new SpaceRefKey(removed.getDisplayName()));
    spaceRefFutureCache.remove(new SpaceRefKey(null, removed.getPrettyName()));
    spaceRefFutureCache.remove(new SpaceRefKey(null, null, removed.getGroupId()));
    spaceRefFutureCache.remove(new SpaceRefKey(null, null, null, removed.getUrl()));
  }

  private void clearIdentityCache() {
    try {
      identitiesCache.select(new IdentityCacheSelector(SpaceIdentityProvider.NAME));
    } catch (Exception e) {
      LOG.error("Error deleting cache entries of provider type 'Space Identities'", e);
    }
  }

  private void clearSpaceCache() {
    try {
      spacesCache.clearCache();
      spacesCountCache.clearCache();
      spacesCountByTemplateCache.clearCache();
    } catch (Exception e) {
      LOG.error("Error deleting space caches", e);
    }
  }

}
