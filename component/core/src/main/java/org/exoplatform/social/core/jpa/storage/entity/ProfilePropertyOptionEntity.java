/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
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

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

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
  @SequenceGenerator(name = "SEQ_SOC_PROFILE_PROPERTY_OPTION_ID", sequenceName = "SEQ_SOC_PROFILE_PROPERTY_OPTION_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_PROFILE_PROPERTY_OPTION_ID")
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
