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

public class GroupSpaceBindingOperationReportEntity extends BaseEntity {

  private static final long serialVersionUID = -8049916455960643317L;

  public GroupSpaceBindingOperationReportEntity() {
  }

  public GroupSpaceBindingOperationReportEntity setSpace(DataEntity spaceEntity) {
    setProperty("space", spaceEntity);
    return this;
  }

  public String getSpace() {
    return getString("space");
  }

  public GroupSpaceBindingOperationReportEntity setGroup(DataEntity group) {
    setProperty("group", group);
    return this;
  }

  public String getGroup() {
    return getString("group");
  }

  public GroupSpaceBindingOperationReportEntity setOperationType(String operationType) {
    setProperty("operationType", operationType);
    return this;
  }

  public String getOperationType() {
    return getString("operationType");
  }

  public GroupSpaceBindingOperationReportEntity setBindingId(String bindingId) {
    setProperty("bindingId", bindingId);
    return this;
  }

  public String getBindingId() {
    return getString("bindingId");
  }

  public GroupSpaceBindingOperationReportEntity setAddedUsersCount(String addedUsers) {
    setProperty("addedUsers", addedUsers);
    return this;
  }

  public String getAddedUsersCount() {
    return getString("addedUsers");
  }

  public GroupSpaceBindingOperationReportEntity setRemovedUsersCount(String removedUsers) {
    setProperty("removedUsers", removedUsers);
    return this;
  }

  public String getRemovedUsersCount() {
    return getString("removedUsers");
  }

  public GroupSpaceBindingOperationReportEntity setStartDate(String startDate) {
    setProperty("startDate", startDate);
    return this;
  }

  public String getStartDate() {
    return getString("startDate");
  }

  public GroupSpaceBindingOperationReportEntity setEndDate(String endDate) {
    setProperty("endDate", endDate);
    return this;
  }

  public String getEndDate() {
    return getString("endDate");
  }
}
