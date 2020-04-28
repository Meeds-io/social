/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;

import java.io.IOException;

/**
 * Mock for FileService
 */
public class MockFileService implements FileService {
  @Override
  public FileInfo getFileInfo(long l) {
    return null;
  }

  @Override
  public FileItem getFile(long l) throws FileStorageException {
    return null;
  }

  @Override
  public FileItem writeFile(FileItem fileItem) throws FileStorageException, IOException {
    return null;
  }

  @Override
  public FileItem updateFile(FileItem fileItem) throws FileStorageException, IOException {
    return null;
  }

  @Override
  public FileInfo deleteFile(long l) {
    return null;
  }
}
