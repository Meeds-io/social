/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.storage.dao.jpa.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity_;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity_;

import org.apache.commons.collections4.CollectionUtils;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProfileQueryBuilder {

  ExtendProfileFilter filter;

  private ProfileQueryBuilder() {

  }

  public static ProfileQueryBuilder builder() {
    return new ProfileQueryBuilder();
  }

  public ProfileQueryBuilder withFilter(ExtendProfileFilter filter) {
    this.filter = filter;
    return this;
  }

  /**
   *
   * @param em the EntityManager
   * @return the JPA TypedQuery
   */
  public TypedQuery[] build(EntityManager em) {
    TypedQuery<IdentityEntity> select = buildListQuery(em);
    TypedQuery<Long> count = buildCountquery(em);
    return new TypedQuery[]{select, count};
  }

  private TypedQuery<Long> buildCountquery(EntityManager em) {
    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    Root<IdentityEntity> identityCountQuery = countQuery.from(IdentityEntity.class);
    countQuery.select(criteriaBuilder.countDistinct(identityCountQuery)).where(buildPredicates(criteriaBuilder, identityCountQuery));
    return em.createQuery(countQuery);
  }

  private TypedQuery<IdentityEntity> buildListQuery(EntityManager em) {
    CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
    CriteriaQuery<IdentityEntity> listQuery = criteriaBuilder.createQuery(IdentityEntity.class);
    Root<IdentityEntity> identityListQuery = listQuery.from(IdentityEntity.class);
    listQuery.select(identityListQuery).distinct(true).where(buildPredicates(criteriaBuilder, identityListQuery));
    return em.createQuery(listQuery);
  }

  private Predicate[] buildPredicates(CriteriaBuilder cb, Root<IdentityEntity> identityListQuery) {
    List<Predicate> predicates = new ArrayList<>();

    if (filter != null) {
      if (filter.isExcludeDeleted()) {
        predicates.add(cb.isFalse(identityListQuery.get(IdentityEntity_.deleted)));
      }

      if (filter.isExcludeDisabled()) {
        predicates.add(cb.isTrue(identityListQuery.get(IdentityEntity_.enabled)));
      }

      if (CollectionUtils.isNotEmpty(filter.getIdentityIds())) {
        predicates.add(identityListQuery.get(IdentityEntity_.id).in(filter.getIdentityIds()));
      }

      if (CollectionUtils.isNotEmpty(filter.getRemoteIds())) {
        predicates.add(identityListQuery.get(IdentityEntity_.remoteId).in(filter.getRemoteIds()));
      }

      if (filter.getProviderId() != null && !filter.getProviderId().isEmpty()) {
        predicates.add(cb.equal(identityListQuery.get(IdentityEntity_.providerId), filter.getProviderId()));
      }

      SetJoin<IdentityEntity, ProfileExperienceEntity> experience = null;

      List<Identity> excludes = filter.getExcludedIdentityList();
      if (CollectionUtils.isNotEmpty(excludes)) {
        List<Long> ids = new ArrayList<>(excludes.size());
        for (Identity id : excludes) {
          ids.add(Long.parseLong(id.getId()));
        }
        predicates.add(cb.not(identityListQuery.get(IdentityEntity_.id).in(ids)));
      }

      String all = filter.getAll();
      if (all == null || all.trim().isEmpty()) {
        String name = filter.getName();
        if (name != null && !name.isEmpty()) {
          name = processLikeString(name);
          MapJoin<IdentityEntity, String, String> properties = identityListQuery.join(IdentityEntity_.properties, JoinType.LEFT);
          predicates.add(cb.and(cb.like(cb.lower(properties.value()), name), properties.key().in(Arrays.asList(Profile.FIRST_NAME, Profile.LAST_NAME, Profile.FULL_NAME))));
        }

        String val = filter.getPosition();
        if (val != null && !val.isEmpty()) {
          val = processLikeString(val);
          Predicate[] p = new Predicate[2];
          MapJoin<IdentityEntity, String, String> properties = identityListQuery.join(IdentityEntity_.properties, JoinType.LEFT);
          p[1] = cb.and(cb.like(cb.lower(properties.value()), val), cb.equal(properties.key(), Profile.POSITION));
          if(experience == null) {
            experience = identityListQuery.join(IdentityEntity_.experiences, JoinType.LEFT);
          }
          p[0] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.position)), val);

          predicates.add(cb.or(p));
        }

        val = filter.getSkills();
        if (val != null && !val.isEmpty()) {
          val = processLikeString(val);
          if(experience == null) {
            experience = identityListQuery.join(IdentityEntity_.experiences, JoinType.LEFT);
          }
          predicates.add(cb.like(cb.lower(experience.get(ProfileExperienceEntity_.skills)), val));
        }

        val = filter.getCompany();
        if (val != null && !val.isEmpty()) {
          val = processLikeString(val);
          if(experience == null) {
            experience = identityListQuery.join(IdentityEntity_.experiences, JoinType.LEFT);
          }
          predicates.add(cb.like(cb.lower(experience.get(ProfileExperienceEntity_.company)), val));
        }
      } else {

        String name = filter.getName();
        all = processLikeString(all).toLowerCase();
        Predicate[] p = new Predicate[5];
        MapJoin<IdentityEntity, String, String> properties = identityListQuery.join(IdentityEntity_.properties, JoinType.LEFT);
        p[0] = cb.and(cb.like(cb.lower(properties.value()), name), properties.key().in(Arrays.asList(Profile.FIRST_NAME, Profile.LAST_NAME, Profile.FULL_NAME)));

        if(experience == null) {
          experience = identityListQuery.join(IdentityEntity_.experiences, JoinType.LEFT);
        }
        p[1] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.position)), all);
        p[2] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.skills)), all);
        p[3] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.company)), all);
        p[4] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.description)), all);

        predicates.add(cb.or(p));
      }
    }

    Predicate[] pds = predicates.toArray(new Predicate[predicates.size()]);
    return pds;
  }

  private String processLikeString(String s) {
    return "%" + s.toLowerCase() + "%";
  }
}
