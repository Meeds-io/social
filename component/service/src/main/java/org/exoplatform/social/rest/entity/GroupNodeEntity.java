/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.rest.entity;

import java.util.List;

public class GroupNodeEntity extends BaseEntity {
  private static final long serialVersionUID = 2513730351488599651L;

  public GroupNodeEntity() {
  }

  public GroupNodeEntity(String id) {
    super(id);
  }

  public GroupNodeEntity setGroupName(String groupName) {
    setProperty("name", groupName);
    return this;
  }

  public String getGroupName() {
    return getString("name");
  }

  public GroupNodeEntity setParentId(String parentId) {
    setProperty("parentId", parentId);
    return this;
  }

  public String getParentId() {
    return getString("parentId");
  }

  public GroupNodeEntity setChildGroupNodesEntities(List<DataEntity> childrenEntities) {
    setProperty("children", childrenEntities);
    return this;
  }

  public String getChildGroupNodes() {
    return getString("children");
  }

}
