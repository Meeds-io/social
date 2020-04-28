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

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.social.common.lifecycle.AbstractListenerPlugin;

/**
 * Convenience class to write and wire {@link ProfileListener} plugin. <br>
 * This base class is a valid {@link ComponentPlugin} and implements {@link ProfileListener}.
 * @see org.exoplatform.social.core.manager.IdentityManager#registerProfileListener(ProfileListener)
 */
public abstract class ProfileListenerPlugin extends AbstractListenerPlugin implements ProfileListener {

  /**
   * {@inheritDoc}
   */
  public void aboutMeUpdated(ProfileLifeCycleEvent event) {}
  
  /**
   * {@inheritDoc}
   */
  public abstract void avatarUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void bannerUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {}

  /**
   * {@inheritDoc}
   */
  public abstract void contactSectionUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void experienceSectionUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {}
  
  /**
   * {@inheritDoc}
   */
  public abstract void createProfile(ProfileLifeCycleEvent event);

}
