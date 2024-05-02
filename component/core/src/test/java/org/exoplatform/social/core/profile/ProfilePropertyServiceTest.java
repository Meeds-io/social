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

package org.exoplatform.social.core.profile;

import static org.junit.Assert.assertThrows;

import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class ProfilePropertyServiceTest extends AbstractCoreTest {

  private ProfilePropertyService    profilePropertyService;

  private ProfilePropertySettingDAO profilePropertySettingDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profilePropertyService = getContainer().getComponentInstanceOfType(ProfilePropertyService.class);
    profilePropertySettingDAO = getContainer().getComponentInstanceOfType(ProfilePropertySettingDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();
    profilePropertySettingDAO.deleteAll();
    super.tearDown();
  }

  public void testCreateProfilePropertySetting() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class,
                                       () -> this.profilePropertyService.createPropertySetting(null));
    assertEquals("Profile property setting Item Object is mandatory", exception.getMessage());
    exception = assertThrows(IllegalArgumentException.class,
                             () -> this.profilePropertyService.createPropertySetting(new ProfilePropertySetting()));
    assertEquals("Profile property name is mandatory", exception.getMessage());
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
    profilePropertyService.createPropertySetting(profilePropertySetting);
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertNotNull(profilePropertySetting.getId());
  }

  public void testCreateDuplicatedProfilePropertySetting() throws Exception {

    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
    profilePropertyService.createPropertySetting(profilePropertySetting);
    Throwable exception1 = assertThrows(ObjectAlreadyExistsException.class,
                                        () -> this.profilePropertyService.createPropertySetting(profilePropertySetting));
    assertEquals("A profile property with provided name already exist", exception1.getMessage());
  }

  public void testDeleteProfilePropertySetting() throws Exception {
    Throwable exception1 = assertThrows(IllegalArgumentException.class,
                                        () -> this.profilePropertyService.deleteProfilePropertySetting(0L));
    assertEquals("Profile Property Setting Technical Identifier is mandatory", exception1.getMessage());
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
    profilePropertyService.createPropertySetting(profilePropertySetting);
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertNotNull(profilePropertySetting.getId());
    profilePropertyService.deleteProfilePropertySetting(profilePropertySetting.getId());
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertNull(profilePropertySetting);
  }

  public void testUpdateProfilePropertySetting() throws Exception {
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("test");
    profilePropertyService.createPropertySetting(profilePropertySetting);
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertTrue(profilePropertySetting.isActive());
    profilePropertySetting.setActive(false);
    profilePropertyService.updatePropertySetting(profilePropertySetting);
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertFalse(profilePropertySetting.isActive());
    profilePropertySetting = profilePropertyService.createPropertySetting(createProfileSettingInstance("fullName"));
    assertFalse(profilePropertySetting.isMultiValued());
    profilePropertySetting.setMultiValued(true);
    profilePropertyService.updatePropertySetting(profilePropertySetting);
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertFalse(profilePropertySetting.isMultiValued());
    profilePropertySetting = createProfileSettingInstance("urls");
    profilePropertySetting.setMultiValued(true);
    profilePropertySetting = profilePropertyService.createPropertySetting(profilePropertySetting);
    assertTrue(profilePropertySetting.isMultiValued());
    profilePropertySetting.setMultiValued(false);
    profilePropertyService.updatePropertySetting(profilePropertySetting);
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertTrue(profilePropertySetting.isMultiValued());
    
    profilePropertySetting.setHiddenbale(true);
    profilePropertyService.updatePropertySetting(profilePropertySetting);
    profilePropertySetting = profilePropertyService.getProfileSettingByName(profilePropertySetting.getPropertyName());
    assertTrue(profilePropertySetting.isHiddenbale());

    ProfilePropertySetting unHiddenableprofilePropertySetting = createProfileSettingInstance("fullName");
    unHiddenableprofilePropertySetting.setHiddenbale(true);
    assertThrows(IllegalArgumentException.class,
                 () -> profilePropertyService.updatePropertySetting(unHiddenableprofilePropertySetting));
  }

  public void testGetUnhiddenableProperties() {
    assertNotNull(profilePropertyService.getUnhiddenableProfileProperties());
    assertFalse(profilePropertyService.getUnhiddenableProfileProperties().isEmpty());
  }

  public void testGetExcludedSearchProperties() {
    assertNotNull(profilePropertyService.getExcludedQuickSearchProperties());
    assertFalse(profilePropertyService.getExcludedQuickSearchProperties().isEmpty());
  }

  public void testIsPropertySettingHiddenable() throws ObjectAlreadyExistsException {
    ProfilePropertySetting unHiddenableprofilePropertySetting = createProfileSettingInstance("fullName");
    ProfilePropertySetting hiddenableprofilePropertySetting = createProfileSettingInstance("prop");
    hiddenableprofilePropertySetting.setHiddenbale(true);
    ProfilePropertySetting propertySetting = profilePropertyService.createPropertySetting(hiddenableprofilePropertySetting);
    ProfilePropertySetting childProp = createProfileSettingInstance("childProp");
    assertFalse(profilePropertyService.isPropertySettingHiddenable(unHiddenableprofilePropertySetting));
    assertTrue(profilePropertyService.isPropertySettingHiddenable(hiddenableprofilePropertySetting));
    ProfilePropertySetting chilePropertySetting = profilePropertyService.createPropertySetting(childProp);
    chilePropertySetting.setParentId(propertySetting.getId());
    profilePropertyService.updatePropertySetting(chilePropertySetting);
    assertFalse(profilePropertyService.isPropertySettingHiddenable(propertySetting));
  }

  public void testHidePropertySetting() throws ObjectAlreadyExistsException {
    ProfilePropertySetting propertySetting = createProfileSettingInstance("testProp");
    propertySetting.setHiddenbale(true);
    propertySetting = profilePropertyService.createPropertySetting(propertySetting);
    profilePropertyService.hidePropertySetting(1L, propertySetting.getId());
    List<Long> Ids = profilePropertyService.getHiddenProfilePropertyIds(1L);
    assertTrue(Ids.contains(propertySetting.getId()));
  }

  public void testShowPropertySetting() throws ObjectAlreadyExistsException {
    ProfilePropertySetting propertySetting = createProfileSettingInstance("testProp1");
    propertySetting.setHiddenbale(true);
    propertySetting = profilePropertyService.createPropertySetting(propertySetting);
    profilePropertyService.hidePropertySetting(1L, propertySetting.getId());
    List<Long> Ids = profilePropertyService.getHiddenProfilePropertyIds(1L);
    assertTrue(Ids.contains(propertySetting.getId()));
    profilePropertyService.showPropertySetting(1L, propertySetting.getId());
    Ids = profilePropertyService.getHiddenProfilePropertyIds(1L);
    assertFalse(Ids.contains(propertySetting.getId()));
  }

  public void testGetHiddenProfilePropertyIds() throws ObjectAlreadyExistsException {
    ProfilePropertySetting propertySetting = createProfileSettingInstance("testProp2");
    propertySetting.setHiddenbale(true);
    propertySetting = profilePropertyService.createPropertySetting(propertySetting);
    profilePropertyService.hidePropertySetting(1L, propertySetting.getId());
    List<Long> Ids = profilePropertyService.getHiddenProfilePropertyIds(1L);
    assertFalse(Ids.isEmpty());
  }

  public void testGetProfilePropertySettings() throws Exception {

    assertEquals(0, profilePropertyService.getPropertySettings().size());
    ProfilePropertySetting profilePropertySetting1 = createProfileSettingInstance("test10");
    ProfilePropertySetting profilePropertySetting2 = createProfileSettingInstance("test20");
    ProfilePropertySetting profilePropertySetting3 = createProfileSettingInstance("test30");
    ProfilePropertySetting profilePropertySetting4 = createProfileSettingInstance("test40");
    ProfilePropertySetting profilePropertySetting5 = createProfileSettingInstance("test50");
    ProfilePropertySetting profilePropertySetting6 = createProfileSettingInstance("test60");
    profilePropertyService.createPropertySetting(profilePropertySetting1);
    profilePropertyService.createPropertySetting(profilePropertySetting2);
    profilePropertyService.createPropertySetting(profilePropertySetting3);
    profilePropertyService.createPropertySetting(profilePropertySetting4);
    profilePropertyService.createPropertySetting(profilePropertySetting5);
    profilePropertyService.createPropertySetting(profilePropertySetting6);
    List<ProfilePropertySetting> profilePropertySettings = profilePropertyService.getPropertySettings();
    assertEquals(6, profilePropertyService.getPropertySettings().size());
    assertEquals("test30", profilePropertySettings.get(2).getPropertyName());
    assertEquals("test40", profilePropertySettings.get(3).getPropertyName());
  }

  public void testGetSynchronizedProperties() throws ObjectAlreadyExistsException {
    ProfilePropertySetting profilePropertySetting = createProfileSettingInstance("postalCode");
    profilePropertySetting.setGroupSynchronized(true);
    profilePropertyService.createPropertySetting(profilePropertySetting);
    assertEquals(1, profilePropertyService.getSynchronizedPropertySettings().size());
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
