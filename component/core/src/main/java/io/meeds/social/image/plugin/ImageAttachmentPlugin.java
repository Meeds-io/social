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
package io.meeds.social.image.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentPlugin;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.service.CMSService;

public class ImageAttachmentPlugin extends AttachmentPlugin {

  public static final String OBJECT_TYPE = "imagePortlet";

  private CMSService         cmsService;

  private IdentityManager    identityManager;

  private SpaceService       spaceService;

  public ImageAttachmentPlugin(CMSService cmsService,
                               SpaceService spaceService,
                               IdentityManager identityManager) {
    this.cmsService = cmsService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(Identity userIdentity, String settingName) throws ObjectNotFoundException {
    return cmsService.hasAccessPermission(userIdentity, OBJECT_TYPE, settingName);
  }

  @Override
  public boolean hasEditPermission(Identity userIdentity, String settingName) throws ObjectNotFoundException {
    return cmsService.hasEditPermission(userIdentity, OBJECT_TYPE, settingName);
  }

  @Override
  public long getAudienceId(String settingName) throws ObjectNotFoundException {
    long spaceId = getSpaceId(settingName);
    if (spaceId > 0) {
      Space space = spaceService.getSpaceById(String.valueOf(spaceId));
      if (space != null) {
        return Long.parseLong(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getId());
      }
    }
    return 0;
  }

  @Override
  public long getSpaceId(String settingName) throws ObjectNotFoundException {
    CMSSetting setting = cmsService.getSetting(OBJECT_TYPE, settingName);
    return setting == null ? 0l : setting.getSpaceId();
  }

}
