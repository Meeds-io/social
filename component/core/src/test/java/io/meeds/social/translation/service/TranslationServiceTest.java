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
package io.meeds.social.translation.service;

import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.plugin.TranslationPlugin;

public class TranslationServiceTest extends AbstractCoreTest { // NOSONAR

  private static final String    OBJECT_TYPE = "activity";

  private TranslationServiceImpl translationService;

  private String                 objectType  = OBJECT_TYPE;

  private long                   objectId    = 322l;

  private String                 fieldName   = "title";

  private String                 username    = "test";

  private Locale                 locale      = Locale.ENGLISH;

  private String                 label       = "Test Label";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    translationService = ExoContainerContext.getService(TranslationServiceImpl.class);
  }

  @Override
  protected void tearDown() throws Exception {
    translationService.deleteTranslationLabels(objectType, objectId);
    removeTranslationPlugin();
    super.tearDown();
  }

  public void testSaveTranslationLabels() throws IllegalAccessException, ObjectNotFoundException {
    Map<Locale, String> labels = new HashMap<>();
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabels(null, objectId, fieldName, labels));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabels(objectType, 0, fieldName, labels));
    assertThrows(IllegalStateException.class,
                 () -> translationService.saveTranslationLabels(objectType, objectId, fieldName, labels, username));

    setTranslationPlugin(true, false, 2, 1);
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabels(objectType, objectId, null, labels));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabels(objectType, objectId, fieldName, null));
    assertThrows(IllegalAccessException.class,
                 () -> translationService.saveTranslationLabels(objectType, objectId, fieldName, labels, null));

    assertThrows(IllegalAccessException.class,
                 () -> translationService.saveTranslationLabels(objectType, objectId, fieldName, labels, username));

    setTranslationPlugin(true, true, 2, 1);
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabels(objectType, objectId, fieldName, labels, username));
    labels.put(locale, label);
    translationService.saveTranslationLabels(objectType, objectId, fieldName, labels, username);
  }

  public void testSaveTranslationLabel() throws ObjectNotFoundException {
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabel(null, objectId, fieldName, locale, label));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabel(objectType, 0, fieldName, locale, label));

    assertThrows(IllegalStateException.class,
                 () -> translationService.saveTranslationLabel(objectType, objectId, fieldName, locale, label));
    setTranslationPlugin(false, false, 2, 1); // Should allow saving without ACL
                                              // check
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.saveTranslationLabel(objectType, objectId, null, locale, label));

    translationService.saveTranslationLabel(objectType, objectId, fieldName, locale, label);
  }

  public void testDeleteTranslationLabels() throws IllegalAccessException, ObjectNotFoundException {
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabels(null, objectId, username));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabels(objectType, 0, username));
    assertThrows(IllegalStateException.class,
                 () -> translationService.deleteTranslationLabels(objectType, objectId, username));

    setTranslationPlugin(true, false, 2, 1);
    assertThrows(IllegalAccessException.class,
                 () -> translationService.deleteTranslationLabels(objectType, objectId, null));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabels(null, objectId));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabels(objectType, 0));

    assertThrows(IllegalAccessException.class,
                 () -> translationService.deleteTranslationLabels(objectType, objectId, username));

    setTranslationPlugin(true, true, 2, 1);
    translationService.deleteTranslationLabels(objectType, objectId, username);
  }

  public void testDeleteTranslationLabel() throws ObjectNotFoundException {
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabel(null, objectId, fieldName, locale));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabel(objectType, 0, fieldName, locale));
    assertThrows(IllegalStateException.class,
                 () -> translationService.deleteTranslationLabel(objectType, objectId, fieldName, null));

    setTranslationPlugin(false, false, 2, 1);
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabel(objectType, objectId, null, locale));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.deleteTranslationLabel(objectType, objectId, fieldName, null));

    translationService.deleteTranslationLabel(objectType, objectId, fieldName, locale);
  }

  public void testGetTranslationLabels() throws IllegalAccessException, ObjectNotFoundException {
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.getTranslationField(null, objectId, fieldName, username));
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.getTranslationField(objectType, 0, fieldName, username));
    assertThrows(IllegalStateException.class,
                 () -> translationService.getTranslationField(objectType, objectId, fieldName, username));

    setTranslationPlugin(false, false, 2, 1);
    assertThrows(IllegalArgumentException.class,
                 () -> translationService.getTranslationField(objectType, objectId, null, username));
    assertThrows(IllegalAccessException.class,
                 () -> translationService.getTranslationField(objectType, objectId, fieldName, null));

    assertThrows(IllegalAccessException.class,
                 () -> translationService.getTranslationField(objectType, objectId, fieldName, username));

    setTranslationPlugin(true, false, 2, 1);
    TranslationField translationField = translationService.getTranslationField(objectType, objectId, fieldName, username);
    assertNotNull(translationField);
    Map<Locale, String> translationLabels = translationField.getLabels();
    assertNotNull(translationLabels);
    assertTrue(translationLabels.isEmpty());

    setTranslationPlugin(true, true, 2, 1);
    Map<Locale, String> labels = new HashMap<>();
    labels.put(locale, label);
    translationService.saveTranslationLabels(objectType, objectId, fieldName, labels);

    translationField = translationService.getTranslationField(objectType, objectId, fieldName, username);
    assertNotNull(translationField);
    translationLabels = translationField.getLabels();
    assertNotNull(translationLabels);
    assertFalse(translationLabels.isEmpty());
    assertEquals(label, translationLabels.get(locale));

    String labelFr = label + "FR";
    labels.put(Locale.FRENCH, labelFr);
    translationService.saveTranslationLabels(objectType, objectId, fieldName, labels);

    translationField = translationService.getTranslationField(objectType, objectId, fieldName, username);
    assertNotNull(translationField);
    translationLabels = translationField.getLabels();
    assertNotNull(translationLabels);
    assertFalse(translationLabels.isEmpty());
    assertEquals(label, translationLabels.get(locale));
    assertEquals(labelFr, translationLabels.get(Locale.FRENCH));
  }

  public void testGetTranslationLabel() throws IllegalAccessException, ObjectNotFoundException {
    setTranslationPlugin(true, true, 2, 1);

    TranslationField translationField = translationService.getTranslationField(objectType, objectId, fieldName, username);
    assertNotNull(translationField);
    Map<Locale, String> translationLabels = translationField.getLabels();
    assertNotNull(translationLabels);
    assertTrue(translationLabels.isEmpty());

    Map<Locale, String> labels = new HashMap<>();
    labels.put(locale, label);
    translationService.saveTranslationLabels(objectType, objectId, fieldName, labels);

    String value = translationService.getTranslationLabel(objectType, objectId, fieldName, locale);
    assertEquals(label, value);

    String labelFr = label + "FR";
    labels.put(Locale.FRENCH, labelFr);
    translationService.saveTranslationLabels(objectType, objectId, fieldName, labels);

    value = translationService.getTranslationLabel(objectType, objectId, fieldName, Locale.FRENCH);
    assertEquals(labelFr, value);
  }

  private void removeTranslationPlugin() {
    translationService.removePlugin(OBJECT_TYPE);
  }

  private void setTranslationPlugin(boolean hasAccessPermission, boolean hasEditPermission, long spaceId, long audienceId) {
    removeTranslationPlugin();
    TranslationPlugin translationPlugin = new TranslationPlugin() {

      @Override
      public String getObjectType() {
        return OBJECT_TYPE;
      }

      @Override
      public boolean hasEditPermission(long objectId, String username) throws ObjectNotFoundException {
        return hasEditPermission;
      }

      @Override
      public boolean hasAccessPermission(long objectId, String username) throws ObjectNotFoundException {
        return hasAccessPermission;
      }

      @Override
      public long getSpaceId(long objectId) throws ObjectNotFoundException {
        return spaceId;
      }

      @Override
      public long getAudienceId(long objectId) throws ObjectNotFoundException {
        return audienceId;
      }
    };
    translationService.addPlugin(translationPlugin);
  }
}
