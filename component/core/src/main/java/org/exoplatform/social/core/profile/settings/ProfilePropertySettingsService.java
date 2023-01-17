/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.profile.settings;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.storage.api.ProfileSettingStorage;
import org.picocontainer.Startable;

public class ProfilePropertySettingsService implements Startable {

  private static final Log      LOG = ExoLogger.getLogger(ProfilePropertySettingsService.class);

  private final ProfileSettingStorage profileSettingStorage;

  private final SettingService    settingService;

  private static final String CREATE_PROFILE_DEFAULT_SETTING_KEY = "social.profile.default.settings";
  private final List<String> predifinedSystemProperties = Arrays.asList(Profile.FIRST_NAME,Profile.LAST_NAME,Profile.EMAIL,Profile.POSITION,Profile.COMPANY,Profile.LOCATION,Profile.DEPARTMENT,Profile.TEAM,Profile.PROFESSION,Profile.COUNTRY,Profile.CITY,Profile.CONTACT_PHONES,"phones.work","phones.home","phones.other",Profile.CONTACT_IMS,"ims.facebook","ims.msn","ims.jitsi","ims.skype","ims.other",Profile.CONTACT_URLS);

  public ProfilePropertySettingsService(ProfileSettingStorage profileSettingStorage, SettingService settingService) {
    this.profileSettingStorage = profileSettingStorage;
    this.settingService = settingService;
  }

  public List<ProfilePropertySetting> getPropertySettings() {
    return profileSettingStorage.getPropertySettings();
  }

  public List<ProfilePropertySetting> getSynchronizedPropertySettings() {
    return profileSettingStorage.getSynchronizedPropertySettings();
  }

  public ProfilePropertySetting getProfileSettingByName(String name) {
    return profileSettingStorage.findProfileSettingByName(name);
  }

  public ProfilePropertySetting createPropertySetting(ProfilePropertySetting profilePropertySetting) throws ObjectAlreadyExistsException {
    if (profilePropertySetting == null) {
      throw new IllegalArgumentException("Profile property setting Item Object is mandatory");
    }
    if (StringUtils.isBlank(profilePropertySetting.getPropertyName())) {
      throw new IllegalArgumentException("Profile property name is mandatory");
    }
    ProfilePropertySetting storedProfilePropertySetting = profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    if (storedProfilePropertySetting!=null) {
      throw new ObjectAlreadyExistsException(storedProfilePropertySetting,"A profile property with provided name already exist");
    }
    return profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, true);
  }
  public void updatePropertySetting(ProfilePropertySetting profilePropertySetting) {
    profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, false);
  }

  public void deleteProfilePropertySetting(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Profile Property Setting Technical Identifier is mandatory");
    }
    profileSettingStorage.deleteProfilePropertySetting(id);
  }

  @Override
  public void start() {
    SettingValue<?> settingsValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, CREATE_PROFILE_DEFAULT_SETTING_KEY);
    if(settingsValue == null){
      LOG.info("Start Creation of default Profile properties Settings");
      int index = 1;
      int numCreated = 1;
      for (String propertyName : predifinedSystemProperties){
        if(getProfileSettingByName(propertyName)== null){
          try {
            Long parentId=null;
            if(propertyName.contains(".")){
              ProfilePropertySetting parent = getProfileSettingByName(propertyName.split("\\.")[0]);
              if(parent!=null){
                parentId = parent.getId();
              }
            }
            ProfilePropertySetting profilePropertySetting =  new ProfilePropertySetting();
            profilePropertySetting.setPropertyName(propertyName);
            profilePropertySetting.setSystemProperty(true);
            profilePropertySetting.setActive(true);
            profilePropertySetting.setEditable(true);
            profilePropertySetting.setOrder(Long.valueOf(index));
            profilePropertySetting.setVisible(true);
            profilePropertySetting.setGroupSynchronized(false);
            profilePropertySetting.setRequired(false);
            profilePropertySetting.setParentId(parentId);
            createPropertySetting(profilePropertySetting);
            numCreated++;
          } catch (ObjectAlreadyExistsException e) {
            LOG.warn("Property setting already exist");
          } catch (Exception e) {
            LOG.warn("PException occured when trying to create {}  profile setting",propertyName);
          }
        }
        index++;
      }
      settingService.set(Context.GLOBAL, Scope.GLOBAL, CREATE_PROFILE_DEFAULT_SETTING_KEY, SettingValue.create("Done"));
      LOG.info("End Of creation of {} default Profile properties Settings",numCreated);
    }
  }

  @Override
  public void stop() {
    // Nothing to stop
  }
}
