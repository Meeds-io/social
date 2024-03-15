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

package org.exoplatform.social.core.jpa.storage.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity(name = "SocProfileSettingEntity")
@ExoEntity
@Table(name = "SOC_PROFILE_PROPERTY_SETTING ")

@NamedQuery(name = "SocProfileSettingEntity.findProfileSettingByName", query = "SELECT c FROM SocProfileSettingEntity c WHERE propertyName = :name")
@NamedQuery(name = "SocProfileSettingEntity.findSynchronizedSettings", query = "SELECT c FROM SocProfileSettingEntity c WHERE isGroupSynchronized = true")
@NamedQuery(name = "SocProfileSettingEntity.findOrderedSettings", query = "SELECT c FROM SocProfileSettingEntity c order by c.order")
@NamedQuery(name = "SocProfileSettingEntity.findChildProperties", query = "SELECT c FROM SocProfileSettingEntity c WHERE parentId = :parentId order by c.order")

public class ProfilePropertySettingEntity implements Serializable {


  @Id
  @SequenceGenerator(name="SEQ_SOC_PROPERTY_SETTING_ID", sequenceName="SEQ_SOC_PROPERTY_SETTING_ID", allocationSize = 1)
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_SOC_PROPERTY_SETTING_ID")
  @Column(name="PROPERTY_SETTING_ID")
  private Long id;


  @Column(name = "PROPERTY_NAME", nullable = false)
  private String            propertyName;

  @Column(name = "VISIBLE")
  private boolean           isVisible;

  @Column(name = "EDITABLE")
  private boolean           isEditable;

  @Column(name = "PARENT_ID")
  private Long parentId;

  @Column(name = "PROPERTY_ORDER")
  private Long order;

  @Column(name = "ACTIVE")
  private boolean isActive;

  @Column(name = "REQUIRED_PROPERTY")
  private boolean isRequired;

  @Column(name = "MULTI_VALUED")
  private boolean isMultiValued;

  @Column(name = "GROUP_SYNCHRONIZED")
  private boolean isGroupSynchronized;

  @Column(name = "IS_HIDDENABLE")
  private boolean isHiddenable;


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

  public boolean isMultiValued() {
    return isMultiValued;
  }

  public void setMultiValued(boolean multiValued) {
    isMultiValued = multiValued;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public void setRequired(boolean required) {
    isRequired = required;
  }

  public boolean isHiddenable() {
    return isHiddenable;
  }

  public void setHiddenable(boolean hiddenable) {
    isHiddenable = hiddenable;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getId().toString());
    builder.append(":").append(getPropertyName());
    builder.append(":").append(isEditable());
    builder.append(":").append(isActive());
    builder.append(":").append(isVisible());
    builder.append(":").append(getOrder());
    builder.append(":").append(isMultiValued());
    builder.append(":").append(isRequired());
    builder.append(":").append(isGroupSynchronized());
    builder.append(":").append(getParentId());
    return builder.toString();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProfilePropertySettingEntity other = (ProfilePropertySettingEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}
