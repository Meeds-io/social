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
package io.meeds.social.space.plugin;

import io.meeds.social.space.template.service.SpaceTemplateService;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpaceGroupDecoratorPluginTest {

  private static final String       SPACE_GROUP_ID    = "/spaces/my-space";

  private static final String       TEMPLATE_GROUP_ID = "/space_templates/template1";

  @Mock
  private SpaceService              spaceService;

  @Mock
  private SpaceTemplateService      spaceTemplateService;

  @Mock
  private OrganizationService       organizationService;

  @Mock
  private Group                     group;

  @Mock
  private Space                     space;

  @InjectMocks
  private SpaceGroupDecoratorPlugin spaceGroupDecoratorPlugin;

  @Before
  public void setUp() {
    when(group.getId()).thenReturn(SPACE_GROUP_ID);
  }

  @Test
  public void shouldReturnGroupAsIs_whenGroupIsNotSpaceGroup() {
    when(group.getId()).thenReturn("/platform/users");

    Group result = spaceGroupDecoratorPlugin.decorate(group);
    verifyNoInteractions(spaceService);
    Assert.assertEquals(result.getEnclosingMemberships(), group.getEnclosingMemberships());
  }

  @Test
  public void shouldDecorateGroupWithTemplateEnclosingMembership() throws ObjectNotFoundException {
    when(spaceService.getSpaceByGroupId(SPACE_GROUP_ID))
            .thenReturn(space);
    long templateId = 1L;
    when(space.getTemplateId()).thenReturn(templateId);
    when(spaceTemplateService.getOrCreateSpaceTemplateGroupId(templateId))
            .thenReturn(TEMPLATE_GROUP_ID);
    NestedMembership existing = NestedMembership.builder()
            .groupId("/existing/test")
            .nestedGroupId(SPACE_GROUP_ID)
            .build();
    NestedMembership templateMembership = NestedMembership.builder()
            .groupId(TEMPLATE_GROUP_ID)
            .nestedGroupId(SPACE_GROUP_ID)
            .build();
    when(group.getEnclosingMemberships()).thenReturn(Set.of(
            existing,
            NestedMembership.builder()
                    .groupId("/space_templates/old")
                    .nestedGroupId(SPACE_GROUP_ID)
                    .build()
    ));
    spaceGroupDecoratorPlugin.decorate(group);
    verify(group).setEnclosingMemberships(argThat(memberships ->
            memberships.size() == 2 &&
                    memberships.contains(existing) &&
                    memberships.contains(templateMembership)
    ));
  }

}
