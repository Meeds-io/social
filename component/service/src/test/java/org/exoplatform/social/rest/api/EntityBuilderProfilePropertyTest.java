/**
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
package org.exoplatform.social.rest.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.social.translation.service.TranslationService;

/**
 * A dropdown profile property holds an option id, or the free text a user typed
 * before the property was turned into a dropdown.
 */
public class EntityBuilderProfilePropertyTest {

  private static final String    PROPERTY  = "position";

  private static final long      OPTION_ID = 12L;

  private ProfilePropertyService profilePropertyService;

  private TranslationService     translationService;

  @Before
  public void setUp() throws Exception {
    profilePropertyService = mock(ProfilePropertyService.class);
    translationService = mock(TranslationService.class);
    setStaticField("profilePropertyService", profilePropertyService);
    setStaticField("translationService", translationService);

    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setPropertyName(PROPERTY);
    setting.setDropdownList(true);
    setting.setPropertyOptions(List.of(new ProfilePropertyOption(OPTION_ID, "Engineer", 1L)));
    when(profilePropertyService.getProfileSettingByName(PROPERTY)).thenReturn(setting);
    when(translationService.getTranslationLabelOrDefault(anyString(), anyLong(), anyString(), any())).thenReturn("Engineer");
  }

  @After
  public void tearDown() throws Exception {
    setStaticField("profilePropertyService", null);
    setStaticField("translationService", null);
  }

  @Test
  public void testOptionIdOfThePropertyIsTranslated() {
    assertEquals("Engineer", EntityBuilder.getProfilePropertyValue(profile(String.valueOf(OPTION_ID)), PROPERTY));
    verify(translationService).getTranslationLabelOrDefault(anyString(), eq(OPTION_ID), anyString(), any());
  }

  @Test
  public void testLegacyFreeTextThatParsesAsANumberIsReturnedAsTyped() {
    // NumberUtils.isCreatable accepts these, Long.parseLong does not
    for (String legacyValue : List.of("1.5", "1e3", "0x1F")) {
      assertEquals(legacyValue, EntityBuilder.getProfilePropertyValue(profile(legacyValue), PROPERTY));
    }
    verify(translationService, never()).getTranslationLabelOrDefault(anyString(), anyLong(), anyString(), any());
  }

  @Test
  public void testLegacyDigitsThatAreNotAnOptionOfThePropertyAreReturnedAsTyped() {
    assertEquals("2024", EntityBuilder.getProfilePropertyValue(profile("2024"), PROPERTY));
    assertEquals("99999999999999999999", EntityBuilder.getProfilePropertyValue(profile("99999999999999999999"), PROPERTY));
    verify(translationService, never()).getTranslationLabelOrDefault(anyString(), anyLong(), anyString(), any());
  }

  private static Profile profile(String value) {
    Profile profile = new Profile();
    profile.setProperty(PROPERTY, value);
    return profile;
  }

  private void setStaticField(String name, Object value) throws Exception {
    Field field = EntityBuilder.class.getDeclaredField(name);
    field.setAccessible(true); // NOSONAR
    field.set(null, value);
  }

}
