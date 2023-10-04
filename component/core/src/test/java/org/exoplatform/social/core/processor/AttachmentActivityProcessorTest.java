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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.FileAttachmentObject;
import org.exoplatform.social.attachment.model.FileAttachmentResourceList;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.core.plugin.ActivityAttachmentPlugin;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.upload.UploadService;

public class AttachmentActivityProcessorTest extends AbstractCoreTest {

  private static final String     MIME_TYPE = "image/png";

  private static final String     FILE_NAME = "cover.png";

  private static final String     UPLOAD_ID = "1234";

  private Identity                demoIdentity;

  private MockUploadService       uploadService;

  private ActivityManager         activityManager;

  private AttachmentService       attachmentService;

  private List<ExoSocialActivity> tearDownActivityList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    attachmentService = getContainer().getComponentInstanceOfType(AttachmentService.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);
    demoIdentity = identityManager.getOrCreateUserIdentity("demo");
    tearDownActivityList = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }

    super.tearDown();
  }

  public void testGetActivityWithAttachmentsMetadataObject() throws Exception {
    String username = demoIdentity.getRemoteId();

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    // test for reserving order of map values for i18n activity
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("key1", "value 1");
    templateParams.put("key2", "value 2");
    templateParams.put("key3", "value 3");
    activity.setTemplateParams(templateParams);
    activity.setTitle("");
    activity.setUserId(demoIdentity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, activity);
    tearDownActivityList.add(activity);

    String fileId = createAttachment(username, activity.getId());

    ObjectAttachmentList objectAttachmentList =
                                              attachmentService.getAttachments(ActivityAttachmentPlugin.ACTIVITY_ATTACHMENT_TYPE,
                                                                               activity.getId());
    assertNotNull(objectAttachmentList);

    List<ObjectAttachmentDetail> attachments = objectAttachmentList.getAttachments();
    assertEquals(1, attachments.size());
    ObjectAttachmentDetail attachmentDetail = attachments.get(0);
    assertNotNull(attachmentDetail);
    assertEquals(FILE_NAME, attachmentDetail.getName());
    assertEquals(MIME_TYPE, attachmentDetail.getMimetype());
    assertEquals(fileId, attachmentDetail.getId());
    assertTrue(attachmentDetail.getUpdated() > 0);
    assertEquals(demoIdentity.getId(), attachmentDetail.getUpdater());
  }

  private String createAttachment(String username, String activityId) throws IOException, Exception {
    String identityId = identityManager.getOrCreateUserIdentity(username).getId();
    FileAttachmentResourceList attachmentList = new FileAttachmentResourceList();
    attachmentList.setAttachedFiles(null);
    attachmentList.setUserIdentityId(Long.parseLong(identityId));
    attachmentList.setObjectType(ActivityAttachmentPlugin.ACTIVITY_ATTACHMENT_TYPE);
    attachmentList.setObjectId(activityId);
    FileAttachmentObject fileAttachmentObject = new FileAttachmentObject();
    fileAttachmentObject.setUploadId(UPLOAD_ID);
    fileAttachmentObject.setAltText("Test alternative text");
    attachmentList.setUploadedFiles(Collections.singletonList(fileAttachmentObject));
    uploadResource();

    attachmentService.saveAttachments(attachmentList);
    ObjectAttachmentList objectAttachmentList =
                                              attachmentService.getAttachments(ActivityAttachmentPlugin.ACTIVITY_ATTACHMENT_TYPE,
                                                                               activityId);
    return objectAttachmentList.getAttachments().get(0).getId();
  }

  private void uploadResource() throws IOException, Exception {
    File tempFile = File.createTempFile("image", "temp");
    uploadService.createUploadResource(UPLOAD_ID, tempFile.getPath(), FILE_NAME, MIME_TYPE);
  }

}
