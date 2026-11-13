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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertyOptionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

import io.meeds.social.core.profileproperty.service.ProfileFormFieldService;
import io.meeds.social.core.profileproperty.util.ProfilePropertyValueUtil;

import jakarta.persistence.EntityManager;

public class ProfileSettingsRestTest extends AbstractResourceTest {

  private ProfilePropertyService profilePropertyService;

  private List<Long>             createdSettingIds = new ArrayList<>();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    profilePropertyService = getContainer().getComponentInstanceOfType(ProfilePropertyService.class);
    ProfileFormFieldService profileFormFieldService =
                                                    new ProfileFormFieldService(getContainer().getComponentInstanceOfType(ProfileSettingStorage.class),
                                                                                getContainer().getComponentInstanceOfType(IdentityStorage.class),
                                                                                identityManager);
    registry(new ProfileSettingsRest(profilePropertyService, profileFormFieldService));
  }

  @Override
  public void tearDown() throws Exception {
    removeResource(ProfileSettingsRest.class);
    restartTransaction();
    createdSettingIds.forEach(profilePropertyService::deleteProfilePropertySetting);
    super.tearDown();
  }

  /**
   * The attribute form disables both listing switches on these names, read from
   * the server's answer rather than from a copy of its own.
   */
  @SuppressWarnings("unchecked")
  public void testTheSettingsAnswerCarriesTheNamesNoFormLists() throws Exception {
    startSessionAs("root");

    ContainerResponse response = service("GET", getURLResource("profile/settings"), "", null, null);

    assertEquals(200, response.getStatus());
    Map<String, List<?>> answer = (Map<String, List<?>>) response.getEntity();
    assertEquals(ProfilePropertyValueUtil.UNLISTABLE_NAMES, answer.get("unlistableProperties"));
  }

  /**
   * The account form's list holds only the attributes listed in it, while the
   * other lists of the answer stay as they are.
   */
  @SuppressWarnings("unchecked")
  public void testTheAccountFormListsOnlyItsAttributes() throws Exception {
    createSetting("formListed", true);
    createSetting("formNotListed", false);
    startSessionAs("root");

    Map<String, List<?>> answer = getSettings("?form=account");
    assertEquals(List.of("formListed"), names(answer));
    assertEquals(ProfilePropertyValueUtil.UNLISTABLE_NAMES, answer.get("unlistableProperties"));
    assertTrue(answer.containsKey("unHiddenableProperties"));

    List<String> allNames = names(getSettings(""));
    assertTrue(allNames.containsAll(List.of("formListed", "formNotListed")));
  }

  public void testAnotherFormIsRefused() throws Exception {
    startSessionAs("root");
    for (String form : List.of("registration", "Account", "")) {
      ContainerResponse response = service("GET", getURLResource("profile/settings?form=" + form), "", null, null);
      assertEquals(form, 400, response.getStatus());
    }
  }

  /**
   * The account form's list follows the store while this node's settings
   * caches hold the previous state: a setting another writer switched off is
   * absent from it, and a listed setting another node created, which the
   * cached list lacks, is in it.
   */
  public void testTheAccountFormListFollowsTheStoreWhileTheCachesHoldThePreviousState() throws Exception {
    Long switchedOffId = createSetting("formSwitchedOff", true);
    startSessionAs("root");
    assertTrue(names(getSettings("")).contains("formSwitchedOff"));
    restartTransaction();

    commitInAnotherEntityManager("UPDATE SocProfileSettingEntity s SET s.listedOnAccountForm = false WHERE s.id = " + switchedOffId);
    ProfileSettingStorage storeOnly = new ProfileSettingStorage(getContainer().getComponentInstanceOfType(ProfilePropertySettingDAO.class),
                                                                getContainer().getComponentInstanceOfType(ProfilePropertyOptionDAO.class));
    createdSettingIds.add(storeOnly.saveProfilePropertySetting(newSetting("formStoreOnly", true), true).getId());
    restartTransaction();

    List<String> cachedNames = names(getSettings(""));
    assertTrue("The cached list still holds the switched-off setting", cachedNames.contains("formSwitchedOff"));
    assertFalse("The cached list lacks the setting only the store holds", cachedNames.contains("formStoreOnly"));
    assertEquals(List.of("formStoreOnly"), names(getSettings("?form=account")));
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<?>> getSettings(String query) throws Exception {
    ContainerResponse response = service("GET", getURLResource("profile/settings" + query), "", null, null);
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
    return (Map<String, List<?>>) response.getEntity();
  }

  private List<String> names(Map<String, List<?>> answer) {
    return answer.get("settings")
                 .stream()
                 .map(setting -> ((ProfilePropertySettingEntity) setting).getPropertyName())
                 .filter(name -> name.startsWith("form"))
                 .toList();
  }

  private Long createSetting(String name, boolean listedOnAccountForm) throws Exception {
    Long id = profilePropertyService.createPropertySetting(newSetting(name, listedOnAccountForm)).getId();
    createdSettingIds.add(id);
    return id;
  }

  private ProfilePropertySetting newSetting(String name, boolean listedOnAccountForm) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setActive(true);
    setting.setEditable(true);
    setting.setVisible(true);
    setting.setOrder(0L);
    setting.setListedOnAccountForm(listedOnAccountForm);
    return setting;
  }

  private void commitInAnotherEntityManager(String jpql) {
    EntityManager current = getContainer().getComponentInstanceOfType(EntityManagerService.class).getEntityManager();
    EntityManager other = current.getEntityManagerFactory().createEntityManager();
    try {
      other.getTransaction().begin();
      assertEquals(1, other.createQuery(jpql).executeUpdate());
      other.getTransaction().commit();
    } finally {
      other.close();
    }
  }

}
