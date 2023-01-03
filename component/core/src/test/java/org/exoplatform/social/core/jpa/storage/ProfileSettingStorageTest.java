/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.storage.api.ProfileSettingStorage;

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
    try {
      ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
      profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
      profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
      assertNotNull(profilePropertySetting.getId());
    } catch (Exception e) {
      LOG.error(e.getMessage());
      fail(e.getMessage());
    }
  }

  public void testDeleteProfilePropertySetting() {
    try {
      ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
      profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
      profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
      assertNotNull(profilePropertySetting.getId());
      profileSettingStorage.deleteProfilePropertySetting(profilePropertySetting.getId());
      profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
      assertNull(profilePropertySetting);
    } catch (Exception e) {
      LOG.error(e.getMessage());
      fail(e.getMessage());
    }
  }

  public void testupdateProfilePropertySetting() {
    try {
      ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
      profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
      profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
      assertTrue(profilePropertySetting.isActive());
      profilePropertySetting.setActive(false);
      profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, false);
      profilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
      assertFalse(profilePropertySetting.isActive());
    } catch (Exception e) {
      LOG.error(e.getMessage());
      fail(e.getMessage());
    }
  }

  public void testGetProfilePropertySettings() {
    try {
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
      assertEquals(6, profileSettingStorage.getPropertySettings().size());
    } catch (Exception e) {
      LOG.error(e.getMessage());
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
    profilePropertySetting.setLdapAttribute("");
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    return profilePropertySetting;
  }

  @Override
  protected void tearDown() throws Exception {
    deleteAllSettingss();
    super.tearDown();
  }

  protected void deleteAllSettingss() {
    for (ProfilePropertySetting p : profileSettingStorage.getPropertySettings()) {
      profileSettingStorage.deleteProfilePropertySetting(p.getId());
    }
  }

}
