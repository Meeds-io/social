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
package io.meeds.social.core.profileproperty.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.social.common.lifecycle.LifeCycleCompletionService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertyOptionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import io.meeds.social.core.profileproperty.constant.ProfileFormKind;
import io.meeds.social.core.profileproperty.model.ProfilePropertyValue;
import io.meeds.social.core.profileproperty.util.ProfilePropertyValueUtil;

/**
 * The group memberships the values the account forms write grant, on the
 * PicketLink IDM store and HSQLDB: the container boots
 * UserAccountServiceImplPicketLinkTest's configuration, with the group
 * synchronization of group-synchronized attributes. Each test waits for the
 * synchronization, which the profile write runs asynchronously.
 */
public class ProfileFormFieldServicePicketLinkTest {

  private static final String        ADMINISTRATOR = "root";

  private PortalContainer            container;

  private OrganizationService        organizationService;

  private IdentityManager            identityManager;

  private ProfilePropertyService     profilePropertyService;

  private LifeCycleCompletionService completionService;

  private ProfileFormFieldService    formFieldService;

  private List<Long>                 createdSettingIds = new ArrayList<>();

  @Before
  public void setUp() {
    container = PortalContainer.getInstance();
    ExoContainerContext.setCurrentContainer(container);
    organizationService = container.getComponentInstanceOfType(OrganizationService.class);
    assertTrue("The organization service of this suite is the PicketLink IDM one, not " + organizationService.getClass(),
               organizationService instanceof PicketLinkIDMOrganizationServiceImpl);
    identityManager = container.getComponentInstanceOfType(IdentityManager.class);
    profilePropertyService = container.getComponentInstanceOfType(ProfilePropertyService.class);
    completionService = container.getComponentInstanceOfType(LifeCycleCompletionService.class);
    formFieldService = new ProfileFormFieldService(container.getComponentInstanceOfType(ProfileSettingStorage.class),
                                                   container.getComponentInstanceOfType(IdentityStorage.class),
                                                   identityManager);
    RequestLifeCycle.begin(container);
  }

  @After
  public void tearDown() {
    try {
      RequestLifeCycle.restartTransaction();
      createdSettingIds.forEach(profilePropertyService::deleteProfilePropertySetting);
    } finally {
      RequestLifeCycle.end();
    }
  }

  /**
   * A value whose group id is exactly 200 characters, the bound the forms
   * apply, gets its group and the user's membership.
   */
  @Test
  public void testAValueWhoseGroupIdIs200CharactersGetsItsGroupAndMembership() throws Exception {
    String login = createUser();
    String name = uniqueName("bound");
    createSynchronizedSetting(name, false);
    String value = "v".repeat(ProfilePropertyValueUtil.MAX_GROUP_ID_LENGTH - groupIdPrefix(name).length());
    String groupId = groupIdPrefix(name) + value;
    assertEquals(200, groupId.length());

    save(login, List.of(single(name, value)), null);

    assertNotNull("Group " + groupId, organizationService.getGroupHandler().findGroupById(groupId));
    assertTrue("Membership of " + groupId, isMember(login, groupId));
  }

  /**
   * A synchronized attribute cleared through the form fields, whether it held
   * a text, a stored list or, multi-valued, a list, leaves its groups, in a
   * save that also changes another synchronized attribute, whose membership
   * moves; a later profile update leaves both as they are.
   */
  @Test
  public void testAClearedSynchronizedValueLeavesItsGroupsAndAnotherOneMoves() throws Exception {
    String login = createUser();
    String text = createSynchronizedSetting(uniqueName("text"), false);
    String storedList = createSynchronizedSetting(uniqueName("stored"), false);
    String multiValued = createSynchronizedSetting(uniqueName("multi"), true);
    String changed = createSynchronizedSetting(uniqueName("changed"), false);

    Profile profile = profileOf(login);
    profile.setProperty(text, "alpha");
    profile.setProperty(storedList, valueList("beta"));
    profile.setProperty(multiValued, valueList("gamma", "delta"));
    profile.setProperty(changed, "before");
    writeContact(profile);
    for (String groupId : List.of(groupIdPrefix(text) + "alpha",
                                  groupIdPrefix(storedList) + "beta",
                                  groupIdPrefix(multiValued) + "gamma",
                                  groupIdPrefix(multiValued) + "delta",
                                  groupIdPrefix(changed) + "before")) {
      assertTrue("Membership of " + groupId + " before the clear", isMember(login, groupId));
    }

    save(login,
         List.of(single(text, ""), single(storedList, ""), multi(multiValued, List.of()), single(changed, "after")),
         profileOf(login));

    assertClearedAndMoved(login, text, storedList, multiValued, changed);

    Profile laterProfile = profileOf(login);
    laterProfile.setProperty("city", "Tunis");
    writeContact(laterProfile);
    assertClearedAndMoved(login, text, storedList, multiValued, changed);
  }

  /**
   * A listed synchronized attribute another node created, which this node's
   * cached settings list lacks, is written with the contact event: its value
   * grants the membership, a changed value moves it and a cleared one removes
   * it.
   */
  @Test
  public void testASettingOnlyTheStoreHoldsIsWrittenWithItsMemberships() throws Exception {
    String login = createUser();
    profilePropertyService.getPropertySettings();
    String name = uniqueName("store");
    ProfilePropertySetting setting = newSetting(name, false);
    ProfileSettingStorage storeOnly = new ProfileSettingStorage(container.getComponentInstanceOfType(ProfilePropertySettingDAO.class),
                                                                container.getComponentInstanceOfType(ProfilePropertyOptionDAO.class));
    createdSettingIds.add(storeOnly.saveProfilePropertySetting(setting, true).getId());
    RequestLifeCycle.restartTransaction();
    assertTrue("The cached settings list lacks " + name,
               profilePropertyService.getPropertySettings().stream().noneMatch(s -> name.equals(s.getPropertyName())));
    assertTrue("The form lists " + name,
               formFieldService.getSettings(ProfileFormKind.ACCOUNT).stream().anyMatch(s -> name.equals(s.getPropertyName())));

    save(login, List.of(single(name, "first")), null);
    assertTrue(isMember(login, groupIdPrefix(name) + "first"));

    save(login, List.of(single(name, "second")), profileOf(login));
    assertFalse(isMember(login, groupIdPrefix(name) + "first"));
    assertTrue(isMember(login, groupIdPrefix(name) + "second"));

    save(login, List.of(single(name, "")), profileOf(login));
    assertFalse(isMember(login, groupIdPrefix(name) + "second"));
  }

  private void assertClearedAndMoved(String login, String text, String storedList, String multiValued, String changed) throws Exception {
    for (String groupId : List.of(groupIdPrefix(text) + "alpha",
                                  groupIdPrefix(storedList) + "beta",
                                  groupIdPrefix(multiValued) + "gamma",
                                  groupIdPrefix(multiValued) + "delta",
                                  groupIdPrefix(changed) + "before")) {
      assertFalse("Membership of " + groupId, isMember(login, groupId));
    }
    assertTrue("Membership of the changed value", isMember(login, groupIdPrefix(changed) + "after"));
  }

  private void save(String login, List<ProfilePropertyValue> values, Profile stored) throws Exception {
    Map<String, Object> profileValues = formFieldService.validate(values, ProfileFormKind.ACCOUNT, false, stored);
    formFieldService.save(login, profileValues, ADMINISTRATOR);
    waitForTheSynchronization();
  }

  /** A profile write with the contact event, as the profile's own edition makes */
  private void writeContact(Profile profile) {
    profile.setListUpdateTypes(new ArrayList<>(List.of(Profile.UpdateType.CONTACT)));
    identityManager.updateProfile(profile, ADMINISTRATOR, false);
    waitForTheSynchronization();
  }

  private void waitForTheSynchronization() {
    completionService.waitAllTaskFinished(10000);
    RequestLifeCycle.restartTransaction();
  }

  private boolean isMember(String login, String groupId) throws Exception {
    return !organizationService.getMembershipHandler().findMembershipsByUserAndGroup(login, groupId).isEmpty();
  }

  private Profile profileOf(String login) {
    return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, login).getProfile();
  }

  private String createUser() throws Exception {
    String login = "fields" + System.nanoTime();
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.createUserInstance(login);
    user.setFirstName("Jane");
    user.setLastName("Doe");
    user.setEmail(login + "@example.com");
    user.setPassword("Password1!");
    userHandler.createUser(user, false);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, login);
    assertNotNull(identity);
    RequestLifeCycle.restartTransaction();
    return login;
  }

  private String createSynchronizedSetting(String name, boolean multiValued) throws Exception {
    ProfilePropertySetting setting = profilePropertyService.createPropertySetting(newSetting(name, multiValued));
    createdSettingIds.add(setting.getId());
    RequestLifeCycle.restartTransaction();
    return name;
  }

  private ProfilePropertySetting newSetting(String name, boolean multiValued) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setActive(true);
    setting.setEditable(true);
    setting.setVisible(true);
    setting.setGroupSynchronized(true);
    setting.setMultiValued(multiValued);
    setting.setOrder(0L);
    setting.setListedOnAccountForm(true);
    return setting;
  }

  /** Lower-case letters and digits, which the group names keep as they are */
  private String uniqueName(String prefix) {
    return prefix + System.nanoTime();
  }

  private String groupIdPrefix(String name) {
    return ProfilePropertyValueUtil.SYNCHRONIZED_GROUP_ID_PREFIX + name + "/";
  }

  private ArrayList<Map<String, String>> valueList(String... values) {
    ArrayList<Map<String, String>> list = new ArrayList<>();
    for (String value : values) {
      Map<String, String> map = new HashMap<>();
      map.put("value", value);
      list.add(map);
    }
    return list;
  }

  private ProfilePropertyValue single(String name, String value) {
    return new ProfilePropertyValue(name, value, null);
  }

  private ProfilePropertyValue multi(String name, List<String> values) {
    return new ProfilePropertyValue(name, null, values);
  }

}
