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

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.model.ProfileLabel;

/**
 * A Service to manage Labels of any content of any type in eXo Platform.
 */
public interface ProfileLabelService {
  /**
   * Retrieves a {@link ProfileLabel} identified by 'Object Type', 'Object Id' and
   * 'language'.
   *
   * @param objectType {@link String} the type of object of the label
   * @param objectId {@link String} the technical identifier of the object related
   *          to the label
   * @param language {@link String} the language of the label
   * @return {@link ProfileLabel}
   */
  ProfileLabel findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language);

  /**
   * Retrieves a {@link List} of {@link ProfileLabel} identified by 'Object Type'
   * and 'Object Id'
   *
   * @param objectType {@link String} the type of object of the label
   * @param objectId {@link String} the technical identifier of the object related
   *          to the label
   * @return {@link List} of {@link ProfileLabel}
   */
  List<ProfileLabel> findLabelByObjectTypeAndObjectId(String objectType, String objectId);

  /**
   * Creates a new {@link ProfileLabel} object
   *
   * @param profileLabel {@link ProfileLabel}
   */
  void createLabel(ProfileLabel profileLabel) throws ObjectAlreadyExistsException;

  /**
   * Updates a {@link ProfileLabel} object
   *
   * @param profileLabel {@link ProfileLabel}
   */
  void updateLabel(ProfileLabel profileLabel);

  /**
   * Delete a {@link ProfileLabel} object
   *
   * @param id {@link Long} id of the label to delete
   */
  void deleteLabel(long id);
}
