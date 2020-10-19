/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link SpaceUtilsWildCardMembership}
 *
 * @since 4.2-M1
 */
public class SpaceUtilsWildCardMembershipTest extends AbstractCoreTest {

  private IdentityStorage identityStorage;

  private SpaceStorage    spaceStorage;

  private List<Identity>  tearDownIdentityList;

  private List<Space>     tearDownSpaceList;

  private List<User>      tearDownUserList;

  private UserHandler     userHandler;

  public void setUp() throws Exception {
    super.setUp();
    assertNotNull("identityStorage must not be null", identityStorage);
    userHandler = SpaceUtils.getOrganizationService().getUserHandler();
    tearDownIdentityList = new ArrayList<Identity>();
    tearDownSpaceList = new ArrayList<Space>();
    tearDownUserList = new ArrayList<User>();
  }

  public void tearDown() throws Exception {
    for (Identity identity : tearDownIdentityList) {
      identityStorage.deleteIdentity(identity);
    }
    for (Space space : tearDownSpaceList) {
      spaceStorage.deleteSpace(space.getId());
    }
    for (User user : tearDownUserList) {
      userHandler.removeUser(user.getUserName(), false);
    }
    super.tearDown();
  }

  /**
   * Test
   * {@link SpaceUtils#isUserHasMembershipTypeInGroup(String, String, String)}
   * 
   * @throws Exception
   */
  public void testIsUserHasMembershipTypesInGroup() throws Exception {
    populateUser("user01");
    populateUser("user02");
    populateUser("user03");

    populateIdentity("user01");
    populateIdentity("user02");
    populateIdentity("user03");

    Space space = populateSpace("space01", "user03");

    assertFalse(SpaceUtils.isUserHasMembershipTypesInGroup("user01",
                                                           space.getGroupId(),
                                                           MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));

    addUserToGroupWithMembership("user01", space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);

    // with spaces member and manager membership are not included in case of
    // wild-card membership
    assertFalse(SpaceUtils.isUserHasMembershipTypesInGroup("user01", space.getGroupId(), "member"));

    assertTrue(SpaceUtils.isUserHasMembershipTypesInGroup("user01",
                                                          space.getGroupId(),
                                                          MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));
  }

  /**
   * Test {@link SpaceUtils#findMembershipUsersByGroupAndType(String, String)}
   * 
   * @throws Exception
   */
  public void testFindMembershipUsersByGroupAndTypes() throws Exception {
    populateUser("user01");
    populateUser("user02");
    populateUser("user03");

    populateIdentity("user01");
    populateIdentity("user02");
    populateIdentity("user03");

    Space space = populateSpace("space02", "user03");

    assertEquals(0,
                 SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE)
                           .size());

    addUserToGroupWithMembership("user01", space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);

    // with spaces member and manager membership are not included in case of
    // wild-card membership
    assertEquals(1,
                 SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE)
                           .size());

    addUserToGroupWithMembership("user02", space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);

    // with spaces member and manager membership are not included in case of
    // wild-card membership
    assertEquals(2,
                 SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE)
                           .size());

    assertEquals(3,
                 SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(),
                                                               new String[] { MembershipTypeHandler.ANY_MEMBERSHIP_TYPE,
                                                                   "member" })
                           .size());
    assertEquals(3,
                 SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(),
                                                               new String[] { MembershipTypeHandler.ANY_MEMBERSHIP_TYPE,
                                                                   "manager" })
                           .size());
    assertEquals(3,
                 SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(),
                                                               new String[] { MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, "member",
                                                                   "manager" })
                           .size());

    // disable user02
    disableUser("user02");
    assertEquals(1,
                 SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(),
                                                               new String[] { MembershipTypeHandler.ANY_MEMBERSHIP_TYPE })
                           .size());
  }

  private void disableUser(String userName) {
    try {
      User user = userHandler.findUserByName(userName);
      user = userHandler.setEnabled(user.getUserName(), false, false);
      userHandler.saveUser(user, false);
    } catch (Exception e) {
    }
  }

  private User populateUser(String name) {
    User user = userHandler.createUserInstance(name);

    try {
      userHandler.createUser(user, false);
    } catch (Exception e) {
      return null;
    }

    tearDownUserList.add(user);

    return user;
  }

  private Space populateSpace(String name, String creator) throws Exception {
    Space space = new Space();
    space.setApp("app");
    space.setDisplayName(name);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space ");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId(SpaceUtils.createGroup(space.getPrettyName(), creator));
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {};
    String[] members = new String[] { "user03" };
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);

    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);

    return space;
  }

  private void populateIdentity(String remoteId) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName" + remoteId);
    profile.setProperty(Profile.LAST_NAME, "LastName" + remoteId);
    profile.setProperty(Profile.FULL_NAME, "FirstName" + remoteId + " " + "LastName" + remoteId);
    profile.setProperty("position", "developer");
    profile.setProperty("gender", "male");
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
    identityStorage.saveProfile(profile);
  }
}
