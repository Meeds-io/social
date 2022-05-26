/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.metadata.model;

import java.util.HashMap;
import java.util.Map;

import lombok.*;

@EqualsAndHashCode
public class MetadataItem implements Cloneable {

  @Getter
  @Setter
  private long                id;

  @Getter
  @Setter
  private Metadata            metadata;

  @Getter
  private MetadataObject      object;

  @Getter
  @Setter
  private long                creatorId;

  @Getter
  @Setter
  private long                createdDate;

  @Getter
  @Setter
  private Map<String, String> properties;

  public MetadataItem() {
    object = new MetadataObject();
  }

  public MetadataItem(long id,
                      Metadata metadata,
                      MetadataObject object,
                      long creatorId,
                      long createdDate,
                      Map<String, String> properties) {
    super();
    this.id = id;
    this.metadata = metadata;
    this.object = object == null ? new MetadataObject() : object;
    this.creatorId = creatorId;
    this.createdDate = createdDate;
    this.properties = properties;
  }

  public String getMetadataTypeName() {
    return metadata == null ? null
                            : metadata.getTypeName();
  }

  public String getObjectType() {
    return this.object.getType();
  }

  public String getObjectId() {
    return this.object.getId();
  }

  public String getParentObjectId() {
    return this.object.getParentId();
  }

  public void setObjectType(final String objectType) {
    this.object.setType(objectType);
  }

  public void setObjectId(final String objectId) {
    this.object.setId(objectId);
  }

  public void setSpaceId(final long spaceId) {
    this.object.setSpaceId(spaceId);
  }

  public long getSpaceId() {
    return this.object.getSpaceId();
  }

  public void setParentObjectId(final String parentObjectId) {
    this.object.setParentId(parentObjectId);
  }

  @Override
  public MetadataItem clone() { // NOSONAR
    return new MetadataItem(id,
                            metadata,
                            object,
                            creatorId,
                            createdDate,
                            properties == null ? null : new HashMap<>(properties));
  }

}
