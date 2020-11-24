package org.exoplatform.social.rest.impl.space;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.application.registry.Application;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ActivityFile;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.mock.MockUploadService;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.upload.UploadService;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SpaceRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private UserACL userACL;
  private ActivityManager activityManager;
  private SpaceService spaceService;

  private SpaceRestResourcesV1 spaceRestResources;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  private MockUploadService    uploadService;

  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);

    rootIdentity = identityManager.getOrCreateIdentity("organization", "root");
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john");
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary");
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo");

    spaceRestResources = new SpaceRestResourcesV1(spaceService, identityManager, uploadService);
    registry(spaceRestResources);
  }

  public void tearDown() throws Exception {
    // TODO
    /*
    for (ExoSocialActivity activity : tearDownActivitiesList) {
      activityManager.deleteActivity(activity);
    }
    */

    super.tearDown();
    removeResource(spaceRestResources.getClass());
  }
public void testSpaceDisplayNameUpdateWithDifferentCases () throws Exception {
  startSessionAs("root");
  Space space = getSpaceInstance(1, "root");
  ContainerResponse response = null;
  String inputWithCorrectName = "{\"displayName\":\"social\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
  String inputWithSpecialCharacter = "{\"displayName\":\"~#Social~#\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
  String inputWithAndCharacter = "{\"displayName\":\"social & social\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";

  response = getResponse("PUT", getURLResource("spaces/" + space.getId()), inputWithCorrectName);
  assertEquals(200, response.getStatus());
  response = getResponse("PUT", getURLResource("spaces/" + space.getId()), inputWithSpecialCharacter);
  assertEquals(500, response.getStatus());
  response = getResponse("PUT", getURLResource("spaces/" + space.getId()), inputWithAndCharacter);
  assertEquals(200, response.getStatus());
}
  public void testSpaceVisibilityUpdateWithDifferentCases () throws Exception {
    startSessionAs("root");
    /*
    *
    * Test of 'private' and 'hidden' fields with a mix of upper/lower cases
    */

    Space space = getSpaceInstance(1, "root");

    Map<String,String> listOfResponses = new HashMap<String,String>() {{
      put("{\"displayName\":\"social\",\"visibility\":PRIVATE}", Space.PRIVATE);
      put("{\"displayName\":\"social\",\"visibility\":private}", Space.PRIVATE);
      put("{\"displayName\":\"social\",\"visibility\":PriVatE}", Space.PRIVATE);
      put("{\"displayName\":\"social\",\"visibility\":HIDDEN}", Space.HIDDEN);
      put("{\"displayName\":\"social\",\"visibility\":hidden}", Space.HIDDEN);
      put("{\"displayName\":\"social\",\"visibility\":HiDdEn}", Space.HIDDEN);
    }};

    ContainerResponse response = null;

    for (Map.Entry<String, String> entry : listOfResponses.entrySet()) {
      String input = entry.getKey();
      String expectedOutput = entry.getValue();
      response = getResponse("PUT", getURLResource("spaces/" + space.getId()), input);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
      assertEquals(expectedOutput, spaceEntity.getVisibility());
    }
  }

  public void testGetSpaces() throws Exception {
    Space rootSpace = getSpaceInstance(1, "root");
    Space johnSpace = getSpaceInstance(2, "john");
    Space demoSpace = getSpaceInstance(3, "demo");

    this.spaceService.addPendingUser(demoSpace, "john");
    this.spaceService.addPendingUser(rootSpace, "demo");
    this.spaceService.addPendingUser(johnSpace, "root");

    startSessionAs("demo");
    ContainerResponse response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    // demo is member of only one space then he got just 1 result
    assertEquals(3, collections.getEntities().size());

    response = service("GET", getURLResource("spaces?limit=5&offset=0&filterType=requests"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service("GET", getURLResource("spaces?limit=5&offset=1&filterType=requests"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    response = service("GET", getURLResource("spaces?limit=5&offset=0&filterType=member"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    HashSet<MembershipEntry> ms = new HashSet<MembershipEntry>();
    ms.add(new MembershipEntry("/platform/administrators"));
    startSessionAs("john", ms);
    response = service("GET", getURLResource("spaces?limit=5&offset=0&filterType=member"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // Only the super user can see all the spaces.
    startSessionAs("root", ms);
    response = service("GET", getURLResource("spaces?limit=5&offset=0&filterType=member"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service("GET", getURLResource("spaces?limit=5&offset=1&filterType=member"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());

    response = service("GET", getURLResource("spaces?limit=5&offset=1"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }

  public void testShouldUseCacheWhenSpacesDidNotChanged() throws Exception {
    getSpaceInstance(1, "root");
    getSpaceInstance(2, "john");
    getSpaceInstance(3, "demo");

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);

    MultivaluedMap<String,String> headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTag.getValue() + "\"");
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    response = service("GET", getURLResource("spaces?limit=5&offset=0&filterType=member"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testCreateSpace() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"social\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    //root try to update demo activity
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);
    assertEquals("social", space.getDisplayName());
  }

  public void testCreateSpaceWithNonLatinName() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"Благодійність\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    //root try to update demo activity
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);
    assertEquals("Благодійність", space.getDisplayName());
    assertEquals("blagodijnist", space.getPrettyName());
    assertEquals("blagodijnist", space.getUrl());
  }

  public void testGetSpaceById() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"test space\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    //root creates a space
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);

    // Get space by its id
    response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity);
    assertEquals("test space", spaceEntity.getDisplayName());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);
  }

  public void testGetSpaceByPrettyName() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"test space\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    // root creates a space
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);

    // Get space by its pretty name
    response = service("GET", getURLResource("spaces/byPrettyName/" + space.getPrettyName()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity);
    assertEquals("test space", spaceEntity.getDisplayName());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);
  }

  public void testGetSpaceByDisplayName() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"test space\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    // root creates a space
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);

    // Get space by its display name
    response = service("GET", getURLResource("spaces/byDisplayName/" + space.getDisplayName().replaceAll(" ", "%20")), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity);
    assertEquals("test space", spaceEntity.getDisplayName());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);
  }

  public void testGetUpdateDeleteSpaceById() throws Exception {
    //root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    space.setVisibility(Space.HIDDEN);
    space.setRegistration(Space.CLOSE);
    space = spaceService.updateSpace(space);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertEquals("space1", spaceEntity.getDisplayName());
    assertEquals(Space.HIDDEN, spaceEntity.getVisibility());
    assertEquals(Space.CLOSE, spaceEntity.getSubscription());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);

    //root update space's description and name
    String spaceId = spaceEntity.getId();
    String input = "{\"displayName\":displayName_updated, \"description\":description_updated}";
    response = getResponse("PUT", getURLResource("spaces/" + spaceId), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    response = service("GET", getURLResource("spaces/" + spaceId), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertEquals("displayName_updated", spaceEntity.getDisplayName());
    assertEquals("description_updated", spaceEntity.getDescription());
    assertEquals(Space.HIDDEN, spaceEntity.getVisibility());
    assertEquals(Space.CLOSE, spaceEntity.getSubscription());
    EntityTag updatedETag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotSame(eTag, updatedETag);
    //root delete his space
    response = service("DELETE", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    space = spaceService.getSpaceById(spaceId);
    assertNull(space);
  }

  public void testGetUsersSpaceById() throws Exception {
    //root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    space.setMembers(new String[] {"root", "john"});
    space.setManagers(new String[] {"root"});
    space.setInvitedUsers(new String[] {"mary"});
    space.setPendingUsers(new String[] {"demo"});
    spaceService.updateSpace(space);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId() + "/users"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    response = service("GET", getURLResource("spaces/" + space.getId() + "/users?role=manager"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service("GET", getURLResource("spaces/" + space.getId() + "/users?role=invited"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    response = service("GET", getURLResource("spaces/" + space.getId() + "/users?role=pending"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testGetSpaceByIdWithDeletedDisableUsers() throws Exception {
    //root creates 1 spaces
    Space space = getSpaceInstance(255, "root");
    space.setMembers(new String[] {"root", "john", "mary", "demo"});
    space.setManagers(new String[] {"root", "john"});
    spaceService.updateSpace(space);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId() + "/users"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    List<DataEntity> dataEntities = (List<DataEntity>) collections.getEntities();
    assertEquals(4, dataEntities.size());
    // Make sure properties 'deleted' and 'enabled' are added to the dataEntity.
    DataEntity rootDataEntity = dataEntities.stream()
                                            .filter(data -> data.get(ProfileEntity.USERNAME).equals("root"))
                                            .findFirst()
                                            .orElse(null);
    assertNotNull(rootDataEntity);
    assertTrue(rootDataEntity.size() >= 12);
    assertEquals(true, rootDataEntity.containsKey("deleted"));
    assertEquals(true, rootDataEntity.containsKey("enabled"));
    assertEquals(rootIdentity.isDeleted(), rootDataEntity.get("deleted"));
    assertEquals(rootIdentity.isEnable(), rootDataEntity.get("enabled"));

    response = service("GET", getURLResource("spaces/" + space.getId() + "/users?role=manager"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    dataEntities = (List<DataEntity>) collections.getEntities();
    // Make sure properties 'deleted' and 'enabled' are added to the dataEntity.
    assertEquals(2, dataEntities.size());
    assertTrue(rootDataEntity.size() >= 12);
    assertEquals(true, rootDataEntity.containsKey("deleted"));
    assertEquals(true, rootDataEntity.containsKey("enabled"));
    assertEquals(johnIdentity.isDeleted(), rootDataEntity.get("deleted"));
    assertEquals(johnIdentity.isEnable(), rootDataEntity.get("enabled"));
  }

  public void testGetActivitiesSpaceById() throws Exception {
    //root creates 1 spaces and post 5 activities on it
    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    for (int i = 0; i < 5 ; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activity.setUserId(rootIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
    }

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId() + "/activities"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity activitiesCollections = (CollectionEntity) response.getEntity();
    assertEquals(6, activitiesCollections.getEntities().size());

    //root posts another activity
    String input = "{\"title\":title6}";
    response = getResponse("POST", getURLResource("spaces/" + space.getId() + "/activities"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    assertEquals(7, listAccess.getSize());
    ExoSocialActivity activity = listAccess.load(0, 10)[0];
    assertEquals("title6", activity.getTitle());
  }

  public void testGetSpaceActivityFileByFileId() throws Exception {
    // Given
    startSessionAs("root");
    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    try {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      Map<String, String> templateParams = new HashMap<>();
      activity.setType("DOC_ACTIVITY");
      activity.setTitle("Activity Title");
      activity.setBody("Activity Content");
      activity.setTemplateParams(templateParams);
      activityManager.saveActivityNoReturn(spaceIdentity, activity);

      // When
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      ExoSocialActivity createdActivity = activityManager.getActivity(activity.getId());

      // Then
      assertEquals(0 , activityManager.getActivityFilesIds(activity).size());
      ContainerResponse response = service("GET",
                                           getURLResource("spaces/" + createdActivity.getId() + "/files/1"),
                                           "",
                                           null,
                                           null);
      assertNotNull(response);
      assertEquals(404, response.getStatus());
    } finally {
      if (space != null) {
        spaceService.deleteSpace(space);
      }
    }
  }

  public void testSpacesApplications() throws Exception {
    startSessionAs("root");
    List<Application> spacesApplications = spaceService.getSpacesApplications();
    assertNotNull(spacesApplications);
    assertEquals(0, spacesApplications.size());

    String input = "{\"applicationName\":\"app\",\"contentId\":\"social/app\"}";
    //root try to update demo activity
    ContainerResponse response = getResponse("POST", getURLResource("spaces/applications"), input);
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    spacesApplications = spaceService.getSpacesApplications();
    assertNotNull(spacesApplications);
    assertEquals(1, spacesApplications.size());

    response = service("GET",
                       getURLResource("spaces/applications"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<Application> applications = (List<Application>) response.getEntity();
    assertNotNull(applications);
    assertEquals(1, applications.size());

    restartTransaction();

    response = service("DELETE",
                       getURLResource("spaces/applications/app"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(204, response.getStatus());
    spacesApplications = spaceService.getSpacesApplications();
    assertNotNull(spacesApplications);
    assertEquals(0, spacesApplications.size());
  }

  private Space getSpaceInstance(int number, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName("space" + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space = this.spaceService.createSpace(space, creator);
    return space;
  }

  private List<ExoSocialActivity> getCreatedSpaceActivities(Space space) {
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    return listAccess.loadAsList(0, 10);
  }
}
