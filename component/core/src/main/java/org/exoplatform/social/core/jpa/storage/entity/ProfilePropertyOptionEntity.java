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

import io.meeds.common.persistence.PortableSequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "SocProfilePropertyOptionEntity")
@Table(name = "SOC_PROFILE_PROPERTY_OPTION")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQueries({
    @NamedQuery(name = "ProfilePropertyOptionEntity.findPropertyOptionsBySettingId", query = "SELECT o FROM SocProfilePropertyOptionEntity o WHERE o.propertySetting.id = :settingId") })
public class ProfilePropertyOptionEntity implements Serializable {

  @Id
  @PortableSequence(name = "SEQ_SOC_PROFILE_PROPERTY_OPTION_ID")
  @Column(name = "PROPERTY_OPTION_ID")
  private Long                         id;

  @Column(name = "PROPERTY_OPTION_VALUE")
  private String                       value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PROPERTY_SETTING_ID", nullable = false)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private ProfilePropertySettingEntity propertySetting;
}
