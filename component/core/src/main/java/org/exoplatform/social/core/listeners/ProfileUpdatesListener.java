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

package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.EntityConverterUtils;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.storage.IdentityStorageException;

import java.io.IOException;
import java.util.Map;

public class ProfileUpdatesListener extends ProfileListenerPlugin {

  private static final String              DEFAULT_AVATAR        = "DEFAULT_AVATAR";

  private final IdentityManager identityManager;

  private final IdentityDAO     identityDAO;

  private final FileService     fileService;

  public ProfileUpdatesListener(IdentityManager identityManager, FileService fileService, IdentityDAO identityDAO) {
    this.identityManager = identityManager;
    this.identityDAO = identityDAO;
    this.fileService = fileService;
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    Profile profile = event.getProfile();

    if (profile != null) {
      Map<String, Object> properties = profile.getProperties();
      long id = EntityConverterUtils.parseId(profile.getIdentity().getId());
      IdentityEntity entity = identityDAO.find(id);
      if (entity != null) {
        Map<String, String> entityProperties = entity.getProperties();
        for (Map.Entry<String, Object> profileProperty : properties.entrySet()) {
          if ((Profile.FIRST_NAME.equalsIgnoreCase(profileProperty.getKey()) || Profile.LAST_NAME.equalsIgnoreCase(profileProperty.getKey()))
              && !profileProperty.getValue().equals(entityProperties.get(profileProperty.getKey()))) {
            FileItem file = identityManager.getAvatarFile(profile.getIdentity());
            if (file != null && file.getFileInfo().getName().equals(DEFAULT_AVATAR)) {
              fileService.deleteFile(file.getFileInfo().getId());
              entity.setAvatarFileId(null);
            }
          }
        }
      }
    }
  }
}
