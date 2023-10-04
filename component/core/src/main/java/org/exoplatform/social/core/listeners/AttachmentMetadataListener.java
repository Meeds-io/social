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

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

@Asynchronous
public class AttachmentMetadataListener extends Listener<Long, MetadataItem> {

  private AttachmentService attachmentService;

  public AttachmentMetadataListener(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @Override
  @ExoTransactional
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    MetadataItem metadataItem = event.getData();
    if (AttachmentService.METADATA_TYPE.equals(metadataItem.getMetadata().getType())) {
      MetadataObject object = metadataItem.getObject();
      String fileId = metadataItem.getMetadata().getName();
      attachmentService.deleteAttachment(object.getType(), object.getId(), fileId);
    }
  }

}
