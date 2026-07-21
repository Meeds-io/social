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
package io.meeds.social.category.model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic, object-type agnostic preview of an entry linked to a Category,
 * supplied by the {@link io.meeds.social.category.plugin.CategoryPlugin}
 * managing the designated objectType.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntryItem {

  /**
   * Object technical identifier
   */
  private String     id;

  /**
   * Object type
   */
  private String     objectType;

  /**
   * Fontawesome Icon identifier used as a corner badge to identify the
   * entry type. Supplied by the owning plugin, never guessed from the
   * objectType value.
   */
  private String     icon;

  private String     title;

  private String     summary;

  private String     illustrationUrl;

  private String     url;

  private String     authorDisplayName;

  private String     authorAvatarUrl;

  private String     spaceDisplayName;

  private String     spaceAvatarUrl;

  private Date       date;

  private long       likesCount;

  private long       commentsCount;

  private long       viewsCount;

  private List<Long> categoryIds;

}
