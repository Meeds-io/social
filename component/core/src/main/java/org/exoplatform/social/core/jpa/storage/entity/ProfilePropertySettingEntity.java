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

package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Entity(name = "SocProfileSettingEntity")
@ExoEntity
@Table(name = "SOC_PROFILE_PROPERTY_SETTING ")

@NamedQueries({ @NamedQuery(name = "SocProfileSettingEntity.findProfileSettingByName",
                query = "SELECT c FROM SocProfileSettingEntity c WHERE propertyName = :name")
})

public class ProfilePropertySettingEntity implements Serializable {


  private static final Log  LOG              = ExoLogger.getLogger(ProfilePropertySettingEntity.class);

  @Id
  @SequenceGenerator(name="SEQ_SOC_PROPERTY_SETTING_ID", sequenceName="SEQ_SOC_PROPERTY_SETTING_ID", allocationSize = 1)
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_SOC_PROPERTY_SETTING_ID")
  @Column(name="PROPERTY_SETTING_ID")
  private Long id;


  @Column(name = "PROPERTY_NAME", nullable = false)
  private String            propertyName;

  @Column(name = "VISIBLE", nullable = false)
  private boolean           isVisible;

  @Column(name = "EDITABLE", nullable = false)
  private boolean           isEditable;

  @Column(name="PARENT_ID")
  private Long parentId;

  @Column(name = "ORDER", nullable = false)
  private Long            order;

  @Column(name = "ACTIVE", nullable = false)
  private boolean           isActive;

  @Column(name = "SYSTEM_PROPERTY", nullable = false)
  private boolean           isSystemProperty;

  @Column(name = "LDAP_ATTRIBUTE", nullable = false)
  private String            ldapAttribute;

  @Column(name = "GROUP_SYNCHRONIZED", nullable = false)
  private boolean           IsGroupSynchronized;


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

  public String getLdapAttribute() {
    return ldapAttribute;
  }

  public void setLdapAttribute(String ldapAttribute) {
    this.ldapAttribute = ldapAttribute;
  }

  public boolean isGroupSynchronized() {
    return IsGroupSynchronized;
  }

  public void setGroupSynchronized(boolean groupSynchronized) {
    IsGroupSynchronized = groupSynchronized;
  }

  public boolean isSystemProperty() {
    return isSystemProperty;
  }

  public void setSystemProperty(boolean systemProperty) {
    isSystemProperty = systemProperty;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getId().toString());
    builder.append(":").append(getPropertyName());
    builder.append(":").append(isEditable());
    builder.append(":").append(isActive());
    builder.append(":").append(isVisible());
    builder.append(":").append(getOrder());
    builder.append(":").append(getLdapAttribute());
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
