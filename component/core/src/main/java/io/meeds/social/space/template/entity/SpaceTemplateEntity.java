/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.space.template.entity;

import java.util.List;

import org.exoplatform.commons.utils.StringListConverter;

import io.meeds.social.space.constant.Registration;
import io.meeds.social.space.constant.Visibility;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "SpaceTemplate")
@Table(name = "SOC_SPACE_TEMPLATES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceTemplateEntity {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_SPACE_TEMPLATE_ID", sequenceName = "SEQ_SOC_SPACE_TEMPLATE_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_SPACE_TEMPLATE_ID")
  @Column(name = "ID")
  private Long         id;

  @Column(name = "ICON")
  private String       icon;

  @Column(name = "ENABLED")
  private boolean      enabled;

  @Column(name = "DELETED")
  private boolean      deleted;

  @Column(name = "IS_SYSTEM")
  private boolean      system;

  @Column(name = "TEMPLATE_ORDER")
  private long         order;

  @Convert(converter = StringListConverter.class)
  @Column(name = "PERMISSIONS")
  private List<String> permissions;

  @Convert(converter = StringListConverter.class)
  @Column(name = "SPACE_LAYOUT_PERMISSIONS")
  private List<String> spaceLayoutPermissions;

  @Convert(converter = StringListConverter.class)
  @Column(name = "SPACE_DELETE_PERMISSIONS")
  private List<String> spaceDeletePermissions;

  @Convert(converter = StringListConverter.class)
  @Column(name = "SPACE_FIELDS")
  private List<String> spaceFields;

  @Column(name = "SPACE_DEFAULT_VISIBILITY")
  private Visibility   spaceDefaultVisibility;

  @Column(name = "SPACE_DEFAULT_ACCESS")
  private Registration spaceDefaultRegistration;

  @Column(name = "SPACE_ALLOW_CONTENT_CREATION")
  private boolean      spaceAllowContentCreation;

}
