/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;

public abstract class SpaceStorageTest extends AbstractCoreTest {

  private SpaceStorage              spaceStorage;

  private IdentityStorage           identityStorage;

  private SocialStorageCacheService cacheService;

  private Identity                  rootIdentity;

  private Identity                  johnIdentity;

  private Identity                  maryIdentity;

  private Identity                  demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    identityStorage = getService(IdentityStorage.class);
    cacheService = getService(SocialStorageCacheService.class);

    rootIdentity = createIdentity("root");
    johnIdentity = createIdentity("john");
    maryIdentity = createIdentity("mary");
    demoIdentity = createIdentity("demo");
  }

  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  protected Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setDisplayName("my space test " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PUBLIC);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] { "demo", "tom" };
    String[] members = new String[] { "demo", "tom", "raul", "ghost", "dragon" };
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    return space;
  }

  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  protected Space getSpaceInstance(int number, String visible, String registration, String manager, String... members) {
    Space space = new Space();
    space.setDisplayName("my space test " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(registration);
    space.setDescription("add new space " + number);
    space.setVisibility(visible);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] { manager };
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members == null ? new String[] { manager } : ArrayUtils.add(members, manager));
    space.setUrl(space.getPrettyName());
    return space;
  }

  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  private Space getSpaceInstanceInvitedMember(int number,
                                              String visible,
                                              String registration,
                                              String[] invitedMember,
                                              String manager,
                                              String... members) {
    Space space = new Space();
    space.setDisplayName("my space test " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(registration);
    space.setDescription("add new space " + number);
    space.setVisibility(visible);
    space.setGroupId("/spaces/space" + number);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] { manager };
    // String[] invitedUsers = new String[] {invitedMember};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedMember);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members == null ? new String[] { manager } : ArrayUtils.add(members, manager));
    return space;
  }

  private List<Space> getSpaceWithRoot(SpaceFilter filter) {
    if (filter == null) {
      return spaceStorage.getAllSpaces();
    } else {
      return spaceStorage.getSpacesByFilter(filter, 0, 200);
    }
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAllSpaces()}
   *
   * @throws Exception
   */
  public void testGetAllSpaces() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      persist();
    }
    assertEquals("spaceStorage.getAllSpaces().size() must return: " + totalSpaces,
                 totalSpaces,
                 spaceStorage.getAllSpaces().size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaces(long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpaces() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      persist();
    }
    int offset = 0;
    int limit = 10;
    List<Space> spaceListAccess = spaceStorage.getSpaces(offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must be: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpaces(offset, 5);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must be: " + 5, 5, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpaces(offset, 20);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must be: " + totalSpaces, totalSpaces, spaceListAccess.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAllSpacesCount()}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesCount() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      persist();
    }
    int spacesCount = spaceStorage.getAllSpacesCount();
    assertEquals("spacesCount must be: ", totalSpaces, spacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getSpacesByFilter(org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpacesByFilter() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      persist();
    }
    List<Space> foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("add"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("my"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("my space test"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("hello"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + 0, 0, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter(), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAllSpacesByFilterCount(org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesByFilterCount() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      persist();
    }
    int count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("add"));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("my"));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("my space test"));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("hello"));
    assertEquals("count must be: " + 0, 0, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter());
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter());
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpaces() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> accessibleSpaces = spaceStorage.getAccessibleSpaces("demo");
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesByFilter() throws Exception {
    int countSpace = 20;
    Space[] listSpace = new Space[countSpace];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo",
                                                                                    new SpaceFilter("my space test"),
                                                                                    0,
                                                                                    10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("tom", new SpaceFilter("my space test"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("ghost", new SpaceFilter("my space test"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter("add new"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter(), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter(), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("newperson", new SpaceFilter("my space test"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 0, accessibleSpacesByFilter.size());
  }

  public void testGetAccessibleSpacesByFilterApp() {
    Space space = this.getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);
    persist();

    SpaceFilter filter = new SpaceFilter("my space test");

    List<Space> accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", filter, 0, 10);
    assertEquals(1, accessibleSpacesByFilter.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesByFilterCount() throws Exception {
    int countSpace = 20;
    Space[] listSpace = new Space[countSpace];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    int accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("demo", new SpaceFilter("my space test"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("tom", new SpaceFilter("my space test"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("tom", new SpaceFilter());
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("tom", new SpaceFilter());
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("ghost", new SpaceFilter("my space test"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("demo", new SpaceFilter("add new"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("newperson", new SpaceFilter("my space test"));
    assertEquals("accessibleSpacesByFilterCount must be: ", 0, accessibleSpacesByFilterCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesCount(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testgetAccessibleSpacesCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    int accessibleSpacesCount = spaceStorage.getAccessibleSpacesCount("demo");
    assertEquals("accessibleSpacesCount mus be: " + countSpace, countSpace, accessibleSpacesCount);

    accessibleSpacesCount = spaceStorage.getAccessibleSpacesCount("dragon");
    assertEquals("accessibleSpacesCount must be: " + countSpace, countSpace, accessibleSpacesCount);

    accessibleSpacesCount = spaceStorage.getAccessibleSpacesCount("nobody");
    assertEquals("accessibleSpacesCount must be: 0", 0, accessibleSpacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesWithOffset() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> accessibleSpaces = spaceStorage.getAccessibleSpaces("demo", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 5, 5, accessibleSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpaces() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> editableSpaces = spaceStorage.getEditableSpaces("demo");
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("top");
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("dragon");
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("add new"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("m"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("M"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter(), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter(), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("add new"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("top", new SpaceFilter("my space test"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("dragon", new SpaceFilter("m"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    int editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter("add new"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter("m"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter("M"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("tom", new SpaceFilter("add new"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("top", new SpaceFilter("my space test"));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("dragon", new SpaceFilter("m"));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpacesWithListAccess() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> editableSpaces = spaceStorage.getEditableSpaces("demo", 0, countSpace);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("top", 0, countSpace);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("dragon", 0, 5);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpaces() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpaces("register1");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("register");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("mary");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("demo");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register1", new SpaceFilter("add new"), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register1", new SpaceFilter("m"), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register", new SpaceFilter("my space test "), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("mary", new SpaceFilter("add"), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("demo", new SpaceFilter("my"), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    int invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register1", new SpaceFilter("add new"));
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register1", new SpaceFilter("m"));
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register", new SpaceFilter("my space test "));
    assertEquals("invitedSpacesCount must be: " + 0, 0, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("mary", new SpaceFilter("add"));
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("demo", new SpaceFilter("my"));
    assertEquals("invitedSpacesCount must be: " + 0, 0, invitedSpacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesWithOffset() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpaces("register1", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 5, 5, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("register", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("mary", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 5, 5, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("demo", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesCount(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    int invitedSpacesCount = spaceStorage.getInvitedSpacesCount("register1");
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesCount("mary");
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesCount("nobody");
    assertEquals("invitedSpacesCount must be: 0", 0, invitedSpacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpaces() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpaces("hacker");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("hack");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("paul");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("jame");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("victory");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpacesByFilter("hacker", new SpaceFilter("add new"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hacker", new SpaceFilter("m"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hack", new SpaceFilter("my space test"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("paul", new SpaceFilter("add"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("jame", new SpaceFilter("my"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("victory", new SpaceFilter("my space test "), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    int pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hacker", new SpaceFilter("add new"));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hacker", new SpaceFilter("m"));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hack", new SpaceFilter("my space test"));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("paul", new SpaceFilter("add"));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("jame", new SpaceFilter("my"));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("victory", new SpaceFilter("my space test "));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesWithOffset() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpaces("hacker", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("paul", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("jame", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("victory", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesCount(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }
    int pendingSpaceCount = spaceStorage.getPendingSpacesCount("jame");
    assertEquals("pendingSpaceCount must be: " + countSpace, countSpace, pendingSpaceCount);

    pendingSpaceCount = spaceStorage.getPendingSpacesCount("paul");
    assertEquals("pendingSpaceCount must be: " + countSpace, countSpace, pendingSpaceCount);

    pendingSpaceCount = spaceStorage.getPendingSpacesCount("hacker");
    assertEquals("pendingSpaceCount must be: " + countSpace, countSpace, pendingSpaceCount);

    pendingSpaceCount = spaceStorage.getPendingSpacesCount("nobody");
    assertEquals("pendingSpaceCount must be: 0", 0, pendingSpaceCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpaces() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    List<Space> memberSpaces = spaceStorage.getMemberSpaces("raul");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("ghost");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("dragon");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("paul");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: 0", 0, memberSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    List<Space> memberSpaces = spaceStorage.getMemberSpacesByFilter("raul", new SpaceFilter("my space test"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter("add new"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter("space"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter("new"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter("m"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("dragon", new SpaceFilter("add"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("paul", new SpaceFilter("space"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: 0", 0, memberSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getManagerSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   */
  public void testGetManagerSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    List<Space> managerSpaces = spaceStorage.getManagerSpacesByFilter("raul", new SpaceFilter("my space test"), 0, 10);
    assertNotNull("managerSpaces must not be  null", managerSpaces);
    assertEquals("managerSpaces.size() must return: 0", 0, managerSpaces.size());

    managerSpaces = spaceStorage.getManagerSpacesByFilter("ghost", new SpaceFilter("add new"), 0, 10);
    assertNotNull("managerSpaces must not be  null", managerSpaces);
    assertEquals("managerSpaces.size() must return: 0", 0, managerSpaces.size());

    managerSpaces = spaceStorage.getManagerSpacesByFilter("dragon", new SpaceFilter("add"), 0, 10);
    assertNotNull("managerSpaces must not be  null", managerSpaces);
    assertEquals("managerSpaces.size() must return: 0", 0, managerSpaces.size());

    managerSpaces = spaceStorage.getManagerSpacesByFilter("demo", new SpaceFilter("space"), 0, 10);
    assertNotNull("managerSpaces must not be  null", managerSpaces);
    assertEquals("managerSpaces.size() must return: " + countSpace, countSpace, managerSpaces.size());

    managerSpaces = spaceStorage.getManagerSpacesByFilter("demo", new SpaceFilter("space2"), 0, 10);
    assertNotNull("managerSpaces must not be  null", managerSpaces);
    assertEquals("managerSpaces.size() must return: 0", 0, managerSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    int memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("raul", new SpaceFilter("my space test"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter("add new"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter("space"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter("new"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter("m"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("dragon", new SpaceFilter("add"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("paul", new SpaceFilter("space"));
    assertEquals("memberSpacesCount must be: 0", 0, memberSpacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getManagerSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   */
  public void testGetManagerSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    for (int i = 0; i < countSpace; i++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    int managerSpacesCount = spaceStorage.getManagerSpacesByFilterCount("raul", new SpaceFilter("my space test"));
    assertEquals("managerSpacesCount must be: " + 0, 0, managerSpacesCount);

    managerSpacesCount = spaceStorage.getManagerSpacesByFilterCount("ghost", new SpaceFilter("add new"));
    assertEquals("managerSpacesCount must be: " + 0, 0, managerSpacesCount);

    managerSpacesCount = spaceStorage.getManagerSpacesByFilterCount("dragon", new SpaceFilter("space"));
    assertEquals("managerSpacesCount must be: " + 0, 0, managerSpacesCount);

    managerSpacesCount = spaceStorage.getManagerSpacesByFilterCount("demo", new SpaceFilter("space"));
    assertEquals("managerSpacesCount must be: " + countSpace, countSpace, managerSpacesCount);

    managerSpacesCount = spaceStorage.getManagerSpacesByFilterCount("demo", new SpaceFilter("old"));
    assertEquals("managerSpacesCount must be: 0", 0, managerSpacesCount);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpacesWithListAccess() throws Exception {
    int countSpace = 10;
    for (int i = 0; i < countSpace; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      persist();
    }

    List<Space> memberSpaces = spaceStorage.getMemberSpaces("raul", 0, 5);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + 5, 5, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("ghost", 0, countSpace);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("dragon", 0, 6);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + 6, 6, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("paul", 0, countSpace);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: 0", 0, memberSpaces.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceById(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceById() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    space.setPublicSiteId(3l);
    space.setPublicSiteVisibility(SpaceUtils.INTERNAL);

    spaceStorage.saveSpace(space, true);
    persist();

    Space savedSpace = spaceStorage.getSpaceById(space.getId());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertNotNull("savedSpace.getId() must not be null", savedSpace.getId());
    assertEquals("space.getId() must return: " + space.getId(), space.getId(), savedSpace.getId());
    assertEquals("space.getPrettyName() must return: " + space.getPrettyName(),
                 space.getPrettyName(),
                 savedSpace.getPrettyName());
    assertEquals("space.getRegistration() must return: " + space.getRegistration(),
                 space.getRegistration(),
                 savedSpace.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(),
                 space.getDescription(),
                 savedSpace.getDescription());
    assertEquals("space.getVisibility() must return: " + space.getVisibility(),
                 space.getVisibility(),
                 savedSpace.getVisibility());
    assertEquals("space.getPublicSiteId() must return: " + space.getPublicSiteId(),
                 space.getPublicSiteId(),
                 savedSpace.getPublicSiteId());
    assertEquals("space.getPublicSiteVisibility() must return: " + space.getPublicSiteVisibility(),
                 space.getPublicSiteVisibility(),
                 savedSpace.getPublicSiteVisibility());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceByGroupId(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByGroupId() throws Exception {
    Space space = getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);
    persist();
    Space savedSpace = spaceStorage.getSpaceByGroupId(space.getGroupId());

    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals(space.getId(), savedSpace.getId());

  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceByPrettyName(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByPrettyName() throws Exception {
    // number for method getSpaceInstance(int number)
    int number = 1;
    // new space
    Space space = this.getSpaceInstance(number);
    // save to space activityStorage
    spaceStorage.saveSpace(space, true);
    persist();

    // get space saved by name
    Space foundSpaceList = spaceStorage.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertNotNull("foundSpaceList.getId() must not be null", foundSpaceList.getId());
    assertEquals("space.getId() must return: " + space.getId(), space.getId(), foundSpaceList.getId());
    assertEquals("space.getPrettyName() must return: " + space.getPrettyName(),
                 space.getPrettyName(),
                 foundSpaceList.getPrettyName());
    assertEquals("space.getRegistration() must return: " + space.getRegistration(),
                 space.getRegistration(),
                 foundSpaceList.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(),
                 space.getDescription(),
                 foundSpaceList.getDescription());
    assertEquals("space.getVisibility() must return: " + space.getVisibility(),
                 space.getVisibility(),
                 foundSpaceList.getVisibility());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#deleteSpace(String)}
   *
   * @throws Exception
   */
  public void testDeleteSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    spaceStorage.saveSpace(space, true);
    persist();
    spaceStorage.deleteSpace(space.getId());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#createSpace(org.exoplatform.social.core.space.model.Space, boolean)}
   *
   * @throws Exception
   */
  public void testSaveSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    spaceStorage.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    String newName = "newnamespace";
    space.setDisplayName(newName);
    space.setPrettyName(space.getDisplayName());
    spaceStorage.saveSpace(space, false);
    persist();
    assertEquals(newName, spaceStorage.getSpaceById(space.getId()).getPrettyName());
    assertEquals(newName, space.getPrettyName());

    Space got = spaceStorage.getSpaceById(space.getId());
    assertNotNull(got.getAvatarUrl());
  }

  /**
   * Test {@link SpaceStorage#renameSpace(Space, String)}
   *
   * @throws Exception
   * @since 1.2.8
   */
  public void testRenameSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    spaceStorage.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    String newName = "newnamespace";
    space.setDisplayName(newName);
    space.setPrettyName(space.getDisplayName());
    spaceStorage.saveSpace(space, false);
    persist();
    assertEquals("spaceStorage.getSpaceById(space.getId()).getName() must return: " + newName,
                 newName,
                 spaceStorage.getSpaceById(space.getId()).getPrettyName());
    assertEquals("space.getName() must return: " + newName, newName, space.getPrettyName());

    Space got = spaceStorage.getSpaceById(space.getId());
    assertNotNull(got.getAvatarUrl());

    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, got.getPrettyName());
    identityStorage.saveIdentity(spaceIdentity);

    String newDisplayName = "new display name";
    spaceStorage.renameSpace(space, newDisplayName);

    got = spaceStorage.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#createSpace(org.exoplatform.social.core.space.model.Space, boolean)}
   * with isNew is false
   *
   * @throws Exception
   */
  public void testUpdateSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null,
                                                             "avatar",
                                                             "png",
                                                             inputStream,
                                                             System.currentTimeMillis());
    assertNotNull("avatar attachment should not be null", avatarAttachment);
    space.setAvatarAttachment(avatarAttachment);
    spaceStorage.saveSpace(space, true);
    persist();

    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    Profile profile = new Profile(identity);
    identity.setProfile(profile);
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    identityStorage.saveIdentity(identity);
//    identityStorage.saveProfile(profile);

    //
    long lastUpdatedTime = space.getLastUpdatedTime();
    Space spaceForUpdate = spaceStorage.getSpaceById(space.getId());
    spaceStorage.saveSpace(spaceForUpdate, false);
    persist();

    //
    Space got = spaceStorage.getSpaceById(spaceForUpdate.getId());

    assertNotNull("avatar URL should not be null", got.getAvatarUrl());
    assertTrue(got.getLastUpdatedTime() >= lastUpdatedTime);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpaces() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];

    // there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i++) {

      if (i < 6)
        // [0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.OPEN, "demo");
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.OPEN, "demo");

      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    // visible with remoteId = 'demo' return 10 spaces with SpaceFilter = null
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }

    // visible with remoteId = 'mary' return 6 spaces with SpaceFilter = null
    {
      int privateSpace1 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace1, privateSpace1, privateSpaces.size());
    }
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpacesWithValidate() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];

    // there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i++) {

      if (i < 6)
        // [0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.VALIDATION, "demo");
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.VALIDATION, "demo");

      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    // visible with remoteId = 'demo' return 10 spaces with SpaceFilter = null
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }

    // visible with remoteId = 'demo' return 10 spaces with SpaceFilter
    {
      int countSpace2 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("M"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace2, countSpace2, visibleAllSpaces.size());
    }

    // visible with remoteId = 'mary' return 6 spaces with SpaceFilter = null
    {
      int privateSpace1 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace1, privateSpace1, privateSpaces.size());
    }
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpacesFilterByName() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];

    // there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i++) {

      if (i < 6)
        // [0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.OPEN, "demo");
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.OPEN, "demo");

      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    // visible with remoteId = 'demo' return 10 spaces with SpaceFilter = null
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }

    // visible with remoteId = 'demo' return 10 spaces with SpaceFilter
    {
      int countSpace2 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("my space test"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace2, countSpace2, visibleAllSpaces.size());
    }

    // visible with remoteId = 'demo' return 0 spaces with SpaceFilter
    {
      int countSpace3 = 0;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("your space"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace3, countSpace3, visibleAllSpaces.size());
    }

    // visible with remoteId = 'mary' return 6 spaces with SpaceFilter = null
    {
      int privateSpace1 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace1, privateSpace1, privateSpaces.size());
    }

    // visible with remoteId = 'mary' return 6 spaces with SpaceFilter
    {
      int privateSpace2 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter("my space test"));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace2, privateSpace2, privateSpaces.size());
    }

    // visible with remoteId = 'mary' return 0 spaces with SpaceFilter
    {
      int privateSpace3 = 0;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter("your space"));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace3, privateSpace3, privateSpaces.size());
    }
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpacesCloseRegistration() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];

    // there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i++) {

      if (i < 6)
        // [0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.CLOSED, "demo");
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSED, "demo");

      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    // visible with remoteId = 'demo' return 10 spaces
    {
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.size());
    }

    // visible with remoteId = 'mary' return 6 spaces: can see
    {
      int registrationCloseSpaceCount = 6;
      List<Space> registrationCloseSpaces = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("registrationCloseSpaces must not be  null", registrationCloseSpaces);
      assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount,
                   registrationCloseSpaceCount,
                   registrationCloseSpaces.size());
    }
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpacesCloseRegistrationByFilter() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];

    // there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i++) {

      if (i < 6)
        // [0->5] :: there are 6 spaces with visible = 'private' and manager =
        // "demo"
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.CLOSED, "demo");
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSED, "demo");

      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    // visible with remoteId = 'demo' return 10 spaces with SpaceFilter
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("M"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }

    // visible with remoteId = 'demo' return 10 spaces with SpaceFilter
    // configured name "my space test"
    {

      int countSpace3 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("my space test"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace3, countSpace3, visibleAllSpaces.size());
    }

    // visible with remoteId = 'root' return 10 spaces with SpaceFilter
    // configured name "my space test"
    {

      int countSpace4 = 10;
      List<Space> visibleAllSpaces = getSpaceWithRoot(new SpaceFilter("my space test"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace4, countSpace4, visibleAllSpaces.size());
    }

    // visible with remoteId = 'root' return 10 spaces with SpaceFilter is null.
    {

      int countSpace5 = 10;
      List<Space> visibleAllSpaces = getSpaceWithRoot(null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace5, countSpace5, visibleAllSpaces.size());
    }

    // visible with remoteId = 'root' return 0 spaces with SpaceFilter
    // configured name "my space test"
    {

      int countSpace6 = 0;
      List<Space> visibleAllSpaces = getSpaceWithRoot(new SpaceFilter("your space"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace6, countSpace6, visibleAllSpaces.size());
    }

    // visible with remoteId = 'mary' return 6 spaces: see although with
    {
      int registrationCloseSpaceCount1 = 6;
      List<Space> registrationCloseSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter("M"));
      assertNotNull("registrationCloseSpaces must not be  null", registrationCloseSpaces);
      assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount1,
                   registrationCloseSpaceCount1,
                   registrationCloseSpaces.size());
    }

    // visible with remoteId = 'root' return 10 spaces: see all spaces:: check
    // at SpaceServiceImpl
    {
      int registrationCloseSpaceCount2 = 10;
      List<Space> registrationCloseSpaces1 = spaceStorage.getSpacesByFilter(new SpaceFilter("M"), 0, 200);
      assertNotNull("registrationCloseSpaces must not be  null", registrationCloseSpaces1);
      assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount2,
                   registrationCloseSpaceCount2,
                   registrationCloseSpaces1.size());
    }
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpacesInvitedMember() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];

    // there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i++) {

      if (i < 6)
        // [0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstanceInvitedMember(i,
                                                          Space.PRIVATE,
                                                          Space.CLOSED,
                                                          new String[] { "mary", "hacker" },
                                                          "demo");
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSED, "demo");

      spaceStorage.saveSpace(listSpace[i], true);
      persist();
    }

    // visible with remoteId = 'demo' return 10 spaces
    {
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.size());
    }

    // visible with invited = 'mary' return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      List<Space> invitedSpaces1 = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.size());
    }

    // visible with invited = 'hacker' return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      List<Space> invitedSpaces1 = spaceStorage.getVisibleSpaces("hacker", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.size());
    }

    // visible with invited = 'paul' return 6 spaces
    {
      int invitedSpaceCount2 = 6;
      List<Space> invitedSpaces2 = spaceStorage.getVisibleSpaces("paul", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces2);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount2, invitedSpaceCount2, invitedSpaces2.size());
    }
  }

  public void testVisited() throws Exception {
    Space space0 = getSpaceInstance(5);
    spaceStorage.saveSpace(space0, true);
    Space space1 = getSpaceInstance(6);
    spaceStorage.saveSpace(space1, true);

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");

    List<Space> result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space0.getId(), result.get(0).getId());

    restartTransaction();
    spaceStorage.updateSpaceAccessed("ghost", space1);

    // getVisitedSpaces return a list of spaces that
    // order by visited space then others
    result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space1.getId(), result.get(0).getId());
  }

  public void testLastAccess() throws Exception {
    Space space2 = getSpaceInstance(7);
    spaceStorage.saveSpace(space2, true);
    Space space3 = getSpaceInstance(8);
    spaceStorage.saveSpace(space3, true);

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");

    List<Space> result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space2.getId(), result.get(0).getId());

    restartTransaction();
    spaceStorage.updateSpaceAccessed("ghost", space3);

    result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space3.getId(), result.get(0).getId());
  }

  public void testGetLastAccessedSpace() {
    // create a new space
    Space space = getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);
    // Update space accessed
    spaceStorage.updateSpaceAccessed("raul", space);
    cacheService.getSpacesCache().clearCache();

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("raul");
    // Get last accessed space list , this will fill all the caches with a
    // complete SpaceData
    spaceStorage.getLastAccessedSpace(filter, 0, 10);
    // clear SpaceCache and SpaceSimpleCache
    cacheService.getSpaceCache().clearCache();
    // Get last accessed space list again, this will fill both SimpleSpaceCache
    // and SpaceCache with a SpaceSimpleData
    // that has members and managers set to null by the function
    // putSpaceInCacheIfNotExists
    spaceStorage.getLastAccessedSpace(filter, 0, 10);

    // Get the space from SpaceCache, this will retrieve a SpaceSimpleData
    // object with managers and members set to null
    Space spaceFromCache = spaceStorage.getSpaceById(space.getId());

    // Check that the space should have 3 members and 2 managers
    assertNotNull(spaceFromCache.getMembers());
    assertEquals(5, spaceFromCache.getMembers().length);
    assertNotNull(spaceFromCache.getManagers());
    assertEquals(2, spaceFromCache.getManagers().length);
  }

  public void testGetLastAccessedPagination() throws Exception {
    assertEquals(0, spaceService.getLastAccessedSpace("raul").getSize());

    int numberOfSpaces = 5;
    for (int i = 0; i < numberOfSpaces; i++) {
      Space s = getSpaceInstance(i);
      spaceStorage.saveSpace(s, true);
      s = spaceStorage.getSpaceByPrettyName(s.getPrettyName());
      assertNotNull(s);
      restartTransaction();

      spaceService.updateSpaceAccessed("raul", s);
      restartTransaction();

      SpaceFilter filter = new SpaceFilter();
      filter.setRemoteId("raul");
      List<Space> spaces = spaceStorage.getLastAccessedSpace(filter, 0, 10);
      assertEquals(s.getPrettyName(), spaces.get(0).getPrettyName());

      spaces = spaceStorage.getLastAccessedSpace(filter, 0, i + 1);
      assertEquals(s.getPrettyName(), spaces.get(0).getPrettyName());
    }

    ListAccess<Space> spacesListAccess = spaceService.getLastAccessedSpace("raul");
    Space[] spaces = spacesListAccess.load(0, 2);
    assertEquals(2, spaces.length);
    assertEquals("my_space_test_4", spaces[0].getPrettyName());
    assertEquals("my_space_test_3", spaces[1].getPrettyName());

    Space space6 = getSpaceInstance(6);
    spaceStorage.saveSpace(space6, true);
    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(0, 2);
    assertEquals(2, spaces.length);
    assertEquals("my_space_test_4", spaces[0].getPrettyName());
    assertEquals("my_space_test_3", spaces[1].getPrettyName());

    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(2, 10);
    assertEquals(4, spaces.length);
    assertEquals("my_space_test_2", spaces[0].getPrettyName());
    assertEquals("my_space_test_1", spaces[1].getPrettyName());
    assertEquals("my_space_test_0", spaces[2].getPrettyName());
    assertEquals("my_space_test_6", spaces[3].getPrettyName());

    spaceService.updateSpaceAccessed("raul", space6);

    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(0, 2);
    assertEquals(2, spaces.length);
    assertEquals("my_space_test_6", spaces[0].getPrettyName());
    assertEquals("my_space_test_4", spaces[1].getPrettyName());

    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(2, 10);
    assertEquals(4, spaces.length);
    assertEquals("my_space_test_3", spaces[0].getPrettyName());
    assertEquals("my_space_test_2", spaces[1].getPrettyName());
    assertEquals("my_space_test_1", spaces[2].getPrettyName());
    assertEquals("my_space_test_0", spaces[3].getPrettyName());
  }

  public void testGetCommonSpaces() throws Exception {

    // Get instances of 3 spaces with different members and 1 common manager
    Space space1 = getSpaceInstance(11, "public", "open", "demo", "ghost", "dragon");
    Space space2 = getSpaceInstance(12, "public", "open", "demo", "ghost", "dragon", "raul");
    Space space3 = getSpaceInstance(13, "public", "open", "demo", "ghost", "dragon", "raul");
    Space space4 = getSpaceInstance(14, "public", "open", "demo", "ghost", "dragon", "raul");
    Space space5 = getSpaceInstance(15, "public", "open", "demo", "ghost", "dragon", "raul");
    Space space6 = getSpaceInstance(16, "public", "open", "demo", "ghost", "dragon", "raul");

    spaceStorage.saveSpace(space1, true);
    spaceStorage.saveSpace(space2, true);
    spaceStorage.saveSpace(space3, true);
    spaceStorage.saveSpace(space4, true);
    spaceStorage.saveSpace(space5, true);
    spaceStorage.saveSpace(space6, true);

    List<Space> resultListCommonSpaces1 = spaceStorage.getCommonSpaces("demo", "raul", 0, 2);
    assertEquals(2, resultListCommonSpaces1.size());
    Space testSpace2 = resultListCommonSpaces1.get(0);
    assertEquals(space2, testSpace2);
    Space testSpace3 = resultListCommonSpaces1.get(1);
    assertEquals(space3, testSpace3);

    List<Space> resultListCommonSpaces2 = spaceStorage.getCommonSpaces("demo", "raul", 2, 2);
    assertEquals(2, resultListCommonSpaces2.size());
    Space testSpace4 = resultListCommonSpaces2.get(0);
    assertEquals(space4, testSpace4);
    Space testSpace5 = resultListCommonSpaces2.get(1);
    assertEquals(space5, testSpace5);

    List<Space> resultListCommonSpaces3 = spaceStorage.getCommonSpaces("demo", "raul", 4, 2);
    assertEquals(1, resultListCommonSpaces3.size());
    Space testSpace6 = resultListCommonSpaces3.get(0);
    assertEquals(space6, testSpace6);

  }

  public void testCountCommonSpaces() throws Exception {

    // Get instances of 3 spaces with different members and 1 common manager
    Space space3 = getSpaceInstance(11, "public", "open", "demo", "ghost", "dragon");
    Space space1 = getSpaceInstance(12, "public", "open", "demo", "ghost", "dragon", "raul");
    Space space2 = getSpaceInstance(13, "public", "open", "demo", "ghost", "dragon", "raul");

    spaceStorage.saveSpace(space3, true);
    spaceStorage.saveSpace(space1, true);
    spaceStorage.saveSpace(space2, true);

    int resultListCommonSpaces = spaceStorage.countCommonSpaces("demo", "raul");
    assertEquals(2, resultListCommonSpaces);

  }

}
