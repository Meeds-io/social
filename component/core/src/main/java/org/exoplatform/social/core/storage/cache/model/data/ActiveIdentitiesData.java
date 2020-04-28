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
package org.exoplatform.social.core.storage.cache.model.data;

import java.util.Set;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 8, 2014  
 */
public class ActiveIdentitiesData implements CacheData<Set<String>> {
  private static final long serialVersionUID = 3762098456323779153L;

  private final Set<String> activeUsers;
  

  public ActiveIdentitiesData(final Set<String> activeUsers) {
    this.activeUsers = activeUsers;
  }
  @Override
  public Set<String> build() {
    return this.activeUsers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ActiveIdentitiesData)) return false;

    ActiveIdentitiesData that = (ActiveIdentitiesData) o;

    return activeUsers != null ? activeUsers.equals(that.activeUsers) : that.activeUsers == null;

  }

  @Override
  public int hashCode() {
    return activeUsers != null ? activeUsers.hashCode() : 0;
  }
}
