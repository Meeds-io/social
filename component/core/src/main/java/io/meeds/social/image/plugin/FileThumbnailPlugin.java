/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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

package io.meeds.social.image.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.commons.utils.CommonsUtils;

import io.meeds.portal.thumbnail.model.FileContent;
import io.meeds.portal.thumbnail.plugin.ImageThumbnailPlugin;

public class FileThumbnailPlugin extends ImageThumbnailPlugin {

  public static final String FILE_TYPE = "file";

  @Override
  public String getFileType() {
    return FILE_TYPE;
  }

  @Override
  public FileContent getImage(String fileId, String userName) throws ObjectNotFoundException {
    FileService fileService = CommonsUtils.getService(FileService.class);
    try {
      FileItem file = fileService.getFile(Long.parseLong(fileId));
      FileInfo fileInfo = file.getFileInfo();
      return new FileContent(Long.toString(fileInfo.getId()), fileInfo.getName(), fileInfo.getMimetype(), file.getAsStream());
    } catch (FileStorageException e) {
      throw new ObjectNotFoundException("Cannot find file with id" + fileId);
    }
  }
}
