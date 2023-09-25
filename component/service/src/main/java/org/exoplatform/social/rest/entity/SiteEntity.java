/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.rest.entity;

import java.util.List;
import java.util.Map;

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.rest.model.UserNodeRestEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteEntity {

  private long                      siteId;

  private SiteType                  siteType;

  private String                    name;

  private String                    displayName;

  private String                    description;

  private List<Map<String, Object>> accessPermissions;

  private Map<String, Object>       editPermission;

  private boolean                   displayed;

  private int                       displayOrder;

  private boolean                   metaSite;

  private List<UserNodeRestEntity>  siteNavigations;

  private boolean                   canEdit;

  private long                      bannerFileId;

  private String                    bannerUrl;
}
