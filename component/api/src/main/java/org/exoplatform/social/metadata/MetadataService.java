package org.exoplatform.social.metadata;

import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.model.*;

/**
 * A Service to manage Metadata of any content of any type in eXo Platform. This
 * Service will allow to store additional Metadata to contents without defining
 * additional Services or Storage layers to allow to extends content definition
 */
public interface MetadataService {

  /**
   * Creates a new Metadata object
   * 
   * @param metadata {@link Metadata}
   * @param userIdentityId {@link Identity} identifier of the creator
   * @return created {@link Metadata}
   * @throws IllegalAccessException when the user is not allowed to create a
   *           {@link Metadata} for designated type
   */
  Metadata createMetadata(Metadata metadata, long userIdentityId) throws IllegalAccessException;

  /**
   * Delete Metadata object identified by type, name and audienceId.
   * 
   * @param type {@link Metadata} type
   * @param name {@link Metadata} name
   * @param audienceId {@link Metadata} audienceId
   * @param userIdentityId {@link Identity} identifier of the user who is
   *          deleting the {@link Metadata}
   * @return deleted {@link Metadata}
   * @throws ObjectNotFoundException when the {@link Metadata} isn't found
   * @throws IllegalAccessException when the user isn't allowed to delete
   *           {@link Metadata}
   */
  Metadata deleteMetadata(String type, String name, long audienceId, long userIdentityId) throws ObjectNotFoundException,
                                                                                          IllegalAccessException;

  /**
   * Retrieves a {@link Metadata} identified by a unique constraint for
   * 'Metadata Type', 'Metadata Name' and 'Metadata Audience'.
   * 
   * @param metadataType {@link MetadataType} name
   * @param metadataName {@link Metadata} name
   * @param audienceId {@link Metadata} audienceId
   * @return {@link Metadata} if found, else null
   */
  Metadata getMetadataByTypeAndNameAndAudience(String metadataType, String metadataName, long audienceId);

  /**
   * Creates a new Metadata Item. When the metadata with the designated name
   * doesn't exists, it will create a new one
   * 
   * @param metadataItem object to store
   * @param type {@link MetadataType} name
   * @param name {@link Metadata} name
   * @param audienceId {@link Metadata} audience
   * @param userIdentityId {@link Identity} technical identifier designating the
   *          user making the operation
   * @return Created {@link MetadataItem}
   * @throws IllegalAccessException when the user isn't allowed to create a
   *           Metadata item on designated Type and Name
   * @throws ObjectAlreadyExistsException when the {@link MetadataTypePlugin}
   *           doesn't allow multiple objects per {@link Metadata} and an object
   *           is already associated to the designated {@link Metadata}
   */
  MetadataItem createMetadataItem(MetadataItem metadataItem,
                                  String type,
                                  String name,
                                  long audienceId,
                                  long userIdentityId) throws IllegalAccessException, ObjectAlreadyExistsException;

  /**
   * @param type {@link MetadataType} name
   * @param name {@link Metadata} name
   * @param itemId {@link MetadataItem} technical identifier
   * @param userIdentityId {@link Identity} technical identifier designating the
   *          user making the operation
   * @return Deleted {@link MetadataItem}
   * @throws IllegalAccessException when the user isn't allowed to delete a
   *           Metadata item on designated Type and Name
   * @throws ObjectNotFoundException when the {@link MetadataItem} isn't found
   */
  MetadataItem deleteMetadataItem(String type, String name, long itemId, long userIdentityId) throws IllegalAccessException,
                                                                                              ObjectNotFoundException;

  /**
   * Deletes Metadata items for a given {@link MetadataItem} objectId and
   * objectType. This is generally called when the associated object has been
   * removed (activity removed, comment removed ...)
   * 
   * @param objectType
   * @param objectId
   */
  void deleteMetadataItemsByObject(String objectType, String objectId);

  /**
   * Retrieves the list of Metadata attached to an object identified by its name
   * and identifier
   * 
   * @param objectType an object type identifier like, ACTIVITY, COMMENT, NOTE,
   *          FILE ...
   * @param objectId the object technical identifier. In general we use here the
   *          DB identifier of the object.
   * @return {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByObject(String objectType, String objectId);

  /**
   * Retrieves the list of Metadata items attached to a {@link Metadata} and an
   * object identified by its name and identifier
   * 
   * @param metadataId {@link Metadata} technical identifier
   * @param objectType an object type identifier like, ACTIVITY, COMMENT, NOTE,
   *          FILE ...
   * @param objectId the object technical identifier. In general we use here the
   *          DB identifier of the object.
   * @return {@link List} of linked {@link MetadataItem}
   */
  List<MetadataItem> getMetadataItemsByMetadataAndObject(long metadataId, String objectType, String objectId);

  /**
   * Retieves a list of object identifiers switch Metadata type, name and
   * objectType
   * 
   * @param metadataType {@link Metadata} type
   * @param metadataName {@link Metadata} name
   * @param objectType {@link MetadataItem} objectType
   * @param limit limit of ids to retrieve
   * @return {@link List} of associated objects ordered by {@link MetadataItem}
   *         creationDate desc
   */
  List<String> getMetadataObjectIds(String metadataType, String metadataName, String objectType, int limit);

  /**
   * Registers a new {@link MetadataType}
   * 
   * @param metadataTypePlugin a {@link ComponentPlugin} defining
   *          {@link MetadataType} to register
   */
  void addMetadataTypePlugin(MetadataTypePlugin metadataTypePlugin);

  /**
   * Retrieves a registered {@link MetadataTypePlugin} by name
   * 
   * @param name {@link MetadataType} name
   * @return {@link MetadataTypePlugin}
   */
  MetadataTypePlugin getMetadataTypePluginByName(String name);

  /**
   * @return {@link List} of Managed {@link MetadataType}
   */
  List<MetadataType> getMetadataTypes();

}
