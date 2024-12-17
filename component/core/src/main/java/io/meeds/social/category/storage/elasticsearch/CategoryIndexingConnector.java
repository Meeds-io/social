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
package io.meeds.social.category.storage.elasticsearch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.social.core.search.DocumentWithMetadata;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.plugin.CategoryTranslationPlugin;
import io.meeds.social.category.storage.CategoryStorage;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;

import lombok.SneakyThrows;

public class CategoryIndexingConnector extends ElasticIndexingServiceConnector {

  public static final String  TYPE         = "category";

  public static final String  ES_MAPPING   = """
          {
             "properties" : {
              "id" : {"type" : "keyword"},
              @name_mappings@,
              "ownerId" : {"type" : "keyword"},
              "parentId" : {"type" : "keyword"},
              "creatorId" : {"type" : "keyword"},
              "icon" : {"type" : "keyword"},
              "accessPermissionIds" : {"type" : "keyword"},
              "linkPermissionIds" : {"type" : "keyword"},
              "lastUpdatedDate" : {"type" : "date", "format": "epoch_millis"}
            }
          }
      """;

  public static final String  NAME_MAPPING = """
        "@name@" : {
          "type" : "text",
          "analyzer": "ngram_analyzer",
          "search_analyzer": "ngram_analyzer_search",
          "index_options": "offsets",
          "fields": {
            "raw": {
              "type": "keyword"
            }
          }
        }
      """;

  private CategoryStorage     categoryStorage;

  private TranslationService  translationService;

  private LocaleConfigService localeConfigService;

  public CategoryIndexingConnector(CategoryStorage categoryStorage,
                                   TranslationService translationService,
                                   LocaleConfigService localeConfigService,
                                   InitParams initParams) {
    super(initParams);
    this.categoryStorage = categoryStorage;
    this.translationService = translationService;
    this.localeConfigService = localeConfigService;
  }

  @Override
  public String getConnectorName() {
    return TYPE;
  }

  @Override
  public Document create(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }

    Category category = categoryStorage.getCategory(Long.parseLong(id));
    if (category == null) {
      return null;
    }

    Map<String, String> fields = new HashMap<>();
    fields.put("id", String.valueOf(category.getId()));
    fields.put("ownerId", String.valueOf(category.getOwnerId()));
    fields.put("parentId", String.valueOf(category.getParentId()));
    fields.put("creatorId", String.valueOf(category.getCreatorId()));
    fields.put("icon", String.valueOf(category.getIcon()));

    DocumentWithMetadata document = new DocumentWithMetadata();
    document.setId(id);
    document.setLastUpdatedDate(new Date());
    document.setFields(fields);
    if (CollectionUtils.isNotEmpty(category.getAccessPermissionIds())) {
      document.addListField("accessPermissionIds", category.getAccessPermissionIds().stream().map(String::valueOf).toList());
    }
    if (CollectionUtils.isNotEmpty(category.getLinkPermissionIds())) {
      document.addListField("linkPermissionIds", category.getLinkPermissionIds().stream().map(String::valueOf).toList());
    }
    addTranslatedNames(category, document);
    return document;
  }

  @Override
  public Document update(String id) {
    return create(id);
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getMapping() {
    String nameMappings = localeConfigService.getLocalConfigs()
                                             .stream()
                                             .map(l -> NAME_MAPPING.replace("@name@", "name." + toLanguageTag(l)))
                                             .collect(Collectors.joining(",\n"));
    return ES_MAPPING.replace("@name_mappings@", nameMappings);
  }

  @SneakyThrows
  private void addTranslatedNames(Category category, DocumentWithMetadata document) {
    TranslationField translationField = translationService.getTranslationField(CategoryTranslationPlugin.OBJECT_TYPE,
                                                                               category.getId(),
                                                                               CategoryTranslationPlugin.NAME_FIELD);
    if (translationField != null && MapUtils.isNotEmpty(translationField.getLabels())) {
      localeConfigService.getLocalConfigs()
                         .forEach(localeConfig -> document.addField("name." + toLanguageTag(localeConfig),
                                                                    getTranslationLabelOrDefault(translationField,
                                                                                                 localeConfig.getLocale())));
    }
  }

  private String getTranslationLabelOrDefault(TranslationField translationField,
                                              Locale locale) {
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

  private String toLanguageTag(LocaleConfig localeConfig) {
    return localeConfig.getLocale().toLanguageTag();
  }

}
