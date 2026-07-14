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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.social.identity.permission.service.UserPermissionService;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionGroupListenerTest {

  private static final String         GROUP_ID        = "/platform/test";

  private static final String         NESTED_GROUP_ID = "/platform/nested";

  @Mock
  private OrganizationService         organizationService;

  @Mock
  private ListenerService             listenerService;

  @Mock
  private UserPermissionService       userPermissionService;

  @InjectMocks
  private UserPermissionGroupListener listener;

  @Test
  public void testPostDeleteRemovesAllRowsForGroup() throws Exception {
    Group group = mock(Group.class);
    when(group.getId()).thenReturn(GROUP_ID);

    listener.postDelete(group);

    verify(userPermissionService).deleteAllForGroup(GROUP_ID);
  }

  @Test
  public void testLinkGroupsBroadcastsNestedMembershipChangedEvent() throws Exception {
    NestedMembership nestedMembership = NestedMembership.builder().groupId(GROUP_ID).nestedGroupId(NESTED_GROUP_ID).build();

    listener.linkGroups(nestedMembership);

    verify(listenerService).broadcast(eq(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT),
                                      eq(nestedMembership),
                                      eq(nestedMembership));
  }

  @Test
  public void testUnlinkGroupsBroadcastsNestedMembershipChangedEvent() throws Exception {
    NestedMembership nestedMembership = NestedMembership.builder().groupId(GROUP_ID).nestedGroupId(NESTED_GROUP_ID).build();

    listener.unlinkGroups(nestedMembership);

    verify(listenerService).broadcast(eq(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT),
                                      eq(nestedMembership),
                                      eq(nestedMembership));
  }

  @Test
  public void testLinkGroupsSwallowsBroadcastException() throws Exception {
    NestedMembership nestedMembership = NestedMembership.builder().groupId(GROUP_ID).nestedGroupId(NESTED_GROUP_ID).build();
    doThrow(new RuntimeException("broadcast failure")).when(listenerService)
                                                      .broadcast(eq(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT),
                                                                 eq(nestedMembership),
                                                                 eq(nestedMembership));

    // Must not propagate: a broadcast error must not fail the caller's linkGroups()
    // call.
    listener.linkGroups(nestedMembership);

    verify(userPermissionService, never()).deleteAllForGroup(GROUP_ID);
  }

}
