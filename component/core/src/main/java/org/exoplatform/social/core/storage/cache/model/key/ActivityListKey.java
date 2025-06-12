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
package org.exoplatform.social.core.storage.cache.model.key;

import java.util.Arrays;
import java.util.Objects;

import org.exoplatform.social.core.activity.ActivityFilter;

/**
 * Immutable activity count key.
 * This key is used to cache the activity count.
 * 
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityListKey implements CacheKey {
  private static final long serialVersionUID = -2153747306033203041L;

  private IdentityKey key;
  
  private IdentityKey viewerKey;
  
  private ActivityKey activityKey;

  private String baseId;

  private ActivityType type;

  private transient ActivityFilter activityFilter;

  private Long time;
  
  private String[] activityTypes;

  public ActivityListKey(final IdentityKey key, final ActivityType type) {
    this.key = key;
    this.type = type;
  }
  
  public ActivityListKey(final IdentityKey key, final ActivityType type, final String...activityTypes) {
    this.key = key;
    this.type = type;
    this.activityTypes = activityTypes;
  }
  
  public ActivityListKey(final IdentityKey key, final IdentityKey viewerKey, final ActivityType type) {
    this.key = key;
    this.type = type;
    this.viewerKey = viewerKey;
  }

  public ActivityListKey(final IdentityKey key, final String baseId, final ActivityType type) {
    this.key = key;
    this.baseId = baseId;
    this.type = type;
  }

  public ActivityListKey(final IdentityKey key, final Long time, final ActivityType type) {
    this.key = key;
    this.time = time;
    this.type = type;
  }
  
  public ActivityListKey(final ActivityKey activityKey, final Long time, final ActivityType type) {
    this.activityKey = activityKey;
    this.time = time;
    this.type = type;
  }
  
  public ActivityListKey(final String baseId, final ActivityType type) {
    this.baseId = baseId;
    this.type = type;
  }

  public ActivityListKey(IdentityKey key, ActivityFilter activityFilter) {
    this.key = key;
    this.activityFilter = activityFilter;
  }

  public IdentityKey getKey() {
    return key;
  }
  
  public ActivityKey getActivityKey() {
    return activityKey;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ActivityListKey)) {
      return false;
    }

    ActivityListKey that = (ActivityListKey) o;
    return (baseId != null ? !baseId.equals(that.baseId) : that.baseId != null)
           && (key != null ? !key.equals(that.key) : that.key != null)
           && (activityKey != null ? !activityKey.equals(that.activityKey) : that.activityKey != null)
           && (activityTypes != null ? !Arrays.equals(activityTypes, that.activityTypes) : that.activityTypes != null)
           && (activityFilter != null ? !activityFilter.equals(that.activityFilter) : that.activityFilter != null)
           && (type != that.type)
           && (time != that.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activityKey,
                        activityTypes,
                        activityFilter,
                        viewerKey,
                        baseId,
                        baseId,
                        type,
                        time);
  }

}