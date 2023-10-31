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

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

public class GroupSynchronizationSocialProfileListenerTest extends AbstractCoreTest {

  private OrganizationService            organizationService;

  private IdentityManager                identityManager;

  private ProfilePropertyService profilePropertyService;

  private Identity                       paul;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    profilePropertyService = getContainer().getComponentInstanceOfType(ProfilePropertyService.class);

    paul = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
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
    String expectedGroupName = "testpropertytest";
    StringBuilder expectedGroupId = new StringBuilder();
    expectedGroupId.append("/profile/");
    expectedGroupId.append(propertyName);
    expectedGroupId.append("/");
    expectedGroupId.append(expectedGroupName);
    profile.setProperty("propertytest", groupLabel);
    identityManager.updateProfile(profile, true);
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
}
