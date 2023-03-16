/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.core.space;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import lombok.Getter;

public enum SpaceAccessType {

  INVITED_SPACE("social.space.access.invited-space")
  {
    @Override
    public boolean doCheck(String remoteId, Space space) {
      return space != null && getSpaceService().isInvitedUser(space, remoteId);
    }
  },
  JOIN_SPACE("social.space.access.join-space")
  {
    @Override
    public boolean doCheck(String remoteId, Space space) {
      return space != null && Space.OPEN.equals(space.getRegistration());
    }
  },
  REQUESTED_JOIN_SPACE("social.space.access.requested-join-space")
  {
    @Override
    public boolean doCheck(String remoteId, Space space) {
      return space != null && Space.VALIDATION.equals(space.getRegistration()) && getSpaceService().isPendingUser(space, remoteId);
    }
  },
  REQUEST_JOIN_SPACE("social.space.access.request-join-space")
  {
    @Override
    public boolean doCheck(String remoteId, Space space) {
      return space != null && Space.VALIDATION.equals(space.getRegistration());
    }
  },
  CLOSED_SPACE("social.space.access.closed-space")
  {
    @Override
    public boolean doCheck(String remoteId, Space space) {
      return space != null && Space.CLOSED.equals(space.getRegistration()) && !Space.HIDDEN.equals(space.getVisibility());
    }
  },
  SPACE_NOT_FOUND("social.space.access.space-not-found")
  {
    @Override
    public boolean doCheck(String remoteId, Space space) {
      return space == null || (Space.CLOSED.equals(space.getRegistration()) && Space.HIDDEN.equals(space.getVisibility()));
    }
  };

  @Getter
  private final String name;

  private SpaceAccessType(String name) {
    this.name = name;
  }

  private static SpaceService getSpaceService() {
    return PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
  }

  @Override
  public String toString() {
    return name;
  }

  public static final String ACCESSED_TYPE_KEY               = "social.accessed.space.type.key";

  public static final String ACCESSED_SPACE_ID_KEY           = "social.accessed.space.id.key";

  public static final String ACCESSED_SPACE_PRETTY_NAME_KEY  = "social.accessed.space.prettyName.key";

  public static final String ACCESSED_SPACE_DISPLAY_NAME_KEY = "social.accessed.space.displayName.key";

  public static final String ACCESSED_SPACE_REQUEST_PATH_KEY = "social.accessed.space.request.path.key";

  public abstract boolean doCheck(String remoteId, Space space);

}
