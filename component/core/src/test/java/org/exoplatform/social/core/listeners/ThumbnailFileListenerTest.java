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
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.thumbnail.ImageThumbnailService;
import org.exoplatform.social.metadata.thumbnail.model.ThumbnailObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.ByteArrayInputStream;
import java.util.Date;


import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ThumbnailFileListenerTest {

  @Mock
  private ImageThumbnailService imageThumbnailService;
  
  private ThumbnailFileListener thumbnailFileListener;

  @Before
  public void setUp() throws Exception {
    thumbnailFileListener = new ThumbnailFileListener(imageThumbnailService);
  }

  @Test
  public void testOnEvent() throws Exception {
    ThumbnailObject thumbnailObject = new ThumbnailObject("file", "1");
    FileItem fileItem = new FileItem(1L,
                                     "test",
                                     "image/png",
                                     "social",
                                     "test".getBytes().length,
                                     new Date(),
                                     "user",
                                     false,
                                     new ByteArrayInputStream("test".getBytes()));
    Event<FileInfo, Object> updateFile = new Event<>("file.updated", fileItem.getFileInfo(), null);
    thumbnailFileListener.onEvent(updateFile);
    verify(imageThumbnailService, times(1)).deleteThumbnails(1L);
    Event<FileInfo, Object> deleteFile = new Event<>("file.deleted", fileItem.getFileInfo(), null);
    thumbnailFileListener.onEvent(deleteFile);
    verify(imageThumbnailService, times(2)).deleteThumbnails(1L);
  }
}
