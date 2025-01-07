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

import io.meeds.social.translation.plugin.TranslationPlugin;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;

public class ProfilePropertySettingOptionTranslation extends TranslationPlugin {

  private final UserACL      userACL;

  public static final String PROFILE_PROPERTY_SETTING_OPTION_OBJECT_TYPE = "propertySettingOption";

  public ProfilePropertySettingOptionTranslation(UserACL userACL) {
    this.userACL = userACL;
  }

  @Override
  public String getObjectType() {
    return PROFILE_PROPERTY_SETTING_OPTION_OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long objectId, String username) throws ObjectNotFoundException {
    return userACL.isAdministrator(userACL.getUserIdentity(username));
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
