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

package org.exoplatform.social.core.profileproperty.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertyOptionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertyOptionEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

public class ProfileSettingStorage {

  private final ProfilePropertySettingDAO profilePropertySettingDAO;

  private final ProfilePropertyOptionDAO  profilePropertyOptionDAO;

  public ProfileSettingStorage(ProfilePropertySettingDAO profilePropertySettingDAO,
                               ProfilePropertyOptionDAO profilePropertyOptionDAO) {
    this.profilePropertySettingDAO = profilePropertySettingDAO;
    this.profilePropertyOptionDAO = profilePropertyOptionDAO;
  }

  public List<ProfilePropertySetting> getPropertySettings() {
    return profilePropertySettingDAO.findOrderedSettings().stream().map(this::convertFromEntity).toList();
  }

  public List<ProfilePropertySetting> getSynchronizedPropertySettings() {
    return profilePropertySettingDAO.findSynchronizedSettings().stream().map(this::convertFromEntity).toList();
  }

  public ProfilePropertySetting findProfileSettingByName(String name) {
    return convertFromEntity(profilePropertySettingDAO.findProfileSettingByName(name));
  }

  public ProfilePropertySetting getProfileSettingById(Long id) {
    return convertFromEntity(profilePropertySettingDAO.find(id));
  }

  public ProfilePropertySetting saveProfilePropertySetting(ProfilePropertySetting profilePropertySetting, boolean isNew) {
    profilePropertySetting.setUpdated(System.currentTimeMillis());
    if (isNew) {
      ProfilePropertySettingEntity newProfilePropertySettingEntity =
                                                                   profilePropertySettingDAO.create(convertToEntity(profilePropertySetting));
      return convertFromEntity(newProfilePropertySettingEntity);
    } else {
      return convertFromEntity(profilePropertySettingDAO.update(convertToEntity(profilePropertySetting)));
    }
  }

  public void deleteProfilePropertySetting(Long id) {
    profilePropertySettingDAO.delete(profilePropertySettingDAO.find(id));
  }

  public List<ProfilePropertyOption> getProfilePropertyOptions(Long propertySettingId, int offset, int limit) {
    return toPropertyOptions(profilePropertyOptionDAO.findPropertyOptionsBySettingId(propertySettingId, offset, limit));
  }

  private ProfilePropertySettingEntity convertToEntity(ProfilePropertySetting profilePropertySetting) {
    if (profilePropertySetting == null) {
      return null;
    }
    ProfilePropertySettingEntity profilePropertySettingEntity = new ProfilePropertySettingEntity();
    profilePropertySettingEntity.setId(profilePropertySetting.getId());
    profilePropertySettingEntity.setActive(profilePropertySetting.isActive());
    profilePropertySettingEntity.setEditable(profilePropertySetting.isEditable());
    profilePropertySettingEntity.setDropdownList(profilePropertySetting.isDropdownList());
    profilePropertySettingEntity.setVisible(profilePropertySetting.isVisible());
    profilePropertySettingEntity.setPropertyName(profilePropertySetting.getPropertyName());
    profilePropertySettingEntity.setParentId(profilePropertySetting.getParentId());
    profilePropertySettingEntity.setRequired(profilePropertySetting.isRequired());
    profilePropertySettingEntity.setGroupSynchronized(profilePropertySetting.isGroupSynchronized());
    profilePropertySettingEntity.setOrder(profilePropertySetting.getOrder());
    profilePropertySettingEntity.setMultiValued(profilePropertySetting.isMultiValued());
    profilePropertySettingEntity.setHiddenable(profilePropertySetting.isHiddenbale());
    profilePropertySettingEntity.setIndexInAnalytics(profilePropertySetting.isIndexInAnalytics());
    profilePropertySettingEntity.setPropertyType(profilePropertySetting.getPropertyType());
    profilePropertySettingEntity.setPropertyOptions(toPropertyOptionEntities(profilePropertySettingEntity, profilePropertySetting.getPropertyOptions()));
    profilePropertySettingEntity.setUpdatedDate(new Date(profilePropertySetting.getUpdated()));
    return profilePropertySettingEntity;
  }

  private ProfilePropertySetting convertFromEntity(ProfilePropertySettingEntity profilePropertySettingEntity) {
    if (profilePropertySettingEntity == null) {
      return null;
    }
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setId(profilePropertySettingEntity.getId());
    profilePropertySetting.setActive(profilePropertySettingEntity.isActive());
    profilePropertySetting.setEditable(profilePropertySettingEntity.isEditable());
    profilePropertySetting.setDropdownList(profilePropertySettingEntity.isDropdownList());
    profilePropertySetting.setVisible(profilePropertySettingEntity.isVisible());
    profilePropertySetting.setPropertyName(profilePropertySettingEntity.getPropertyName());
    profilePropertySetting.setParentId(profilePropertySettingEntity.getParentId());
    profilePropertySetting.setRequired(profilePropertySettingEntity.isRequired());
    profilePropertySetting.setGroupSynchronized(profilePropertySettingEntity.isGroupSynchronized());
    profilePropertySetting.setOrder(profilePropertySettingEntity.getOrder());
    profilePropertySetting.setMultiValued(profilePropertySettingEntity.isMultiValued());
    profilePropertySetting.setHiddenbale(profilePropertySettingEntity.isHiddenable());
    profilePropertySetting.setIndexInAnalytics(profilePropertySettingEntity.isIndexInAnalytics());
    profilePropertySetting.setPropertyType(profilePropertySettingEntity.getPropertyType());
    profilePropertySetting.setPropertyOptions(toPropertyOptions(profilePropertySettingEntity.getPropertyOptions()));
    profilePropertySetting.setUpdated(profilePropertySettingEntity.getUpdatedDate().getTime());
    profilePropertySetting.setHasChildProperties(hasChildProperties(profilePropertySettingEntity.getId()));
    return profilePropertySetting;
  }

  private ProfilePropertyOptionEntity toPropertyOptionEntity(ProfilePropertySettingEntity profilePropertySettingEntity,
                                                             ProfilePropertyOption profilePropertyOption) {
    if (profilePropertyOption == null) {
      return null;
    }
    return new ProfilePropertyOptionEntity(profilePropertyOption.getId(),
                                           profilePropertyOption.getValue(),
                                           profilePropertyOption.getPropertySettingId() != null ? profilePropertySettingDAO.find(profilePropertyOption.getPropertySettingId())
                                                                                                : profilePropertySettingEntity);

  }

  private boolean hasChildProperties(Long id) {
    return profilePropertySettingDAO.hasChildProperties(id);
  }

  private ProfilePropertyOption fromPropertyOptionEntity(ProfilePropertyOptionEntity profilePropertyOptionEntity) {
    if (profilePropertyOptionEntity == null) {
      return null;
    }
    return new ProfilePropertyOption(profilePropertyOptionEntity.getId(),
                                     profilePropertyOptionEntity.getValue(),
                                     profilePropertyOptionEntity.getPropertySetting().getId());
  }

  private List<ProfilePropertyOptionEntity> toPropertyOptionEntities(ProfilePropertySettingEntity profilePropertySettingEntity,
                                                                     List<ProfilePropertyOption> profilePropertyOptions) {
    if (profilePropertyOptions == null || profilePropertyOptions.isEmpty()) {
      return new ArrayList<>();
    }
    return profilePropertyOptions.stream()
                                 .map(profileOption -> toPropertyOptionEntity(profilePropertySettingEntity, profileOption))
                                 .collect(Collectors.toList());
  }

  private List<ProfilePropertyOption> toPropertyOptions(List<ProfilePropertyOptionEntity> profilePropertyOptionEntities) {
    if (profilePropertyOptionEntities == null) {
      return new ArrayList<>();
    }
    return profilePropertyOptionEntities.stream().map(this::fromPropertyOptionEntity).toList();
  }

}
