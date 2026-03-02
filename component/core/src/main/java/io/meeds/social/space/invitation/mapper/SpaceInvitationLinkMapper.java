/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.space.invitation.mapper;

import io.meeds.social.space.invitation.entity.SpaceInvitationLinkEntity;
import io.meeds.social.space.invitation.model.SpaceInvitationLink;

public final class SpaceInvitationLinkMapper {

  private SpaceInvitationLinkMapper() {}

  public static SpaceInvitationLink fromEntity(SpaceInvitationLinkEntity entity) {
   if (entity == null) {
     return null;
   }

    SpaceInvitationLink model = new SpaceInvitationLink();
    model.setId(entity.getId());
    model.setSpaceId(entity.getSpaceId());
    model.setInviterId(entity.getInviterId());
    model.setInvitedUserId(entity.getInvitedUserId());
    return model;
  }

  public static SpaceInvitationLinkEntity toEntity(SpaceInvitationLink model) {
    if (model == null) {
      return null;
    }

    SpaceInvitationLinkEntity entity = new SpaceInvitationLinkEntity();
    entity.setId(model.getId());
    entity.setSpaceId(model.getSpaceId());
    entity.setInviterId(model.getInviterId());
    entity.setInvitedUserId(model.getInvitedUserId());
    return entity;
  }
}
