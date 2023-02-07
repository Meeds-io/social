/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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

package org.exoplatform.social.core.profileproperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;

public class ProfilePropertyServiceImpl implements ProfilePropertyService, Startable {

  private static final Log                           LOG                                 =
                                                         ExoLogger.getLogger(ProfilePropertyServiceImpl.class);

  private final ProfileSettingStorage                profileSettingStorage;

  protected List<ProfilePropertyDatabaseInitializer> profielPropertyPlugins              =
                                                                            new ArrayList<>();

  private final List<String>                         synchronizedGroupDisabledProperties = Arrays.asList(Profile.FULL_NAME,
                                                                                                         Profile.FIRST_NAME,
                                                                                                         Profile.LAST_NAME,
                                                                                                         Profile.EMAIL,
                                                                                                         Profile.CONTACT_PHONES,
                                                                                                         "phones.work",
                                                                                                         "phones.home",
                                                                                                         "phones.other",
                                                                                                         Profile.CONTACT_IMS,
                                                                                                         "ims.facebook",
                                                                                                         "ims.msn",
                                                                                                         "ims.jitsi",
                                                                                                         "ims.skype",
                                                                                                         "ims.other",
                                                                                                         Profile.CONTACT_URLS);

  public ProfilePropertyServiceImpl(ProfileSettingStorage profileSettingStorage) {
    this.profileSettingStorage = profileSettingStorage;
  }

  @Override
  public List<ProfilePropertySetting> getPropertySettings() {
    return profileSettingStorage.getPropertySettings();
  }

  @Override
  public List<ProfilePropertySetting> getSynchronizedPropertySettings() {
    return profileSettingStorage.getSynchronizedPropertySettings();
  }

  @Override
  public ProfilePropertySetting getProfileSettingByName(String name) {
    return profileSettingStorage.findProfileSettingByName(name);
  }

  @Override
  public ProfilePropertySetting createPropertySetting(ProfilePropertySetting profilePropertySetting) throws ObjectAlreadyExistsException {
    if (profilePropertySetting == null) {
      throw new IllegalArgumentException("Profile property setting Item Object is mandatory");
    }
    if (StringUtils.isBlank(profilePropertySetting.getPropertyName())) {
      throw new IllegalArgumentException("Profile property name is mandatory");
    }
    ProfilePropertySetting storedProfilePropertySetting =
                                                        profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    if (storedProfilePropertySetting != null) {
      throw new ObjectAlreadyExistsException(storedProfilePropertySetting, "A profile property with provided name already exist");
    }
    if (!isGroupSynchronizedEnabledProperty(profilePropertySetting)) {
      profilePropertySetting.setGroupSynchronized(false);
    }
    profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
    if (profilePropertySetting.getOrder() == null) {
      profilePropertySetting.setOrder(profilePropertySetting.getId());
      profilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, false);
    }
    return profilePropertySetting;
  }

  @Override
  public void updatePropertySetting(ProfilePropertySetting profilePropertySetting) {
    if (!isGroupSynchronizedEnabledProperty(profilePropertySetting)) {
      profilePropertySetting.setGroupSynchronized(false);
    }
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, false);
  }

  @Override
  public void deleteProfilePropertySetting(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Profile Property Setting Technical Identifier is mandatory");
    }
    profileSettingStorage.deleteProfilePropertySetting(id);
  }

  @Override
  public boolean isGroupSynchronizedEnabledProperty(ProfilePropertySetting profilePropertySetting) {
    if (synchronizedGroupDisabledProperties.contains(profilePropertySetting.getPropertyName())) {
      return false;
    }
    if (profilePropertySetting.getParentId() != null && profilePropertySetting.getParentId() > 0) {
      ProfilePropertySetting parent = profileSettingStorage.getProfileSettingById(profilePropertySetting.getParentId());
      return parent == null || !synchronizedGroupDisabledProperties.contains(parent.getPropertyName());
    }
    return true;
  }

  @Override
  public List<String> getPropertySettingNames() {
    return getPropertySettings().stream().map(ProfilePropertySetting::getPropertyName).toList();
  }

  @Override
  public void start() {

    for (ProfilePropertyDatabaseInitializer plugin : profielPropertyPlugins) {
      try {
        plugin.init(this);
      } catch (Exception ex) {
        LOG.error("Failed start Profile properties Service , probably because of configuration error. Error occurs when initializing properties for {}", plugin.getClass().getName(),ex);
      }
    }

  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  @Override
  public void addProfilePropertyPlugin(ComponentPlugin profilePropertyInitPlugin) {
    this.profielPropertyPlugins.add((ProfilePropertyDatabaseInitializer) profilePropertyInitPlugin);
  }

}
