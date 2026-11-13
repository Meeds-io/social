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

import java.util.Locale;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.translation.storage.TranslationStorage;

/**
 * The labels of a field read again after a change, through the cache of the
 * translations: a change that removes another key than the one the field is
 * read with leaves the former labels in the cache.
 */
public class CachedTranslationStorageCacheTest extends AbstractCoreTest {

  private static final String OBJECT_TYPE = "activity";

  private static final String OBJECT_ID   = "323";

  private TranslationStorage  translationStorage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    translationStorage = ExoContainerContext.getService(TranslationStorage.class);
  }

  @Override
  protected void tearDown() throws Exception {
    translationStorage.deleteTranslationLabels(OBJECT_TYPE, OBJECT_ID);
    super.tearDown();
  }

  public void testSavedLabelIsReadOnceTheFieldIsCached() {
    assertEquals(Map.of(), labels("title"));

    translationStorage.saveTranslationLabel(OBJECT_TYPE, OBJECT_ID, "title", Locale.FRENCH, "Titre", 0, 0);
    assertEquals("Titre", labels("title").get(Locale.FRENCH));

    translationStorage.saveTranslationLabels(OBJECT_TYPE, OBJECT_ID, "title", Map.of(Locale.FRENCH, "Intitulé"), 0, 0);
    assertEquals("Intitulé", labels("title").get(Locale.FRENCH));
  }

  public void testDeletedLabelIsNotReadOnceTheFieldIsCached() {
    translationStorage.saveTranslationLabels(OBJECT_TYPE,
                                             OBJECT_ID,
                                             "title",
                                             Map.of(Locale.FRENCH, "Titre", Locale.GERMAN, "Titel"),
                                             0,
                                             0);
    assertEquals(2, labels("title").size());

    translationStorage.deleteTranslationLabel(OBJECT_TYPE, OBJECT_ID, "title", Locale.FRENCH);

    assertEquals(Map.of(Locale.GERMAN, "Titel"), labels("title"));
  }

  public void testDeletedLabelsOfEveryFieldAreNotReadOnceTheFieldsAreCached() {
    translationStorage.saveTranslationLabel(OBJECT_TYPE, OBJECT_ID, "title", Locale.FRENCH, "Titre", 0, 0);
    translationStorage.saveTranslationLabel(OBJECT_TYPE, OBJECT_ID, "summary", Locale.FRENCH, "Résumé", 0, 0);
    assertEquals(1, labels("title").size());
    assertEquals(1, labels("summary").size());

    translationStorage.deleteTranslationLabels(OBJECT_TYPE, OBJECT_ID);

    assertEquals(Map.of(), labels("title"));
    assertEquals(Map.of(), labels("summary"));
  }

  private Map<Locale, String> labels(String fieldName) {
    return translationStorage.getTranslationLabels(OBJECT_TYPE, OBJECT_ID, fieldName);
  }

}
