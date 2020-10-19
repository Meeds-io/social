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

package org.exoplatform.social.core.storage.cache.model.key;

/**
 * Immutable space list key.
 * This key is used to cache space list.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ListSpacesKey extends ListCacheKey {

  private final SpaceFilterKey key;

  public ListSpacesKey(final SpaceFilterKey key, final long offset, final long limit) {
    super(offset, limit);
    this.key = key;
  }

  public SpaceFilterKey getKey() {
    return key;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListSpacesKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ListSpacesKey that = (ListSpacesKey) o;

    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (key != null ? key.hashCode() : 0);
    return result;
  }

}
