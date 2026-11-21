/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.organizationalunit.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.social.organizationalunit.model.OrganizationalUnit;
import io.meeds.social.organizationalunit.storage.OrganizationalUnitStorage;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationalUnitServiceTest {

  private static final String GROUP_ID = "/platform/test";

  private static final String LABEL    = "Test Group";

  @Mock
  private OrganizationalUnitStorage organizationalUnitStorage;

  @Mock
  private OrganizationService       organizationService;

  @Mock
  private GroupHandler              groupHandler;

  @InjectMocks
  private OrganizationalUnitService organizationalUnitService;

  @Before
  public void setUp() {
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
  }

  @Test
  public void testIsOrganizationalUnitDelegatesToStorage() {
    when(organizationalUnitStorage.isOrganizationalUnit(GROUP_ID)).thenReturn(true);

    assertTrue(organizationalUnitService.isOrganizationalUnit(GROUP_ID));
  }

  @Test
  public void testGetManagedOrganizationalUnitsDelegatesToStorage() {
    OrganizationalUnit organizationalUnit = new OrganizationalUnit(GROUP_ID, LABEL);
    when(organizationalUnitStorage.getManagedOrganizationalUnits("john")).thenReturn(List.of(organizationalUnit));

    List<OrganizationalUnit> result = organizationalUnitService.getManagedOrganizationalUnits("john");

    assertEquals(1, result.size());
    assertEquals(GROUP_ID, result.get(0).getGroupId());
  }

  @Test
  public void testIsManagedOrganizationalUnitDelegatesToStorage() {
    when(organizationalUnitStorage.isManagedOrganizationalUnit(GROUP_ID, "john")).thenReturn(true);

    assertTrue(organizationalUnitService.isManagedOrganizationalUnit(GROUP_ID, "john"));
    assertFalse(organizationalUnitService.isManagedOrganizationalUnit(GROUP_ID, "mary"));
  }

  @Test
  public void testCanManageGroupWhenGroupIsAManagedOrganizationalUnit() {
    when(organizationalUnitStorage.isManagedOrganizationalUnit(GROUP_ID, "john")).thenReturn(true);

    assertTrue(organizationalUnitService.canManageGroup(GROUP_ID, "john"));
  }

  @Test
  public void testCanManageGroupWhenGroupIsNestedInsideAManagedOrganizationalUnit() throws Exception {
    String nestedGroupId = GROUP_ID + "/sales";
    String deeplyNestedGroupId = GROUP_ID + "/sales/emea";
    mockGroupWithEnclosing(GROUP_ID, null);
    mockGroupWithEnclosing(nestedGroupId, GROUP_ID);
    mockGroupWithEnclosing(deeplyNestedGroupId, nestedGroupId);
    when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(nestedGroupId, GROUP_ID),
                                                                  "john")).thenReturn(true);

    assertTrue(organizationalUnitService.canManageGroup(deeplyNestedGroupId, "john"));
    assertFalse(organizationalUnitService.canManageGroup(deeplyNestedGroupId, "mary"));
  }

  @Test
  public void testCanManageGroupSurvivesEnclosingGroupsCycle() throws Exception {
    String nestedGroupId = GROUP_ID + "/sales";
    // Cycle: each group declares the other as enclosing
    mockGroupWithEnclosing(GROUP_ID, nestedGroupId);
    mockGroupWithEnclosing(nestedGroupId, GROUP_ID);
    when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(nestedGroupId, GROUP_ID), "john")).thenReturn(true);

    assertTrue(organizationalUnitService.canManageGroup(nestedGroupId, "john"));
  }

  @Test
  public void testCanManageGroupMembershipsInsideTheManagedPerimeter() throws Exception {
    String unitId = "/organization/it";
    String nestedGroupId = "/organization/it/team";
    mockGroupWithEnclosing(unitId, null);
    mockGroupWithEnclosing(nestedGroupId, unitId);
    when(organizationalUnitStorage.isManagedOrganizationalUnit(unitId, "john")).thenReturn(true);
    when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(unitId), "john")).thenReturn(true);

    assertTrue(organizationalUnitService.canManageGroupMemberships(unitId, "john"));
    assertTrue(organizationalUnitService.canManageGroupMemberships(nestedGroupId, "john"));
    assertFalse(organizationalUnitService.canManageGroupMemberships(nestedGroupId, "mary"));
  }

  @Test
  public void testCanManageGroupMembershipsRefusesPlatformGroups() throws Exception {
    String unitId = "/organization/it";
    String platformGroupId = "/platform/analytics";
    mockGroupWithEnclosing(platformGroupId, unitId);
    mockGroupWithEnclosing(unitId, null);
    when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(unitId), "john")).thenReturn(true);
    // the platform group itself, inside the perimeter, is refused its memberships
    mockGroupWithEnclosing("/platform", unitId);

    // a platform group nested in the unit stays in the perimeter, but its memberships are refused
    assertTrue(organizationalUnitService.canManageGroup(platformGroupId, "john"));
    assertFalse(organizationalUnitService.canManageGroupMemberships(platformGroupId, "john"));
    assertFalse(organizationalUnitService.canManageGroupMemberships("/platform", "john"));
  }

  @Test
  public void testCanManageGroupMembershipsRefusesGroupsNestedInAPlatformGroup() throws Exception {
    String unitId = "/organization/it";
    String sharedGroupId = "/organization/it/admins";
    String deeperGroupId = "/organization/it/admins/team";
    mockGroupWithEnclosing(unitId, null);
    Group sharedGroup = mock(Group.class);
    lenient().when(sharedGroup.getId()).thenReturn(sharedGroupId);
    when(sharedGroup.getEnclosingMemberships()).thenReturn(Set.of(new NestedMembership(null, unitId, null, sharedGroupId),
                                                                 new NestedMembership(null,
                                                                                      "/platform/analytics",
                                                                                      null,
                                                                                      sharedGroupId)));
    when(groupHandler.findGroupById(sharedGroupId)).thenReturn(sharedGroup);
    mockGroupWithEnclosing("/platform/analytics", null);
    mockGroupWithEnclosing(deeperGroupId, sharedGroupId);
    // even a unit the user manages is refused once it is nested in a platform group
    lenient().when(organizationalUnitStorage.isManagedOrganizationalUnit(sharedGroupId, "john")).thenReturn(true);
    // and so is a group nested in it at any level, though inside the perimeter
    lenient().when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(sharedGroupId, unitId, "/platform/analytics"),
                                                                            "john"))
             .thenReturn(true);

    assertFalse(organizationalUnitService.canManageGroupMemberships(sharedGroupId, "john"));
    assertFalse(organizationalUnitService.canManageGroupMemberships(deeperGroupId, "john"));
  }

  @Test
  public void testCanManageGroupMembershipsRefusesAGroupWhoseEnclosingGroupsCannotBeRead() throws Exception {
    String unitId = "/organization/it";
    String nestedGroupId = "/organization/it/team";
    mockGroupWithEnclosing(nestedGroupId, unitId);
    when(groupHandler.findGroupById(unitId)).thenThrow(new IllegalStateException("store unavailable"));
    lenient().when(organizationalUnitStorage.isManagedOrganizationalUnit(unitId, "john")).thenReturn(true);
    lenient().when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(unitId), "john")).thenReturn(true);

    assertFalse(organizationalUnitService.canManageGroupMemberships(unitId, "john"));
    assertFalse(organizationalUnitService.canManageGroupMemberships(nestedGroupId, "john"));
    // listing keeps the lenient reading: the nested group stays in the perimeter
    assertTrue(organizationalUnitService.canManageGroup(nestedGroupId, "john"));
  }

  @Test
  public void testCanManageGroupMembershipsRefusesWhenOneOfSeveralEnclosingGroupsCannotBeRead() throws Exception {
    String unitId = "/organization/it";
    String otherUnitId = "/organization/sales";
    String sharedGroupId = "/organization/shared";
    Group sharedGroup = mock(Group.class);
    lenient().when(sharedGroup.getId()).thenReturn(sharedGroupId);
    // the unreadable enclosing group comes first, a readable one after it
    when(sharedGroup.getEnclosingMemberships()).thenReturn(new LinkedHashSet<>(List.of(new NestedMembership(null,
                                                                                                         otherUnitId,
                                                                                                         null,
                                                                                                         sharedGroupId),
                                                                                    new NestedMembership(null,
                                                                                                         unitId,
                                                                                                         null,
                                                                                                         sharedGroupId))));
    when(groupHandler.findGroupById(sharedGroupId)).thenReturn(sharedGroup);
    when(groupHandler.findGroupById(otherUnitId)).thenThrow(new IllegalStateException("store unavailable"));
    mockGroupWithEnclosing(unitId, null);
    lenient().when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(unitId, otherUnitId), "john"))
             .thenReturn(true);

    assertFalse(organizationalUnitService.canManageGroupMemberships(sharedGroupId, "john"));
  }

  @Test
  public void testCanManageGroupMembershipsRefusesAnIdThatIsNotTheStoredGroupId() throws Exception {
    String unitId = "/organization/it";
    String givenId = "/organization/sales";
    String storedId = "/organization/sales-team";
    Group storedGroup = mock(Group.class);
    when(storedGroup.getId()).thenReturn(storedId);
    lenient().when(storedGroup.getEnclosingMemberships())
             .thenReturn(Set.of(new NestedMembership(null, unitId, null, storedId)));
    when(groupHandler.findGroupById(givenId)).thenReturn(storedGroup);
    mockGroupWithEnclosing(unitId, null);
    lenient().when(organizationalUnitStorage.isAnyManagedOrganizationalUnit(Set.of(unitId), "john")).thenReturn(true);

    assertFalse(organizationalUnitService.canManageGroupMemberships(givenId, "john"));
  }

  @Test
  public void testCanManageGroupMembershipsRefusesAnUnknownGroup() throws Exception {
    String groupId = "/organization/unknown";
    when(groupHandler.findGroupById(groupId)).thenReturn(null);
    lenient().when(organizationalUnitStorage.isManagedOrganizationalUnit(groupId, "john")).thenReturn(true);

    assertFalse(organizationalUnitService.canManageGroupMemberships(groupId, "john"));
  }

  @Test
  public void testCanManageGroupMembershipsRefusesAGroupThatCouldNotBeRead() throws Exception {
    String groupId = "/organization/finance";
    Group group = mock(Group.class);
    lenient().when(group.getId()).thenReturn(groupId);
    when(groupHandler.findGroupById(groupId)).thenThrow(new IllegalStateException("store unavailable")).thenReturn(group);
    lenient().when(organizationalUnitStorage.isManagedOrganizationalUnit(groupId, "john")).thenReturn(true);

    assertFalse(organizationalUnitService.canManageGroupMemberships(groupId, "john"));
  }

  private void mockGroupWithEnclosing(String groupId, String enclosingGroupId) throws Exception {
    Group group = mock(Group.class);
    lenient().when(group.getId()).thenReturn(groupId);
    if (enclosingGroupId != null) {
      when(group.getEnclosingMemberships()).thenReturn(Set.of(new NestedMembership(null, enclosingGroupId, null, groupId)));
    }
    when(groupHandler.findGroupById(groupId)).thenReturn(group);
  }

  @Test
  public void testSetOrganizationalUnitTrueSavesGroupLabelWhenGroupExists() throws Exception {
    Group group = mock(Group.class);
    when(group.getLabel()).thenReturn(LABEL);
    when(groupHandler.findGroupById(GROUP_ID)).thenReturn(group);

    organizationalUnitService.setOrganizationalUnit(GROUP_ID, true);

    verify(organizationalUnitStorage, times(1)).setOrganizationalUnit(GROUP_ID, LABEL, true);
  }

  @Test
  public void testSetOrganizationalUnitTrueThrowsObjectNotFoundExceptionWhenGroupMissing() throws Exception {
    when(groupHandler.findGroupById(GROUP_ID)).thenReturn(null);

    assertThrows(ObjectNotFoundException.class, () -> organizationalUnitService.setOrganizationalUnit(GROUP_ID, true));

    verify(organizationalUnitStorage, never()).setOrganizationalUnit(anyString(), anyString(), anyBoolean());
  }

  @Test
  public void testSetOrganizationalUnitFalseDeletesWithoutLookingUpGroup() throws Exception {
    organizationalUnitService.setOrganizationalUnit(GROUP_ID, false);

    verify(organizationalUnitStorage, times(1)).setOrganizationalUnit(GROUP_ID, null, false);
    verify(groupHandler, never()).findGroupById(anyString());
  }

}
