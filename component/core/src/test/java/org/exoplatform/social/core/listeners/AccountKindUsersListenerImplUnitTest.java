/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.listeners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.services.organization.Membership;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.core.identity.plugin.AccountKindPlugin;
import io.meeds.social.core.identity.service.AccountKindService;

/**
 * Pins the branches of {@link AccountKindUsersListenerImpl} the container
 * test cannot reach: a membership whose memberships cannot be read, and a
 * listener without registry, leave every profile as it is.
 */
public class AccountKindUsersListenerImplUnitTest {

  private static final String       USER_NAME = "bot-1";

  private static final String       GROUP_ID  = "/platform/bots";

  private AccountKindService        accountKindService;

  private IdentityManager           identityManager;

  private AccountKindUsersListenerImpl listener;

  private Membership                membership;

  /**
   * Builds the listener over a mocked registry and identity manager.
   *
   * @throws Exception never
   */
  @Before
  public void setUp() throws Exception {
    accountKindService = mock(AccountKindService.class);
    identityManager = mock(IdentityManager.class);
    listener = new AccountKindUsersListenerImpl();
    setField("accountKindService", accountKindService);
    setField("identityManager", identityManager);
    membership = mock(Membership.class);
    when(membership.getGroupId()).thenReturn(GROUP_ID);
    when(membership.getUserName()).thenReturn(USER_NAME);
    when(accountKindService.getPluginByGroup(GROUP_ID)).thenReturn(mock(AccountKindPlugin.class));
  }

  /**
   * When the memberships cannot be read, neither a save nor a removal reads
   * or writes the profile.
   */
  @Test
  public void testUnreadableMembershipsLeaveTheProfileAlone() {
    when(accountKindService.computeAccountKind(USER_NAME)).thenThrow(new IllegalStateException("down"));
    listener.postSave(membership, true);
    listener.postDelete(membership);
    verifyNoInteractions(identityManager);
  }

  /**
   * A membership in a group no plugin names computes nothing.
   */
  @Test
  public void testOtherGroupComputesNothing() {
    when(membership.getGroupId()).thenReturn("/platform/users");
    listener.postSave(membership, true);
    listener.postDelete(membership);
    verify(accountKindService, never()).computeAccountKind(USER_NAME);
    verifyNoInteractions(identityManager);
  }

  /**
   * Sets a private field of the listener.
   *
   * @param name the field name
   * @param value the value
   * @throws Exception never
   */
  private void setField(String name, Object value) throws Exception {
    Field field = AccountKindUsersListenerImpl.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(listener, value);
  }

}
