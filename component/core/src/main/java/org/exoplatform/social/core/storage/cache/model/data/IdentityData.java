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

package org.exoplatform.social.core.storage.cache.model.data;

import org.exoplatform.social.core.identity.model.Identity;

import java.util.Objects;

/**
 * Immutable identity data.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityData implements CacheData<Identity> {
  private static final long serialVersionUID = -4418555926612067560L;

  private final String id;

  private final String providerId;

  private final String remoteId;

  private final boolean isDeleted;
  
  private final boolean isEnabled;

  private final long cacheTime;

  public IdentityData(final Identity identity) {
    if (identity != null) {
      this.id = identity.getId();
      this.providerId = identity.getProviderId();
      this.remoteId = identity.getRemoteId();
      this.isDeleted = identity.isDeleted();
      this.isEnabled = identity.isEnable();
    }
    else {
      this.id = null;
      this.providerId = null;
      this.remoteId = null;
      this.isDeleted = false;
      this.isEnabled = true;
    }
    this.cacheTime = System.currentTimeMillis();
  }

  public String getId() {
    return id;
  }

  public String getProviderId() {
    return providerId;
  }

  public String getRemoteId() {
    return remoteId;
  }

  public Identity build() {

    if (id == null) {
      return null;
    }

    Identity identity = new Identity(id);
    identity.setProviderId(providerId);
    identity.setRemoteId(remoteId);
    identity.setDeleted(isDeleted);
    identity.setEnable(isEnabled);
    identity.setCacheTime(cacheTime);
    return identity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IdentityData that = (IdentityData) o;
    return isDeleted == that.isDeleted &&
            isEnabled == that.isEnabled &&
            Objects.equals(id, that.id) &&
            Objects.equals(providerId, that.providerId) &&
            Objects.equals(remoteId, that.remoteId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, providerId, remoteId, isDeleted, isEnabled);
  }
}
