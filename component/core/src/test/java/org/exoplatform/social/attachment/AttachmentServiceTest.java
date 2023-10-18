/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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
package org.exoplatform.social.attachment;

import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENTS_DELETED_EVENT;
import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENTS_UPDATED_EVENT;
import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENT_CREATED_EVENT;
import static org.exoplatform.social.attachment.AttachmentService.ATTACHMENT_DELETED_EVENT;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.model.*;
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

      AttachmentPlugin attachmentPlugin = new AttachmentPlugin() {

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
          return OBJECT_TYPE;
        }

        @Override
        public long getAudienceId(String objectId) throws ObjectNotFoundException {
          return attachmentAudienceId;
        }

      };
      attachmentService.addPlugin(attachmentPlugin);
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
    assertEquals(fileId, attachments.get(0).getId());
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

    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(null, objectId, userAcl));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(OBJECT_TYPE, null, userAcl));
    assertThrows(IllegalArgumentException.class, () -> attachmentService.getAttachments(OBJECT_TYPE, objectId, null));
    hasEditPermission.set(true);
    assertThrows(IllegalAccessException.class, () -> attachmentService.getAttachments(OBJECT_TYPE, objectId, userAcl));
    hasEditPermission.set(false);
    hasViewPermission.set(true);

    ObjectAttachmentList objectAttachmentList = attachmentService.getAttachments(OBJECT_TYPE, objectId, userAcl);
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
    Identity userAcl = startSessionAndRegisterAs(USERNAME);

    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(null, objectId, fileId, "0x0", userAcl));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_TYPE, null, fileId, "0x0", userAcl));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, fileId, "0x0", null));
    assertThrows(IllegalArgumentException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, null, "0x0", userAcl));
    hasEditPermission.set(true);
    assertThrows(IllegalAccessException.class,
                 () -> attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, fileId, null, userAcl));
    hasEditPermission.set(false);
    hasViewPermission.set(true);

    try (InputStream inputStream = attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, fileId, "0x0", userAcl)) {
      assertNotNull(inputStream);
    }
    try (InputStream inputStream = attachmentService.getAttachmentInputStream(OBJECT_TYPE, objectId, fileId, null, userAcl)) {
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

}
