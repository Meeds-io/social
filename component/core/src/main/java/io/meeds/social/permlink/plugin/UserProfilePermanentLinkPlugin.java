/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.permlink.plugin;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.plugin.PermanentLinkPlugin;

/**
 * A plugin to generate a permanent link to a given user profile
 */
public class UserProfilePermanentLinkPlugin implements PermanentLinkPlugin {

  public static final String      OBJECT_TYPE = "user";

  public static final String      URL_FORMAT  = "/portal/%s/profile/%s";

  private IdentityManager         identityManager;

  private UserPortalConfigService portalConfigService;

  public UserProfilePermanentLinkPlugin(IdentityManager identityManager,
                                        UserPortalConfigService portalConfigService) {
    this.identityManager = identityManager;
    this.portalConfigService = portalConfigService;
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canAccess(PermanentLinkObject object, Identity identity) throws ObjectNotFoundException {
    return identity != null && identity.getUserId() != null;
  }

  @Override
  public String getDirectAccessUrl(PermanentLinkObject object) throws ObjectNotFoundException {
    String userId = object.getObjectId();
    if (StringUtils.isNumeric(userId)) {
      org.exoplatform.social.core.identity.model.Identity identity = identityManager.getIdentity(userId);
      if (identity == null) {
        throw new ObjectNotFoundException(String.format("User profile with id %s doesn't exists", userId));
      } else if (!identity.isUser()) {
        throw new ObjectNotFoundException(String.format("Identity with id %s isn't of type user", userId));
      }
      userId = identity.getRemoteId();
    }
    return String.format(URL_FORMAT, portalConfigService.getMetaPortal(), userId);
  }

}
