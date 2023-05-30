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

import java.util.Set;

public class FileAttachmentListener extends Listener<Set<String>, String> {

  private static final String FILE_DELETED_EVENT = "metadataItem.deleted";

  private FileService fileService;

  public FileAttachmentListener(FileService fileService) {
    this.fileService = fileService;
  }

  @Override
  public void onEvent(Event<Set<String>, String> event) throws Exception {
    String eventName = event.getEventName();
    if(eventName == FILE_DELETED_EVENT) {
      Set<String> fileIds = event.getSource();
      fileIds.forEach(fileId -> {
        fileService.deleteFile(Long.parseLong(fileId));
      });
    }
  }
}
