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

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.services.thumbnail.ImageResizeService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.metadata.thumbnail.model.ThumbnailObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

public class ImageThumbnailServiceImplTest extends AbstractCoreTest {

  private MetadataService           metadataService;

  private FileService               fileService;

  private ImageResizeService        imageResizeService;

  private ImageThumbnailServiceImpl imageThumbnailService;

  private IdentityManager           identityManager;

  private Identity                  userIdentity;

  public void setUp() throws Exception {
    super.setUp();
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    fileService = getContainer().getComponentInstanceOfType(FileService.class);
    imageResizeService = getContainer().getComponentInstanceOfType(ImageResizeService.class);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    imageThumbnailService = new ImageThumbnailServiceImpl(metadataService, fileService, imageResizeService);
    userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
  }

  public void testGetOrCreateThumbnail() throws Exception {
    MetadataType metadataType = new MetadataType(5, "thumbnail");
    File file = new File(getClass().getClassLoader().getResource("test.png").getFile());
    byte[] content = Files.readAllBytes(file.toPath());
    FileItem fileThumbnail = new FileItem(null,
                                          "testAvatar",
                                          "image/png",
                                          "social",
                                          content.length,
                                          new Date(),
                                          userIdentity.getRemoteId(),
                                          false,
                                          new ByteArrayInputStream(content));
    fileThumbnail = fileService.writeFile(fileThumbnail);
    FileInfo fileInfo = fileThumbnail.getFileInfo();
    ThumbnailObject thumbnailObject = new ThumbnailObject("file", Long.toString(fileInfo.getId()));
    List<MetadataItem> metadataItemList = metadataService.getMetadataItemsByMetadataTypeAndObject(metadataType.getName(),
                                                                                                  thumbnailObject);
    assertEquals(0, metadataItemList.size());
    FileItem thumbnail = imageThumbnailService.getOrCreateThumbnail(fileThumbnail, userIdentity, 45, 45);
    assertNotNull(thumbnail);

    metadataItemList = metadataService.getMetadataItemsByMetadataTypeAndObject(metadataType.getName(),
            thumbnailObject);
    assertEquals(1, metadataItemList.size());

    thumbnail = imageThumbnailService.getOrCreateThumbnail(fileThumbnail, userIdentity, 45, 45);
    assertNotNull(thumbnail);
    metadataItemList = metadataService.getMetadataItemsByMetadataTypeAndObject(metadataType.getName(),
            thumbnailObject);
    assertEquals(1, metadataItemList.size());
  }
}
