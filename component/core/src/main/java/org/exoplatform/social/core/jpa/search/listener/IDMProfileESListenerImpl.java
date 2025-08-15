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
package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;

public class IDMProfileESListenerImpl extends UserProfileEventListener {

  private static final Log LOG = ExoLogger.getLogger(IDMProfileESListenerImpl.class);

  @Override
  public void postSave(UserProfile user, boolean isNew) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName());

      if (identity != null) {
        LOG.debug("Notifying indexing service for IDM user profile update id={}", identity.getId());
        indexingService.reindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      }
    } finally {
      RequestLifeCycle.end();
    }
  }

}
