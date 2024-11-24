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
package io.meeds.social.navigation.plugin;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.services.resources.LocaleConfigService;

import io.meeds.social.navigation.model.SidebarItem;

public class SidebarPluginUtils {

  public static String getNameFromProperties(LocaleConfigService localeConfigService,
                                             SidebarItem item,
                                             String propName,
                                             Locale locale) {
    String names = item.getProperties().get(propName);
    JSONObject jsonObject = new JSONObject(names);
    String lang = getLocaleName(locale);
    Object name = getName(jsonObject, lang);
    if (name == null) {
      lang = getLocaleName(localeConfigService.getDefaultLocaleConfig().getLocale());
      name = getName(jsonObject, lang);
      if (name == null) {
        lang = jsonObject.keys().next();
        name = getName(jsonObject, lang);
      }
    }
    return String.valueOf(name);
  }

  public static String getLocaleName(Locale locale) {
    return locale.toLanguageTag().replace("-", "_"); // Use same name as
                                                     // localeConfigService
  }

  public static Object getName(JSONObject jsonObject, String lang) {
    try {
      return jsonObject.get(lang);
    } catch (JSONException e) {
      return null;
    }
  }

}
