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
package org.exoplatform.social.core.space.spi;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.EntityConverterUtils;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.mock.SpaceListenerPluginMock;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent.Type;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;

import lombok.SneakyThrows;

public class SpaceServiceTest extends AbstractCoreTest {

  private static final String           EXTERNAL_USER = "externalUser";

  private IdentityStorage               identityStorage;

  private OrganizationService           organizationService;

  protected SpacesAdministrationService spacesAdministrationService;

  private List<Space>                   tearDownSpaceList;

  private List<Identity>                tearDownUserList;

  private final Log                     LOG = ExoLogger.getLogger(SpaceServiceTest.class);

  private Identity                      demo;

  private Identity                      tom;

  private Identity                      raul;

  private Identity                      ghost;

  private Identity                      dragon;

  private Identity                      register1;

  private Identity                      john;

  private Identity                      mary;

  private Identity                      harry;

  private Identity                      root;

  private Identity                      jame;

  private Identity                      paul;

  private Identity                      hacker;

  private Identity                      hearBreaker;

  private Identity                      newInvitedUser;

  private Identity                      newPendingUser;

  private Identity                      user_new;

  private Identity                      user_new1;

  private Identity                      user_new_dot;

  private Identity                      creator;

  private Identity                      manager;

  private Identity                      member1;

  private Identity                      member2;

  private Identity                      member3;

  private Identity                      externalUser;


  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    spacesAdministrationService = getContainer().getComponentInstanceOfType(SpacesAdministrationService.class);
    tearDownSpaceList = new ArrayList<Space>();
    tearDownUserList = new ArrayList<Identity>();

    user_new = new Identity(OrganizationIdentityProvider.NAME, "user-new");
    user_new1 = new Identity(OrganizationIdentityProvider.NAME, "user-new.1");
    user_new_dot = new Identity(OrganizationIdentityProvider.NAME, "user.new");
    demo = new Identity(OrganizationIdentityProvider.NAME, "demo");
    tom = new Identity(OrganizationIdentityProvider.NAME, "tom");
    raul = new Identity(OrganizationIdentityProvider.NAME, "raul");
    ghost = new Identity(OrganizationIdentityProvider.NAME, "ghost");
    dragon = new Identity(OrganizationIdentityProvider.NAME, "dragon");
    register1 = new Identity(OrganizationIdentityProvider.NAME, "register1");
    mary = new Identity(OrganizationIdentityProvider.NAME, "mary");
    john = new Identity(OrganizationIdentityProvider.NAME, "john");
    harry = new Identity(OrganizationIdentityProvider.NAME, "harry");
    root = new Identity(OrganizationIdentityProvider.NAME, "root");
    jame = new Identity(OrganizationIdentityProvider.NAME, "jame");
    paul = new Identity(OrganizationIdentityProvider.NAME, "paul");
    hacker = new Identity(OrganizationIdentityProvider.NAME, "hacker");
    hearBreaker = new Identity(OrganizationIdentityProvider.NAME, "hearBreaker");
    newInvitedUser = new Identity(OrganizationIdentityProvider.NAME, "newInvitedUser");
    newPendingUser = new Identity(OrganizationIdentityProvider.NAME, "newPendingUser");
    manager = new Identity(OrganizationIdentityProvider.NAME, "manager");
    creator = new Identity(OrganizationIdentityProvider.NAME, "creator");
    member1 = new Identity(OrganizationIdentityProvider.NAME, "member1");
    member2 = new Identity(OrganizationIdentityProvider.NAME, "member2");
    member3 = new Identity(OrganizationIdentityProvider.NAME, "member3");
    externalUser = new Identity(OrganizationIdentityProvider.NAME, EXTERNAL_USER);
    checkExternalUserMemberships();

    identityStorage.saveIdentity(demo);
    identityStorage.saveIdentity(tom);
    identityStorage.saveIdentity(raul);
    identityStorage.saveIdentity(ghost);
    identityStorage.saveIdentity(dragon);
    identityStorage.saveIdentity(register1);
    identityStorage.saveIdentity(mary);
    identityStorage.saveIdentity(harry);
    identityStorage.saveIdentity(john);
    identityStorage.saveIdentity(root);
    identityStorage.saveIdentity(jame);
    identityStorage.saveIdentity(paul);
    identityStorage.saveIdentity(hacker);
    identityStorage.saveIdentity(hearBreaker);
    identityStorage.saveIdentity(newInvitedUser);
    identityStorage.saveIdentity(newPendingUser);
    identityStorage.saveIdentity(user_new1);
    identityStorage.saveIdentity(user_new);
    identityStorage.saveIdentity(user_new_dot);
    identityStorage.saveIdentity(manager);
    identityStorage.saveIdentity(creator);
    identityStorage.saveIdentity(member1);
    identityStorage.saveIdentity(member2);
    identityStorage.saveIdentity(member3);

    tearDownUserList.add(demo);
    tearDownUserList.add(tom);
    tearDownUserList.add(raul);
    tearDownUserList.add(ghost);
    tearDownUserList.add(dragon);
    tearDownUserList.add(register1);
    tearDownUserList.add(mary);
    tearDownUserList.add(harry);
    tearDownUserList.add(john);
    tearDownUserList.add(root);
    tearDownUserList.add(jame);
    tearDownUserList.add(paul);
    tearDownUserList.add(hacker);
    tearDownUserList.add(hearBreaker);
    tearDownUserList.add(newInvitedUser);
    tearDownUserList.add(newPendingUser);
    tearDownUserList.add(user_new1);
    tearDownUserList.add(user_new);
    tearDownUserList.add(user_new_dot);
    tearDownUserList.add(manager);
    tearDownUserList.add(creator);
    tearDownUserList.add(member1);
    tearDownUserList.add(member2);
    tearDownUserList.add(member3);
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();

    for (Identity identity : tearDownUserList) {
      identityRegistry.unregister(identity.getRemoteId());
      try {
        identityStorage.deleteIdentity(identity);
      } catch (IdentityStorageException e) {
        // It's expected on some identities that could be deleted in tests
      }
    }

    super.tearDown();
  }

  /**
   * Test {@link SpaceService#getAllSpacesWithListAccess()}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesWithListAccess() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      this.getSpaceInstance(i);
    }
    ListAccess<Space> allSpaces = spaceService.getAllSpacesWithListAccess();
    assertNotNull("allSpaces must not be null", allSpaces);
    assertEquals("allSpaces.getSize() must return: " + count, count, allSpaces.getSize());
    assertEquals("allSpaces.load(0, 1).length must return: 1", 1, allSpaces.load(0, 1).length);
    assertEquals("allSpaces.load(0, count).length must return: " + count, count, allSpaces.load(0, count).length);
  }

  public void testGetSpaceMembershipDate() throws Exception {
    Space space = populateData();
    assertNotNull(spaceService.getSpaceMembershipDate(Long.parseLong(space.getId()), "root"));
    assertNotNull(spaceService.getSpaceMembershipDate(Long.parseLong(space.getId()), "john"));
    assertNull(spaceService.getSpaceMembershipDate(Long.parseLong(space.getId()), "notExistingUser"));
  }

  /**
   * Test {@link SpaceService#getSpaceByDisplayName(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByDisplayName() throws Exception {
    Space space = populateData();
    Space gotSpace1 = spaceService.getSpaceByDisplayName("Space1");

    assertNotNull("gotSpace1 must not be null", gotSpace1);

    assertEquals(space.getDisplayName(), gotSpace1.getDisplayName());
  }

  /**
   * Test {@link SpaceService#getSpaceByPrettyName(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpaceByPrettyName() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      this.getSpaceInstance(i);
    }
    Space foundSpace = spaceService.getSpaceByPrettyName("my_space_4");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 4", "my space 4", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_4", "my_space_4", foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByPrettyName("my_space_0");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 0", "my space 0", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_0", "my_space_0", foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByPrettyName("my_space_5");
    assertNull("foundSpace must be null", foundSpace);
  }

  public void testDefaultSpaceAvatar() throws Exception {
    this.getSpaceInstance(0);
    Space space = spaceService.getSpaceByPrettyName("my_space_0");
    assertNotNull("space must not be null", space);
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);
    tearDownUserList.add(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FULL_NAME, space.getDisplayName());
    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    String identityId = identity.getId();
    assertNotNull(identityId);

    FileItem avatarFile = identityStorage.getAvatarFile(identity);
    assertNotNull(avatarFile);
    Long avatarFileId = avatarFile.getFileInfo().getId();
    assertNotNull(avatarFileId);
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    profile = identityStorage.loadProfile(profile);
    assertTrue(profile.isDefaultAvatar());
  }

  public void testUpdateSpaceAvatar() throws Exception {
    this.getSpaceInstance(0);
    Space space = spaceService.getSpaceByPrettyName("my_space_0");
    assertNotNull("space must not be null", space);
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);
    tearDownUserList.add(identity);
    FileItem avatarFile = identityStorage.getAvatarFile(identity);
    assertNotNull(avatarFile);
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());

    Profile profile = new Profile(identity);
    identity.setProfile(profile);
    profile = identityStorage.loadProfile(profile);
    assertTrue(profile.isDefaultAvatar());

    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment =
        new AvatarAttachment(null, "space-avatar", "png", inputStream, System.currentTimeMillis());
    space.setAvatarAttachment(avatarAttachment);
    spaceService.updateSpaceAvatar(space, space.getManagers()[0]);
    profile = new Profile(identity);
    profile.setProperty(Profile.AVATAR, space.getAvatarAttachment());
    identityStorage.saveProfile(profile);
    FileItem newAvatarFile = identityStorage.getAvatarFile(identity);
    assertNotNull(avatarFile);
    assertNotEquals(EntityConverterUtils.DEFAULT_AVATAR, newAvatarFile.getFileInfo().getName());

    profile = identityStorage.loadProfile(profile);
    assertFalse(profile.isDefaultAvatar());
  }

  public void testRenameSpaceWithDefaultAvatar() throws Exception {
    Space space = this.getSpaceInstance(0);
    assertNotNull(space);

    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    assertNotNull(identity);

    identityStorage.saveIdentity(identity);
    tearDownUserList.add(identity);
    String newDisplayName = "new display name";
    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      spaceService.renameSpace(space, newDisplayName, root.getRemoteId());
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }
    Space got = spaceService.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());
    assertEquals(Type.SPACE_RENAMED, spaceListenerPlugin.getEvents().get(0));
  }

  public void testGetBookmarkedSpace() throws Exception {
    Space space1 = createSpace("Space1", john.getRemoteId());
    createSpace("Space2", john.getRemoteId());
    Space space3 = createSpace("Space3", john.getRemoteId());
    createSpace("Space4", john.getRemoteId());

    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Favorite space1Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE, space1.getId(), null, Long.parseLong(john.getId()));
    favoriteService.createFavorite(space1Favorite);
    Favorite space2Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE, space3.getId(), null, Long.parseLong(john.getId()));
    favoriteService.createFavorite(space2Favorite);

    SpaceFilter spaceFilter = new SpaceFilter();
    assertEquals(4, spaceService.getAccessibleSpacesByFilter(john.getRemoteId(), spaceFilter).getSize());

    spaceFilter.setRemoteId(john.getRemoteId());
    ListAccess<Space> listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
    assertEquals(4, listAccess.getSize());
    Space[] spacesList = listAccess.load(0, 10);
    assertEquals(4, spacesList.length);
    assertEquals(4, spaceService.getAccessibleSpacesByFilter(john.getRemoteId(), spaceFilter).getSize());

    spaceFilter.setFavorite(true);
    listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
    assertEquals(2, listAccess.getSize());
    spacesList = listAccess.load(0, 10);
    assertEquals(2, spacesList.length);
    assertEquals(2, spaceService.getAccessibleSpacesByFilter(john.getRemoteId(), spaceFilter).getSize());

    favoriteService.deleteFavorite(space1Favorite);
    listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
    assertEquals(1, listAccess.getSize());
    spacesList = listAccess.load(0, 10);
    assertEquals(1, spacesList.length);
    assertEquals(1, spaceService.getAccessibleSpacesByFilter(john.getRemoteId(), spaceFilter).getSize());

    favoriteService.deleteFavorite(space2Favorite);
    listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
    assertEquals(0, listAccess.getSize());
    spacesList = listAccess.load(0, 10);
    assertEquals(0, spacesList.length);
    assertEquals(0, spaceService.getAccessibleSpacesByFilter(john.getRemoteId(), spaceFilter).getSize());
  }
  
  /**
   * Test {@link SpaceService#getMemberSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   */
  public void testGetFavoriteSpacesByFilter() throws Exception {
    Space space1 = createSpace("Space1", john.getRemoteId());
    createSpace("Space2", john.getRemoteId());
    Space space3 = createSpace("Space3", john.getRemoteId());
    createSpace("Space4", john.getRemoteId());

    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Favorite space1Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE, space1.getId(), null, Long.parseLong(john.getId()));
    favoriteService.createFavorite(space1Favorite);
    Favorite space3Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE, space3.getId(), null, Long.parseLong(john.getId()));
    favoriteService.createFavorite(space3Favorite);
    favoriteService.createFavorite(new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE, space3.getId(), null, Long.parseLong(root.getId())));

    SpaceFilter spaceFilter = new SpaceFilter();
    assertEquals(4, spaceService.getAccessibleSpacesByFilter(john.getRemoteId(), spaceFilter).getSize());
    assertEquals(2, spaceService.getFavoriteSpacesByFilter(john.getRemoteId(), spaceFilter).getSize());
    assertEquals(1, spaceService.getFavoriteSpacesByFilter(root.getRemoteId(), spaceFilter).getSize());
    assertEquals(0, spaceService.getFavoriteSpacesByFilter(mary.getRemoteId(), spaceFilter).getSize());

    ListAccess<Space> listAccess = spaceService.getFavoriteSpacesByFilter(john.getRemoteId(), spaceFilter);
    Space[] spacesList = listAccess.load(0, 10);
    assertEquals(2, spacesList.length);

    favoriteService.deleteFavorite(space1Favorite);
    listAccess = spaceService.getFavoriteSpacesByFilter(john.getRemoteId(), spaceFilter);
    assertEquals(1, listAccess.getSize());
    spacesList = listAccess.load(0, 10);
    assertEquals(1, spacesList.length);

    spaceService.deleteSpace(space3);
    listAccess = spaceService.getFavoriteSpacesByFilter(john.getRemoteId(), spaceFilter);
    assertEquals(0, listAccess.getSize());
    spacesList = listAccess.load(0, 10);
    assertEquals(0, spacesList.length);
  }

  /**
   * Test {@link SpaceService#getAllSpacesByFilter(SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
//FIXME regression JCR to RDBMS migration
//  public void testGetAllSpacesByFilterWithSpaceNameSearchCondition() throws Exception {
//    int count = 5;
//    for (int i = 0; i < count; i++) {
//      this.getSpaceInstance(i);
//    }
//
//    ListAccess<Space> foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("my space"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//    assertEquals("foundSpaceListAccess.load(0, 1).length must return: 1", 1, foundSpaceListAccess.load(0, 1).length);
//    assertEquals("foundSpaceListAccess.load(0, count).length must return: " + count,
//                 count,
//                 foundSpaceListAccess.load(0, count).length);
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("1"));
//    assertEquals("foundSpaceListAccess.getSize() must return 11", 11, foundSpaceListAccess.getSize());
//    assertEquals("foundSpaceListAccess.load(0, 10).length must return 10",
//                 10,
//                 foundSpaceListAccess.load(0, 10).length);
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("add new space"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("space"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*space"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*space*"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*a*e*"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*a*e"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("a*e"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("a*"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%a%e%"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%a*e%"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%a*e*"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("***"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%%%%%"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("new"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("<new>new(\"new\")</new>"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    // correct test case : the term "new new new new" should not match the
//    // result
//    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());
//
//    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("what new space add"));
//    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
//    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());
//  }

  /**
   * Test {@link SpaceService#getSpaceByGroupId(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
//FIXME regression JCR to RDBMS migration
//  public void testGetSpaceByGroupId() throws Exception {
//    int count = 5;
//    for (int i = 0; i < count; i++) {
//      this.getSpaceInstance(i);
//    }
//    Space foundSpace = spaceService.getSpaceByGroupId("/space/space0");
//    assertNotNull("foundSpace must not be null", foundSpace);
//    assertEquals("foundSpace.getDisplayName() must return: my space 0", "my space 0", foundSpace.getDisplayName());
//    assertEquals("foundSpace.getGroupId() must return: /space/space0", "/space/space0", foundSpace.getGroupId());
//  }

  /**
   * Test {@link SpaceService#getSpaceById(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceById() throws Exception {
    Space space = populateData();
    createMoreSpace("Space2");
    assertEquals(space.getDisplayName(), spaceService.getSpaceById(space.getId()).getDisplayName());
  }

  /**
   * Test {@link SpaceService#getSpaceByUrl(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByUrl() throws Exception {
    Space space = populateData();
    assertEquals(space.getDisplayName(), spaceService.getSpaceByUrl("space1").getDisplayName());
  }

  public void testCanRedact() throws Exception {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity("root");
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity("john",
                                                                                                                Collections.singleton(new MembershipEntry("/platform/administrators",
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity("demo");
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity("james");
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity("mary");
    org.exoplatform.services.security.Identity raulACLIdentity = new org.exoplatform.services.security.Identity("raul");

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestRedact", "demo");
    space.setMembers(new String[]{"demo", "james", "raul"});
    spaceService.updateSpace(space);

    // Super Manager can redact
    assertTrue(spaceService.canRedactOnSpace(space, rootACLIdentity));
    // Platform Manager can redact
    assertTrue(spaceService.canRedactOnSpace(space, johnACLIdentity));
    // Space Manager can redact
    assertTrue(spaceService.canRedactOnSpace(space, demoACLIdentity));
    // Member can redact
    assertTrue(spaceService.canRedactOnSpace(space, jamesACLIdentity));
    assertTrue(spaceService.canRedactOnSpace(space, raulACLIdentity));
    // Non Member can't redact
    assertFalse(spaceService.canRedactOnSpace(space, maryACLIdentity));

    space.setMembers(new String[]{"demo", "james", "raul", "mary"});
    space.setRedactors(new String[]{"james"});
    space.setPublishers(new String[]{"mary"});
    spaceService.updateSpace(space);

    // Super Manager can redact
    assertTrue(spaceService.canRedactOnSpace(space, rootACLIdentity));
    // Platform Manager can redact
    assertTrue(spaceService.canRedactOnSpace(space, johnACLIdentity));
    // Space Manager can redact
    assertTrue(spaceService.canRedactOnSpace(space, demoACLIdentity));
    // Redactor can redact
    assertTrue(spaceService.canRedactOnSpace(space, jamesACLIdentity));
    // space publisher can't redact
    assertFalse(spaceService.canRedactOnSpace(space, maryACLIdentity));
    // space members can't redact
    assertFalse(spaceService.canRedactOnSpace(space, raulACLIdentity));
  }

  public void testCanView() throws Exception {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity("root");
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity("john",
                                                                                                                Collections.singleton(new MembershipEntry("/platform/administrators",
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity("demo");
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity("james");
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity("mary");

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestView", "demo");
    space.setMembers(new String[]{"demo", "james"});
    spaceService.updateSpace(space);

    // Super Manager can view space content
    assertTrue(spaceService.canViewSpace(space, rootACLIdentity.getUserId()));
    // Platform Manager can view space content
    assertTrue(spaceService.canViewSpace(space, johnACLIdentity.getUserId()));
    // Member can view space content
    assertTrue(spaceService.canViewSpace(space, jamesACLIdentity.getUserId()));
    // Outside space can't view space content
    assertFalse(spaceService.canViewSpace(space, maryACLIdentity.getUserId()));
  }

  public void testCanManage() throws Exception {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity("root");
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity("john",
                                                                                                                Collections.singleton(new MembershipEntry("/platform/administrators",
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity("demo");
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity("james");
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity("mary");
    org.exoplatform.services.security.Identity raulACLIdentity = new org.exoplatform.services.security.Identity("raul");
    org.exoplatform.services.security.Identity paulACLIdentity = new org.exoplatform.services.security.Identity("paul");

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestManage", "demo");
    space.setMembers(new String[]{"demo", "james", "mary", "raul"});
    space.setManagers(new String[]{"mary"});
    space.setPublishers(new String[]{"raul"});
    space.setRedactors(new String[]{"james"});
    spaceService.updateSpace(space);

    // Super Manager can manage space content
    assertTrue(spaceService.canManageSpace(space, rootACLIdentity.getUserId()));
    // Platform Manager can manage space content
    assertTrue(spaceService.canManageSpace(space, johnACLIdentity.getUserId()));
    // Manager can manage space content
    assertTrue(spaceService.canManageSpace(space, maryACLIdentity.getUserId()));
    // Publisher can't manage space content
    assertFalse(spaceService.canManageSpace(space, raulACLIdentity.getUserId()));
    // Redactor can't manage space content
    assertFalse(spaceService.canManageSpace(space, jamesACLIdentity.getUserId()));
    // Member can't manage space content
    assertFalse(spaceService.canManageSpace(space, demoACLIdentity.getUserId()));
    // Outside space can't manage space content
    assertFalse(spaceService.canManageSpace(space, paulACLIdentity.getUserId()));
  }

  public void testCanPublish() throws Exception {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity("root");
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity("john",
                                                                                                                Collections.singleton(new MembershipEntry("/platform/administrators",
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity("demo");
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity("james");
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity("mary");
    org.exoplatform.services.security.Identity raulACLIdentity = new org.exoplatform.services.security.Identity("raul");
    org.exoplatform.services.security.Identity paulACLIdentity = new org.exoplatform.services.security.Identity("paul");

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestPublish", "demo");
    space.setMembers(new String[]{"demo", "james", "mary", "raul"});
    space.setManagers(new String[]{"mary"});
    space.setPublishers(new String[]{"raul"});
    space.setRedactors(new String[]{"james"});
    spaceService.updateSpace(space);

    // Super Manager can publish space content
    assertTrue(spaceService.canPublishOnSpace(space, rootACLIdentity.getUserId()));
    // Platform Manager can publish space content
    assertTrue(spaceService.canPublishOnSpace(space, johnACLIdentity.getUserId()));
    // Manager can publish space content
    assertTrue(spaceService.canPublishOnSpace(space, maryACLIdentity.getUserId()));
    // Publisher can publish space content
    assertTrue(spaceService.canPublishOnSpace(space, raulACLIdentity.getUserId()));
    // Redactor can't publish space content
    assertFalse(spaceService.canPublishOnSpace(space, jamesACLIdentity.getUserId()));
    // Member can't publish space content
    assertFalse(spaceService.canPublishOnSpace(space, demoACLIdentity.getUserId()));
    // Outside space can't publish space content
    assertFalse(spaceService.canPublishOnSpace(space, paulACLIdentity.getUserId()));
  }

  /**
   * Test {@link SpaceService#getInvitedSpacesWithListAccess(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesWithListAccess() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      this.getSpaceInstance(i);
    }
    ListAccess<Space> invitedSpaces = spaceService.getInvitedSpacesWithListAccess("register1");
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());
    assertEquals("invitedSpaces.load(0, 1).length must return: " + 1, 1, invitedSpaces.load(0, 1).length);
    assertEquals("invitedSpaces.load(0, count).length must return: " + count,
                 count,
                 invitedSpaces.load(0, count).length);
    invitedSpaces = spaceService.getInvitedSpacesWithListAccess("mary");
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesWithListAccess("demo");
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

  }

  public void testCountPendingSpaceRequestsToManage() throws Exception {
    populateData();
    Space space = spaceService.getSpaceByDisplayName("Space1");
    spaceService.addPendingUser(space, "paul");
    spaceService.addPendingUser(space, "james");

    ListAccess<Space> listAccess = spaceService.getPendingSpaceRequestsToManage("root");
    assertNotNull(listAccess);
    assertEquals(2, listAccess.getSize());

    assertEquals(2, listAccess.load(0, 10).length);
    Space[] pendingSpaceRequestsToManage = listAccess.load(0, 1);
    assertEquals(1, pendingSpaceRequestsToManage.length);
    Space spaceToManage = pendingSpaceRequestsToManage[0];
    String[] pendingUsers = spaceToManage.getPendingUsers();
    assertEquals(1, pendingUsers.length);

    pendingSpaceRequestsToManage = listAccess.load(0, 10);
    assertEquals(2, pendingSpaceRequestsToManage.length);
    boolean hasPaul = pendingSpaceRequestsToManage[0].getPendingUsers()[0].equals("paul") || pendingSpaceRequestsToManage[1].getPendingUsers()[0].equals("paul");
    boolean hasJames = pendingSpaceRequestsToManage[0].getPendingUsers()[0].equals("james") || pendingSpaceRequestsToManage[1].getPendingUsers()[0].equals("james");
    assertTrue(hasJames && hasPaul);
  }

  /**
   * Test {@link SpaceService#getPendingSpacesWithListAccess(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesWithListAccess() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      this.getSpaceInstance(i);
    }
    ListAccess<Space> foundSpaces = spaceService.getPendingSpacesWithListAccess("jame");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());
    assertEquals("foundSpaces.load(0, 1).length must return: 1",
                 1,
                 foundSpaces.load(0, 1).length);
    assertEquals("foundSpaces.load(0, count).length must return: " + count,
                 count,
                 foundSpaces.load(0, count).length);

    foundSpaces = spaceService.getPendingSpacesWithListAccess("paul");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess("hacker");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess("ghost");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess("hellgate");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#createSpace(Space, String)}
   *
   * @throws Exception
   */
  public void testCreateSpace() throws Exception {
    populateData();
    createMoreSpace("Space2");
    ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
    assertNotNull("spaceListAccess must not be null", spaceListAccess);
    assertEquals("spaceListAccess.getSize() must return: 2", 2, spaceListAccess.getSize());
  }

  public void testCreateSpaceWithManagersAndMembers() throws SpaceException {
    String[] managers = { "manager" };
    String[] members = { "manager", "member1", "member2", "member3" };
    String creator = "root";
    String invitedGroup = "invited";
    Space space = new Space();
    space.setDisplayName("testSpace");
    space.setDescription("Space Description for Testing");
    String shortName = Utils.cleanString(space.getDisplayName());
    space.setGroupId("/spaces/" + shortName);
    space.setManagers(managers);
    space.setMembers(members);
    space.setPrettyName(space.getDisplayName());
    space.setPriority("3");
    space.setRegistration("validation");
    space.setTag("Space Tag for Testing");
    space.setType("classic");
    space.setUrl(shortName);
    space.setVisibility("public");
    spaceService.createSpace(space, creator);
    tearDownSpaceList.add(space);
    // 2 = 1 creator + 1 managers
    assertEquals(2, space.getManagers().length);
    // 4 = 1 creator + 3 members
    assertEquals(5, space.getMembers().length);
  }

  public void testCreateSpaceEvent() throws SpaceException {
    String[] managers = { "manager" };
    String creator = "root";
    String[] users = { "member1", "member2" };
    List<Identity> invitedIdentities = new ArrayList<>(Arrays.asList(tom, dragon, hearBreaker));
    Space space = new Space();
    space.setDisplayName("testSpace");
    space.setDescription("Space Description for Testing");
    String shortName = Utils.cleanString(space.getDisplayName());
    space.setGroupId("/spaces/" + shortName);
    space.setManagers(managers);
    space.setPrettyName(space.getDisplayName());
    space.setPriority("3");
    space.setRegistration("validation");
    space.setTag("Space Tag for Testing");
    space.setType("classic");
    space.setUrl(shortName);
    space.setVisibility("public");

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      spaceService.createSpace(space, creator, invitedIdentities);
      tearDownSpaceList.add(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(4, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_CREATED, spaceListenerPlugin.getEvents().get(0));
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(1));
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(2));
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(3));
  }

  public void testUpdateSpaceDescription() throws Exception {
    Space space = createSpace("spaceUpdateDescription", "demo");

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setDescription("Updated Description");
      spaceService.updateSpace(space);
      tearDownSpaceList.add(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_DESCRIPTION_EDITED, spaceListenerPlugin.getEvents().get(0));
  }

  public void testUpdateSpaceAccess() throws Exception {
    Space space = createSpace("spaceUpdateAccess", "demo");

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setVisibility(Space.HIDDEN);
      spaceService.updateSpace(space);
      tearDownSpaceList.add(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_HIDDEN, spaceListenerPlugin.getEvents().get(0));
  }

  public void testUpdateSpaceRegistration() throws Exception {
    Space space = createSpace("spaceUpdateRegistration", "demo");

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setRegistration(Space.VALIDATION);
      spaceService.updateSpace(space);
      tearDownSpaceList.add(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_REGISTRATION, spaceListenerPlugin.getEvents().get(0));
  }

  public void testSpaceUserInvitation() throws Exception {
    Space space = createSpace("spaceUserInvitation", "demo");

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      spaceService.addInvitedUser(space, john.getRemoteId());
      tearDownSpaceList.add(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(0));
  }

  public void testSpaceUserInvitationDeny() throws Exception {
    Space space = createSpace("spaceUserInvitationDeny", "demo");
    spaceService.addInvitedUser(space, john.getRemoteId());

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      spaceService.removeInvitedUser(space, john.getRemoteId());
      tearDownSpaceList.add(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.DENY_INVITED_USER, spaceListenerPlugin.getEvents().get(0));
  }

  /**
   * Test
   * {@link SpaceService#createSpace(org.exoplatform.social.core.space.model.Space, String, List)}
   */
  public void testCreateSpaceWithInvitation() throws Exception {
    String[] managers = { "manager" };
    String creator = "root";
    Space spaceCreated = createMoreSpace("invitedSpace");
    String[] users = { "member1", "member2" };
    spaceCreated.setMembers(users);
    spaceService.updateSpace(spaceCreated);
    Identity spaceIdentity = getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                                   "invitedspace",
                                                                                   false);
    List<Identity> invitedIdentities = new ArrayList<>(Arrays.asList(tom, dragon, hearBreaker, spaceIdentity));
    Space space = new Space();
    space.setDisplayName("testSpace");
    space.setDescription("Space Description for Testing");
    String shortName = Utils.cleanString(space.getDisplayName());
    space.setGroupId("/spaces/" + shortName);
    space.setManagers(managers);
    space.setPrettyName(space.getDisplayName());
    space.setPriority("3");
    space.setRegistration("validation");
    space.setTag("Space Tag for Testing");
    space.setType("classic");
    space.setUrl(shortName);
    space.setVisibility("public");
    spaceService.createSpace(space, creator, invitedIdentities);
    tearDownSpaceList.add(space);
    // 2 = 1 creator + 1 managers
    assertEquals(2, space.getManagers().length);
    // 5 = member1, member2 from invitedSpace + tom + dragon + hearBreaker
    assertEquals(5, space.getInvitedUsers().length);
  }

  public void testCreateSpaceExceedingNameLimit() throws RuntimeException {
    Space space = new Space();
    String spaceDisplayName =
                            "zzz0123456791011121314151617181920012345679101112131415161718192001234567910111213141516171819200123456791011121314151617181920012345679101112131415161718192001234567910111213141516171819200123456791011121314151617181920012345679101112131415161718192001234567910111213141516171819200123456791011121314151617181920";
    String shortName = "zzz";
    space.setDisplayName(spaceDisplayName);
    space.setPrettyName(shortName);
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space ");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String creator = "root";
    space.setGroupId("/spaces/" + shortName);
    try {
      spaceService.createSpace(space, creator);
      fail("Should have thrown an RuntimeException because Name length exceeds limits");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("space name cannot exceed 200 characters"));
    }
  }

  public void testCreateSpaceWithInvalidSpaceName() throws RuntimeException {
    Space space = new Space();
    String spaceDisplayName = "%zzz:^!/<>😁";
    space.setDisplayName(spaceDisplayName);
    String creator = "root";
    String shortName = "zzz";
    space.setDisplayName(spaceDisplayName);
    space.setPrettyName(shortName);
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space ");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space = spaceService.createSpace(space, creator);
    assertEquals(spaceDisplayName, space.getDisplayName());
    assertFalse(space.getPrettyName().contains("%"));
    assertFalse(space.getPrettyName().contains("^"));
    assertFalse(space.getPrettyName().contains(":"));
    assertFalse(space.getPrettyName().contains("!"));
    assertFalse(space.getPrettyName().contains("/"));
    assertFalse(space.getPrettyName().contains("<"));
    assertFalse(space.getPrettyName().contains(">"));
    assertFalse(space.getPrettyName().contains("😁"));
  }

  /**
   * Test {@link SpaceService#createSpace(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testCreateSpaceNoUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + spaceDisplayName, spaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());
    assertNotNull(savedSpace.getAvatarUrl());
  }

  /**
   * Test {@link SpaceService#renameSpace(Space, String)}
   *
   * @throws Exception
   * @since 1.2.8
   */
  public void testRenameSpace() throws Exception {
    Space space = this.getSpaceInstance(0);

    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);
    tearDownUserList.add(identity);

    String newDisplayName = "new display name";

    spaceService.renameSpace(space, newDisplayName);

    Space got = spaceService.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());

    {
      newDisplayName = "new display name with super admin";

      //
      spaceService.renameSpace(space, newDisplayName, root.getRemoteId());

      got = spaceService.getSpaceById(space.getId());
      assertEquals(newDisplayName, got.getDisplayName());
    }

    assertThrows(SpaceException.class,
                 () -> spaceService.renameSpace(space,
                                                "new display name with normal admin",
                                                mary.getRemoteId()));

    assertNotNull(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()));

    String oldPrettyName = space.getPrettyName();
    {
      newDisplayName = "new display name with null remoteId";

      //
      spaceService.renameSpace(space, newDisplayName);

      got = spaceService.getSpaceById(space.getId());
      assertEquals(newDisplayName, got.getDisplayName());
    }

    assertNotNull(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()));
    assertNull(identityManager.getOrCreateSpaceIdentity(oldPrettyName));
  }

  /**
   * Test {@link SpaceService#deleteSpace(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testDeleteSpace() throws Exception {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + spaceDisplayName, spaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());
    assertNotNull("the group " +groupId + " must exist", organizationService.getGroupHandler().findGroupById(groupId));
    spaceService.deleteSpace(space);
    savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNull("savedSpace must be null", savedSpace);
    assertNull("the group " +groupId + " must be deleted after space deletion ", organizationService.getGroupHandler().findGroupById(groupId));
  }


  public void testDeleteSpaceWithoutDelitingGroup() throws Exception {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + spaceDisplayName, spaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());
    assertNotNull("the group " +groupId + " must exist", organizationService.getGroupHandler().findGroupById(groupId));
    spaceService.deleteSpace(space, false);
    savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNull("savedSpace must be null", savedSpace);
    assertNotNull("the group " +groupId + " must exist after space deletion", organizationService.getGroupHandler().findGroupById(groupId));
    Group group = organizationService.getGroupHandler().findGroupById(groupId);
    organizationService.getGroupHandler().removeGroup(group, true);
  }

  public void testUpdateSpacePermissions() throws Exception {
    Space space = this.getSpaceInstance(0);
    try {
      spaceService.updateSpaceBanner(space, "raul");
      fail("Space member shouldn't be able to update space banner");
    } catch (Exception e) {
      // Expected
    }
    try {
      spaceService.updateSpaceAvatar(space, "raul");
      fail("Space member shouldn't be able to update space avatar");
    } catch (Exception e) {
      // Expected
    }
  }

  /**
   * Test {@link SpaceService#updateSpace(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testUpdateSpace() throws Exception {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + spaceDisplayName, spaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());

    String updateSpaceDisplayName = "update new space display name";
    space.setDisplayName(updateSpaceDisplayName);
    space.setPrettyName(space.getDisplayName());
    spaceService.updateSpace(space);
    savedSpace = spaceService.getSpaceByDisplayName(updateSpaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + updateSpaceDisplayName,
                 updateSpaceDisplayName,
                 savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());
  }

  /**
   * Test {@link SpaceService#addPendingUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAddPendingUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1,
                 space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#removePendingUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRemovePendingUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1,
                 space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));

    spaceService.removePendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount,
                 pendingUsersCount,
                 space.getPendingUsers().length);
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#isPendingUser(Space, String)}
   *
   * @throws Exception@since 1.2.0-GA
   * @since 1.2.0-GA
   */
  public void testIsPendingUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertTrue("spaceService.isPendingUser(savedSpace, \"jame\") must return true",
               spaceService.isPendingUser(savedSpace, "jame"));
    assertTrue("spaceService.isPendingUser(savedSpace, \"paul\") must return true",
               spaceService.isPendingUser(savedSpace, "paul"));
    assertTrue("spaceService.isPendingUser(savedSpace, \"hacker\") must return true",
               spaceService.isPendingUser(savedSpace, "hacker"));
    assertFalse("spaceService.isPendingUser(savedSpace, \"newpendinguser\") must return false",
                spaceService.isPendingUser(savedSpace, "newpendinguser"));
  }

  /**
   * Test {@link SpaceService#addInvitedUser(Space, String)}
   *
   * @throws Exception
   */
  public void testAddInvitedUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1,
                 savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
               ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#removeInvitedUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRemoveInvitedUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1,
                 savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
               ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.removeInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount,
                 invitedUsersCount,
                 savedSpace.getInvitedUsers().length);
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#isInvitedUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsInvitedUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertTrue("spaceService.isInvitedUser(savedSpace, \"register1\") must return true",
               spaceService.isInvitedUser(savedSpace, "register1"));
    assertTrue("spaceService.isInvitedUser(savedSpace, \"mary\") must return true",
               spaceService.isInvitedUser(savedSpace, "mary"));
    assertFalse("spaceService.isInvitedUser(savedSpace, \"hacker\") must return false",
                spaceService.isInvitedUser(savedSpace, "hacker"));
    assertFalse("spaceService.isInvitedUser(savedSpace, \"nobody\") must return false",
                spaceService.isInvitedUser(savedSpace, "nobody"));
  }

  /**
   * Test {@link SpaceService#setManager(Space, String, boolean)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testSetManager() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { "demo", "tom" };
    String[] members = new String[] { "demo", "tom" , "raul", "ghost", "dragon" };
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);

    // Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int managers = savedSpace.getManagers().length;
    spaceService.setManager(savedSpace, "demo", true);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers, managers, savedSpace.getManagers().length);

    spaceService.setManager(savedSpace, "john", true);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers + 1, managers + 1, savedSpace.getManagers().length);

    spaceService.setManager(savedSpace, "demo", false);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers, managers, savedSpace.getManagers().length);
  }

  /**
   * Test {@link SpaceService#isManager(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsManager() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertTrue("spaceService.isManager(savedSpace, \"demo\") must return true", spaceService.isManager(savedSpace, "demo"));
    assertTrue("spaceService.isManager(savedSpace, \"tom\") must return true", spaceService.isManager(savedSpace, "tom"));
    assertFalse("spaceService.isManager(savedSpace, \"mary\") must return false", spaceService.isManager(savedSpace, "mary"));
    assertFalse("spaceService.isManager(savedSpace, \"john\") must return false", spaceService.isManager(savedSpace, "john"));
  }
  
  /**
   * Test {@link SpaceService#setRedactor(Space, String, boolean)}
   *
   * @throws Exception
   */
  public void testSetRedactor() throws Exception {
    Space space = new Space();
    space.setDisplayName("space1");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space1");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space1");
    space.setUrl(space.getPrettyName());
    space = spaceService.createSpace(space, "root");
    String[] members = new String[] { "ghost", "john" };
    space.setMembers(members);
    MembershipType redactorMembershipType = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
    redactorMembershipType.setName("redactor");
    spaceService.addRedactor(space, "ghost");
    spaceService.addRedactor(space, "john");
    assertEquals(2, space.getRedactors().length);
    spaceService.removeRedactor(space, "john");
    assertEquals(1, space.getRedactors().length);
    assertTrue(spaceService.isRedactor(space, "ghost"));
    assertFalse(spaceService.isRedactor(space, "john"));
  }
  
  /**
   * Test {@link SpaceService#addPublisher(Space, String, boolean)}
   * Test {@link SpaceService#removePublisher(Space, String)}
   * Test {@link SpaceService#isPublisher(Space, String)}
   *
   * @throws Exception
   */
  public void testSetPublisher() throws Exception {
    Space space = new Space();
    space.setDisplayName("space1");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space1");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space1");
    space.setUrl(space.getPrettyName());
    space = spaceService.createSpace(space, "root");
    String[] members = new String[] { "ghost", "john" };
    space.setMembers(members);
    MembershipType publisherMembershipType = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
    publisherMembershipType.setName("publisher");
    spaceService.addPublisher(space, "ghost");
    spaceService.addPublisher(space, "john");
    assertEquals(2, space.getPublishers().length);
    spaceService.removePublisher(space, "john");
    assertEquals(1, space.getPublishers().length);
    assertTrue(spaceService.isPublisher(space, "ghost"));
    assertFalse(spaceService.isPublisher(space, "john"));
  }

  /**
   * Test {@link SpaceService#isOnlyManager(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsOnlyManager() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertFalse("spaceService.isOnlyManager(savedSpace, \"tom\") must return false",
                spaceService.isOnlyManager(savedSpace, "tom"));
    assertFalse("spaceService.isOnlyManager(savedSpace, \"demo\") must return false",
                spaceService.isOnlyManager(savedSpace, "demo"));

    savedSpace.setManagers(new String[] { "demo" });
    spaceService.updateSpace(savedSpace);
    assertTrue("spaceService.isOnlyManager(savedSpace, \"demo\") must return true",
               spaceService.isOnlyManager(savedSpace, "demo"));
    assertFalse("spaceService.isOnlyManager(savedSpace, \"tom\") must return false",
                spaceService.isOnlyManager(savedSpace, "tom"));

    savedSpace.setManagers(new String[] { "tom" });
    spaceService.updateSpace(savedSpace);
    assertFalse("spaceService.isOnlyManager(savedSpace, \"demo\") must return false",
                spaceService.isOnlyManager(savedSpace, "demo"));
    assertTrue("spaceService.isOnlyManager(savedSpace, \"tom\") must return true", spaceService.isOnlyManager(savedSpace, "tom"));
  }

  /**
   * Test {@link SpaceService#addMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAddMember() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { "demo" };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.addMember(savedSpace, "root");
    spaceService.addMember(savedSpace, "mary");
    spaceService.addMember(savedSpace, "john");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);
  }

  /**
   * Test {@link SpaceService#addMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAddMemberSpecialCharacter() throws Exception {
    String reg = "^\\p{L}[\\p{L}\\d\\s._,-]+$";
    Pattern pattern = Pattern.compile(reg);
    assertTrue(pattern.matcher("user-new.1").matches());
    assertTrue(pattern.matcher("user.new").matches());
    assertTrue(pattern.matcher("user-new").matches());

    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { "demo" };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    User user = organizationService.getUserHandler().createUserInstance("user-new.1");
    organizationService.getUserHandler().createUser(user, false);
    user = organizationService.getUserHandler().createUserInstance("user.new");
    organizationService.getUserHandler().createUser(user, false);
    user = organizationService.getUserHandler().createUserInstance("user-new");
    organizationService.getUserHandler().createUser(user, false);

    spaceService.addMember(savedSpace, "user-new.1");
    spaceService.addMember(savedSpace, "user.new");
    spaceService.addMember(savedSpace, "user-new");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals(4, savedSpace.getMembers().length);
  }

  /**
   * Test {@link SpaceService#removeMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRemoveMember() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { "demo" };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.addMember(savedSpace, "root");
    spaceService.addMember(savedSpace, "mary");
    spaceService.addMember(savedSpace, "john");
    spaceService.setManager(savedSpace, "john", true);
    spaceService.addRedactor(savedSpace, "john");

    GroupHandler groupHandler = organizationService.getGroupHandler();
    UserHandler userHandler = organizationService.getUserHandler();
    MembershipType mbShipTypeMember = organizationService.getMembershipTypeHandler()
                                                         .findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    User user = userHandler.findUserByName("john");
    organizationService.getMembershipHandler()
                       .linkMembership(user, groupHandler.findGroupById(space.getGroupId()), mbShipTypeMember, true);

    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);

    spaceService.removeMember(savedSpace, "root");
    spaceService.removeMember(savedSpace, "mary");
    spaceService.removeMember(savedSpace, "john");
    Membership any = organizationService.getMembershipHandler()
                                        .findMembershipByUserGroupAndType("john",
                                                                          space.getGroupId(),
                                                                          MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    assertNull(any);
    assertFalse(spaceService.isManager(savedSpace, "john"));
    assertFalse(spaceService.isRedactor(savedSpace, "john"));
    assertFalse(spaceService.isMember(savedSpace, "john"));

    assertEquals("savedSpace.getMembers().length must return 1", 1, savedSpace.getMembers().length);
  }

  public void testDeleteSpaceWithBoundMember() throws Exception {
    String userToBind = "paul";

    int number = 17;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    space.setMembers(new String[] { "john", "mary" });
    space = this.createSpaceNonInitApps(space, "john", null);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    spaceService.addMember(savedSpace, userToBind);

    Group groupToBind = organizationService.getGroupHandler().createGroupInstance();
    String groupName = "groupForSpaceToBind";
    String groupId = "/" + groupName;
    groupToBind.setId(groupId);
    groupToBind.setGroupName(groupName);
    groupToBind.setLabel(groupName);
    organizationService.getGroupHandler().addChild(null, groupToBind, true);

    MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(SpaceUtils.MEMBER);
    User user = organizationService.getUserHandler().findUserByName(userToBind);
    organizationService.getMembershipHandler().linkMembership(user, groupToBind, membershipType, true);
    Membership membership = organizationService.getMembershipHandler()
                                               .findMembershipByUserGroupAndType(user.getUserName(),
                                                                                 groupToBind.getId(),
                                                                                 membershipType.getName());
    assertNotNull(membership);

    restartTransaction();
    GroupSpaceBindingService groupSpaceBindingService = getContainer().getComponentInstanceOfType(GroupSpaceBindingService.class);

    GroupSpaceBinding groupSpaceBinding = new GroupSpaceBinding(space.getId(), groupToBind.getId());
    groupSpaceBindingService.saveGroupSpaceBinding(groupSpaceBinding);
    restartTransaction();

    List<GroupSpaceBinding> groupSpaceBindings = groupSpaceBindingService.findGroupSpaceBindingsBySpace(savedSpace.getId());
    assertNotNull(groupSpaceBindings);
    assertEquals(1, groupSpaceBindings.size());

    assertTrue(spaceService.isMember(savedSpace, userToBind));

    spaceService.deleteSpace(savedSpace);
    restartTransaction();

    assertNull(spaceService.getSpaceById(savedSpace.getId()));

    membership = organizationService.getMembershipHandler()
                                    .findMembershipByUserGroupAndType(user.getUserName(),
                                                                      groupToBind.getId(),
                                                                      membershipType.getName());
    assertNotNull(membership);

    membership = organizationService.getMembershipHandler()
                                    .findMembershipByUserGroupAndType(user.getUserName(),
                                                                      savedSpace.getGroupId(),
                                                                      SpaceUtils.MEMBER);
    assertNull(membership);
  }

  public void testRemoveBoundMember() throws Exception {
    String userToBind = "paul";

    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    space.setMembers(new String[] { "john", "mary" });
    space = this.createSpaceNonInitApps(space, "john", null);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    spaceService.addMember(savedSpace, userToBind);

    Group groupToBind = organizationService.getGroupHandler().createGroupInstance();
    String groupName = "groupForSpaceToBind";
    String groupId = "/" + groupName;
    groupToBind.setId(groupId);
    groupToBind.setGroupName(groupName);
    groupToBind.setLabel(groupName);
    organizationService.getGroupHandler().addChild(null, groupToBind, true);

    MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(SpaceUtils.MEMBER);
    User user = organizationService.getUserHandler().findUserByName(userToBind);
    organizationService.getMembershipHandler().linkMembership(user, groupToBind, membershipType, true);
    Membership membership = organizationService.getMembershipHandler()
                                               .findMembershipByUserGroupAndType(user.getUserName(),
                                                                                 groupToBind.getId(),
                                                                                 membershipType.getName());
    assertNotNull(membership);

    restartTransaction();
    GroupSpaceBindingService groupSpaceBindingService = getContainer().getComponentInstanceOfType(GroupSpaceBindingService.class);

    GroupSpaceBinding groupSpaceBinding = new GroupSpaceBinding(space.getId(), groupToBind.getId());
    groupSpaceBindingService.saveGroupSpaceBinding(groupSpaceBinding);
    restartTransaction();

    List<GroupSpaceBinding> groupSpaceBindings = groupSpaceBindingService.findGroupSpaceBindingsBySpace(savedSpace.getId());
    assertNotNull(groupSpaceBindings);
    assertEquals(1, groupSpaceBindings.size());

    groupSpaceBinding = groupSpaceBindings.get(0);
    groupSpaceBindingService.bindUsersFromGroupSpaceBinding(groupSpaceBinding);
    restartTransaction();

    assertThrows(IllegalStateException.class, () -> spaceService.removeMember(savedSpace, userToBind));

    assertThrows(IllegalStateException.class,
                 () -> organizationService.getMembershipHandler()
                                          .removeMembership(String.format("%s:%s:%s",
                                                                          SpaceUtils.MEMBER,
                                                                          userToBind,
                                                                          savedSpace.getGroupId()),
                                                            true));

    membership = organizationService.getMembershipHandler()
                                    .findMembershipByUserGroupAndType(user.getUserName(),
                                                                      groupToBind.getId(),
                                                                      membershipType.getName());
    assertNotNull(membership);

    membership = organizationService.getMembershipHandler()
                                    .findMembershipByUserGroupAndType(user.getUserName(),
                                                                      savedSpace.getGroupId(),
                                                                      SpaceUtils.MEMBER);
    assertNotNull(membership);

    assertTrue(spaceService.isMember(savedSpace, userToBind));

    groupSpaceBindingService.deleteAllSpaceBindingsBySpace(savedSpace.getId());
    restartTransaction();

    spaceService.removeMember(savedSpace, userToBind);

    assertFalse(spaceService.isMember(savedSpace, userToBind));
    membership = organizationService.getMembershipHandler()
                                    .findMembershipByUserGroupAndType(user.getUserName(),
                                                                      groupToBind.getId(),
                                                                      membershipType.getName());
    assertNotNull(membership);

    membership = organizationService.getMembershipHandler()
                                    .findMembershipByUserGroupAndType(user.getUserName(),
                                                                      savedSpace.getGroupId(),
                                                                      SpaceUtils.MEMBER);
    assertNull(membership);
  }

  /**
   * Test {@link SpaceService#getMemberSpacesIds(String, int, int)}
   *
   * @throws Exception
   * @since 6.4.0-GA
   */
  public void testGetMemberSpaces() throws Exception {
    Space[] listSpace = new Space[10];
    for (int i = 0; i < listSpace.length; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
    }

    restartTransaction();

    String raulUsername = "raul";
    String jameUsername = "jame";

    List<String> spaceIds = spaceService.getMemberSpacesIds(raulUsername, 0, -1);
    assertNotNull(spaceIds);
    assertEquals(listSpace.length, spaceIds.size());

    spaceIds = spaceService.getMemberSpacesIds(raulUsername, 0, listSpace.length);
    assertNotNull(spaceIds);
    assertEquals(listSpace.length, spaceIds.size());

    spaceIds = spaceService.getMemberSpacesIds(raulUsername, 0, listSpace.length / 2);
    assertNotNull(spaceIds);
    assertEquals(listSpace.length / 2, spaceIds.size());

    spaceIds = spaceService.getMemberSpacesIds(raulUsername, listSpace.length / 2, listSpace.length / 2);
    assertNotNull(spaceIds);
    assertEquals(listSpace.length / 2, spaceIds.size());

    Space space0 = spaceService.getSpaceById(spaceIds.get(0));
    assertNotNull(space0);

    spaceService.removeMember(space0, raulUsername);
    restartTransaction();

    spaceIds = spaceService.getMemberSpacesIds(raulUsername, 0, -1);
    assertNotNull(spaceIds);
    assertEquals(listSpace.length - 1, spaceIds.size());

    spaceIds = spaceService.getMemberSpacesIds(jameUsername, 0, -1);
    assertNotNull(spaceIds);
    assertEquals(0, spaceIds.size());
  }

  /**
   * Test {@link SpaceService#isMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsMember() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    assertTrue("spaceService.isMember(savedSpace, \"raul\") must return true", spaceService.isMember(savedSpace, "raul"));
    assertTrue("spaceService.isMember(savedSpace, \"ghost\") must return true", spaceService.isMember(savedSpace, "ghost"));
    assertTrue("spaceService.isMember(savedSpace, \"dragon\") must return true", spaceService.isMember(savedSpace, "dragon"));
    assertFalse("spaceService.isMember(savedSpace, \"stranger\") must return true",
                spaceService.isMember(savedSpace, "stranger"));
  }

  /**
   * Test {@link SpaceService#activateApplication(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testActivateApplication() throws Exception {
    startSessionAs("root");
    String spaceName = "testSpace";
    String creator = "john";
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setManagers(new String[] { creator });
    space.setMembers(new String[] { creator });
    space = spaceService.createSpace(space, "root");
    tearDownSpaceList.add(space);

    assertTrue(space.getApp().contains("DashboardPortlet"));
    spaceService.removeApplication(space, "DashboardPortlet", "Dashboard");
    assertFalse(space.getApp().contains("DashboardPortlet"));
    spaceService.activateApplication(space, "DashboardPortlet");
    assertTrue(space.getApp().contains("DashboardPortlet"));

    NavigationContext navContext = SpaceUtils.getGroupNavigationContext(space.getGroupId());
    NodeContext<NodeContext<?>> homeNodeCtx = SpaceUtils.getHomeNodeWithChildren(navContext, space.getUrl());
    boolean found = homeNodeCtx.getNodes()
                               .stream()
                               .filter(node -> "dashboard".equals(node.getName()))
                               .findAny()
                               .isPresent();
    assertTrue(found);
  }

  public void testRequestJoin() throws Exception {
    Space space = this.getSpaceInstance(0);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1,
                 space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#removePendingUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRevokeRequestJoin() throws Exception {
    Space space = this.getSpaceInstance(0);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1,
                 space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));

    spaceService.removePendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount,
                 pendingUsersCount,
                 space.getPendingUsers().length);
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#addInvitedUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testInviteMember() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1,
                 savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
               ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#removeInvitedUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRevokeInvitation() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1,
                 savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
               ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.removeInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount,
                 invitedUsersCount,
                 savedSpace.getInvitedUsers().length);
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#addMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAcceptInvitation() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { "demo" };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.addMember(savedSpace, "root");
    spaceService.addMember(savedSpace, "mary");
    spaceService.addMember(savedSpace, "john");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);
    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
  }

  /**
   * Test {@link SpaceService#removeInvitedUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testDenyInvitation() throws Exception {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.removeInvitedUser(savedSpace, "new member 1");
    spaceService.removeInvitedUser(savedSpace, "new member 2");
    spaceService.removeInvitedUser(savedSpace, "new member 3");
    assertEquals("savedSpace.getMembers().length must return 2", 2, savedSpace.getInvitedUsers().length);

    spaceService.removeInvitedUser(savedSpace, "raul");
    spaceService.removeInvitedUser(savedSpace, "ghost");
    spaceService.removeInvitedUser(savedSpace, "dragon");
    assertEquals("savedSpace.getMembers().length must return 2", 2, savedSpace.getInvitedUsers().length);

    spaceService.removeInvitedUser(savedSpace, "register1");
    spaceService.removeInvitedUser(savedSpace, "mary");
    assertEquals("savedSpace.getMembers().length must return 0", 0, savedSpace.getInvitedUsers().length);
  }

  /**
   * Test {@link SpaceService#addMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testValidateRequest() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { "demo" };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.addMember(savedSpace, "root");
    spaceService.addMember(savedSpace, "mary");
    spaceService.addMember(savedSpace, "john");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);
  }

  /**
   * Test
   * {@link SpaceStorage#getVisibleSpaces(java.lang.String, org.exoplatform.social.core.space.SpaceFilter)(String)}
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

      spaceService.createSpace(listSpace[i]);
    }

    // visible with remoteId = 'demo' return 10 spaces
    Space[] visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess("demo", null).load(0, countSpace * 2);
    assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
    assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.length);
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

      spaceService.createSpace(listSpace[i]);
    }

    // visible with remoteId = 'demo' return 10 spaces
    {
      Space[] visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess("demo", null).load(0, countSpace * 2);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.length);
    }

    // visible with remoteId = 'mary' return 6 spaces: can see
    {
      int registrationCloseSpaceCount = 6;
      Space[] visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess("mary", null).load(0, registrationCloseSpaceCount * 2);
      assertNotNull("registrationCloseSpaces must not be  null", visibleAllSpaces);
      assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount,
                   registrationCloseSpaceCount,
                   visibleAllSpaces.length);
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
        listSpace[i] =
                     this.getSpaceInstanceInvitedMember(i,
                                                        Space.PRIVATE,
                                                        Space.CLOSED,
                                                        new String[] { "mary", "hacker" },
                                                        "demo");
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSED, "demo");

      spaceService.createSpace(listSpace[i]);
    }

    // visible with remoteId = 'demo' return 10 spaces
    {
      Space[] visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess("demo", null).load(0, countSpace * 2);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.length);
    }

    // visible with invited = 'mary' return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      Space[] invitedSpaces1 = spaceService.getVisibleSpacesWithListAccess("mary", null).load(0, invitedSpaceCount1 * 2);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.length);
    }

    // visible with invited = 'hacker' return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      Space[] invitedSpaces1 = spaceService.getVisibleSpacesWithListAccess("hacker", null).load(0, invitedSpaceCount1 * 2);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.length);
    }

    // visible with invited = 'paul' return 6 spaces
    {
      int invitedSpaceCount2 = 6;
      Space[] invitedSpaces2 = spaceService.getVisibleSpacesWithListAccess("paul", null).load(0, invitedSpaceCount2 * 2);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces2);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount2, invitedSpaceCount2, invitedSpaces2.length);
    }
  }

  public void testGetLastSpaces() throws Exception {
    populateData();
    createMoreSpace("Space2");
    List<Space> lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    Space sp1 = lastSpaces.get(0);
    lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    assertEquals(sp1, lastSpaces.get(0));
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(2, lastSpaces.size());
    assertEquals(sp1, lastSpaces.get(0));
    Space newSp1 = createMoreSpace("newSp1");
    lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    assertEquals(newSp1, lastSpaces.get(0));
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(3, lastSpaces.size());
    assertEquals(newSp1, lastSpaces.get(0));
    Space newSp2 = createMoreSpace("newSp2");
    lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    assertEquals(newSp2, lastSpaces.get(0));
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(4, lastSpaces.size());
    assertEquals(newSp2, lastSpaces.get(0));
    assertEquals(newSp1, lastSpaces.get(1));
    spaceService.deleteSpace(newSp1);
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(3, lastSpaces.size());
    assertEquals(newSp2, lastSpaces.get(0));
    assertFalse(newSp1.equals(lastSpaces.get(1)));
    spaceService.deleteSpace(newSp2);
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(2, lastSpaces.size());
    assertEquals(sp1, lastSpaces.get(0));
  }

  public void testInviteSuperManager() throws Exception {
    // Create a super manager user
    String username = "ali";
    User superManager = organizationService.getUserHandler().createUserInstance(username);
    organizationService.getUserHandler().createUser(superManager, false);
    Identity superManagerIdentity = identityManager.getOrCreateUserIdentity(username);
    assertFalse(spaceService.isSuperManager(username));
    // Create Super managers group
    Group group = organizationService.getGroupHandler().createGroupInstance();
    group.setGroupName("space-managers");
    organizationService.getGroupHandler().addChild(null, group, true);
    MembershipType msType = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
    msType.setName("test-ms");
    organizationService.getMembershipTypeHandler().createMembershipType(msType, true);
    //Add user to super managers
    organizationService.getMembershipHandler().linkMembership(superManager, group, msType, true);
    // Register group as super administrators
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(new MembershipEntry("/space-managers",
                                                                                                       "test-ms")));
    assertTrue(spaceService.isSuperManager(username));

    Space space = createSpace("spacename1", "root");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.OPEN);
    spaceService.inviteIdentities(space, Collections.singletonList(superManagerIdentity));
    assertTrue(spaceService.isInvitedUser(space, username));
  }

  public void testExternalSpaceInvitations() {
    spaceService.saveSpaceExternalInvitation("5", "external@external.com","test");
    spaceService.saveSpaceExternalInvitation("5", "external1@external1.com","test");
    spaceService.saveSpaceExternalInvitation("6", "external@external.com","test");
    spaceService.saveSpaceExternalInvitation("7", "external2@external2.com","test");
    spaceService.saveSpaceExternalInvitation("7", "external3@external3.com","test");

    List<SpaceExternalInvitation> spaceExternalInvitationEntities = spaceService.findSpaceExternalInvitationsBySpaceId("5");
    assertNotNull(spaceExternalInvitationEntities);
    assertEquals(2, spaceExternalInvitationEntities.size());

    List<String> spaceIds = spaceService.findExternalInvitationsSpacesByEmail("external@external.com");
    assertNotNull(spaceIds);
    assertEquals(2, spaceExternalInvitationEntities.size());

    spaceService.deleteExternalUserInvitations("external@external.com");

    List<SpaceExternalInvitation> spaceExternalInvitationEntities1 = spaceService.findSpaceExternalInvitationsBySpaceId("5");
    assertNotNull(spaceExternalInvitationEntities);
    assertEquals(1, spaceExternalInvitationEntities1.size());

    List<String> spaceIds2 = spaceService.findExternalInvitationsSpacesByEmail("external2@external2.com");
    assertEquals(1, spaceIds2.size());

  }

  public void testDeleteSpaceExternalInvitation() {
    spaceService.saveSpaceExternalInvitation("8", "external@external.com","token");
    spaceService.saveSpaceExternalInvitation("8", "external1@external1.com","token1");
    spaceService.saveSpaceExternalInvitation("8", "external2@external2.com","token2");

    List<SpaceExternalInvitation> spaceExternalInvitationEntities = spaceService.findSpaceExternalInvitationsBySpaceId("8");
    assertNotNull(spaceExternalInvitationEntities);
    assertEquals(3, spaceExternalInvitationEntities.size());

    spaceService.deleteSpaceExternalInvitation(spaceExternalInvitationEntities.get(0).getInvitationId().toString());

    List<SpaceExternalInvitation> spaceExternalInvitationEntities1 = spaceService.findSpaceExternalInvitationsBySpaceId("8");
    assertNotNull(spaceExternalInvitationEntities1);
    assertEquals(2, spaceExternalInvitationEntities1.size());

    spaceService.deleteSpaceExternalInvitation(spaceExternalInvitationEntities1.get(0).getInvitationId().toString());
    spaceService.deleteSpaceExternalInvitation(spaceExternalInvitationEntities1.get(1).getInvitationId().toString());

    List<SpaceExternalInvitation> spaceExternalInvitationEntities2 = spaceService.findSpaceExternalInvitationsBySpaceId("8");
    assertNotNull(spaceExternalInvitationEntities2);
    assertEquals(0, spaceExternalInvitationEntities2.size());

  }

  public void testIsSuperManager() throws Exception {
    Space space = createSpace("spacename1", "root");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.OPEN);
    spaceService.updateSpace(space);

    space = createSpace("spacename2", "root");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.CLOSED);
    spaceService.updateSpace(space);

    space = createSpace("spacename3", "root");
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    spaceService.updateSpace(space);

    space = createSpace("spacename4", "root");
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.CLOSED);
    spaceService.updateSpace(space);

    space = createSpace("spacename5", "root");
    space.setVisibility(Space.HIDDEN);
    space.setRegistration(Space.OPEN);
    spaceService.updateSpace(space);

    space = createSpace("spacename6", "root");
    space.setVisibility(Space.HIDDEN);
    space.setRegistration(Space.CLOSED);
    spaceService.updateSpace(space);

    User user = organizationService.getUserHandler().createUserInstance("user-space-admin");
    organizationService.getUserHandler().createUser(user, false);
    Group group = organizationService.getGroupHandler().createGroupInstance();
    group.setGroupName("testgroup");
    organizationService.getGroupHandler().addChild(null, group, true);
    MembershipType mstype = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
    mstype.setName("mstypetest");
    organizationService.getMembershipTypeHandler().createMembershipType(mstype, true);

    organizationService.getMembershipHandler().linkMembership(user, group, mstype, true);

    String userName = user.getUserName();

    assertEquals(6, spaceService.getAllSpacesWithListAccess().getSize());
    assertFalse(spaceService.isSuperManager(userName));
    spacesAdministrationService.updateSpacesAdministratorsMemberships(Arrays.asList(new MembershipEntry("/testgroup",
                                                                                                                "mstypetest")));
    assertTrue(spaceService.isSuperManager(userName));
  }

  private Space populateData() throws Exception {
    String spaceDisplayName = "Space1";
    Space space1 = new Space();
    space1.setApp("Calendar;FileSharing");
    space1.setDisplayName(spaceDisplayName);
    space1.setPrettyName(space1.getDisplayName());
    String shortName = Utils.cleanString(spaceDisplayName);
    space1.setGroupId("/spaces/" + shortName);
    space1.setUrl(shortName);
    space1.setRegistration("validation");
    space1.setDescription("This is my first space for testing");
    space1.setType("classic");
    space1.setVisibility("public");
    space1.setPriority("2");
    String[] manager = new String[] { "root" };
    String[] members = new String[] { "demo", "john", "mary", "tom", "harry" };
    space1.setManagers(manager);
    space1.setMembers(members);

    spaceService.createSpace(space1);
    tearDownSpaceList.add(space1);
    return space1;
  }

  private Space createSpace(String spaceName, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String[] managers = new String[] { creator };
    String[] members = new String[] { creator };
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.createSpace(space);
    tearDownSpaceList.add(space);
    return space;
  }

  /**
   * Gets an instance of the space.
   *
   * @param number
   * @return
   * @throws Exception
   * @since 1.2.0-GA
   */
  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    String[] managers = new String[] { "demo", "tom" };
    String[] members = new String[] { "demo", "raul", "ghost", "dragon" };
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    this.spaceService.createSpace(space, "root");
    tearDownSpaceList.add(space);
    return space;
  }

  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  @Override
  public Space getSpaceInstance(int number, String visible, String registration, String manager, String... members) {
    Space space = super.getSpaceInstance(number, visible, registration, manager, members);
    tearDownSpaceList.add(space);
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
    space.setApp("app");
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(registration);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(visible);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] { manager };
    // String[] invitedUsers = new String[] {invitedMember};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedMember);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    tearDownSpaceList.add(space);
    return space;
  }

  private Space createMoreSpace(String spaceName) throws Exception {
    Space space2 = new Space();
    space2.setApp("Contact,Forum");
    space2.setDisplayName(spaceName);
    space2.setPrettyName(space2.getDisplayName());
    String shortName = Utils.cleanString(spaceName);
    space2.setGroupId("/spaces/" + shortName);
    space2.setUrl(shortName);
    space2.setRegistration("open");
    space2.setDescription("This is my second space for testing");
    space2.setType("classic");
    space2.setVisibility("public");
    space2.setPriority("2");

    spaceService.createSpace(space2);
    tearDownSpaceList.add(space2);

    // sleep 1ms to be sure that 2 spaces created in a row
    // have not the same createdDate
    Thread.sleep(1);
    return space2;
  }

  public void testSpaceContainsExternalMembers() {
    checkExternalUserMemberships();
    externalUser.getProfile().setProperty("external", "true");
    identityStorage.saveIdentity(externalUser);
    Space space = getSpaceInstance(10);
    assertFalse(spaceService.isSpaceContainsExternals(Long.valueOf(space.getId())));

    spaceService.addMember(space, EXTERNAL_USER);
    assertTrue(spaceService.isSpaceContainsExternals(Long.valueOf(space.getId())));
  }

  public void testGetCommonSpaces() throws Exception {

    getSpaceInstance(11);
    Space space1 = getSpaceInstance(12);
    Space space2 = getSpaceInstance(13);
    Space space3 = getSpaceInstance(14);
    Space space4 = getSpaceInstance(15);
    Space space5 = getSpaceInstance(16);

    User otherUser = organizationService.getUserHandler().createUserInstance("otherUser");
    organizationService.getUserHandler().createUser(otherUser, false);

    spaceService.addMember(space1,"otherUser");
    spaceService.addMember(space2,"otherUser");
    spaceService.addMember(space3,"otherUser");
    spaceService.addMember(space4,"otherUser");
    spaceService.addMember(space5,"otherUser");

    ListAccess<Space> resultListCommonSpacesAccessList1 = spaceService.getCommonSpaces("root","otherUser");
    assertEquals(5,resultListCommonSpacesAccessList1.getSize());
    Space[] spaceArray = resultListCommonSpacesAccessList1.load(0,2);
    assertEquals(2,spaceArray.length);
    Space testSpace1 = spaceArray[0];
    assertEquals(space1,testSpace1);
    Space testSpace2 = spaceArray[1];
    assertEquals(space2,testSpace2);

    spaceArray = resultListCommonSpacesAccessList1.load(2,2);
    assertEquals(2,spaceArray.length);
    Space testSpace3 = spaceArray[0];
    assertEquals(space3,testSpace3);
    Space testSpace4 = spaceArray[1];
    assertEquals(space4,testSpace4);

    spaceArray = resultListCommonSpacesAccessList1.load(4,2);
    assertEquals(1,spaceArray.length);
    Space testSpace5 = spaceArray[0];
    assertEquals(space5,testSpace5);

  }

  public void testRestoreSpacePageLayout() throws Exception {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity("root");
    ConversationState.setCurrent(new ConversationState(rootACLIdentity));

    Space space = new Space();
    space.setDisplayName("testSpaceHomeLayout");
    space.setDescription("Space Description for Testing");
    String shortName = Utils.cleanString(space.getDisplayName());
    space.setGroupId("/spaces/" + shortName);
    String[] managers = new String[] {
      rootACLIdentity.getUserId()
    };
    space.setManagers(managers);
    space.setPrettyName(space.getDisplayName());
    space.setPriority("3");
    space.setRegistration("validation");
    space.setTag("Space Tag for Testing");
    String template = "classic";
    space.setTemplate(template);
    space.setUrl(shortName);
    space.setVisibility("public");
    space = spaceService.createSpace(space, rootACLIdentity.getUserId());

    SpaceTemplateService spaceTemplateService = getContainer().getComponentInstanceOfType(SpaceTemplateService.class);
    String spaceTemplateName = space.getTemplate();
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(spaceTemplateName);
    assertNotNull(spaceTemplate);
    assertEquals(template, spaceTemplate.getName());

    NavigationService navService = getContainer().getComponentInstanceOfType(NavigationService.class);
    NavigationContext navContext = SpaceUtils.getGroupNavigationContext(space.getGroupId());
    assertNotNull(navContext);
    NodeContext<NodeContext<?>> parentNodeCtx = navService.loadNode(NodeModel.SELF_MODEL, navContext, Scope.CHILDREN, null);
    assertNotNull(parentNodeCtx);

    NodeContext<NodeContext<?>> homeNodeContext = parentNodeCtx.get(0);
    assertNotNull(homeNodeContext);

    PageKey homePageKey = homeNodeContext.getState().getPageRef();
    assertNotNull(homePageKey);

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    Page page = layoutService.getPage(homePageKey.format());
    assertNotNull(page);
    assertEquals(5, countPageApplications(page.getChildren(), 0));

    space.setTemplate("meeds");
    space = spaceService.updateSpace(space);

    String spaceId = space.getId();
    assertThrows(IllegalAccessException.class, () -> spaceService.restoreSpacePageLayout(spaceId, "home", new org.exoplatform.services.security.Identity("demo")));

    spaceService.restoreSpacePageLayout(spaceId, "home", rootACLIdentity);
    page = layoutService.getPage(homePageKey.format());
    assertNotNull(page);
    assertEquals(2, countPageApplications(page.getChildren(), 0));
  }

  @SneakyThrows
  public void testSaveSpacePublicSite() {
    Space space = getSpaceInstance(18);
    String spaceId = space.getId();

    assertThrows(ObjectNotFoundException.class,
                 () -> spaceService.saveSpacePublicSite("15587688", SpaceUtils.AUTHENTICATED, "demo"));
    assertThrows(IllegalAccessException.class,
                 () -> spaceService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "raul"));

    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "demo");
    space = spaceService.getSpaceById(space.getId());
    assertEquals(SpaceUtils.AUTHENTICATED, space.getPublicSiteVisibility());
    long publicSiteId = space.getPublicSiteId();
    assertTrue(publicSiteId > 0);

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    PortalConfig publicSitePortalConfig = layoutService.getPortalConfig(publicSiteId);
    assertNotNull(publicSitePortalConfig);
    assertEquals(space.getPrettyName(), publicSitePortalConfig.getName());
    assertEquals(space.getDisplayName(), publicSitePortalConfig.getLabel());
    assertEquals(2, publicSitePortalConfig.getAccessPermissions().length);
    assertEquals(new HashSet<String>(Arrays.asList("member:/platform/externals",
                                                   "member:/platform/users")),
                 new HashSet<String>(Arrays.asList(publicSitePortalConfig.getAccessPermissions())));
    assertEquals(SpaceUtils.MANAGER + ":" + space.getGroupId(), publicSitePortalConfig.getEditPermission());
    assertEquals(spaceId, publicSitePortalConfig.getProperty("SPACE_ID"));
    assertEquals("true", publicSitePortalConfig.getProperty("IS_SPACE_PUBLIC_SITE"));
  }

  @SneakyThrows
  public void testChangeSpacePublicSiteVisibility() {
    Space space = getSpaceInstance(18);
    String spaceId = space.getId();

    assertThrows(ObjectNotFoundException.class,
                 () -> spaceService.saveSpacePublicSite("15587688", SpaceUtils.AUTHENTICATED, "demo"));
    assertThrows(IllegalAccessException.class,
                 () -> spaceService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "raul"));

    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "demo");
    space = spaceService.getSpaceById(space.getId());
    assertEquals(SpaceUtils.AUTHENTICATED, space.getPublicSiteVisibility());
    long publicSiteId = space.getPublicSiteId();
    assertTrue(publicSiteId > 0);

    space.setVisibility(Space.HIDDEN);
    spaceService.updateSpace(space);
    space = spaceService.getSpaceById(space.getId());

    assertEquals(SpaceUtils.MEMBER, space.getPublicSiteVisibility());
    assertEquals(space.getPrettyName(), spaceService.getSpacePublicSiteName(space));

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    PortalConfig publicSitePortalConfig = layoutService.getPortalConfig(publicSiteId);
    assertNotNull(publicSitePortalConfig);
    assertEquals(space.getPrettyName(), publicSitePortalConfig.getName());
    assertEquals(space.getDisplayName(), publicSitePortalConfig.getLabel());
    assertFalse(publicSitePortalConfig.isDefaultSite());
    assertEquals(1, publicSitePortalConfig.getAccessPermissions().length);
    assertEquals(new HashSet<String>(Arrays.asList(SpaceUtils.MEMBER + ":" + space.getGroupId())),
                 new HashSet<String>(Arrays.asList(publicSitePortalConfig.getAccessPermissions())));
    assertEquals(SpaceUtils.MANAGER + ":" + space.getGroupId(), publicSitePortalConfig.getEditPermission());
  }

  @SneakyThrows
  public void testSpaceWithPublicSiteRemoved() {
    Space space = getSpaceInstance(19);
    String spaceId = space.getId();
    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "demo");
    space = spaceService.getSpaceById(space.getId());
    long publicSiteId = space.getPublicSiteId();
    assertTrue(publicSiteId > 0);

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    PortalConfig publicSitePortalConfig = layoutService.getPortalConfig(publicSiteId);
    assertNotNull(publicSitePortalConfig);

    spaceService.deleteSpace(space);
    assertNull(layoutService.getPortalConfig(publicSiteId));
  }

  @SneakyThrows
  public void testCanAccessSpacePublicSite() {
    checkExternalUserMemberships();

    Space space = getSpaceInstance(20);
    String spaceId = space.getId();

    assertFalse(spaceService.canAccessSpacePublicSite(null, "demo"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, "demo"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, "john"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.MANAGER, "demo");
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, "john"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "demo"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, "raul"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, "mary"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, "paul"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.MEMBER, "demo");
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, "john"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "demo"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "raul"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, "mary"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, "paul"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.INTERNAL, "demo");
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, "john"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "raul"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "mary"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "paul"));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, "demo");
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, "john"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "demo"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "raul"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "mary"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "paul"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceService.saveSpacePublicSite(spaceId, SpaceUtils.EVERYONE, "demo");
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, "root"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "john"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "demo"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "raul"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "mary"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, "paul"));
    assertTrue(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER));
    assertTrue(spaceService.canAccessSpacePublicSite(space, null));
    assertTrue(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));
  }

  private int countPageApplications(ArrayList<ModelObject> children, int size) {
    for (ModelObject modelObject : children) {
      if (modelObject instanceof org.exoplatform.portal.config.model.Application) {
        size++;
      } else if (modelObject instanceof org.exoplatform.portal.config.model.Container container) {
        size = countPageApplications(container.getChildren(), size);
      }
    }
    return size;
  }

  @SneakyThrows
  private void checkExternalUserMemberships() {
    Collection<Membership> internalMemberships = organizationService.getMembershipHandler()
                                                                    .findMembershipsByUserAndGroup(EXTERNAL_USER,
                                                                                                   "/platform/users");
    if (CollectionUtils.isNotEmpty(internalMemberships)) {
      internalMemberships.forEach(this::removeMembership);
    }
    Collection<Membership> externalMemberships = organizationService.getMembershipHandler()
                                                                    .findMembershipsByUserAndGroup(EXTERNAL_USER,
                                                                                                   "/platform/externals");
    if (CollectionUtils.isEmpty(externalMemberships)) {
      organizationService.getMembershipHandler()
                         .linkMembership(organizationService.getUserHandler().findUserByName(EXTERNAL_USER),
                                         organizationService.getGroupHandler().findGroupById("/platform/externals"),
                                         organizationService.getMembershipTypeHandler().findMembershipType("member"),
                                         true);
    }
    restartTransaction();
  }

  @SneakyThrows
  private Membership removeMembership(Membership m) {
    return organizationService.getMembershipHandler().removeMembership(m.getId(), true);
  }

}
