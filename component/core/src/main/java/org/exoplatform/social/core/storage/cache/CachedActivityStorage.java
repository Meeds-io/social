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
package org.exoplatform.social.core.storage.cache;

import static org.exoplatform.social.core.storage.ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.ExoCache;
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
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ListActivitiesData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityListKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.ListActivitiesKey;
import org.exoplatform.social.core.storage.cache.selector.ActivityAttachmentCacheSelector;
import org.exoplatform.social.core.storage.cache.selector.ActivityMetadataCacheSelector;
import org.exoplatform.social.core.storage.cache.selector.ActivityOwnerCacheSelector;
import org.exoplatform.social.core.storage.cache.selector.ActivityStreamOwnerCacheSelector;

public class CachedActivityStorage implements ActivityStorage {

  /** Logger */
  private static final Log                                                                                LOG =
                                                                                                              ExoLogger.getLogger(CachedActivityStorage.class);

  private final ExoCache<ActivityKey, ActivityData>                                                       exoActivityCache;

  private final ExoCache<ActivityListKey, IntegerData>                                                   exoActivitiesCountCache;

  private final ExoCache<ListActivitiesKey, ListActivitiesData>                                           exoActivitiesCache;

  private final FutureExoCache<ActivityKey, ActivityData, ServiceContext<ActivityData>>                   activityCache;

  private ActivityStorage                                                                                 storage;

  /**
   * Loads an activity list or count from the storage without caching it.
   * {@link ActivityListKey#equals(Object)} joins "this field differs" tests
   * with {@code &&}, so two distinct keys of the same type are never equal;
   * each lookup builds a new key of its call site's type, so an entry keyed by
   * it is never read back and the activity list and count caches would only
   * fill up. These lists and counts are therefore read from
   * the storage on every call; {@link #clearCache()} still clears both caches,
   * which other storages share.
   */
  private static <T> T load(ServiceContext<T> context) {
    return context.execute();
  }

  public void clearCache() {

    try {
      exoActivitiesCache.clearCache();
      exoActivitiesCountCache.clearCache();
    } catch (Exception e) {
      LOG.error(e);
    }

  }

  public void clearOwnerCache(String ownerId) {

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
  public void clearOwnerStreamCache(String streamOwner) {
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

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getUserActivities(owner, offset, limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  public List<String> getUserIdsActivities(final Identity owner,
                                           final long offset,
                                           final long limit) throws ActivityStorageException {

    //
    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getUserIdsActivities(owner, offset, limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  });

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
  public ExoSocialActivity publishScheduledActivity(String activityId) {
    ExoSocialActivity a = storage.publishScheduledActivity(activityId);
    if (a != null) {
      ActivityKey key = new ActivityKey(a.getId());
      exoActivityCache.remove(key);
      clearCache();
    }
    return a;
  }

  @Override
  public List<String> getScheduledActivityIds(long dueTime, int offset, int limit) {
    return storage.getScheduledActivityIds(dueTime, offset, limit);
  }

  @Override
  public ExoSocialActivity pinActivity(String activityId, String userIdentityId) {
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
    ExoSocialActivity activity = getActivity(activityId);
    if (activity != null && StringUtils.isNotBlank(activity.getParentId())) {
      deleteComment(activity.getParentId(), activityId);
    } else {
      storage.deleteActivity(activityId);
      ActivityKey key = new ActivityKey(activityId);
      exoActivityCache.remove(key);
    }
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

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfUserActivities(owner));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getActivitiesCountByFilter(Identity viewerIdentity, ActivityFilter activityFilter) {
    return load(() -> new IntegerData(storage.getActivitiesCountByFilter(viewerIdentity, activityFilter)))
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnUserActivities(ownerIdentity,
                                                                                                        baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnUserActivities(final Identity ownerIdentity,
                                                          final ExoSocialActivity baseActivity,
                                                          final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnUserActivities(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnUserActivities(ownerIdentity,
                                                                                                        baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnUserActivities(final Identity ownerIdentity,
                                                          final ExoSocialActivity baseActivity,
                                                          final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnUserActivities(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivityFeed(final Identity ownerIdentity, final int offset, final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivityFeed(ownerIdentity,
                                                                                                            offset,
                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public List<String> getActivityIdsFeed(final Identity ownerIdentity, final int offset, final int limit) {
    //
    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getActivityIdsFeed(ownerIdentity, offset, limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  });

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

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitesOnActivityFeed(ownerIdentity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnActivityFeed(ownerIdentity,
                                                                                                      baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnActivityFeed(final Identity ownerIdentity,
                                                        final ExoSocialActivity baseActivity,
                                                        final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getNewerOnActivityFeed(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnActivityFeed(ownerIdentity,
                                                                                                      baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnActivityFeed(final Identity ownerIdentity,
                                                        final ExoSocialActivity baseActivity,
                                                        final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getOlderOnActivityFeed(ownerIdentity,
                                                                                                                   baseActivity,
                                                                                                                   limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(final Identity ownerIdentity, final int offset, final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getActivitiesOfConnections(ownerIdentity,
                                                                                                                     offset,
                                                                                                                     limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  @Override
  public List<String> getActivityIdsOfConnections(final Identity ownerIdentity, final int offset, final int limit) {
    //
    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getActivityIdsOfConnections(ownerIdentity,
                                                                                                             offset,
                                                                                                             limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  });

    //
    return buildActivityIds(keys);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfActivitiesOfConnections(final Identity ownerIdentity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitiesOfConnections(ownerIdentity));
                                      }
                                    })
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

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity,
                                                                                                                 baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(final Identity ownerIdentity,
                                                                   final ExoSocialActivity baseActivity,
                                                                   final long limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnActivitiesOfConnections(ownerIdentity,
                                                                                                                            baseActivity,
                                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity,
                                                                                                                 baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(final Identity ownerIdentity,
                                                                   final ExoSocialActivity baseActivity,
                                                                   final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnActivitiesOfConnections(ownerIdentity,
                                                                                                                            baseActivity,
                                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getUserSpacesActivities(final Identity ownerIdentity, final int offset, final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getUserSpacesActivities(ownerIdentity,
                                                                                                                    offset,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  @Override
  public List<String> getUserSpacesActivityIds(final Identity ownerIdentity, final int offset, final int limit) {
    //
    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got = storage.getUserSpacesActivityIds(ownerIdentity,
                                                                                                          offset,
                                                                                                          limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  });

    //
    return buildActivityIds(keys);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfUserSpacesActivities(final Identity ownerIdentity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfUserSpacesActivities(ownerIdentity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity,
                                                                                                              baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(final Identity ownerIdentity,
                                                                final ExoSocialActivity baseActivity,
                                                                final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnUserSpacesActivities(ownerIdentity,
                                                                                                                         baseActivity,
                                                                                                                         limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnUserSpacesActivities(ownerIdentity,
                                                                                                              baseActivity));
                                      }
                                    })
                               .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(final Identity ownerIdentity,
                                                                final ExoSocialActivity baseActivity,
                                                                final int limit) {

    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnUserSpacesActivities(ownerIdentity,
                                                                                                                         baseActivity,
                                                                                                                         limit);
                                                      return buildIds(got);
                                                    }
                                                  });

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

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getComments(existingActivity,
                                                                                                        loadSubComments,
                                                                                                        offset,
                                                                                                        limit,
                                                                                                        sortDescending);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfComments(final ExoSocialActivity existingActivity) {

    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfComments(existingActivity));
                                      }
                                    })
                               .build();
  }

  @Override
  public int getNumberOfAllComments(String activityId) {
    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfAllComments(activityId));
      }
    }).build();
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


    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnActivityFeed(ownerIdentity, sinceTime));
      }
    }).build();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserActivities(final Identity ownerIdentity, final Long sinceTime) {


    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnUserActivities(ownerIdentity, sinceTime));
      }
    }).build();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivitiesOfConnections(final Identity ownerIdentity, final Long sinceTime) {


    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, sinceTime));
      }
    }).build();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserSpacesActivities(final Identity ownerIdentity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity,
                                                                              sinceTime));
      }
    }).build();
  }

  @Override
  public int getNumberOfSpaceActivities(final Identity spaceIdentity) {
    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfSpaceActivities(spaceIdentity));
                                      }
                                    })
                               .build();
  }

  @Override
  public int getNumberOfSpaceActivitiesForUpgrade(final Identity spaceIdentity) {
    //

    //
    IntegerData countData = load(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfSpaceActivitiesForUpgrade(spaceIdentity));
                                                       }
                                                     });


    return countData.build();
  }

  @Override
  public List<ExoSocialActivity> getSpaceActivities(final Identity ownerIdentity, final int offset, final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getSpaceActivities(ownerIdentity,
                                                                                                               offset,
                                                                                                               limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public List<String> getSpaceActivityIds(final Identity spaceIdentity, final int offset, final int limit) {
    //
    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<String> got =
                                                                       storage.getSpaceActivityIds(spaceIdentity, offset, limit);
                                                      return buildActivityIds(got);
                                                    }
                                                  });

    //
    return buildActivityIds(keys);
  }

  @Override
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(final Identity ownerIdentity, final int offset, final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getSpaceActivitiesForUpgrade(ownerIdentity,
                                                                                                                       offset,
                                                                                                                       limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(final Identity posterIdentity,
                                                       final int offset,
                                                       final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivitiesByPoster(posterIdentity,
                                                                                                                  offset,
                                                                                                                  limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(final Identity posterIdentity,
                                                       final int offset,
                                                       final int limit,
                                                       final String... activityTypes) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivitiesByPoster(posterIdentity,
                                                                                                                  offset,
                                                                                                                  limit,
                                                                                                                  activityTypes);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfActivitiesByPoster(final Identity posterIdentity) {
    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitiesByPoster(posterIdentity));
                                      }
                                    })
                               .build();
  }

  @Override
  public int getNumberOfActivitiesByPoster(final Identity ownerIdentity, final Identity viewerIdentity) {
    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfActivitiesByPoster(ownerIdentity,
                                                                                                     viewerIdentity));
                                      }
                                    })
                               .build();
  }

  @Override
  public List<ExoSocialActivity> getNewerOnSpaceActivities(final Identity ownerIdentity,
                                                           final ExoSocialActivity baseActivity,
                                                           final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getNewerOnSpaceActivities(ownerIdentity,
                                                                                                                    baseActivity,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(final Identity ownerIdentity,
                                               final ExoSocialActivity baseActivity) {
    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfNewerOnSpaceActivities(ownerIdentity,
                                                                                                         baseActivity));
                                      }
                                    })
                               .build();
  }

  @Override
  public List<ExoSocialActivity> getOlderOnSpaceActivities(final Identity ownerIdentity,
                                                           final ExoSocialActivity baseActivity,
                                                           final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getOlderOnSpaceActivities(ownerIdentity,
                                                                                                                    baseActivity,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(final Identity ownerIdentity,
                                               final ExoSocialActivity baseActivity) {
    //

    //
    return load(
                                    new ServiceContext<IntegerData>() {
                                      public IntegerData execute() {
                                        return new IntegerData(storage.getNumberOfOlderOnSpaceActivities(ownerIdentity,
                                                                                                         baseActivity));
                                      }
                                    })
                               .build();
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(final Identity ownerIdentity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity,
                                                                              sinceTime));
      }
    }).build();
  }

  public List<ExoSocialActivity> getNewerFeedActivities(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerFeedActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  public List<ExoSocialActivity> getNewerSpaceActivities(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerSpaceActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getNewerUserActivities(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerUserActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getNewerUserSpacesActivities(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerUserSpacesActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerActivitiesOfConnections(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivities(final Identity owner,
                                               final Identity viewer,
                                               final long offset,
                                               final long limit) throws ActivityStorageException {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getActivities(owner,
                                                                                                          viewer,
                                                                                                          offset,
                                                                                                          limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);

  }

  @Override
  public List<ExoSocialActivity> getOlderFeedActivities(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderFeedActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderUserActivities(final Identity ownerIdentity, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderUserActivities(ownerIdentity, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderUserSpacesActivities(final Identity ownerIdentity,
                                                              final Long sinceTime,
                                                              final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderUserSpacesActivities(ownerIdentity, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderActivitiesOfConnections(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderSpaceActivities(final Identity owner, final Long sinceTime, final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderSpaceActivities(owner, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public int getNumberOfOlderOnActivityFeed(final Identity ownerIdentity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnActivityFeed(ownerIdentity, sinceTime));
      }
    }).build();
  }

  @Override
  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnUserActivities(ownerIdentity, sinceTime));
      }
    }).build();
  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(final Identity ownerIdentity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, sinceTime));
      }
    }).build();
  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(final Identity ownerIdentity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, sinceTime));
      }
    }).build();
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(final Identity ownerIdentity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderOnSpaceActivities(ownerIdentity, sinceTime));
      }
    }).build();
  }

  @Override
  public List<ExoSocialActivity> getNewerComments(final ExoSocialActivity existingActivity,
                                                  final Long sinceTime,
                                                  final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getNewerComments(existingActivity, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getOlderComments(final ExoSocialActivity existingActivity,
                                                  final Long sinceTime,
                                                  final int limit) {

    ListActivitiesData keys = load(new ServiceContext<ListActivitiesData>() {
      public ListActivitiesData execute() {
        List<ExoSocialActivity> got = storage.getOlderComments(existingActivity, sinceTime, limit);
        return buildIds(got);
      }
    });

    return buildActivities(keys);
  }

  @Override
  public int getNumberOfNewerComments(final ExoSocialActivity existingActivity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfNewerComments(existingActivity, sinceTime));
      }
    }).build();
  }

  @Override
  public int getNumberOfOlderComments(final ExoSocialActivity existingActivity, final Long sinceTime) {

    return load(new ServiceContext<IntegerData>() {
      public IntegerData execute() {
        return new IntegerData(storage.getNumberOfOlderComments(existingActivity, sinceTime));
      }
    }).build();
  }

  @Override
  public List<ExoSocialActivity> getUserActivitiesForUpgrade(final Identity owner,
                                                             final long offset,
                                                             final long limit) throws ActivityStorageException {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getUserActivitiesForUpgrade(owner,
                                                                                                                        offset,
                                                                                                                        limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByFilter(Identity viewerIdentity,
                                                       ActivityFilter activityFilter,
                                                       long offset,
                                                       long limit) {
    ListActivitiesData keys = load(() -> {
      List<String> got = storage.getActivityIdsByFilter(viewerIdentity, activityFilter, offset, limit);
      return buildActivityIds(got);
    });
    return buildActivities(keys);
  }

  @Override
  public List<String> getActivityIdsByFilter(Identity viewerIdentity, ActivityFilter activityFilter, long offset, long limit) {
    ListActivitiesData keys = load(() -> {
      List<String> got = storage.getActivityIdsByFilter(viewerIdentity, activityFilter, offset, limit);
      return buildActivityIds(got);
    });
    return buildActivityIds(keys);
  }

  @Override
  public int getNumberOfUserActivitiesForUpgrade(final Identity owner) throws ActivityStorageException {
    //

    //
    IntegerData countData = load(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfUserActivitiesForUpgrade(owner));
                                                       }
                                                     });

    //

    //
    return countData.build();

  }

  @Override
  public List<ExoSocialActivity> getActivityFeedForUpgrade(final Identity ownerIdentity,
                                                           final int offset,
                                                           final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getActivityFeedForUpgrade(ownerIdentity,
                                                                                                                    offset,
                                                                                                                    limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfActivitesOnActivityFeedForUpgrade(final Identity ownerIdentity) {
    //

    //
    IntegerData countData = load(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfActivitesOnActivityFeedForUpgrade(ownerIdentity));
                                                       }
                                                     });

    //

    //
    return countData.build();
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnectionsForUpgrade(final Identity ownerIdentity,
                                                                      final int offset,
                                                                      final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getActivitiesOfConnectionsForUpgrade(ownerIdentity,
                                                                                                                               offset,
                                                                                                                               limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfActivitiesOfConnectionsForUpgrade(final Identity ownerIdentity) {
    //

    //
    IntegerData countData = load(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfActivitiesOfConnectionsForUpgrade(ownerIdentity));
                                                       }
                                                     });

    //

    //
    return countData.build();
  }

  @Override
  public List<ExoSocialActivity> getUserSpacesActivitiesForUpgrade(final Identity ownerIdentity,
                                                                   final int offset,
                                                                   final int limit) {
    //

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got =
                                                                                  storage.getUserSpacesActivitiesForUpgrade(ownerIdentity,
                                                                                                                            offset,
                                                                                                                            limit);
                                                      return buildIds(got);
                                                    }
                                                  });

    //
    return buildActivities(keys);
  }

  @Override
  public int getNumberOfUserSpacesActivitiesForUpgrade(final Identity ownerIdentity) {
    //

    //
    IntegerData countData = load(
                                                     new ServiceContext<IntegerData>() {
                                                       public IntegerData execute() {
                                                         return new IntegerData(storage.getNumberOfUserSpacesActivitiesForUpgrade(ownerIdentity));
                                                       }
                                                     });


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

    //
    ListActivitiesData keys = load(
                                                  new ServiceContext<ListActivitiesData>() {
                                                    public ListActivitiesData execute() {
                                                      List<ExoSocialActivity> got = storage.getSubComments(comment);
                                                      return buildIds(got);
                                                    }
                                                  });

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

  @Override
  public List<Long> getActivityCategoryIds(long spaceIdentityId) {
    return storage.getActivityCategoryIds(spaceIdentityId);
  }

  @Override
  public List<Long> getActivityCategoryIds(Set<Long> streamFeedOwnerIds) {
    return storage.getActivityCategoryIds(streamFeedOwnerIds);
  }

  public void clearActivityCachedByAttachmentId(String attachmentId) {
    try {
      exoActivityCache.select(new ActivityAttachmentCacheSelector(attachmentId));
    } catch (Exception e) {
      LOG.error("Error clearing cache of activities having attachment with id {}", attachmentId, e);
    }
  }

  public boolean clearActivityByMetadataObject(String objectType, String objectId) {
    ActivityMetadataCacheSelector metadataCacheSelector = new ActivityMetadataCacheSelector(objectType, objectId);
    try {
      exoActivityCache.select(metadataCacheSelector);
    } catch (Exception e) {
      LOG.error("Error clearing cache of activities having using metadata object {}/{}", objectType, objectId, e);
    }
    return metadataCacheSelector.isFound();
  }

}
