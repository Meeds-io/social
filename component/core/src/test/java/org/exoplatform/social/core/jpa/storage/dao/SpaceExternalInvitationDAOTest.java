/*
 * Copyright (C) 2020 eXo Platform SAS.
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

import org.exoplatform.social.core.jpa.storage.entity.*;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;

import java.util.*;

public class SpaceExternalInvitationDAOTest extends BaseCoreTest {

    private List<SpaceExternalInvitationEntity> toDeleteIdentities = new ArrayList<SpaceExternalInvitationEntity>();

    private SpaceExternalInvitationDAO spaceExternalInvitationDAO;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        spaceExternalInvitationDAO = getService(SpaceExternalInvitationDAO.class);
    }

    @Override
    public void tearDown() throws Exception {
        for (SpaceExternalInvitationEntity e : toDeleteIdentities) {
            spaceExternalInvitationDAO.delete(e);
        }
        super.tearDown();
    }

    public void testFindSpaceExternalInvitationsBySpaceId() {
        SpaceExternalInvitationEntity spaceExternalInvitationEntity = new SpaceExternalInvitationEntity();
        spaceExternalInvitationEntity.setSpaceId("1");
        spaceExternalInvitationEntity.setUserEmail("user@user.com");
        spaceExternalInvitationEntity.setTokenId("token");
        SpaceExternalInvitationEntity spaceExternalInvitationEntity1 = new SpaceExternalInvitationEntity();
        spaceExternalInvitationEntity1.setSpaceId("1");
        spaceExternalInvitationEntity1.setUserEmail("user1@user1.com");
        spaceExternalInvitationEntity1.setTokenId("token1");
        spaceExternalInvitationDAO.create(spaceExternalInvitationEntity);
        spaceExternalInvitationDAO.create(spaceExternalInvitationEntity1);
        toDeleteIdentities.add(spaceExternalInvitationEntity1);
        //When
        List<SpaceExternalInvitationEntity> externalSpaceInvitations = spaceExternalInvitationDAO.findSpaceExternalInvitationsBySpaceId("1");

        //Then
        assertEquals(2, externalSpaceInvitations.size());

        spaceExternalInvitationDAO.deleteExternalUserInvitations("user@user.com");

        //When
        List<SpaceExternalInvitationEntity> externalSpaceInvitations1 = spaceExternalInvitationDAO.findSpaceExternalInvitationsBySpaceId("1");

        //Then
        assertEquals(1, externalSpaceInvitations1.size());
    }

    public void testFindExternalInvitationsSpacesByEmail() {
        SpaceExternalInvitationEntity spaceExternalInvitationEntity = new SpaceExternalInvitationEntity();
        spaceExternalInvitationEntity.setSpaceId("2");
        spaceExternalInvitationEntity.setUserEmail("user2@user2.com");
        spaceExternalInvitationEntity.setTokenId("token");
        SpaceExternalInvitationEntity spaceExternalInvitationEntity1 = new SpaceExternalInvitationEntity();
        spaceExternalInvitationEntity1.setSpaceId("3");
        spaceExternalInvitationEntity1.setUserEmail("user2@user2.com");
        spaceExternalInvitationEntity1.setTokenId("token1");
        SpaceExternalInvitationEntity spaceExternalInvitationEntity2 = new SpaceExternalInvitationEntity();
        spaceExternalInvitationEntity2.setSpaceId("4");
        spaceExternalInvitationEntity2.setUserEmail("user2@user2.com");
        spaceExternalInvitationEntity2.setTokenId("token2");
        spaceExternalInvitationDAO.create(spaceExternalInvitationEntity);
        spaceExternalInvitationDAO.create(spaceExternalInvitationEntity1);
        spaceExternalInvitationDAO.create(spaceExternalInvitationEntity2);

        //When
        List<String> spaceIds = spaceExternalInvitationDAO.findExternalInvitationsSpacesByEmail("user2@user2.com");

        //Then
        assertEquals(3, spaceIds.size());

        spaceExternalInvitationDAO.deleteExternalUserInvitations("user2@user2.com");

        //When
        List<String> spaceIds1 = spaceExternalInvitationDAO.findExternalInvitationsSpacesByEmail("user2@user2.com");

        //Then
        assertEquals(0, spaceIds1.size());
    }
}	