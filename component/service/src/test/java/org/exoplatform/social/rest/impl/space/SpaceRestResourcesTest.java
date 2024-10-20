package org.exoplatform.social.rest.impl.space;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.thumbnail.ImageThumbnailService;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.ProfileEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.rest.impl.activity.ActivityRest;
import org.exoplatform.social.service.test.AbstractResourceTest;
import org.exoplatform.upload.UploadService;

import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.social.space.service.SpaceLayoutService;

public class SpaceRestResourcesTest extends AbstractResourceTest {
  private IdentityManager        identityManager;

  private OrganizationService    organizationService;

  private UserACL                userACL;

  private ActivityManager        activityManager;

  private SpaceService           spaceService;

  private SpaceRest              spaceRestResources;

  private Identity               rootIdentity;

  private Identity               johnIdentity;

  private Identity               maryIdentity;

  private Identity               demoIdentity;

  private Identity               externalUserIdentity;

  private ImageThumbnailService  imageThumbnailService;

  private SecuritySettingService securitySettingService;

  private MockUploadService      uploadService;

  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);
    imageThumbnailService = getContainer().getComponentInstanceOfType(ImageThumbnailService.class);
    securitySettingService = getContainer().getComponentInstanceOfType(SecuritySettingService.class);
    SpaceLayoutService spaceLayoutService = getContainer().getComponentInstanceOfType(SpaceLayoutService.class);

    rootIdentity = identityManager.getOrCreateIdentity("organization", "root");
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john");
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary");
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo");

    spaceRestResources =
                       new SpaceRest(new ActivityRest(activityManager, identityManager, spaceService, null),
                                     spaceService,
                                     spaceLayoutService,
                                     identityManager,
                                     uploadService,
                                     imageThumbnailService,
                                     securitySettingService);
    registry(spaceRestResources);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(spaceRestResources.getClass());
  }

  public void testSpaceDisplayNameUpdateWithDifferentCases() throws Exception {
    startSessionAs("root");
    Space space = getSpaceInstance(1, "root");
    ContainerResponse response = null;
    String inputWithCorrectName = "{\"displayName\":\"social\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    String inputWithSpecialCharacter = "{\"displayName\":\"~#Social~#\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    String inputWithAndCharacter = "{\"displayName\":\"social & social\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";

    response = getResponse("PUT", getURLResource("spaces/" + space.getId()), inputWithCorrectName);
    assertEquals(200, response.getStatus());
    response = getResponse("PUT", getURLResource("spaces/" + space.getId()), inputWithSpecialCharacter);
    assertEquals(200, response.getStatus());
    response = getResponse("PUT", getURLResource("spaces/" + space.getId()), inputWithAndCharacter);
    assertEquals(200, response.getStatus());
  }

  public void testSpaceVisibilityUpdateWithDifferentCases() throws Exception {
    startSessionAs("root");
    /*
     * Test of 'private' and 'hidden' fields with a mix of upper/lower cases
     */

    Space space = getSpaceInstance(1, "root");

    Map<String, String> listOfResponses = new HashMap<String, String>() {
      {
        put("{\"displayName\":\"social\",\"visibility\":PRIVATE}", Space.PRIVATE);
        put("{\"displayName\":\"social\",\"visibility\":private}", Space.PRIVATE);
        put("{\"displayName\":\"social\",\"visibility\":PriVatE}", Space.PRIVATE);
        put("{\"displayName\":\"social\",\"visibility\":HIDDEN}", Space.HIDDEN);
        put("{\"displayName\":\"social\",\"visibility\":hidden}", Space.HIDDEN);
        put("{\"displayName\":\"social\",\"visibility\":HiDdEn}", Space.HIDDEN);
      }
    };

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

    startSessionAs("john", true);
    response = service("GET", getURLResource("spaces?limit=5&offset=0&filterType=member"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // Only the super user can see all the spaces.
    startSessionAs("root", true);
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

    response = service("GET", getURLResource("spaces?limit=5&offset=0&filterType=lastVisited"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

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

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
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

  public void testShouldNotUseUseSameCacheWhenUserChange() throws Exception {
    getSpaceInstance(1, "root");
    getSpaceInstance(2, "john");
    getSpaceInstance(3, "demo");

    startSessionAs("root");

    ContainerResponse response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());
    EntityTag eTagRoot = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTagRoot);

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTagRoot.getValue() + "\"");
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    startSessionAs("john");

    headers.putSingle("If-None-Match", "\"" + eTagRoot.getValue() + "\"");
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", headers, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    EntityTag eTagJohn = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTagJohn);

    headers.putSingle("If-None-Match", "\"" + eTagJohn.getValue() + "\"");
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    startSessionAs("root");

    headers.putSingle("If-None-Match", "\"" + eTagRoot.getValue() + "\"");
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());

    headers.putSingle("If-None-Match", "\"" + eTagJohn.getValue() + "\"");
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", headers, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testCreateSpace() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"social\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    // root try to update demo activity
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
    // root try to update demo activity
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);
    assertEquals("Благодійність", space.getDisplayName());
  }

  public void testGetSpaceAvatarForAnonymous() throws Exception {
    String user = "john";

    Space space = getSpaceInstance(1, user);
    uploadSpaceAvatar(user, space.getId());

    space = spaceService.getSpaceById(space.getId());
    assertNotNull(space.getAvatarUrl());

    String avatarUrl = space.getAvatarUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("spaces/" + space.getPrettyName() + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    response = service("GET", getURLResource("spaces/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    space.setVisibility(Space.HIDDEN);
    space = spaceService.updateSpace(space);
    avatarUrl = space.getAvatarUrl().replace("/portal/rest", "");

    response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());
  }

  public void testGetSpaceAvatarForAuthentiticatedUser() throws Exception {
    String user = "john";

    Space space = getSpaceInstance(1, user);
    uploadSpaceAvatar(user, space.getId());

    space = spaceService.getSpaceById(space.getId());
    assertNotNull(space.getAvatarUrl());

    startSessionAs("mary");

    String avatarUrl = space.getAvatarUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("spaces/" + space.getPrettyName() + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("spaces/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    space.setVisibility(Space.HIDDEN);
    space = spaceService.updateSpace(space);
    avatarUrl = space.getAvatarUrl().replace("/portal/rest", "");

    response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    startSessionAs(user);

    response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGetSpaceAvatarWithDefaultSize() throws Exception {
    String user = "john";

    Space space = getSpaceInstance(1, user);
    uploadSpaceAvatar(user, space.getId(), "hr-avatar.png");

    space = spaceService.getSpaceById(space.getId());
    assertNotNull(space.getAvatarUrl());

    startSessionAs("mary");

    String avatarUrl = space.getAvatarUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream((byte[]) response.getEntity()));
    assertEquals(100, receivedImage.getWidth());
    assertEquals(100, receivedImage.getHeight());
  }

  public void testGetSpaceAvatarWithOriginalSize() throws Exception {
    String user = "john";

    Space space = getSpaceInstance(1, user);
    uploadSpaceAvatar(user, space.getId(), "hr-avatar.png");

    space = spaceService.getSpaceById(space.getId());
    assertNotNull(space.getAvatarUrl());

    startSessionAs("mary");

    String avatarUrl = space.getAvatarUrl().replace("/portal/rest", "");
    avatarUrl += "&size=0x0";

    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream((byte[]) response.getEntity()));
    assertEquals(595, receivedImage.getWidth());
    assertEquals(595, receivedImage.getHeight());
  }

  public void testGetSpaceBannerForAnonymous() throws Exception {
    String user = "john";

    Space space = getSpaceInstance(1, user);

    String bannerUrl = space.getBannerUrl().replace("/portal/rest", "");
    assertThat(bannerUrl, containsString("/social/images/defaultSpaceBanner.webp"));

    startSessionAs(user);

    uploadSpaceBanner(user, space.getId());

    space = spaceService.getSpaceById(space.getId());
    assertNotNull(space.getBannerUrl());

    bannerUrl = space.getBannerUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", bannerUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("spaces/" + space.getPrettyName() + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    response = service("GET", getURLResource("spaces/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    space.setVisibility(Space.HIDDEN);
    space = spaceService.updateSpace(space);
    restartTransaction();
    space = spaceService.getSpaceById(space.getId());
    bannerUrl = space.getBannerUrl().replace("/portal/rest", "");

    response = service("GET", bannerUrl, "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());
  }

  public void testGetSpaceBannerForAuthentiticatedUser() throws Exception {
    Space space = getSpaceInstance(1, "john");
    uploadSpaceBanner("john", space.getId());

    space = spaceService.getSpaceById(space.getId());
    assertNotNull(space.getBannerUrl());

    startSessionAs("mary");

    String bannerUrl = space.getBannerUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", bannerUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("spaces/" + space.getPrettyName() + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("spaces/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    space.setVisibility(Space.HIDDEN);
    space = spaceService.updateSpace(space);
    restartTransaction();
    space = spaceService.getSpaceById(space.getId());
    bannerUrl = space.getBannerUrl().replace("/portal/rest", "");

    response = service("GET", bannerUrl, "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    startSessionAs("john");

    response = service("GET", bannerUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    removeSpaceBanner(space.getId());

    response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity);
    assertThat(spaceEntity.getBannerUrl(), containsString("/social/images/defaultSpaceBanner.webp"));
  }

  public void testGetSpaceById() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"test space\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    // root creates a space
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

  public void testGetSpaceByGroupSuffix() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"test space\",\"visibility\":\"hidden\",\"subscription\":\"open\"}";
    // root creates a space
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);

    // Get space by its groupId
    response = service("GET",
                       getURLResource("spaces/byGroupSuffix/" + space.getGroupId().replace(SpaceUtils.SPACE_GROUP + "/", "")),
                       "",
                       null,
                       null);
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
    response = service("GET",
                       getURLResource("spaces/byDisplayName/" + space.getDisplayName().replaceAll(" ", "%20")),
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity);
    assertEquals("test space", spaceEntity.getDisplayName());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);
  }

  public void testGetUpdateDeleteSpaceById() throws Exception {
    // root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    space.setVisibility(Space.HIDDEN);
    space.setRegistration(Space.CLOSED);
    space = spaceService.updateSpace(space);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertEquals("space1", spaceEntity.getDisplayName());
    assertEquals(Space.HIDDEN, spaceEntity.getVisibility());
    assertEquals(Space.CLOSED, spaceEntity.getSubscription());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);

    // root update space's description and name
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
    assertEquals(Space.CLOSED, spaceEntity.getSubscription());
    EntityTag updatedETag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotSame(eTag, updatedETag);
    // root delete his space
    response = service("DELETE", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    space = spaceService.getSpaceById(spaceId);
    assertNull(space);
  }

  public void testSaveSpacePublicSite() throws Exception {
    // root creates 1 spaces
    Space space = getSpaceInstance(1, "root");

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity.getPublicSiteId());
    assertEquals(0l, spaceEntity.getPublicSiteId().longValue());

    String spaceId = spaceEntity.getId();

    String input = "{\"publicSiteVisibility\":\"authenticated\",\"publicSiteName\":\"Test Public Site\"}";
    response = getResponse("PUT", getURLResource("spaces/" + spaceId), input);
    restartTransaction();

    assertNotNull(response);
    assertEquals(200, response.getStatus());
    spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity.getPublicSiteId());
    assertTrue(spaceEntity.getPublicSiteId().longValue() > 0);
    assertEquals("authenticated", spaceEntity.getPublicSiteVisibility());

    LayoutService layoutService = getContainer().getComponentInstanceOfType(LayoutService.class);
    PortalConfig portalConfig = layoutService.getPortalConfig(spaceEntity.getPublicSiteId());
    assertNotNull(portalConfig);
    assertEquals(space.getDisplayName(), portalConfig.getLabel());
    assertEquals(space.getPrettyName(), portalConfig.getName());
  }

  public void testGetUsersSpaceById() throws Exception {
    // root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    space.setMembers(new String[] { "root", "john" });
    space.setManagers(new String[] { "root" });
    space.setInvitedUsers(new String[] { "mary" });
    space.setPendingUsers(new String[] { "demo" });
    spaceService.updateSpace(space);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    DataEntity dataEntity = (DataEntity) response.getEntity();
    assertEquals(1, dataEntity.get("managersCount"));
    assertEquals(2, dataEntity.get("membersCount"));
    assertEquals(1, dataEntity.get("invitedUsersCount"));
    assertEquals(1, dataEntity.get("pendingUsersCount"));

    response = service("GET", getURLResource("spaces/" + space.getId() + "/users"), "", null, null);
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
    // root creates 1 spaces
    Space space = getSpaceInstance(255, "root");
    space.setMembers(new String[] { "root", "john", "mary", "demo" });
    space.setManagers(new String[] { "root", "john" });
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

  private Space getSpaceInstance(int number, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName("space" + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space = this.spaceService.createSpace(space, creator);
    return space;
  }

  private List<ExoSocialActivity> getCreatedSpaceActivities(Space space) {
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    return listAccess.loadAsList(0, 10);
  }

  private void uploadSpaceBanner(String user, String spaceId) throws Exception {
    startSessionAs(user);
    String uploadId = "testtest";
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    URL resource = getClass().getClassLoader().getResource("blank.gif");
    uploadService.createUploadResource(uploadId, resource.getFile(), "banner.png", "image/png");
    String input = "{\"id\":\"" + spaceId + "\",\"bannerId\":\"" + uploadId + "\"}";
    // root try to update demo activity
    ContainerResponse response = getResponse("PUT", getURLResource("spaces/" + spaceId), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    endSession();
  }

  private void removeSpaceBanner(String spaceId) throws Exception {
    String input = "{\"id\":\"" + spaceId + "\",\"bannerId\":\"DEFAULT_BANNER\"}";
    // root try to update demo activity
    ContainerResponse response = getResponse("PUT", getURLResource("spaces/" + spaceId), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  private void uploadSpaceAvatar(String user, String spaceId) throws Exception {
    uploadSpaceAvatar(user, spaceId, "blank.gif");
  }

  private void uploadSpaceAvatar(String user, String spaceId, String fileName) throws Exception {
    startSessionAs(user);
    String uploadId = fileName;
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    URL resource = getClass().getClassLoader().getResource(fileName);
    uploadService.createUploadResource(uploadId, resource.getFile(), fileName, "image/png");
    String input = "{\"id\":\"" + spaceId + "\",\"avatarId\":\"" + uploadId + "\"}";
    // root try to update demo activity
    ContainerResponse response = getResponse("PUT", getURLResource("spaces/" + spaceId), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    endSession();
  }

  public void testIsSpaceContainsExternals() throws Exception {
    startSessionAs("root");
    User external = organizationService.getUserHandler().createUserInstance("externalUser");
    organizationService.getUserHandler().createUser(external, false);
    externalUserIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "externalUser");
    externalUserIdentity.getProfile().setProperty("username", "externalUser");
    externalUserIdentity.getProfile().setProperty("external", "true");
    identityManager.updateProfile(externalUserIdentity.getProfile());
    Space space = getSpaceInstance(10, "root");
    ContainerResponse response;
    response = service("GET", getURLResource("spaces/" + space.getId() + "/checkExternals"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertEquals("false", response.getEntity());
    spaceService.addMember(space, "externalUser");
    response = service("GET", getURLResource("spaces/" + space.getId() + "/checkExternals"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertEquals("true", response.getEntity());
    endSession();
  }
}
