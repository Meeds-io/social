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
package org.exoplatform.social.core.profile;

import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.core.profileproperty.storage.CachedProfileSettingStorage;

public class ProfilePropertyServiceTest extends AbstractCoreTest {

  private ProfilePropertyService    profilePropertyService;

  private ProfilePropertySettingDAO profilePropertySettingDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profilePropertyService = getContainer().getComponentInstanceOfType(ProfilePropertyService.class);
    profilePropertySettingDAO = getContainer().getComponentInstanceOfType(ProfilePropertySettingDAO.class);
    cleanData();
  }

  @Override
  protected void afterClass() {
    cleanData();
    super.afterClass();
  }

  public void testCreateProfilePropertySetting() throws Exception {
    Throwable exception = assertThrows(IllegalArgumentException.class,
                                       () -> this.profilePropertyService.createPropertySetting(null));
    assertEquals("Profile property setting object is mandatory.", exception.getMessage());
    exception = assertThrows(IllegalArgumentException.class,
                             () -> this.profilePropertyService.createPropertySetting(new ProfilePropertySetting()));
    assertEquals("Profile property name is mandatory.", exception.getMessage());
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
    assertEquals("A profile property with the provided name already exists.", exception1.getMessage());
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

    ProfilePropertySetting propertySetting = createProfileSettingInstance("testProperty");
    propertySetting.setPropertyType("user");
    propertySetting = profilePropertyService.createPropertySetting(propertySetting);
    assertEquals("user", propertySetting.getPropertyType());
    propertySetting.setPropertyType("text");
    ProfilePropertySetting finalPropertySetting = propertySetting;
    assertThrows(IllegalArgumentException.class, () -> profilePropertyService.updatePropertySetting(finalPropertySetting));

    ProfilePropertySetting propertySetting1 = createProfileSettingInstance("testProperty1");
    propertySetting1.setPropertyType("text");
    propertySetting1 = profilePropertyService.createPropertySetting(propertySetting1);
    propertySetting1.setPropertyType("user");
    ProfilePropertySetting finalPropertySetting1 = propertySetting1;
    assertThrows(IllegalArgumentException.class, () -> profilePropertyService.updatePropertySetting(finalPropertySetting1));

    ProfilePropertySetting propertySetting2 = createProfileSettingInstance("testProperty2");
    propertySetting2.setPropertyType("text");
    propertySetting2 = profilePropertyService.createPropertySetting(propertySetting2);
    propertySetting2.setPropertyType("call");
    profilePropertyService.updatePropertySetting(propertySetting2);
    assertEquals("call", profilePropertyService.getProfileSettingById(propertySetting2.getId()).getPropertyType());
  }

  public void testGetUnhiddenableProperties() {
    assertNotNull(profilePropertyService.getUnhiddenableProfileProperties());
    assertFalse(profilePropertyService.getUnhiddenableProfileProperties().isEmpty());
  }

  public void testGetExcludedSearchProperties() {
    assertNotNull(profilePropertyService.getExcludedQuickSearchProperties());
    assertFalse(profilePropertyService.getExcludedQuickSearchProperties().isEmpty());
  }

  public void testIsSystemPropertySettingNonHiddenable() throws ObjectAlreadyExistsException {
    ProfilePropertySetting unHiddenableProfilePropertySetting = createProfileSettingInstance("fullName");
    unHiddenableProfilePropertySetting = profilePropertyService.createPropertySetting(unHiddenableProfilePropertySetting);
    assertFalse(profilePropertyService.isPropertySettingHiddenable(unHiddenableProfilePropertySetting.getId()));
  }

  public void testIsPropertySettingHiddenable() throws ObjectAlreadyExistsException {
    ProfilePropertySetting propertySetting = createProfileSettingInstance("prop");
    propertySetting.setHiddenbale(true);
    propertySetting = profilePropertyService.createPropertySetting(propertySetting);

    ProfilePropertySetting childProp = createProfileSettingInstance("childProp");
    assertTrue(profilePropertyService.isPropertySettingHiddenable(propertySetting.getId()));

    ProfilePropertySetting chilePropertySetting = profilePropertyService.createPropertySetting(childProp);
    chilePropertySetting.setParentId(propertySetting.getId());
    profilePropertyService.updatePropertySetting(chilePropertySetting);
    assertFalse(profilePropertyService.isPropertySettingHiddenable(propertySetting.getId()));
  }

  public void testDropdownListPropertySetting() throws ObjectAlreadyExistsException {
    ProfilePropertySetting dropdownListPropertySetting = createProfileSettingInstance("propDropdown");
    dropdownListPropertySetting.setDropdownList(true);
    dropdownListPropertySetting.setPropertyType("user");
    assertThrows(IllegalArgumentException.class, () -> profilePropertyService.createPropertySetting(dropdownListPropertySetting));

    dropdownListPropertySetting.setPropertyType("text");
    ProfilePropertySetting propertySetting = profilePropertyService.createPropertySetting(dropdownListPropertySetting);
    assertTrue(propertySetting.isDropdownList());

    propertySetting.setPropertyType("user");
    assertThrows(IllegalArgumentException.class, () -> profilePropertyService.updatePropertySetting(propertySetting));
  }

  public void testIndexedInAnalyticsPropertySetting() throws ObjectAlreadyExistsException {
    ProfilePropertySetting indexedInAnalyticsPropertySetting = createProfileSettingInstance("propIndexedInAnalytics");
    indexedInAnalyticsPropertySetting.setIndexInAnalytics(true);
    ProfilePropertySetting propertySetting = profilePropertyService.createPropertySetting(indexedInAnalyticsPropertySetting);
    assertTrue(propertySetting.isIndexInAnalytics());
  }

  public void testSavePropertySettingWithOptions() throws ObjectAlreadyExistsException {
    ProfilePropertySetting dropdownListPropertySetting = createProfileSettingInstanceWithOptions("propDropdown", 3);

    ProfilePropertySetting propertySetting = profilePropertyService.createPropertySetting(dropdownListPropertySetting);
    assertNotNull(propertySetting);
    assertEquals(3, propertySetting.getPropertyOptions().size());
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
    profilePropertySetting.setPropertyType("text");
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    return profilePropertySetting;
  }

  private ProfilePropertySetting createProfileSettingInstanceWithOptions(String propertyName, int numberOfOptions) {
    ProfilePropertySetting propertySetting = createProfileSettingInstance(propertyName);
    propertySetting.setDropdownList(true);

    List<ProfilePropertyOption> profilePropertyOptions = Stream.generate(ProfilePropertyOption::new)
                                                               .limit(numberOfOptions)
                                                               .peek(option -> option.setValue("test"))
                                                               .toList();
    propertySetting.setPropertyOptions(profilePropertyOptions);

    return propertySetting;
  }

  private void cleanData() {
    restartTransaction();
    profilePropertySettingDAO.deleteAll();
    getService(CachedProfileSettingStorage.class).clearCaches();
  }

}
