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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "SocLink")
@ExoEntity
@Table(name = "SOC_LINKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQuery(
  name = "LinkEntity.findByName",
  query = "SELECT l from SocLink l"
    + " INNER JOIN l.setting s ON s.name = :name"
    + " ORDER BY l.order ASC"
)
@NamedQuery(
  name = "LinkEntity.getLinkSettingByLinkId",
  query = "SELECT l.setting.id from SocLink l"
  + " WHERE l.id = :id"
)
public class LinkEntity implements Serializable {

  private static final long serialVersionUID = -8563536841046360579L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_LINK_ID", sequenceName = "SEQ_SOC_LINK_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_LINK_ID")
  @Column(name = "LINK_ID")
  private Long              id;

  @Column(name = "URL", nullable = false)
  private String            url;

  @Column(name = "SAME_TAB")
  private boolean           sameTab;

  @Column(name = "LINK_ORDER")
  private int               order;

  @Column(name = "ICON_FILE_ID")
  private long              iconFileId;

  @lombok.EqualsAndHashCode.Exclude
  @lombok.ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "LINK_SETTING_ID", nullable = false)
  private LinkSettingEntity setting;

}
