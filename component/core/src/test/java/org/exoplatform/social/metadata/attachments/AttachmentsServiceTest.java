package org.exoplatform.social.metadata.attachments;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.metadata.attachment.AttachmentServiceImpl;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.metadata.AttachmentPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.metadata.attachment.model.ObjectUploadResourceList;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.thumbnail.ImageThumbnailService;
import org.exoplatform.upload.UploadResource;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentsServiceTest {

  private static final String   UPLOAD_ID        = "1248TestUploadId";

  private static final long     FILE_ID          = 12l;

  private static final String   USER_NAME        = "testuser";

  private static final String   USER_IDENTITY_ID = "1";

  private static final String   OBJECT_TYPE      = "activity";

  private static final String   OBJECT_ID        = "10";

  private static final String   PARENT_OBJECT_ID = "5";

  private static final long     AUDIENCE_ID      = 5l;

  private static final long     SPACE_ID         = 7l;

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
  private AttachmentPlugin      attachmentPlugin;

  @Before
  public void setUp() throws Exception {
    attachmentService = new AttachmentServiceImpl(metadataService,
                                                  identityManager,
                                                  fileService,
                                                  imageThumbnailService,
                                                  uploadService);
    when(attachmentPlugin.getAudienceId(OBJECT_ID)).thenReturn(AUDIENCE_ID);
    when(attachmentPlugin.getSpaceId(OBJECT_ID)).thenReturn(SPACE_ID);
    when(attachmentPlugin.getObjectType()).thenReturn(OBJECT_TYPE);

    attachmentService.addPlugin(attachmentPlugin);
  }

  @Test
  public void testCreateAttachments() throws Exception {
    List<String> uploadIds = Arrays.asList(UPLOAD_ID);

    Identity userIdentity = Mockito.mock(Identity.class);
    Mockito.when(userIdentity.isEnable()).thenReturn(true);
    Mockito.when(userIdentity.isDeleted()).thenReturn(false);

    org.exoplatform.services.security.Identity userACLIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    when(userACLIdentity.getUserId()).thenReturn(USER_NAME);

    Mockito.when(identityManager.getIdentity(USER_IDENTITY_ID)).thenReturn(userIdentity);
    ObjectUploadResourceList attachmentsList = new ObjectUploadResourceList(uploadIds,
                                                                            1L,
                                                                            OBJECT_TYPE,
                                                                            OBJECT_ID,
                                                                            PARENT_OBJECT_ID);
    MetadataObject metadataObject =
                                  new MetadataObject(OBJECT_TYPE, OBJECT_ID, PARENT_OBJECT_ID, Long.parseLong(USER_IDENTITY_ID));
    List<MetadataItem> attachedFilesMetadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("attachments",
                                                                                                            metadataObject);
    assertEquals(0, attachedFilesMetadataItems.size());

    UploadResource uploadResource = new UploadResource(UPLOAD_ID);
    File file = new File(getClass().getClassLoader().getResource("test.png").getFile());
    assertNotNull(file);
    uploadResource.setFileName("test.png");
    uploadResource.setStoreLocation(file.getPath());
    uploadResource.setMimeType("image/png");
    uploadResource.setEstimatedSize(0);

    when(uploadService.getUploadResource(UPLOAD_ID)).thenReturn(uploadResource);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.createAttachments(null, userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(null,
                                                                                        1L,
                                                                                        OBJECT_TYPE,
                                                                                        OBJECT_ID,
                                                                                        PARENT_OBJECT_ID),
                                                           userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(uploadIds,
                                                                                        -1,
                                                                                        OBJECT_TYPE,
                                                                                        OBJECT_ID,
                                                                                        PARENT_OBJECT_ID),
                                                           userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(uploadIds,
                                                                                        1L,
                                                                                        null,
                                                                                        OBJECT_ID,
                                                                                        PARENT_OBJECT_ID),
                                                           userACLIdentity));

    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> attachmentService.createAttachments(new ObjectUploadResourceList(uploadIds,
                                                                                        1L,
                                                                                        OBJECT_TYPE,
                                                                                        null,
                                                                                        PARENT_OBJECT_ID),
                                                           userACLIdentity));

    assertThrows(IllegalAccessException.class, () -> attachmentService.createAttachments(attachmentsList, userACLIdentity));

    when(fileService.writeFile(any())).thenAnswer(invocation -> {
      FileItem fileItem = invocation.getArgument(0, FileItem.class);
      fileItem.getFileInfo().setId(FILE_ID);
      return fileItem;
    });

    when(attachmentPlugin.hasEditPermission(userACLIdentity, OBJECT_ID)).thenReturn(true);

    ObjectAttachmentOperationReport report = attachmentService.createAttachments(attachmentsList, userACLIdentity);
    assertNotNull(report);
    assertEquals("Attachment error report should be empty, but was: " + report.getErrorByUploadId(),
                 0,
                 report.getErrorByUploadId().size());

    MetadataKey metadataKey = new MetadataKey(AttachmentService.METADATA_TYPE.getName(), String.valueOf(FILE_ID), AUDIENCE_ID);
    MetadataObject object = new MetadataObject(OBJECT_TYPE, OBJECT_ID, PARENT_OBJECT_ID, SPACE_ID);

    verify(metadataService, times(1)).createMetadataItem(object, metadataKey, Long.parseLong(USER_IDENTITY_ID));
  }

  @Test
  public void testGetAttachments() throws ObjectNotFoundException, IllegalAccessException {
    List<String> fileIds = Arrays.asList(String.valueOf(FILE_ID));

    Identity userIdentity = Mockito.mock(Identity.class);
    when(userIdentity.isEnable()).thenReturn(true);
    when(userIdentity.isDeleted()).thenReturn(false);

    org.exoplatform.services.security.Identity userACLIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    when(userACLIdentity.getUserId()).thenReturn(USER_NAME);
    when(identityManager.getOrCreateUserIdentity(USER_NAME)).thenReturn(userIdentity);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(null, null, null));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(null, OBJECT_ID, userACLIdentity));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(OBJECT_TYPE, null, userACLIdentity));

    when(attachmentPlugin.hasAccessPermission(userACLIdentity, OBJECT_ID)).thenReturn(true);

    when(metadataService.getMetadataNamesByMetadataTypeAndObject("attachments", OBJECT_TYPE, OBJECT_ID)).thenReturn(fileIds);

    FileInfo fileInfo = Mockito.mock(FileInfo.class);
    when(fileService.getFileInfo(FILE_ID)).thenReturn(fileInfo);
    when(fileInfo.getUpdatedDate()).thenReturn(new Date());
    ObjectAttachmentList objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, OBJECT_ID, userACLIdentity);
    assertNotNull(objectAttachmentList);
    assertEquals(1, objectAttachmentList.getAttachments().size());
  }

  @Test
  public void testGetAttachment() throws ObjectNotFoundException, IllegalAccessException {
    String fileId = String.valueOf(FILE_ID);
    List<String> fileIds = Arrays.asList(String.valueOf(FILE_ID));
    Identity userIdentity = Mockito.mock(Identity.class);
    when(userIdentity.isEnable()).thenReturn(true);
    when(userIdentity.isDeleted()).thenReturn(false);

    org.exoplatform.services.security.Identity userACLIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    when(userACLIdentity.getUserId()).thenReturn(USER_NAME);
    when(identityManager.getOrCreateUserIdentity(USER_NAME)).thenReturn(userIdentity);

    when(attachmentPlugin.hasAccessPermission(userACLIdentity, OBJECT_ID)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachment(null, null, null, null));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachment(null, OBJECT_ID, fileId, userACLIdentity));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachment(OBJECT_TYPE, null, fileId, userACLIdentity));

    when(metadataService.getMetadataNamesByMetadataTypeAndObject("attachments", OBJECT_TYPE, OBJECT_ID)).thenReturn(fileIds);
    FileInfo fileInfo = Mockito.mock(FileInfo.class);
    when(fileService.getFileInfo(FILE_ID)).thenReturn(fileInfo);
    when(fileInfo.getUpdatedDate()).thenReturn(new Date());

    ObjectAttachmentDetail objectAttachmentDetail = attachmentService.getAttachment(OBJECT_TYPE,
                                                                                    OBJECT_ID,
                                                                                    fileId,
                                                                                    userACLIdentity);
    assertNotNull(objectAttachmentDetail);
  }

  @Test
  public void testGetAttachmentInputStream() throws Exception {
    String imageDimensions = "125x125";
    String fileId = String.valueOf(FILE_ID);
    List<String> fileIds = Arrays.asList(String.valueOf(FILE_ID));
    Identity userIdentity = Mockito.mock(Identity.class);
    when(userIdentity.isEnable()).thenReturn(true);
    when(userIdentity.isDeleted()).thenReturn(false);

    org.exoplatform.services.security.Identity userACLIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    when(userACLIdentity.getUserId()).thenReturn(USER_NAME);
    when(identityManager.getOrCreateUserIdentity(USER_NAME)).thenReturn(userIdentity);

    when(attachmentPlugin.hasAccessPermission(userACLIdentity, OBJECT_ID)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachmentInputStream(null, null, null, null, null));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(null, OBJECT_ID, fileId, imageDimensions, userACLIdentity));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_ID, null, fileId, imageDimensions, userACLIdentity));

    when(metadataService.getMetadataNamesByMetadataTypeAndObject("attachments", OBJECT_TYPE, OBJECT_ID)).thenReturn(fileIds);

    FileInfo fileInfo = Mockito.mock(FileInfo.class);
    when(fileService.getFileInfo(FILE_ID)).thenReturn(fileInfo);

    FileItem fileItem = Mockito.mock(FileItem.class);
    when(fileService.getFile(FILE_ID)).thenReturn(fileItem);
    when(fileInfo.getMimetype()).thenReturn("image/");

    FileItem thumbnailFileItem = Mockito.mock(FileItem.class);
    when(imageThumbnailService.getOrCreateThumbnail(fileItem, userIdentity, 125, 125)).thenReturn(thumbnailFileItem);

    InputStream attachmentInputStream = Mockito.mock(InputStream.class);
    when(thumbnailFileItem.getAsStream()).thenReturn(attachmentInputStream);
    InputStream attachmentInputStream1 = attachmentService.getAttachmentInputStream(OBJECT_TYPE, OBJECT_ID, fileId, imageDimensions, userACLIdentity);
    assertNotNull(attachmentInputStream);
    assertNotNull(attachmentInputStream1);
    assertEquals(attachmentInputStream,attachmentInputStream1);
  }

}
