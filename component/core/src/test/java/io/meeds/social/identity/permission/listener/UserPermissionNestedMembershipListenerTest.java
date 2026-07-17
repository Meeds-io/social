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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import io.meeds.social.identity.permission.service.UserPermissionService;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionNestedMembershipListenerTest {

  private static final String                    NESTED_GROUP_ID = "/platform/nested";

  @Mock
  private OrganizationService                    organizationService;

  @Mock
  private GroupHandler                           groupHandler;

  @Mock
  private MembershipHandler                      membershipHandler;

  @Mock
  private IdentityManager                        identityManager;

  @Mock
  private UserPermissionService                  userPermissionService;

  @Mock
  private IndexingService                        indexingService;

  @Mock
  private IdentityStorage                        identityStorage;

  @InjectMocks
  private UserPermissionNestedMembershipListener listener;

  private NestedMembership newNestedMembership() {
    return NestedMembership.builder().nestedGroupId(NESTED_GROUP_ID).groupId("/platform/parent").build();
  }

  @SuppressWarnings("unchecked")
  private void mockNestedGroupWithMembers(Membership... memberships) throws Exception {
    Group nestedGroup = mock(Group.class);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    when(groupHandler.findGroupById(NESTED_GROUP_ID)).thenReturn(nestedGroup);
    ListAccess<Membership> membershipsListAccess = mock(ListAccess.class);
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    when(membershipHandler.findAllMembershipsByGroup(nestedGroup)).thenReturn(membershipsListAccess);
    when(membershipsListAccess.getSize()).thenReturn(memberships.length);
    when(membershipsListAccess.load(0, memberships.length)).thenReturn(memberships);
  }

  @Test
  public void testOnEventRecomputesForEachMemberAndInvalidatesCacheOnce() throws Exception {
    MembershipImpl membership = new MembershipImpl();
    membership.setUserName("alice");
    mockNestedGroupWithMembers(membership);

    Identity aliceIdentity = new Identity("101");
    when(identityManager.getOrCreateUserIdentity("alice")).thenReturn(aliceIdentity);
    when(membershipHandler.findMembershipsByUser("alice", true)).thenReturn(List.of());

    listener.onEvent(new Event<>(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT,
                                 newNestedMembership(),
                                 newNestedMembership()));

    verify(userPermissionService, times(1)).recomputeInheritedMemberships(101L, "alice", List.of());
    verify(indexingService, times(1)).reindex(UserPermissionService.INDEX_CONNECTOR_NAME, "alice");
    verify(indexingService, times(1)).reindex(ProfileIndexingServiceConnector.TYPE, "101");
    verify(identityStorage, times(1)).updateIdentityMembership(null);
  }

  @Test
  public void testOnEventReturnsEarlyWhenNestedGroupNotFound() throws Exception {
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    when(groupHandler.findGroupById(NESTED_GROUP_ID)).thenReturn(null);

    listener.onEvent(new Event<>(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT,
                                 newNestedMembership(),
                                 newNestedMembership()));

    verify(userPermissionService, never()).recomputeInheritedMemberships(anyLong(), any(), any());
    verify(identityStorage, never()).updateIdentityMembership(any());
  }

  @Test
  public void testOnEventSkipsUserWhenIdentityCannotBeResolved() throws Exception {
    MembershipImpl membership = new MembershipImpl();
    membership.setUserName("ghost");
    mockNestedGroupWithMembers(membership);

    when(identityManager.getOrCreateUserIdentity("ghost")).thenReturn(null);

    listener.onEvent(new Event<>(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT,
                                 newNestedMembership(),
                                 newNestedMembership()));

    verify(userPermissionService, never()).recomputeInheritedMemberships(anyLong(), any(), any());
    verify(indexingService, never()).reindex(any(), any());
  }

  @Test
  public void testOnEventDoesNotInvalidateCacheWhenGroupHasNoMembers() throws Exception {
    mockNestedGroupWithMembers();

    listener.onEvent(new Event<>(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT,
                                 newNestedMembership(),
                                 newNestedMembership()));

    verify(identityStorage, never()).updateIdentityMembership(any());
  }

}
