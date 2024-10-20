/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpaceAccessTest extends AbstractCoreTest {

  private static final String SPACE_NAME = "request space";

  private IdentityStorage     identityStorage;

  private List<Space>         tearDownSpaceList;

  private List<Identity>      tearDownUserList;

  private Identity            demo;

  private Identity            john;

  private Identity            mary;

  private Identity            root;

  private Identity            jame;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityStorage = getService(IdentityStorage.class);
    tearDownSpaceList = new ArrayList<>();
    tearDownUserList = new ArrayList<>();

    demo = new Identity(OrganizationIdentityProvider.NAME, "demo");
    mary = new Identity(OrganizationIdentityProvider.NAME, "mary");
    john = new Identity(OrganizationIdentityProvider.NAME, "john");
    root = new Identity(OrganizationIdentityProvider.NAME, "root");
    jame = new Identity(OrganizationIdentityProvider.NAME, "jame");

    identityStorage.saveIdentity(demo);
    identityStorage.saveIdentity(mary);
    identityStorage.saveIdentity(john);
    identityStorage.saveIdentity(root);
    identityStorage.saveIdentity(jame);

    tearDownUserList = new ArrayList<>();
    tearDownUserList.add(demo);
    tearDownUserList.add(mary);
    tearDownUserList.add(john);
    tearDownUserList.add(root);
    tearDownUserList.add(jame);

  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();

    for (Identity identity : tearDownUserList) {
      identityStorage.deleteIdentity(identity);
    }
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityStorage.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    super.tearDown();
  }

  public void testSpaceNotFound() {
    Space space = spaceService.getSpaceByPrettyName("space_not_found");
    boolean gotStatus = SpaceAccessType.SPACE_NOT_FOUND.doCheck(root.getRemoteId(), space);
    assertTrue(gotStatus);
  }

  public void testInvitedSpace() {
    Space space = createSpaceData("invited space",
                                  Space.VALIDATION,
                                  new String[] { root.getRemoteId(), john.getRemoteId() },
                                  new String[] { root.getRemoteId(), john.getRemoteId(), mary.getRemoteId() });
    space.setInvitedUsers(new String[] { demo.getRemoteId(), jame.getRemoteId() });
    spaceService.createSpace(space);

    space = spaceService.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull(space);
    boolean gotStatus = SpaceAccessType.INVITED_SPACE.doCheck(demo.getRemoteId(), space);
    assertTrue(gotStatus);

    gotStatus = SpaceAccessType.INVITED_SPACE.doCheck(jame.getRemoteId(), space);
    assertTrue(gotStatus);
    tearDownSpaceList.add(space);
  }

  /**
   * RequesTED to join space.
   * 
   * @throws Exception
   */
  public void testRequestedToJoinSpace() {
    Space space = createSpaceData("requested space",
                                  Space.VALIDATION,
                                  new String[] { root.getRemoteId(), john.getRemoteId() },
                                  new String[] { root.getRemoteId(), john.getRemoteId(), mary.getRemoteId() });
    space.setPendingUsers(new String[] { demo.getRemoteId(), jame.getRemoteId() });
    spaceService.createSpace(space);

    space = spaceService.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull(space);
    boolean gotStatus = SpaceAccessType.REQUESTED_JOIN_SPACE.doCheck(demo.getRemoteId(), space);
    assertTrue(gotStatus);

    gotStatus = SpaceAccessType.REQUESTED_JOIN_SPACE.doCheck(jame.getRemoteId(), space);
    assertTrue(gotStatus);
    tearDownSpaceList.add(space);
  }

  /**
   * Request to join space
   * 
   * @throws Exception
   */
  public void testRequestToJoinSpace() {
    Space space = createSpaceData(SPACE_NAME,
                                  Space.VALIDATION,
                                  new String[] { root.getRemoteId(), john.getRemoteId() },
                                  new String[] { root.getRemoteId(), john.getRemoteId(), mary.getRemoteId() });
    spaceService.createSpace(space);

    space = spaceService.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull(space);
    boolean gotStatus = SpaceAccessType.REQUEST_JOIN_SPACE.doCheck(demo.getRemoteId(), space);
    assertTrue(gotStatus);

    gotStatus = SpaceAccessType.REQUEST_JOIN_SPACE.doCheck(jame.getRemoteId(), space);
    assertTrue(gotStatus);
    tearDownSpaceList.add(space);
  }

  public void testJoinSpace() {
    Space space = createSpaceData(SPACE_NAME,
                                  "open",
                                  new String[] { root.getRemoteId(), john.getRemoteId() },
                                  new String[] { root.getRemoteId(), john.getRemoteId(), mary.getRemoteId() });
    spaceService.createSpace(space);
    space = spaceService.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull(space);
    boolean gotStatus = SpaceAccessType.JOIN_SPACE.doCheck(demo.getRemoteId(), space);
    assertTrue(gotStatus);

    gotStatus = SpaceAccessType.JOIN_SPACE.doCheck(jame.getRemoteId(), space);
    assertTrue(gotStatus);
    tearDownSpaceList.add(space);
  }

  public void testClosedSpace() {
    Space space = createSpaceData(SPACE_NAME,
                                  "close",
                                  new String[] { root.getRemoteId(), john.getRemoteId() },
                                  new String[] { root.getRemoteId(), john.getRemoteId(), mary.getRemoteId() });
    spaceService.createSpace(space);
    space = spaceService.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull(space);
    boolean gotStatus = SpaceAccessType.CLOSED_SPACE.doCheck(demo.getRemoteId(), space);
    assertTrue(gotStatus);

    gotStatus = SpaceAccessType.CLOSED_SPACE.doCheck(jame.getRemoteId(), space);
    assertTrue(gotStatus);
    tearDownSpaceList.add(space);
  }

  public void testHiddenValidationSpace() {
    Space space = createSpaceData(SPACE_NAME,
                                  Space.VALIDATION,
                                  Space.HIDDEN,
                                  new String[] { root.getRemoteId(), john.getRemoteId() },
                                  new String[] { root.getRemoteId(), john.getRemoteId(), mary.getRemoteId() });
    spaceService.createSpace(space);
    space = spaceService.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull(space);
    boolean gotStatus = (!SpaceAccessType.SPACE_NOT_FOUND.doCheck(demo.getRemoteId(), space)
                         && SpaceAccessType.REQUEST_JOIN_SPACE.doCheck(demo.getRemoteId(), space));
    assertTrue(gotStatus);

    gotStatus = (!SpaceAccessType.SPACE_NOT_FOUND.doCheck(jame.getRemoteId(), space)
                 && SpaceAccessType.REQUEST_JOIN_SPACE.doCheck(jame.getRemoteId(), space));
    assertTrue(gotStatus);
    tearDownSpaceList.add(space);
  }

  private Space createSpaceData(String spaceName, String registration, String[] managers, String[] members) {
    Space space2 = new Space();
    space2.setDisplayName(spaceName);
    space2.setPrettyName(space2.getDisplayName());
    String shortName = Utils.cleanString(spaceName);
    space2.setGroupId("/spaces/" + shortName);
    space2.setUrl(shortName);
    space2.setMembers(members);
    space2.setManagers(managers);
    space2.setRegistration(registration);
    space2.setDescription("This is my second space for testing");
    space2.setVisibility("public");
    return space2;
  }

  private Space createSpaceData(String spaceName,
                                String registration,
                                String visibility,
                                String[] managers,
                                String[] members) {
    Space space2 = createSpaceData(spaceName, registration, managers, members);
    space2.setVisibility(visibility);
    return space2;
  }
}
