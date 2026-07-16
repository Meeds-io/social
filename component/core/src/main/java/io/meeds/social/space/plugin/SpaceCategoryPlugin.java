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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.category.plugin.CategoryPlugin;
import io.meeds.social.category.service.CategoryPluginService;

import jakarta.annotation.PostConstruct;

@Component
public class SpaceCategoryPlugin implements CategoryPlugin {

  public static final String OBJECT_TYPE = SpaceAclPlugin.OBJECT_TYPE;

  @Autowired
  private PortalContainer    container;

  @Autowired
  private SpaceService       spaceService;

  @PostConstruct
  public void init() {
    container.getComponentInstanceOfType(CategoryPluginService.class).addPlugin(this);
  }

  @Override
  public String getType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canAccess(String spaceId, String username) {
    return spaceService.canViewSpace(spaceService.getSpaceById(Long.parseLong(spaceId)), username);
  }

  @Override
  public boolean canEdit(String spaceId, String username) {
    return spaceService.canManageSpace(spaceService.getSpaceById(Long.parseLong(spaceId)), username);
  }

  @Override
  public List<Long> getCategoryIds() {
    return spaceService.getSpaceCategoryIds();
  }

}
