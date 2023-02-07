/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage;

import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;

/**
 * Unit Tests for {@link ProfileSettingStorage}
 */
public class ProfileSettingStorageTest extends AbstractCoreTest {

  private final Log             LOG = ExoLogger.getLogger(ProfileSettingStorageTest.class);

  private ProfileSettingStorage profileSettingStorage;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profileSettingStorage = getContainer().getComponentInstanceOfType(ProfileSettingStorage.class);
    assertNotNull(profileSettingStorage);
  }

  public void testSaveProfilePropertySetting() {
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
    profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
    profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    assertNotNull(profilePropertySetting.getId());
  }

  public void testDeleteProfilePropertySetting() {
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
    profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
    profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    assertNotNull(profilePropertySetting.getId());
    profileSettingStorage.deleteProfilePropertySetting(profilePropertySetting.getId());
    profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    assertNull(profilePropertySetting);
  }

  public void testupdateProfilePropertySetting() {
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
    profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
    profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    assertTrue(profilePropertySetting.isActive());
    profilePropertySetting.setActive(false);
    profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, false);
    profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    assertFalse(profilePropertySetting.isActive());
  }

  public void testGetProfilePropertySettings() {
    assertEquals(0, profileSettingStorage.getPropertySettings().size());
    ProfilePropertySetting profilePropertySetting1 = createProfileSettingInstance("test10");
    ProfilePropertySetting profilePropertySetting2 = createProfileSettingInstance("test20");
    ProfilePropertySetting profilePropertySetting3 = createProfileSettingInstance("test30");
    ProfilePropertySetting profilePropertySetting4 = createProfileSettingInstance("test40");
    ProfilePropertySetting profilePropertySetting5 = createProfileSettingInstance("test50");
    ProfilePropertySetting profilePropertySetting6 = createProfileSettingInstance("test60");
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting1, true);
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting2, true);
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting3, true);
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting4, true);
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting5, true);
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting6, true);
    List<ProfilePropertySetting> profilePropertySettings = profileSettingStorage.getPropertySettings();
    assertEquals(6, profilePropertySettings.size());
    assertEquals("test30", profilePropertySettings.get(2).getPropertyName());
    assertEquals("test40", profilePropertySettings.get(3).getPropertyName());
  }

  public void testGetSynchronizedProperties() {
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("testProp");
    profilePropertySetting.setGroupSynchronized(true);
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
    assertEquals(1, profileSettingStorage.getSynchronizedPropertySettings().size());
  }

  @Override
  protected void tearDown() throws Exception {
    deleteAllSettings();
    super.tearDown();
  }

  protected void deleteAllSettings() {
    for (ProfilePropertySetting p : profileSettingStorage.getPropertySettings()) {
      profileSettingStorage.deleteProfilePropertySetting(p.getId());
    }
  }

  private ProfilePropertySetting createProfileSettingInstance(String propertyName) {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName(propertyName);
    profilePropertySetting.setGroupSynchronized(false);
    profilePropertySetting.setMultiValued(false);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    return profilePropertySetting;
  }

}
