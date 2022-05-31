/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.core.service;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class LinkProviderTest extends AbstractCoreTest {
  private IdentityManager identityManager;
  private IdentityStorage identityStorage;

  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    identityStorage = (IdentityStorage) this.getContainer().getComponentInstanceOfType(IdentityStorage.class);

    assertNotNull(identityManager);
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   *
   * @throws Exception
   */
  public void testGetProfileLink() throws Exception {
    final String portalOwner = "classic";

    IdentityManager identityManger = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    Identity rootIdentity = identityManger.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    String rootFullName = rootIdentity.getProfile().getFullName();
    assertNotNull("rootFullName must not be null.", rootFullName);
    // but when we have the identity we generate a link
    String actualLink = LinkProvider.getProfileLink(rootIdentity.getRemoteId(), portalOwner);
    Object external = rootIdentity.getProfile().getProperty(Profile.EXTERNAL);
    String expected = "<a class=\"user-suggester\" href=\"/portal/" + portalOwner + "/profile/" +
        rootIdentity.getRemoteId() + "\" "
        + "target=\"_parent\" v-identity-popover=\"{id: '" + rootIdentity.getId() + "',username: '" + rootIdentity.getRemoteId() + "',fullName: '"
        + rootIdentity.getProfile().getFullName() + "',avatar: '" + rootIdentity.getProfile().getAvatarUrl() + "',position: '"
        + StringUtils.trimToEmpty(rootIdentity.getProfile().getPosition()) + "',external: '" + (external == null ? "false" : external)
        + "',}\">" + rootFullName + "</a>";
    assertEquals(expected, actualLink);

    identityManager.deleteIdentity(rootIdentity);
  }
}
