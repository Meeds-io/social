/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.organizationalunit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "SocOrganizationalUnit")
@Table(name = "SOC_ORGANIZATIONAL_UNIT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationalUnitEntity {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_ORGANIZATIONAL_UNIT_ID", sequenceName = "SEQ_SOC_ORGANIZATIONAL_UNIT_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_ORGANIZATIONAL_UNIT_ID")
  @Column(name = "ORGANIZATIONAL_UNIT_ID")
  private Long   id;

  @Column(name = "GROUP_ID", nullable = false, unique = true)
  private String groupId;

  @Column(name = "LABEL", nullable = false)
  private String label;

}
