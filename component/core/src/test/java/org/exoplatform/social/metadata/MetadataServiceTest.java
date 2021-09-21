package org.exoplatform.social.metadata;

import java.util.*;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.model.*;

public class MetadataServiceTest extends AbstractCoreTest {

  private Identity                       rootIdentity;

  private Identity                       johnIdentity;

  private IdentityManager                identityManager;

  private SpaceService                   spaceService;

  private MetadataService                metadataService;

  private UserOnlyMetadataPlugin         userMetadataTypePlugin  = new UserOnlyMetadataPlugin();

  private SpaceManagerOnlyMetadataPlugin spaceMetadataTypePlugin = new SpaceManagerOnlyMetadataPlugin();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    try {
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
      metadataService.addMetadataTypePlugin(spaceMetadataTypePlugin);
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);

    super.tearDown();
  }

  public void testMetadataTypePlugin() {
    try {
      metadataService.addMetadataTypePlugin(new UserOnlyMetadataPlugin());
      fail("Should throw an exception when attempting to redefine an existing ");
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    List<MetadataType> metadataTypes = metadataService.getMetadataTypes();
    assertNotNull(metadataTypes);
    assertEquals(2, metadataTypes.size());

    assertNotNull(metadataService.getMetadataTypePluginByName("user"));
    assertTrue(metadataService.getMetadataTypePluginByName("user") instanceof UserOnlyMetadataPlugin);
    assertNotNull(metadataService.getMetadataTypePluginByName("space"));
    assertTrue(metadataService.getMetadataTypePluginByName("space") instanceof SpaceManagerOnlyMetadataPlugin);
  }

  public void testCreateMetadata() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String name = "testMetadata1";
    Map<String, String> properties = Collections.singletonMap("testMetadata1Key", "testMetadata1Value");

    try {
      metadataService.createMetadata(null, creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadata(newMetadataInstance(audienceId,
                                                         creatorId,
                                                         name,
                                                         null,
                                                         properties),
                                     creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadata(newMetadataInstance(audienceId,
                                                         creatorId,
                                                         name,
                                                         userMetadataTypePlugin.getMetadataType(),
                                                         properties),
                                     0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadata(newMetadataInstance(audienceId,
                                                         creatorId,
                                                         name,
                                                         new MetadataType(3, "test"),
                                                         properties),
                                     creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadata(newMetadataInstance(audienceId,
                                                         creatorId,
                                                         name,
                                                         userMetadataTypePlugin.getMetadataType(),
                                                         properties),
                                     Long.parseLong(rootIdentity.getId()));
      fail();
    } catch (IllegalAccessException e) {
      // Expected
    }

    Metadata storedMetadata = metadataService.createMetadata(newMetadataInstance(audienceId,
                                                                                 creatorId,
                                                                                 name,
                                                                                 userMetadataTypePlugin.getMetadataType(),
                                                                                 properties),
                                                             creatorId);
    assertNotNull(storedMetadata);
    assertTrue(storedMetadata.getId() > 0);
    assertTrue(storedMetadata.getCreatedDate() > 0);
    assertEquals(creatorId, storedMetadata.getCreatorId());
    assertEquals(audienceId, storedMetadata.getAudienceId());
    assertEquals(name, storedMetadata.getName());
    assertEquals(properties, storedMetadata.getProperties());
    assertEquals(userMetadataTypePlugin.getMetadataType(), storedMetadata.getType());
  }

  public void testCreateMetadataItem() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType";
    String type = userMetadataTypePlugin.getMetadataType().getName();
    String name = "testMetadata2";
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    MetadataItem metadataItem = newMetadataItemInstance(objectType, objectId, parentObjectId, properties);

    try {
      metadataService.createMetadataItem(null,
                                         type,
                                         name,
                                         audienceId,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(newMetadataItemInstance(null, objectId, parentObjectId, properties),
                                         type,
                                         name,
                                         audienceId,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(newMetadataItemInstance(objectType, null, parentObjectId, properties),
                                         type,
                                         name,
                                         audienceId,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(metadataItem,
                                         null,
                                         name,
                                         audienceId,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(metadataItem,
                                         type,
                                         name,
                                         audienceId,
                                         0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(metadataItem,
                                         "test",
                                         name,
                                         audienceId,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(metadataItem,
                                         type,
                                         name,
                                         audienceId,
                                         Long.parseLong(rootIdentity.getId()));
      fail();
    } catch (IllegalAccessException e) {
      // Expected
    }

    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItem,
                                                                         type,
                                                                         name,
                                                                         audienceId,
                                                                         creatorId);
    assertNotNull(storedMetadataItem);
    assertTrue(storedMetadataItem.getId() > 0);
    assertTrue(storedMetadataItem.getCreatedDate() > 0);
    assertEquals(creatorId, storedMetadataItem.getCreatorId());
    assertNotNull(storedMetadataItem.getMetadata());
    assertEquals(audienceId, storedMetadataItem.getMetadata().getAudienceId());
    assertEquals(name, storedMetadataItem.getMetadata().getName());
    assertEquals(userMetadataTypePlugin.getMetadataType(), storedMetadataItem.getMetadata().getType());
    assertEquals(properties, storedMetadataItem.getProperties());
    assertEquals(parentObjectId, storedMetadataItem.getParentObjectId());
    assertEquals(objectId, storedMetadataItem.getObjectId());
    assertEquals(objectType, storedMetadataItem.getObjectType());
  }

  public void testDeleteMetadataItem() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId10";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType11";
    String type = userMetadataTypePlugin.getMetadataType().getName();
    String name = "testMetadata8";
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    MetadataItem metadataItem = newMetadataItemInstance(objectType, objectId, parentObjectId, properties);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItem,
                                                                         type,
                                                                         name,
                                                                         audienceId,
                                                                         creatorId);

    try {
      metadataService.deleteMetadataItem(null,
                                         name,
                                         storedMetadataItem.getId(),
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem(type,
                                         null,
                                         storedMetadataItem.getId(),
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem(type,
                                         name,
                                         0,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem(type,
                                         name,
                                         storedMetadataItem.getId(),
                                         0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem("test",
                                         name,
                                         storedMetadataItem.getId(),
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem("space",
                                         name,
                                         storedMetadataItem.getId(),
                                         creatorId);
      fail();
    } catch (IllegalStateException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem(type,
                                         "name2",
                                         storedMetadataItem.getId(),
                                         creatorId);
      fail();
    } catch (IllegalStateException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem(type,
                                         name,
                                         storedMetadataItem.getId(),
                                         Long.parseLong(rootIdentity.getId()));
      fail();
    } catch (IllegalAccessException e) {
      // Expected
    }
    MetadataItem deletedMetadataItem = metadataService.deleteMetadataItem(type,
                                                                          name,
                                                                          storedMetadataItem.getId(),
                                                                          creatorId);

    assertNotNull(deletedMetadataItem);
    assertTrue(deletedMetadataItem.getId() > 0);
    assertTrue(deletedMetadataItem.getCreatedDate() > 0);
    assertEquals(creatorId, deletedMetadataItem.getCreatorId());
    assertNotNull(deletedMetadataItem.getMetadata());
    assertEquals(audienceId, deletedMetadataItem.getMetadata().getAudienceId());
    assertEquals(name, deletedMetadataItem.getMetadata().getName());
    assertEquals(userMetadataTypePlugin.getMetadataType(), deletedMetadataItem.getMetadata().getType());
    assertEquals(properties, deletedMetadataItem.getProperties());
    assertEquals(parentObjectId, deletedMetadataItem.getParentObjectId());
    assertEquals(objectId, deletedMetadataItem.getObjectId());
    assertEquals(objectType, deletedMetadataItem.getObjectType());

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(objectType, objectId);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());
  }

  public void testDeleteMetadata() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType";
    String type = userMetadataTypePlugin.getMetadataType().getName();
    String name = "testMetadata3";
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    createNewMetadataItem(type, name, objectType, objectId, parentObjectId, creatorId, audienceId, properties);

    try {
      metadataService.deleteMetadata(null, name, audienceId, creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadata(type, name, audienceId, 0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadata(type, "test", audienceId, creatorId);
      fail();
    } catch (ObjectNotFoundException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadata("test", name, audienceId, creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadata(type, name, audienceId, Long.parseLong(rootIdentity.getId()));
      fail();
    } catch (IllegalAccessException e) {
      // Expected
    }

    Metadata storedMetadata = metadataService.deleteMetadata(type, name, audienceId, creatorId);

    assertNotNull(storedMetadata);
    assertTrue(storedMetadata.getId() > 0);
    assertTrue(storedMetadata.getCreatedDate() > 0);
    assertEquals(creatorId, storedMetadata.getCreatorId());
    assertEquals(audienceId, storedMetadata.getAudienceId());
    assertEquals(name, storedMetadata.getName());
    assertNull(storedMetadata.getProperties());
    assertEquals(userMetadataTypePlugin.getMetadataType(), storedMetadata.getType());
  }

  public void testGetMetadataItemsByObject() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataTypePlugin.getMetadataType().getName();
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId2",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType2",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType3",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType4",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject("objectType1", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByObject("objectType2", "objectId2");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByObject("objectType2", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  public void testDeleteMetadataItemsByObject() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataTypePlugin.getMetadataType().getName();
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    createNewMetadataItem(type,
                          "testMetadata15",
                          "objectType11",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata15",
                          "objectType11",
                          "objectId2",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata16",
                          "objectType11",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata16",
                          "objectType12",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata16",
                          "objectType13",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    createNewMetadataItem(type,
                          "testMetadata15",
                          "objectType14",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject("objectType11", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByObject("objectType12", "objectId2");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByObject("objectType12", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());

    metadataService.deleteMetadataItemsByObject("objectType11", "objectId1");

    metadataItems = metadataService.getMetadataItemsByObject("objectType11", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByObject("objectType12", "objectId2");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByObject("objectType12", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  private MetadataItem createNewMetadataItem(String type,
                                             String name,
                                             String objectType,
                                             String objectId,
                                             String parentObjectId,
                                             long creatorId,
                                             long audienceId,
                                             Map<String, String> properties) throws IllegalAccessException {
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setObjectId(objectId);
    metadataItem.setObjectType(objectType);
    metadataItem.setParentObjectId(parentObjectId);
    metadataItem.setProperties(properties);

    return metadataService.createMetadataItem(metadataItem,
                                              type,
                                              name,
                                              audienceId,
                                              creatorId);
  }

  private MetadataItem newMetadataItemInstance(String objectType,
                                               String objectId,
                                               String parentObjectId,
                                               Map<String, String> properties) {
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setObjectId(objectId);
    metadataItem.setObjectType(objectType);
    metadataItem.setParentObjectId(parentObjectId);
    metadataItem.setProperties(properties);
    return metadataItem;
  }

  private Metadata newMetadataInstance(long audienceId,
                                       long creatorId,
                                       String name,
                                       MetadataType metadataType,
                                       Map<String, String> properties) {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(audienceId);
    metadata.setCreatorId(creatorId);
    metadata.setName(name);
    metadata.setType(metadataType);
    metadata.setProperties(properties);
    return metadata;
  }

  public final class UserOnlyMetadataPlugin extends MetadataTypePlugin {
    public UserOnlyMetadataPlugin() {
      super(new MetadataType(1, "user"));
    }

    @Override
    public boolean canCreateMetadata(Metadata metadata, long userIdentityId) {
      return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canUpdateMetadata(Metadata metadata, long userIdentityId) {
      return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canDeleteMetadata(Metadata metadata, long userIdentityId) {
      return userIdentityId > 0 && metadata.getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canCreateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canUpdateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
    }

    @Override
    public boolean canDeleteMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return userIdentityId > 0 && metadataItem.getMetadata().getAudienceId() == userIdentityId;
    }
  }

  public final class SpaceManagerOnlyMetadataPlugin extends MetadataTypePlugin {
    public SpaceManagerOnlyMetadataPlugin() {
      super(new MetadataType(2, "space"));
    }

    @Override
    public boolean canCreateMetadata(Metadata metadata, long userIdentityId) {
      return isSpaceAudienceManager(metadata, userIdentityId);
    }

    @Override
    public boolean canUpdateMetadata(Metadata metadata, long userIdentityId) {
      return isSpaceAudienceManager(metadata, userIdentityId);
    }

    @Override
    public boolean canDeleteMetadata(Metadata metadata, long userIdentityId) {
      return isSpaceAudienceManager(metadata, userIdentityId);
    }

    @Override
    public boolean canCreateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return isSpaceAudienceManager(metadataItem.getMetadata(), userIdentityId);
    }

    @Override
    public boolean canUpdateMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return isSpaceAudienceManager(metadataItem.getMetadata(), userIdentityId);
    }

    @Override
    public boolean canDeleteMetadataItem(MetadataItem metadataItem, long userIdentityId) {
      return isSpaceAudienceManager(metadataItem.getMetadata(), userIdentityId);
    }

    private boolean isSpaceAudienceManager(Metadata metadata, long userIdentityId) {
      Identity audienceIdentity = identityManager.getIdentity(String.valueOf(metadata.getAudienceId()));
      if (audienceIdentity == null || !audienceIdentity.isSpace()) {
        return false;
      }
      Identity userIdentity = identityManager.getIdentity(String.valueOf(userIdentityId));
      Space space = spaceService.getSpaceByPrettyName(audienceIdentity.getRemoteId());
      return SpaceUtils.isSpaceManagerOrSuperManager(userIdentity.getRemoteId(), space.getGroupId());
    }
  }

}
