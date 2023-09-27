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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.translation.model.TranslationEvent;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.plugin.TranslationPlugin;
import io.meeds.social.translation.storage.TranslationStorage;

public class TranslationServiceImpl implements TranslationService {

  private static final Log               LOG                             = ExoLogger.getLogger(TranslationServiceImpl.class);

  private static final String            NO_PERMISSION_TO_ACCESS_MESSAGE =
                                                                         "User %s doesn't have enough permissions to access translations of object of type %s identified by %s";

  private static final String            NO_PERMISSION_TO_EDIT_MESSAGE   =
                                                                       "User %s doesn't have enough permissions to write translations of object of type %s identified by %s";

  private static final String            NO_PERMISSION_TO_DELETE_MESSAGE =
                                                                         "User %s doesn't have enough permissions to delete translations of object of type %s identified by %s";

  private ListenerService                listenerService;

  private TranslationStorage             translationStorage;

  private Map<String, TranslationPlugin> translationPlugins              = new HashMap<>();

  public TranslationServiceImpl(TranslationStorage translationStorage, ListenerService listenerService) {
    this.translationStorage = translationStorage;
    this.listenerService = listenerService;
  }

  @Override
  public void addPlugin(TranslationPlugin translationPlugin) {
    translationPlugins.put(translationPlugin.getObjectType(), translationPlugin);
  }

  @Override
  public void removePlugin(String objectType) {
    translationPlugins.remove(objectType);
  }

  @Override
  public TranslationField getTranslationField(String objectType,
                                               long objectId,
                                               String fieldName,
                                               String username) throws IllegalAccessException, ObjectNotFoundException {

    checkParameters(objectType, objectId, fieldName);
    checkAccessPermission(objectType, objectId, username);
    return getTranslationField(objectType, objectId, fieldName);
  }

  @Override
  public TranslationField getTranslationField(String objectType,
                                               long objectId,
                                               String fieldName) {
    return translationStorage.getTranslationField(objectType, objectId, fieldName);
  }

  @Override
  public String getTranslationLabel(String objectType,
                                    long objectId,
                                    String fieldName,
                                    Locale locale) {
    return translationStorage.getTranslationLabel(objectType, objectId, fieldName, locale);
  }

  @Override
  public void saveTranslationLabels(String objectType,
                                    long objectId,
                                    String fieldName,
                                    Map<Locale, String> labels,
                                    String username) throws IllegalAccessException, ObjectNotFoundException {
    checkParameters(objectType, objectId, fieldName);
    checkEditPermission(objectType, objectId, username, NO_PERMISSION_TO_EDIT_MESSAGE);
    saveTranslationLabelsNoBroadcast(objectType, objectId, fieldName, labels);
    broadcastEvent(TRANSLATION_SAVED_EVENT_NAME, objectType, objectId, fieldName, null, username);
  }

  @Override
  public void saveTranslationLabels(String objectType,
                                    long objectId,
                                    String fieldName,
                                    Map<Locale, String> labels) throws ObjectNotFoundException {
    checkParameters(objectType, objectId, fieldName);
    saveTranslationLabelsNoBroadcast(objectType, objectId, fieldName, labels);
    broadcastEvent(TRANSLATION_SAVED_EVENT_NAME, objectType, objectId, fieldName, null, null);
  }

  @Override
  public void saveTranslationLabel(String objectType,
                                   long objectId,
                                   String fieldName,
                                   Locale locale,
                                   String label) throws ObjectNotFoundException {
    checkParameters(objectType, objectId, fieldName, locale);
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    long audienceId = translationPlugin.getAudienceId(objectId);
    long spaceId = translationPlugin.getSpaceId(objectId);
    translationStorage.saveTranslationLabel(objectType, objectId, fieldName, locale, label, audienceId, spaceId);
    broadcastEvent(TRANSLATION_SAVED_EVENT_NAME, objectType, objectId, fieldName, locale, null);
  }

  @Override
  public void deleteTranslationLabels(String objectType,
                                      long objectId,
                                      String username) throws IllegalAccessException, ObjectNotFoundException {
    checkParameters(objectType, objectId);
    checkEditPermission(objectType, objectId, username, NO_PERMISSION_TO_DELETE_MESSAGE);
    deleteTranslationLabelsNoBroadcast(objectType, objectId);
    broadcastEvent(TRANSLATION_DELETED_EVENT_NAME, objectType, objectId, null, null, username);
  }

  @Override
  public void deleteTranslationLabels(String objectType, long objectId) throws ObjectNotFoundException {
    checkParameters(objectType, objectId);
    deleteTranslationLabelsNoBroadcast(objectType, objectId);
    broadcastEvent(TRANSLATION_DELETED_EVENT_NAME, objectType, objectId, null, null, null);
  }

  @Override
  public void deleteTranslationLabel(String objectType,
                                     long objectId,
                                     String fieldName,
                                     Locale locale) throws ObjectNotFoundException {
    checkParameters(objectType, objectId, fieldName, locale);
    translationStorage.deleteTranslationLabel(objectType, objectId, fieldName, locale);
    broadcastEvent(TRANSLATION_DELETED_EVENT_NAME, objectType, objectId, fieldName, locale, null);
  }

  private void saveTranslationLabelsNoBroadcast(String objectType,
                                                long objectId,
                                                String fieldName,
                                                Map<Locale, String> labels) throws ObjectNotFoundException {
    if (MapUtils.isEmpty(labels)) {
      throw new IllegalArgumentException("labels is empty");
    }
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    long audienceId = translationPlugin.getAudienceId(objectId);
    long spaceId = translationPlugin.getSpaceId(objectId);
    translationStorage.saveTranslationLabels(objectType, objectId, fieldName, labels, audienceId, spaceId);
  }

  private void deleteTranslationLabelsNoBroadcast(String objectType, long objectId) {
    translationStorage.deleteTranslationLabels(objectType, objectId);
  }

  private void broadcastEvent(String eventName,
                              String objectType,
                              long objectId,
                              String fieldName,
                              Locale locale,
                              String username) {
    try {
      listenerService.broadcast(eventName, new TranslationEvent(objectType, objectId, fieldName, locale), username);
    } catch (Exception e) {
      LOG.warn("An error occurred while broadcasting event {} for object type {} identified by {}", objectType, objectId, e);
    }
  }

  private void checkAccessPermission(String objectType, long objectId, String username) throws ObjectNotFoundException,
                                                                                        IllegalAccessException {
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    if (!translationPlugin.hasAccessPermission(objectId, username)) {
      throw new IllegalAccessException(String.format(NO_PERMISSION_TO_ACCESS_MESSAGE,
                                                     username,
                                                     objectType,
                                                     objectId));
    }
  }

  private void checkEditPermission(String objectType, long objectId, String username,
                                   String message) throws ObjectNotFoundException, IllegalAccessException {
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    if (!translationPlugin.hasEditPermission(objectId, username)) {
      throw new IllegalAccessException(String.format(message,
                                                     username,
                                                     objectType,
                                                     objectId));
    }
  }

  private void checkParameters(String objectType, long objectId, String fieldName, Locale locale) {
    checkParameters(objectType, objectId, fieldName);
    if (StringUtils.isBlank(fieldName)) {
      throw new IllegalArgumentException("Field name is mandatory");
    }
    if (locale == null) {
      throw new IllegalArgumentException("locale is mandatory");
    }
  }

  private void checkParameters(String objectType, long objectId, String fieldName) {
    checkParameters(objectType, objectId);
    if (StringUtils.isBlank(fieldName)) {
      throw new IllegalArgumentException("Field name is mandatory");
    }
  }

  private void checkParameters(String objectType, long objectId) {
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (objectId <= 0) {
      throw new IllegalArgumentException("Object identifier is mandatory");
    }
    checkObjectTypePlugin(objectType);
  }

  private void checkObjectTypePlugin(String objectType) {
    if (!translationPlugins.containsKey(objectType)) {
      throw new IllegalStateException("TranslationPlugin associated to " + objectType + " wasn't found");
    }
  }

}
