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
package io.meeds.social.space.plugin;

import io.meeds.services.organization.plugin.GroupDecoratorPlugin;
import io.meeds.social.space.template.service.SpaceTemplateService;
import lombok.SneakyThrows;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaceGroupDecoratorPlugin extends BaseComponentPlugin implements GroupDecoratorPlugin {

  private static final Log     LOG                  = ExoLogger.getLogger(SpaceGroupDecoratorPlugin.class);

  private static final String  SPACES_ROOT          = "/spaces/";

  private static final String  SPACE_TEMPLATES_ROOT = "/space_templates/";

  @Autowired
  private SpaceService         spaceService;

  @Autowired
  private SpaceTemplateService spaceTemplateService;

  @Autowired
  private OrganizationService  organizationService;

  @PostConstruct
  public void init() {
    organizationService.addDecoratorPlugin(this);
  }

  @Override
  public Group decorate(Group group) {
    if (group == null || !group.getId().startsWith(SPACES_ROOT)) {
      return group;
    }
    try {
      Space space = spaceService.getSpaceByGroupId(group.getId());
      if (space == null) {
        return group;
      }
      String templateGroupId = spaceTemplateService.getOrCreateSpaceTemplateGroupId(space.getTemplateId());
      Set<NestedMembership> enclosingMemberships = Optional.ofNullable(group.getEnclosingMemberships())
                                                           .orElseGet(Collections::emptySet)
                                                           .stream()
                                                           .filter(m -> !m.getGroupId().startsWith(SPACE_TEMPLATES_ROOT))
                                                           .collect(Collectors.toSet());
      enclosingMemberships.add(NestedMembership.builder().groupId(templateGroupId).nestedGroupId(group.getId()).build());
      group.setEnclosingMemberships(enclosingMemberships);
      return group;
    } catch (Exception e) {
      LOG.error("Failed to decorate space group {}. Group will be returned without decoration.", group.getId(), e);
      return group;
    }
  }
}
