/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.storage.cache.model.key;

import org.exoplatform.social.core.identity.model.ActiveIdentityFilter;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 8, 2014  
 */
public class ActiveIdentityKey implements CacheKey {
  private static final long serialVersionUID = -1731263881104519519L;

  private final int days;
  
  private final String userGroup;

  public ActiveIdentityKey(final int days) {
    this.days = days;
    this.userGroup = null;
  }
  
  public ActiveIdentityKey(final ActiveIdentityFilter filter) {
    this.days = filter.getDays();
    this.userGroup = filter.getUserGroups();
  }
  
  public ActiveIdentityKey(final String userGroup) {
    this.days = 0;
    this.userGroup = userGroup;
  }
  
  public ActiveIdentityKey(final int days, final String userGroup) {
    this.days = days;
    this.userGroup = userGroup;
  }

  public int getDays() {
    return days;
  }
  
  public String getUserGroup() {
    return userGroup;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ActiveIdentityKey)) {
      return false;
    }

    ActiveIdentityKey that = (ActiveIdentityKey) o;

    if (days != that.days) {
      return false;
    }
    
    if (userGroup != null ? !userGroup.equals(that.userGroup) : that.userGroup != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = days;
    result = 31 * result + (userGroup != null ? userGroup.hashCode() : 0);
    return result;
  }

}
