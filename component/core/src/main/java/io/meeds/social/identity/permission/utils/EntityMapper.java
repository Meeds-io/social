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
package io.meeds.social.identity.permission.utils;

import io.meeds.social.identity.permission.entity.UserPermissionEntity;
import io.meeds.social.identity.permission.model.UserPermission;

public class EntityMapper {

  private EntityMapper() {
    // Utils class
  }

  public static UserPermission fromEntity(UserPermissionEntity entity) {
    if (entity == null) {
      return null;
    }
    return new UserPermission(entity.getId() == null ? 0 : entity.getId(),
                              entity.getIdentityId() == null ? 0 : entity.getIdentityId(),
                              entity.getUserName(),
                              entity.getGroupId(),
                              entity.getMembershipType(),
                              entity.isInherited());
  }

  public static UserPermissionEntity toEntity(UserPermission userPermission) {
    if (userPermission == null) {
      return null;
    }
    UserPermissionEntity entity = new UserPermissionEntity();
    entity.setId(userPermission.getId() == 0 ? null : userPermission.getId());
    entity.setIdentityId(userPermission.getIdentityId());
    entity.setUserName(userPermission.getUserName());
    entity.setGroupId(userPermission.getGroupId());
    entity.setMembershipType(userPermission.getMembershipType());
    entity.setInherited(userPermission.isInherited());
    return entity;
  }

}
