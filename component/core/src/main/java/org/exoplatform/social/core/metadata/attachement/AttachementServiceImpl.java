/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.metadata.attachement;

import org.exoplatform.social.metadata.attachement.AttachementService;
import org.exoplatform.social.metadata.attachement.model.Attachement;
import org.exoplatform.social.metadata.attachement.model.AttachementObject;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.ObjectAlreadyExistsException;

public class AttachementServiceImpl implements AttachementService {

  private final MetadataService metadataService;

  public AttachementServiceImpl(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public void attachFiles(Attachement attachment) throws ObjectAlreadyExistsException {
    long userIdentityId = attachment.getUserIdentityId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(), String.valueOf(userIdentityId), userIdentityId);
    AttachementObject object = attachment.getObject();
    metadataService.createMetadataItem(object, metadataKey, userIdentityId);
  }

}
