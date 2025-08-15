/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.storage.cache.model.key;

import java.util.List;
import java.util.Objects;

import org.exoplatform.social.core.space.SpaceFilter;

import lombok.Data;

@Data
public class SpaceFilterKey implements CacheKey {

  private static final long serialVersionUID = 2363449672896832814L;

  private final SpaceType   type;

  private final String      userId;

  private List<Long>        templateIds;

  private final int         hash;

  public SpaceFilterKey(String userId, SpaceFilter filter, SpaceType type) {
    this.hash = Objects.hash(filter);
    this.templateIds = filter == null ? null : filter.getTemplateIds();
    this.type = type;
    this.userId = userId;
  }

}
