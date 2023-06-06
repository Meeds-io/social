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
package org.exoplatform.social.metadata.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.metadata.AttachmentPlugin;
import org.exoplatform.social.metadata.attachment.model.FileAttachmentResourceList;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.metadata.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.metadata.model.MetadataType;

public interface AttachmentService {

  public static final MetadataType METADATA_TYPE = new MetadataType(7, "attachments");

  /**
   * Attach a list of files to an entity: activity, comment, task...
   *
   * @param  attachment              {@link FileAttachmentResourceList} to store
   * @param  userAclIdentity         user ACL identity retrieved used
   *                                   {@link IdentityRegistry}
   * @return                         {@link ObjectAttachmentOperationReport}
   *                                 containing error codes/messages by uploadId
   * @throws IllegalAccessException  when user identified by its
   *                                   {@link org.exoplatform.social.core.identity.model.Identity}
   *                                   id doesn't have "write" permission of
   *                                   selected object
   * @throws ObjectNotFoundException when the object identified by its id in
   *                                   {@link FileAttachmentResourceList} doesn't
   *                                   exists
   */
  ObjectAttachmentOperationReport createAttachments(FileAttachmentResourceList attachment,
                                                    Identity userAclIdentity) throws IllegalAccessException,
                                                                              ObjectNotFoundException;

  /**
   * Updates attached files with its ids linked to a given object
   *
   * @param  attachment              {@link FileAttachmentResourceList} to store
   * @param  userAclIdentity         user ACL making the update
   * @throws ObjectNotFoundException when the object doesn't exist
   * @throws IllegalAccessException  when user identified by its
   *                                 {@link org.exoplatform.social.core.identity.model.Identity}
   *                                 id can't modify attachments
   */
  ObjectAttachmentOperationReport updateAttachments(FileAttachmentResourceList attachment,
                                                    Identity userAclIdentity) throws ObjectNotFoundException,
                                                                              IllegalAccessException;

  /**
   * Retrieves the list of attachments of a given object identified by its id
   *
   * @param  objectType              object type, can be of any type: activity,
   *                                   comment, notes...
   * @param  objectId                object technical unique identifier
   * @param  userAclIdentity         user ACL identity retrieved used
   *                                   {@link IdentityRegistry}
   * @return                         {@link ObjectAttachmentList} with the list
   *                                 of attached files. If no attached files, it
   *                                 will return an object containing empty list
   * @throws IllegalAccessException  when user identified by its
   *                                   {@link org.exoplatform.social.core.identity.model.Identity}
   *                                   id doesn't have "read" permission of
   *                                   selected object
   * @throws ObjectNotFoundException when the object identified by its id in
   *                                   {@link FileAttachmentResourceList} doesn't
   *                                   exists
   */
  ObjectAttachmentList getAttachments(String objectType,
                                      String objectId,
                                      Identity userAclIdentity) throws ObjectNotFoundException,
                                                                IllegalAccessException;

  /**
   * Retrieve an attached file to a dedicated object identified by its type and
   * id
   *
   * @param  objectType              object type, can be of any type: activity,
   *                                   comment, notes...
   * @param  objectId                object technical unique identifier
   * @param  fileId                  attachment file identifier
   * @param  userAclIdentity         user ACL identity retrieved used
   *                                   {@link IdentityRegistry}
   * @return                         {@link ObjectAttachmentDetail}
   *                                 corresponding to a given object, else null
   * @throws IllegalAccessException  when user identified by its
   *                                   {@link org.exoplatform.social.core.identity.model.Identity}
   *                                   id doesn't have "read" permission of
   *                                   selected object
   * @throws ObjectNotFoundException when the object identified by its id in
   *                                   {@link FileAttachmentResourceList} doesn't
   *                                   exists
   */
  ObjectAttachmentDetail getAttachment(String objectType,
                                       String objectId,
                                       String fileId,
                                       Identity userAclIdentity) throws ObjectNotFoundException,
                                                                 IllegalAccessException;


  /**
   * Retrieves the input stream of an attached file to a dedicated object
   * identified by its type and id
   *
   * @param  objectType              object type, can be of any type: activity,
   *                                   comment, notes...
   * @param  objectId                object technical unique identifier
   * @param  fileId                  attachment file identifier
   * @param  imageDimensions         applied only when mime type is of type
   *                                   image/*
   * @param  userAclIdentity         user ACL identity retrieved used
   *                                   {@link IdentityRegistry}
   * @return                         {@link InputStream} of an attached file
   * @throws IllegalAccessException  when user identified by its
   *                                   {@link org.exoplatform.social.core.identity.model.Identity}
   *                                   id doesn't have "read" permission of
   *                                   selected object
   * @throws ObjectNotFoundException when the object identified by its id in
   *                                   {@link FileAttachmentResourceList} doesn't
   *                                   exists
   * @throws IOException             when an error occurs while reading attached
   *                                   file content
   */
  InputStream getAttachmentInputStream(String objectType,
                                       String objectId,
                                       String fileId,
                                       String imageDimensions,
                                       Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException,
                                                                 IOException;

  /**
   * Checks whether the user have access permission to a given object identified
   * by its id
   *
   * @param  userAclIdentity         user ACL identity retrieved used
   *                                   {@link IdentityRegistry}
   * @param  objectType              object type, can be of any type: activity,
   *                                   comment, notes...
   * @param  objectId                object technical unique identifier
   * @return                         true if the user can have access to an
   *                                 entity.
   * @throws ObjectNotFoundException when the object identified by its id
   *                                   doesn't exists
   */
  boolean hasAccessPermission(Identity userAclIdentity, String objectType, String objectId) throws ObjectNotFoundException;

  /**
   * Checks whether the user have edit permission to a given object identified
   * by its id
   *
   * @param  userAclIdentity         user ACL identity retrieved used
   *                                   {@link IdentityRegistry}
   * @param  objectType              object type, can be of any type: activity,
   *                                   comment, notes...
   * @param  objectId                object technical unique identifier
   * @return                         true if the user can have access to an
   *                                 entity.
   * @throws ObjectNotFoundException when the object identified by its id
   *                                   doesn't exists
   */
  boolean hasEditPermission(Identity userAclIdentity, String objectType, String objectId) throws ObjectNotFoundException;

  /**
   * Add an Attachment Permission Plugin that will serve to make an ACL for a
   * given object type
   *
   * @param attachmentPermissionPlugin {@link AttachmentPlugin}
   */
  void addPlugin(AttachmentPlugin attachmentPermissionPlugin);

}
