/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
package org.exoplatform.social.metadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

/**
 * A Service to manage Metadata of any content of any type in eXo Platform. This
 * Service will allow to store additional Metadata to contents without defining
 * additional Services or Storage layers to allow to extends content definition
 */
public interface MetadataService {

  /**
   * Creates a new {@link Metadata} object
   * 
   * @param  metadata       {@link Metadata}
   * @param  userIdentityId {@link Identity} identifier of the creator
   * @return                created {@link Metadata}
   */
  Metadata createMetadata(Metadata metadata, long userIdentityId);

  /**
   * Updates a {@link Metadata} object
   *
   * @param  metadata       {@link Metadata}
   * @param  userIdentityId {@link Identity} identifier of the creator
   * @return                updated {@link Metadata}
   */
  Metadata updateMetadata(Metadata metadata, long userIdentityId);

  /**
   * Deletes a {@link Metadata} by a given {@link Metadata} identifier
   * 
   * @param  metadataId {@link Metadata} technical identifier
   * @return            Deleted {@link Metadata}
   */
  Metadata deleteMetadataById(long metadataId);

  /**
   * Deletes all {@link MetadataItem} by a given {@link Space} identifier
   * 
   * @param  spaceId {@link Space} technical identifier
   * @return         deleted items count
   */
  int deleteMetadataBySpaceId(long spaceId);

  /**
   * Deletes all {@link MetadataItem} by a given {@link Space} identifier and
   * audience {@link Identity} identifier
   * 
   * @param  spaceId
   * @param  audienceId
   * @return            deleted items count
   */
  int deleteMetadataBySpaceIdAndAudienceId(long spaceId, long audienceId);

  /**
   * Retrieves a {@link Metadata} identified by a unique constraint for
   * 'Metadata Type', 'Metadata Name' and 'Metadata Audience'.
   * 
   * @param  metadataKey {@link MetadataKey} that contains {@link MetadataType}
   *                       name {@link Metadata} name and {@link Metadata}
   *                       audience
   * @return             {@link Metadata} if found, else null
   */
  Metadata getMetadataByKey(MetadataKey metadataKey);

  /**
   * Creates a new Metadata Item. When the metadata with the designated key
   * doesn't exists, it will create a new one
   * 
   * @param  metadataObject               object to store
   * @param  metadataKey                  {@link MetadataKey} that contains
   *                                        {@link MetadataType} name
   *                                        {@link Metadata} name and
   *                                        {@link Metadata} audience
   * @param  userIdentityId               {@link Identity} technical identifier
   *                                        designating the user making the
   *                                        operation
   * @return                              Created {@link MetadataItem}
   * @throws ObjectAlreadyExistsException when the {@link MetadataTypePlugin}
   *                                        doesn't allow multiple objects per
   *                                        {@link Metadata} and an object is
   *                                        already associated to the designated
   *                                        {@link Metadata}
   */
  MetadataItem createMetadataItem(MetadataObject metadataObject,
                                  MetadataKey metadataKey,
                                  long userIdentityId) throws ObjectAlreadyExistsException;

  /**
   * Creates a new Metadata Item. When the metadata with the designated key
   * doesn't exists, it will create a new one
   *
   * @param  metadataObject               object to store
   * @param  metadataKey                  {@link MetadataKey} that contains
   *                                        {@link MetadataType} name
   *                                        {@link Metadata} name and
   *                                        {@link Metadata} audience
   * @param  properties                   {@link Map} properties of
   *                                        {@link MetadataItem}
   * @param  userIdentityId               {@link Identity} technical identifier
   *                                        designating the user making the
   *                                        operation
   * @return                              Created {@link MetadataItem}
   * @throws ObjectAlreadyExistsException when the {@link MetadataTypePlugin}
   *                                        doesn't allow multiple objects per
   *                                        {@link Metadata} and an object is
   *                                        already associated to the designated
   *                                        {@link Metadata}
   */
  MetadataItem createMetadataItem(MetadataObject metadataObject,
                                  MetadataKey metadataKey,
                                  Map<String, String> properties,
                                  long userIdentityId) throws ObjectAlreadyExistsException;

  /**
   * Creates a new Metadata Item. When the metadata with the designated key
   * doesn't exists, it will create a new one
   *
   * @param  metadataObject               object to store
   * @param  metadataKey                  {@link MetadataKey} that contains
   *                                        {@link MetadataType} name
   *                                        {@link Metadata} name and
   *                                        {@link Metadata} audience
   * @param  properties                   {@link Map} properties of
   *                                        {@link MetadataItem}
   * @return                              Created {@link MetadataItem}
   * @throws ObjectAlreadyExistsException when the {@link MetadataTypePlugin}
   *                                        doesn't allow multiple objects per
   *                                        {@link Metadata} and an object is
   *                                        already associated to the designated
   *                                        {@link Metadata}
   */
  MetadataItem createMetadataItem(MetadataObject metadataObject,
                                  MetadataKey metadataKey,
                                  Map<String, String> properties) throws ObjectAlreadyExistsException;

  /**
   * @param metadataItem   {@link MetadataItem}
   * @param userIdentityId {@link Identity} technical identifier designating the
   *                       user making the operation
   * @return               Deleted {@link MetadataItem}
   */
  MetadataItem updateMetadataItem(MetadataItem metadataItem, long userIdentityId);

  /**
   * @param  itemId                  {@link MetadataItem} technical identifier
   * @param  userIdentityId          {@link Identity} technical identifier
   *                                   designating the user making the operation
   * @return                         Deleted {@link MetadataItem}
   * @throws ObjectNotFoundException when the {@link MetadataItem} isn't found
   */
  MetadataItem deleteMetadataItem(long itemId, long userIdentityId) throws ObjectNotFoundException;

  /**
   * @param  itemId                  {@link MetadataItem} technical identifier
   * @param  broadcast               whether broadcast event after deleting or
   *                                   not
   * @return                         Deleted {@link MetadataItem}
   * @throws ObjectNotFoundException when the {@link MetadataItem} isn't found
   */
  MetadataItem deleteMetadataItem(long itemId, boolean broadcast) throws ObjectNotFoundException;

  /**
   * Deletes Metadata items for a given {@link MetadataItem} objectId and
   * objectType. This is generally called when the associated object has been
   * removed (activity removed, comment removed ...)
   * 
   * @param object {@link MetadataObject} that defines an objectType (an object
   *                 type identifier like, ACTIVITY, COMMENT, NOTE, FILE ...)
   *                 and an objectId (the object technical identifier. In
   *                 general we use here the DB identifier of the object).
   */
  void deleteMetadataItemsByObject(MetadataObject object);

  /**
   * Deletes all metadata items by {@link MetadataType} and spaceId and
   * audienceId
   * 
   * @param metadataTypeName {@link MetadataType} name
   * @param spaceId {@link Space} id
   * @param userIdentityId {@link Identity} id
   */
  void deleteByMetadataTypeAndSpaceIdAndCreatorId(String metadataTypeName, long spaceId, long userIdentityId);

  /**
   * Deletes all metadata items by {@link MetadataType} and audienceId
   * 
   * @param metadataTypeName {@link MetadataType} name
   * @param userIdentityId {@link Identity} id
   */
  void deleteByMetadataTypeAndCreatorId(String metadataTypeName, long userIdentityId);

  /**
   * Deletes Metadata items for a given {@link Metadata} type, objectId and
   * objectType. This is generally called when the associated object has been
   * removed (activity removed, comment removed ...)
   *
   * @param metadataType {@link Metadata} type
   * @param object       {@link MetadataObject} that defines an objectType (an
   *                       object type identifier like, ACTIVITY, COMMENT, NOTE,
   *                       FILE ...) and an objectId (the object technical
   *                       identifier. In general we use here the DB identifier
   *                       of the object).
   */
  void deleteMetadataItemsByMetadataTypeAndObject(String metadataType, MetadataObject object);

  /**
   * Shares/copy the {@link MetadataItem} list of a shared object to the newly
   * created object.
   * 
   * @param  sourceObject   {@link MetadataObject} that defines an objectType
   *                          (an object type identifier like, ACTIVITY,
   *                          COMMENT, NOTE, FILE ...) and an objectId (the
   *                          object technical identifier. In general we use
   *                          here the DB identifier of the object).
   * @param  targetObjectId target content that will receive the metadata items
   * @param  audienceId     targeted audience {@link Identity} identifier
   * @param  creatorId      {@link Identity} identifier of user sharing object
   * @return                newly created {@link MetadataItem} associated to
   *                        targetObjectId
   */
  List<MetadataItem> shareMetadataItemsByObject(MetadataObject sourceObject,
                                                String targetObjectId,
                                                long audienceId,
                                                long creatorId);

  /**
   * return {@link Set} of {@link Metadata} names associated to a given object
   * 
   * @param  object {@link MetadataObject} used to search items
   * @return        {@link Set} of {@link Metadata} name
   */
  Set<String> getMetadataNamesByObject(MetadataObject object);

  /**
   * return {@link List} of {@link Metadata} names associated to a given
   * metadata type, object type and object id. The returned list is sorted
   * descending by id.
   * 
   * @param  metadataTypeName
   * @param  objectType
   * @param  objectId
   * @return {@link List} of metadata names
   */
  List<String> getMetadataNamesByMetadataTypeAndObject(String metadataTypeName, String objectType, String objectId);

  /**
   * Retrieves the list of Metadata attached to an object identified by its name
   * and identifier
   * 
   * @param  object {@link MetadataObject} that defines an objectType (an object
   *                  type identifier like, ACTIVITY, COMMENT, NOTE, FILE ...)
   *                  and an objectId (the object technical identifier. In
   *                  general we use here the DB identifier of the object).
   * @return        {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByObject(MetadataObject object);

  /**
   * Retrieves the list of Metadata items attached to a given {@link Metadata}
   * type and an object identified by its name and identifier
   *
   * @param  metadataType {@link Metadata} type
   * @param  object       {@link MetadataObject} that defines an objectType (an
   *                        object type identifier like, ACTIVITY, COMMENT,
   *                        NOTE, FILE ...) and an objectId (the object
   *                        technical identifier. In general we use here the DB
   *                        identifier of the object).
   * @return              {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByMetadataTypeAndObject(String metadataType, MetadataObject object);

  /**
   * Retrieves the Metadata item attached to a given {@link Metadata} name, type
   * and {@link MetadataItem} object type
   *
   * @param  metadataName     {@link Metadata} name
   * @param  metadataTypeName {@link Metadata} type name
   * @param  objectType       objectType {@link MetadataItem} objectType
   * @param  offset           offset of ids to retrieve
   * @param  limit            limit of ids to retrieve
   * @return                  {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObject(String metadataName,
                                                                    String metadataTypeName,
                                                                    String objectType,
                                                                    long offset,
                                                                    long limit);

  /**
   * Retrieves the Metadata item attached to a given {@link Metadata} name, type
   * and {@link MetadataItem} object type
   *
   * @param  metadataName     {@link Metadata} name
   * @param  metadataTypeName {@link Metadata} type name
   * @param  objectType       {@link MetadataItem} objectType
   * @param  objectId         {@link MetadataItem} objectId
   * @param  offset           offset of ids to retrieve
   * @param  limit            limit of ids to retrieve
   * @return                  {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObject(String metadataName,
                                                                    String metadataTypeName,
                                                                    String objectType,
                                                                    String objectId,
                                                                    long offset,
                                                                    long limit);

  /**
   * Retrieves the Metadata item attached to a given {@link Metadata} name, type
   * and {@link MetadataItem} object type and {@link MetadataItem} space id
   *
   * @param  metadataName     {@link Metadata} name
   * @param  metadataTypeName {@link Metadata} type name
   * @param  objectType       objectType {@link MetadataItem} objectType
   * @param  spaceId          spaceId {@link MetadataItem} spaceId
   * @param  offset           offset of ids to retrieve
   * @param  limit            limit of ids to retrieve
   * @return                  {@link List} of linked {@link MetadataItem}
   */
  default List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObjectAndSpaceId(String metadataName,
                                                                                      String metadataTypeName,
                                                                                      String objectType,
                                                                                      long spaceId,
                                                                                      long offset,
                                                                                      long limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Retrieves the Metadata item attached to a given {@link Metadata} name, type
   * and {@link MetadataItem} object type and {@link MetadataItem} space id
   *
   * @param metadataName {@link Metadata} name
   * @param metadataTypeName {@link Metadata} type name
   * @param spaceIds spaceId {@link List} of {@link MetadataItem}
   * @param offset offset of ids to retrieve
   * @param limit limit of ids to retrieve
   * @return {@link List} of linked {@link MetadataItem}
   */
  default List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndSpaceIds(String metadataName,
                                                                              String metadataTypeName,
                                                                              List<Long> spaceIds,
                                                                              long offset,
                                                                              long limit) {
    throw new UnsupportedOperationException();
  }

  /**
   * Retrieves the Metadata item attached to a given {@link Metadata} name,
   * type, {@link MetadataItem} object type and {@link MetadataItem} property
   *
   * @param  metadataName     {@link Metadata} name
   * @param  metadataTypeName {@link Metadata} type name
   * @param  objectType       objectType {@link MetadataItem} objectType
   * @param  propertyKey      {@link MetadataItem} property key
   * @param  propertyValue    {@link MetadataItem} property value
   * @param  offset           offset of ids to retrieve
   * @param  limit            limit of ids to retrieve
   * @return                  {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(String metadataName,
                                                                                           String metadataTypeName,
                                                                                           String objectType,
                                                                                           String propertyKey,
                                                                                           String propertyValue,
                                                                                           long offset,
                                                                                           long limit);

  /**
   * Retrieves the Metadata item attached to a given {@link Metadata} type,
   * {@link MetadataItem} creatorId
   *
   * @param  metadataTypeName {@link Metadata} type
   * @param  creatorId        {@link MetadataItem} creatorId
   * @param  offset           offset of ids to retrieve
   * @param  limit            limit of ids to retrieve
   * @return                  {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByMetadataTypeAndCreator(String metadataTypeName, long creatorId, long offset, long limit);

  /**
   * Count the size of metadata items to a given {@link Metadata} type,
   * {@link MetadataItem} creatorId
   *
   * @param  metadataTypeName {@link Metadata} type
   * @param  creatorId        {@link MetadataItem} creatorId
   * @return                  integer
   */
  int countMetadataItemsByMetadataTypeAndCreator(String metadataTypeName, long creatorId);

  /**
   * Count the size of Metadata items to a given {@link Metadata} type,
   * {@link MetadataItem} creatorId by a given {@link Space} identifier
   *
   * @param  metadataTypeName {@link Metadata} type
   * @param  creatorId        {@link MetadataItem} creatorId
   * @param  spaceId          {@link Space} technical identifier
   * @return                  map of Metadata items grouped by objectId
   *                          {@link MetadataObject}
   */
  Map<String, Long> countMetadataItemsByMetadataTypeAndAudienceId(String metadataTypeName, long creatorId, long spaceId);

  /**
   * Count the size of Metadata items to a given {@link Metadata} type,
   * creatorId {@link MetadataItem} by {@link Space} technical identifier
   *
   * @param metadataTypeName {@link Metadata} name
   * @param creatorId {@link MetadataItem} creatorId
   * @param spaceIds {@link Space} technical identifier
   * @return map of Metadata items grouped by space {@link MetadataObject}
   */
  Map<Long, Long> countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(String metadataTypeName,
                                                                          long creatorId,
                                                                          List<Long> spaceIds);

  /**
   * Retrieves the list of Metadata items attached to a {@link MetadataKey} and
   * an object identified by its name and identifier
   * 
   * @param  metadataKey {@link MetadataKey} that contains {@link MetadataType}
   *                       name {@link Metadata} name and {@link Metadata}
   *                       audience
   * @param  object      {@link MetadataObject} that defines an objectType (an
   *                       object type identifier like, ACTIVITY, COMMENT, NOTE,
   *                       FILE ...) and an objectId (the object technical
   *                       identifier. In general we use here the DB identifier
   *                       of the object).
   * @return             {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByMetadataAndObject(MetadataKey metadataKey, MetadataObject object);

  /**
   * Retieves a list of object identifiers switch Metadata type, name and
   * objectType
   * 
   * @param  metadataType {@link Metadata} type
   * @param  metadataName {@link Metadata} name
   * @param  objectType   {@link MetadataItem} objectType
   * @param  offset       offset of ids to retrieve
   * @param  limit        limit of ids to retrieve
   * @return              {@link List} of associated objects ordered by
   *                      {@link MetadataItem} creationDate desc
   */
  List<String> getMetadataObjectIds(String metadataType, String metadataName, String objectType, long offset, long limit);

  /**
   * Registers a new {@link MetadataType}
   * 
   * @param metadataTypePlugin a {@link ComponentPlugin} defining
   *                             {@link MetadataType} to register
   */
  void addMetadataTypePlugin(MetadataTypePlugin metadataTypePlugin);

  /**
   * Save a new {@link Metadata}
   *
   * @param metadataInitPlugin a {@link ComponentPlugin} defining
   *                             {@link Metadata} to save
   */
  void addMetadataPlugin(MetadataInitPlugin metadataInitPlugin);

  /**
   * Retrieves a registered {@link MetadataTypePlugin} by name
   * 
   * @param  name {@link MetadataType} name
   * @return      {@link MetadataTypePlugin}
   */
  MetadataTypePlugin getMetadataTypePluginByName(String name);

  /**
   * Retrieves a registered {@link MetadataType} by name
   * 
   * @param  name {@link MetadataType} name
   * @return      {@link MetadataType}
   */
  MetadataType getMetadataTypeByName(String name);

  /**
   * @return {@link List} of Managed {@link MetadataType}
   */
  List<MetadataType> getMetadataTypes();

  /**
   * @param  propertyKey   {@link Metadata} property key
   * @param  propertyValue {@link Metadata} property value
   * @param  limit         limit of results to retrieve
   * @return               {@link List} of Managed {@link Metadata} by property
   */
  List<Metadata> getMetadatasByProperty(String propertyKey, String propertyValue, long limit);

  /**
   * @param  metadataTypeName metadata name {@link Metadata} name
   * @param  limit            limit of results to retrieve
   * @return                  {@link List} of Managed {@link Metadata}
   */
  List<Metadata> getMetadatas(String metadataTypeName, long limit);

  /**
   * Deletes Metadata items for a given {@link MetadataItem} parentObjectId and
   * objectType. This is generally called when the associated parent object has
   * been removed (activity removed bu example, its comments metadata items has
   * to be deleted as well)
   * 
   * @param object {@link MetadataObject} that defines an objectType (an object
   *                 type identifier like, ACTIVITY, COMMENT, NOTE, FILE ...)
   *                 and a parentObjectId (the object technical identifier. In
   *                 general we use here the DB identifier of the object).
   */
  void deleteMetadataItemsByParentObject(MetadataObject object);

  /**
   * Retrieves a {@link Set} of {@link Metadata} name matching the given
   * {@link MetadataType} and {@link Set} of audience {@link Identity} ids
   * 
   * @param  term             Search query matching {@link Metadata} name
   * @param  metadataTypeName {@link MetadataType} name
   * @param  audienceIds      {@link Set} of {@link Identity} identifiers
   * @param  limit            limit of results to retrieve
   * @return                  {@link List} of {@link Metadata} names
   */
  List<String> findMetadataNamesByAudiences(String term, String metadataTypeName, Set<Long> audienceIds, long limit);

  /**
   * Retrieves a {@link Set} of {@link Metadata} name matching the given
   * {@link MetadataType} and {@link Set} of audience {@link Identity} ids
   * 
   * @param  term             Search query matching {@link Metadata} name
   * @param  metadataTypeName {@link MetadataType} name
   * @param  creatorId        {@link Identity} identifier of creator
   * @param  limit            limit of results to retrieve
   * @return                  {@link List} of {@link Metadata} names
   */
  List<String> findMetadataNamesByCreator(String term, String metadataTypeName, long creatorId, long limit);

  /**
   * Retrieves a {@link Set} of {@link Metadata} name matching the given
   * {@link MetadataType} and {@link Set} of audience {@link Identity} ids
   *
   * @param  term             Search query matching {@link Metadata} name
   * @param  metadataTypeName {@link MetadataType} name
   * @param  audienceIds      {@link Set} of {@link Identity} identifiers
   * @param  creatorId        {@link Identity} identifier of creator
   * @param  limit            limit of results to retrieve
   * @return                  {@link List} of {@link Metadata} names
   */
  List<String> findMetadataNamesByUserAndQuery(String term, String metadataTypeName, Set<Long> audienceIds, long creatorId,
                                               long limit);

}
