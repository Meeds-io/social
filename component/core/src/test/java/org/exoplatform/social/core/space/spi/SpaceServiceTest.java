/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.space.spi;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
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
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.mock.SpaceListenerPluginMock;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.SpaceExternalInvitation;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent.Type;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;

import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;
import io.meeds.social.space.service.SpaceLayoutService;
import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.space.template.utils.EntityMapper;

import lombok.SneakyThrows;
import org.exoplatform.web.security.codec.CodecInitializer;

public class SpaceServiceTest extends AbstractCoreTest {

  private static final String EXTERNAL4_USER_EMAIL         = "external4@external4.com";

  private static final String EXTERNAL3_USER_EMAIL         = "external3@external3.com";

  private static final String EXTERNAL2_USER_EMAIL         = "external2@external2.com";

  private static final String EXTERNAL1_USER_EMAIL         = "external1@external1.com";


  private static final String EXTERNAL_USER_EMAIL          = "external@external.com";

  private static final String OTHER_USER_NAME              = "otherUser";

  private static final String MY_SPACE_0_PRETTY_NAME       = "my_space_0";

  private static final String MY_SPACE_DISPLAY_NAME_PREFIX = "my space ";

  private static final String TEST_SPACE_DESCRIPTION       = "Space Description for Testing";

  private static final String TEST_SPACE_DISPLAY_NAME      = "testSpace";

  private static final String SPACES_GROUP_PREFIX          = "/spaces/";

  private static final String SPACE_DESCRIPTION            = "add new space ";

  private static final String SPACE2_DISPLAY_NAME          = "Space2";

  private static final String SPACE1_DISPLAY_NAME          = "Space1";

  private static final String SPACE1_NAME                  = "space1";

  private static final String JAMES_NAME                   = "james";

  private static final String PLATFORM_ADMINISTRATORS      = "/platform/administrators";

  private static final String MEMBER3_NAME                 = "member3";

  private static final String NEW_PENDING_USER_NAME        = "newPendingUser";

  private static final String NEW_INVITED_USER_NAME        = "newInvitedUser";

  private static final String HEAR_BREAKER_NAME            = "hearBreaker";

  private static final String PAUL_NAME                    = "paul";

  private static final String JAME_NAME                    = "jame";

  private static final String ROOT_NAME                    = "root";

  private static final String JOHN_NAME                    = "john";

  private static final String MARY_NAME                    = "mary";

  private static final String REGISTER1_NAME               = "register1";

  private static final String DRAGON_NAME                  = "dragon";

  private static final String GHOST_NAME                   = "ghost";

  private static final String RAUL_NAME                    = "raul";

  private static final String TOM_NAME                     = "tom";

  private static final String DEMO_NAME                    = "demo";

  private static final String USER_DOT_NEW_NAME            = "user.new";

  private static final String USER_NEW_1_NAME              = "user-new.1";

  private static final String USER_NEW_NAME                = "user-new";

  private static final String MEMBER2_NAME                 = "member2";

  private static final String MEMBER1_NAME                 = "member1";

  private static final String MANAGER_NAME                 = "manager";

  private static final String HACKER_NAME                  = "hacker";

  private static final String EXTERNAL_USER_NAME           = "externalUser";

  private IdentityStorage     identityStorage;

  private OrganizationService organizationService;

  private SpaceLayoutService  spaceLayoutService;

  private Identity            tom;

  private Identity            dragon;

  private Identity            john;

  private Identity            mary;

  private Identity            root;

  private Identity            hearBreaker;

  private Identity            newInvitedUser;

  private Identity            newPendingUser;

  private Identity            externalUser;

  private CodecInitializer   codecInitializer;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    spaceLayoutService = getContainer().getComponentInstanceOfType(SpaceLayoutService.class);
    codecInitializer = getContainer().getComponentInstanceOfType(CodecInitializer.class);

    Identity userNew = new Identity(OrganizationIdentityProvider.NAME, USER_NEW_NAME);
    Identity userNew1 = new Identity(OrganizationIdentityProvider.NAME, USER_NEW_1_NAME);
    Identity userNewDot = new Identity(OrganizationIdentityProvider.NAME, USER_DOT_NEW_NAME);
    Identity demo = new Identity(OrganizationIdentityProvider.NAME, DEMO_NAME);
    tom = new Identity(OrganizationIdentityProvider.NAME, TOM_NAME);
    Identity raul = new Identity(OrganizationIdentityProvider.NAME, RAUL_NAME);
    Identity ghost = new Identity(OrganizationIdentityProvider.NAME, GHOST_NAME);
    dragon = new Identity(OrganizationIdentityProvider.NAME, DRAGON_NAME);
    Identity register1 = new Identity(OrganizationIdentityProvider.NAME, REGISTER1_NAME);
    mary = new Identity(OrganizationIdentityProvider.NAME, MARY_NAME);
    john = new Identity(OrganizationIdentityProvider.NAME, JOHN_NAME);
    root = new Identity(OrganizationIdentityProvider.NAME, ROOT_NAME);
    Identity jame = new Identity(OrganizationIdentityProvider.NAME, JAME_NAME);
    Identity paul = new Identity(OrganizationIdentityProvider.NAME, PAUL_NAME);
    Identity hacker = new Identity(OrganizationIdentityProvider.NAME, HACKER_NAME);
    hearBreaker = new Identity(OrganizationIdentityProvider.NAME, HEAR_BREAKER_NAME);
    newInvitedUser = new Identity(OrganizationIdentityProvider.NAME, NEW_INVITED_USER_NAME);
    newPendingUser = new Identity(OrganizationIdentityProvider.NAME, NEW_PENDING_USER_NAME);
    Identity manager = new Identity(OrganizationIdentityProvider.NAME, MANAGER_NAME);
    Identity member1 = new Identity(OrganizationIdentityProvider.NAME, MEMBER1_NAME);
    Identity member2 = new Identity(OrganizationIdentityProvider.NAME, MEMBER2_NAME);
    Identity member3 = new Identity(OrganizationIdentityProvider.NAME, MEMBER3_NAME);
    externalUser = new Identity(OrganizationIdentityProvider.NAME, EXTERNAL_USER_NAME);
    checkExternalUserMemberships();

    identityStorage.saveIdentity(demo);
    identityStorage.saveIdentity(tom);
    identityStorage.saveIdentity(raul);
    identityStorage.saveIdentity(ghost);
    identityStorage.saveIdentity(dragon);
    identityStorage.saveIdentity(register1);
    identityStorage.saveIdentity(mary);
    identityStorage.saveIdentity(john);
    identityStorage.saveIdentity(root);
    identityStorage.saveIdentity(jame);
    identityStorage.saveIdentity(paul);
    identityStorage.saveIdentity(hacker);
    identityStorage.saveIdentity(hearBreaker);
    identityStorage.saveIdentity(newInvitedUser);
    identityStorage.saveIdentity(newPendingUser);
    identityStorage.saveIdentity(userNew1);
    identityStorage.saveIdentity(userNew);
    identityStorage.saveIdentity(userNewDot);
    identityStorage.saveIdentity(manager);
    identityStorage.saveIdentity(member1);
    identityStorage.saveIdentity(member2);
    identityStorage.saveIdentity(member3);

    begin();
  }

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

  public void testGetVisibleSpacesWithSpaceTemplateAdmin() throws Exception {
    SpaceTemplate spaceTemplate = mockSpaceTemplate();

    int count = 5;
    Space firstSpace = null;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i == 0) {
        firstSpace = space;
        spaceService.removeMember(space, TOM_NAME);
        spaceTemplate.setAdminPermissions(Collections.singletonList(space.getGroupId()));
      } else if (i % 2 == 0) {
        space.setVisibility(Space.HIDDEN);
        space.setTemplateId(spaceTemplate.getId());
        space = spaceService.updateSpace(space);
        spaceService.removeMember(space, DEMO_NAME);
        spaceService.removeMember(space, TOM_NAME);
      }
    }
    ListAccess<Space> allSpaces = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());

    allSpaces = spaceService.getVisibleSpacesWithListAccess(TOM_NAME, new SpaceFilter());
    assertEquals(3, allSpaces.getSize());

    firstSpace.setVisibility(Space.HIDDEN); // NOSONAR
    spaceService.updateSpace(firstSpace);

    allSpaces = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());

    allSpaces = spaceService.getVisibleSpacesWithListAccess(null, new SpaceFilter());
    assertEquals(2, allSpaces.getSize());
  }

  public void testGetSpacesByCategories() throws Exception {
    List<Long> categoryIds = new ArrayList<>();
    categoryIds.add(35l);
    categoryIds.add(27l);
    categoryIds.add(67l);
    categoryIds.add(58l);
    categoryIds.add(53l);

    int count = 5;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      space.setCategoryIds(Collections.singletonList(categoryIds.get(i)));
      space.setEditor(TOM_NAME);
      spaceService.updateSpace(space);
    }

    SpaceFilter spaceFilter = new SpaceFilter();
    spaceFilter.setCategoryIds(categoryIds);
    ListAccess<Space> spaceListAccess = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, spaceFilter);
    assertEquals(count, spaceListAccess.getSize());

    for (int i = 0; i < count; i++) {
      spaceFilter.setCategoryIds(Collections.singletonList(categoryIds.get(i)));
      spaceListAccess = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, spaceFilter);
      assertEquals(1, spaceListAccess.getSize());
    }

    // Exclude category
    spaceFilter = new SpaceFilter();
    spaceFilter.setCategoryIds(categoryIds);
    spaceFilter.setExcludedCategoryIds(Collections.singletonList(categoryIds.getFirst()));
    spaceListAccess = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, spaceFilter);
    assertEquals(4, spaceListAccess.getSize());

    Space space = this.getSpaceInstance(6);
    space.setCategoryIds(List.of(30L, 40L));
    space.setEditor(TOM_NAME);
    spaceService.updateSpace(space);

    spaceFilter = new SpaceFilter();
    spaceFilter.setCategoryIds(Collections.singletonList(30L));
    spaceListAccess = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, spaceFilter);
    assertEquals(1, spaceListAccess.getSize());

    spaceFilter.setExcludedCategoryIds(Collections.singletonList(40L));
    spaceListAccess = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, spaceFilter);
    assertEquals(0, spaceListAccess.getSize());
  }

  public void testGetMemberSpacesWithSpaceTemplateAdmin() throws Exception {
    SpaceTemplate spaceTemplate = mockSpaceTemplate();

    int count = 5;
    Space firstSpace = null;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i == 0) {
        firstSpace = space;
        spaceService.removeMember(space, TOM_NAME);
        spaceTemplate.setAdminPermissions(Collections.singletonList(space.getGroupId()));
      } else if (i % 2 == 0) {
        space.setVisibility(Space.HIDDEN);
        space.setTemplateId(spaceTemplate.getId());
        space = spaceService.updateSpace(space);
        spaceService.removeMember(space, DEMO_NAME);
        spaceService.removeMember(space, TOM_NAME);
      }
    }
    ListAccess<Space> allSpaces = spaceService.getMemberSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(3, allSpaces.getSize());

    allSpaces = spaceService.getMemberSpacesByFilter(TOM_NAME, new SpaceFilter());
    assertEquals(2, allSpaces.getSize());

    firstSpace.setVisibility(Space.HIDDEN); // NOSONAR
    spaceService.updateSpace(firstSpace);

    allSpaces = spaceService.getMemberSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(3, allSpaces.getSize());

    allSpaces = spaceService.getMemberSpacesByFilter(null, new SpaceFilter());
    assertEquals(0, allSpaces.getSize());
  }

  public void testGetAccessibleSpacesWithSpaceTemplateAdmin() throws Exception {
    SpaceTemplate spaceTemplate = mockSpaceTemplate();

    int count = 5;
    Space firstSpace = null;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i == 0) {
        firstSpace = space;
        spaceService.removeMember(space, TOM_NAME);
        spaceTemplate.setAdminPermissions(Collections.singletonList(space.getGroupId()));
      } else if (i % 2 == 0) {
        space.setVisibility(Space.HIDDEN);
        space.setTemplateId(spaceTemplate.getId());
        space = spaceService.updateSpace(space);
        spaceService.removeMember(space, DEMO_NAME);
        spaceService.removeMember(space, TOM_NAME);
      }
    }
    ListAccess<Space> allSpaces = spaceService.getAccessibleSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());

    allSpaces = spaceService.getAccessibleSpacesByFilter(TOM_NAME, new SpaceFilter());
    assertEquals(2, allSpaces.getSize());

    firstSpace.setVisibility(Space.HIDDEN); // NOSONAR
    spaceService.updateSpace(firstSpace);

    allSpaces = spaceService.getAccessibleSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());

    allSpaces = spaceService.getAccessibleSpacesByFilter(null, new SpaceFilter());
    assertEquals(0, allSpaces.getSize());
  }

  public void testGetManagerSpacesWithSpaceTemplateAdmin() throws Exception {
    SpaceTemplate spaceTemplate = mockSpaceTemplate();

    int count = 5;
    Space firstSpace = null;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i == 0) {
        firstSpace = space;
        spaceService.removeMember(space, TOM_NAME);
        spaceTemplate.setAdminPermissions(Collections.singletonList(space.getGroupId()));
      } else if (i % 2 == 0) {
        space.setVisibility(Space.HIDDEN);
        space.setTemplateId(spaceTemplate.getId());
        space = spaceService.updateSpace(space);
        spaceService.removeMember(space, DEMO_NAME);
        spaceService.removeMember(space, TOM_NAME);
      } else {
        spaceService.setManager(space, TOM_NAME, false);
      }
    }
    ListAccess<Space> allSpaces = spaceService.getManagerSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(3, allSpaces.getSize());

    allSpaces = spaceService.getManagerSpacesByFilter(TOM_NAME, new SpaceFilter());
    assertEquals(0, allSpaces.getSize());

    firstSpace.setVisibility(Space.HIDDEN); // NOSONAR
    spaceService.updateSpace(firstSpace);

    allSpaces = spaceService.getManagerSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(3, allSpaces.getSize());

    allSpaces = spaceService.getManagerSpacesByFilter(null, new SpaceFilter());
    assertEquals(0, allSpaces.getSize());
  }

  public void testGetVisibleSpacesWithNoSpaceTemplateAsAdmin() throws Exception {
    SpaceTemplate spaceTemplate = mockSpaceTemplate();

    int count = 5;
    Space firstSpace = null;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i == 0) {
        firstSpace = space;
        spaceService.removeMember(space, TOM_NAME);
        spaceTemplate.setAdminPermissions(Collections.singletonList(space.getGroupId()));
      } else if (i % 2 == 0) {
        space.setVisibility(Space.HIDDEN);
        space.setTemplateId(spaceTemplate.getId());
        space = spaceService.updateSpace(space);
        spaceService.removeMember(space, DEMO_NAME);
        spaceService.removeMember(space, TOM_NAME);
      }
    }
    ListAccess<Space> allSpaces = spaceService.getVisibleSpacesWithListAccess(ROOT_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());

    assertNotNull(firstSpace);
    firstSpace.setTemplateId(0l); // NOSONAR
    firstSpace.setVisibility(Space.HIDDEN);
    spaceService.updateSpace(firstSpace);

    allSpaces = spaceService.getVisibleSpacesWithListAccess(ROOT_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());
  }

  public void testGetEditableSpacesWithSpaceTemplateAdmin() throws Exception {
    SpaceTemplate spaceTemplate = mockSpaceTemplate();

    int count = 5;
    Space firstSpace = null;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i == 0) {
        firstSpace = space;
        spaceService.removeMember(space, TOM_NAME);
        spaceTemplate.setAdminPermissions(Collections.singletonList(space.getGroupId()));
      } else if (i % 2 == 0) {
        space.setVisibility(Space.HIDDEN);
        space.setTemplateId(spaceTemplate.getId());
        space = spaceService.updateSpace(space);
        spaceService.removeMember(space, DEMO_NAME);
        spaceService.removeMember(space, TOM_NAME);
      } else {
        spaceService.setManager(space, TOM_NAME, false);
      }
    }
    ListAccess<Space> allSpaces = spaceService.getEditableSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());

    allSpaces = spaceService.getEditableSpacesByFilter(TOM_NAME, new SpaceFilter());
    assertEquals(0, allSpaces.getSize());

    firstSpace.setVisibility(Space.HIDDEN); // NOSONAR
    spaceService.updateSpace(firstSpace);

    allSpaces = spaceService.getEditableSpacesByFilter(DEMO_NAME, new SpaceFilter());
    assertEquals(count, allSpaces.getSize());

    allSpaces = spaceService.getEditableSpacesByFilter(null, new SpaceFilter());
    assertEquals(0, allSpaces.getSize());
  }

  public void testGetVisibleSpacesByRegistration() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i == 0) {
        space.setRegistration(Space.OPEN);
        spaceService.updateSpace(space);
      } else if (i % 2 == 0) {
        space.setRegistration(Space.VALIDATION);
        spaceService.updateSpace(space);
      } else {
        space.setRegistration(Space.CLOSED);
        spaceService.updateSpace(space);
      }
    }
    SpaceFilter spaceFilter = new SpaceFilter();
    spaceFilter.setRegistration(SpaceRegistration.CLOSED);
    ListAccess<Space> allSpaces = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, spaceFilter);
    assertEquals(2, allSpaces.getSize());

    spaceFilter.setRegistration(SpaceRegistration.VALIDATION);
    allSpaces = spaceService.getVisibleSpacesWithListAccess(TOM_NAME, spaceFilter);
    assertEquals(2, allSpaces.getSize());

    spaceFilter.setRegistration(SpaceRegistration.OPEN);
    allSpaces = spaceService.getVisibleSpacesWithListAccess(TOM_NAME, spaceFilter);
    assertEquals(1, allSpaces.getSize());
  }

  public void testGetVisibleSpacesByVisibility() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      Space space = this.getSpaceInstance(i);
      if (i % 2 == 0) {
        space.setVisibility(Space.PRIVATE);
        spaceService.updateSpace(space);
      } else {
        space.setVisibility(Space.HIDDEN);
        spaceService.updateSpace(space);
      }
    }
    SpaceFilter spaceFilter = new SpaceFilter();
    spaceFilter.setVisibility(SpaceVisibility.HIDDEN);
    ListAccess<Space> allSpaces = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, spaceFilter);
    assertEquals(2, allSpaces.getSize());

    spaceFilter.setVisibility(SpaceVisibility.PRIVATE);
    allSpaces = spaceService.getVisibleSpacesWithListAccess(TOM_NAME, spaceFilter);
    assertEquals(3, allSpaces.getSize());
  }

  public void testGetSpaceMembershipDate() {
    Space space = populateData();
    assertNotNull(spaceService.getSpaceMembershipDate(Long.parseLong(space.getId()), ROOT_NAME));
    assertNotNull(spaceService.getSpaceMembershipDate(Long.parseLong(space.getId()), JOHN_NAME));
    assertNull(spaceService.getSpaceMembershipDate(Long.parseLong(space.getId()), "notExistingUser"));
  }

  public void testGetSpaceByPrettyName() {
    int count = 5;
    for (int i = 0; i < count; i++) {
      this.getSpaceInstance(i);
    }
    Space foundSpace = spaceService.getSpaceByPrettyName("my_space_4");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 4", "my space 4", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_4", "my_space_4", foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByPrettyName(MY_SPACE_0_PRETTY_NAME);
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 0", "my space 0", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_0", MY_SPACE_0_PRETTY_NAME, foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByPrettyName("my_space_5");
    assertNull("foundSpace must be null", foundSpace);
  }

  public void testDefaultSpaceAvatar() {
    this.getSpaceInstance(0);
    Space space = spaceService.getSpaceByPrettyName(MY_SPACE_0_PRETTY_NAME);
    assertNotNull("space must not be null", space);
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);

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

  public void testGetSpacesWithExcludedIds() throws Exception {
    SpaceFilter spaceFilter = new SpaceFilter();
    ArrayList<Long> excludedIds = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      Space space = this.getSpaceInstance(i);
      if (i % 2 == 0) {
        excludedIds.add(Long.parseLong(space.getId()));
      }
    }
    ListAccess<Space> spaces = spaceService.getAllSpacesByFilter(spaceFilter);
    int size = spaces.getSize();
    spaceFilter.setExcludedIds(excludedIds);
    spaces = spaceService.getAllSpacesByFilter(spaceFilter);
    assertEquals(size - 3, spaces.getSize());
  }

  public void testUpdateSpaceAvatar() throws IOException {
    this.getSpaceInstance(0);
    Space space = spaceService.getSpaceByPrettyName(MY_SPACE_0_PRETTY_NAME);
    assertNotNull("space must not be null", space);
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);
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

  public void testRenameSpaceWithDefaultAvatar() throws SpaceException {
    Space space = this.getSpaceInstance(0);
    assertNotNull(space);

    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    assertNotNull(identity);

    identityStorage.saveIdentity(identity);
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
    Space space1 = createSpace(SPACE1_DISPLAY_NAME, john.getRemoteId());
    createSpace(SPACE2_DISPLAY_NAME, john.getRemoteId());
    Space space3 = createSpace("Space3", john.getRemoteId());
    createSpace("Space4", john.getRemoteId());

    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Favorite space1Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                           space1.getId(),
                                           null,
                                           Long.parseLong(john.getId()));
    favoriteService.createFavorite(space1Favorite);
    Favorite space2Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                           space3.getId(),
                                           null,
                                           Long.parseLong(john.getId()));
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

  public void testGetFavoriteSpacesByFilter() throws Exception {
    Space space1 = createSpace(SPACE1_DISPLAY_NAME, john.getRemoteId());
    createSpace(SPACE2_DISPLAY_NAME, john.getRemoteId());
    Space space3 = createSpace("Space3", john.getRemoteId());
    createSpace("Space4", john.getRemoteId());

    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Favorite space1Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                           space1.getId(),
                                           null,
                                           Long.parseLong(john.getId()));
    favoriteService.createFavorite(space1Favorite);
    Favorite space3Favorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                           space3.getId(),
                                           null,
                                           Long.parseLong(john.getId()));
    favoriteService.createFavorite(space3Favorite);
    favoriteService.createFavorite(new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                                space3.getId(),
                                                null,
                                                Long.parseLong(root.getId())));

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

  public void testGetSpaceById() {
    Space space = populateData();
    createMoreSpace(SPACE2_DISPLAY_NAME);
    assertEquals(space.getDisplayName(), spaceService.getSpaceById(space.getId()).getDisplayName());
  }

  public void testCanRedact() {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity(ROOT_NAME);
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity(JOHN_NAME,
                                                                                                                Collections.singleton(new MembershipEntry(PLATFORM_ADMINISTRATORS,
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity(DEMO_NAME);
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity(JAMES_NAME);
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity(MARY_NAME);
    org.exoplatform.services.security.Identity raulACLIdentity = new org.exoplatform.services.security.Identity(RAUL_NAME);

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestRedact", DEMO_NAME);
    Arrays.stream(new String[] { DEMO_NAME, JAMES_NAME, RAUL_NAME }).forEach(u -> spaceService.addMember(space, u));
    if (ArrayUtils.isNotEmpty(space.getRedactors())) {
      Arrays.stream(space.getRedactors()).forEach(u -> spaceService.removeRedactor(space, u));
    }

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

    spaceService.addMember(space, MARY_NAME);
    spaceService.addRedactor(space, JAMES_NAME);
    spaceService.addPublisher(space, MARY_NAME);

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

  public void testCanView() {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity(ROOT_NAME);
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity(JOHN_NAME,
                                                                                                                Collections.singleton(new MembershipEntry(PLATFORM_ADMINISTRATORS,
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity(DEMO_NAME);
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity(JAMES_NAME);
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity(MARY_NAME);

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestView", DEMO_NAME);
    space.setMembers(new String[] { DEMO_NAME, JAMES_NAME });
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

  public void testCanManage() {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity(ROOT_NAME);
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity(JOHN_NAME,
                                                                                                                Collections.singleton(new MembershipEntry(PLATFORM_ADMINISTRATORS,
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity(DEMO_NAME);
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity(JAMES_NAME);
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity(MARY_NAME);
    org.exoplatform.services.security.Identity raulACLIdentity = new org.exoplatform.services.security.Identity(RAUL_NAME);
    org.exoplatform.services.security.Identity paulACLIdentity = new org.exoplatform.services.security.Identity(PAUL_NAME);

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestManage", DEMO_NAME);
    space.setMembers(new String[] { DEMO_NAME, JAMES_NAME, MARY_NAME, RAUL_NAME });
    space.setManagers(new String[] { MARY_NAME });
    space.setPublishers(new String[] { RAUL_NAME });
    space.setRedactors(new String[] { JAMES_NAME });
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

  public void testCanPublish() {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity(ROOT_NAME);
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity(JOHN_NAME,
                                                                                                                Collections.singleton(new MembershipEntry(PLATFORM_ADMINISTRATORS,
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity(DEMO_NAME);
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity(JAMES_NAME);
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity(MARY_NAME);
    org.exoplatform.services.security.Identity raulACLIdentity = new org.exoplatform.services.security.Identity(RAUL_NAME);
    org.exoplatform.services.security.Identity paulACLIdentity = new org.exoplatform.services.security.Identity(PAUL_NAME);

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    Space space = createSpace("spaceTestPublish", DEMO_NAME);
    space.setMembers(new String[] { DEMO_NAME, JAMES_NAME, MARY_NAME, RAUL_NAME });
    space.setManagers(new String[] { MARY_NAME });
    space.setPublishers(new String[] { RAUL_NAME });
    space.setRedactors(new String[] { JAMES_NAME });
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

  public void testGetInvitedSpacesWithListAccess() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      this.getSpaceInstance(i);
    }
    ListAccess<Space> invitedSpaces = spaceService.getInvitedSpacesWithListAccess(REGISTER1_NAME);
    assertNotNull(invitedSpaces);
    assertEquals(count, invitedSpaces.getSize());
    assertEquals(1, invitedSpaces.load(0, 1).length);
    assertEquals(count, invitedSpaces.load(0, count).length);
    invitedSpaces = spaceService.getInvitedSpacesWithListAccess(MARY_NAME);
    assertNotNull(invitedSpaces);
    assertEquals(count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesWithListAccess(DEMO_NAME);
    assertNotNull(invitedSpaces);
    assertEquals(0, invitedSpaces.getSize());
  }

  public void testCountPendingSpaceRequestsToManage() throws Exception {
    Space space = populateData();
    spaceService.addPendingUser(space, PAUL_NAME);
    spaceService.addPendingUser(space, JAMES_NAME);

    ListAccess<Space> listAccess = spaceService.getPendingSpaceRequestsToManage(ROOT_NAME);
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
    boolean hasPaul = pendingSpaceRequestsToManage[0].getPendingUsers()[0].equals(PAUL_NAME)
                      || pendingSpaceRequestsToManage[1].getPendingUsers()[0].equals(PAUL_NAME);
    boolean hasJames = pendingSpaceRequestsToManage[0].getPendingUsers()[0].equals(JAMES_NAME)
                       || pendingSpaceRequestsToManage[1].getPendingUsers()[0].equals(JAMES_NAME);
    assertTrue(hasJames && hasPaul);
  }

  public void testGetPendingSpacesWithListAccess() throws Exception {
    int count = 5;
    for (int i = 0; i < count; i++) {
      this.getSpaceInstance(i);
    }
    ListAccess<Space> foundSpaces = spaceService.getPendingSpacesWithListAccess(JAME_NAME);
    assertNotNull(foundSpaces);
    assertEquals(count, foundSpaces.getSize());
    assertEquals(1, foundSpaces.load(0, 1).length);
    assertEquals(count, foundSpaces.load(0, count).length);

    foundSpaces = spaceService.getPendingSpacesWithListAccess(PAUL_NAME);
    assertNotNull(foundSpaces);
    assertEquals(count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess(HACKER_NAME);
    assertNotNull(foundSpaces);
    assertEquals(count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess(GHOST_NAME);
    assertNotNull(foundSpaces);
    assertEquals(0, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess("hellgate");
    assertNotNull(foundSpaces);
    assertEquals(0, foundSpaces.getSize());
  }

  public void testCreateSpace() throws Exception {
    populateData();
    createMoreSpace(SPACE2_DISPLAY_NAME);
    ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
    assertNotNull("spaceListAccess must not be null", spaceListAccess);
    assertEquals("spaceListAccess.getSize() must return: 2", 2, spaceListAccess.getSize());
  }

  public void testCreateSpaceWithManagersAndMembers() {
    Space space = new Space();
    space.setDisplayName(TEST_SPACE_DISPLAY_NAME);
    space.setDescription(TEST_SPACE_DESCRIPTION);
    String shortName = Utils.cleanString(space.getDisplayName());
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.VALIDATION);
    space.setUrl(shortName);
    space.setVisibility(Space.PUBLIC);
    Space createdSpace = spaceService.createSpace(space, ROOT_NAME);
    String[] managers = { JOHN_NAME };
    String[] members = { JOHN_NAME, MARY_NAME, PAUL_NAME, JAME_NAME };
    Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));
    Arrays.stream(managers).forEach(u -> spaceService.setManager(createdSpace, u, true));

    space = spaceService.getSpaceById(createdSpace.getId());
    // 2 = 1 creator + 1 managers
    assertEquals(2, space.getManagers().length);
    // 4 = 1 creator + 3 members
    assertEquals(5, space.getMembers().length);
  }

  public void testCreateSpaceEvent() throws SpaceException {
    String[] managers = { MANAGER_NAME };
    String creator = ROOT_NAME;
    List<Identity> invitedIdentities = new ArrayList<>(Arrays.asList(tom, dragon, hearBreaker));
    Space space = new Space();
    space.setDisplayName(TEST_SPACE_DISPLAY_NAME);
    space.setDescription(TEST_SPACE_DESCRIPTION);
    String shortName = Utils.cleanString(space.getDisplayName());
    space.setManagers(managers);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.VALIDATION);
    space.setUrl(shortName);
    space.setVisibility(Space.PUBLIC);

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      spaceService.createSpace(space, creator, invitedIdentities);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(4, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_CREATED, spaceListenerPlugin.getEvents().get(0));
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(1));
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(2));
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(3));
  }

  public void testUpdateSpaceCategories() {
    Space space = new Space();
    space.setDisplayName(TEST_SPACE_DISPLAY_NAME);
    space.setDescription(TEST_SPACE_DESCRIPTION);
    space.setRegistration(Space.VALIDATION);
    space.setVisibility(Space.PUBLIC);
    space = spaceService.createSpace(space, JOHN_NAME);

    List<Long> categoryIds = new ArrayList<>();
    categoryIds.add(37l);
    categoryIds.add(25l);
    space.setCategoryIds(categoryIds);
    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setEditor(JOHN_NAME);
      spaceService.updateSpace(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }
    assertEquals(2, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.CATEGORY_ADDED, spaceListenerPlugin.getEvents().get(0));
    assertEquals(Type.CATEGORY_ADDED, spaceListenerPlugin.getEvents().get(1));
    space = spaceService.getSpaceById(space.getId());
    assertEquals(new HashSet<>(categoryIds), new HashSet<>(space.getCategoryIds()));

    categoryIds = Collections.singletonList(categoryIds.get(0));
    space.setCategoryIds(Collections.singletonList(categoryIds.get(0)));
    spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setEditor(JOHN_NAME);
      spaceService.updateSpace(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }
    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.CATEGORY_REMOVED, spaceListenerPlugin.getEvents().get(0));
    space = spaceService.getSpaceById(space.getId());
    assertEquals(new HashSet<>(categoryIds), new HashSet<>(space.getCategoryIds()));
  }

  public void testUpdateSpaceDescription() {
    Space space = createSpace("spaceUpdateDescription", DEMO_NAME);

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setDescription("Updated Description");
      spaceService.updateSpace(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_DESCRIPTION_EDITED, spaceListenerPlugin.getEvents().get(0));
  }

  public void testUpdateSpaceAccess() {
    Space space = createSpace("spaceUpdateAccess", DEMO_NAME);

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setVisibility(Space.HIDDEN);
      spaceService.updateSpace(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_HIDDEN, spaceListenerPlugin.getEvents().get(0));
  }

  public void testUpdateSpaceRegistration() {
    Space space = createSpace("spaceUpdateRegistration", DEMO_NAME);

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setRegistration(Space.VALIDATION);
      spaceService.updateSpace(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_REGISTRATION, spaceListenerPlugin.getEvents().get(0));
  }

  public void testUpdateSpaceSovereignty() {
    Space space = createSpace("spaceUpdateSovereignty", DEMO_NAME);
    assertFalse(space.isSovereign());
    assertFalse(spaceService.getSpaceById(space.getSpaceId()).isSovereign());

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      space.setSovereign(!space.isSovereign());
      space = spaceService.updateSpace(space);
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.SPACE_SOVEREIGNTY, spaceListenerPlugin.getEvents().get(0));
    assertTrue(space.isSovereign());
    assertTrue(spaceService.getSpaceById(space.getSpaceId()).isSovereign());
  }

  public void testSpaceUserInvitation() {
    Space space = createSpace("spaceUserInvitation", DEMO_NAME);

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      spaceService.addInvitedUser(space, john.getRemoteId());
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.ADD_INVITED_USER, spaceListenerPlugin.getEvents().get(0));
  }

  public void testSpaceUserInvitationDeny() {
    Space space = createSpace("spaceUserInvitationDeny", DEMO_NAME);
    spaceService.addInvitedUser(space, john.getRemoteId());

    SpaceListenerPluginMock spaceListenerPlugin = new SpaceListenerPluginMock();
    spaceService.registerSpaceListenerPlugin(spaceListenerPlugin);
    try {
      spaceService.removeInvitedUser(space, john.getRemoteId());
    } finally {
      spaceService.unregisterSpaceListenerPlugin(spaceListenerPlugin);
    }

    assertEquals(1, spaceListenerPlugin.getEvents().size());
    assertEquals(Type.DENY_INVITED_USER, spaceListenerPlugin.getEvents().get(0));
  }

  public void testCreateSpaceWithInvitation() throws SpaceException {
    String[] managers = { MANAGER_NAME };
    Space spaceCreated = createMoreSpace("invitedSpace");
    String[] users = { MEMBER1_NAME, MEMBER2_NAME };
    spaceCreated.setMembers(users);
    spaceService.updateSpace(spaceCreated);
    Identity spaceIdentity = getService(IdentityManager.class).getOrCreateSpaceIdentity("invitedspace");
    List<Identity> invitedIdentities = new ArrayList<>(Arrays.asList(tom, dragon, hearBreaker, spaceIdentity));
    Space space = new Space();
    space.setDisplayName(TEST_SPACE_DISPLAY_NAME);
    space.setDescription(TEST_SPACE_DESCRIPTION);
    String shortName = Utils.cleanString(space.getDisplayName());
    space.setManagers(managers);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration("validation");
    space.setUrl(shortName);
    space.setVisibility(Space.PUBLIC);
    space = spaceService.createSpace(space, ROOT_NAME, invitedIdentities);
    // 5 = member1, member2 from invitedSpace + tom + dragon + hearBreaker
    assertEquals(5, space.getInvitedUsers().length);
  }

  public void testCreateSpaceExceedingNameLimit() {
    Space space = new Space();
    String spaceDisplayName =
                            "zzz0123456791011121314151617181920012345679101112131415161718192001234567910111213141516171819200123456791011121314151617181920012345679101112131415161718192001234567910111213141516171819200123456791011121314151617181920012345679101112131415161718192001234567910111213141516171819200123456791011121314151617181920";
    space.setDisplayName(spaceDisplayName);
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    assertThrows(SpaceException.class, () -> spaceService.createSpace(space, ROOT_NAME));
  }

  public void testCreateSpaceNameTooShort() {
    Space space = new Space();
    String spaceDisplayName = "zz";
    space.setDisplayName(spaceDisplayName);
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    assertThrows(SpaceException.class, () -> spaceService.createSpace(space, ROOT_NAME));
  }

  public void testCreateSpaceWithNoName() {
    Space space = new Space();
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    Space createdSpace = spaceService.createSpace(space, DRAGON_NAME);
    assertNotNull(createdSpace);
    assertEquals("Dragon", createdSpace.getDisplayName());
  }

  public void testCreateSpaceWithExistingName() {
    Space space = new Space();
    space.setDisplayName(SPACE1_DISPLAY_NAME);
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    Space space1 = spaceService.createSpace(space, DRAGON_NAME);
    assertNotNull(space1);

    space = new Space();
    space.setDisplayName(SPACE1_DISPLAY_NAME);
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    Space space2 = spaceService.createSpace(space, DRAGON_NAME);
    assertNotNull(space2);

    assertNotEquals(space1.getId(), space2.getId());
    assertNotEquals(space1.getPrettyName(), space2.getPrettyName());
    assertNotEquals(space1.getGroupId(), space2.getGroupId());
  }

  public void testCreateSpaceWithExistingPrettyName() {
    Space space = new Space();
    space.setPrettyName(SPACE1_NAME);
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    Space space1 = spaceService.createSpace(space, DRAGON_NAME);
    assertNotNull(space1);

    space = new Space();
    space.setPrettyName(SPACE1_NAME);
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    Space space2 = spaceService.createSpace(space, DRAGON_NAME);
    assertNotNull(space2);

    assertNotEquals(space1.getId(), space2.getId());
    assertNotEquals(space1.getPrettyName(), space2.getPrettyName());
    assertNotEquals(space1.getGroupId(), space2.getGroupId());
  }

  public void testCreateSpaceWithTemplateCharacteristics() throws  ObjectNotFoundException, SpaceException {
    SpaceTemplateService spaceTemplateService = getService(SpaceTemplateService.class);
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplates().getFirst();
    List<String> originalSpaceFields = spaceTemplate.getSpaceFields();
    spaceTemplate.setSpaceFields(Collections.singletonList("invitation"));
    spaceTemplateService.updateSpaceTemplate(spaceTemplate);

    try {
      Space createdSpace = spaceService.createSpace(new Space(), RAUL_NAME, Arrays.asList(dragon, john));
      assertNotNull(createdSpace);
      assertTrue(createdSpace.getDisplayName().contains("Dragon"));
      assertTrue(createdSpace.getDisplayName().contains("and 1 more"));
      assertTrue(createdSpace.getDisplayName().contains(", "));
      assertEquals(spaceTemplate.getSpaceDefaultVisibility().name().toLowerCase(), createdSpace.getVisibility());
      assertEquals(spaceTemplate.getSpaceDefaultRegistration().name().toLowerCase(), createdSpace.getRegistration());
      assertEquals(SpaceUtils.MANAGER + ":" + createdSpace.getGroupId(), createdSpace.getDeletePermissions().get(0));
      assertEquals(SpaceUtils.MANAGER + ":" + createdSpace.getGroupId(), createdSpace.getLayoutPermissions().get(0));
      assertEquals(SpaceUtils.MANAGER + ":" + createdSpace.getGroupId(), createdSpace.getPublicSitePermissions().get(0));
    } finally {
      spaceTemplate.setSpaceFields(originalSpaceFields);
      spaceTemplateService.updateSpaceTemplate(spaceTemplate);
    }
  }

  public void testIsSuperManagerBySpaceTemplate() throws ObjectNotFoundException {
    Space raulSpace = createSpace("raulSpace", RAUL_NAME);
    Space rootSpace = createSpace("rootSpace", ROOT_NAME);
    assertFalse(spaceService.isSuperManager(rootSpace.getSpaceId(), RAUL_NAME));

    SpaceTemplateService spaceTemplateService = getService(SpaceTemplateService.class);
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplates().getFirst();
    List<String> originalSpaceAdminPermissions = spaceTemplate.getAdminPermissions();
    spaceTemplate.setAdminPermissions(Collections.singletonList(raulSpace.getGroupId()));
    spaceTemplateService.updateSpaceTemplate(spaceTemplate);
    try {
      assertFalse(spaceService.isSuperManager(rootSpace, RAUL_NAME));
      assertFalse(spaceService.canManageSpace(rootSpace, RAUL_NAME));
      assertFalse(spaceService.canDeleteSpace(rootSpace, RAUL_NAME));
      assertFalse(spaceService.canManageSpaceLayout(rootSpace, RAUL_NAME));
      assertFalse(spaceService.canPublishOnSpace(rootSpace, RAUL_NAME));
      assertFalse(spaceService.canRedactOnSpace(rootSpace, RAUL_NAME));
      assertFalse(spaceService.canViewSpace(rootSpace, RAUL_NAME));
      assertFalse(spaceService.isMember(rootSpace, RAUL_NAME));
      assertFalse(spaceService.isManager(rootSpace, RAUL_NAME));

      restartTransaction();
      rootSpace = spaceService.getSpaceById(rootSpace.getSpaceId());
      rootSpace.setTemplateId(spaceTemplate.getId());

      assertTrue(spaceService.isSuperManager(rootSpace, RAUL_NAME));
      assertTrue(spaceService.canManageSpace(rootSpace, RAUL_NAME));
      assertTrue(spaceService.canDeleteSpace(rootSpace, RAUL_NAME));
      assertTrue(spaceService.canManageSpaceLayout(rootSpace, RAUL_NAME));
      assertTrue(spaceService.canPublishOnSpace(rootSpace, RAUL_NAME));
      assertTrue(spaceService.canRedactOnSpace(rootSpace, RAUL_NAME));
      assertTrue(spaceService.canViewSpace(rootSpace, RAUL_NAME));
      assertFalse(spaceService.isMember(rootSpace, RAUL_NAME));
      assertFalse(spaceService.isManager(rootSpace, RAUL_NAME));
    } finally {
      spaceTemplate.setAdminPermissions(originalSpaceAdminPermissions);
      spaceTemplateService.updateSpaceTemplate(spaceTemplate);
    }
  }

  public void testCreateSpaceWithInvalidSpaceName() {
    Space space = new Space();
    String spaceDisplayName = "%zzz:^!/<>😁";
    space.setDisplayName(spaceDisplayName);
    String creator = ROOT_NAME;
    String shortName = "zzz";
    space.setDisplayName(spaceDisplayName);
    space.setPrettyName(shortName);
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
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

  public void testCreateSpaceNoUser() {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertEquals(spaceDisplayName, savedSpace.getDisplayName());
    assertEquals(spaceDescription, savedSpace.getDescription());
    assertEquals(groupId, savedSpace.getGroupId());
    assertNotNull(savedSpace.getAvatarUrl());
  }

  public void testRenameSpace() throws SpaceException {
    Space space = this.getSpaceInstance(0);
    assertEquals(Space.HOME_URL, space.getUrl());
    String newUrl = "fakeUrl";
    space.setUrl(newUrl);
    spaceService.updateSpace(space);

    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);

    String newDisplayName = "new display name";

    spaceService.renameSpace(space, newDisplayName);

    Space got = spaceService.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());
    assertEquals(Space.HOME_URL, space.getUrl());

    newDisplayName = "new display name with super admin";

    //
    spaceService.renameSpace(space, newDisplayName, root.getRemoteId());

    got = spaceService.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());

    assertThrows(SpaceException.class,
                 () -> spaceService.renameSpace(space,
                                                "new display name with normal admin",
                                                mary.getRemoteId()));

    assertNotNull(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()));

    String oldPrettyName = space.getPrettyName();
    newDisplayName = "new display name with null remoteId";

    //
    spaceService.renameSpace(space, newDisplayName);

    got = spaceService.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());

    assertNotNull(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()));
    assertNull(identityManager.getOrCreateSpaceIdentity(oldPrettyName));
  }

  public void testDeleteSpace() throws Exception {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertEquals(spaceDisplayName, savedSpace.getDisplayName());
    assertEquals(spaceDescription, savedSpace.getDescription());
    assertEquals(groupId, savedSpace.getGroupId());
    assertNotNull(organizationService.getGroupHandler().findGroupById(groupId));
    spaceService.deleteSpace(space);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertNull("savedSpace must be null", savedSpace);
    assertNull("the group " + groupId + " must be deleted after space deletion ",
               organizationService.getGroupHandler().findGroupById(groupId));
  }

  public void testDeleteSpaceWithoutDeletingGroup() throws Exception {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertEquals(spaceDisplayName, savedSpace.getDisplayName());
    assertEquals(spaceDescription, savedSpace.getDescription());
    assertEquals(groupId, savedSpace.getGroupId());
    assertNotNull(organizationService.getGroupHandler().findGroupById(groupId));
    spaceService.deleteSpace(space, false);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertNull("savedSpace must be null", savedSpace);
    assertNotNull("the group " + groupId + " must exist after space deletion",
                  organizationService.getGroupHandler().findGroupById(groupId));
    Group group = organizationService.getGroupHandler().findGroupById(groupId);
    organizationService.getGroupHandler().removeGroup(group, true);
  }

  public void testUpdateSpacePermissions() {
    Space space = this.getSpaceInstance(0);
    try {
      spaceService.updateSpaceBanner(space, RAUL_NAME);
      fail("Space member shouldn't be able to update space banner");
    } catch (Exception e) {
      // Expected
    }
    try {
      spaceService.updateSpaceAvatar(space, RAUL_NAME);
      fail("Space member shouldn't be able to update space avatar");
    } catch (Exception e) {
      // Expected
    }
  }

  public void testUpdateSpace() {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertEquals(spaceDisplayName, savedSpace.getDisplayName());
    assertEquals(spaceDescription, savedSpace.getDescription());
    assertEquals(groupId, savedSpace.getGroupId());

    String updateSpaceDisplayName = "update new space display name";
    space.setDisplayName(updateSpaceDisplayName);
    space.setPrettyName(space.getDisplayName());
    spaceService.updateSpace(space);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + updateSpaceDisplayName,
                 updateSpaceDisplayName,
                 savedSpace.getDisplayName());
    assertEquals(spaceDescription, savedSpace.getDescription());
    assertEquals(groupId, savedSpace.getGroupId());
  }

  public void testAddPendingUser() {
    Space space = this.getSpaceInstance(0);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceById(space.getId());
    assertEquals(pendingUsersCount + 1,
                 space.getPendingUsers().length);
    assertTrue(
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  public void testRemovePendingUser() {
    Space space = this.getSpaceInstance(0);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceById(space.getId());
    assertEquals(pendingUsersCount + 1,
                 space.getPendingUsers().length);
    assertTrue(
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));

    spaceService.removePendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceById(space.getId());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount,
                 pendingUsersCount,
                 space.getPendingUsers().length);
    assertFalse(
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  public void testIsPendingUser() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertTrue("spaceService.isPendingUser(savedSpace, \"jame\") must return true",
               spaceService.isPendingUser(savedSpace, JAME_NAME));
    assertTrue("spaceService.isPendingUser(savedSpace, \"paul\") must return true",
               spaceService.isPendingUser(savedSpace, PAUL_NAME));
    assertTrue("spaceService.isPendingUser(savedSpace, \"hacker\") must return true",
               spaceService.isPendingUser(savedSpace, HACKER_NAME));
    assertFalse("spaceService.isPendingUser(savedSpace, \"newpendinguser\") must return false",
                spaceService.isPendingUser(savedSpace, "newpendinguser"));
  }

  public void testAddInvitedUser() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse(ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals(invitedUsersCount + 1,
                 savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
               ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  public void testRemoveInvitedUser() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse(ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals(invitedUsersCount + 1,
                 savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
               ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.removeInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount,
                 invitedUsersCount,
                 savedSpace.getInvitedUsers().length);
    assertFalse(ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  public void testIsInvitedUser() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertTrue("spaceService.isInvitedUser(savedSpace, \"register1\") must return true",
               spaceService.isInvitedUser(savedSpace, REGISTER1_NAME));
    assertTrue("spaceService.isInvitedUser(savedSpace, \"mary\") must return true",
               spaceService.isInvitedUser(savedSpace, MARY_NAME));
    assertFalse("spaceService.isInvitedUser(savedSpace, \"hacker\") must return false",
                spaceService.isInvitedUser(savedSpace, HACKER_NAME));
    assertFalse("spaceService.isInvitedUser(savedSpace, \"nobody\") must return false",
                spaceService.isInvitedUser(savedSpace, "nobody"));
  }

  public void testSetManager() {
    int number = 0;
    Space space = new Space();
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { DEMO_NAME, TOM_NAME };
    String[] members = new String[] { DEMO_NAME, TOM_NAME, RAUL_NAME, GHOST_NAME, DRAGON_NAME };
    String[] invitedUsers = new String[] { REGISTER1_NAME, MARY_NAME };
    String[] pendingUsers = new String[] { JAME_NAME, PAUL_NAME, HACKER_NAME };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, DEMO_NAME, null);

    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    int managers = savedSpace.getManagers().length;
    spaceService.setManager(savedSpace, DEMO_NAME, true);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals(managers, savedSpace.getManagers().length);

    spaceService.setManager(savedSpace, JOHN_NAME, true);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals("savedSpace.getManagers().length must return: " + managers + 1, managers + 1, savedSpace.getManagers().length);

    spaceService.setManager(savedSpace, DEMO_NAME, false);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals(managers, savedSpace.getManagers().length);
  }

  public void testIsManager() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertTrue("spaceService.isManager(savedSpace, \"demo\") must return true", spaceService.isManager(savedSpace, DEMO_NAME));
    assertTrue("spaceService.isManager(savedSpace, \"tom\") must return true", spaceService.isManager(savedSpace, TOM_NAME));
    assertFalse("spaceService.isManager(savedSpace, \"mary\") must return false", spaceService.isManager(savedSpace, MARY_NAME));
    assertFalse("spaceService.isManager(savedSpace, \"john\") must return false", spaceService.isManager(savedSpace, JOHN_NAME));
  }

  public void testSetRedactor() {
    Space space = new Space();
    space.setDisplayName(SPACE1_NAME);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space1");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    Space createdSpace = spaceService.createSpace(space, ROOT_NAME);
    String[] members = new String[] { GHOST_NAME, JOHN_NAME };
    Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));
    assertEquals(0, createdSpace.getRedactors().length);

    MembershipType redactorMembershipType = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
    redactorMembershipType.setName("redactor");
    spaceService.addRedactor(createdSpace, GHOST_NAME);
    spaceService.addRedactor(createdSpace, JOHN_NAME);

    space = spaceService.getSpaceById(createdSpace.getId());
    assertEquals(2, space.getRedactors().length);

    spaceService.removeRedactor(createdSpace, JOHN_NAME);
    space = spaceService.getSpaceById(createdSpace.getId());
    assertEquals(1, space.getRedactors().length);
    assertTrue(spaceService.isRedactor(createdSpace, GHOST_NAME));
    assertFalse(spaceService.isRedactor(createdSpace, JOHN_NAME));
  }

  public void testSetPublisher() {
    Space space = new Space();
    space.setDisplayName(SPACE1_NAME);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space1");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setUrl(space.getPrettyName());
    Space createdSpace = spaceService.createSpace(space, ROOT_NAME);
    String[] members = new String[] { GHOST_NAME, JOHN_NAME };
    Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));

    MembershipType publisherMembershipType = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
    publisherMembershipType.setName("publisher");
    spaceService.addPublisher(createdSpace, GHOST_NAME);
    spaceService.addPublisher(createdSpace, JOHN_NAME);
    assertEquals(2, createdSpace.getPublishers().length);
    spaceService.removePublisher(createdSpace, JOHN_NAME);
    assertEquals(1, createdSpace.getPublishers().length);
    assertTrue(spaceService.isPublisher(createdSpace, GHOST_NAME));
    assertFalse(spaceService.isPublisher(createdSpace, JOHN_NAME));
  }

  public void testIsOnlyManager() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);
    assertFalse("spaceService.isOnlyManager(savedSpace, \"tom\") must return false",
                spaceService.isOnlyManager(savedSpace, TOM_NAME));
    assertFalse("spaceService.isOnlyManager(savedSpace, \"demo\") must return false",
                spaceService.isOnlyManager(savedSpace, DEMO_NAME));

    savedSpace.setManagers(new String[] { DEMO_NAME });
    spaceService.updateSpace(savedSpace);
    assertTrue("spaceService.isOnlyManager(savedSpace, \"demo\") must return true",
               spaceService.isOnlyManager(savedSpace, DEMO_NAME));
    assertFalse("spaceService.isOnlyManager(savedSpace, \"tom\") must return false",
                spaceService.isOnlyManager(savedSpace, TOM_NAME));

    savedSpace.setManagers(new String[] { TOM_NAME });
    spaceService.updateSpace(savedSpace);
    assertFalse("spaceService.isOnlyManager(savedSpace, \"demo\") must return false",
                spaceService.isOnlyManager(savedSpace, DEMO_NAME));
    assertTrue("spaceService.isOnlyManager(savedSpace, \"tom\") must return true",
               spaceService.isOnlyManager(savedSpace, TOM_NAME));
  }

  public void testAddMember() {
    int number = 0;
    Space space = new Space();
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { DEMO_NAME };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { REGISTER1_NAME, MARY_NAME };
    String[] pendingUsers = new String[] { JAME_NAME, PAUL_NAME, HACKER_NAME };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, DEMO_NAME, null);

    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);

    spaceService.addMember(savedSpace, ROOT_NAME);
    spaceService.addMember(savedSpace, MARY_NAME);
    spaceService.addMember(savedSpace, JOHN_NAME);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);
  }

  public void testAddMemberSpecialCharacter() throws Exception {
    String reg = "^\\p{L}[\\p{L}\\d\\s._,-]+$";
    Pattern pattern = Pattern.compile(reg);
    assertTrue(pattern.matcher(USER_NEW_1_NAME).matches());
    assertTrue(pattern.matcher(USER_DOT_NEW_NAME).matches());
    assertTrue(pattern.matcher(USER_NEW_NAME).matches());

    int number = 0;
    Space space = new Space();
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { DEMO_NAME };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { REGISTER1_NAME, MARY_NAME };
    String[] pendingUsers = new String[] { JAME_NAME, PAUL_NAME, HACKER_NAME };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, DEMO_NAME, null);

    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);

    User user = organizationService.getUserHandler().createUserInstance(USER_NEW_1_NAME);
    organizationService.getUserHandler().createUser(user, false);
    user = organizationService.getUserHandler().createUserInstance(USER_DOT_NEW_NAME);
    organizationService.getUserHandler().createUser(user, false);
    user = organizationService.getUserHandler().createUserInstance(USER_NEW_NAME);
    organizationService.getUserHandler().createUser(user, false);

    spaceService.addMember(savedSpace, USER_NEW_1_NAME);
    spaceService.addMember(savedSpace, USER_DOT_NEW_NAME);
    spaceService.addMember(savedSpace, USER_NEW_NAME);
    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals(4, savedSpace.getMembers().length);
  }

  public void testRemoveMember() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { DEMO_NAME };
    String[] members = new String[] {};
    String[] invitedUsers = new String[] { REGISTER1_NAME, MARY_NAME };
    String[] pendingUsers = new String[] { JAME_NAME, PAUL_NAME, HACKER_NAME };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, DEMO_NAME, null);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);

    spaceService.addMember(savedSpace, ROOT_NAME);
    spaceService.addMember(savedSpace, MARY_NAME);
    spaceService.addMember(savedSpace, JOHN_NAME);
    spaceService.setManager(savedSpace, JOHN_NAME, true);
    spaceService.addRedactor(savedSpace, JOHN_NAME);

    GroupHandler groupHandler = organizationService.getGroupHandler();
    UserHandler userHandler = organizationService.getUserHandler();
    MembershipType mbShipTypeMember = organizationService.getMembershipTypeHandler()
                                                         .findMembershipType(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    User user = userHandler.findUserByName(JOHN_NAME);
    organizationService.getMembershipHandler()
                       .linkMembership(user, groupHandler.findGroupById(space.getGroupId()), mbShipTypeMember, true);

    savedSpace = spaceService.getSpaceById(space.getId());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);

    spaceService.removeMember(savedSpace, ROOT_NAME);
    spaceService.removeMember(savedSpace, MARY_NAME);
    spaceService.removeMember(savedSpace, JOHN_NAME);
    Membership any = organizationService.getMembershipHandler()
                                        .findMembershipByUserGroupAndType(JOHN_NAME,
                                                                          space.getGroupId(),
                                                                          MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    assertNull(any);
    assertFalse(spaceService.isManager(savedSpace, JOHN_NAME));
    assertFalse(spaceService.isRedactor(savedSpace, JOHN_NAME));
    assertFalse(spaceService.isMember(savedSpace, JOHN_NAME));

    assertEquals("savedSpace.getMembers().length must return 1", 1, savedSpace.getMembers().length);
  }

  public void testDeleteSpaceWithBoundMember() throws Exception {
    String userToBind = PAUL_NAME;

    int number = 17;
    Space space = new Space();
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setUrl(space.getPrettyName());
    space.setMembers(new String[] { JOHN_NAME, MARY_NAME });
    space = this.createSpaceNonInitApps(space, JOHN_NAME, null);
    Space savedSpace = spaceService.getSpaceById(space.getId());
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
    String userToBind = PAUL_NAME;

    int number = 0;
    Space space = new Space();
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setUrl(space.getPrettyName());
    space.setMembers(new String[] { JOHN_NAME, MARY_NAME });
    space = this.createSpaceNonInitApps(space, JOHN_NAME, null);
    Space savedSpace = spaceService.getSpaceById(space.getId());
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

  public void testGetMemberSpaces() {
    Space[] listSpace = new Space[10];
    for (int i = 0; i < listSpace.length; i++) {
      listSpace[i] = this.getSpaceInstance(i);
    }

    restartTransaction();

    String raulUsername = RAUL_NAME;
    String jameUsername = JAME_NAME;

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

  public void testIsMember() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);

    assertTrue("spaceService.isMember(savedSpace, \"raul\") must return true", spaceService.isMember(savedSpace, RAUL_NAME));
    assertTrue("spaceService.isMember(savedSpace, \"ghost\") must return true", spaceService.isMember(savedSpace, GHOST_NAME));
    assertTrue("spaceService.isMember(savedSpace, \"dragon\") must return true", spaceService.isMember(savedSpace, DRAGON_NAME));
    assertFalse("spaceService.isMember(savedSpace, \"stranger\") must return true",
                spaceService.isMember(savedSpace, "stranger"));
  }

  public void testRevokeRequestJoin() {
    Space space = this.getSpaceInstance(0);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceById(space.getId());
    assertEquals(pendingUsersCount + 1,
                 space.getPendingUsers().length);
    assertTrue(
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));

    spaceService.removePendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceById(space.getId());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount,
                 pendingUsersCount,
                 space.getPendingUsers().length);
    assertFalse(ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  public void testDenyInvitation() {
    Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(savedSpace);

    spaceService.removeInvitedUser(savedSpace, "new member 1");
    spaceService.removeInvitedUser(savedSpace, "new member 2");
    spaceService.removeInvitedUser(savedSpace, "new member 3");
    assertEquals("savedSpace.getMembers().length must return 2", 2, savedSpace.getInvitedUsers().length);

    spaceService.removeInvitedUser(savedSpace, RAUL_NAME);
    spaceService.removeInvitedUser(savedSpace, GHOST_NAME);
    spaceService.removeInvitedUser(savedSpace, DRAGON_NAME);
    assertEquals("savedSpace.getMembers().length must return 2", 2, savedSpace.getInvitedUsers().length);

    spaceService.removeInvitedUser(savedSpace, REGISTER1_NAME);
    spaceService.removeInvitedUser(savedSpace, MARY_NAME);
    assertEquals("savedSpace.getMembers().length must return 0", 0, savedSpace.getInvitedUsers().length);
  }

  public void testGetVisibleSpaces() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];

    // there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i++) {

      if (i < 6)
        // [0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.OPEN, DEMO_NAME);
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.OPEN, DEMO_NAME);

      spaceService.createSpace(listSpace[i]);
    }

    // visible with remoteId = 'demo' return 10 spaces
    Space[] visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, null).load(0, countSpace * 2);
    assertNotNull(visibleAllSpaces);
    assertEquals(countSpace, visibleAllSpaces.length);
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
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.CLOSED, DEMO_NAME);
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSED, DEMO_NAME);

      spaceService.createSpace(listSpace[i]);
    }

    // visible with remoteId = 'demo' return 10 spaces
    Space[] visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, null).load(0, countSpace * 2);
    assertNotNull(visibleAllSpaces);
    assertEquals(countSpace, visibleAllSpaces.length);

    // visible with remoteId = 'mary' return 6 spaces: can see
    int registrationCloseSpaceCount = 6;
    visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess(MARY_NAME, null)
                                   .load(0, registrationCloseSpaceCount * 2);
    assertNotNull("registrationCloseSpaces must not be  null", visibleAllSpaces);
    assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount,
                 registrationCloseSpaceCount,
                 visibleAllSpaces.length);

  }

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
                                                        new String[] { MARY_NAME, HACKER_NAME },
                                                        DEMO_NAME);
      else
        // [6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSED, DEMO_NAME);

      spaceService.createSpace(listSpace[i]);
    }

    // visible with remoteId = 'demo' return 10 spaces
    Space[] visibleAllSpaces = spaceService.getVisibleSpacesWithListAccess(DEMO_NAME, null).load(0, countSpace * 2);
    assertNotNull(visibleAllSpaces);
    assertEquals(countSpace, visibleAllSpaces.length);

    // visible with invited = 'mary' return 6 spaces
    int invitedSpaceCount1 = 6;
    Space[] invitedSpaces1 = spaceService.getVisibleSpacesWithListAccess(MARY_NAME, null).load(0, invitedSpaceCount1 * 2);
    assertNotNull(invitedSpaces1);
    assertEquals(invitedSpaceCount1, invitedSpaces1.length);

    // visible with invited = 'hacker' return 6 spaces
    invitedSpaceCount1 = 6;
    invitedSpaces1 = spaceService.getVisibleSpacesWithListAccess(HACKER_NAME, null).load(0, invitedSpaceCount1 * 2);
    assertNotNull(invitedSpaces1);
    assertEquals(invitedSpaceCount1, invitedSpaces1.length);

    // visible with invited = 'paul' return 6 spaces
    int invitedSpaceCount2 = 6;
    Space[] invitedSpaces2 = spaceService.getVisibleSpacesWithListAccess(PAUL_NAME, null).load(0, invitedSpaceCount2 * 2);
    assertNotNull(invitedSpaces2);
    assertEquals(invitedSpaceCount2, invitedSpaces2.length);
  }

  public void testGetLastSpaces() throws SpaceException {
    populateData();
    createMoreSpace(SPACE2_DISPLAY_NAME);
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
    Group group = organizationService.getGroupHandler().findGroupById(PLATFORM_ADMINISTRATORS);
    MembershipType msType = organizationService.getMembershipTypeHandler().findMembershipType(SpaceUtils.MANAGER);
    organizationService.getMembershipHandler().linkMembership(superManager, group, msType, true);
    restartTransaction();

    assertTrue(spaceService.isSuperManager(username));

    Space space = createSpace("spacename1", ROOT_NAME);
    spaceService.inviteIdentities(space, Collections.singletonList(superManagerIdentity));

    space = spaceService.getSpaceById(space.getId());
    assertTrue(spaceService.isInvitedUser(space, username));
  }

  public void testExternalSpaceInvitations() {
    spaceService.saveSpaceExternalInvitation("5", EXTERNAL_USER_EMAIL, "test");
    spaceService.saveSpaceExternalInvitation("5", EXTERNAL1_USER_EMAIL, "test");
    spaceService.saveSpaceExternalInvitation("6", EXTERNAL_USER_EMAIL, "test");
    spaceService.saveSpaceExternalInvitation("7", EXTERNAL2_USER_EMAIL, "test");
    spaceService.saveSpaceExternalInvitation("7", EXTERNAL3_USER_EMAIL, "test");

    List<SpaceExternalInvitation> spaceExternalInvitationEntities = spaceService.findSpaceExternalInvitationsBySpaceId("5");
    assertNotNull(spaceExternalInvitationEntities);
    assertEquals(2, spaceExternalInvitationEntities.size());

    List<String> spaceIds = spaceService.findExternalInvitationsSpacesByEmail(EXTERNAL_USER_EMAIL);
    assertNotNull(spaceIds);
    assertEquals(2, spaceExternalInvitationEntities.size());

    spaceService.deleteExternalUserInvitations(EXTERNAL_USER_EMAIL);

    List<SpaceExternalInvitation> spaceExternalInvitationEntities1 = spaceService.findSpaceExternalInvitationsBySpaceId("5");
    assertNotNull(spaceExternalInvitationEntities);
    assertEquals(1, spaceExternalInvitationEntities1.size());

    List<String> spaceIds2 = spaceService.findExternalInvitationsSpacesByEmail(EXTERNAL2_USER_EMAIL);
    assertEquals(1, spaceIds2.size());
  }

  @SneakyThrows
  public void testSpaceInvitations() {
    Space space = createSpace("spacename156", ROOT_NAME);
    spaceService.saveSpaceExternalInvitation(space.getId(), EXTERNAL4_USER_EMAIL, "test");

    String username = "external4";
    User user = organizationService.getUserHandler().createUserInstance(username);
    user.setEmail(EXTERNAL4_USER_EMAIL);
    user.setPassword("Test1234@");
    user.setFirstName("External");
    user.setLastName("User");
    organizationService.getUserHandler().createUser(user, true);

    assertTrue(spaceService.isMember(space.getId(), username));
    Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(username);
    assertNotNull(memberships);
    assertTrue(memberships.stream().anyMatch(m -> m.getUserName().equals(username)));
  }

  public void testDeleteSpaceExternalInvitation() {
    spaceService.saveSpaceExternalInvitation("8", EXTERNAL_USER_EMAIL, "token");
    spaceService.saveSpaceExternalInvitation("8", EXTERNAL1_USER_EMAIL, "token1");
    spaceService.saveSpaceExternalInvitation("8", EXTERNAL2_USER_EMAIL, "token2");

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
    Space space = createSpace("spacename1", ROOT_NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.OPEN);
    spaceService.updateSpace(space);

    space = createSpace("spacename2", ROOT_NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.CLOSED);
    spaceService.updateSpace(space);

    space = createSpace("spacename3", ROOT_NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    spaceService.updateSpace(space);

    space = createSpace("spacename4", ROOT_NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.CLOSED);
    spaceService.updateSpace(space);

    space = createSpace("spacename5", ROOT_NAME);
    space.setVisibility(Space.HIDDEN);
    space.setRegistration(Space.OPEN);
    spaceService.updateSpace(space);

    space = createSpace("spacename6", ROOT_NAME);
    space.setVisibility(Space.HIDDEN);
    space.setRegistration(Space.CLOSED);
    spaceService.updateSpace(space);

    User user = organizationService.getUserHandler().createUserInstance("user-space-admin");
    organizationService.getUserHandler().createUser(user, false);
    Group group = organizationService.getGroupHandler().findGroupById(PLATFORM_ADMINISTRATORS);
    MembershipType mstype = organizationService.getMembershipTypeHandler().findMembershipType(SpaceUtils.MANAGER);
    organizationService.getMembershipHandler().linkMembership(user, group, mstype, true);
    restartTransaction();

    String userName = user.getUserName();

    assertEquals(6, spaceService.getAllSpacesWithListAccess().getSize());
    assertTrue(spaceService.isSuperManager(userName));
  }

  @SneakyThrows
  public void testCanAccessSpacePublicSite() {
    checkExternalUserMemberships();

    Space space = getSpaceInstance(20);
    String spaceId = space.getId();

    assertFalse(spaceService.canAccessSpacePublicSite(null, DEMO_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, DEMO_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, JOHN_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.MANAGER, DEMO_NAME);
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, JOHN_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, DEMO_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, RAUL_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, MARY_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, PAUL_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.MEMBER, DEMO_NAME);
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, JOHN_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, DEMO_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, RAUL_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, MARY_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, PAUL_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.INTERNAL, DEMO_NAME);
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, JOHN_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, RAUL_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, MARY_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, PAUL_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.AUTHENTICATED, DEMO_NAME);
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, JOHN_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, DEMO_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, RAUL_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, MARY_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, PAUL_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER_NAME));
    assertFalse(spaceService.canAccessSpacePublicSite(space, null));
    assertFalse(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));

    spaceLayoutService.saveSpacePublicSite(spaceId, SpaceUtils.EVERYONE, DEMO_NAME);
    space = spaceService.getSpaceById(spaceId);
    assertTrue(spaceService.canAccessSpacePublicSite(space, ROOT_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, JOHN_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, DEMO_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, RAUL_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, MARY_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, PAUL_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, EXTERNAL_USER_NAME));
    assertTrue(spaceService.canAccessSpacePublicSite(space, null));
    assertTrue(spaceService.canAccessSpacePublicSite(space, IdentityConstants.ANONIM));
  }

  private Space populateData() {
    String spaceDisplayName = SPACE1_DISPLAY_NAME;
    Space space1 = new Space();
    space1.setDisplayName(spaceDisplayName);
    space1.setRegistration("validation");
    space1.setDescription("This is my first space for testing");
    space1.setVisibility(Space.PUBLIC);
    space1.setEditor(ROOT_NAME);

    Space createdSpace = spaceService.createSpace(space1);
    String[] members = new String[] { DEMO_NAME, JOHN_NAME, MARY_NAME, TOM_NAME };
    Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));

    return createdSpace;
  }

  private Space createSpace(String spaceName, String username) {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setVisibility(Space.PRIVATE);
    return spaceService.createSpace(space, username);
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
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    String[] managers = new String[] { DEMO_NAME, TOM_NAME };
    String[] members = new String[] { DEMO_NAME, RAUL_NAME, GHOST_NAME, DRAGON_NAME };
    String[] invitedUsers = new String[] { REGISTER1_NAME, MARY_NAME };
    String[] pendingUsers = new String[] { JAME_NAME, PAUL_NAME, HACKER_NAME };
    Space createdSpace = this.spaceService.createSpace(space, DEMO_NAME);
    Arrays.stream(pendingUsers).forEach(u -> spaceService.addPendingUser(createdSpace, u));
    Arrays.stream(invitedUsers).forEach(u -> spaceService.addInvitedUser(createdSpace, u));
    Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));
    Arrays.stream(managers).forEach(u -> spaceService.addMember(createdSpace, u));
    Arrays.stream(managers).forEach(u -> spaceService.setManager(createdSpace, u, true));
    return createdSpace;
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
    space.setDisplayName(MY_SPACE_DISPLAY_NAME_PREFIX + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(registration);
    space.setDescription(SPACE_DESCRIPTION + number);
    space.setVisibility(visible);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] { manager };
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedMember);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    return space;
  }

  @SneakyThrows
  private Space createMoreSpace(String spaceName) {
    Space space2 = new Space();
    space2.setDisplayName(spaceName);
    space2.setPrettyName(space2.getDisplayName());
    String shortName = Utils.cleanString(spaceName);
    space2.setGroupId(SPACES_GROUP_PREFIX + shortName);
    space2.setUrl(shortName);
    space2.setRegistration(Space.OPEN);
    space2.setDescription("This is my second space for testing");
    space2.setVisibility(Space.PUBLIC);
    space2.setEditor(ROOT_NAME);

    spaceService.createSpace(space2);

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

    spaceService.addMember(space, EXTERNAL_USER_NAME);
    assertTrue(spaceService.isSpaceContainsExternals(Long.valueOf(space.getId())));
  }

  public void testGetCommonSpaces() throws Exception {

    getSpaceInstance(11);
    Space space1 = getSpaceInstance(12);
    Space space2 = getSpaceInstance(13);
    Space space3 = getSpaceInstance(14);
    Space space4 = getSpaceInstance(15);
    Space space5 = getSpaceInstance(16);

    User otherUser = organizationService.getUserHandler().createUserInstance(OTHER_USER_NAME);
    organizationService.getUserHandler().createUser(otherUser, false);

    spaceService.addMember(space1, OTHER_USER_NAME);
    spaceService.addMember(space2, OTHER_USER_NAME);
    spaceService.addMember(space3, OTHER_USER_NAME);
    spaceService.addMember(space4, OTHER_USER_NAME);
    spaceService.addMember(space5, OTHER_USER_NAME);

    ListAccess<Space> resultListCommonSpacesAccessList1 = spaceService.getCommonSpaces(DEMO_NAME, OTHER_USER_NAME);
    assertEquals(5, resultListCommonSpacesAccessList1.getSize());
    Space[] spaceArray = resultListCommonSpacesAccessList1.load(0, 2);
    assertEquals(2, spaceArray.length);
    Space testSpace1 = spaceArray[0];
    assertEquals(space1, testSpace1);
    Space testSpace2 = spaceArray[1];
    assertEquals(space2, testSpace2);

    spaceArray = resultListCommonSpacesAccessList1.load(2, 2);
    assertEquals(2, spaceArray.length);
    Space testSpace3 = spaceArray[0];
    assertEquals(space3, testSpace3);
    Space testSpace4 = spaceArray[1];
    assertEquals(space4, testSpace4);

    spaceArray = resultListCommonSpacesAccessList1.load(4, 2);
    assertEquals(1, spaceArray.length);
    Space testSpace5 = spaceArray[0];
    assertEquals(space5, testSpace5);

  }

  public void testGenerateInvitationToken() throws Exception {

    Space space = createSpace("tokenTestSpace", ROOT_NAME);
    Long spaceId = Long.parseLong(space.getId());
    Long johnIdentityId = Long.parseLong(john.getId());

    String invitationUrl1 = spaceService.generateInvitationLink(spaceId, johnIdentityId);

    String token1 = extractTokenFromUrl(invitationUrl1);

    String[] parts = codecInitializer.getCodec().decode(token1).split(":");
    assertEquals(4, parts.length);
    assertEquals(String.valueOf(spaceId), parts[1]);
    assertEquals(String.valueOf(johnIdentityId), parts[2]);

    String invitationUrl2 = spaceService.generateInvitationLink(spaceId, johnIdentityId);
    String token2 = extractTokenFromUrl(invitationUrl2);
    assertNotEquals(token1, token2);
  }

  public void testJoinSpaceByInvitation() throws Exception {
    Space space = createSpace("invitationSpace", ROOT_NAME);
    space.setRegistration(Space.OPEN);
    spaceService.updateSpace(space);
    Long spaceId = Long.parseLong(space.getId());
    Long johnIdentityId = Long.parseLong(john.getId());

    // Generate a valid token
    String token = extractTokenFromUrl(spaceService.generateInvitationLink(spaceId, johnIdentityId));
    assertFalse(spaceService.isMember(space, MARY_NAME));

    spaceService.joinSpaceByInvitation(token, MARY_NAME);

    assertTrue(spaceService.isMember(spaceService.getSpaceById(space.getId()), MARY_NAME));

    Space closedSpace = createSpace("closedInvitationSpace", ROOT_NAME);
    closedSpace.setRegistration(Space.CLOSED);
    spaceService.updateSpace(closedSpace);
    String closedToken = extractTokenFromUrl(spaceService.generateInvitationLink(Long.parseLong(closedSpace.getId()), johnIdentityId));
    spaceService.joinSpaceByInvitation(closedToken, TOM_NAME);
    assertFalse(spaceService.isMember(spaceService.getSpaceById(closedSpace.getId()), TOM_NAME));

    assertThrows(IllegalArgumentException.class, () -> spaceService.joinSpaceByInvitation("invalid-token", MARY_NAME));

    String fakeToken = codecInitializer.getCodec().encode(System.currentTimeMillis() + ":999999:" + johnIdentityId + ":x");
    assertThrows(ObjectNotFoundException.class, () -> spaceService.joinSpaceByInvitation(fakeToken, MARY_NAME));
  }

  private String extractTokenFromUrl(String invitationUrl) {
    invitationUrl =  URLDecoder.decode(invitationUrl, StandardCharsets.UTF_8);
    int index = invitationUrl.indexOf("invitation_id=");
	  if (index == -1) {
	    throw new IllegalArgumentException("Invalid invitation URL: " + invitationUrl);
	  }
	  return invitationUrl.substring(index + "invitation_id=".length());
  }

  @SneakyThrows
  private void checkExternalUserMemberships() {
    Collection<Membership> internalMemberships = organizationService.getMembershipHandler()
                                                                    .findMembershipsByUserAndGroup(EXTERNAL_USER_NAME,
                                                                                                   "/platform/users");
    if (CollectionUtils.isNotEmpty(internalMemberships)) {
      internalMemberships.forEach(this::removeMembership);
    }
    Collection<Membership> externalMemberships = organizationService.getMembershipHandler()
                                                                    .findMembershipsByUserAndGroup(EXTERNAL_USER_NAME,
                                                                                                   "/platform/externals");
    if (CollectionUtils.isEmpty(externalMemberships)) {
      organizationService.getMembershipHandler()
                         .linkMembership(organizationService.getUserHandler().findUserByName(EXTERNAL_USER_NAME),
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

  private SpaceTemplate mockSpaceTemplate() throws ObjectNotFoundException {
    SpaceTemplateService spaceTemplateService = getService(SpaceTemplateService.class);
    SpaceTemplateStorage spaceTemplateStorage = getService(SpaceTemplateStorage.class);
    SpaceTemplateDAO spaceTemplateDao = new SpaceTemplateDAO();

    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplates().getFirst();
    if (spaceTemplateDao.find(spaceTemplate.getId()) == null) {
      SpaceTemplateEntity spaceTemplateEntity = EntityMapper.toEntity(spaceTemplate);
      spaceTemplateEntity.setId(null);
      spaceTemplateEntity = spaceTemplateDao.create(spaceTemplateEntity);
      if (spaceTemplate.getId() != spaceTemplateEntity.getId()) {
        spaceTemplate.setId(spaceTemplateEntity.getId());
        spaceTemplateStorage.updateSpaceTemplate(spaceTemplate);
      }
    }
    return spaceTemplate;
  }

  public static class SpaceTemplateDAO extends GenericDAOJPAImpl<SpaceTemplateEntity, Long> {
  }
}
