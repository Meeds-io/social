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
package org.exoplatform.social.core.storage.cache.model.key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Cache key of a space listing or count: the listing scope ({@link #type},
 * {@link #userId}, {@link #viewerId}) and an immutable, serializable snapshot
 * of every {@link SpaceFilter} field. Equality compares all of them, so two
 * filters share a cache entry only when they are equal, and a filter mutated
 * after the key was built (as {@code SpaceListAccess} does) leaves the key
 * untouched.
 */
@Getter
@ToString
@EqualsAndHashCode
public class SpaceFilterKey implements CacheKey {

  private static final long           serialVersionUID = -8353204473175620401L;

  private final SpaceType             type;

  private final String                userId;

  /**
   * Remote id of the user the listing is filtered for, when it differs from
   * {@link #userId} — the profile spaces listing, where userId is the profile
   * owner and this is the viewer whose visibility rules were applied.
   */
  private final String                viewerId;

  private final String                spaceNameSearchCondition;

  private final List<String>          includeSpaceIds;

  private final List<Long>            excludedIds;

  private final String                remoteId;

  private final long                  identityId;

  private final List<Long>            templateIds;

  private final List<Long>            categoryIds;

  private final List<Long>            excludedCategoryIds;

  private final List<Long>            managingTemplateIds;

  private final SpaceMembershipStatus status;

  private final SpaceMembershipStatus extraStatus;

  private final SpaceRegistration     registration;

  private final SpaceVisibility       visibility;

  private final Sorting               sorting;

  private final boolean               favorite;

  private final List<String>          tagNames;

  private final long                  parentSpaceId;

  private final List<Long>            subspaceTemplateIds;

  private final boolean               onlyParentSpaces;

  public SpaceFilterKey(String userId, SpaceFilter filter, SpaceType type) {
    this(userId, null, filter, type);
  }

  public SpaceFilterKey(String userId, String viewerId, SpaceFilter filter, SpaceType type) {
    this.type = type;
    this.userId = userId;
    this.viewerId = viewerId;
    if (filter == null) {
      this.spaceNameSearchCondition = null;
      this.includeSpaceIds = null;
      this.excludedIds = null;
      this.remoteId = null;
      this.identityId = 0;
      this.templateIds = null;
      this.categoryIds = null;
      this.excludedCategoryIds = null;
      this.managingTemplateIds = null;
      this.status = null;
      this.extraStatus = null;
      this.registration = null;
      this.visibility = null;
      this.sorting = null;
      this.favorite = false;
      this.tagNames = null;
      this.parentSpaceId = 0;
      this.subspaceTemplateIds = null;
      this.onlyParentSpaces = false;
    } else {
      this.spaceNameSearchCondition = filter.getSpaceNameSearchCondition();
      this.includeSpaceIds = spaceIds(filter.getIncludeSpaces());
      this.excludedIds = snapshot(filter.getExcludedIds());
      this.remoteId = filter.getRemoteId();
      this.identityId = filter.getIdentityId();
      this.templateIds = snapshot(filter.getTemplateIds());
      this.categoryIds = snapshot(filter.getCategoryIds());
      this.excludedCategoryIds = snapshot(filter.getExcludedCategoryIds());
      this.managingTemplateIds = snapshot(filter.getManagingTemplateIds());
      this.status = filter.getStatus();
      this.extraStatus = filter.getExtraStatus();
      this.registration = filter.getRegistration();
      this.visibility = filter.getVisibility();
      this.sorting = filter.getSorting();
      this.favorite = filter.isFavorite();
      this.tagNames = snapshot(filter.getTagNames());
      this.parentSpaceId = filter.getParentSpaceId();
      this.subspaceTemplateIds = snapshot(filter.getSubspaceTemplateIds());
      this.onlyParentSpaces = filter.isOnlyParentSpaces();
    }
  }

  /**
   * Copies a filter list so the key neither aliases the filter's mutable list
   * nor holds a non-serializable {@link Space}. {@code List.copyOf} is not
   * used: it rejects {@code null} elements, which a filter list may carry.
   */
  private static <T> List<T> snapshot(List<T> list) {
    return list == null ? null : Collections.unmodifiableList(new ArrayList<>(list));
  }

  private static List<String> spaceIds(List<Space> spaces) {
    return spaces == null ? null : snapshot(spaces.stream().map(space -> space == null ? null : space.getId()).toList());
  }

}
