/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.metadata.thumbnail;

import org.apache.commons.io.IOUtils;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.thumbnail.ImageResizeService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.metadata.thumbnail.ImageThumbnailService;
import org.exoplatform.social.metadata.thumbnail.model.ThumbnailObject;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageThumbnailServiceImpl implements ImageThumbnailService {

  private static final Log          LOG                       = ExoLogger.getExoLogger(ImageThumbnailServiceImpl.class);

  private static final MetadataType THUMBNAIL_METADATA_TYPE   = new MetadataType(5, "thumbnail");

  private static final String       SOCIAL_NAME_SPACE         = "social";

  private static final String       THUMBNAIL_OBJECT_TYPE     = "file";

  private static final String       THUMBNAIL_WIDTH_PROPERTY  = "width";

  private static final String       THUMBNAIL_HEIGHT_PROPERTY = "height";

  private final MetadataService     metadataService;

  private final FileService         fileService;

  private final ImageResizeService  imageResizeService;

  public ImageThumbnailServiceImpl(MetadataService metadataService,
                                   FileService fileService,
                                   ImageResizeService imageResizeService) {
    this.metadataService = metadataService;
    this.fileService = fileService;
    this.imageResizeService = imageResizeService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public FileItem getOrCreateThumbnail(FileItem file, Identity identity, int width, int height) throws Exception {
    if (file == null) {
      throw new IllegalArgumentException("file argument is mandatory");
    }
    if (width == 0 && height == 0) {
      return file;
    }
    FileInfo fileInfo = file.getFileInfo();
    ThumbnailObject thumbnailObject = new ThumbnailObject(THUMBNAIL_OBJECT_TYPE, Long.toString(fileInfo.getId()));
    List<MetadataItem> metadataItemList = metadataService.getMetadataItemsByMetadataTypeAndObject(THUMBNAIL_METADATA_TYPE.getName(),
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
        LOG.warn("Error while getting thumbnail for image of identity {}, original Image will be returned", identity.getId(), e.getMessage());
        return file;
      }
    } else {
      byte[] imageContent = imageResizeService.scaleImage(IOUtils.toByteArray(file.getAsStream()), width, height, false, false);
      FileItem thumbnail = new FileItem(null,
                                        fileInfo.getName(),
                                        fileInfo.getMimetype(),
                                        SOCIAL_NAME_SPACE,
                                        imageContent.length,
                                        new Date(),
                                        fileInfo.getUpdater(),
                                        false,
                                        new ByteArrayInputStream(imageContent));
      FileItem thumbnailFileItem = fileService.writeFile(thumbnail);
      FileInfo thumbnailFileInfo = thumbnailFileItem.getFileInfo();
      ThumbnailObject thumbnailMetadataObject = new ThumbnailObject(THUMBNAIL_OBJECT_TYPE,
                                                                    Long.toString(fileInfo.getId()),
                                                                    Long.toString(thumbnailFileInfo.getId()));
      MetadataKey metadataKey = new MetadataKey(THUMBNAIL_METADATA_TYPE.getName(), THUMBNAIL_METADATA_TYPE.getName(), 0);
      Map<String, String> properties = new HashMap<>();
      properties.put(THUMBNAIL_WIDTH_PROPERTY, String.valueOf(width));
      properties.put(THUMBNAIL_HEIGHT_PROPERTY, String.valueOf(height));
      metadataService.createMetadataItem(thumbnailMetadataObject, metadataKey, properties, Long.parseLong(identity.getId()));
      return thumbnailFileItem;
    }
  }

  @Override
  public void deleteThumbnails(Long fileId) {
    ThumbnailObject thumbnailObject = new ThumbnailObject(THUMBNAIL_OBJECT_TYPE, Long.toString(fileId));
    metadataService.deleteMetadataItemsByMetadataTypeAndObject(THUMBNAIL_METADATA_TYPE.getName(), thumbnailObject);
  }
}
