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
package org.exoplatform.social.core.storage.cache.model.data;

import java.io.Serializable;
import java.util.Map;

public class SuggestionsData implements Serializable {
  private static final long serialVersionUID = 5251447311633368247L;

  private final Map<String, Integer> map;

  public SuggestionsData(Map<String, Integer> map) {
    this.map = map;
  }
  
  public Map<String, Integer> getMap() {
    return this.map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SuggestionsData)) return false;

    SuggestionsData that = (SuggestionsData) o;

    return map != null ? map.equals(that.map) : that.map == null;

  }

  @Override
  public int hashCode() {
    return map != null ? map.hashCode() : 0;
  }
}
