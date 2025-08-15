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
package org.exoplatform.social.core.listeners;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.metadata.MetadataService;

/**
 * A listener to delete associated metadata to a deleted Space
 */
public class SpaceMetadataListenerImpl extends SpaceListenerPlugin {

  private MetadataService metadataService;
  private IdentityManager identityManager;

  public SpaceMetadataListenerImpl(MetadataService metadataService,
                                   IdentityManager identityManager) {
    this.metadataService = metadataService;
    this.identityManager = identityManager;
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    long spaceId = Long.parseLong(event.getSpace().getId());
    this.metadataService.deleteMetadataBySpaceId(spaceId);
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    long spaceId = Long.parseLong(event.getSpace().getId());
    String username = event.getSource();
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    if (identity != null) {
      long userId = Long.parseLong(identity.getId());
      metadataService.deleteMetadataBySpaceIdAndAudienceId(spaceId, userId);
    }
  }

}
