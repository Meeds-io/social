/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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

import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.model.MetadataItem;

import java.util.List;

public class FileAttachmentListener extends Listener<Long,List<MetadataItem>> {

  private FileService fileService;

  public FileAttachmentListener(FileService fileService) {
    this.fileService = fileService;
  }

  @Override
  public void onEvent(Event<Long, List<MetadataItem>> event) throws Exception {
    List<MetadataItem> metadataItems = event.getData();
    metadataItems.forEach(metadataItem -> {
      if (metadataItem.getMetadata().getType().getName().equals(AttachmentService.METADATA_TYPE.getName())) {
        fileService.deleteFile(Long.parseLong(metadataItem.getMetadata().getName()));
      }
    });
  }
}
