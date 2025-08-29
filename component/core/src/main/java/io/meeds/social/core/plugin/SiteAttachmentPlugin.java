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
package io.meeds.social.core.plugin;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentPlugin;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.portal.mop.plugin.SiteAclPlugin;

import jakarta.annotation.PostConstruct;

@Component
public class SiteAttachmentPlugin extends AttachmentPlugin {

  public static final String  OBJECT_TYPE            = "site";

  private static final String SITE_WITH_ID_NOT_FOUND = "Site with id '%s' not found";

  @Autowired
  private IdentityManager     identityManager;

  @Autowired
  private SpaceService        spaceService;

  @Autowired
  private LayoutService       layoutService;

  @Autowired
  private UserACL             userAcl;

  @Autowired
  private AttachmentService   attachmentService;

  @PostConstruct
  public void init() {
    attachmentService.addPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(Identity userIdentity, String siteId) throws ObjectNotFoundException {
    return userAcl.hasAccessPermission(SiteAclPlugin.OBJECT_TYPE, siteId, userIdentity);
  }

  @Override
  public boolean hasEditPermission(Identity userIdentity, String siteId) throws ObjectNotFoundException {
    return userAcl.hasEditPermission(SiteAclPlugin.OBJECT_TYPE, siteId, userIdentity);
  }

  @Override
  public long getAudienceId(String siteId) throws ObjectNotFoundException {
    PortalConfig portalConfig = layoutService.getPortalConfig(Long.parseLong(siteId));
    if (portalConfig == null) {
      throw new ObjectNotFoundException(String.format(SITE_WITH_ID_NOT_FOUND, siteId));
    } else if (isSpaceSite(portalConfig)) {
      Space space = spaceService.getSpaceByGroupId(portalConfig.getName());
      return getSpaceIdentityId(space);
    } else if (isUserSite(portalConfig)) {
      return getUserIdentityId(portalConfig);
    } else {
      return 0;
    }
  }

  @Override
  public long getSpaceId(String siteId) throws ObjectNotFoundException {
    PortalConfig portalConfig = layoutService.getPortalConfig(Long.parseLong(siteId));
    if (portalConfig == null) {
      throw new ObjectNotFoundException(String.format(SITE_WITH_ID_NOT_FOUND, siteId));
    } else if (isSpaceSite(portalConfig)) {
      Space space = spaceService.getSpaceByGroupId(portalConfig.getName());
      return space == null ? 0 : space.getSpaceId();
    } else {
      return 0;
    }
  }

  private long getSpaceIdentityId(Space space) {
    if (space != null) {
      return identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getIdentityId();
    } else {
      return 0;
    }
  }

  private long getUserIdentityId(PortalConfig portalConfig) {
    org.exoplatform.social.core.identity.model.Identity userIdentity =
                                                                     identityManager.getOrCreateUserIdentity(portalConfig.getName());
    if (userIdentity != null) {
      return userIdentity.getIdentityId();
    } else {
      return 0;
    }
  }

  private boolean isSpaceSite(PortalConfig portalConfig) {
    return StringUtils.equalsIgnoreCase(portalConfig.getType(),
                                        PortalConfig.GROUP_TYPE)
           && StringUtils.startsWith(portalConfig.getName(),
                                     SpaceUtils.SPACE_GROUP);
  }

  private boolean isUserSite(PortalConfig portalConfig) {
    return StringUtils.equalsIgnoreCase(portalConfig.getType(), PortalConfig.USER_TYPE);
  }

}
