/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

package io.meeds.social.link.dao;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

import io.meeds.social.link.entity.LinkEntity;

public class LinkDAO extends GenericDAOJPAImpl<LinkEntity, Long> {

  public List<LinkEntity> getLinks(String name) {
    TypedQuery<LinkEntity> query = getEntityManager().createNamedQuery("LinkEntity.findByName", LinkEntity.class);
    query.setParameter("name", name);
    List<LinkEntity> list = query.getResultList();
    if (CollectionUtils.isEmpty(list)) {
      return Collections.emptyList();
    } else {
      return list;
    }
  }

  public long getLinkSettingByLinkId(long linkId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("LinkEntity.getLinkSettingByLinkId", Long.class);
    query.setParameter("id", linkId);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return 0l;
    }
  }

}
