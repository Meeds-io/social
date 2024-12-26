/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.cache.future.FutureCache;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;

public class CachedProfileSettingStorage extends ProfileSettingStorage {

  private ExoCache<Long, ProfilePropertySetting>            cache;

  private FutureCache<Long, ProfilePropertySetting, Object> futureCache;

  private ExoCache<String, Long>                            idByNameCache;

  private FutureCache<String, Long, Object>                 idByNameFutureCache;

  private List<Long>                                        profileSettingIds;

  public CachedProfileSettingStorage(CacheService cacheService,
                                     ProfilePropertySettingDAO profilePropertySettingDAO) {
    super(profilePropertySettingDAO);
    cache = cacheService.getCacheInstance("social.profileSettings");
    idByNameCache = cacheService.getCacheInstance("social.profileSettingIdsByName");
    futureCache = new FutureExoCache<>((c, id) -> super.getProfileSettingById(id), cache);
    idByNameFutureCache = new FutureExoCache<>((c, name) -> {
      ProfilePropertySetting s = super.findProfileSettingByName(name);
      return s == null ? 0l : s.getId();
    }, idByNameCache);
  }

  @Override
  public List<ProfilePropertySetting> getPropertySettings() {
    if (profileSettingIds == null) {
      List<ProfilePropertySetting> propertySettings = super.getPropertySettings();
      profileSettingIds = CollectionUtils.isEmpty(propertySettings) ? Collections.emptyList() :
                                                                    propertySettings.stream()
                                                                                    .map(ProfilePropertySetting::getId)
                                                                                    .toList();
    }
    return profileSettingIds.stream().map(this::getProfileSettingById).toList();
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
    try {
      return super.saveProfilePropertySetting(profilePropertySetting, isNew);
    } finally {
      clearCaches();
    }
  }

  @Override
  public void deleteProfilePropertySetting(Long id) {
    try {
      super.deleteProfilePropertySetting(id);
    } finally {
      clearCaches();
    }
  }

  public void clearCaches() {
    profileSettingIds = null;
    cache.clearCache();
    idByNameCache.clearCache();
  }

}
