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

import java.io.ByteArrayInputStream;
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
import org.exoplatform.social.attachment.model.UploadedAttachmentDetail;
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

  private static final String   REPLACEMENT_UPLOAD_ID = "5678";

  private static final String   REPLACEMENT_FILE_NAME = "replacement.png";

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

  private AtomicBoolean         failAudience         = new AtomicBoolean();

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

  public void testSaveAttachmentsReplacesOnlyTheObjectsOwnFiles() throws Exception { // NOSONAR
    Identity userAcl = startSessionAndRegisterAs(USERNAME);
    long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(USERNAME).getId());
    String otherObjectId = objectId;
    String fileId = createAttachment(USERNAME);
    String editedObjectId = "objectId" + RANDOM.nextInt();
    hasEditPermission.set(true);

    FileAttachmentObject replacement = new FileAttachmentObject();
    replacement.setId(fileId);
    replacement.setUploadId(REPLACEMENT_UPLOAD_ID);
    FileAttachmentResourceList attachmentList = new FileAttachmentResourceList();
    attachmentList.setUserIdentityId(userIdentityId);
    attachmentList.setObjectType(OBJECT_TYPE);
    attachmentList.setObjectId(editedObjectId);
    attachmentList.setAttachedFiles(new ArrayList<>(List.of(replacement)));
    attachmentList.setUploadedFiles(new ArrayList<>());
    uploadReplacement();
    assertThrows(IllegalAccessException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));
    attachmentList.setAttachedFiles(new ArrayList<>());
    attachmentList.setUploadedFiles(new ArrayList<>(List.of(replacement)));
    assertThrows(IllegalAccessException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));

    UploadedAttachmentDetail replacementDetail = new UploadedAttachmentDetail(uploadService.getUploadResource(REPLACEMENT_UPLOAD_ID));
    replacementDetail.setId(fileId);
    long fileIdBefore = writeProbeFile(userIdentityId);
    assertThrows(ObjectNotFoundException.class,
                 () -> attachmentService.saveAttachment(replacementDetail, OBJECT_TYPE, editedObjectId, null, userIdentityId));
    long fileIdAfter = writeProbeFile(userIdentityId);
    for (long writtenFileId = fileIdBefore + 1; writtenFileId < fileIdAfter; writtenFileId++) {
      assertNull(attachmentStorage.getAttachment(new ObjectAttachmentId(String.valueOf(writtenFileId),
                                                                        OBJECT_TYPE,
                                                                        editedObjectId)));
    }

    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, otherObjectId)).getName());
    assertEquals(List.of(fileId), attachmentService.getAttachmentFileIds(OBJECT_TYPE, otherObjectId));
    assertTrue(attachmentService.getAttachmentFileIds(OBJECT_TYPE, editedObjectId).isEmpty());

    attachmentList.setObjectId(otherObjectId);
    attachmentList.setAttachedFiles(new ArrayList<>(List.of(replacement)));
    attachmentList.setUploadedFiles(new ArrayList<>());
    uploadReplacement();
    attachmentService.saveAttachments(attachmentList, userAcl);
    assertEquals(REPLACEMENT_FILE_NAME,
                 attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, otherObjectId)).getName());
    assertEquals(List.of(fileId), attachmentService.getAttachmentFileIds(OBJECT_TYPE, otherObjectId));
  }

  public void testSaveAttachmentsRefusesAForeignFileBeforeChangingTheObject() throws Exception { // NOSONAR
    Identity userAcl = startSessionAndRegisterAs(USERNAME);
    long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(USERNAME).getId());
    String foreignFileId = createAttachment(USERNAME);
    String editedObjectId = "objectId" + RANDOM.nextInt();
    hasEditPermission.set(true);
    FileAttachmentObject ownUpload = new FileAttachmentObject();
    ownUpload.setUploadId(UPLOAD_ID);
    FileAttachmentResourceList ownList = new FileAttachmentResourceList();
    ownList.setUserIdentityId(userIdentityId);
    ownList.setObjectType(OBJECT_TYPE);
    ownList.setObjectId(editedObjectId);
    ownList.setUploadedFiles(new ArrayList<>(List.of(ownUpload)));
    uploadResource();
    attachmentService.saveAttachments(ownList);
    String ownFileId = attachmentService.getAttachmentFileIds(OBJECT_TYPE, editedObjectId).get(0);

    // The own file is left out, so the save would delete it, and a new
    // upload comes with the foreign file, so the save would write it
    FileAttachmentResourceList attachmentList = replacementList(foreignFileId, editedObjectId, userIdentityId);
    FileAttachmentObject newUpload = new FileAttachmentObject();
    newUpload.setUploadId(UPLOAD_ID);
    attachmentList.setUploadedFiles(new ArrayList<>(List.of(newUpload)));
    uploadResource();
    long createdCount = eventCounts.getOrDefault(ATTACHMENT_CREATED_EVENT, 0l);
    long deletedCount = eventCounts.getOrDefault(ATTACHMENT_DELETED_EVENT, 0l);

    long fileIdBefore = writeProbeFile(userIdentityId);
    assertThrows(IllegalAccessException.class, () -> attachmentService.saveAttachments(attachmentList, userAcl));
    long fileIdAfter = writeProbeFile(userIdentityId);

    for (long writtenFileId = fileIdBefore + 1; writtenFileId < fileIdAfter; writtenFileId++) {
      assertNull(attachmentStorage.getAttachment(new ObjectAttachmentId(String.valueOf(writtenFileId),
                                                                        OBJECT_TYPE,
                                                                        editedObjectId)));
    }
    assertEquals(List.of(ownFileId), attachmentService.getAttachmentFileIds(OBJECT_TYPE, editedObjectId));
    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(ownFileId, OBJECT_TYPE, editedObjectId)).getName());
    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(foreignFileId, OBJECT_TYPE, objectId)).getName());
    assertEquals(createdCount, eventCounts.getOrDefault(ATTACHMENT_CREATED_EVENT, 0l).longValue());
    assertEquals(deletedCount, eventCounts.getOrDefault(ATTACHMENT_DELETED_EVENT, 0l).longValue());
  }

  public void testSaveAttachmentsKeepsAFileAnotherObjectLinks() throws Exception { // NOSONAR
    Identity userAcl = startSessionAndRegisterAs(USERNAME);
    long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(USERNAME).getId());
    String sourceObjectId = objectId;
    // The two objects link the file under two audiences
    attachmentAudienceId = 5;
    String fileId = createAttachment(USERNAME);
    attachmentAudienceId = 0;
    String linkingObjectId = "objectId" + RANDOM.nextInt();
    attachmentService.createAttachment(fileId, OBJECT_TYPE, linkingObjectId, null, userIdentityId, Collections.emptyMap());
    hasEditPermission.set(true);
    long deletedCount = eventCounts.getOrDefault(ATTACHMENT_DELETED_EVENT, 0l);

    FileAttachmentResourceList attachmentList = replacementList(fileId, linkingObjectId, userIdentityId);
    attachmentService.saveAttachments(attachmentList, userAcl);

    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, sourceObjectId)).getName());
    assertEquals(List.of(fileId), attachmentService.getAttachmentFileIds(OBJECT_TYPE, sourceObjectId));
    List<String> linkingFileIds = attachmentService.getAttachmentFileIds(OBJECT_TYPE, linkingObjectId);
    assertEquals(1, linkingFileIds.size());
    assertFalse(linkingFileIds.contains(fileId));
    String replacementFileId = linkingFileIds.get(0);
    assertEquals(REPLACEMENT_FILE_NAME,
                 attachmentStorage.getAttachment(new ObjectAttachmentId(replacementFileId, OBJECT_TYPE, linkingObjectId))
                                  .getName());
    assertEquals(deletedCount + 1, eventCounts.getOrDefault(ATTACHMENT_DELETED_EVENT, 0l).longValue());

    attachmentService.createAttachment(fileId, OBJECT_TYPE, linkingObjectId, null, userIdentityId, Collections.emptyMap());
    attachmentList.setAttachedFiles(new ArrayList<>());
    attachmentService.saveAttachments(attachmentList, userAcl);
    assertTrue(attachmentService.getAttachmentFileIds(OBJECT_TYPE, linkingObjectId).isEmpty());
    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, sourceObjectId)).getName());
    assertNull(attachmentStorage.getAttachment(new ObjectAttachmentId(replacementFileId, OBJECT_TYPE, linkingObjectId)));
  }

  public void testSaveAttachmentsKeepsAFileAnObjectOfAnotherTypeLinks() throws Exception { // NOSONAR
    Identity userAcl = startSessionAndRegisterAs(USERNAME);
    long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(USERNAME).getId());
    String fileId = createAttachment(USERNAME);
    attachmentService.createAttachment(fileId, PUBLIC_OBJECT_TYPE, objectId, null, userIdentityId, Collections.emptyMap());
    hasEditPermission.set(true);

    attachmentService.saveAttachments(replacementList(fileId, objectId, userIdentityId), userAcl);

    assertEquals(List.of(fileId), attachmentService.getAttachmentFileIds(PUBLIC_OBJECT_TYPE, objectId));
    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, PUBLIC_OBJECT_TYPE, objectId)).getName());
    assertFalse(attachmentService.getAttachmentFileIds(OBJECT_TYPE, objectId).contains(fileId));
  }

  public void testSaveAttachmentsKeepsTheSharedFileWhenTheNewLinkFails() throws Exception { // NOSONAR
    Identity userAcl = startSessionAndRegisterAs(USERNAME);
    long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(USERNAME).getId());
    String fileId = createAttachment(USERNAME);
    String linkingObjectId = "objectId" + RANDOM.nextInt();
    attachmentService.createAttachment(fileId, OBJECT_TYPE, linkingObjectId, null, userIdentityId, Collections.emptyMap());
    hasEditPermission.set(true);
    failAudience.set(true);
    FileAttachmentResourceList attachmentList = replacementList(fileId, linkingObjectId, userIdentityId);

    long fileIdBefore = writeProbeFile(userIdentityId);
    attachmentService.saveAttachments(attachmentList, userAcl);
    long fileIdAfter = writeProbeFile(userIdentityId);

    assertEquals(List.of(fileId), attachmentService.getAttachmentFileIds(OBJECT_TYPE, linkingObjectId));
    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, objectId)).getName());
    assertTrue(fileIdAfter > fileIdBefore + 1);
    for (long writtenFileId = fileIdBefore + 1; writtenFileId < fileIdAfter; writtenFileId++) {
      assertNull(attachmentStorage.getAttachment(new ObjectAttachmentId(String.valueOf(writtenFileId),
                                                                        OBJECT_TYPE,
                                                                        linkingObjectId)));
    }
  }

  public void testDeleteAttachmentsKeepsAFileAnotherObjectLinks() throws Exception { // NOSONAR
    startSessionAndRegisterAs(USERNAME);
    long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(USERNAME).getId());
    String fileId = createAttachment(USERNAME);
    String linkingObjectId = "objectId" + RANDOM.nextInt();
    attachmentService.createAttachment(fileId, OBJECT_TYPE, linkingObjectId, null, userIdentityId, Collections.emptyMap());

    attachmentService.deleteAttachments(OBJECT_TYPE, linkingObjectId);

    assertTrue(attachmentService.getAttachmentFileIds(OBJECT_TYPE, linkingObjectId).isEmpty());
    assertEquals(List.of(fileId), attachmentService.getAttachmentFileIds(OBJECT_TYPE, objectId));
    assertEquals(FILE_NAME, attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, objectId)).getName());

    attachmentService.deleteAttachment(OBJECT_TYPE, objectId, fileId);

    assertTrue(attachmentService.getAttachmentFileIds(OBJECT_TYPE, objectId).isEmpty());
    assertNull(attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, OBJECT_TYPE, objectId)));
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

  private FileAttachmentResourceList replacementList(String fileId,
                                                     String editedObjectId,
                                                     long userIdentityId) throws Exception {
    FileAttachmentObject replacement = new FileAttachmentObject();
    replacement.setId(fileId);
    replacement.setUploadId(REPLACEMENT_UPLOAD_ID);
    FileAttachmentResourceList attachmentList = new FileAttachmentResourceList();
    attachmentList.setUserIdentityId(userIdentityId);
    attachmentList.setObjectType(OBJECT_TYPE);
    attachmentList.setObjectId(editedObjectId);
    attachmentList.setAttachedFiles(new ArrayList<>(List.of(replacement)));
    attachmentList.setUploadedFiles(new ArrayList<>());
    uploadReplacement();
    return attachmentList;
  }

  private long writeProbeFile(long userIdentityId) throws IOException {
    return Long.parseLong(attachmentStorage.uploadAttachment(null,
                                                             OBJECT_TYPE,
                                                             objectId,
                                                             "probe.png",
                                                             MIME_TYPE,
                                                             new ByteArrayInputStream(new byte[0]),
                                                             userIdentityId));
  }

  private void uploadReplacement() throws Exception {
    File tempFile = File.createTempFile("replacement", "temp");
    uploadService.createUploadResource(REPLACEMENT_UPLOAD_ID, tempFile.getPath(), REPLACEMENT_FILE_NAME, MIME_TYPE);
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
  
  public void testGetAttachmentsKeepsTheFormatOfAnAttachmentWithoutAltText() throws Exception {
    Identity userAcl = startSessionAndRegisterAs(USERNAME);
    hasEditPermission.set(true);
    hasViewPermission.set(true);
    uploadResource();
    FileAttachmentObject fileAttachmentObject = new FileAttachmentObject();
    fileAttachmentObject.setUploadId(UPLOAD_ID);
    fileAttachmentObject.setFormat("square");

    ObjectAttachmentDetail createdAttachment = attachmentService.createAttachment(PUBLIC_OBJECT_TYPE,
                                                                                  objectId,
                                                                                  fileAttachmentObject,
                                                                                  userAcl);

    ObjectAttachmentDetail attachmentDetail = attachmentService.getAttachments(PUBLIC_OBJECT_TYPE, objectId)
                                                               .getAttachments()
                                                               .getFirst();
    assertEquals(createdAttachment.getId(), attachmentDetail.getId());
    assertNull(attachmentDetail.getAltText());
    assertEquals("square", attachmentDetail.getFormat());
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
        if (failAudience.get()) {
          throw new ObjectNotFoundException("Object " + objectId + " isn't found");
        }
        return attachmentAudienceId;
      }

      @Override
      public boolean  canUpdateAttachmentList() {
        return canUpdateList;
      }
    };
  }
}
