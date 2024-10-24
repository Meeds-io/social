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

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.EntityConverterUtils;
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

  private IdentityStorage identityStorage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
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
    Identity jamesIdentity = identityManager.getOrCreateUserIdentity("james");
    FileItem avatarFile = identityStorage.getAvatarFile(jamesIdentity);
    Profile profile = identityStorage.loadProfile(jamesIdentity.getProfile());
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
