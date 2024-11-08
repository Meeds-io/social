package io.meeds.social.space.service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.model.SpacePermissions;
import io.meeds.social.space.model.SpaceTemplatePatch;

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

  /**
   * @param spaceId {@link Space} technical id
   * @param templatePatch SpaceTemplate properties to apply
   * @throws ObjectNotFoundException when the space doesn't exist
   */
  void applySpaceTemplate(long spaceId, SpaceTemplatePatch templatePatch) throws ObjectNotFoundException;

}
