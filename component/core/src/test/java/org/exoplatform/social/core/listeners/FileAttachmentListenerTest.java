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
package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileAttachmentListenerTest extends AbstractCoreTest {

  private static final Log        LOG                = ExoLogger.getLogger(FileAttachmentListenerTest.class);

  private static final String     METADATA_TYPE_NAME = "attachments";

  private static final Long       FILE_ID            = 12L;

  private static final Long       AUDIENCE_ID        = 5L;

  private static final String     PARENT_OBJECT_ID   = "7";

  private Identity                johnIdentity;

  private ActivityManager         activityManager;

  private IdentityManager         identityManager;

  private MetadataService         metadataService;

  private MetadataDAO             metadataDAO;

  private FileService             fileService;

  private List<ExoSocialActivity> tearDownActivityList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    fileService = getContainer().getComponentInstanceOfType(FileService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    if (metadataService.getMetadataTypeByName(METADATA_TYPE_NAME) == null) {
      MetadataTypePlugin userMetadataTypePlugin = new MetadataTypePlugin(newParam(2654, METADATA_TYPE_NAME)) {
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
      activityManager.deleteActivity(activity.getId());
    }
    super.tearDown();
  }

  public void testDeleteActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    long creatorId = Long.parseLong(johnIdentity.getId());
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(FILE_ID), AUDIENCE_ID);
    MetadataObject metadataObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                       activity.getId(),
                                                       PARENT_OBJECT_ID);
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(1, metadataItems.size());

    FileItem fileItem = new FileItem(FILE_ID,
                                     "test",
                                     "image/png",
                                     "attachment",
                                     "test".getBytes().length,
                                     new Date(),
                                     "user",
                                     false,
                                     new ByteArrayInputStream("test".getBytes()));

    fileItem = fileService.writeFile(fileItem);

    activityManager.deleteActivity(activity);

    restartTransaction();

    assertNull(fileService.getFile(fileItem.getFileInfo().getId()));
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
