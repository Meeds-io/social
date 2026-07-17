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
package io.meeds.social.identity.permission.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.impl.MembershipImpl;

import io.meeds.social.identity.permission.model.UserPermission;
import io.meeds.social.identity.permission.storage.UserPermissionStorage;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionServiceTest {

  private static final String   USER_NAME   = "john";

  private static final long     IDENTITY_ID = 42L;

  @Mock
  private UserPermissionStorage userPermissionStorage;

  @InjectMocks
  private UserPermissionService userPermissionService;

  @Test
  public void testSaveDirectMembership() {
    userPermissionService.saveDirectMembership(IDENTITY_ID, USER_NAME, "/platform/administrators", "member");

    ArgumentCaptor<UserPermission> captor = ArgumentCaptor.forClass(UserPermission.class);
    verify(userPermissionStorage, times(1)).saveMembership(captor.capture());
    UserPermission saved = captor.getValue();
    assertEquals(IDENTITY_ID, saved.getIdentityId());
    assertEquals(USER_NAME, saved.getUserName());
    assertEquals("/platform/administrators", saved.getGroupId());
    assertEquals("member", saved.getMembershipType());
    assertTrue(!saved.isInherited());
  }

  @Test
  public void testRemoveDirectMembership() {
    userPermissionService.removeDirectMembership(USER_NAME, "/platform/administrators", "member");

    verify(userPermissionStorage, times(1)).deleteMembership(USER_NAME, "/platform/administrators", "member");
  }

  @Test
  public void testRecomputeInheritedMembershipsOnlyPersistsInheritedRows() {
    Membership direct = membership(USER_NAME, "/platform/administrators", "member", false);
    Membership inherited = membership(USER_NAME, "/platform/users", "*", true);

    userPermissionService.recomputeInheritedMemberships(IDENTITY_ID, USER_NAME, List.of(direct, inherited));

    verify(userPermissionStorage, times(1)).deleteInheritedMemberships(USER_NAME);

    ArgumentCaptor<UserPermission> captor = ArgumentCaptor.forClass(UserPermission.class);
    verify(userPermissionStorage, times(1)).saveMembership(captor.capture());
    UserPermission saved = captor.getValue();
    assertEquals("/platform/users", saved.getGroupId());
    assertEquals("*", saved.getMembershipType());
    assertTrue(saved.isInherited());
  }

  @Test
  public void testGetPermissionTokensFormatsDirectRowPlain() {
    when(userPermissionStorage.getPermissions(USER_NAME)).thenReturn(List.of(new UserPermission(1,
                                                                                                IDENTITY_ID,
                                                                                                USER_NAME,
                                                                                                "/platform/test",
                                                                                                "member",
                                                                                                false)));

    List<String> tokens = userPermissionService.getPermissionTokens(USER_NAME);

    assertEquals(1, tokens.size());
    assertEquals("member:/platform/test", tokens.get(0));
  }

  @Test
  public void testGetPermissionTokensPrefixesInheritedRow() {
    when(userPermissionStorage.getPermissions(USER_NAME)).thenReturn(List.of(new UserPermission(1,
                                                                                                IDENTITY_ID,
                                                                                                USER_NAME,
                                                                                                "/platform/admin",
                                                                                                "member",
                                                                                                true)));

    List<String> tokens = userPermissionService.getPermissionTokens(USER_NAME);

    assertEquals(1, tokens.size());
    assertEquals("inherited:member:/platform/admin", tokens.get(0));
  }

  @Test
  public void testDeleteAllForUserAndGroup() {
    userPermissionService.deleteAllForUser(USER_NAME);
    userPermissionService.deleteAllForGroup("/platform/administrators");

    verify(userPermissionStorage, times(1)).deleteByUserName(USER_NAME);
    verify(userPermissionStorage, times(1)).deleteByGroupId("/platform/administrators");
  }

  private Membership membership(String userName, String groupId, String membershipType, boolean inherited) {
    MembershipImpl membership = new MembershipImpl();
    membership.setUserName(userName);
    membership.setGroupId(groupId);
    membership.setMembershipType(membershipType);
    membership.setInherited(inherited);
    return membership;
  }

}
