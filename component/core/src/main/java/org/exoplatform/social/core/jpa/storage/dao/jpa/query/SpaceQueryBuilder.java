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
package org.exoplatform.social.core.jpa.storage.dao.jpa.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity_;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity_;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.constant.Visibility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;

public final class SpaceQueryBuilder {
  private long offset;
  private long limit;
  private XSpaceFilter spaceFilter;
  
  public static SpaceQueryBuilder builder() {
    return new SpaceQueryBuilder();
  }  
  
  public SpaceQueryBuilder offset(long offset) {
    this.offset = offset;
    return this;
  }
  
  public SpaceQueryBuilder limit(long limit) {
    this.limit = limit;
    return this;
  }
  
  public SpaceQueryBuilder filter(XSpaceFilter spaceFilter) {
    this.spaceFilter = spaceFilter;
    return this;
  }

  public TypedQuery<Tuple> build() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Tuple> criteria = cb.createQuery(Tuple.class);
    Root<SpaceEntity> spaceEntity = criteria.from(SpaceEntity.class);
    //
    List<Selection<?>> selections = new ArrayList<>();
    selections.add(spaceEntity.get(SpaceEntity_.id));

    Order[] orderBy = buildOrder(spaceEntity, cb, selections);
    Predicate predicateFilter = buildPredicateFilter(spaceEntity, criteria, cb);
    CriteriaQuery<Tuple> select = criteria.select(cb.tuple(selections.toArray(new Selection[0]))).distinct(true);
    if (!predicateFilter.getExpressions().isEmpty()) {
      select.where(predicateFilter);
    }
    select.orderBy(orderBy);
    
    //
    TypedQuery<Tuple> typedQuery = em.createQuery(select);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }
    //
    return typedQuery;
  }

  public TypedQuery<Long> buildCount() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
    Root<SpaceEntity> spaceEntity = criteria.from(SpaceEntity.class);
    CriteriaQuery<Long> select = criteria.select(cb.countDistinct(spaceEntity.get(SpaceEntity_.id)));
    Predicate predicateFilter = buildPredicateFilter(spaceEntity, criteria, cb);
    if (!predicateFilter.getExpressions().isEmpty()) {
      select.where(predicateFilter);
    }
    return em.createQuery(select);
  }
  
  private Predicate buildPredicateFilter(Root<SpaceEntity> root, CriteriaQuery<?> criteria, CriteriaBuilder cb) {
    List<Predicate> predicates = new LinkedList<>();

    if (CollectionUtils.isNotEmpty(spaceFilter.getIds())) {
      predicates.add(root.get(SpaceEntity_.id).in(spaceFilter.getIds()));
    } else if (CollectionUtils.isNotEmpty(spaceFilter.getIncludeSpaces())) {
      predicates.add(root.get(SpaceEntity_.id)
                         .in(spaceFilter.getIncludeSpaces()
                                        .stream()
                                        .map(Space::getId)
                                        .map(Long::parseLong)
                                        .toList()));
    }

    //status
    if (CollectionUtils.isNotEmpty(spaceFilter.getStatusList())) {
      Path<SpaceMemberEntity> join = getMembersJoin(root, JoinType.INNER);

      List<Predicate> pStatusList = new LinkedList<>();
      for (SpaceMembershipStatus status : spaceFilter.getStatusList()) {
        pStatusList.add(cb.equal(join.get(SpaceMemberEntity_.status), status));
      }

      Predicate tmp = spaceFilter.getRemoteId() == null ? cb.or(pStatusList.toArray(new Predicate[pStatusList.size()])) :
                                                        cb.and(cb.or(pStatusList.toArray(new Predicate[pStatusList.size()])),
                                                               cb.equal(join.get(SpaceMemberEntity_.userId),
                                                                        spaceFilter.getRemoteId()));
      boolean includePrivate = spaceFilter.isIncludePrivate();
      if (includePrivate) {
        predicates.add(cb.or(cb.equal(root.get(SpaceEntity_.visibility), Visibility.PRIVATE), tmp));
      } else {
        predicates.add(tmp);
      }
    }

    if (spaceFilter.isPublic() && spaceFilter.getRemoteId() != null) {
      Subquery<Long> sub = criteria.subquery(Long.class);
      Root<SpaceEntity> spaceSub = sub.from(SpaceEntity.class);

      Path<SpaceMemberEntity> join = spaceSub.join(SpaceEntity_.members);
      sub.select(spaceSub.get(SpaceEntity_.id));
      sub.where(cb.equal(join.get(SpaceMemberEntity_.userId), spaceFilter.getRemoteId()));

      predicates.add(cb.not(cb.in(root.get(SpaceEntity_.id)).value(sub)));
    }

    //searchCondition
    String search = spaceFilter.getSpaceNameSearchCondition();
    if (StringUtils.isNotBlank(search)) {
      String searchCondition = buildSearchCondition(search.trim(), true);
      Predicate prettyName = cb.like(cb.lower(root.get(SpaceEntity_.prettyName)), searchCondition);
      Predicate displayName = cb.like(cb.lower(root.get(SpaceEntity_.displayName)), searchCondition);
      Predicate description = cb.like(cb.lower(root.get(SpaceEntity_.description)), searchCondition);
      predicates.add(cb.or(prettyName, displayName, description));
    }    

    //not hidden
    boolean notHidden = spaceFilter.isNotHidden();
    if (notHidden) {
      predicates.add(cb.notEqual(root.get(SpaceEntity_.visibility), Visibility.HIDDEN));
    }
    
    //
    return cb.and(predicates.toArray(new Predicate[predicates.size()]));
  }

  @SuppressWarnings("unchecked")
  private Join<SpaceEntity, SpaceMemberEntity> getMembersJoin(Root<SpaceEntity> root, JoinType joinType) {
    if(root.getJoins() == null || root.getJoins().isEmpty()) {
      SetJoin<SpaceEntity, SpaceMemberEntity> join = root.join(SpaceEntity_.members, joinType);
      join.alias("mem");
      return join;
    } else {
      return (Join<SpaceEntity, SpaceMemberEntity>) root.getJoins().iterator().next();
    }
  }

  private String buildSearchCondition(String app, boolean searchBack) {
    StringBuilder builder = new StringBuilder(app);
    removeAll(builder, "*");
    removeAll(builder, "%");
    removeAll(builder, "  ");
    
    if (searchBack) {
      builder.insert(0, "%");
    }
    builder.append("%");
    
    return builder.toString().toLowerCase();
  }

  private void removeAll(StringBuilder builder, String string) {
    int i;
    while ((i = builder.indexOf(string)) >= 0) {
      builder.deleteCharAt(i);
    }
  }

  private Order[] buildOrder(Root<SpaceEntity> spaceEntity, CriteriaBuilder cb, List<Selection<?>> selections) {
    List<Order> orders = new LinkedList<>();
    
    if (spaceFilter.isLastAccess()) {
      Path<SpaceMemberEntity> join = getMembersJoin(spaceEntity, JoinType.LEFT);
      Path<Date> field = join.get(SpaceMemberEntity_.lastAccess);
      orders.add(cb.desc(field));
      selections.add(field);
    } else if (spaceFilter.isVisited()) {
      Path<SpaceMemberEntity> join = getMembersJoin(spaceEntity, JoinType.LEFT);
      Path<Boolean> visitedField = join.get(SpaceMemberEntity_.visited);
      Path<String> displayNameField = spaceEntity.get(SpaceEntity_.displayName);
      orders.add(cb.desc(visitedField));
      orders.add(cb.asc(displayNameField));
      selections.add(visitedField);
      selections.add(displayNameField);
    } else {
      Sorting sorting = spaceFilter.getSorting();
      Expression<?> shortField = getSortField(spaceEntity, sorting.sortBy);
      if (sorting.orderBy.equals(Sorting.OrderBy.DESC)) {
        orders.add(cb.desc(shortField));
      } else {
        orders.add(cb.asc(shortField));
      }
      selections.add(shortField);
    }

    return orders.toArray(new Order[orders.size()]);
  }

  private Expression<?> getSortField(Root<SpaceEntity> spaceEntity, SortBy sortBy) {
    if (sortBy.equals(SortBy.DATE)) {      
      return spaceEntity.get(SpaceEntity_.createdDate);
    } else {
      return spaceEntity.get(SpaceEntity_.prettyName);
    }
  }
}
