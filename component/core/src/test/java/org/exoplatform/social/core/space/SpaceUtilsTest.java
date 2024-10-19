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

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.EntityConverterUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link SpaceUtilsTest}
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jan 27, 2011
 * @since 1.2.0-GA
 */
public class SpaceUtilsTest extends AbstractCoreTest {

  private List<Space>     tearDown = new ArrayList<>();

  private Identity        rootIdentity;

  private IdentityStorage identityStorage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    String spaceDisplayName = "Space1";
    Space space1 = new Space();
    space1.setDisplayName(spaceDisplayName);
    space1.setPrettyName(space1.getDisplayName());
    String shortName = Utils.cleanString(spaceDisplayName);
    space1.setGroupId("/spaces/" + shortName);
    space1.setUrl(shortName);
    space1.setRegistration("validation");
    space1.setDescription("This is my first space for testing");
    space1.setVisibility("public");
    String[] manager = new String[] { "root" };
    String[] members = new String[] { "root", "demo", "john", "mary" };
    space1.setManagers(manager);
    space1.setMembers(members);

    spaceService.createSpace(space1, "root");
    tearDown.add(space1);
  }

  @Override
  protected void tearDown() throws Exception {
    for (Space space : tearDown) {
      spaceService.deleteSpace(space);
    }
    identityManager.deleteIdentity(rootIdentity);
    super.tearDown();
  }

  /**
   * Test {@link SpaceUtils#removeSpecialCharacterInSpaceFilter(String)}
   * 
   * @throws Exception
   * @since 1.2.2
   */
  public void testRemoveSpecialCharacter() {
    assertEquals("The filter should only filter special characters only",
                 "script alert 'Hello' script 100",
                 Utils.removeSpecialCharacterInSpaceFilter("<script>alert('Hello');</script> 100"));
    assertEquals("The filter should keep wildcard *,? and %",
                 "% * '?",
                 Utils.removeSpecialCharacterInSpaceFilter("( ) %{ } * [ ] \'? \""));
  }

  public void testProcessUnifiedSearchCondition() {
    String input = "spa~ce~0.5";
    assertEquals("spa~ce", Utils.processUnifiedSearchCondition(input));
    input = "space~0.5";
    assertEquals("space", Utils.processUnifiedSearchCondition(input));
    input = "space~0.5 test~0.5";
    assertEquals("space test", Utils.processUnifiedSearchCondition(input));
  }

  public void testUpdateDefaultUserAvatar() {
    FileItem avatarFile = identityStorage.getAvatarFile(rootIdentity);
    Profile profile = identityStorage.loadProfile(rootIdentity.getProfile());
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());

    Long avatarFileId = avatarFile.getFileInfo().getId();
    assertNotNull(avatarFileId);

    profile.setProperty("test", "test");
    identityStorage.updateProfile(profile);

    profile = identityStorage.loadProfile(profile);
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());

    assertEquals("Avatar File shouldn't change if User not renamed", avatarFileId, avatarFile.getFileInfo().getId());
  }
}
