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

package org.exoplatform.social.core.profilelabel.storage;

import java.util.List;

import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfileLabelDAO;
import org.exoplatform.social.core.jpa.storage.entity.ProfileLabelEntity;
import org.exoplatform.social.core.model.ProfileLabel;

public class ProfileLabelStorage {

  private final ProfileLabelDAO labelDAO;

  public ProfileLabelStorage(ProfileLabelDAO labelDAO) {
    this.labelDAO = labelDAO;
  }

  public ProfileLabel findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language) {
    ProfileLabelEntity profileLabelEntity = labelDAO.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, language);
    return convertFromEntity(profileLabelEntity);
  }

  public List<ProfileLabel> findLabelByObjectTypeAndObjectId(String objectType, String objectId) {
    List<ProfileLabelEntity> labelEntities = labelDAO.findLabelByObjectTypeAndObjectId(objectType, objectId);
    return labelEntities.stream().map(this::convertFromEntity).toList();
  }

  public ProfileLabel saveLabel(ProfileLabel profileLabel, boolean isNew) {
    ProfileLabelEntity profileLabelEntity = convertToEntity(profileLabel);
    if (isNew) {
      profileLabelEntity = labelDAO.create(profileLabelEntity);
      return convertFromEntity(profileLabelEntity);
    } else {
      profileLabelEntity = labelDAO.update(profileLabelEntity);
      return convertFromEntity(profileLabelEntity);
    }
  }

  public void deleteLabel(Long id) {
    ProfileLabelEntity profileLabelEntity = labelDAO.find(id);
    labelDAO.delete(profileLabelEntity);
  }

  private ProfileLabelEntity convertToEntity(ProfileLabel profileLabel) {
    if (profileLabel == null) {
      return null;
    }
    ProfileLabelEntity profileLabelEntity = new ProfileLabelEntity();
    profileLabelEntity.setId(profileLabel.getId());
    profileLabelEntity.setObjectType(profileLabel.getObjectType());
    profileLabelEntity.setObjectId(profileLabel.getObjectId());
    profileLabelEntity.setLabel(profileLabel.getLabel());
    profileLabelEntity.setLanguage(profileLabel.getLanguage());
    return profileLabelEntity;
  }

  private ProfileLabel convertFromEntity(ProfileLabelEntity profileLabelEntity) {
    if (profileLabelEntity == null) {
      return null;
    }
    ProfileLabel profileLabel = new ProfileLabel();
    profileLabel.setId(profileLabelEntity.getId());
    profileLabel.setObjectType(profileLabelEntity.getObjectType());
    profileLabel.setObjectId(profileLabelEntity.getObjectId());
    profileLabel.setLabel(profileLabelEntity.getLabel());
    profileLabel.setLanguage(profileLabelEntity.getLanguage());
    return profileLabel;
  }

}
