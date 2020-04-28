/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;

public class SpaceDAOImpl extends GenericDAOJPAImpl<SpaceEntity, Long> implements SpaceDAO {

  @Override
  public List<SpaceEntity> getLastSpaces(int limit) {
    TypedQuery<SpaceEntity> query = getEntityManager().createNamedQuery("SpaceEntity.getLastSpaces", SpaceEntity.class);
    query.setMaxResults(limit);
    return query.getResultList();
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

}
