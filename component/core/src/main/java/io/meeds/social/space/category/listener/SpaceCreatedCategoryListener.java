/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.space.category.listener;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.category.service.SpaceCategoryService;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

import jakarta.annotation.PostConstruct;

@Component
public class SpaceCreatedCategoryListener implements SpaceLifeCycleListener {

  @Autowired
  private SpaceService         spaceService;

  @Autowired
  private SpaceCategoryService spaceCategoryService;

  @Autowired
  private SpaceTemplateService spaceTemplateService;

  @PostConstruct
  public void init() {
    spaceService.registerSpaceLifeCycleListener(this);
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    if (space.getTemplateId() > 0) {
      SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplate(space.getTemplateId());
      if (spaceTemplate != null && CollectionUtils.isNotEmpty(spaceTemplate.getSpaceDefaultCategoryIds())) {
        spaceCategoryService.updateSpaceCategories(space.getSpaceId(),
                                                   spaceTemplate.getSpaceDefaultCategoryIds());
      }
    }
  }

}
