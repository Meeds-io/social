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
package io.meeds.social.organizationalunit.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.social.organizationalunit.dao.OrganizationalUnitDAO;
import io.meeds.social.organizationalunit.entity.OrganizationalUnitEntity;
import io.meeds.social.organizationalunit.model.OrganizationalUnit;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationalUnitStorageTest {

  private static final String GROUP_ID = "/platform/test";

  private static final String LABEL    = "Test Group";

  @Mock
  private OrganizationalUnitDAO organizationalUnitDAO;

  private OrganizationalUnitStorage storage;

  private OrganizationalUnitEntity newEntity(Long id) {
    OrganizationalUnitEntity entity = new OrganizationalUnitEntity();
    entity.setId(id);
    entity.setGroupId(GROUP_ID);
    entity.setLabel(LABEL);
    return entity;
  }

  @Before
  public void setUp() {
    storage = new OrganizationalUnitStorage(organizationalUnitDAO);
  }

  @Test
  public void testIsOrganizationalUnitDelegatesToDao() {
    when(organizationalUnitDAO.existsByGroupId(GROUP_ID)).thenReturn(true);

    assertTrue(storage.isOrganizationalUnit(GROUP_ID));

    when(organizationalUnitDAO.existsByGroupId(GROUP_ID)).thenReturn(false);

    assertFalse(storage.isOrganizationalUnit(GROUP_ID));
  }

  @Test
  public void testGetManagedOrganizationalUnitsMapsEntitiesToModels() {
    when(organizationalUnitDAO.findManagedByUserName("john")).thenReturn(List.of(newEntity(1L)));

    List<OrganizationalUnit> organizationalUnits = storage.getManagedOrganizationalUnits("john");

    assertEquals(1, organizationalUnits.size());
    assertEquals(GROUP_ID, organizationalUnits.get(0).getGroupId());
    assertEquals(LABEL, organizationalUnits.get(0).getLabel());
  }

  @Test
  public void testSetOrganizationalUnitTrueCreatesNewEntityWhenNoneExists() {
    when(organizationalUnitDAO.findByGroupId(GROUP_ID)).thenReturn(Optional.empty());
    ArgumentCaptor<OrganizationalUnitEntity> savedEntity = ArgumentCaptor.forClass(OrganizationalUnitEntity.class);
    when(organizationalUnitDAO.save(savedEntity.capture())).thenReturn(newEntity(1L));

    storage.setOrganizationalUnit(GROUP_ID, LABEL, true);

    assertEquals(GROUP_ID, savedEntity.getValue().getGroupId());
    assertEquals(LABEL, savedEntity.getValue().getLabel());
  }

  @Test
  public void testSetOrganizationalUnitTrueUpdatesExistingEntityWhenFound() {
    OrganizationalUnitEntity existing = newEntity(1L);
    when(organizationalUnitDAO.findByGroupId(GROUP_ID)).thenReturn(Optional.of(existing));
    when(organizationalUnitDAO.save(existing)).thenReturn(existing);

    storage.setOrganizationalUnit(GROUP_ID, "New Label", true);

    verify(organizationalUnitDAO, times(1)).save(existing);
    assertEquals("New Label", existing.getLabel());
  }

  @Test
  public void testSetOrganizationalUnitFalseDeletesByGroupId() {
    storage.setOrganizationalUnit(GROUP_ID, null, false);

    verify(organizationalUnitDAO, times(1)).deleteByGroupId(GROUP_ID);
  }

  @Test
  public void testUpdateLabelUpdatesWhenFound() {
    OrganizationalUnitEntity existing = newEntity(1L);
    when(organizationalUnitDAO.findByGroupId(GROUP_ID)).thenReturn(Optional.of(existing));

    storage.updateLabel(GROUP_ID, "Renamed Group");

    verify(organizationalUnitDAO, times(1)).save(existing);
    assertEquals("Renamed Group", existing.getLabel());
  }

  @Test
  public void testUpdateLabelDoesNothingWhenNotFound() {
    when(organizationalUnitDAO.findByGroupId(GROUP_ID)).thenReturn(Optional.empty());

    storage.updateLabel(GROUP_ID, "Renamed Group");

    verify(organizationalUnitDAO, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void testDeleteByGroupIdDelegatesToDao() {
    storage.deleteByGroupId(GROUP_ID);

    verify(organizationalUnitDAO, times(1)).deleteByGroupId(GROUP_ID);
  }

}
