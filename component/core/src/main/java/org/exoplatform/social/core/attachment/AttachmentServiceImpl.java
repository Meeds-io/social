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
package org.exoplatform.social.core.attachment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.attachment.AttachmentPlugin;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.FileAttachmentObject;
import org.exoplatform.social.attachment.model.FileAttachmentResourceList;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.attachment.model.ObjectAttachmentId;
import org.exoplatform.social.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.attachment.model.UploadedAttachmentDetail;
import org.exoplatform.social.core.attachment.storage.FileAttachmentStorage;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

public class AttachmentServiceImpl implements AttachmentService {

  private static final Log                    LOG                 = ExoLogger.getLogger(AttachmentServiceImpl.class);

  private static final String                 ATTACHMENT_ALT_TEXT = "alt";

  private static final String                 ATTACHMENT_FORMAT   = "format";

  private final Map<String, AttachmentPlugin> attachmentPlugins = new HashMap<>();

  private FileAttachmentStorage               attachmentStorage;

  private IdentityManager                     identityManager;

  private MetadataService                     metadataService;

  private UploadService                       uploadService;

  private ListenerService                     listenerService;

  public AttachmentServiceImpl(FileAttachmentStorage attachmentStorage,
                               MetadataService metadataService,
                               IdentityManager identityManager,
                               UploadService uploadService,
                               ListenerService listenerService) {
    this.attachmentStorage = attachmentStorage;
    this.metadataService = metadataService;
    this.identityManager = identityManager;
    this.uploadService = uploadService;
    this.listenerService = listenerService;
  }

  @Override
  public void addPlugin(AttachmentPlugin attachmentPlugin) {
    this.attachmentPlugins.put(attachmentPlugin.getObjectType(), attachmentPlugin);
  }

  @Override
  public Set<String> getSupportedObjectTypes() {
    return this.attachmentPlugins.keySet();
  }

  @Override
  public Map<String, AttachmentPlugin> getAttachmentPlugins() {
    return this.attachmentPlugins;
  }

  @Override
  public ObjectAttachmentOperationReport saveAttachments(FileAttachmentResourceList attachmentList,
                                                         Identity userAclIdentity) throws ObjectNotFoundException,
                                                                                   IllegalAccessException {
    checkEditPermissions(attachmentList, userAclIdentity);
    if (!this.attachmentPlugins.get(attachmentList.getObjectType()).canUpdateAttachmentList()) {
      throw new IllegalStateException("Updating the attachment list is not allowed for object type: "
          + attachmentList.getObjectType());
    }
    return saveAttachments(attachmentList);
  }

  @Override
  public ObjectAttachmentOperationReport saveAttachments(FileAttachmentResourceList attachmentList) {
    long userIdentityId = attachmentList.getUserIdentityId();
    String username = getUserName(userIdentityId);
    String objectType = attachmentList.getObjectType();
    String objectId = attachmentList.getObjectId();
    MetadataObject metadataObject = new MetadataObject(objectType, objectId);

    List<MetadataItem> existingAttachments =
                                           metadataService.getMetadataItemsByMetadataTypeAndObject(AttachmentService.METADATA_TYPE.getName(),
                                                                                                   metadataObject);
    ObjectAttachmentOperationReport report = null;
    List<FileAttachmentObject> remainingFiles =
                                              CollectionUtils.isEmpty(attachmentList.getAttachedFiles()) ?
                                                                                                         Collections.emptyList() :
                                                                                                         attachmentList.getAttachedFiles();
    if (CollectionUtils.isNotEmpty(existingAttachments)) {

      List<String> remainingFileIds = remainingFiles.stream().map(file -> file.getId()).distinct().toList();

      existingAttachments.stream()
                         .map(existingAttachment -> existingAttachment.getMetadata().getName())
                         .filter(fileId -> !remainingFileIds.contains(fileId))
                         .forEach(fileId -> deleteAttachment(objectType, objectId, fileId, username));
      remainingFiles = remainingFiles.stream()
                                     .filter(remainingFile -> StringUtils.isNotEmpty(remainingFile.getId()))
                                     .toList();
    }

    String parentObjectId = attachmentList.getParentObjectId();

    List<FileAttachmentObject> uploadedFiles = attachmentList.getUploadedFiles();
    if (CollectionUtils.isNotEmpty(remainingFiles)) {
      uploadedFiles.addAll(remainingFiles);
    }
    if (CollectionUtils.isNotEmpty(uploadedFiles)) {
      report = attachUploadFiles(uploadedFiles, objectType, objectId, parentObjectId, userIdentityId);
    }

    broadcastAttachmentsChange(ATTACHMENTS_UPDATED_EVENT, objectType, objectId, username);
    return report;
  }


  @Override
  public void saveAttachment(UploadedAttachmentDetail uploadedAttachmentDetail,
                             String objectType,
                             String objectId,
                             String parentObjectId,
                             long userIdentityId) throws IOException,
                                                  ObjectAlreadyExistsException,
                                                  ObjectNotFoundException {
    UploadResource uploadResource = uploadedAttachmentDetail.getUploadedResource();
    String altText = uploadedAttachmentDetail.getAltText();
    String format = uploadedAttachmentDetail.getFormat();
    Map<String, String> properties = new HashMap<>();
    properties.put(ATTACHMENT_ALT_TEXT, altText);
    properties.put(ATTACHMENT_FORMAT, format);

    Long attachmentId =
                      !(StringUtils.isBlank(uploadedAttachmentDetail.getId())) ?
                                                                               Long.parseLong(uploadedAttachmentDetail.getId()) :
                                                                               null;
    if (uploadResource == null) {
      if (attachmentId != null) {
        updateAttachment(String.valueOf(attachmentId), objectType, objectId, userIdentityId, properties);
      }
    } else {
      String fileDiskLocation = uploadResource.getStoreLocation();
      String uploadId = uploadResource.getUploadId();
      try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
        String fileId = attachmentStorage.uploadAttachment(attachmentId,
                                                           objectType,
                                                           objectId,
                                                           uploadResource.getFileName(),
                                                           uploadResource.getMimeType(),
                                                           inputStream,
                                                           userIdentityId);
        if (attachmentId == null) {
          createAttachment(fileId, objectType, objectId, parentObjectId, userIdentityId, properties);
          uploadedAttachmentDetail.setId(fileId);
        } else {
          updateAttachment(fileId, objectType, objectId, userIdentityId, properties);
        }

      } finally {
        uploadService.removeUploadResource(uploadId);
      }
    }
  }

  @Override
  public ObjectAttachmentDetail createAttachment(String objectType,
                                                 String objectId,
                                                 FileAttachmentObject attachmentObject,
                                                 Identity userAclIdentity) throws ObjectNotFoundException,
                                                                           IllegalAccessException {
    checkAccessPermission(objectType, objectId, userAclIdentity);
    String uploadId = attachmentObject.getUploadId();
    UploadResource uploadResource = uploadService.getUploadResource(uploadId);
    if (uploadResource != null) {
      String fileId = null;
      Map<String, String> properties = new HashMap<>();
      properties.put(ATTACHMENT_ALT_TEXT, attachmentObject.getAltText());
      properties.put(ATTACHMENT_FORMAT, attachmentObject.getFormat());
      String fileDiskLocation = uploadResource.getStoreLocation();
      try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
        long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(userAclIdentity.getUserId()).getId());
        fileId = attachmentStorage.uploadAttachment(null,
                                                    objectType,
                                                    objectId,
                                                    uploadResource.getFileName(),
                                                    uploadResource.getMimeType(),
                                                    inputStream,
                                                    userIdentityId);
        createAttachment(fileId, objectType, objectId, null, userIdentityId, properties);
      } catch (Exception e) {
        LOG.error("Error creating attachment of objectType: {} and objectId: {}", objectType, objectId, e);
      } finally {
        uploadService.removeUploadResource(uploadId);
      }
      return getAttachment(objectType, objectId, fileId);
    }
    return null;
  }

  @Override
  public void deleteAttachments(String objectType, String objectId) {
    List<String> fileIds = getAttachmentFileIds(objectType, objectId);
    fileIds.forEach(fileId -> deleteAttachment(objectType, objectId, fileId));
    broadcastAttachmentsChange(ATTACHMENTS_DELETED_EVENT, objectType, objectId, null);
  }

  @Override
  public void deleteAttachment(String objectType,
                               String objectId,
                               String fileId) {
    deleteAttachment(objectType, objectId, fileId, null);
  }

  @Override
  public ObjectAttachmentList getAttachments(String objectType,
                                             String objectId,
                                             Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException {
    return getAttachments(objectType, objectId, userAclIdentity, 0, 0);
  }
  
  @Override
  public ObjectAttachmentList getAttachments(String objectType,
                                             String objectId,
                                             Identity userAclIdentity,
                                             int offset,
                                             int limit) throws ObjectNotFoundException, IllegalAccessException {
    checkAccessPermission(objectType, objectId, userAclIdentity);
    return getAttachments(objectType, objectId, offset, limit);
  }

  @Override
  public List<String> getAttachmentFileIds(String objectType,
                                           String objectId,
                                           Identity userAclIdentity) throws IllegalAccessException, ObjectNotFoundException {
    checkAccessPermission(objectType, objectId, userAclIdentity);
    return getAttachmentFileIds(objectType, objectId);
  }

  @Override
  public List<String> getAttachmentFileIds(String objectType, String objectId) {
    return getAttachmentFileIds(objectType, objectId, 0, 0);
  }

  @Override
  public ObjectAttachmentList getAttachments(String objectType, String objectId) {
   return getAttachments(objectType, objectId, 0, 0);
  }
  
  @Override
  public ObjectAttachmentDetail getAttachment(String objectType,
                                              String objectId,
                                              String fileId,
                                              Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException {
    checkAccessPermission(objectType, objectId, fileId, userAclIdentity);
    return getAttachment(objectType, objectId, fileId);
  }

  @Override
  public ObjectAttachmentDetail getAttachment(String objectType, String objectId, String fileId) {
    List<String> fileIds = getAttachmentFileIds(objectType, objectId);
    if (fileIds.contains(fileId)) {
      ObjectAttachmentDetail attachmentDetail = attachmentStorage.getAttachment(new ObjectAttachmentId(fileId,
                                                                                                       objectType,
                                                                                                       objectId));
      enrichAttachmentWithMetadata(attachmentDetail, objectType, objectId);
      return attachmentDetail;
    } else {
      return null;
    }
  }

  @Override
  public InputStream getAttachmentInputStream(String objectType,
                                              String objectId,
                                              String fileId,
                                              String imageDimensions,
                                              Identity userAclIdentity) throws ObjectNotFoundException,
                                                                        IllegalAccessException,
                                                                        IOException {
    if (objectType == null) {
      throw new IllegalArgumentException("objectType is mandatory");
    }
    if (objectId == null) {
      throw new IllegalArgumentException("objectId is mandatory");
    }
    if (fileId == null) {
      throw new IllegalArgumentException("fileId is mandatory");
    }
    if (!hasAccessPermission(userAclIdentity, objectType, objectId)) {
      throw new IllegalAccessException(String.format("User %s doesn't have enough permissions to get attached files on object %s/%s",
                                                     userAclIdentity.getUserId(),
                                                     objectType,
                                                     objectId));
    }
    return attachmentStorage.getAttachmentInputStream(new ObjectAttachmentId(fileId, objectType, objectId),
                                                      imageDimensions);
  }

  @Override
  public boolean hasAccessPermission(Identity userIdentity, String objectType, String objectId) throws ObjectNotFoundException {
    AttachmentPlugin attachmentPlugin = this.attachmentPlugins.get(objectType);
    return attachmentPlugin != null && attachmentPlugin.hasAccessPermission(userIdentity, objectId);
  }

  @Override
  public boolean hasEditPermission(Identity userIdentity, String objectType, String objectId) throws ObjectNotFoundException {
    AttachmentPlugin attachmentPlugin = this.attachmentPlugins.get(objectType);
    return attachmentPlugin != null && attachmentPlugin.hasEditPermission(userIdentity, objectId);
  }

  @Override
  public void moveAttachments(String sourceObjectType,
                              String sourceObjectId,
                              String destinationObjectType,
                              String destinationObjectId,
                              String destinationParentObjectId,
                              long userIdentityId) {
    moveAttachments(sourceObjectType,
                    sourceObjectId,
                    destinationObjectType,
                    destinationObjectId,
                    destinationParentObjectId,
                    userIdentityId,
                    true);
  }

  @Override
  public void copyAttachments(String sourceObjectType,
                              String sourceObjectId,
                              String destinationObjectType,
                              String destinationObjectId,
                              String destinationParentObjectId,
                              long userIdentityId) {
    moveAttachments(sourceObjectType,
                    sourceObjectId,
                    destinationObjectType,
                    destinationObjectId,
                    destinationParentObjectId,
                    userIdentityId,
                    false);
  }



  private void moveAttachments(String sourceObjectType,
                               String sourceObjectId,
                               String destinationObjectType,
                               String destinationObjectId,
                               String destinationParentObjectId,
                               long userIdentityId,
                               boolean deleteAfterMove) {
    ObjectAttachmentList objectAttachmentList = getAttachments(sourceObjectType, sourceObjectId);
    List<ObjectAttachmentDetail> attachments = objectAttachmentList.getAttachments();
    if (CollectionUtils.isNotEmpty(attachments)) {
      attachments.forEach(attachment -> {
        String altText = attachment.getAltText();
        String format = attachment.getFormat();
        Map<String, String> properties = new HashMap<>();
        properties.put(ATTACHMENT_ALT_TEXT, altText);
        properties.put(ATTACHMENT_FORMAT, format);
        try {
          createAttachment(attachment.getId(),
                           destinationObjectType,
                           destinationObjectId,
                           destinationParentObjectId,
                           userIdentityId,
                           properties);
          if (deleteAfterMove) {
            List<MetadataItem> metadataItemToDelete =
                metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(attachment.getId(),
                                                                               AttachmentService.METADATA_TYPE.getName(),
                                                                               sourceObjectType,
                                                                               sourceObjectId,
                                                                               0,
                                                                               0);
            if (CollectionUtils.isNotEmpty(metadataItemToDelete)) {
              metadataItemToDelete.forEach(metadataItem -> {
                try {
                  metadataService.deleteMetadataItem(metadataItem.getId(), true);
                } catch (ObjectNotFoundException e) {
                  LOG.error("Error when deleting metadata item", e);
                }
              });
            }
          }
        } catch (Exception e) {
          LOG.error("Error when creating attachment", e);
        }
      });
    }
  }

  @Override
  public void createAttachment(String fileId,
                               String objectType,
                               String objectId,
                               String parentObjectId,
                               long userIdentityId,
                               Map<String, String> properties) throws ObjectNotFoundException, ObjectAlreadyExistsException {
    MetadataKey metadataKey = null;
    metadataKey = new MetadataKey(METADATA_TYPE.getName(), fileId, getAudienceId(objectType, objectId));
    MetadataObject object = new MetadataObject(objectType,
            objectId,
            parentObjectId,
            getSpaceId(objectType, objectId));
    metadataService.createMetadataItem(object,
            metadataKey,
            properties,
            userIdentityId);
    broadcastAttachmentChange(ATTACHMENT_CREATED_EVENT,
            fileId,
            objectType,
            objectId,
            getUserName(userIdentityId));
  }


  private org.exoplatform.social.core.identity.model.Identity checkAccessPermission(String objectType,
                                                                                    String objectId,
                                                                                    String fileId,
                                                                                    Identity userAclIdentity) throws ObjectNotFoundException,
                                                                                                              IllegalAccessException {
    if (StringUtils.isBlank(fileId)) {
      throw new IllegalArgumentException("File identifier is mandatory");
    }

    return checkAccessPermission(objectType, objectId, userAclIdentity);
  }

  private org.exoplatform.social.core.identity.model.Identity checkAccessPermission(String objectType,
                                                                                    String objectId,
                                                                                    Identity userAclIdentity) throws ObjectNotFoundException,
                                                                                                              IllegalAccessException {
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("Object identifier is mandatory");
    }
    if (userAclIdentity == null) {
      throw new IllegalArgumentException("User ACL identity is mandatory");
    }
    if (!hasAccessPermission(userAclIdentity, objectType, objectId)) {
      throw new IllegalAccessException("User " + userAclIdentity.getUserId() +
          " doesn't have enough permissions to attach files on object " + objectType + "/" + objectId);
    }
    return isAnonymous(userAclIdentity) ? null :
                                        identityManager.getOrCreateUserIdentity(userAclIdentity.getUserId());
  }

  private void checkEditPermissions(FileAttachmentResourceList attachmentList,
                                    Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException {
    if (attachmentList == null) {
      throw new IllegalArgumentException("Attachment is mandatory");
    }
    if (userAclIdentity == null) {
      throw new IllegalArgumentException("User ACL identity is mandatory");
    }

    long userIdentityId = attachmentList.getUserIdentityId();
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("User identity id is mandatory");
    }

    org.exoplatform.social.core.identity.model.Identity userIdentity =
                                                                     identityManager.getIdentity(String.valueOf(userIdentityId));

    if (userIdentity == null || userIdentity.isDeleted() || !userIdentity.isEnable()) {
      throw new IllegalStateException("User with id " + userIdentityId + " isn't valid");
    }

    String objectType = attachmentList.getObjectType();
    String objectId = attachmentList.getObjectId();
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("Object identifier is mandatory");
    }

    if (!hasEditPermission(userAclIdentity, objectType, objectId)) {
      throw new IllegalAccessException(String.format("User %s doesn't have enough permissions to update file attachments of object %s/%s",
                                                     userAclIdentity.getUserId(),
                                                     objectType,
                                                     objectId));
    }
  }

  private long getAudienceId(String objectType, String objectId) throws ObjectNotFoundException {
    AttachmentPlugin attachmentPlugin = this.attachmentPlugins.get(objectType);
    return attachmentPlugin == null ? 0 : attachmentPlugin.getAudienceId(objectId);
  }

  private long getSpaceId(String objectType, String objectId) throws ObjectNotFoundException {
    AttachmentPlugin attachmentPlugin = this.attachmentPlugins.get(objectType);
    return attachmentPlugin == null ? 0 : attachmentPlugin.getSpaceId(objectId);
  }

  private ObjectAttachmentOperationReport attachUploadFiles(List<FileAttachmentObject> uploadedFiles,
                                                            String objectType,
                                                            String objectId,
                                                            String parentObjectId,
                                                            long userIdentityId) {
    ObjectAttachmentOperationReport report = new ObjectAttachmentOperationReport();
    uploadedFiles.stream().distinct().map(uploadedFile -> {
      UploadedAttachmentDetail uploadedAttachmentDetail = new UploadedAttachmentDetail();
      UploadResource uploadedResource = uploadService.getUploadResource(uploadedFile.getUploadId());
      if (uploadedResource == null) {
        LOG.warn("Uploaded resource with id " + uploadedFile.getUploadId() + " wasn't found");
        report.addError(uploadedFile.getUploadId(), "attachment.uploadIdNotFound");
      }
      uploadedAttachmentDetail.setId(uploadedFile.getId());
      uploadedAttachmentDetail.setAltText(uploadedFile.getAltText());
      uploadedAttachmentDetail.setFormat(uploadedFile.getFormat());
      uploadedAttachmentDetail.setUploadedResource(uploadedResource);
      return uploadedAttachmentDetail;
    })
                 .filter(Objects::nonNull)
                 .forEach(uploadedAttachmentDetail -> saveAttachment(uploadedAttachmentDetail,
                                                                     objectType,
                                                                     objectId,
                                                                     parentObjectId,
                                                                     userIdentityId,
                                                                     report));
    return report;
  }

  private void saveAttachment(UploadedAttachmentDetail uploadedAttachmentDetail,
                              String objectType,
                              String objectId,
                              String parentObjectId,
                              long userIdentityId,
                              ObjectAttachmentOperationReport report) {
    try {
      saveAttachment(uploadedAttachmentDetail, objectType, objectId, parentObjectId, userIdentityId);
    } catch (FileNotFoundException e) {
      LOG.warn("Uploaded File wasn't found", e);
      report.addError(uploadedAttachmentDetail.getUploadedResource().getUploadId(), "attachment.uploadIdFileNotExistsError");
    } catch (IOException e) {
      LOG.warn("Error accessing Uploaded resource", e);
      report.addError(uploadedAttachmentDetail.getUploadedResource().getUploadId(), "attachment.uploadIdIOError");
    } catch (Exception e) {
      LOG.warn("Error attaching uploaded file", e);
      report.addError(uploadedAttachmentDetail.getUploadedResource().getUploadId(), "attachment.uploadIdNotAttachedError");
    }
  }

  private void updateAttachment(String fileId,
                                String objectType,
                                String objectId,
                                long userIdentityId,
                                Map<String, String> properties) {
    List<MetadataItem> attachmentItem =
                                      metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(fileId,
                                                                                                     AttachmentService.METADATA_TYPE.getName(),
                                                                                                     objectType,
                                                                                                     objectId,
                                                                                                     0,
                                                                                                     0);
    if (CollectionUtils.isNotEmpty(attachmentItem)) {
      MetadataItem attachmentItemMetadata = attachmentItem.get(0);
      attachmentItemMetadata.setProperties(properties);
      metadataService.updateMetadataItem(attachmentItemMetadata, userIdentityId);
      broadcastAttachmentChange(ATTACHMENTS_UPDATED_EVENT,
                                fileId,
                                objectType,
                                objectId,
                                getUserName(userIdentityId));
    }
  }

  private void deleteAttachment(String objectType,
                                String objectId,
                                String fileId,
                                String username) {
    List<MetadataItem> metadataItemToDelete = metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(fileId,
                                                                                                             AttachmentService.METADATA_TYPE.getName(),
                                                                                                             objectType,
                                                                                                             objectId,
                                                                                                             0,
                                                                                                             0);

    if (CollectionUtils.isNotEmpty(metadataItemToDelete)) {
      metadataItemToDelete.forEach(metadataItem -> {
        try {
          metadataService.deleteMetadataItem(metadataItem.getId(), true);
          attachmentStorage.deleteAttachment(new ObjectAttachmentId(fileId, objectType, objectId));
        } catch (Exception e) {
          LOG.warn("Error while deleting metadata ite {} for attachment deletion. Continue processing object attachments update.",
                   metadataItem,
                   e);
        }
      });
      broadcastAttachmentChange(ATTACHMENT_DELETED_EVENT,
                                fileId,
                                objectType,
                                objectId,
                                username);
    }
  }

  private void broadcastAttachmentChange(String eventName,
                                         String fileId,
                                         String objectType,
                                         String objectId,
                                         String username) {
    ObjectAttachmentId payload = new ObjectAttachmentId(fileId, objectType, objectId);
    try {
      this.listenerService.broadcast(eventName, username, payload);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event {} for attachment {}", eventName, payload, e);
    }
  }

  private void broadcastAttachmentsChange(String eventName,
                                          String objectType,
                                          String objectId,
                                          String username) {
    ObjectAttachmentId payload = new ObjectAttachmentId(objectType, objectId);
    try {
      this.listenerService.broadcast(eventName, username, payload);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event {} for attachment {}", eventName, payload, e);
    }
  }

  private String getUserName(long userIdentityId) {
    org.exoplatform.social.core.identity.model.Identity identity =
                                                                 userIdentityId
                                                                     > 0 ?
                                                                         identityManager.getIdentity(String.valueOf(userIdentityId)) :
                                                                         null;
    return identity == null ? null : identity.getRemoteId();
  }

  private boolean isAnonymous(Identity userAclIdentity) {
    return userAclIdentity == null || IdentityConstants.ANONIM.equals(userAclIdentity.getUserId());
  }

  private List<String> getAttachmentFileIds(String objectType, String objectId, int offset, int limit) {
    return metadataService.getMetadataNamesByMetadataTypeAndObject(METADATA_TYPE.getName(), objectType, objectId, offset, limit)
            .stream()
            .toList();
  }

  private ObjectAttachmentList getAttachments(String objectType, String objectId, int offset, int limit) {
    List<String> fileIds = getAttachmentFileIds(objectType, objectId, offset, limit);
    if (fileIds.isEmpty()) {
      return new ObjectAttachmentList(Collections.emptyList(), objectType, objectId);
    }

    List<ObjectAttachmentDetail> attachments =
                                             fileIds.stream()
                                                    .map(fileId -> attachmentStorage.getAttachment(new ObjectAttachmentId(fileId,
                                                                                                                          objectType,
                                                                                                                          objectId)))
                                                    .filter(Objects::nonNull)
                                                    .toList();

    if (attachments.isEmpty()) {
      return new ObjectAttachmentList(Collections.emptyList(), objectType, objectId);
    }
    attachments.forEach(attachment -> enrichAttachmentWithMetadata(attachment, objectType, objectId));
    return new ObjectAttachmentList(attachments, objectType, objectId);
  }

  private void enrichAttachmentWithMetadata(ObjectAttachmentDetail attachment, String objectType, String objectId) {
    List<MetadataItem> attachmentItems =
                                       metadataService.getMetadataItemsByMetadataNameAndTypeAndObject(attachment.getId(),
                                                                                                      AttachmentService.METADATA_TYPE.getName(),
                                                                                                      objectType,
                                                                                                      objectId,
                                                                                                      0,
                                                                                                      0);

    if (CollectionUtils.isNotEmpty(attachmentItems)) {
      MetadataItem metadataItem = attachmentItems.getFirst();
      Map<String, String> properties = metadataItem.getProperties();

      if (properties != null && properties.containsKey(ATTACHMENT_ALT_TEXT)) {
        attachment.setAltText(properties.get(ATTACHMENT_ALT_TEXT));
        attachment.setFormat(properties.get(ATTACHMENT_FORMAT));
      }
    }
  }
}
