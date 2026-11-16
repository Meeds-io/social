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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.template.model.SpaceTemplate;

/**
 * A change of a space template's enclosing memberships invalidates, on every
 * node, the conversations of the users it reaches, read from the store
 * whichever node they signed in through: the members of the spaces created
 * from the template, who hold no stored membership in the template group,
 * and the template group's own members, who are in none of those spaces.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateMembershipListenerTest {

  private static final String     SPACE_TEMPLATE_CREATED_EVENT = "space.template.created";

  private static final String     SPACE_TEMPLATE_UPDATED_EVENT = "space.template.updated";

  private static final String     TEMPLATE_GROUP_ID            = "/space_templates/test";

  private static final String     ENCLOSING_GROUP_ID           = "/enclosing/test";

  private static final long       TEMPLATE_ID                  = 3;

  @Mock
  private ListenerService         listenerService;

  @Mock
  private OrganizationService     organizationService;

  @Mock
  private GroupHandler            groupHandler;

  @Mock
  private MembershipHandler       membershipHandler;

  @Mock
  private ConversationRegistry    conversationRegistry;

  @Mock
  private SpaceService            spaceService;

  @InjectMocks
  private SpaceTemplateMembershipListener listener;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    Group group = mock(Group.class);
    when(groupHandler.findGroupById(TEMPLATE_GROUP_ID)).thenReturn(group);
    ListAccess<Membership> memberships = mock(ListAccess.class);
    when(membershipHandler.findAllMembershipsByGroup(group)).thenReturn(memberships);
    Membership directMembership = mock(Membership.class);
    when(directMembership.getUserName()).thenReturn("paul");
    when(memberships.getSize()).thenReturn(1);
    when(memberships.load(0, 1)).thenReturn(new Membership[] { directMembership });

    Space space = new Space();
    space.setMembers(new String[] { "john", "mary" });
    space.setManagers(new String[] { "mary" });
    ListAccess<Space> spaces = mock(ListAccess.class);
    when(spaces.getSize()).thenReturn(1);
    when(spaces.load(0, 1)).thenReturn(new Space[] { space });
    when(spaceService.getAllSpacesByFilter(argThat(filter -> filter.getTemplateIds().equals(List.of(TEMPLATE_ID)))))
                                                                                                       .thenReturn(spaces);
  }

  @Test
  public void shouldClearCacheAndInvalidateTheStoredMembersOnCreation() throws Exception {
    SpaceTemplate template = template(List.of(String.format("*:~:%s", ENCLOSING_GROUP_ID)));

    listener.onEvent(new Event<>(SPACE_TEMPLATE_CREATED_EVENT, null, template));

    verify(groupHandler).clearGroupCache(TEMPLATE_GROUP_ID);
    verify(conversationRegistry).invalidateUser("john");
    verify(conversationRegistry).invalidateUser("mary");
    verify(conversationRegistry).invalidateUser("paul");
    verify(conversationRegistry, never()).unregisterByUserId(anyString());
  }

  @Test
  public void shouldInvalidateNobodyWhenTheEnclosingMembershipsDoNotChange() throws Exception {
    SpaceTemplate template = template(List.of(String.format("*:~:%s", ENCLOSING_GROUP_ID)));

    listener.onEvent(new Event<>(SPACE_TEMPLATE_UPDATED_EVENT, template, template));

    verify(conversationRegistry, never()).invalidateUser(anyString());
  }

  @Test
  public void shouldClearCacheAndInvalidateTheStoredMembersOnUpdate() throws Exception {
    SpaceTemplate storedTemplate = template(List.of(String.format("*:~:%s", ENCLOSING_GROUP_ID)));
    SpaceTemplate updatedTemplate = template(Collections.emptyList());

    listener.onEvent(new Event<>(SPACE_TEMPLATE_UPDATED_EVENT, storedTemplate, updatedTemplate));

    verify(groupHandler).clearGroupCache(TEMPLATE_GROUP_ID);
    verify(conversationRegistry).invalidateUser("john");
    verify(conversationRegistry).invalidateUser("mary");
    verify(conversationRegistry).invalidateUser("paul");
  }

  private SpaceTemplate template(List<String> enclosingMemberships) {
    SpaceTemplate template = mock(SpaceTemplate.class);
    when(template.getGroupId()).thenReturn(TEMPLATE_GROUP_ID);
    when(template.getId()).thenReturn(TEMPLATE_ID);
    when(template.getEnclosingMemberships()).thenReturn(enclosingMemberships);
    return template;
  }

}
