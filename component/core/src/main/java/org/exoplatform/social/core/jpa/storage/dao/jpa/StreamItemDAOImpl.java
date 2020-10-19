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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.StreamItemDAO;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity_;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 18, 2015  
 */
public class StreamItemDAOImpl extends GenericDAOJPAImpl<StreamItemEntity, Long>  implements StreamItemDAO {

  //Add customize methods here
  
  public List<StreamItemEntity> findStreamItemByActivityId(Long activityId) {
    try {
      EntityManager em = getEntityManager();
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<StreamItemEntity> criteria = cb.createQuery(StreamItemEntity.class);
      Root<StreamItemEntity> root = criteria.from(StreamItemEntity.class);
      CriteriaQuery<StreamItemEntity> select = criteria.select(root);
      select.where(cb.equal(root.get(StreamItemEntity_.activity), activityId));
      //
      TypedQuery<StreamItemEntity> typedQuery = em.createQuery(select);
      return typedQuery.getResultList();
    } catch (RuntimeException e) {
      return null;
    }
  }
}
