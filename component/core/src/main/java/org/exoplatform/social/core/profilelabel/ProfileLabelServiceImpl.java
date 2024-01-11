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

package org.exoplatform.social.core.profilelabel;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.model.ProfileLabel;
import org.exoplatform.social.core.profilelabel.storage.ProfileLabelStorage;

public class ProfileLabelServiceImpl implements ProfileLabelService {

  private final ProfileLabelStorage profileLabelStorage;

  public ProfileLabelServiceImpl(ProfileLabelStorage profileLabelStorage) {
    this.profileLabelStorage = profileLabelStorage;
  }

  @Override
  public ProfileLabel findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language) {
    return profileLabelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, language);
  }

  @Override
  public List<ProfileLabel> findLabelByObjectTypeAndObjectId(String objectType, String objectId) {
    return profileLabelStorage.findLabelByObjectTypeAndObjectId(objectType, objectId);
  }

  @Override
  public void createLabel(ProfileLabel profileLabel) throws ObjectAlreadyExistsException {
    if (profileLabel == null) {
      throw new IllegalArgumentException("ProfileLabel Item Object is mandatory");
    }
    if (StringUtils.isBlank(profileLabel.getObjectType())) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (profileLabel.getObjectId() == null) {
      throw new IllegalArgumentException("Object Id is mandatory");
    }
    if (StringUtils.isBlank(profileLabel.getLanguage())) {
      throw new IllegalArgumentException("language is mandatory");
    }
    if (StringUtils.isBlank(profileLabel.getLabel())) {
      throw new IllegalArgumentException("profileLabel value is mandatory");
    }
    ProfileLabel storedProfileLabel = profileLabelStorage.findLabelByObjectTypeAndObjectIdAndLang(profileLabel.getObjectType(),
                                                                                                  profileLabel.getObjectId(),
                                                                                                  profileLabel.getLanguage());
    if (storedProfileLabel != null) {
      throw new ObjectAlreadyExistsException(storedProfileLabel, "ProfileLabel already exist");
    }
    profileLabel.setId(null);
    profileLabelStorage.saveLabel(profileLabel, true);
  }

  @Override
  public void updateLabel(ProfileLabel profileLabel) {
    if (StringUtils.isBlank(profileLabel.getObjectType())) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (profileLabel.getObjectId() == null) {
      throw new IllegalArgumentException("Object Id is mandatory");
    }
    profileLabelStorage.saveLabel(profileLabel, false);
  }

  @Override
  public void deleteLabel(long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("ProfileLabel Technical Identifier is mandatory");
    }
    profileLabelStorage.deleteLabel(id);
  }

}
