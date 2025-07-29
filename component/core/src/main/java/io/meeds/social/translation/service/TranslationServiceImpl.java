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
package io.meeds.social.translation.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfigService;

import io.meeds.social.html.model.HtmlProcessorContext;
import io.meeds.social.html.model.HtmlTransformerContext;
import io.meeds.social.html.utils.HtmlUtils;
import io.meeds.social.translation.model.TranslationEvent;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.plugin.TranslationPlugin;
import io.meeds.social.translation.storage.TranslationStorage;
import org.exoplatform.social.core.utils.MentionUtils;

public class TranslationServiceImpl implements TranslationService {

  private static final Log               LOG                             = ExoLogger.getLogger(TranslationServiceImpl.class);

  private static final String            NO_PERMISSION_TO_ACCESS_MESSAGE =
                                                                         "User %s doesn't have enough permissions to access translations of object of type %s identified by %s";

  private static final String            NO_PERMISSION_TO_EDIT_MESSAGE   =
                                                                       "User %s doesn't have enough permissions to write translations of object of type %s identified by %s";

  private static final String            NO_PERMISSION_TO_DELETE_MESSAGE =
                                                                         "User %s doesn't have enough permissions to delete translations of object of type %s identified by %s";

  private String                         portalOwner;

  private LocaleConfigService            localeConfigService;

  private ListenerService                listenerService;

  private TranslationStorage             translationStorage;

  private Map<String, TranslationPlugin> translationPlugins              = new HashMap<>();

  public TranslationServiceImpl(TranslationStorage translationStorage,
                                LocaleConfigService localeConfigService,
                                ListenerService listenerService) {
    this.localeConfigService = localeConfigService;
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
                                              String objectId,
                                              String fieldName,
                                              String username) throws IllegalAccessException, ObjectNotFoundException {

    checkParameters(objectType, objectId, fieldName);
    checkAccessPermission(objectType, objectId, username);
    return getTranslationField(objectType, objectId, fieldName);
  }

  @Override
  public TranslationField getTranslationField(String objectType,
                                              String objectId,
                                              String fieldName) {
    TranslationField translationField = translationStorage.getTranslationField(objectType, objectId, fieldName);
    if (translationField != null && translationField.getLabels() != null) {
      Map<Locale, String> labels = translationField.getLabels();
      labels = labels.entrySet()
                     .stream()
                     .map(entry -> Pair.of(entry.getKey(),
                                           HtmlUtils.transform(entry.getValue(),
                                                               new HtmlTransformerContext(entry.getKey()))))
                     .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      translationField.setLabels(labels);
    }
    return translationField;
  }

  @Override
  public Map<String, TranslationField> getAllTranslationFields(String objectType,
                                                               String objectId,
                                                               String username) throws IllegalAccessException,
                                                                                ObjectNotFoundException {
    checkParameters(objectType, objectId);
    checkAccessPermission(objectType, objectId, username);
    return getAllTranslationFields(objectType, objectId);
  }

  @Override
  public Map<String, TranslationField> getAllTranslationFields(String objectType,
                                                               String objectId) throws ObjectNotFoundException {
    Map<String, TranslationField> translationFields = translationStorage.getAllTranslationFields(objectType, objectId);
    for (Entry<String, TranslationField> entry : translationFields.entrySet()) {
      TranslationField translationField = entry.getValue();
      if (translationField != null && translationField.getLabels() != null) {
        Map<Locale, String> labels = translationField.getLabels();
        labels = labels.entrySet()
                       .stream()
                       .map(labelEntry -> Pair.of(labelEntry.getKey(),
                                                  HtmlUtils.transform(labelEntry.getValue(),
                                                                      new HtmlTransformerContext(labelEntry.getKey()))))
                       .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        translationField.setLabels(labels);
      }
    }
    return translationFields;
  }

  @Override
  public String getTranslationLabel(String objectType,
                                    String objectId,
                                    String fieldName,
                                    Locale locale) {
    String translationLabel = translationStorage.getTranslationLabel(objectType, objectId, fieldName, locale);
    if (StringUtils.isNotBlank(translationLabel)) {
      translationLabel = HtmlUtils.transform(translationLabel,
                                             new HtmlTransformerContext(locale));
    }
    return translationLabel;
  }

  @Override
  public String getTranslationLabelOrDefault(String objectType,
                                             String objectId,
                                             String fieldName,
                                             Locale locale) {
    if (locale == null) {
      locale = localeConfigService.getDefaultLocaleConfig().getLocale();
    }
    TranslationField translationField = getTranslationField(objectType,
                                                            objectId,
                                                            fieldName);
    if (translationField != null && MapUtils.isNotEmpty(translationField.getLabels())) {
      String label = translationField.getLabels().get(locale);
      if (label == null) {
        Locale defaultLocale = localeConfigService.getDefaultLocaleConfig().getLocale();
        label = translationField.getLabels().get(defaultLocale);
      }
      if (label == null) {
        label = translationField.getLabels().values().iterator().next();
      }
      return label;
    } else {
      return null;
    }
  }

  @Override
  public void saveTranslationLabels(String objectType,
                                    String objectId,
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
                                    String objectId,
                                    String fieldName,
                                    Map<Locale, String> labels) throws ObjectNotFoundException {
    checkParameters(objectType, objectId, fieldName);
    saveTranslationLabelsNoBroadcast(objectType, objectId, fieldName, labels);
    broadcastEvent(TRANSLATION_SAVED_EVENT_NAME, objectType, objectId, fieldName, null, null);
  }

  @Override
  public void saveTranslationLabel(String objectType,
                                   String objectId,
                                   String fieldName,
                                   Locale locale,
                                   String label) throws ObjectNotFoundException {
    checkParameters(objectType, objectId, fieldName, locale);
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    long audienceId = translationPlugin.getAudienceId(objectId);
    long spaceId = translationPlugin.getSpaceId(objectId);
    label = processLabelLocale(objectType, objectId, fieldName, locale, label);
    translationStorage.saveTranslationLabel(objectType, objectId, fieldName, locale, label, audienceId, spaceId);
    broadcastEvent(TRANSLATION_SAVED_EVENT_NAME, objectType, objectId, fieldName, locale, null);
  }

  @Override
  public void deleteTranslationLabels(String objectType,
                                      String objectId,
                                      String username) throws IllegalAccessException, ObjectNotFoundException {
    checkParameters(objectType, objectId);
    checkEditPermission(objectType, objectId, username, NO_PERMISSION_TO_DELETE_MESSAGE);
    deleteTranslationLabelsNoBroadcast(objectType, objectId);
    broadcastEvent(TRANSLATION_DELETED_EVENT_NAME, objectType, objectId, null, null, username);
  }

  @Override
  public void deleteTranslationLabels(String objectType, String objectId) throws ObjectNotFoundException {
    checkParameters(objectType, objectId);
    deleteTranslationLabelsNoBroadcast(objectType, objectId);
    broadcastEvent(TRANSLATION_DELETED_EVENT_NAME, objectType, objectId, null, null, null);
  }

  @Override
  public void deleteTranslationLabel(String objectType,
                                     String objectId,
                                     String fieldName,
                                     Locale locale) throws ObjectNotFoundException {
    checkParameters(objectType, objectId, fieldName, locale);
    translationStorage.deleteTranslationLabel(objectType, objectId, fieldName, locale);
    processLabelLocale(objectType, objectId, fieldName, locale, StringUtils.EMPTY);
    broadcastEvent(TRANSLATION_DELETED_EVENT_NAME, objectType, objectId, fieldName, locale, null);
  }

  private void saveTranslationLabelsNoBroadcast(String objectType,
                                                String objectId,
                                                String fieldName,
                                                Map<Locale, String> labels) throws ObjectNotFoundException {
    if (MapUtils.isEmpty(labels)) {
      throw new IllegalArgumentException("labels is empty");
    }
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    long audienceId = translationPlugin.getAudienceId(objectId);
    long spaceId = translationPlugin.getSpaceId(objectId);
    labels = labels.entrySet()
                   .stream()
                   .map(entry -> Pair.of(entry.getKey(),
                                         processLabelLocale(objectType,
                                                            objectId,
                                                            fieldName,
                                                            entry.getKey(),
                                                            entry.getValue())))
                   .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    Set<Locale> labelLocales = labels.keySet();

    translationStorage.saveTranslationLabels(objectType, objectId, fieldName, labels, audienceId, spaceId);
    processLabelLocalesDeletion(objectType, objectId, fieldName, labelLocales);
  }

  private void deleteTranslationLabelsNoBroadcast(String objectType, String objectId) {
    translationStorage.deleteTranslationLabels(objectType, objectId);
    processLabelLocalesDeletion(objectType, objectId, null, Collections.emptySet());
  }

  private void processLabelLocalesDeletion(String objectType, String objectId, String fieldName, Set<Locale> labelLocales) {
    // Process deletion of Field Translations
    TranslationField translationField = getTranslationField(objectType, objectId, fieldName);
    if (translationField != null && MapUtils.isNotEmpty(translationField.getLabels())) {
      translationField.getLabels()
                      .keySet()
                      .stream()
                      .filter(locale -> !labelLocales.contains(locale))
                      .forEach(locale -> processLabelLocale(objectType,
                                                            objectId,
                                                            fieldName,
                                                            locale,
                                                            StringUtils.EMPTY));
    }
  }

  private String processLabelLocale(String objectType,
                                    String objectId,
                                    String fieldName,
                                    Locale locale,
                                    String label) {
    return HtmlUtils.process(processMentions(label), new HtmlProcessorContext(objectType, String.valueOf(objectId), fieldName, locale));
  }

  private void broadcastEvent(String eventName,
                              String objectType,
                              String objectId,
                              String fieldName,
                              Locale locale,
                              String username) {
    try {
      listenerService.broadcast(eventName, new TranslationEvent(objectType, objectId, fieldName, locale), username);
    } catch (Exception e) {
      LOG.warn("An error occurred while broadcasting event {} for object type {} identified by {}", objectType, objectId, e);
    }
  }

  private void checkAccessPermission(String objectType, String objectId, String username) throws ObjectNotFoundException,
                                                                                          IllegalAccessException {
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    if (!translationPlugin.hasAccessPermission(objectId, username)) {
      throw new IllegalAccessException(String.format(NO_PERMISSION_TO_ACCESS_MESSAGE,
                                                     username,
                                                     objectType,
                                                     objectId));
    }
  }

  private void checkEditPermission(String objectType,
                                   String objectId,
                                   String username,
                                   String message) throws ObjectNotFoundException, IllegalAccessException {
    TranslationPlugin translationPlugin = translationPlugins.get(objectType);
    if (!translationPlugin.hasEditPermission(objectId, username)) {
      throw new IllegalAccessException(String.format(message,
                                                     username,
                                                     objectType,
                                                     objectId));
    }
  }

  private void checkParameters(String objectType, String objectId, String fieldName, Locale locale) {
    checkParameters(objectType, objectId, fieldName);
    if (StringUtils.isBlank(fieldName)) {
      throw new IllegalArgumentException("Field name is mandatory");
    }
    if (locale == null) {
      throw new IllegalArgumentException("locale is mandatory");
    }
  }

  private void checkParameters(String objectType, String objectId, String fieldName) {
    checkParameters(objectType, objectId);
    if (StringUtils.isBlank(fieldName)) {
      throw new IllegalArgumentException("Field name is mandatory");
    }
  }

  private void checkParameters(String objectType, String objectId) {
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("Object identifier is mandatory");
    }
    checkObjectTypePlugin(objectType);
  }

  private void checkObjectTypePlugin(String objectType) {
    if (!translationPlugins.containsKey(objectType)) {
      throw new IllegalStateException("TranslationPlugin associated to " + objectType + " wasn't found");
    }
  }

  private String processMentions(String label) {
    String sanitizedBody = HTMLSanitizer.sanitize(label);
    sanitizedBody = sanitizedBody.replace("&#64;", "@");
    return MentionUtils.substituteUsernames(getPortalOwner(), sanitizedBody);
  }

  private String getPortalOwner() {
    if (portalOwner == null) {
      portalOwner = CommonsUtils.getCurrentPortalOwner();
    }
    return portalOwner;
  }

}
