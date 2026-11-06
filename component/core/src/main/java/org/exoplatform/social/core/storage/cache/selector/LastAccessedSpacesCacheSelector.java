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
package org.exoplatform.social.core.storage.cache.selector;

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.storage.cache.model.data.ListSpacesData;
import org.exoplatform.social.core.storage.cache.model.key.ListSpacesKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceType;

/**
 * Selects the lists of the spaces of a user whose order a visit changes: the
 * ones of the last accessed spaces, and every list sorted by the last visit,
 * whichever spaces it holds (member, manager, accessible or visible ones). The
 * entries of the other users stay.
 */
public class LastAccessedSpacesCacheSelector extends CacheSelector<ListSpacesKey, ListSpacesData> {

  private static final long serialVersionUID = 1L;

  private final String      remoteId;

  public LastAccessedSpacesCacheSelector(String remoteId) {
    this.remoteId = remoteId;
  }

  @Override
  public boolean select(ListSpacesKey key, ObjectCacheInfo<? extends ListSpacesData> ocinfo) {
    SpaceFilterKey spaceFilterKey = key == null ? null : key.getKey();
    return spaceFilterKey != null
        && remoteId.equals(spaceFilterKey.getUserId())
        && (SpaceType.LATEST_ACCESSED.equals(spaceFilterKey.getType()) || isSortedByLastVisit(spaceFilterKey));
  }

  private boolean isSortedByLastVisit(SpaceFilterKey spaceFilterKey) {
    return spaceFilterKey.getSorting() != null
        && spaceFilterKey.getSorting().sortBy == Sorting.SortBy.LASTVISITED;
  }

  @Override
  protected Object[] criteria() {
    return new Object[] { remoteId };
  }

}
