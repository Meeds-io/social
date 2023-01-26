/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.settings.ProfilePropertySettingsService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.storage.api.IdentityStorage;

public class SocialUserProfileEventListenerImpl extends UserProfileEventListener {

  private static final Log                     LOG                   =
                                                   ExoLogger.getLogger(SocialUserProfileEventListenerImpl.class);

  private final List<String>                   exlcudedAttributeList = List.of("authenticationAttempts", "latestAuthFailureTime");

  private final IdentityManager                identityManager;

  private final ProfilePropertySettingsService profilePropertySettingsService;

  public SocialUserProfileEventListenerImpl(IdentityManager identityManager,
                                            ProfilePropertySettingsService profilePropertySettingsService) {
    this.identityManager = identityManager;
    this.profilePropertySettingsService = profilePropertySettingsService;
  }

  @Override
  public void postSave(UserProfile userProfile, boolean isNew) throws Exception {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userProfile.getUserName());
    Profile profile = identity.getProfile();
    String uGender = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]);// "user.gender"
    String uPosition = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]);// user.jobtitle
    String pGender = (String) profile.getProperty(Profile.GENDER);
    String pPosition = (String) profile.getProperty(Profile.POSITION);

    AtomicBoolean hasUpdated = new AtomicBoolean(false);
    Map<String, String> properties = userProfile.getUserInfoMap();
    exlcudedAttributeList.forEach(properties.keySet()::remove);
    properties.forEach((name, value) -> {
      updateProfilePropertySettings(name, profilePropertySettingsService);
      if (isNew) {
        profile.setProperty(name, value);
      } else if (!StringUtils.equals((String) profile.getProperty(name), userProfile.getAttribute(name))) {
        profile.setProperty(name, value);
        hasUpdated.set(true);
      }
    });

    if (!StringUtils.equals(uGender, pGender)) {
      profile.setProperty(Profile.GENDER, uGender);
      hasUpdated.set(true);
    }
    if (!StringUtils.equals(uPosition, pPosition)) {
      profile.setProperty(Profile.POSITION, uPosition);
      hasUpdated.set(true);
    }

    if (hasUpdated.get()) {
      List<Profile.UpdateType> updateTypes = new ArrayList<>();
      updateTypes.add(Profile.UpdateType.CONTACT);
      profile.setListUpdateTypes(updateTypes);
    }

    if (hasUpdated.get() && !isNew) {
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      identityManager.updateProfile(profile);
    }

    if (isNew) {
      IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);
      identityStorage.updateProfile(profile);
    }
  }

  private void updateProfilePropertySettings(String propertyName, ProfilePropertySettingsService profilePropertySettingsService) {
    ProfilePropertySetting propertySetting = profilePropertySettingsService.getProfileSettingByName(propertyName);
    if (propertySetting == null) {
      ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
      profilePropertySetting.setPropertyName(propertyName);
      profilePropertySetting.setMultiValued(false);
      profilePropertySetting.setActive(true);
      profilePropertySetting.setEditable(false);
      profilePropertySetting.setVisible(true);
      profilePropertySetting.setParentId(null);
      try {
        profilePropertySettingsService.createPropertySetting(profilePropertySetting);
      } catch (ObjectAlreadyExistsException e) {
        LOG.error("Error while adding new profile setting property", e);
      }
    }
  }

}
