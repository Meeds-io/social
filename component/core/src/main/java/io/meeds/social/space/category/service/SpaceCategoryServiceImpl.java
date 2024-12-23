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
package io.meeds.social.space.category.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.plugin.SpaceCategoryPlugin;
import io.meeds.social.category.service.CategoryLinkService;

@Service
public class SpaceCategoryServiceImpl implements SpaceCategoryService {

  private static final Log    LOG = ExoLogger.getLogger(SpaceCategoryServiceImpl.class);

  @Autowired
  private SpaceService        spaceService;

  @Autowired
  private CategoryLinkService categoryLinkService;

  @Override
  public List<Long> getSpaceCategoryIds(long spaceId) {
    return categoryLinkService.getLinkedIds(new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE,
                                                               String.valueOf(spaceId),
                                                               spaceId));
  }

  @Override
  public void updateSpaceCategories(long spaceId, // NOSONAR
                                    List<Long> categoryIds,
                                    boolean removeExisting) {
    Space space = spaceService.getSpaceById(spaceId);
    List<Long> oldCategoryIds = space.getCategoryIds() == null ? Collections.emptyList() : space.getCategoryIds();
    List<Long> newCategoryIds = categoryIds == null ? Collections.emptyList() : categoryIds;
    if (removeExisting && CollectionUtils.isNotEmpty(oldCategoryIds)) {
      oldCategoryIds.stream()
                    .filter(id -> !newCategoryIds.contains(id))
                    .forEach(id -> {
                      try {
                        categoryLinkService.unlink(id,
                                                   new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE,
                                                                      space.getId(),
                                                                      space.getSpaceId()));
                      } catch (Exception e) {
                        if (LOG.isDebugEnabled()) {
                          LOG.warn("Error while deleting category with id {} from space with id {}", id, space.getId(), e);
                        } else {
                          LOG.warn("Error while deleting category with id {}", id, space.getId());
                        }
                      }
                    });
    }
    if (CollectionUtils.isNotEmpty(newCategoryIds)) {
      newCategoryIds.stream()
                    .filter(id -> !oldCategoryIds.contains(id))
                    .forEach(id -> {
                      try {
                        categoryLinkService.link(id,
                                                 new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE,
                                                                    space.getId(),
                                                                    space.getSpaceId()));
                      } catch (Exception e) {
                        if (LOG.isDebugEnabled()) {
                          LOG.warn("Error while updating link of category with id {} from space with id {}",
                                   id,
                                   space.getId(),
                                   e);
                        } else {
                          LOG.warn("Error while deleting link of category with id {}", id, space.getId());
                        }
                      }
                    });
    }
  }

}
