/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.*;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.MetadataItemEntity;

public class MetadataItemDAO extends GenericDAOJPAImpl<MetadataItemEntity, Long> {

  private static final String METADATA_ID      = "metadataId";

  private static final String METADATA_NAME    = "metadataName";

  private static final String METADATA_TYPE    = "metadataType";

  private static final String PARENT_OBJECT_ID = "parentObjectId";

  private static final String OBJECT_ID        = "objectId";

  private static final String OBJECT_TYPE      = "objectType";

  public List<MetadataItemEntity> getMetadataItemsByObject(String objectType, String objectId) {
    TypedQuery<MetadataItemEntity> query = getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByObject",
                                                                               MetadataItemEntity.class);
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(OBJECT_ID, objectId);
    return query.getResultList();
  }

  public Set<String> getMetadataNamesByObject(String objectType, String objectId) {
    TypedQuery<String> query = getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataNamesByObject",
                                                                   String.class);
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(OBJECT_ID, objectId);
    List<String> resultList = query.getResultList();
    return resultList == null ? Collections.emptySet() : new HashSet<>(resultList);
  }

  public List<MetadataItemEntity> getMetadataItemsByMetadataAndObject(long metadataId, String objectType, String objectId) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataAndObject",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_ID, metadataId);
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(OBJECT_ID, objectId);
    return query.getResultList();
  }

  public List<String> getMetadataObjectIds(long metadataType,
                                           String metadataName,
                                           String objectType,
                                           long offset,
                                           long limit) {
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataObjectIds",
                                                                  Tuple.class);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(METADATA_NAME, metadataName);
    query.setParameter(OBJECT_TYPE, objectType);
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
      return result.stream()
                   .map(tuple -> (String) tuple.get(0))
                   .distinct()
                   .collect(Collectors.toList());
    }
  }

  @ExoTransactional
  public int deleteMetadataItemsByObject(String objectType, String objectId) {
    Query query = getEntityManager().createNamedQuery("SocMetadataItemEntity.deleteMetadataItemsByObject");
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(OBJECT_ID, objectId);
    return query.executeUpdate();
  }

  @ExoTransactional
  public int deleteMetadataItemsByParentObject(String objectType, String parentObjectId) {
    Query query = getEntityManager().createNamedQuery("SocMetadataItemEntity.deleteMetadataItemsByParentObject");
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(PARENT_OBJECT_ID, parentObjectId);
    return query.executeUpdate();
  }

  @ExoTransactional
  public int deleteMetadataItemById(long id) {
    Query query = getEntityManager().createNamedQuery("SocMetadataItemEntity.deleteMetadataItemById");
    query.setParameter("id", id);
    return query.executeUpdate();
  }

}
