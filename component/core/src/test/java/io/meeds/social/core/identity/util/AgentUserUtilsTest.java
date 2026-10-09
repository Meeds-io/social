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
package io.meeds.social.core.identity.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

import io.meeds.social.core.identity.plugin.AgentAccountLoginGuard;

/**
 * Pins the membership reads of {@link AgentUserUtils} and
 * {@link AgentAccountLoginGuard}, their fail-closed branches included.
 */
public class AgentUserUtilsTest {

  private static final String USER_NAME = "agent-tasks";

  private OrganizationService organizationService;

  private MembershipHandler   membershipHandler;

  /**
   * Mocks the organization service.
   */
  @Before
  public void setUp() {
    organizationService = mock(OrganizationService.class);
    membershipHandler = mock(MembershipHandler.class);
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
  }

  /**
   * A user is an agent account only with a membership in the agents group, and
   * a blank user name is never one.
   */
  @Test
  public void testIsAgentUserReadsTheAgentsGroupMemberships() throws Exception {
    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP))
      .thenReturn(List.of(mock(Membership.class)));
    assertTrue(AgentUserUtils.isAgentUser(organizationService, USER_NAME));

    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP)).thenReturn(List.of());
    assertFalse(AgentUserUtils.isAgentUser(organizationService, USER_NAME));

    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP)).thenReturn(null);
    assertFalse(AgentUserUtils.isAgentUser(organizationService, USER_NAME));

    assertFalse(AgentUserUtils.isAgentUser(organizationService, " "));
  }

  /**
   * A manager change on a profile whose memberships cannot be read is refused
   * to a non-administrator, and an administrator's change never reads them.
   */
  @Test
  public void testCheckManagerUpdateFailsClosedWhenMembershipsCannotBeRead() throws Exception {
    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP))
      .thenThrow(new IllegalStateException("IDM unavailable"));
    Profile profile = profile();

    assertThrows(IllegalAccessException.class, () -> AgentUserUtils.checkManagerUpdate(organizationService, profile, false));

    AgentUserUtils.checkManagerUpdate(organizationService, profile, true);
    verify(membershipHandler, times(1)).findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP);
  }

  /**
   * The login guard refuses an agent account, lets another user through, and
   * refuses the login when the memberships cannot be read.
   */
  @Test
  public void testLoginGuardRefusesAgentsAndFailsClosed() throws Exception {
    AgentAccountLoginGuard guard = new AgentAccountLoginGuard(organizationService);
    User user = mock(User.class);
    when(user.getUserName()).thenReturn(USER_NAME);

    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP)).thenReturn(List.of());
    guard.doCheck(user);
    guard.doCheck(null);

    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP))
      .thenReturn(List.of(mock(Membership.class)));
    assertThrows(DisabledUserException.class, () -> guard.doCheck(user));

    when(membershipHandler.findMembershipsByUserAndGroup(USER_NAME, AgentUserUtils.PLATFORM_AGENTS_GROUP))
      .thenThrow(new IllegalStateException("IDM unavailable"));
    assertThrows(DisabledUserException.class, () -> guard.doCheck(user));
  }

  /**
   * Builds a profile of {@link #USER_NAME}.
   *
   * @return the profile
   */
  private Profile profile() {
    Identity identity = mock(Identity.class);
    when(identity.getRemoteId()).thenReturn(USER_NAME);
    Profile profile = mock(Profile.class);
    when(profile.getIdentity()).thenReturn(identity);
    return profile;
  }

}
