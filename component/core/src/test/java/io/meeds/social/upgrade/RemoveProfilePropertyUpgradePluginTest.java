/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.upgrade;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class RemoveProfilePropertyUpgradePluginTest {

  private ProfilePropertyService profilePropertyService;

  private InitParams             initParams;

  private ValuesParam            valuesParam;

  @BeforeEach
  public void setUp() {
    profilePropertyService = mock(ProfilePropertyService.class);
    initParams = mock(InitParams.class);
    valuesParam = mock(ValuesParam.class);
  }

  @Test
  public void testProcessUpgrade() {
    // Arrange
    String propertyName = "phones";
    when(initParams.getValuesParam("targetProperties")).thenReturn(valuesParam);
    when(valuesParam.getValues()).thenReturn(List.of(propertyName));

    ProfilePropertySetting propertySetting = mock(ProfilePropertySetting.class);
    when(propertySetting.getId()).thenReturn(123L);
    when(profilePropertyService.getProfileSettingByName(propertyName)).thenReturn(propertySetting);

    RemoveProfilePropertyUpgradePlugin plugin = new RemoveProfilePropertyUpgradePlugin(initParams, profilePropertyService);

    // Act
    plugin.processUpgrade("1.0", "2.0");

    // Assert
    verify(profilePropertyService).getProfileSettingByName(propertyName);
    verify(profilePropertyService).deleteProfilePropertySetting(123L);
  }
}
