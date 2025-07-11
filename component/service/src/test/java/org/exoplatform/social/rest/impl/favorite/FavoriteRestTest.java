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
package org.exoplatform.social.rest.impl.favorite;

import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.metadata.storage.MetadataStorage;
import org.exoplatform.social.core.plugin.ActivityFavoriteACLPlugin;
import org.exoplatform.social.core.plugin.SpaceFavoriteACLPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.FavoriteACLPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class FavoriteRestTest extends AbstractResourceTest {

  private Identity        johnIdentity;

  private Identity        maryIdentity;

  private MetadataService metadataService;

  private FavoriteService favoriteService;

  private SpaceService    spaceService;

  private ActivityManager activityManager;

  private MetadataDAO     metadataDAO;

  private MetadataType    favoriteMetadataType;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    favoriteService = getContainer().getComponentInstanceOfType(FavoriteService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    favoriteMetadataType = new MetadataType(1, "favorites");
    getContainer().getComponentInstanceOfType(MetadataStorage.class).clearCaches();

    try {
      InitParams params = new InitParams();
      ObjectParameter parameter = new ObjectParameter();
      parameter.setName("metadataType");
      parameter.setObject(FavoriteService.METADATA_TYPE);
      params.addParameter(parameter);
      metadataService.addMetadataTypePlugin(new MetadataTypePlugin(params));
      FavoriteACLPlugin favoriteACLPlugin = new SpaceFavoriteACLPlugin(spaceService, identityManager);
      FavoriteACLPlugin favoriteACLPlugin1 = new ActivityFavoriteACLPlugin(activityManager);
      favoriteService.addFavoriteACLPlugin(favoriteACLPlugin);
      favoriteService.addFavoriteACLPlugin(favoriteACLPlugin1);
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    registry(new FavoriteRest(favoriteService));

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();
    identityManager.hardDeleteIdentity(johnIdentity);
    metadataDAO.deleteAll();

    super.tearDown();
  }

  public void testCreateFavorites() throws Exception {
    String objectType = "objectType";
    String objectId = "objectId";
    String parentObjectId = "parentObjectId";
    long userIdentityId = Long.parseLong(johnIdentity.getId());

    startSessionAs(johnIdentity.getRemoteId());
    ContainerResponse response = getResponse("POST",
                                             getURLResource("favorites/" + objectType + "/" + objectId) + "?parentObjectId="
                                                 + parentObjectId,
                                             null);
    assertEquals(204, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/" + objectType + "/" + objectId),
                           null);
    assertEquals(409, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/" + objectType + "/" + objectId)
                               + "?ignoreWhenExisting=true",
                           null);
    assertEquals(204, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/" + objectType + "/" + objectId)
                               + "?parentObjectId=" + parentObjectId + "&ignoreWhenExisting=true",
                           null);
    assertEquals(204, response.getStatus());

    List<MetadataItem> metadataItemsByObject = metadataService.getMetadataItemsByObject(new MetadataObject(objectType,
                                                                                                              objectId));
    assertEquals(1, metadataItemsByObject.size());
    MetadataItem metadataItem = metadataItemsByObject.get(0);
    assertEquals(userIdentityId, metadataItem.getCreatorId());
    assertEquals(objectId, metadataItem.getObjectId());
    assertEquals(objectType, metadataItem.getObjectType());
    assertEquals(parentObjectId, metadataItem.getParentObjectId());
  }

  public void testCreateFavoritesACL() throws Exception {

    Space space1 = new Space();
    space1.setDisplayName("space1");
    space1.setRegistration("validation");
    space1.setVisibility("public");
    space1 = spaceService.createSpace(space1, johnIdentity.getRemoteId());

    Space createdSpace = spaceService.getSpaceByPrettyName(space1.getPrettyName());

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setUserId(johnIdentity.getId());
    activity.setTitle("activity 1");
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space1.getPrettyName());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);

    startSessionAs(maryIdentity.getRemoteId());
    ContainerResponse response = getResponse("POST",
                                             getURLResource("favorites/space/" + createdSpace.getId())
                                                 + "?parentObjectId=parentObjectId",
                                             null);
    assertEquals(401, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/activity/" + activity.getId()) + "?parentObjectId=parentObjectId",
                           null);
    assertEquals(401, response.getStatus());

    spaceService.addMember(space1, maryIdentity.getRemoteId());

    response = getResponse("POST",
                           getURLResource("favorites/space/" + createdSpace.getId()) + "?parentObjectId=parentObjectId",
                           null);
    assertEquals(204, response.getStatus());

    response = getResponse("POST",
                           getURLResource("favorites/activity/" + activity.getId()) + "?parentObjectId=parentObjectId",
                           null);
    assertEquals(204, response.getStatus());
  }

  public void testDeleteFavorite() throws Exception {
    String objectType = "objectType";
    String objectId = "objectId";

    startSessionAs(johnIdentity.getRemoteId());
    ContainerResponse response = getResponse("POST",
                                             getURLResource("favorites/" + objectType + "/" + objectId),
                                             null);
    assertEquals(204, response.getStatus());

    startSessionAs(johnIdentity.getRemoteId());
    response = getResponse("DELETE",
                           getURLResource("favorites/" + objectType + "/" + objectId),
                           null);
    assertEquals(204, response.getStatus());

    response = getResponse("DELETE",
                           getURLResource("favorites/user/name"),
                           null);
    assertEquals(404, response.getStatus());

    response = getResponse("DELETE",
                           getURLResource("favorites/user/name") + "?ignoreNotExisting=true",
                           null);
    assertEquals(204, response.getStatus());
  }

  public void testGetFavoriteItemsByCreator() throws Exception {
    startSessionAs(johnIdentity.getRemoteId());

    long userIdentityId = Long.parseLong(johnIdentity.getId());
    long audienceId = userIdentityId;
    String favoriteType = favoriteMetadataType.getName();

    String objectType = "objectType1";
    createNewMetadataItem(favoriteType, "testMetadata1", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);
    createNewMetadataItem(favoriteType, "testMetadata3", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);
    createNewMetadataItem(favoriteType, "testMetadata5", objectType, "objectId1", "parentObjectId1", userIdentityId, audienceId);

    ContainerResponse response = getResponse("GET",
                                             getURLResource("favorites?offset=0&limit=5&returnSize=true"),
                                             null);
    assertEquals(200, response.getStatus());
    FavoriteEntity favoriteEntity = (FavoriteEntity) response.getEntity();
    List<MetadataItem> favoritesList = favoriteEntity.getFavoritesItem();

    assertEquals(3, favoritesList.size());
    assertEquals(3, favoriteEntity.getSize().intValue());
    assertEquals(3, favoriteService.getFavoriteItemsSize(userIdentityId));
    assertEquals(3, favoriteService.getFavoriteItemsSize(objectType, userIdentityId));
  }

  public void testGetFavoriteItemsByCreatorAndType() throws Exception {
    startSessionAs(johnIdentity.getRemoteId());

    long userIdentityId = Long.parseLong(johnIdentity.getId());
    String objectType = "space";
    String otherObjectType = "activity";

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

    ContainerResponse response = getResponse("GET",
                                             getURLResource("favorites?offset=0&limit=5&returnSize=true&type=" + objectType),
                                             null);
    assertEquals(200, response.getStatus());
    FavoriteEntity favoriteEntity = (FavoriteEntity) response.getEntity();
    List<MetadataItem> favoritesList = favoriteEntity.getFavoritesItem();

    assertEquals(2, favoritesList.size());
    assertEquals(2, favoriteEntity.getSize().intValue());
    assertEquals(favorite2.getObjectId(), favoritesList.get(0).getObjectId());
    assertEquals(favorite1.getObjectId(), favoritesList.get(1).getObjectId());

    response = getResponse("GET",
                           getURLResource("favorites?offset=0&limit=5&returnSize=true&type=test"),
                           null);
    favoriteEntity = (FavoriteEntity) response.getEntity();
    favoritesList = favoriteEntity.getFavoritesItem();

    assertEquals(0, favoritesList.size());
    assertEquals(0, favoriteEntity.getSize().intValue());
  }

  public void testSetFavoriteAsLastAccessed() throws Exception {
    startSessionAs(johnIdentity.getRemoteId());

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

    ContainerResponse response = getResponse("GET",
                                             getURLResource("favorites?offset=0&limit=5&returnSize=true&type=" + objectType),
                                             null);
    assertEquals(200, response.getStatus());
    FavoriteEntity favoriteEntity = (FavoriteEntity) response.getEntity();
    List<MetadataItem> favoritesList = favoriteEntity.getFavoritesItem();

    assertEquals(4, favoritesList.size());
    assertEquals(favorite4.getObjectId(), favoritesList.get(0).getObjectId());
    assertEquals(favorite3.getObjectId(), favoritesList.get(1).getObjectId());
    assertEquals(favorite2.getObjectId(), favoritesList.get(2).getObjectId());
    assertEquals(favorite1.getObjectId(), favoritesList.get(3).getObjectId());

    response = getResponse("PATCH",
                           getURLResource("favorites/" + objectType + "/" + favorite4.getObjectId() + "/view"),
                           null);
    assertEquals(204, response.getStatus());
    Thread.sleep(10); // NOSONAR
    response = getResponse("PATCH",
                           getURLResource("favorites/" + objectType + "/" + favorite3.getObjectId() + "/view"),
                           null);
    assertEquals(204, response.getStatus());
    Thread.sleep(10); // NOSONAR
    response = getResponse("PATCH",
                           getURLResource("favorites/" + objectType + "/" + favorite2.getObjectId() + "/view"),
                           null);
    assertEquals(204, response.getStatus());
    Thread.sleep(10); // NOSONAR
    response = getResponse("PATCH",
                           getURLResource("favorites/" + objectType + "/" + favorite1.getObjectId() + "/view"),
                           null);
    assertEquals(204, response.getStatus());
    Thread.sleep(10); // NOSONAR
    response = getResponse("GET",
                           getURLResource("favorites?offset=0&limit=5&returnSize=true&type=" + objectType),
                           null);
    favoriteEntity = (FavoriteEntity) response.getEntity();
    favoritesList = favoriteEntity.getFavoritesItem();

    assertEquals(4, favoritesList.size());
    assertEquals(favorite4.getObjectId(), favoritesList.get(3).getObjectId());
    assertEquals(favorite3.getObjectId(), favoritesList.get(2).getObjectId());
    assertEquals(favorite2.getObjectId(), favoritesList.get(1).getObjectId());
    assertEquals(favorite1.getObjectId(), favoritesList.get(0).getObjectId());
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

}
