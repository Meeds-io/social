/*
 * 
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */
package org.exoplatform.social.core.plugin;

import io.meeds.social.translation.plugin.TranslationPlugin;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.gatein.api.Portal;
import org.gatein.api.Util;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;

public class SiteTranslationPlugin extends TranslationPlugin {

  public static final String SITE_OBJECT_TYPE            = "site";

  public static final String SITE_DESCRIPTION_FIELD_NAME = "description";

  public static final String SITE_TITLE_FIELD_NAME       = "title";

  private Portal             portal;

  private LayoutService layoutService;

  public SiteTranslationPlugin(Portal portal, LayoutService layoutService) {
    this.portal = portal;
    this.layoutService = layoutService;
  }

  @Override
  public String getObjectType() {
    return SITE_OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long siteId, String username) throws ObjectNotFoundException {
    PortalConfig portalConfig = layoutService.getPortalConfig(siteId);
    SiteId id = Util.from(new SiteKey(portalConfig.getType(), portalConfig.getName()));
    Site site = portal.getSite(id);
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    return userACL.hasPermission(Util.from(site.getEditPermission())[0]);
  }

  @Override
  public boolean hasEditPermission(long siteId, String username) throws ObjectNotFoundException {
    PortalConfig portalConfig = layoutService.getPortalConfig(siteId);
    SiteId id = Util.from(new SiteKey(portalConfig.getType(), portalConfig.getName()));
    Site site = portal.getSite(id);
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    return userACL.hasPermission(Util.from(site.getAccessPermission())[0]);
  }

  @Override
  public long getAudienceId(long programId) throws ObjectNotFoundException {
    return 0L;
  }

  @Override
  public long getSpaceId(long programId) throws ObjectNotFoundException {
    return 0L;
  }

}
