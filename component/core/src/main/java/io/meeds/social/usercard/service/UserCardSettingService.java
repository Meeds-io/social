/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.usercard.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.social.usercard.model.UserCardSettings;

/**
 * Saves which profile properties the user card displays, settings the
 * administrators choose for all the users
 */
@Service
public class UserCardSettingService {

  public static final String     INVALID_PROPERTY_MESSAGE = "userCard.invalidProperty";

  public static final String     FIRST_FIELD_KEY          = "UserCardFirstFieldSetting";

  public static final String     SECOND_FIELD_KEY         = "UserCardSecondFieldSetting";

  public static final String     THIRD_FIELD_KEY          = "UserCardThirdFieldSetting";

  public static final String     PHONE_KEY                = "UserDisplayedPhonePropertySetting";

  public static final String     EMAIL_KEY                = "UserDisplayedEmailPropertySetting";

  private static final Scope     USER_CARD_SCOPE          = new Scope(Scope.GLOBAL.getName(), "UserCardSettings");

  private static final String    TEXT_TYPE                = "text";

  private static final String    PHONE_TYPE               = "call";

  private static final String    EMAIL_TYPE               = "email";

  /** The property the user card displays at a place while none is stored */
  private static final Map<String, String> DEFAULT_PROPERTIES = Map.of(FIRST_FIELD_KEY, "position",
                                                                      SECOND_FIELD_KEY, "team",
                                                                      THIRD_FIELD_KEY, "city");

  @Autowired
  private SettingService         settingService;

  @Autowired
  private ProfilePropertyService profilePropertyService;

  @Autowired
  private UserACL                userAcl;

  /**
   * @param settings the profile properties the user card displays
   * @param username the user who saves the settings
   * @throws IllegalAccessException when the user isn't an administrator
   * @throws IllegalArgumentException when a property doesn't exist or can't
   *           be displayed at its place in the user card
   */
  public void saveSettings(UserCardSettings settings, String username) throws IllegalAccessException {
    if (StringUtils.isBlank(username) || !userAcl.isAdministrator(userAcl.getUserIdentity(username))) {
      throw new IllegalAccessException("User " + username + " can't edit the user card settings");
    }
    if (settings == null
        || !isValidProperty(FIRST_FIELD_KEY, settings.firstField(), TEXT_TYPE)
        || !isValidProperty(SECOND_FIELD_KEY, settings.secondField(), TEXT_TYPE)
        || !isValidProperty(THIRD_FIELD_KEY, settings.thirdField(), TEXT_TYPE)
        || (settings.displayedPhone() != null && !isValidProperty(PHONE_KEY, settings.displayedPhone(), PHONE_TYPE))
        || (settings.displayedEmail() != null && !isValidProperty(EMAIL_KEY, settings.displayedEmail(), EMAIL_TYPE))) {
      throw new IllegalArgumentException(INVALID_PROPERTY_MESSAGE);
    }
    saveSetting(FIRST_FIELD_KEY, settings.firstField());
    saveSetting(SECOND_FIELD_KEY, settings.secondField());
    saveSetting(THIRD_FIELD_KEY, settings.thirdField());
    saveSetting(PHONE_KEY, settings.displayedPhone());
    saveSetting(EMAIL_KEY, settings.displayedEmail());
  }

  /**
   * A property stored before, or the one displayed by default while none is
   * stored, keeps being accepted for its place even when it was deleted or
   * changed since, since the settings drawer sends it back with every save,
   * whatever the field the administrator changed
   */
  private boolean isValidProperty(String key, String propertyName, String propertyType) {
    if (StringUtils.isBlank(propertyName)) {
      return false;
    }
    SettingValue<?> storedValue = settingService.get(Context.GLOBAL, USER_CARD_SCOPE, key);
    if (storedValue != null && storedValue.getValue() != null) {
      if (propertyName.equals(storedValue.getValue().toString())) {
        return true;
      }
    } else if (propertyName.equals(DEFAULT_PROPERTIES.get(key))) {
      return true;
    }
    ProfilePropertySetting property = profilePropertyService.getProfileSettingByName(propertyName);
    return property != null
           && StringUtils.equals(propertyType, property.getPropertyType())
           && !property.isMultiValued()
           && !property.isHasChildProperties();
  }

  private void saveSetting(String key, String propertyName) {
    if (propertyName == null) {
      settingService.remove(Context.GLOBAL, USER_CARD_SCOPE, key);
    } else {
      settingService.set(Context.GLOBAL, USER_CARD_SCOPE, key, SettingValue.create(propertyName));
    }
  }

}
