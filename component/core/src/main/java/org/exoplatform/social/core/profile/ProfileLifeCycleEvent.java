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
package org.exoplatform.social.core.profile;

import org.exoplatform.social.common.lifecycle.LifeCycleEvent;
import org.exoplatform.social.core.identity.model.Profile;


/**
 * event propagated along the {@link org.exoplatform.social.core.profile.ProfileLifeCycle}
 * @see org.exoplatform.social.core.profile.ProfileListener
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ProfileLifeCycleEvent extends LifeCycleEvent<String, Profile> {

  public enum Type {ABOUT_ME, AVATAR_UPDATED, BASIC_UPDATED, CONTACT_UPDATED, EXPERIENCE_UPDATED, HEADER_UPDATED, CREATED, BANNER_UPDATED}

  private Type type;

  public ProfileLifeCycleEvent(Type type, String user, Profile profile) {
    super(user, profile);
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  /**
   * username of the profile updated
   * @return
   */
  public String getUsername() {
    return source;
  }

  /**
   * actual profile section;
   * @return
   */
  public Profile getProfile() {
    return payload;
  }

}
