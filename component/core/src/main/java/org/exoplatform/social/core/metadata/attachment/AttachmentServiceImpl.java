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
package org.exoplatform.social.core.metadata.attachment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.AttachmentPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.attachment.model.ObjectUploadResourceList;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.thumbnail.ImageThumbnailService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

public class AttachmentServiceImpl implements AttachmentService {

  private static final Log                    LOG                           =
                                                  ExoLogger.getLogger(AttachmentServiceImpl.class);

  private static final String                 FILE_API_NAMESPACE            = "attachment";

  private final Map<String, AttachmentPlugin> attachmentPermissionPluginMap = new HashMap<>();

  private IdentityManager                     identityManager;

  private FileService                         fileService;

  private MetadataService                     metadataService;

  private ImageThumbnailService               imageThumbnailService;

  private UploadService                       uploadService;

  public AttachmentServiceImpl(MetadataService metadataService,
                               IdentityManager identityManager,
                               FileService fileService,
                               ImageThumbnailService imageThumbnailService,
                               UploadService uploadService) {
    this.metadataService = metadataService;
    this.identityManager = identityManager;
    this.imageThumbnailService = imageThumbnailService;
    this.fileService = fileService;
    this.uploadService = uploadService;
  }

  @Override
  public void addPlugin(AttachmentPlugin attachmentPermessionPlugin) {
    this.attachmentPermissionPluginMap.put(attachmentPermessionPlugin.getObjectType(), attachmentPermessionPlugin);
  }

  @Override
  public ObjectAttachmentOperationReport createAttachments(ObjectUploadResourceList attachmentList,
                                                           Identity userAclIdentity) throws IllegalAccessException,
                                                                                     ObjectNotFoundException {
    if (attachmentList == null) {
      throw new IllegalArgumentException("Attachment is mandatory");
    }
    List<String> uploadIds = attachmentList.getUploadIds();
    if (CollectionUtils.isEmpty(uploadIds)) {
      throw new IllegalArgumentException("Attachment uploadIds is mandatory");
    }
    uploadIds = uploadIds.stream().distinct().toList();

    long userIdentityId = attachmentList.getUserIdentityId();
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("User identity id is mandatory");
    }
    String objectType = attachmentList.getObjectType();
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    String objectId = attachmentList.getObjectId();
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("Object identifier is mandatory");
    }
    String parentObjectId = attachmentList.getParentObjectId();

    org.exoplatform.social.core.identity.model.Identity userIdentity =
                                                                     identityManager.getIdentity(String.valueOf(userIdentityId));
    if (userIdentity == null || userIdentity.isDeleted() || !userIdentity.isEnable()) {
      throw new IllegalStateException("User with id " + userIdentityId + " isn't valid");
    }

    if (!hasEditPermission(userAclIdentity, objectType, objectId)) {
      throw new IllegalAccessException("User " + userAclIdentity.getUserId()
          + " doesn't have enough permissions to attach files on object " + objectType + "/" + objectId);
    }
    long spaceId = attachmentList.getSpaceId();

    ObjectAttachmentOperationReport report = new ObjectAttachmentOperationReport();
    uploadIds.stream()
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
                                                         spaceId,
                                                         userIdentityId,
                                                         report));
    return report;
  }

  @Override
  public ObjectAttachmentList getAttachments(String objectType,
                                             String objectId,
                                             Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException {
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("Object identifier is mandatory");
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

    List<String> fileIds = metadataService.getMetadataNamesByMetadataTypeAndObject(METADATA_TYPE.getName(), objectType, objectId);
    List<ObjectAttachmentDetail> attachments = fileIds.stream().map(fileId -> {
      FileInfo fileInfo = fileService.getFileInfo(Long.parseLong(fileId));
      if (fileInfo == null) {
        return null;
      } else {
        return new ObjectAttachmentDetail(String.valueOf(fileInfo.getId()),
                                          fileInfo.getName(),
                                          fileInfo.getMimetype(),
                                          fileInfo.getSize(),
                                          fileInfo.getUpdatedDate().getTime(),
                                          fileInfo.getUpdater());
      }
    }).toList();
    return new ObjectAttachmentList(attachments, objectType, objectId);
  }

  @Override
  public ObjectAttachmentDetail getAttachment(String objectType,
                                              String objectId,
                                              String fileId,
                                              Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException {
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("Object identifier is mandatory");
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

    FileInfo fileInfo = getAttachment(objectType, objectId, fileId);
    if (fileInfo == null) {
      return null;
    } else {
      return new ObjectAttachmentDetail(String.valueOf(fileInfo.getId()),
                                        fileInfo.getName(),
                                        fileInfo.getMimetype(),
                                        fileInfo.getSize(),
                                        fileInfo.getUpdatedDate().getTime(),
                                        fileInfo.getUpdater());
    }
  }

  @Override
  public InputStream getAttachmentInputStream(String objectType,
                                              String objectId,
                                              String fileId,
                                              String imageDimensions,
                                              Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException,
                                                                        IOException {
    if (StringUtils.isBlank(objectType)) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (StringUtils.isBlank(objectId)) {
      throw new IllegalArgumentException("Object identifier is mandatory");
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

    FileInfo fileInfo = getAttachment(objectType, objectId, fileId);
    if (fileInfo == null) {
      throw new IOException("Attached file with id " + fileId + " wasn't found");
    }
    FileItem fileItem;
    try {
      fileItem = fileService.getFile(Long.parseLong(fileId));
    } catch (FileStorageException e) {
      throw new IOException("Error reading file with id " + fileId, e);
    }
    InputStream attachmentInputStream;
    if (StringUtils.contains(fileInfo.getMimetype(), "image/")) {
      int[] dimension = Utils.parseDimension(imageDimensions);
      try {
        FileItem imageFileItem = imageThumbnailService.getOrCreateThumbnail(fileItem,
                                                                            userIdentity,
                                                                            dimension[0],
                                                                            dimension[1]);
        attachmentInputStream = imageFileItem.getAsStream();
      } catch (Exception e) {
        LOG.warn("Error while resizing attachment with Id {} for object {}/{}, original Image will be returned",
                 fileId,
                 objectType,
                 objectId,
                 e);
        attachmentInputStream = fileItem.getAsStream();
      }
    } else {
      attachmentInputStream = fileItem.getAsStream();
    }
    return attachmentInputStream;
  }

  @Override
  public boolean hasAccessPermission(Identity userIdentity, String objectType, String objectId) throws ObjectNotFoundException {
    AttachmentPlugin attachmentPermessionPlugin = this.attachmentPermissionPluginMap.get(objectType);
    return attachmentPermessionPlugin != null && attachmentPermessionPlugin.hasAccessPermission(userIdentity, objectId);
  }

  @Override
  public boolean hasEditPermission(Identity userIdentity, String objectType, String objectId) throws ObjectNotFoundException {
    AttachmentPlugin attachmentPermessionPlugin = this.attachmentPermissionPluginMap.get(objectType);
    return attachmentPermessionPlugin != null && attachmentPermessionPlugin.hasEditPermission(userIdentity, objectId);
  }

  private void createAttachment(UploadResource uploadResource,
                                String objectType,
                                String objectId,
                                String parentObjectId,
                                long spaceId,
                                long userIdentityId,
                                ObjectAttachmentOperationReport report) {
    String fileDiskLocation = uploadResource.getStoreLocation();
    String uploadId = uploadResource.getUploadId();
    try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
      FileItem fileItem = fileService.writeFile(new FileItem(null,
                                                             uploadResource.getFileName(),
                                                             uploadResource.getMimeType(),
                                                             FILE_API_NAMESPACE,
                                                             Double.doubleToLongBits(uploadResource.getUploadedSize()),
                                                             new Date(),
                                                             String.valueOf(userIdentityId),
                                                             false,
                                                             inputStream));
      long audienceId = spaceId > 0 ? spaceId : userIdentityId;
      MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(),
                                                String.valueOf(fileItem.getFileInfo().getId()),
                                                audienceId);
      MetadataObject object = new MetadataObject(objectType,
                                                 objectId,
                                                 parentObjectId,
                                                 spaceId);
      metadataService.createMetadataItem(object, metadataKey, userIdentityId);
    } catch (FileNotFoundException e) {
      LOG.warn("File with upload id " + uploadId + " doesn't exist", e);
      report.addError(uploadId, "attachment.uploadIdFileNotExistsError");
    } catch (IOException e) {
      LOG.warn("Error accessing resource with upload id " + uploadId, e);
      report.addError(uploadId, "attachment.uploadIdIOError");
    } catch (FileStorageException e) {
      LOG.warn("Error writing resource on database with upload id " + uploadId, e);
      report.addError(uploadId, "attachment.uploadIdStorageError");
    } catch (Exception e) {
      LOG.warn("Error attaching file with upload id " + uploadId, e);
      report.addError(uploadId, "attachment.uploadIdNotAttachedError");
    } finally {
      uploadService.removeUploadResource(uploadId);
    }
  }

  private FileInfo getAttachment(String objectType, String objectId, String fileId) {
    List<String> fileIds = metadataService.getMetadataNamesByMetadataTypeAndObject(METADATA_TYPE.getName(), objectType, objectId);
    FileInfo fileInfo = null;
    if (fileIds.contains(fileId)) {
      fileInfo = fileService.getFileInfo(Long.parseLong(fileId));
    }
    return fileInfo;
  }

}
