package org.exoplatform.social.metadata;

import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.model.*;

/**
 * A Service to manage Metadata of any content of any type in eXo Platform. This
 * Service will allow to store additional Metadata to contents without defining
 * additional Services or Storage layers to allow to extends content definition
 */
public interface MetadataService {

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
   */
  MetadataItem createMetadataItem(MetadataItem metadataItem,
                          String type,
                          String name,
                          long audienceId,
                          long userIdentityId) throws IllegalAccessException;

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
  List<MetadataItem> getMetadatasByObject(String objectType, String objectId);

  /**
   * Registers a new {@link MetadataType}
   * 
   * @param metadataTypePlugin a {@link ComponentPlugin} defining
   *          {@link MetadataType} to register
   */
  void addMetadataTypePlugin(MetadataTypePlugin metadataTypePlugin);

  /**
   * @return {@link List} of Managed {@link MetadataType}
   */
  List<MetadataType> getMetadataTypes();

}
