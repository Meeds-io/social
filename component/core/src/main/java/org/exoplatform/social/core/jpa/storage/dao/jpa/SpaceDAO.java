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
package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.constant.SpaceVisibility;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

public class SpaceDAO extends GenericDAOJPAImpl<SpaceEntity, Long> {

  /**
   * Statuses making a hidden space visible to a user
   */
  private static final List<SpaceMembershipStatus> VISIBLE_STATUSES            = Arrays.asList(SpaceMembershipStatus.MEMBER,
                                                                                               SpaceMembershipStatus.INVITED);

  private static final String                      PARAM_PUBLIC_VISIBILITY     = "publicVisibility";

  private static final String                      PARAM_IDS                   = "ids";

  private static final String                      PARAM_TEMPLATE_IDS          = "templateIds";

  private static final String                      PARAM_EXCLUDED_IDS          = "excludedIds";

  private static final String                      PARAM_PRIVATE_VISIBILITY    = "privateVisibility";

  private static final String                      PARAM_USER_ID               = "userId";
  
  private static final String                      PARAM_PARENT_SPACE_ID       = "parentSpaceId";

  private static final String                      PARAM_STATUSES              = "statuses";

  private static final String                      PARAM_VISIBLE_STATUSES      = "visibleStatuses";

  private static final String                      PARAM_MANAGING_TEMPLATE_IDS = "managingTemplateIds";

  private static final String                      PARAM_CATEGORY_IDS          = "categoryIds";

  public static final String                       PARAM_EXCLUDED_CATEGORY_IDS = "excludedCategoryIds";

  private static final String                      PARAM_KEYWORD               = "keyword";

  private static final String                      PARAM_VISIBILITY            = "visibility";

  private static final String                      PARAM_REGISTRATION          = "registration";

  private static final String                      PARAM_HIDDEN_VISIBILITY     = "hiddenVisibility";

  private static final String                      QUERY_FILTER_FIND_PREFIX    = "Space.findSpaces";

  private static final String                      QUERY_FILTER_COUNT_PREFIX   = "Space.countSpaces";

  private final Map<String, Boolean>               filterNamedQueries          = new ConcurrentHashMap<>();

  public List<Long> getLastSpaces(int limit) {
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SpaceEntity.getLastSpaces", Tuple.class);
    query.setMaxResults(limit);
    List<Tuple> resultList = query.getResultList();
    if (resultList == null || resultList.isEmpty()) {
      return Collections.emptyList();
    } else {
      return resultList.stream()
                       .map(tuple -> tuple.get(0, Long.class))
                       .toList();
    }
  }

  public SpaceEntity getSpaceByGroupId(String groupId) {
    TypedQuery<SpaceEntity> query = getEntityManager().createNamedQuery("SpaceEntity.getSpaceByGroupId", SpaceEntity.class);
    query.setParameter("groupId", groupId);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  public SpaceEntity getSpaceByPrettyName(String spacePrettyName) {
    TypedQuery<SpaceEntity> query = getEntityManager().createNamedQuery("SpaceEntity.getSpaceByPrettyName", SpaceEntity.class);
    query.setParameter("prettyName", spacePrettyName);
    query.setMaxResults(1);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  public List<SpaceEntity> getCommonSpaces(String userId, String otherUserId, int offset, int limit) {
    if (userId == null || userId.equals("")) {
      throw new IllegalArgumentException("the userId is null or equals to 0");
    }
    if (otherUserId == null || otherUserId.equals("")) {
      throw new IllegalArgumentException("the otherUserId is null or equals to 0");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be positive");
    }
    if (limit <= 0) {
      throw new IllegalArgumentException("limit must be > 0");
    }
    TypedQuery<SpaceEntity> query = getEntityManager().createNamedQuery("SpaceEntity.getCommonSpacesBetweenTwoUsers",
                                                                        SpaceEntity.class);
    query.setParameter(PARAM_USER_ID, userId);
    query.setParameter("otherUserId", otherUserId);
    query.setFirstResult(offset);
    query.setMaxResults(limit);
    return query.getResultList();

  }

  public int countCommonSpaces(String userId, String otherUserId) {
    if (userId == null || userId.equals("")) {
      throw new IllegalArgumentException("userId is null or equals to 0");
    }
    if (otherUserId == null || otherUserId.equals("")) {
      throw new IllegalArgumentException("otherUserId is null or equals to 0");
    }

    TypedQuery<Long> query = getEntityManager().createNamedQuery("SpaceEntity.countCommonSpacesBetweenTwoUsers",
                                                                 Long.class);
    query.setParameter(PARAM_USER_ID, userId);
    query.setParameter("otherUserId", otherUserId);
    return query.getSingleResult().intValue();

  }

  public Map<Long, Long> countSpacesByTemplate() {
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SpaceEntity.countSpacesByTemplate",
                                                                  Tuple.class);
    List<Tuple> resultList = query.getResultList();
    if (CollectionUtils.isEmpty(resultList)) {
      return Collections.emptyMap();
    } else {
      return resultList.stream()
                       .collect(Collectors.toMap(t -> t.get(0, Long.class), t -> t.get(1, Long.class)));
    }
  }

  public List<Long> getSpaceIdsByFilter(XSpaceFilter filter, long offset, long limit) {
    return findSpaceIdsByFilter(filter, offset, limit);
  }

  public int getSpacesCountByFilter(XSpaceFilter filter) {
    return countSpacesByFilter(filter);
  }

  public List<Long> findSpaceIdsByFilter(XSpaceFilter filter, long offset, long limit) {
    TypedQuery<Tuple> query = buildQueryFromFilter(filter, Tuple.class, false);
    if (offset > 0) {
      query.setFirstResult((int) offset);
    }
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    List<Tuple> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      return result.stream().map(t -> t.get(0, Long.class)).toList();
    }
  }

  public int countSpacesByFilter(XSpaceFilter filter) {
    TypedQuery<Long> query = buildQueryFromFilter(filter, Long.class, true);
    return query.getSingleResult().intValue();
  }

  public List<Long> getSpaceCategoryIds() {
    return getEntityManager().createNamedQuery("SpaceEntity.getSpaceCategoryIds", Long.class)
                             .getResultList();
  }

  private <T> TypedQuery<T> buildQueryFromFilter(XSpaceFilter filter, Class<T> clazz, boolean count) {
    List<String> suffixes = new ArrayList<>();
    List<String> predicates = new ArrayList<>();
    List<String> parameterNames = new ArrayList<>();
    buildPredicates(filter, suffixes, predicates, parameterNames);

    TypedQuery<T> query;
    String queryName = getQueryFilterName(suffixes, count);
    if (filterNamedQueries.containsKey(queryName)) {
      query = getEntityManager().createNamedQuery(queryName, clazz);
    } else {
      String queryContent = getQueryFilterContent(filter, predicates, parameterNames, count);
      query = getEntityManager().createQuery(queryContent, clazz);
      getEntityManager().getEntityManagerFactory().addNamedQuery(queryName, query);
      filterNamedQueries.put(queryName, true);
    }

    addQueryFilterParameters(filter, query, parameterNames);
    return query;
  }

  private <T> void addQueryFilterParameters(XSpaceFilter filter, TypedQuery<T> query, List<String> parameterNames) { // NOSONAR
    if (parameterNames.contains(PARAM_KEYWORD)) {
      query.setParameter(PARAM_KEYWORD, "%" + StringUtils.lowerCase(filter.getSpaceNameSearchCondition()) + "%");
    }
    if (parameterNames.contains(PARAM_PUBLIC_VISIBILITY)) {
      query.setParameter(PARAM_PUBLIC_VISIBILITY, SpaceVisibility.PUBLIC);
    }
    if (parameterNames.contains(PARAM_PRIVATE_VISIBILITY)) {
      query.setParameter(PARAM_PRIVATE_VISIBILITY, SpaceVisibility.PRIVATE);
    }
    if (parameterNames.contains(PARAM_HIDDEN_VISIBILITY)) {
      query.setParameter(PARAM_HIDDEN_VISIBILITY, SpaceVisibility.HIDDEN);
    }
    if (parameterNames.contains(PARAM_IDS)) {
      if (CollectionUtils.isNotEmpty(filter.getIds())) {
        query.setParameter(PARAM_IDS, filter.getIds());
      } else {
        query.setParameter(PARAM_IDS,
                           filter.getIncludeSpaces()
                                 .stream()
                                 .map(Space::getSpaceId)
                                 .toList());
      }
    }
    if (parameterNames.contains(PARAM_TEMPLATE_IDS)) {
      query.setParameter(PARAM_TEMPLATE_IDS, filter.getTemplateIds());
    }
    if (parameterNames.contains(PARAM_EXCLUDED_IDS)) {
      query.setParameter(PARAM_EXCLUDED_IDS, filter.getExcludedIds());
    }
    if (parameterNames.contains(PARAM_USER_ID)) {
      query.setParameter(PARAM_USER_ID, filter.getRemoteId());
    }
    if (parameterNames.contains(PARAM_STATUSES)) {
      query.setParameter(PARAM_STATUSES, filter.getStatusList());
    }
    if (parameterNames.contains(PARAM_VISIBLE_STATUSES)) {
      query.setParameter(PARAM_VISIBLE_STATUSES, VISIBLE_STATUSES);
    }
    if (parameterNames.contains(PARAM_MANAGING_TEMPLATE_IDS)) {
      query.setParameter(PARAM_MANAGING_TEMPLATE_IDS, filter.getManagingTemplateIds());
    }
    if (parameterNames.contains(PARAM_CATEGORY_IDS)) {
      if (CollectionUtils.isNotEmpty(filter.getExcludedCategoryIds())) {
        List<Long> categoryIds = new ArrayList<>(filter.getCategoryIds());
        categoryIds.removeAll(filter.getExcludedCategoryIds());
        filter.setCategoryIds(categoryIds);
      }
      query.setParameter(PARAM_CATEGORY_IDS, filter.getCategoryIds());
    }
    if (parameterNames.contains(PARAM_EXCLUDED_CATEGORY_IDS)) {
      query.setParameter(PARAM_EXCLUDED_CATEGORY_IDS, filter.getExcludedCategoryIds());
    }
    if (parameterNames.contains(PARAM_VISIBILITY)) {
      query.setParameter(PARAM_VISIBILITY, filter.getVisibility());
    }
    if (parameterNames.contains(PARAM_REGISTRATION)) {
      query.setParameter(PARAM_REGISTRATION, filter.getRegistration());
    }  
    if (parameterNames.contains(PARAM_PARENT_SPACE_ID)) {
      query.setParameter(PARAM_PARENT_SPACE_ID, filter.getParentSpaceId());
    }
  }

  private String getQueryFilterName(List<String> suffixes, boolean count) {
    String queryName;
    if (suffixes.isEmpty()) {
      queryName = count ? QUERY_FILTER_COUNT_PREFIX : QUERY_FILTER_FIND_PREFIX;
    } else {
      queryName = (count ? QUERY_FILTER_COUNT_PREFIX : QUERY_FILTER_FIND_PREFIX) + "By" + StringUtils.join(suffixes, "And");
    }
    return queryName;
  }

  private String getQueryFilterContent(XSpaceFilter spaceFilter,
                                       List<String> predicates,
                                       List<String> parameterNames,
                                       boolean count) {
    String querySelect = count ? "SELECT COUNT(DISTINCT s.id) FROM SocSpaceEntity s " :
                               "SELECT DISTINCT(s.id), " + getSortField(spaceFilter) + " FROM SocSpaceEntity s ";
    boolean lastAccess = isLastAccess(spaceFilter);
    if (parameterNames.contains(PARAM_USER_ID) || lastAccess) {
      if (StringUtils.isNotBlank(spaceFilter.getRemoteId()) && lastAccess) {
        querySelect +=
                    " INNER JOIN s.members sm ON sm.userId = :userId AND sm.status = io.meeds.social.space.constant.SpaceMembershipStatus.MEMBER ";
      } else {
        querySelect += " INNER JOIN s.members sm ";
      }
    }

    if (CollectionUtils.isNotEmpty(spaceFilter.getCategoryIds())) {
      querySelect += " INNER JOIN s.categories catInclude ON catInclude.categoryId in :categoryIds";
    }

    if (CollectionUtils.isNotEmpty(spaceFilter.getExcludedCategoryIds())) {
      querySelect += " LEFT JOIN s.categories catExclude ON catExclude.categoryId IN :excludedCategoryIds";
      predicates.add("catExclude.categoryId IS NULL");
    }

    String queryContent;
    if (predicates.isEmpty()) {
      queryContent = querySelect;
    } else {
      queryContent = querySelect + " WHERE " + StringUtils.join(predicates, " AND ");
    }
    if (!count) {
      queryContent += " ORDER BY " + getSortField(spaceFilter) +
          (isSortDescending(spaceFilter) ? " DESC " : " ASC ");
    }
    return queryContent;
  }

  private void buildPredicates(XSpaceFilter spaceFilter,
                               List<String> suffixes,
                               List<String> predicates,
                               List<String> parameterNames) {
    if (CollectionUtils.isNotEmpty(spaceFilter.getTemplateIds())) {
      suffixes.add("TemplateId");
      predicates.add("s.templateId IN :templateIds");
      parameterNames.add(PARAM_TEMPLATE_IDS);
    }

    if (spaceFilter.getVisibility() != null) {
      suffixes.add("Visibility");
      predicates.add("s.visibility = :visibility");
      parameterNames.add(PARAM_VISIBILITY);
    }

    if (spaceFilter.getRegistration() != null) {
      suffixes.add("Registration");
      predicates.add("s.registration = :registration");
      parameterNames.add(PARAM_REGISTRATION);
    }

    if (CollectionUtils.isNotEmpty(spaceFilter.getIds()) || CollectionUtils.isNotEmpty(spaceFilter.getIncludeSpaces())) {
      suffixes.add("SpaceIds");
      predicates.add("s.id IN :ids");
      parameterNames.add(PARAM_IDS);
    }

    if (CollectionUtils.isNotEmpty(spaceFilter.getCategoryIds())) {
      suffixes.add("CategoryIds");
      parameterNames.add(PARAM_CATEGORY_IDS);
    }

    if (CollectionUtils.isNotEmpty(spaceFilter.getExcludedCategoryIds())) {
      suffixes.add("ExcludedCategoryIds");
      parameterNames.add(PARAM_EXCLUDED_CATEGORY_IDS);
    }

    if (CollectionUtils.isNotEmpty(spaceFilter.getExcludedIds())) {
      suffixes.add("ExcludeSpaceIds");
      predicates.add("s.id NOT IN :excludedIds");
      parameterNames.add(PARAM_EXCLUDED_IDS);
    }

    if (StringUtils.isNotBlank(spaceFilter.getSpaceNameSearchCondition())) {
      // Shouldn't be used in real case since
      // the search by keyword will be made using ES in runtime
      suffixes.add("Keyword");
      predicates.add("(s.prettyName LIKE :keyword OR s.displayName LIKE :keyword OR s.description LIKE :keyword)");
      parameterNames.add(PARAM_KEYWORD);
    }

    if (isLastAccess(spaceFilter) && StringUtils.isNotBlank(spaceFilter.getRemoteId())) {
      suffixes.add("UserId");
      parameterNames.add(PARAM_USER_ID);
    }

    if (spaceFilter.getParentSpaceId() > 0) {
      suffixes.add("ParentSpaceId");
      predicates.add("s.parentSpaceEntity.id = :parentSpaceId");
      parameterNames.add(PARAM_PARENT_SPACE_ID);
    }

    buildPermissionPredicates(spaceFilter, suffixes, predicates, parameterNames);
    buildSortSuffixes(spaceFilter, suffixes);
  }

  private void buildPermissionPredicates(XSpaceFilter spaceFilter, // NOSONAR
                                         List<String> suffixes,
                                         List<String> predicates,
                                         List<String> parameterNames) {
    if (CollectionUtils.isNotEmpty(spaceFilter.getStatusList())) {
      if (spaceFilter.isIncludePrivate() || spaceFilter.isNotHidden()) {
        if ((spaceFilter.getRemoteId() == null || StringUtils.equals(spaceFilter.getRemoteId(), IdentityConstants.ANONIM))) {
          suffixes.add("SpacePrivate");
          predicates.add("s.visibility <> :hiddenVisibility"); // NOSONAR
          parameterNames.add(PARAM_HIDDEN_VISIBILITY);
        } else if (CollectionUtils.isEmpty(spaceFilter.getManagingTemplateIds())) {
          suffixes.add("SpacePrivateOrStatuses");
          predicates.add("(s.visibility <> :hiddenVisibility OR (sm.userId = :userId AND sm.status IN :statuses))");
          parameterNames.add(PARAM_HIDDEN_VISIBILITY);
          parameterNames.add(PARAM_USER_ID);
          parameterNames.add(PARAM_STATUSES);
        } else if (!spaceFilter.getManagingTemplateIds().contains(0l)) {
          suffixes.add("SpacePrivateOrStatusesOrManaging");
          predicates.add("(s.visibility <> :hiddenVisibility OR s.templateId IN :managingTemplateIds OR (sm.userId = :userId AND sm.status IN :statuses))");
          parameterNames.add(PARAM_HIDDEN_VISIBILITY);
          parameterNames.add(PARAM_USER_ID);
          parameterNames.add(PARAM_STATUSES);
          parameterNames.add(PARAM_MANAGING_TEMPLATE_IDS);
        }
      } else {
        if ((spaceFilter.getRemoteId() == null || StringUtils.equals(spaceFilter.getRemoteId(), IdentityConstants.ANONIM))) {
          suffixes.add("SpacePrivate");
          predicates.add("s.visibility <> :hiddenVisibility"); // NOSONAR
          parameterNames.add(PARAM_HIDDEN_VISIBILITY);
        } else if (CollectionUtils.isEmpty(spaceFilter.getManagingTemplateIds())) {
          suffixes.add("SpaceWithStatuses");
          predicates.add("sm.userId = :userId AND sm.status IN :statuses");
          parameterNames.add(PARAM_USER_ID);
          parameterNames.add(PARAM_STATUSES);
        } else if (!spaceFilter.getManagingTemplateIds().contains(0l)) {
          suffixes.add("SpaceWithStatusesOrManaging");
          predicates.add("(s.templateId IN :managingTemplateIds OR (sm.userId = :userId AND sm.status IN :statuses))");
          parameterNames.add(PARAM_USER_ID);
          parameterNames.add(PARAM_STATUSES);
          parameterNames.add(PARAM_MANAGING_TEMPLATE_IDS);
        }
      }
    } else if (spaceFilter.getRemoteId() != null) {
      if (StringUtils.equals(spaceFilter.getRemoteId(), IdentityConstants.ANONIM)) {
        suffixes.add("SpacePrivate");
        predicates.add("s.visibility <> :hiddenVisibility"); // NOSONAR
        parameterNames.add(PARAM_HIDDEN_VISIBILITY);
      } else if (CollectionUtils.isEmpty(spaceFilter.getManagingTemplateIds())) {
        suffixes.add("SpaceWithVisibleStatus");
        predicates.add("(s.visibility <> :hiddenVisibility OR (sm.userId = :userId AND sm.status IN :visibleStatuses))");
        parameterNames.add(PARAM_HIDDEN_VISIBILITY);
        parameterNames.add(PARAM_USER_ID);
        parameterNames.add(PARAM_VISIBLE_STATUSES);
      } else {
        suffixes.add("SpaceWithVisibleStatusOrManaging");
        predicates.add("(s.visibility <> :hiddenVisibility OR s.templateId IN :managingTemplateIds OR (sm.userId = :userId AND sm.status IN :visibleStatuses))");
        parameterNames.add(PARAM_HIDDEN_VISIBILITY);
        parameterNames.add(PARAM_MANAGING_TEMPLATE_IDS);
        parameterNames.add(PARAM_USER_ID);
        parameterNames.add(PARAM_VISIBLE_STATUSES);
      }
    }
  }

  private void buildSortSuffixes(XSpaceFilter spaceFilter, List<String> suffixes) {
    String sortField = getSortField(spaceFilter);
    suffixes.add("OrderBy");
    suffixes.add(StringUtils.capitalize(sortField.replace("s.", "").replace("sm.", "")));
    if (isSortDescending(spaceFilter)) {
      suffixes.add("DESC");
    } else {
      suffixes.add("ASC");
    }
  }

  private String getSortField(XSpaceFilter spaceFilter) {
    Sorting sorting = spaceFilter.getSorting();
    if (isLastAccess(spaceFilter)) {
      return "sm.lastAccess";
    } else if (sorting.sortBy.equals(SortBy.DATE)) {
      return "s.createdDate";
    } else {
      return "s.displayName";
    }
  }

  private boolean isSortDescending(XSpaceFilter spaceFilter) {
    return isLastAccess(spaceFilter)
           || spaceFilter.getSorting() == null
           || spaceFilter.getSorting().orderBy.equals(Sorting.OrderBy.DESC);
  }

  private boolean isLastAccess(XSpaceFilter spaceFilter) {
    return spaceFilter.isLastAccess()
           || (spaceFilter.getSorting() != null && spaceFilter.getSorting().sortBy.equals(SortBy.LASTVISITED));
  }

}
