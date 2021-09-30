/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.metadata;

import static org.junit.Assert.assertNotEquals;

import java.util.List;

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

  private static boolean  shareable;

  private Identity        rootIdentity;

  private Identity        johnIdentity;

  private Identity        maryIdentity;

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

        @Override
        public boolean isShareable() {
          return MetadataServiceTest.shareable;
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
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    MetadataServiceTest.allowMultipleItemsPerObject = false;
    MetadataServiceTest.shareable = false;
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
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

    assertNotNull(metadataService.getMetadataTypePluginByName(userMetadataType.getName()));
    assertNotNull(metadataService.getMetadataTypePluginByName(spaceMetadataType.getName()));
  }

  public void testCreateMetadata() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String name = "testMetadata1";

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
                                                         null),
                                     creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadata(newMetadataInstance(audienceId,
                                                         creatorId,
                                                         name,
                                                         userMetadataType),
                                     0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadata(newMetadataInstance(audienceId,
                                                         creatorId,
                                                         name,
                                                         new MetadataType(3, "test")),
                                     creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }

    Metadata storedMetadata = metadataService.createMetadata(newMetadataInstance(audienceId,
                                                                                 creatorId,
                                                                                 name,
                                                                                 userMetadataType),
                                                             creatorId);
    assertNotNull(storedMetadata);
    assertTrue(storedMetadata.getId() > 0);
    assertTrue(storedMetadata.getCreatedDate() > 0);
    assertEquals(creatorId, storedMetadata.getCreatorId());
    assertEquals(audienceId, storedMetadata.getAudienceId());
    assertEquals(name, storedMetadata.getName());
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
    MetadataKey metadataKey = new MetadataKey(type, name, audienceId);

    MetadataObjectKey metadataObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    try {
      metadataService.createMetadataItem(null,
                                         metadataKey,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(newMetadataObjectInstance(null, objectId, parentObjectId),
                                         metadataKey,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(newMetadataObjectInstance(objectType, null, parentObjectId),
                                         metadataKey,
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(metadataObject,
                                         new MetadataKey(null, name, audienceId),
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(metadataObject,
                                         metadataKey,
                                         0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      metadataService.createMetadataItem(metadataObject,
                                         new MetadataKey("test",
                                                         name,
                                                         audienceId),
                                         creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }

    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataObject,
                                                                         new MetadataKey(type,
                                                                                         name,
                                                                                         audienceId),
                                                                         creatorId);
    assertNotNull(storedMetadataItem);
    assertTrue(storedMetadataItem.getId() > 0);
    assertTrue(storedMetadataItem.getCreatedDate() > 0);
    assertEquals(creatorId, storedMetadataItem.getCreatorId());
    assertNotNull(storedMetadataItem.getMetadata());
    assertEquals(audienceId, storedMetadataItem.getMetadata().getAudienceId());
    assertEquals(name, storedMetadataItem.getMetadata().getName());
    assertEquals(userMetadataType, storedMetadataItem.getMetadata().getType());
    assertEquals(parentObjectId, storedMetadataItem.getParentObjectId());
    assertEquals(objectId, storedMetadataItem.getObjectId());
    assertEquals(objectType, storedMetadataItem.getObjectType());

    try {
      storedMetadataItem = metadataService.createMetadataItem(metadataObject,
                                                              new MetadataKey(type,
                                                                              name,
                                                                              audienceId),
                                                              creatorId);
      fail();
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }

    MetadataServiceTest.allowMultipleItemsPerObject = true;
    try {
      MetadataItem secondStoredMetadataItem = metadataService.createMetadataItem(metadataObject,
                                                                                 new MetadataKey(type,
                                                                                                 name,
                                                                                                 audienceId),
                                                                                 creatorId);
      assertNotNull(secondStoredMetadataItem);
      assertNotEquals(storedMetadataItem.getId(), secondStoredMetadataItem.getId());
      assertTrue(secondStoredMetadataItem.getCreatedDate() > 0);
      assertEquals(creatorId, secondStoredMetadataItem.getCreatorId());
      assertNotNull(secondStoredMetadataItem.getMetadata());
      assertEquals(audienceId, secondStoredMetadataItem.getMetadata().getAudienceId());
      assertEquals(name, secondStoredMetadataItem.getMetadata().getName());
      assertEquals(userMetadataType, secondStoredMetadataItem.getMetadata().getType());
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

    createNewMetadataItem(userMetadataType.getName(),
                          name,
                          objectType,
                          objectId,
                          parentObjectId,
                          creatorId,
                          audienceId);
    try {
      createNewMetadataItem(userMetadataType.getName(),
                            name,
                            objectType,
                            objectId,
                            parentObjectId,
                            creatorId,
                            audienceId);
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
                          spaceIdentityId);

    try {
      createNewMetadataItem(spaceMetadataType.getName(),
                            name,
                            objectType,
                            objectId,
                            parentObjectId,
                            creatorId,
                            spaceIdentityId);
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

    MetadataObjectKey metadataItemObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItemObject,
                                                                         new MetadataKey(type,
                                                                                         name,
                                                                                         audienceId),
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
    assertEquals(parentObjectId, deletedMetadataItem.getParentObjectId());
    assertEquals(objectId, deletedMetadataItem.getObjectId());
    assertEquals(objectType, deletedMetadataItem.getObjectType());

    List<MetadataItem> metadataItems = getMetadataItemsByObject(objectType, objectId);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());
  }

  private List<MetadataItem> getMetadataItemsByObject(String objectType, String objectId) {
    return metadataService.getMetadataItemsByObject(new MetadataObjectKey(objectType, objectId));
  }

  public void testGetMetadataItemsByObject() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataType.getName();

    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId2",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType2",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType3",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType4",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);

    List<MetadataItem> metadataItems = getMetadataItemsByObject("objectType1", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataItems = getMetadataItemsByObject("objectType2", "objectId2");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = getMetadataItemsByObject("objectType2", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  public void testGetMetadataObjectIds() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataType.getName();

    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType1",
                          "objectId2",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType2",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType3",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    restartTransaction();
    createNewMetadataItem(type,
                          "testMetadata5",
                          "objectType4",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);

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

    createNewMetadataItem(type,
                          "testMetadata15",
                          "objectType11",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata15",
                          "objectType11",
                          "objectId2",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata16",
                          "objectType11",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata16",
                          "objectType12",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata16",
                          "objectType13",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata15",
                          "objectType14",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);

    List<MetadataItem> metadataItems = getMetadataItemsByObject("objectType11", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataItems = getMetadataItemsByObject("objectType12", "objectId2");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = getMetadataItemsByObject("objectType12", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());

    metadataService.deleteMetadataItemsByObject(new MetadataObjectKey("objectType11", "objectId1"));

    metadataItems = getMetadataItemsByObject("objectType11", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = getMetadataItemsByObject("objectType12", "objectId2");
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = getMetadataItemsByObject("objectType12", "objectId1");
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  public void testShareMetadataItemsByObject() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId1 = 10000l;
    long audienceId2 = 10001l;

    String type = userMetadataType.getName();

    String objectType1 = "objectType11";

    String objectId1 = "objectId1";
    String objectId2 = "objectId2";

    String metadataName1 = "testMetadata15";
    String metadataName2 = "testMetadata16";

    createNewMetadataItem(type,
                          metadataName1,
                          objectType1,
                          objectId1,
                          "parentObjectId1",
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName1,
                          objectType1,
                          objectId2,
                          "parentObjectId1",
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName1,
                          objectType1,
                          objectId2,
                          "parentObjectId1",
                          creatorId,
                          audienceId2);
    createNewMetadataItem(type,
                          metadataName2,
                          objectType1,
                          objectId1,
                          "parentObjectId1",
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName2,
                          "objectType12",
                          objectId1,
                          "parentObjectId1",
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName2,
                          "objectType13",
                          objectId1,
                          "parentObjectId1",
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName1,
                          "objectType14",
                          objectId1,
                          "parentObjectId1",
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName1,
                          "objectType14",
                          objectId1,
                          "parentObjectId1",
                          creatorId,
                          audienceId2);

    List<MetadataItem> metadataItems = getMetadataItemsByObject(objectType1, objectId1);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataItems = getMetadataItemsByObject(objectType1, objectId2);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataService.shareMetadataItemsByObject(new MetadataObjectKey(objectType1,
                                                                     objectId1),
                                               objectId2,
                                               audienceId2,
                                               Long.parseLong(maryIdentity.getId()));

    metadataItems = getMetadataItemsByObject(objectType1, objectId1);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataItems = getMetadataItemsByObject(objectType1, objectId2);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    MetadataServiceTest.shareable = true;
    try {
      metadataService.shareMetadataItemsByObject(new MetadataObjectKey(objectType1,
                                                                       objectId1),
                                                 objectId2,
                                                 audienceId2,
                                                 Long.parseLong(maryIdentity.getId()));

      metadataItems = getMetadataItemsByObject(objectType1, objectId1);
      assertNotNull(metadataItems);
      assertEquals(2, metadataItems.size());

      metadataItems = getMetadataItemsByObject(objectType1, objectId2);
      assertNotNull(metadataItems);
      assertEquals(3, metadataItems.size());
    } finally {
      MetadataServiceTest.shareable = false;
    }
  }

  public void testDeleteMetadataItemsByParentObject() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId1 = 10000l;
    long audienceId2 = 10001l;

    String type = userMetadataType.getName();

    String objectType1 = "objectType11";
    String objectType2 = "objectType17";

    String objectId1 = "objectId1";
    String objectId2 = "objectId2";

    String metadataName1 = "testMetadata15";
    String metadataName2 = "testMetadata16";

    String parentObjectId = "parentObjectId1";
    String parentObjectId2 = "parentObjectId2";

    createNewMetadataItem(type,
                          metadataName1,
                          objectType1,
                          objectId1,
                          parentObjectId,
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName1,
                          objectType1,
                          objectId2,
                          parentObjectId,
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName2,
                          objectType1,
                          objectId1,
                          parentObjectId,
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName2,
                          "objectType12",
                          objectId1,
                          parentObjectId,
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName2,
                          "objectType13",
                          objectId1,
                          parentObjectId,
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName1,
                          "objectType14",
                          objectId1,
                          parentObjectId,
                          creatorId,
                          audienceId1);
    createNewMetadataItem(type,
                          metadataName1,
                          "objectType14",
                          objectId1,
                          parentObjectId,
                          creatorId,
                          audienceId2);
    createNewMetadataItem(type,
                          metadataName1,
                          objectType2,
                          objectId1,
                          parentObjectId2,
                          creatorId,
                          audienceId2);

    List<MetadataItem> metadataItems = getMetadataItemsByObject(objectType1, objectId1);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    metadataItems = getMetadataItemsByObject(objectType2, objectId1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());

    metadataService.deleteMetadataItemsByParentObject(new MetadataObjectKey(objectType1, null, parentObjectId));

    metadataItems = getMetadataItemsByObject(objectType1, objectId1);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = getMetadataItemsByObject(objectType2, objectId1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  private MetadataItem createNewMetadataItem(String type,
                                             String name,
                                             String objectType,
                                             String objectId,
                                             String parentObjectId,
                                             long creatorId,
                                             long audienceId) throws ObjectAlreadyExistsException {
    MetadataItem metadataItem = new MetadataItem();
    metadataItem.setObjectId(objectId);
    metadataItem.setObjectType(objectType);
    metadataItem.setParentObjectId(parentObjectId);
    return metadataService.createMetadataItem(metadataItem.getObject(),
                                              new MetadataKey(type,
                                                              name,
                                                              audienceId),
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

  private MetadataObjectKey newMetadataObjectInstance(String objectType, String objectId, String parentObjectId) {
    return new MetadataObjectKey(objectType, objectId, parentObjectId);
  }

  private Metadata newMetadataInstance(long audienceId,
                                       long creatorId,
                                       String name,
                                       MetadataType metadataType) {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(audienceId);
    metadata.setCreatorId(creatorId);
    metadata.setName(name);
    metadata.setType(metadataType);
    return metadata;
  }

}
