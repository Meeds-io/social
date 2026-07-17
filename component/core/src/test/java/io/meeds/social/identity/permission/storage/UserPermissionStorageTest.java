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
package io.meeds.social.identity.permission.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.social.identity.permission.dao.UserPermissionDAO;
import io.meeds.social.identity.permission.entity.UserPermissionEntity;
import io.meeds.social.identity.permission.model.UserPermission;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionStorageTest {

  private static final String USER_NAME       = "alice";

  private static final String GROUP_ID        = "/platform/test";

  private static final String MEMBERSHIP_TYPE = "member";

  @Mock
  private UserPermissionDAO userPermissionDAO;

  private UserPermissionStorage storage;

  private UserPermissionEntity newEntity(Long id) {
    UserPermissionEntity entity = new UserPermissionEntity();
    entity.setId(id);
    entity.setIdentityId(101L);
    entity.setUserName(USER_NAME);
    entity.setGroupId(GROUP_ID);
    entity.setMembershipType(MEMBERSHIP_TYPE);
    entity.setInherited(false);
    return entity;
  }

  @org.junit.Before
  public void setUp() {
    storage = new UserPermissionStorage(userPermissionDAO);
  }

  @Test
  public void testGetPermissionsMapsEntitiesToModels() {
    when(userPermissionDAO.findByUserName(USER_NAME)).thenReturn(List.of(newEntity(1L)));

    List<UserPermission> permissions = storage.getPermissions(USER_NAME);

    assertEquals(1, permissions.size());
    assertEquals(USER_NAME, permissions.get(0).getUserName());
    assertEquals(GROUP_ID, permissions.get(0).getGroupId());
    assertEquals(MEMBERSHIP_TYPE, permissions.get(0).getMembershipType());
    assertFalse(permissions.get(0).isInherited());
  }

  @Test
  public void testSaveMembershipCreatesNewEntityWhenNoneExists() {
    when(userPermissionDAO.findByUserNameAndGroupIdAndMembershipType(USER_NAME, GROUP_ID, MEMBERSHIP_TYPE))
                                                                                                            .thenReturn(Optional.empty());
    ArgumentCaptor<UserPermissionEntity> savedEntity = ArgumentCaptor.forClass(UserPermissionEntity.class);
    when(userPermissionDAO.save(savedEntity.capture())).thenReturn(newEntity(1L));

    UserPermission result = storage.saveMembership(new UserPermission(0, 101L, USER_NAME, GROUP_ID, MEMBERSHIP_TYPE, true));

    assertEquals(101L, savedEntity.getValue().getIdentityId().longValue());
    assertEquals(USER_NAME, savedEntity.getValue().getUserName());
    assertEquals(GROUP_ID, savedEntity.getValue().getGroupId());
    assertEquals(MEMBERSHIP_TYPE, savedEntity.getValue().getMembershipType());
    assertTrue(savedEntity.getValue().isInherited());
    assertEquals(USER_NAME, result.getUserName());
  }

  @Test
  public void testSaveMembershipUpdatesExistingEntityWhenFound() {
    UserPermissionEntity existing = newEntity(1L);
    when(userPermissionDAO.findByUserNameAndGroupIdAndMembershipType(USER_NAME, GROUP_ID, MEMBERSHIP_TYPE))
                                                                                                            .thenReturn(Optional.of(existing));
    when(userPermissionDAO.save(existing)).thenReturn(existing);

    storage.saveMembership(new UserPermission(0, 101L, USER_NAME, GROUP_ID, MEMBERSHIP_TYPE, true));

    verify(userPermissionDAO, times(1)).save(existing);
    assertTrue(existing.isInherited());
  }

  @Test
  public void testDeleteMembershipDeletesWhenFound() {
    UserPermissionEntity existing = newEntity(1L);
    when(userPermissionDAO.findByUserNameAndGroupIdAndMembershipType(USER_NAME, GROUP_ID, MEMBERSHIP_TYPE))
                                                                                                            .thenReturn(Optional.of(existing));

    storage.deleteMembership(USER_NAME, GROUP_ID, MEMBERSHIP_TYPE);

    verify(userPermissionDAO, times(1)).delete(existing);
  }

  @Test
  public void testDeleteMembershipDoesNothingWhenNotFound() {
    when(userPermissionDAO.findByUserNameAndGroupIdAndMembershipType(USER_NAME, GROUP_ID, MEMBERSHIP_TYPE))
                                                                                                            .thenReturn(Optional.empty());

    storage.deleteMembership(USER_NAME, GROUP_ID, MEMBERSHIP_TYPE);

    verify(userPermissionDAO, never()).delete(any());
  }

  @Test
  public void testDeleteInheritedMembershipsDelegatesToDao() {
    storage.deleteInheritedMemberships(USER_NAME);

    verify(userPermissionDAO, times(1)).deleteByUserNameAndInheritedTrue(USER_NAME);
  }

  @Test
  public void testDeleteByUserNameDelegatesToDao() {
    storage.deleteByUserName(USER_NAME);

    verify(userPermissionDAO, times(1)).deleteByUserName(USER_NAME);
  }

  @Test
  public void testDeleteByGroupIdDelegatesToDao() {
    storage.deleteByGroupId(GROUP_ID);

    verify(userPermissionDAO, times(1)).deleteByGroupId(GROUP_ID);
  }

}
