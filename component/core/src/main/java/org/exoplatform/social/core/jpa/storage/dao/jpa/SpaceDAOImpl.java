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

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.*;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;

public class SpaceDAOImpl extends GenericDAOJPAImpl<SpaceEntity, Long> implements SpaceDAO {

  @Override
  public List<Long> getLastSpaces(int limit) {
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SpaceEntity.getLastSpaces", Tuple.class);
    query.setMaxResults(limit);
    List<Tuple> resultList = query.getResultList();
    if (resultList == null || resultList.isEmpty()) {
      return Collections.emptyList();
    } else {
      return resultList.stream()
                       .map(tuple -> tuple.get(0, Long.class))
                       .collect(Collectors.toList());
    }
  }

  @Override
  public SpaceEntity getSpaceByGroupId(String groupId) {
    TypedQuery<SpaceEntity> query = getEntityManager().createNamedQuery("SpaceEntity.getSpaceByGroupId", SpaceEntity.class);
    query.setParameter("groupId", groupId);
    try {
      return query.getSingleResult();      
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public SpaceEntity getSpaceByURL(String url) {
    TypedQuery<SpaceEntity> query = getEntityManager().createNamedQuery("SpaceEntity.getSpaceByURL", SpaceEntity.class);
    query.setParameter("url", url);
    try {
      return query.getSingleResult();      
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public SpaceEntity getSpaceByDisplayName(String spaceDisplayName) {
    TypedQuery<SpaceEntity> query = getEntityManager().createNamedQuery("SpaceEntity.getSpaceByDisplayName", SpaceEntity.class);
    query.setParameter("displayName", spaceDisplayName);
    try {
      return query.getSingleResult();      
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
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

  @Override
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
    query.setParameter("userId", userId);
    query.setParameter("otherUserId", otherUserId);
    query.setFirstResult(offset);
    query.setMaxResults(limit);
    return query.getResultList();

  }

  @Override
  public int countCommonSpaces(String userId, String otherUserId) {
    if (userId == null || userId.equals("")) {
      throw new IllegalArgumentException("userId is null or equals to 0");
    }
    if (otherUserId == null || otherUserId.equals("")) {
      throw new IllegalArgumentException("otherUserId is null or equals to 0");
    }

    TypedQuery<Long> query = getEntityManager().createNamedQuery("SpaceEntity.countCommonSpacesBetweenTwoUsers",
            Long.class);
    query.setParameter("userId", userId);
    query.setParameter("otherUserId", otherUserId);
    return query.getSingleResult().intValue();

  }
}
