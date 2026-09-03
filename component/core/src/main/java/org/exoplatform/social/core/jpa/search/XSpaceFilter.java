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

import io.meeds.social.space.constant.UserSpacesScope;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class XSpaceFilter extends SpaceFilter {

  private Set<Long>       ids    = null;

  private boolean         includePrivate;

  private boolean         notHidden;

  private boolean         lastAccess;

  private boolean         visited;

  /**
   * Remote id of a second user the listed spaces must be shared with, i.e. a
   * user holding the member role on the same space. Used to express the "common
   * with the viewer" condition of the profile spaces listing, alongside the
   * profile owner carried by {@link #getRemoteId()}.
   */
  private String          commonWithUserId;

  /**
   * Scope of the "common with the viewer" condition:
   * {@link UserSpacesScope#COMMON} keeps only the spaces shared with
   * {@link #getCommonWithUserId()}, {@link UserSpacesScope#ALL} keeps the shared
   * ones plus the ones that are not hidden.
   */
  private UserSpacesScope commonWithUserScope;

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
        this.setCommonWithUserId(filter.getCommonWithUserId());
        this.setCommonWithUserScope(filter.getCommonWithUserScope());
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

  public String getCommonWithUserId() {
    return commonWithUserId;
  }

  public XSpaceFilter setCommonWithUserId(String commonWithUserId) {
    this.commonWithUserId = commonWithUserId;
    return this;
  }

  public UserSpacesScope getCommonWithUserScope() {
    return commonWithUserScope;
  }

  public XSpaceFilter setCommonWithUserScope(UserSpacesScope commonWithUserScope) {
    this.commonWithUserScope = commonWithUserScope;
    return this;
  }

  public void setIds(Set<Long> ids) {
    this.ids = ids;
  }

}
