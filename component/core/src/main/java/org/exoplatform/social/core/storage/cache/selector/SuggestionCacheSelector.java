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

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.data.SuggestionsData;
import org.exoplatform.social.core.storage.cache.model.key.*;

public class SuggestionCacheSelector extends CacheSelector<CacheKey, Object> {
  
  private String[] target;

  public SuggestionCacheSelector(String... target) {
    this.target = target;
  }
  
  @Override
  public boolean select(CacheKey key, ObjectCacheInfo<? extends Object> ocinfo) {
    
    if (!super.select(key, ocinfo)) {
      return false;
    }

    if (key instanceof SuggestionKey) {
      return select((SuggestionKey) key, ocinfo);
    }

    return false;

  }

  private boolean select(SuggestionKey key, ObjectCacheInfo<? extends Object> ocinfo) {

    if (key.getKey() instanceof IdentityKey) {
      String id = ((IdentityKey) key.getKey()).getId();
      for (String i : target) {
        if (id.equals(i)) return true;
      }
      if (ocinfo == null)
        return true;
      Object value = ocinfo.get();
      if (value instanceof SuggestionsData) {
        SuggestionsData data = (SuggestionsData) value;
        if (data.getMap() == null)
          return false;
        for (String i : target) {
          if (data.getMap().containsKey(i)) return true;
        }
      }
    }
    return false;
  }
  
}
