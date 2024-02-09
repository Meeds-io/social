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

import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

public interface ProfilePropertyService {
  String LABELS_OBJECT_TYPE = "profileProperty";

  /**
   * Retrive the list of all {@link ProfilePropertySetting} objects
   *
   * @return {@link List} of {@link ProfilePropertySetting}
   */
  List<ProfilePropertySetting> getPropertySettings();

  /**
   * Retrive the list of synchronized {@link ProfilePropertySetting} objects
   *
   * @return {@link List} of {@link ProfilePropertySetting}
   */
  List<ProfilePropertySetting> getSynchronizedPropertySettings();

  /**
   * Retrieves the ProfileProperty item with given {@link ProfilePropertySetting}
   * propertyName
   *
   * @return {@link ProfilePropertySetting} if exist or null if not
   */
  ProfilePropertySetting getProfileSettingById(Long id);

  /**
   * Retrieves the ProfileProperty item with given {@link ProfilePropertySetting}
   * propertyName
   *
   * @return {@link ProfilePropertySetting} if exist or null if not
   */
  ProfilePropertySetting getProfileSettingByName(String name);

  /**
   * Creates a new {@link ProfilePropertySetting} object
   *
   * @param profilePropertySetting {@link ProfilePropertySetting}
   * @return newly created {@link ProfilePropertySetting}
   */
  ProfilePropertySetting createPropertySetting(ProfilePropertySetting profilePropertySetting) throws ObjectAlreadyExistsException;

  /**
   * Updates the given {@link ProfilePropertySetting} object
   *
   * @param profilePropertySetting {@link ProfilePropertySetting}
   */
  void updatePropertySetting(ProfilePropertySetting profilePropertySetting);

  /**
   * Deletes the {@link ProfilePropertySetting} object with given Id
   *
   * @param id {@link Long}
   */
  void deleteProfilePropertySetting(Long id);

  /**
   * Checks if the given {@link ProfilePropertySetting} object can be synchronized
   * with a group
   *
   * @return {@link Boolean}
   */
  boolean isGroupSynchronizedEnabledProperty(ProfilePropertySetting profilePropertySetting);

  /**
   * Retreive the list {@link List} of {@link String} of property settings names
   *
   * @return {@link List} of {@link String}
   */
  List<String> getPropertySettingNames();

  void addProfilePropertyPlugin(ComponentPlugin profilePropertyInitPlugin);

  /**
   * Checks if the current property has child properties
   * @param propertySetting
   * @return Boolean : true if the current property has child properties
   */
  boolean hasChildProperties(ProfilePropertySetting propertySetting);

  /**
   * Checks if the current property is a default propertie
   * @param propertySetting
   * @return Boolean : true if the current property is a default propertie
   */
  boolean isDefaultProperties(ProfilePropertySetting propertySetting);

  /**
   * Gets unhiddenable properties
   *
   * @return {@link List} of {@link String}
   */
  List<String> getUnhiddenableProfileProperties();

  /**
   * Gets excluded quick search properties
   *
   * @return {@link List} of {@link String}
   */
  List<String> getExcludedQuickSearchProperties();

  /**
   * Checks if property is hiddenable
   *
   * @param propertySetting profile property setting
   * @return true if property is hiddenbale and false otherwise
   */
  boolean isPropertySettingHiddenable(ProfilePropertySetting propertySetting);

  /**
   * Hide profile property setting
   *
   * @param userIdentityId user identity id
   * @param profilePropertyId profile property id
   */
  void hidePropertySetting(long userIdentityId, long profilePropertyId);


  /**
   * Show profile property setting
   *
   * @param userIdentityId user identity id
   * @param profilePropertyId profile property id
   */
  void showPropertySetting(long userIdentityId, long profilePropertyId);

  /**
   * Get hidden profile property Ids
   *
   * @param userIdentityId user identity ide
   * @return {@link List} of {@link Long}
   */
  List<Long> getHiddenProfilePropertyIds(long userIdentityId);

}
