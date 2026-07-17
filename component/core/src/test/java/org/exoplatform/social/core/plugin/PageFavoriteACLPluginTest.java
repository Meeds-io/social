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
package org.exoplatform.social.core.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;

@RunWith(MockitoJUnitRunner.class)
public class PageFavoriteACLPluginTest {

  private static final String     STORAGE_ID = "page_139";

  @Mock
  private LayoutService           layoutService;

  @Mock
  private UserACL                 userACL;

  @InjectMocks
  private PageFavoriteACLPlugin   plugin;

  private Identity                userIdentity;

  @Before
  public void setup() {
    userIdentity = mock(Identity.class);
  }

  @Test
  public void shouldExposePageEntityType() {
    assertEquals("page", plugin.getEntityType());
  }

  @Test
  public void shouldReturnFalseWhenPageNotFound() {
    when(layoutService.getPage(139L)).thenReturn(null);

    assertFalse(plugin.canCreateFavorite(userIdentity, STORAGE_ID));
  }

  @Test
  public void shouldReturnTrueWhenUserHasAccessToPage() {
    Page page = mock(Page.class);
    when(layoutService.getPage(139L)).thenReturn(page);
    when(userACL.hasAccessPermission(page, userIdentity)).thenReturn(true);

    assertTrue(plugin.canCreateFavorite(userIdentity, STORAGE_ID));
  }

  @Test
  public void shouldReturnFalseWhenUserHasNoAccessToPage() {
    Page page = mock(Page.class);
    when(layoutService.getPage(139L)).thenReturn(page);
    when(userACL.hasAccessPermission(page, userIdentity)).thenReturn(false);

    assertFalse(plugin.canCreateFavorite(userIdentity, STORAGE_ID));
  }

}
