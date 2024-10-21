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
package io.meeds.social.space.template.service.injection.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceTemplateDescriptor {

  private String              id;

  private Map<String, String> names;

  private Map<String, String> descriptions;

  private String              bannerPath;

  private String              icon;

  private boolean             enabled;

  private boolean             system;

  private String              layout;

  private List<String>        spaceFields;

  private List<String>        permissions;

  private List<String>        spaceLayoutPermissions;

  private List<String>        spaceDeletePermissions;

  private String              spaceDefaultRegistration;

  private String              spaceDefaultVisibility;

  private boolean             spaceAllowContentCreation;

}
