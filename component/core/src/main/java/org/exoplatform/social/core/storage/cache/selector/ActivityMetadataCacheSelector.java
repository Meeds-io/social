/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.storage.cache.selector;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;

import lombok.Getter;

/**
 * A cache selector to purge activities from cache using Metadata objectType and objectId
 */
public class ActivityMetadataCacheSelector extends CacheSelector<ActivityKey, ActivityData> {

  private String objectType;

  private String objectId;

  @Getter
  private boolean found;

  public ActivityMetadataCacheSelector(String objectType, String objectId) {
    this.objectType = objectType;
    this.objectId = objectId;
  }

  @Override
  public boolean select(final ActivityKey key, final ObjectCacheInfo<? extends ActivityData> ocinfo) {
    if (!super.select(key, ocinfo)) {
      return false;
    }

    ActivityData data = ocinfo.get();
    boolean match = StringUtils.equals(data.getMetadataObjectType(), objectType)
        && StringUtils.equals(data.getMetadataObjectId(), objectId);
    this.found = this.found || match;
    return match;
  }

}
