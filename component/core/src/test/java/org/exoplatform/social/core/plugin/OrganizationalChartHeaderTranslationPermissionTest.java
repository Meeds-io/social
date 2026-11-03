/**
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
package org.exoplatform.social.core.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.social.organizationalchart.service.OrganizationalChartSettingService;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationalChartHeaderTranslationPermissionTest {

  @Mock
  private OrganizationalChartSettingService     organizationalChartSettingService;

  private OrganizationalChartHeaderTranslation plugin;

  @Before
  public void setup() {
    plugin = new OrganizationalChartHeaderTranslation();
    plugin.setOrganizationalChartSettingService(organizationalChartSettingService);
  }

  @Test
  public void hasEditPermissionOnlyForWhoCanEditTheSettings() {
    when(organizationalChartSettingService.canEdit("admin")).thenReturn(true);
    assertTrue(plugin.hasEditPermission("125", "admin"));
    assertFalse(plugin.hasEditPermission("125", "john"));
  }

  @Test
  public void hasAccessPermissionForEveryone() {
    assertTrue(plugin.hasAccessPermission("125", "john"));
  }

}
