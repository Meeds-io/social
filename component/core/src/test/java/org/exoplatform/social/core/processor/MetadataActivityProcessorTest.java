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
package org.exoplatform.social.core.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MapUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

public class MetadataActivityProcessorTest extends AbstractCoreTest {

  private static final Log        LOG                = ExoLogger.getLogger(MetadataActivityProcessorTest.class);

  private static final String     METADATA_TYPE_NAME = "testMetadataActivityProcessor";

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private ActivityManager         activityManager;

  private IdentityManager         identityManager;

  private MetadataService         metadataService;

  private MetadataDAO             metadataDAO;

  private List<ExoSocialActivity> tearDownActivityList;

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
      MetadataTypePlugin userMetadataTypePlugin = new MetadataTypePlugin(newParam(58466, METADATA_TYPE_NAME)) {
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

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    tearDownActivityList = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    metadataDAO.deleteAll();

    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }

    super.tearDown();
  }

  public void testSaveActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);

    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String metadataName = "testMetadata2";
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME,
                                              metadataName,
                                              audienceId);
    MetadataObject metadataObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                       activity.getId(),
                                                       null);
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(1, metadataItems.size());

    activity = activityManager.getActivity(activity.getId());
    assertNotNull(activity.getMetadatas());
    assertEquals(1, activity.getMetadatas().size());
    assertNotNull(activity.getMetadatas().get(METADATA_TYPE_NAME));
    assertEquals(metadataItems, activity.getMetadatas().get(METADATA_TYPE_NAME));
  }

  public void testSaveActivityWithSpecificMetadataObject() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);

    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    String metadataName = "testMetadata2";
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME,
                                              metadataName,
                                              audienceId);

    String specificMetadataObjectType = "CUSTOME_METADATA_OBJECT_TYPE";
    String specificMetadataObjectId = "CUSTOME_METADATA_OBJECT_ID";

    MetadataObject metadataObject = new MetadataObject(specificMetadataObjectType,
                                                       specificMetadataObjectId,
                                                       null);
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(1, metadataItems.size());

    activity = activityManager.getActivity(activity.getId());
    assertTrue(MapUtils.isEmpty(activity.getMetadatas()));

    activity.setMetadataObjectType(specificMetadataObjectType);
    activity.setMetadataObjectId(specificMetadataObjectId);
    activityManager.saveActivityNoReturn(activity);

    activity = activityManager.getActivity(activity.getId());
    assertNotNull(activity.getMetadatas());
    assertEquals(1, activity.getMetadatas().size());
    assertNotNull(activity.getMetadatas().get(METADATA_TYPE_NAME));
    assertEquals(metadataItems, activity.getMetadatas().get(METADATA_TYPE_NAME));
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
