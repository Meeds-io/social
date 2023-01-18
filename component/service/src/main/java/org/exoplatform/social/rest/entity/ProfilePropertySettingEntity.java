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

import org.exoplatform.social.core.model.Label;

import java.util.ArrayList;
import java.util.List;

public class ProfilePropertySettingEntity {
  private Long id;

  private String propertyName;

  private String value;

  private String resolvedLabel;

  private boolean isVisible;

  private boolean isEditable;

  private Long parentId;

  private Long order;

  private boolean isActive;

  private boolean isGroupSynchronized;

  private boolean isRequired;

  private boolean isMultiValued;

  private List<Label> labels;

  private List<ProfilePropertySettingEntity> children;

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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getResolvedLabel() {
    return resolvedLabel;
  }

  public void setResolvedLabel(String resolvedLabel) {
    this.resolvedLabel = resolvedLabel;
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

  public boolean isMultiValued() {
    return isMultiValued;
  }

  public void setMultiValued(boolean multiValued) {
    isMultiValued = multiValued;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(List<Label> labels) {
    this.labels = labels;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public void setRequired(boolean required) {
    isRequired = required;
  }

  public List<ProfilePropertySettingEntity> getChildren() {
    if (children!=null){
      return children;
    }
    return new ArrayList<ProfilePropertySettingEntity>();
  }

  public void setChildren(List<ProfilePropertySettingEntity> children) {
    this.children = children;
  }
}
