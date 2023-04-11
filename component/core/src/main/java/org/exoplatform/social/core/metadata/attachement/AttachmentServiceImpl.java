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
package org.exoplatform.social.core.metadata.attachement;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import org.exoplatform.social.metadata.attachement.AttachmentService;
import org.exoplatform.social.metadata.attachement.model.Attachment;
import org.exoplatform.social.metadata.AttachmentPermissionPlugin;
import org.exoplatform.social.metadata.FavoriteACLPlugin;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.upload.UploadResource;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.upload.UploadService;

public class AttachmentServiceImpl implements AttachmentService {

  private static final String                           FILE_API_NAMESPACE         = "social";

  private final MetadataService                         metadataService;

  private final Map<String, AttachmentPermissionPlugin> attachmentPermissionPluginMap = new HashMap<>();

  private FileService                                   fileService;

  private UploadService                                 uploadService;

  public AttachmentServiceImpl(MetadataService metadataService, FileService fileService, UploadService uploadService) {
    this.metadataService = metadataService;
    this.fileService = fileService;
    this.uploadService = uploadService;
  }

  @Override
  public void attachFiles(Attachment attachment) throws Exception {
    if (attachment == null) {
      throw new IllegalArgumentException("Product is mandatory");
    }

    Set<String> uploadIds = new HashSet<>(attachment.getUploadIds());
    if (uploadIds != null && !uploadIds.isEmpty()) {
      StringBuilder fileIds = new StringBuilder();
      for (String uploadId : uploadIds) {
        UploadResource uploadedResource = uploadService.getUploadResource(uploadId);
        if (uploadedResource == null) {
          throw new IllegalStateException("Cannot attach uploaded file " + uploadId + ", it may not exist");
        }

        FileItem fileItem = null;
        String fileDiskLocation = uploadedResource.getStoreLocation();
        try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
          fileItem = fileService.writeFile(new FileItem(null,
                                                        uploadedResource.getFileName(),
                                                        uploadedResource.getMimeType(),
                                                        FILE_API_NAMESPACE,
                                                        Double.doubleToLongBits(uploadedResource.getUploadedSize()),
                                                        new Date(),
                                                        Long.toString(attachment.getUserIdentityId()),
                                                        false,
                                                        inputStream));
        }
        fileIds.append(fileItem.getFileInfo().getId()).append(",");
        uploadService.removeUploadResource(uploadId);
      }

      Map<String, String> mapFileIds = new HashMap<String, String>();
      mapFileIds.put("fileIds", fileIds.toString());
      long userIdentityId = attachment.getUserIdentityId();
      MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(),
                                                String.valueOf(attachment.getUserIdentityId()),
                                                attachment.getUserIdentityId());
      MetadataObject object = new MetadataObject(attachment.getObjectType(),
                                                 attachment.getObjectId(),
                                                 null,
                                                 attachment.getSpaceId());
      metadataService.createMetadataItem(object, metadataKey, mapFileIds, userIdentityId);
    }
  }
  
  @Override
  public boolean hasAccessPermission(Identity userIdentity, String objectType, String entityId) {
    AttachmentPermissionPlugin attachmentPermessionPlugin = this.attachmentPermissionPluginMap.get(objectType);
    if (attachmentPermessionPlugin != null) {
      return attachmentPermessionPlugin.hasAccessPermission(userIdentity, entityId);
    }
    return true;
  }
  
  @Override
  public void addAttachmentPermessionPlugin(AttachmentPermissionPlugin attachmentPermessionPlugin) {
    this.attachmentPermissionPluginMap.put(attachmentPermessionPlugin.getObjectType(), attachmentPermessionPlugin);
  }

}
