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

import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.metadata.attachment.AttachmentServiceImpl;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.AttachmentPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.attachment.model.FileAttachmentResourceList;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.metadata.thumbnail.ImageThumbnailService;
import org.exoplatform.upload.UploadService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.*;

public class FileAttachmentListenerTest extends AbstractCoreTest {

  private static final String                        METADATA_TYPE_NAME = "attachments";

  private static final Long                          AUDIENCE_ID        = 5L;

  private static final String                        PARENT_OBJECT_ID   = "7";

  private static final String                        OBJECT_ID          = "10";

  private static final long                          SPACE_ID           = 7l;

  private Identity                                   demoIdentity;

  private org.exoplatform.services.security.Identity userAclIdentity;

  private ActivityManager                            activityManager;

  private IdentityManager                            identityManager;

  private MetadataService                            metadataService;

  private FileService                                fileService;

  private ImageThumbnailService                      imageThumbnailService;

  private MockUploadService                          uploadService;

  private AttachmentService                          attachmentService;

  private ListenerService                            listenerService;

  private List<ExoSocialActivity>                    tearDownActivityList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    InitParams initParams = newParam(7l, METADATA_TYPE_NAME);

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    fileService = getContainer().getComponentInstanceOfType(FileService.class);
    uploadService = (MockUploadService) ExoContainerContext.getService(UploadService.class);
    listenerService = getContainer().getComponentInstanceOfType(ListenerService.class);

    File tempFile = File.createTempFile("image", "temp");
    uploadService.createUploadResource("1234", tempFile.getPath(), "cover.png", "image/png");

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

      demoIdentity = identityManager.getOrCreateUserIdentity("demo");
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
    }

    attachmentService = new AttachmentServiceImpl(metadataService,
                                                  identityManager,
                                                  fileService,
                                                  imageThumbnailService,
                                                  uploadService,
                                                  listenerService);

    userAclIdentity =
                    new org.exoplatform.services.security.Identity("demo",
                                                                   Collections.singleton(new MembershipEntry("/platform/users")));

    tearDownActivityList = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }
    super.tearDown();
  }

  public void testDeleteActivity() throws Exception {

    String activityTitle = "activity title";
    String userId = demoIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activity.setPosterId(userId);
    activityManager.saveActivityNoReturn(demoIdentity, activity);

    long creatorId = Long.parseLong(demoIdentity.getId());

    List<String> uploadIds = Collections.singletonList("1234");
    List<String> fileIds = Collections.singletonList("12");

    FileAttachmentResourceList attachmentsList = new FileAttachmentResourceList(uploadIds,
                                                                                fileIds,
                                                                                creatorId,
                                                                                "activity",
                                                                                activity.getId(),
                                                                                PARENT_OBJECT_ID);

    AttachmentPlugin attachmentPlugin = mock(AttachmentPlugin.class);

    when(attachmentPlugin.getAudienceId(OBJECT_ID)).thenReturn(AUDIENCE_ID);
    when(attachmentPlugin.getSpaceId(OBJECT_ID)).thenReturn(SPACE_ID);
    when(attachmentPlugin.getObjectType()).thenReturn("activity");

    attachmentService.addPlugin(attachmentPlugin);

    when(attachmentPlugin.hasEditPermission(userAclIdentity, activity.getId())).thenReturn(true);
    when(attachmentPlugin.hasAccessPermission(userAclIdentity, activity.getId())).thenReturn(true);

    attachmentService.createAttachments(attachmentsList, userAclIdentity);

    ObjectAttachmentList attachmentList = attachmentService.getAttachments("activity", activity.getId(), userAclIdentity);
    assertFalse(attachmentList.getAttachments().isEmpty());

    long attachFileId = Long.parseLong(attachmentList.getAttachments().get(0).getId());

    assertNotNull(fileService.getFile(attachFileId));

    activityManager.deleteActivity(activity);
    restartTransaction();

    attachmentList = attachmentService.getAttachments("activity", activity.getId(), userAclIdentity);
    assertTrue(attachmentList.getAttachments().isEmpty());

    assertTrue(fileService.getFile(attachFileId).getFileInfo().isDeleted());
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
