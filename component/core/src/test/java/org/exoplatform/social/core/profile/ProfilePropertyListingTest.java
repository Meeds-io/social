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
package org.exoplatform.social.core.profile;

import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.function.Consumer;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertyOptionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.ProfilePropertyServiceImpl;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.core.profileproperty.constant.ProfileFormKind;
import io.meeds.social.core.profileproperty.storage.CachedProfileSettingStorage;
import io.meeds.social.translation.service.TranslationService;

import jakarta.persistence.EntityManager;

/**
 * The listing rules of the attribute settings: the flags a setting that cannot
 * be listed loses at each save, whatever the client sent, and the switch-off
 * of a parent's flags when a setting is saved under it, in the save's
 * transaction, read back on the real database.
 */
public class ProfilePropertyListingTest extends AbstractCoreTest {

  private static final String       NAME_200 = "a".repeat(200);

  private static final String       NAME_201 = "a".repeat(201);

  private ProfilePropertyService    profilePropertyService;

  private ProfilePropertySettingDAO profilePropertySettingDAO;

  private ProfileSettingStorage     profileSettingStorage;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profilePropertyService = getService(ProfilePropertyService.class);
    profilePropertySettingDAO = getService(ProfilePropertySettingDAO.class);
    profileSettingStorage = getService(ProfileSettingStorage.class);
    cleanData();
  }

  @Override
  public void tearDown() throws Exception {
    cleanData();
    super.tearDown();
  }

  public void testAListableSettingKeepsBothFlagsOnCreationAndUpdate() throws Exception {
    ProfilePropertySetting setting = createListed("department", s -> {
    });
    assertListed(setting.getId(), true, true);
    assertEquals(List.of(setting.getId()), listedIds(ProfileFormKind.ACCOUNT));
    assertEquals(List.of(setting.getId()), listedIds(ProfileFormKind.REGISTRATION));

    setting = copyOf(setting.getId());
    setting.setListedOnRegistration(false);
    profilePropertyService.updatePropertySetting(setting);
    assertListed(setting.getId(), true, false);
    assertEquals(List.of(setting.getId()), listedIds(ProfileFormKind.ACCOUNT));
    assertEquals(List.of(), listedIds(ProfileFormKind.REGISTRATION));
  }

  public void testANonEditableSettingIsNeverListed() throws Exception {
    ProfilePropertySetting setting = createListed("department", s -> s.setEditable(false));
    assertListed(setting.getId(), false, false);

    setting = createListed("team", s -> {
    });
    setting = copyOf(setting.getId());
    setting.setEditable(false);
    profilePropertyService.updatePropertySetting(setting);
    assertListed(setting.getId(), false, false);
  }

  public void testAReservedNameIsNeverListedWhateverItsCase() throws Exception {
    for (String name : List.of("Avatar", "Experiences", "skills", "Url", "EXTERNAL", "UserName", "FirstName", "fullname")) {
      ProfilePropertySetting setting = createListed(name, s -> {
      });
      assertListed(setting.getId(), false, false);
    }
  }

  public void testANameOverTwoHundredCharactersIsNeverListed() throws Exception {
    ProfilePropertySetting setting = createListed(NAME_201, s -> {
    });
    assertListed(setting.getId(), false, false);
    setting = createListed(NAME_200, s -> {
    });
    assertListed(setting.getId(), true, true);
  }

  public void testAUserTypeSettingIsNeverListedInTheRegistrationForm() throws Exception {
    ProfilePropertySetting setting = createListed("assistant", s -> s.setPropertyType("user"));
    assertListed(setting.getId(), true, false);
  }

  public void testAGroupSynchronizedNameIsListedOnlyWithACleanedNameOfOneTo189Characters() throws Exception {
    assertListed(createListed("s".repeat(189), s -> s.setGroupSynchronized(true)).getId(), true, true);
    assertListed(createListed("t".repeat(190), s -> s.setGroupSynchronized(true)).getId(), false, false);
    // Each é is cleaned into two characters
    assertListed(createListed("é".repeat(95), s -> s.setGroupSynchronized(true)).getId(), false, false);
    assertListed(createListed("\u00A0\u00A0", s -> s.setGroupSynchronized(true)).getId(), false, false);
    assertListed(createListed("\u0001", s -> s.setGroupSynchronized(true)).getId(), false, false);

    // The same names keep their flags when the attribute is not synchronized
    assertListed(createListed("u".repeat(190), s -> {
    }).getId(), true, true);
    assertListed(createListed("\u00A0\u00A0\u00A0", s -> {
    }).getId(), true, true);

    // Synchronization turned on after listing
    ProfilePropertySetting setting = createListed("v".repeat(190), s -> {
    });
    assertListed(setting.getId(), true, true);
    setting = copyOf(setting.getId());
    setting.setGroupSynchronized(true);
    profilePropertyService.updatePropertySetting(setting);
    assertListed(setting.getId(), false, false);
  }

  public void testASubFieldIsNeverListedAndClearsItsParentFlags() throws Exception {
    ProfilePropertySetting parent = createListed("address", s -> {
    });
    long parentUpdated = parent.getUpdated();
    waitForTheClockToMove();

    ProfilePropertySetting child = createListed("street", s -> s.setParentId(parent.getId()));

    assertListed(child.getId(), false, false);
    assertListed(parent.getId(), false, false);
    assertTrue("The parent's update date moves with its flags",
               profilePropertyService.getProfileSettingById(parent.getId()).getUpdated() > parentUpdated);

    // A save of the parent that sends both flags on, with a DTO that does not
    // say it has sub-fields, keeps them off
    ProfilePropertySetting parentToSave = copyOf(parent.getId());
    parentToSave.setHasChildProperties(false);
    parentToSave.setListedOnAccountForm(true);
    parentToSave.setListedOnRegistration(true);
    profilePropertyService.updatePropertySetting(parentToSave);
    assertListed(parent.getId(), false, false);
  }

  public void testAReParentedSettingClearsItsNewParentFlags() throws Exception {
    ProfilePropertySetting parent = createListed("address", s -> {
    });
    ProfilePropertySetting setting = createListed("street", s -> {
    });
    assertListed(parent.getId(), true, true);
    assertListed(setting.getId(), true, true);

    setting = copyOf(setting.getId());
    setting.setParentId(parent.getId());
    profilePropertyService.updatePropertySetting(setting);

    assertListed(setting.getId(), false, false);
    assertListed(parent.getId(), false, false);
  }

  public void testASettingWithNoTypeKeepsItsFlagsAndItsNullType() throws Exception {
    ProfilePropertySetting setting = createListed("legacy", s -> s.setPropertyType(null));
    setting = profilePropertyService.getProfileSettingById(setting.getId());
    assertNull(setting.getPropertyType());
    assertListed(setting.getId(), true, true);

    setting = copyOf(setting.getId());
    setting.setActive(false);
    profilePropertyService.updatePropertySetting(setting);
    setting = profilePropertyService.getProfileSettingById(setting.getId());
    assertNull(setting.getPropertyType());
    assertFalse(setting.isActive());
    assertListed(setting.getId(), true, true);

    setting = copyOf(setting.getId());
    setting.setPropertyName("legacyRenamed");
    profilePropertyService.updatePropertySetting(setting);
    setting = profilePropertyService.getProfileSettingById(setting.getId());
    assertEquals("legacyRenamed", setting.getPropertyName());
    assertNull(setting.getPropertyType());
    assertListed(setting.getId(), true, true);

    ProfilePropertySetting toUserType = copyOf(setting.getId());
    toUserType.setPropertyType("user");
    assertThrows(IllegalArgumentException.class, () -> profilePropertyService.updatePropertySetting(toUserType));

    setting = copyOf(setting.getId());
    setting.setPropertyType("text");
    profilePropertyService.updatePropertySetting(setting);
    setting = profilePropertyService.getProfileSettingById(setting.getId());
    assertEquals("text", setting.getPropertyType());
    assertListed(setting.getId(), true, true);
  }

  /**
   * A miss cached for a name on this node lets a creation of that name pass
   * the service's check, while the name exists in the store: the refusal of
   * the store's unique constraint answers as the check does.
   */
  public void testADuplicateNameCachedAsMissingAnswersObjectAlreadyExists() {
    assertNull(profilePropertyService.getProfileSettingByName("duplicate"));
    ProfilePropertySettingEntity entity = new ProfilePropertySettingEntity();
    entity.setPropertyName("duplicate");
    entity.setPropertyType("text");
    entity.setOrder(1L);
    profilePropertySettingDAO.create(entity);
    restartTransaction();

    ProfilePropertySetting setting = newSetting("duplicate");
    assertThrows(ObjectAlreadyExistsException.class, () -> profilePropertyService.createPropertySetting(setting));
  }

  /**
   * The parent's switch-off failing after the sub-field's insert leaves
   * neither row changed: both writes run in the save's transaction.
   */
  public void testTheParentSwitchOffAndTheSaveCommitTogether() throws Exception {
    ProfilePropertySetting parent = createListed("address", s -> {
    });
    restartTransaction();
    ProfileSettingStorage failingStorage = new ProfileSettingStorage(profilePropertySettingDAO,
                                                                     getService(ProfilePropertyOptionDAO.class)) {
      @Override
      public boolean clearListingFlags(Long id) {
        super.clearListingFlags(id);
        throw new IllegalStateException("The parent's switch-off failed");
      }
    };
    ProfilePropertyServiceImpl failingService = new ProfilePropertyServiceImpl(null,
                                                                                failingStorage,
                                                                                getService(SettingService.class),
                                                                                getService(IndexingService.class),
                                                                                getService(ListenerService.class),
                                                                                getService(TranslationService.class));

    ProfilePropertySetting child = newSetting("street");
    child.setParentId(parent.getId());
    assertThrows(RuntimeException.class, () -> failingService.createPropertySetting(child));

    restartTransaction();
    assertNull("The sub-field's insert is rolled back", profilePropertySettingDAO.findProfileSettingByName("street"));
    ProfilePropertySettingEntity storedParent = profilePropertySettingDAO.find(parent.getId());
    assertTrue("The parent's switch-off is rolled back", storedParent.isListedOnAccountForm());
    assertTrue(storedParent.isListedOnRegistration());
  }

  /**
   * The parent read again in the request that saved a sub-field under it
   * carries its switched-off flags and a change another writer committed after
   * the request first read it; so does its cached copy.
   */
  public void testTheParentReadAfterASubFieldCreationCarriesItsFlagsAndAConcurrentChange() throws Exception {
    ProfilePropertySetting parent = createListed("address", s -> {
    });
    long parentUpdated = parent.getUpdated();
    restartTransaction();
    getService(CachedProfileSettingStorage.class).clearCaches();
    waitForTheClockToMove();

    // The request reads the parent, then another writer commits a change of it
    assertTrue(profilePropertyService.getProfileSettingById(parent.getId()).isEditable());
    commitInAnotherEntityManager("UPDATE SocProfileSettingEntity s SET s.isEditable = false WHERE s.id = " + parent.getId());

    ProfilePropertySetting child = newSetting("street");
    child.setParentId(parent.getId());
    profilePropertyService.createPropertySetting(child);

    assertParentReadBack(parent.getId(), parentUpdated);
    restartTransaction();
    assertParentReadBack(parent.getId(), parentUpdated);
  }

  public void testTheParentReadAfterAReParentingCarriesItsFlagsAndAConcurrentChange() throws Exception {
    ProfilePropertySetting parent = createListed("address", s -> {
    });
    ProfilePropertySetting setting = createListed("street", s -> {
    });
    long parentUpdated = parent.getUpdated();
    restartTransaction();
    getService(CachedProfileSettingStorage.class).clearCaches();
    waitForTheClockToMove();

    assertTrue(profilePropertyService.getProfileSettingById(parent.getId()).isEditable());
    commitInAnotherEntityManager("UPDATE SocProfileSettingEntity s SET s.isEditable = false WHERE s.id = " + parent.getId());

    setting = copyOf(setting.getId());
    setting.setParentId(parent.getId());
    profilePropertyService.updatePropertySetting(setting);

    assertParentReadBack(parent.getId(), parentUpdated);
    restartTransaction();
    assertParentReadBack(parent.getId(), parentUpdated);
  }

  /**
   * The switch-off writes the two flags and the update date only: every other
   * column of the parent that another writer committed after the request first
   * read it survives on the database. A full-row update writes them back from
   * the request's snapshot.
   */
  public void testTheParentSwitchOffWritesOnlyTheListingColumns() throws Exception {
    ProfilePropertySetting parent = createListed("address", s -> {
    });
    restartTransaction();
    getService(CachedProfileSettingStorage.class).clearCaches();

    // First read of the parent in this request, as an entity of its context
    assertNotNull(profilePropertySettingDAO.find(parent.getId()));
    commitInAnotherEntityManager("UPDATE SocProfileSettingEntity s SET s.isEditable = false, s.isVisible = false, "
        + "s.isActive = false, s.isRequired = true, s.isMultiValued = true, s.isHiddenable = false, "
        + "s.indexInAnalytics = true, s.order = 77, s.propertyType = 'concurrent', s.propertyName = 'concurrentName', "
        + "s.isGroupSynchronized = true, s.isDropdownList = true, s.parentId = 999 WHERE s.id = " + parent.getId());

    ProfilePropertySetting child = newSetting("street");
    child.setParentId(parent.getId());
    profilePropertyService.createPropertySetting(child);
    restartTransaction();

    ProfilePropertySettingEntity stored = profilePropertySettingDAO.find(parent.getId());
    assertFalse(stored.isListedOnAccountForm());
    assertFalse(stored.isListedOnRegistration());
    assertFalse(stored.isEditable());
    assertFalse(stored.isVisible());
    assertFalse(stored.isActive());
    assertTrue(stored.isRequired());
    assertTrue(stored.isMultiValued());
    assertFalse(stored.isHiddenable());
    assertTrue(stored.isIndexInAnalytics());
    assertEquals(Long.valueOf(77), stored.getOrder());
    assertEquals("concurrent", stored.getPropertyType());
    assertEquals("concurrentName", stored.getPropertyName());
    assertTrue(stored.isGroupSynchronized());
    assertTrue(stored.isDropdownList());
    assertEquals(Long.valueOf(999), stored.getParentId());
  }

  private void assertParentReadBack(Long parentId, long parentUpdated) {
    ProfilePropertySetting read = profilePropertyService.getProfileSettingById(parentId);
    assertFalse(read.isListedOnAccountForm());
    assertFalse(read.isListedOnRegistration());
    assertTrue(read.getUpdated() > parentUpdated);
    // Its cached copy
    read = profilePropertyService.getProfileSettingById(parentId);
    assertFalse(read.isListedOnAccountForm());
    assertFalse("A change committed by another writer after the request first read the parent", read.isEditable());
  }

  private void commitInAnotherEntityManager(String jpql) {
    EntityManager current = getService(EntityManagerService.class).getEntityManager();
    EntityManager other = current.getEntityManagerFactory().createEntityManager();
    try {
      other.getTransaction().begin();
      assertEquals(1, other.createQuery(jpql).executeUpdate());
      other.getTransaction().commit();
    } finally {
      other.close();
    }
  }

  private ProfilePropertySetting createListed(String name, Consumer<ProfilePropertySetting> configurer) throws Exception {
    ProfilePropertySetting setting = newSetting(name);
    setting.setListedOnAccountForm(true);
    setting.setListedOnRegistration(true);
    configurer.accept(setting);
    return profilePropertyService.createPropertySetting(setting);
  }

  /**
   * A copy of the stored setting, as the REST layer builds one from a request:
   * the instance the cache returns is never mutated.
   */
  private ProfilePropertySetting copyOf(Long id) {
    ProfilePropertySetting stored = profilePropertyService.getProfileSettingById(id);
    ProfilePropertySetting copy = new ProfilePropertySetting();
    copy.setId(stored.getId());
    copy.setPropertyName(stored.getPropertyName());
    copy.setPropertyType(stored.getPropertyType());
    copy.setDropdownList(stored.isDropdownList());
    copy.setVisible(stored.isVisible());
    copy.setEditable(stored.isEditable());
    copy.setParentId(stored.getParentId());
    copy.setOrder(stored.getOrder());
    copy.setActive(stored.isActive());
    copy.setGroupSynchronized(stored.isGroupSynchronized());
    copy.setHiddenbale(stored.isHiddenbale());
    copy.setIndexInAnalytics(stored.isIndexInAnalytics());
    copy.setRequired(stored.isRequired());
    copy.setMultiValued(stored.isMultiValued());
    copy.setPropertyOptions(stored.getPropertyOptions());
    copy.setUpdated(stored.getUpdated());
    copy.setListedOnAccountForm(stored.isListedOnAccountForm());
    copy.setListedOnRegistration(stored.isListedOnRegistration());
    return copy;
  }

  private ProfilePropertySetting newSetting(String name) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setActive(true);
    setting.setEditable(true);
    setting.setVisible(true);
    setting.setOrder(0L);
    return setting;
  }

  private void assertListed(Long id, boolean listedOnAccountForm, boolean listedOnRegistration) {
    ProfilePropertySetting setting = profilePropertyService.getProfileSettingById(id);
    assertEquals("Listed in account form of " + setting.getPropertyName(), listedOnAccountForm, setting.isListedOnAccountForm());
    assertEquals("Listed in registration form of " + setting.getPropertyName(),
                 listedOnRegistration,
                 setting.isListedOnRegistration());
  }

  private List<Long> listedIds(ProfileFormKind formKind) {
    return profileSettingStorage.getListedSettings(formKind).stream().map(ProfilePropertySetting::getId).toList();
  }

  /** The update dates compared by the tests move with the clock */
  private void waitForTheClockToMove() {
    long now = System.currentTimeMillis();
    while (System.currentTimeMillis() <= now) {
      Thread.onSpinWait();
    }
  }

  private void cleanData() {
    restartTransaction();
    profilePropertySettingDAO.deleteAll();
    getService(CachedProfileSettingStorage.class).clearCaches();
  }

}
