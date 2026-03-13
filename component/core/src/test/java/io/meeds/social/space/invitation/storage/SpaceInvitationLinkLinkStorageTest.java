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

import io.meeds.social.space.invitation.dao.SpaceInvitationLinkDAO;
import io.meeds.social.space.invitation.entity.SpaceInvitationLinkEntity;
import io.meeds.social.space.invitation.model.SpaceInvitationLink;
import io.meeds.social.space.invitation.mapper.SpaceInvitationLinkMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpaceInvitationLinkLinkStorageTest {

  @Mock
  private SpaceInvitationLinkDAO spaceInvitationLinkDAO;

  @InjectMocks
  private SpaceInvitationLinkStorage spaceInvitationLinkStorage;

  private final Long spaceId = 100L;
  private final String invitedUserId = "user1";

  private SpaceInvitationLink       model;
  private SpaceInvitationLinkEntity entity;

  @Before
  public void setUp() {
    model = new SpaceInvitationLink();
    model.setSpaceId(spaceId);
    model.setInvitedUserId(invitedUserId);

    entity = SpaceInvitationLinkMapper.toEntity(model);
  }

  @Test
  public void testDeleteInvitationBySpaceAndUserAndTypeShouldCallDao() {
    spaceInvitationLinkStorage.deleteInvitationLinkBySpaceAndUserAndType(spaceId, invitedUserId);
    verify(spaceInvitationLinkDAO).deleteBySpaceIdAndInvitedUserId(spaceId, invitedUserId);
  }

  @Test
  public void testGetInvitationsBySpaceAndUserAndTypeShouldReturnMappedModel() {
    when(spaceInvitationLinkDAO.findBySpaceIdAndInvitedUserId(spaceId, invitedUserId
    )).thenReturn(entity);

    SpaceInvitationLink result = spaceInvitationLinkStorage.getInvitationLinkBySpaceAndUserAndType(spaceId, invitedUserId
    );

    assertNotNull(result);
    assertEquals(spaceId, result.getSpaceId());
    assertEquals(invitedUserId, result.getInvitedUserId());

    verify(spaceInvitationLinkDAO).findBySpaceIdAndInvitedUserId(spaceId, invitedUserId
    );
  }

  @Test
  public void testSaveInvitationShouldSaveIfNotExists() {
    when(spaceInvitationLinkDAO.findBySpaceIdAndInvitedUserId(spaceId, invitedUserId
    )).thenReturn(null);

    spaceInvitationLinkStorage.saveInvitationLink(model);

    verify(spaceInvitationLinkDAO).save(any(SpaceInvitationLinkEntity.class));
  }
}
