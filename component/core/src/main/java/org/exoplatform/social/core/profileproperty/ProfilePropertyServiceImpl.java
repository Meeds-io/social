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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.picocontainer.Startable;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;

public class ProfilePropertyServiceImpl implements ProfilePropertyService, Startable {

  private static final Log                           LOG                                    =
                                                         ExoLogger.getLogger(ProfilePropertyServiceImpl.class);

  private final ProfileSettingStorage                profileSettingStorage;

  private final SettingService                       settingService;

  private final IndexingService                      indexingService;

  private static final String                        SYNCHRONIZED_DISABLED_PROPERTIES       = "synchronizationDisabledProperties";

  private static final String                        UNHIDDENABLE_PROPERTIES_PARAM          = "unHiddenableProperties";

  private static final String                        EXCLUDED_QUICK_SEARCH_PROPERTIES_PARAM = "excludedQuickSearchProperties";

  protected List<ProfilePropertyDatabaseInitializer> profielPropertyPlugins                 = new ArrayList<>();

  private List<String>                               synchronizedGroupDisabledProperties    = new ArrayList<>();

  private static List<String>                        nonHiddenableProps                     = new ArrayList<>();

  private static List<String>                        excludedQuickSearchProps               = new ArrayList<>();

  private static final Scope                         HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE =
                                                                                            Scope.APPLICATION.id("ProfilePropertySettings");

  private static final String                        HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY   = "HiddenProfilePropertySettings";

  public ProfilePropertyServiceImpl(InitParams params,
                                    ProfileSettingStorage profileSettingStorage,
                                    SettingService settingService,
                                    IndexingService indexingService) {
    this.profileSettingStorage = profileSettingStorage;
    this.settingService = settingService;
    this.indexingService = indexingService;
    if (params != null) {
      try {
        synchronizedGroupDisabledProperties = Arrays.asList(params.getValueParam(SYNCHRONIZED_DISABLED_PROPERTIES)
                                                                  .getValue()
                                                                  .split(","));
        nonHiddenableProps = Arrays.asList(params.getValueParam(UNHIDDENABLE_PROPERTIES_PARAM).getValue().split(","));
        excludedQuickSearchProps = Arrays.asList(params.getValueParam(EXCLUDED_QUICK_SEARCH_PROPERTIES_PARAM)
                                                       .getValue()
                                                       .split(","));
      } catch (Exception e) {
        LOG.warn("List of disabled properties for synchronization not provided, all properties can be synchronized! ");
      }
    }
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
  public ProfilePropertySetting getProfileSettingById(Long id) {
    return profileSettingStorage.getProfileSettingById(id);
  }

  @Override
  public List<String> getUnhiddenableProfileProperties() {
    return nonHiddenableProps;
  }

  @Override
  public List<String> getExcludedQuickSearchProperties() {
    return excludedQuickSearchProps;
  }

  @Override
  public boolean isPropertySettingHiddenable(ProfilePropertySetting propertySetting) {
    if (nonHiddenableProps.contains(propertySetting.getPropertyName()) || hasChildProperties(propertySetting)) {
      return false;
    }
    return propertySetting.isHiddenbale();
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
    if (profilePropertySetting.isHiddenbale()
        && getUnhiddenableProfileProperties().contains(profilePropertySetting.getPropertyName())) {
      throw new IllegalArgumentException(String.format("%s cannot be hidden", profilePropertySetting.getPropertyName()));
    }
    if (!isGroupSynchronizedEnabledProperty(profilePropertySetting)) {
      profilePropertySetting.setGroupSynchronized(false);
    }
    if (isDefaultProperties(profilePropertySetting)) {
      ProfilePropertySetting createdProfilePropertySetting =
                                                           profileSettingStorage.getProfileSettingById(profilePropertySetting.getId());
      profilePropertySetting.setMultiValued(createdProfilePropertySetting.isMultiValued());
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void hidePropertySetting(long userIdentityId, long profilePropertyId) {
    List<Long> hiddenProperties = getHiddenProfilePropertyIds(userIdentityId);hiddenProperties.remove(profilePropertyId);
    hiddenProperties.add(profilePropertyId);
    settingService.set(Context.USER.id(String.valueOf(userIdentityId)),
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE,
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY,
                       SettingValue.create(hiddenProperties.toString()));
    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(userIdentityId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void showPropertySetting(long userIdentityId, long profilePropertyId) {
    List<Long> hiddenProperties = getHiddenProfilePropertyIds(userIdentityId);
    hiddenProperties.remove(profilePropertyId);
    settingService.set(Context.USER.id(String.valueOf(userIdentityId)),
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE,
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY,
                       SettingValue.create(hiddenProperties.toString()));
    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(userIdentityId));
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public List<Long> getHiddenProfilePropertyIds(long userIdentityId) {
    List<Long> hiddenProfileProperties = new ArrayList<>();
    SettingValue<?> settingValue = settingService.get(Context.USER.id(String.valueOf(userIdentityId)),
                                                      HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE,
                                                      HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY);
    if (settingValue == null) {
      return hiddenProfileProperties;
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      hiddenProfileProperties = mapper.readValue(settingValue.getValue().toString(), new TypeReference<ArrayList<Long>>() {
      });
    } catch (Exception e) {
      LOG.error("Error while parsing hidden properties setting of user: {}", userIdentityId, e);
    }
    return hiddenProfileProperties;
  }

  @Override
  public void start() {

    for (ProfilePropertyDatabaseInitializer plugin : profielPropertyPlugins) {
      try {
        plugin.init(this);
      } catch (Exception ex) {
        LOG.error("Failed start Profile properties Service , probably because of configuration error. Error occurs when initializing properties for {}",
                  plugin.getClass().getName(),
                  ex);
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

  @Override
  public boolean hasChildProperties(ProfilePropertySetting propertySetting) {
    return profileSettingStorage.hasChildProperties(propertySetting.getId());
  }

  @Override
  public boolean isDefaultProperties(ProfilePropertySetting propertySetting) {
    for (ProfilePropertyDatabaseInitializer plugin : profielPropertyPlugins) {
      if (plugin.getConfig().getProfileProperties() != null && !plugin.getConfig().getProfileProperties().isEmpty()
          && plugin.getConfig()
                   .getProfileProperties()
                   .stream()
                   .anyMatch(profileProperty -> profileProperty.getPropertyName().equals(propertySetting.getPropertyName()))) {
        return true;
      }
    }
    return false;
  }
}
