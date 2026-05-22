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
package org.exoplatform.social.core.space.impl;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.EntityConverterUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.space.service.SpaceLayoutService;

public class SpaceRenamedListenerImpl extends SpaceListenerPlugin {

  private static final Log    LOG = ExoLogger.getLogger(SpaceRenamedListenerImpl.class);

  private SpaceLayoutService  spaceLayoutService;

  private SpaceService        spaceService;

  private OrganizationService organizationService;

  private IdentityManager     identityManager;

  private FileService         fileService;

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    renameGroupLabel(space);
    updateDefaultSpaceAvatar(space);
    updateSpaceUrl(space);
    getSpaceLayoutService().renameSpaceSite(space);
  }

  @ContainerTransactional
  private void renameGroupLabel(Space space) {
    try {
      GroupHandler groupHandler = getOrganizationService().getGroupHandler();
      Group group = groupHandler.findGroupById(space.getGroupId());
      group.setLabel(space.getDisplayName());
      groupHandler.saveGroup(group, true);
    } catch (Exception e) {
      LOG.error("Error while renaming, space group Label, ignore minor change and keep the old Group Label", e);
    }
  }

  private void updateDefaultSpaceAvatar(Space space) {
    Identity spaceIdentity = getIdentityManager().getOrCreateSpaceIdentity(space.getPrettyName());
    FileItem spaceAvatar = identityManager.getAvatarFile(spaceIdentity);
    if (spaceAvatar != null && spaceAvatar.getFileInfo().getId() != null
        && EntityConverterUtils.DEFAULT_AVATAR.equals(spaceAvatar.getFileInfo().getName())) {
      Profile profile = spaceIdentity.getProfile();
      profile.removeProperty(Profile.AVATAR);
      profile.setAvatarUrl(null);
      profile.setAvatarLastUpdated(null);
      profile.setProperty(Profile.FULL_NAME, space.getDisplayName());
      space.setAvatarAttachment(null);
      space.setAvatarLastUpdated(System.currentTimeMillis());
      identityManager.updateProfile(profile);
      getFileService().deleteFile(spaceAvatar.getFileInfo().getId());
    }
  }

  private void updateSpaceUrl(Space space) {
    String spaceUri = getSpaceLayoutService().getFirstSpacePageUri(space.getGroupId());
    if (!StringUtils.equals(spaceUri, space.getUrl())) {
      space.setUrl(StringUtils.firstNonBlank(spaceUri, space.getUrl(), Space.HOME_URL));
      getSpaceService().updateSpace(space);
    }
  }

  public SpaceLayoutService getSpaceLayoutService() {
    if (spaceLayoutService == null) {
      spaceLayoutService = ExoContainerContext.getService(SpaceLayoutService.class);
    }
    return spaceLayoutService;
  }

  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  public FileService getFileService() {
    if (fileService == null) {
      fileService = ExoContainerContext.getService(FileService.class);
    }
    return fileService;
  }

  public IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }

  public OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = ExoContainerContext.getService(OrganizationService.class);
    }
    return organizationService;
  }
}
