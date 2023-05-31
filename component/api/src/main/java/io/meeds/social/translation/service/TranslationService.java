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

import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.plugin.TranslationPlugin;

public interface TranslationService {

  String TRANSLATION_SAVED_EVENT_NAME   = "translation.saved";

  String TRANSLATION_DELETED_EVENT_NAME = "translation.deleted";

  /**
   * Retrieves the list of Translation Labels for a given field of an Object
   * (identified by its type and id). This will ensure at the same time to check
   * the User ACL to know whether it can access object or not.
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @param  fieldName               Object field
   * @param  username                user name accessing the list, used for ACL
   *                                   check
   * @return                         {@link TranslationField} of Translations
   *                                 with a corresponding label for each saved
   *                                 {@link Locale}
   * @throws IllegalAccessException  When user doesn't have access permission to
   *                                   object
   * @throws ObjectNotFoundException When object wasn't found
   */
  TranslationField getTranslationField(String objectType,
                                       long objectId,
                                       String fieldName,
                                       String username) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * Retrieves the list of Translation Labels for a given field of an Object
   * (identified by its type and id).
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @param  fieldName               Object field
   * @return                         {@link TranslationField} of Translations
   *                                 with a corresponding label for each saved
   *                                 {@link Locale}
   * @throws ObjectNotFoundException When object wasn't found
   */
  TranslationField getTranslationField(String objectType,
                                       long objectId,
                                       String fieldName) throws ObjectNotFoundException;

  /**
   * Retrieves the Translation Label for a given field of an Object (identified
   * by its type and id) with a designated {@link Locale}. not.
   * 
   * @param  objectType Object type for which the Translation Metadata will be
   *                      attached
   * @param  objectId   Object unique identifier
   * @param  fieldName  Object field
   * @param  locale     {@link Locale}
   * @return            the translated label for a given {@link Locale}
   */
  String getTranslationLabel(String objectType,
                             long objectId,
                             String fieldName,
                             Locale locale);

  /**
   * Saves Translation Labels for a given Object's field. This will replace any
   * existing list of translations
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @param  fieldName               Object field
   * @param  labels                  {@link Map} of Translations with a
   *                                   corresponding label for each
   *                                   {@link Locale}
   * @param  username                user name updating the list, used for ACL
   *                                   check
   * @throws IllegalAccessException  When user doesn't have write permission to
   *                                   object
   * @throws ObjectNotFoundException When object wasn't found
   */
  void saveTranslationLabels(String objectType,
                             long objectId,
                             String fieldName,
                             Map<Locale, String> labels,
                             String username) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * Saves Translation Labels for a given Object's field. This will replace any
   * existing list of translations
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @param  fieldName               Object field
   * @param  labels                  {@link Map} of Translations with a
   *                                   corresponding label for each
   *                                   {@link Locale}
   * @throws ObjectNotFoundException When object wasn't found
   */
  void saveTranslationLabels(String objectType,
                             long objectId,
                             String fieldName,
                             Map<Locale, String> labels) throws ObjectNotFoundException;

  /**
   * Saves a single Translation Label with a given {@link Locale} for a given
   * Object's field. This will not replace other existing translation for other
   * languages
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @param  fieldName               Object field
   * @param  locale                  {@link Locale}
   * @param  label                   Translation label value
   * @throws ObjectNotFoundException When object wasn't found
   */
  void saveTranslationLabel(String objectType,
                            long objectId,
                            String fieldName,
                            Locale locale,
                            String label) throws ObjectNotFoundException;

  /**
   * Deletes the list of translations for a given object
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @param  username                user name deleting the list, used for ACL
   *                                   check
   * @throws IllegalAccessException  When user doesn't have write permission to
   *                                   object
   * @throws ObjectNotFoundException When object wasn't found
   */
  void deleteTranslationLabels(String objectType,
                               long objectId,
                               String username) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * Deletes the list of translations for a given object
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @throws ObjectNotFoundException When object wasn't found
   */
  void deleteTranslationLabels(String objectType, long objectId) throws ObjectNotFoundException;

  /**
   * Deletes a translation label for a given object's field
   * 
   * @param  objectType              Object type for which the Translation
   *                                   Metadata will be attached
   * @param  objectId                Object unique identifier
   * @param  fieldName               Object field
   * @param  locale                  {@link Locale}
   * @throws ObjectNotFoundException When object wasn't found
   */
  void deleteTranslationLabel(String objectType,
                              long objectId,
                              String fieldName,
                              Locale locale) throws ObjectNotFoundException;

  /**
   * Add a new {@link TranslationPlugin} for a given Object Type
   * 
   * @param translationPlugin {@link TranslationPlugin}
   */
  void addPlugin(TranslationPlugin translationPlugin);

  /**
   * Removes a {@link TranslationPlugin} identified by its objectType
   * 
   * @param objectType Object type
   */
  void removePlugin(String objectType);
}
