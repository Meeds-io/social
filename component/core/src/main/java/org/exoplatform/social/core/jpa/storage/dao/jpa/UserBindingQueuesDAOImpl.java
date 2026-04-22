/**
 * This file is part of the Meeds project (https://meeds.io/).
 * <p>
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.dao.jpa;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.UserBindingsQueueDAO;
import org.exoplatform.social.core.jpa.storage.entity.UserBindingsQueueEntity;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserBindingQueuesDAOImpl extends GenericDAOJPAImpl<UserBindingsQueueEntity, Long>
        implements UserBindingsQueueDAO {
  @Override
  public UserBindingsQueueEntity findFirstUserBindingsQueue() {
    TypedQuery<UserBindingsQueueEntity> query =
            getEntityManager().createNamedQuery("SocUserBindingsQueue.findFirstUserBindingsQueue",
                    UserBindingsQueueEntity.class);
    query.setMaxResults(1);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public List<UserBindingsQueueEntity> findUserBindingsQueueByUserAndAction(String userId, String action) {
    TypedQuery<UserBindingsQueueEntity> query =
            getEntityManager().createNamedQuery("SocUserBindingsQueue.getUserBindingsQueueByUserAndAction",
                    UserBindingsQueueEntity.class);
    query.setParameter("userId", userId);
    query.setParameter("action", action);
    return query.getResultList();
  }
}
