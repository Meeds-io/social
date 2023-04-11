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
package org.exoplatform.social.metadata.attachement;

import org.exoplatform.social.metadata.AttachmentPermissionPlugin;
import org.exoplatform.social.metadata.attachement.model.Attachment;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.services.security.Identity;

public interface AttachmentService {

  public static final MetadataType METADATA_TYPE=new MetadataType(7, "attachement");

  /**
   * Attach a list of files to an entity: activity,comment, task...
   *
   * @param attachment
   * @throws Exception
   */
  public void attachFiles(Attachment attachment) throws Exception;


  /**
   * Checks whether the user have access to an entity
   *
   * @param userIdentity user identity
   * @param entityType   object type, can be of any type: activity, comment,
   *                     notes...
   * @param entityId     object technical unique identifier
   * @return true if the user can have access to an entity.
   */
  default boolean hasAccessPermission(Identity userIdentity, String objectType, String entityId) {
    throw new UnsupportedOperationException();
  }

  /**
   * Add a Attachment Permission Plugin
   */
  default void addAttachmentPermessionPlugin(AttachmentPermissionPlugin attachmentPermissionPlugin) {
    throw new UnsupportedOperationException();
  }

}
