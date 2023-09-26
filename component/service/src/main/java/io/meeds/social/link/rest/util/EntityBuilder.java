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

package io.meeds.social.link.rest.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.social.rest.api.RestUtils;

import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.model.LinkWithIconAttachment;
import io.meeds.social.link.plugin.LinkSettingTranslationPlugin;
import io.meeds.social.link.plugin.LinkTranslationPlugin;
import io.meeds.social.link.rest.model.LinkRestEntity;
import io.meeds.social.link.rest.model.LinkSettingRestEntity;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;

public class EntityBuilder {

  public static final String LINK_SETTINGS_HEADER_FIELD      = "header";

  public static final String LINK_SETTINGS_NAME_FIELD        = "name";

  public static final String LINK_SETTINGS_DESCRIPTION_FIELD = "description";

  private EntityBuilder() {
    // Utils class
  }

  public static LinkSettingRestEntity build(TranslationService translationService,
                                            LocaleConfigService localeConfigService,
                                            LinkSetting linkSetting,
                                            List<Link> links,
                                            String language) {
    Map<String, String> header = getTranslations(translationService,
                                                 localeConfigService,
                                                 LinkSettingTranslationPlugin.LINK_SETTINGS_OBJECT_TYPE,
                                                 linkSetting.getId(),
                                                 LINK_SETTINGS_HEADER_FIELD,
                                                 linkSetting.getHeader(),
                                                 language);
    return new LinkSettingRestEntity(linkSetting.getId(),
                                     linkSetting.getName(),
                                     header,
                                     linkSetting.getType(),
                                     linkSetting.isLargeIcon(),
                                     linkSetting.isShowName(),
                                     linkSetting.getSeeMore(),
                                     CollectionUtils.isEmpty(links) ? Collections.emptyList()
                                                                    : links.stream()
                                                                           .map(l -> build(translationService,
                                                                                           localeConfigService,
                                                                                           linkSetting,
                                                                                           l,
                                                                                           language))
                                                                           .toList());
  }

  public static LinkRestEntity build(TranslationService translationService,
                                     LocaleConfigService localeConfigService,
                                     LinkSetting linkSetting,
                                     Link link,
                                     String language) {
    Map<String, String> name = getTranslations(translationService,
                                               localeConfigService,
                                               LinkTranslationPlugin.LINKS_OBJECT_TYPE,
                                               link.getId(),
                                               LINK_SETTINGS_NAME_FIELD,
                                               link.getName(),
                                               language);
    Map<String, String> description = getTranslations(translationService,
                                                      localeConfigService,
                                                      LinkTranslationPlugin.LINKS_OBJECT_TYPE,
                                                      link.getId(),
                                                      LINK_SETTINGS_DESCRIPTION_FIELD,
                                                      link.getDescription(),
                                                      language);
    return new LinkRestEntity(link.getId(),
                              name,
                              description,
                              link.getUrl(),
                              link.isSameTab(),
                              link.getOrder(),
                              buildLinkIconUrl(linkSetting, link),
                              link.getIconFileId(),
                              null);
  }

  public static String buildLinkIconUrl(LinkSetting linkSetting, Link link) {
    return RestUtils.getBaseRestUrl() + "/links/" + linkSetting.getName() + "/" + link.getId() + "?v="
        + Objects.hash(linkSetting.getLastModified());
  }

  public static LinkSetting toLinkSetting(LocaleConfigService localeConfigService, LinkSettingRestEntity linkSettingEntity) {
    return new LinkSetting(linkSettingEntity.getId(),
                           linkSettingEntity.getName(),
                           null,
                           linkSettingEntity.getHeader().get(localeConfigService.getDefaultLocaleConfig().getLanguage()),
                           linkSettingEntity.getType(),
                           linkSettingEntity.isLargeIcon(),
                           linkSettingEntity.isShowName(),
                           linkSettingEntity.getSeeMore(),
                           0);
  }

  public static List<Link> toLinks(LocaleConfigService localeConfigService, LinkSettingRestEntity linkSettingEntity) {
    List<LinkRestEntity> links = linkSettingEntity.getLinks();
    return CollectionUtils.isEmpty(links) ? Collections.emptyList()
                                          : links.stream().map(l -> toLink(localeConfigService, l)).toList();
  }

  public static Link toLink(LocaleConfigService localeConfigService, LinkRestEntity linkEntity) {
    if (StringUtils.isBlank(linkEntity.getIconUploadId())) {
      return new Link(linkEntity.getId(),
                      linkEntity.getName().get(localeConfigService.getDefaultLocaleConfig().getLanguage()),
                      linkEntity.getDescription().get(localeConfigService.getDefaultLocaleConfig().getLanguage()),
                      linkEntity.getUrl(),
                      linkEntity.isSameTab(),
                      linkEntity.getOrder(),
                      linkEntity.getIconFileId());
    } else {
      return new LinkWithIconAttachment(linkEntity.getId(),
                            linkEntity.getName().get(localeConfigService.getDefaultLocaleConfig().getLanguage()),
                            linkEntity.getDescription().get(localeConfigService.getDefaultLocaleConfig().getLanguage()),
                            linkEntity.getUrl(),
                            linkEntity.isSameTab(),
                            linkEntity.getOrder(),
                            linkEntity.getIconFileId(),
                            linkEntity.getIconUploadId());
    }
  }

  private static Map<String, String> getTranslations(TranslationService translationService,
                                                     LocaleConfigService localeConfigService,
                                                     String objectType,
                                                     long objectId,
                                                     String fieldName,
                                                     String defaultValue,
                                                     String language) {
    if (StringUtils.isBlank(language)) {
      TranslationField translationField;
      try {
        translationField = translationService.getTranslationField(objectType, objectId, fieldName);
      } catch (ObjectNotFoundException e) {
        translationField = null;
      }
      return toTranslations(localeConfigService, translationField, defaultValue);
    } else {
      String label = translationService.getTranslationLabel(objectType, objectId, fieldName, Locale.forLanguageTag(language));
      if (StringUtils.isBlank(label)) {
        return Collections.singletonMap(language, defaultValue);
      } else {
        return Collections.singletonMap(language, label);
      }
    }
  }

  private static Map<String, String> toTranslations(LocaleConfigService localeConfigService,
                                                    TranslationField translationField,
                                                    String defaultValue) {
    boolean hasTranslation = translationField == null || MapUtils.isEmpty(translationField.getLabels());
    return hasTranslation ? Collections.singletonMap(localeConfigService.getDefaultLocaleConfig().getLanguage(), defaultValue)
                          : translationField.getLabels()
                                            .entrySet()
                                            .stream()
                                            .collect(Collectors.toMap(e -> e.getKey().toLanguageTag(), Entry::getValue));
  }

}
