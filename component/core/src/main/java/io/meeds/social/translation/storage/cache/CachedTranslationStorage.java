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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.translation.storage.cache;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.metadata.MetadataService;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.storage.TranslationStorage;
import io.meeds.social.translation.storage.cache.model.CacheKey;

public class CachedTranslationStorage extends TranslationStorage {

  private static final Log                                   LOG                    =
                                                                 ExoLogger.getLogger(CachedTranslationStorage.class);

  private static final String                                TRANSLATION_CACHE_NAME = "social.translation";

  private ExoCache<CacheKey, TranslationField>               translationCache;

  private FutureExoCache<CacheKey, TranslationField, Object> translationFutureCache;

  public CachedTranslationStorage(MetadataService metadataService,
                                  CacheService cacheService) {
    super(metadataService);
    translationCache = cacheService.getCacheInstance(TRANSLATION_CACHE_NAME);

    Loader<CacheKey, TranslationField, Object> labelsLoader = new Loader<>() {
      @Override
      public TranslationField retrieve(Object context, CacheKey key) throws Exception {
        return CachedTranslationStorage.super.getTranslationField(key.getObjectType(), key.getObjectId(), key.getFieldName());
      }
    };
    this.translationFutureCache = new FutureExoCache<>(labelsLoader, translationCache);
  }

  @Override
  public TranslationField getTranslationField(String objectType, long objectId, String fieldName) {
    TranslationField translationField = this.translationFutureCache.get(null, new CacheKey(objectType, objectId, fieldName));
    return translationField.clone();
  }

  @Override
  public void deleteTranslationLabel(String objectType, long objectId, String fieldName, Locale locale) {
    try {
      super.deleteTranslationLabel(objectType, objectId, fieldName, locale);
    } finally {
      clearCache(objectType, objectId);
    }
  }

  @Override
  public void deleteTranslationLabels(String objectType, long objectId) {
    try {
      super.deleteTranslationLabels(objectType, objectId);
    } finally {
      clearCache(objectType, objectId);
    }
  }

  @Override
  public void saveTranslationLabel(String objectType,
                                   long objectId,
                                   String fieldName,
                                   Locale locale,
                                   String label,
                                   long audienceId, long spaceId) {
    try {
      super.saveTranslationLabel(objectType, objectId, fieldName, locale, label, audienceId, spaceId);
    } finally {
      clearCache(objectType, objectId);
    }
  }

  @Override
  public void saveTranslationLabels(String objectType,
                                    long objectId,
                                    String fieldName,
                                    Map<Locale, String> labels,
                                    long audienceId, long spaceId) {
    try {
      super.saveTranslationLabels(objectType, objectId, fieldName, labels, audienceId, spaceId);
    } finally {
      clearCache(objectType, objectId);
    }
  }

  private void clearCache(String objectType, long objectId) {
    try {
      translationCache.select(new CachedObjectSelector<CacheKey, TranslationField>() {
        @Override
        public boolean select(CacheKey key, ObjectCacheInfo<? extends TranslationField> ocinfo) {
          return StringUtils.equals(key.getObjectType(), objectType) && objectId == key.getObjectId();
        }

        @Override
        public void onSelect(ExoCache<? extends CacheKey, ? extends TranslationField> cache, CacheKey key,
                             ObjectCacheInfo<? extends TranslationField> ocinfo) throws Exception {
          cache.remove(key);
        }
      });
    } catch (Exception e) {
      LOG.warn("Unable to clean cache entries for object {} with id {}. Clean all cache entries to preserve coherence.",
               objectType,
               objectId,
               e);
      translationCache.clearCache();
    }
  }

}
