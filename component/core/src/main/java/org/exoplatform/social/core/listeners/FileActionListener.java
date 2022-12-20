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
package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.thumbnail.model.ThumbnailObject;

public class FileActionListener extends Listener<FileInfo, Object> {

  private static final String   FILE_UPDATED_EVENT      = "file.updated";

  private static final String   FILE_DELETED_EVENT      = "file.deleted";

  private static final String   THUMBNAIL_METADATA_NAME = "thumbnail";

  private static final String   THUMBNAIL_OBJECT_TYPE   = "file";

  private final MetadataService metadataService;

  public FileActionListener(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public void onEvent(Event<FileInfo, Object> event) throws Exception {
    String eventName = event.getEventName();
    FileInfo fileInfo = event.getSource();
    if (eventName.equals(FILE_UPDATED_EVENT) || eventName.equals(FILE_DELETED_EVENT)) {
      ThumbnailObject thumbnailObject = new ThumbnailObject(THUMBNAIL_OBJECT_TYPE, Long.toString(fileInfo.getId()));
      metadataService.deleteMetadataItemsByMetadataTypeAndObject(THUMBNAIL_METADATA_NAME, thumbnailObject);
    }
  }
}
