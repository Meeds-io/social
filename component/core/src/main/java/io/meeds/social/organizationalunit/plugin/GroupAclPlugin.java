/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.organizationalunit.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;

import io.meeds.portal.plugin.AclPlugin;
import io.meeds.social.organizationalunit.service.OrganizationalUnitService;

import jakarta.annotation.PostConstruct;

@Component
public class GroupAclPlugin implements AclPlugin {

  public static final String        OBJECT_TYPE                        = "group";

  public static final String        LIST_MEMBERS_PERMISSION_TYPE       = "listMembers";

  public static final String        MANAGE_MEMBERSHIPS_PERMISSION_TYPE = "manageMemberships";

  @Autowired
  private PortalContainer           container;

  private UserACL                   userACL;

  @Autowired
  private OrganizationalUnitService organizationalUnitService;

  @PostConstruct
  public void init() {
    this.userACL = container.getComponentInstanceOfType(UserACL.class);
    this.userACL.addAclPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasPermission(String objectId, String permissionType, Identity identity) {
    if (identity == null) {
      return false;
    }
    switch (permissionType) {
    case LIST_MEMBERS_PERMISSION_TYPE, MANAGE_MEMBERSHIPS_PERMISSION_TYPE -> {
      return organizationalUnitService.canManageGroup(objectId, identity.getUserId());
    }
    case EDIT_PERMISSION_TYPE, DELETE_PERMISSION_TYPE -> {
      return userACL.isAdministrator(identity);
    }
    default -> {
      return false;
    }
    }
  }

}
