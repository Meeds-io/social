/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.search;

import java.util.Set;

import org.exoplatform.social.core.space.SpaceFilter;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class XSpaceFilter extends SpaceFilter {

  private Set<Long>   ids    = null;

  private boolean     includePrivate;

  private boolean     notHidden;

  private boolean     isPublic;

  private boolean     lastAccess;

  private boolean     visited;

  public XSpaceFilter setSpaceFilter(SpaceFilter spaceFilter) {
    if (spaceFilter != null) {
      this.setIncludeSpaces(spaceFilter.getIncludeSpaces());
      this.setRemoteId(spaceFilter.getRemoteId());
      this.setSorting(spaceFilter.getSorting());
      this.setFavorite(spaceFilter.isFavorite());
      if (spaceFilter.getSpaceNameSearchCondition() != null) {
        this.setSpaceNameSearchCondition(spaceFilter.getSpaceNameSearchCondition());
      }
      if (spaceFilter.getStatus() != null) {
        this.setStatus(spaceFilter.getStatus());
      }

      if (spaceFilter instanceof XSpaceFilter filter) {
        this.setIncludePrivate(filter.isIncludePrivate());
        this.setNotHidden(filter.isNotHidden());
        this.setLastAccess(filter.isLastAccess());
        this.setVisited(filter.isVisited());
        if (filter.isPublic()) {
          this.setPublic(filter.getRemoteId());
        }
      }
    }
    return this;
  }

  public XSpaceFilter setIncludePrivate(boolean includePrivate) {
    this.includePrivate = includePrivate;
    return this;
  }

  public XSpaceFilter setNotHidden(boolean notHidden) {
    this.notHidden = notHidden;
    return this;
  }

  public boolean isIncludePrivate() {
    return includePrivate;
  }

  public boolean isNotHidden() {
    return notHidden;
  }

  public void setPublic(String userId) {
    setRemoteId(userId);
    this.isPublic = (userId != null);
  }

  public boolean isPublic() {
    return isPublic;
  }
  
  public void setVisited(boolean visited) {
    this.visited = visited;
  }

  public void setLastAccess(boolean lastAccess) {
    this.lastAccess = lastAccess;
  }

  public boolean isLastAccess() {
    return lastAccess;
  }

  public boolean isVisited() {
    return visited;
  }

  public Set<Long> getIds() {
    return ids;
  }

  public void setIds(Set<Long> ids) {
    this.ids = ids;
  }

}
