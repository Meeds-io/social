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
