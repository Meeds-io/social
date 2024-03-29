/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingEntity;

import jakarta.persistence.TypedQuery;
import java.util.List;

public class GroupSpaceBindingDAOImpl extends GenericDAOJPAImpl<GroupSpaceBindingEntity, Long> implements GroupSpaceBindingDAO {

  @Override
  public List<GroupSpaceBindingEntity> findGroupSpaceBindingsBySpace(Long spaceId) {
    TypedQuery<GroupSpaceBindingEntity> query =
                                              getEntityManager().createNamedQuery("SocGroupSpaceBinding.findGroupSpaceBindingsBySpace",
                                                                                  GroupSpaceBindingEntity.class);
    query.setParameter("spaceId", spaceId);
    return query.getResultList();
  }

  @Override
  public List<GroupSpaceBindingEntity> findGroupSpaceBindingsByGroup(String group) {
    TypedQuery<GroupSpaceBindingEntity> query =
                                              getEntityManager().createNamedQuery("SocGroupSpaceBinding.findGroupSpaceBindingsByGroup",
                                                                                  GroupSpaceBindingEntity.class);
    query.setParameter("group", group);
    return query.getResultList();
  }

}
