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
package io.meeds.social.user.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.SpaceUtils;

import io.meeds.portal.plugin.AclPlugin;

import jakarta.annotation.PostConstruct;

@Component
public class UserAclPlugin implements AclPlugin {

  public static final String OBJECT_TYPE = UserProfilePermanentLinkPlugin.OBJECT_TYPE;

  @Autowired
  private PortalContainer    container;

  private UserACL            userAcl;

  @PostConstruct
  public void init() {
    this.userAcl = container.getComponentInstanceOfType(UserACL.class);
    this.userAcl.addAclPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasPermission(String objectId, String permissionType, Identity identity) {
    if (identity == null) {
      return false;
    } else {
      Identity userIdentity = userAcl.getUserIdentity(objectId);
      if (userIdentity == null) {
        return DELETE_PERMISSION_TYPE.equals(permissionType) ? userAcl.isSuperUser(identity) : userAcl.isAdministrator(identity);
      } else {
        return switch (permissionType) {
        case DELETE_PERMISSION_TYPE: {
          yield userAcl.isSuperUser(identity);
        }
        case VIEW_PERMISSION_TYPE: {
          yield userAcl.isAdministrator(identity) || userIdentity.getGroups()
                                                                 .stream()
                                                                 .filter(g -> !SpaceUtils.PLATFORM_EXTERNALS_GROUP.equals(g))
                                                                 .anyMatch(g -> identity.getGroups().contains(g));
        }
        default:
          yield userAcl.isAdministrator(identity);
        };
      }
    }
  }

}
