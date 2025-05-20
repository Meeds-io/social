/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;

import io.meeds.social.translation.plugin.TranslationPlugin;
import io.meeds.social.translation.service.TranslationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class SpaceCreationLabelTranslation extends TranslationPlugin {

  public static final String SPACE_CREATION_OBJECT_TYPE = "spaceCreation";

  @Autowired
  private TranslationService  translationService;

  @Autowired
  private UserACL            userACL;

  @PostConstruct
  public void init() {
    translationService.addPlugin(this);
  }

  @Override
  public String getObjectType() {
    return SPACE_CREATION_OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long objectId, String username) throws ObjectNotFoundException {
    return true;
  }

  @Override
  public boolean hasEditPermission(long objectId, String username) throws ObjectNotFoundException {
    return userACL.isAdministrator(userACL.getUserIdentity(username));
  }

  @Override
  public long getAudienceId(long objectId) throws ObjectNotFoundException {
    return 0;
  }

  @Override
  public long getSpaceId(long objectId) throws ObjectNotFoundException {
    return 0;
  }

}
