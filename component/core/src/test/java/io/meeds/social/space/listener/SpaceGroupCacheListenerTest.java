/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.space.listener;

import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpaceGroupCacheListenerTest {

  @Mock
  private OrganizationService     organizationService;

  @Mock
  private GroupHandler            groupHandler;

  @Mock
  private ConversationRegistry    conversationRegistry;

  @Mock
  private SpaceService            spaceService;

  @InjectMocks
  private SpaceGroupCacheListener listener;

  @Before
  public void setup() {
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
  }

  @Test
  public void shouldClearCacheAndUnregisterOnSpaceCreated() {
    // Given
    Space space = mock(Space.class);
    SpaceLifeCycleEvent event = mock(SpaceLifeCycleEvent.class);

    when(event.getSpace()).thenReturn(space);
    when(event.getTarget()).thenReturn("john");
    when(space.getGroupId()).thenReturn("/spaces/test");

    // When
    listener.spaceCreated(event);

    // Then
    verify(groupHandler).clearGroupCache("/spaces/test");
    verify(conversationRegistry).invalidateUser("john");
    verify(conversationRegistry, never()).unregisterByUserId(anyString());
  }

  @Test
  public void shouldClearCacheAndUnregisterAllMembersOnTemplateApplied() {
    // Given
    Space space = mock(Space.class);
    SpaceLifeCycleEvent event = mock(SpaceLifeCycleEvent.class);

    when(event.getSpace()).thenReturn(space);
    when(space.getGroupId()).thenReturn("/spaces/test");
    when(space.getMembers()).thenReturn(new String[] { "john", "mary" });

    // When
    listener.templateApplied(event);

    // Then
    verify(groupHandler).clearGroupCache("/spaces/test");
    verify(conversationRegistry).invalidateUser("john");
    verify(conversationRegistry).invalidateUser("mary");
    verify(conversationRegistry, never()).unregisterByUserId(anyString());
  }

}
