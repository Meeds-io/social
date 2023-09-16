/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package io.meeds.social.observe.model;

import org.exoplatform.social.metadata.model.MetadataObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ObserverObject extends MetadataObject {

  public ObserverObject() {
  }

  public ObserverObject(String objectType, String objectId, String parentObjectId, long spaceId) {
    super(objectType, objectId, parentObjectId, spaceId);
  }

  public ObserverObject(MetadataObject metadataObject) {
    super(metadataObject.getType(), metadataObject.getId(), metadataObject.getParentId(), metadataObject.getSpaceId());
  }

}
