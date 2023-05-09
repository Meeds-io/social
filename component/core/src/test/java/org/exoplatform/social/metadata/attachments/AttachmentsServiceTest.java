package org.exoplatform.social.metadata.attachments;

import static org.junit.Assert.*;

import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.metadata.attachment.AttachmentServiceImpl;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.core.plugin.ActivityAttachmentPlugin;
import org.exoplatform.social.metadata.AttachmentPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.attachment.model.ObjectUploadResourceList;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.thumbnail.ImageThumbnailService;
import org.exoplatform.upload.UploadResource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentsServiceTest {

  private AttachmentService     attachmentService;

  @Mock
  private IdentityManager       identityManager;

  @Mock
  private MetadataService       metadataService;

  @Mock
  private ImageThumbnailService imageThumbnailService;

  @Mock
  private MockUploadService     uploadService;

  @Mock
  private FileService           fileService;

  @Mock
  private ActivityManager       activityManager;

  @Before
  public void setUp() throws Exception {
    attachmentService = new AttachmentServiceImpl(metadataService,
                                                  identityManager,
                                                  fileService,
                                                  imageThumbnailService,
                                                  uploadService);

    AttachmentPlugin attachmentPlugin = new ActivityAttachmentPlugin(activityManager);
    attachmentService.addPlugin(attachmentPlugin);
  }

  @Test
  public void testCreateAttachments() throws Exception {
    List<String> uploadIds = Arrays.asList("test.png");
    String objectType = "activity";
    String objectId = "10";
    String parentObjectId = "5";

    Identity johnIdentity = Mockito.mock(Identity.class);
    Mockito.when(johnIdentity.isEnable()).thenReturn(true);
    Mockito.when(johnIdentity.isDeleted()).thenReturn(false);

    org.exoplatform.services.security.Identity userACLIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivityImpl.class);
    Mockito.when(activityManager.getActivity(objectId)).thenReturn(activity);

    Mockito.when(activityManager.isActivityEditable(activity, userACLIdentity)).thenReturn(true);

    Mockito.when(identityManager.getIdentity("1")).thenReturn(johnIdentity);
    ObjectUploadResourceList attachmentsList = new ObjectUploadResourceList(uploadIds, 1L, objectType, objectId, parentObjectId);
    MetadataObject metadataObject = new MetadataObject(objectType, objectId, parentObjectId, Long.parseLong("1"));
    List<MetadataItem> attachedFilesMetadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("attachments",
                                                                                                            metadataObject);
    assertEquals(0, attachedFilesMetadataItems.size());

    UploadResource uploadResource = new UploadResource("test.png");
    File file = new File(getClass().getClassLoader().getResource("test.png").getFile());
    assertNotNull(file);
    uploadResource.setFileName("test.png");
    uploadResource.setStoreLocation(file.getPath());
    uploadResource.setMimeType("image/png");
    uploadResource.setEstimatedSize(0);
    when(uploadService.getUploadResource(any())).thenReturn(uploadResource);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.createAttachments(null, userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(null,
                                                                                        1L,
                                                                                        objectType,
                                                                                        objectId,
                                                                                        parentObjectId),
                                                           userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(uploadIds,
                                                                                        -1,
                                                                                        objectType,
                                                                                        objectId,
                                                                                        parentObjectId),
                                                           userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(uploadIds,
                                                                                        1L,
                                                                                        null,
                                                                                        objectId,
                                                                                        parentObjectId),
                                                           userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(uploadIds,
                                                                                        1L,
                                                                                        objectType,
                                                                                        null,
                                                                                        parentObjectId),
                                                           userACLIdentity));

    attachmentService.createAttachments(attachmentsList, userACLIdentity);
    attachedFilesMetadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("attachments", metadataObject);
    assertEquals(1, attachedFilesMetadataItems.size());
  }

}
