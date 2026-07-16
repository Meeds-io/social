/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamType;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.storage.ActivityStorageException;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

public class ActivityDAOImpl extends GenericDAOJPAImpl<ActivityEntity, Long> implements ActivityDAO {

  private static final List<StreamType> MAIN_STREAM_TYPES         = List.of(StreamType.SPACE, StreamType.POSTER);

  private static final String           MAIN_STREAM_TYPES_PARAM   = "mainStreamTypes";

  private static final String           ACTIVITY_POSTER_ID        = "posterId";

  private static final String           ACTIVITY_ID_PARAM         = "activityId";

  private static final String           ACTIVITY_PROVIDER_ID      = "providerId";

  private static final String           USER_PROVIDER_ID          = "userProviderId";

  private static final String           SPACE_PROVIDER_ID         = "spaceProviderId";

  private static final String           ACTIVITY_OWNER_IDS        = "ownerIds";

  public static final String            CATEGORY_IDS              = "categoryIds";

  public static final String            EXCLUDED_CATEGORY_IDS     = "excludedCategoryIds";

  private static final String           STREAM_TYPE               = "streamType";

  private static final String           QUERY_FILTER_FIND_PREFIX  = "SocActivity.findAllActivities";

  private static final String           QUERY_FILTER_COUNT_PREFIX = "SocActivity.countAllActivities";

  private final Map<String, Boolean>    filterNamedQueries        = new HashMap<>();

  private final ConnectionDAO           connectionDAO;

  public ActivityDAOImpl(ConnectionDAO connectionDAO) {
    this.connectionDAO = connectionDAO;
  }

  public List<Long> getActivities(Identity owner, Identity viewer, long offset, long limit) throws ActivityStorageException {
    long ownerId = Long.parseLong(owner.getId());

    TypedQuery<Tuple> query = null;
    if (viewer != null && !viewer.getId().equals(owner.getId())) {
      // if viewer is different from owner
      // get activities of type 'organization' where:
      // owner is the creator of activity
      // owner has reacted on any other activity
      // outside space activities
      query = getEntityManager().createNamedQuery("SocActivity.getActivityByOwnerAndProviderId", Tuple.class);
      query.setParameter(ACTIVITY_PROVIDER_ID, OrganizationIdentityProvider.NAME);
    } else {
      // if viewer the owner
      // get all his activities including spaces activities
      query = getEntityManager().createNamedQuery("SocActivity.getActivityByOwner", Tuple.class);
    }
    query.setParameter("owners", Collections.singleton(ownerId));
    if (limit > 0) {
      query.setFirstResult(offset > 0 ? (int) offset : 0);
      query.setMaxResults((int) limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public List<Long> getScheduledActivityIds(long dueTime, int offset, int limit) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getScheduledActivityIds", Long.class);
    query.setParameter("dueTime", dueTime);
    if (limit > 0) {
      query.setFirstResult(Math.max(offset, 0));
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  @ExoTransactional
  public boolean claimScheduledActivity(long activityId, long publishTime) {
    Query query = getEntityManager().createNamedQuery("SocActivity.publishScheduledActivity");
    query.setParameter(ACTIVITY_ID_PARAM, activityId);
    query.setParameter("publishTime", publishTime);
    boolean claimed = query.executeUpdate() > 0;
    if (claimed) {
      // The bulk update bypasses the persistence context: refresh the entity
      // so that callers of the current transaction don't read a stale state
      ActivityEntity activityEntity = getEntityManager().find(ActivityEntity.class, activityId);
      if (activityEntity != null) {
        getEntityManager().refresh(activityEntity);
      }
    }
    return claimed;
  }

  @Override
  public List<String> getUserIdsActivities(Identity owner, long offset, long limit) throws ActivityStorageException {
    long ownerId = Long.parseLong(owner.getId());

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocActivity.getActivityIdsByOwner", Tuple.class);
    query.setParameter("owner", ownerId);
    if (limit > 0) {
      query.setFirstResult(offset > 0 ? (int)offset : 0);
      query.setMaxResults((int)limit);
    }
    return convertActivityEntitiesToIdsString(query.getResultList());
  }

  public List<Long> getActivityFeed(Identity ownerIdentity, int offset, int limit, List<String> spaceIds) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    String queryName = "SocActivity.getActivityFeed";
    if (connections.isEmpty()) {
      queryName += "NoConnections";
    }

    List<Long> owners = new ArrayList<>();
    owners.add(ownerId);
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery(queryName, Tuple.class);
    if (!connections.isEmpty()) {
      query.setParameter("connections", connections);
      query.setParameter("connStreamType", StreamType.POSTER);
    }
    query.setParameter("owners", owners);

    if (limit > 0) {
      query.setFirstResult(offset > 0 ? offset : 0);
      query.setMaxResults(limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public List<Long> getActivityByFilter(ActivityFilter activityFilter, List<String> streamIdentityIds, int offset, int limit) {

    TypedQuery<Tuple> query = buildQueryFromFilter(activityFilter, streamIdentityIds, Tuple.class, false);
    if (limit > 0) {
      query.setFirstResult(Math.max(offset, 0));
      query.setMaxResults(limit);
    }
    List<Tuple> resultList = query.getResultList();

    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public List<String> getActivityIdsByFilter(ActivityFilter activityFilter,
                                             List<String> streamIdentityIds,
                                             int offset,
                                             int limit) {

    TypedQuery<Tuple> query = buildQueryFromFilter(activityFilter, streamIdentityIds, Tuple.class, false);
    if (limit > 0) {
      query.setFirstResult(Math.max(offset, 0));
      query.setMaxResults(limit);
    }
    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIdsString(resultList);
  }

  @Override
  public int getActivitiesCountByFilter(ActivityFilter activityFilter, List<String> streamIdentityIds) {
    TypedQuery<Long> query = buildQueryFromFilter(activityFilter, streamIdentityIds, Long.class, true);
    return query.getSingleResult().intValue();
  }

  @Override
  public List<String> getActivityIdsFeed(Identity ownerIdentity,
                                           int offset,
                                           int limit,
                                           List<String> spaceIds) {
    long ownerId = Long.parseLong(ownerIdentity.getId());
    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    String queryName = "SocActivity.getActivityIdsFeed";
    if (connections.isEmpty()) {
      queryName += "NoConnections";
    }

    List<Long> owners = new ArrayList<>();
    owners.add(ownerId);
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery(queryName, Tuple.class);
    if (!connections.isEmpty()) {
      query.setParameter("connections", connections);
      query.setParameter(STREAM_TYPE, StreamType.POSTER);
    }
    query.setParameter("owners", owners);
    query.setParameter("streamTypes", Arrays.asList(StreamType.POSTER,StreamType.SPACE));

    if (limit > 0) {
      query.setFirstResult(offset > 0 ? offset : 0);
      query.setMaxResults(limit);
    }

    return convertActivityEntitiesToIdsString(query.getResultList());
  }

  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity, List<String> spaceIds) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    String queryName = "SocActivity.getNumberOfActivitesOnActivityFeed";
    if(connections.isEmpty()) {
      queryName += "NoConnections";
    }
    List<Long> owners = new ArrayList<>();
    for (String id : spaceIds) {
      owners.add(Long.parseLong(id));
    }

    owners.add(ownerId);
    
    TypedQuery<Long> query = getEntityManager().createNamedQuery(queryName, Long.class);
    if(!connections.isEmpty()) {
      query.setParameter("connections", connections);
      query.setParameter("connStreamType", StreamType.POSTER);
    }
    query.setParameter("owners", owners);

    return query.getSingleResult().intValue();
  }
  
  @Override
  public List<Long> getNewerOnActivityFeed(Identity ownerIdentity, long sinceTime, int limit, List<String> spaceIds) {
    long ownerId = Long.parseLong(ownerIdentity.getId());
    List<Long> owners = new ArrayList<>();
    owners.add(ownerId);
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }
    }

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    String queryName = "SocActivity.getNewerActivityFeed";
    if (connections.isEmpty()) {
      queryName += "NoConnections";
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery(queryName, Tuple.class);
    if (!connections.isEmpty()) {
      query.setParameter("connections", connections);
      query.setParameter("connStreamType", StreamType.POSTER);
    }
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("owners", owners);

    if (limit > 0) {
      query.setFirstResult(0);
      query.setMaxResults(limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  
  @Override
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, long sinceTime, List<String> spaceIds) {
    long ownerId = Long.parseLong(ownerIdentity.getId());
    List<Long> owners = new ArrayList<>();
    owners.add(ownerId);
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }
    }

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    String queryName = "SocActivity.getNumberOfNewerOnActivityFeed";
    if(connections.isEmpty()) {
      queryName += "NoConnections";
    }
    TypedQuery<Long> query = getEntityManager().createNamedQuery(queryName, Long.class);
    if(!connections.isEmpty()) {
      query.setParameter("connections", connections);
      query.setParameter("connStreamType", StreamType.POSTER);
    }
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("owners", owners);

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getOlderOnActivityFeed(Identity ownerIdentity, long sinceTime,int limit, List<String> spaceIds) {
    long ownerId = Long.parseLong(ownerIdentity.getId());
    List<Long> owners = new ArrayList<>();
    owners.add(ownerId);
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }
    }

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    String queryName = "SocActivity.getOlderActivityFeed";
    if (connections.isEmpty()) {
      queryName += "NoConnections";
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery(queryName, Tuple.class);
    if (!connections.isEmpty()) {
      query.setParameter("connections", connections);
      query.setParameter("connStreamType", StreamType.POSTER);
    }
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("owners", owners);

    if (limit > 0) {
      query.setFirstResult(0);
      query.setMaxResults(limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, long sinceTime, List<String> spaceIds) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    String queryName = "SocActivity.getNumberOfOlderOnActivityFeed";
    if(connections.isEmpty()) {
      queryName += "NoConnections";
    }

    List<Long> owners = new ArrayList<>();
    owners.add(ownerId);
    if (spaceIds != null && !spaceIds.isEmpty()) {
      for (String id : spaceIds) {
        owners.add(Long.valueOf(id));
      }
    }

    TypedQuery<Long> query = getEntityManager().createNamedQuery(queryName, Long.class);
    if(!connections.isEmpty()) {
      query.setParameter("connections", connections);
      query.setParameter("connStreamType", StreamType.POSTER);
    }
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("owners", owners);

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getUserActivities(Identity owner,
                                          long offset,
                                          long limit) throws ActivityStorageException {

    return getOwnerActivities(Arrays.asList(owner.getId()), -1, -1, offset, limit);
  }
  
  @Override
  public int getNumberOfUserActivities(Identity ownerIdentity) {
    long ownerId = Long.parseLong(ownerIdentity.getId());
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfActivitiesByOwner", Long.class);
    query.setParameter("owner", ownerId);

    return query.getSingleResult().intValue();
  }
  
  @Override
  public List<Long> getNewerOnUserActivities(Identity ownerIdentity, long sinceTime, int limit) {
    return getOwnerActivities(Arrays.asList(ownerIdentity.getId()), sinceTime, -1, 0, limit);

  }

  @Override
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, long sinceTime) {
    long ownerId = Long.parseLong(ownerIdentity.getId());
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfNewerActivityByOwner", Long.class);
    query.setParameter("owner", ownerId);
    query.setParameter("sinceTime", sinceTime);

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getOlderOnUserActivities(Identity ownerIdentity, long sinceTime, int limit) {
    return getOwnerActivities(Arrays.asList(ownerIdentity.getId()), -1, sinceTime, 0, limit);
  }

  @Override
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, long sinceTime) {
    long ownerId = Long.parseLong(ownerIdentity.getId());
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfOlderActivityByOwner", Long.class);
    query.setParameter("owner", ownerId);
    query.setParameter("sinceTime", sinceTime);

    return query.getSingleResult().intValue();
  }

  public List<Long> getSpaceActivities(Identity spaceOwner, long offset, long limit) throws ActivityStorageException {
    return getOwnerActivities(Arrays.asList(spaceOwner.getId()), -1, -1, offset, limit);
  }

  public List<String> getSpaceActivityIds(Identity spaceIdentity, long offset, long limit) throws ActivityStorageException {
    long ownerId = Long.parseLong(spaceIdentity.getId());
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocActivity.getSpacesActivityIds", Tuple.class);
    query.setParameter("owners", Collections.singleton(ownerId));
    query.setParameter(STREAM_TYPE, StreamType.SPACE);
    if (limit > 0) {
      query.setFirstResult(offset > 0 ? (int)offset : 0);
      query.setMaxResults((int)limit);
    }
    return convertActivityEntitiesToIdsString(query.getResultList());
  }
  
  @Override
  public int getNumberOfSpaceActivities(Identity spaceIdentity) {
    long ownerId = Long.parseLong(spaceIdentity.getId());
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfActivitiesByOwner", Long.class);
    query.setParameter("owner", ownerId);

    return query.getSingleResult().intValue();
  }
  
  @Override
  public List<Long> getNewerOnSpaceActivities(Identity spaceIdentity, long sinceTime, int limit) {
    return getOwnerActivities(Arrays.asList(spaceIdentity.getId()), sinceTime, -1, 0, limit);
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, long sinceTime) {
    long ownerId = Long.parseLong(spaceIdentity.getId());

    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfNewerOnActivityFeedNoConnections", Long.class);
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("owners", Collections.singleton(ownerId));

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getOlderOnSpaceActivities(Identity spaceIdentity, long sinceTime, int limit) {
    return getOwnerActivities(Arrays.asList(spaceIdentity.getId()), -1, sinceTime, 0, limit);
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity, long sinceTime) {
    long ownerId = Long.parseLong(spaceIdentity.getId());

    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfOlderOnActivityFeedNoConnections", Long.class);
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("owners", Collections.singleton(ownerId));

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit, List<String> spaceIds) {
    if (spaceIds.size() > 0) {
      return getOwnerActivities(spaceIds, -1, -1, offset, limit);
    } else {
      return Collections.emptyList();
    }
  }
  
  @Override
  public List<String> getUserSpacesActivityIds(Identity ownerIdentity,
                                               int offset,
                                               int limit,
                                               List<String> spaceIds) {
    if (spaceIds.size() == 0) {
      return Collections.emptyList();
    } else {
      List<Long> ids = new ArrayList<>();
      for (String id : spaceIds) {
        ids.add(Long.parseLong(id));
      }
      TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocActivity.getSpacesActivityIds", Tuple.class);

      query.setParameter("owners", ids);
      query.setParameter(STREAM_TYPE, StreamType.SPACE);
      if (limit > 0) {
        query.setFirstResult(offset > 0 ? (int)offset : 0);
        query.setMaxResults((int)limit);
      }

      return convertActivityEntitiesToIdsString(query.getResultList());
    }
  }
  
  public int getNumberOfUserSpacesActivities(Identity ownerIdentity, List<String> spaceIds) {
    if (spaceIds.size() == 0) {
      return 0;
    } else {
      List<Long> owners = new ArrayList<>();
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }

      TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfActivitesOnActivityFeedNoConnections",
                                                                   Long.class);
      query.setParameter("owners", owners);
      return query.getSingleResult().intValue();
    }
  }
  
  @Override
  public List<Long> getNewerOnUserSpacesActivities(Identity ownerIdentity,
                                                       long sinceTime,
                                                       int limit, List<String> spaceIds) {
    if (spaceIds.size() > 0) {
      return getOwnerActivities(spaceIds, sinceTime, -1, 0, limit);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, long sinceTime, List<String> spaceIds) {
    if (spaceIds.size() == 0) {
      return 0;
    } else {
      List<Long> owners = new ArrayList<>();
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }

      TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfNewerOnActivityFeedNoConnections",
                                                                   Long.class);
      query.setParameter("sinceTime", sinceTime);
      query.setParameter("owners", owners);
      return query.getSingleResult().intValue();
    }
  }

  @Override
  public List<Long> getOlderOnUserSpacesActivities(Identity ownerIdentity, long sinceTime, int limit, List<String> spaceIds) {
    if (spaceIds.size() > 0) {
      return getOwnerActivities(spaceIds, -1, sinceTime, 0, limit);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, long sinceTime, List<String> spaceIds) {
    if (spaceIds.size() == 0) {
      return 0;
    } else {
      List<Long> owners = new ArrayList<>();
      for (String id : spaceIds) {
        owners.add(Long.parseLong(id));
      }

      TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfOlderOnActivityFeedNoConnections",
                                                                   Long.class);
      query.setParameter("sinceTime", sinceTime);
      query.setParameter("owners", owners);
      return query.getSingleResult().intValue();
    }
  }

  @Override
  public List<Long> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    if (connections.isEmpty()) {
      return Collections.emptyList();
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocActivity.getActivityOfConnection", Tuple.class);
    query.setParameter("connections", connections);
    query.setParameter("connStreamType", StreamType.POSTER);

    if (limit > 0) {
      query.setFirstResult(offset > 0 ? offset : 0);
      query.setMaxResults(limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }
  
  @Override
  public List<String> getActivityIdsOfConnections(Identity ownerIdentity, int offset, int limit) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    if(connections.isEmpty()) {
      return Collections.emptyList();
    }
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocActivity.getActivityIdsOfConnections", Tuple.class);
    query.setParameter("connections", connections.stream().map(id -> id.toString()).collect(Collectors.toSet()));
    query.setParameter("connStreamType", StreamType.POSTER);

    if (limit > 0) {
      query.setFirstResult(offset > 0 ? offset : 0);
      query.setMaxResults(limit);
    }
    return convertActivityEntitiesToIdsString(query.getResultList());
  }

  @Override
  public int getNumberOfActivitiesOfConnections(Identity ownerIdentity) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    if (connections.isEmpty()) {
      return 0;
    }
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.numberOfActivitiesOfConnections", Long.class);
    query.setParameter("connections", connections.stream().map(id -> id.toString()).collect(Collectors.toSet()));
    query.setParameter("connStreamType", StreamType.POSTER);

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getNewerOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime, long limit) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    if (connections.isEmpty()) {
      return Collections.emptyList();
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocActivity.getNewerActivityOfConnection", Tuple.class);
    query.setParameter("connections", connections);
    query.setParameter("connStreamType", StreamType.POSTER);
    query.setParameter("sinceTime", sinceTime);

    if (limit > 0) {
      query.setFirstResult(0);
      query.setMaxResults((int)limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    if(connections.isEmpty()) {
      return 0;
    }
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfNewerOnActivitiesOfConnections", Long.class);
    query.setParameter("connections", connections);
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("connStreamType", StreamType.POSTER);

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getOlderOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime, int limit) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    if (connections.isEmpty()) {
      return Collections.emptyList();
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SocActivity.getOlderActivityOfConnection", Tuple.class);
    query.setParameter("connections", connections);
    query.setParameter("connStreamType", StreamType.POSTER);
    query.setParameter("sinceTime", sinceTime);

    if (limit > 0) {
      query.setFirstResult(0);
      query.setMaxResults(limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime) {
    long ownerId = Long.parseLong(ownerIdentity.getId());

    Set<Long> connections = connectionDAO.getConnectionIds(ownerId, Type.CONFIRMED);

    if(connections.isEmpty()) {
      return 0;
    }
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.getNumberOfOlderOnActivitiesOfConnections", Long.class);
    query.setParameter("connections", connections);
    query.setParameter("sinceTime", sinceTime);
    query.setParameter("connStreamType", StreamType.POSTER);

    return query.getSingleResult().intValue();
  }

  @Override
  public List<Long> getActivitiesByPoster(Identity posterIdentity, int offset, int limit, String... activityTypes) {
    String queryName = "SocActivity.getActivitiesByPoster";
    List<String> types = new ArrayList<>();
    if (activityTypes != null && activityTypes.length > 0) {
      types.addAll(Arrays.asList(activityTypes));
    } else {
      queryName += "NoTypes";
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery(queryName, Tuple.class);
    if (!types.isEmpty()) {
      query.setParameter("types", types);
    }
    query.setParameter("owner", posterIdentity.getId());

    if (limit > 0) {
      query.setFirstResult(0);
      query.setMaxResults(limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public int getNumberOfActivitiesByPoster(Identity posterIdentity, String... activityTypes) {
    String queryName = "SocActivity.getNumberOfActivitiesByPoster";
    List<String> types = new ArrayList<String>();
    if (activityTypes != null && activityTypes.length > 0) {
      types.addAll(Arrays.asList(activityTypes));
    } else {
      queryName += "NoTypes";
    }

    TypedQuery<Long> query = getEntityManager().createNamedQuery(queryName, Long.class);
    if (!types.isEmpty()) {
      query.setParameter("types", types);
    }
    query.setParameter("owner", posterIdentity.getId());
    return query.getSingleResult().intValue();
  }
  
  /**
   * Gets the activity's ID only and return the list of this one
   * 
   * @param list Activity's Ids
   * @return
   */
  private List<Long> convertActivityEntitiesToIds(List<Tuple> list) {
    Set<Long> ids = new LinkedHashSet<>();
    if (list == null) return Collections.emptyList();
    for (Tuple t : list) {
      ids.add((long) t.get(0));
    }
    return new LinkedList<>(ids);
  }

  /**
   * Gets the activity's ID only and return the list of this one
   * 
   * @param list Activity's Ids
   * @return
   */
  private List<String> convertActivityEntitiesToIdsString(List<Tuple> list) {
    Set<String> ids = new LinkedHashSet<>();
    if (list == null) return Collections.emptyList();
    for (Tuple t : list) {
      ids.add(String.valueOf(t.get(0)));
    }
    return new LinkedList<>(ids);
  }

  @Override
  public long getNumberOfComments(long activityId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.numberCommentsOfActivity", Long.class);
    query.setParameter(ACTIVITY_ID_PARAM, activityId);
    return query.getSingleResult();
  }

  @Override
  public int getNumberOfAllComments(long activityId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocActivity.numberCommentsAndSubCommentsOfActivity", Long.class);
    query.setParameter(ACTIVITY_ID_PARAM, activityId);
    return query.getSingleResult().intValue();
  }

  @Override
  public List<ActivityEntity> findCommentsOfActivities(List<Long> ids) {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.findCommentsOfActivities", ActivityEntity.class);
    query.setParameter("ids", ids);
    return query.getResultList();
  }
  
  @Override
  public List<ActivityEntity> findCommentsAndSubCommentsOfActivity(Long activityId) {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.findCommentsAndSubCommentsOfActivity",
                                                                           ActivityEntity.class);
    query.setParameter(ACTIVITY_ID_PARAM, activityId);
    return query.getResultList();
  }

  @Override
  public List<ActivityEntity> getComments(long activityId, int offset, int limit, boolean sortDescending) {
    String queryString = sortDescending ? "SocActivity.findLastCommentsOfActivity" : "SocActivity.findCommentsOfActivity";
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery(queryString, ActivityEntity.class);
    query.setParameter(ACTIVITY_ID_PARAM, activityId);
    if (limit > 0) {
      query.setFirstResult(offset >= 0 ? offset : 0);
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  public List<ActivityEntity> getNewerComments(long activityId, Date sinceTime, int offset, int limit) {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.findNewerCommentsOfActivity", ActivityEntity.class);
    query.setParameter(ACTIVITY_ID_PARAM, activityId);
    query.setParameter("sinceTime", sinceTime != null ? sinceTime.getTime() : 0);
    if (limit > 0) {
      query.setFirstResult(offset >= 0 ? offset : 0);
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  public List<ActivityEntity> getOlderComments(long activityId, Date sinceTime, int offset, int limit) {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.findOlderCommentsOfActivity", ActivityEntity.class);
    query.setParameter(ACTIVITY_ID_PARAM, activityId);
    query.setParameter("sinceTime", sinceTime != null ? sinceTime.getTime() : 0);
    if (limit > 0) {
      query.setFirstResult(offset >= 0 ? offset : 0);
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  public ActivityEntity getParentActivity(long commentId) {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.getParentActivity", ActivityEntity.class);
    query.setParameter("commentId", commentId);
    query.setMaxResults(1);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public List<ActivityEntity> getAllActivities() {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.getAllActivities", ActivityEntity.class);
    return query.getResultList();
  }

  @Override
  @ExoTransactional
  public void deleteActivitiesByOwnerId(String ownerId) {
    Query query = getEntityManager().createNamedQuery("SocActivity.deleteActivityByOwner");
    query.setParameter("ownerId", ownerId);
    query.executeUpdate();
  }

  public List<Long> getOwnerActivities(List<String> owners, long newerTime, long olderTime,
                                                long offset, long limit) throws ActivityStorageException {

    TypedQuery<Tuple> query;

    if (newerTime > 0) {
      query = getEntityManager().createNamedQuery("SocActivity.getNewerActivityByOwner", Tuple.class);
      query.setParameter("sinceTime", newerTime);
    } else if (olderTime > 0) {
      query = getEntityManager().createNamedQuery("SocActivity.getOlderActivityByOwner", Tuple.class);
      query.setParameter("sinceTime", olderTime);
    } else {
      query = getEntityManager().createNamedQuery("SocActivity.getActivityByOwner", Tuple.class);
    }

    List<Long> ids = new ArrayList<>();
    for (String id : owners) {
      ids.add(Long.parseLong(id));
    }

    query.setParameter("owners", ids);
    if (limit > 0) {
      query.setFirstResult(offset > 0 ? (int)offset : 0);
      query.setMaxResults((int)limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public List<ActivityEntity> findActivities(List<Long> activityIds) {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.findActivities", ActivityEntity.class);
    query.setParameter("ids", activityIds);
    return query.getResultList();
  }

  @Override
  public void deleteAll(List<ActivityEntity> entities) {
    if (entities != null) {
      entities = entities.stream().sorted((a1, a2) -> {
        // Safely transform long to int
        long c = a1.getId() - a2.getId();
        if (c > 0) {
          return -1;
        } else if (c < 0) {
          return 1;
        } else {
          return 0;
        }
      }).toList();
      super.deleteAll(entities);
    }
  }

  @Override
  public List<Long> getActivityCategoryIds(Set<Long> streamFeedOwnerIds) {
    return getEntityManager().createNamedQuery("ActivityEntity.getActivityCategoryIds", Long.class)
                             .setParameter("owners", streamFeedOwnerIds)
                             .getResultList();
  }

  @Override
  public List<Long> getActivityCategoryIds(long spaceIdentityId) {
    return getEntityManager().createNamedQuery("ActivityEntity.getActivityCategoryIdsBySpaceId", Long.class)
                             .setParameter("spaceIdentityId", spaceIdentityId)
                             .setParameter(STREAM_TYPE, StreamType.SPACE)
                             .getResultList();

  }

  private <T> TypedQuery<T> buildQueryFromFilter(ActivityFilter activityFilter,
                                                 List<String> streamIdentityIds,
                                                 Class<T> clazz,
                                                 boolean count) {
    List<String> suffixes = new ArrayList<>();
    List<String> predicates = new ArrayList<>();
    buildPredicates(activityFilter, streamIdentityIds, suffixes, predicates);

    TypedQuery<T> query;
    String queryName = getQueryFilterName(suffixes, count);
    if (filterNamedQueries.containsKey(queryName)) {
      query = getEntityManager().createNamedQuery(queryName, clazz);
    } else {
      String queryContent = getQueryFilterContent(activityFilter, predicates, count);
      query = getEntityManager().createQuery(queryContent, clazz);
      getEntityManager().getEntityManagerFactory().addNamedQuery(queryName, query);
      filterNamedQueries.put(queryName, true);
    }

    addQueryFilterParameters(activityFilter, streamIdentityIds, query, count);
    return query;
  }

  private <T> void addQueryFilterParameters(ActivityFilter activityFilter,
                                            List<String> streamIdentityIds,
                                            TypedQuery<T> query,
                                            boolean count) {
    if (!count && !activityFilter.isScheduled()) {
      query.setParameter(MAIN_STREAM_TYPES_PARAM, MAIN_STREAM_TYPES);
    }
    if (activityFilter.getSpaceIdentityId() > 0) {
      query.setParameter(ACTIVITY_OWNER_IDS, Collections.singleton(activityFilter.getSpaceIdentityId()));
      if (activityFilter.getUserId() > 0) {
        query.setParameter(ACTIVITY_POSTER_ID, activityFilter.getUserId());
      }
    } else if (activityFilter.getUserId() > 0) {
      query.setParameter(ACTIVITY_POSTER_ID, activityFilter.getUserId());
      query.setParameter(USER_PROVIDER_ID, OrganizationIdentityProvider.NAME);
      if (CollectionUtils.isNotEmpty(streamIdentityIds)) {
        query.setParameter(SPACE_PROVIDER_ID, SpaceIdentityProvider.NAME);
        query.setParameter(ACTIVITY_OWNER_IDS, streamIdentityIds);
      }
    } else if (activityFilter.getPosterId() == 0) {
      query.setParameter(ACTIVITY_OWNER_IDS, streamIdentityIds == null ? Collections.emptyList() : streamIdentityIds);
    } else if (CollectionUtils.isEmpty(streamIdentityIds)) {
      query.setParameter(ACTIVITY_POSTER_ID, activityFilter.getPosterId());
      query.setParameter(USER_PROVIDER_ID, OrganizationIdentityProvider.NAME);
    } else {
      query.setParameter(ACTIVITY_POSTER_ID, activityFilter.getPosterId());
      query.setParameter(USER_PROVIDER_ID, OrganizationIdentityProvider.NAME);
      query.setParameter(ACTIVITY_OWNER_IDS, streamIdentityIds);
    }
    if (CollectionUtils.isNotEmpty(activityFilter.getCategoryIds())) {
      if (CollectionUtils.isNotEmpty(activityFilter.getExcludedCategoryIds())) {
        activityFilter.getCategoryIds().removeAll(activityFilter.getExcludedCategoryIds());
      }
      query.setParameter(CATEGORY_IDS, activityFilter.getCategoryIds());
    }
    if (CollectionUtils.isNotEmpty(activityFilter.getExcludedCategoryIds())) {
      query.setParameter(EXCLUDED_CATEGORY_IDS, activityFilter.getExcludedCategoryIds());
    }
  }

  private void buildPredicates(ActivityFilter activityFilter,
                               List<String> streamIdentityIds,
                               List<String> suffixes,
                               List<String> predicates) {
    if (activityFilter.getSpaceIdentityId() > 0) {
      if (activityFilter.getUserId() > 0) {
        suffixes.add("PostedSpaceStream");
        predicates.add("activity.posterId = :posterId");
      } else {
        suffixes.add("SpaceStream");
      }
      predicates.add("activity.ownerId in (:ownerIds)");
    } else if (activityFilter.getUserId() > 0) {
      predicates.add("activity.posterId = :posterId");
      if (CollectionUtils.isEmpty(streamIdentityIds)) {
        suffixes.add("PostedActivitiesInUserStreams");
        predicates.add("activity.providerId = :userProviderId");
      } else {
        suffixes.add("PostedActivitiesInAllStreams");
        predicates.add("(activity.providerId = :userProviderId OR (activity.providerId = :spaceProviderId AND activity.ownerId in (:ownerIds)))");
      }
    } else if (activityFilter.getPosterId() == 0) {
      suffixes.add("SpacesAndConnectionsStream");
      predicates.add("activity.ownerId in (:ownerIds)");
    } else if (CollectionUtils.isEmpty(streamIdentityIds)) {
      suffixes.add("UserStream");
      predicates.add("activity.posterId = :posterId");
      predicates.add("activity.providerId = :userProviderId");
    } else {
      suffixes.add("AllStream");
      predicates.add("(activity.ownerId in (:ownerIds) OR (activity.posterId = :posterId and activity.providerId = :userProviderId))");
    }
    if (activityFilter.isPinned()) {
      suffixes.add("Pinned");
      predicates.add("activity.pinned = true");
    }
    if (activityFilter.isScheduled()) {
      suffixes.add("Scheduled");
      predicates.add("activity.publicationStartTime IS NOT NULL");
    }
    if (CollectionUtils.isNotEmpty(activityFilter.getCategoryIds())) {
      suffixes.add("CategoryIds");
    }
    if (CollectionUtils.isNotEmpty(activityFilter.getExcludedCategoryIds())) {
      suffixes.add("ExcludedCategoryIds");
    }
  }

  private String getQueryFilterName(List<String> suffixes, boolean count) {
    String queryName;
    if (suffixes.isEmpty()) {
      queryName = count ? QUERY_FILTER_COUNT_PREFIX : QUERY_FILTER_FIND_PREFIX;
    } else {
      queryName = (count ? QUERY_FILTER_COUNT_PREFIX : QUERY_FILTER_FIND_PREFIX) + "By" + StringUtils.join(suffixes, "By");
    }
    return queryName;
  }

  private String getQueryFilterContent(ActivityFilter activityFilter, List<String> predicates, boolean count) {
    boolean scheduled = activityFilter.isScheduled();
    String querySelect;
    if (count) {
      querySelect = "SELECT COUNT(DISTINCT activity.id)";
    } else if (scheduled) {
      querySelect = "SELECT DISTINCT(activity.id), activity.publicationStartTime";
    } else {
      querySelect = "SELECT DISTINCT(activity.id), activity.pinDate, item.updatedDate updatedDate";
    }
    querySelect = querySelect + " FROM SocActivity activity";

    String orderBy;
    if (scheduled) {
      // From the closest schedule to the farthest one
      orderBy = " ORDER BY activity.publicationStartTime ASC";
    } else if (activityFilter.isShowPinned()) {
      orderBy = " ORDER BY activity.pinDate DESC NULLS LAST, updatedDate DESC NULLS LAST";
    } else {
      orderBy = " ORDER BY updatedDate DESC NULLS LAST";
    }

    if (!count && !scheduled) {
      querySelect += " INNER JOIN activity.streamItems item ON item.streamType in (:mainStreamTypes)";
    }
    if (CollectionUtils.isNotEmpty(activityFilter.getCategoryIds())) {
      querySelect += " INNER JOIN activity.categories catInclude ON catInclude.categoryId IN :categoryIds";
    }
    if (CollectionUtils.isNotEmpty(activityFilter.getExcludedCategoryIds())) {
      querySelect += " LEFT JOIN activity.categories catExclude ON catExclude.categoryId IN :excludedCategoryIds";
    }

    // A scheduled activity is hidden until its publication: the hidden
    // exclusion doesn't apply to the scheduled stream
    querySelect += scheduled ? " WHERE activity.parent IS NULL"
                             : " WHERE activity.hidden = false AND activity.parent IS NULL";

    if (CollectionUtils.isNotEmpty(activityFilter.getExcludedCategoryIds())) {
      querySelect += " AND catExclude.categoryId IS NULL";
    }

    String queryContent;
    if (predicates.isEmpty()) {
      queryContent = querySelect;
    } else {
      queryContent = querySelect + " AND " + StringUtils.join(predicates, " AND ");
    }
    if (!count) {
      queryContent = queryContent + orderBy;
    }
    return queryContent;
  }

}
