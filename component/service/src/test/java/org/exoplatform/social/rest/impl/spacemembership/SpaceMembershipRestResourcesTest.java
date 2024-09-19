package org.exoplatform.social.rest.impl.spacemembership;

import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;

import org.apache.commons.lang3.ArrayUtils;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

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

  @Override
  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    Identity rootIdentity = identityManager.getOrCreateUserIdentity("root");
    Identity johnIdentity = identityManager.getOrCreateUserIdentity("john");
    Identity maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    Identity demoIdentity = identityManager.getOrCreateUserIdentity("demo");

    Stream.of(rootIdentity, johnIdentity, maryIdentity, demoIdentity).forEach(identity -> {
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
      space.setPriority(Space.INTERMEDIATE_PRIORITY);
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
