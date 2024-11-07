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
package io.meeds.social.space.administration.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.administration.model.SpacePermissions;

@RunWith(MockitoJUnitRunner.class)
public class SpaceAdministrationServiceTest {

  @Mock
  SpaceService                   spaceService;

  @Mock
  Space                          space;

  SpaceAdministrationServiceImpl spaceAdministrationService;

  @Before
  public void setup() {
    spaceAdministrationService = new SpaceAdministrationServiceImpl();
    spaceAdministrationService.setSpaceService(spaceService);
  }

  @Test
  public void testGetSpacePermissions() throws ObjectNotFoundException {
    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.getSpacePermissions(2l));
    when(spaceService.getSpaceById(2l)).thenReturn(space);
    when(space.getLayoutPermissions()).thenReturn(Collections.singletonList("layoutPermissions"));
    when(space.getDeletePermissions()).thenReturn(Collections.singletonList("deletePermissions"));
    when(space.getPublicSitePermissions()).thenReturn(Collections.singletonList("publicSitePermissionsPermissions"));
    SpacePermissions spacePermissions = spaceAdministrationService.getSpacePermissions(2l);
    assertNotNull(spacePermissions);
    assertEquals(space.getDeletePermissions(), spacePermissions.getDeletePermissions());
    assertEquals(space.getLayoutPermissions(), spacePermissions.getLayoutPermissions());
    assertEquals(space.getPublicSitePermissions(), spacePermissions.getPublicSitePermissions());
  }

  @Test
  public void testUpdateSpacePermissions() throws ObjectNotFoundException {
    SpacePermissions spacePermissions = mock(SpacePermissions.class);
    when(spacePermissions.getLayoutPermissions()).thenReturn(Collections.singletonList("layoutPermissions"));
    when(spacePermissions.getDeletePermissions()).thenReturn(Collections.singletonList("deletePermissions"));
    when(spacePermissions.getPublicSitePermissions()).thenReturn(Collections.singletonList("publicSitePermissionsPermissions"));

    assertThrows(ObjectNotFoundException.class, () -> spaceAdministrationService.updateSpacePermissions(2l, spacePermissions));
    when(spaceService.getSpaceById(2l)).thenReturn(space);
    spaceAdministrationService.updateSpacePermissions(2l, spacePermissions);
    verify(space).setDeletePermissions(spacePermissions.getDeletePermissions());
    verify(space).setLayoutPermissions(spacePermissions.getLayoutPermissions());
    verify(space).setPublicSitePermissions(spacePermissions.getPublicSitePermissions());
    verify(spaceService).updateSpace(space);
  }

}
