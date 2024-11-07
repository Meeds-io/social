package io.meeds.social.space.administration.service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.administration.model.SpacePermissions;

public interface SpaceAdministrationService {

  /**
   * @param spaceId {@link Space} technical id
   * @return Space permissions with layoutPermissions, deletePermissions and
   *         publicSitePermissions
   * @throws ObjectNotFoundException when the space doesn't exist
   */
  SpacePermissions getSpacePermissions(long spaceId) throws ObjectNotFoundException;

  /**
   * @param spaceId {@link Space} technical id
   * @param permissions Space permissions with layoutPermissions,
   *          deletePermissions and publicSitePermissions
   * @throws ObjectNotFoundException when the space doesn't exist
   */
  void updateSpacePermissions(long spaceId, SpacePermissions permissions) throws ObjectNotFoundException;

}
