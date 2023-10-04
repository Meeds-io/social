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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.metadata.model.MetadataItem;

public class AttachmentActivityProcessor extends BaseActivityProcessorPlugin {

  private AttachmentService attachmentService;

  public AttachmentActivityProcessor(AttachmentService attachmentService, InitParams params) {
    super(params);
    this.attachmentService = attachmentService;
  }

  public void processActivity(ExoSocialActivity activity) {
    if (activity != null && MapUtils.isNotEmpty(activity.getMetadatas())
        && CollectionUtils.isNotEmpty(activity.getMetadatas().get(AttachmentService.METADATA_TYPE.getName()))) {
      List<MetadataItem> attachmentMetadataItems = activity.getMetadatas().get(AttachmentService.METADATA_TYPE.getName());
      attachmentMetadataItems.forEach(metadataItem -> {
        String fileId = metadataItem.getMetadata().getName();
        String objectId = activity.getMetadataObjectId();
        String objectType = activity.getMetadataObjectType();
        ObjectAttachmentDetail attachment = attachmentService.getAttachment(objectType, objectId, fileId);
        if (attachment == null) {
          return;
        }

        Map<String, String> metadataItemProperties = metadataItem.getProperties();
        Map<String, String> properties = metadataItem.getMetadata().getProperties();
        if (properties == null) {
          properties = new HashMap<>();
          metadataItem.setProperties(properties);
        }
        if (metadataItemProperties != null) {
          properties.put("alt", metadataItemProperties.get("alt"));
        }
        properties.put("fileName", attachment.getName());
        properties.put("fileSize", String.valueOf(attachment.getSize()));
        properties.put("fileMimeType", attachment.getMimetype());
        properties.put("fileUpdateDate", String.valueOf(attachment.getUpdated()));

      });
    }
  }
}
