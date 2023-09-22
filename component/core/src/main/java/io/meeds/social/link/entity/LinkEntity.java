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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

import lombok.Data;

@Entity(name = "SocLink")
@ExoEntity
@Table(name = "SOC_LINKS")
@Data
public class LinkEntity {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_LINK_ID", sequenceName = "SEQ_SOC_LINK_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_LINK_ID")
  @Column(name = "LINK_ID")
  private Long              id;

  @Column(name = "ORDER")
  private int               order;

  @Column(name = "NAME")
  private String            name;

  @Column(name = "DESCRIPTION")
  private String            description;

  @Column(name = "URL", nullable = false)
  private String            url;

  @Column(name = "SAME_TAB")
  private boolean           sameTab;

  @Column(name = "ICON_FILE_ID")
  private long              iconFileId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "LINK_SETTING_ID", nullable = false)
  private LinkSettingEntity setting;

}
