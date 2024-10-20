/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.space;

import static org.exoplatform.social.core.space.SpaceListAccessType.ACCESSIBLE;
import static org.exoplatform.social.core.space.SpaceListAccessType.ACCESSIBLE_FILTER;
import static org.exoplatform.social.core.space.SpaceListAccessType.ALL;
import static org.exoplatform.social.core.space.SpaceListAccessType.ALL_FILTER;
import static org.exoplatform.social.core.space.SpaceListAccessType.VISIBLE;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.core.search.SpaceSearchConnector;
import io.meeds.social.core.search.model.SpaceSearchFilter;
import io.meeds.social.core.search.model.SpaceSearchResult;
import io.meeds.social.space.constant.SpaceMembershipStatus;

public class SpaceListAccess implements ListAccess<Space> {

  private SpaceStorage         spaceStorage;

  private SpaceSearchConnector spaceSearchConnector;

  private String               userId;

  /**
   * The visited profile id, used while getting list of common spaces between
   * two users.
   */
  private String               otherUserId;

  private SpaceFilter          spaceFilter;

  SpaceListAccessType          type;

  public SpaceListAccess(SpaceStorage spaceStorage,
                         SpaceSearchConnector spaceSearchConnector,
                         String userId,
                         SpaceFilter spaceFilter,
                         SpaceListAccessType type) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.userId = userId;
    this.spaceFilter = spaceFilter;
    this.type = type;
  }

  public SpaceListAccess(SpaceStorage spaceStorage,
                         SpaceSearchConnector spaceSearchConnector,
                         SpaceListAccessType type) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.spaceFilter = new SpaceFilter();
    this.type = type;
  }

  public SpaceListAccess(SpaceStorage spaceStorage,
                         SpaceSearchConnector spaceSearchConnector,
                         String userId,
                         SpaceListAccessType type) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.userId = userId;
    this.spaceFilter = new SpaceFilter();
    this.type = type;
  }

  public SpaceListAccess(SpaceStorage spaceStorage,
                         SpaceSearchConnector spaceSearchConnector,
                         SpaceFilter spaceFilter,
                         SpaceListAccessType type) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.spaceFilter = spaceFilter;
    this.type = type;
  }

  public SpaceListAccess(SpaceStorage spaceStorage,
                         SpaceSearchConnector spaceSearchConnector,
                         SpaceListAccessType type,
                         String userId,
                         String otherUserId) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.otherUserId = otherUserId;
    this.userId = userId;
    this.type = type;
    this.spaceFilter = new SpaceFilter();
  }

  @Override
  public int getSize() {
    if (spaceFilter != null && spaceFilter.isFavorite() && StringUtils.isBlank(spaceFilter.getRemoteId())) {
      spaceFilter.setRemoteId(userId);
    }

    SpaceSearchFilter searchFilter = getSpaceSearchFilter();
    if (searchFilter != null) {
      return spaceSearchConnector.count(searchFilter);
    } else {
      switch (type) {
      case ALL:
        return spaceStorage.getAllSpacesCount();
      case ALL_FILTER:
        return spaceStorage.getAllSpacesByFilterCount(this.spaceFilter);
      case ACCESSIBLE:
        return spaceStorage.getAccessibleSpacesCount(this.userId);
      case ACCESSIBLE_FILTER:
        return spaceStorage.getAccessibleSpacesByFilterCount(this.userId, this.spaceFilter);
      case INVITED:
        return spaceStorage.getInvitedSpacesCount(userId);
      case INVITED_FILTER:
        return spaceStorage.getInvitedSpacesByFilterCount(userId, spaceFilter);
      case PENDING:
        return spaceStorage.getPendingSpacesCount(this.userId);
      case PENDING_FILTER:
        return spaceStorage.getPendingSpacesByFilterCount(this.userId, this.spaceFilter);
      case MEMBER:
        return spaceStorage.getMemberSpacesCount(this.userId);
      case MEMBER_FILTER:
        return spaceStorage.getMemberSpacesByFilterCount(this.userId, this.spaceFilter);
      case FAVORITE_FILTER:
        return spaceStorage.getFavoriteSpacesByFilterCount(this.userId, this.spaceFilter);
      case MANAGER:
        return spaceStorage.getManagerSpacesCount(this.userId);
      case MANAGER_FILTER:
        return spaceStorage.getManagerSpacesByFilterCount(this.userId, this.spaceFilter);
      case VISIBLE:
        return spaceStorage.getVisibleSpacesCount(this.userId, this.spaceFilter);
      case LASTEST_ACCESSED:
        return spaceStorage.getLastAccessedSpaceCount(this.spaceFilter);
      case PENDING_REQUESTS:
        return spaceStorage.countPendingSpaceRequestsToManage(userId);
      case COMMON:
        return spaceStorage.countCommonSpaces(this.userId, this.otherUserId);
      default:
        return 0;
      }
    }
  }

  @Override
  public Space[] load(int offset, int limit) { // NOSONAR
    if (spaceFilter != null
        && StringUtils.isBlank(spaceFilter.getRemoteId())) {
      spaceFilter.setRemoteId(userId);
    }
    List<Space> listSpaces = null;

    SpaceSearchFilter searchFilter = getSpaceSearchFilter();
    if (searchFilter != null) {
      List<SpaceSearchResult> spaces = spaceSearchConnector.search(searchFilter, offset, limit);
      listSpaces = spaces.stream()
                         .map(SpaceSearchResult::getId)
                         .map(String::valueOf)
                         .map(spaceStorage::getSpaceById)
                         .filter(Objects::nonNull)
                         .toList();
    } else {
      switch (type) {
      case ALL:
        listSpaces = spaceStorage.getSpaces(offset, limit);
        break;
      case ALL_FILTER:
        listSpaces = spaceStorage.getSpacesByFilter(this.spaceFilter, offset, limit);
        break;
      case ACCESSIBLE:
        listSpaces = spaceStorage.getAccessibleSpaces(this.userId, offset, limit);
        break;
      case ACCESSIBLE_FILTER:
        listSpaces = spaceStorage.getAccessibleSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case INVITED:
        listSpaces = spaceStorage.getInvitedSpaces(this.userId, offset, limit);
        break;
      case INVITED_FILTER:
        listSpaces = spaceStorage.getInvitedSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case PENDING:
        listSpaces = spaceStorage.getPendingSpaces(this.userId, offset, limit);
        break;
      case PENDING_FILTER:
        listSpaces = spaceStorage.getPendingSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case MEMBER:
        listSpaces = spaceStorage.getMemberSpaces(this.userId, offset, limit);
        break;
      case MEMBER_FILTER:
        listSpaces = spaceStorage.getMemberSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case FAVORITE_FILTER:
        listSpaces = spaceStorage.getFavoriteSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case MANAGER:
        listSpaces = spaceStorage.getManagerSpaces(this.userId, offset, limit);
        break;
      case MANAGER_FILTER:
        listSpaces = spaceStorage.getManagerSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case VISIBLE:
        listSpaces = spaceStorage.getVisibleSpaces(this.userId, this.spaceFilter, offset, limit);
        break;
      case LASTEST_ACCESSED:
        listSpaces = spaceStorage.getLastAccessedSpace(this.spaceFilter, offset, limit);
        break;
      case VISITED:
        listSpaces = spaceStorage.getVisitedSpaces(this.spaceFilter, offset, limit);
        break;
      case COMMON:
        listSpaces = spaceStorage.getCommonSpaces(this.userId, this.otherUserId, offset, limit);
        break;
      case PENDING_REQUESTS: {
        // The computing of spaces content is done here to use cached
        // spaceStorage
        // to retrieve contents
        List<Space> pendingSpaceRequestsToManage = spaceStorage.getPendingSpaceRequestsToManage(userId, offset, limit);
        listSpaces = pendingSpaceRequestsToManage.stream().map(space -> {
          Space storedSpace = spaceStorage.getSpaceById(space.getId());
          storedSpace.setPendingUsers(space.getPendingUsers());
          return storedSpace;
        }).toList();
      }
        break;
      }
    }
    return listSpaces == null ? new Space[0] : listSpaces.toArray(new Space[listSpaces.size()]);
  }

  private SpaceSearchFilter getSpaceSearchFilter() {
    if (spaceFilter == null || !spaceSearchConnector.isEnabled()) {
      return null;
    }
    String username = StringUtils.firstNonBlank(userId, spaceFilter.getRemoteId());
    if (username == null) {
      return null;
    }
    SpaceMembershipStatus statusType = getUnifiedSearchStatusType(spaceFilter.getStatus());
    if (spaceFilter.isUnifiedSearch()
        && (type == ALL_FILTER
            || type == ALL
            || type == ACCESSIBLE_FILTER
            || type == ACCESSIBLE
            || type == VISIBLE
            || statusType != null)) {
      return new SpaceSearchFilter(username,
                                   spaceFilter.getIdentityId(),
                                   spaceFilter.getSpaceNameSearchCondition(),
                                   spaceFilter.isFavorite(),
                                   spaceFilter.getTagNames(),
                                   statusType);
    } else {
      return null;
    }
  }

  private SpaceMembershipStatus getUnifiedSearchStatusType(SpaceMembershipStatus spaceMembershipStatus) {
    return switch (type) {
    case MEMBER_FILTER, FAVORITE_FILTER:
      yield SpaceMembershipStatus.MEMBER;
    case MANAGER_FILTER:
      yield SpaceMembershipStatus.MANAGER;
    case INVITED_FILTER:
      yield SpaceMembershipStatus.INVITED;
    case PENDING_FILTER:
      yield SpaceMembershipStatus.PENDING;
    default:
      if (spaceMembershipStatus == null) {
        yield null;
      } else {
        yield switch (spaceMembershipStatus) {
        case MEMBER:
          yield SpaceMembershipStatus.MEMBER;
        case MANAGER:
          yield SpaceMembershipStatus.MANAGER;
        case INVITED:
          yield SpaceMembershipStatus.INVITED;
        case PENDING:
          yield SpaceMembershipStatus.PENDING;
        case PUBLISHER:
          yield SpaceMembershipStatus.PUBLISHER;
        case REDACTOR:
          yield SpaceMembershipStatus.REDACTOR;
        default:
          yield null;
        };
      }
    };
  }
}
