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

import java.util.Collection;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.core.identity.util.AgentUserUtils;

/**
 * Pins {@link AgentUsersListenerImpl}: a membership in the agents group sets
 * the profile's agent flag, its removal clears it, and a membership in another
 * group leaves the flag alone.
 */
public class AgentUsersListenerImplTest extends AbstractCoreTest {

  private static final String USER_NAME = "raul";

  private OrganizationService organizationService;

  /**
   * Resolves the services and makes sure the agents group exists and the test
   * user holds no membership in it.
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    organizationService = getService(OrganizationService.class);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USER_NAME);
    getOrCreateAgentsGroup(organizationService);
    removeAgentMemberships(organizationService, USER_NAME);
    restartTransaction();
  }

  /**
   * Removes the test user's agent memberships.
   */
  @Override
  public void tearDown() throws Exception {
    removeAgentMemberships(organizationService, USER_NAME);
    restartTransaction();
    super.tearDown();
  }

  /**
   * A membership in the agents group flags the profile as an agent's, and its
   * removal flags it back as a non-agent's.
   */
  public void testAgentMembershipSetsAndClearsTheAgentFlag() throws Exception {
    assertFalse(getIdentity().isAgent());

    linkAgentMembership(organizationService, USER_NAME);
    restartTransaction();

    Identity identity = getIdentity();
    assertEquals("true", identity.getProfile().getProperty(Profile.AGENT));
    assertTrue(identity.isAgent());

    removeAgentMemberships(organizationService, USER_NAME);
    restartTransaction();

    identity = getIdentity();
    assertEquals("false", identity.getProfile().getProperty(Profile.AGENT));
    assertFalse(identity.isAgent());
  }

  /**
   * A membership in another group does not touch the agent flag.
   */
  public void testOtherGroupMembershipLeavesTheAgentFlagUnchanged() throws Exception {
    Object agentFlagBefore = getIdentity().getProfile().getProperty(Profile.AGENT);
    Group usersGroup = organizationService.getGroupHandler().findGroupById("/platform/users");
    Collection<Membership> before = organizationService.getMembershipHandler()
                                                       .findMembershipsByUserAndGroup(USER_NAME, usersGroup.getId());
    for (Membership membership : before) {
      organizationService.getMembershipHandler().removeMembership(membership.getId(), true);
    }
    organizationService.getMembershipHandler()
                       .linkMembership(organizationService.getUserHandler().findUserByName(USER_NAME),
                                       usersGroup,
                                       organizationService.getMembershipTypeHandler().findMembershipType("member"),
                                       true);
    restartTransaction();

    assertEquals(agentFlagBefore, getIdentity().getProfile().getProperty(Profile.AGENT));
    assertFalse(getIdentity().isAgent());
  }

  /**
   * Creates {@link AgentUserUtils#PLATFORM_AGENTS_GROUP} when it is missing.
   *
   * @param organizationService the organization service
   * @return the agents group
   */
  public static Group getOrCreateAgentsGroup(OrganizationService organizationService) throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.findGroupById(AgentUserUtils.PLATFORM_AGENTS_GROUP);
    if (group == null) {
      group = groupHandler.createGroupInstance();
      group.setGroupName("agents");
      group.setLabel("Agents");
      groupHandler.addChild(groupHandler.findGroupById("/platform"), group, true);
      group = groupHandler.findGroupById(AgentUserUtils.PLATFORM_AGENTS_GROUP);
    }
    return group;
  }

  /**
   * Makes the user a member of the agents group.
   *
   * @param organizationService the organization service
   * @param userName the user to add
   */
  public static void linkAgentMembership(OrganizationService organizationService, String userName) throws Exception {
    organizationService.getMembershipHandler()
                       .linkMembership(organizationService.getUserHandler().findUserByName(userName),
                                       getOrCreateAgentsGroup(organizationService),
                                       organizationService.getMembershipTypeHandler().findMembershipType("member"),
                                       true);
  }

  /**
   * Removes every membership the user holds in the agents group.
   *
   * @param organizationService the organization service
   * @param userName the user to remove
   */
  public static void removeAgentMemberships(OrganizationService organizationService, String userName) throws Exception {
    Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                            .findMembershipsByUserAndGroup(userName,
                                                                                           AgentUserUtils.PLATFORM_AGENTS_GROUP);
    for (Membership membership : memberships) {
      organizationService.getMembershipHandler().removeMembership(membership.getId(), true);
    }
  }

  /**
   * Reads the test user's identity, profile included.
   *
   * @return the identity
   */
  private Identity getIdentity() {
    return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USER_NAME);
  }

}
