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
package org.exoplatform.social.core.listeners;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

/**
 * Glue: when an edit of a property's options changes the string the profile
 * index holds for its values, hands the property to
 * {@link ProfilePropertyService#reindexPropertyHolders(String)}, which
 * reindexes its holders on its own thread.
 */
public class ProfilePropertySettingOptionTranslationListener extends Listener<Object, ProfilePropertySetting> {

  private final ProfilePropertyService profilePropertyService;

  public ProfilePropertySettingOptionTranslationListener(ProfilePropertyService profilePropertyService) {
    this.profilePropertyService = profilePropertyService;
  }

  @Override
  public void onEvent(Event<Object, ProfilePropertySetting> event) {
    ProfilePropertySetting propertySetting = event.getData();
    if (propertySetting != null) {
      profilePropertyService.reindexPropertyHolders(propertySetting.getPropertyName());
    }
  }
}
