/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.storage.cache.model.key;

import java.util.Objects;

import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.SpaceFilter;

/**
 * Immutable space filter key. This key is used to cache the search results.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceFilterKey implements CacheKey {

  private static final long serialVersionUID = 2363449672896832814L;

  private String            userId;

  private String            spaceNameSearchCondition;

  private boolean           favorite;

  private String            appId;

  private SpaceType         type;

  private Sorting           sorting;

  private final int         hash;

  public SpaceFilterKey(String userId, SpaceFilter filter, SpaceType type) {
    this.userId = userId;
    if (filter != null) {
      this.spaceNameSearchCondition = filter.getSpaceNameSearchCondition();
      this.sorting = filter.getSorting();
      this.appId = filter.getAppId();
      this.favorite = filter.isFavorite();
      if (userId == null) {
        this.userId = filter.getRemoteId();
      }
    }
    this.type = type;
    this.hash = Objects.hash(this.userId, filter, this.type, this.favorite);
  }

  public String getUserId() {
    return userId;
  }

  public String getSpaceNameSearchCondition() {
    return spaceNameSearchCondition;
  }

  public String getAppId() {
    return appId;
  }

  public SpaceType getType() {
    return type;
  }

  public Sorting getSorting() {
    return sorting;
  }

  public boolean isFavorite() {
    return favorite;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof SpaceFilterKey))
      return false;
    return o.hashCode() == hashCode();
  }

  @Override
  public int hashCode() {
    return hash;
  }

}
