/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.cache;

import static org.exoplatform.social.core.storage.ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY;

import java.util.*;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.services.cache.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.activity.model.ActivityShareAction;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImpl;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.*;
import org.exoplatform.social.core.storage.cache.model.key.*;
import org.exoplatform.social.core.storage.cache.selector.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedActivityStorage implements ActivityStorage {

  /** Logger */
  private static final Log                                                                                LOG =
                                                                                                              ExoLogger.getLogger(CachedActivityStorage.class);

  private final ExoCache<ActivityKey, ActivityData>                                                       exoActivityCache;

  private final ExoCache<ActivityCountKey, IntegerData>                                                   exoActivitiesCountCache;

  private final ExoCache<ListActivitiesKey, ListActivitiesData>                                           exoActivitiesCache;

  private final FutureExoCache<ActivityKey, ActivityData, ServiceContext<ActivityData>>                   activityCache;

  private final FutureExoCache<ActivityCountKey, IntegerData, ServiceContext<IntegerData>>                activitiesCountCache;

  private final FutureExoCache<ListActivitiesKey, ListActivitiesData, ServiceContext<ListActivitiesData>> activitiesCache;

  private ActivityStorage                                                                                 storage;

  public void clearCache() {

    try {
      exoActivitiesCache.select(new CacheSelector<ListActivitiesKey, ListActivitiesData>());
      exoActivitiesCountCache.select(new CacheSelector<ActivityCountKey, IntegerData>());
    } catch (Exception e) {
      LOG.error(e);
    }

  }

  void clearOwnerCache(String ownerId) {

    try {
      exoActivityCache.select(new ActivityOwnerCacheSelector(ownerId));
    } catch (Exception e) {
      LOG.error(e);
    }

    clearCache();

  }

  /**
   * Clears activities of input owner from cache.
   * 
   * @param streamOwner owner of stream to be cleared.
   */
  void clearOwnerStreamCache(String streamOwner) {
    try {
      exoActivityCache.select(new ActivityStreamOwnerCacheSelector(streamOwner));
    } catch (Exception e) {
      LOG.error(e);
    }

    clearCache();
  }

  /**
   * Clear activity cached.
   * 
   * @param activityId
   * @since 1.2.8
   */
  public void clearActivityCached(String activityId) {
    ActivityKey key = new ActivityKey(activityId);
    exoActivityCache.remove(key);
    clearCache();
  }

  /**
   * Build the activity list from the caches Ids.
   *
   * @param data ids
   * @return activities
   */
  private List<ExoSocialActivity> buildActivities(ListActivitiesData data) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (ActivityKey k : data.getIds()) {
      ExoSocialActivity a = getActivity(k.getId());
      activities.add(a);
    }
    return activities;

  }

  /**
   * Build the ids from the activity list.
   *
   * @param activities activities
   * @return ids
   */
  private ListActivitiesData buildIds(List<ExoSocialActivity> activities) {

    List<ActivityKey> data = new ArrayList<ActivityKey>();
    for (ExoSocialActivity a : activities) {
      if (a == null) {
        continue;
      }
      ActivityKey k = new ActivityKey(a.getId());
      if (exoActivityCache.get(k) == null) {
        exoActivityCache.putLocal(k, new ActivityData(a));
      }
      data.add(k);
    }
    return new ListActivitiesData(data);

  }

  /**
   * Build the ids from the activity list.
   *
   * @param activities activities
   * @return ids
   */
  private ListActivitiesData buildActivityIds(List<String> ids) {
    List<ActivityKey> data = new ArrayList<ActivityKey>();
    for (String id : ids) {
      ActivityKey k = new ActivityKey(id);
      data.add(k);
    }
    return new ListActivitiesData(data);

  }

  public CachedActivityStorage(final RDBMSActivityStorageImpl storage, final SocialStorageCacheService cacheService) {

    //
    this.storage = storage;

    //
    this.exoActivityCache = cacheService.getActivityCache();
    this.exoActivitiesCountCache = cacheService.getActivitiesCountCache();
    this.exoActivitiesCache = cacheService.getActivitiesCache();

    this.exoActivityCache.addCacheListener(new CacheActivityListener());

    //
    this.activityCache = CacheType.ACTIVITY.createFutureCache(exoActivityCache);
    this.activitiesCountCache = CacheType.ACTIVITIES_COUNT.createFutureCache(exoActivitiesCountCache);
    this.activitiesCache = CacheType.ACTIVITIES.createFutureCache(exoActivitiesCache);

  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getActivity(final String activityId) throws ActivityStorageException {

    if (activityId == null || activityId.length() == 0) {
      return ActivityData.NULL.build();
    }
    //
    ActivityKey key = new ActivityKey(activityId);

    //
    ActivityData activity = activityCache.get(
                                              new ServiceContext<ActivityData>() {
                                                public ActivityData execute() {
                                                  try {
                                                    ExoSocialActivity got = storage.getActivity(activityId);
                                                    if (got != null) {
                                                      return new ActivityData(got);
                                                    } else {
                                                      return ActivityData.NULL;
                                                    }
                                                  } catch (Exception e) {
                                                    throw new ActivityStorageException(FAILED_TO_GET_ACTIVITY,
                                                                                       "failed to get activity with id: "
                                                                                           + activityId,
                                                                                       e);
                                                  }
                                                }
                                              },
                                              key);

    //
    return activity.build();

  }

  @Override
  public boolean isActivityExists(String activityId) {
    ActivityKey key = new ActivityKey(activityId);
    ActivityData cachedActivityData = exoActivityCache.get(key);
    boolean cachedActivityExists = cachedActivityData != null && cachedActivityData.getId() != null;
    if (cachedActivityExists) {
      return true;
    } else {
      return storage.isActivityExists(activityId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getUserActivities(final Identity owner) throws ActivityStorageException {
    return storage.getUserActivities(owner);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getUserActivities(final Identity owner, final long offset, final long limit)
                                                                                                              throws ActivityStorageException {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityType.USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getUserActivities(owner, offset, limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  public List<String> getUserIdsActivities(final Identity owner,
                                           final long offset,
                                           final long limit) throws ActivityStorageException {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityType.USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);
    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getUserIdsActivities(owner, offset, limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivityIds(keys);
  }

  /**
   * {@inheritDoc}
   */
  public void saveComment(final ExoSocialActivity activity, final ExoSocialActivity comment) throws ActivityStorageException {

    //
    storage.saveComment(activity, comment);

    //
    exoActivityCache.remove(new ActivityKey(comment.getId()));
    exoActivityCache.put(new ActivityKey(comment.getId()), new ActivityData(getActivity(comment.getId())));
    ActivityKey activityKey = new ActivityKey(activity.getId());
    exoActivityCache.remove(activityKey);
    clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity saveActivity(final Identity owner, final ExoSocialActivity activity) throws ActivityStorageException {

    //
    ExoSocialActivity a = storage.saveActivity(owner, activity);

    //
    ActivityKey key = new ActivityKey(a.getId());
    exoActivityCache.remove(key);
    clearCache();

    //
    return getActivity(a.getId());

  }

  @Override
  public ExoSocialActivity hideActivity(String activityId) {
    ExoSocialActivity a = storage.hideActivity(activityId);
    ActivityKey key = new ActivityKey(a.getId());
    exoActivityCache.remove(key);
    return a;
  }

  @Override
  public ExoSocialActivity pinActivity(String activityId, Long userIdentityId) {
    ExoSocialActivity a = storage.pinActivity(activityId, userIdentityId);
    ActivityKey key = new ActivityKey(a.getId());
    exoActivityCache.remove(key);
    return a;
  }

  @Override
  public ExoSocialActivity unpinActivity(String activityId) {
    ExoSocialActivity a = storage.unpinActivity(activityId);
    ActivityKey key = new ActivityKey(a.getId());
    exoActivityCache.remove(key);
    return a;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getParentActivity(final ExoSocialActivity comment) throws ActivityStorageException {
    return getActivity(comment.getParentId());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(final String activityId) throws ActivityStorageException {

    //
    ExoSocialActivity a = storage.getActivity(activityId);
    storage.deleteActivity(activityId);

    //
    ActivityKey key = new ActivityKey(activityId);
    exoActivityCache.remove(key);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(final String activityId, final String commentId) throws ActivityStorageException {

    //
    storage.deleteComment(activityId, commentId);

    //
    exoActivityCache.remove(new ActivityKey(activityId));
    exoActivityCache.remove(new ActivityKey(commentId));
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfUserActivities(final Identity owner) throws ActivityStorageException {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityType.USER);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfUserActivities(owner));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getActivitiesCountByFilter(Identity viewerIdentity, ActivityFilter activityFilter) {

    ActivityCountKey key = new ActivityCountKey(new IdentityKey(viewerIdentity), activityFilter);

    return activitiesCountCache.get(() -> new IntegerData(storage.getActivitiesCountByFilter(viewerIdentity, activityFilter)),
                                    key)
                               .build();

  }
  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_USER);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnUserActivities(ownerIdentity,
                                                                                                        baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnUserActivities(final Identity ownerIdentity,
                                                          final ExoSocialActivity baseActivity,
                                                          final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.NEWER_USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnUserActivities(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_USER);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnUserActivities(ownerIdentity,
                                                                                                        baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnUserActivities(final Identity ownerIdentity,
                                                          final ExoSocialActivity baseActivity,
                                                          final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnUserActivities(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivityFeed(final Identity ownerIdentity, final int offset, final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivityFeed(ownerIdentity,
                                                                                                            offset,
                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public List<String> getActivityIdsFeed(final Identity ownerIdentity, final int offset, final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);
    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getActivityIdsFeed(ownerIdentity, offset, limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivityIds(keys);
  }

  private List<String> buildActivityIds(ListActivitiesData data) {
    List<String> ids = new LinkedList<String>();
    for (ActivityKey k : data.getIds()) {
      ids.add(k.getId());
    }
    return ids;

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfActivitesOnActivityFeed(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitesOnActivityFeed(ownerIdentity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_FEED);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnActivityFeed(ownerIdentity,
                                                                                                      baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnActivityFeed(final Identity ownerIdentity,
                                                        final ExoSocialActivity baseActivity,
                                                        final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getNewerOnActivityFeed(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_FEED);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnActivityFeed(ownerIdentity,
                                                                                                      baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnActivityFeed(final Identity ownerIdentity,
                                                        final ExoSocialActivity baseActivity,
                                                        final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getOlderOnActivityFeed(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(final Identity ownerIdentity, final int offset, final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getActivitiesOfConnections(ownerIdentity,
                                                                                                                     offset,
                                                                                                                     limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  @Override
  public List<String> getActivityIdsOfConnections(final Identity ownerIdentity, final int offset, final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);
    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getActivityIdsOfConnections(ownerIdentity,
                                                                                                             offset,
                                                                                                             limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivityIds(keys);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfActivitiesOfConnections(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitiesOfConnections(ownerIdentity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfIdentity(final Identity ownerIdentity, final long offset, final long limit)
                                                                                                                            throws ActivityStorageException {
    return storage.getActivitiesOfIdentity(ownerIdentity, offset, limit);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity),
                                              baseActivity.getId(),
                                              ActivityType.NEWER_CONNECTION);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity,
                                                                                                                 baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(final Identity ownerIdentity,
                                                                   final ExoSocialActivity baseActivity,
                                                                   final long limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                baseActivity.getId(),
                                                ActivityType.NEWER_CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnActivitiesOfConnections(ownerIdentity,
                                                                                                                            baseActivity,
                                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity),
                                              baseActivity.getId(),
                                              ActivityType.OLDER_CONNECTION);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity,
                                                                                                                 baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(final Identity ownerIdentity,
                                                                   final ExoSocialActivity baseActivity,
                                                                   final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                baseActivity.getId(),
                                                ActivityType.OLDER_CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnActivitiesOfConnections(ownerIdentity,
                                                                                                                            baseActivity,
                                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getUserSpacesActivities(final Identity ownerIdentity, final int offset, final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACES);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getUserSpacesActivities(ownerIdentity,
                                                                                                                    offset,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  @Override
  public List<String> getUserSpacesActivityIds(final Identity ownerIdentity, final int offset, final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACES);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);
    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getUserSpacesActivityIds(ownerIdentity,
                                                                                                          offset,
                                                                                                          limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivityIds(keys);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfUserSpacesActivities(final Identity ownerIdentity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACES);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfUserSpacesActivities(ownerIdentity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_SPACES);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity,
                                                                                                              baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(final Identity ownerIdentity,
                                                                final ExoSocialActivity baseActivity,
                                                                final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_SPACES);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnUserSpacesActivities(ownerIdentity,
                                                                                                                         baseActivity,
                                                                                                                         limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_SPACES);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnUserSpacesActivities(ownerIdentity,
                                                                                                              baseActivity));
                                      }
                                    },
                                    key)
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(final Identity ownerIdentity,
                                                                final ExoSocialActivity baseActivity,
                                                                final int limit) {

    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_SPACES);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnUserSpacesActivities(ownerIdentity,
                                                                                                                         baseActivity,
                                                                                                                         limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getComments(final ExoSocialActivity existingActivity,
                                             final boolean loadSubComments,
                                             final int offset,
                                             final int limit) {
    return getComments(existingActivity, loadSubComments, offset, limit, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getComments(final ExoSocialActivity existingActivity,
                                             final boolean loadSubComments,
                                             final int offset,
                                             final int limit,
                                             boolean sortDescending) {
    ActivityCountKey key = new ActivityCountKey(existingActivity.getId(),
                                                loadSubComments ? ActivityType.COMMENTS_AND_SUB_COMMENTS : ActivityType.COMMENTS);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit, sortDescending);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getComments(existingActivity,
                                                                                                        loadSubComments,
                                                                                                        offset,
                                                                                                        limit,
                                                                                                        sortDescending);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfComments(final ExoSocialActivity existingActivity) {

    //
    ActivityCountKey key =
                         new ActivityCountKey(existingActivity.getId(), ActivityType.COMMENTS);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfComments(existingActivity));
                                      }
                                    },
                                    key)
                               .build();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment) {
    return storage.getNumberOfNewerComments(existingActivity, baseComment);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerComments(final ExoSocialActivity existingActivity,
                                                  final ExoSocialActivity baseComment,
                                                  final int limit) {
    return storage.getNewerComments(existingActivity, baseComment, limit);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment) {
    return storage.getNumberOfOlderComments(existingActivity, baseComment);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderComments(final ExoSocialActivity existingActivity,
                                                  final ExoSocialActivity baseComment,
                                                  final int limit) {
    return storage.getOlderComments(existingActivity, baseComment, limit);
  }

  /**
   * {@inheritDoc}
   */
  public SortedSet<ActivityProcessor> getActivityProcessors() {
    return storage.getActivityProcessors();
  }

  /**
   * {@inheritDoc}
   */
  public void updateActivity(final ExoSocialActivity existingActivity) throws ActivityStorageException {

    //
    storage.updateActivity(existingActivity);

    //
    exoActivityCache.remove(new ActivityKey(existingActivity.getId()));
    if (existingActivity.getParentId() != null) {
      exoActivityCache.remove(new ActivityKey(existingActivity.getParentId()));
    }
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivityFeed(final Identity ownerIdentity, final Long sinceTime) {

    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.NEWER_FEED);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnActivityFeed(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserActivities(final Identity ownerIdentity, final Long sinceTime) {

    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.NEWER_USER);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnUserActivities(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivitiesOfConnections(final Identity ownerIdentity, final Long sinceTime) {

    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), sinceTime, ActivityType.NEWER_CONNECTION);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserSpacesActivities(final Identity ownerIdentity, final Long sinceTime) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.NEWER_SPACE);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity,
                                                                              sinceTime));
      }
    }, key).build();
  }

  @Override
  public int getNumberOfSpaceActivities(final Identity spaceIdentity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(spaceIdentity), ActivityType.SPACE);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfSpaceActivities(spaceIdentity));
                                      }
                                    },
                                    key)
                               .build();
  }

  @Override
  public int getNumberOfSpaceActivitiesForUpgrade(final Identity spaceIdentity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(spaceIdentity), ActivityType.SPACE_FOR_UPGRADE);

    //
    IntegerData countData = activitiesCountCache.get(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfSpaceActivitiesForUpgrade(spaceIdentity));
                                                       }
                                                     },
                                                     key);

    ActivityCountKey keySpace =
                              new ActivityCountKey(new IdentityKey(spaceIdentity), ActivityType.SPACE);
    exoActivitiesCountCache.putLocal(keySpace, countData);

    return countData.build();
  }

  @Override
  public List<ExoSocialActivity> getSpaceActivities(final Identity ownerIdentity, final int offset, final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getSpaceActivities(ownerIdentity,
                                                                                                               offset,
                                                                                                               limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public List<String> getSpaceActivityIds(final Identity spaceIdentity, final int offset, final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(spaceIdentity), ActivityType.SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);
    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got =
                                                                       storage.getSpaceActivityIds(spaceIdentity, offset, limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivityIds(keys);
  }

  @Override
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(final Identity ownerIdentity, final int offset, final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getSpaceActivitiesForUpgrade(ownerIdentity,
                                                                                                                       offset,
                                                                                                                       limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(final Identity posterIdentity,
                                                       final int offset,
                                                       final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(posterIdentity), ActivityType.POSTER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivitiesByPoster(posterIdentity,
                                                                                                                  offset,
                                                                                                                  limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(final Identity posterIdentity,
                                                       final int offset,
                                                       final int limit,
                                                       final String... activityTypes) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(posterIdentity), ActivityType.POSTER, activityTypes);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivitiesByPoster(posterIdentity,
                                                                                                                  offset,
                                                                                                                  limit,
                                                                                                                  activityTypes);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfActivitiesByPoster(final Identity posterIdentity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(posterIdentity), ActivityType.POSTER);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitiesByPoster(posterIdentity));
                                      }
                                    },
                                    key)
                               .build();
  }

  @Override
  public int getNumberOfActivitiesByPoster(final Identity ownerIdentity, final Identity viewerIdentity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity),
                                              new IdentityKey(viewerIdentity),
                                              ActivityType.POSTER);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitiesByPoster(ownerIdentity,
                                                                                                     viewerIdentity));
                                      }
                                    },
                                    key)
                               .build();
  }

  @Override
  public List<ExoSocialActivity> getNewerOnSpaceActivities(final Identity ownerIdentity,
                                                           final ExoSocialActivity baseActivity,
                                                           final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnSpaceActivities(ownerIdentity,
                                                                                                                    baseActivity,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(final Identity ownerIdentity,
                                               final ExoSocialActivity baseActivity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.NEWER_SPACE);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnSpaceActivities(ownerIdentity,
                                                                                                         baseActivity));
                                      }
                                    },
                                    key)
                               .build();
  }

  @Override
  public List<ExoSocialActivity> getOlderOnSpaceActivities(final Identity ownerIdentity,
                                                           final ExoSocialActivity baseActivity,
                                                           final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnSpaceActivities(ownerIdentity,
                                                                                                                    baseActivity,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(final Identity ownerIdentity,
                                               final ExoSocialActivity baseActivity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), baseActivity.getId(), ActivityType.OLDER_SPACE);

    //
    return activitiesCountCache.get(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnSpaceActivities(ownerIdentity,
                                                                                                         baseActivity));
                                      }
                                    },
                                    key)
                               .build();
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(final Identity ownerIdentity, final Long sinceTime) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.NEWER_SPACE);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity,
                                                                              sinceTime));
      }
    }, key).build();
  }

  public List<ExoSocialActivity> getNewerFeedActivities(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.NEWER_FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerFeedActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  public List<ExoSocialActivity> getNewerSpaceActivities(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.NEWER_SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerSpaceActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getNewerUserActivities(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.NEWER_USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerUserActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getNewerUserSpacesActivities(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.NEWER_SPACES);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerUserSpacesActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.NEWER_CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerActivitiesOfConnections(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivities(final Identity owner,
                                               final Identity viewer,
                                               final long offset,
                                               final long limit) throws ActivityStorageException {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), new IdentityKey(viewer), ActivityType.VIEWER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivities(owner,
                                                                                                          viewer,
                                                                                                          offset,
                                                                                                          limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);

  }

  @Override
  public List<ExoSocialActivity> getOlderFeedActivities(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.OLDER_FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderFeedActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderUserActivities(final Identity ownerIdentity, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), sinceTime, ActivityType.OLDER_USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderUserActivities(ownerIdentity, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderUserSpacesActivities(final Identity ownerIdentity,
                                                              final Long sinceTime,
                                                              final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), sinceTime, ActivityType.OLDER_SPACES);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderUserSpacesActivities(ownerIdentity, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.OLDER_CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderActivitiesOfConnections(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderSpaceActivities(final Identity owner, final Long sinceTime, final int limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), sinceTime, ActivityType.OLDER_SPACE);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderSpaceActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public int getNumberOfOlderOnActivityFeed(final Identity ownerIdentity, final Long sinceTime) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.OLDER_FEED);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnActivityFeed(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  @Override
  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final Long sinceTime) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.OLDER_USER);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnUserActivities(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(final Identity ownerIdentity, final Long sinceTime) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.OLDER_CONNECTION);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(final Identity ownerIdentity, final Long sinceTime) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.OLDER_SPACES);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(final Identity ownerIdentity, final Long sinceTime) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity),
                                                sinceTime,
                                                ActivityType.OLDER_SPACE);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnSpaceActivities(ownerIdentity, sinceTime));
      }
    }, key).build();
  }

  @Override
  public List<ExoSocialActivity> getNewerComments(final ExoSocialActivity existingActivity,
                                                  final Long sinceTime,
                                                  final int limit) {
    ActivityCountKey key =
                         new ActivityCountKey(new ActivityKey(existingActivity.getId()), sinceTime, ActivityType.NEWER_COMMENTS);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerComments(existingActivity, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderComments(final ExoSocialActivity existingActivity,
                                                  final Long sinceTime,
                                                  final int limit) {
    ActivityCountKey key =
                         new ActivityCountKey(new ActivityKey(existingActivity.getId()), sinceTime, ActivityType.OLDER_COMMENTS);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, limit);

    ListActivitiesData keys = activitiesCache.get(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderComments(existingActivity, sinceTime, limit);
        return buildIds(got);
      }
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public int getNumberOfNewerComments(final ExoSocialActivity existingActivity, final Long sinceTime) {
    ActivityCountKey key =
                         new ActivityCountKey(new ActivityKey(existingActivity.getId()), sinceTime, ActivityType.NEWER_COMMENTS);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerComments(existingActivity, sinceTime));
      }
    }, key).build();
  }

  @Override
  public int getNumberOfOlderComments(final ExoSocialActivity existingActivity, final Long sinceTime) {
    ActivityCountKey key =
                         new ActivityCountKey(new ActivityKey(existingActivity.getId()), sinceTime, ActivityType.OLDER_COMMENTS);

    return activitiesCountCache.get(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderComments(existingActivity, sinceTime));
      }
    }, key).build();
  }

  @Override
  public List<ExoSocialActivity> getUserActivitiesForUpgrade(final Identity owner,
                                                             final long offset,
                                                             final long limit) throws ActivityStorageException {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityType.USER);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getUserActivitiesForUpgrade(owner,
                                                                                                                        offset,
                                                                                                                        limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByFilter(Identity viewerIdentity,
                                                       ActivityFilter activityFilter,
                                                       long offset,
                                                       long limit) {
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(viewerIdentity), activityFilter);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    ListActivitiesData keys = activitiesCache.get(() -> {
      List<ExoSocialActivity> got = storage.getActivitiesByFilter(viewerIdentity, activityFilter, offset, limit);
      return buildIds(got);
    }, listKey);

    return buildActivities(keys);
  }

  @Override
  public List<String> getActivityIdsByFilter(Identity viewerIdentity, ActivityFilter activityFilter, long offset, long limit) {
    return storage.getActivityIdsByFilter(viewerIdentity, activityFilter, offset, limit);
  }

  @Override
  public int getNumberOfUserActivitiesForUpgrade(final Identity owner) throws ActivityStorageException {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(owner), ActivityType.USER_FOR_UPGRADE);

    //
    IntegerData countData = activitiesCountCache.get(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfUserActivitiesForUpgrade(owner));
                                                       }
                                                     },
                                                     key);

    //
    ActivityCountKey keyUser =
                             new ActivityCountKey(new IdentityKey(owner), ActivityType.USER);
    exoActivitiesCountCache.putLocal(keyUser, countData);

    //
    return countData.build();

  }

  @Override
  public List<ExoSocialActivity> getActivityFeedForUpgrade(final Identity ownerIdentity,
                                                           final int offset,
                                                           final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getActivityFeedForUpgrade(ownerIdentity,
                                                                                                                    offset,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfActivitesOnActivityFeedForUpgrade(final Identity ownerIdentity) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED_FOR_UPGRADE);

    //
    IntegerData countData = activitiesCountCache.get(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfActivitesOnActivityFeedForUpgrade(ownerIdentity));
                                                       }
                                                     },
                                                     key);

    //
    ActivityCountKey keyFeed =
                             new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.FEED);
    exoActivitiesCountCache.putLocal(keyFeed, countData);

    //
    return countData.build();
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnectionsForUpgrade(final Identity ownerIdentity,
                                                                      final int offset,
                                                                      final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getActivitiesOfConnectionsForUpgrade(ownerIdentity,
                                                                                                                               offset,
                                                                                                                               limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfActivitiesOfConnectionsForUpgrade(final Identity ownerIdentity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION_FOR_UPGRADE);

    //
    IntegerData countData = activitiesCountCache.get(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfActivitiesOfConnectionsForUpgrade(ownerIdentity));
                                                       }
                                                     },
                                                     key);

    //
    ActivityCountKey keyConnection =
                                   new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.CONNECTION);
    exoActivitiesCountCache.putLocal(keyConnection, countData);

    //
    return countData.build();
  }

  @Override
  public List<ExoSocialActivity> getUserSpacesActivitiesForUpgrade(final Identity ownerIdentity,
                                                                   final int offset,
                                                                   final int limit) {
    //
    ActivityCountKey key = new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACES);
    ListActivitiesKey listKey = new ListActivitiesKey(key, offset, limit);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getUserSpacesActivitiesForUpgrade(ownerIdentity,
                                                                                                                            offset,
                                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfUserSpacesActivitiesForUpgrade(final Identity ownerIdentity) {
    //
    ActivityCountKey key =
                         new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACES_FOR_UPGRADE);

    //
    IntegerData countData = activitiesCountCache.get(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfUserSpacesActivitiesForUpgrade(ownerIdentity));
                                                       }
                                                     },
                                                     key);

    ActivityCountKey keySpaces =
                               new ActivityCountKey(new IdentityKey(ownerIdentity), ActivityType.SPACES);
    exoActivitiesCountCache.putLocal(keySpaces, countData);

    return countData.build();
  }

  @Override
  public List<ExoSocialActivity> getAllActivities(int index, int limit) {
    return storage.getAllActivities(index, limit);
  }

  /**
   * Cache activity listener to clean related local cache onPut an remove
   * operation
   */
  private class CacheActivityListener implements CacheListener<ActivityKey, ActivityData> {

    @Override
    public void onExpire(CacheListenerContext context, ActivityKey key, ActivityData obj) throws Exception {

    }

    @Override
    public void onRemove(CacheListenerContext context, ActivityKey key, ActivityData obj) throws Exception {
      clearCache();
    }

    @Override
    public void onPut(CacheListenerContext context, ActivityKey key, ActivityData obj) throws Exception {
      clearCache();
    }

    @Override
    public void onPutLocal(CacheListenerContext context, ActivityKey key, ActivityData obj) throws Exception {
      // nothing
    }

    @Override
    public void onGet(CacheListenerContext context, ActivityKey key, ActivityData obj) throws Exception {

    }

    @Override
    public void onClearCache(CacheListenerContext context) throws Exception {

    }
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {
    ActivityCountKey key = new ActivityCountKey(comment.getId(), ActivityType.SUB_COMMENTS);
    ListActivitiesKey listKey = new ListActivitiesKey(key, 0, Integer.MAX_VALUE);

    //
    ListActivitiesData keys = activitiesCache.get(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getSubComments(comment);
                                                      return buildIds(got);
                                                    }
                                                  },
                                                  listKey);

    //
    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {
    if (activityIdList == null || activityIdList.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> activityIdsToGetFromStore = new ArrayList<>();
    Map<String, ExoSocialActivity> resultMap = new HashMap<>();
    for (String activityId : activityIdList) {
      ActivityData activityData = exoActivityCache.get(new ActivityKey(activityId));
      if (activityData == null) {
        // Retrieve activity from store
        activityIdsToGetFromStore.add(activityId);
      } else if (activityData != ActivityData.NULL && activityData.getId() != null) {
        // Add found activity in cache into results
        resultMap.put(activityId, activityData.build());
      } else {
        // activity is equals to ActivityData.NULL,
        // thus no need to add it in results, not get it from store
      }
    }
    List<ExoSocialActivity> activitiesGotFromStore = storage.getActivities(activityIdsToGetFromStore);
    if (activitiesGotFromStore != null && !activitiesGotFromStore.isEmpty()) {
      for (ExoSocialActivity exoSocialActivity : activitiesGotFromStore) {
        // Update local cache by found value
        activityCache.putOnly(new ActivityKey(exoSocialActivity.getId()), new ActivityData(exoSocialActivity));
        // Add found activity to list of results
        resultMap.put(exoSocialActivity.getId(), exoSocialActivity);
      }
    }
    List<ExoSocialActivity> result = new ArrayList<>();
    // Compute result list switch requested order from original IDs List
    for (String activityId : activityIdList) {
      ExoSocialActivity exoSocialActivity = resultMap.get(activityId);
      if (exoSocialActivity == null) {
        continue;
      }
      result.add(exoSocialActivity);
    }
    return result;
  }

  @Override
  public Set<Long> getStreamFeedOwnerIds(Identity identity) {
    return storage.getStreamFeedOwnerIds(identity);
  }

  @Override
  public ActivityShareAction createShareActivityAction(ActivityShareAction activityShareAction) {
    activityShareAction = storage.createShareActivityAction(activityShareAction);

    ActivityKey key = new ActivityKey(String.valueOf(activityShareAction.getActivityId()));
    exoActivityCache.remove(key);

    return activityShareAction;
  }

  public void clearActivityCachedByAttachmentId(String attachmentId) {
    try {
      exoActivityCache.select(new ActivityAttachmentCacheSelector(attachmentId));
    } catch (Exception e) {
      LOG.error("Error clearing cache of activities having attachment with id {}", attachmentId, e);
    }
  }

}
