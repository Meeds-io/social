package org.exoplatform.social.rest.impl.activity;

import java.lang.reflect.Field;
import java.util.*;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.OutputHeadersMap;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.*;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class ActivityRestResourcesTest extends AbstractResourceTest {

  private ActivityRestResourcesV1 activityRestResourcesV1;

  private IdentityStorage         identityStorage;

  private IdentityManager         identityManager;

  private ActivityManager         activityManager;

  private RelationshipManager     relationshipManager;

  private SpaceService            spaceService;

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private Identity                demoIdentity;

  private Identity                testSpaceIdentity;

    public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    activityRestResourcesV1 = new ActivityRestResourcesV1(activityManager, identityManager, spaceService, null);
    registry(activityRestResourcesV1);

    ExoContainerContext.setCurrentContainer(getContainer());
    restartTransaction();
    begin();
  }

  public void tearDown() throws Exception {
    end();
    super.tearDown();
    removeResource(activityRestResourcesV1.getClass());
  }

  public void testAddActivityByUser() throws Exception {
    startSessionAs("john");
    String input = "{\"title\":titleOfActivity,\"templateParams\":{\"param1\": value1,\"param2\":value2}}";
    ContainerResponse response = getResponse("POST", getURLResource("activities"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    DataEntity responseEntity = (DataEntity) response.getEntity();
    String title = (String) responseEntity.get("title");
    assertNotNull(title);
    assertEquals("titleOfActivity", title);
    DataEntity templateParams = (DataEntity) responseEntity.get("templateParams");
    assertNotNull(templateParams);
    assertEquals(2, templateParams.size());
  }

  public void testAddActivityByUserWhenPostOnUserStreamDisabled() throws Exception {
    Field field = ActivityManagerImpl.class.getDeclaredField("enableUserComposer");
    field.setAccessible(true);
    field.set(activityManager, false);
    try {
      startSessionAs("john");
      String input = "{\"title\":titleOfActivity,\"templateParams\":{\"param1\": value1,\"param2\":value2}}";
      ContainerResponse response = getResponse("POST", getURLResource("activities"), input);
      assertNotNull(response);
      assertEquals(401, response.getStatus());
    } finally {
      field.set(activityManager, true);
    }
  }

  public void testGetActivitiesOfUser() throws Exception {
    startSessionAs("root");
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    restartTransaction();

    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
    maryActivity.setTitle("mary activity");
    activityManager.saveActivityNoReturn(maryIdentity, maryActivity);

    restartTransaction();

    ContainerResponse response = service("GET", getURLResource("activities?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    // must return one activity of root and one of demo
    assertEquals(2, collections.getEntities().size());
    ActivityEntity activityEntity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    assertEquals("demo activity", activityEntity.getTitle());
    activityEntity = getBaseEntity(collections.getEntities().get(1), ActivityEntity.class);
    assertEquals("root activity", activityEntity.getTitle());
    Boolean canPost = (boolean) collections.get("canPost");
    assertNotNull(canPost);
    assertTrue(canPost);

    Field field = ActivityManagerImpl.class.getDeclaredField("enableUserComposer");
    field.setAccessible(true);
    field.set(activityManager, false);
    try {
      response = service("GET", getURLResource("activities?limit=5&offset=0"), "", null, null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      collections = (CollectionEntity) response.getEntity();
      canPost = (boolean) collections.get("canPost");
      assertNotNull(canPost);
      assertFalse(canPost);
    } finally {
      field.set(activityManager, true);
    }

    activityManager.deleteActivity(maryActivity);
    activityManager.deleteActivity(demoActivity);
    activityManager.deleteActivity(rootActivity);
  }

  public void testGetActivitiesByStreamType() throws Exception {
    startSessionAs("mary");

    // Get my posted activities
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("mary activity");
    activityManager.saveActivityNoReturn(maryIdentity, activity);

    restartTransaction();

    ContainerResponse response = service("GET",
                                         getURLResource("activities?streamType=USER_STREAM&limit=5&offset=0"),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    ActivityEntity activityEntity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    assertEquals("mary activity", activityEntity.getTitle());

    // Get manage spaces activities
    Space space = getSpaceInstance(1, "mary");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());

    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setTitle("mary activity1");
    activityManager.saveActivityNoReturn(spaceIdentity, activity1);

    restartTransaction();

    response = service("GET", getURLResource("activities?streamType=MANAGE_SPACES_STREAM&limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
    activityEntity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    assertEquals("mary activity1", activityEntity.getTitle());

    // Get favorite space activities
    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Favorite spaceFavorite = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                          space.getId(),
                                          null,
                                          Long.parseLong(maryIdentity.getId()));
    favoriteService.createFavorite(spaceFavorite);

    restartTransaction();

    response = service("GET", getURLResource("activities?streamType=FAVORITE_SPACES_STREAM&limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    // Get favorite activities
    Favorite favoriteActivity = new Favorite(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                             activity.getId(),
                                             null,
                                             Long.parseLong(maryIdentity.getId()));
    Favorite favoriteActivity1 = new Favorite(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                              activity1.getId(),
                                              null,
                                              Long.parseLong(maryIdentity.getId()));

    favoriteActivity1.getObject().setSpaceId(Long.parseLong(space.getId()));
    favoriteService.createFavorite(favoriteActivity);
    favoriteService.createFavorite(favoriteActivity1);

    response = service("GET", getURLResource("activities?streamType=USER_FAVORITE_STREAM&limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    // Get any space activity
    ExoSocialActivityImpl activity3 = new ExoSocialActivityImpl();
    activity3.setTitle("activity 3");
    activity3.setUserId(maryIdentity.getId());

    ExoSocialActivityImpl activity4 = new ExoSocialActivityImpl();
    activity4.setTitle("activity 4");
    activity4.setUserId(maryIdentity.getId());

    activityManager.saveActivityNoReturn(spaceIdentity, activity3);
    activityManager.saveActivityNoReturn(spaceIdentity, activity4);

    response = service("GET",
                       getURLResource("activities?streamType=ALL_STREAM&spaceId=" + space.getId() + "&limit=5&offset=0"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());

    // Get space favorite activities
    response =
             service("GET",
                     getURLResource("activities?streamType=USER_FAVORITE_STREAM&spaceId=" + space.getId() + "&limit=5&offset=0"),
                     "",
                     null,
                     null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // Get space my posted activities
    response = service("GET",
                       getURLResource("activities?streamType=USER_STREAM&spaceId=" + space.getId() + "&limit=5&offset=0"),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    // without filter
    response = service("GET", getURLResource("activities?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());
  }

  public void testGetActivity() throws Exception {
    startSessionAs("root");

    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    rootActivity.setPosterId(rootIdentity.getId());
    rootActivity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, rootActivity);

    restartTransaction();

    rootActivity = activityManager.getActivity(rootActivity.getId());

    ContainerResponse response = service("GET", getURLResource("activities/"+ rootActivity.getId() +"?expand="), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Object responseEtag = response.getHttpHeaders().getFirst("etag");
    assertNotNull(responseEtag);

    DataEntity dataEntityActivity = (DataEntity) response.getEntity();
    assertNotNull(dataEntityActivity);
    assertEquals(rootActivity.getId(), dataEntityActivity.get("id"));
    assertEquals(rootActivity.getTitle(), dataEntityActivity.get("title"));

    Map<String, List<String>> headers = new HashMap<>();
    headers.put("if-none-match", Collections.singletonList(responseEtag.toString()));
    response = service("GET", getURLResource("activities/"+ rootActivity.getId() +"?expand="), "", headers, null, new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    // Changing poster identity should return HTTP 200
    rootIdentity.getProfile().setProperty("test", "test");
    identityManager.updateProfile(rootIdentity.getProfile());

    response = service("GET", getURLResource("activities/"+ rootActivity.getId() +"?expand="), "", headers, null, new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    responseEtag = response.getHttpHeaders().getFirst("etag");
    assertNotNull(responseEtag);

    headers.put("if-none-match", Collections.singletonList(responseEtag.toString()));
    response = service("GET", getURLResource("activities/"+ rootActivity.getId() +"?expand="), "", headers, null, new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    // Changing stream owner identity should return HTTP 200
    spaceService.renameSpace(space, "New Display Name");

    response = service("GET", getURLResource("activities/"+ rootActivity.getId() +"?expand="), "", headers, null, new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    activityManager.deleteActivity(rootActivity);
  }

  public void testGetCachedActivity() throws Exception {
    startSessionAs("root");

    Space space = getSpaceInstance("testGetCachedActivity", "root", "john");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    rootActivity.setPosterId(rootIdentity.getId());
    rootActivity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, rootActivity);

    restartTransaction();

    rootActivity = activityManager.getActivity(rootActivity.getId());

    ContainerResponse response =
                               service("GET", getURLResource("activities/" + rootActivity.getId() + "?expand="), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Object responseEtag = response.getHttpHeaders().getFirst("etag");
    assertNotNull(responseEtag);

    DataEntity dataEntityActivity = (DataEntity) response.getEntity();
    assertNotNull(dataEntityActivity);
    assertEquals(rootActivity.getId(), dataEntityActivity.get("id"));
    assertEquals(rootActivity.getTitle(), dataEntityActivity.get("title"));

    Map<String, List<String>> headers = new HashMap<>();
    headers.put("if-none-match", Collections.singletonList(responseEtag.toString()));
    response = service("GET",
                       getURLResource("activities/" + rootActivity.getId() + "?expand="),
                       "",
                       headers,
                       null,
                       new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    // Changing poster identity should return HTTP 200
    rootIdentity.getProfile().setProperty("test", "test");
    identityManager.updateProfile(rootIdentity.getProfile());

    response = service("GET",
                       getURLResource("activities/" + rootActivity.getId() + "?expand="),
                       "",
                       headers,
                       null,
                       new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    responseEtag = response.getHttpHeaders().getFirst("etag");
    assertNotNull(responseEtag);

    headers.put("if-none-match", Collections.singletonList(responseEtag.toString()));
    response = service("GET",
                       getURLResource("activities/" + rootActivity.getId() + "?expand="),
                       "",
                       headers,
                       null,
                       new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    startSessionAs("john");

    headers.put("if-none-match", Collections.singletonList(responseEtag.toString()));
    response = service("GET",
                       getURLResource("activities/" + rootActivity.getId() + "?expand="),
                       "",
                       headers,
                       null,
                       new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    startSessionAs("root");
    headers.put("if-none-match", Collections.singletonList(responseEtag.toString()));
    response = service("GET",
                       getURLResource("activities/" + rootActivity.getId() + "?expand="),
                       "",
                       headers,
                       null,
                       new DummyContainerResponseWriter());
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    activityManager.deleteActivity(rootActivity);
  }

  public void testGetActivitiesSpaceById() throws Exception {
    //root creates 1 spaces and post 5 activities on it
    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
    for (int i = 0; i < 5 ; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activity.setUserId(rootIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
    }

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("activities?spaceId=" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity activitiesCollections = (CollectionEntity) response.getEntity();
    assertEquals(6, activitiesCollections.getEntities().size());

    //root posts another activity
    String input = "{\"title\":title6}";
    response = getResponse("POST", getURLResource("activities?spaceId=" + space.getId()), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    assertEquals(7, listAccess.getSize());
    ExoSocialActivity activity = listAccess.load(0, 10)[0];
    assertEquals("title6", activity.getTitle());
  }

  public void testGetSpaceActivity() throws Exception {
    startSessionAs("root");

    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    try {
      ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
      testSpaceActivity.setTitle("Test space activity");
      activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);
      restartTransaction();

      assertNotNull(testSpaceIdentity.getId());
      // Test get an activity(which is not a comment)
      ContainerResponse response = service("GET",
                                           "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                                           "",
                                           null,
                                           null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      ActivityEntity activityEntity = getBaseEntity((DataEntity) response.getEntity(), ActivityEntity.class);
      assertNotNull(activityEntity);
      assertNotNull(activityEntity.getOwner());
      assertTrue(activityEntity.getOwner().contains("/social/spaces/" + space.getId()));

      // Test get a comment
      restartTransaction();
      ExoSocialActivity testComment = new ExoSocialActivityImpl();
      testComment.setTitle("Test Comment");
      testComment.setUserId(rootIdentity.getId());
      testComment.setPosterId(rootIdentity.getId());
      activityManager.saveComment(testSpaceActivity, testComment);
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + testComment.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      CommentEntity commentEntity = getBaseEntity((DataEntity) response.getEntity(), CommentEntity.class);
      assertNotNull(commentEntity);
      assertNotNull(commentEntity.getTitle());
      assertEquals(commentEntity.getTitle(), "Test Comment");
      // Test get an activity which is not a comment
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());

      startSessionAs("John");
      // Test get a comment when logged user is not a member of space in which
      // the comment is posted
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + testComment.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(404, response.getStatus());

      // Test get an activity when logged user is not a member of space in which
      // the activity is posted
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(404, response.getStatus());

      startSessionAs("root");
      // Test get an activity which does not exist
      activityManager.deleteActivity(testSpaceActivity);
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
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

  public void testGetSpaceActivityWithBody() throws Exception {
    startSessionAs("root");

    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    try {
      ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
      testSpaceActivity.setTitle("Test space activity title");
      testSpaceActivity.setBody("test space activity body");
      activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);

      assertNotNull(testSpaceIdentity.getId());
      // Test get an activity(which is not a comment)
      ContainerResponse response = service("GET",
                                           "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                                           "",
                                           null,
                                           null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      ActivityEntity activityEntity = getBaseEntity((DataEntity) response.getEntity(), ActivityEntity.class);
      assertNotNull(activityEntity);
      assertNotNull(activityEntity.getBody());
      assertEquals("Test space activity title", activityEntity.getTitle());
      assertEquals("test space activity body", activityEntity.getBody());

      assertNotNull(activityEntity.getOwner());
      assertTrue(activityEntity.getOwner().contains("/social/spaces/" + space.getId()));
    } finally {
      if (space != null) {
        spaceService.deleteSpace(space);
      }
    }
  }

  public void testGetActivitiesOfCurrentUser() throws Exception {
    startSessionAs("root");

    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    //
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    //
    ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
    maryActivity.setTitle("mary activity");
    activityManager.saveActivityNoReturn(maryIdentity, maryActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities?limit=5&offset=0",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    // must return one activity of root and one of demo
    assertEquals(2, collections.getEntities().size());
    List<String> activitiesTitle = new ArrayList<>(2);
    ActivityEntity entity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    activitiesTitle.add(entity.getTitle());
    entity = getBaseEntity(collections.getEntities().get(1), ActivityEntity.class);
    activitiesTitle.add(entity.getTitle());
    assertTrue(activitiesTitle.contains("root activity"));
    assertTrue(activitiesTitle.contains("demo activity"));
  }

  public void testUpdateActivityById() throws Exception {
    startSessionAs("root");

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("test activity");
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("WORKSPACE", "collaboration");
    templateParams.put("MESSAGE", "old message");
    templateParams.put("description", "description of the activity");
    templateParams.put("FAKE_PARAM", "fake param");
    rootActivity.setTemplateParams(templateParams);
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    ContainerResponse response = service("GET",
            "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ActivityEntity result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals("test activity", result.getTitle());

    String input = "{\"title\":\"updated title\",\"templateParams\":{\"MESSAGE\":\"updated message\",\"NOT_EXIST_KEY\":\"any value\",\"FAKE_PARAM\":\"-\"}}";

    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId(), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals( "updated title", result.getTitle());
    assertEquals( "updated message", result.getTemplateParams().get("MESSAGE"));
    assertEquals( "collaboration", result.getTemplateParams().get("WORKSPACE"));
    assertTrue(result.getTemplateParams().containsKey("NOT_EXIST_KEY"));
    assertFalse(result.getTemplateParams().containsKey("FAKE_PARAM"));
    assertEquals(4, result.getTemplateParams().size());
  }

  public void testUpdateLinkActivityById() throws Exception {
    startSessionAs("root");

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("test activity");
    rootActivity.setType("LINK_ACTIVITY");
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "https://www.linkedin.com/");
    rootActivity.setTemplateParams(templateParams);
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    ContainerResponse response = service("GET",
            "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ActivityEntity result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals("test activity", result.getTitle());
    assertEquals("https://www.linkedin.com/", result.getTemplateParams().get("link"));

    String input = "{\"title\":\"updated title\",\"templateParams\":{\"link\":\"-\"}}";

    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId(), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals("updated title", result.getTitle());
    rootActivity = activityManager.getActivity(rootActivity.getId());
    assertEquals("", rootActivity.getTemplateParams().get("link"));
  }

  public void testHideActivityById() throws Exception {
    startSessionAs("root");

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("test activity");
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    RealtimeListAccess<ExoSocialActivity> activities = activityManager.getActivityFeedWithListAccess(rootIdentity);
    assertEquals(1, activities.getSize());

    ContainerResponse response = service("DELETE",
            "/" + VersionResources.VERSION_ONE + "/social/activities/" + activity.getId() + "?hide=true", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ActivityEntity result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals("test activity", result.getTitle());

    activities = activityManager.getActivityFeedWithListAccess(rootIdentity);
    assertEquals(0, activities.getSize());

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("Should be able to access activity even when hidden", activity);
  }

  public void testUnhideActivityById() throws Exception {
    startSessionAs("root");

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("test activity");
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    
    RealtimeListAccess<ExoSocialActivity> activities = activityManager.getActivityFeedWithListAccess(rootIdentity);
    assertEquals(1, activities.getSize());
    
    ContainerResponse response = service("DELETE",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + activity.getId() + "?hide=true", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ActivityEntity result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals("test activity", result.getTitle());

    activities = activityManager.getActivityFeedWithListAccess(rootIdentity);
    assertEquals(0, activities.getSize());

    response = service("PUT",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + activity.getId() + "/unhide",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    activities = activityManager.getActivityFeedWithListAccess(rootIdentity);
    assertEquals(1, activities.getSize());

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("Should be able to access activity even when hidden", activity);
  }

  public void testPinActivityById() throws Exception {
    startSessionAs("mary");

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("test activity");

    Space space = getSpaceInstance("spaceTestPin", "john");
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);
    // when
    ContainerResponse response = service("POST",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + activity.getId() + "/pins",
                                         "",
                                         null,
                                         null);
    // then
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    // when
    response = service("DELETE",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + activity.getId() + "/pins",
                       "",
                       null,
                       null);
    // then
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    // when
    response = service("DELETE", "/" + VersionResources.VERSION_ONE + "/social/activities/20000/pins", "", null, null);
    // then
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    // when
    ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
    maryActivity.setTitle("root activity");
    maryActivity.setPosterId(maryIdentity.getId());
    maryActivity.setUserId(maryIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, maryActivity);
    restartTransaction();

    response = service("POST",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + maryActivity.getId() + "/pins",
                       "",
                       null,
                       null);
    // then
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    // when
    response = service("DELETE",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + maryActivity.getId() + "/pins",
                       "",
                       null,
                       null);
    // then
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    // when
    String[] redactors = new String[] { "mary" };
    space.setRedactors(redactors);
    spaceService.updateSpace(space);

    response = service("POST",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + maryActivity.getId() + "/pins",
                       "",
                       null,
                       null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ActivityEntity activityEntity = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertTrue(activityEntity.isPinned());

    // when
    response = service("DELETE",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + maryActivity.getId() + "/pins",
                       "",
                       null,
                       null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    activityEntity = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertFalse(activityEntity.isPinned());
  }

  public void testGetUpdatedDeletedActivityById() throws Exception {
    startSessionAs("root");

    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    //
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    ActivityEntity result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals(result.getTitle(), "demo activity");

    String input = "{\"title\":updated}";
    // root try to update demo activity
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), input);
    assertNotNull(response);
    // root is not the poster of activity then he can't modify it
    assertEquals(401, response.getStatus());

    // demo try to update demo activity
    startSessionAs("demo");
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals(result.getTitle(), "updated");

    // demo delete his activity
    response =
             service("DELETE", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    assertNull(activityManager.getActivity(demoActivity.getId()));
  }

  public void testGetComments() throws Exception {
    startSessionAs("root");
    int nbComments = 5;
    // root posts one activity and some comments
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    //
    for (int i = 0; i < nbComments; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(rootActivity, comment);
      Thread.sleep(10); // NOSONAR Make sure that comment update time is different
    }

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/comments?sortDescending=false",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    startSessionAs("demo");
    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    // demo has no permission to view activity
    assertEquals(401, response.getStatus());

    // demo connects with root
    relationshipManager.inviteToConnect(demoIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments?sortDescending=true",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    for (int i = 0; i < nbComments; i++) {
      assertEquals("comment " + (4 - i), ((DataEntity) collections.getEntities().get(i)).get("title"));
    }

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testGetCommentsWithReplies() throws Exception {
    startSessionAs("root");
    int nbComments = 5;
    int nbReplies = 5;
    // root posts one activity and some comments
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    //
    for (int i = 0; i < nbComments; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(rootActivity, comment);
      for (int j = 0; j < nbReplies; j++) {
        ExoSocialActivity commentReply = new ExoSocialActivityImpl();
        commentReply.setTitle("comment reply " + i + " - " + j);
        commentReply.setUserId(maryIdentity.getId());
        commentReply.setParentCommentId(comment.getId());
        activityManager.saveComment(rootActivity, commentReply);
      }
    }

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/comments",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                           + "/comments?expand=subComments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(nbComments + nbComments * nbReplies, collections.getEntities().size());

    startSessionAs("demo");
    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    // demo has no permission to view activity
    assertEquals(401, response.getStatus());

    // demo connects with root
    relationshipManager.inviteToConnect(demoIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                           + "/comments?expand=subComments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(nbComments + nbComments * nbReplies, collections.getEntities().size());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testPostComment() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    // post a comment by root on the prevous activity
    String input = "{\"body\":comment1, \"title\":comment1}";
    ContainerResponse response = getResponse("POST",
                                             "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                                 + "/comments",
                                             input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity result = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment1", result.getTitle());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testUpdateComment() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    // post a comment by root on the prevous activity
    String input = "{\"body\":comment1, \"title\":comment1}";
    ContainerResponse response = getResponse("POST",
                                             "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                                 + "/comments",
                                             input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity result = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment1", result.getTitle());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    input = "{\"body\":\"comment2\", \"title\":\"comment2\", \"id\":\"" + result.getId() + "\"}";
    response = getResponse("PUT",
                           "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                               + "/comments",
                           input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    result = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment2", result.getTitle());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testShareActivityOnSpaces() throws Exception {
    startSessionAs("root");

    getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
    testSpaceActivity.setTitle("Test space activity");
    activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);
      
    getSpaceInstance("share", "root");
    Identity shareTargetSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "share");
    identityStorage.saveIdentity(shareTargetSpaceIdentity);

    //root shares another activity
    String input = "{\"title\":\"shared default activity\",\"type\":SHARED_DEFAULT_ACTIVITY,\"targetSpaces\":[\"share\"]}";
    ContainerResponse response = getResponse("POST", getURLResource("activities/" + testSpaceActivity.getId() + "/share"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(shareTargetSpaceIdentity);
    assertEquals(2, listAccess.getSize());
    ExoSocialActivity activity = listAccess.load(0, 10)[0];
    assertEquals("shared default activity", activity.getTitle());
  }

  public void testGetSharedActivityOnSpaces() throws Exception {
    startSessionAs("root");
    Space originalSpace = getSpaceInstance("originalSpace", "root", "john");

    startSessionAs("john");
    
    String param1 = "param1";
    String param2 = "param2";
    String value1 = "value1";
    String value2 = "value2";

    String input = "{\"title\":titleOfActivity,\"templateParams\":{\"" + param1 + "\": \"" + value1 + "\",\"" + param2 + "\":\""
        + value2 + "\"}}";
    ContainerResponse response = getResponse("POST", getURLResource("activities?spaceId=" + originalSpace.getId()), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ActivityEntity originalActivity = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertNotNull(originalActivity);
    assertNotNull(originalActivity.getActivityStream());
    assertNotNull(originalActivity.getActivityStream().get("space"));

    Space targetSpace = getSpaceInstance("targetSpace", "mary", "james", "demo");

    String message = "Share activity Message";
    input = "{\"title\":\"" + message + "\",\"type\":SHARED_DEFAULT_ACTIVITY,\"targetSpaces\":[\"" + targetSpace.getPrettyName()
        + "\"]}";
    response = getResponse("POST", getURLResource("activities/" + originalActivity.getId() + "/share"), input);
    assertNotNull(response);
    assertEquals("User john is not member of target space", 401, response.getStatus());

    spaceService.addMember(targetSpace, "john");
    spaceService.addRedactor(targetSpace, "demo");

    response = getResponse("POST", getURLResource("activities/" + originalActivity.getId() + "/share"), input);
    assertNotNull(response);
    assertEquals("User john is not redactor of target space", 401, response.getStatus());

    spaceService.addRedactor(targetSpace, "john");

    response = getResponse("POST", getURLResource("activities/" + originalActivity.getId() + "/share"), input);
    assertNotNull(response);
    assertEquals("User john is redactor of target space and member on original space", 200, response.getStatus());

    CollectionEntity sharedActivities = (CollectionEntity) response.getEntity();
    assertNotNull(sharedActivities);
    assertEquals(1, sharedActivities.getEntities().size());
    ActivityEntity sharedActivity = getBaseEntity(sharedActivities.getEntities().get(0), ActivityEntity.class);
    assertNotNull(sharedActivity);
    assertNotNull(sharedActivity.getId());
    assertEquals(originalActivity.getId(), sharedActivity.getTemplateParams().get(ActivityManager.SHARED_ACTIVITY_ID_PARAM));

    restartTransaction();
    startSessionAs("demo");
    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + sharedActivity.getId() + "?expand=" + RestProperties.SHARED,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    sharedActivity = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertNotNull(sharedActivity);
    assertNotNull(sharedActivity.getOriginalActivity());
    assertTrue(!sharedActivity.getTemplateParams().containsKey(param1));
    assertTrue(!sharedActivity.getTemplateParams().containsKey(param2));

    restartTransaction();
    startSessionAs("john");
    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + originalActivity.getId() + "?expand=" + RestProperties.SHARED,
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    originalActivity = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertNotNull(originalActivity);
    assertNull(originalActivity.getOriginalActivity());
    assertNotNull(originalActivity.getShareActions());
    assertEquals(1, originalActivity.getShareActions().size());
  }

  public void testPostCommentReply() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    // post a comment by root on the previous activity
    String input = "{\"body\":\"comment1 body\", \"title\":comment1}";
    ContainerResponse response = getResponse("POST",
                                             "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                                 + "/comments",
                                             input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity comment = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment1", comment.getTitle());
    assertEquals("comment1 body", comment.getBody());
    assertNotNull(comment.getId());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    String commentReplyInput = "{\"body\":\"comment reply 1 body\", \"title\":\"comment reply 1\", \"parentCommentId\": "
        + comment.getId() + "}";
    response = getResponse("POST",
                           "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                           commentReplyInput);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity commentReply = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment reply 1", commentReply.getTitle());
    assertEquals("comment reply 1 body", commentReply.getBody());
    assertEquals(comment.getId(), commentReply.getParentCommentId());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity, true).getSize());

    assertEquals(2, activityManager.getCommentsWithListAccess(rootActivity, true).load(0, -1).length);

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testGetLikes() throws Exception {
    startSessionAs("root", true);
    // root posts one activity and some comments
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    rootActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(rootActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/likes",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testPostLike() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    rootActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(rootActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/likes",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // post a like by root on the activity
    List<String> updatedLikes = new ArrayList<String>();
    updatedLikes.add(activityManager.getActivity(rootActivity.getId()).getLikeIdentityIds()[0]);
    updatedLikes.add(maryIdentity.getId());
    rootActivity.setLikeIdentityIds(updatedLikes.toArray(new String[updatedLikes.size()]));
    activityManager.updateActivity(rootActivity);

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/likes",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testDeleteLike() throws Exception {
    startSessionAs("demo");

    // root posts one activity
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    demoActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(demoActivity);

    ContainerResponse response = service("DELETE",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId()
                                             + "/likes",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());
    assertEquals(0, collections.getSize());

    // clean data
    activityManager.deleteActivity(demoActivity);
  }

  public void testAddLike() throws Exception {
    startSessionAs("demo");

    // root posts one activity
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    ContainerResponse response = service("POST",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId()
                                         + "/likes",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    assertEquals(1, collections.getSize());

    // clean data
    activityManager.deleteActivity(demoActivity);
  }

  public void testDeleteLikeWhenNoPermissionOnActivity() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    startSessionAs("demo");

    ContainerResponse response = service("DELETE",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/likes",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(401, response.getStatus());
    DataEntity activityEntity = (DataEntity) response.getEntity();
    // the activity data must not be returned since the user has not the
    // permissions to view it
    assertNull(activityEntity);

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  private Space getSpaceInstance(String prettyName, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName(prettyName);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + prettyName);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    this.spaceService.createSpace(space, creator);
    return space;
  }

  private Space getSpaceInstance(String prettyName, String creator, String ...members) throws Exception {
    Space space = new Space();
    space.setDisplayName(prettyName);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + prettyName);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setMembers(members);
    this.spaceService.createSpace(space, creator);
    return space;
  }

  private Space getSpaceInstance(int number, String creator) throws Exception {
    restartTransaction();
    Space space = new Space();
    space.setDisplayName("space" + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space = this.spaceService.createSpace(space, creator);
    restartTransaction();
    return space;
  }

  public void testPreloadActivitiesId() throws Exception {
    startSessionAs("john");
    Space space = getSpaceInstance(1, "john");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
    for (int i = 0; i < 4; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity"+i);
      activity.setUserId(johnIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
    }
    restartTransaction();
    ContainerResponse response = service("GET",
            getURLResource("activities?streamType=ALL_STREAM&spaceId=" + space.getId() + "&limit=20&offset=0&expand=ids"),
            "",
            null,
            null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());
    OutputHeadersMap outputHeadersMap = (OutputHeadersMap) response.getHttpHeaders();
    assertEquals(5, outputHeadersMap.get("Link").size());
    //
    for (int i = 5; i < 52; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity"+i);
      activity.setUserId(johnIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
    }
    restartTransaction();
    response = service("GET",
            getURLResource("activities?streamType=ALL_STREAM&spaceId=" + space.getId() + "&limit=40&offset=0&expand=ids"),
            "",
            null,
            null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(40, collections.getEntities().size());
    outputHeadersMap = (OutputHeadersMap) response.getHttpHeaders();
     //Max to preload is 10
    assertEquals(10, outputHeadersMap.get("Link").size());
    //
    restartTransaction();
    response = service("GET",
            getURLResource("activities?streamType=ALL_STREAM&spaceId=" + space.getId() + "&limit=120&offset=0&expand=ids"),
            "",
            null,
            null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(52, collections.getEntities().size());
    outputHeadersMap = (OutputHeadersMap) response.getHttpHeaders();
    assertEquals(4, outputHeadersMap.size());
    /* Activity ids list length is 52
     Max to preload is 10
     Preload limit is limit / 2 = 60
     Offset is Preload limit - max to preload = 50
     Expected: 2 links  */
    assertEquals(2, outputHeadersMap.get("Link").size());
  }

}
