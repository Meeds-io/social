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
package org.exoplatform.social.core.jpa.search;

import java.util.Set;

import org.exoplatform.social.core.space.SpaceFilter;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class XSpaceFilter extends SpaceFilter {

  private Set<Long>   ids    = null;

  private boolean     includePrivate;

  private boolean     notHidden;

  private boolean     lastAccess;

  private boolean     visited;

  public XSpaceFilter setSpaceFilter(SpaceFilter spaceFilter) {
    if (spaceFilter != null) {
      this.setIncludeSpaces(spaceFilter.getIncludeSpaces());
      this.setRemoteId(spaceFilter.getRemoteId());
      this.setSorting(spaceFilter.getSorting());
      this.setFavorite(spaceFilter.isFavorite());
      this.setTemplateIds(spaceFilter.getTemplateIds());
      this.setManagingTemplateIds(spaceFilter.getManagingTemplateIds());
      this.setCategoryIds(spaceFilter.getCategoryIds());
      this.setExcludedCategoryIds(spaceFilter.getExcludedCategoryIds());
      this.setExcludedIds(spaceFilter.getExcludedIds());
      this.setVisibility(spaceFilter.getVisibility());
      this.setRegistration(spaceFilter.getRegistration());
      this.setParentSpaceId(spaceFilter.getParentSpaceId());
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
