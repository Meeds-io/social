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
package io.meeds.social.navigation.model;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.meeds.social.navigation.constant.TopbarItemType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopbarApplication {

  public static final String  CONTENT_ID_PROP_NAME = "contentId";

  private String              id;

  private String              name;

  private String              description;

  private String              icon;

  private TopbarItemType      type;

  private boolean             enabled;

  private boolean             mobile;

  private Map<String, String> properties;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TopbarApplication app)) {
      return false;
    } else if (StringUtils.equals(id, app.getId())) {
      return true;
    } else if (properties == null || app.getProperties() == null) {
      return false;
    } else {
      return StringUtils.equals(properties.get(CONTENT_ID_PROP_NAME),
                                app.getProperties().get(CONTENT_ID_PROP_NAME));
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(properties == null ? id :
                                           StringUtils.firstNonBlank(properties.get(CONTENT_ID_PROP_NAME), id));
  }

}
