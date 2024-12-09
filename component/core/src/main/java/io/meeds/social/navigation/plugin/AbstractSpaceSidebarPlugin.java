/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation.plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

import lombok.SneakyThrows;

public abstract class AbstractSpaceSidebarPlugin implements SidebarPlugin {

  public static final int     SPACES_LIMIT_DEFAULT               = 4;

  public static final String  SPACES_LIMIT                       = "limit";

  public static final String  SPACES_SORT_BY                     = "sortBy";

  public static final String  DISPLAY_ONLY_WHEN_MEMBER_PROP_NAME = "displayOnlyWhenMember";

  private static final String NOT_MEMBER_PROP_NAME               = "notMember";

  @Autowired
  private SpaceService        spaceService;

  protected abstract void buildSpaceFilter(SidebarItem item, SpaceFilter spaceFilter);

  protected List<SidebarItem> getSpaces(SidebarItem item, String username) {
    SpaceFilter spaceFilter = new SpaceFilter();
    SidebarSpaceSortBy sortBy = getSortBy(item);
    spaceFilter.setSorting(getSorting(item, sortBy));
    spaceFilter.setRemoteId(username);
    buildSpaceFilter(item, spaceFilter);
    int limit = getLimit(item);
    Space[] spaces = getSpaces(item, spaceFilter, username, sortBy, limit);
    if (spaces.length == 0 && isDisplayOnlyWhenMember(item)) {
      item.getProperties().put(NOT_MEMBER_PROP_NAME, String.valueOf(!isMember(spaceFilter, username)));
    }
    return Arrays.stream(spaces)
                 .filter(Objects::nonNull)
                 .map(this::toSidebarItem)
                 .toList();
  }

  private SidebarItem toSidebarItem(Space space) {
    Map<String, String> properties = new HashMap<>();
    properties.put("id", space.getId());
    properties.put("groupId", space.getGroupId());
    return new SidebarItem(space.getDisplayName(),
                           "/portal/s/" + space.getId(),
                           null,
                           space.getAvatarUrl(),
                           null,
                           SidebarItemType.SPACE,
                           null,
                           properties);
  }

  private SortBy getSortField(SidebarItem item, SidebarSpaceSortBy sortBy) { // NOSONAR
    return Sorting.SortBy.TITLE;
  }

  private OrderBy getSortDirection(SidebarItem item, SidebarSpaceSortBy sortBy) { // NOSONAR
    return sortBy == SidebarSpaceSortBy.LAST_ACCESS ? OrderBy.DESC : OrderBy.ASC;
  }

  private SidebarSpaceSortBy getSortByField(String sortByProperty) {
    return StringUtils.isBlank(sortByProperty) ? SidebarSpaceSortBy.TITLE :
                                               SidebarSpaceSortBy.valueOf(sortByProperty);
  }

  private Sorting getSorting(SidebarItem item, SidebarSpaceSortBy sortBy) {
    return new Sorting(getSortField(item, sortBy), getSortDirection(item, sortBy));
  }

  @SneakyThrows
  private Space[] getSpaces(SidebarItem item, SpaceFilter spaceFilter, String username, SidebarSpaceSortBy sortBy, int limit) {
    if (limit <= 0) {
      return new Space[0];
    }
    return switch (sortBy) {
    case TITLE: {
      yield spaceService.getMemberSpacesByFilter(username, spaceFilter).load(0, limit);
    }
    case FAVORITE: {
      yield spaceService.getFavoriteSpacesByFilter(username, spaceFilter).load(0, limit);
    }
    case LAST_ACCESS: {
      yield spaceService.getLastAccessedSpaceByFilter(username, spaceFilter).load(0, limit);
    }
    default:
      yield new Space[0];
    };
  }

  @SneakyThrows
  private boolean isMember(SpaceFilter spaceFilter, String username) {
    return spaceService.getMemberSpacesByFilter(username, spaceFilter).getSize() > 0;
  }

  private SidebarSpaceSortBy getSortBy(SidebarItem item) {
    String sortByProperty = item.getProperties().get(SPACES_SORT_BY);
    return getSortByField(sortByProperty);
  }

  private int getLimit(SidebarItem item) {
    String limitProperty = item.getProperties().get(SPACES_LIMIT);
    return StringUtils.isBlank(limitProperty) ? SPACES_LIMIT_DEFAULT : Integer.parseInt(limitProperty);
  }

  private boolean isDisplayOnlyWhenMember(SidebarItem item) {
    return StringUtils.equals(item.getProperties().get(DISPLAY_ONLY_WHEN_MEMBER_PROP_NAME), "true");
  }

  protected enum SidebarSpaceSortBy {
    TITLE, FAVORITE, LAST_ACCESS;
  }

}
