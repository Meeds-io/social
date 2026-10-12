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
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

import io.meeds.social.core.identity.plugin.TestAccountKindPlugin;
import io.meeds.social.core.identity.service.AccountKindService;

/**
 * Pins {@link AccountKindUsersListenerImpl}: a membership in the group of a
 * registered account kind writes the kind on the profile, the removal of the
 * last one clears it, and a membership in another group leaves the profile
 * alone.
 */
public class AccountKindUsersListenerImplTest extends AbstractCoreTest {

  private static final String USER_NAME = "raul";

  private OrganizationService organizationService;

  /**
   * Registers the test account kind, and makes sure its group exists and the
   * test user holds no membership in it and no kind.
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    organizationService = getService(OrganizationService.class);
    TestAccountKindPlugin.register(getService(AccountKindService.class));
    TestAccountKindPlugin.registerOther(getService(AccountKindService.class));
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, USER_NAME);
    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME);
    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME, TestAccountKindPlugin.OTHER_GROUP_ID);
    restartTransaction();
    clearAccountKind();
  }

  /**
   * Removes the test user's memberships in the test kind's group, and its
   * kind.
   */
  @Override
  public void tearDown() throws Exception {
    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME);
    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME, TestAccountKindPlugin.OTHER_GROUP_ID);
    restartTransaction();
    clearAccountKind();
    super.tearDown();
  }

  /**
   * A membership in the kind's group writes the kind on the profile, and its
   * removal clears it, as read from a fresh load of the identity.
   */
  public void testMembershipWritesAndClearsTheAccountKind() throws Exception {
    assertNull(getIdentity().getAccountKind());

    TestAccountKindPlugin.linkMembership(organizationService, USER_NAME, "member");
    restartTransaction();
    assertEquals(TestAccountKindPlugin.KIND, getIdentity().getAccountKind());

    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME);
    restartTransaction();
    assertNull(getIdentity().getAccountKind());
  }

  /**
   * Any membership type in the kind's group counts: removing one of two
   * memberships keeps the kind, removing the last one clears it.
   */
  public void testRemovingOneOfTwoMembershipsKeepsTheAccountKind() throws Exception {
    TestAccountKindPlugin.linkMembership(organizationService, USER_NAME, "member");
    TestAccountKindPlugin.linkMembership(organizationService, USER_NAME, "manager");
    restartTransaction();
    assertEquals(TestAccountKindPlugin.KIND, getIdentity().getAccountKind());
    Membership manager = organizationService.getMembershipHandler()
                                            .findMembershipByUserGroupAndType(USER_NAME,
                                                                              TestAccountKindPlugin.GROUP_ID,
                                                                              "manager");
    organizationService.getMembershipHandler().removeMembership(manager.getId(), true);
    restartTransaction();
    assertEquals(TestAccountKindPlugin.KIND, getIdentity().getAccountKind());
    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME);
    restartTransaction();
    assertNull(getIdentity().getAccountKind());
  }

  /**
   * The removal of the last membership leaves a profile carrying a kind no
   * plugin declares as it is.
   */
  public void testRemovalLeavesAnotherKindAlone() throws Exception {
    TestAccountKindPlugin.linkMembership(organizationService, USER_NAME, "member");
    restartTransaction();
    Profile profile = getIdentity().getProfile();
    profile.setProperty(Profile.ACCOUNT_KIND, "other");
    identityManager.updateProfile(profile);
    restartTransaction();

    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME);
    restartTransaction();
    assertEquals("other", getIdentity().getAccountKind());
  }

  /**
   * A user in two kinds' groups gets the first kind in alphabetical order,
   * whatever the order of its memberships, and keeps the other kind when the
   * first kind's membership goes.
   */
  public void testTwoKindsGiveTheFirstOneAndTheRemovalGivesTheOther() throws Exception {
    TestAccountKindPlugin.linkMembership(organizationService, USER_NAME, "member", TestAccountKindPlugin.OTHER_GROUP_ID);
    restartTransaction();
    assertEquals(TestAccountKindPlugin.OTHER_KIND, getIdentity().getAccountKind());

    TestAccountKindPlugin.linkMembership(organizationService, USER_NAME, "member");
    restartTransaction();
    assertEquals(TestAccountKindPlugin.KIND, getIdentity().getAccountKind());

    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME);
    restartTransaction();
    assertEquals(TestAccountKindPlugin.OTHER_KIND, getIdentity().getAccountKind());

    TestAccountKindPlugin.linkMembership(organizationService, USER_NAME, "member");
    restartTransaction();
    TestAccountKindPlugin.removeMemberships(organizationService, USER_NAME, TestAccountKindPlugin.OTHER_GROUP_ID);
    restartTransaction();
    assertEquals(TestAccountKindPlugin.KIND, getIdentity().getAccountKind());
  }

  /**
   * A membership in a group no plugin names does not touch the kind.
   */
  public void testOtherGroupMembershipLeavesTheAccountKindUnchanged() throws Exception {
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

    assertNull(getIdentity().getAccountKind());
  }

  /**
   * Removes the account kind from the test user's profile.
   */
  private void clearAccountKind() {
    Profile profile = getIdentity().getProfile();
    if (profile.getProperty(Profile.ACCOUNT_KIND) != null) {
      profile.removeProperty(Profile.ACCOUNT_KIND);
      identityManager.updateProfile(profile);
      restartTransaction();
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
