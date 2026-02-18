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
package io.meeds.social.space.template.plugin.decorator;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.common.ContainerTransactional;
import io.meeds.services.organization.plugin.GroupDecoratorPlugin;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaceTemplateGroupDecoratorPlugin extends BaseComponentPlugin implements GroupDecoratorPlugin {

  private static final Log     LOG                  = ExoLogger.getLogger(SpaceTemplateGroupDecoratorPlugin.class);

  private static final String  SPACE_TEMPLATES_ROOT = "/space_templates/";

  @Autowired
  private SpaceTemplateService spaceTemplateService;

  @Autowired
  private OrganizationService  organizationService;

  @PostConstruct
  public void init() {
    organizationService.addDecoratorPlugin(this);
  }

  @Override
  @ContainerTransactional
  public Group decorate(Group group) {
    if (group == null || !group.getId().startsWith(SPACE_TEMPLATES_ROOT)) {
      return group;
    }
    try {
      SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByGroupId(group.getId());
      if (spaceTemplate == null) {
        return group;
      }

      Set<NestedMembership> templateEnclosingMembership =
                                                        Optional.ofNullable(spaceTemplate.getEnclosingMemberships())
                                                                .orElseGet(Collections::emptyList)
                                                                .stream()
                                                                .distinct()
                                                                .filter(SpaceTemplateGroupDecoratorPlugin::isValidEnclosingExpression)
                                                                .map(expression -> NestedMembership.parseEnclosingMembership(expression,
                                                                                                                             group.getId()))
                                                                .filter(nestedMembership -> !nestedMembership.getGroupId()
                                                                                                             .equals(group.getId()))
                                                                .collect(Collectors.toSet());

      group.setEnclosingMemberships(templateEnclosingMembership);
      group.setLabel(spaceTemplate.getName());
      return group;
    } catch (Exception e) {
      LOG.error("Failed to decorate space template group {}. Group will be returned without decoration.", group.getId(), e);
      return group;
    }
  }

  private static boolean isValidEnclosingExpression(String expression) {
    if (expression == null) {
      return false;
    }
    String[] parts = expression.split(":");
    return parts.length == 3;
  }
}
