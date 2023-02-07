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
package org.exoplatform.social.core.label;

import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.social.core.model.Label;

/**
 * A Service to manage Labels of any content of any type in eXo Platform.
 */
public interface LabelService {

  Label findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language);

  List<Label> findLabelByObjectTypeAndObjectId(String objectType, String objectId);

  /**
   * Creates a list of new {@link Label} objects
   *
   * @param labels {@link List} of {@link Label}
   * @param objectType {@link String} the object type of the given objectId
   * @param objectId {@link String} the object Id related to the new label
   */
  void createLabels(List<Label> labels, String objectType, String objectId);

  /**
   * Creates a new {@link Label} object
   *
   * @param label {@link Label}
   */
  void createLabel(Label label) throws ObjectAlreadyExistsException;

  /**
   * Merge a list of {@link Label} objects with existing labels related to the
   * given object type and id
   *
   * @param labels {@link List} of {@link Label}
   * @param objectType {@link String} the object type of the given objectId
   * @param objectId {@link String} the object Id related to the new label
   */
  void mergeLabels(List<Label> labels, String objectType, String objectId);

  /**
   * Check if the given {@link Label} object exist in the given list of
   * {@link Label} objects
   *
   * @param list {@link List} of {@link Label}
   * @param label {@link Label} object to chack
   */
  Label containsLabel(List<Label> list, Label label);

  /**
   * Updates a {@link Label} object
   *
   * @param label {@link Label}
   */
  void updateLabel(Label label);

  /**
   * Deletes a list {@link Label} objects
   *
   * @param labels {@link List} of {@link Label}
   */
  void deleteLabels(List<Label> labels);

  /**
   * Delete a {@link Label} object
   *
   * @param id {@link Long} id of the label to delete
   */
  void deleteLabel(Long id);
}
