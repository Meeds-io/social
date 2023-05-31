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
package io.meeds.social.translation.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

import io.meeds.social.translation.model.TranslationField;

public class TranslationStorage {

  private static final Log         LOG                  = ExoLogger.getLogger(TranslationStorage.class);

  private static final String      LABEL_PROPERTY_NAME  = "label";

  private static final String      LOCALE_PROPERTY_NAME = "locale";

  public static final MetadataType METADATA_TYPE        = new MetadataType(255, "translation");

  private MetadataService          metadataService;

  public TranslationStorage(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  public TranslationField getTranslationField(String objectType, long objectId, String fieldName) {
    Map<Locale, String> labels = getLabels(objectType, objectId, fieldName);
    return new TranslationField(objectType, objectId, fieldName, labels, System.currentTimeMillis());
  }

  public Map<Locale, String> getTranslationLabels(String objectType, long objectId, String fieldName) {
    return getTranslationField(objectType, objectId, fieldName).getLabels();
  }

  public String getTranslationLabel(String objectType, long objectId, String fieldName, Locale locale) {
    return getTranslationLabels(objectType, objectId, fieldName).get(locale);
  }

  public void saveTranslationLabels(String objectType,
                                    long objectId,
                                    String fieldName,
                                    Map<Locale, String> labels,
                                    long audienceId,
                                    long spaceId) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(fieldName,
                                                                                                      METADATA_TYPE.getName(),
                                                                                                      objectType,
                                                                                                      String.valueOf(objectId),
                                                                                                      0,
                                                                                                      -1);
    if (CollectionUtils.isNotEmpty(metadataItems)) {
      metadataItems.stream()
                   .map(MetadataItem::getId)
                   .forEach(id -> {
                     try {
                       metadataService.deleteMetadataItem(id, false);
                     } catch (ObjectNotFoundException e) {
                       LOG.debug("Metadata Item with id {} not found, ignore deleting it", id, e);
                     }
                   });
    }
    labels.forEach((locale, label) -> createMetadataItem(getMetadataKey(fieldName, audienceId),
                                                         getMetadataObject(objectType, objectId, spaceId),
                                                         locale,
                                                         label));
  }

  public void saveTranslationLabel(String objectType,
                                   long objectId,
                                   String fieldName,
                                   Locale locale,
                                   String label,
                                   long audienceId,
                                   long spaceId) {
    Map<Locale, String> translationLabels = getTranslationLabels(objectType, objectId, fieldName);
    translationLabels.put(locale, label);
    saveTranslationLabels(objectType, objectId, fieldName, translationLabels, audienceId, spaceId);
  }

  public void deleteTranslationLabels(String objectType, long objectId) {
    metadataService.deleteMetadataItemsByMetadataTypeAndObject(METADATA_TYPE.getName(),
                                                               getMetadataObject(objectType, objectId, 0));
  }

  public void deleteTranslationLabel(String objectType, long objectId, String fieldName, Locale locale) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(fieldName,
                                                                                                      METADATA_TYPE.getName(),
                                                                                                      objectType,
                                                                                                      String.valueOf(objectId),
                                                                                                      0,
                                                                                                      -1);
    if (CollectionUtils.isNotEmpty(metadataItems)) {
      metadataItems.stream()
                   .filter(item -> MapUtils.isNotEmpty(item.getProperties())
                       && StringUtils.equals(item.getProperties().get(LOCALE_PROPERTY_NAME), locale.toLanguageTag()))
                   .map(MetadataItem::getId)
                   .forEach(id -> {
                     try {
                       metadataService.deleteMetadataItem(id, false);
                     } catch (ObjectNotFoundException e) {
                       LOG.debug("Metadata Item with id {} not found, ignore deleting it", id, e);
                     }
                   });
    }
  }

  private void createMetadataItem(MetadataKey metadataKey, MetadataObject metadataObject, Locale locale, String label) {
    try {
      Map<String, String> properties = new HashMap<>();
      properties.put(LOCALE_PROPERTY_NAME, locale.toLanguageTag());
      properties.put(LABEL_PROPERTY_NAME, label);
      metadataService.createMetadataItem(metadataObject, metadataKey, properties);
    } catch (Exception e) {
      throw new IllegalStateException(String.format("An error occurred while saving metadata items of %s with id %s",
                                                    metadataKey.getName(),
                                                    metadataObject.getId()),
                                      e);
    }
  }

  private MetadataKey getMetadataKey(String fieldName, long audienceId) {
    return new MetadataKey(METADATA_TYPE.getName(),
                           fieldName,
                           audienceId);
  }

  private MetadataObject getMetadataObject(String objectType, long objectId, long spaceId) {
    return new MetadataObject(objectType,
                              String.valueOf(objectId),
                              null,
                              spaceId);
  }

  private Map<Locale, String> getLabels(String objectType, long objectId, String fieldName) {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(fieldName,
                                                                                                      METADATA_TYPE.getName(),
                                                                                                      objectType,
                                                                                                      String.valueOf(objectId),
                                                                                                      0,
                                                                                                      -1);
    if (CollectionUtils.isEmpty(metadataItems)) {
      return new HashMap<>();
    } else {
      return metadataItems.stream()
                          .filter(item -> MapUtils.isNotEmpty(item.getProperties()))
                          .collect(Collectors.toMap(item -> Locale.forLanguageTag(item.getProperties().get(LOCALE_PROPERTY_NAME)),
                                                    item -> item.getProperties().get(LABEL_PROPERTY_NAME)));
    }
  }

}
