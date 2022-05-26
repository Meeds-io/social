/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
package org.exoplatform.social.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataObject implements Cloneable {

  private String type;

  private String id;

  private String parentId;

  private long   spaceId;

  public MetadataObject(String type, String id) {
    this.type = type;
    this.id = id;
  }

  public MetadataObject(String type, String id, String parentId) {
    this.type = type;
    this.id = id;
    this.parentId = parentId;
  }

  @Override
  public MetadataObject clone() { // NOSONAR
    return new MetadataObject(type, id, parentId, spaceId);
  }
}
