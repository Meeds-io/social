/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImpl;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ListActivitiesData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityListKey;
import org.exoplatform.social.core.storage.cache.model.key.ListActivitiesKey;

public class CachedActivityStorageTest {

  private static final int                                     COUNT                 = 7;

  /**
   * The reads that do not go through the activity list and count caches: they
   * read single activities by id, processors or scheduled ids. Activities read
   * by a list of ids are excluded by signature in {@link #isReadByIds(Method)},
   * since the viewer's stream shares their method name.
   */
  private static final Set<String>                             NOT_A_LIST_READ       = Set.of("getActivity",
                                                                                              "getParentActivity",
                                                                                              "getActivityProcessors",
                                                                                              "getScheduledActivityIds");

  private RDBMSActivityStorageImpl                             storage;

  private ExoCache<ActivityListKey, IntegerData>               activitiesCountCache;

  private ExoCache<ListActivitiesKey, ListActivitiesData>      activitiesCache;

  private CachedActivityStorage                                cachedActivityStorage;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    storage = mock(RDBMSActivityStorageImpl.class, invocation -> {
      Class<?> returnType = invocation.getMethod().getReturnType();
      if (returnType == int.class) {
        return COUNT;
      } else if (List.class.isAssignableFrom(returnType)) {
        return Collections.emptyList();
      } else {
        return null;
      }
    });
    activitiesCountCache = mock(ExoCache.class);
    activitiesCache = mock(ExoCache.class);
    SocialStorageCacheService cacheService = mock(SocialStorageCacheService.class);
    when(cacheService.getActivityCache()).thenReturn(mock(ExoCache.class));
    when(cacheService.getActivitiesCountCache()).thenReturn(activitiesCountCache);
    when(cacheService.getActivitiesCache()).thenReturn(activitiesCache);
    cachedActivityStorage = new CachedActivityStorage(storage, cacheService);
  }

  /**
   * Every activity list and count is read from the storage on each call and
   * never written to the list and count caches: an {@link ActivityListKey} is
   * never equal to another one, so an entry keyed by it would never be read
   * back.
   */
  @Test
  public void testListsAndCountsAreReadFromTheStorageOnEachCall() throws Exception {
    List<Method> reads = Arrays.stream(ActivityStorage.class.getMethods())
                               .filter(m -> !Modifier.isStatic(m.getModifiers()))
                               .filter(m -> m.getReturnType() == int.class || m.getReturnType() == List.class)
                               .filter(m -> m.getName().startsWith("get"))
                               .filter(m -> !NOT_A_LIST_READ.contains(m.getName()))
                               .filter(m -> !isReadByIds(m))
                               .toList();
    assertFalse(reads.isEmpty());

    List<String> cached = new ArrayList<>();
    for (Method read : reads) {
      clearInvocations(storage);
      Object[] args = Arrays.stream(read.getParameterTypes()).map(this::argument).toArray();
      Object first = read.invoke(cachedActivityStorage, args);
      Object second = read.invoke(cachedActivityStorage, args);

      long storageCalls = mockingDetails(storage).getInvocations().size();
      if (storageCalls != 2 || (read.getReturnType() == int.class && !(first.equals(COUNT) && second.equals(COUNT)))) {
        cached.add(read.getName() + Arrays.toString(read.getParameterTypes()));
      }
    }
    assertEquals(Collections.emptyList(), cached);
    verifyNoInteractions(activitiesCache, activitiesCountCache);
  }

  private static boolean isReadByIds(Method method) {
    return method.getName().equals("getActivities") && Arrays.equals(method.getParameterTypes(), new Class<?>[] { List.class });
  }

  private Object argument(Class<?> type) {
    if (type == int.class) {
      return 10;
    } else if (type == long.class) {
      return 10L;
    } else if (type == boolean.class) {
      return false;
    } else if (type == Long.class) {
      return 1L;
    } else if (type == String.class) {
      return "2";
    } else if (type == Identity.class) {
      Identity identity = new Identity(OrganizationIdentityProvider.NAME, "john");
      identity.setId("1");
      return identity;
    } else if (type == ExoSocialActivity.class) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setId("2");
      return activity;
    } else if (type == ActivityFilter.class) {
      return new ActivityFilter();
    } else if (List.class.isAssignableFrom(type)) {
      return Collections.emptyList();
    } else if (type.isArray()) {
      return java.lang.reflect.Array.newInstance(type.getComponentType(), 0);
    } else if (type.isEnum()) {
      return type.getEnumConstants()[0];
    } else {
      return mock(type);
    }
  }

}
