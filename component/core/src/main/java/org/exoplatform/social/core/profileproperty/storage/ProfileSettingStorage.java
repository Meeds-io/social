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

import java.util.List;

import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

public class ProfileSettingStorage {

  private final ProfilePropertySettingDAO profilePropertySettingDAO;

  public ProfileSettingStorage(ProfilePropertySettingDAO profilePropertySettingDAO) {
    this.profilePropertySettingDAO = profilePropertySettingDAO;
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

  private ProfilePropertySettingEntity convertToEntity(ProfilePropertySetting profilePropertySetting) {
    if (profilePropertySetting == null) {
      return null;
    }
    ProfilePropertySettingEntity profilePropertySettingEntity = new ProfilePropertySettingEntity();
    profilePropertySettingEntity.setId(profilePropertySetting.getId());
    profilePropertySettingEntity.setActive(profilePropertySetting.isActive());
    profilePropertySettingEntity.setEditable(profilePropertySetting.isEditable());
    profilePropertySettingEntity.setVisible(profilePropertySetting.isVisible());
    profilePropertySettingEntity.setPropertyName(profilePropertySetting.getPropertyName());
    profilePropertySettingEntity.setParentId(profilePropertySetting.getParentId());
    profilePropertySettingEntity.setRequired(profilePropertySetting.isRequired());
    profilePropertySettingEntity.setGroupSynchronized(profilePropertySetting.isGroupSynchronized());
    profilePropertySettingEntity.setOrder(profilePropertySetting.getOrder());
    profilePropertySettingEntity.setMultiValued(profilePropertySetting.isMultiValued());
    profilePropertySettingEntity.setHiddenable(profilePropertySetting.isHiddenbale());
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
    profilePropertySetting.setVisible(profilePropertySettingEntity.isVisible());
    profilePropertySetting.setPropertyName(profilePropertySettingEntity.getPropertyName());
    profilePropertySetting.setParentId(profilePropertySettingEntity.getParentId());
    profilePropertySetting.setRequired(profilePropertySettingEntity.isRequired());
    profilePropertySetting.setGroupSynchronized(profilePropertySettingEntity.isGroupSynchronized());
    profilePropertySetting.setOrder(profilePropertySettingEntity.getOrder());
    profilePropertySetting.setMultiValued(profilePropertySettingEntity.isMultiValued());
    profilePropertySetting.setHiddenbale(profilePropertySettingEntity.isHiddenable());
    return profilePropertySetting;
  }

  public boolean hasChildProperties(Long parentId) {
    return !profilePropertySettingDAO.findChildProperties(parentId).isEmpty();
  }
}
