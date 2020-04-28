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

package org.exoplatform.social.core.storage.cache.selector;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.key.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipCacheSelector extends CacheSelector<CacheKey, Object> {

  private String[] target;

  public RelationshipCacheSelector(final String... target) {
    this.target = target;
  }

  @Override
  public boolean select(final CacheKey key, final ObjectCacheInfo<? extends Object> ocinfo) {

    if (!super.select(key, ocinfo)) {
      return false;
    }

    if (key instanceof ListRelationshipsKey) {
      return select((ListRelationshipsKey) key);
    }

    if (key instanceof RelationshipCountKey) {
      return select((RelationshipCountKey) key);
    }

    return false;

  }

  private boolean select(final ListRelationshipsKey key) {

    if (key.getKey() instanceof IdentityKey) {
      String id = ((IdentityKey) key.getKey()).getId();
      return id.equals(target[0]) || id.equals(target[1]);
    }

    return true;

  }

  private boolean select(final RelationshipCountKey key) {

    if (key.getKey() instanceof IdentityKey) {
      String id = ((IdentityKey) key.getKey()).getId();
      return id.equals(target[0]) || id.equals(target[1]);
    } else if (key.getKey() instanceof RelationshipIdentityKey) {
      RelationshipIdentityKey idKey = ((RelationshipIdentityKey) key.getKey());
      return StringUtils.equals(target[0], idKey.getIdentityId1()) || StringUtils.equals(target[1], idKey.getIdentityId1())
          || StringUtils.equals(target[0], idKey.getIdentityId2()) || StringUtils.equals(target[1], idKey.getIdentityId2());
    }

    return true;

  }

}
