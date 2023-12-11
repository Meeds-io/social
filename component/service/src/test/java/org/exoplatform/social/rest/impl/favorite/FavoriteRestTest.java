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
import org.exoplatform.social.core.plugin.ActivityFavoriteACLPlugin;
import org.exoplatform.social.core.plugin.SpaceFavoriteACLPlugin;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.FavoriteACLPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.rest.impl.favorite.FavoriteRest;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class FavoriteRestTest extends AbstractResourceTest {

  private Identity        johnIdentity;

  private Identity        maryIdentity;

  private IdentityManager identityManager;

  private FavoriteRest    metadataRest;

  private FavoriteService favoriteService;

  private MetadataService metadataService;
  
  private SpaceService    spaceService;

  private ActivityManager activityManager;

  private MetadataDAO     metadataDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    favoriteService = getContainer().getComponentInstanceOfType(FavoriteService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);

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

    metadataRest = new FavoriteRest(favoriteService);
    registry(metadataRest);

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
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
    space1.setPrettyName(space1.getDisplayName());
    space1.setRegistration("validation");
    space1.setVisibility("public");
    space1.setPriority("2");
    String[] manager = new String[] { "john" };
    String[] members = new String[] { "john" };
    space1.setManagers(manager);
    space1.setMembers(members);

    spaceService.createSpace(space1, johnIdentity.getRemoteId());

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

}
