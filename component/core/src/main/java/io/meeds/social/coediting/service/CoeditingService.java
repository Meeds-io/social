/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import io.meeds.social.coediting.model.CoeditionObjectDraft;
import io.meeds.social.coediting.model.CoeditionObjectKey;
import io.meeds.social.coediting.model.CoeditionObjectLock;
import io.meeds.social.util.JsonUtils;

import jakarta.annotation.PostConstruct;
import lombok.Setter;

@Service
public class CoeditingService {

  private static final Scope                                COEDITION_SCOPE = Scope.APPLICATION.id("Coedition");

  @Autowired
  private SettingService                                    settingService;

  @Autowired
  private CacheService                                      cacheService;

  @Value("${meeds.coediting.lockPeriod:20}")
  private long                                              lockPeriod;

  @Setter
  private ExoCache<CoeditionObjectKey, CoeditionObjectLock> lockCache;

  @PostConstruct
  public void init() {
    lockCache = cacheService.getCacheInstance("portal.coediting");
    if (lockCache != null) {
      lockCache.setLiveTime(lockPeriod);
    }
  }

  public List<String> getLockHolders(CoeditionObjectKey key) {
    CoeditionObjectLock lock = lockCache.get(key);
    return lock == null ? Collections.emptyList() :
                        lock.getUsers()
                            .entrySet()
                            .stream()
                            .map(e -> {
                              if (isLockTimeValid(e.getValue())) {
                                return e.getKey();
                              } else {
                                return null;
                              }
                            })
                            .filter(Objects::nonNull)
                            .toList();
  }

  public CoeditionObjectDraft setLock(String username, CoeditionObjectKey key, String revision) {
    CoeditionObjectLock lock = lockCache.get(key);
    if (lock == null) {
      lock = new CoeditionObjectLock();
    } else {
      cleanOutdatedLocks(lock);
    }
    lock.getUsers().put(username, System.currentTimeMillis());
    lockCache.put(key, lock);

    CoeditionObjectDraft draft = getRevision(username, key);
    if (draft != null && StringUtils.equals(draft.getRevision(), revision)) {
      return null;
    } else {
      setRevision(username, key, revision);
      return draft;
    }
  }

  public CoeditionObjectDraft getRevision(String username, CoeditionObjectKey key) {
    SettingValue<?> value = settingService.get(Context.USER.id(username), COEDITION_SCOPE, String.valueOf(key.hashCode()));
    return value == null
           || value.getValue() == null ? null : JsonUtils.fromJsonString(value.getValue().toString(), CoeditionObjectDraft.class);
  }

  public void setRevision(String username, CoeditionObjectKey key, String versionReference) {
    settingService.set(Context.USER.id(username),
                       COEDITION_SCOPE,
                       String.valueOf(key.hashCode()),
                       SettingValue.create(JsonUtils.toJsonString(new CoeditionObjectDraft(versionReference, System.currentTimeMillis()))));
  }

  public void removeRevision(String username, CoeditionObjectKey key) {
    settingService.remove(Context.USER.id(username),
                          COEDITION_SCOPE,
                          String.valueOf(key.hashCode()));
  }

  private boolean isLockTimeValid(long lockTime) {
    return (lockTime + lockPeriod * 1000) > System.currentTimeMillis();
  }

  private void cleanOutdatedLocks(CoeditionObjectLock lock) {
    Iterator<Entry<String, Long>> userLockIterator = lock.getUsers().entrySet().iterator();
    while (userLockIterator.hasNext()) {
      Map.Entry<String, Long> e = userLockIterator.next();
      if (!isLockTimeValid(e.getValue())) {
        userLockIterator.remove();
      }
    }
  }

}
