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
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.space.category.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.service.CategoryLinkService;
import io.meeds.social.space.plugin.SpaceCategoryPlugin;

@RunWith(MockitoJUnitRunner.class)
public class SpaceCategoryServiceTest {

  private static final long   SPACE_ID         = 7l;

  private static final String USER             = "mary";

  private static final long   LINKABLE_ID      = 11l;

  private static final long   NOT_LINKABLE_ID  = 12l;

  private static final long   OLD_ID           = 13l;

  @Mock
  private SpaceService        spaceService;

  @Mock
  private CategoryLinkService categoryLinkService;

  @InjectMocks
  private SpaceCategoryServiceImpl spaceCategoryService;

  private Space space() {
    Space space = new Space();
    space.setId("7");
    space.setCategoryIds(List.of(OLD_ID));
    when(spaceService.getSpaceById(SPACE_ID)).thenReturn(space);
    return space;
  }

  private CategoryObject spaceObject() {
    return new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, "7", SPACE_ID);
  }

  @Test
  public void updateAsUserLinksOnlyWhatTheUserMayLink() throws Exception {
    space();
    doThrow(IllegalAccessException.class).when(categoryLinkService).link(eq(NOT_LINKABLE_ID), any(), eq(USER));

    spaceCategoryService.updateSpaceCategories(SPACE_ID, List.of(LINKABLE_ID, NOT_LINKABLE_ID), false, USER);

    verify(categoryLinkService).link(LINKABLE_ID, spaceObject(), USER);
    verify(categoryLinkService).link(NOT_LINKABLE_ID, spaceObject(), USER);
    verify(categoryLinkService, never()).link(anyLong(), any());
  }

  @Test
  public void updateAsUserUnlinksAsTheUser() throws Exception {
    space();

    spaceCategoryService.updateSpaceCategories(SPACE_ID, List.of(), true, USER);

    verify(categoryLinkService).unlink(OLD_ID, spaceObject(), USER);
    verify(categoryLinkService, never()).unlink(anyLong(), any());
  }

  @Test
  public void updateWithoutUserKeepsTheSystemLinkForTheSpaceCreationListener() throws Exception {
    space();

    spaceCategoryService.updateSpaceCategories(SPACE_ID, List.of(LINKABLE_ID), false);

    verify(categoryLinkService).link(LINKABLE_ID, spaceObject());
    verify(categoryLinkService, never()).link(anyLong(), any(), any());
  }

}
