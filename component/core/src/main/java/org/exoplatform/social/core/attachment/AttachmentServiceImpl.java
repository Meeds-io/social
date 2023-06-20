/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentPlugin;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.FileAttachmentResourceList;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.attachment.model.ObjectAttachmentId;
import org.exoplatform.social.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.core.attachment.storage.FileAttachmentStorage;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

public class AttachmentServiceImpl implements AttachmentService {

  private static final Log                    LOG               = ExoLogger.getLogger(AttachmentServiceImpl.class);

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
  public ObjectAttachmentOperationReport createAttachments(FileAttachmentResourceList attachmentList,
                                                           Identity userAclIdentity) throws IllegalAccessException,
                                                                                     ObjectNotFoundException {
    checkEditPermissions(attachmentList, userAclIdentity);
    return createAttachments(attachmentList);
  }

  @Override
  public ObjectAttachmentOperationReport createAttachments(FileAttachmentResourceList attachmentList) {
    long userIdentityId = attachmentList.getUserIdentityId();
    String userName = getUserName(userIdentityId);
    String objectType = attachmentList.getObjectType();
    String objectId = attachmentList.getObjectId();

    List<String> uploadIds = attachmentList.getUploadIds();
    if (CollectionUtils.isEmpty(uploadIds)) {
      throw new IllegalArgumentException("Attachment uploadIds is mandatory");
    }
    ObjectAttachmentOperationReport report = attachUploadFiles(uploadIds,
                                                               objectType,
                                                               objectId,
                                                               attachmentList.getParentObjectId(),
                                                               userIdentityId);
    broadcastAttachmentsChange(ATTACHMENTS_CREATED_EVENT, objectType, objectId, userName);
    return report;
  }

  @Override
  public ObjectAttachmentOperationReport updateAttachments(FileAttachmentResourceList attachmentList,
                                                           Identity userAclIdentity) throws ObjectNotFoundException,
                                                                                     IllegalAccessException {
    checkEditPermissions(attachmentList, userAclIdentity);
    return updateAttachments(attachmentList);
  }

  @Override
  public ObjectAttachmentOperationReport updateAttachments(FileAttachmentResourceList attachmentList) {
    long userIdentityId = attachmentList.getUserIdentityId();
    String username = getUserName(userIdentityId);
    String objectType = attachmentList.getObjectType();
    String objectId = attachmentList.getObjectId();
    MetadataObject metadataObject = new MetadataObject(objectType, objectId);

    List<MetadataItem> existingAttachments =
                                           metadataService.getMetadataItemsByMetadataTypeAndObject(AttachmentService.METADATA_TYPE.getName(),
                                                                                                   metadataObject);
    if (CollectionUtils.isNotEmpty(existingAttachments)) {
      List<String> remainingFileIds = CollectionUtils.isEmpty(attachmentList.getFileIds()) ? Collections.emptyList()
                                                                                           : attachmentList.getFileIds();
      existingAttachments.stream()
                         .map(existingAttachment -> existingAttachment.getMetadata().getName())
                         .filter(fileId -> !remainingFileIds.contains(fileId))
                         .forEach(fileId -> deleteAttachment(objectType, objectId, fileId, username));
    }

    String parentObjectId = attachmentList.getParentObjectId();

    List<String> uploadIds = attachmentList.getUploadIds();
    ObjectAttachmentOperationReport report = null;
    if (CollectionUtils.isNotEmpty(uploadIds)) {
      report = attachUploadFiles(uploadIds, objectType, objectId, parentObjectId, userIdentityId);
    }
    broadcastAttachmentsChange(ATTACHMENTS_UPDATED_EVENT, objectType, objectId, username);
    return report;
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
    checkAccessPermission(objectType, objectId, userAclIdentity);
    return getAttachments(objectType, objectId);
  }

  @Override
  public ObjectAttachmentList getAttachments(String objectType, String objectId) {
    List<String> fileIds = getAttachmentFileIds(objectType, objectId);
    List<ObjectAttachmentDetail> attachments = fileIds.stream()
                                                      .map(fileId -> attachmentStorage.getAttachment(new ObjectAttachmentId(fileId,
                                                                                                                            objectType,
                                                                                                                            objectId)))
                                                      .filter(Objects::nonNull)
                                                      .toList();
    return new ObjectAttachmentList(attachments, objectType, objectId);
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
      return attachmentStorage.getAttachment(new ObjectAttachmentId(fileId, objectType, objectId));
    } else {
      return null;
    }
  }

  @Override
  public InputStream getAttachmentInputStream(String objectType,
                                              String objectId,
                                              String fileId,
                                              String imageDimensions,
                                              Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException,
                                                                        IOException {
    org.exoplatform.social.core.identity.model.Identity userIdentity = checkAccessPermission(objectType,
                                                                                             objectId,
                                                                                             fileId,
                                                                                             userAclIdentity);
    return attachmentStorage.getAttachmentInputStream(new ObjectAttachmentId(fileId, objectType, objectId),
                                                      imageDimensions,
                                                      userIdentity);
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

  private org.exoplatform.social.core.identity.model.Identity checkAccessPermission(String objectType, String objectId,
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
    org.exoplatform.social.core.identity.model.Identity userIdentity =
                                                                     identityManager.getOrCreateUserIdentity(userAclIdentity.getUserId());
    if (userIdentity == null || userIdentity.isDeleted() || !userIdentity.isEnable()) {
      throw new IllegalStateException("User with id " + userAclIdentity.getUserId() + " isn't valid");
    }

    if (!hasAccessPermission(userAclIdentity, objectType, objectId)) {
      throw new IllegalAccessException("User " + userAclIdentity.getUserId()
          + " doesn't have enough permissions to attach files on object " + objectType + "/" + objectId);
    }
    return userIdentity;
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
      throw new IllegalAccessException("User " + userAclIdentity.getUserId()
          + " doesn't have enough permissions to update file attachments of object " + objectType + "/" + objectId);
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

  private List<String> getAttachmentFileIds(String objectType, String objectId) {
    return metadataService.getMetadataNamesByMetadataTypeAndObject(METADATA_TYPE.getName(), objectType, objectId);
  }

  private ObjectAttachmentOperationReport attachUploadFiles(List<String> uploadIds,
                                                            String objectType,
                                                            String objectId,
                                                            String parentObjectId,
                                                            long userIdentityId) {
    ObjectAttachmentOperationReport report = new ObjectAttachmentOperationReport();
    uploadIds.stream()
             .distinct()
             .map(uploadId -> {
               UploadResource uploadedResource = uploadService.getUploadResource(uploadId);
               if (uploadedResource == null) {
                 LOG.warn("Uploaded resource with id " + uploadId + " wasn't found");
                 report.addError(uploadId, "attachment.uploadIdNotFound");
               }
               return uploadedResource;
             })
             .filter(Objects::nonNull)
             .forEach(uploadResource -> createAttachment(uploadResource,
                                                         objectType,
                                                         objectId,
                                                         parentObjectId,
                                                         userIdentityId,
                                                         report));
    return report;
  }

  private void createAttachment(UploadResource uploadResource,
                                String objectType,
                                String objectId,
                                String parentObjectId,
                                long userIdentityId,
                                ObjectAttachmentOperationReport report) {
    String fileDiskLocation = uploadResource.getStoreLocation();
    String uploadId = uploadResource.getUploadId();
    try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
      String fileId = attachmentStorage.uploadAttachment(objectType,
                                                         objectId,
                                                         uploadResource.getFileName(),
                                                         uploadResource.getMimeType(),
                                                         inputStream,
                                                         userIdentityId);
      MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(),
                                                fileId,
                                                getAudienceId(objectType, objectId));
      MetadataObject object = new MetadataObject(objectType,
                                                 objectId,
                                                 parentObjectId,
                                                 getSpaceId(objectType, objectId));
      metadataService.createMetadataItem(object, metadataKey, userIdentityId);

      broadcastAttachmentChange(ATTACHMENT_CREATED_EVENT,
                                metadataKey.getName(),
                                objectType,
                                objectId,
                                getUserName(userIdentityId));
    } catch (FileNotFoundException e) {
      LOG.warn("File with upload id " + uploadId + " doesn't exist", e);
      report.addError(uploadId, "attachment.uploadIdFileNotExistsError");
    } catch (IOException e) {
      LOG.warn("Error accessing resource with upload id " + uploadId, e);
      report.addError(uploadId, "attachment.uploadIdIOError");
    } catch (Exception e) {
      LOG.warn("Error attaching file with upload id " + uploadId, e);
      report.addError(uploadId, "attachment.uploadIdNotAttachedError");
    } finally {
      uploadService.removeUploadResource(uploadId);
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
          metadataService.deleteMetadataItem(metadataItem.getId(), false);
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
                                                                 userIdentityId > 0 ? identityManager.getIdentity(String.valueOf(userIdentityId))
                                                                                    : null;
    return identity == null ? null : identity.getRemoteId();
  }

}
