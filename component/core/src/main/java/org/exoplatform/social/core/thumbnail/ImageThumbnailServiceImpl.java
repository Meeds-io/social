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
package org.exoplatform.social.core.thumbnail;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.thumbnail.ImageResizeService;
import org.exoplatform.services.thumbnail.ImageThumbnailService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.metadata.thumbnail.model.ThumbnailObject;

import io.meeds.portal.thumbnail.model.FileContent;
import io.meeds.portal.thumbnail.plugin.ImageThumbnailPlugin;
import io.meeds.social.image.plugin.FileThumbnailPlugin;

public class ImageThumbnailServiceImpl implements ImageThumbnailService {

  private static final Log LOG = ExoLogger.getExoLogger(ImageThumbnailService.class);

  private static final MetadataType THUMBNAIL_METADATA_TYPE   = new MetadataType(5, "thumbnail");

  private static final String       SOCIAL_NAME_SPACE         = "social";

  private static final String       THUMBNAIL_OBJECT_TYPE     = "file";

  private static final String       THUMBNAIL_WIDTH_PROPERTY  = "width";

  private static final String       THUMBNAIL_HEIGHT_PROPERTY = "height";

  private final MetadataService     metadataService;

  private final FileService         fileService;

  private final ImageResizeService  imageResizeService;

  private static final Map<String, ImageThumbnailPlugin> imageThumbnailPlugins = new HashMap<>();

  public ImageThumbnailServiceImpl(MetadataService metadataService,
                                   FileService fileService,
                                   ImageResizeService imageResizeService) {
    this.metadataService = metadataService;
    this.fileService = fileService;
    this.imageResizeService = imageResizeService;
  }

  @Override
  public void addPlugin(ImageThumbnailPlugin imageThumbnailPlugin) {
    imageThumbnailPlugins.put(imageThumbnailPlugin.getFileType(), imageThumbnailPlugin);
  }

  @Override
  public void removePlugin(String fileType) {
    imageThumbnailPlugins.remove(fileType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FileItem getOrCreateThumbnail(FileItem file, int width, int height) throws Exception {
    if (file == null) {
      throw new IllegalArgumentException("file argument is mandatory");
    }
    if (width == 0 && height == 0) {
      return file;
    }
    FileInfo fileInfo = file.getFileInfo();
    try {
      return getOrCreateThumbnail(null, file, width, height);
    } catch (FileStorageException e) {
      LOG.warn("Error while getting thumbnail for image with file Id {}, original Image will be returned",
              fileInfo.getId(), e);
      return file;
    }
  }

  @Override
  public FileItem getOrCreateThumbnail(ImageResizeService resizeSupplier, FileItem file, int width, int height) throws Exception {
    if (file == null) {
      throw new IllegalArgumentException("file argument is mandatory");
    }
    if (width == 0 && height == 0) {
      return file;
    }
    if (resizeSupplier == null) {
      resizeSupplier = imageResizeService;
    }
    FileInfo fileInfo = file.getFileInfo();
    FileItem thumbnail = getThumbnail(Long.toString(fileInfo.getId()), width, height);
    if (thumbnail != null) {
      return thumbnail;
    } else {
      FileContent fileContent = new FileContent(Long.toString(fileInfo.getId()), fileInfo.getName(), fileInfo.getMimetype(), file.getAsStream());
      return createThumbnail(resizeSupplier,
              Long.toString(fileInfo.getId()),
              fileContent,
              file.getFileInfo().getUpdater(),
              width,
              height);
    }
  }

  @Override
  public FileItem getOrCreateThumbnail(String fileType, String id, String userName, int width, int height) throws Exception {
    ImageThumbnailPlugin imageThumbnailPlugin = imageThumbnailPlugins.get(fileType);
    if (!imageThumbnailPlugin.hasAccessPermission(id, userName)) {
      throw new IllegalArgumentException("Error checking access rights for on document'" + id + "' fro user " + userName);
    }
    String thumnailId = fileType.equals(FileThumbnailPlugin.FILE_TYPE) ? id : fileType + id;
    FileItem thumbnail = getThumbnail(thumnailId, width, height);
    if (thumbnail != null) {
      return thumbnail;
    } else {
      if (imageThumbnailPlugin != null) {
        FileContent fileContent = imageThumbnailPlugin.getImage(id, userName);
        if (fileContent != null) {
          return createThumbnail(imageResizeService, thumnailId, fileContent, userName, width, height);
        }
        return null;
      }
    }
    return null;
  }

  @Override
  public FileItem getThumbnail(String id, int width, int height) throws Exception {
    ThumbnailObject thumbnailObject = new ThumbnailObject(THUMBNAIL_OBJECT_TYPE, id);
    List<MetadataItem> metadataItemList =
                                        metadataService.getMetadataItemsByMetadataTypeAndObject(THUMBNAIL_METADATA_TYPE.getName(),
                                                                                                thumbnailObject);
    List<MetadataItem> items = metadataItemList.stream()
                                               .filter(metadataItem -> metadataItem.getProperties() != null
                                                       && metadataItem.getProperties()
                                                       .get(THUMBNAIL_WIDTH_PROPERTY)
                                                       .equals(String.valueOf(width))
                                                       && metadataItem.getProperties()
                                                       .get(THUMBNAIL_HEIGHT_PROPERTY)
                                                       .equals(String.valueOf(height)))
                                               .toList();
    if (!items.isEmpty()) {
      long fileId = Long.parseLong(items.get(0).getParentObjectId());
      try {
        return fileService.getFile(fileId);
      } catch (FileStorageException e) {
        LOG.warn("Error while getting thumbnail for image with file Id {}, original Image will be returned",
                 fileId,
                 e.getMessage());
        return null;
      }
    }
    return null;
  }

  @Override
  public FileItem createThumbnail(String id, FileContent fileContent, String userName, int width, int height) throws Exception {
    return createThumbnail(imageResizeService, id, fileContent, userName, width, height);
  }

  private FileItem createThumbnail(ImageResizeService resizeSupplier,
                                   String id,
                                   FileContent fileContent,
                                   String userName,
                                   int width,
                                   int height) throws Exception {
    byte[] imageContent = resizeSupplier.scaleImage(IOUtils.toByteArray(fileContent.getContent()), width, height, false, false);
    FileItem thumbnail = new FileItem(null,
            fileContent.getName(),
            fileContent.getMimeType(),
            SOCIAL_NAME_SPACE,
            imageContent.length,
            new Date(),
            userName,
            false,
            new ByteArrayInputStream(imageContent));
    FileItem thumbnailFileItem = fileService.writeFile(thumbnail);
    FileInfo thumbnailFileInfo = thumbnailFileItem.getFileInfo();
    ThumbnailObject thumbnailMetadataObject = new ThumbnailObject(THUMBNAIL_OBJECT_TYPE,
            id,
            Long.toString(thumbnailFileInfo.getId()));
    MetadataKey metadataKey = new MetadataKey(THUMBNAIL_METADATA_TYPE.getName(), THUMBNAIL_METADATA_TYPE.getName(), 0);
    Map<String, String> properties = new HashMap<>();
    properties.put(THUMBNAIL_WIDTH_PROPERTY, String.valueOf(width));
    properties.put(THUMBNAIL_HEIGHT_PROPERTY, String.valueOf(height));
    metadataService.createMetadataItem(thumbnailMetadataObject, metadataKey, properties);
    return thumbnailFileItem;
  }

  @Override
  public void deleteThumbnails(Long fileId) {
    deleteThumbnails(FileThumbnailPlugin.FILE_TYPE, Long.toString(fileId));
  }

  @Override
  public void deleteThumbnails(String fileType, String fileId) {
    String thumnailId = fileType.equals(FileThumbnailPlugin.FILE_TYPE) ? fileId : fileType + fileId;
    ThumbnailObject thumbnailObject = new ThumbnailObject(THUMBNAIL_OBJECT_TYPE, thumnailId);
    metadataService.deleteMetadataItemsByMetadataTypeAndObject(THUMBNAIL_METADATA_TYPE.getName(), thumbnailObject);
  }
}
