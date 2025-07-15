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
package org.exoplatform.social.metadata.favorite;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.metadata.storage.MetadataStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;

public class FavoriteServiceTest extends AbstractCoreTest {

  private Identity        johnIdentity;

  private FavoriteService favoriteService;

  private MetadataService metadataService;

  private MetadataType    favoriteMetadataType;

  private MetadataType    userMetadataType;

  private MetadataDAO     metadataDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    favoriteService = getContainer().getComponentInstanceOfType(FavoriteService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    getContainer().getComponentInstanceOfType(MetadataStorage.class).clearCaches();

    favoriteMetadataType = new MetadataType(1, "favorites");
    userMetadataType = new MetadataType(2, "user");

    if (metadataService.getMetadataTypeByName(userMetadataType.getName()) == null) {
      MetadataTypePlugin userMetadataTypePlugin = new MetadataTypePlugin(newParam(1000, "user")) {
        @Override
        public boolean isAllowMultipleItemsPerObject() {
          return false;
        }

        @Override
        public boolean isShareable() {
          return true;
        }
      };
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
    }

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();
    identityManager.deleteIdentity(johnIdentity);
    metadataDAO.deleteAll();

    super.tearDown();
  }

  public void testCreateFavorite() throws Exception {
    String objectType = "type";
    String objectId = "1";
    String parentObjectId = "2";
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    long spaceId = 500l;
    Favorite favorite = new Favorite(objectType, objectId, parentObjectId, userIdentityId, spaceId);

    List<MetadataItem> metadataItemsByObject = metadataService.getMetadataItemsByObject(favorite.getObject());
    assertTrue(CollectionUtils.isEmpty(metadataItemsByObject));

    favoriteService.createFavorite(favorite);

    metadataItemsByObject = metadataService.getMetadataItemsByObject(favorite.getObject());
    assertEquals(1, metadataItemsByObject.size());
    MetadataItem metadataItem = metadataItemsByObject.get(0);
    assertEquals(userIdentityId, metadataItem.getCreatorId());
    assertEquals(objectId, metadataItem.getObjectId());
    assertEquals(objectType, metadataItem.getObjectType());
    assertEquals(parentObjectId, metadataItem.getParentObjectId());

    assertThrows(ObjectAlreadyExistsException.class, () -> favoriteService.createFavorite(favorite));
  }

  public void testGetFavoriteItemsByCreator() throws Exception {

    long userIdentityId = Long.parseLong(johnIdentity.getId());
    long audienceId = userIdentityId;
    String favoriteType = favoriteMetadataType.getName();
    String otherType = userMetadataType.getName();

    String objectType = "objectType1";
    createNewMetadataItem(favoriteType, "testMetadata1", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);
    createNewMetadataItem(otherType, "testMetadata2", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);
    createNewMetadataItem(favoriteType, "testMetadata3", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);
    createNewMetadataItem(otherType, "testMetadata4", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);
    createNewMetadataItem(favoriteType, "testMetadata5", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);
    createNewMetadataItem(otherType, "testMetadata6", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);

    List<MetadataItem> favoriteList = favoriteService.getFavoriteItemsByCreator(userIdentityId, 0, 5);
    assertEquals(3, favoriteList.size());
    assertEquals(3, favoriteService.getFavoriteItemsSize(userIdentityId));
    assertEquals(3, favoriteService.getFavoriteItemsSize(objectType, userIdentityId));

    favoriteList = favoriteService.getFavoriteItemsByCreator(100l, 0, 5);
    assertEquals(0, favoriteList.size());

  }
  
  public void testGetFavoriteItemsByCreatorAndType() throws Exception {
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    String objectType = "space";
    String otherObjectType = "activite";

    Favorite favorite1 = new Favorite(objectType, "objectId1", null, userIdentityId);
    favoriteService.createFavorite(favorite1);
    Thread.sleep(10); // NOSONAR
    Favorite favorite2 = new Favorite(objectType, "objectId2", null, userIdentityId);
    favoriteService.createFavorite(favorite2);
    Thread.sleep(10); // NOSONAR
    Favorite favorite3 = new Favorite(otherObjectType, "objectId3", null, userIdentityId);
    favoriteService.createFavorite(favorite3);
    Thread.sleep(10); // NOSONAR
    Favorite favorite4 = new Favorite(otherObjectType, "objectId4", null, userIdentityId + 1);
    favoriteService.createFavorite(favorite4);

    List<MetadataItem> favoritesList = favoriteService.getFavoriteItemsByCreatorAndType(objectType,
                                                                                        userIdentityId,
                                                                                        2,
                                                                                        2);
    assertEquals(0, favoritesList.size());

    favoritesList = favoriteService.getFavoriteItemsByCreatorAndType(objectType,
                                                                     userIdentityId,
                                                                     0,
                                                                     2);
    assertEquals(2, favoritesList.size());
    assertEquals(favorite2.getObjectId(), favoritesList.get(0).getObjectId());
    assertEquals(favorite1.getObjectId(), favoritesList.get(1).getObjectId());
    assertEquals(3, favoriteService.getFavoriteItemsSize(userIdentityId));
    assertEquals(2, favoriteService.getFavoriteItemsSize(objectType, userIdentityId));
    assertEquals(1, favoriteService.getFavoriteItemsSize(otherObjectType, userIdentityId));

    favoritesList = favoriteService.getFavoriteItemsByCreatorAndType("test",
                                                                     userIdentityId,
                                                                     0,
                                                                     3);
    assertEquals(0, favoritesList.size());
  }

  public void testSetFavoriteAsLastAccessed() throws Exception {
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    String objectType = "space";

    Favorite favorite1 = new Favorite(objectType, "objectId1", null, userIdentityId);
    favoriteService.createFavorite(favorite1);
    Thread.sleep(10); // NOSONAR
    Favorite favorite2 = new Favorite(objectType, "objectId2", null, userIdentityId);
    favoriteService.createFavorite(favorite2);
    Thread.sleep(10); // NOSONAR
    Favorite favorite3 = new Favorite(objectType, "objectId3", null, userIdentityId);
    favoriteService.createFavorite(favorite3);
    Thread.sleep(10); // NOSONAR
    Favorite favorite4 = new Favorite(objectType, "objectId4", null, userIdentityId);
    favoriteService.createFavorite(favorite4);

    List<MetadataItem> favoritesList = favoriteService.getFavoriteItemsByCreatorAndType(objectType,
                                                                                        userIdentityId,
                                                                                        0,
                                                                                        10);
    assertEquals(4, favoritesList.size());
    assertEquals(favorite4.getObjectId(), favoritesList.get(0).getObjectId());
    assertEquals(favorite3.getObjectId(), favoritesList.get(1).getObjectId());
    assertEquals(favorite2.getObjectId(), favoritesList.get(2).getObjectId());
    assertEquals(favorite1.getObjectId(), favoritesList.get(3).getObjectId());

    favoriteService.setFavoriteAsLastAccessed(favorite4.getObjectType(), favorite4.getObjectId(), userIdentityId);
    Thread.sleep(10); // NOSONAR
    favoriteService.setFavoriteAsLastAccessed(favorite3.getObjectType(), favorite3.getObjectId(), userIdentityId);
    Thread.sleep(10); // NOSONAR
    favoriteService.setFavoriteAsLastAccessed(favorite2.getObjectType(), favorite2.getObjectId(), userIdentityId);
    Thread.sleep(10); // NOSONAR
    favoriteService.setFavoriteAsLastAccessed(favorite1.getObjectType(), favorite1.getObjectId(), userIdentityId);

    favoritesList = favoriteService.getFavoriteItemsByCreatorAndType(objectType,
                                                                     userIdentityId,
                                                                     0,
                                                                     10);
    assertEquals(4, favoritesList.size());
    assertEquals(favorite4.getObjectId(), favoritesList.get(3).getObjectId());
    assertEquals(favorite3.getObjectId(), favoritesList.get(2).getObjectId());
    assertEquals(favorite2.getObjectId(), favoritesList.get(1).getObjectId());
    assertEquals(favorite1.getObjectId(), favoritesList.get(0).getObjectId());
  }

  public void testDeleteFavorite() throws Exception {
    String objectType = "type";
    String objectId = "1";
    String parentObjectId = "2";
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    long spaceId = 500l;
    Favorite favorite = new Favorite(objectType, objectId, parentObjectId, userIdentityId, spaceId);

    List<MetadataItem> metadataItemsByObject = metadataService.getMetadataItemsByObject(favorite.getObject());
    assertTrue(CollectionUtils.isEmpty(metadataItemsByObject));

    favoriteService.createFavorite(favorite);

    metadataItemsByObject = metadataService.getMetadataItemsByObject(favorite.getObject());
    assertEquals(1, metadataItemsByObject.size());

    favoriteService.deleteFavorite(favorite);

    metadataItemsByObject = metadataService.getMetadataItemsByObject(favorite.getObject());
    assertEquals(0, metadataItemsByObject.size());

    try {
      favoriteService.deleteFavorite(favorite);
      fail();
    } catch (ObjectNotFoundException e) {
      // Expected
    }
  }

  private MetadataItem createNewMetadataItem(String type,
                                             String name,
                                             String objectType,
                                             String objectId,
                                             String parentObjectId,
                                             long creatorId,
                                             long audienceId) throws Exception {
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

}
