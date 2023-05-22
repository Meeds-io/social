/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.metadata.attachment.AttachmentService;
import org.exoplatform.social.metadata.model.MetadataItem;

public class AttachmentActivityProcessor extends BaseActivityProcessorPlugin {
  private FileService fileService;

  public AttachmentActivityProcessor(FileService fileService, InitParams params) {
    super(params);
    this.fileService = fileService;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null && MapUtils.isNotEmpty(activity.getMetadatas())
        && CollectionUtils.isNotEmpty(activity.getMetadatas().get(AttachmentService.METADATA_TYPE.getName()))) {
      List<MetadataItem> attachmentMetadataItems = activity.getMetadatas().get(AttachmentService.METADATA_TYPE.getName());
      attachmentMetadataItems.forEach(metadataItem -> {
        long fileId = Long.parseLong(metadataItem.getMetadata().getName());
        FileInfo fileInfo = fileService.getFileInfo(fileId);
        Map<String, String> properties = metadataItem.getMetadata().getProperties();
        if (MapUtils.isEmpty(properties)) {
          properties = new HashMap<>();
          metadataItem.setProperties(properties);
        }
        properties.put("fileName", fileInfo.getName());
        properties.put("fileSize", String.valueOf(fileInfo.getSize()));
        properties.put("fileMimeType", fileInfo.getMimetype());
        properties.put("fileUpdateDate", String.valueOf(fileInfo.getUpdatedDate().getTime()));
      });
    }
  }
}
