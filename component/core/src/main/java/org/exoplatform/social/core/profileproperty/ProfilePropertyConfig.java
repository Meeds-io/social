/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.core.profileproperty;

import java.util.ArrayList;
import java.util.List;

public class ProfilePropertyConfig {
  private List<ProfileProperty> profileProperties;

  public ProfilePropertyConfig() {
    profileProperties = new ArrayList<>();
  }

  public List<ProfileProperty> getProfileProperties() {
    return profileProperties;
  }

  public void setProfileProperties(List<ProfileProperty> profileProperties) {
    this.profileProperties = profileProperties;
  }

  public static class ProfileProperty {
    private String  propertyName;

    private boolean visible;

    private boolean editable;

    private String  parentName;

    private int     order;

    private boolean active;

    private boolean groupSynchronized;

    private boolean required;

    private boolean multiValued;

    public String getPropertyName() {
      return propertyName;
    }

    public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
    }

    public boolean isVisible() {
      return visible;
    }

    public void setVisible(boolean visible) {
      this.visible = visible;
    }

    public boolean isEditable() {
      return editable;
    }

    public void setEditable(boolean editable) {
      this.editable = editable;
    }

    public String getParentName() {
      return parentName;
    }

    public void setParentName(String parentName) {
      this.parentName = parentName;
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }

    public boolean isGroupSynchronized() {
      return groupSynchronized;
    }

    public void setGroupSynchronized(boolean groupSynchronized) {
      this.groupSynchronized = groupSynchronized;
    }

    public boolean isRequired() {
      return required;
    }

    public void setRequired(boolean required) {
      this.required = required;
    }

    public boolean isMultiValued() {
      return multiValued;
    }

    public void setMultiValued(boolean multiValued) {
      this.multiValued = multiValued;
    }
  }

}
