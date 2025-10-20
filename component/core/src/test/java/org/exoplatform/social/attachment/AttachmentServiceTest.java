/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.attachment;

import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENTS_DELETED_EVENT;
import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENTS_UPDATED_EVENT;
import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENT_CREATED_EVENT;
import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENT_DELETED_EVENT;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.model.FileAttachmentObject;
import org.exoplatform.social.attachment.model.FileAttachmentResourceList;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.attachment.model.ObjectAttachmentId;
import org.exoplatform.social.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.core.attachment.storage.FileAttachmentStorage;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.upload.UploadService;

public class AttachmentServiceTest extends AbstractCoreTest {

  private static final String   FORMAT               = "landscape";

  private static final String   ALT_TEXT             = "Test alternative text";

  private static final String   USERNAME             = "demo";

  private static final String   MIME_TYPE            = "image/png";

  private static final String   FILE_NAME            = "cover.png";

  private static final String   UPLOAD_ID            = "1234";

  private static final String   OBJECT_TYPE          = "objectType";

  private static final String   PUBLIC_OBJECT_TYPE   = "public";

  private static final String   DEST_OBJECT_TYPE     = "destinationObjectType";

  private static final Random   RANDOM               = new Random();

  private Map<String, Long>     eventCounts          = new HashMap<>();

  private Listener<?, ?>        listener;

  private ListenerService       listenerService;

  private MockUploadService     uploadService;

  private FileAttachmentStorage attachmentStorage;

  private AttachmentService     attachmentService;

  private AtomicBoolean         hasEditPermission    = new AtomicBoolean();

  private AtomicBoolean         hasViewPermission    = new AtomicBoolean();

  private long                  attachmentSpaceId    = 0;

  private long                  attachmentAudienceId = 0;

  private String                objectId;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    attachmentService = getContainer().getComponentInstanceOfType(AttachmentService.class);
    listenerService = getContainer().getComponentInstanceOfType(ListenerService.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);
    attachmentStorage = getContainer().getComponentInstanceOfType(FileAttachmentStorage.class);
    if (listener == null) {
      listener = new Listener<Object, Object>() {
        @Override
        public void onEvent(Event<Object, Object> event) throws Exception {
          String eventName = event.getEventName();
          long count = eventCounts.computeIfAbsent(eventName, key -> 0l);
          eventCounts.put(eventName, count + 1);
        }
      };
      listenerService.addListener(ATTACHMENT_CREATED_EVENT, listener);
      listenerService.addListener(ATTACHMENT_DELETED_EVENT, listener);
      listenerService.addListener(ATTACHMENTS_UPDATED_EVENT, listener);
      listenerService.addListener(ATTACHMENTS_DELETED_EVENT, listener);

      AttachmentPlugin attachmentPlugin = createAttachmentPlugin(OBJECT_TYPE,true);
      AttachmentPlugin publicAttachmentPlugin = createAttachmentPlugin(PUBLIC_OBJECT_TYPE, false);

      attachmentService.addPlugin(attachmentPlugin);
      attachmentService.addPlugin(publicAttachmentPlugin);
    } else {
      eventCounts.clear();
    }
    objectId = "objectId" + RANDOM.nextInt();
  }

  public void testUpdateAttachments() throws Exception { // NOSONAR

    Identity userAcl = startSessionAndRegisterAs(USERNAME);

    String fileId = createAttachment(USERNAME);

    String identityId = identityManager.getOrCreateUserIdentity(USERNAME).getId();
    FileAttachmentResourceList attachmentList = new FileAttachmentResourceList();

    List<FileAttachmentObject> fileAttachmentObjectList = new ArrayList<>();
    FileAttachmentObject fileAttachmentObject = new FileAttachmentObject();
    fileAttachmentObject.setId(fileId);
    fileAttachmentObjectList.add(fileAttachmentObject);
    attachmentList.setAttachedFiles(fileAttachmentObjectList);

    List<FileAttachmentObject> fileUploadedObjectList = new ArrayList<>();
    FileAttachmentObject fileUploadedObject = new FileAttachmentObject();
    attachmentList.setUploadedFiles(new ArrayList<FileAttachmentObject>());

    assertThrows(IllegalArgumentException.class, () -> attachmentService.saveAttachments(null, userAcl));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.saveAttachments(attachmentList, null));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));
    attachmentList.setUserIdentityId(Long.parseLong(identityId));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));
    attachmentList.setObjectType(OBJECT_TYPE);
    assertThrows(IllegalArgumentException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));
    attachmentList.setObjectId(objectId);
    assertThrows(IllegalAccessException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));
    hasViewPermission.set(true);
    assertThrows(IllegalAccessException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));
    hasEditPermission.set(true);
    hasViewPermission.set(false);

    attachmentList.setObjectType(PUBLIC_OBJECT_TYPE);
    assertThrows(IllegalStateException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));

    attachmentList.setObjectType(OBJECT_TYPE);
    assertListenerCount(1l, 0l, 1l, 0l);

    ObjectAttachmentOperationReport report = attachmentService.saveAttachments(attachmentList, userAcl);
    assertNotNull(report);
    assertListenerCount(1l, 0l, 3l, 0l);

    ObjectAttachmentList objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    assertNotNull(objectAttachmentList);

    List<ObjectAttachmentDetail> attachments = objectAttachmentList.getAttachments();
    assertEquals(1, attachments.size());
    ObjectAttachmentDetail attachmentDetail = attachments.get(0);
    assertNotNull(attachmentDetail);
    assertEquals(FILE_NAME, attachmentDetail.getName());
    assertEquals(MIME_TYPE, attachmentDetail.getMimetype());
    assertEquals(fileId, attachmentDetail.getId());
    assertTrue(attachmentDetail.getUpdated() > 0);
    assertEquals(identityId, attachmentDetail.getUpdater());

    fileUploadedObject.setUploadId(UPLOAD_ID);
    fileUploadedObject.setAltText("");
    fileUploadedObject.setFormat("");
    fileUploadedObjectList.add(fileUploadedObject);
    attachmentList.setUploadedFiles(fileUploadedObjectList);
    attachmentList.setAttachedFiles(fileAttachmentObjectList);

    uploadResource();
    report = attachmentService.saveAttachments(attachmentList, userAcl);
    assertNotNull(report);
    assertListenerCount(2l, 0l, 5l, 0l);

    objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    assertNotNull(objectAttachmentList);

    attachments = objectAttachmentList.getAttachments();
    assertEquals(2, attachments.size());
    attachmentDetail = attachments.get(0);
    assertNotNull(attachmentDetail);
    assertEquals(fileId, attachments.getLast().getId());
    assertEquals(FILE_NAME, attachmentDetail.getName());
    assertEquals(MIME_TYPE, attachmentDetail.getMimetype());
    assertNotNull(attachmentDetail.getId());
    assertTrue(attachmentDetail.getUpdated() > 0);
    assertEquals(identityId, attachmentDetail.getUpdater());

    attachmentList.setAttachedFiles(Collections.singletonList(fileAttachmentObject));
    attachmentList.setUploadedFiles(new ArrayList<FileAttachmentObject>());
    report = attachmentService.saveAttachments(attachmentList, userAcl);
    assertNotNull(report);
    assertListenerCount(2l, 1l, 7l, 0l);

    objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    attachments = objectAttachmentList.getAttachments();
    assertEquals(1, attachments.size());
    attachmentDetail = attachments.get(0);
    assertNotNull(attachmentDetail);
    assertEquals(FILE_NAME, attachmentDetail.getName());
    assertEquals(MIME_TYPE, attachmentDetail.getMimetype());
    assertEquals(fileId, attachmentDetail.getId());
    assertTrue(attachmentDetail.getUpdated() > 0);
    assertEquals(identityId, attachmentDetail.getUpdater());

    ObjectAttachmentDetail attachment = attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, objectId));
    assertNotNull(attachment);

    attachmentList.setAttachedFiles(new ArrayList<FileAttachmentObject>());
    attachmentList.setUploadedFiles(new ArrayList<FileAttachmentObject>());
    report = attachmentService.saveAttachments(attachmentList, userAcl);
    assertNull(report);
    assertListenerCount(2l, 2l, 8l, 0l);

    objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    attachments = objectAttachmentList.getAttachments();
    assertEquals(0, attachments.size());

    attachment = attachmentService.getAttachment(OBJECT_TYPE, objectId, fileId);
    assertNull(attachment);
  }

  public void testGetAttachments() throws Exception { // NOSONAR

    Identity userAcl = startSessionAndRegisterAs(USERNAME);

    String fileId = createAttachment(USERNAME);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(null, objectId, userAcl, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(OBJECT_TYPE, null, userAcl, 0, 0));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(OBJECT_TYPE, objectId, null, 0, 0));
    hasEditPermission.set(true);
    assertThrows(IllegalAccessException.class, () -> attachmentService.getAttachments(OBJECT_TYPE, objectId, userAcl, 0, 0));
    hasEditPermission.set(false);
    hasViewPermission.set(true);

    ObjectAttachmentList objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId, userAcl, 0, 10);
    assertNotNull(objectAttachmentList);

    List<ObjectAttachmentDetail> attachments = objectAttachmentList.getAttachments();
    assertEquals(1, attachments.size());
    ObjectAttachmentDetail attachmentDetail = attachments.get(0);
    assertNotNull(attachmentDetail);
    assertEquals(FILE_NAME, attachmentDetail.getName());
    assertEquals(MIME_TYPE, attachmentDetail.getMimetype());
    assertEquals(fileId, attachmentDetail.getId());
    assertTrue(attachmentDetail.getUpdated() > 0);
  }

  public void testGetAttachment() throws Exception { // NOSONAR
    String fileId = createAttachment(USERNAME);
    Identity userAcl = startSessionAndRegisterAs(USERNAME);

    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachment(null, objectId, fileId, userAcl));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachment(OBJECT_TYPE, null, fileId, userAcl));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachment(OBJECT_TYPE, objectId, fileId, null));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachment(OBJECT_TYPE, objectId, null, userAcl));
    hasEditPermission.set(true);
    assertThrows(IllegalAccessException.class, () -> attachmentService.getAttachment(OBJECT_TYPE, objectId, fileId, userAcl));
    hasEditPermission.set(false);
    hasViewPermission.set(true);

    ObjectAttachmentDetail attachmentDetail = attachmentService.getAttachment(OBJECT_TYPE, objectId, fileId, userAcl);
    assertNotNull(attachmentDetail);
    assertEquals(FILE_NAME, attachmentDetail.getName());
    assertEquals(MIME_TYPE, attachmentDetail.getMimetype());
    assertEquals(fileId, attachmentDetail.getId());
    assertTrue(attachmentDetail.getUpdated() > 0);
  }

  public void testGetAttachmentInputStream() throws Exception { // NOSONAR
    String fileId = createAttachment(USERNAME);
    Identity userAclIdentity = startSessionAndRegisterAs(USERNAME);

    assertThrows(IllegalAccessException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, fileId, "0x0", userAclIdentity));
    hasViewPermission.set(true);
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(null, objectId, fileId, "0x0", userAclIdentity));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_TYPE, null, fileId, "0x0", userAclIdentity));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, null, "0x0", userAclIdentity));
    hasEditPermission.set(false);
    hasViewPermission.set(true);

    try (InputStream inputStream = attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, fileId, "0x0", userAclIdentity)) {
      assertNotNull(inputStream);
    }
    try (InputStream inputStream = attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, fileId, null, userAclIdentity)) {
      assertNotNull(inputStream);
    }
  }

  public void testDeleteAttachments() throws Exception { // NOSONAR
    String fileId = createAttachment(USERNAME);
    ObjectAttachmentList objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    assertNotNull(objectAttachmentList);
    List<ObjectAttachmentDetail> attachments = objectAttachmentList.getAttachments();
    assertEquals(1, attachments.size());
    assertListenerCount(1l, 0l, 1l, 0l);
    assertEquals(ALT_TEXT, attachments.get(0).getAltText());
    assertEquals(FORMAT, attachments.get(0).getFormat());

    attachmentService.deleteAttachments(OBJECT_TYPE, objectId);
    objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    assertNotNull(objectAttachmentList);
    attachments = objectAttachmentList.getAttachments();
    assertEquals(0, attachments.size());
    assertListenerCount(1l, 1l, 1l, 1l);

    ObjectAttachmentDetail attachment = attachmentService.getAttachment(OBJECT_TYPE, objectId, fileId);
    assertNull(attachment);
  }

  private void uploadResource() throws IOException, Exception {
    File tempFile = File.createTempFile("image", "temp");
    uploadService.createUploadResource(UPLOAD_ID, tempFile.getPath(), FILE_NAME, MIME_TYPE);
  }

  private String createAttachment(String username) throws IOException, Exception {
    try {
      String identityId = identityManager.getOrCreateUserIdentity(username).getId();
      FileAttachmentResourceList attachmentList = new FileAttachmentResourceList();
      attachmentList.setAttachedFiles(null);
      attachmentList.setUserIdentityId(Long.parseLong(identityId));
      attachmentList.setObjectType(OBJECT_TYPE);
      attachmentList.setObjectId(objectId);
      hasEditPermission.set(true);
      FileAttachmentObject fileAttachmentObject = new FileAttachmentObject();
      fileAttachmentObject.setUploadId(UPLOAD_ID);
      fileAttachmentObject.setAltText(ALT_TEXT);
      fileAttachmentObject.setFormat(FORMAT);
      attachmentList.setUploadedFiles(Collections.singletonList(fileAttachmentObject));
      uploadResource();

      attachmentService.saveAttachments(attachmentList);
      ObjectAttachmentList objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
      return objectAttachmentList.getAttachments().get(0).getId();
    } finally {
      hasEditPermission.set(false);
    }
  }

  private void assertListenerCount(long createdCount,
                                   long deletedCount,
                                   long updatedListCount,
                                   long deletedListCount) {
    assertEquals(createdCount, eventCounts.getOrDefault(ATTACHMENT_CREATED_EVENT, 0l).longValue());
    assertEquals(deletedCount, eventCounts.getOrDefault(ATTACHMENT_DELETED_EVENT, 0l).longValue());
    assertEquals(updatedListCount, eventCounts.getOrDefault(ATTACHMENTS_UPDATED_EVENT, 0l).longValue());
    assertEquals(deletedListCount, eventCounts.getOrDefault(ATTACHMENTS_DELETED_EVENT, 0l).longValue());
  }

  public void testMoveAttachments() throws Exception { // NOSONAR
    startSessionAndRegisterAs(USERNAME);
    String identityId = identityManager.getOrCreateUserIdentity(USERNAME).getId();
    String fileId = createAttachment(USERNAME);

    String destinationObjectId = "destinationObjectId" + System.currentTimeMillis();
    String destinationParentObjectId = null;

    attachmentService.moveAttachments(OBJECT_TYPE, objectId, DEST_OBJECT_TYPE, destinationObjectId, destinationParentObjectId, Long.parseLong(identityId));

    // Verify the attachments are moved to the destination object
    ObjectAttachmentList destinationObjectAttachmentList = attachmentService.getAttachments(DEST_OBJECT_TYPE, destinationObjectId);
    assertNotNull(destinationObjectAttachmentList);
    assertEquals(1, destinationObjectAttachmentList.getAttachments().size());
    assertEquals(fileId, destinationObjectAttachmentList.getAttachments().get(0).getId());

    // Verify the attachments are removed from the source object
    ObjectAttachmentList sourceObjectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    assertNotNull(sourceObjectAttachmentList);
    assertEquals(0, sourceObjectAttachmentList.getAttachments().size());
  }

  public void testCopyAttachments() throws Exception { // NOSONAR
    startSessionAndRegisterAs(USERNAME);
    String identityId = identityManager.getOrCreateUserIdentity(USERNAME).getId();
    String fileId = createAttachment(USERNAME);

    String destinationObjectId = "destinationObjectId" + System.currentTimeMillis();
    String destinationParentObjectId = null;

    attachmentService.copyAttachments(OBJECT_TYPE, objectId, DEST_OBJECT_TYPE, destinationObjectId, destinationParentObjectId, Long.parseLong(identityId));

    // Verify the attachments are moved to the destination object
    // and the initial attachment have been duplicated
    ObjectAttachmentList destinationObjectAttachmentList = attachmentService.getAttachments(DEST_OBJECT_TYPE, destinationObjectId);
    assertNotNull(destinationObjectAttachmentList);
    assertEquals(1, destinationObjectAttachmentList.getAttachments().size());
    assertTrue(!fileId.equals(destinationObjectAttachmentList.getAttachments().get(0).getId()));

    // Verify the attachments are NOT removed from the source object
    ObjectAttachmentList sourceObjectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId);
    assertNotNull(sourceObjectAttachmentList);
    assertEquals(1, sourceObjectAttachmentList.getAttachments().size());
    assertEquals(fileId, sourceObjectAttachmentList.getAttachments().get(0).getId());

  }

  public void testCreateAttachment() throws Exception {
    Identity userAcl = startSessionAndRegisterAs(USERNAME);

    // Create a FileAttachmentObject
    FileAttachmentObject fileAttachmentObject = new FileAttachmentObject();
    fileAttachmentObject.setUploadId(UPLOAD_ID);
    fileAttachmentObject.setAltText(ALT_TEXT);
    fileAttachmentObject.setFormat(FORMAT);

    // Test the case when the object type or object ID is null
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.createAttachment(null, objectId, fileAttachmentObject, userAcl));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.createAttachment(PUBLIC_OBJECT_TYPE, null, fileAttachmentObject, userAcl));
    assertThrows(IllegalAccessException.class, () -> attachmentService.createAttachment(PUBLIC_OBJECT_TYPE, objectId, null, userAcl));

    // Test for insufficient permissions
    hasEditPermission.set(false);
    assertThrows(IllegalAccessException.class,
                 () -> attachmentService.createAttachment(PUBLIC_OBJECT_TYPE, objectId, fileAttachmentObject, userAcl));

    // Test for valid permissions
    hasEditPermission.set(true);
    hasViewPermission.set(true);
    uploadResource();
    ObjectAttachmentDetail createdAttachment = attachmentService.createAttachment(PUBLIC_OBJECT_TYPE,
                                                                                  objectId,
                                                                                  fileAttachmentObject,
                                                                                  userAcl);

    // Validate that the attachment was created
    assertNotNull(createdAttachment);
    assertEquals(ALT_TEXT, createdAttachment.getAltText());
    assertEquals(FORMAT, createdAttachment.getFormat());

    // Validate that the attachment is now part of the object
    ObjectAttachmentList objectAttachmentList = attachmentService.getAttachments(PUBLIC_OBJECT_TYPE, objectId);
    assertNotNull(objectAttachmentList);
    assertEquals(1, objectAttachmentList.getAttachments().size());
    ObjectAttachmentDetail attachmentDetail = objectAttachmentList.getAttachments().getFirst();
    assertEquals(createdAttachment.getId(), attachmentDetail.getId());
    assertEquals(ALT_TEXT, attachmentDetail.getAltText());
    assertEquals(FORMAT, attachmentDetail.getFormat());
  }
  
  private AttachmentPlugin createAttachmentPlugin(String objectType, boolean canUpdateList) {
    return new AttachmentPlugin() {
      @Override
      public boolean hasEditPermission(Identity userIdentity, String entityId) throws ObjectNotFoundException {
        return hasEditPermission.get();
      }

      @Override
      public boolean hasAccessPermission(Identity userIdentity, String entityId) throws ObjectNotFoundException {
        return hasViewPermission.get();
      }

      @Override
      public long getSpaceId(String objectId) throws ObjectNotFoundException {
        return attachmentSpaceId;
      }

      @Override
      public String getObjectType() {
        return objectType;
      }

      @Override
      public long getAudienceId(String objectId) throws ObjectNotFoundException {
        return attachmentAudienceId;
      }

      @Override
      public boolean  canUpdateAttachmentList() {
        return canUpdateList;
      }
    };
  }
}
