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
package org.exoplatform.social.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.attachment.model.FileAttachmentResourceList;
import org.exoplatform.social.attachment.model.ObjectAttachmentDetail;
import org.exoplatform.social.attachment.model.ObjectAttachmentList;
import org.exoplatform.social.attachment.model.ObjectAttachmentOperationReport;
import org.exoplatform.social.attachment.model.UploadedAttachmentDetail;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.model.MetadataType;

public interface AttachmentService {

  public static final MetadataType METADATA_TYPE             = new MetadataType(7, "attachments");

  public static final String       ATTACHMENT_CREATED_EVENT  = "attachment.created";

  public static final String       ATTACHMENT_DELETED_EVENT  = "attachment.deleted";

  public static final String       ATTACHMENTS_UPDATED_EVENT = "attachments.updated";

  public static final String       ATTACHMENTS_DELETED_EVENT = "attachments.deleted";

  /**
   * Makes an update of attached files to an object (activity, comment, task...)
   * by adding newly uploaded files and deletes attachments not listed in
   * attached fileIds
   *
   * @param  attachment              {@link FileAttachmentResourceList} to store
   * @param  userAclIdentity         user ACL making the update
   * @return                         {@link ObjectAttachmentOperationReport}
   * @throws ObjectNotFoundException when the object identified by its id in
   *                                   {@link FileAttachmentResourceList}
   *                                   doesn't exists
   * @throws IllegalAccessException  when user doesn't have "write" permission
   *                                   on selected object
   */
  ObjectAttachmentOperationReport saveAttachments(FileAttachmentResourceList attachment,
                                                  Identity userAclIdentity) throws ObjectNotFoundException,
                                                                            IllegalAccessException;

  /**
   * Makes an update of attached files to an object (activity, comment, task...)
   * by adding newly uploaded files and deletes attachments not listed in
   * attached fileIds
   *
   * @param  attachment {@link FileAttachmentResourceList} to store
   * @return            {@link ObjectAttachmentOperationReport}
   */
  ObjectAttachmentOperationReport saveAttachments(FileAttachmentResourceList attachment);

  /**
   * @param uploadedAttachmentDetail {@link UploadedAttachmentDetail} object to attach
   * @param objectType Object type
   * @param objectId Object identifier
   * @param parentObjectId Parent object identifier
   * @param userIdentityId User {@link org.exoplatform.social.core.identity.model.Identity} id
   * @throws IOException when an error occurred while accessing uploaded resource
   * @throws ObjectAlreadyExistsException when attachment already exists for given object
   * @throws ObjectNotFoundException when attachment not found
   */
  void saveAttachment(UploadedAttachmentDetail uploadedAttachmentDetail,
                      String objectType,
                      String objectId,
                      String parentObjectId,
                      long userIdentityId) throws IOException, ObjectAlreadyExistsException, ObjectNotFoundException;

  /**
   * Delete attachments of a given object identified by its type and id
   * 
   * @param objectType object type, can be of any type: activity, comment,
   *                     notes...
   * @param objectId   object technical unique identifier
   */
  void deleteAttachments(String objectType, String objectId);

  /**
   * Delete attachment of a given object identified by its type and id
   * 
   * @param objectType object type, can be of any type: activity, comment,
   *                     notes...
   * @param objectId   object technical unique identifier
   * @param fileId     attachment file identifier
   */
  void deleteAttachment(String objectType, String objectId, String fileId);

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
   *                                   {@link FileAttachmentResourceList}
   *                                   doesn't exists
   */
  ObjectAttachmentList getAttachments(String objectType,
                                      String objectId,
                                      Identity userAclIdentity) throws ObjectNotFoundException,
                                                                IllegalAccessException;

  /**
   * Retrieves the list of attachments of a given object identified by its id
   *
   * @param  objectType object type, can be of any type: activity, comment,
   *                      notes...
   * @param  objectId   object technical unique identifier
   * @return            {@link ObjectAttachmentList} with the list of attached
   *                    files. If no attached files, it will return an object
   *                    containing empty list
   */
  ObjectAttachmentList getAttachments(String objectType,
                                      String objectId);

  /**
   * @param objectType object type, can be of any type: activity, comment,
   *          notes...
   * @param objectId object technical unique identifier
   * @param userAclIdentity user ACL identity retrieved used
   *          {@link IdentityRegistry}
   * @return {@link List} of attached {@link FileInfo} id
   * @throws IllegalAccessException  when user identified by its
   *                                   {@link org.exoplatform.social.core.identity.model.Identity}
   *                                   id doesn't have "read" permission of
   *                                   selected object
   * @throws ObjectNotFoundException when the object identified by its id doesn't exists
   */
  List<String> getAttachmentFileIds(String objectType, String objectId, Identity userAclIdentity) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * @param objectType object type, can be of any type: activity, comment,
   *          notes...
   * @param objectId object technical unique identifier
   * @return {@link List} of attached {@link FileInfo} id
   */
  List<String> getAttachmentFileIds(String objectType, String objectId);

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
   *                                   {@link FileAttachmentResourceList}
   *                                   doesn't exists
   */
  ObjectAttachmentDetail getAttachment(String objectType,
                                       String objectId,
                                       String fileId,
                                       Identity userAclIdentity) throws ObjectNotFoundException,
                                                                 IllegalAccessException;

  /**
   * Retrieve an attached file to a dedicated object identified by its type and
   * id
   *
   * @param  objectType      object type, can be of any type: activity, comment,
   *                           notes...
   * @param  objectId        object technical unique identifier
   * @param  fileId          attachment file identifier
   * @return                 {@link ObjectAttachmentDetail} corresponding to a
   *                         given object, else null
   */
  ObjectAttachmentDetail getAttachment(String objectType,
                                       String objectId,
                                       String fileId);

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
   *                                   {@link FileAttachmentResourceList}
   *                                   doesn't exists
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

  /**
   * @return the {@link List} of supported object types
   */
  Set<String> getSupportedObjectTypes();

  /**
   * @return the {@link Map} of supported Attachment plugin
   */
  Map<String, AttachmentPlugin> getAttachmentPlugins();

}
