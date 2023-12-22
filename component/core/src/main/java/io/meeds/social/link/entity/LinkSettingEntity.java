/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.link.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

import io.meeds.social.link.constant.LinkDisplayType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "SocLinkSetting")
@ExoEntity
@Table(name = "SOC_LINK_SETTINGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQuery(
  name = "LinkSettingEntity.findByName",
  query = "SELECT s from SocLinkSetting s"
      + " WHERE s.name = :name"
)
public class LinkSettingEntity implements Serializable {

  private static final long serialVersionUID = -3658613044697163309L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_LINK_SETTINGS_ID", sequenceName = "SEQ_SOC_LINK_SETTINGS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_LINK_SETTINGS_ID")
  @Column(name = "LINK_SETTING_ID")
  private Long              id;

  @Column(name = "NAME", unique = true, nullable = false)
  private String            name;

  @Column(name = "PAGE_REFERENCE")
  private String            pageReference;

  @Column(name = "SPACE_ID")
  private long              spaceId;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "TYPE", nullable = false)
  private LinkDisplayType   type;

  @Column(name = "LARGE_ICON")
  private boolean           largeIcon;

  @Column(name = "SHOW_NAME")
  private boolean           showName;

  @Column(name = "SHOW_DESCRIPTION")
  private boolean           showDescription;

  @Column(name = "SEE_MORE_URL")
  private String            seeMore;

  @Column(name = "LAST_MODIFIED")
  private Instant           lastModified;

  @lombok.EqualsAndHashCode.Exclude
  @lombok.ToString.Exclude
  @OneToMany(mappedBy = "setting", fetch = FetchType.LAZY)
  private Set<LinkEntity>   links;

}
