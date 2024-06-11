/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataFilter {

  private Long                creatorId;

  private String              metadataName;

  private String              metadataTypeName;

  private List<String>        metadataObjectTypes;

  private List<Long>          metadataSpaceIds;

  private String              sortField;

  private Map<String, String> metadataProperties;

  /**
   * To be used when we need to combine list of properties using or condition:
   * ((in spacesIds and metadataProperties equal condition)
   * or (combinedMetadataProperties equal condition))
   */
  private Map<String, String> combinedMetadataProperties;

}
