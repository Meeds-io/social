package org.exoplatform.social.metadata;

import static org.junit.Assert.assertNotEquals;

import java.util.*;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.model.*;

public class MetadataServiceTest extends AbstractCoreTest {

  private static boolean  allowMultipleItemsPerObject;

  private Identity        rootIdentity;

  private Identity        johnIdentity;

  private IdentityManager identityManager;

  private MetadataService metadataService;

  private MetadataDAO     metadataDAO;

  private MetadataType    userMetadataType;

  private MetadataType    spaceMetadataType;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    try {
      MetadataTypePlugin userMetadataTypePlugin = new MetadataTypePlugin(newParam(1000, "user")) {
        @Override
        public boolean isAllowMultipleItemsPerObject() {
          return MetadataServiceTest.allowMultipleItemsPerObject;
        }
      };
      userMetadataType = userMetadataTypePlugin.getMetadataType();
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }
    try {
      MetadataTypePlugin spaceMetadataTypePlugin = new MetadataTypePlugin(newParam(2000, "space")) {
        @Override
        public boolean isAllowMultipleItemsPerObject() {
          return true;
        }
      };
      spaceMetadataType = spaceMetadataTypePlugin.getMetadataType();
      metadataService.addMetadataTypePlugin(spaceMetadataTypePlugin);
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    MetadataServiceTest.allowMultipleItemsPerObject = false;
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    metadataDAO.deleteAll();

    super.tearDown();
  }

  public void testMetadataTypePlugin() {
    try {
      metadataService.addMetadataTypePlugin(new MetadataTypePlugin(newParam(2, "space")));
      fail("Should throw an exception when attempting to redefine an existing ");
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    List<MetadataType> metadataTypes = metadataService.getMetadataTypes();
    assertNotNull(metadataTypes);
    assertTrue(metadataTypes.size() >= 2);

    assertNotNull(metadataService.getMetadataTypePluginByName("user"));
    assertNotNull(metadataService.getMetadataTypePluginByName("space"));
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
                                                         userMetadataType,
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

    Metadata storedMetadata = metadataService.createMetadata(newMetadataInstance(audienceId,
                                                                                 creatorId,
                                                                                 name,
                                                                                 userMetadataType,
                                                                                 properties),
                                                             creatorId);
    assertNotNull(storedMetadata);
    assertTrue(storedMetadata.getId() > 0);
    assertTrue(storedMetadata.getCreatedDate() > 0);
    assertEquals(creatorId, storedMetadata.getCreatorId());
    assertEquals(audienceId, storedMetadata.getAudienceId());
    assertEquals(name, storedMetadata.getName());
    assertEquals(properties, storedMetadata.getProperties());
    assertEquals(userMetadataType, storedMetadata.getType());
  }

  public void testCreateMetadataItem() throws Exception { // NOSONAR
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType";
    String type = userMetadataType.getName();
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
    assertEquals(userMetadataType, storedMetadataItem.getMetadata().getType());
    assertEquals(properties, storedMetadataItem.getProperties());
    assertEquals(parentObjectId, storedMetadataItem.getParentObjectId());
    assertEquals(objectId, storedMetadataItem.getObjectId());
    assertEquals(objectType, storedMetadataItem.getObjectType());

    try {
      storedMetadataItem = metadataService.createMetadataItem(metadataItem,
                                                              type,
                                                              name,
                                                              audienceId,
                                                              creatorId);
      fail();
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }

    MetadataServiceTest.allowMultipleItemsPerObject = true;
    try {
      MetadataItem secondStoredMetadataItem = metadataService.createMetadataItem(metadataItem,
                                                                                 type,
                                                                                 name,
                                                                                 audienceId,
                                                                                 creatorId);
      assertNotNull(secondStoredMetadataItem);
      assertNotEquals(storedMetadataItem.getId(), secondStoredMetadataItem.getId());
      assertTrue(secondStoredMetadataItem.getCreatedDate() > 0);
      assertEquals(creatorId, secondStoredMetadataItem.getCreatorId());
      assertNotNull(secondStoredMetadataItem.getMetadata());
      assertEquals(audienceId, secondStoredMetadataItem.getMetadata().getAudienceId());
      assertEquals(name, secondStoredMetadataItem.getMetadata().getName());
      assertEquals(userMetadataType, secondStoredMetadataItem.getMetadata().getType());
      assertEquals(properties, secondStoredMetadataItem.getProperties());
      assertEquals(parentObjectId, secondStoredMetadataItem.getParentObjectId());
      assertEquals(objectId, secondStoredMetadataItem.getObjectId());
      assertEquals(objectType, secondStoredMetadataItem.getObjectType());
    } finally {
      MetadataServiceTest.allowMultipleItemsPerObject = false;
    }
  }

  public void testCreateDuplicatedMetadataItem() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType";
    String name = "testMetadata21";
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    createNewMetadataItem(userMetadataType.getName(),
                          name,
                          objectType,
                          objectId,
                          parentObjectId,
                          creatorId,
                          audienceId,
                          properties);
    try {
      createNewMetadataItem(userMetadataType.getName(),
                            name,
                            objectType,
                            objectId,
                            parentObjectId,
                            creatorId,
                            audienceId,
                            properties);
      fail("MetadataTypePlugin shouldn't allow to have twice the same object for a same Metadata");
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }

    Space space = getSpaceInstance(1, Space.PUBLIC, Space.VALIDATION, johnIdentity.getRemoteId());
    space = createSpaceNonInitApps(space, johnIdentity.getRemoteId(), null);

    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    long spaceIdentityId = Long.parseLong(spaceIdentity.getId());

    createNewMetadataItem(spaceMetadataType.getName(),
                          name,
                          objectType,
                          objectId,
                          parentObjectId,
                          creatorId,
                          spaceIdentityId,
                          properties);

    try {
      createNewMetadataItem(spaceMetadataType.getName(),
                            name,
                            objectType,
                            objectId,
                            parentObjectId,
                            creatorId,
                            spaceIdentityId,
                            properties);
    } catch (ObjectAlreadyExistsException e) {
      fail("MetadataTypePlugin should allow to have twice the same object for a same Metadata");
    }
  }

  public void testDeleteMetadataItem() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId10";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType11";
    String type = userMetadataType.getName();
    String name = "testMetadata8";
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    MetadataItem metadataItem = newMetadataItemInstance(objectType, objectId, parentObjectId, properties);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItem,
                                                                         type,
                                                                         name,
                                                                         audienceId,
                                                                         creatorId);

    try {
      metadataService.deleteMetadataItem(0, creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.deleteMetadataItem(storedMetadataItem.getId(), 0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    MetadataItem deletedMetadataItem = metadataService.deleteMetadataItem(storedMetadataItem.getId(),
                                                                          creatorId);

    assertNotNull(deletedMetadataItem);
    assertTrue(deletedMetadataItem.getId() > 0);
    assertTrue(deletedMetadataItem.getCreatedDate() > 0);
    assertEquals(creatorId, deletedMetadataItem.getCreatorId());
    assertNotNull(deletedMetadataItem.getMetadata());
    assertEquals(audienceId, deletedMetadataItem.getMetadata().getAudienceId());
    assertEquals(name, deletedMetadataItem.getMetadata().getName());
    assertEquals(userMetadataType, deletedMetadataItem.getMetadata().getType());
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
    String type = userMetadataType.getName();
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

    Metadata storedMetadata = metadataService.deleteMetadata(type, name, audienceId, creatorId);

    assertNotNull(storedMetadata);
    assertTrue(storedMetadata.getId() > 0);
    assertTrue(storedMetadata.getCreatedDate() > 0);
    assertEquals(creatorId, storedMetadata.getCreatorId());
    assertEquals(audienceId, storedMetadata.getAudienceId());
    assertEquals(name, storedMetadata.getName());
    assertNull(storedMetadata.getProperties());
    assertEquals(userMetadataType, storedMetadata.getType());
  }

  public void testGetMetadataItemsByObject() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataType.getName();
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

  public void testGetMetadataObjectIds() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataType.getName();
    Map<String, String> properties = Collections.singletonMap("testMetadataItem1Key", "testMetadataItem1Value");

    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId2",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType2",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType3",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType4",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId,
                          properties);

    List<String> metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata5", "objectType1", 0, 2);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());
    assertEquals("objectId2", metadataItems.get(0));
    assertEquals("objectId1", metadataItems.get(1));

    metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata5", "objectType1", 1, 2);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals("objectId1", metadataItems.get(0));

    metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata5", "objectType1", 0, 1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals("objectId2", metadataItems.get(0));

    metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata5", "objectType2", 0, 1);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata6", "objectType2", 0, 5);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  public void testDeleteMetadataItemsByObject() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataType.getName();
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
                                             Map<String, String> properties) throws ObjectAlreadyExistsException {
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

  private InitParams newParam(long id, String name) {
    InitParams params = new InitParams();
    MetadataType metadataType = new MetadataType(id, name);
    ObjectParameter parameter = new ObjectParameter();
    parameter.setName("metadataType");
    parameter.setObject(metadataType);
    params.addParameter(parameter);
    return params;
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

}
