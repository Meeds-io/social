/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.digest.service;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.services.resources.ResourceBundleService;
import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * Translates the label of a digest category. The categories of the addons are
 * translated in the language of the user here and not in the browser, because
 * the settings page only loads the resource bundles of the platform: the labels
 * of an addon are read from the bundle its own notification plugins already
 * use, so an addon has nothing more to declare than what it declares today.
 */
@Component
public class DigestCategoryLabelResolver {

  private final PluginSettingService  pluginSettingService;

  private final ResourceBundleService resourceBundleService;

  public DigestCategoryLabelResolver(PluginSettingService pluginSettingService, ResourceBundleService resourceBundleService) {
    this.pluginSettingService = pluginSettingService;
    this.resourceBundleService = resourceBundleService;
  }

  /**
   * @param category the category to display
   * @param locale the language to translate the label in, the request one:
   *          these REST services are served by the social webapp, which doesn't
   *          fill the portal locale of the thread
   * @return the label of the category in the language of the user, its
   *         identifier when no bundle holds it
   */
  public String getLabel(DigestCategoryProvider category, Locale locale) {
    Locale userLocale = locale == null ? Locale.ENGLISH : locale;
    String labelKey = category.getLabelKey();
    List<String> pluginIds = category.getPluginIds();
    if (StringUtils.isBlank(labelKey) || pluginIds == null) {
      return category.getId();
    }
    for (String pluginId : pluginIds) {
      String label = getLabel(labelKey, pluginId, userLocale);
      if (label != null) {
        return label;
      }
    }
    return category.getId();
  }

  private String getLabel(String labelKey, String pluginId, Locale userLocale) {
    PluginConfig pluginConfig = pluginSettingService.getPluginConfig(pluginId);
    if (pluginConfig == null || StringUtils.isBlank(pluginConfig.getBundlePath())) {
      return null;
    }
    ResourceBundle bundle = resourceBundleService.getResourceBundle(pluginConfig.getBundlePath(), userLocale);
    return bundle != null && bundle.containsKey(labelKey) ? bundle.getString(labelKey) : null;
  }

}
