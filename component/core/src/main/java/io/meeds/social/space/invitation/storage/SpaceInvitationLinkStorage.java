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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package io.meeds.social.space.invitation.storage;

import io.meeds.social.space.invitation.mapper.SpaceInvitationLinkMapper;
import io.meeds.social.space.invitation.model.SpaceInvitationLink;

import io.meeds.social.space.invitation.dao.SpaceInvitationLinkDAO;
import org.springframework.stereotype.Service;

@Service
// TODO to switch to @Component once SpaceServiceImpl switched to use a Spring Base Definition
public class SpaceInvitationLinkStorage {

  private final SpaceInvitationLinkDAO spaceInvitationLinkDAO;

  public SpaceInvitationLinkStorage(SpaceInvitationLinkDAO spaceInvitationLinkDAO) {
    this.spaceInvitationLinkDAO = spaceInvitationLinkDAO;
  }

  public SpaceInvitationLink getInvitationLinkBySpaceAndUserAndType(Long spaceId, String invitedUserId) {
    return SpaceInvitationLinkMapper.fromEntity(spaceInvitationLinkDAO.findBySpaceIdAndInvitedUserId(spaceId, invitedUserId));
  }

  public void deleteInvitationLinkBySpaceAndUserAndType(Long spaceId, String invitedUserId) {
    spaceInvitationLinkDAO.deleteBySpaceIdAndInvitedUserId(spaceId, invitedUserId);
  }

  public void saveInvitationLink(SpaceInvitationLink model) {
    if (model == null) {
      throw new IllegalArgumentException("SpaceInvitation model cannot be null");
    }
    if (getInvitationLinkBySpaceAndUserAndType(model.getSpaceId(), model.getInvitedUserId()) != null) {
      return;
    }
    spaceInvitationLinkDAO.save(SpaceInvitationLinkMapper.toEntity(model));
  }
}
