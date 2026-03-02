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
package org.exoplatform.social.rest.impl.spacemembership;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;

import io.meeds.social.space.invitation.storage.SpaceInvitationLinkStorage;
import io.meeds.social.space.service.SpaceServiceImpl;
import org.apache.commons.lang3.ArrayUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SpaceMembershipRestResourcesTest extends AbstractResourceTest {

  private static final String INVITED                      = "invited";

  private static final String SPACE6                       = "space6";

  private static final String SPACE5                       = "space5";

  private static final String SPACE4                       = "space4";

  private static final String SPACE3                       = "space3";

  private static final String SPACE2                       = "space2";

  private static final String IGNORED                      = "ignored";

  private static final String SPACES_MEMBERSHIPS_SPACE_URL = "spacesMemberships?space=";

  private static final String SPACES_MEMBERSHIPS_URL       = "spacesMemberships";

  private static final String SPACE1                       = "space1";

  private SpaceService        spaceService;

  private SpaceMembershipRest membershipRestResources;

  private SpaceInvitationLinkStorage spaceInvitationLinkStorage;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    spaceInvitationLinkStorage = mock(SpaceInvitationLinkStorage.class);
    Field spaceStorageField = SpaceServiceImpl.class.getDeclaredField("spaceInvitationLinkStorage");
    spaceStorageField.setAccessible(true);
    spaceStorageField.set(spaceService, spaceInvitationLinkStorage);

    Identity rootIdentity = identityManager.getOrCreateUserIdentity("root");
    Identity johnIdentity = identityManager.getOrCreateUserIdentity("john");
    Identity maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    Identity demoIdentity = identityManager.getOrCreateUserIdentity("demo");

    Stream.of(rootIdentity, johnIdentity, maryIdentity, demoIdentity).filter(Objects::nonNull).forEach(identity -> {
      identity.setDeleted(false);
      identity.setEnable(true);
      identityManager.updateIdentity(identity);
    });
    // root creates 2 spaces, john 1 and mary 3
    createSpaceIfNotExist(1, "root", Space.CLOSED);
    createSpaceIfNotExist(2, "root", Space.VALIDATION, Space.HIDDEN);
    createSpaceIfNotExist(3, "john");
    createSpaceIfNotExist(4, "mary");
    createSpaceIfNotExist(5, "mary");
    createSpaceIfNotExist(6, "mary");

    membershipRestResources = new SpaceMembershipRest(spaceService, identityManager);
    registry(membershipRestResources);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(membershipRestResources.getClass());
  }

  public void testGetSpacesMembershipsOfCurrentUser() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service(HttpMethod.GET, getURLResource(SPACES_MEMBERSHIPS_URL), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    response = service(HttpMethod.GET, getURLResource(SPACES_MEMBERSHIPS_URL + "?limit=1"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET, getURLResource(SPACES_MEMBERSHIPS_URL + "?user=root&offset=1"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    startSessionAs("john");
    response = service(HttpMethod.GET, getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(5) + "&user=john"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    response = service(HttpMethod.GET, getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(5)), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(5) + "&status=manager"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testGetSpaceMembershipsOfAnotherUserAsANonSpacesAdministrator() throws Exception {
    startSessionAs("mary");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource(SPACES_MEMBERSHIPS_URL + "?user=john"),
                                         "",
                                         null,
                                         null,
                                         "mary");
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }

  public void testGetSpaceMembershipsOfAnotherUserAsASpacesAdministrator() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource("spacesMemberships?user=mary&status=member"),
                                         "",
                                         null,
                                         null,
                                         "root");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());

    response = service(HttpMethod.GET, getURLResource("spacesMemberships?user=mary&status=manager"), "", null, null, "root");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());
  }

  public void testGetSpaceMembershipsOfASpaceAsANonMember() throws Exception {
    startSessionAs("mary");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(2)),
                                         "",
                                         null,
                                         null,
                                         "mary");
    assertNotNull(response);
    assertEquals(401, response.getStatus());
    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(1)),
                       "",
                       null,
                       null,
                       "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(3)),
                       "",
                       null,
                       null,
                       "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGetSpaceMembershipsOfASpaceAsAPendingUser() throws Exception {
    startSessionAs("root");
    ContainerResponse response = getResponse(HttpMethod.POST,
                                             getURLResource(SPACES_MEMBERSHIPS_URL),
                                             getJsonStatusInput(getSpaceId(1), "demo", INVITED));
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    Space space = spaceService.getSpaceByPrettyName(SPACE1);
    spaceService.addPendingUser(space, "mary");

    startSessionAs("mary");
    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(1) + "&user=mary"),
                       "",
                       null,
                       null,
                       "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity spacesMemberships = (CollectionEntity) response.getEntity();
    assertNotNull(spacesMemberships);
    assertNotNull(spacesMemberships.getEntities());
    assertEquals(0, spacesMemberships.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(1) + "&user=mary&status=pending"),
                       "",
                       null,
                       null,
                       "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    spacesMemberships = (CollectionEntity) response.getEntity();
    assertNotNull(spacesMemberships);
    assertNotNull(spacesMemberships.getEntities());
    assertEquals(1, spacesMemberships.getEntities().size());

    DataEntity data = spacesMemberships.getEntities().get(0);
    assertEquals("space1:mary:pending", data.get("id"));
  }

  public void testGetSpaceMembershipsOfASpaceAsAnInvitedUser() throws Exception {
    startSessionAs("root");
    ContainerResponse response = getResponse(HttpMethod.POST,
                                             getURLResource(SPACES_MEMBERSHIPS_URL),
                                             getJsonRoleInput(getSpaceId(1), "demo", SpaceUtils.MEMBER));
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    Space space = spaceService.getSpaceByPrettyName(SPACE1);
    spaceService.addInvitedUser(space, "mary");

    startSessionAs("mary");
    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(1) + "&user=mary"),
                       "",
                       null,
                       null,
                       "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity spacesMemberships = (CollectionEntity) response.getEntity();
    assertNotNull(spacesMemberships);
    assertNotNull(spacesMemberships.getEntities());
    assertEquals(0, spacesMemberships.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(1) + "&user=mary&status=invited"),
                       "",
                       null,
                       null,
                       "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    spacesMemberships = (CollectionEntity) response.getEntity();
    assertNotNull(spacesMemberships);
    assertNotNull(spacesMemberships.getEntities());
    assertEquals(1, spacesMemberships.getEntities().size());
    DataEntity data = spacesMemberships.getEntities().get(0);
    assertEquals("space1:mary:invited", data.get("id"));
  }

  public void testGetSpaceMembershipsOfASpaceAsManager() throws Exception {
    startSessionAs("mary");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(4) + "&user=mary"),
                                         "",
                                         null,
                                         null,
                                         "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_SPACE_URL + getSpaceId(4)),
                       "",
                       null,
                       null,
                       "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testAddSpaceMembership() throws Exception {
    // root add demo as member of his space
    startSessionAs("root");
    ContainerResponse response = getResponse(HttpMethod.POST, getURLResource(SPACES_MEMBERSHIPS_URL), "{\"user\":demo}");
    assertNotNull(response);
    assertEquals(400, response.getStatus());

    response = getResponse(HttpMethod.POST, getURLResource(SPACES_MEMBERSHIPS_URL), "{\"space\":15523, \"user\":demo}");
    assertNotNull(response);
    assertEquals(400, response.getStatus());

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           "{\"space\":" + getSpaceId(1) + ", \"user\":demoxx}");
    assertNotNull(response);
    assertEquals(400, response.getStatus());

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonStatusInput(getSpaceId(1), "john", "pending"));
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "john", IGNORED));
    assertNotNull(response);
    assertEquals(400, response.getStatus());

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonStatusInput(getSpaceId(1), "root", IGNORED));
    assertNotNull(response);
    assertEquals(409, response.getStatus());

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonStatusInput(getSpaceId(1), "root", INVITED));
    assertNotNull(response);
    assertEquals(409, response.getStatus());

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "demo", SpaceUtils.MEMBER));
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    Space space = spaceService.getSpaceByPrettyName(SPACE1);
    assertTrue(ArrayUtils.contains(space.getMembers(), "demo"));

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "john", "redactor"));
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    space = spaceService.getSpaceByPrettyName(SPACE1);
    assertTrue(ArrayUtils.contains(space.getRedactors(), "john"));
    assertFalse(ArrayUtils.contains(space.getRedactors(), "demo"));
    assertEquals(1, space.getRedactors().length);

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "demo", "publisher"));
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    space = spaceService.getSpaceByPrettyName(SPACE1);

    assertTrue(ArrayUtils.contains(space.getPublishers(), "demo"));
    assertFalse(ArrayUtils.contains(space.getPublishers(), "john"));
    assertEquals(1, space.getPublishers().length);

    // demo add mary as member of space1 but has no permission
    startSessionAs("demo");
    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "mary", SpaceUtils.MEMBER));
    assertEquals(401, response.getStatus());

    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonStatusInput(getSpaceId(1), "demo", IGNORED));
    assertNotNull(response);
    assertEquals(409, response.getStatus());

    startSessionAs("mary");
    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonStatusInput(getSpaceId(1), "mary", IGNORED));
    assertNotNull(response);
    assertEquals(204, response.getStatus());
  }

  public void testGetUpdateDeleteSpaceMembership() throws Exception {
    // root creates 1 space
    spaceService.addMember(spaceService.getSpaceByPrettyName(SPACE1), "demo");

    // root add demo as member of his space
    startSessionAs("root");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource(SPACES_MEMBERSHIPS_URL + "?space=" + getSpaceId(1) +
                                             "&user=demo&status=member"),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    // update demo to manager
    response = getResponse(HttpMethod.POST,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "demo", "manager"));
    assertEquals(204, response.getStatus());

    Space space1 = spaceService.getSpaceByPrettyName(SPACE1);
    assertTrue(spaceService.isManager(space1, "demo"));

    // delete membership of demo from space1
    response = getResponse(HttpMethod.DELETE,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "demo", "member"));
    assertEquals(204, response.getStatus());
    assertFalse(spaceService.isMember(spaceService.getSpaceByPrettyName(SPACE1), "demo"));
    assertFalse(spaceService.isManager(spaceService.getSpaceByPrettyName(SPACE1), "demo"));

    spaceService.addRedactor(spaceService.getSpaceByPrettyName(SPACE1), "demo");
    response = getResponse(HttpMethod.DELETE,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "demo", "redactor"));
    assertEquals(204, response.getStatus());
    assertFalse(spaceService.isRedactor(spaceService.getSpaceByPrettyName(SPACE1), "demo"));

    spaceService.addPublisher(spaceService.getSpaceByPrettyName(SPACE1), "demo");
    response = getResponse(HttpMethod.DELETE,
                           getURLResource(SPACES_MEMBERSHIPS_URL),
                           getJsonRoleInput(getSpaceId(1), "demo", "publisher"));
    assertEquals(204, response.getStatus());
    assertFalse(spaceService.isPublisher(spaceService.getSpaceByPrettyName(SPACE1), "demo"));
  }

  public void testGetMemberSpaceMemberships() throws Exception {
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE1), "demo");
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE2), "demo");
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE3), "demo");
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE4), "demo");
    spaceService.addPendingUser(spaceService.getSpaceByPrettyName(SPACE5), "demo");
    spaceService.addMember(spaceService.getSpaceByPrettyName(SPACE6), "demo");

    startSessionAs("demo");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource(SPACES_MEMBERSHIPS_URL + "?status=approved&limit=3"),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_URL + "?status=approved&space=" + getSpaceId(2) +
                           "&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_URL + "?status=approved&space=" + getSpaceId(2) +
                           "&user=demo&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_URL + "?status=invited&space=" + getSpaceId(4) +
                           "&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_URL + "?status=approved&space=" + getSpaceId(4) +
                           "&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    
    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_URL + "?status=approved&space=" + getSpaceId(6) +
                           "&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_URL + "?status=approved&space=" + getSpaceId(6) +
                           "&offset=3&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource(SPACES_MEMBERSHIPS_URL + "?status=approved&space=" + getSpaceId(6) +
                           "&user=demo&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testGetInvitedSpaceMemberships() throws Exception {
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE1), "demo");
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE2), "demo");
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE3), "demo");
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE4), "demo");
    spaceService.addPendingUser(spaceService.getSpaceByPrettyName(SPACE5), "demo");
    spaceService.addMember(spaceService.getSpaceByPrettyName(SPACE6), "demo");

    startSessionAs("demo");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource("spacesMemberships?status=invited&limit=3"),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource("spacesMemberships?status=invited&space=" + getSpaceId(5) +
                           "&user=demo&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource("spacesMemberships?status=invited&space=" + getSpaceId(4) +
                           "&user=demo&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource("spacesMemberships?status=invited&space=" + getSpaceId(6) +
                           "&user=demo&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());
  }

  public void testGetPendingSpaceMemberships() throws Exception {
    spaceService.addPendingUser(spaceService.getSpaceByPrettyName(SPACE1), "demo");
    spaceService.addPendingUser(spaceService.getSpaceByPrettyName(SPACE2), "demo");
    spaceService.addPendingUser(spaceService.getSpaceByPrettyName(SPACE3), "demo");
    spaceService.addPendingUser(spaceService.getSpaceByPrettyName(SPACE4), "demo");
    spaceService.addInvitedUser(spaceService.getSpaceByPrettyName(SPACE5), "demo");
    spaceService.addMember(spaceService.getSpaceByPrettyName(SPACE6), "demo");

    startSessionAs("demo");
    ContainerResponse response = service(HttpMethod.GET,
                                         getURLResource("spacesMemberships?status=pending&user=demo&limit=3"),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource("spacesMemberships?status=pending&user=demo&space=" + getSpaceId(5) +
                           "&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource("spacesMemberships?status=pending&user=demo&space=" + getSpaceId(4) +
                           "&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service(HttpMethod.GET,
                       getURLResource("spacesMemberships?status=pending&user=demo&space=" + getSpaceId(6) +
                           "&limit=3"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());
  }

  public void testGenerateInvitationToken() throws Exception {
    String invitationTokenUrl = SPACES_MEMBERSHIPS_URL + "/invitationLink";

    startSessionAs("root");
    ContainerResponse response = service(HttpMethod.GET, getURLResource(invitationTokenUrl), "", null, null);
    assertEquals(400, response.getStatus());

    response = service(HttpMethod.GET, getURLResource(invitationTokenUrl + "?spaceId=99999"), "", null, null);
    assertEquals(404, response.getStatus());

    startSessionAs("mary");
    response = service(HttpMethod.GET, getURLResource(invitationTokenUrl + "?spaceId=" + getSpaceId(1)), "", null, null);
    assertEquals(200, response.getStatus());

    startSessionAs("root");
    response = service(HttpMethod.GET, getURLResource(invitationTokenUrl + "?spaceId=" + getSpaceId(1)), "", null, null);
    assertEquals(200, response.getStatus());

    String token = (String) response.getEntity();
    assertNotNull(token);
    assertFalse(token.isBlank());
    assertTrue(token.startsWith(CommonsUtils.getCurrentDomain() + "/portal/s/" + getSpaceId(1) + "?invitation_id="));

    ContainerResponse response2 = service(HttpMethod.GET, getURLResource(invitationTokenUrl + "?spaceId=" + getSpaceId(1)), "", null, null);
    assertEquals(200, response2.getStatus());

    String token2 = (String) response2.getEntity();
    assertNotNull(token2);
    assertNotEquals(token, token2);
  }

  public void testAddSpacesMembershipWithInvitationToken() throws Exception {
    // Generate a real invitation link as root (manager of space1)
    startSessionAs("root");
    ContainerResponse linkResponse = service(HttpMethod.GET,
      getURLResource(SPACES_MEMBERSHIPS_URL + "/invitationLink?spaceId=" + getSpaceId(1)), "", null, null);
    assertNotNull(linkResponse);
    assertEquals(200, linkResponse.getStatus());

    String invitationUrl = (String) linkResponse.getEntity();
    assertNotNull(invitationUrl);
    assertFalse(invitationUrl.isBlank());
    assertTrue(invitationUrl.contains("invitation_id="));

    // Extract the URL-encoded token from the URL, then decode it for use in JSON
    // body
    String encodedToken = invitationUrl
        .substring(invitationUrl.indexOf("invitation_id=") + "invitation_id=".length());
    assertFalse(encodedToken.isBlank());
    String invitationToken = URLDecoder.decode(encodedToken, StandardCharsets.UTF_8);

    // demo tries to join space1 (VALIDATION registration) with the invitation token
    startSessionAs("demo");
    String jsonInput = String.format(
       "{\"space\":\"%s\", \"user\":\"demo\", \"status\":\"pending\", \"invitationToken\":\"%s\"}",
       getSpaceId(1),
    // Escape any backslashes or quotes in the token for valid JSON
    invitationToken.replace("\\", "\\\\").replace("\"", "\\\""));

    ContainerResponse response = getResponse(HttpMethod.POST, getURLResource(SPACES_MEMBERSHIPS_URL), jsonInput);
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    // Verify that demo is now a pending member of space1
    Space space = spaceService.getSpaceByPrettyName(SPACE1);
    assertTrue(spaceService.isPendingUser(space, "demo"));

    verify(spaceInvitationLinkStorage).saveInvitationLink(argThat(invitation -> invitation != null
       && Long.parseLong(space.getId()) == invitation.getSpaceId()
       && "demo".equals(invitation.getInvitedUserId())));
	}

  private void createSpaceIfNotExist(int index, String creator) throws Exception {
    createSpaceIfNotExist(index, creator, Space.OPEN);
  }

  private void createSpaceIfNotExist(int index, String creator, String registration) throws Exception {
    createSpaceIfNotExist(index, creator, Space.OPEN, Space.PRIVATE);
  }

  private void createSpaceIfNotExist(int index, String creator, String registration, String visibility) throws Exception {
    String spaceName = "space" + index;
    if (spaceService.getSpaceByPrettyName(spaceName) == null) {
      Space space = new Space();
      space.setDisplayName(spaceName);
      space.setPrettyName(space.getDisplayName());
      space.setRegistration(registration);
      space.setDescription("add new space " + index);
      space.setVisibility(visibility);
      space.setRegistration(Space.VALIDATION);
      this.spaceService.createSpace(space, creator);
    }
  }

  private String getJsonRoleInput(String spaceId, String username, String role) {
    return String.format("{\"space\":\"%s\", \"user\":\"%s\", \"role\":\"%s\"}",
                         spaceId,
                         username,
                         role);
  }

  private String getJsonStatusInput(String spaceId, String username, String status) {
    return String.format("{\"space\":\"%s\", \"user\":\"%s\", \"status\":\"%s\"}",
                         spaceId,
                         username,
                         status);
  }

  private String getSpaceId(int index) {
    return spaceService.getSpaceByPrettyName("space" + index).getId();
  }

}
