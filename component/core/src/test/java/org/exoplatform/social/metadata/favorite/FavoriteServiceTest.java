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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataServiceTest;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;

public class FavoriteServiceTest extends AbstractCoreTest {

  private Identity        johnIdentity;

  private IdentityManager identityManager;

  private FavoriteService favoriteService;

  private MetadataService metadataService;

  private MetadataType    favoriteMetadataType;

  private MetadataType    userMetadataType;

  private MetadataDAO     metadataDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    favoriteService = getContainer().getComponentInstanceOfType(FavoriteService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);

    favoriteMetadataType = new MetadataType(1, "favorites");
    userMetadataType = new MetadataType(2, "user");

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(johnIdentity);
    metadataDAO.deleteAll();

    super.tearDown();
  }

  public void testCreateFavorite() throws ObjectAlreadyExistsException {
    String objectType = "type";
    String objectId = "1";
    String parentObjectId = "2";
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    Favorite favorite = new Favorite(objectType, objectId, parentObjectId, userIdentityId);

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

    try {
      favoriteService.createFavorite(favorite);
      fail();
    } catch (ObjectAlreadyExistsException e) {
      // Expected
    }
  }

  public void testGetFavoriteItemsByCreator() throws Exception {

    long userIdentityId = Long.parseLong(johnIdentity.getId());
    long audienceId = userIdentityId;
    String favoriteType = favoriteMetadataType.getName();
    String otherType = userMetadataType.getName();

    createNewMetadataItem(favoriteType,"testMetadata1","objectType1","objectId1","parentObjectId1",userIdentityId,audienceId);
    createNewMetadataItem(otherType,"testMetadata2","objectType1","objectId1","parentObjectId1",userIdentityId,audienceId);
    createNewMetadataItem(favoriteType,"testMetadata3","objectType1","objectId1","parentObjectId1",userIdentityId,audienceId);
    createNewMetadataItem(otherType,"testMetadata4","objectType1","objectId1","parentObjectId1",userIdentityId,audienceId);
    createNewMetadataItem(favoriteType,"testMetadata5","objectType1","objectId1","parentObjectId1",userIdentityId,audienceId);
    createNewMetadataItem(otherType,"testMetadata6","objectType1","objectId1","parentObjectId1",userIdentityId,audienceId);

    List<MetadataItem> favoritesList = metadataService.getMetadataItemsByMetadataTypeAndCreator("favorites", userIdentityId, 3 ,0);

    favoritesList = metadataService.getMetadataItemsByMetadataTypeAndCreator("favorites", userIdentityId, 0, 5);
    assertEquals(3, favoritesList.size());

    favoritesList = metadataService.getMetadataItemsByMetadataTypeAndCreator("favorites", 100, 0, 5);
    assertEquals(0, favoritesList.size());

  }

  public void testDeleteFavorite() throws ObjectAlreadyExistsException, ObjectNotFoundException {
    String objectType = "type";
    String objectId = "1";
    String parentObjectId = "2";
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    Favorite favorite = new Favorite(objectType, objectId, parentObjectId, userIdentityId);

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

}
