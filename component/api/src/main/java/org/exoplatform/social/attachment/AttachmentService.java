/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.attachment.model.*;
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
   * Creates an attachment associated with a specific object.
   *
   * @param objectType The type of the object to which the attachment belongs.
   * @param objectId The unique identifier of the object.
   * @param attachmentObject The attachment details, including metadata and file
   *          information.
   * @param userAclIdentity The identity acl of the user creating the attachment.
   * @return {@link ObjectAttachmentDetail}
   */
  ObjectAttachmentDetail createAttachment(String objectType,
                        String objectId,
                        FileAttachmentObject attachmentObject,
                        Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException;

  /**
   * Retrieves the list of attachments of a given object identified by its id
   *
   * @param objectType object type, can be of any type: activity, comment,
   *          notes...
   * @param objectId object technical unique identifier
   * @param userAclIdentity user ACL identity retrieved used
   *          {@link IdentityRegistry}
   * @return {@link ObjectAttachmentList} with the list of attached files. If no
   *         attached files, it will return an object containing empty list
   * @throws IllegalAccessException when user identified by its
   *           {@link org.exoplatform.social.core.identity.model.Identity} id
   *           doesn't have "read" permission of selected object
   * @throws ObjectNotFoundException when the object identified by its id in
   *           {@link FileAttachmentResourceList} doesn't exists
   */
  ObjectAttachmentList getAttachments(String objectType,
                                      String objectId,
                                      Identity userAclIdentity) throws ObjectNotFoundException, IllegalAccessException;
  
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
   * Retrieves a list of {@link ObjectAttachmentDetail} objects associated with a
   * specific object type and object ID, with metadata enrichment based on the
   * given user's access identity.The result is paginated based on the provided
   * offset and limit.
   * <p>
   * If no attachments are found, an empty {@link ObjectAttachmentList} is
   * returned. If the user does not have the necessary access permissions, an
   * {@link IllegalAccessException} will be thrown.
   * </p>
   *
   * @param objectType The type of the object associated with the attachments.
   * @param objectId The unique ID of the object associated with the attachments.
   * @param userAclIdentity The identity of the user requesting the attachments,
   *          used for access control checks.
   * @param offset The offset to be used for pagination (zero-based index).
   * @param limit The maximum number of items to return (for pagination).
   * @return {@link ObjectAttachmentList} A list of attachments associated with
   *         the specified object type and object ID, enriched with metadata
   *         (e.g., alt text and format) where applicable.
   * @throws ObjectNotFoundException If the object associated with the provided
   *           object ID does not exist.
   * @throws IllegalAccessException If the user does not have the necessary
   *           permissions to access the requested attachments.
   */
  ObjectAttachmentList getAttachments(String objectType,
                                      String objectId,
                                      Identity userAclIdentity,
                                      int offset,
                                      int limit) throws ObjectNotFoundException, IllegalAccessException;

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

  /**
   * Moves attachments from a specified source object type to a specified destination object type.
   *
   * @param sourceObjectType          the type of the source object
   * @param sourceObjectId            the ID of the source object
   * @param destinationObjectType     the type of the destination object
   * @param destinationObjectId       the ID of the destination object
   * @param destinationParentObjectId the ID of the destination's parent object
   * @param userIdentityId            the ID of the user performing the operation
   */
  void moveAttachments(String sourceObjectType, String sourceObjectId, String destinationObjectType, String destinationObjectId, String destinationParentObjectId, long userIdentityId);

  /**
   * Creates an attachment for a specified object.
   * 
   * @param fileId the identifier of the file to be attached
   * @param objectType the type of the object
   * @param objectId the identifier of the object to which the attachment is being
   *          added.
   * @param parentObjectId the parent object identifier
   * @param userIdentityId user
   *          {@link org.exoplatform.social.core.identity.model.Identity} id
   * @param properties the attachment properties
   * @throws ObjectNotFoundException when the object identified by its id doesn't
   *           exist
   * @throws ObjectAlreadyExistsException when attachment already exists for given
   *           object
   */
  void createAttachment(String fileId,
                        String objectType,
                        String objectId,
                        String parentObjectId,
                        long userIdentityId,
                        Map<String, String> properties) throws ObjectNotFoundException, ObjectAlreadyExistsException;
}
