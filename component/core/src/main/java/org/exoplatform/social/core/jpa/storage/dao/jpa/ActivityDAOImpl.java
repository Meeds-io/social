/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamType;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 18, 2015  
 */
public class ActivityDAOImpl extends GenericDAOJPAImpl<ActivityEntity, Long> implements ActivityDAO {
  
  private static final String        STREAM_TYPE               = "streamType";

  private static final String        QUERY_FILTER_FIND_PREFIX  = "SocActivity.findAllActivities";

  private static final String        QUERY_FILTER_COUNT_PREFIX = "SocActivity.countAllActivities";

  private final Map<String, Boolean> filterNamedQueries        = new HashMap<>();

  private final ConnectionDAO        connectionDAO;

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
      query.setParameter("providerId", OrganizationIdentityProvider.NAME);
    } else {
      // if viewer the owner
      // get all his activities including spaces activities
      query = getEntityManager().createNamedQuery("SocActivity.getActivityByOwner", Tuple.class);
    }
    query.setParameter("owners", Collections.singleton(ownerId));
    if (limit > 0) {
      query.setFirstResult(offset > 0 ? (int)offset : 0);
      query.setMaxResults((int)limit);
    }

    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIds(resultList);
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
  public List<Long> getActivityByFilter(ActivityFilter activityFilter, List<String> spaceIdentityIds, int offset, int limit) {

    TypedQuery<Tuple> query = buildQueryFromFilter(activityFilter, spaceIdentityIds, Tuple.class, false);
    if (limit > 0) {
      query.setFirstResult(Math.max(offset, 0));
      query.setMaxResults(limit);
    }
    if (limit > 0) {
      query.setFirstResult(Math.max(offset, 0));
      query.setMaxResults(limit);
    }
    List<Tuple> resultList = query.getResultList();

    return convertActivityEntitiesToIds(resultList);
  }

  @Override
  public List<String> getActivityIdsByFilter(ActivityFilter activityFilter,
                                             List<String> spaceIdentityIds,
                                             int offset,
                                             int limit) {

    TypedQuery<Tuple> query = buildQueryFromFilter(activityFilter, spaceIdentityIds, Tuple.class, false);
    if (limit > 0) {
      query.setFirstResult(Math.max(offset, 0));
      query.setMaxResults(limit);
    }
    List<Tuple> resultList = query.getResultList();
    return convertActivityEntitiesToIdsString(resultList);
  }

  @Override
  public int getActivitiesCountByFilter(ActivityFilter activityFilter, List<String> spaceIdentityIds) {
    TypedQuery<Long> query = buildQueryFromFilter(activityFilter, spaceIdentityIds, Long.class, true);
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
    query.setParameter("activityId", activityId);
    return query.getSingleResult();
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
    query.setParameter("activityId", activityId);
    return query.getResultList();
  }

  @Override
  public List<ActivityEntity> getComments(long activityId, int offset, int limit, boolean sortDescending) {
    String queryString = sortDescending ? "SocActivity.findLastCommentsOfActivity" : "SocActivity.findCommentsOfActivity";
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery(queryString, ActivityEntity.class);
    query.setParameter("activityId", activityId);
    if (limit > 0) {
      query.setFirstResult(offset >= 0 ? offset : 0);
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  public List<ActivityEntity> getNewerComments(long activityId, Date sinceTime, int offset, int limit) {
    TypedQuery<ActivityEntity> query = getEntityManager().createNamedQuery("SocActivity.findNewerCommentsOfActivity", ActivityEntity.class);
    query.setParameter("activityId", activityId);
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
    query.setParameter("activityId", activityId);
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

  private <T> TypedQuery<T> buildQueryFromFilter(ActivityFilter activityFilter, List<String> spaceIdentityIds, Class<T> clazz, boolean count) {
    List<String> suffixes = new ArrayList<>();
    List<String> predicates = new ArrayList<>();
    buildPredicates(activityFilter, spaceIdentityIds, suffixes, predicates);

    TypedQuery<T> query;
    String queryName = getQueryFilterName(suffixes, count);
    if (filterNamedQueries.containsKey(queryName)) {
      query = getEntityManager().createNamedQuery(queryName, clazz);
    } else {
      String queryContent = getQueryFilterContent(predicates, activityFilter.isShowPinned(), count);
      query = getEntityManager().createQuery(queryContent, clazz);
      getEntityManager().getEntityManagerFactory().addNamedQuery(queryName, query);
      filterNamedQueries.put(queryName, true);
    }

    addQueryFilterParameters(activityFilter, spaceIdentityIds, query);
    return query;
  }

  private <T> void addQueryFilterParameters(ActivityFilter activityFilter, List<String> spaceIdentityIds, TypedQuery<T> query) {
    if (activityFilter.getUserId() != null) {
      query.setParameter("posterId", activityFilter.getUserId());
    }
    if (activityFilter.getSpaceId() != null) {
      long ownerId = Long.parseLong(activityFilter.getSpaceId());
      query.setParameter(STREAM_TYPE, StreamType.SPACE);
      query.setParameter("ownerIds", Collections.singleton(ownerId));
    }
    if (CollectionUtils.isNotEmpty(spaceIdentityIds)) {
      List<Long> owners = new ArrayList<>();
      for (String id : spaceIdentityIds) {
        owners.add(Long.parseLong(id));
      }
      query.setParameter(STREAM_TYPE, StreamType.SPACE);
      query.setParameter("ownerIds", owners);
    }
  }

  private void buildPredicates(ActivityFilter activityFilter,
                               List<String> spaceIdentityIds,
                               List<String> suffixes,
                               List<String> predicates) {
    if (activityFilter.getUserId() != null) {
      suffixes.add("Poster");
      predicates.add("item.activity.posterId = :posterId");
    }
    if (CollectionUtils.isNotEmpty(spaceIdentityIds) || activityFilter.getSpaceId() != null) {
      suffixes.add("StreamType");
      predicates.add("item.streamType = :streamType");
      predicates.add("item.ownerId in (:ownerIds)");
    }
    if (activityFilter.isPinned()) {
      suffixes.add("Pinned");
      predicates.add("item.activity.pinned = true");
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

  private String getQueryFilterContent(List<String> predicates, boolean showPinned, boolean count) {
    String querySelect = count ? "SELECT COUNT(item.activity.id)" : "SELECT DISTINCT(item.activity.id), item.activity.pinDate, item.updatedDate";
    querySelect = querySelect + " FROM SocStreamItem item WHERE item.activity.hidden = false";

    String orderBy = showPinned ? " ORDER BY item.activity.pinDate DESC NULLS LAST, item.updatedDate DESC" : " ORDER BY item.updatedDate DESC";

    String queryContent;
    if (predicates.isEmpty()) {
      queryContent = querySelect ;
    } else {
      queryContent = querySelect + " AND " + StringUtils.join(predicates, " AND ");
    }
    if (!count) {
      queryContent = queryContent + orderBy;
    }
    return queryContent;
  }
}
