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

package org.exoplatform.social.core.storage.cache.selector;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.key.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityCacheSelector extends CacheSelector<CacheKey, Object> {

  private String provider;

  public IdentityCacheSelector(final String provider) {

    if (StringUtils.isBlank(provider)) {
      throw new IllegalArgumentException("Provider id is empty");
    }

    this.provider = provider;
  }

  @Override
  public boolean select(final CacheKey key, final ObjectCacheInfo<? extends Object> ocinfo) {

    if (key == null) {
      return false;
    }

    if (!super.select(key, ocinfo)) {
      return false;
    }

    if (key instanceof IdentityFilterKey) {
      return provider.equals(((IdentityFilterKey)key).getProviderId());
    }

    if (key instanceof ListIdentitiesKey) {
      return provider.equals(((ListIdentitiesKey)key).getKey().getProviderId());
    }

    return false;

  }

}
