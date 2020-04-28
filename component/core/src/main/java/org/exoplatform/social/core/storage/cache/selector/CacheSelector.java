/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
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

package org.exoplatform.social.core.storage.cache.selector;

import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.key.CacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CacheSelector<T extends CacheKey, U> implements CachedObjectSelector<T, U> {

  private T key;

  public CacheSelector() {
  }

  public CacheSelector(T key) {
    this.key = key;
  }

  public boolean select(final T key, final ObjectCacheInfo<? extends U> ocinfo) {
    return this.key == null || this.key.equals(key);
  }

  public void onSelect(final ExoCache<? extends T, ? extends U> exoCache, final T key, final ObjectCacheInfo<? extends U> ocinfo) throws Exception {
    exoCache.remove(key);
  }

}
