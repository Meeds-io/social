/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.core.profileproperty.storage;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertyOptionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;

import io.meeds.common.TransactionCallbacks;

/**
 * Caches the settings by id, their ids by name and their ordered id list. A
 * write invalidates every key it changes: the setting's id, its old and new
 * parents' ids, its old and new names and the id list. Each key is read through
 * its cache before the write, so that this node holds it and its eviction is
 * published to the other nodes of a cluster, then evicted after the write, and
 * again once the transaction completes, when one is active.
 */
public class CachedProfileSettingStorage extends ProfileSettingStorage {

  public static final String                                PROFILE_SETTINGS_CACHE_NAME            = "social.profileSettings";

  public static final String                                PROFILE_SETTING_IDS_BY_NAME_CACHE_NAME = "social.profileSettingIdsByName";

  public static final String                                PROFILE_SETTING_IDS_CACHE_NAME         = "social.profileSettingIds";

  /** The single key of the ordered id list's cache */
  private static final String                               PROFILE_SETTING_IDS_KEY                = "ids";

  private static final Log                                  LOG                                    =
                                                                ExoLogger.getLogger(CachedProfileSettingStorage.class);

  private final ExoCache<Long, ProfilePropertySetting>      cache;

  private final FutureExoCache<Long, ProfilePropertySetting, Object> futureCache;

  private final ExoCache<String, Long>                      idByNameCache;

  private final FutureExoCache<String, Long, Object>        idByNameFutureCache;

  private final ExoCache<String, List<Long>>                idsCache;

  private final FutureExoCache<String, List<Long>, Object>  idsFutureCache;

  public CachedProfileSettingStorage(CacheService cacheService,
                                     ProfilePropertySettingDAO profilePropertySettingDAO,
                                     ProfilePropertyOptionDAO profilePropertyOptionDAO) {
    super(profilePropertySettingDAO, profilePropertyOptionDAO);
    cache = cacheService.getCacheInstance(PROFILE_SETTINGS_CACHE_NAME);
    idByNameCache = cacheService.getCacheInstance(PROFILE_SETTING_IDS_BY_NAME_CACHE_NAME);
    idsCache = cacheService.getCacheInstance(PROFILE_SETTING_IDS_CACHE_NAME);
    futureCache = new FutureExoCache<>((c, id) -> super.getProfileSettingById(id), cache);
    idByNameFutureCache = new FutureExoCache<>((c, name) -> {
      ProfilePropertySetting s = super.findProfileSettingByName(name);
      return s == null ? 0l : s.getId();
    }, idByNameCache);
    idsFutureCache = new FutureExoCache<>((c, key) -> super.getPropertySettings().stream()
                                                          .map(ProfilePropertySetting::getId)
                                                          .toList(),
                                          idsCache);
  }

  @Override
  public List<ProfilePropertySetting> getPropertySettings() {
    List<Long> profileSettingIds = idsFutureCache.get(null, PROFILE_SETTING_IDS_KEY);
    if (profileSettingIds == null) {
      return Collections.emptyList();
    }
    // An id whose setting no longer loads was deleted after the list was cached
    return profileSettingIds.stream().map(this::getProfileSettingById).filter(Objects::nonNull).toList();
  }

  @Override
  public ProfilePropertySetting findProfileSettingByName(String name) {
    long id = idByNameFutureCache.get(null, name);
    return id == 0l ? null : getProfileSettingById(id);
  }

  @Override
  public ProfilePropertySetting getProfileSettingById(Long id) {
    return futureCache.get(null, id);
  }

  @Override
  public ProfilePropertySetting saveProfilePropertySetting(ProfilePropertySetting profilePropertySetting, boolean isNew) {
    Objects.requireNonNull(profilePropertySetting, "Profile property setting is mandatory");
    Set<Long> ids = new LinkedHashSet<>();
    Set<String> names = new LinkedHashSet<>();
    addKeys(profilePropertySetting, ids, names);
    if (!isNew && profilePropertySetting.getId() != null) {
      addKeys(readBeforeWrite(() -> getProfileSettingById(profilePropertySetting.getId()), profilePropertySetting.getId()),
              ids,
              names);
    }
    loadKeys(ids, names);
    ProfilePropertySetting savedProfilePropertySetting = null;
    try {
      savedProfilePropertySetting = super.saveProfilePropertySetting(profilePropertySetting, isNew);
      return savedProfilePropertySetting;
    } finally {
      addKeys(savedProfilePropertySetting, ids, names);
      invalidate(ids, names);
    }
  }

  @Override
  public void deleteProfilePropertySetting(Long id) {
    Set<Long> ids = new LinkedHashSet<>();
    Set<String> names = new LinkedHashSet<>();
    ids.add(id);
    addKeys(readBeforeWrite(() -> getProfileSettingById(id), id), ids, names);
    loadKeys(ids, names);
    try {
      super.deleteProfilePropertySetting(id);
    } finally {
      invalidate(ids, names);
    }
  }

  @Override
  public boolean clearListingFlags(Long id) {
    Set<Long> ids = new LinkedHashSet<>();
    Set<String> names = new LinkedHashSet<>();
    ids.add(id);
    loadKeys(ids, names);
    try {
      return super.clearListingFlags(id);
    } finally {
      invalidate(ids, names);
    }
  }

  /**
   * Clears this node's three caches and the loads in flight, with no eviction
   * published to the other nodes.
   */
  public void clearCaches() {
    futureCache.clearFutures();
    idByNameFutureCache.clearFutures();
    idsFutureCache.clearFutures();
    cache.clearCache();
    idByNameCache.clearCache();
    idsCache.clearCache();
  }

  private void addKeys(ProfilePropertySetting profilePropertySetting, Set<Long> ids, Set<String> names) {
    if (profilePropertySetting == null) {
      return;
    }
    if (profilePropertySetting.getId() != null) {
      ids.add(profilePropertySetting.getId());
    }
    if (profilePropertySetting.getParentId() != null) {
      ids.add(profilePropertySetting.getParentId());
    }
    if (profilePropertySetting.getPropertyName() != null) {
      names.add(profilePropertySetting.getPropertyName());
    }
  }

  /**
   * Reads each key through its cache, so that this node holds it and its
   * eviction is published.
   */
  private void loadKeys(Set<Long> ids, Set<String> names) {
    ids.forEach(id -> readBeforeWrite(() -> futureCache.get(null, id), id));
    names.forEach(name -> readBeforeWrite(() -> idByNameFutureCache.get(null, name), name));
    readBeforeWrite(() -> idsFutureCache.get(null, PROFILE_SETTING_IDS_KEY), PROFILE_SETTING_IDS_KEY);
  }

  /**
   * A read made only to collect or load the keys a write evicts never fails
   * the write: this node's caches are cleared anyway, and only the eviction of
   * that key may not reach the other nodes. Such a read fails when it converts
   * a setting outside a request lifecycle, whose options can no longer load.
   */
  private <T> T readBeforeWrite(Supplier<T> read, Serializable key) {
    try {
      return read.get();
    } catch (RuntimeException e) {
      LOG.warn("Can't read the profile setting cache key '{}' before its write, its eviction may not reach the other nodes",
               key,
               e);
      return null;
    }
  }

  private void invalidate(Set<Long> ids, Set<String> names) {
    evict(ids, names);
    TransactionCallbacks.afterCompletion(() -> {
      loadKeys(ids, names);
      evict(ids, names);
    });
  }

  private void evict(Set<Long> ids, Set<String> names) {
    ids.forEach(id -> {
      futureCache.removeFuture(id);
      futureCache.remove(id);
    });
    names.forEach(name -> {
      idByNameFutureCache.removeFuture(name);
      idByNameFutureCache.remove(name);
    });
    idsFutureCache.removeFuture(PROFILE_SETTING_IDS_KEY);
    idsFutureCache.remove(PROFILE_SETTING_IDS_KEY);
    clearCaches();
  }

}
