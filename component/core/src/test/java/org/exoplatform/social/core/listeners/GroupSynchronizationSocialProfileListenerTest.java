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
package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.common.lifecycle.LifeCycleCompletionService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class GroupSynchronizationSocialProfileListenerTest extends AbstractCoreTest {

  private OrganizationService            organizationService;

  private IdentityManager                identityManager;

  private ProfilePropertyService profilePropertyService;
  private LifeCycleCompletionService      completionService;

  private final List<Long>              createdSettingIds = new ArrayList<>();

  private Identity                       paul;
  private Identity                       john;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    profilePropertyService = getContainer().getComponentInstanceOfType(ProfilePropertyService.class);
    completionService = getContainer().getComponentInstanceOfType(LifeCycleCompletionService.class);

    paul = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
    john = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);
  }

  @After
  public void cleanUpSynchronizedProperties() throws Exception {
    createdSettingIds.forEach(profilePropertyService::deleteProfilePropertySetting);
    createdSettingIds.clear();
    for (String propertyGroupId : List.of("/profile/teamassignment", "/profile/officelocation")) {
      removeGroupTree(propertyGroupId);
    }
  }

  private void removeGroupTree(String groupId) throws Exception {
    Group group = organizationService.getGroupHandler().findGroupById(groupId);
    if (group == null) {
      return;
    }
    for (Group child : organizationService.getGroupHandler().findGroups(group)) {
      removeGroupTree(child.getId());
    }
    for (String username : List.of("paul", "john")) {
      for (Membership membership : new ArrayList<>(organizationService.getMembershipHandler().findMembershipsByUser(username))) {
        if (membership.getGroupId().equals(groupId)) {
          organizationService.getMembershipHandler().removeMembership(membership.getId(), false);
        }
      }
    }
    organizationService.getGroupHandler().removeGroup(group, false);
  }

  @Test
  public void testProfilePropertiesGroupSynchronization() throws Exception {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName("postalCode");
    profilePropertySetting.setGroupSynchronized(true);
    profilePropertySetting.setMultiValued(false);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    profilePropertyService.createPropertySetting(profilePropertySetting);
    profilePropertySetting.setPropertyName("street");
    profilePropertyService.createPropertySetting(profilePropertySetting);

    String paulRemoteId = "paul";
    Profile profile = paul.getProfile();
    profile.setProperty("postalCode", "2100");
    identityManager.updateProfile(profile, true);

    completionService.waitAllTaskFinished(1000); //wait 1 second for listener finish


    Group group = organizationService.getGroupHandler().findGroupById("/profile/postalcode/2100");
    assertNotNull(group);
    Collection<Group> groups = organizationService.getGroupHandler().findGroupsOfUser(paulRemoteId);
    assertTrue(groups.contains(group));
    int groupsCount = groups.size();
    Group group1 = organizationService.getGroupHandler().findGroupById("/profile/street");
    assertNull(group1);
    //
    String propertyName = "propertytest";
    profilePropertySetting.setPropertyName(propertyName);
    profilePropertyService.createPropertySetting(profilePropertySetting);
    String groupLabel = "Test'propertytest";
    String expectedGroupName = "test_propertytest";
    StringBuilder expectedGroupId = new StringBuilder();
    expectedGroupId.append("/profile/");
    expectedGroupId.append(propertyName);
    expectedGroupId.append("/");
    expectedGroupId.append(expectedGroupName);
    profile.setProperty("propertytest", groupLabel);
    identityManager.updateProfile(profile, true);

    completionService.waitAllTaskFinished(1000); //wait 1 second for listener finish

    Group group2 = organizationService.getGroupHandler().findGroupById(expectedGroupId.toString());
    assertNotNull(group2);
    assertEquals(group2.getLabel(), groupLabel);
    assertEquals(group2.getGroupName(), expectedGroupName);
    groups = organizationService.getGroupHandler().findGroupsOfUser(paulRemoteId);
    assertTrue(groups.contains(group2));
    assertEquals(groupsCount + 1, groups.size());
    //
    identityManager.updateProfile(profile);
    groups = organizationService.getGroupHandler().findGroupsOfUser(paulRemoteId);
    assertEquals(groupsCount + 1, groups.size());


  }

  @Test
  public void testSynchronizingAnAttributeKeepsGroupsOfAttributeWithSamePrefix() throws Exception {
    createSynchronizedSetting("syncteam", false);
    createSynchronizedSetting("syncteams", false);

    Profile profile = new Profile(john);
    profile.setProperty("syncteams", "Alpha");
    synchronize(profile);
    Group alphaGroup = organizationService.getGroupHandler().findGroupById("/profile/syncteams/alpha");
    assertNotNull(alphaGroup);
    assertTrue(isMember("john", alphaGroup));

    profile = new Profile(john);
    profile.setProperty("syncteam", "Blue");
    synchronize(profile);

    Group blueGroup = organizationService.getGroupHandler().findGroupById("/profile/syncteam/blue");
    assertNotNull(blueGroup);
    assertTrue(isMember("john", blueGroup));
    assertTrue(isMember("john", alphaGroup));

    profile = new Profile(john);
    profile.setProperty("syncteam", "Green");
    synchronize(profile);
    assertFalse(isMember("john", blueGroup));
    assertTrue(isMember("john", organizationService.getGroupHandler().findGroupById("/profile/syncteam/green")));
    assertTrue(isMember("john", alphaGroup));
  }

  @Test
  public void testValueTooLongForAGroupIdIsSkippedAndOtherValuesAreSynchronized() throws Exception {
    createSynchronizedSetting("syncdivision", false);
    createSynchronizedSetting("syncremarks", false);

    Profile profile = new Profile(john);
    profile.setProperty("syncremarks", "a".repeat(220));
    profile.setProperty("syncdivision", "Sales");
    synchronize(profile);

    Group salesGroup = organizationService.getGroupHandler().findGroupById("/profile/syncdivision/sales");
    assertNotNull(salesGroup);
    assertTrue(isMember("john", salesGroup));
    assertNull(organizationService.getGroupHandler().findGroupById("/profile/syncremarks/" + "a".repeat(220)));
    assertTrue(organizationService.getGroupHandler()
                                  .findGroupsOfUser("john")
                                  .stream()
                                  .noneMatch(g -> g.getId().startsWith("/profile/syncremarks/")));
  }

  @Test
  public void testMembershipInGroupOfRefusedValueIsKept() throws Exception {
    createSynchronizedSetting("syncretained", false);
    Profile profile = new Profile(john);
    profile.setProperty("syncretained", "Seed");
    synchronize(profile);
    Group attributeGroup = organizationService.getGroupHandler().findGroupById("/profile/syncretained");
    assertNotNull(attributeGroup);

    String longName = "a".repeat(220);
    Group legacyGroup = organizationService.getGroupHandler().createGroupInstance();
    legacyGroup.setGroupName(longName);
    legacyGroup.setLabel("Legacy");
    organizationService.getGroupHandler().addChild(attributeGroup, legacyGroup, true);
    legacyGroup = organizationService.getGroupHandler().findGroupById("/profile/syncretained/" + longName);
    assertNotNull(legacyGroup);
    organizationService.getMembershipHandler()
                       .linkMembership(organizationService.getUserHandler().findUserByName("john"),
                                       legacyGroup,
                                       organizationService.getMembershipTypeHandler().findMembershipType("member"),
                                       true);
    assertTrue(isMember("john", legacyGroup));

    profile = new Profile(john);
    profile.setProperty("syncretained", longName);
    synchronize(profile);

    assertTrue(isMember("john", legacyGroup));
  }

  @Test
  public void testMembershipInAttributeGroupItselfIsRemoved() throws Exception {
    createSynchronizedSetting("syncitself", false);
    Profile profile = new Profile(john);
    profile.setProperty("syncitself", "One");
    synchronize(profile);
    Group attributeGroup = organizationService.getGroupHandler().findGroupById("/profile/syncitself");
    assertNotNull(attributeGroup);
    organizationService.getMembershipHandler()
                       .linkMembership(organizationService.getUserHandler().findUserByName("john"),
                                       attributeGroup,
                                       organizationService.getMembershipTypeHandler().findMembershipType("member"),
                                       true);
    assertTrue(isMember("john", attributeGroup));

    synchronize(profile);

    assertFalse(isMember("john", attributeGroup));
    assertTrue(isMember("john", organizationService.getGroupHandler().findGroupById("/profile/syncitself/one")));
  }

  @Test
  public void testAttributeNameTooLongForAGroupIsSkippedAndOtherAttributesAreSynchronized() throws Exception {
    String wideAttribute = "syncwide" + "w".repeat(185);
    createSynchronizedSetting("syncdivision", false);
    createSynchronizedSetting(wideAttribute, false);

    Profile profile = new Profile(john);
    profile.setProperty(wideAttribute, "Value");
    profile.setProperty("syncdivision", "Legal");
    synchronize(profile);

    assertTrue(isMember("john", organizationService.getGroupHandler().findGroupById("/profile/syncdivision/legal")));
    assertNull(organizationService.getGroupHandler().findGroupById("/profile/" + wideAttribute));
  }

  @Test
  public void testValueWithoutAnyUsableCharacterIsSkippedAndOtherValuesAreSynchronized() throws Exception {
    createSynchronizedSetting("syncdivision", false);
    createSynchronizedSetting("syncalias", false);

    Profile profile = new Profile(john);
    profile.setProperty("syncalias", "  ");
    profile.setProperty("syncdivision", "Finance");
    synchronize(profile);

    assertTrue(isMember("john", organizationService.getGroupHandler().findGroupById("/profile/syncdivision/finance")));
    assertTrue(organizationService.getGroupHandler()
                                  .findGroupsOfUser("john")
                                  .stream()
                                  .noneMatch(g -> g.getId().startsWith("/profile/syncalias")));
  }

  @Test
  public void testDropdownAttributeWithHyphenLabelsOptionGroupsWithOptionText() throws Exception {
    ProfilePropertySetting setting = createSynchronizedSetting("sync-job-title", true);
    ProfilePropertyOption option = setting.getPropertyOptions().get(0);

    Profile profile = new Profile(john);
    profile.setProperty("sync-job-title", String.valueOf(option.getId()));
    synchronize(profile);

    Group group = organizationService.getGroupHandler().findGroupById("/profile/sync_job_title/" + option.getId());
    assertNotNull(group);
    assertEquals(option.getValue(), group.getLabel());
    assertTrue(isMember("john", group));
  }

  @Test
  public void testDropdownAttributeKeepsRawValueLabelForTextOrUnmatchedDigitsLeftBeforeTheSwitch() throws Exception {
    ProfilePropertySetting setting = createSynchronizedSetting("sync-job-title", true);
    long unmatchedId = setting.getPropertyOptions().stream().mapToLong(ProfilePropertyOption::getId).max().orElse(0) + 1000L;

    Profile profile = new Profile(john);
    profile.setProperty("sync-job-title", "Legacytext");
    synchronize(profile);
    Group textGroup = organizationService.getGroupHandler().findGroupById("/profile/sync_job_title/legacytext");
    assertNotNull(textGroup);
    assertEquals("Legacytext", textGroup.getLabel());
    assertTrue(isMember("john", textGroup));

    profile.setProperty("sync-job-title", String.valueOf(unmatchedId));
    synchronize(profile);
    Group digitsGroup = organizationService.getGroupHandler().findGroupById("/profile/sync_job_title/" + unmatchedId);
    assertNotNull(digitsGroup);
    assertEquals(String.valueOf(unmatchedId), digitsGroup.getLabel());
    assertTrue(isMember("john", digitsGroup));
    assertFalse(isMember("john", textGroup));
  }

  private ProfilePropertySetting createSynchronizedSetting(String name, boolean dropdown) throws Exception {
    ProfilePropertySetting existing = profilePropertyService.getProfileSettingByName(name);
    if (existing != null) {
      return existing;
    }
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setActive(true);
    setting.setEditable(true);
    setting.setVisible(true);
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setGroupSynchronized(true);
    setting.setMultiValued(false);
    setting.setParentId(0L);
    setting.setOrder(0L);
    if (dropdown) {
      setting.setDropdownList(true);
      List<ProfilePropertyOption> options = new ArrayList<>();
      options.add(newOption("Engineering"));
      options.add(newOption("Marketing"));
      setting.setPropertyOptions(options);
    }
    return profilePropertyService.createPropertySetting(setting);
  }

  private ProfilePropertyOption newOption(String value) {
    ProfilePropertyOption option = new ProfilePropertyOption(null, value, null);
    option.setTranslations(new HashMap<>());
    return option;
  }

  private void synchronize(Profile profile) {
    RequestLifeCycle.begin(getContainer());
    try {
      new GroupSynchronizationSocialProfileListener(profilePropertyService, organizationService)
          .createProfile(new ProfileLifeCycleEvent(ProfileLifeCycleEvent.Type.CREATED, profile.getIdentity().getRemoteId(), profile));
    } finally {
      RequestLifeCycle.end();
    }
  }

  private boolean isMember(String username, Group group) throws Exception {
    return group != null && organizationService.getGroupHandler().findGroupsOfUser(username).stream().anyMatch(g -> g.getId().equals(group.getId()));
  }

  @Test
  public void testMultivaluedProfilePropertiesGroupSynchronization() throws Exception {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName("multivaluedProperty");
    profilePropertySetting.setGroupSynchronized(true);
    profilePropertySetting.setMultiValued(true);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    profilePropertyService.createPropertySetting(profilePropertySetting);

    Profile profile = john.getProfile();
    List<Map<String,String>> values = new ArrayList<>();
    Map<String,String> value1 = new HashMap<>();
    value1.put("key",null);
    value1.put("value","multivaluedPropertyValue1");
    values.add(value1);

    Map<String,String> value2 = new HashMap<>();
    value2.put("key",null);
    value2.put("value","multivaluedPropertyValue2");
    values.add(value2);

    profile.setProperty("multivaluedProperty", values);
    identityManager.updateProfile(profile, true);
    completionService.waitAllTaskFinished(2000); //wait 2 second for listener finish

    Group group1 = organizationService.getGroupHandler().findGroupById("/profile/multivaluedproperty/multivaluedpropertyvalue1");
    assertNotNull(group1);
    Group group2 = organizationService.getGroupHandler().findGroupById("/profile/multivaluedproperty/multivaluedpropertyvalue2");
    assertNotNull(group2);
    Collection<Group> groups = organizationService.getGroupHandler().findGroupsOfUser(john.getRemoteId());
    assertTrue(groups.contains(group1));
    assertTrue(groups.contains(group2));


    values = new ArrayList<>();
    Map<String,String> value3 = new HashMap<>();
    value3.put("key",null);
    value3.put("value","multivaluedPropertyValue3");
    values.add(value3);
    values.add(value2);

    profile.setProperty("multivaluedProperty", values);
    identityManager.updateProfile(profile, true);
    completionService.waitAllTaskFinished(2000); //wait 2 second for listener finish
    group2 = organizationService.getGroupHandler().findGroupById("/profile/multivaluedproperty/multivaluedpropertyvalue2");
    assertNotNull(group2);
    Group group3 = organizationService.getGroupHandler().findGroupById("/profile/multivaluedproperty/multivaluedpropertyvalue3");
    assertNotNull(group3);
    groups = organizationService.getGroupHandler().findGroupsOfUser(john.getRemoteId());
    assertFalse(groups.contains(group1));
    assertTrue(groups.contains(group2));
    assertTrue(groups.contains(group3));

  }

  @Test
  public void testGroupsFollowProfileChangeOfDisabledAccount() throws Exception {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName("teamAssignment");
    profilePropertySetting.setGroupSynchronized(true);
    profilePropertySetting.setMultiValued(false);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    createdSettingIds.add(profilePropertyService.createPropertySetting(profilePropertySetting).getId());

    String username = "paul";
    UserHandler userHandler = organizationService.getUserHandler();
    Profile profile = paul.getProfile();
    profile.setProperty("teamAssignment", "sales");
    identityManager.updateProfile(profile, true);
    completionService.waitAllTaskFinished(2000);

    Group oldGroup = organizationService.getGroupHandler().findGroupById("/profile/teamassignment/sales");
    assertNotNull(oldGroup);
    assertTrue(organizationService.getGroupHandler().findGroupsOfUser(username).contains(oldGroup));

    try {
      userHandler.setEnabled(username, false, true);
      profile.setProperty("teamAssignment", "support");
      identityManager.updateProfile(profile, true);
      completionService.waitAllTaskFinished(2000);

      Group newGroup = organizationService.getGroupHandler().findGroupById("/profile/teamassignment/support");
      assertNotNull(newGroup);
      Collection<Group> groups = organizationService.getGroupHandler().findGroupsOfUser(username);
      assertTrue(groups.contains(newGroup));
      assertFalse(groups.contains(oldGroup));
    } finally {
      userHandler.setEnabled(username, true, true);
    }

    Group newGroup = organizationService.getGroupHandler().findGroupById("/profile/teamassignment/support");
    Collection<Group> groups = organizationService.getGroupHandler().findGroupsOfUser(username);
    assertTrue(groups.contains(newGroup));
    assertFalse(groups.contains(oldGroup));
  }

  @Test
  public void testGroupsResynchronizedWhenAccountEnabled() throws Exception {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName("officeLocation");
    profilePropertySetting.setGroupSynchronized(true);
    profilePropertySetting.setMultiValued(false);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    createdSettingIds.add(profilePropertyService.createPropertySetting(profilePropertySetting).getId());

    String username = "john";
    UserHandler userHandler = organizationService.getUserHandler();
    Profile profile = john.getProfile();
    profile.setProperty("officeLocation", "paris");
    identityManager.updateProfile(profile, true);
    completionService.waitAllTaskFinished(2000);

    Group currentGroup = organizationService.getGroupHandler().findGroupById("/profile/officelocation/paris");
    assertNotNull(currentGroup);
    // memberships left behind by a profile that changed without being synchronized
    Group staleGroup = organizationService.getGroupHandler().createGroupInstance();
    staleGroup.setGroupName("tunis");
    staleGroup.setLabel("Tunis");
    organizationService.getGroupHandler()
                       .addChild(organizationService.getGroupHandler().findGroupById("/profile/officelocation"), staleGroup, true);
    staleGroup = organizationService.getGroupHandler().findGroupById("/profile/officelocation/tunis");
    organizationService.getMembershipHandler()
                       .removeMembership(organizationService.getMembershipHandler()
                                                            .findMembershipByUserGroupAndType(username, currentGroup.getId(), "member")
                                                            .getId(),
                                         false);
    organizationService.getMembershipHandler()
                       .linkMembership(userHandler.findUserByName(username),
                                       staleGroup,
                                       organizationService.getMembershipTypeHandler().findMembershipType("member"),
                                       true);

    try {
      userHandler.setEnabled(username, false, true);
      Collection<Group> groups = organizationService.getGroupHandler().findGroupsOfUser(username);
      assertTrue(groups.contains(staleGroup));
      assertFalse(groups.contains(currentGroup));
    } finally {
      userHandler.setEnabled(username, true, true);
    }

    Collection<Group> groups = organizationService.getGroupHandler().findGroupsOfUser(username);
    assertTrue(groups.contains(currentGroup));
    assertFalse(groups.contains(staleGroup));
  }
}
