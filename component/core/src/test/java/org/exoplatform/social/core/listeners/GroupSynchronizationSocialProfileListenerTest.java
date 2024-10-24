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

package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.common.lifecycle.LifeCycleCompletionService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class GroupSynchronizationSocialProfileListenerTest extends AbstractCoreTest {

  private OrganizationService            organizationService;

  private IdentityManager                identityManager;

  private ProfilePropertyService profilePropertyService;
  private LifeCycleCompletionService      completionService;

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
    assertEquals(3, groups.size());
    //
    identityManager.updateProfile(profile);
    groups = organizationService.getGroupHandler().findGroupsOfUser(paulRemoteId);
    assertEquals(3, groups.size());


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
}
