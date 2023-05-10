/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.metadata;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

@SuppressWarnings("removal")
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

  private List<Space>     tearDownSpaceList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    userMetadataType = new MetadataType(1000, "user");
    spaceMetadataType = new MetadataType(2000, "space");
    tearDownSpaceList = new ArrayList<>();

    if (metadataService.getMetadataTypeByName(userMetadataType.getName()) == null) {
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
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
    }
    if (metadataService.getMetadataTypeByName(spaceMetadataType.getName()) == null) {
      MetadataTypePlugin spaceMetadataTypePlugin = new MetadataTypePlugin(newParam(2000, "space")) {
        @Override
        public boolean isAllowMultipleItemsPerObject() {
          return true;
        }
      };
      metadataService.addMetadataTypePlugin(spaceMetadataTypePlugin);
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

    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

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
                                                         null,
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
                                                         null,
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
                                                         null,
                                                         new MetadataType(3, "test")),
                                     creatorId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    HashMap<String, String> properties = new HashMap<>();
    properties.put("description", "description1");
    Metadata storedMetadata = metadataService.createMetadata(newMetadataInstance(audienceId,
                                                                                 creatorId,
                                                                                 name,
                                                                                 properties,
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

  public void testDeleteMetadata() {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String metadata1Name = "testMetadata1";
    Metadata metadata1 = metadataService.createMetadata(newMetadataInstance(audienceId,
                                                                            creatorId,
                                                                            metadata1Name,
                                                                            null,
                                                                            userMetadataType),
                                                        creatorId);
    String metadata2Name = "testMetadata2";
    Metadata metadata2 = metadataService.createMetadata(newMetadataInstance(audienceId,
                                                                            creatorId,
                                                                            metadata2Name,
                                                                            null,
                                                                            userMetadataType),
                                                        creatorId);
    List<Metadata> metadatas = metadataService.getMetadatas(userMetadataType.getName(), -1);
    assertEquals(2, metadatas.size());
    assertEquals(metadata1Name, metadatas.get(0).getName());

    metadataService.deleteMetadataById(metadata1.getId());
    metadatas = metadataService.getMetadatas(userMetadataType.getName(), -1);

    assertEquals(1, metadatas.size());
    assertEquals(metadata2Name, metadatas.get(0).getName());

    metadataService.deleteMetadataById(metadata2.getId());
    metadatas = metadataService.getMetadatas(userMetadataType.getName(), -1);

    assertEquals(0, metadatas.size());
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

    MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
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

  public void testGetMetadataItemsByMetadataNameAndTypeAndSpaceIds() throws ObjectAlreadyExistsException {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    long spaceId = 200l;
    String objectId = "objectId105";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType16";
    String type = userMetadataType.getName();
    String name = "testMetadata11";

    MetadataObject metadataItemObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    metadataItemObject.setSpaceId(spaceId);
    MetadataKey metadataKey = new MetadataKey(type, name, audienceId);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItemObject, metadataKey, creatorId);

    restartTransaction();
    List<MetadataItem> metadataItems =
                                     metadataService.getMetadataItemsByMetadataNameAndTypeAndSpaceIds(name,
                                                                                                      type,
                                                                                                      Collections.singletonList(spaceId),
                                                                                                      0,
                                                                                                      10);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals(storedMetadataItem.getId(), metadataItems.get(0).getId());
  }

  public void testGetMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty() throws ObjectAlreadyExistsException {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectIdTest2";
    String parentObjectId = "parentObjectIdTest2";
    String objectType = "objectTypeTest2";
    String name1 = "testMetadataName1";
    String name2 = "testMetadataName2";
    String type = userMetadataType.getName();
    MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    Map<String, String> properties = new LinkedHashMap<>();
    properties.put("staged", String.valueOf(false));

    metadataService.createMetadataItem(metadataObject, new MetadataKey(type, name1, audienceId), properties, creatorId);

    Map<String, String> properties1 = new LinkedHashMap<>();
    properties1.put("staged", String.valueOf(true));
    metadataService.createMetadataItem(metadataObject, new MetadataKey(type, name2, audienceId), properties1, creatorId);

    List<MetadataItem> metadataItems =
                                     metadataService.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(name1,
                                                                                                                           type,
                                                                                                                           objectType,
                                                                                                                           "staged",
                                                                                                                           String.valueOf(false),
                                                                                                                           0,
                                                                                                                           10);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals(objectType, metadataItems.get(0).getObjectType());
    assertEquals(type, metadataItems.get(0).getMetadataTypeName());

    // Staged property is true
    metadataItems = metadataService.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(name1,
                                                                                                          type,
                                                                                                          objectType,
                                                                                                          "staged",
                                                                                                          String.valueOf(true),
                                                                                                          0,
                                                                                                          10);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());
  }

  public void testGetMetadataItemsByMetadataNameAndTypeAndObject() {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectIdTestJohn";
    String parentObjectId = "parentObjectIdTestJohn";
    String objectType = "objectTypeTestJohn";
    String name = "testMetadataJohn";
    String type = userMetadataType.getName();
    MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    MetadataKey metadataKey = new MetadataKey(type, name, audienceId);

    try {
      metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(name,
                                                                                                      type,
                                                                                                      objectType,
                                                                                                      0,
                                                                                                      10);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals(objectType, metadataItems.get(0).getObjectType());
    assertEquals(type, metadataItems.get(0).getMetadataTypeName());
  }

  public void testGetMetadatas() {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String name = "testMetadata1User1";

    try {
      metadataService.createMetadata(newMetadataInstance(audienceId,
                                                         creatorId,
                                                         name,
                                                         null,
                                                         spaceMetadataType),
                                     creatorId);
    } catch (IllegalArgumentException e) {
      // Expected
    }

    List<Metadata> metadataList = metadataService.getMetadatas("space", 100);

    assertNotNull(metadataList);
    assertEquals(1, metadataList.size());
  }

  public void testGetReferencedMetadatas() {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String name = "testMetadata1User1";
    String name1 = "testMetadata1User12";
    try {
      Metadata metadata = new Metadata();
      metadata.setType(spaceMetadataType);
      metadata.setName(name);
      metadata.setAudienceId(audienceId);
      metadata.setCreatorId(creatorId);
      HashMap<String, String> properties = new HashMap<>();
      properties.put("referenced", "true");
      metadata.setProperties(properties);
      metadataService.createMetadata(metadata, creatorId);

      Metadata metadata1 = new Metadata();
      metadata1.setType(spaceMetadataType);
      metadata1.setName(name1);
      metadata1.setAudienceId(audienceId);
      metadata1.setCreatorId(creatorId);
      HashMap<String, String> properties1 = new HashMap<>();
      properties1.put("referenced", "false");
      metadata1.setProperties(properties1);
      metadataService.createMetadata(metadata1, creatorId);
    } catch (IllegalArgumentException e) {
      // Expected
    }
    List<Metadata> metadataList = metadataService.getMetadatasByProperty("referenced", "true", 100);
    assertNotNull(metadataList);
    assertEquals(1, metadataList.size());
  }

  public void testGetMetadataItemsByMetadataTypeAndObject() {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectIdTest";
    String parentObjectId = "parentObjectIdTest";
    String objectType = "objectTypeTest";
    String name = "testMetadataJohn";
    String type = userMetadataType.getName();
    MetadataKey metadataKey = new MetadataKey(type, name, audienceId);
    MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);

    try {
      metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject(userMetadataType.getName(),
                                                                                               metadataObject);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  public void testGetMetadataNamesByMetadataTypeAndObject() {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectIdTest";
    String parentObjectId = "parentObjectIdTest";
    String objectType = "objectTypeTest";
    String name = "testMetadataJohn";
    String type = userMetadataType.getName();
    MetadataKey metadataKey = new MetadataKey(type, name, audienceId);
    MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);

    try {
      metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }
    List<String> metadataNames = metadataService.getMetadataNamesByMetadataTypeAndObject(userMetadataType.getName(),
                                                                                         objectType,
                                                                                         objectId);
    assertNotNull(metadataNames);
    assertEquals(1, metadataNames.size());
  }

  public void testDeleteMetadataItemsByMetadataTypeAndObject() {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectIdTest1";
    String parentObjectId = "parentObjectIdTest1";
    String objectType = "objectTypeTest1";
    String name = "testMetadataJohn1";
    String type = userMetadataType.getName();
    MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    try {
      MetadataKey metadataKey1 = new MetadataKey(type, name, audienceId);
      metadataService.createMetadataItem(metadataObject, metadataKey1, creatorId);
      name = "testMetadataJohn2";
      MetadataKey metadataKey2 = new MetadataKey(type, name, audienceId);
      metadataService.createMetadataItem(metadataObject, metadataKey2, creatorId);

    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject(userMetadataType.getName(),
                                                                                               metadataObject);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());
    metadataService.deleteMetadataItemsByMetadataTypeAndObject(userMetadataType.getName(), metadataObject);
    metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject(userMetadataType.getName(), metadataObject);
    assertEquals(0, metadataItems.size());
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

  public void testDeleteMetadataItemByUserIdentity() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId10";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType11";
    String type = userMetadataType.getName();
    String name = "testMetadata8";

    MetadataObject metadataItemObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItemObject,
                                                                         new MetadataKey(type,
                                                                                         name,
                                                                                         audienceId),
                                                                         creatorId);

    assertThrows(IllegalArgumentException.class, () -> metadataService.deleteMetadataItem(0, creatorId));
    assertThrows(IllegalArgumentException.class, () -> metadataService.deleteMetadataItem(storedMetadataItem.getId(), 0));
    assertThrows(ObjectNotFoundException.class, () -> metadataService.deleteMetadataItem(5000l, creatorId));

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

  public void testDeleteMetadataItem() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String objectId = "objectId100";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType11";
    String type = userMetadataType.getName();
    String name = "testMetadata8";

    MetadataObject metadataItemObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItemObject,
                                                                         new MetadataKey(type,
                                                                                         name,
                                                                                         audienceId),
                                                                         creatorId);
    assertThrows(IllegalArgumentException.class, () -> metadataService.deleteMetadataItem(0, true));
    assertThrows(ObjectNotFoundException.class, () -> metadataService.deleteMetadataItem(5000l, true));

    MetadataItem deletedMetadataItem = metadataService.deleteMetadataItem(storedMetadataItem.getId(),
                                                                          true);

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

  public void testDeleteMetadataItemsBySpaceId() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    long spaceId = 200l;
    String objectId = "objectId100";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType11";
    String type = userMetadataType.getName();
    String name = "testMetadata8";

    MetadataObject metadataItemObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    metadataItemObject.setSpaceId(spaceId);
    MetadataKey metadataKey = new MetadataKey(type,
                                              name,
                                              audienceId);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItemObject,
                                                                         metadataKey,
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
    assertEquals(spaceId, storedMetadataItem.getSpaceId());

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataItemObject);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());

    storedMetadataItem = metadataItems.get(0);
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
    assertEquals(spaceId, storedMetadataItem.getSpaceId());

    assertThrows(IllegalArgumentException.class, () -> metadataService.deleteMetadataBySpaceId(0));

    assertEquals(0, metadataService.deleteMetadataBySpaceId(spaceId + 1));
    assertEquals(1, metadataService.deleteMetadataBySpaceId(spaceId));

    metadataItems = getMetadataItemsByObject(objectType, objectId);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());
  }

  public void testDeleteMetadataItemsBySpaceIdAndAudienceId() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    long spaceId = 200l;
    String objectId = "objectId100";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType11";
    String type = userMetadataType.getName();
    String name = "testMetadata8";

    MetadataObject metadataItemObject = newMetadataObjectInstance(objectType, objectId, parentObjectId);
    metadataItemObject.setSpaceId(spaceId);
    MetadataKey metadataKey = new MetadataKey(type,
                                              name,
                                              audienceId);
    MetadataItem storedMetadataItem = metadataService.createMetadataItem(metadataItemObject,
                                                                         metadataKey,
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
    assertEquals(spaceId, storedMetadataItem.getSpaceId());

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataItemObject);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());

    storedMetadataItem = metadataItems.get(0);
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
    assertEquals(spaceId, storedMetadataItem.getSpaceId());

    assertThrows(IllegalArgumentException.class, () -> metadataService.deleteMetadataBySpaceIdAndAudienceId(0, audienceId));
    assertThrows(IllegalArgumentException.class, () -> metadataService.deleteMetadataBySpaceIdAndAudienceId(spaceId, 0));

    assertEquals(0, metadataService.deleteMetadataBySpaceIdAndAudienceId(spaceId + 1, audienceId));
    assertEquals(1, metadataService.deleteMetadataBySpaceIdAndAudienceId(spaceId, audienceId));

    metadataItems = getMetadataItemsByObject(objectType, objectId);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());
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

  public void testGetMetadataItemsByMetadataTypeAndCreator() throws Exception { // NOSONAR

    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataType.getName();
    createNewMetadataItem(type,
                          "testMetadata1",
                          "objectType1",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata2",
                          "objectType1",
                          "objectId2",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata3",
                          "objectType2",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata4",
                          "objectType2",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndCreator("space", creatorId, 0, 2);

    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByMetadataTypeAndCreator("space", 100, 0, 5);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = metadataService.getMetadataItemsByMetadataTypeAndCreator(type, creatorId, 0, 5);
    assertNotNull(metadataItems);
    assertEquals(4, metadataItems.size());
  }

  public void testGetMetadataObjectIds() throws Exception {
    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String type = userMetadataType.getName();
    List<String> objectIds = new ArrayList<>();

    MetadataServiceTest.allowMultipleItemsPerObject = true;
    try {
      for (int i = 0; i < 10; i++) {
        String objectId = "objectId-" + i;
        createNewMetadataItem(type,
                              "testMetadata50",
                              "objectType1",
                              objectId,
                              "parentObjectId1",
                              creatorId,
                              audienceId);
        objectIds.add(0, objectId);
        restartTransaction();
      }
      for (int i = 19; i > 9; i--) {
        String objectId = "objectId-" + (30 - i);
        createNewMetadataItem(type,
                              "testMetadata50",
                              "objectType1",
                              objectId,
                              "parentObjectId1",
                              creatorId,
                              audienceId);
        objectIds.add(0, objectId);
        restartTransaction();
      }
    } finally {
      MetadataServiceTest.allowMultipleItemsPerObject = false;
    }
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
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType3",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);
    createNewMetadataItem(type,
                          "testMetadata6",
                          "objectType4",
                          "objectId1",
                          "parentObjectId1",
                          creatorId,
                          audienceId);

    List<String> metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata50", "objectType1", 0, 2);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());
    assertEquals(objectIds.get(0), metadataItems.get(0));
    assertEquals(objectIds.get(1), metadataItems.get(1));

    metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata50", "objectType1", 1, 1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals(objectIds.get(1), metadataItems.get(0));

    metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata50", "objectType1", 0, 1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals(objectIds.get(0), metadataItems.get(0));

    metadataItems = metadataService.getMetadataObjectIds(type, "testMetadata50", "objectType1", 0, 20);
    assertNotNull(metadataItems);
    assertEquals(20, metadataItems.size());
    for (int i = 0; i < metadataItems.size(); i++) {
      assertEquals(objectIds.get(i), metadataItems.get(i));
    }

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

    metadataService.deleteMetadataItemsByObject(new MetadataObject("objectType11", "objectId1"));

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

    metadataService.shareMetadataItemsByObject(new MetadataObject(objectType1,
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
      metadataService.shareMetadataItemsByObject(new MetadataObject(objectType1,
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

    metadataService.deleteMetadataItemsByParentObject(new MetadataObject(objectType1, null, parentObjectId));

    metadataItems = getMetadataItemsByObject(objectType1, objectId1);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    metadataItems = getMetadataItemsByObject(objectType2, objectId1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
  }

  public void testFindMetadataNamesByCreator() throws Exception { // NOSONAR
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType";
    String type = userMetadataType.getName();

    List<Space> spaces = new ArrayList<>();
    List<Long> spaceCreators = new ArrayList<>();
    Space space = createSpace("FindMetadataNamesByCreator1",
                              maryIdentity.getRemoteId(),
                              maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(maryIdentity.getId()));
    space = createSpace("FindMetadataNamesByCreator2",
                        johnIdentity.getRemoteId(),
                        johnIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(johnIdentity.getId()));
    space = createSpace("FindMetadataNamesByCreator3",
                        maryIdentity.getRemoteId(),
                        johnIdentity.getRemoteId(),
                        maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(maryIdentity.getId()));

    List<String> metadataNames = Arrays.asList("foo",
                                               "bar",
                                               "baz",
                                               "qux",
                                               "quux",
                                               "bar",
                                               "baz",
                                               "qux",
                                               "quux",
                                               "quuz",
                                               "corge",
                                               "grault",
                                               "garply",
                                               "waldo",
                                               "fred",
                                               "plugh",
                                               "xyzzy",
                                               "thud",
                                               "Wibble",
                                               "WOBBLE",
                                               "WUBBLE",
                                               "FLOB",
                                               "FOO",
                                               "BAR",
                                               "BAZ",
                                               "QUX",
                                               "QUUX",
                                               "QUUZ",
                                               "CORGE",
                                               "GRAULT",
                                               "GARPLY",
                                               "WALDO",
                                               "FRED",
                                               "PLUGH",
                                               "XYZZY",
                                               "THUD",
                                               "WIBBLE",
                                               "WOBBLE",
                                               "WUBBLE",
                                               "FLOB");

    for (int i = 0; i < metadataNames.size(); i++) {
      long audienceId = Long.parseLong(spaces.get(i % 3).getId());
      MetadataKey metadataKey = new MetadataKey(type, metadataNames.get(i), audienceId);
      MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId + i, parentObjectId);
      metadataService.createMetadataItem(metadataObject,
                                         metadataKey,
                                         spaceCreators.get(i % 3));
    }

    List<String> allUserNames = metadataService.findMetadataNamesByCreator(null, type, Long.parseLong(rootIdentity.getId()), 100);
    assertNotNull(allUserNames);
    assertEquals(0, allUserNames.size());

    allUserNames = metadataService.findMetadataNamesByCreator(null, type, Long.parseLong(maryIdentity.getId()), 100);
    assertNotNull(allUserNames);
    assertEquals(24, allUserNames.size());

    String term = "ar";
    int count = countTerms(allUserNames, term);
    List<String> names = metadataService.findMetadataNamesByCreator(term, type, Long.parseLong(maryIdentity.getId()), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "az";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByCreator(term, type, Long.parseLong(maryIdentity.getId()), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "oo";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByCreator(term, type, Long.parseLong(maryIdentity.getId()), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    allUserNames = metadataService.findMetadataNamesByCreator(null, type, Long.parseLong(johnIdentity.getId()), 100);
    assertNotNull(allUserNames);
    assertEquals(12, allUserNames.size());

    term = "ar";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByCreator(term, type, Long.parseLong(johnIdentity.getId()), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "az";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByCreator(term, type, Long.parseLong(johnIdentity.getId()), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "oo";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByCreator(term, type, Long.parseLong(johnIdentity.getId()), 100);
    assertNotNull(names);
    assertEquals(count, names.size());
  }

  public void testFindMetadataNamesByAudiences() throws Exception { // NOSONAR
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    String objectType = "objectType";
    String type = userMetadataType.getName();

    List<Space> spaces = new ArrayList<>();
    List<Long> spaceIds = new ArrayList<>();
    List<Long> spaceCreators = new ArrayList<>();
    Space space = createSpace("FindMetadataNamesByAudiences1",
                              maryIdentity.getRemoteId(),
                              maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(maryIdentity.getId()));
    spaceIds.add(Long.parseLong(space.getId()));
    space = createSpace("FindMetadataNamesByAudiences2",
                        johnIdentity.getRemoteId(),
                        johnIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(johnIdentity.getId()));
    spaceIds.add(Long.parseLong(space.getId()));
    space = createSpace("FindMetadataNamesByAudiences3",
                        maryIdentity.getRemoteId(),
                        johnIdentity.getRemoteId(),
                        maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(maryIdentity.getId()));
    spaceIds.add(Long.parseLong(space.getId()));

    List<String> metadataNames = Arrays.asList("foo",
                                               "bar",
                                               "baz",
                                               "qux",
                                               "quux",
                                               "bar",
                                               "baz",
                                               "qux",
                                               "quux",
                                               "quuz",
                                               "corge",
                                               "grault",
                                               "garply",
                                               "waldo",
                                               "fred",
                                               "plugh",
                                               "xyzzy",
                                               "thud",
                                               "Wibble",
                                               "WOBBLE",
                                               "WUBBLE",
                                               "FLOB",
                                               "FOO",
                                               "BAR",
                                               "BAZ",
                                               "QUX",
                                               "QUUX",
                                               "QUUZ",
                                               "CORGE",
                                               "GRAULT",
                                               "GARPLY",
                                               "WALDO",
                                               "FRED",
                                               "PLUGH",
                                               "XYZZY",
                                               "THUD",
                                               "WIBBLE",
                                               "WOBBLE",
                                               "WUBBLE",
                                               "FLOB");

    for (int i = 0; i < metadataNames.size(); i++) {
      long audienceId = Long.parseLong(spaces.get(i % 3).getId());
      MetadataKey metadataKey = new MetadataKey(type, metadataNames.get(i), audienceId);
      MetadataObject metadataObject = newMetadataObjectInstance(objectType, objectId + i, parentObjectId);
      metadataService.createMetadataItem(metadataObject,
                                         metadataKey,
                                         spaceCreators.get(i % 3));
    }

    List<String> allUserNames = metadataService.findMetadataNamesByAudiences(null, type, Collections.emptySet(), 100);
    assertNotNull(allUserNames);
    assertEquals(0, allUserNames.size());

    allUserNames = metadataService.findMetadataNamesByAudiences(null, type, Collections.singleton(spaceIds.get(0)), 100);
    assertNotNull(allUserNames);
    assertEquals(13, allUserNames.size());

    String term = "ar";
    int count = countTerms(allUserNames, term);
    List<String> names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(0)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "az";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(0)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "oo";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(0)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    allUserNames = metadataService.findMetadataNamesByAudiences(null, type, Collections.singleton(spaceIds.get(1)), 100);
    assertNotNull(allUserNames);
    assertEquals(12, allUserNames.size());

    term = "ar";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(1)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "az";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(1)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "oo";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(1)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    allUserNames = metadataService.findMetadataNamesByAudiences(null, type, Collections.singleton(spaceIds.get(2)), 100);
    assertNotNull(allUserNames);
    assertEquals(12, allUserNames.size());

    term = "ar";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(2)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "az";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(2)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());

    term = "oo";
    count = countTerms(allUserNames, term);
    names = metadataService.findMetadataNamesByAudiences(term, type, Collections.singleton(spaceIds.get(2)), 100);
    assertNotNull(names);
    assertEquals(count, names.size());
  }

  public void testAddMetadataInitPlugin() {
    InitParams params = new InitParams();
    Metadata metadata = new Metadata();
    metadata.setAudienceId(2l);
    metadata.setName("test8");
    metadata.setType(userMetadataType);
    metadata.setProperties(Collections.singletonMap("propName", "propValue"));

    ObjectParameter parameter = new ObjectParameter();
    parameter.setName("metadata");
    parameter.setObject(metadata);
    params.addParameter(parameter);
    MetadataInitPlugin initPlugin = new MetadataInitPlugin(params);
    metadataService.addMetadataPlugin(initPlugin);

    ((Startable) metadataService).start();

    Metadata storedMetadata = metadataService.getMetadataByKey(new MetadataKey(metadata.getTypeName(),
                                                                               metadata.getName(),
                                                                               metadata.getAudienceId()));
    assertNotNull(storedMetadata);
    assertEquals(metadata.getName(), storedMetadata.getName());
    assertEquals(metadata.getTypeName(), storedMetadata.getTypeName());
    assertEquals(metadata.getAudienceId(), storedMetadata.getAudienceId());
    assertEquals(metadata.getProperties(), storedMetadata.getProperties());
    assertTrue(storedMetadata.getCreatedDate() > 0);
    assertEquals(0, storedMetadata.getCreatorId());
  }

  private int countTerms(List<String> names, String term) {
    return (int) names.stream()
                      .filter(name -> StringUtils.containsIgnoreCase(name, term))
                      .count();
  }

  private List<MetadataItem> getMetadataItemsByObject(String objectType, String objectId) {
    return metadataService.getMetadataItemsByObject(new MetadataObject(objectType, objectId));
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

  private MetadataObject newMetadataObjectInstance(String objectType, String objectId, String parentObjectId) {
    return new MetadataObject(objectType, objectId, parentObjectId);
  }

  private Metadata newMetadataInstance(long audienceId,
                                       long creatorId,
                                       String name,
                                       Map<String, String> properties,
                                       MetadataType metadataType) {
    Metadata metadata = new Metadata();
    metadata.setAudienceId(audienceId);
    metadata.setCreatorId(creatorId);
    metadata.setName(name);
    metadata.setType(metadataType);
    metadata.setProperties(properties);
    return metadata;
  }

  @SuppressWarnings("deprecation")
  private Space createSpace(String spaceName, String creator, String... members) throws Exception {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String[] managers = new String[] { creator };
    String[] spaceMembers = members == null ? new String[] { creator } : members;
    space.setManagers(managers);
    space.setMembers(spaceMembers);
    spaceService.saveSpace(space, true); // NOSONAR
    tearDownSpaceList.add(space);
    return space;
  }
}
