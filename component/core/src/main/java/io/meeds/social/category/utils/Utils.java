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
package io.meeds.social.category.utils;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.GroupIdentityProvider;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class Utils {
  private Utils() {
    // static methods
  }

  public static boolean isMemberOf(IdentityManager identityManager,
                                   SpaceService spaceService,
                                   UserACL userAcl,
                                   long identityId,
                                   String username) {
    org.exoplatform.services.security.Identity userAclIdentity = userAcl.getUserIdentity(username);
    if (identityId == 0) {
      return true;
    } else if (userAclIdentity == null) {
      return false;
    }
    Identity identity = identityManager.getIdentity(identityId);
    if (identity == null) {
      return false;
    }
    return switch (identity.getProviderId()) {
    case SpaceIdentityProvider.NAME: {
      Space space = spaceService.getSpaceByPrettyName(identity.getRemoteId());
      yield space != null && spaceService.canViewSpace(space, userAclIdentity.getUserId());
    }
    case GroupIdentityProvider.NAME: {
      yield userAclIdentity.isMemberOf(identity.getRemoteId());
    }
    case OrganizationIdentityProvider.NAME: {
      yield StringUtils.equals(identity.getRemoteId(), userAclIdentity.getUserId());
    }
    default:
      yield false;
    };
  }

  public static boolean isManagerOf(IdentityManager identityManager,
                                    SpaceService spaceService,
                                    UserACL userAcl,
                                    long identityId,
                                    String username) {
    org.exoplatform.services.security.Identity userAclIdentity = userAcl.getUserIdentity(username);
    if (identityId == 0) {
      return true;
    } else if (userAclIdentity == null) {
      return false;
    }
    Identity identity = identityManager.getIdentity(identityId);
    if (identity == null) {
      return false;
    }
    return switch (identity.getProviderId()) {
    case SpaceIdentityProvider.NAME: {
      Space space = spaceService.getSpaceByPrettyName(identity.getRemoteId());
      yield space != null && spaceService.canManageSpace(space, userAclIdentity.getUserId());
    }
    case GroupIdentityProvider.NAME: {
      yield userAclIdentity.isMemberOf(userAcl.getAdminMSType(), identity.getRemoteId());
    }
    case OrganizationIdentityProvider.NAME: {
      yield StringUtils.equals(identity.getRemoteId(), userAclIdentity.getUserId());
    }
    default:
      yield false;
    };
  }

}
