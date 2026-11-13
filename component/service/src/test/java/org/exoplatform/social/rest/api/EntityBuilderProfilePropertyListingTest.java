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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.rest.entity.ProfilePropertySettingEntity;

/**
 * The two listing flags between the REST entity of an attribute setting and its
 * DTO: an update that does not state a flag keeps its stored value, as an
 * update resolves every field its caller did not state.
 */
public class EntityBuilderProfilePropertyListingTest {

  private static final long      SETTING_ID = 5L;

  private ProfilePropertyService profilePropertyService;

  @Before
  public void setUp() {
    profilePropertyService = mock(ProfilePropertyService.class);
  }

  @Test
  public void testAnUpdateThatStatesNoFlagKeepsBothStoredValues() {
    storedSetting(true, false);

    ProfilePropertySetting setting = EntityBuilder.buildProfilePropertySettingFromEntity(entity(SETTING_ID, null, null),
                                                                                         profilePropertyService);

    assertTrue(setting.isListedOnAccountForm());
    assertFalse(setting.isListedOnRegistration());
  }

  @Test
  public void testAnUpdateThatStatesOneFlagKeepsTheOtherStoredValue() {
    storedSetting(true, true);

    ProfilePropertySetting setting = EntityBuilder.buildProfilePropertySettingFromEntity(entity(SETTING_ID, false, null),
                                                                                         profilePropertyService);
    assertFalse(setting.isListedOnAccountForm());
    assertTrue(setting.isListedOnRegistration());

    setting = EntityBuilder.buildProfilePropertySettingFromEntity(entity(SETTING_ID, null, false), profilePropertyService);
    assertTrue(setting.isListedOnAccountForm());
    assertFalse(setting.isListedOnRegistration());
  }

  @Test
  public void testAnUpdateThatStatesBothFlagsTakesThemWithoutReadingTheStoredSetting() {
    ProfilePropertySetting setting = EntityBuilder.buildProfilePropertySettingFromEntity(entity(SETTING_ID, true, false),
                                                                                         profilePropertyService);

    assertTrue(setting.isListedOnAccountForm());
    assertFalse(setting.isListedOnRegistration());
    verify(profilePropertyService, never()).getProfileSettingById(any());
  }

  @Test
  public void testANewSettingThatStatesNoFlagIsListedInNoForm() {
    ProfilePropertySetting setting = EntityBuilder.buildProfilePropertySettingFromEntity(entity(null, null, null),
                                                                                         profilePropertyService);

    assertFalse(setting.isListedOnAccountForm());
    assertFalse(setting.isListedOnRegistration());
    verify(profilePropertyService, never()).getProfileSettingById(any());
  }

  @Test
  public void testTheEntityCarriesBothFlags() {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setId(SETTING_ID);
    setting.setPropertyName("department");
    setting.setListedOnAccountForm(false);
    setting.setListedOnRegistration(true);
    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      ProfileLabelService profileLabelService = mock(ProfileLabelService.class);
      when(profileLabelService.findLabelByObjectTypeAndObjectId(anyString(), anyString())).thenReturn(List.of());
      commonsUtils.when(() -> CommonsUtils.getService(ProfileLabelService.class)).thenReturn(profileLabelService);

      ProfilePropertySettingEntity entity = EntityBuilder.buildEntityProfilePropertySetting(setting,
                                                                                           profilePropertyService,
                                                                                           ProfilePropertyService.LABELS_OBJECT_TYPE);

      assertEquals(Boolean.FALSE, entity.getListedOnAccountForm());
      assertEquals(Boolean.TRUE, entity.getListedOnRegistration());
    }
  }

  private void storedSetting(boolean listedOnAccountForm, boolean listedOnRegistration) {
    ProfilePropertySetting storedSetting = new ProfilePropertySetting();
    storedSetting.setId(SETTING_ID);
    storedSetting.setListedOnAccountForm(listedOnAccountForm);
    storedSetting.setListedOnRegistration(listedOnRegistration);
    when(profilePropertyService.getProfileSettingById(SETTING_ID)).thenReturn(storedSetting);
  }

  private ProfilePropertySettingEntity entity(Long id, Boolean listedOnAccountForm, Boolean listedOnRegistration) {
    ProfilePropertySettingEntity entity = new ProfilePropertySettingEntity();
    entity.setId(id);
    entity.setPropertyName("department");
    entity.setPropertyType("text");
    entity.setEditable(true);
    entity.setListedOnAccountForm(listedOnAccountForm);
    entity.setListedOnRegistration(listedOnRegistration);
    return entity;
  }

}
