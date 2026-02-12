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
package io.meeds.social.space.template.plugin.decorator;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateGroupDecoratorPluginTest {

  private static final String               TEMPLATE_GROUP_ID = "/space_templates/template1";

  @Mock
  private SpaceService                      spaceService;

  @Mock
  private SpaceTemplateService              spaceTemplateService;

  @Mock
  private OrganizationService               organizationService;

  @Mock
  private Group                             group;

  @InjectMocks
  private SpaceTemplateGroupDecoratorPlugin spaceTemplateGroupDecoratorPlugin;

  @Before
  public void init() {
    when(group.getId()).thenReturn(TEMPLATE_GROUP_ID);
  }

  @Test
  public void shouldReturnGroupAsIs_whenGroupIsNotSpaceTemplate() {
    when(group.getId()).thenReturn("/spaces/my-space");
    Group result = spaceTemplateGroupDecoratorPlugin.decorate(group);
    verifyNoInteractions(spaceTemplateService);
    Assert.assertEquals(result.getEnclosingMemberships(), group.getEnclosingMemberships());
  }

  @Test
  public void shouldReturnGroupAsIs_whenSpaceTemplateIsNull() {
    when(spaceTemplateService.getSpaceTemplateByGroupId(TEMPLATE_GROUP_ID)).thenReturn(null);

    Group result = spaceTemplateGroupDecoratorPlugin.decorate(group);

    verify(spaceTemplateService).getSpaceTemplateByGroupId(TEMPLATE_GROUP_ID);
    Assert.assertEquals(result.getEnclosingMemberships(), group.getEnclosingMemberships());
  }


  @Test
  public void shouldDecorateGroupWithEnclosingMemberships() {
    SpaceTemplate spaceTemplate = mock(SpaceTemplate.class);
    when(spaceTemplateService.getSpaceTemplateByGroupId(TEMPLATE_GROUP_ID)).thenReturn(spaceTemplate);

    when(spaceTemplate.getEnclosingMemberships()).thenReturn(List.of("*:~:/test/test_group"));

    Group result = spaceTemplateGroupDecoratorPlugin.decorate(group);

    verify(group).setEnclosingMemberships(argThat(memberships -> memberships.size() == 1
        && memberships.stream().map(NestedMembership::getGroupId).noneMatch(TEMPLATE_GROUP_ID::equals)));

    Assert.assertEquals(result.getEnclosingMemberships(), group.getEnclosingMemberships());

  }

}
