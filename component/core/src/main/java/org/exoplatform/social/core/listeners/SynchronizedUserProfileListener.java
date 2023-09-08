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
package org.exoplatform.social.core.listeners;

import java.util.*;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

public class SynchronizedUserProfileListener extends Listener<IDMExternalStoreImportService, Map<String, String>> {

  @Override
  public void onEvent(Event<IDMExternalStoreImportService, Map<String, String>> event) throws Exception {
    Map<String, String> userPropertiesMap = event.getData();
    String userName = userPropertiesMap.get("username");
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    if (userIdentity != null) {
      Profile profile = userIdentity.getProfile();
      Map<String, Object> internalUserProperties = profile.getProperties();
      Map<String, Object> externalUserProperties = buildProfilePropertiesMap(userPropertiesMap);
      boolean isModified = false;
      Set<String> externalUserProfileAttributes = externalUserProperties.keySet();
      for (String externalUserProfileAttribute : externalUserProfileAttributes) {
        if (!internalUserProperties.containsKey(externalUserProfileAttribute)
            || !Objects.equals(externalUserProperties.get(externalUserProfileAttribute),
                               internalUserProperties.get(externalUserProfileAttribute))) {
          // set the newly or modified property
          profile.setProperty(externalUserProfileAttribute, externalUserProperties.get(externalUserProfileAttribute));
          isModified = true;
        }
      }
      if (isModified) {
        // broadcast changes
        identityManager.updateProfile(cleanProfilePropertySetting(profile), true);
      }
    }
  }

  private Map<String, Object> buildProfilePropertiesMap(Map<String, String> externalUserPropertiesMap) {
    Map<String, Object> proprtiesMap = new HashMap<>();
    ProfilePropertyService profilePropertyService = CommonsUtils.getService(ProfilePropertyService.class);
    List<String> profilePropertySettingNames = profilePropertyService.getPropertySettingNames();
    for (Map.Entry<String, String> entry : externalUserPropertiesMap.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();
      if (profilePropertySettingNames.contains(name)
          || (name.contains(".") && profilePropertySettingNames.contains(name.substring(0, name.indexOf('.'))))) {
        ProfilePropertySetting propertySetting = name
                                                     .contains(".") ? profilePropertyService.getProfileSettingByName(name.substring(0, name.indexOf('.'))) : profilePropertyService.getProfileSettingByName(name);
        //
        String propertyName = propertySetting.getPropertyName();
        List<String> systemMultivaluedFields = Arrays.asList("user", "phones", "ims", "urls");
        if (systemMultivaluedFields.contains(propertyName)) {
          // child list is empty
          if (!proprtiesMap.containsKey(propertyName)) {
            List<Map<String, String>> maps = new ArrayList<>();
            Map<String, String> childProperty = new HashMap<>();
            childProperty.put("key", name);
            childProperty.put("value", value);
            maps.add(childProperty);
            proprtiesMap.put(propertyName, maps);
            // child list isn't empty
          } else {
            List<Map<String, String>> existingmaps = (List<Map<String, String>>) proprtiesMap.get(propertyName);
            Map<String, String> childProperty = new HashMap<>();
            childProperty.put("key", name);
            childProperty.put("value", value);
            existingmaps.add(childProperty);
            proprtiesMap.put(propertyName, existingmaps);
          }
        } else {
          proprtiesMap.put(name, value);
        }
      }
    }
    return proprtiesMap;
  }
  /*
   *  Remove the old saved multivalued property as a map of string key-value pairs.
   */
  private Profile cleanProfilePropertySetting(Profile internalProfile){
    Iterator<Map.Entry<String, Object>> iterator = internalProfile.getProperties().entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      if (key.contains(".") && key.split(".").length < 2) {
        String parentfield = key.substring(0, key.indexOf('.'));
        if (internalProfile.getProperties().containsKey(parentfield)) {
          iterator.remove();
        }
      }
    }
    return internalProfile;
  }
}
