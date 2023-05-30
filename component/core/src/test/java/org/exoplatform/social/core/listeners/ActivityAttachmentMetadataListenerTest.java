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
package org.exoplatform.social.core.listeners;

import org.apache.commons.collections.CollectionUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

public class ActivityAttachmentMetadataListenerTest extends AbstractCoreTest {

  private static final Log        LOG                = ExoLogger.getLogger(ActivityAttachmentMetadataListenerTest.class);

  private static final String     METADATA_TYPE_NAME = "attachments";

  private static final Long       FILE_ID            = 12l;

  private static final Long       AUDIENCE_ID        = 5l;

  private static final String     PARENT_OBJECT_ID   = "7";

  private Identity                johnIdentity;

  private ActivityManager         activityManager;

  private IdentityManager         identityManager;

  private MetadataService         metadataService;

  private MetadataDAO             metadataDAO;

  private List<ExoSocialActivity> tearDownActivityList;

  private List<Space>             tearDownSpaceList;

  @Override
  public void setUp() throws Exception {
    try {
      super.setUp();
    } catch (Exception e) {
      LOG.error("Error initializing parent Test class", e);
    }
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    if (metadataService.getMetadataTypeByName(METADATA_TYPE_NAME) == null) {
      MetadataTypePlugin userMetadataTypePlugin = new MetadataTypePlugin(newParam(2658, METADATA_TYPE_NAME)) {
        @Override
        public boolean isAllowMultipleItemsPerObject() {
          return true;
        }

        @Override
        public boolean isShareable() {
          return true;
        }
      };
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
    }

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    tearDownActivityList = new ArrayList<>();
    tearDownSpaceList = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(johnIdentity);
    metadataDAO.deleteAll();

    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }

    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

    super.tearDown();
  }

  public void testDeleteActivity() throws ObjectAlreadyExistsException{
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(FILE_ID), AUDIENCE_ID);
    MetadataObject metadataObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                       activity.getId(),
                                                       PARENT_OBJECT_ID);
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<Metadata> metadatas = metadataService.getMetadatas(METADATA_TYPE_NAME, -1);
    assertNotNull(metadatas);
    assertEquals(1, metadatas.size());

    activityManager.deleteActivity(activity);

    restartTransaction();

    metadatas = metadataService.getMetadatas(METADATA_TYPE_NAME, -1);
    assertNotNull(metadatas);
    assertEquals(0, metadatas.size());
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
