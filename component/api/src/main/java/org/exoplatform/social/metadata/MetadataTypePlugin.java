package org.exoplatform.social.metadata;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.model.*;

public abstract class MetadataTypePlugin extends BaseComponentPlugin {

  protected MetadataType metadataType;

  protected MetadataTypePlugin(MetadataType metadataType) {
    this.metadataType = metadataType;
  }

  /**
   * @return {@link MetadataType} handled by the current {@link MetadataService}
   *         Plugin
   */
  public MetadataType getMetadataType() {
    return metadataType;
  }

  /**
   * A method to test whether the user can create a metadata for targeted
   * audience or not
   * 
   * @param metadata {@link Metadata} object
   * @param userIdentityId {@link Identity} identifier of the user making the
   *          operation
   * @return true if the user is allowed to create the {@link Metadata}, else
   *         false
   */
  public abstract boolean canCreateMetadata(Metadata metadata, long userIdentityId);

  /**
   * A method to test whether the user can update a metadata for targeted
   * audience or not
   * 
   * @param metadata {@link Metadata} object
   * @param userIdentityId {@link Identity} identifier of the user making the
   *          operation
   * @return true if the user is allowed to update the {@link Metadata}, else
   *         false
   */
  public abstract boolean canUpdateMetadata(Metadata metadata, long userIdentityId);

  /**
   * A method to test whether the user can delete a metadata for targeted
   * audience or not
   * 
   * @param metadata {@link Metadata} object
   * @param userIdentityId {@link Identity} identifier of the user making the
   *          operation
   * @return true if the user is allowed to delete the {@link Metadata}, else
   *         false
   */
  public abstract boolean canDeleteMetadata(Metadata metadata, long userIdentityId);

  /**
   * A method to test whether the user can associate a {@link Metadata} to an
   * object or not
   * 
   * @param metadataItem {@link MetadataItem}
   * @param userIdentityId {@link Identity} identifier of the user making the
   *          operation
   * @return true if the user is allowed to create the {@link MetadataItem},
   *         else false
   */
  public abstract boolean canCreateMetadataItem(MetadataItem metadataItem, long userIdentityId);

  /**
   * A method to test whether the user can update an association of a
   * {@link Metadata} to an object or not
   * 
   * @param metadataItem {@link MetadataItem}
   * @param userIdentityId {@link Identity} identifier of the user making the
   *          operation
   * @return true if the user is allowed to create the {@link MetadataItem},
   *         else false
   */
  public abstract boolean canUpdateMetadataItem(MetadataItem metadataItem, long userIdentityId);

  /**
   * A method to test whether the user can delete association of a
   * {@link Metadata} to an object or not
   * 
   * @param metadataItem {@link MetadataItem}
   * @param userIdentityId {@link Identity} identifier of the user making the
   *          operation
   * @return true if the user is allowed to delete the {@link MetadataItem},
   *         else false
   */
  public abstract boolean canDeleteMetadataItem(MetadataItem metadataItem, long userIdentityId);

}
