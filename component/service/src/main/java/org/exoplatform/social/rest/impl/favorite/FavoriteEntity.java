/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.rest.impl.favorite;

import org.exoplatform.social.metadata.model.MetadataItem;

import java.util.List;

public class FavoriteEntity {

  private List<MetadataItem> favoritesItem;
  private Integer offset;

  private Integer limit;

  public List<MetadataItem> getFavoritesItem() {
    return favoritesItem;
  }

  public void setFavoritesItem(List<MetadataItem> favoritesItem) {
    this.favoritesItem=favoritesItem;
  }

  private Integer size;

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset=offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit=limit;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size=size;
  }
}
