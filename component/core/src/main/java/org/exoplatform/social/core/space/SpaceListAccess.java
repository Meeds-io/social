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
package org.exoplatform.social.core.space;

import static org.exoplatform.social.core.space.SpaceListAccessType.ACCESSIBLE;
import static org.exoplatform.social.core.space.SpaceListAccessType.ACCESSIBLE_FILTER;
import static org.exoplatform.social.core.space.SpaceListAccessType.ALL;
import static org.exoplatform.social.core.space.SpaceListAccessType.*;
import static org.exoplatform.social.core.space.SpaceListAccessType.VISIBLE;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.search.SpaceSearchConnector;
import io.meeds.social.search.model.SpaceSearchFilter;
import io.meeds.social.search.model.SpaceSearchResult;
import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.template.service.SpaceTemplateService;

public class SpaceListAccess implements ListAccess<Space> {

  private SpaceTemplateService spaceTemplateService;

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
                         SpaceTemplateService spaceTemplateService,
                         String userId,
                         SpaceFilter spaceFilter,
                         SpaceListAccessType type) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.spaceTemplateService = spaceTemplateService;
    this.userId = StringUtils.firstNonBlank(userId, IdentityConstants.ANONIM);
    this.spaceFilter = spaceFilter;
    this.type = type;
  }

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
                         SpaceTemplateService spaceTemplateService,
                         String userId,
                         SpaceListAccessType type) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.spaceTemplateService = spaceTemplateService;
    this.userId = userId;
    this.spaceFilter = new SpaceFilter();
    this.type = type;
  }

  public SpaceListAccess(SpaceStorage spaceStorage,
                         SpaceSearchConnector spaceSearchConnector,
                         SpaceTemplateService spaceTemplateService,
                         SpaceFilter spaceFilter,
                         SpaceListAccessType type) {
    this.spaceStorage = spaceStorage;
    this.spaceSearchConnector = spaceSearchConnector;
    this.spaceTemplateService = spaceTemplateService;
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
    SpaceFilter filter = getSpaceFilter();
    if (filter != null && userId != null && StringUtils.isBlank(filter.getRemoteId())) {
      filter.setRemoteId(userId);
    }

    SpaceSearchFilter searchFilter = getSpaceSearchFilter();
    if (searchFilter != null) {
      return spaceSearchConnector.count(searchFilter);
    } else {
      switch (type) {
      case ALL:
        return spaceStorage.getAllSpacesCount();
      case ALL_FILTER:
        return spaceStorage.getAllSpacesByFilterCount(filter);
      case ACCESSIBLE:
        return spaceStorage.getAccessibleSpacesCount(userId);
      case ACCESSIBLE_FILTER:
        return spaceStorage.getAccessibleSpacesByFilterCount(userId, filter);
      case INVITED:
        return spaceStorage.getInvitedSpacesCount(userId);
      case INVITED_FILTER:
        return spaceStorage.getInvitedSpacesByFilterCount(userId, filter);
      case PENDING:
        return spaceStorage.getPendingSpacesCount(userId);
      case PENDING_FILTER:
        return spaceStorage.getPendingSpacesByFilterCount(userId, filter);
      case MEMBER:
        return spaceStorage.getMemberSpacesCount(userId);
      case MEMBER_FILTER:
        return spaceStorage.getMemberSpacesByFilterCount(userId, filter);
      case FAVORITE_FILTER:
        return spaceStorage.getFavoriteSpacesByFilterCount(userId, filter);
      case MANAGER:
        return spaceStorage.getManagerSpacesCount(userId);
      case MANAGER_FILTER:
        return spaceStorage.getManagerSpacesByFilterCount(userId, filter);
      case VISIBLE:
        return spaceStorage.getVisibleSpacesCount(userId, filter);
      case LASTEST_ACCESSED:
        return spaceStorage.getLastAccessedSpaceCount(filter);
      case PENDING_REQUESTS:
        return spaceStorage.countPendingSpaceRequestsToManage(userId);
      case COMMON:
        return spaceStorage.countCommonSpaces(userId, otherUserId);
      default:
        return 0;
      }
    }
  }

  @Override
  public Space[] load(int offset, int limit) { // NOSONAR
    SpaceFilter filter = getSpaceFilter();
    if (filter != null && StringUtils.isBlank(filter.getRemoteId())) {
      filter.setRemoteId(userId);
    }
    List<Space> listSpaces = null;
    SpaceSearchFilter searchFilter = getSpaceSearchFilter();
    if (searchFilter != null) {
      List<SpaceSearchResult> spaces = spaceSearchConnector.search(searchFilter, offset, limit);
      listSpaces = spaces.stream()
                         .map(SpaceSearchResult::getId)
                         .map(spaceStorage::getSpaceById)
                         .filter(Objects::nonNull)
                         .toList();
    } else {
      switch (type) {
      case ALL:
        listSpaces = spaceStorage.getSpaces(offset, limit);
        break;
      case ALL_FILTER:
        listSpaces = spaceStorage.getSpacesByFilter(this.getSpaceFilter(), offset, limit);
        break;
      case ACCESSIBLE:
        listSpaces = spaceStorage.getAccessibleSpaces(this.userId, offset, limit);
        break;
      case ACCESSIBLE_FILTER:
        listSpaces = spaceStorage.getAccessibleSpacesByFilter(this.userId, this.getSpaceFilter(), offset, limit);
        break;
      case INVITED:
        listSpaces = spaceStorage.getInvitedSpaces(this.userId, offset, limit);
        break;
      case INVITED_FILTER:
        listSpaces = spaceStorage.getInvitedSpacesByFilter(this.userId, this.getSpaceFilter(), offset, limit);
        break;
      case PENDING:
        listSpaces = spaceStorage.getPendingSpaces(this.userId, offset, limit);
        break;
      case PENDING_FILTER:
        listSpaces = spaceStorage.getPendingSpacesByFilter(this.userId, this.getSpaceFilter(), offset, limit);
        break;
      case MEMBER:
        listSpaces = spaceStorage.getMemberSpaces(this.userId, offset, limit);
        break;
      case MEMBER_FILTER:
        listSpaces = spaceStorage.getMemberSpacesByFilter(this.userId, this.getSpaceFilter(), offset, limit);
        break;
      case FAVORITE_FILTER:
        listSpaces = spaceStorage.getFavoriteSpacesByFilter(this.userId, this.getSpaceFilter(), offset, limit);
        break;
      case MANAGER:
        listSpaces = spaceStorage.getManagerSpaces(this.userId, offset, limit);
        break;
      case MANAGER_FILTER:
        listSpaces = spaceStorage.getManagerSpacesByFilter(this.userId, this.getSpaceFilter(), offset, limit);
        break;
      case VISIBLE:
        listSpaces = spaceStorage.getVisibleSpaces(this.userId, this.getSpaceFilter(), offset, limit);
        break;
      case LASTEST_ACCESSED:
        listSpaces = spaceStorage.getLastAccessedSpace(this.getSpaceFilter(), offset, limit);
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
          Space storedSpace = spaceStorage.getSpaceById(space.getSpaceId());
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
    SpaceFilter filter = getSpaceFilter();
    if (filter == null || !spaceSearchConnector.isEnabled()) {
      return null;
    }
    String username = StringUtils.firstNonBlank(userId, filter.getRemoteId());
    if (username == null) {
      return null;
    }
    SpaceMembershipStatus statusType = getUnifiedSearchStatusType(filter.getStatus());
    if (filter.isUnifiedSearch()
        && (type == ALL_FILTER
            || type == MEMBER_FILTER
            || type == MANAGER_FILTER
            || type == ALL
            || type == ACCESSIBLE_FILTER
            || type == ACCESSIBLE
            || type == VISIBLE
            || statusType != null)) {
      return new SpaceSearchFilter(username,
                                   filter.getIdentityId(),
                                   filter.getTemplateIds(),
                                   filter.getManagingTemplateIds(),
                                   filter.getCategoryIds(),
                                   filter.getExcludedCategoryIds(),
                                   filter.getSpaceNameSearchCondition(),
                                   filter.isFavorite(),
                                   filter.getTagNames(),
                                   statusType,
                                   filter.getRegistration(),
                                   filter.getVisibility(),
                                   filter.isSortingEmpty() ? null : filter.getSorting().sortBy.getFieldName(),
                                   filter.isSortingEmpty() ? null : filter.getSorting().orderBy.name().toLowerCase());
    } else {
      return null;
    }
  }

  private SpaceFilter getSpaceFilter() {
    if (spaceFilter != null
        && spaceTemplateService != null
        && spaceFilter.getManagingTemplateIds() == null
        && (userId != null || spaceFilter.getRemoteId() != null)) {
      spaceFilter.setManagingTemplateIds(spaceTemplateService.getManagingSpaceTemplates(StringUtils.firstNonBlank(userId,
                                                                                                                  spaceFilter.getRemoteId())));
    }
    return spaceFilter;
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
