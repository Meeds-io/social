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
package org.exoplatform.social.core.service;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.model.Label;
import org.exoplatform.social.core.storage.api.LabelStorage;
import java.util.ArrayList;
import java.util.List;

public class LabelService {

  private static final Log LOG = ExoLogger.getLogger(LabelService.class);

  private final LabelStorage labelStorage;


  public LabelService(LabelStorage labelStorage) {
    this.labelStorage = labelStorage;
  }

  public Label findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language) {
    return labelStorage.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, language);
  }

  public List<Label> findLabelByObjectTypeAndObjectId(String objectType, String objectId) {
    return labelStorage.findLabelByObjectTypeAndObjectId(objectType, objectId);
  }

  public void createLabels(List<Label> labels, String objectType, String objectId) {
    for (Label label : labels) {
      try {
        if (StringUtils.isBlank(label.getObjectType())) {
          label.setObjectType(objectType);
        }
        if (StringUtils.isBlank(label.getObjectId())) {
          label.setObjectId(objectId);
        }
        createLabel(label);
      } catch (ObjectAlreadyExistsException e) {
        LOG.warn("Label already exist it will not be created");
      }
    }
  }

  public void createLabel(Label label) throws ObjectAlreadyExistsException {
    if (label == null) {
      throw new IllegalArgumentException("Label Item Object is mandatory");
    }
    if (StringUtils.isBlank(label.getObjectType())) {
      throw new IllegalArgumentException("Object type is mandatory");
    }
    if (label.getObjectId() == null) {
      throw new IllegalArgumentException("Object Id is mandatory");
    }
    if (StringUtils.isBlank(label.getLanguage())) {
      throw new IllegalArgumentException("language is mandatory");
    }
    if (StringUtils.isBlank(label.getLabel())) {
      throw new IllegalArgumentException("label value is mandatory");
    }
    Label storedLabel = labelStorage.findLabelByObjectTypeAndObjectIdAndLang(label.getObjectType(), label.getObjectId(), label.getLanguage());
    if (storedLabel != null) {
      throw new ObjectAlreadyExistsException(storedLabel, "Label already exist");
    }
    labelStorage.saveLabel(label, true);
  }

  public void mergeLabels(List<Label> labels, String objectType, String ObjectId) {
    List<Label> storedLabels = findLabelByObjectTypeAndObjectId(objectType, ObjectId);
    List<Label> labelstoUpdate = new ArrayList<>();
    List<Label> labelstoDelete = new ArrayList<>();
    List<Label> labelstoCreate = new ArrayList<>();
    if (labels.isEmpty() && !storedLabels.isEmpty()) {
      deleteLabels(storedLabels);
    } else {
      for (Label label : storedLabels) {
        Label foundLabel = containsLabel(labels, label);
        if (foundLabel != null) {
          if (!foundLabel.equals(label)) {
            labelstoUpdate.add(foundLabel);
          }
        } else {
          labelstoDelete.add(label);
        }
      }
      for (Label label : labels) {
        if (label.getId() == null || label.getId() == 0) {
          labelstoCreate.add(label);
        }
      }
      for (Label label : labelstoCreate) {
        try {
          if (StringUtils.isBlank(label.getObjectType())) {
            label.setObjectType(objectType);
          }
          if (StringUtils.isBlank(label.getObjectId())) {
            label.setObjectId(ObjectId);
          }
          createLabel(label);
        } catch (ObjectAlreadyExistsException e) {
          LOG.warn("Label already exist it will not be created");
        }
      }
      for (Label label : labelstoUpdate) {
        updateLabel(label);
      }
      deleteLabels(labelstoDelete);
    }
  }

  private Label containsLabel(final List<Label> list, final Label label) {
    return list.stream().filter(o -> o.getId() != null && o.getId().equals(label.getId())).findAny().orElse(null);
  }

  public void updateLabel(Label label) {
    labelStorage.saveLabel(label, false);
  }

  public void deleteLabels(List<Label> labels) {
    for (Label label : labels) {
      deleteLabel(label.getId());
    }
  }

  public void deleteLabel(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Label Technical Identifier is mandatory");
    }
    labelStorage.deleteLabel(id);
  }
}
