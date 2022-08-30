/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2022 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.plugin;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.FavoriteACLPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class SpaceFavoriteACLPlugin extends FavoriteACLPlugin {

  private static final String   SPACE_FAVORITE_TYPE = "space";

  private final SpaceService    spaceService;

  private final IdentityManager identityManager;

  public SpaceFavoriteACLPlugin(SpaceService spaceService, IdentityManager identityManager) {
    this.spaceService = spaceService;
    this.identityManager = identityManager;
  }

  @Override
  public String getEntityType() {
    return SPACE_FAVORITE_TYPE;
  }

  @Override
  public boolean canCreateFavorite(org.exoplatform.services.security.Identity userIdentity, String objectId) {
    String userIdentityId = userIdentity.getUserId();
    Identity identity = identityManager.getOrCreateUserIdentity(userIdentityId);
    if (identity == null) {
      throw new IllegalStateException("User with id " + userIdentityId + " wasn't found");
    }
    Space space = spaceService.getSpaceById(objectId);
    if (space == null) {
      throw new IllegalStateException("Space with id " + objectId + " wasn't found");
    }
    String userRemoteId = identity.getRemoteId();
    return spaceService.isMember(space, userRemoteId);
  }
}
