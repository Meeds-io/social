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

package org.exoplatform.social.core.storage.cache;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.storage.cache.loader.CacheLoader;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.key.CacheKey;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public enum CacheType {

  //
  IDENTITY("social.IdentityCache"),
  IDENTITY_INDEX("social.IdentityIndexCache"),
  PROFILE("social.ProfileCache"),
  IDENTITIES_COUNT("social.IdentitiesCountCache"),
  IDENTITIES("social.IdentitiesCache"),
  ACTIVE_IDENTITIES("social.ActiveIdentitiesCache"),

  //
  RELATIONSHIP("social.RelationshipCache"),
  RELATIONSHIP_FROM_IDENTITY("social.RelationshipFromIdentityCache"),
  RELATIONSHIPS_COUNT("social.RelationshipsCountCache"),
  RELATIONSHIPS("social.RelationshipsCache"),
  SUGGESTIONS("social.SuggestionsCache"),

  //
  ACTIVITY("social.ActivityCache"),
  ACTIVITIES_COUNT("social.ActivitiesCountCache"),
  ACTIVITIES("social.ActivitiesCache"),
  
  //
  ACTIVITY_REF("social.ActivityRefCache"),
  ACTIVITIES_REF_COUNT("social.ActivitiesRefCountCache"),
  ACTIVITIES_REF("social.ActivitiesRefCache"),

  //
  SPACE("social.SpaceCache"),
  SPACE_REF("social.SpaceRefCache"),
  SPACES_COUNT("social.SpacesCountCache"),
  SPACES("social.SpacesCache"),
  
  //
  SPACE_SIMPLE("social.SpaceSimpleCache")

  ;

  private final String name;

  private CacheType(final String name) {
    this.name = name;
  }

  public <K extends CacheKey, V extends Serializable> ExoCache<K, V> getFromService(CacheService service) {
    return service.getCacheInstance(name);
  }

  public <K extends CacheKey, V extends Serializable> FutureExoCache<K, V, ServiceContext<V>> createFutureCache(
      ExoCache<K, V> cache) {

    return new FutureExoCache<K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);

  }

}
