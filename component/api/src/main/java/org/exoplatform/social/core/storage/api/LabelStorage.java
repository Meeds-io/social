/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.exoplatform.social.core.storage.api;

import java.util.List;

import org.exoplatform.social.core.model.Label;

public interface LabelStorage {

  /**
   * get label by objectType, objectId and language
   *
   * @param objectType
   * @param objectId
   * @param language
   */
  Label findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language);

  /**
   * get labels by objectType and objectId
   *
   * @param objectType
   * @param objectId
   */
  List<Label> findLabelByObjectTypeAndObjectId(String objectType, String objectId);

  /**
   * Saves a label. If isNew is true, creates new label. If not only updates the
   * label and saves it.
   *
   * @param label
   * @param isNew
   */
  public Label saveLabel(Label label, boolean isNew);

  /**
   * Deletes a label by id.
   *
   * @param id
   */
  public void deleteLabel(Long id);
}
