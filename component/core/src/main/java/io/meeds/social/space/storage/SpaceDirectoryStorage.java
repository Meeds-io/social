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
package io.meeds.social.space.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import io.meeds.social.space.model.SpaceDirectorySettings;
import io.meeds.social.util.JsonUtils;

import lombok.Setter;

@Component
public class SpaceDirectoryStorage {

  public static final Context CONTEXT = Context.GLOBAL.id("SpaceDirectory");

  public static final Scope   SCOPE   = Scope.APPLICATION.id("SpaceDirectory");

  @Autowired
  @Setter
  private SettingService    settingService;

  @Cacheable("social.spaceDirectorySetting")
  public SpaceDirectorySettings get(String settingName) {
    SettingValue<?> settingValue = settingService.get(CONTEXT, SCOPE, settingName);
    return settingValue == null || settingValue.getValue() == null ? null :
                                                                   JsonUtils.fromJsonString(settingValue.getValue().toString(),
                                                                                            SpaceDirectorySettings.class);
  }

  @CacheEvict(cacheNames = "social.spaceDirectorySetting", key = "#p0")
  public void save(String settingName, SpaceDirectorySettings spaceDirectorySettings) {
    settingService.set(CONTEXT, SCOPE, settingName, SettingValue.create(JsonUtils.toJsonString(spaceDirectorySettings)));
  }

  @CacheEvict(cacheNames = "social.spaceDirectorySetting", key = "#p0")
  public void remove(String settingName) {
    settingService.remove(CONTEXT, SCOPE, settingName);
  }

}
