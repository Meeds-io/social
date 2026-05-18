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

import static io.meeds.social.space.storage.SpaceDirectoryStorage.CONTEXT;
import static io.meeds.social.space.storage.SpaceDirectoryStorage.SCOPE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;

import io.meeds.social.space.model.SpaceDirectorySettings;
import io.meeds.social.util.JsonUtils;

@SpringBootTest(classes = {
  SpaceDirectoryStorage.class,
})
@RunWith(SpringRunner.class)
public class SpaceDirectoryStorageTest {

  private static final String   SETTING_NAME = "settingName";

  @MockitoBean
  private SettingService        settingService;

  @Autowired
  private SpaceDirectoryStorage spaceDirectoryStorage;

  @Test
  public void testSave() {
    SpaceDirectorySettings spaceDirectorySettings = new SpaceDirectorySettings();
    spaceDirectoryStorage.save(SETTING_NAME, spaceDirectorySettings);
    verify(settingService).set(eq(CONTEXT),
                               eq(SCOPE),
                               eq(SETTING_NAME),
                               argThat(s -> s.getValue().toString().equals(JsonUtils.toJsonString(spaceDirectorySettings))));
  }

  @Test
  public void testRemove() {
    spaceDirectoryStorage.remove(SETTING_NAME);
    verify(settingService).remove(CONTEXT,
                                  SCOPE,
                                  SETTING_NAME);
  }

  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testGet() {
    assertNull(spaceDirectoryStorage.get(SETTING_NAME));
    when(settingService.get(CONTEXT, SCOPE, SETTING_NAME)).thenReturn((SettingValue) SettingValue.create("{}"));
    assertNotNull(spaceDirectoryStorage.get(SETTING_NAME));
  }

}
