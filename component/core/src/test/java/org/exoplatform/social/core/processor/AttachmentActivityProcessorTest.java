/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.file.model.FileInfo;
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
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

public class AttachmentActivityProcessorTest extends AbstractCoreTest {

  private static final Log        LOG                = ExoLogger.getLogger(AttachmentActivityProcessorTest.class);

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

  @Override
  public void setUp() throws Exception {
    try {
      super.setUp();
    } catch (Exception e) {
      LOG.error("Error initializing parent Test class", e);
    }
    InitParams initParams = newParam(58467, METADATA_TYPE_NAME);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    if (metadataService.getMetadataTypeByName(METADATA_TYPE_NAME) == null) {
      MetadataTypePlugin userMetadataTypePlugin = new MetadataTypePlugin(initParams) {
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

    super.tearDown();
  }

  public void testGetActivityWithAttachmentsMetadataObject() throws Exception {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String userId = johnIdentity.getId();
    activity.setTitle("activity title");
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);

    long creatorId = Long.parseLong(johnIdentity.getId());

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(FILE_ID), AUDIENCE_ID);
    MetadataObject metadataObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                       activity.getId(),
                                                       PARENT_OBJECT_ID);
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertEquals(1, metadataItems.size());

    activity = activityManager.getActivity(activity.getId());

    assertNotNull(activity.getMetadatas());
    assertEquals(1, activity.getMetadatas().size());
    assertNotNull(activity.getMetadatas().get(METADATA_TYPE_NAME));

    FileInfo fileInfo = mock(FileInfo.class);

    Map<String, String> properties = new HashMap<>();

    Metadata metadata = metadataItem.getMetadata();
    metadata.setProperties(properties);

    metadataItem.setProperties(properties);

    String fileName = "attchmentFileName";
    Long fileSize = 20894l;
    String fileMimeType = "image/png";
    Date today = new Date();

    when(fileInfo.getName()).thenReturn(fileName);
    when(fileInfo.getSize()).thenReturn(fileSize);
    when(fileInfo.getMimetype()).thenReturn(fileMimeType);
    when(fileInfo.getUpdatedDate()).thenReturn(today);

    properties.put("fileName", fileName);
    properties.put("fileSize", String.valueOf(fileSize));
    properties.put("fileMimeType", fileMimeType);
    properties.put("fileUpdateDate", String.valueOf(today.getTime()));

    assertNotNull(activity.getMetadatas().get(METADATA_TYPE_NAME).get(0).getProperties());
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
