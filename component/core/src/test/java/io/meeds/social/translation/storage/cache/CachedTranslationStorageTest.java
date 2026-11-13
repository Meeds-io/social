/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.translation.storage.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.storage.cache.model.CacheKey;

/**
 * The entries of the translations are removed by their key, which reaches
 * every node of a cluster.
 */
@RunWith(MockitoJUnitRunner.class)
public class CachedTranslationStorageTest {

  private static final String                  OBJECT_TYPE = "activity";

  private static final String                  OBJECT_ID   = "322";

  @Mock
  private MetadataService                      metadataService;

  @Mock
  private CacheService                         cacheService;

  @Mock
  private ExoCache<CacheKey, TranslationField> translationCache;

  private CachedTranslationStorage             translationStorage;

  @Before
  public void setUp() {
    when(cacheService.<CacheKey, TranslationField> getCacheInstance("social.translation")).thenReturn(translationCache);
    translationStorage = new CachedTranslationStorage(metadataService, cacheService);
  }

  @Test
  public void testSaveTranslationLabelRemovesTheEntryOfTheField() throws Exception {
    translationStorage.saveTranslationLabel(OBJECT_TYPE, OBJECT_ID, "title", Locale.FRENCH, "Titre", 0, 0);

    // The label is saved with the labels of the field, which remove the entry too
    verify(translationCache, atLeastOnce()).remove(new CacheKey(OBJECT_TYPE, OBJECT_ID, "title"));
    verify(translationCache, never()).select(any());
  }

  @Test
  public void testSaveTranslationLabelsRemovesTheEntryOfTheField() throws Exception {
    translationStorage.saveTranslationLabels(OBJECT_TYPE, OBJECT_ID, "title", Map.of(Locale.FRENCH, "Titre"), 0, 0);

    verify(translationCache).remove(new CacheKey(OBJECT_TYPE, OBJECT_ID, "title"));
    verify(translationCache, never()).select(any());
  }

  @Test
  public void testDeleteTranslationLabelRemovesTheEntryOfTheField() throws Exception {
    translationStorage.deleteTranslationLabel(OBJECT_TYPE, OBJECT_ID, "title", Locale.FRENCH);

    verify(translationCache).remove(new CacheKey(OBJECT_TYPE, OBJECT_ID, "title"));
    verify(translationCache, never()).select(any());
  }

  @Test
  public void testDeleteTranslationLabelsRemovesTheEntriesOfEveryField() throws Exception {
    when(metadataService.getMetadataItemsByMetadataTypeAndObject(eq("translation"),
                                                                 eq(new MetadataObject(OBJECT_TYPE, OBJECT_ID))))
                                                                                                                 .thenReturn(List.of(label("title"),
                                                                                                                                     label("summary")));

    translationStorage.deleteTranslationLabels(OBJECT_TYPE, OBJECT_ID);

    verify(metadataService).deleteMetadataItemsByMetadataTypeAndObject(eq("translation"), any());
    verify(translationCache).remove(new CacheKey(OBJECT_TYPE, OBJECT_ID, "title"));
    verify(translationCache).remove(new CacheKey(OBJECT_TYPE, OBJECT_ID, "summary"));
    verify(translationCache, times(2)).remove(any());
    verify(translationCache, never()).select(any());
  }

  private MetadataItem label(String fieldName) {
    Metadata metadata = new Metadata();
    metadata.setName(fieldName);
    MetadataItem item = new MetadataItem();
    item.setMetadata(metadata);
    item.setProperties(Map.of("locale", "fr", "label", fieldName));
    return item;
  }

}
