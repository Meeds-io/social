package org.exoplatform.social.metadata.favorite;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.model.MetadataType;

public interface FavoriteService {

  public static final MetadataType METADATA_TYPE = new MetadataType(1, "favorites");

  /**
   * Create a new Favorite for corresponding object identified by its type/id
   * for a user
   * 
   * @param objectType object type, can be of any type: activity, comment,
   *          notes...
   * @param objectId object technical unique identifier
   * @param userIdentityId {@link Identity} technical identifier of the user
   * @throws ObjectAlreadyExistsException when the favorite already exists
   */
  public void createFavorite(String objectType, String objectId, long userIdentityId) throws ObjectAlreadyExistsException;

  /**
   * Deletes an existing Favorite for corresponding object identified by its
   * type/id for a user
   * 
   * @param objectType object type, can be of any type: activity, comment,
   *          notes...
   * @param objectId object technical unique identifier
   * @param userIdentityId {@link Identity} technical identifier of the user
   * @throws ObjectNotFoundException when the favorite doesn't exists
   */
  public void deleteFavorite(String objectType, String objectId, long userIdentityId) throws ObjectNotFoundException;

}
