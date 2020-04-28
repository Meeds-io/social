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

package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.BaseESTest;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RelationshipManagerTestIT extends BaseESTest {
  private OrganizationService        organizationService;

  private List<User>                 tearDownUserList;

  public void setUp() throws Exception {
    super.setUp();
    identityManager = getService(IdentityManager.class);
    relationshipManager = getService(RelationshipManager.class);
    organizationService = getService(OrganizationService.class);
    relationshipManager = getService(RelationshipManager.class);
    tearDownUserList = new ArrayList<User>();
  }

  public void tearDown() throws Exception {
    for (User user : tearDownUserList) {
      organizationService.getUserHandler().removeUser(user.getUserName(), true);
    }

    super.tearDown();
  }

  /**
   * Tests
   * {@link IdentityManager#getIdentitiesForMentionsCount(String, ProfileFilter, Relationship.Type)}
   */
  @SuppressWarnings("deprecation")
  public void testCountIdentitiesForMentions() throws Exception {

    String providerId = OrganizationIdentityProvider.NAME;

    Identity identity1 = null;
    int total = 10;
    int incoming = 0;
    int outgoing = 0;
    int connections = 0;

    for (int i = 0; i < total; i++) {
      String remoteId = "username" + i;
      User user = organizationService.getUserHandler().createUserInstance(remoteId);
      user.setFirstName("Prénom" + i);
      user.setLastName("Nom" + i);
      user.setEmail("user" + i + "@exemple.com");
      user.setPassword("testuser");
      user.setCreatedDate(new Date());
      organizationService.getUserHandler().createUser(user, true);
      tearDownUserList.add(user);

      Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
      if (identity1 == null) {
        identity1 = identity;
      } else {
        if (i % 3 == 0) {
          relationshipManager.inviteToConnect(identity1, identity);
          outgoing++;
        } else if (i % 3 == 1) {
          relationshipManager.inviteToConnect(identity, identity1);
          incoming++;
        } else {
          relationshipManager.inviteToConnect(identity, identity1);
          relationshipManager.confirm(identity1, identity);
          connections++;
        }
      }

      Profile profile = identity.getProfile();
      profile.setProperty(Profile.FIRST_NAME, "Prénom" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName");
      profile.setProperty(Profile.FULL_NAME, "Prénom" + i + " " + "LastName" + i);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");
      identityManager.updateProfile(profile);

      reindexProfileById(identity.getId());
    }

    ProfileFilter filter = new ProfileFilter();
    filter.setName("prenom");
    ListAccess<Identity> listAccess = relationshipManager.getConnectionsByFilter(identity1, filter);
    int size = listAccess.getSize();
    assertEquals(connections, size);
    assertEquals(listAccess.load(0, size).length, connections);

    listAccess = relationshipManager.getIncomingByFilter(identity1, filter);
    size = listAccess.getSize();
    assertEquals(size, incoming);
    assertEquals(listAccess.load(0, size).length, incoming);
    listAccess = relationshipManager.getOutgoingByFilter(identity1, filter);
    size = listAccess.getSize();
    assertEquals(size, outgoing);
    assertEquals(listAccess.load(0, size).length, outgoing);
  }
}
