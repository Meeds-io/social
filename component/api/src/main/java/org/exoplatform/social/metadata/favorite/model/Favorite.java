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
package org.exoplatform.social.metadata.favorite.model;

import lombok.*;

@EqualsAndHashCode
public class Favorite {

  @Getter
  private FavoriteObject object;

  @Getter
  @Setter
  private long           userIdentityId;

  public Favorite() {
    this.object = new FavoriteObject();
  }

  public Favorite(String objectType, String objectId, String parentObjectId, long userIdentityId) {
    this(objectType, objectId, parentObjectId, userIdentityId, 0);
  }

  public Favorite(String objectType, String objectId, String parentObjectId, long userIdentityId, long spaceId) {
    this.object = new FavoriteObject(objectType, objectId, parentObjectId, spaceId);
    this.userIdentityId = userIdentityId;
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

  public void setParentObjectId(final String parentObjectId) {
    this.object.setParentId(parentObjectId);
  }

}
