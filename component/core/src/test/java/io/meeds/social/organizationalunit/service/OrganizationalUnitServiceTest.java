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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
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
