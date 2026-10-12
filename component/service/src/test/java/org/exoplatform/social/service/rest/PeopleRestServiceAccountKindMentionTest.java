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
package org.exoplatform.social.service.rest;

import java.util.Collection;

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.test.AbstractResourceTest;

import io.meeds.social.core.identity.plugin.TestAccountKindPlugin;
import io.meeds.social.core.identity.service.AccountKindService;

/**
 * Pins that an account of a kind, an enabled internal user, is suggested for a
 * mention like any other member.
 */
public class PeopleRestServiceAccountKindMentionTest extends AbstractResourceTest {

  private IdentityManager identityManager;

  private SpaceService    spaceService;

  private Identity        rootIdentity;

  private Identity        maryIdentity;

  /**
   * Resolves the services and registers the people REST resource.
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    addResource(PeopleRestService.class, null);
  }

  /**
   * Unregisters the people REST resource.
   */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(PeopleRestService.class);
  }

  /**
   * An account of a kind is an enabled internal user: a member of a space is
   * suggested for a mention in that space's stream whether or not it is an
   * account of a kind.
   */
  public void testAccountOfKindIsSuggestedForMentionInSpaceActivityStream() throws Exception {
    TestAccountKindPlugin.register(getContainer().getComponentInstanceOfType(AccountKindService.class));
    startSessionAs("root");
    Space space = new Space();
    space.setPrettyName("accountkindmentionspace");
    space.setDisplayName("accountkindmentionspace");
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.OPEN);
    space = spaceService.createSpace(space, rootIdentity.getRemoteId());
    spaceService.addMember(space, maryIdentity.getRemoteId());
    Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary").getProfile();
    Object previousKind = profile.getProperty(Profile.ACCOUNT_KIND);
    Object previousExternal = profile.getProperty(Profile.EXTERNAL);
    try {
      profile.setProperty(Profile.EXTERNAL, "false");
      identityManager.updateProfile(profile);
      assertTrue("a space member must be suggested for a mention", isSuggestedForMention("mary", space));

      profile.setProperty(Profile.ACCOUNT_KIND, TestAccountKindPlugin.KIND);
      identityManager.updateProfile(profile);
      assertEquals(TestAccountKindPlugin.KIND,
                   identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary").getAccountKind());
      assertTrue("an account of a kind must be suggested for a mention", isSuggestedForMention("mary", space));
    } finally {
      profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary").getProfile();
      profile.setProperty(Profile.ACCOUNT_KIND, previousKind);
      profile.setProperty(Profile.EXTERNAL, previousExternal);
      identityManager.updateProfile(profile);
      spaceService.deleteSpace(space);
    }
  }

  /**
   * Asks the people suggester for the mentions of a space's activity stream,
   * on behalf of root.
   *
   * @param userName the user expected among the suggestions, whose
   *          suggestion id is the user name prefixed with {@code @}
   * @param space the space whose stream is being written
   * @return true when the user is suggested
   */
  private boolean isSuggestedForMention(String userName, Space space) throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("username", "root");
    ContainerResponse response = service("GET",
                                         "/social/people/suggest.json?nameToSearch=m&currentUser=root&typeOfRelation=mention_activity_stream&spacePrettyName="
                                             + space.getPrettyName(),
                                         "",
                                         headers,
                                         null,
                                         new ByteArrayContainerResponseWriter());
    assertEquals(200, response.getStatus());
    return ((Collection<?>) response.getEntity()).stream()
                                                 .map(PeopleRestService.UserInfo.class::cast)
                                                 .anyMatch(userInfo -> ("@" + userName).equals(userInfo.getId()));
  }

}
