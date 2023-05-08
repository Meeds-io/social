package org.exoplatform.social.metadata.attachments;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.attachment.model.ObjectUploadResourceList;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.upload.UploadService;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AttachmentsServiceTest extends AbstractCoreTest {

  private Identity          johnIdentity;

  private AttachmentService attachmentService;

  private IdentityManager   identityManager;

  private MetadataService   metadataService;

  private MockUploadService uploadService;

  private FileService       fileService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    attachmentService = getContainer().getComponentInstanceOfType(AttachmentService.class);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);
    fileService = getContainer().getComponentInstanceOfType(FileService.class);
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testCreateAttachments() throws Exception {
    List<String> uploadIds = Arrays.asList("testFile1", "testFile2");
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    String objectType = "activity";
    String objectId = "10";
    String parentObjectId = "5";
    String spaceId = "1";

    File file1 = new File(getClass().getClassLoader().getResource("testFile1").getFile());
    File file2 = new File(getClass().getClassLoader().getResource("testFile2").getFile());

    byte[] contentFile1 = Files.readAllBytes(file1.toPath());
    byte[] contentFile2 = Files.readAllBytes(file2.toPath());
    FileItem fileItem1 = new FileItem(null,
                                      "testFile1",
                                      "image/jpg",
                                      "attachment",
                                      contentFile1.length,
                                      new Date(),
                                      johnIdentity.getRemoteId(),
                                      false,
                                      new ByteArrayInputStream(contentFile1));

    fileItem1 = fileService.writeFile(fileItem1);

    FileItem fileItem2 = new FileItem(null,
                                      "testFile2",
                                      "image/png",
                                      "attachment",
                                      contentFile1.length,
                                      new Date(),
                                      johnIdentity.getRemoteId(),
                                      false,
                                      new ByteArrayInputStream(contentFile2));

    fileItem2 = fileService.writeFile(fileItem2);

    MetadataObject metadataObject = new MetadataObject(objectType, objectId, parentObjectId, Long.parseLong(spaceId));

    List<MetadataItem> attachedFilesMetadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("attachments",
                                                                                                            metadataObject);

    assertEquals(0, attachedFilesMetadataItems.size());

    MetadataKey metadataKey = new MetadataKey("attachments", String.valueOf(fileItem1.getFileInfo().getId()), userIdentityId);
    MetadataKey metadataKey1 = new MetadataKey("attachments", String.valueOf(fileItem2.getFileInfo().getId()), userIdentityId);

    ObjectUploadResourceList attachmentList = new ObjectUploadResourceList(uploadIds,
                                                                           userIdentityId,
                                                                           objectType,
                                                                           objectId,
                                                                           parentObjectId);
    org.exoplatform.services.security.Identity ownerIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    attachmentService.createAttachments(attachmentList, ownerIdentity);

    metadataService.createMetadataItem(metadataObject, metadataKey, userIdentityId);
    metadataService.createMetadataItem(metadataObject, metadataKey1, userIdentityId);

    attachedFilesMetadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("attachments", metadataObject);
    assertEquals(2, attachedFilesMetadataItems.size());
  }

}
