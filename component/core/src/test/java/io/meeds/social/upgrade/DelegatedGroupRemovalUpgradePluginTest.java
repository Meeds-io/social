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
package io.meeds.social.upgrade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;

class DelegatedGroupRemovalUpgradePluginTest {

  private static final String                DELEGATED_GROUP_ID = "/platform/delegated";

  private OrganizationService                organizationService;

  private GroupHandler                       groupHandler;

  private DelegatedGroupRemovalUpgradePlugin plugin;

  @BeforeEach
  public void setUp() {
    organizationService = mock(OrganizationService.class);
    groupHandler = mock(GroupHandler.class);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    plugin = new DelegatedGroupRemovalUpgradePlugin(mock(InitParams.class), organizationService);
  }

  @Test
  public void testProcessUpgradeRemovesGroup() throws Exception {
    Group group = mock(Group.class);
    when(groupHandler.findGroupById(DELEGATED_GROUP_ID)).thenReturn(group);

    plugin.processUpgrade("1.0", "2.0");

    verify(groupHandler).removeGroup(group, true);
  }

  @Test
  public void testProcessUpgradeWhenGroupDoesNotExist() throws Exception {
    when(groupHandler.findGroupById(DELEGATED_GROUP_ID)).thenReturn(null);

    plugin.processUpgrade("1.0", "2.0");

    verify(groupHandler, never()).removeGroup(any(), anyBoolean());
  }

  @Test
  public void testProcessUpgradeFailurePropagates() throws Exception {
    Group group = mock(Group.class);
    when(groupHandler.findGroupById(DELEGATED_GROUP_ID)).thenReturn(group);
    doThrow(new RuntimeException("store error")).when(groupHandler).removeGroup(group, true);

    assertThrows(IllegalStateException.class, () -> plugin.processUpgrade("1.0", "2.0"));
  }

}
