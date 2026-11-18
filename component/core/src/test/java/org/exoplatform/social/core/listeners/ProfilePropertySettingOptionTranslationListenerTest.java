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
package org.exoplatform.social.core.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.ProfilePropertyServiceImpl;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

@RunWith(MockitoJUnitRunner.class)
public class ProfilePropertySettingOptionTranslationListenerTest {

  @Mock
  private ProfilePropertyService                          profilePropertyService;

  private ProfilePropertySettingOptionTranslationListener profilePropertySettingOptionListener;

  @Before
  public void setUp() {
    profilePropertySettingOptionListener = new ProfilePropertySettingOptionTranslationListener(profilePropertyService);
  }

  @Test
  public void testOnEventHandsThePropertyToTheService() {
    ProfilePropertySetting propertySetting = new ProfilePropertySetting();
    propertySetting.setId(1L);
    propertySetting.setPropertyName("department");

    profilePropertySettingOptionListener.onEvent(new Event<>(ProfilePropertyServiceImpl.PROPERTY_OPTIONS_UPDATED_EVENT,
                                                             profilePropertyService,
                                                             propertySetting));

    verify(profilePropertyService).reindexPropertyHolders("department");
  }

  @Test
  public void testOnEventWithoutSettingDoesNothing() {
    profilePropertySettingOptionListener.onEvent(new Event<>(ProfilePropertyServiceImpl.PROPERTY_OPTIONS_UPDATED_EVENT,
                                                             profilePropertyService,
                                                             null));

    verify(profilePropertyService, never()).reindexPropertyHolders(any());
  }
}
