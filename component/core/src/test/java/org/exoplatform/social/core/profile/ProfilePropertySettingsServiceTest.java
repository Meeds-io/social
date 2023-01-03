/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.profile;

import static org.junit.Assert.assertThrows;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.jpa.storage.dao.ProfilePropertySettingDAO;
import org.exoplatform.social.core.profile.settings.ProfilePropertySettingsService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class ProfilePropertySettingsServiceTest extends AbstractCoreTest {

  private ProfilePropertySettingsService profilePropertySettingsService;

  private ProfilePropertySettingDAO      profilePropertySettingDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profilePropertySettingsService = getContainer().getComponentInstanceOfType(ProfilePropertySettingsService.class);
    profilePropertySettingDAO = getContainer().getComponentInstanceOfType(ProfilePropertySettingDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    profilePropertySettingDAO.deleteAll();
    super.tearDown();
  }

  public void testCreateProfilePropertySetting() {
    Throwable exception =
            assertThrows(IllegalArgumentException.class,
                    () -> this.profilePropertySettingsService.createPropertySetting(null));
    assertEquals("Profile property setting Item Object is mandatory", exception.getMessage());
    exception =
            assertThrows(IllegalArgumentException.class,
                    () -> this.profilePropertySettingsService.createPropertySetting(new ProfilePropertySetting()));
    assertEquals("Profile property name is mandatory", exception.getMessage());
    try {
      ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
      profilePropertySettingsService.createPropertySetting(profilePropertySetting);
      profilePropertySetting = profilePropertySettingsService.getProfileSettingByName(profilePropertySetting.getPropertyName());
      assertNotNull(profilePropertySetting.getId());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testCreateDuplicatedProfilePropertySetting() {

    try {
      ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
      profilePropertySettingsService.createPropertySetting(profilePropertySetting);
      Throwable exception1 =
                           assertThrows(ObjectAlreadyExistsException.class,
                                        () -> this.profilePropertySettingsService.createPropertySetting(profilePropertySetting));
      assertEquals("A profile property with provided name already exist", exception1.getMessage());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testDeleteProfilePropertySetting() {
    Throwable exception1 = assertThrows(IllegalArgumentException.class,
                                        () -> this.profilePropertySettingsService.deleteProfilePropertySetting(0L));
    assertEquals("Profile Property Setting Technical Identifier is mandatory", exception1.getMessage());
    try {
      ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
      profilePropertySettingsService.createPropertySetting(profilePropertySetting);
      profilePropertySetting = profilePropertySettingsService.getProfileSettingByName(profilePropertySetting.getPropertyName());
      assertNotNull(profilePropertySetting.getId());
      profilePropertySettingsService.deleteProfilePropertySetting(profilePropertySetting.getId());
      profilePropertySetting = profilePropertySettingsService.getProfileSettingByName(profilePropertySetting.getPropertyName());
      assertNull(profilePropertySetting);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testUpdateProfilePropertySetting() {
    try {
      ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
      profilePropertySettingsService.createPropertySetting(profilePropertySetting);
      profilePropertySetting = profilePropertySettingsService.getProfileSettingByName(profilePropertySetting.getPropertyName());
      assertTrue(profilePropertySetting.isActive());
      profilePropertySetting.setActive(false);
      profilePropertySettingsService.updatePropertySetting(profilePropertySetting);
      profilePropertySetting = profilePropertySettingsService.getProfileSettingByName(profilePropertySetting.getPropertyName());
      assertFalse(profilePropertySetting.isActive());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetProfilePropertySettings() {
    try {
      assertEquals(0, profilePropertySettingsService.getPropertySettings().size());
      ProfilePropertySetting profilePropertySetting1 = createProfileSettingInstance("test10");
      ProfilePropertySetting profilePropertySetting2 = createProfileSettingInstance("test20");
      ProfilePropertySetting profilePropertySetting3 = createProfileSettingInstance("test30");
      ProfilePropertySetting profilePropertySetting4 = createProfileSettingInstance("test40");
      ProfilePropertySetting profilePropertySetting5 = createProfileSettingInstance("test50");
      ProfilePropertySetting profilePropertySetting6 = createProfileSettingInstance("test60");
      profilePropertySettingsService.createPropertySetting(profilePropertySetting1);
      profilePropertySettingsService.createPropertySetting(profilePropertySetting2);
      profilePropertySettingsService.createPropertySetting(profilePropertySetting3);
      profilePropertySettingsService.createPropertySetting(profilePropertySetting4);
      profilePropertySettingsService.createPropertySetting(profilePropertySetting5);
      profilePropertySettingsService.createPropertySetting(profilePropertySetting6);
      assertEquals(6, profilePropertySettingsService.getPropertySettings().size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private ProfilePropertySetting createProfileSettingInstance(String propertyName) {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName(propertyName);
    profilePropertySetting.setGroupSynchronized(false);
    profilePropertySetting.setSystemProperty(false);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    return profilePropertySetting;
  }
}
