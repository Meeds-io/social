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
package io.meeds.social.cms.service;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.cms.model.CMSSetting;
import io.meeds.social.cms.storage.CMSStorage;

public class CMSServiceImpl implements CMSService {

  private LayoutService layoutService;

  private SpaceService  spaceService;

  private CMSStorage    cmsStorage;

  private UserACL       userACL;

  public CMSServiceImpl(LayoutService layoutService,
                        SpaceService spaceService,
                        CMSStorage cmsStorage,
                        UserACL userACL) {
    this.layoutService = layoutService;
    this.spaceService = spaceService;
    this.cmsStorage = cmsStorage;
    this.userACL = userACL;
  }

  @Override
  public CMSSetting getSetting(String type, String name) {
    return cmsStorage.getSetting(type, name);
  }

  @Override
  public boolean hasAccessPermission(Identity identity, String type, String name) {
    CMSSetting setting = getSetting(type, name);
    return setting == null ? isContentManager(identity) :
                           hasAccessPermission(identity, setting.getPageReference(), setting.getSpaceId());
  }

  @Override
  public boolean hasAccessPermission(Identity identity, String pageReference, long spaceId) {
    String[] accessPermissions = getAccessPermissions(pageReference);
    return (accessPermissions != null && accessPermissions.length > 0 && StringUtils.equals(UserACL.EVERYONE, accessPermissions[0]))
           || (identity != null && Arrays.stream(accessPermissions).anyMatch(perm -> userACL.hasPermission(identity, perm)))
           || hasEditPermission(identity, pageReference, spaceId);
  }

  @Override
  public boolean hasEditPermission(Identity identity, String type, String name) {
    CMSSetting setting = getSetting(type, name);
    return setting == null ? isContentManager(identity) :
                           hasEditPermission(identity, setting.getPageReference(), setting.getSpaceId());
  }

  @Override
  public boolean hasEditPermission(Identity identity, String pageReference, long spaceId) {
    if (identity == null || StringUtils.equals(identity.getUserId(), IdentityConstants.ANONIM)) {
      return false;
    }
    String editPermission = getEditPermission(pageReference);
    boolean hasEditPermission = isContentManager(identity)
                                || (editPermission != null && userACL.hasPermission(identity, editPermission));
    if (hasEditPermission || spaceId <= 0) {
      return hasEditPermission;
    } else {
      String username = identity.getUserId();
      Space space = spaceService.getSpaceById(String.valueOf(spaceId));
      return spaceService.isSuperManager(username)
             || (space != null && (spaceService.isPublisher(space, username) || spaceService.isManager(space, username)));
    }
  }

  @Override
  public void saveSettingName(String type,
                              String name,
                              String pageReference,
                              long spaceId,
                              long userCreatorId) throws org.exoplatform.commons.ObjectAlreadyExistsException {
    if (StringUtils.isBlank(pageReference)) {
      throw new IllegalArgumentException("pageReference is mandatory");
    }
    if (StringUtils.isBlank(type)) {
      throw new IllegalArgumentException("contentType is mandatory");
    }
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("name is mandatory");
    }
    cmsStorage.saveSetting(type, name, pageReference, spaceId, userCreatorId);
  }

  @Override
  public boolean isSettingNameExists(String type, String name) {
    return getSetting(type, name) != null;
  }

  private boolean isContentManager(Identity identity) {
    return identity.isMemberOf(userACL.getAdminGroups())
           || identity.isMemberOf(SpaceUtils.PLATFORM_PUBLISHER_GROUP, SpaceUtils.PUBLISHER);
  }

  private String[] getAccessPermissions(String pageReference) {
    Page page = layoutService.getPage(pageReference);
    return page == null ? new String[0] : page.getAccessPermissions();
  }

  private String getEditPermission(String pageReference) {
    Page page = layoutService.getPage(pageReference);
    return page == null ? null : page.getEditPermission();
  }

}
