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
package io.meeds.social.space.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.portal.plugin.AclPlugin;

import jakarta.annotation.PostConstruct;

@Component
public class SpaceAclPlugin implements AclPlugin {

  public static final String OBJECT_TYPE             = "space";

  public static final String LIST_PERMISSION_TYPE    = "list";

  public static final String LAYOUT_PERMISSION_TYPE  = "layout";

  public static final String PUBLISH_PERMISSION_TYPE = "publish";

  public static final String REDACT_PERMISSION_TYPE  = "redact";

  public static final String MANAGE_PERMISSION_TYPE  = "manage";

  @Autowired
  private PortalContainer    container;

  @Autowired
  private SpaceService       spaceService;

  @PostConstruct
  public void init() {
    container.getComponentInstanceOfType(UserACL.class).addAclPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasPermission(String objectId, String permissionType, Identity identity) {
    Space space = spaceService.getSpaceById(Long.parseLong(objectId));
    if (space == null) {
      return false;
    } else {
      String username = identity == null ? null : identity.getUserId();
      return switch (permissionType) {
      case VIEW_PERMISSION_TYPE: {
        yield spaceService.canViewSpace(space, username);
      }
      case MANAGE_PERMISSION_TYPE, EDIT_PERMISSION_TYPE: {
        yield identity != null && spaceService.canManageSpace(space, username);
      }
      case DELETE_PERMISSION_TYPE: {
        yield spaceService.canDeleteSpace(space, username);
      }
      case LIST_PERMISSION_TYPE: {
        yield spaceService.canListSpace(space, username);
      }
      case REDACT_PERMISSION_TYPE: {
        yield spaceService.canRedactOnSpace(space, username);
      }
      case PUBLISH_PERMISSION_TYPE: {
        yield spaceService.canPublishOnSpace(space, username);
      }
      case LAYOUT_PERMISSION_TYPE: {
        yield spaceService.canManageSpaceLayout(space, username);
      }
      default:
        yield false;
      };
    }
  }

}
