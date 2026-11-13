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
package io.meeds.social.core.profileproperty.service;

import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.INVALID_EMAIL;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.INVALID_OPTION;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.INVALID_VALUE;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.NOT_ALLOWED;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.REQUIRED;
import static io.meeds.social.core.profileproperty.exception.ProfileFormFieldException.TOO_LONG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import io.meeds.social.core.profileproperty.constant.ProfileFormKind;
import io.meeds.social.core.profileproperty.exception.ProfileFormFieldException;
import io.meeds.social.core.profileproperty.model.ProfilePropertyValue;
import io.meeds.social.core.profileproperty.util.ProfilePropertyValueUtil;

/**
 * Owns the rules of the profile attributes the account and registration forms
 * write: which attributes a form lists, which values it accepts, and how they
 * are saved in the user's profile.
 */
@Service
public class ProfileFormFieldService {

  private static final String         USER_TYPE  = "user";

  private static final String         EMAIL_TYPE = "email";

  private static final String         VALUE_KEY  = "value";

  private static final char           NUL        = '\u0000';

  private final ProfileSettingStorage profileSettingStorage;

  private final IdentityStorage       identityStorage;

  private final IdentityManager       identityManager;

  public ProfileFormFieldService(ProfileSettingStorage profileSettingStorage,
                                 IdentityStorage identityStorage,
                                 IdentityManager identityManager) {
    this.profileSettingStorage = profileSettingStorage;
    this.identityStorage = identityStorage;
    this.identityManager = identityManager;
  }

  /**
   * Reads the attributes a form lists from the store, never from a cache, in
   * the settings' order: the settings whose flag for the form is on, keeping
   * only those a form can write whatever their stored flags say.
   *
   * @param  kind the form
   * @return      the form's allow-list
   */
  public List<ProfilePropertySetting> getSettings(ProfileFormKind kind) {
    return profileSettingStorage.getListedSettings(kind)
                                .stream()
                                .filter(setting -> isAllowed(setting, kind))
                                .toList();
  }

  /**
   * Checks the values a form sends against the form's allow-list and the value
   * rules, and builds them in the storage shape. Nothing is written.
   *
   * @param  values                     the values sent
   * @param  kind                       the form
   * @param  complete                   true on creation and registration, where
   *                                      every Required attribute of the form
   *                                      must be sent
   * @param  stored                     the user's profile on edition, null on
   *                                      creation and registration: on edition
   *                                      an attribute sent empty clears its
   *                                      value, and a dropdown value equal to a
   *                                      stored one is kept
   * @return                            the attributes to write, by name: a
   *                                    text for a single-valued attribute, an
   *                                    {@link ArrayList} of maps holding the
   *                                    value under "value" for a multi-valued
   *                                    one
   * @throws ProfileFormFieldException  the first value refused, with its code
   *                                      and attribute name
   */
  public Map<String, Object> validate(List<ProfilePropertyValue> values,
                                      ProfileFormKind kind,
                                      boolean complete,
                                      Profile stored) {
    Map<String, ProfilePropertySetting> settings = new LinkedHashMap<>();
    getSettings(kind).forEach(setting -> settings.put(setting.getPropertyName(), setting));

    List<ProfilePropertyValue> sentValues = values == null ? Collections.emptyList()
                                                           : values.stream().filter(Objects::nonNull).toList();
    for (ProfilePropertyValue sentValue : sentValues) {
      // The allow-list holds no reserved name, whatever the settings say
      if (!settings.containsKey(sentValue.getPropertyName())) {
        throw new ProfileFormFieldException(NOT_ALLOWED, sentValue.getPropertyName());
      }
    }

    Map<String, Object> profileValues = new LinkedHashMap<>();
    Set<String> sentNames = new HashSet<>();
    for (ProfilePropertyValue sentValue : sentValues) {
      ProfilePropertySetting setting = settings.get(sentValue.getPropertyName());
      sentNames.add(setting.getPropertyName());
      validateValue(setting, sentValue, stored, profileValues);
    }
    if (complete) {
      settings.values()
              .stream()
              .filter(ProfilePropertySetting::isRequired)
              .filter(setting -> !sentNames.contains(setting.getPropertyName()))
              .findFirst()
              .ifPresent(setting -> {
                throw new ProfileFormFieldException(REQUIRED, setting.getPropertyName());
              });
    }
    return profileValues;
  }

  /**
   * Writes validated values in a user's profile, once, with the contact event,
   * so that the group synchronization runs whatever this node's caches hold.
   *
   * @param  username                the user's login
   * @param  values                  the attributes to write, as
   *                                   {@link #validate} returns them
   * @param  modifier                the login of the user who writes
   * @throws ObjectNotFoundException when the user has no identity, or a
   *                                   deleted one
   */
  public void save(String username, Map<String, Object> values, String modifier) throws ObjectNotFoundException {
    Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, username);
    if (identity == null || identity.isDeleted()) {
      throw new ObjectNotFoundException("User " + username + " has no identity");
    }
    if (values == null || values.isEmpty()) {
      return;
    }
    Profile profile = identity.getProfile();
    values.forEach(profile::setProperty);
    profile.setListUpdateTypes(new ArrayList<>(List.of(Profile.UpdateType.CONTACT)));
    identityManager.updateProfile(profile, modifier, false);
  }

  private boolean isAllowed(ProfilePropertySetting setting, ProfileFormKind kind) {
    boolean listed = kind == ProfileFormKind.REGISTRATION ? setting.isListedOnRegistration() : setting.isListedOnAccountForm();
    return listed
           && setting.isActive()
           && setting.isEditable()
           && !isSubField(setting)
           && !setting.isHasChildProperties()
           && ProfilePropertyValueUtil.isListableName(setting.getPropertyName(), setting.isGroupSynchronized())
           && (kind != ProfileFormKind.REGISTRATION || !USER_TYPE.equals(setting.getPropertyType()));
  }

  private void validateValue(ProfilePropertySetting setting,
                             ProfilePropertyValue sentValue,
                             Profile stored,
                             Map<String, Object> profileValues) {
    String propertyName = setting.getPropertyName();
    List<String> entries = getEntries(setting, sentValue);
    if (entries.stream().anyMatch(entry -> entry.indexOf(NUL) >= 0)) {
      throw new ProfileFormFieldException(INVALID_VALUE, propertyName);
    }
    if (entries.isEmpty()) {
      if (setting.isRequired()) {
        throw new ProfileFormFieldException(REQUIRED, propertyName);
      } else if (stored != null) {
        profileValues.put(propertyName, setting.isMultiValued() ? new ArrayList<>() : "");
      }
      return;
    }
    if (!setting.isMultiValued() && ProfilePropertyValueUtil.readsBackAsList(entries.get(0))) {
      throw new ProfileFormFieldException(INVALID_VALUE, propertyName);
    }
    if (setting.isDropdownList()) {
      List<String> storedValues = stored == null ? Collections.emptyList()
                                                 : getStoredValues(stored.getProperty(propertyName), setting.isMultiValued());
      entries = entries.stream().map(entry -> toOptionValue(setting, entry, storedValues)).toList();
    }
    if (!setting.isMultiValued()
        && EMAIL_TYPE.equals(setting.getPropertyType())
        && !ProfilePropertyValueUtil.isValidEmail(entries.get(0))) {
      throw new ProfileFormFieldException(INVALID_EMAIL, propertyName);
    }
    if (setting.isGroupSynchronized()) {
      for (String entry : entries) {
        if (ProfilePropertyValueUtil.getCleanedLength(entry) == 0) {
          throw new ProfileFormFieldException(INVALID_VALUE, propertyName);
        } else if (ProfilePropertyValueUtil.getSynchronizedGroupIdLength(propertyName,
                                                                         entry) > ProfilePropertyValueUtil.MAX_GROUP_ID_LENGTH) {
          throw new ProfileFormFieldException(TOO_LONG, propertyName);
        }
      }
    }
    Object profileValue = setting.isMultiValued() ? toStorageList(entries) : entries.get(0);
    if (ProfilePropertyValueUtil.storedLength(profileValue) > ProfilePropertyValueUtil.MAX_STORED_VALUE_LENGTH) {
      throw new ProfileFormFieldException(TOO_LONG, propertyName);
    }
    profileValues.put(propertyName, profileValue);
  }

  /**
   * @return the non-blank values sent in the attribute's own shape; a value
   *         sent in the other shape is refused rather than ignored, since on
   *         edition ignoring it would clear the attribute
   */
  private List<String> getEntries(ProfilePropertySetting setting, ProfilePropertyValue sentValue) {
    List<String> values = sentValue.getValues() == null ? Collections.emptyList()
                                                        : sentValue.getValues().stream().filter(StringUtils::isNotBlank).toList();
    String value = StringUtils.isBlank(sentValue.getValue()) ? null : sentValue.getValue();
    if (setting.isMultiValued()) {
      if (value != null) {
        throw new ProfileFormFieldException(INVALID_VALUE, setting.getPropertyName());
      }
      return values;
    } else {
      if (!values.isEmpty()) {
        throw new ProfileFormFieldException(INVALID_VALUE, setting.getPropertyName());
      }
      return value == null ? Collections.emptyList() : List.of(value);
    }
  }

  /**
   * @return the option id the value names, as stored; else, on edition, the
   *         value itself when it is one of the attribute's stored values
   */
  private String toOptionValue(ProfilePropertySetting setting, String value, List<String> storedValues) {
    if (StringUtils.isNumeric(value) && setting.getPropertyOptions() != null) {
      try {
        long optionId = Long.parseLong(value);
        for (ProfilePropertyOption option : setting.getPropertyOptions()) {
          if (option != null && option.getId() != null && option.getId() == optionId) {
            return String.valueOf(option.getId());
          }
        }
      } catch (NumberFormatException e) {
        // Longer than an id: no option has it
      }
    }
    if (storedValues.contains(value)) {
      return value;
    }
    throw new ProfileFormFieldException(INVALID_OPTION, setting.getPropertyName());
  }

  /**
   * Reads a stored value as the profile's read side serves it, following the
   * stored value's own type, never the setting's: an empty text is no value,
   * another text one value, split on commas under a multi-valued attribute,
   * and a list its entries' values under any attribute.
   */
  private List<String> getStoredValues(Object storedValue, boolean multiValued) {
    if (storedValue instanceof String text) {
      if (text.isEmpty()) {
        return Collections.emptyList();
      }
      return multiValued ? Arrays.asList(StringUtils.split(text, ',')) : List.of(text);
    } else if (storedValue instanceof List<?> list) {
      return list.stream()
                 .filter(Map.class::isInstance)
                 .map(entry -> ((Map<?, ?>) entry).get(VALUE_KEY))
                 .filter(String.class::isInstance)
                 .map(String.class::cast)
                 .filter(StringUtils::isNotEmpty)
                 .toList();
    }
    return Collections.emptyList();
  }

  private ArrayList<Map<String, String>> toStorageList(List<String> entries) {
    ArrayList<Map<String, String>> list = new ArrayList<>();
    for (String entry : entries) {
      Map<String, String> map = new HashMap<>();
      map.put(VALUE_KEY, entry);
      list.add(map);
    }
    return list;
  }

  private boolean isSubField(ProfilePropertySetting setting) {
    return setting.getParentId() != null && setting.getParentId() > 0;
  }

}
