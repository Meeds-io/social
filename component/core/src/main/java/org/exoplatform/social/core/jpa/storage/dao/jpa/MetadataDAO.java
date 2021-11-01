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

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.entity.MetadataEntity;

public class MetadataDAO extends GenericDAOJPAImpl<MetadataEntity, Long> {

  private static final String AUDIENCE_IDS       = "audienceIds";

  private static final String CREATOR_ID         = "creatorId";

  private static final String METADATA_TYPE      = "type";

  private static final String METADATA_NAME_PART = "term";

  public List<String> getMetadataNamesByAudiences(long metadataTypeId,
                                                  Set<Long> audienceIds,
                                                  long limit) {
    if (CollectionUtils.isEmpty(audienceIds)) {
      return Collections.emptyList();
    }
    TypedQuery<String> query = getEntityManager().createNamedQuery("SocMetadataEntity.getMetadataNamesByAudiences",
                                                                   String.class);
    query.setParameter(METADATA_TYPE, metadataTypeId);
    query.setParameter(AUDIENCE_IDS, audienceIds);
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    List<String> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      return result.stream().distinct().collect(Collectors.toList());
    }
  }

  public List<String> getMetadataNamesByCreator(long metadataTypeId,
                                                long creatorIdentityId,
                                                long limit) {
    TypedQuery<String> query = getEntityManager().createNamedQuery("SocMetadataEntity.getMetadataNamesByCreator",
                                                                   String.class);
    query.setParameter(METADATA_TYPE, metadataTypeId);
    query.setParameter(CREATOR_ID, creatorIdentityId);
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    List<String> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      return result.stream().distinct().collect(Collectors.toList());
    }
  }

  public List<String> findMetadataNameByAudiencesAndQuery(String term,
                                                          long metadataTypeId,
                                                          Set<Long> audienceIds,
                                                          long limit) {
    if (CollectionUtils.isEmpty(audienceIds)) {
      return Collections.emptyList();
    }
    TypedQuery<String> query = getEntityManager().createNamedQuery("SocMetadataEntity.findMetadataNameByAudiencesAndQuery",
                                                                   String.class);
    query.setParameter(METADATA_TYPE, metadataTypeId);
    query.setParameter(AUDIENCE_IDS, audienceIds);
    query.setParameter(METADATA_NAME_PART, "%" + StringUtils.lowerCase(term) + "%");
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    List<String> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      return result.stream().distinct().collect(Collectors.toList());
    }
  }

  public List<String> findMetadataNameByCreatorAndQuery(String term, long metadataTypeId, long creatorIdentityId, long limit) {
    TypedQuery<String> query = getEntityManager().createNamedQuery("SocMetadataEntity.findMetadataNameByCreatorAndQuery",
                                                                   String.class);
    query.setParameter(METADATA_TYPE, metadataTypeId);
    query.setParameter(CREATOR_ID, creatorIdentityId);
    query.setParameter(METADATA_NAME_PART, "%" + StringUtils.lowerCase(term) + "%");
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    List<String> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      return result.stream().distinct().collect(Collectors.toList());
    }
  }

  public MetadataEntity findMetadata(long type, String name, long audienceId) {
    TypedQuery<MetadataEntity> query = getEntityManager().createNamedQuery("SocMetadataEntity.findMetadata",
                                                                           MetadataEntity.class);
    query.setParameter("type", type);
    query.setParameter("name", name);
    query.setParameter("audienceId", audienceId);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

}
