/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.MetadataItemEntity;

public class MetadataItemDAO extends GenericDAOJPAImpl<MetadataItemEntity, Long> {

  private static final String PROPERTY_VALUE_PARAM = "propertyValue";

  private static final String PROPERTY_KEY_PARAM   = "propertyKey";

  private static final String OBJECT_TYPE_PARAM    = "objectType";

  private static final String METADATA_NAME_PARAM  = "metadataName";

  private static final String METADATA_TYPE_PARAM  = "metadataType";

  private static final String METADATA_ID          = "metadataId";

  private static final String METADATA_NAME        = METADATA_NAME_PARAM;

  private static final String METADATA_TYPE        = METADATA_TYPE_PARAM;

  private static final String PARENT_OBJECT_ID     = "parentObjectId";

  private static final String OBJECT_ID            = "objectId";

  private static final String SPACE_ID             = "spaceId";
  
  private static final String SPACE_IDS            = "spaceIds";

  private static final String OBJECT_TYPE          = OBJECT_TYPE_PARAM;

  private static final String CREATOR_ID           = "creatorId";

  private static final String AUDIENCE_ID          = "audienceId";

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

  public List<MetadataItemEntity> getMetadataItemsByMetadataTypeAndObject(long metadataType, String objectType, String objectId) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndObject",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(OBJECT_ID, objectId);
    return query.getResultList();
  }

  public List<MetadataItemEntity> getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(String metadataName,
                                                                                                        long metadataType,
                                                                                                        String objectType,
                                                                                                        String propertyKey,
                                                                                                        String propertyValue,
                                                                                                        long offset,
                                                                                                        long limit) {
    try {
      TypedQuery<MetadataItemEntity> query =
                                           getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty",
                                                                               MetadataItemEntity.class);
      query.setParameter(METADATA_TYPE_PARAM, metadataType);
      query.setParameter(METADATA_NAME_PARAM, metadataName);
      query.setParameter(OBJECT_TYPE_PARAM, objectType);
      query.setParameter(PROPERTY_KEY_PARAM, propertyKey);
      query.setParameter(PROPERTY_VALUE_PARAM, propertyValue);
      if (offset > 0) {
        query.setFirstResult((int) offset);
      }
      if (limit > 0) {
        query.setMaxResults((int) limit);
      }
      return query.getResultList();
    } catch (NoResultException e) {
      return Collections.emptyList();
    }
  }

  public List<MetadataItemEntity> getMetadataItemsByMetadataNameAndTypeAndObject(String metadataName,
                                                                                 long metadataType,
                                                                                 String objectType,
                                                                                 long offset,
                                                                                 long limit) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndNameAndObject",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_NAME, metadataName);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(OBJECT_TYPE, objectType);
    if (offset > 0) {
      query.setFirstResult((int) offset);
    }
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    return query.getResultList();
  }

  public List<MetadataItemEntity> getMetadataItemsByMetadataNameAndTypeAndObject(String metadataName,
                                                                                 long metadataType,
                                                                                 String objectType,
                                                                                 String objectId,
                                                                                 long offset,
                                                                                 long limit) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndNameAndObjectTypeAndId",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_NAME, metadataName);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(OBJECT_ID, objectId);
    if (offset > 0) {
      query.setFirstResult((int) offset);
    }
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    return query.getResultList();
  }

  public List<MetadataItemEntity> getMetadataItemsByMetadataNameAndTypeAndSpaceIds(String metadataName,
                                                                                   long metadataType,
                                                                                   List<Long> spaceIds,
                                                                                   long offset,
                                                                                   long limit) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataNameAndTypeAndSpaceIds",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_NAME, metadataName);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(SPACE_IDS, spaceIds);
    if (offset > 0) {
      query.setFirstResult((int) offset);
    }
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    return query.getResultList();
  }

  public List<MetadataItemEntity> getMetadataItemsByMetadataNameAndTypeAndObjectAndSpaceIds(String metadataName,
                                                                                            long metadataType,
                                                                                            String objectType,
                                                                                            List<Long> spaceIds,
                                                                                            long offset,
                                                                                            long limit) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndNameAndObjectAndSpaceIds",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_NAME, metadataName);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(OBJECT_TYPE, objectType);
    query.setParameter(SPACE_IDS, spaceIds);
    if (offset > 0) {
      query.setFirstResult((int) offset);
    }
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    return query.getResultList();
  }

  public List<MetadataItemEntity> getMetadataItemsByTypeAndSpaceIdAndCreatorId(long metadataType,
                                                                               long spaceId,
                                                                               long creatorId) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByTypeAndSpaceIdAndCreatorId",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(CREATOR_ID, creatorId);
    query.setParameter(SPACE_ID, spaceId);
    List<MetadataItemEntity> result = query.getResultList();
    return result == null ? Collections.emptyList() : result;
  }

  public List<MetadataItemEntity> getMetadataItemsByMetadataTypeAndCreator(long metadataType,
                                                                           long creatorId,
                                                                           long offset,
                                                                           long limit) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataItemsByMetadataTypeAndCreator",
                                                                             MetadataItemEntity.class);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(CREATOR_ID, creatorId);
    if (offset > 0) {
      query.setFirstResult((int) offset);
    }
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    return query.getResultList();
  }

  public int countMetadataItemsByMetadataTypeAndCreator(long metadataType, long creatorId) {
    TypedQuery<Long> query =
                           getEntityManager().createNamedQuery("SocMetadataItemEntity.countMetadataItemsByMetadataTypeAndCreator",
                                                               Long.class);
    query.setParameter(METADATA_TYPE, metadataType);
    query.setParameter(CREATOR_ID, creatorId);
    return query.getSingleResult().intValue();
  }

  public List<Tuple> countMetadataItemsByMetadataTypeAndAudienceId(long metadataType, long creatorId, long spaceId) {
    TypedQuery<Tuple> query =
                            getEntityManager().createNamedQuery("SocMetadataItemEntity.countMetadataItemsByMetadataTypeAndAudienceId",
                                                                Tuple.class);
    query.setParameter(METADATA_TYPE_PARAM, metadataType);
    query.setParameter(CREATOR_ID, creatorId);
    query.setParameter(SPACE_ID, spaceId);
    List<Tuple> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  public Map<Long, Long> countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(long metadataType,
                                                                                 long creatorId,
                                                                                 List<Long> spaceIds) {
    TypedQuery<Tuple> query =
                            getEntityManager().createNamedQuery("SocMetadataItemEntity.countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId",
                                                                Tuple.class);
    query.setParameter(METADATA_TYPE_PARAM, metadataType);
    query.setParameter(AUDIENCE_ID, creatorId);
    query.setParameter(SPACE_IDS, spaceIds);
    Map<Long, Long> result = query.getResultList()
                                  .stream()
                                  .collect(Collectors.toMap(t -> t.get(0, Long.class), t -> t.get(1, Long.class)));
    if (MapUtils.isEmpty(result)) {
      return Collections.emptyMap();
    } else {
      return result;
    }
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
                   .toList();
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

  @ExoTransactional
  public int deleteMetadataItemsBySpaceId(long spaceId) {
    Query query = getEntityManager().createNamedQuery("SocMetadataItemEntity.deleteMetadataItemsBySpaceId");
    query.setParameter(SPACE_ID, spaceId);
    return query.executeUpdate();
  }

  @ExoTransactional
  public int deleteMetadataItemsBySpaceIdAndAudienceId(long spaceId, long audienceId) {
    TypedQuery<MetadataItemEntity> query =
                                         getEntityManager().createNamedQuery("SocMetadataItemEntity.getMetadataBySpaceIdAndAudienceId",
                                                                             MetadataItemEntity.class);
    query.setParameter(SPACE_ID, spaceId);
    query.setParameter(AUDIENCE_ID, audienceId);
    List<MetadataItemEntity> items = query.getResultList();
    if (CollectionUtils.isNotEmpty(items)) {
      deleteAll(items);
    }
    return items.size();
  }

}
