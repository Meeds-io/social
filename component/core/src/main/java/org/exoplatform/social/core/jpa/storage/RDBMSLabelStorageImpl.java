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
package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.social.core.jpa.storage.dao.jpa.LabelDAO;
import org.exoplatform.social.core.jpa.storage.entity.LabelEntity;
import org.exoplatform.social.core.model.Label;
import org.exoplatform.social.core.storage.api.LabelStorage;
import java.util.List;

public class RDBMSLabelStorageImpl implements LabelStorage {

  private final LabelDAO labelDAO;

  public RDBMSLabelStorageImpl(LabelDAO labelDAO) {
    this.labelDAO = labelDAO;
  }

  @Override
  public Label findLabelByObjectTypeAndObjectIdAndLang(String objectType, String objectId, String language) {
    return convertFromEntity(labelDAO.findLabelByObjectTypeAndObjectIdAndLang(objectType, objectId, language));
  }

  @Override
  public List<Label> findLabelByObjectTypeAndObjectId(String objectType, String objectId) {
    return labelDAO.findLabelByObjectTypeAndObjectId(objectType, objectId)
            .stream()
            .map(this::convertFromEntity).toList();
  }

  @Override
  public Label saveLabel(Label label, boolean isNew) {
    if (isNew) {
      return convertFromEntity(labelDAO.create(convertToEntity(label)));
    } else {
      return convertFromEntity(labelDAO.update(convertToEntity(label)));
    }
  }

  @Override
  public void deleteLabel(Long id) {
    labelDAO.delete(labelDAO.find(id));
  }

  private LabelEntity convertToEntity(Label label) {
    if (label == null)
      return null;
    LabelEntity labelEntity = new LabelEntity();
    labelEntity.setId(label.getId());
    labelEntity.setObjectType(label.getObjectType());
    labelEntity.setObjectId(label.getObjectId());
    labelEntity.setLabel(label.getLabel());
    labelEntity.setLanguage(label.getLanguage());
    return labelEntity;
  }

  private Label convertFromEntity(LabelEntity labelEntity) {
    if (labelEntity == null)
      return null;
    Label label = new Label();
    label.setId(labelEntity.getId());
    label.setObjectType(labelEntity.getObjectType());
    label.setObjectId(labelEntity.getObjectId());
    label.setLabel(labelEntity.getLabel());
    label.setLanguage(labelEntity.getLanguage());
    return label;
  }

}
