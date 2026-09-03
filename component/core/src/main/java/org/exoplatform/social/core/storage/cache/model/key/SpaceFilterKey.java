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

  /**
   * Remote id of the user the listing is filtered for, when it differs from
   * {@code getUserId()} — the profile spaces listing, where userId is the
   * profile owner and this is the viewer whose visibility rules were applied.
   * <p>
   * It is an explicit, equals-compared field on purpose: key equality here is
   * decided on {@link #hash}, an int folded from the whole filter, so two
   * colliding filters are the same key. That is a stale-list risk for a
   * viewer-agnostic listing, but an access-control one as soon as the key
   * carries a viewer — a collision would serve one viewer's filtered list to
   * another. The same reason puts the scope in {@code getType()} rather than in
   * the hash.
   */
  private final String      viewerId;

  public SpaceFilterKey(String userId, SpaceFilter filter, SpaceType type) {
    this(userId, null, filter, type);
  }

  public SpaceFilterKey(String userId, String viewerId, SpaceFilter filter, SpaceType type) {
    this.hash = Objects.hash(filter);
    this.templateIds = filter == null ? null : filter.getTemplateIds();
    this.type = type;
    this.userId = userId;
    this.viewerId = viewerId;
  }

}
