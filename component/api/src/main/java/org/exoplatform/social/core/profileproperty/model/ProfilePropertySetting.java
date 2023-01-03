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

package org.exoplatform.social.core.profileproperty.model;

public class  ProfilePropertySetting {
  String       propertyName;
  boolean      isVisible;
  boolean      isEditable;
  Long         parentId;
  Long          order;
  boolean      isActive;
  boolean      isGroupSynchronized;
  boolean      isSystemProperty;
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public boolean isVisible() {
    return isVisible;
  }

  public void setVisible(boolean visible) {
    isVisible = visible;
  }

  public boolean isEditable() {
    return isEditable;
  }

  public void setEditable(boolean editable) {
    isEditable = editable;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public Long getOrder() {
    return order;
  }

  public void setOrder(Long order) {
    this.order = order;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public boolean isGroupSynchronized() {
    return isGroupSynchronized;
  }

  public void setGroupSynchronized(boolean groupSynchronized) {
    isGroupSynchronized = groupSynchronized;
  }

  public boolean isSystemProperty() {
    return isSystemProperty;
  }

  public void setSystemProperty(boolean systemProperty) {
    isSystemProperty = systemProperty;
  }
}
