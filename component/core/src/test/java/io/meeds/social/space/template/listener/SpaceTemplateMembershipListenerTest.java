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
package io.meeds.social.space.template.listener;

import io.meeds.social.space.template.model.SpaceTemplate;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateMembershipListenerTest {

  private static final String SPACE_TEMPLATE_CREATED_EVENT = "space.template.created";

  private static final String SPACE_TEMPLATE_UPDATED_EVENT = "space.template.updated";

  @Mock
  private ListenerService listenerService;

  @Mock
  private OrganizationService organizationService;

  @Mock
  private GroupHandler groupHandler;

  @Mock
  private IdentityRegistry identityRegistry;

  @Mock
  private ConversationRegistry conversationRegistry;

  @InjectMocks
  private SpaceTemplateMembershipListener listener;

  @Before
  public void setUp() {
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
  }

  @Test
  public void shouldClearCacheAndRefreshIdentitiesOnCreation() throws Exception {
    SpaceTemplate template = mock(SpaceTemplate.class);
    String enclosingGroupId = "/enclosing/test";
    String spaceTemplateGroupId = "/space_tesmplates/test";
    String userName = "john";
    Identity identity = mock(Identity.class);
    MembershipEntry membershipEntry = mock(MembershipEntry.class);
    when(identity.getUserId()).thenReturn(userName);
    when(identity.getMemberships()).thenReturn(List.of(membershipEntry));
    when(membershipEntry.getGroup()).thenReturn(spaceTemplateGroupId);
    when(identityRegistry.getIdentities()).thenReturn(List.of(identity));
    when(template.getGroupId()).thenReturn(spaceTemplateGroupId);
    when(template.getEnclosingMemberships())
            .thenReturn(List.of(String.format("*:~:%s", enclosingGroupId)));
    //when
    Event<String, SpaceTemplate> event =
            new Event<>(SPACE_TEMPLATE_CREATED_EVENT, null, template);
    listener.onEvent(event);
    //Then
    verify(groupHandler).clearGroupCache(spaceTemplateGroupId);
    verify(identityRegistry).unregister(userName);
    verify(conversationRegistry).unregisterByUserId(userName);
  }

  @Test
  public void shouldClearCacheAndRefreshIdentitiesOnUpdate() throws Exception {
    // Given
    String groupId = "/space_templates/test";
    String enclosingGroupId = "/enclosing/test";
    String userName = "john";

    SpaceTemplate storedTemplate = mock(SpaceTemplate.class);
    SpaceTemplate updatedTemplate = mock(SpaceTemplate.class);

    Identity identity = mock(Identity.class);
    MembershipEntry membershipEntry = mock(MembershipEntry.class);

    when(identity.getUserId()).thenReturn(userName);
    when(identity.getMemberships()).thenReturn(List.of(membershipEntry));
    when(identityRegistry.getIdentities()).thenReturn(List.of(identity));

    when(storedTemplate.getGroupId()).thenReturn(groupId);
    when(updatedTemplate.getGroupId()).thenReturn(groupId);

    when(storedTemplate.getEnclosingMemberships()).thenReturn(List.of(String.format("*:~:%s", enclosingGroupId)));

    when(updatedTemplate.getEnclosingMemberships()).thenReturn(Collections.emptyList());

    when(membershipEntry.getGroup()).thenReturn(groupId);

    Event<SpaceTemplate, SpaceTemplate> event = new Event<>(SPACE_TEMPLATE_UPDATED_EVENT, storedTemplate, updatedTemplate);
    // When
    listener.onEvent(event);
    // Then
    verify(groupHandler).clearGroupCache(groupId);
    verify(identityRegistry).unregister(userName);
    verify(conversationRegistry).unregisterByUserId(userName);
  }

}
