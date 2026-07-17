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
package io.meeds.social.identity.permission.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import io.meeds.social.identity.permission.service.UserPermissionService;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionMembershipListenerTest {

  private static final String GROUP_ID = "/platform/test";

  @Mock
  private OrganizationService   organizationService;

  @Mock
  private MembershipHandler      membershipHandler;

  @Mock
  private IdentityManager       identityManager;

  @Mock
  private UserPermissionService userPermissionService;

  @Mock
  private IndexingService       indexingService;

  @Mock
  private IdentityStorage       identityStorage;

  @InjectMocks
  private UserPermissionMembershipListener listener;

  private Membership newMembership(String userName) {
    MembershipImpl membership = new MembershipImpl();
    membership.setUserName(userName);
    membership.setGroupId(GROUP_ID);
    membership.setMembershipType("member");
    return membership;
  }

  @Test
  public void testPostSaveSavesDirectMembershipAndRecomputesAndReindexes() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    Identity aliceIdentity = new Identity("101");
    when(identityManager.getOrCreateUserIdentity("alice")).thenReturn(aliceIdentity);
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(List.of());

    listener.postSave(newMembership("alice"), true);

    verify(userPermissionService).saveDirectMembership(101L, "alice", GROUP_ID, "member");
    verify(userPermissionService, never()).removeDirectMembership(any(), any(), any());
    verify(userPermissionService).recomputeInheritedMemberships(101L, "alice", List.of());
    verify(indexingService).reindex(UserPermissionService.INDEX_CONNECTOR_NAME, "alice");
    verify(indexingService).reindex(ProfileIndexingServiceConnector.TYPE, "101");
    verify(identityStorage).updateIdentityMembership("alice");
  }

  @Test
  public void testPostDeleteRemovesDirectMembershipAndRecomputesAndReindexes() throws Exception {
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    Identity aliceIdentity = new Identity("101");
    when(identityManager.getOrCreateUserIdentity("alice")).thenReturn(aliceIdentity);
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(List.of());

    listener.postDelete(newMembership("alice"));

    verify(userPermissionService).removeDirectMembership("alice", GROUP_ID, "member");
    verify(userPermissionService, never()).saveDirectMembership(anyLong(), any(), any(), any());
    verify(userPermissionService).recomputeInheritedMemberships(101L, "alice", List.of());
    verify(identityStorage).updateIdentityMembership("alice");
  }

  @Test
  public void testSyncMembershipSkipsWhenIdentityCannotBeResolved() throws Exception {
    when(identityManager.getOrCreateUserIdentity("ghost")).thenReturn(null);

    listener.postSave(newMembership("ghost"), true);

    verify(userPermissionService, never()).saveDirectMembership(anyLong(), any(), any(), any());
    verify(userPermissionService, never()).recomputeInheritedMemberships(anyLong(), any(), any());
    verify(identityStorage, never()).updateIdentityMembership(any());
  }

  @Test
  public void testSyncMembershipSwallowsException() throws Exception {
    Identity aliceIdentity = mock(Identity.class);
    when(aliceIdentity.getId()).thenReturn("not-a-number");
    when(identityManager.getOrCreateUserIdentity("alice")).thenReturn(aliceIdentity);

    // Long.parseLong("not-a-number") throws: must not propagate out of postSave().
    listener.postSave(newMembership("alice"), true);

    verify(identityStorage, never()).updateIdentityMembership(any());
  }

}
