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
package io.meeds.social.organizationalunit.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.social.organizationalunit.storage.OrganizationalUnitStorage;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationalUnitGroupListenerTest {

  private static final String GROUP_ID = "/platform/test";

  private static final String LABEL    = "Test Group";

  @Mock
  private OrganizationService organizationService;

  @Mock
  private OrganizationalUnitStorage organizationalUnitStorage;

  @InjectMocks
  private OrganizationalUnitGroupListener listener;

  @Test
  public void testPostSaveUpdatesLabelWhenNotNew() throws Exception {
    Group group = mock(Group.class);
    when(group.getId()).thenReturn(GROUP_ID);
    when(group.getLabel()).thenReturn(LABEL);

    listener.postSave(group, false);

    verify(organizationalUnitStorage).updateLabel(GROUP_ID, LABEL);
  }

  @Test
  public void testPostSaveDoesNothingWhenNew() throws Exception {
    Group group = mock(Group.class);

    listener.postSave(group, true);

    verify(organizationalUnitStorage, never()).updateLabel(org.mockito.ArgumentMatchers.anyString(),
                                                            org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  public void testPostDeleteRemovesRowForGroup() throws Exception {
    Group group = mock(Group.class);
    when(group.getId()).thenReturn(GROUP_ID);

    listener.postDelete(group);

    verify(organizationalUnitStorage).deleteByGroupId(GROUP_ID);
  }

  @Test
  public void testInitRegistersListenerOnGroupHandler() {
    GroupHandler groupHandler = mock(GroupHandler.class);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);

    listener.init();

    verify(groupHandler).addGroupEventListener(listener);
  }

  @Test
  public void testDestroyUnregistersListenerFromGroupHandler() {
    GroupHandler groupHandler = mock(GroupHandler.class);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);

    listener.destroy();

    verify(groupHandler).removeGroupEventListener(listener);
  }

}
