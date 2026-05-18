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
package io.meeds.social.coediting.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import io.meeds.social.coediting.model.CoeditionObjectKey;
import io.meeds.social.coediting.model.CoeditionObjectLock;

@SpringBootTest(classes = {
  CoeditingService.class,
})
@RunWith(SpringRunner.class)
public class CoeditingServiceTest {

  private static final String                               REVISION = "revision";

  private static final String                               USERNAME = "username";

  @MockitoBean
  private SettingService                                    settingService;

  @MockitoBean
  private CacheService                                      cacheService;

  @Autowired
  private CoeditingService                                  coeditingService;

  @Mock
  private ExoCache<CoeditionObjectKey, CoeditionObjectLock> lockCache;

  @Mock
  private CoeditionObjectKey                                key;

  @Mock
  private CoeditionObjectLock                               lock;

  @Value("${meeds.coediting.lockPeriod:20}")
  private long                                              lockPeriod;

  @Before
  public void setup() {
    coeditingService.setLockCache(lockCache);
  }

  @Test
  public void getLockHoldersEmpty() {
    List<String> lockHolders = coeditingService.getLockHolders(key);
    assertNotNull(lockHolders);
    assertEquals(0, lockHolders.size());
  }

  @Test
  public void getLockHoldersWithOutdatedLocks() {
    when(lockCache.get(key)).thenReturn(lock);
    when(lock.getUsers()).thenReturn(Collections.singletonMap(USERNAME, System.currentTimeMillis() - 20001));
    List<String> lockHolders = coeditingService.getLockHolders(key);
    assertNotNull(lockHolders);
    assertEquals(0, lockHolders.size());
  }

  @Test
  public void getLockHolders() {
    when(lockCache.get(key)).thenReturn(lock);
    when(lock.getUsers()).thenReturn(Collections.singletonMap(USERNAME, System.currentTimeMillis()));
    List<String> lockHolders = coeditingService.getLockHolders(key);
    assertNotNull(lockHolders);
    assertEquals(1, lockHolders.size());
  }

  @Test
  public void setLockWithNotExisting() {
    coeditingService.setLock(USERNAME, key, REVISION);
    verify(lockCache).put(eq(key),
                          argThat(l -> l.getUsers().size() == 1
                                       && l.getUsers().get(USERNAME) != null
                                       && l.getUsers().get(USERNAME) > (System.currentTimeMillis() - 1000)));
    verify(settingService).set(any(), any(), any(), any());
  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setLockWithExisting() {
    when(lockCache.get(key)).thenReturn(lock);
    Map<String, Long> users = new ConcurrentHashMap<>();
    users.put(USERNAME, System.currentTimeMillis());
    users.put(USERNAME + "2", System.currentTimeMillis() - 20001);
    when(lock.getUsers()).thenReturn(users);
    when(settingService.get(any(),
                            any(),
                            any())).thenReturn((SettingValue) SettingValue.create("{\"revision\": \"" + REVISION + "2" + "\"}"));

    coeditingService.setLock(USERNAME, key, REVISION);

    verify(lockCache).put(key, lock);
    verify(settingService).set(any(), any(), any(), any());
    assertEquals(1, lock.getUsers().size());
  }

}
