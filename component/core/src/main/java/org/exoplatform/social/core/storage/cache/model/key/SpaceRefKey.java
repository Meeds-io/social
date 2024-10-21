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

package org.exoplatform.social.core.storage.cache.model.key;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class SpaceRefKey implements CacheKey {

  private static final long serialVersionUID = -7953413006239399161L;

  private final String      prettyName;

  private final String      groupId;

  public SpaceRefKey(final String prettyName) {
    this(prettyName, null);
  }

  public SpaceRefKey(final String prettyName, final String groupId) {
    this.prettyName = prettyName;
    this.groupId = groupId;
  }

}
