/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
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
    space.setDisplayName(name);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space ");
    space.setVisibility(Space.PUBLIC);
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
