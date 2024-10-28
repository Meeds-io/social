/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;

import io.meeds.social.space.constant.Registration;
import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.constant.Visibility;

public class SpaceMemberDAOTest extends BaseCoreTest {

  private List<IdentityEntity> toDeleteIdentities = new ArrayList<>();

  private SpaceDAO spaceDAO;
  private SpaceMemberDAO spaceMemberDAO;
  private IdentityDAO identityDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    spaceDAO = getService(SpaceDAO.class);
    spaceMemberDAO = getService(SpaceMemberDAO.class);
    identityDAO = getService(IdentityDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    spaceDAO.deleteAll();
    for (IdentityEntity e : toDeleteIdentities) {
      identityDAO.delete(e);
    }
    super.tearDown();
  }

  public void testGetUserSpaces() throws Exception {
    String userId = "test";
    String spacePrettyName = "test";

    IdentityEntity userIdentity = createIdentity(userId, OrganizationIdentityProvider.NAME);
    identityDAO.create(userIdentity);

    toDeleteIdentities.add(userIdentity);

    IdentityEntity spaceIdentity = createIdentity(spacePrettyName, SpaceIdentityProvider.NAME);
    identityDAO.create(spaceIdentity);
    toDeleteIdentities.add(spaceIdentity);

    List<Long> spacesIds = spaceMemberDAO.getSpacesIdsByUserName(userId, 0, 10);
    assertTrue(spacesIds == null || spacesIds.isEmpty());

    SpaceEntity space = createSpace("test");
    addIdentityToSpace(space, SpaceMembershipStatus.MANAGER, userId);
    addIdentityToSpace(space, SpaceMembershipStatus.MEMBER, userId);
    spaceDAO.create(space);

    spacesIds = spaceMemberDAO.getSpacesIdsByUserName(userId, 0, 10);
    assertEquals(1, spacesIds.size());

    space = createSpace("test2");
    addIdentityToSpace(space, SpaceMembershipStatus.MANAGER, userId);
    addIdentityToSpace(space, SpaceMembershipStatus.MEMBER, userId);
    spaceIdentity = createIdentity("test2", SpaceIdentityProvider.NAME);
    identityDAO.create(spaceIdentity);
    toDeleteIdentities.add(spaceIdentity);
    spaceDAO.create(space);

    spacesIds = spaceMemberDAO.getSpacesIdsByUserName(userId, 0, 10);
    assertEquals(2, spacesIds.size());

    space = createSpace("testspace2");
    addIdentityToSpace(space, SpaceMembershipStatus.MANAGER, userId);
    spaceIdentity = createIdentity("testspace2", SpaceIdentityProvider.NAME);
    identityDAO.create(spaceIdentity);
    toDeleteIdentities.add(spaceIdentity);
    spaceDAO.create(space);

    spacesIds = spaceMemberDAO.getSpacesIdsByUserName(userId, 0, 10);
    assertEquals(2, spacesIds.size());

    space = createSpace("testspace3");
    addIdentityToSpace(space, SpaceMembershipStatus.PENDING, userId);
    spaceIdentity = createIdentity("testspace3", SpaceIdentityProvider.NAME);
    identityDAO.create(spaceIdentity);
    toDeleteIdentities.add(spaceIdentity);
    spaceDAO.create(space);

    spacesIds = spaceMemberDAO.getSpacesIdsByUserName(userId, 0, 10);
    assertEquals(2, spacesIds.size());
  }

  private SpaceEntity createSpace(String name) {
    SpaceEntity spaceEntity = new SpaceEntity();    
    spaceEntity.setAvatarLastUpdated(new Date());
    spaceEntity.setDescription("testDesc");
    spaceEntity.setDisplayName(name);
    spaceEntity.setGroupId(name+"GroupId");
    spaceEntity.setPrettyName(name);
    spaceEntity.setRegistration(Registration.OPEN);
    spaceEntity.setUrl("testUrl");
    spaceEntity.setVisibility(Visibility.PRIVATE);
    spaceEntity.setBannerLastUpdated(new Date());
    return spaceEntity;
  }

  private void addIdentityToSpace(SpaceEntity spaceEntity, SpaceMembershipStatus status, String userId) {
    SpaceMemberEntity mem = new SpaceMemberEntity();
    mem.setSpace(spaceEntity);
    mem.setStatus(status);
    mem.setUserId(userId);
    mem.setCreatedDate(Instant.now());
    spaceEntity.getMembers().add(mem);
  }

  private IdentityEntity createIdentity(String id, String providerId) {
    IdentityEntity entity = new IdentityEntity();
    entity.setProviderId(providerId);
    entity.setRemoteId(id);
    entity.setEnabled(true);
    entity.setDeleted(false);
    return entity;
  }
}
