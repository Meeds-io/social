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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSSpaceStorageImpl;
import org.exoplatform.social.core.jpa.storage.dao.*;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.*;
import org.exoplatform.social.core.storage.cache.model.key.*;
import org.exoplatform.social.core.storage.cache.selector.*;
import org.exoplatform.social.metadata.favorite.FavoriteService;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedSpaceStorage extends RDBMSSpaceStorageImpl {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(CachedSpaceStorage.class);

  private final ExoCache<SpaceKey, SpaceData> exoSpaceCache;
  private final ExoCache<SpaceKey, SpaceSimpleData> exoSpaceSimpleCache;
  private final ExoCache<SpaceRefKey, SpaceKey> exoRefSpaceCache;
  private final ExoCache<SpaceFilterKey, IntegerData> exoSpacesCountCache;
  private final ExoCache<ListSpacesKey, ListSpacesData> exoSpacesCache;
  private final ExoCache<ListIdentitiesKey, ListIdentitiesData> exoIdentitiesCache;

  private final FutureExoCache<SpaceKey, SpaceData, ServiceContext<SpaceData>> spaceCache;
  private final FutureExoCache<SpaceKey, SpaceSimpleData, ServiceContext<SpaceSimpleData>> spaceSimpleCache;
  private final FutureExoCache<SpaceRefKey, SpaceKey, ServiceContext<SpaceKey>> spaceRefCache;
  private final FutureExoCache<SpaceFilterKey, IntegerData, ServiceContext<IntegerData>> spacesCountCache;
  private final FutureExoCache<ListSpacesKey, ListSpacesData, ServiceContext<ListSpacesData>> spacesCache;

  private SocialStorageCacheService cacheService;
  private CachedActivityStorage cachedActivityStorage;
  private CachedIdentityStorage cachedIdentityStorage;

  /**
   * Build the activity list from the caches Ids.
   *
   * @param data ids
   * @return activities
   */
  private List<Space> buildSpaces(ListSpacesData data) {

    List<Space> spaces = new ArrayList<Space>();
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
   * Build the activity list from the caches Ids.
   *
   * @param data ids
   * @return activities
   */
  private List<Space> buildSimpleSpaces(ListSpacesData data) {

    List<Space> spaces = new ArrayList<Space>();
    for (SpaceKey k : data.getIds()) {
      Space s = getSpaceSimpleById(k.getId());
      spaces.add(s);
    }
    return spaces;

  }

  public CachedActivityStorage getCachedActivityStorage() {
    if (cachedActivityStorage == null) {
      cachedActivityStorage = (CachedActivityStorage)
          PortalContainer.getInstance().getComponentInstanceOfType(CachedActivityStorage.class);
    }
    return cachedActivityStorage;
  }

  /**
   * Get cached identity storage.
   * 
   * @return
   * @since 1.2.8
   */
  public CachedIdentityStorage getCachedIdentityStorage() {
    if (cachedIdentityStorage == null) {
      cachedIdentityStorage = (CachedIdentityStorage)
          PortalContainer.getInstance().getComponentInstanceOfType(CachedIdentityStorage.class);
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

    List<SpaceKey> data = new ArrayList<SpaceKey>();
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

    List<SpaceKey> data = new ArrayList<SpaceKey>();
    for (String identityId : identities) {
      SpaceKey k = new SpaceKey(identityId);
      data.add(k);
    }
    return new ListSpacesData(data);

  }
  
  /**
   * Build the ids from the space briefing list.
   *
   * @param spaces briefing spaces
   * @return ids
   */
  private ListSpacesData buildSimpleIds(List<Space> spaces) {

    List<SpaceKey> data = new ArrayList<SpaceKey>();
    for (Space s : spaces) {
      SpaceKey k = putSpaceInCacheIfNotExists(s);
      data.add(k);
    }
    return new ListSpacesData(data);

  }

  public CachedSpaceStorage(SpaceDAO spaceDAO,
                            SpaceMemberDAO spaceMemberDAO,
                            IdentityStorage identityStorage,
                            IdentityDAO identityDAO,
                            ActivityDAO activityDAO,
                            SpaceExternalInvitationDAO spaceExternalInvitationDAO,
                            SocialStorageCacheService cacheService,
                            FavoriteService favoriteService) {
    super(spaceDAO, spaceMemberDAO, identityStorage, identityDAO, activityDAO, spaceExternalInvitationDAO,favoriteService);
    this.cacheService = cacheService;

    this.exoSpaceCache = cacheService.getSpaceCache();
    this.exoSpaceSimpleCache = cacheService.getSpaceSimpleCache();
    this.exoRefSpaceCache = cacheService.getSpaceRefCache();
    this.exoSpacesCountCache = cacheService.getSpacesCountCache();
    this.exoSpacesCache = cacheService.getSpacesCache();
    this.exoIdentitiesCache = cacheService.getIdentitiesCache();

    this.spaceCache = CacheType.SPACE.createFutureCache(exoSpaceCache);
    this.spaceSimpleCache = CacheType.SPACE_SIMPLE.createFutureCache(exoSpaceSimpleCache);
    this.spaceRefCache = CacheType.SPACE_REF.createFutureCache(exoRefSpaceCache);
    this.spacesCountCache = CacheType.SPACES_COUNT.createFutureCache(exoSpacesCountCache);
    this.spacesCache = CacheType.SPACES.createFutureCache(exoSpacesCache);

  }

  private void cleanRef(Space removed) {
    if (removed == null) {
      return;
    }
    exoRefSpaceCache.remove(new SpaceRefKey(removed.getDisplayName()));
    exoRefSpaceCache.remove(new SpaceRefKey(null, removed.getPrettyName()));
    exoRefSpaceCache.remove(new SpaceRefKey(null, null, removed.getGroupId()));
    exoRefSpaceCache.remove(new SpaceRefKey(null, null, null, removed.getUrl()));

    spaceRefCache.remove(new SpaceRefKey(removed.getDisplayName()));
    spaceRefCache.remove(new SpaceRefKey(null, removed.getPrettyName()));
    spaceRefCache.remove(new SpaceRefKey(null, null, removed.getGroupId()));
    spaceRefCache.remove(new SpaceRefKey(null, null, null, removed.getUrl()));
  }

  void clearIdentityCache() {

    try {
      exoIdentitiesCache.select(new IdentityCacheSelector(SpaceIdentityProvider.NAME));
    }
    catch (Exception e) {
      LOG.error("Error deleting cache entries of provider type 'Space Identities'", e);
    }

  }

  void clearSpaceCache() {

    try {
      exoSpacesCache.select(new CacheSelector<ListSpacesKey, ListSpacesData>());
      exoSpacesCountCache.select(new CacheSelector<SpaceFilterKey, IntegerData>());
    }
    catch (Exception e) {
      LOG.error("Error deleting space caches", e);
    }

  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByDisplayName(final String spaceDisplayName) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(spaceDisplayName);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = CachedSpaceStorage.super.getSpaceByDisplayName(spaceDisplayName);
            if (space != null) {
              return putSpaceInCacheIfNotExists(space);
            }
            else {
              return SpaceKey.NULL_OBJECT;
            }
          }
        },
        refKey);

    //
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  public void saveSpace(final Space space, final boolean isNew) throws SpaceStorageException {

    //
    super.saveSpace(space, isNew);

    
    //
    exoSpaceSimpleCache.remove(new SpaceKey(space.getId()));
    exoSpaceCache.remove(new SpaceKey(space.getId()));
    
    clearSpaceCache();
    clearIdentityCache();
    cleanRef(space);
  }

  /**
   * {@inheritDoc}
   */
  public void renameSpace(Space space, String newDisplayName) throws SpaceStorageException {
    renameSpace(null, space, newDisplayName);
  }
  
  /**
   * {@inheritDoc}
   */
  public void renameSpace(String remoteId, Space space, String newDisplayName) throws SpaceStorageException {
    String oldDisplayName = space.getDisplayName();
    String oldUrl = SpaceUtils.cleanString(oldDisplayName);
    String oldPrettyName = space.getPrettyName();
    
    //
    super.renameSpace(remoteId, space, newDisplayName);

    //remove identity and profile from cache
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
    exoSpaceCache.remove(new SpaceKey(space.getId()));
    clearSpaceCache();
    clearIdentityCache();
    cleanRef(space);
    exoRefSpaceCache.remove(new SpaceRefKey(oldDisplayName));
    exoRefSpaceCache.remove(new SpaceRefKey(null, oldPrettyName));
    exoRefSpaceCache.remove(new SpaceRefKey(null, null, space.getGroupId()));
    exoRefSpaceCache.remove(new SpaceRefKey(null, null, null, oldUrl));
  }
  
  /**
   * {@inheritDoc}
   */
  public void deleteSpace(final String id) throws SpaceStorageException {

    //
    Space space = getSpaceById(id);
    super.deleteSpace(id);

    //
    exoSpaceCache.remove(new SpaceKey(id));
    clearSpaceCache();
    cleanRef(space);

    //
    getCachedActivityStorage().clearCache();

  }

  @Override
  public void ignoreSpace(String spaceId, String userId) {
    super.ignoreSpace(spaceId, userId);
    exoSpaceSimpleCache.remove(new SpaceKey(spaceId));
    SpaceData spaceData = exoSpaceCache.remove(new SpaceKey(spaceId));
    if (spaceData != null) {
      Space space = spaceData.build();
      clearSpaceCache();
      clearIdentityCache();
      if (space != null) {
        cleanRef(space);
      }
    }
  }

  @Override
  public boolean isSpaceIgnored(String spaceId, String userId) {
    return super.isSpaceIgnored(spaceId, userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getManagerSpacesByFilter(
                                              final String userId,
                                              final SpaceFilter spaceFilter,
                                              final long offset,
                                              final long limit) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MANAGER);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);
    ListSpacesData keys = spacesCache.get(
                                          new ServiceContext<ListSpacesData>() {
                                            public ListSpacesData execute() {
                                              if (limit == 0) {
                                                return buildIds(Collections.emptyList());
                                              }
                                              List<Space> got = CachedSpaceStorage.super.getManagerSpacesByFilter(userId,
                                                                                                 spaceFilter,
                                                                                                 offset,
                                                                                                 limit);
                                              return buildIds(got);
                                            }
                                          },
                                          listKey);
    return buildSpaces(keys);

  }

  @Override
  public int getManagerSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MANAGER);
    return spacesCountCache.get(
                                new ServiceContext<IntegerData>() {
                                  public IntegerData execute() {
                                    return new IntegerData(CachedSpaceStorage.super.getManagerSpacesByFilterCount(userId, spaceFilter));
                                  }
                                },
                                key)
                           .build();
  }

  /**
   * {@inheritDoc}
   */
  public int getMemberSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MEMBER);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getMemberSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getMemberSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MEMBER);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<Space> got = CachedSpaceStorage.super.getMemberSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getPendingSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PENDING);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getPendingSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPendingSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PENDING);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<Space> got = CachedSpaceStorage.super.getPendingSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);
    
  }

  /**
   * {@inheritDoc}
   */
  public int getInvitedSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.INVITED);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getInvitedSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getInvitedSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.INVITED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<Space> got = CachedSpaceStorage.super.getInvitedSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);
    
  }

  /**
   * {@inheritDoc}
   */
  public int getPublicSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PUBLIC);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getPublicSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getAccessibleSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {
    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.ACCESSIBLE);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getAccessibleSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();
    
  }

  /**
   * {@inheritDoc}
   */
  public int getVisibleSpacesCount(final String userId, final SpaceFilter spaceFilter) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.VISIBLE);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getVisibleSpacesCount(userId, spaceFilter));
          }
        },
        key).build();
  }
  
  /**
   * {@inheritDoc}
   */
  public int getUnifiedSearchSpacesCount(final String userId, final SpaceFilter spaceFilter) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.UNIFIED_SEARCH);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getUnifiedSearchSpacesCount(userId, spaceFilter));
          }
        },
        key).build();
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getAccessibleSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.ACCESSIBLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<Space> got = CachedSpaceStorage.super.getAccessibleSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getEditableSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.EDITABLE);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getEditableSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();
    
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getEditableSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.EDITABLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<Space> got = CachedSpaceStorage.super.getEditableSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getAllSpacesByFilterCount(final SpaceFilter spaceFilter) {
    //
    SpaceFilterKey key = new SpaceFilterKey(null, spaceFilter, null);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getAllSpacesByFilterCount(spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getSpacesByFilter(final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(spaceFilter == null ? null : spaceFilter.getRemoteId(), spaceFilter, null);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<Space> got = CachedSpaceStorage.super.getSpacesByFilter(spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceById(final String id) throws SpaceStorageException {

    //
    SpaceKey key = new SpaceKey(id);

    //
    SpaceData data = spaceCache.get(
        new ServiceContext<SpaceData>() {
          public SpaceData execute() {
            Space space = CachedSpaceStorage.super.getSpaceById(id);
            if (space != null) {
              putSpaceInCacheIfNotExists(space);
              return new SpaceData(space);
            }
            else {
              return null;
            }
          }
        },
        key);

    if (data != null) {
      return data.build();
    }
    else {
      return null;
    }
    
  }
  
  /**
   * {@inheritDoc}
   */
  public Space getSpaceSimpleById(final String id) throws SpaceStorageException {

    //
    SpaceKey key = new SpaceKey(id);

    SpaceData data = exoSpaceCache.get(key);
    if (data != null) {
      Space s = data.build();
      if (exoSpaceSimpleCache.get(key) == null) {
        putSpaceInCacheIfNotExists(s);
      }
      
      return s;
      
    }
    //
    SpaceSimpleData simpleData = spaceSimpleCache.get(
        new ServiceContext<SpaceSimpleData>() {
          public SpaceSimpleData execute() {
            Space space = CachedSpaceStorage.super.getSpaceSimpleById(id);
            if (space != null) {
              return new SpaceSimpleData(space);
            }
            else {
              return null;
            }
          }
        },
        key);

    if (simpleData != null) {
      return simpleData.build();
    }
    else {
      return null;
    }
    
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByPrettyName(final String spacePrettyName) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, spacePrettyName);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = CachedSpaceStorage.super.getSpaceByPrettyName(spacePrettyName);
            if (space != null) {
              return putSpaceInCacheIfNotExists(space);
            }
            else {
              return SpaceKey.NULL_OBJECT;
            }
          }
        },
        refKey);

    //
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByGroupId(final String groupId) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, groupId);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = CachedSpaceStorage.super.getSpaceByGroupId(groupId);
            if (space != null) {
              return putSpaceInCacheIfNotExists(space);
            }
            else {
              return SpaceKey.NULL_OBJECT;
            }
          }
        },
        refKey);

    //
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByUrl(final String url) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, null, url);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = CachedSpaceStorage.super.getSpaceByUrl(url);
            if (space != null) {
              return putSpaceInCacheIfNotExists(space);
            }
            else {
              return SpaceKey.NULL_OBJECT;
            }
          }
        },
        refKey);

    //
    if (key != null && key != SpaceKey.NULL_OBJECT && key.getId() != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }
    
  }

  @Override
  public void updateSpaceAccessed(String remoteId, Space space) throws SpaceStorageException {
    // we remove all cache entries for the given userId and for space type LATEST_ACCESSED
    LastAccessedSpacesCacheSelector selector = new LastAccessedSpacesCacheSelector(remoteId, space, cacheService);
    try {
      exoSpacesCache.select(selector);
    } catch (Exception e) {
      LOG.error("Error while removing cache entries for remoteId=" + remoteId + ", space=" + space.getDisplayName() +
              " and type=" + SpaceType.LATEST_ACCESSED.name() + " or type=" + SpaceType.VISITED, e);
    }

    // Update the storage only if the user has accessed a different space
    if (selector.isUpdateStore()) {
      super.updateSpaceAccessed(remoteId, space);
    }
  }

  @Override
  public List<Space> getLastAccessedSpace(final SpaceFilter filter, final int offset, final int limit) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(filter.getRemoteId(), filter, SpaceType.LATEST_ACCESSED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<Space> got = CachedSpaceStorage.super.getLastAccessedSpace(filter, offset, limit);
            return buildSimpleIds(got);
          }
        },
        listKey);

    //
    return buildSimpleSpaces(keys);
  }

  public List<Space> getLastSpaces(final int limit) {
     SpaceFilter filter = new SpaceFilter(null, null);
     filter.setSorting(new Sorting(Sorting.SortBy.DATE, Sorting.OrderBy.DESC));

     SpaceFilterKey key = new SpaceFilterKey(null, filter, null);
     ListSpacesKey listKey = new ListSpacesKey(key, 0, limit);

     //
     ListSpacesData keys = spacesCache.get(
         new ServiceContext<ListSpacesData>() {
           public ListSpacesData execute() {
             if (limit == 0) {
               return buildIds(Collections.emptyList());
             }
             List<Space> got = CachedSpaceStorage.super.getLastSpaces(limit);
             return buildIds(got);
           }
         },
         listKey);

     //
     return buildSimpleSpaces(keys);
  }

  @Override
  public int getNumberOfMemberPublicSpaces(final String userId) {
    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.PUBLIC);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(CachedSpaceStorage.super.getNumberOfMemberPublicSpaces(userId));
          }
        },
        key)
        .build();

  }
  
  @Override
  public List<Space> getVisitedSpaces(final SpaceFilter filter, final int offset, final int limit) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(filter.getRemoteId(), filter, SpaceType.VISITED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(new ServiceContext<ListSpacesData>() {
      public ListSpacesData execute() {
        if (limit == 0) {
          return buildIds(Collections.emptyList());
        }
        List<Space> got = CachedSpaceStorage.super.getVisitedSpaces(filter, offset, limit);
        return buildIds(got);
      }
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
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            if (limit == 0) {
              return buildIds(Collections.emptyList());
            }
            List<String> got = CachedSpaceStorage.super.getMemberRoleSpaceIdentityIds(identityId, offset, limit);
            return buildListIdentityIds(got);
          }
        },
        listKey);

    //
    return buildIds(keys);
  }

  @Override
  public List<String> getMemberRoleSpaceIds(String identityId, int offset, int limit) throws SpaceStorageException {
    //
    SpaceFilterKey key = new SpaceFilterKey(identityId, null, SpaceType.MEMBER_IDS);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
                                          new ServiceContext<ListSpacesData>() {
                                            public ListSpacesData execute() {
                                              if (limit == 0) {
                                                return buildIds(Collections.emptyList());
                                              }
                                              List<String> got = CachedSpaceStorage.super.getMemberRoleSpaceIds(identityId,
                                                                                                                offset,
                                                                                                                limit);
                                              return buildListIdentityIds(got);
                                            }
                                          },
                                          listKey);

    //
    return buildIds(keys);
  }

  private SpaceKey putSpaceInCacheIfNotExists(Space space) {
    SpaceKey key = new SpaceKey(space.getId());
    if(exoSpaceCache.get(key) == null) {
      exoSpaceCache.putLocal(key, new SpaceData(space));
      exoSpaceSimpleCache.putLocal(key, new SpaceSimpleData(space));
    }
    SpaceRefKey refKey = new SpaceRefKey(space.getDisplayName(), null, null, null);
    if(exoRefSpaceCache.get(refKey) == null) {
      exoRefSpaceCache.putLocal(refKey, key);
    }
    refKey = new SpaceRefKey(null, null, space.getGroupId(), null);
    if(exoRefSpaceCache.get(refKey) == null) {
      exoRefSpaceCache.putLocal(refKey, key);
    }
    refKey = new SpaceRefKey(null, space.getPrettyName(), null, null);
    if(exoRefSpaceCache.get(refKey) == null) {
      exoRefSpaceCache.putLocal(refKey, key);
    }
    refKey = new SpaceRefKey(null, null, null, space.getUrl());
    if(exoRefSpaceCache.get(refKey) == null) {
      exoRefSpaceCache.putLocal(refKey, key);
    }
    return key;
  }

  public void clearSpaceCached(String spaceId) {
    SpaceKey cacheKey = new SpaceKey(spaceId);
    SpaceData cachedSpace = exoSpaceCache.get(cacheKey);
    if (cachedSpace != null) {
      Space space = cachedSpace.build();
      exoSpaceSimpleCache.remove(cacheKey);
      exoSpaceCache.remove(cacheKey);
      cleanRef(space);
    }
    clearSpaceCache();
    clearIdentityCache();
  }

  public void clearCaches() {
    exoSpaceCache.clearCache();
    exoSpaceSimpleCache.clearCache();
    exoSpacesCountCache.clearCache();
    exoSpacesCache.clearCache();
    exoRefSpaceCache.clearCache();
    exoIdentitiesCache.clearCache();
  }

}

