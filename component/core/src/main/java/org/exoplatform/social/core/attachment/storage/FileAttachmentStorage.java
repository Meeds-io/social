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
package org.exoplatform.social.core.attachment.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.thumbnail.ImageThumbnailService;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.attachment.model.ObjectAttachmentId;
import org.exoplatform.social.common.Utils;

import io.meeds.social.image.plugin.FileThumbnailPlugin;

public class FileAttachmentStorage {

  private static final Log      LOG                = ExoLogger.getLogger(FileAttachmentStorage.class);

  private static final String   FILE_API_NAMESPACE = "attachment";

  private FileService           fileService;

  private ImageThumbnailService imageThumbnailService;

  public FileAttachmentStorage(FileService fileService, ImageThumbnailService imageThumbnailService) {
    this.fileService = fileService;
    this.imageThumbnailService = imageThumbnailService;
  }

  public String copyAttachment(ObjectAttachmentId sourceAttachmentId, long userIdentityId) {
    try {
      FileItem sourceFile = fileService.getFile(Long.parseLong(sourceAttachmentId.getFileId()));
      if (sourceFile == null) {
        return null;
      }
      FileItem newFileItem =  new FileItem(null,
                                            sourceFile.getFileInfo().getName(),
                                            sourceFile.getFileInfo().getMimetype(),
                                            FILE_API_NAMESPACE,
                                            sourceFile.getFileInfo().getSize(),
                                            new Date(),
                                            String.valueOf(userIdentityId),
                                           false,
                                            sourceFile.getAsStream());
      newFileItem = fileService.writeFile(newFileItem);
      return String.valueOf(newFileItem.getFileInfo().getId());
    } catch (Exception e) {
      LOG.error("Unable to copy attachment with id " + sourceAttachmentId, e);
      return null;
    }

  }


  public String uploadAttachment(Long attachmentId,
                                 String objectType,
                                 String objectId,
                                 String fileName,
                                 String fileMimeType,
                                 InputStream fileInputStream,
                                 long userIdentityId) throws IOException {

    try {
      FileItem fileItem =  new FileItem(attachmentId,
                                        fileName,
                                        fileMimeType,
                                        FILE_API_NAMESPACE,
                                        fileInputStream.available(),
                                        new Date(),
                                        String.valueOf(userIdentityId),
                                        false,
                                        fileInputStream);
      if(attachmentId == null) {
        fileItem = fileService.writeFile(fileItem);
      }
      else {
        fileItem = fileService.updateFile(fileItem);
      }

      return String.valueOf(fileItem.getFileInfo().getId());
    } catch (Exception e) {
      throw new IOException("Error attaching file " + fileName + " to object " + objectType + "/" + objectId, e);
    }
  }

  public ObjectAttachmentDetail getAttachment(ObjectAttachmentId attachmentId) {
    FileInfo fileInfo = fileService.getFileInfo(Long.parseLong(attachmentId.getFileId()));
    if (fileInfo != null && !fileInfo.isDeleted()) {
      return new ObjectAttachmentDetail(String.valueOf(fileInfo.getId()),
                                        fileInfo.getName(),
                                        fileInfo.getMimetype(),
                                        fileInfo.getSize(),
                                        fileInfo.getUpdatedDate().getTime(),
                                        fileInfo.getUpdater());
    }
    return null;
  }

  public InputStream getAttachmentInputStream(ObjectAttachmentId attachmentId, String imageDimensions) throws IOException {
    long fileId = Long.parseLong(attachmentId.getFileId());
    FileInfo fileInfo = fileService.getFileInfo(fileId);
    if (fileInfo == null) {
      throw new IOException("Attached file with id " + attachmentId + " wasn't found");
    }
    FileItem fileItem;
    try {
      fileItem = fileService.getFile(fileId);
    } catch (FileStorageException e) {
      throw new IOException("Error reading file with id " + fileId, e);
    }
    if (StringUtils.contains(fileInfo.getMimetype(), "image/") && StringUtils.isNotBlank(imageDimensions)) {
      int[] dimension = Utils.parseDimension(imageDimensions);
      if (dimension[0] == 0 || dimension[1] == 0) return fileItem.getAsStream();
      try {
        FileItem imageFileItem = imageThumbnailService.getOrCreateThumbnail(FileThumbnailPlugin.FILE_TYPE,
                Long.toString(fileId),
                fileInfo.getUpdater(),
                                                                            dimension[0],
                                                                            dimension[1]);
        return imageFileItem != null ? imageFileItem.getAsStream() : fileItem.getAsStream();
      } catch (Exception e) {
        LOG.warn("Error while resizing attachment with Id {}, original Image will be returned",
                 fileId,
                 e);
        return fileItem.getAsStream();
      }
    } else {
      return fileItem.getAsStream();
    }
  }

  public void deleteAttachment(ObjectAttachmentId objectAttachmentId) throws IOException {
    long fileId = Long.parseLong(objectAttachmentId.getFileId());
    try {
      FileItem fileItem = fileService.getFile(fileId);
      if (fileItem != null) {
        fileService.deleteFile(fileId);
      }
    } catch (FileStorageException e) {
      throw new IOException("Error deleting attached file " + objectAttachmentId, e);
    }
  }

}
