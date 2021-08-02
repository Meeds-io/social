/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.entity;

import java.io.Serializable;
import java.util.List;

public class LinkEntity implements Serializable {
  private static final long serialVersionUID = 8636599542655416634L;

  private List<? extends DataEntity> entities;

  private BaseEntity linkEntity;

  private CollectionEntity collectionEntity;

  private String href;
  
  public LinkEntity(String href) {
    this.href = href;
  }

  public LinkEntity(BaseEntity linkEntity) {
    this.linkEntity = linkEntity;
  }

  public LinkEntity(List<? extends DataEntity> entities) {
    this.entities = entities;
  }

  public LinkEntity(CollectionEntity collectionEntity) {
    this.collectionEntity = collectionEntity;
  }

  @Override
  public String toString() {
    return getData().toString();
  }

  public Object getData() {
    if (href != null) {
      return href;
    }
    if (linkEntity != null) {
      return linkEntity.getDataEntity();
    }
    if (collectionEntity != null) {
      return collectionEntity;
    }
    return entities;
  }
  
  
}
