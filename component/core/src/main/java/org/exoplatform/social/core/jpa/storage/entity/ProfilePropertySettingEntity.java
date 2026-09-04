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
package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import io.meeds.common.persistence.PortableSequence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "SocProfileSettingEntity")
@Table(name = "SOC_PROFILE_PROPERTY_SETTING ")

@NamedQuery(name = "SocProfileSettingEntity.findProfileSettingByName", query = "SELECT c FROM SocProfileSettingEntity c WHERE c.propertyName = :name")
@NamedQuery(name = "SocProfileSettingEntity.findSynchronizedSettings", query = "SELECT c FROM SocProfileSettingEntity c WHERE c.isGroupSynchronized = true")
@NamedQuery(name = "SocProfileSettingEntity.findOrderedSettings", query = "SELECT c FROM SocProfileSettingEntity c order by c.order")
@NamedQuery(name = "SocProfileSettingEntity.countChildProperties", query = "SELECT COUNT(c) FROM SocProfileSettingEntity c WHERE c.parentId = :id")

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfilePropertySettingEntity implements Serializable {

  private static final long                 serialVersionUID = -2657229462239171339L;

  @Id
  @PortableSequence(name = "SEQ_SOC_PROPERTY_SETTING_ID")
  @Column(name = "PROPERTY_SETTING_ID")
  private Long                              id;

  @Column(name = "PROPERTY_NAME", nullable = false)
  private String                            propertyName;

  @Column(name = "VISIBLE")
  private boolean                           isVisible;

  @Column(name = "EDITABLE")
  private boolean                           isEditable;

  @Column(name = "PARENT_ID")
  private Long                              parentId;

  @Column(name = "PROPERTY_ORDER")
  private Long                              order;

  @Column(name = "ACTIVE")
  private boolean                           isActive;

  @Column(name = "REQUIRED_PROPERTY")
  private boolean                           isRequired;

  @Column(name = "MULTI_VALUED")
  private boolean                           isMultiValued;

  @Column(name = "GROUP_SYNCHRONIZED")
  private boolean                           isGroupSynchronized;

  @Column(name = "IS_HIDDENABLE")
  private boolean                           isHiddenable;

  @Column(name = "IS_DROPDOWN")
  private boolean                           isDropdownList;

  @Column(name = "INDEX_IN_ANALYTICS")
  private boolean                           indexInAnalytics;
  
  @Column(name = "PROPERTY_TYPE")
  private String                            propertyType;

  @BatchSize(size = 10)
  @OneToMany(mappedBy = "propertySetting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private List<ProfilePropertyOptionEntity> propertyOptions;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "UPDATED_DATE", nullable = false)
  private Date                              updatedDate = new Date();

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getId().toString());
    builder.append(":").append(getPropertyName());
    builder.append(":").append(isEditable());
    builder.append(":").append(isActive());
    builder.append(":").append(isDropdownList());
    builder.append(":").append(isVisible());
    builder.append(":").append(getOrder());
    builder.append(":").append(isMultiValued());
    builder.append(":").append(isRequired());
    builder.append(":").append(isGroupSynchronized());
    builder.append(":").append(isHiddenable());
    builder.append(":").append(isIndexInAnalytics());
    builder.append(":").append(getPropertyType());
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
