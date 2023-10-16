/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.cms.storage.cache;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.metadata.MetadataService;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.storage.CMSStorage;
import io.meeds.social.cms.storage.model.CMSSettingKey;

public class CachedCMSStorage extends CMSStorage {

  public static final String                                CACHE_NAME = "social.cmsSetting";

  private ExoCache<CMSSettingKey, CMSSetting>               cache;

  private FutureExoCache<CMSSettingKey, CMSSetting, Object> futureCache;

  public CachedCMSStorage(CacheService cacheService,
                          MetadataService metadataService) {
    super(metadataService);
    cache = cacheService.getCacheInstance(CACHE_NAME);
    Loader<CMSSettingKey, CMSSetting, Object> loader = new Loader<>() {
      @Override
      public CMSSetting retrieve(Object context, CMSSettingKey key) throws Exception {
        return CachedCMSStorage.super.getSetting(key.getType(), key.getName());
      }
    };
    this.futureCache = new FutureExoCache<>(loader, cache);
  }

  @Override
  public void saveSetting(String type,
                          String name,
                          String pageReference,
                          long spaceId,
                          long userCreatorId) throws ObjectAlreadyExistsException {
    try {
      super.saveSetting(type, name, pageReference, spaceId, userCreatorId);
    } finally {
      clearSetting(type, name);
    }
  }

  @Override
  public CMSSetting getSetting(String type, String name) {
    return clone(futureCache.get(null, new CMSSettingKey(type, name)));
  }

  private void clearSetting(String type, String name) {
    futureCache.remove(new CMSSettingKey(type, name));
  }

  private CMSSetting clone(CMSSetting cmsSetting) {
    return cmsSetting == null ? null : cmsSetting.clone();
  }

}
