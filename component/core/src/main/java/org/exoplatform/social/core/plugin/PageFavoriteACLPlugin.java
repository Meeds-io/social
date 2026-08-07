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
package org.exoplatform.social.core.plugin;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.metadata.FavoriteACLPlugin;

public class PageFavoriteACLPlugin extends FavoriteACLPlugin {

  private static final String PAGE_FAVORITE_TYPE     = "page";

  private static final String PAGE_STORAGE_ID_PREFIX  = "page_";

  private final LayoutService layoutService;

  private final UserACL       userACL;

  public PageFavoriteACLPlugin(LayoutService layoutService, UserACL userACL) {
    this.layoutService = layoutService;
    this.userACL = userACL;
  }

  @Override
  public String getEntityType() {
    return PAGE_FAVORITE_TYPE;
  }

  @Override
  public boolean canCreateFavorite(Identity userIdentity, String objectId) {
    Page page = layoutService.getPage(parseStorageId(objectId));
    return page != null && userACL.hasAccessPermission(page, userIdentity);
  }

  private long parseStorageId(String storageId) {
    return Long.parseLong(StringUtils.removeStart(storageId, PAGE_STORAGE_ID_PREFIX));
  }

}
