/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.portlet;

import org.exoplatform.container.ExoContainerContext;

import io.meeds.social.link.service.LinkService;

public class LinkPortlet extends CMSPortlet {

  private static LinkService linkService;

  @Override
  protected void saveSettingName(String name, String pageReference, long spaceId) {
    getLinkService().initLinkSetting(name, pageReference, spaceId);
  }

  @Override
  protected boolean isSettingNameExists(String name) {
    return getLinkService().hasLinkSetting(name);
  }

  @Override
  protected boolean canEdit(String name) {
    return getLinkService().hasEditPermission(name, getCurrentAclIdentity());
  }

  private static LinkService getLinkService() {
    if (linkService == null) {
      linkService = ExoContainerContext.getService(LinkService.class);
    }
    return linkService;
  }

}
