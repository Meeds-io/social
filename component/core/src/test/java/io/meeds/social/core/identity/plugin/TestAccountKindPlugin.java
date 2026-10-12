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
package io.meeds.social.core.identity.plugin;

import java.util.Collection;
import java.util.Set;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Profile;

import io.meeds.social.core.identity.service.AccountKindService;

/**
 * The account kind the container tests register: the members of
 * {@link #GROUP_ID} are {@link #KIND} accounts, whose manager only an
 * administrator changes. One class for every test, so that registering it
 * again in the shared test container replaces it.
 */
public class TestAccountKindPlugin implements AccountKindPlugin {

  /** The test account kind. */
  public static final String KIND     = "bot";

  /** The group whose members are {@link #KIND} accounts. */
  public static final String GROUP_ID = "/platform/bots";

  /**
   * Registers this kind in a registry.
   *
   * @param accountKindService the registry
   */
  public static void register(AccountKindService accountKindService) {
    accountKindService.register(new TestAccountKindPlugin());
  }

  /**
   * Creates {@link #GROUP_ID} when it is missing.
   *
   * @param organizationService the organization service
   * @return the group
   * @throws Exception when the group cannot be read or created
   */
  public static Group getOrCreateGroup(OrganizationService organizationService) throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.findGroupById(GROUP_ID);
    if (group == null) {
      group = groupHandler.createGroupInstance();
      group.setGroupName("bots");
      group.setLabel("Bots");
      groupHandler.addChild(groupHandler.findGroupById("/platform"), group, true);
      group = groupHandler.findGroupById(GROUP_ID);
    }
    return group;
  }

  /**
   * Makes a user a member of {@link #GROUP_ID}, with a membership type.
   *
   * @param organizationService the organization service
   * @param userName the user to add
   * @param membershipType the membership type, such as {@code member}
   * @throws Exception when the membership cannot be created
   */
  public static void linkMembership(OrganizationService organizationService,
                                    String userName,
                                    String membershipType) throws Exception {
    organizationService.getMembershipHandler()
                       .linkMembership(organizationService.getUserHandler().findUserByName(userName),
                                       getOrCreateGroup(organizationService),
                                       organizationService.getMembershipTypeHandler().findMembershipType(membershipType),
                                       true);
  }

  /**
   * Removes every membership a user holds in {@link #GROUP_ID}.
   *
   * @param organizationService the organization service
   * @param userName the user to remove
   * @throws Exception when the memberships cannot be read or removed
   */
  public static void removeMemberships(OrganizationService organizationService, String userName) throws Exception {
    getOrCreateGroup(organizationService);
    Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                            .findMembershipsByUserAndGroup(userName, GROUP_ID);
    for (Membership membership : memberships) {
      organizationService.getMembershipHandler().removeMembership(membership.getId(), true);
    }
  }

  /**
   * @return {@link #KIND}
   */
  @Override
  public String getKind() {
    return KIND;
  }

  /**
   * @return {@link #GROUP_ID}
   */
  @Override
  public String getGroupId() {
    return GROUP_ID;
  }

  /**
   * @return the manager
   */
  @Override
  public Set<String> getAdminOnlyProperties() {
    return Set.of(Profile.MANAGER);
  }

}
