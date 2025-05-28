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
package io.meeds.social.category.plugin;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;

import io.meeds.social.category.service.CategoryLinkService;

public class DefaultCategoryPlugin implements CategoryPlugin {

  public final PortalContainer container;

  public final String          objectType;

  private UserACL              userAcl;

  private CategoryLinkService  categoryLinkService;

  public DefaultCategoryPlugin(PortalContainer container, String objectType) {
    this.container = container;
    this.objectType = objectType;
  }

  @Override
  public String getType() {
    return objectType;
  }

  @Override
  public boolean canAccess(String newsId, String username) {
    return getUserAcl().hasAccessPermission(objectType, newsId, username);
  }

  @Override
  public boolean canEdit(String newsId, String username) {
    return getUserAcl().hasEditPermission(objectType, newsId, username);
  }

  @Override
  public List<Long> getCategoryIds() {
    return getCategoryLinkService().getLinkedIds(objectType);
  }

  public UserACL getUserAcl() {
    if (userAcl == null) {
      userAcl = container.getComponentInstanceOfType(UserACL.class);
    }
    return userAcl;
  }

  public CategoryLinkService getCategoryLinkService() {
    if (categoryLinkService == null) {
      categoryLinkService = container.getComponentInstanceOfType(CategoryLinkService.class);
    }
    return categoryLinkService;
  }

}
